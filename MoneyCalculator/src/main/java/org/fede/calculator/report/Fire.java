/*
 * Copyright (C) 2025 fede
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

import com.diogonunes.jcolor.AnsiFormat;
import com.diogonunes.jcolor.Attribute;
import java.math.BigDecimal;
import static java.math.BigDecimal.ONE;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;
import java.time.Period;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import org.fede.calculator.chart.ChartStyle;
import org.fede.calculator.chart.LabeledXYDataItem;
import org.fede.calculator.chart.Scale;
import org.fede.calculator.chart.ScatterXYChart;
import org.fede.calculator.chart.ValueFormat;
import org.fede.calculator.money.Currency;
import org.fede.calculator.money.MoneyAmount;
import static org.fede.calculator.money.Currency.USD;
import org.fede.calculator.money.ForeignExchanges;
import static org.fede.calculator.money.MathConstants.C;
import org.fede.calculator.money.PortfolioProjections;
import org.fede.calculator.money.SlidingWindow;
import static org.fede.calculator.report.Series.ESSENTIAL;
import static org.fede.calculator.report.Series.OTHER;
import static org.fede.calculator.report.Series.IRREGULAR;
import static org.fede.calculator.report.Series.DISCRETIONARY;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.SeriesReader;
import static org.fede.calculator.money.series.SeriesReader.readBigDecimal;
import static org.fede.calculator.money.series.SeriesReader.readPercent;
import static org.fede.calculator.money.series.SeriesReader.readUSD;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.xy.XYSeries;

/**
 *
 * @author fede
 */
public class Fire {

    private static final DateTimeFormatter DF = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
            .withLocale(Locale.ENGLISH);

    private final Format format;
    private final Series series;
    private final Console console;

    public Fire(Format format, Series series, Console console) {
        this.format = format;
        this.series = series;
        this.console = console;
    }

    private MoneyAmount futureHealth() {
        return this.health("futureHealth");
    }

    private MoneyAmount currentHealth() {
        return this.health("currentHealth");
    }

    private MoneyAmount health(String price) {
        final var ars = new MoneyAmount(
                readBigDecimal(price)
                        .multiply(BigDecimal.TWO, C)
                        .multiply(BigDecimal.valueOf(121).movePointLeft(2), C),
                Currency.ARS);

        return ForeignExchanges.getForeignExchange(Currency.ARS, USD)
                .exchange(ars, USD, YearMonth.now());
    }

