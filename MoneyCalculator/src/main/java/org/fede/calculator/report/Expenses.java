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
package org.fede.calculator.report;

import com.diogonunes.jcolor.Attribute;
import java.math.BigDecimal;
import static java.math.BigDecimal.ZERO;
import java.text.MessageFormat;
import static java.text.MessageFormat.format;
import java.util.Comparator;
import static java.util.Comparator.comparing;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;
import org.fede.calculator.money.Currency;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.series.MoneyAmountSeries;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.fede.calculator.money.ForeignExchanges;
import org.fede.calculator.money.Inflation;
import org.fede.calculator.money.SlidingWindow;
import org.fede.calculator.money.series.JSONDataPoint;
import org.fede.calculator.money.series.SeriesReader;
import org.fede.calculator.money.series.YearMonthUtil;

/**
 *
 * @author federicogentile
 */
public class Expenses {

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

    public void expensesChange(int months) {

        this.console.appendLine(this.format.title("Expenses Change"));

        this.bar.evolution(format("{0}-month average expenses change", months),
                new SlidingWindow(1)
                        .change(new SlidingWindow(months)
                                .average(this.series.realExpenses(null))), 3);
    }

    public void expenses(Map<String, String> params) {

        Runnable otherwise = () -> {

            final String exp = params.get("type");
            final int months = Integer.parseInt(params.getOrDefault("m", "12"));

            final Set<YearMonth> monthSet = HashSet.newHashSet(months);
            var ym = YearMonth.now();
            for (int i = 0; i < months; i++) {
                ym = ym.plusMonths(-1);
                monthSet.add(ym);
            }

            this.console.appendLine(this.format.title(format("Real USD expenses in the last {0} months", months)));

            final List<TypeAndAmount> list = this.series.getRealUSDExpensesByType()
                    .entrySet()
                    .stream()
                    .filter(p -> exp == null || exp.equals(p.getKey()))
                    .map(e -> new TypeAndAmount(e.getKey(), this.aggregate(e.getValue(), s -> this.lastMonths(s, monthSet)).amount()))
                    .toList();

            final var total = list.stream()
                    .map(TypeAndAmount::amount)
                    .reduce(ZERO, BigDecimal::add);

            list.stream()
                    .sorted(comparing(TypeAndAmount::amount, Comparator.reverseOrder()))
                    .map(e -> format("{0}{1}{2}{3}",
                            this.format.text(e.type(), 15),
                            this.format.text(" USD ", 4),
                            this.format.currency(e.amount(), 15),
                            this.bar.pctBar(e.amount(), total)))
                    .forEach(this.console::appendLine);

            this.console.appendLine(format("--------------------------------\n{0} USD {1}",
                    this.format.text("Total", 5),
                    this.format.currency(total, 10)));

            this.console.appendLine(this.format.subtitle(Series.IRREGULAR + " Detail"));

            this.irregularExpenseDetailReport(monthSet);

            this.console.appendLine("");
            this.investmentExpensesReport(monthSet);

            this.console.appendLine(this.format.subtitle("Banking Detail"));
            this.bankingExpensesReport(monthSet);

            this.console.appendLine(this.format.subtitle("Vacations Detail"));
            this.vacationsExpensesReport(monthSet);

            this.console.appendLine(this.format.subtitle("Credit Card Detail"));
            this.creditCardExpensesReport(monthSet);
        };

        new By()
                .by(
                        params,
                        this::quarterExpenses,
                        this::halfExpenses,
                        this::yearlyExpenses,
                        this::monthlyExpenses,
                        otherwise);
    }

    
    private void creditCardExpensesReport(Set<YearMonth> months) {
        var totalStampDuty = this.expenseDetailReport(months,
                List.of(
                        "expense/sellos.json"));

        this.console.appendLine("Spending ~ ",
                this.format.currency(totalStampDuty.adjust(new BigDecimal("0.012"), BigDecimal.ONE), 14));
    }

    private void bankingExpensesReport(Set<YearMonth> months) {
        this.expenseDetailReport(months,
                List.of(
                        "expense/itau-uy.json",
                        "expense/box.json",
                        "expense/santander.json",
                        "expense/atlantico.json"));
    }

    private void vacationsExpensesReport(Set<YearMonth> months) {
        this.expenseDetailReport(months,
                List.of(
                        "expense/viajes.json",
                        "expense/viajes-usd.json"));
    }

    private void investmentExpensesReport(Set<YearMonth> months) {
        var investingExpenses = this.series.investingExpenses();

        var totalInvestmentExpenses = months
                .stream()
                .map(investingExpenses::getAmountOrElseZero)
                .reduce(MoneyAmount.zero(Currency.USD), MoneyAmount::add);

        this.console.appendLine(
                MessageFormat.format("{0} {1}",
                        this.format.currency(totalInvestmentExpenses, 14),
                        "Investing Expenses")
        );
    }

    private void irregularExpenseDetailReport(Set<YearMonth> months) {
        this.expenseDetailReport(months,
                List.of(
                        "expense/reparaciones.json",
                        "expense/other.json",
                        "expense/other-usd.json"));
    }

