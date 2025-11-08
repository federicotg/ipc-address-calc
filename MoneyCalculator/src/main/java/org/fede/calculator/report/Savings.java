/*
 * Copyright (C) 2022 federicogentile
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

import org.fede.calculator.chart.TimeSeriesDatapoint;
import com.diogonunes.jcolor.Attribute;
import java.io.IOException;
import java.math.BigDecimal;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.fede.calculator.report.Format.format;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.toMap;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.fede.calculator.chart.BarChart;
import org.fede.calculator.chart.ChartSeriesMapper;
import org.fede.calculator.money.Currency;
import org.fede.calculator.money.ForeignExchanges;
import org.fede.calculator.money.Inflation;
import org.fede.calculator.money.MathConstants;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.SimpleAggregation;
import static org.fede.calculator.money.Currency.ARS;
import static org.fede.calculator.money.Currency.USD;
import static org.fede.calculator.money.Inflation.USD_INFLATION;
import static org.fede.calculator.money.MathConstants.C;
import org.fede.calculator.chart.ChartStyle;
import org.fede.calculator.chart.LabeledXYDataItem;
import org.fede.calculator.chart.Scale;
import org.fede.calculator.chart.ScatterXYChart;
import org.fede.calculator.chart.StackedTimeSeriesChart;
import org.fede.calculator.chart.TimeSeriesChart;
import org.fede.calculator.chart.ValueFormat;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.SeriesReader;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import org.fede.calculator.money.series.YearMonthUtil;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;

/**
 *
 * @author federicogentile
 */
public class Savings {

    private final Format format;
    private final Series series;
    private final Bar bar;
    private final Console console;

    public Savings(Format format, Series series, Bar bar, Console console) {
        this.format = format;
        this.series = series;
        this.bar = bar;
        this.console = console;
    }

    private int getScale(int months) {

        return switch (months) {
            case 1 ->
                400;
            case 2 ->
                270;
            case 3, 4, 5, 6 ->
                200;
            case 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24 ->
                100;
            default ->
                50;
        };
    }

    public void netAvgSavingSpent(int months, String title) {

        this.console.appendLine(this.format.title(title));

        final var agg = new SimpleAggregation(months);
        final var income = agg.average(this.series.realIncome());
        final var netSaving = agg.average(this.series.realNetSavings());
        final var spending = agg.average(this.series.realExpenses(null));

        netSaving.forEach((ym, savingMa) -> this.console.appendLine(this.bar.genericBar(ym, this.series(ym, spending, income, savingMa), this.getScale(months))));

        this.savingsRefs(title);

    }

    public void netAvgSavingSpentChart(int months) {

        final var agg = new SimpleAggregation(months);

        final var netSaving = agg.average(this.series.realNetSavings());
        final var spending = agg.average(this.series.realExpenses(null));
        spending.setName("Spending");
        netSaving.setName("Savings");

        new StackedTimeSeriesChart()
                .create(
                        months + "-month Savings and Spending",
                        List.of(spending, agg.average(this.series.realOtherExpenses()), netSaving),
                        "savings-spending" + months);

    }

    public SpendingAndSaving averageSpendingAndSaving(int months) {
        final var agg = new SimpleAggregation(months);
        final var income = agg.average(this.series.realIncome()).getAmount(Inflation.USD_INFLATION.getTo());
        final var netSaving = agg.average(this.series.realNetSavings()).getAmount(Inflation.USD_INFLATION.getTo());
        return new SpendingAndSaving(income.subtract(netSaving), netSaving);
    }

    public void netAvgSavingSpentPct(int months, String title) {

        this.console.appendLine(this.format.title(title));

        final var agg = new SimpleAggregation(months);
        final var income = agg.average(this.series.realIncome());
        final var netSaving = agg.average(this.series.realNetSavings());
        final var spending = agg.average(this.series.realExpenses(null));

        netSaving
                .forEach((ym, savingMa) -> this.console.appendLine(this.bar.percentBar(ym, this.series(ym, spending, income, savingMa))));
        this.savingsRefs(title);
    }

    public void savingsDistributionEvolution() {

        this.console.appendLine(this.format.title("Savings Distribution Evolution"));

        final var cash = this.series.realSavings("LIQ");
        final var eq = this.series.realSavings("EQ");
        final var bo = this.series.realSavings("BO");

        cash.forEach((ym, cashMa) -> this.console.appendLine(
                this.bar.bar(
                        ym,
                        cashMa.amount(),
                        eq.getAmountOrElseZero(ym).amount(),
                        bo.getAmountOrElseZero(ym).amount(),
                        SeriesReader.readInt("scale")
                )));

        this.cashEquityBondsRef("Savings Distribution Evolution");

    }

