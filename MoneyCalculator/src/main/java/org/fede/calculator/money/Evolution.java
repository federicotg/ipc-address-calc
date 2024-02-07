/*
 * Copyright (C) 2021 federicogentile
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
package org.fede.calculator.money;

import com.diogonunes.jcolor.Ansi;
import com.diogonunes.jcolor.Attribute;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toMap;
import java.util.stream.IntStream;
import org.fede.calculator.money.series.YearMonth;

/**
 *
 * @author fede
 * @param <T>
 */
public class Evolution<T> {

    private static final MoneyAmount ZERO_USD = MoneyAmount.zero("USD");

    private final Console console;
    private final Bar bar;

    public Evolution(Console console, Bar bar) {
        this.console = console;
        this.bar = bar;
    }

    public void evo(
            BiFunction<T, YearMonth, MoneyAmount> totalFunction,
            Function<T, YearMonth> startFunction,
            Function<T, YearMonth> endFunction,
            Function<T, String> classifier,
            Predicate<T> filterPredicate,
            Comparator<T> comparator,
            List<T> list,
            boolean pct) {

        final var inv = list.stream()
                .filter(filterPredicate)
                .sorted(comparator)
                .toList();

        final var start = inv
                .stream()
                .map(startFunction)
                .reduce((left, right) -> left.min(right))
                .get();

        final var end = inv
                .stream()
                .map(endFunction)
                .reduce((left, right) -> left.max(right))
                .get();

        final var refs = new HashSet<String>();

        var ym = start;
        while (ym.compareTo(end) <= 0) {

            final var moment = ym;

            final Map<String, List<T>> grouped = inv
                    .stream()
                    .collect(Collectors.groupingBy(classifier));

            final Map<String, MoneyAmount> totals = grouped.entrySet()
                    .stream()
                    .collect(toMap(Map.Entry::getKey, e -> this.accum(e.getValue(), moment, totalFunction, startFunction, endFunction)));

            final var colorList = List.of(
                    Attribute.BLUE_BACK(),
                    Attribute.CYAN_BACK(),
                    Attribute.GREEN_BACK(),
                    Attribute.MAGENTA_BACK(),
                    Attribute.RED_BACK(),
                    Attribute.YELLOW_BACK(),
                    Attribute.WHITE_BACK(),
                    Attribute.BRIGHT_BLUE_BACK(),
                    Attribute.BRIGHT_CYAN_BACK(),
                    Attribute.BRIGHT_GREEN_BACK(),
                    Attribute.BRIGHT_MAGENTA_BACK(),
                    Attribute.BRIGHT_YELLOW_BACK(),
                    Attribute.BRIGHT_RED_BACK(),
                    Attribute.BACK_COLOR(80),
                    Attribute.BACK_COLOR(100),
                    Attribute.BRIGHT_WHITE_BACK()
                   
                    );

            final var typeList = totals.keySet().stream().sorted().toList();

            final var elements = IntStream.range(0, typeList.size())
                    .mapToObj(i -> new AmountAndColor(totals.get(typeList.get(i)), colorList.get(i)))
                    .toList();

            this.console.appendLine(
                    pct
                            ? this.pctBar(ym, elements)
                            : this.bar(ym, elements, 1800));

            refs.addAll(IntStream.range(0, typeList.size())
                    .mapToObj(i -> Ansi.colorize(" ", colorList.get(i)) + typeList.get(i))
                    .toList());

            ym = ym.next();
        }
        this.console.appendLine("");
        this.console.appendLine("References:");

        this.console.appendLine(refs.stream().collect(Collectors.joining(" ")));

    }

    private MoneyAmount accum(
            List<T> investments,
            YearMonth yearMonth,
            BiFunction<T, YearMonth, MoneyAmount> extractor,
            Function<T, YearMonth> startFunction,
            Function<T, YearMonth> endFunction) {

        return investments.stream()
                .filter(i -> startFunction.apply(i).compareTo(yearMonth) <= 0)
                .filter(i -> endFunction.apply(i).compareTo(yearMonth) >= 0)
                .map(i -> extractor.apply(i, yearMonth))
                .reduce(ZERO_USD, MoneyAmount::add);
    }

    private String bar(YearMonth ym, List<AmountAndColor> elements, int scale) {
        return this.bar.genericBar(ym, elements, scale);
    }

    private String pctBar(YearMonth ym, List<AmountAndColor> elements) {
        return this.bar.percentBar(ym, elements);
    }

}
