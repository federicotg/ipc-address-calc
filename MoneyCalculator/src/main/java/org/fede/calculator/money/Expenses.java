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

import com.diogonunes.jcolor.Attribute;
import java.math.BigDecimal;
import static java.math.BigDecimal.ZERO;
import static java.text.MessageFormat.format;
import java.util.Comparator;
import static java.util.Comparator.comparing;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;
import static org.fede.calculator.money.Inflation.USD_INFLATION;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.YearMonth;

/**
 *
 * @author federicogentile
 */
public class Expenses {

    private static final MoneyAmount ZERO_USD = MoneyAmount.zero("USD");

    private final Series series;
    private final Console console;
    private final Bar bar;
    private final Format format;

    public Expenses(Series series, Console console, Bar bar, Format format) {
        this.series = series;
        this.console = console;
        this.bar = bar;
        this.format = format;
    }

    public void expensesChange(String type, int months) {

        this.console.appendLine(this.format.title("Expenses Change"));

        final var s = "full".equals(type)
                ? this.series.realExpense()
                : this.series.realExpenses(null);

        this.bar.evolution(format("{0}-month average expenses change", months),
                new SimpleAggregation(2)
                        .change(new SimpleAggregation(months)
                                .average(s)), 1);
    }

    public void expenses(Map<String, String> params) {

        Runnable otherwise = () -> {

            final String exp = params.get("type");
            final int months = Integer.parseInt(params.getOrDefault("m", "12"));

            this.console.appendLine(this.format.title(format("Real USD expenses in the last {0} months", months)));

            final var list = this.series.getRealUSDExpensesByType()
                    .entrySet()
                    .stream()
                    .filter(p -> exp == null || exp.equals(p.getKey()))
                    .map(e -> new TypeAndAmount(e.getKey(), this.aggregate(e.getValue(), s -> this.lastMonths(s, months)).getAmount()))
                    .toList();

            final var total = list.stream()
                    .map(TypeAndAmount::amount)
                    .reduce(ZERO, BigDecimal::add);

            list.stream()
                    .sorted(comparing(TypeAndAmount::amount, Comparator.reverseOrder()))
                    .map(e -> format("{0}{1}{2}{3}",
                    this.format.text(e.type(), 13),
                    this.format.text(" USD ", 4),
                    this.format.currency(e.amount(), 10),
                    this.bar.pctBar(e.amount(), total)))
                    .forEach(this.console::appendLine);

            this.console.appendLine(format("-----------------------------\n{0} USD {1}",
                    this.format.text("Total", 5),
                    this.format.currency(total, 10)));
        };

        new By().by(params, this::quarterExpenses, this::halfExpenses, this::yearlyExpenses, this::monthlyExpenses, otherwise);
    }

    private MoneyAmount aggregate(List<MoneyAmountSeries> mas, Function<MoneyAmountSeries, MoneyAmount> aggregation) {
        return mas.stream()
                .map(aggregation)
                .reduce(ZERO_USD, MoneyAmount::add);
    }

    private MoneyAmount lastMonths(MoneyAmountSeries s, int months) {

        var ym = USD_INFLATION.getTo();
        var amount = ZERO_USD;

        for (var i = 0; i < months; i++) {
            amount = amount.add(s.getAmountOrElseZero(ym));
            ym = ym.prev();
        }

        return amount;

    }

    private void quarterExpenses() {
        new Group(console, format, bar).group("Quarterly expenses", this.series.realExpense(), null, YearMonth::quarter, 3);
    }

    private void monthlyExpenses() {
        new Group(console, format, bar).group("Monthly expenses", this.series.realExpense(), null, YearMonth::monthString, 3);
    }

    private void yearlyExpenses() {
        new Group(console, format, bar).group("Yearly expenses", this.series.realExpense(), null, ym -> String.valueOf(ym.getYear()), 12);
    }

    private void halfExpenses() {
        new Group(console, format, bar).group("Half expenses", this.series.realExpense(), null, YearMonth::half, 3);
    }

    public void expenseBySource(int months) {
        this.expenseBySource(months, this.series.getRealUSDExpensesByType());

    }

    public void expenseByEssential(int months) {
        this.expenseBySource(months, this.series.getRealUSDExpensesByEssential());

    }

    public void expenseBySource(int months, Map<String, List<MoneyAmountSeries>> source) {

        final var title = format("Average {0}-month expenses by source", months);

        final var colorList = List.of(
                Attribute.BLUE_BACK(),
                Attribute.RED_BACK(),
                Attribute.YELLOW_BACK(),
                Attribute.GREEN_BACK(),
                Attribute.MAGENTA_BACK(),
                Attribute.WHITE_BACK()
        );
        this.console.appendLine(this.format.title(title));

        final var agg = new SimpleAggregation(months);

        final var seriesGroups = source;

        final var ss = seriesGroups.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(e -> e.getValue().stream().reduce(MoneyAmountSeries::add).get())
                .map(agg::average)
                .toList();

        final var labels = seriesGroups.entrySet().stream()
                .map(Map.Entry::getKey)
                .sorted()
                .toList();

        final var oldestSeries = ss.stream().min(Comparator.comparing(MoneyAmountSeries::getFrom)).get();

        oldestSeries.map((ym, ma) -> ZERO_USD.max(ma))
                .forEach((ym, savingMa) -> this.console.appendLine(this.bar.genericBar(ym, this.independenSeries(ym, ss, colorList), 8)));

        new References(console, format).refs(title, labels, colorList);

    }

    private List<AmountAndColor> independenSeries(YearMonth ym, List<MoneyAmountSeries> series, List<Attribute> colors) {

        return IntStream.range(0, series.size())
                .mapToObj(i -> new AmountAndColor(ZERO_USD.max(series.get(i).getAmountOrElseZero(ym)), colors.get(i)))
                .toList();
    }

    private record TypeAndAmount(String type, BigDecimal amount) {

    }
}