    public void fire(int months) {

        this.console.appendLine(this.format.title("F.I.R.E."));
        final var budgets = this.budgets(months);

        final var essential = this.sumExpenses(ESSENTIAL, months);
        final var discretionary = this.sumExpenses(DISCRETIONARY, months);
        final var irregular = this.sumExpenses(IRREGULAR, months);
        final var other = this.sumExpenses(OTHER, months);
        final var totalSavings = this.series.currentSavingsUSD();

        final var expectedFutureIncome = this.expectedFutureSavings();

        this.console.appendLine(this.format.subtitle(months + "-Month Average Spending"));

        final var futureHealth = this.futureHealth();
        final var currentHealth = this.currentHealth();
        final var futureRent = SeriesReader.readUSD("futureRent");

        final var currentlyEstimated = this.currentlyEstimatedSavings();
        final var currentCash = this.series.realSavings("LIQ").getAmount(YearMonth.now())
                .add(currentlyEstimated);
        final var futureCash = currentCash
                .add(readUSD("futureCash.1"))
                .add(this.severance())
                .add(readUSD("futureCash.3"));

        this.conceptLine("Essential", essential);
        this.conceptLine("Other", other);
        if (!futureRent.isZero()) {
            this.conceptLine("Future Rent", futureRent);
        }
        this.conceptLine("Current Health Cost", currentHealth);
        this.conceptLine("  => Future Health Cost", futureHealth);
        this.conceptLine("Discretionary", discretionary);
        this.conceptLine("Irregular", irregular);

        this.console.appendLine(this.format.subtitle("Savings"));

        this.conceptLine("Current", totalSavings);
        this.conceptLine("  => Current Estimated", currentlyEstimated);
        this.conceptLine("  => Future", expectedFutureIncome);

        var allEquity = this.series.realSavings("EQ").getAmount(YearMonth.now());
        var sp500 = this.lastUSDAmount("ahorros-cspx", YearMonth.now());
        var nonSP500 = allEquity.subtract(sp500);

        var totalSavingAndMetals = totalSavings.add(currentlyEstimated);
        this.console.appendLine("");

        this.compositionLine("SP500", sp500, totalSavingAndMetals);
        this.compositionLine("Other Equity", nonSP500, totalSavingAndMetals);
        this.compositionLine("Cash", currentCash, totalSavingAndMetals);

        this.console.appendLine(this.format.subtitle("Income"));
        this.conceptLine("  => Future Pension DCF", new Pension().discountedCashFlowValue());

        this.console.appendLine(this.format.subtitle("Portfolio Size by Spending and Withdrawal Percent"));

        final var sales1 = readUSD("futureRealState.1");
        final var sales2 = readUSD("futureRealState.2");
        final var sales3 = readUSD("futureRealState.3");

        this.console.appendLine("Spending ", this.format.currency(budgets.currentWithHealth(), 12));
        this.coveredCashLine("Metals", currentlyEstimated, budgets.currentWithHealth(), false);
        this.coveredCashLine("Current cash", currentCash, budgets.currentWithHealth());
        this.coveredCashLine("Current cash & sev.", currentCash
                .add(this.severance()), budgets.currentWithHealth(), false);
        this.console.appendLine("");
        this.coveredCashLine("Future cash", futureCash, budgets.currentWithHealth());
        this.coveredCashLine("Near future sales", futureCash.add(sales1), budgets.currentWithHealth(), false);
        this.coveredCashLine("Future sales", futureCash.add(sales1).add(sales2), budgets.currentWithHealth(), false);
        this.coveredCashLine("All sales", futureCash.add(sales1).add(sales2).add(sales3), budgets.currentWithHealth(), false);

        final var unlp = this.series.incomeSource("unlp");
        final var initialMonth = unlp.yearMonthStream()
                .filter(ym -> !unlp.getAmountOrElseZero(ym).isZero())
                .findFirst()
                .get();
        final var worked = initialMonth.until(YearMonth.now(), ChronoUnit.MONTHS);
        this.console.appendLine(MessageFormat.format(
                "Worked since {3} {0} years and {1} months. {2} months left.",
                Math.floorDiv(worked, 12),
                Math.floorMod(worked, 12),
                (30L * 12L) - worked,
                initialMonth.toString())
        );

        this.currentSustainableARS(
                "Current Monthly Sustainable Portfolio Spending",
                totalSavingAndMetals);

        this.currentSustainableARS(
                "  => With Sev.",
                totalSavingAndMetals.add(this.severance()));

        this.console.appendLine(MessageFormat.format(
                "Current Spending {0}",
                this.format.currency(
                        ForeignExchanges.getMoneyAmountForeignExchange(USD, Currency.ARS)
                                .apply(budgets.currentWithHealth(), YearMonth.now()), 18)));

        //this.console.appendLine(this.format.subtitle("Failsafe Around Prominent Market Peaks"));

        /*this.console.appendLine(
                Stream.of(
                        this.refLine("pre-1900", "3.95"),
                        this.refLine("1900-1910", "3.38"),
                        this.refLine("1911-1928", "3.57"),
                        this.refLine("1929", "3.25"),
                        this.refLine("1964-69", "3.66"),
                        this.refLine("1972/73", "4.07"),
                        this.refLine("1999-2000", "3.53"),
                        this.refLine("2008/09", "4.42"))
                        .collect(Collectors.joining(", ")));*/
        final var percents = this.percents();

        this.console.appendLine("");

        final var alreadyThere = Attribute.GREEN_TEXT();
        final var withGrowth = Attribute.BRIGHT_YELLOW_TEXT();
        final var withGrowthAndIncome = Attribute.YELLOW_TEXT();
        final var farAway = Attribute.RED_TEXT();

        this.console.appendLine(
                Stream.concat(
                        Stream.of(this.format.text("Spending", 12)),
                        percents.stream()
                                .map(pct -> this.format.center(this.format.percent(pct), 8)))
                        .collect(Collectors.joining()));
        this.spendingAmounts(budgets)
                .stream()
                .map(monthlySpending
                        -> this.retirementWithdrawalRow(
                        monthlySpending,
                        totalSavings,
                        totalSavings.add(currentlyEstimated),
                        totalSavings.add(currentlyEstimated).add(expectedFutureIncome),
                        percents,
                        alreadyThere,
                        withGrowth,
                        withGrowthAndIncome,
                        farAway,
                        budgets
                ))
                .forEach(this.console::appendLine);

        //new References(console, format)
        //        .refsLabels(
        //                List.of("Savings", "Savings+Current", "Savings+Current+Future", "Far Away"),
        //                List.of(alreadyThere,
        //                        withGrowth,
        //                        withGrowthAndIncome,
        //                        farAway));
        this.conceptLine("Current Avg. " + months + " months", budgets.current(), "❌");
        this.conceptLine("Current + Health - CGT", budgets.currentWithHealth(), "✅");
        this.conceptLine("Essential", budgets.essentialWithoutRent(), "✅");
        this.conceptLine("Everything", budgets.everythingWithoutRent(), "✅✅");
        if (!futureRent.isZero()) {
            this.conceptLine("Essential + Rent", budgets.essentialWithRent(), "✅✅");
            this.conceptLine("Everything + Rent", budgets.everythingWithRent(), "✅✅✅");
        }

    }

