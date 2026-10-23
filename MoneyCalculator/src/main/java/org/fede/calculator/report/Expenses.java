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

import java.math.BigDecimal;
import static java.math.BigDecimal.ZERO;
import java.text.MessageFormat;
import static java.text.MessageFormat.format;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import static java.util.Comparator.comparing;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.fede.calculator.chart.BarChart;
import org.fede.calculator.chart.ChartStyle;
import org.fede.calculator.chart.Scale;
import org.fede.calculator.chart.ValueFormat;
import org.fede.calculator.money.Currency;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.series.MoneyAmountSeries;
import java.time.Year;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.HashSet;
import static org.fede.calculator.report.SpendingSeries.*;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.fede.calculator.money.ForeignExchanges;
import org.fede.calculator.money.Inflation;
import org.fede.calculator.money.SlidingWindow;
import org.fede.calculator.money.series.JSONDataPoint;
import org.fede.calculator.money.series.SeriesReader;
import org.fede.calculator.money.series.YearMonthUtil;
import org.jfree.data.category.DefaultCategoryDataset;

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

    private Set<YearMonth> monthSet(int months) {
        final Set<YearMonth> monthSet = HashSet.newHashSet(months);
        var ym = YearMonth.now();
        for (int i = 0; i < months; i++) {
            ym = ym.plusMonths(-1);
            monthSet.add(ym);
        }
        return monthSet;
    }

    public void spending(Map<String, String> params) {

        final int months = Integer.parseInt(params.getOrDefault("m", "12"));
        final Set<YearMonth> monthSet = this.monthSet(months);
        final List<SpendingSeries> selected = this.resolveSeries(params);

        final var spending = this.series.allSpending();

        this.console.appendLine(this.format.title(format(
                "Spending last {0} months ({1})",
                months,
                this.seriesFilterLabel(params))));

        MoneyAmount totalUSD = MoneyAmount.zero(Currency.USD);
        MoneyAmount totalARS = MoneyAmount.zero(Currency.ARS);

        for (var ym : monthSet.stream().sorted().toList()) {
            final var usd = selected.stream()
                    .map(spending::get)
                    .map(s -> s.getAmountOrElseZero(ym))
                    .reduce(MoneyAmount.zero(Currency.USD), MoneyAmount::add);
            final var ars = this.ars(usd, ym);

            totalUSD = totalUSD.add(usd);
            totalARS = totalARS.add(ars);

            this.console.appendLine(MessageFormat.format("{0} {1} {2}",
                    ym,
                    this.format.currency(usd, 14),
                    this.format.currency(ars, 18)));
        }

        this.console.appendLine(MessageFormat.format(
                "Total: {0} {1}",
                this.format.currency(totalUSD, 14),
                this.format.currency(totalARS, 20)));

        final var monthsBd = BigDecimal.valueOf(months);
        this.console.appendLine(MessageFormat.format(
                "Avg:   {0} {1}",
                this.format.currency(totalUSD.adjust(monthsBd, BigDecimal.ONE), 14),
                this.format.currency(totalARS.adjust(monthsBd, BigDecimal.ONE), 20)));
    }

    public void spendingAverageTop(Map<String, String> params) {

        final int months = Integer.parseInt(params.getOrDefault("m", "12"));
        final int averageMonths = Integer.parseInt(params.getOrDefault("n", "12"));
        final int top = Integer.parseInt(params.getOrDefault("t", "10"));

        final Map<String, String> effectiveParams = new HashMap<>(params);
        effectiveParams.putIfAbsent("ex", UNCLASSIFIED.name());
        final List<SpendingSeries> selected = this.resolveSeries(effectiveParams);

        final Set<YearMonth> monthSet = this.monthSet(months);
        final var spending = this.series.allSpending();

        final Map<SpendingSeries, MoneyAmountSeries> averages = selected.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        s -> new SlidingWindow(averageMonths).average(spending.get(s))));

        final var monthsBd = BigDecimal.valueOf(months);
        final MoneyAmount totalAvgUSD = monthSet.stream()
                .map(ym -> selected.stream()
                        .map(spending::get)
                        .map(s -> s.getAmountOrElseZero(ym))
                        .reduce(MoneyAmount.zero(Currency.USD), MoneyAmount::add))
                .reduce(MoneyAmount.zero(Currency.USD), MoneyAmount::add)
                .adjust(monthsBd, BigDecimal.ONE);

        this.console.appendLine(this.format.title(format(
                "Top {0} {1}-month averages in last {2} months ({3})",
                top,
                averageMonths,
                months,
                this.seriesFilterLabel(effectiveParams))));

        this.console.appendLine(MessageFormat.format(
                "Avg total spending: {0}",
                this.format.currency(totalAvgUSD, 14)));

        averages.entrySet()
                .stream()
                .flatMap(e -> monthSet.stream()
                        .map(ym -> new AverageRow(e.getKey(), ym, e.getValue().getAmountOrElseZero(ym)))
                        .filter(row -> !row.amount().isZero()))
                .sorted(comparing((AverageRow row) -> row.amount().amount(), Comparator.reverseOrder()))
                .limit(top)
                .forEach(row -> this.console.appendLine(MessageFormat.format(
                        "{0} {1} {2} {3} {4}",
                        this.format.text(row.series().name(), 22),
                        row.ym(),
                        this.format.currency(row.amount(), 14),
                        this.format.currency(this.ars(row.amount(), row.ym()), 18),
                        this.bar.pctBar(row.amount().amount(), totalAvgUSD.amount()))));
    }

    private record AverageRow(SpendingSeries series, YearMonth ym, MoneyAmount amount) {
    }

    private String seriesFilterLabel(Map<String, String> params) {
        final var include = params.get("in");
        if (include != null) {
            return "in=" + include.toUpperCase();
        }
        final var exclude = params.get("ex");
        if (exclude != null) {
            return "ex=" + exclude.toUpperCase();
        }
        return "all";
    }

    private List<SpendingSeries> resolveSeries(Map<String, String> params) {
        final var include = params.get("in");
        final var exclude = params.get("ex");

        if (include != null) {
            return this.parseSeriesList(include);
        }

        final var all = Arrays.stream(SpendingSeries.values()).collect(Collectors.toCollection(ArrayList::new));
        if (exclude != null) {
            all.removeAll(this.parseSeriesList(exclude));
        }
        return all;
    }

    private List<SpendingSeries> parseSeriesList(String csv) {
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toUpperCase)
                .flatMap(this::matchSeries)
                .distinct()
                .toList();
    }

    private Stream<SpendingSeries> matchSeries(String token) {
        if (!token.contains("*")) {
            return Stream.of(SpendingSeries.valueOf(token));
        }
        final var pattern = Pattern.compile(
                Arrays.stream(token.split("\\*", -1))
                        .map(Pattern::quote)
                        .collect(Collectors.joining(".*")));
        return Arrays.stream(SpendingSeries.values())
                .filter(s -> pattern.matcher(s.name()).matches());
    }

    public void expenses(Map<String, String> params) {

        Runnable otherwise = () -> {

            final String exp = params.get("type");
            final int months = Integer.parseInt(params.getOrDefault("m", "12"));

            final Set<YearMonth> monthSet = this.monthSet(months);

            this.console.appendLine(this.format.title(format("Real USD expenses in the last {0} months", months)));

            final List<TypeAndAmount> list = this.series.expenseSeriesBy(ExpenseTier::of)
                    .entrySet()
                    .stream()
                    .filter(p -> exp == null || ExpenseTier.valueOf(exp.toUpperCase()) == p.getKey())
                    .map(e -> new TypeAndAmount(e.getKey().name(), this.aggregate(e.getValue(), s -> this.lastMonths(s, monthSet)).amount()))
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

            this.console.appendLine(this.format.subtitle("Irregular Detail"));

            this.irregularExpenseDetailReport(monthSet);

            this.console.appendLine("");
            this.investmentExpensesReport(monthSet);

            this.console.appendLine(this.format.subtitle("Banking"));
            this.bankingExpensesReport(monthSet);

            this.console.appendLine(this.format.subtitle("Vacations"));
            this.vacationsExpensesReport(monthSet);

            this.console.appendLine(this.format.subtitle("Credit Card"));
            this.creditCardExpensesReport(monthSet);

            this.console.appendLine(this.format.subtitle("Taxes"));
            this.expenseDetailReport(monthSet, List.of(BBPP, INMOBILIARIO_43, MUNICIPAL_43));

            this.console.appendLine(this.format.subtitle("Power and Heating"));
            this.expenseDetailReport(monthSet,
                    List.of(LUZ, GAS));

            this.console.appendLine(this.format.subtitle("Subscriptions"));
            this.expenseDetailReport(monthSet,
                    List.of(SUSCRIPCIONES_ARS, SUSCRIPCIONES_USD, EMERGENCIA, NETFLIX));

            this.console.appendLine(this.format.subtitle("Ángeles Other Discretionary"));
            this.expenseDetailReport(monthSet,
                    List.of(ANGELES_OTHER_DISC));

            this.regularExpenses(months);
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
                List.of(SELLOS));

        this.console.appendLine("Spending ~ ",
                this.format.currency(totalStampDuty.adjust(new BigDecimal("0.012"), BigDecimal.ONE), 14));
    }

    private void bankingExpensesReport(Set<YearMonth> months) {
        this.expenseDetailReport(months,
                List.of(ITAU_UY, BOX, SANTANDER, ATLANTICO));
    }

    private void vacationsExpensesReport(Set<YearMonth> months) {
        this.expenseDetailReport(months,
                List.of(VIAJES,
                        //VIAJES_USD,
                        COLON_CAMUZZI,
                        COLON_EDEA,
                        COLON_EXPENSAS,
                        COLON_FLOW,
                        COLON_MUNICIPAL,
                        COLON_OSSE));
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
                List.of(REPARACIONES, OTHER, OTHER_USD, VIAJES_USD, EXTRAORDINARY));
    }

    private MoneyAmount expenseDetailReport(Set<YearMonth> months, List<SpendingSeries> series) {

        Map<YearMonth, Optional<ExpenseDetail>> grouped = series
                .stream()
                .map(s -> "expense/" + s.getSeriesName() + ".json")
                .map(SeriesReader::readJSONSeries)
                .flatMap(s
                        -> s.data()
                                .stream()
                                .filter(dp -> months.contains(dp.yearMonth()))
                                .map(dp -> this.detail(s.currency(), dp)))
                .filter(d -> !d.amount().isZero())
                .collect(Collectors.groupingBy(
                        ExpenseDetail::ym,
                        Collectors.reducing(this::merge)
                ));

        for (var ym : grouped.keySet().stream().sorted().toList()) {

            grouped.get(ym)
                    .ifPresent(this::expenseDetailReport);
        }

        final var totalARS = grouped
                .values()
                .stream()
                .map(o -> o.map(ed -> this.ars(ed.amount(), ed.ym()))
                        .orElse(MoneyAmount.zero(Currency.ARS)))
                .reduce(MoneyAmount.zero(Currency.ARS), MoneyAmount::add);

        final var total = grouped
                .values()
                .stream()
                .map(o -> o.map(ExpenseDetail::amount)
                        .orElse(MoneyAmount.zero(Currency.USD)))
                .reduce(MoneyAmount.zero(Currency.USD), MoneyAmount::add);
        this.console.appendLine(MessageFormat.format(
                "Total: {0} {1}",
                this.format.currency(total, 14),
                this.format.currency(totalARS, 20)
        ));
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

    private MoneyAmount ars(MoneyAmount usd, YearMonth ym) {
        return ForeignExchanges.getForeignExchange(usd.currency(), Currency.ARS)
                .exchange(usd, Currency.ARS, ym);
    }

    private void expenseDetailReport(ExpenseDetail d) {
        this.console.appendLine(
                MessageFormat.format("{0} {1} {2} {3}",
                        d.ym(),
                        this.format.currency(d.amount(), 14),
                        this.format.currency(ars(d.amount(), d.ym()), 18),
                        d.comment() == null ? "" : d.comment())
        );
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

    private ExpenseTotal regularExpenses(Set<YearMonth> monthSet) {
        var details = this.series.allSpending()
                .entrySet()
                .stream()
                .filter(entry -> SpendingFrequency.of(entry.getKey()) == SpendingFrequency.REGULAR)
                .flatMap(entry -> this.details(monthSet, entry.getValue()))
                .toList();

        final var totalARS = details
                .stream()
                .map(d -> this.ars(d.amount(), d.ym()))
                .reduce(MoneyAmount.zero(Currency.ARS), MoneyAmount::add);

        final var totalUSD = details
                .stream()
                .map(d -> d.amount())
                .reduce(MoneyAmount.zero(Currency.USD), MoneyAmount::add);
        return new ExpenseTotal(totalUSD, totalARS);

    }

    public void regularExpenses(int months) {

        final var monthSet = this.monthSet(months);

        var totals = this.regularExpenses(monthSet);

        this.console.appendLine(this.format.subtitle("Total Regular"));

        this.console.appendLine("Total ",
                this.format.currency(totals.totalUSD, 14),
                " ",
                this.format.currency(totals.totalARS, 18)
        );

        this.console.appendLine("Month ",
                this.format.currency(totals.totalUSD.adjust(BigDecimal.valueOf(months), BigDecimal.ONE), 14),
                " ",
                this.format.currency(totals.totalARS.adjust(BigDecimal.valueOf(months), BigDecimal.ONE), 18)
        );

    }

    private Set<YearMonth> months(int year) {
        return IntStream.rangeClosed(1, 12)
                .mapToObj(m -> YearMonth.of(year, m))
                .collect(Collectors.toSet());
    }

    private ExpenseTotal regularExpensesByYear(int year) {

        return this.regularExpenses(this.months(year));

    }

    public void regularExpensesByYearChart(Savings savings) {
        final var dataset = new DefaultCategoryDataset();

        IntStream.rangeClosed(2008, Year.now().getValue())
                .forEach(year -> {
                    final var months = BigDecimal.valueOf(
                            year < YearMonth.now().getYear()
                            ? 12
                            : YearMonth.now().getMonthValue());
                    final var yearLabel = String.valueOf(year);

                    dataset.addValue(
                            this.regularExpensesByYear(year).totalUSD().amount(),
                            "Gastos",
                            yearLabel);
                    dataset.addValue(
                            savings.yearRegularIncome(year).amount().multiply(months),
                            "Ingresos",
                            yearLabel);
                });

        new BarChart(new ChartStyle(ValueFormat.CURRENCY, Scale.LINEAR))
                .create("Gastos Regulares", "Año", dataset, "spending-regular");
    }

    private Stream<ExpenseDetail> details(Set<YearMonth> monthSet, MoneyAmountSeries series) {
        return series.yearMonthStream()
                .filter(monthSet::contains)
                .map(ym -> new ExpenseDetail(series.getAmountOrElseZero(ym), ym, null));
    }

    private record ExpenseTotal(MoneyAmount totalUSD, MoneyAmount totalARS) {

    }

}