    private void cashEquityBondsRef(String title) {
        new References(console, format).refs(
                this.format.title(title),
                List.of("Cash", "Equity", "Bonds"),
                List.of(Attribute.BLUE_BACK(), Attribute.RED_BACK(), Attribute.YELLOW_BACK()));
    }

    public void savingsDistributionPercentEvolution() {

        this.console.appendLine(this.format.title("Savings Distribution Percent Evolution"));

        final var cash = this.series.realSavings("LIQ");
        final var eq = this.series.realSavings("EQ");
        final var bo = this.series.realSavings("BO");

        cash.forEach((ym, cashMa) -> this.console.appendLine(
                this.bar.percentBar(ym, cashMa, eq.getAmountOrElseZero(ym), bo.getAmountOrElseZero(ym))
        ));

        this.cashEquityBondsRef("Savings Distribution Percent Evolution");
    }

    private void savingsRefs(String title) {

        new References(console, format).refs(
                title,
                List.of("saved", "spent", "other spending"),
                List.of(Attribute.BLUE_BACK(), Attribute.RED_BACK(), Attribute.YELLOW_BACK()));

    }

    private List<AmountAndColor> series(YearMonth ym, MoneyAmountSeries spending, MoneyAmountSeries income, MoneyAmount savingMa) {
        return List.of(
                new AmountAndColor(spending.getAmountOrElseZero(ym), Attribute.RED_BACK()),
                new AmountAndColor(MoneyAmount.zero(Currency.USD).max(
                        income.getAmountOrElseZero(ym)
                                .subtract(savingMa)
                                .subtract(spending.getAmountOrElseZero(ym))), Attribute.YELLOW_BACK()),
                new AmountAndColor(MoneyAmount.zero(Currency.USD).max(savingMa), Attribute.BLUE_BACK()));
    }

    private void income(int months) {
        final var limit = USD_INFLATION.getTo();
        final var averageRealUSDIncome = this.series.getRegularIncomeSeries()
                .stream()
                .collect(reducing(MoneyAmountSeries::add))
                .map(new SimpleAggregation(months)::average)
                .map(allRealUSDIncome -> allRealUSDIncome.getAmount(YearMonthUtil.min(limit, allRealUSDIncome.getTo())))
                .orElse(MoneyAmount.zero(Currency.USD));

        this.console.appendLine(this.format.title(format("Average {0}-month income in {1}/{2} real USD",
                months,
                limit.getMonth(),
                String.valueOf(limit.getYear()))));

        this.console.appendLine("\tIncome: ",
                averageRealUSDIncome.currency().name(),
                " ",
                this.format.currency(averageRealUSDIncome.amount()));

        final var savingRate = new BigDecimal("0.66");

        final var savingPct = new MoneyAmount(
                averageRealUSDIncome.amount().multiply(savingRate, C),
                averageRealUSDIncome.currency());

        this.console.appendLine(format.percent(savingRate), " saving: ",
                averageRealUSDIncome.currency().name(),
                " ",
                this.format.currency(savingPct.amount()),
                " / ",
                this.format.currency(ForeignExchanges.getMoneyAmountForeignExchange(savingPct.currency(), ARS).apply(savingPct, limit).amount()));

        this.console.appendLine(format("Saved salaries {0}",
                this.series.currentSavingsUSD().amount()
                        .divide(averageRealUSDIncome.amount(), C)));

    }

    public void income(Map<String, String> params) {

        final var months = Integer.parseInt(params.getOrDefault("m", "12"));
        final Runnable otherwise = () -> {
            new Savings(format, series, bar, console).income(months);
            final var totalIncome = this.series.getIncomeSeries()
                    .stream()
                    .flatMap(MoneyAmountSeries::moneyAmountStream)
                    .collect(reducing(MoneyAmount::add))
                    .orElse(MoneyAmount.zero(Currency.USD))
                    .amount();
            this.console.appendLine(format("Total income: {0}", this.format.currency(totalIncome)));
        };
        new By().by(params, this::quarterIncome, this::halfIncome, this::yearlyIncome, otherwise);

    }

    private void yearlyIncome() {
        new Group(console, format, bar).group("Yearly income", this.series.realIncome(), null, ym -> String.valueOf(ym.getYear()), 12);
    }