    private void currentSustainableARS(String title, MoneyAmount savings) {
        final var swr = readPercent("safewithdrawalrate");
        final var fx = ForeignExchanges.getMoneyAmountForeignExchange(USD, Currency.ARS);

        this.console.appendLine(MessageFormat.format(
                title + " {0}",
                this.format.currency(
                        fx.apply(
                                new MoneyAmount(
                                        savings.amount()
                                                .multiply(swr, C)
                                                .divide(BigDecimal.valueOf(12L), C),
                                        USD),
                                YearMonth.now()),
                        18)
        ));
    }

    private void compositionLine(String categoryName, MoneyAmount amount, MoneyAmount total) {
        this.console.appendLine(MessageFormat.format(
                "{0} {1} {2}",
                this.format.text(categoryName, 20),
                this.format.currency(amount, 16),
                this.format.percent(amount.amount().divide(total.amount(), C), 12)));
    }

    private MoneyAmount lastAmount(String seriesName, YearMonth ym) {
        return SeriesReader.readSeries("saving/".concat(seriesName).concat(".json")).getAmountOrElseZero(ym);
    }

    private MoneyAmount lastUSDAmount(String seriesName, YearMonth ym) {
        var amount = this.lastAmount(seriesName, ym);
        return ForeignExchanges.getMoneyAmountForeignExchange(amount.currency(), USD)
                .apply(amount, ym);
    }

    private void coveredCashLine(String name, MoneyAmount amount, MoneyAmount essential) {
        this.coveredCashLine(name, amount, essential, true);
    }

    private void coveredCashLine(String name, MoneyAmount amount, MoneyAmount essential, boolean singular) {
        final var coveredMonths = amount
                .adjust(essential.amount(), ONE)
                .amount();

        final BigDecimal[] result = coveredMonths
                .divideAndRemainder(BigDecimal.valueOf(12));

        final var moment = LocalDate.now()
                .plusDays(coveredMonths.multiply(BigDecimal.valueOf(30L)).intValue());

        this.console.appendLine(
                MessageFormat.format(
                        "{3} " + (singular ? "covers" : "cover") + " essentials for {0} years and {1} months until {2}, age {4}.",
                        result[0],
                        result[1].setScale(0, RoundingMode.FLOOR),
                        DF.format(moment),
                        name,
                        Period.between(SeriesReader.readDate("dob"), moment).getYears()
                ));
    }

