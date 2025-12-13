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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import org.fede.calculator.chart.ChartStyle;
import org.fede.calculator.chart.LabeledXYDataItem;
import org.fede.calculator.chart.Scale;
import org.fede.calculator.chart.ScatterXYChart;
import org.fede.calculator.chart.ValueFormat;
import org.fede.calculator.money.MoneyAmount;
import static org.fede.calculator.money.Currency.USD;
import static org.fede.calculator.money.Inflation.USD_INFLATION;
import static org.fede.calculator.money.MathConstants.C;
import org.fede.calculator.money.PortfolioProjections;
import org.fede.calculator.money.SlidingWindow;
import static org.fede.calculator.report.Series.ESSENTIAL;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.SeriesReader;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.xy.XYSeries;

/**
 *
 * @author fede
 */
public class Fire {

    private final Format format;
    private final Series series;
    private final Console console;

    public Fire(Format format, Series series, Console console) {
        this.format = format;
        this.series = series;
        this.console = console;
    }

    public void fire(int months) {

        this.console.appendLine(this.format.title("F.I.R.E."));
        final var budgets = this.budgets(months);

        final var essential = this.sumExpenses(ESSENTIAL, months);
        final var discretionary = this.sumExpenses(Series.DISCRETIONARY, months);
        final var irregular = this.sumExpenses(Series.IRREGULAR, months);
        final var other = this.sumExpenses(Series.OTHER, months);
        final var totalSavings = this.series.currentSavingsUSD();

        final var capitalGainsTaxRate = SeriesReader.readPercent("capitalGainsTaxRate");

        final var expectedFutureIncome = this.expectedFutureSavings();

        this.console.appendLine(this.format.subtitle(months + "-Month Average Spending"));

        final var futureHealth = SeriesReader.readUSD("futureHealth");
        final var futureRent = SeriesReader.readUSD("futureRent");

        final var pensionPresentValue = new Pension().discountedCashFlowValue();
        this.conceptLine("Essential", essential);
        this.conceptLine("Other", other);
        this.conceptLine("Future Rent", futureRent);
        this.conceptLine("Future Health Cost", futureHealth);
        this.conceptLine("Discretionary", discretionary);
        this.conceptLine("Irregular", irregular);
        this.conceptLine("Current Savings", totalSavings);
        this.conceptLine("Future Income Present Value", expectedFutureIncome);
        this.conceptLine("Future Pension Present Value", pensionPresentValue);

        this.console.appendLine(
                this.format.text("Capital Gains", 20),
                this.format.percent(capitalGainsTaxRate, 6));

        this.console.appendLine(this.format.subtitle("Portfolio Size by Spending and Withdrawal Percent"));

        this.conceptLine("Curr. " + months + " months", budgets.current(), "❌");
        this.conceptLine("Essential - Rent", budgets.essentialWithoutRent(), "✅");
        this.conceptLine("Everything - Rent", budgets.everythingWithoutRent(), "✅✅");
        if (!futureRent.isZero()) {
            this.conceptLine("Essential + Rent", budgets.essentialWithRent(), "✅✅");
            this.conceptLine("Everything + Rent", budgets.everythingWithRent(), "✅✅✅");
        }
        this.console.appendLine("");

        this.console.appendLine(this.format.subtitle("Failsafe Around Prominent Market Peaks"));

        this.console.appendLine(
                Stream.of(
                        this.refLine("pre-1900", "3.95"),
                        this.refLine("1900-1910", "3.38"),
                        this.refLine("1911-1928", "3.57"),
                        this.refLine("1929", "3.25"),
                        this.refLine("1964-69", "3.66"),
                        this.refLine("1972/73", "4.07"),
                        this.refLine("1999-2000", "3.53"),
                        this.refLine("2008/09", "4.42"))
                        .collect(Collectors.joining(", ")));

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
                        totalSavings.add(pensionPresentValue),
                        totalSavings.add(expectedFutureIncome).add(pensionPresentValue),
                        percents,
                        alreadyThere,
                        withGrowth,
                        withGrowthAndIncome,
                        farAway,
                        budgets
                ))
                .forEach(this.console::appendLine);

        new References(console, format)
                .refsLabels(
                        List.of("Savings", "Savings+Pension", "Savings+Pension+Income", "Far Away"),
                        List.of(alreadyThere,
                                withGrowth,
                                withGrowthAndIncome,
                                farAway));
    }

    private String refLine(String label, String pct) {

        return label 
                + " "
                + this.format.percent(new BigDecimal(pct).movePointLeft(2), 6);
    }

    private List<BigDecimal> percents() {
        final var step = new BigDecimal("0.0025");
        return Stream.concat(
                LongStream.range(12l, 25l)
                        .mapToObj(i -> BigDecimal.valueOf(i).multiply(step, C)),
                Stream.of(
                        new BigDecimal("3.66"),
                        new BigDecimal("3.38"),
                        new BigDecimal("3.53"),
                        new BigDecimal("4.42"),
                        new BigDecimal("4.07"))
                        .map(v -> v.movePointLeft(2)))
                .sorted()
                .toList();
    }

    private List<BigDecimal> spendingAmounts(SpendingBudgets budgets) {
        return Stream.concat(
                budgets.asStream(),
                IntStream.range(12, 41)
                        .map(i -> i * 100)
                        .mapToObj(BigDecimal::valueOf))
                .sorted()
                .toList();
    }

    private SpendingBudgets budgets(int months) {

        final var capitalGainsTaxRate = SeriesReader.readPercent("capitalGainsTaxRate");
        final var costBasisPct = SeriesReader.readPercent("costBasisPct");

        final var futureHealth = SeriesReader.readUSD("futureHealth");
        final var futureRent = SeriesReader.readUSD("futureRent");
        final var essential = this.sumExpenses(ESSENTIAL, months);
        final var discretionary = this.sumExpenses(Series.DISCRETIONARY, months);
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
                        capitalGainsTaxRate, costBasisPct));
    }

    private MoneyAmount adjustforCapitalGains(
            MoneyAmount netAmount,
            BigDecimal capitalGainsTaxRate,
            BigDecimal costBasisPct) {

        var divisor = ONE.subtract(capitalGainsTaxRate
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
                .getAmountOrElseZero(USD_INFLATION.getTo());
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
            MoneyAmount plusPension,
            MoneyAmount plusPensionAndIncome,
            List<BigDecimal> percents,
            Attribute alreadyThere,
            Attribute withGrowth,
            Attribute withGrowthAndIncome,
            Attribute farAway,
            SpendingBudgets budgets) {
        final var annualSpendingFromPortfolio = monthlySpending
                .multiply(new BigDecimal(12l), C);

        Attribute textStyle = budgets.asStream().anyMatch(b -> b.equals(monthlySpending))
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
                        plusPension,
                        plusPensionAndIncome,
                        alreadyThere,
                        withGrowth,
                        withGrowthAndIncome,
                        farAway))
                        .collect(Collectors.joining());
    }

    private String coloredAmount(
            BigDecimal amount,
            MoneyAmount currentPortfolioSize,
            MoneyAmount plusPension,
            MoneyAmount plusPensionAndIncome,
            Attribute alreadyThere,
            Attribute withGrowth,
            Attribute withGrowthAndIncome,
            Attribute farAway) {

        final var cols = 8;

        if (amount.compareTo(currentPortfolioSize.amount()) <= 0) {
            return this.format.center(this.format.currencyShort(amount), cols, new AnsiFormat(alreadyThere));
        } else if (amount.compareTo(plusPension.amount()) <= 0) {
            return this.format.center(this.format.currencyShort(amount), cols, new AnsiFormat(withGrowth));
        } else if (amount.compareTo(plusPensionAndIncome.amount()) <= 0) {
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
        final var basePct = SeriesReader.readPercent("safewithdrawalrate");
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
        return SeriesReader.readPercent("futureReturn")
                .subtract(SeriesReader.readPercent("expectedInflation"), C);
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

        final var vol = SeriesReader.readPercent("futureVolatility");

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
        final var safeWithdrawalRate = SeriesReader.readPercent("safewithdrawalrate").doubleValue();
        return new MoneyAmount(
                BigDecimal.valueOf((portfiolioAmount / 12.0d) * safeWithdrawalRate),
                USD);
    }

    public void fireChartBudgets(int months) {

        final var budgets = this.budgets(months);

        final List<XYSeries> ss = List.of(
                this.fireSeries("Essential-Rent", budgets.essentialWithoutRent()),
                this.fireSeries("Essential+Rent", budgets.essentialWithRent()),
                this.fireSeries("Everything-Rent", budgets.everythingWithoutRent()),
                this.fireSeries("Everything+Rent", budgets.everythingWithRent()),
                this.fireSeries("Curr. Spending", budgets.current()));

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

    /**
     * DCF future income. income.
     *
     * @return
     */
    private MoneyAmount expectedFutureSavings() {

        var r = SeriesReader
                .readBigDecimal("futureReturn")
                .movePointLeft(2)
                .add(ONE)
                .pow(20);
        // 20 years in the future
        return SeriesReader.readUSD("futureRealState")
                .add(SeriesReader.readUSD("futureCash"))
                .adjust(r, ONE);

    }
}