    private void halfIncome() {
        new Group(console, format, bar)
                .group("Half income", this.series.realIncome(), null, YearMonthUtil::half, 6);
    }

    private void quarterIncome() {
        new Group(console, format, bar)
                .group("Quarterly income", this.series.realIncome(), null, YearMonthUtil::quarter, 3);
    }

    public void incomeAverageBySource(int months) {
        this.incomeAverageBySource(months, false);
    }

    public void incomeAverageBySource(int months, boolean pct) {

        final var title = format("Average {0}-month income by source", months);
        final var colorList = List.of(Attribute.BLUE_BACK(), Attribute.RED_BACK(), Attribute.YELLOW_BACK(), Attribute.GREEN_BACK(), Attribute.WHITE_BACK());
        this.console.appendLine(this.format.title(title));

        final var agg = new SimpleAggregation(months);

        final var unlp = agg.average(this.series.incomeSource("unlp"));
        final var lifia = agg.average(this.series.incomeSource("lifia"));
        final var despARS = agg.average(this.series.incomeSource("despegar"));
        final var despUSD = agg.average(this.series.incomeSource("despegar-split"));
        final var other = agg.average(this.series.incomeSource("other-ars")
                .add(this.series.incomeSource("other-usd")));

        if (pct) {
            unlp.map((ym, ma) -> MoneyAmount.zero(Currency.USD).max(ma))
                    .forEach((ym, savingMa) -> this.console.appendLine(
                    this.bar.percentBar(
                            ym,
                            this.independenSeries(ym, List.of(unlp, lifia, despARS, despUSD, other), colorList))));
        } else {
            var scale = this.incomeScale(months);

            unlp.map((ym, ma) -> MoneyAmount.zero(Currency.USD).max(ma))
                    .forEach((ym, savingMa) -> this.console.appendLine(
                    this.bar.genericBar(
                            ym,
                            this.independenSeries(ym, List.of(unlp, lifia, despARS, despUSD, other), colorList),
                            scale)));
        }
        new References(console, format).refs(
                title,
                List.of("UNLP", "LIFIA", "Despegar ARS", "Despegar USD", "Other"),
                colorList);

    }

    public int incomeScale(int months) {
        return switch (months) {
            case 1, 2 ->
                400;
            case 3, 4, 5 ->
                220;
            case 6, 7 ->
                120;
            case 8, 9, 10 ->
                100;
            case 11, 12 ->
                90;
            default ->
                80;
        };
    }

    public void incomeAccumBySource() {

        final var title = "Accumulated income by source";
        final var colorList = List.of(Attribute.BLUE_BACK(), Attribute.RED_BACK(), Attribute.YELLOW_BACK(), Attribute.GREEN_BACK(), Attribute.WHITE_BACK());
        this.console.appendLine(this.format.title(title));

        final var agg = new SimpleAggregation();

        final var unlp = agg.sum(this.series.incomeSource("unlp"));
        final var other = agg.sum(this.series.incomeSource("other-usd").add(this.series.incomeSource("other-ars")));
        final var lifia = agg.sum(this.series.incomeSource("lifia"));
        final var despARS = agg.sum(this.series.incomeSource("despegar"));
        final var despUSD = agg.sum(this.series.incomeSource("despegar-split"));

        final var maSeries = List.of(unlp, lifia, despARS, despUSD, other);

        unlp.map((ym, ma) -> MoneyAmount.zero(Currency.USD).max(ma))
                .forEach((ym, savingMa) -> this.console.appendLine(this.bar.genericBar(ym, this.independenSeries(ym, maSeries, colorList), 5300)));

        new References(console, format).refs(
                title,
                List.of("UNLP", "LIFIA", "Despegar ARS", "Despegar USD", "Other"),
                colorList);

    }

    public void incomeAccumBySourcePct() {

        final var title = "Accumulated income by source";
        final var colorList = List.of(Attribute.BLUE_BACK(), Attribute.RED_BACK(), Attribute.YELLOW_BACK(), Attribute.GREEN_BACK(), Attribute.WHITE_BACK());
        this.console.appendLine(this.format.title(title));

        final var agg = new SimpleAggregation();

        final var unlp = agg.sum(this.series.incomeSource("unlp"));
        final var other = agg.sum(this.series.incomeSource("other-usd").add(this.series.incomeSource("other-ars")));
        final var lifia = agg.sum(this.series.incomeSource("lifia"));
        final var despARS = agg.sum(this.series.incomeSource("despegar"));
        final var despUSD = agg.sum(this.series.incomeSource("despegar-split"));

        final var maSeries = List.of(unlp, lifia, despARS, despUSD, other);

        unlp.map((ym, ma) -> MoneyAmount.zero(Currency.USD).max(ma))
                .forEach((ym, savingMa) -> this.console.appendLine(this.bar.percentBar(ym, this.independenSeries(ym, maSeries, colorList))));

        new References(console, format).refs(
                title,
                List.of("UNLP", "LIFIA", "Despegar ARS", "Despegar USD", "Other"),
                colorList);

    }

