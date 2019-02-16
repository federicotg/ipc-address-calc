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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.groupingBy;
import java.util.stream.IntStream;
import java.util.stream.Stream;
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

    private static final Set<String> USD_SERIES = Stream.of("fci/CONLLBU-AR.json", "fci/COINLAT-AR.json").collect(Collectors.toSet());

    private static final Map<String, MoneyAmountSeries> CACHE = new HashMap<>();

    public static <T> String list(Collection<T> elements) {
        return list(elements, ", ");
    }

    public static <T> String list(Collection<T> elements, String separator) {
        return elements.stream().map(T::toString).collect(joining(separator));
    }

    public static MoneyAmountSeries sumSeries(String currency, List<ExpenseChartSeriesDTO> dtos) {
        return sumSeries(currency, dtos.stream().map(ExpenseChartSeriesDTO::getSeriesName).toArray(String[]::new));
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

        try (InputStream is = new FileInputStream("/home/fede/Sync/app-resources/" + name)) {

            JSONSeries series;
            if (name.startsWith("fci/")) {
                series = readConsultatioSeries(is, OM, USD_SERIES.contains(name) ? "USD" : "ARS");
            } else {
                series = OM.readValue(is, JSONSeries.class);
            }

            final SortedMap<YearMonth, MoneyAmount> interpolatedData = new TreeMap<>();
            final String currency = series.getCurrency();
            for (JSONDataPoint dp : series.getData()) {
                if (interpolatedData.put(new YearMonth(dp.getYear(), dp.getMonth()), new MoneyAmount(dp.getValue(), currency)) != null) {
                    throw new IllegalArgumentException(MessageFormat.format("Series {0} has two values for year {1} and month {2}", name, dp.getYear(), dp.getMonth()));
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
            throw new IllegalArgumentException(MessageFormat.format("Could not read series named {0}", name), ioEx);
        }

    }

    private static <T> Stream<List<T>> sliding(List<T> list, int size) {
        if (size > list.size()) {
            return Stream.empty();
        }
        return IntStream.range(0, list.size() - size + 1)
                .mapToObj(start -> list.subList(start, start + size));
    }

    private static Pair<Integer, Integer> key(ConsultatioDataPoint dataPoint) {
        LocalDate d = dataPoint.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return new Pair<>(d.getYear(), d.getMonthValue());
    }

    private static ConsultatioDataPoint average(List<ConsultatioDataPoint> list) {
        ConsultatioDataPoint answer = new ConsultatioDataPoint();
        ConsultatioDataPoint last = list.get(list.size() - 1);
        answer.setDate(last.getDate());
        answer.setValue(list.stream().collect(Collectors.mapping(ConsultatioDataPoint::getValue, new BigDecimalAverageCollector())));
        return answer;
    }

    private static JSONSeries readConsultatioSeries(InputStream is, ObjectMapper om, String currency) throws IOException {

        final List<ConsultatioDataPoint> data = om.readValue(is, new TypeReference<List<ConsultatioDataPoint>>() {
        });

        final Stream<ConsultatioDataPoint> slidingData = sliding(data, 30).map(Util::average);

        final Map<Pair<Integer, Integer>, BigDecimal> groups = slidingData.collect(
                groupingBy(Util::key,
                        Collectors.mapping(ConsultatioDataPoint::getValue, new BigDecimalAverageCollector())));

        final List<JSONDataPoint> points = groups.entrySet().stream()
                .map(e -> new JSONDataPoint(e.getKey().getFirst(), e.getKey().getSecond(), e.getValue()))
                .collect(Collectors.toList());

        return new JSONSeries(currency, points, "LAST_VALUE_INTERPOLATION");
    }

}
