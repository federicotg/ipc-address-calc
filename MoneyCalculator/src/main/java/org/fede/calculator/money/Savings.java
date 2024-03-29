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
package org.fede.calculator.money;

import com.diogonunes.jcolor.Attribute;
import java.math.BigDecimal;
import static java.math.BigDecimal.ONE;
import java.math.RoundingMode;
import static java.text.MessageFormat.format;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.toMap;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import static org.fede.calculator.money.Inflation.USD_INFLATION;
import static org.fede.calculator.money.MathConstants.C;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.SeriesReader;
import org.fede.calculator.money.series.YearMonth;

/**
 *
 * @author federicogentile
 */
public class Savings {

    private static final MoneyAmount ZERO_USD = MoneyAmount.zero("USD");

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
            case 1, 2, 3 ->
                100;
            case 4, 5, 6 ->
                65;
            case 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24 ->
                50;
            default ->
                40;
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

        final var nf = NumberFormat.getCurrencyInstance();

        cash.forEach((ym, cashMa) -> this.console.appendLine(
                this.bar.bar(
                        ym,
                        cashMa.getAmount(),
                        eq.getAmountOrElseZero(ym).getAmount(),
                        bo.getAmountOrElseZero(ym).getAmount(),
                        1500,
                        value -> String.format("%13s", nf.format(value)))));

