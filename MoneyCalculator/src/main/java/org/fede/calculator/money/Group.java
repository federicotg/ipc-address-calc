/*
 * Copyright (C) 2023 federicogentile
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

import java.math.BigDecimal;
import static java.text.MessageFormat.format;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static org.fede.calculator.money.MathConstants.C;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.YearMonth;

/**
 *
 * @author federicogentile
 */
public class Group {

    private final Console console;
    private final Format format;
    private final Bar bar;

    public Group(Console console, Format format, Bar bar) {
        this.console = console;
        this.format = format;
        this.bar = bar;
    }

    public void group(String title, MoneyAmountSeries series, MoneyAmountSeries comparisonSeries, Function<YearMonth, String> classifier, int months) {

        this.console.appendLine(this.format.title(title));

        final Map<String, MoneyAmount> byYear = HashMap.newHashMap(100);

        series.forEachNonZero((ym, ma) -> byYear.merge(classifier.apply(ym), ma, MoneyAmount::add));

        final Map<String, Long> counts = series.yearMonthStream()
                .collect(groupingBy(classifier, counting()));

        final Map<String, MoneyAmount> comparisonByYear = HashMap.newHashMap(32);

        if (comparisonSeries != null) {
            comparisonSeries.forEachNonZero((ym, ma) -> comparisonByYear.merge(classifier.apply(ym), ma, MoneyAmount::add));
        }

        byYear.entrySet()
                .stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .forEach(e -> this.console.appendLine(
                format("{0}{1}{2} {3}",
                        this.format.text(e.getKey(), 9),
                        this.format.currency(e.getValue().getAmount().divide(BigDecimal.valueOf(Math.min(months, counts.get(e.getKey()))), C), 10),
                        Optional.ofNullable(comparisonByYear.get(e.getKey()))
                                .map(comp -> this.format.percent(e.getValue().getAmount().divide(comp.getAmount(), C), 10))
                                .orElse(""),
                        this.bar.bar(e.getValue().getAmount().divide(BigDecimal.valueOf(Math.min(months, counts.get(e.getKey()))), C), 50))
        )
                );
    }

}
