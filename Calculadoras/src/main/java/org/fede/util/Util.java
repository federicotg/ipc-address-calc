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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.NoSeriesDataFoundException;
import org.fede.calculator.money.series.ConsultatioDataPoint;
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

    private static final Set<String> CONSULTATIO_SERIES;

    static {
        CONSULTATIO_SERIES = new HashSet<>();
        CONSULTATIO_SERIES.add("fci/CAHORROA.json");
        CONSULTATIO_SERIES.add("fci/CAPLUSA.json");
        CONSULTATIO_SERIES.add("fci/CBAL01.json");
        CONSULTATIO_SERIES.add("fci/CDeudaA.json");
        CONSULTATIO_SERIES.add("fci/CGRO01.json");
        CONSULTATIO_SERIES.add("fci/CPYMESA.json");
        CONSULTATIO_SERIES.add("fci/CRVariable.json");
        CONSULTATIO_SERIES.add("fci/CRentaNacionalA.json");
    }

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
        return sumSeries("USD", seriesNames.toArray(new String[seriesNames.size()]));
    }

    public static MoneyAmountSeries sumSeries(String currency, List<String> names) throws NoSeriesDataFoundException {
        return sumSeries(currency, names.toArray(new String[names.size()]));
    }
    
    public static MoneyAmountSeries sumSeries(String currency, String... names) throws NoSeriesDataFoundException {
        if (names.length == 0) {
            throw new IllegalArgumentException("You must at least read one series");
        }
        MoneyAmountSeries answer = null;
        for (String seriesName : names) {
            if (seriesName != null && seriesName.length() > 0) {
                MoneyAmountSeries s = readSeries(seriesName).exchangeInto(currency);
                answer = answer == null ? s : answer.add(s);
            }
        }
        return answer;
    }

    public static <T> T read(String name, TypeReference<T> typeReference) {
        try (InputStream in = Util.class.getResourceAsStream("/" + name);) {
            ObjectMapper om = new ObjectMapper();

            return om.readValue(in, typeReference);
        } catch (IOException ioEx) {
            throw new IllegalArgumentException("Could not read investments from resource " + name, ioEx);
        }
    }

    public static MoneyAmountSeries readSeries(String name) throws NoSeriesDataFoundException {
        try (InputStream is = Util.class.getResourceAsStream("/" + name)) {

            ObjectMapper om = new ObjectMapper();

            JSONSeries series;
            if (CONSULTATIO_SERIES.contains(name)) {
                series = readConsultatioSeries(is, om);
            } else {
                series = om.readValue(is, JSONSeries.class);
            }
            final InterpolationStrategy strategy = InterpolationStrategy.valueOf(series.getInterpolation());
            SortedMap<YearMonth, MoneyAmount> interpolatedData = new TreeMap<>();
            final String currency = series.getCurrency();
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

    private static JSONSeries readConsultatioSeries(InputStream is, ObjectMapper om) throws IOException {

        List<ConsultatioDataPoint> data = om.readValue(is, new TypeReference<List<ConsultatioDataPoint>>() {
        });

        Map<Pair<Integer, Integer>, List<BigDecimal>> groups = new HashMap<>();

        List<JSONDataPoint> points = new ArrayList<>(data.size());
        Calendar cal = Calendar.getInstance();
        for (ConsultatioDataPoint c : data) {
            cal.setTime(c.getDate());

            Pair<Integer, Integer> key = new Pair<>(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);
            List<BigDecimal> values = groups.get(key);

            if (values == null) {
                values = new ArrayList<>(32);
                groups.put(key, values);
            }
            values.add(c.getValue());
        }

        for (Map.Entry<Pair<Integer, Integer>, List<BigDecimal>> entry : groups.entrySet()) {
            points.add(new JSONDataPoint(entry.getKey().getFirst(), entry.getKey().getSecond(), avg(entry.getValue())));
        }

        return new JSONSeries("ARS", points, "LAST_VALUE_INTERPOLATION");
    }

    private static BigDecimal sum(List<BigDecimal> list) {
        BigDecimal total = BigDecimal.ZERO;
        for (BigDecimal v : list) {
            total = total.add(v);
        }
        return total;
    }

    private static BigDecimal avg(List<BigDecimal> list) {
        if (list.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(list).setScale(7, RoundingMode.HALF_UP).divide(new BigDecimal(list.size()), MathContext.DECIMAL32);
    }
}