    private String refLine(String label, String pct) {

        return label
                + " "
                + this.format.percent(new BigDecimal(pct).movePointLeft(2), 6);
    }

    private List<BigDecimal> percents() {
        final var step = BigDecimal.valueOf(25L).movePointLeft(4);
        return Stream.concat(
                LongStream.range(11l, 25l)
                        .mapToObj(i -> BigDecimal.valueOf(i).multiply(step, C)),
                Stream.of(
                        BigDecimal.valueOf(366L),
                        BigDecimal.valueOf(338L),
                        BigDecimal.valueOf(353L),
                        BigDecimal.valueOf(442L),
                        BigDecimal.valueOf(407L))
                        .map(v -> v.movePointLeft(4)))
                .sorted()
                .toList();
    }

    private List<BigDecimal> spendingAmounts(SpendingBudgets budgets) {

        final var fireFrom = SeriesReader.readInt("fire.from");
        final var fireTo = SeriesReader.readInt("fire.to");

        return Stream.concat(
                budgets.asStream(),
                IntStream.iterate(fireFrom, i -> i <= fireTo, i -> i += 100)
                        .mapToObj(BigDecimal::valueOf))
                .sorted()
                .toList();
    }

    public SpendingBudgets budgets(int months) {

        final var capitalGainsTaxRate = readPercent("capitalGainsTaxRate");
        final var costBasisPct = readPercent("costBasisPct");

        final var futureHealth = this.futureHealth();
        final var futureRent = readUSD("futureRent");
        final var essential = this.sumExpenses(ESSENTIAL, months);
        final var discretionary = this.sumExpenses(DISCRETIONARY, months);
        final var irregular = this.sumExpenses(Series.IRREGULAR, months);
        final var other = this.sumExpenses(Series.OTHER, months);

        final var essentialWithoutRent = essential
                .add(futureHealth)
                .add(other);
        final var essentialWithRent = essentialWithoutRent
                .add(futureRent);
        final var everythingWithoutRent = essential
                .add(futureHealth)
                .add(other)
                .add(discretionary)
                .add(irregular);
        final var everythingWithRent = everythingWithoutRent
                .add(futureRent);
        return new SpendingBudgets(
                adjustforCapitalGains(essentialWithRent, capitalGainsTaxRate, costBasisPct),
                adjustforCapitalGains(essentialWithoutRent, capitalGainsTaxRate, costBasisPct),
                adjustforCapitalGains(everythingWithRent, capitalGainsTaxRate, costBasisPct),
                adjustforCapitalGains(everythingWithoutRent, capitalGainsTaxRate, costBasisPct),
                adjustforCapitalGains(Stream.of(essential, discretionary, irregular, other)
                        .reduce(MoneyAmount.zero(USD), MoneyAmount::add),
                        capitalGainsTaxRate, costBasisPct),
                essential
                        .add(this.currentHealth())
                        .add(discretionary)
                        .add(other));
    }

    private MoneyAmount adjustforCapitalGains(
            MoneyAmount netAmount,
            BigDecimal capitalGainsTaxRate,
            BigDecimal costBasisPct) {

        final var divisor = ONE.subtract(capitalGainsTaxRate
                .multiply(ONE.subtract(costBasisPct, C), C), C);

        return netAmount.adjust(divisor, ONE);
    }

    private MoneyAmount sumExpenses(String type, int months) {

        return this.series.getRealUSDExpensesByType()
                .get(type)
                .stream()
                .reduce(MoneyAmountSeries::add)
                .map(new SlidingWindow(months)::average)
                .get()
                .getAmountOrElseZero(YearMonth.now().plusMonths(-1));
    }