    private List<AmountAndColor> independenSeries(YearMonth ym, List<MoneyAmountSeries> series, List<Attribute> colors) {

        return IntStream.range(0, series.size())
                .mapToObj(i -> new AmountAndColor(MoneyAmount.zero(Currency.USD).max(series.get(i).getAmountOrElseZero(ym)), colors.get(i)))
                .toList();
    }

    public void savingsIncomeTable() {

        final int[] years = new int[]{1, 2, 4, 6, 8, 10, 12, 14, 16};

        final var incomes = IntStream.of(years)
                .mapToObj(i -> Map.entry(i, this.incomeAverage(i)))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        final var savings = IntStream.of(years)
                .mapToObj(i -> Map.entry(i, this.savingsAverage(i)))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        this.console.appendLine(this.format.title("Average Income / Spending"));
        this.console.appendLine(
                this.row(Stream.concat(
                        Stream.of("Years"),
                        IntStream.of(years).mapToObj(y -> format("-= {0} =-", y)))));
        this.console.appendLine(
                this.row(Stream.concat(
                        Stream.of("Income"),
                        IntStream.of(years)
                                .mapToObj(incomes::get)
                                .map(MoneyAmount::amount)
                                .map(this.format::currency))));
        this.console.appendLine(
                this.row(
                        Stream.concat(
                                Stream.of("Savings"),
                                IntStream.of(years)
                                        .mapToObj(savings::get)
                                        .map(MoneyAmount::amount)
                                        .map(this.format::currency))));
        this.console.appendLine(
                this.row(
                        Stream.concat(
                                Stream.of("Spending"),
                                IntStream.of(years)
                                        .mapToObj(y -> incomes.get(y).subtract(savings.get(y)))
                                        .map(MoneyAmount::amount)
                                        .map(this.format::currency))));
        this.console.appendLine(this.row(Stream.concat(Stream.of("Saving %"),
                IntStream.of(years)
                        .mapToObj(y -> savings.get(y).amount().divide(incomes.get(y).amount().subtract(ONE, C), C))
                        .map(this.format::percent))));
    }

    private MoneyAmount incomeAverage(int years) {

        return this.series.getIncomeSeries()
                .stream()
                .collect(reducing(MoneyAmountSeries::add))
                .map(new SimpleAggregation(years * 12)::average)
                .map(allRealUSDIncome -> allRealUSDIncome.getAmount(USD_INFLATION.getTo()))
                .orElse(MoneyAmount.zero(Currency.USD));
    }

    private MoneyAmount savingsAverage(int years) {
        return new SimpleAggregation(years * 12)
                .average(this.series.realNetSavings())
                .getAmount(USD_INFLATION.getTo());
    }

    public void yearSavingsIncomeTable() {

        final int[] years = IntStream.rangeClosed(1999, USD_INFLATION.getTo().getYear()).toArray();

        final var incomes = IntStream.of(years)
                .mapToObj(i -> Map.entry(i, this.yearIncome(i)))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        final var savings = IntStream.of(years)
                .mapToObj(year -> Map.entry(year, this.yearSavings(year)
                .add(year == 2011
                        ? this.spendingAdjustment()
                                .adjust(BigDecimal.valueOf(12), ONE)
                        : MoneyAmount.zero(USD))))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        this.console.appendLine(this.format.title("Income / Spending by Year"));

        this.console.appendLine(this.row(Stream.of("-= Year =-", "Income", "Sav.", "Spend.", "Sav. %", "Sav./Spend.")));

        IntStream.of(years)
                .mapToObj(y -> this.row(Stream.of(format("-= {0} =-", String.valueOf(y) + (y == 2011 || y == USD_INFLATION.getTo().getYear() ? "*" : " ")),
                this.format.currency(incomes.get(y).amount()),
                this.format.currency(savings.get(y).amount()),
                this.format.currency(incomes.get(y).subtract(savings.get(y)).amount()),
                format("{0}", this.format.percent(savings.get(y)
                        .amount()
                        .divide(incomes.get(y)
                                .amount()
                                .subtract(ONE, C), C))),
                this.format.number(savings.get(y).amount()
                        .divide(incomes.get(y)
                                .subtract(savings.get(y))
                                .amount(), C)))))
                .forEach(this.console::appendLine);

        var savedYears = IntStream.of(years)
                .filter(year -> year >= 2007)
                .mapToObj(year
                        -> savings.get(year).amount()
                        .divide(incomes.get(year).amount(), C))
                .map(savingsRate -> savingsRate.divide(ONE.subtract(savingsRate, C), C))
                .reduce(ZERO, BigDecimal::add);
        this.console.appendLine("Saved years ", this.format.number(savedYears));
    }

