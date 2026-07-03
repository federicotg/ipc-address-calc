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
import java.time.LocalDate;
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

    private static final MoneyAmount ZERO_USD = MoneyAmount.zero(USD);

    private static final DateTimeFormatter DF = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
            .withLocale(Locale.ENGLISH);

    private final Format format;
    private final Series series;
    private final Console console;
    private final LastAmounts last = new LastAmounts();

    public Fire(Format format, Series series, Console console) {
        this.format = format;
        this.series = series;
        this.console = console;
    }

    private MoneyAmount futureHealth() {
        return Future.futureHealth();
    }

    private MoneyAmount currentHealth() {
        return Future.contingencyHealth();
    }

    public void fi(int months) {

        final var budgets = this.budgets(months);
        final var essential = this.sumExpenses(ESSENTIAL, months);
        final var discretionary = this.sumExpenses(DISCRETIONARY, months);
        final var irregular = this.sumExpenses(IRREGULAR, 3 * months);
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

        this.conceptLine("Essential", essential);
        this.conceptLine("Other", other);
        if (!futureRent.isZero()) {
            this.conceptLine("Future Rent", futureRent);
        }
        this.conceptLine("Current Health Cost", currentHealth);
        this.conceptLine(" ➞ Future Health Cost", futureHealth);
        this.conceptLine("Discretionary", discretionary);
        this.conceptLine("Irregular (*)", irregular);

        this.console.appendLine("");

        this.conceptLine("Current " + months + " months",
                budgets.current(),
                this.format.currency(
                        ForeignExchanges.getMoneyAmountForeignExchange(USD, Currency.ARS)
                                .apply(budgets.current(), YearMonth.now()), 18)
        );

        this.conceptLine("Current + Health - CGT",
                budgets.currentWithHealth(),
                this.format.currency(
                        ForeignExchanges.getMoneyAmountForeignExchange(USD, Currency.ARS)
                                .apply(budgets.currentWithHealth(), YearMonth.now()), 18)
        );

        this.conceptLine("Future Essential",
                budgets.essentialWithoutRent(),
                this.format.currency(
                        ForeignExchanges.getMoneyAmountForeignExchange(USD, Currency.ARS)
                                .apply(budgets.essentialWithoutRent(), YearMonth.now()), 18)
        );

        this.console.appendLine(this.format.subtitle("Savings"));

        this.conceptLine("Current", totalSavings);
        this.conceptLine(" ➞ Current Estimated", currentlyEstimated);
        this.conceptLine("   ➞ Future", expectedFutureIncome);

        var allEquity = this.series.realSavings("EQ").getAmount(YearMonth.now());
        var sp500 = this.last.lastAmount(Currency.CSPX);
        var nonSP500 = allEquity.subtract(sp500);

        var totalSavingAndMetals = totalSavings.add(currentlyEstimated);
        this.console.appendLine("");

        this.compositionLine("SP500", sp500, totalSavingAndMetals);
        this.compositionLine("Other Equity", nonSP500, totalSavingAndMetals);
        this.compositionLine("Cash", currentCash, totalSavingAndMetals);
        this.console.appendLine(
                this.format.text("Deposit", 21),
                this.format.currency(
                        currentCash
                                .add(Future.severance(BigDecimal.valueOf(66).movePointLeft(2)).getTotal())
                                .subtract(new MoneyAmount(BigDecimal.valueOf(60000L), USD)),
                        16));

        this.console.appendLine(this.format.subtitle("Income"));

        final var sev = Future.severance();

        final var futureCash = currentCash
                .add(readUSD("futureCash.1"))
                .add(sev.getTotal())
                .add(readUSD("futureCash.3"));

        final var sales1 = readUSD("futureRealState.1");
        final var sales2 = readUSD("futureRealState.2");
        final var sales3 = readUSD("futureRealState.3");

        this.coveredCashLine("Metals", currentlyEstimated, budgets.currentWithHealth(), false);
        this.coveredCashLine("Current cash", currentCash, budgets.currentWithHealth());
        this.coveredCashLine(" ? Current cash & sev.", currentCash
                .add(sev.getTotal()), budgets.currentWithHealth(), false);
        this.coveredCashLine(" ➞ Future cash", futureCash, budgets.currentWithHealth());
        this.coveredCashLine("   ➞ Near future sales", futureCash.add(sales1), budgets.currentWithHealth(), false);
        this.coveredCashLine("     ➞ Future sales", futureCash.add(sales1).add(sales2), budgets.currentWithHealth(), false);
        this.coveredCashLine("       ➞ All sales", futureCash.add(sales1).add(sales2).add(sales3), budgets.currentWithHealth(), false);
        this.conceptLine(" ➞ Future Pension DCF", new Pension().discountedCashFlowValue());

    }

    private void spendingReport(String title, BigDecimal wr, MoneyAmount totalPortfolioValue) {
        final int col = 20;
        final var spending = totalPortfolioValue
                .adjust(ONE, wr.divide(BigDecimal.valueOf(12L), C));

        this.console.appendLine(MessageFormat.format("{0}{1}{2}{3}",
                this.format.text(title, col),
                this.format.currency(spending, col),
                this.format.currency(ForeignExchanges.getForeignExchange(USD, Currency.ARS).exchange(spending, Currency.ARS, YearMonth.now()), col),
                this.format.percent(wr, 6)
        ));

    }

    public void re(int months) {

        final var unlp = this.series.incomeSource("unlp");
        final var initialMonth = unlp.yearMonthStream()
                .filter(ym -> !unlp.getAmountOrElseZero(ym).isZero())
                .findFirst()
                .get();
        final var worked = initialMonth.until(YearMonth.now(), ChronoUnit.MONTHS);

        final BigDecimal[] result = BigDecimal.valueOf((30L * 12L) - worked)
                .divideAndRemainder(BigDecimal.valueOf(12L));

        this.console.appendLine(MessageFormat.format(
                "Worked since {3} {0} years and {1} months. {2} months left ({4} years and {5} months).",
                Math.floorDiv(worked, 12),
                Math.floorMod(worked, 12),
                (30L * 12L) - worked,
                initialMonth.toString(),
                result[0],
                result[1])
        );

        final var budgets = this.budgets(months);
        final var currentlyEstimated = this.currentlyEstimatedSavings();
        final var currentSavings = this.series.currentSavingsUSD();
        final var totalSavingsPlusCurrentlyEstimated = currentSavings
                .add(currentlyEstimated);

        var swr = new CAEYSafeWithdrawalRate();
        this.spendingReport("Current", this.withdrawalRate(swr), totalSavingsPlusCurrentlyEstimated);

        this.spendingReport(" ➞ All Equity", this.allEquityWithdrawalRate(swr), totalSavingsPlusCurrentlyEstimated);

        var additional = Future.expectedWealth();
        this.spendingReport(" ➞ Future", this.futureWealthWithdrawalRate(swr, additional), totalSavingsPlusCurrentlyEstimated.add(additional));

        this.spendingReport(" ➞ Future less cash", this.futureWealthWithLessCashWithdrawalRate(swr, additional), totalSavingsPlusCurrentlyEstimated.add(additional));

        this.console.appendLine(MessageFormat.format(
                "{0}{1}{2}",
                this.format.text("Current Spending", 20),
                this.format.currency(budgets.currentWithHealth(), 20),
                this.format.currency(
                        ForeignExchanges.getMoneyAmountForeignExchange(USD, Currency.ARS)
                                .apply(budgets.currentWithHealth(), YearMonth.now()), 20)
        ));

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
        final var expectedFutureIncome = this.expectedFutureSavings();
        this.spendingAmounts(budgets)
                .stream()
                .map(monthlySpending
                        -> this.retirementWithdrawalRow(
                        monthlySpending,
                        currentSavings,
                        totalSavingsPlusCurrentlyEstimated,
                        totalSavingsPlusCurrentlyEstimated.add(expectedFutureIncome),
                        percents,
                        alreadyThere,
                        withGrowth,
                        withGrowthAndIncome,
                        farAway,
                        budgets
                ))
                .forEach(this.console::appendLine);

        this.conceptLine("Current Avg. " + months + " months", budgets.current(), "❌");
        this.conceptLine("Current + Health - CGT", budgets.currentWithHealth(), "✅");
        this.conceptLine("Essential", budgets.essentialWithoutRent(), "✅");
        this.conceptLine("Everything", budgets.everythingWithoutRent(), "✅✅");
        final var futureRent = SeriesReader.readUSD("futureRent");
        if (!futureRent.isZero()) {
            this.conceptLine("Essential + Rent", budgets.essentialWithRent(), "✅✅");
            this.conceptLine("Everything + Rent", budgets.everythingWithRent(), "✅✅✅");
        }

    }

    private BigDecimal allEquityWithdrawalRate(CAEYSafeWithdrawalRate swr) {

        var now = YearMonth.now();

        var equity = last.last();

        var cash = this.series.realSavings("LIQ").getAmount(now)
                .add(this.currentlyEstimatedSavings());

        var bonds = this.series.realSavings("BO").getAmountOrElseZero(now);

        var allCash = bonds.add(cash);

        return swr.capeWR(
                equity.us().add(allCash.adjust(ONE, equity.usWeight())),
                equity.exUs().add(allCash.adjust(ONE, equity.exUsWeight())),
                equity.em().add(allCash.adjust(ONE, equity.emWeight())),
                ZERO_USD,
                ZERO_USD);

    }

    private BigDecimal futureWealthWithdrawalRate(CAEYSafeWithdrawalRate swr, MoneyAmount additional) {

        var now = YearMonth.now();

        var equity = last.last();

        var cash = this.series.realSavings("LIQ").getAmount(now)
                .add(this.currentlyEstimatedSavings());

        var bonds = this.series.realSavings("BO").getAmountOrElseZero(now);

        return swr.capeWR(
                equity.us().add(additional.adjust(ONE, equity.usWeight())),
                equity.exUs().add(additional.adjust(ONE, equity.exUsWeight())),
                equity.em().add(additional.adjust(ONE, equity.emWeight())),
                bonds,
                cash);
    }

    private BigDecimal futureWealthWithLessCashWithdrawalRate(CAEYSafeWithdrawalRate swr, MoneyAmount additional) {

        var now = YearMonth.now();

        var equity = last.last();

        var cash = this.series.realSavings("LIQ").getAmount(now)
                .add(this.currentlyEstimatedSavings());

        var bonds = this.series.realSavings("BO").getAmountOrElseZero(now);

        var minCash = new MoneyAmount(BigDecimal.valueOf(60000l), Currency.USD)
                .min(cash.add(bonds));

        var everything = additional.add(cash).add(bonds).subtract(minCash);

        return swr.capeWR(
                equity.us().add(everything.adjust(ONE, equity.usWeight())),
                equity.exUs().add(everything.adjust(ONE, equity.exUsWeight())),
                equity.em().add(everything.adjust(ONE, equity.emWeight())),
                ZERO_USD,
                minCash);
    }

    private BigDecimal withdrawalRate() {
        return this.withdrawalRate(new CAEYSafeWithdrawalRate());
    }

    private BigDecimal withdrawalRate(CAEYSafeWithdrawalRate swr) {

        var now = YearMonth.now();

        var equity = last.last();

        var cash = this.series.realSavings("LIQ").getAmount(now)
                .add(this.currentlyEstimatedSavings());

        var bonds = this.series.realSavings("BO").getAmountOrElseZero(now);

        return swr.capeWR(equity.us(), equity.exUs(), equity.em(), bonds, cash);
    }

    public void fiReport(int months) {
        this.console.appendLine(this.format.title("F.I.R.E."));
        this.fi(months);

    }

    public void reReport(int months) {
        this.console.appendLine(this.format.title("F.I.R.E."));
        this.re(months);

    }

    public void fire(int months) {

        this.console.appendLine(this.format.title("F.I.R.E."));

        this.fi(months);
        this.re(months);

    }

    private void compositionLine(String categoryName, MoneyAmount amount, MoneyAmount total) {
        this.console.appendLine(MessageFormat.format(
                "{0} {1} {2}",
                this.format.text(categoryName, 20),
                this.format.currency(amount, 16),
                this.format.percent(amount.amount().divide(total.amount(), C), 12)));
    }

    private void coveredCashLine(String name, MoneyAmount amount, MoneyAmount essential) {
        this.coveredCashLine(name, amount, essential, true);
    }

    private void coveredCashLine(String name, MoneyAmount amount, MoneyAmount essential, boolean singular) {
        final var coveredMonths = amount
                .adjust(essential.amount(), ONE)
                .amount();

        final BigDecimal[] result = coveredMonths
                .divideAndRemainder(BigDecimal.valueOf(12L));

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

    private List<BigDecimal> percents() {
        final var swr = new CAEYSafeWithdrawalRate();
        final var step = BigDecimal.valueOf(25L).movePointLeft(4);
        final var futureWealth = Future.expectedWealth();
        return Stream.concat(
                LongStream.range(13L, 20L)
                        .mapToObj(i -> BigDecimal.valueOf(i).multiply(step, C)),
                Stream.concat(
                        Stream.of(
                                this.withdrawalRate(),
                                this.futureWealthWithdrawalRate(swr, futureWealth),
                                this.allEquityWithdrawalRate(swr),
                                this.futureWealthWithLessCashWithdrawalRate(swr, futureWealth)),
                        Stream.of(
                                BigDecimal.valueOf(366L),
                                BigDecimal.valueOf(338L),
                                BigDecimal.valueOf(353L),
                                BigDecimal.valueOf(442L),
                                BigDecimal.valueOf(407L))
                                .map(v -> v.movePointLeft(4))))
                .map(BigDecimal::stripTrailingZeros)
                .distinct()
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

    private SpendingBudgets budgets(int months) {

        final var capitalGainsTaxRate = readPercent("capitalGainsTaxRate");
        final var costBasisPct = readPercent("costBasisPct");

        final var futureHealth = this.futureHealth();
        final var futureRent = readUSD("futureRent");
        final var essential = this.sumExpenses(ESSENTIAL, months);
        final var discretionary = this.sumExpenses(DISCRETIONARY, months);
        final var irregular = this.sumExpenses(Series.IRREGULAR, 3 * months);
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
                //essentialWithRent
                adjustforCapitalGains(essentialWithRent, capitalGainsTaxRate, costBasisPct),
                //essentialWithoutRent
                adjustforCapitalGains(essentialWithoutRent, capitalGainsTaxRate, costBasisPct),
                //everythingWithRent
                adjustforCapitalGains(everythingWithRent, capitalGainsTaxRate, costBasisPct),
                //everythingWithoutRent
                adjustforCapitalGains(everythingWithoutRent, capitalGainsTaxRate, costBasisPct),
                //current
                adjustforCapitalGains(Stream.of(essential, discretionary, irregular, other)
                        .reduce(ZERO_USD, MoneyAmount::add),
                        capitalGainsTaxRate, costBasisPct),
                //currentWithHealth
                essential
                        .add(this.currentHealth())
                        .add(discretionary)
                        .add(irregular)
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
        final var basePct = this.withdrawalRate();//readPercent("safewithdrawalrate");
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
        final var safeWithdrawalRate = this.withdrawalRate().doubleValue();//readPercent("safewithdrawalrate").doubleValue();
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

        return Future.expectedWealth();
    }

}