    private void conceptLine(String label, MoneyAmount value) {
        this.conceptLine(label, value, "");
    }

    private void conceptLine(String label, MoneyAmount value, String extra) {
        this.console.appendLine(
                this.format.text(label, 30),
                this.format.currency(value, 12),
                " ", extra);
    }

    private String retirementWithdrawalRow(
            BigDecimal monthlySpending,
            MoneyAmount currentPortfolioSize,
            MoneyAmount plusCurrentlyEstimated,
            MoneyAmount plusCurrenttlyEstimatedAndFutureEstamiated,
            List<BigDecimal> percents,
            Attribute alreadyThere,
            Attribute withGrowth,
            Attribute withGrowthAndIncome,
            Attribute farAway,
            SpendingBudgets budgets) {
        final var annualSpendingFromPortfolio = monthlySpending
                .multiply(BigDecimal.valueOf(12l), C);

        final Attribute textStyle = budgets
                .asStream()
                .anyMatch(b -> b.compareTo(monthlySpending) == 0)
                ? Attribute.BOLD()
                : Attribute.DIM();

        return this.format.text(
                this.format.currency(monthlySpending),
                12,
                new AnsiFormat(textStyle))
                + percents.stream()
                        .map(percent -> annualSpendingFromPortfolio.divide(percent, C))
                        .map(portfolioLevel -> this.coloredAmount(
                        portfolioLevel,
                        currentPortfolioSize,
                        plusCurrentlyEstimated,
                        plusCurrenttlyEstimatedAndFutureEstamiated,
                        alreadyThere,
                        withGrowth,
                        withGrowthAndIncome,
                        farAway))
                        .collect(Collectors.joining());
    }

    private String coloredAmount(
            BigDecimal amount,
            MoneyAmount currentPortfolioSize,
            MoneyAmount plusCurrentlyEstimated,
            MoneyAmount plusCurrenttlyEstimatedAndFutureEstamiated,
            Attribute alreadyThere,
            Attribute withGrowth,
            Attribute withGrowthAndIncome,
            Attribute farAway) {

        final var cols = 8;

        if (amount.compareTo(currentPortfolioSize.amount()) <= 0) {
            return this.format.center(this.format.currencyShort(amount), cols, new AnsiFormat(alreadyThere));
        } else if (amount.compareTo(plusCurrentlyEstimated.amount()) <= 0) {
            return this.format.center(this.format.currencyShort(amount), cols, new AnsiFormat(withGrowth));
        } else if (amount.compareTo(plusCurrenttlyEstimatedAndFutureEstamiated.amount()) <= 0) {
            return this.format.center(this.format.currencyShort(amount), cols, new AnsiFormat(withGrowthAndIncome));
        } else {
            return this.format.center(this.format.currencyShort(amount), cols, new AnsiFormat(farAway));
        }
    }

    private void fireChart(List<XYSeries> ss, String title, String subtitle, String filename) {

        new ScatterXYChart(
                new ChartStyle(ValueFormat.PERCENTAGE, Scale.LINEAR),
                new ChartStyle(ValueFormat.CURRENCY, Scale.LINEAR))
                .create(title,
                        subtitle,
                        ss.stream().sorted(Comparator.comparingDouble(s -> s.getDataItem(0).getYValue())).toList(),
                        "Spending %", "Porfolio Amount",
                        RectangleEdge.RIGHT,
                        filename);
    }