    private MoneyAmount expenseDetailReport(Set<YearMonth> months, List<String> series) {

        Map<YearMonth, Optional<ExpenseDetail>> grouped = series
                .stream()
                .map(SeriesReader::readJSONSeries)
                .flatMap(s
                        -> s.data()
                                .stream()
                                .filter(dp -> months.contains(dp.yearMonth()))
                                .map(dp -> this.detail(s.currency(), dp)))
                .filter(d -> !d.amount().isZero())
                .collect(Collectors.groupingBy(
                        ExpenseDetail::ym, Collectors.reducing(this::merge)
                ));

        for (var ym : grouped.keySet().stream().sorted().toList()) {

            grouped.get(ym)
                    .ifPresent(this::expenseDetailReport);
        }
        var total = grouped
                .values()
                .stream()
                .map(o -> o.map(ExpenseDetail::amount)
                        .orElse(MoneyAmount.zero(Currency.USD)))
                .reduce(MoneyAmount.zero(Currency.USD), MoneyAmount::add);
        this.console.appendLine("Total: ", this.format.currency(
                total, 14));
        return total;
    }

    private ExpenseDetail merge(ExpenseDetail a, ExpenseDetail b) {
        return new ExpenseDetail(
                a.amount().add(b.amount()),
                a.ym(),
                this.joinOptionalComments(a.comment(), b.comment()));
    }

    private String joinOptionalComments(String a, String b) {
        if (a == null && b == null) {
            return "";
        }
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return MessageFormat.format("{0}, {1}", a, b);

    }

    private void expenseDetailReport(ExpenseDetail d) {
        this.console.appendLine(
                MessageFormat.format("{0} {1} {2}",
                        d.ym(),
                        this.format.currency(d.amount(), 14),
                        d.comment() == null ? "" : d.comment())
        );
    }

    private boolean between(YearMonth ym, YearMonth from, YearMonth to) {
        return (ym.equals(from) || ym.isAfter(from))
                && (ym.equals(to) || ym.isBefore(to));

    }

    private ExpenseDetail detail(Currency c, JSONDataPoint dp) {
        var nominalUSD = ForeignExchanges.getMoneyAmountForeignExchange(c, Currency.USD)
                .apply(new MoneyAmount(dp.value(), c), dp.yearMonth());
        var realUSD = Inflation.usdInflation().adjust(nominalUSD, dp.yearMonth(), Inflation.usdInflation().getTo());
        return new ExpenseDetail(realUSD, dp.yearMonth(), dp.comment());

    }

    private MoneyAmount aggregate(List<MoneyAmountSeries> mas, Function<MoneyAmountSeries, MoneyAmount> aggregation) {
        return mas.stream()
                .map(aggregation)
                .reduce(MoneyAmount.zero(Currency.USD), MoneyAmount::add);
    }

    private MoneyAmount lastMonths(MoneyAmountSeries s, Set<YearMonth> months) {

        return months.stream()
                .map(s::getAmountOrElseZero)
                .reduce(MoneyAmount.zero(Currency.USD), MoneyAmount::add);

    }

    private void quarterExpenses() {
        new Group(console, format, bar)
                .group("Quarterly expenses", this.series.realExpense(), null, YearMonthUtil::quarter, 3);
    }

    private void monthlyExpenses() {
        new Group(console, format, bar)
                .group("Monthly expenses", this.series.realExpense(), null, YearMonthUtil::monthString, 1);
    }

    private void yearlyExpenses() {
        new Group(console, format, bar)
                .group("Yearly expenses", this.series.realExpense(), null, ym -> String.valueOf(ym.getYear()), 12);
    }

    private void halfExpenses() {
        new Group(console, format, bar)
                .group("Half expenses", this.series.realExpense(), null, YearMonthUtil::half, 6);
    }

    public void expenseBySource(int months) {
        this.expenseBySource(months, this.series.getRealUSDExpensesByType());

    }

    private void expenseBySource(int months, Map<String, List<MoneyAmountSeries>> source) {

        final var title = format("Average {0}-month expenses by source", months);

        final var colorList = List.of(
                Attribute.BLUE_BACK(),
                Attribute.RED_BACK(),
                Attribute.YELLOW_BACK(),
                Attribute.GREEN_BACK());
        this.console.appendLine(this.format.title(title));

        final var agg = new SlidingWindow(months);

        final var seriesGroups = source;

        final var ss = seriesGroups.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getValue().stream().reduce(MoneyAmountSeries::add).get())
                .map(agg::average)
                .toList();

        final var labels = seriesGroups.entrySet().stream()
                .map(Map.Entry::getKey)
                .sorted()
                .toList();

        final var oldestSeries = ss.stream().min(Comparator.comparing(MoneyAmountSeries::getFrom)).get();

        final var scale = switch (months) {
            case 1 ->
                80;
            case 2 ->
                60;
            case 3 ->
                40;
            default ->
                20;
        };

        final var nextMonth = YearMonth.now().plusMonths(1);

        oldestSeries.map((ym, ma) -> MoneyAmount.zero(Currency.USD).max(ma))
                //.forEach((ym, savingMa) -> this.console.appendLine(this.bar.genericBar(ym, this.independenSeries(ym, ss, colorList), scale)));
                .forEach((ym, savingMa)
                        -> this.printIf(
                                currentYm -> currentYm.isBefore(nextMonth),
                                () -> this.console.appendLine(this.bar.genericBar(ym, this.independenSeries(ym, ss, colorList), scale)),
                                ym)
                );

        new References(console, format).refs(title, labels, colorList);

    }

    private void printIf(Predicate<YearMonth> condition, Runnable action, YearMonth ym) {
        if (condition.test(ym)) {
            action.run();
        }
    }

    private List<AmountAndColor> independenSeries(YearMonth ym, List<MoneyAmountSeries> series, List<Attribute> colors) {

        return IntStream.range(0, series.size())
                .mapToObj(i -> new AmountAndColor(MoneyAmount.zero(Currency.USD).max(series.get(i).getAmountOrElseZero(ym)), colors.get(i)))
                .toList();
    }

}
