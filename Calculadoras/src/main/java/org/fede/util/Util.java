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
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.averagingDouble;
import static java.util.stream.Collectors.groupingBy;
import org.fede.calculator.money.MoneyAmount;
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

    private static final ObjectMapper OM = new ObjectMapper();

    private static final Map<String, MoneyAmountSeries> CACHE = new HashMap<>();

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
        return elements.stream().map(e -> e.toString()).collect(joining(separator));
    }

    public static MoneyAmountSeries sumSeries(String currency, List<ExpenseChartSeriesDTO> dtos) {
        return sumSeries(currency, dtos.stream().map(dto -> dto.getSeriesName()).toArray(String[]::new));
    }

    public static MoneyAmountSeries sumSeries(List<ExpenseChartSeriesDTO> dtos) {
        return sumSeries("USD", dtos);
    }

    public static MoneyAmountSeries sumSeries(String currency, String... names) {
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

    public static MoneyAmountSeries readSeries(String name) {

        return CACHE.computeIfAbsent(name, (seriesName) -> read(seriesName));

    }

    private static MoneyAmountSeries read(String name) {

        try (InputStream is = Util.class.getResourceAsStream("/" + name)) {

            final JSONSeries series = CONSULTATIO_SERIES.contains(name)
                    ? readConsultatioSeries(is, OM)
                    : OM.readValue(is, JSONSeries.class);

            final SortedMap<YearMonth, MoneyAmount> interpolatedData = new TreeMap<>();
            final String currency = series.getCurrency();
            for (JSONDataPoint dp : series.getData()) {
                if (interpolatedData.put(new YearMonth(dp.getYear(), dp.getMonth()), new MoneyAmount(dp.getValue(), currency)) != null) {
                    throw new IllegalArgumentException("Series " + name + " has two values for year " + dp.getYear() + " and month " + dp.getMonth());
                }
            }

            final InterpolationStrategy strategy = InterpolationStrategy.valueOf(series.getInterpolation());

            YearMonth ym = interpolatedData.firstKey();
            final YearMonth last = interpolatedData.lastKey();
            while (ym.monthsUntil(last) > 0) {
                YearMonth next = ym.next();
                if (!interpolatedData.containsKey(next)) {
                    interpolatedData.put(next, strategy.interpolate(interpolatedData.get(ym), ym, currency));
                }
                ym = ym.next();
            }
            return new SortedMapMoneyAmountSeries(currency, interpolatedData);

        } catch (IOException ioEx) {
            throw new IllegalArgumentException("Could not read series named " + name, ioEx);
        }

    }

    
    private static Pair<Integer, Integer> key(ConsultatioDataPoint dataPoint){
        LocalDate d = dataPoint.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return new Pair<>(d.getYear(), d.getMonthValue());
    }
    
    private static JSONSeries readConsultatioSeries(InputStream is, ObjectMapper om) throws IOException {

        List<ConsultatioDataPoint> data = om.readValue(is, new TypeReference<List<ConsultatioDataPoint>>() {
        });
        
        Map<Pair<Integer, Integer>, Double> groups = data.stream()
                .collect(groupingBy(dp -> key(dp), averagingDouble(dp -> dp.getValue().doubleValue())));
        
        List<JSONDataPoint> points = groups.entrySet().stream()
                .map(e -> new JSONDataPoint(e.getKey().getFirst(), e.getKey().getSecond(), new BigDecimal(e.getValue())))
                .collect(Collectors.toList());
        

        return new JSONSeries("ARS", points, "LAST_VALUE_INTERPOLATION");
    }

}