    public void fireChartFuture() {

        final var totalSavings = this.series.currentSavingsUSD();
        final var basePct = readPercent("safewithdrawalrate");
        final var monthsInAYear = BigDecimal.valueOf(12l);
        final var expectedFutureIncome = this.expectedFutureSavings();
        final var years = SeriesReader.readInt("retirementHorizon");

        final var p10Spending = this.projectedSafeMonthlySpending(totalSavings, years, 10);
        final var p5Spending = this.projectedSafeMonthlySpending(totalSavings, years, 5);
        final var p50Spending = this.projectedSafeMonthlySpending(totalSavings, years, 50);

        final var ss = List.of(
                this.fireSeries("C P5 F", p5Spending),
                this.fireSeries("C P10 F", p10Spending),
                this.fireSeries("C P50 F", p50Spending),
                this.fireSeries("C F", totalSavings
                        .add(expectedFutureIncome)
                        .adjust(monthsInAYear, basePct)),
                this.fireSeries("C", totalSavings.adjust(monthsInAYear, basePct)));

        this.fireChart(ss,
                "Projected FIRE Number",
                this.format.percent(basePct)
                + " safe withdrawal. "
                + "C: current, P: " + years + "-year returns percentile, F: future income",
                "fire-projected");
    }

    private BigDecimal cagr() {
        return readPercent("futureReturn")
                .subtract(readPercent("expectedInflation"), C);
    }

    /**
     * Safe projected monthly spending
     *
     * @param totalSavings
     * @param years
     * @param percentile
     * @return
     */
    private MoneyAmount projectedSafeMonthlySpending(
            MoneyAmount totalSavings,
            int years,
            int percentile) {

        final var cagr = this.cagr();

        final var vol = readPercent("futureVolatility");

        return asMonthlySpending(
                PortfolioProjections.calculatePortfolioPercentile(
                        totalSavings.amount().doubleValue(),
                        cagr.doubleValue(),
                        vol.doubleValue(),
                        years,
                        (double) percentile / 100.0d)
                + this.expectedFutureSavings().amount().doubleValue());
    }

    private MoneyAmount asMonthlySpending(double portfiolioAmount) {
        final var safeWithdrawalRate = readPercent("safewithdrawalrate").doubleValue();
        return new MoneyAmount(
                BigDecimal.valueOf((portfiolioAmount / 12.0d) * safeWithdrawalRate),
                USD);
    }

    public void fireChartBudgets(int months) {

        final var budgets = this.budgets(months);

        List<XYSeries> ss;

        if (budgets.essentialWithRent().amount().compareTo(budgets.essentialWithoutRent().amount()) != 0) {
            ss = List.of(
                    this.fireSeries("Essential-Rent", budgets.essentialWithoutRent()),
                    this.fireSeries("Essential+Rent", budgets.essentialWithRent()),
                    this.fireSeries("Everything-Rent", budgets.everythingWithoutRent()),
                    this.fireSeries("Everything+Rent", budgets.everythingWithRent()),
                    this.fireSeries("Curr. Spending", budgets.current()));
        } else {
            ss = List.of(
                    this.fireSeries("Essential", budgets.essentialWithoutRent()),
                    this.fireSeries("Everything", budgets.everythingWithoutRent()),
                    this.fireSeries("Curr. Spending", budgets.current()));
        }
        this.fireChart(ss,
                "FIRE  Months",
                "Fire numbers based on last " + months + " months of spending and retirement budget.",
                "fire-" + String.valueOf(months));
    }

    private XYSeries fireSeries(String name, MoneyAmount monthlySpending) {
        final var s = new XYSeries(name + " " + this.format.currencyShort(monthlySpending.amount()));

        final var from = new BigDecimal("0.025");
        final var to = new BigDecimal("0.05");
        final var step = new BigDecimal("0.0025");
        Stream.iterate(from, pct -> pct.compareTo(to) <= 0, pct -> pct.add(step))
                .map(portfolioPercent -> this.fireNumber(monthlySpending.amount(), portfolioPercent))
                .forEach(s::add);
        return s;
    }

    public LabeledXYDataItem fireNumber(BigDecimal monthlySpending, BigDecimal portfolioPercent) {
        final var fireNumber = monthlySpending
                .multiply(BigDecimal.valueOf(12l), C)
                .divide(portfolioPercent, C);

        return new LabeledXYDataItem(portfolioPercent,
                fireNumber,
                this.format.currencyShort(fireNumber));
    }

