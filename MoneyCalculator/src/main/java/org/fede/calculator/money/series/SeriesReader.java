/*
 * Copyright (C) 2016 Federico Tello Gentile <federicotg@gmail.com>
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
package org.fede.calculator.money.series;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.fede.calculator.money.MoneyAmount;

/**
 *
 * @author fede
 */
public class SeriesReader {

    private static final ObjectMapper OM = new ObjectMapper();

    private static final Map<String, JSONIndexSeries> CACHE = new ConcurrentHashMap<>();

    private static final TypeReference<List<JSONDataPoint>> INDEX_SERIES_TYPE_REFERENCE = new TypeReference<List<JSONDataPoint>>() {
    };

    private static final Map<String, MoneyAmountSeries> MACACHE = new ConcurrentHashMap<>();

    static {
        OM.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    
    public static IndexSeries readIndexSeries(String name) {
        return CACHE.computeIfAbsent(name, seriesName -> new JSONIndexSeries(read(seriesName, INDEX_SERIES_TYPE_REFERENCE)));
    }

    public static <T> T read(String name, TypeReference<T> typeReference) {
        try (InputStream in = new BufferedInputStream(new FileInputStream(new File(System.getProperty("user.home") + "/Sync/app-resources/" + name)));) {
            return OM.readValue(in, typeReference);
        } catch (IOException ioEx) {
            throw new IllegalArgumentException("Could not read series from resource " + name, ioEx);
        }
    }

    public static MoneyAmountSeries readSeries(String name) {
        return MACACHE.computeIfAbsent(name, (seriesName) -> read(seriesName));
    }

    private static MoneyAmount moneyAmount(BigDecimal value, String currency) {
        return value.signum() == 0
                ? MoneyAmount.zero(currency)
                : new MoneyAmount(value, currency);
    }

    private static MoneyAmountSeries read(String name) {

        try (InputStream is = new BufferedInputStream(new FileInputStream(System.getProperty("user.home") + "/Sync/app-resources/" + name))) {

            JSONSeries series = OM.readValue(is, JSONSeries.class);

            final SortedMap<YearMonth, MoneyAmount> interpolatedData = new TreeMap<>();
            final String currency = series.getCurrency();
            for (JSONDataPoint dp : series.getData()) {
                if (interpolatedData.put(YearMonth.of(dp.year(), dp.month()), moneyAmount(dp.value(), currency)) != null) {
                    throw new IllegalArgumentException(MessageFormat.format("Series {0} has two values for year {1} and month {2}", name, dp.year(), dp.month()));
                }
            }

            final InterpolationStrategy strategy = InterpolationStrategy.valueOf(series.getInterpolation());
           
            YearMonth ym = interpolatedData.firstKey();
            final YearMonth last = interpolatedData.lastKey();
            
            final var allValues = new HashMap<>(interpolatedData);
            while (ym.monthsUntil(last) > 0) {
                YearMonth next = ym.next();
                if (!allValues.containsKey(next)) {
                    allValues.put(next, strategy.interpolate(allValues.get(ym), ym, currency));
                }
                ym = ym.next();
            }
            return new SortedMapMoneyAmountSeries(currency, allValues);

        } catch (IOException ioEx) {
            throw new IllegalArgumentException(MessageFormat.format("Could not read series named {0}", name), ioEx);
        }

    }

}