    private MoneyAmount yearIncome(int year, Supplier<List<MoneyAmountSeries>> supplier) {
        final var months = year < USD_INFLATION.getTo().getYear()
                ? 12
                : USD_INFLATION.getTo().getMonthValue();

        return supplier.get()
                .stream()
                .map(s -> s.filter((ym, ma) -> ym.getYear() == year))
                .flatMap(Function.identity())
                .reduce(MoneyAmount.zero(Currency.USD), MoneyAmount::add)
                .adjust(BigDecimal.valueOf(months), ONE);
    }

    public MoneyAmount yearRegularIncome(int year) {
        return this.yearIncome(year, this.series::getRegularIncomeSeries);
    }

    public MoneyAmount yearIncome(int year) {

        return this.yearIncome(year, this.series::getIncomeSeries);
    }

    private MoneyAmount yearSavings(int year) {

        final var months = year < USD_INFLATION.getTo().getYear()
                ? 12
                : USD_INFLATION.getTo().getMonthValue();

        return this.series.realNetSavings()
                .filter((ym, ma) -> ym.getYear() == year)
                .reduce(MoneyAmount.zero(Currency.USD), MoneyAmount::add)
                .adjust(BigDecimal.valueOf(months), ONE);
    }

    private String row(Stream<String> values) {
        return values
                .map(this::cell)
                .collect(joining());
    }

    private String cell(String value) {
        return String.format("%12s", value);
    }