    private MoneyAmount currentlyEstimatedSavings() {
        return readUSD("xau")
                .adjust(
                        BigDecimal.TWO,
                        readBigDecimal("currentGoldTrOz")
                                .multiply(BigDecimal.valueOf(75)
                                        .movePointLeft(2),
                                        C));
    }

    /**
     * DCF future income.
     *
     * @return
     */
    private MoneyAmount expectedFutureSavings() {

        final var discountRate = readBigDecimal("realstateDiscountRate")
                .movePointLeft(2)
                .add(ONE);

        final var realStateDivisor1 = discountRate
                .pow(readBigDecimal("futureRealStateYears.1").intValue(), C);
        final var realStateDivisor2 = discountRate
                .pow(readBigDecimal("futureRealStateYears.2").intValue(), C);
        final var realStateDivisor3 = discountRate
                .pow(readBigDecimal("futureRealStateYears.3").intValue(), C);

        final var inflation = readPercent("expectedInflation")
                .add(ONE);

        final var cashDivisor1 = inflation
                .pow(readBigDecimal("futureCashYears.1").intValue(), C);

        final var cashDivisor2 = inflation
                .pow(readBigDecimal("futureCashYears.2").intValue(), C);

        final var cashDivisor3 = inflation
                .pow(readBigDecimal("futureCashYears.3").intValue(), C);

        return new Pension().discountedCashFlowValue()
                .add(readUSD("futureRealState.1")
                        .adjust(realStateDivisor1, ONE))
                .add(readUSD("futureRealState.2")
                        .adjust(realStateDivisor2, ONE))
                .add(readUSD("futureRealState.3")
                        .adjust(realStateDivisor3, ONE))
                .add(readUSD("futureCash.1").adjust(cashDivisor1, ONE))
                .add(this.severance().adjust(cashDivisor2, ONE))
                .add(readUSD("futureCash.3").adjust(cashDivisor3, ONE));

    }

    private MoneyAmount severance() {
        final long totalMonths = YearMonth.of(2015, Month.DECEMBER)
                .until(YearMonth.now(), ChronoUnit.MONTHS);

        final long years = Math.floorDiv(totalMonths, 12);

        final long months = Math.floorMod(totalMonths, 12);

        final var toppedSalaries = BigDecimal.valueOf(years + (months > 3 ? 1 : 0));

        var currentMonth = LocalDate.now().getMonthValue();

        var notice = years < 5 ? ONE : BigDecimal.TWO;
        var severanceMonth = ONE;
        var vacationsPart = BigDecimal.valueOf(
                ChronoUnit.DAYS.between(LocalDate.now().withMonth(1).withDayOfMonth(1), LocalDate.now()))
                .divide(BigDecimal.valueOf(365L), C);
        var sacPart = BigDecimal.valueOf(
                ChronoUnit.DAYS.between(LocalDate.now().withMonth(currentMonth > 6 ? 7 : 1).withDayOfMonth(1), LocalDate.now()))
                .divide(BigDecimal.valueOf(180L), C)
                .divide(BigDecimal.TWO, C);
        var sacFactor = ONE
                .add(ONE
                        .divide(BigDecimal.valueOf(12), C), C);

        var taxedSalaries = notice
                .add(severanceMonth, C)
                .add(vacationsPart, C)
                .multiply(sacFactor, C)
                .add(sacPart, C);

        // 67%
        final var maxSalaryFactor = BigDecimal.valueOf(67).movePointLeft(2);

        // 70%
        final var taxSalaryFactor = BigDecimal.valueOf(70).movePointLeft(2);
        final var salary = SeriesReader.readBigDecimal("salary");

        return new MoneyAmount(
                salary
                        .multiply(toppedSalaries, C)
                        .multiply(maxSalaryFactor, C)
                        .add(salary
                                .multiply(taxedSalaries, C)
                                .multiply(taxSalaryFactor, C)),
                USD);
    }
}