        this.cashEquityBondsRef("Savings Distribution Evolution");

    }

    private void cashEquityBondsRef(String title) {
        new References(console, format).refs(
                this.format.title(title),
                List.of("Cash", "equity", "bonds"),
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
                new AmountAndColor(ZERO_USD.max(
                        income.getAmountOrElseZero(ym)
                                .subtract(savingMa)
                                .subtract(spending.getAmountOrElseZero(ym))), Attribute.YELLOW_BACK()),
                new AmountAndColor(ZERO_USD.max(savingMa), Attribute.BLUE_BACK()));
    }

    private void income(int months) {
        final var limit = USD_INFLATION.getTo();
        final var averageRealUSDIncome = this.series.getIncomeSeries()
                .stream()
                .collect(reducing(MoneyAmountSeries::add))
                .map(new SimpleAggregation(months)::average)
                .map(allRealUSDIncome -> allRealUSDIncome.getAmount(limit.min(allRealUSDIncome.getTo())))
                .orElse(ZERO_USD);

        this.console.appendLine(this.format.title(format("Average {0}-month income in {1}/{2} real USD",
                months,
                limit.getMonth(),
                String.valueOf(limit.getYear()))));

        this.console.appendLine("\tIncome: ",
                averageRealUSDIncome.getCurrency(),
                " ",
                this.format.currency(averageRealUSDIncome.getAmount()));

        final var savingPct = new MoneyAmount(averageRealUSDIncome.getAmount().multiply(new BigDecimal("0.5"), C), averageRealUSDIncome.getCurrency());

        this.console.appendLine("50% saving: ",
                averageRealUSDIncome.getCurrency(),
                " ",
                this.format.currency(savingPct.getAmount()),
                " / ",
                this.format.currency(ForeignExchanges.getMoneyAmountForeignExchange(savingPct.getCurrency(), "ARS").apply(savingPct, limit).getAmount()));

        this.console.appendLine(format("Saved salaries {0}",
                this.series.realSavings(null).getAmount(limit).getAmount()
                        .divide(averageRealUSDIncome.getAmount(), C)));

    }

    public void income(Map<String, String> params) {

        final var months = Integer.parseInt(params.getOrDefault("m", "12"));
        final Runnable otherwise = () -> {
            new Savings(format, series, bar, console).income(months);
            final var totalIncome = this.series.getIncomeSeries()
                    .stream()
                    .flatMap(MoneyAmountSeries::moneyAmountStream)
                    .collect(reducing(MoneyAmount::add))
                    .orElse(ZERO_USD)
                    .getAmount();
            this.console.appendLine(format("Total income: {0}", this.format.currency(totalIncome)));
        };
        new By().by(params, this::quarterIncome, this::halfIncome, this::yearlyIncome, otherwise);

    }

    private void yearlyIncome() {
        new Group(console, format, bar).group("Yearly income", this.series.realIncome(), null, ym -> String.valueOf(ym.getYear()), 12);
    }

    private void halfIncome() {
        new Group(console, format, bar).group("Half income", this.series.realIncome(), null, YearMonth::half, 6);
    }

    private void quarterIncome() {
        new Group(console, format, bar).group("Quarterly income", this.series.realIncome(), null, YearMonth::quarter, 3);
    }

    public void incomeAverageBySource(int months) {

        final var title = format("Average {0}-month income by source", months);
        final var colorList = List.of(Attribute.BLUE_BACK(), Attribute.RED_BACK(), Attribute.YELLOW_BACK(), Attribute.GREEN_BACK());
        this.console.appendLine(this.format.title(title));

        final var agg = new SimpleAggregation(months);

        final var unlp = agg.average(this.series.incomeSource("unlp"));
        final var lifia = agg.average(this.series.incomeSource("lifia"));
        final var despARS = agg.average(this.series.incomeSource("despegar"));
        final var despUSD = agg.average(this.series.incomeSource("despegar-split"));

        unlp.map((ym, ma) -> ZERO_USD.max(ma))
                .forEach((ym, savingMa) -> this.console.appendLine(this.bar.genericBar(ym, this.independenSeries(ym, List.of(unlp, lifia, despARS, despUSD), colorList), 25)));

        new References(console, format).refs(
                title,
                List.of("UNLP", "LIFIA", "Despegar ARS", "Despegar USD"),
                colorList);

    }

    private List<AmountAndColor> independenSeries(YearMonth ym, List<MoneyAmountSeries> series, List<Attribute> colors) {

        return IntStream.range(0, series.size())
                .mapToObj(i -> new AmountAndColor(ZERO_USD.max(series.get(i).getAmountOrElseZero(ym)), colors.get(i)))
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
                                .map(MoneyAmount::getAmount)
                                .map(this.format::currency))));
        this.console.appendLine(
                this.row(
                        Stream.concat(
                                Stream.of("Savings"),
                                IntStream.of(years)
                                        .mapToObj(savings::get)
                                        .map(MoneyAmount::getAmount)
                                        .map(this.format::currency))));
        this.console.appendLine(
                this.row(
                        Stream.concat(
                                Stream.of("Spending"),
                                IntStream.of(years)
                                        .mapToObj(y -> incomes.get(y).subtract(savings.get(y)))
                                        .map(MoneyAmount::getAmount)
                                        .map(this.format::currency))));
        this.console.appendLine(this.row(Stream.concat(Stream.of("Saving %"),
                IntStream.of(years)
                        .mapToObj(y -> savings.get(y).getAmount().divide(incomes.get(y).getAmount().subtract(ONE, C), C))
                        .map(this.format::percent))));
    }

    private MoneyAmount incomeAverage(int years) {

        return this.series.getIncomeSeries()
                .stream()
                .collect(reducing(MoneyAmountSeries::add))
                .map(new SimpleAggregation(years * 12)::average)
                .map(allRealUSDIncome -> allRealUSDIncome.getAmount(USD_INFLATION.getTo()))
                .orElse(ZERO_USD);
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
                .mapToObj(i -> Map.entry(i, this.yearSavings(i)))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        this.console.appendLine(this.format.title("Income / Spending by Year"));

        this.console.appendLine(this.row(Stream.of("-= Year =-", "Income", "Sav.", "Spend.", "Sav. %", "Sav./Spend.")));

        IntStream.of(years)
                .mapToObj(y -> this.row(Stream.of(format("-= {0} =-", String.valueOf(y) + (y == USD_INFLATION.getTo().getYear() ? "*" : "")),
                this.format.currency(incomes.get(y).getAmount()),
                this.format.currency(savings.get(y).getAmount()),
                this.format.currency(incomes.get(y).subtract(savings.get(y)).getAmount()),
                format("{0}", this.format.percent(savings.get(y).getAmount()
                        .divide(incomes.get(y).getAmount()
                                .subtract(ONE, C), C))),
                this.format.number(savings.get(y).getAmount().divide(incomes.get(y).subtract(savings.get(y)).getAmount(), C)))))
                .forEach(this.console::appendLine);
    }

    private MoneyAmount yearIncome(int year) {

        final var months = year < USD_INFLATION.getTo().getYear()
                ? 12
                : USD_INFLATION.getTo().getMonth();

        return this.series.getIncomeSeries()
                .stream()
                .map(s -> s.filter((ym, ma) -> ym.getYear() == year))
                .flatMap(Function.identity())
                .reduce(ZERO_USD, MoneyAmount::add)
                .adjust(BigDecimal.valueOf(months), ONE);
    }

    private MoneyAmount yearSavings(int year) {

        final var months = year < USD_INFLATION.getTo().getYear()
                ? 12
                : USD_INFLATION.getTo().getMonth();

        return this.series.realNetSavings()
                .filter((ym, ma) -> ym.getYear() == year)
                .reduce(ZERO_USD, MoneyAmount::add)
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

    public void savings(Map<String, String> params) {

        Runnable otherwise = () -> {

            this.console.appendLine(this.format.title("Historical Real USD Savings Stats"));

            final var limit = USD_INFLATION.getTo();

            final var totalSavings = this.series.realSavings(null).getAmount(limit);

            // total income
            final var totalIncome = this.series.realIncome()
                    .moneyAmountStream()
                    .reduce(ZERO_USD, MoneyAmount::add);

            final var months = this.series.realIncome().getFrom().monthsUntil(limit);

            final var avgSalary = totalIncome.getAmount().divide(BigDecimal.valueOf(months), C);

            this.console.appendLine(format("Income USD {0}\nSavings USD {1} {2}\nAverage salary {3}\nSaved salaries {4}",
                    this.format.currency(totalIncome.getAmount()),
                    this.format.currency(totalSavings.getAmount()),
                    this.format.percent(totalSavings.getAmount().divide(totalIncome.getAmount(), C)),
                    this.format.currency(avgSalary),
                    totalSavings.getAmount().divide(avgSalary, C)));

            //ingreso promedio de N meses
            final var agg = new SimpleAggregation(YearMonth.of(2012, 1).monthsUntil(USD_INFLATION.getTo()));

            final var averageIncome = agg.average(this.series.realIncome()).getAmount(USD_INFLATION.getTo());

            // ahorro promedio de N meses
            final var averagNetSavings = agg.average(this.series.realNetSavings()).getAmount(USD_INFLATION.getTo());

            final var m = totalSavings.getAmount().divide(averageIncome.subtract(averagNetSavings).getAmount(), C);

            final var MONTHS_IN_ONE_YEAR = BigDecimal.valueOf(12);
            
            final var yearAndMonth = m.divideAndRemainder(MONTHS_IN_ONE_YEAR, C);

            final var avgIncome = averageIncome.subtract(averagNetSavings).getAmount();
            
            this.console.appendLine(format("Projected {0} years and {1} months of USD {3} income (equivalent to {2} of historical real income).",
                    yearAndMonth[0],
                    yearAndMonth[1].setScale(0, MathConstants.RM),
                    this.format.percent(ONE.subtract(averagNetSavings.getAmount().divide(averageIncome.getAmount(), C), C)),
                    avgIncome));

            final var unlp = SeriesReader.readSeries("income/unlp.json");
            final var despegar = SeriesReader.readSeries("income/despegar.json");

            final var totalYears = Math.round((double) unlp.getFrom().next().monthsUntil(unlp.getTo()) / 12.0d);
            final var simultaneousYears = Math.round((double) despegar.getFrom().monthsUntil(despegar.getTo()) / 12.0d);

            final var simultaneousPercent = new BigDecimal("0.82").divide(new BigDecimal("30"), MathConstants.C);

            final var yearsLeft = 1978 + 65 - LocalDate.now().getYear();

            this.console.appendLine(format("Retirement: {0} last 120 average salaries plus {1} best UNLP salary.",
                    this.format.percent(BigDecimal.valueOf(totalYears).multiply(new BigDecimal("0.015"), MathConstants.C)),
                    this.format.percent(simultaneousPercent
                            .multiply(BigDecimal.valueOf(simultaneousYears), MathConstants.C))));

            this.console.appendLine(format("Projected: {0} last 120 average salaries plus {1} best UNLP salary.",
                    this.format.percent(BigDecimal.valueOf(totalYears + yearsLeft).multiply(new BigDecimal("0.015"), MathConstants.C)),
                    this.format.percent(simultaneousPercent
                            .multiply(BigDecimal.valueOf(simultaneousYears + yearsLeft), MathConstants.C))));

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
            final var retiementDate = LocalDateTime.of(1978+65, Month.MARCH, 13,23,59,59);
            
            final long gapYears = Math.ceilDiv(Duration.between(contingencyDate, retiementDate).toDays(), 365l);
            
            this.console.appendLine(format("Gap is {0,number} years.", gapYears));
            
            
        };

        new By().by(params, this::quarterSavings, this::halfSavings, this::yearlySavings, otherwise);
    }

    private void quarterSavings() {

        new Group(console, format, bar).group("Net quarter savings", this.series.realNetSavings(), this.series.realIncome(), YearMonth::quarter, 3);
    }

    private void halfSavings() {

        new Group(console, format, bar).group("Net half savings", this.series.realNetSavings(), this.series.realIncome(), YearMonth::half, 6);
    }

    private void yearlySavings() {

        new Group(console, format, bar).group("Net yearly savings", this.series.realNetSavings(), this.series.realIncome(), ym -> String.valueOf(ym.getYear()), 12);
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
                percentEvolutionReport(ym, change.getAmount(ym).getAmount().divide(average.getAmount(ym).getAmount(), C));
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
            this.percentEvolutionReport(ym, s.getIndex(ym.getYear(), ym.getMonth()));
            ym = ym.next();
        }
    }

    private void percentEvolutionReport(YearMonth ym, BigDecimal ma) {

        this.console.appendLine(
                format("{0}/{1}", String.valueOf(ym.getYear()), String.format("%02d", ym.getMonth())),
                " ",
                this.format.percent(ma, 8),
                " ",
                this.bar.bar(ma.movePointRight(2), 1));
    }

    public void monthlySavings(int months) {

        final var title = format("Average {0}-month net monthly savings", months);
        this.console.appendLine(this.format.title(title));

        this.bar.evolution(title,
                new SimpleAggregation(months).average(this.series.realNetSavings()),
                100);
    }

    public void averageSavedSalaries(int months) {

        final var title = format("Average {0}-month real USD saved salaries", months);
        this.console.appendLine(this.format.title(title));

        final var savings = new SimpleAggregation(months).average(this.series.realSavings(null));
        final var income = new SimpleAggregation(months).average(this.series.realIncome());

        this.bar.evolution(title,
                income.map((ym, ma) -> new MoneyAmount(savings.getAmountOrElseZero(ym).getAmount().divide(ONE.max(ma.getAmount()), C), ma.getCurrency())),
                2);
    }

    public void incomeAverageEvolution(int months, boolean ars) {
        this.console.appendLine(this.format.title(format("Average {0}-month income evolution", months)));
        int baseBarSize = 40;

        if (months < 6) {
            baseBarSize = 50;
        }

        final var s = ars
                ? this.series.realIncome().exchangeInto("ARS")
                : this.series.realIncome();

        final var barSize = ars
                ? Math.round((float) (baseBarSize - 10) / ForeignExchanges.USD_ARS.exchange(new MoneyAmount(ONE, "ARS"), "USD", Inflation.USD_INFLATION.getTo()).getAmount().floatValue())
                : baseBarSize;

        this.bar.evolution(format("Average {0}-month income {1}", months, ars ? "ARS" : "USD"),
                new SimpleAggregation(months).average(s),
                barSize);
    }

    public record SpendingAndSaving(MoneyAmount spending, MoneyAmount saving) {

    }
}