    private void otherwise() {
        this.console.appendLine(this.format.title("Historical Real USD Savings Stats"));

        final var limit = USD_INFLATION.getTo();

        final var totalSavings = this.series.currentSavingsUSD();
        // total income
        final var totalIncome = this.series.realIncome()
                .moneyAmountStream()
                .reduce(MoneyAmount.zero(Currency.USD), MoneyAmount::add);

        final var months = this.series.realIncome().getFrom().until(limit, ChronoUnit.MONTHS);

        final var avgSalary = totalIncome.amount().divide(BigDecimal.valueOf(months), C);

        this.console.appendLine(format("Income USD {0}\nSavings USD {1} {2}\nAverage salary {3}\nSaved salaries {4}",
                this.format.currency(totalIncome.amount()),
                this.format.currency(totalSavings.amount()),
                this.format.percent(totalSavings.amount().divide(totalIncome.amount(), C)),
                this.format.currency(avgSalary),
                totalSavings.amount().divide(avgSalary, C)));

        //ingreso promedio de N meses
        final var agg = new SimpleAggregation((int) YearMonth.of(2012, 1).until(USD_INFLATION.getTo(), ChronoUnit.MONTHS));

        final var averageIncome = agg.average(this.series.realIncome()).getAmount(USD_INFLATION.getTo());

        // ahorro promedio de N meses
        final var averagNetSavings = agg.average(this.series.realNetSavings()).getAmount(USD_INFLATION.getTo());

        final var m = totalSavings.amount().divide(averageIncome.subtract(averagNetSavings).amount(), C);

        final var MONTHS_IN_ONE_YEAR = BigDecimal.valueOf(12);

        final var yearAndMonth = m.divideAndRemainder(MONTHS_IN_ONE_YEAR, C);

        final var avgIncome = averageIncome.subtract(averagNetSavings).amount();

        this.console.appendLine(format("Projected {0} years and {1} months of USD {3} income (equivalent to {2} of historical real income).",
                yearAndMonth[0],
                yearAndMonth[1].setScale(0, MathConstants.RM),
                this.format.percent(ONE.subtract(averagNetSavings.amount().divide(averageIncome.amount(), C), C)),
                avgIncome));

        final var unlp = SeriesReader.readSeries("income/unlp.json");
        final var despegar = SeriesReader.readSeries("income/despegar.json");

        final var totalYears = Math.round((double) unlp.getFrom().plusMonths(1).until(unlp.getTo(), ChronoUnit.MONTHS) / 12.0d);
        final var simultaneousYears = Math.round((double) despegar.getFrom().until(unlp.getTo(), ChronoUnit.MONTHS) / 12.0d);

        final var simultaneousPercent = new BigDecimal("0.82").divide(new BigDecimal("30"), MathConstants.C);

        final var yearsLeft = SeriesReader.readDate("dob").getYear() + 65 - LocalDate.now().getYear();

        this.console.appendLine(format("Retirement: {0} last 120 average salaries plus {1} best UNLP salary.",
                this.format.percent(BigDecimal.valueOf(totalYears).multiply(new BigDecimal("0.015"), MathConstants.C)),
                this.format.percent(simultaneousPercent
                        .multiply(BigDecimal.valueOf(simultaneousYears), MathConstants.C))));

        this.console.appendLine(format("Projected: {0} last 120 average salaries plus {1} best UNLP salary.",
                this.format.percent(BigDecimal.valueOf(totalYears + yearsLeft).multiply(new BigDecimal("0.015"), MathConstants.C)),
                this.format.percent(simultaneousPercent
                        .multiply(BigDecimal.valueOf(simultaneousYears), MathConstants.C))));

        final long daysWorked = Duration.between(LocalDateTime.of(2015, Month.DECEMBER, 22, 0, 0, 0), LocalDateTime.now()).toDays();
        final long yearsWorked = daysWorked / 365 + ((daysWorked % 365 > 180) ? 1 : 0);

        final var contingency = BigDecimal.valueOf(yearsWorked)
                .multiply(new BigDecimal(2900))
                .add(new BigDecimal(100000));

        final var contingencyMonths = contingency.divide(avgIncome, C).setScale(0, MathConstants.RM);

        final var contingencyYearsAndmonths = contingencyMonths.divideAndRemainder(MONTHS_IN_ONE_YEAR, C);

        this.console.appendLine(format("Contingency: USD {0,number,currency}, {1,number} years and {2,number} months of income.",
                contingency,
                contingencyYearsAndmonths[0],
                contingencyYearsAndmonths[1].setScale(0, MathConstants.RM)));

        final var contingencyDate = LocalDateTime.now().plusMonths(contingencyMonths.longValue());
        final var retirementDate = SeriesReader.readDate("dob").atTime(23, 59, 59).plusYears(65);
        final long gapYears = Math.ceilDiv(Duration.between(contingencyDate, retirementDate).toDays(), 365l);

        this.console.appendLine(format("Gap is {0,number} years.", gapYears));

        final var currentIlliquidAssets = new MoneyAmount(new BigDecimal("75000"), Currency.USD);// 50% 43 y d80

        final var futureIlliquidAssets = new MoneyAmount(new BigDecimal("137500"), Currency.USD);// 50% 47 53 moreno colon

        // 50% caja, severance y deuda casa
        final var futureCash = new MoneyAmount(new BigDecimal("69320"), Currency.USD);

        this.console.appendLine(format("Future est. net worth is {0,number,currency}.",
                totalSavings
                        .add(currentIlliquidAssets)
                        .add(futureCash)
                        .add(futureIlliquidAssets).amount()));

    }

    public void savings(Map<String, String> params) {

        new By().by(params, this::quarterSavings, this::halfSavings, this::yearlySavings, this::otherwise);
    }

    private void quarterSavings() {

        new Group(console, format, bar)
                .group("Net quarter savings", this.series.realNetSavings(), this.series.realIncome(), YearMonthUtil::quarter, 3);
    }

    private void halfSavings() {

        new Group(console, format, bar)
                .group("Net half savings", this.series.realNetSavings(), this.series.realIncome(), YearMonthUtil::half, 6);
    }

    private void yearlySavings() {

        new Group(console, format, bar)
                .group("Net yearly savings", this.series.realNetSavings(), this.series.realIncome(), ym -> String.valueOf(ym.getYear()), 12);
    }

    // increase in real USD -  rolling N months
    public void incomeDelta(int months) {
        final var title = format("{0}-month real USD income change over {0}-month real income average.", months);
        this.console.appendLine(this.format.title(title));

        final var allIncomeSeries = this.series.getIncomeSeries().stream().reduce(MoneyAmountSeries::add).get();
        final var agg = new SimpleAggregation(months);
        final var average = agg.average(allIncomeSeries);
        final var change = agg.change(average);
        final var limit = Inflation.USD_INFLATION.getTo();
        average.forEachNonZero((ym, ch) -> {
            if (ym.compareTo(limit) <= 0) {
                percentEvolutionReport(
                        ym,
                        change.getAmount(ym).amount().divide(average.getAmount(ym).amount(), C),
                        1);
            }
        });

        this.console.appendLine(this.format.title(title));

    }

