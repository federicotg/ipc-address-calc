/*
 * Copyright (C) 2014 fede
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fede.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Currency;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.NoSeriesDataFoundException;
import org.fede.calculator.money.series.InterpolationStrategy;
import org.fede.calculator.money.series.JSONDataPoint;
import org.fede.calculator.money.series.JSONSeries;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.SortedMapMoneyAmountSeries;
import org.fede.calculator.money.series.YearMonth;
import org.fede.calculator.web.dto.ExpenseChartSeriesDTO;

/**
 *
 * @author fede
 */
public class Util {

    public static <T> String list(Collection<T> elements) {
        return list(elements, ", ");
    }

    public static <T> String list(Collection<T> elements, String separator) {
        StringBuilder sb = new StringBuilder(elements.size() * 10);
        for (Iterator<T> it = elements.iterator(); it.hasNext();) {
            sb.append(it.next().toString());
            if (it.hasNext()) {
                sb.append(separator);
            }
        }
        return sb.toString();
    }

    public static MoneyAmountSeries sumSeries(List<ExpenseChartSeriesDTO> dtos) throws NoSeriesDataFoundException {

        List<String> seriesNames = new ArrayList<>(dtos.size());
        for (ExpenseChartSeriesDTO dto : dtos) {
            seriesNames.add(dto.getSeriesName());
        }
        return sumSeries(seriesNames.toArray(new String[seriesNames.size()]));

    }

    public static MoneyAmountSeries sumSeries(String... names) throws NoSeriesDataFoundException {
        if (names.length == 0) {
            throw new IllegalArgumentException("You must at least read one series");
        }
        MoneyAmountSeries answer = null;
        for (String seriesName : names) {
            if (seriesName != null && seriesName.length() > 0) {
                MoneyAmountSeries s = readSeries(seriesName);
                answer = answer == null ? s : answer.add(s);
            }
        }
        return answer;
    }

    public static MoneyAmountSeries readSeries(String name) throws NoSeriesDataFoundException {
        try (InputStream is = Util.class.getResourceAsStream("/" + name)) {
            JSONSeries series = new ObjectMapper().readValue(is, JSONSeries.class);

            final InterpolationStrategy strategy = InterpolationStrategy.valueOf(series.getInterpolation());
            SortedMap<YearMonth, MoneyAmount> interpolatedData = new TreeMap<>();
            final Currency currency = Currency.getInstance(series.getCurrency());
            for (JSONDataPoint dp : series.getData()) {
                interpolatedData.put(new YearMonth(dp.getYear(), dp.getMonth()), new MoneyAmount(dp.getValue(), currency));
            }
            if (series.getData().size() != interpolatedData.size()) {
                throw new IllegalArgumentException("Series " + name + " has incorrect year and month sequence.");
            }
            Map<YearMonth, MoneyAmount> extraData = new HashMap<>();
            YearMonth previousKey = interpolatedData.firstKey();
            MoneyAmount previousValue = interpolatedData.get(interpolatedData.firstKey());
            for (Map.Entry<YearMonth, MoneyAmount> entry : interpolatedData.entrySet()) {
                while (previousKey.monthsUntil(entry.getKey()) > 1) {
                    previousKey = previousKey.next();
                    extraData.put(previousKey, strategy.interpolate(previousValue, currency));
                }
                previousValue = entry.getValue();
                previousKey = entry.getKey();
            }
            interpolatedData.putAll(extraData);

            return new SortedMapMoneyAmountSeries(currency, interpolatedData);
        } catch (IOException ioEx) {
            throw new IllegalArgumentException("Could not read series named " + name, ioEx);
        }
    }

}