    public void savingsPercentChange(int months) {

        this.console.appendLine(this.format.title(format("{0}-month Savings Change", months - 1)));
        final var s = new SimpleAggregation(months)
                .percentChange(this.series.realSavings(null));

        var ym = s.getFrom();
        var limit = s.getTo();

        while (ym.compareTo(limit) <= 0) {
            this.percentEvolutionReport(ym, s.getIndex(ym), months / 24);
            ym = ym.plusMonths(1);
        }
    }

    private void percentEvolutionReport(YearMonth ym, BigDecimal ma, int scale) {

        this.console.appendLine(
                format("{0}/{1}", String.valueOf(ym.getYear()), String.format("%02d", ym.getMonthValue())),
                " ",
                this.format.percent(ma, 8),
                " ",
                this.bar.bar(ma.movePointRight(2), Math.max(1, scale)));
    }

    public void monthlySavings(int months) {

        final var title = format("Average {0}-month net monthly savings", months);
        this.console.appendLine(this.format.title(title));

        this.bar.evolution(title,
                new SimpleAggregation(months).average(this.series.realNetSavings()),
                120);
    }

    public void averageSavedSalaries(int months) {

        final var title = format("Average {0}-month real USD saved salaries", months);
        this.console.appendLine(this.format.title(title));

        final var savings = this.series.realSavings(null);
        final var income = new SimpleAggregation(months).average(this.series.realRegularIncome());

        this.bar.evolution(title,
                income.map((ym, ma) -> new MoneyAmount(savings.getAmountOrElseZero(ym).amount().divide(ONE.max(ma.amount()), C), ma.currency())),
                2);
    }

    public void incomeAverageEvolution(int months, boolean ars) {
        this.console.appendLine(this.format.title(format("Average {0}-month income evolution", months)));

        final var baseBarSize = this.incomeScale(months);

        final var s = ars
                ? this.series.realIncome().exchangeInto(Currency.ARS)
                : this.series.realIncome();

        final var barSize = ars
                ? Math.round((float) (baseBarSize - 10) / ForeignExchanges.USD_ARS.exchange(new MoneyAmount(ONE, Currency.ARS), Currency.USD, Inflation.USD_INFLATION.getTo()).amount().floatValue())
                : baseBarSize;

        this.bar.evolution(format("Average {0}-month income {1}", months, ars ? "ARS" : "USD"),
                new SimpleAggregation(months).average(s),
                barSize);
    }

    public record SpendingAndSaving(MoneyAmount spending, MoneyAmount saving) {

    }

    private MoneyAmount spendingAdjustment() {

        var arsSavings = SeriesReader.readSeries("saving/ahorros-peso.json");

        var ym = YearMonth.of(2011, 8);

        var ars = arsSavings.getAmount(ym.plusMonths(-1))
                .subtract(arsSavings.getAmount(ym));

        var usd = ForeignExchanges.getForeignExchange(ARS, USD).exchange(ars, USD, ym);
        return Inflation.USD_INFLATION.adjust(usd, ym, Inflation.USD_INFLATION.getTo());

    }

    public void spendingByYear() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        Map<Integer, BigDecimal> values = new HashMap<>();
        final var months = new BigDecimal(12);
        IntStream.rangeClosed(2007, USD_INFLATION.getTo().getYear())
                .forEach(year
                        -> values.put(year - 2000,
                        this.yearIncome(year)
                                .subtract(this.yearSavings(year))
                                .amount()
                                .multiply(months)
                                .subtract(year == 2011
                                        ? this.spendingAdjustment().amount()
                                        : ZERO)));

        var avg = values.values()
                .stream()
                .reduce(ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(values.size()), C);
        values.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e
                        -> dataset.addValue(e.getValue(),
                        "Spending",
                        e.getKey() == 11 ? "11(*)" : e.getKey().toString()));

        dataset.addValue(avg,
                "Spending",
                "Avg.");

        new BarChart(new ChartStyle(ValueFormat.CURRENCY, Scale.LINEAR))
                .create("Spending", "Year", dataset, "spending");

    }

    public void spendingByRegularIncomeChart() throws IOException {

        final var ss = new XYSeries("Year");

        IntStream.rangeClosed(2007, USD_INFLATION.getTo().getYear())
                .forEach(year
                        -> ss.add(
                        new LabeledXYDataItem(
                                this.yearRegularIncome(year).amount(),
                                this.yearIncome(year)
                                        .subtract(this.yearSavings(year))
                                        .subtract(year == 2011
                                                ? new MoneyAmount(this.spendingAdjustment().amount()
                                                        .divide(BigDecimal.valueOf(12), C), USD)
                                                : MoneyAmount.zero(USD))
                                        .adjust(this.yearRegularIncome(year).amount(), ONE)
                                        .amount(),
                                year == 2011 ? "2011(*)" : String.valueOf(year)
                        )));

        new ScatterXYChart(new ChartStyle(ValueFormat.CURRENCY, Scale.LINEAR),
                new ChartStyle(ValueFormat.PERCENTAGE, Scale.LINEAR))
                .create(
                        "Spending by Regular Income",
                        USD,
                        List.of(ss),
                        "Income",
                        "Spending",
                        "by-income-spending");
    }

    public void savingsByIncomeChart() throws IOException {

        final var ss = new XYSeries("Year");
        final var months = BigDecimal.valueOf(12);
        IntStream.rangeClosed(2007, USD_INFLATION.getTo().getYear())
                .forEach(year -> {
                    ss.add(new LabeledXYDataItem(
                            this.yearRegularIncome(year).amount(),
                            this.yearSavings(year)
                                    .add(year == 2011
                                            ? new MoneyAmount(this.spendingAdjustment().amount()
                                                    .divide(months, C), USD)
                                            : MoneyAmount.zero(USD))
                                    .adjust(this.yearRegularIncome(year).amount(), ONE).amount(),
                            year == 2011 ? "2011(*)" : String.valueOf(year)));
                });

        new ScatterXYChart(
                new ChartStyle(ValueFormat.CURRENCY, Scale.LINEAR),
                new ChartStyle(ValueFormat.PERCENTAGE, Scale.LINEAR))
                .create(
                        "Saving by Regular Income",
                        USD,
                        List.of(ss),
                        "Income",
                        "Savings",
                        "by-income-savings");
    }

    public void savingRate(int months) {

        Function<MoneyAmountSeries, MoneyAmountSeries> avg = new SimpleAggregation(months)::average;

        var regularAvgIncome = this.series.getRegularIncomeSeries()
                .stream()
                .reduce(MoneyAmountSeries::add)
                .map(avg)
                .get();

        var regularAvgExpenses = Stream.concat(
                this.series.getRealUSDExpenses().stream(),
                Stream.of(this.series.realOtherExpenses()))
                .reduce(MoneyAmountSeries::add)
                .map(avg)
                .get();

        final var from = YearMonth.of(2007, 1);
        final var to = Inflation.USD_INFLATION.getTo();

        List<TimeSeriesDatapoint> spendingRate = new ArrayList<>((int) from.until(to, ChronoUnit.MONTHS));
        List<TimeSeriesDatapoint> savingRate = new ArrayList<>((int) from.until(to, ChronoUnit.MONTHS));

        Stream.iterate(from, ym -> ym.compareTo(to) <= 0, ym -> ym.plusMonths(1))
                .forEach(ym -> this.addRate(ym, regularAvgIncome, regularAvgExpenses, spendingRate, savingRate));

        new TimeSeriesChart(new ChartStyle(ValueFormat.PERCENTAGE, Scale.LINEAR))
                .createFromTimeSeries(
                        "Saving Rate " + months + " Month Average",
                        List.of(ChartSeriesMapper.asTimeSeries(spendingRate, "Spending Rate"),
                                ChartSeriesMapper.asTimeSeries(savingRate, "Saving Rate")),
                        "saving-rate-" + (months < 10 ? "0" : "") + months);
    }

    private void addRate(
            YearMonth ym,
            MoneyAmountSeries income,
            MoneyAmountSeries spending,
            List<TimeSeriesDatapoint> spendingRates,
            List<TimeSeriesDatapoint> savingRates) {

        var spendingRate = spending.getAmount(ym).amount()
                .divide(income.getAmount(ym).amount(), C)
                .min(ONE)
                .max(ZERO);

        spendingRates.add(new TimeSeriesDatapoint(ym, spendingRate));
        savingRates.add(new TimeSeriesDatapoint(ym, ONE.subtract(spendingRate)));

    }
}
