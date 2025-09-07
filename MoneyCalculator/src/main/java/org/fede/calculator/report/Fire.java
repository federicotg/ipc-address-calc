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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.fede.calculator.chart.ChartStyle;
import org.fede.calculator.chart.LabeledXYDataItem;
import org.fede.calculator.chart.Scale;
import org.fede.calculator.chart.ScatterXYChart;
import org.fede.calculator.chart.ValueFormat;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.SimpleAggregation;
import static org.fede.calculator.money.Currency.USD;
import org.fede.calculator.money.Inflation;
import static org.fede.calculator.money.Inflation.USD_INFLATION;
import static org.fede.calculator.money.MathConstants.C;
import org.fede.calculator.money.PortfolioProjections;
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
        final var limit = USD_INFLATION.getTo();
        var budgets = this.budgets(months);

        final var essential = this.sumExpenses(ESSENTIAL, months);
        final var discretionary = this.sumExpenses(Series.DISCRETIONARY, months);
        final var irregular = this.sumExpenses(Series.IRREGULAR, months);
        final var other = this.sumExpenses(Series.OTHER, months);

        final var totalSavings = this.series.realSavings(null).getAmount(limit);
        final var years = SeriesReader.readInt("retirementHorizon");

        final var expectedFutureIncome = this.expectedFutureIncome();

        this.console.appendLine(this.format.subtitle(months + "-Month Average Spending"));

        final var futureHealth = SeriesReader.readUSD("futureHealth");
        final var futureRent = SeriesReader.readUSD("futureRent");
        final var futurePension = SeriesReader.readUSD("futurePension");

        this.conceptLine("Essential", essential);
        this.conceptLine("Other", other);
        this.conceptLine("Future Rent", futureRent);
        this.conceptLine("Future Health Cost", futureHealth);
        this.conceptLine("Discretionary", discretionary);
        this.conceptLine("Irregular", irregular);
        this.conceptLine("Future Pension", futurePension);

        this.conceptLine("Current Savings", totalSavings);

        //this.conceptLine("Future Savings", new MoneyAmount(futureSavings, USD));
        this.conceptLine("Future Income", new MoneyAmount(expectedFutureIncome, USD));

        var expected10YearCAGR = SeriesReader.readPercent("futureReturn").add(ONE, C);
        var expectedGrowth = expected10YearCAGR.pow(years, C);

        this.console.appendLine(
                String.valueOf(years),
                this.format.text(" Year Growth", 20),
                this.format.percent(expectedGrowth.subtract(ONE, C), 6));

        this.console.appendLine(this.format.subtitle("Portfolio Size by Spending and Withdrawal Percent"));

        this.conceptLine("Curr. " + months + " months", budgets.current(), "❌");
        this.conceptLine("Essential - Rent", budgets.essentialWithoutRent(), "✅");
        this.conceptLine("Everything - Rent", budgets.everythingWithoutRent(), "✅✅");
        this.conceptLine("Essential + Rent", budgets.essentialWithRent(), "✅✅");
        this.conceptLine("Everything + Rent", budgets.everythingWithRent(), "✅✅✅");

        this.console.appendLine("");

        final var percents = this.percents();

        var alreadyThere = Attribute.GREEN_TEXT();
        var withGrowth = Attribute.BRIGHT_YELLOW_TEXT();
        var withGrowthAndIncome = Attribute.YELLOW_TEXT();
        var farAway = Attribute.RED_TEXT();

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
                        expectedFutureIncome,
                        expectedGrowth,
                        percents,
                        alreadyThere,
                        withGrowth,
                        withGrowthAndIncome,
                        farAway
                ))
                .forEach(this.console::appendLine);

        this.console.appendLine(this.format.subtitle("Failsafe Around Prominent Market Peaks"));

        this.refLine("pre-1900", "3.95");
        this.refLine("1900-1910", "3.38");
        this.refLine("1911-1928", "3.57");
        this.refLine("1929", "3.25");
        this.refLine("1964-69", "3.66");
        this.refLine("1972/73", "4.07");
        this.refLine("1999-2000", "3.53");
        this.refLine("2008/09", "4.42");

        new References(console, format)
                .refsLabels(
                        List.of("Savings", "Savings+Growth", "Savings+Growth+Income", "Far Away"),
                        List.of(alreadyThere,
                                withGrowth,
                                withGrowthAndIncome,
                                farAway));
    }

    private void refLine(String label, String pct) {
        this.console.appendLine(
                this.format.text(label, 16),
                this.format.percent(new BigDecimal(pct).movePointLeft(2), 6));
    }

    private List<BigDecimal> percents() {
        final var step = new BigDecimal("0.0025");
        return Stream.concat(
                IntStream.range(12, 19)
                        .mapToObj(i -> new BigDecimal(i).multiply(step, C)),
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
                IntStream.range(5, 21)
                        .map(i -> i * 200)
                        .mapToObj(BigDecimal::new))
                .sorted()
                .toList();
    }

    private SpendingBudgets budgets(int months) {

        final var futureHealth = SeriesReader.readUSD("futureHealth");
        final var futureRent = SeriesReader.readUSD("futureRent");
        final var futurePension = SeriesReader.readUSD("futurePension");

        final var essential = this.sumExpenses(ESSENTIAL, months);
        final var discretionary = this.sumExpenses(Series.DISCRETIONARY, months);
        final var irregular = this.sumExpenses(Series.IRREGULAR, months);
        final var other = this.sumExpenses(Series.OTHER, months);

        final var essentialWithoutRent = essential
                .add(futureHealth)
                .add(other)
                .subtract(futurePension);
        final var essentialWithRent = essentialWithoutRent
                .add(futureRent);
        final var everythingWithoutRent = essential
                .add(futureHealth)
                .add(other)
                .subtract(futurePension)
                .add(discretionary)
                .add(irregular);
        final var everythingWithRent = everythingWithoutRent
                .add(futureRent);
        return new SpendingBudgets(
                essentialWithRent,
                essentialWithoutRent,
                everythingWithRent,
                everythingWithoutRent,
                Stream.of(essential, discretionary, irregular, other)
                        .reduce(MoneyAmount.zero(USD), MoneyAmount::add));
    }

    private MoneyAmount sumExpenses(String type, int months) {

        return this.series.getRealUSDExpensesByType()
                .get(type)
                .stream()
                .reduce(MoneyAmountSeries::add)
                .map(new SimpleAggregation(months)::average)
                .get()
                .getAmountOrElseZero(USD_INFLATION.getTo());

    }

    private void conceptLine(String label, MoneyAmount value) {
        this.conceptLine(label, value, "");
    }

    private void conceptLine(String label, MoneyAmount value, String extra) {
        this.console.appendLine(
                this.format.text(label, 20),
                this.format.currency(value, 12),
                " ", extra);
    }

    private String retirementWithdrawalRow(
            BigDecimal monthlySpending,
            MoneyAmount currentSavings,
            BigDecimal expectedFutureSavings,
            BigDecimal expected10YearGrowth,
            List<BigDecimal> percents,
            Attribute alreadyThere,
            Attribute withGrowth,
            Attribute withGrowthAndIncome,
            Attribute farAway) {
        var annualSpending = monthlySpending.multiply(new BigDecimal(12l), C);
        return this.format.text(this.format.currency(monthlySpending), 12)
                + percents.stream()
                        .map(percent -> annualSpending.divide(percent, C))
                        .map(portfolioLevel -> this.coloredAmount(
                        portfolioLevel,
                        currentSavings,
                        expectedFutureSavings,
                        expected10YearGrowth,
                        alreadyThere,
                        withGrowth,
                        withGrowthAndIncome,
                        farAway))
                        .collect(Collectors.joining());
    }

    private String coloredAmount(
            BigDecimal amount,
            MoneyAmount currentSavings,
            BigDecimal expectedFutureSavings,
            BigDecimal expected10YearGrowth,
            Attribute alreadyThere,
            Attribute withGrowth,
            Attribute withGrowthAndIncome,
            Attribute farAway) {

        var cols = 8;

        if (amount.compareTo(currentSavings.amount()) <= 0) {
            return this.format.center(this.format.currencyShort(amount), cols, new AnsiFormat(alreadyThere));
        } else if (amount.compareTo(currentSavings.amount()
                .multiply(expected10YearGrowth, C)) <= 0) {
            return this.format.center(this.format.currencyShort(amount), cols, new AnsiFormat(withGrowth));
        } else if (amount.compareTo(currentSavings.amount()
                .multiply(expected10YearGrowth, C)
                .add(expectedFutureSavings, C)) <= 0) {
            return this.format.center(this.format.currencyShort(amount), cols, new AnsiFormat(withGrowthAndIncome));
        } else {
            return this.format.center(this.format.currencyShort(amount), cols, new AnsiFormat(farAway));
        }
    }

    private void fireChart(List<XYSeries> ss, String title, String filename) {

        new ScatterXYChart(
                new ChartStyle(ValueFormat.PERCENTAGE, Scale.LINEAR),
                new ChartStyle(ValueFormat.CURRENCY, Scale.LINEAR))
                .create(title,
                        USD,
                        ss.stream().sorted(Comparator.comparingDouble(s -> s.getDataItem(0).getYValue())).toList(),
                        "Spending %", "Porfolio Amount",
                        RectangleEdge.RIGHT,
                        filename);

    }

    public void fireChartFuture() {

        final var totalSavings = this.series.realSavings(null)
                .getAmount(Inflation.USD_INFLATION.getTo());

        var basePct = new BigDecimal("0.0325");
        var monthsInAYear = BigDecimal.valueOf(12l);
        final var expectedFutureIncome = this.expectedFutureIncome();
        final var years = SeriesReader.readInt("retirementHorizon");
        var cagr = SeriesReader.readPercent("futureReturn");
        var vol = SeriesReader.readPercent("futureVolatility");

        var p90Growth = asMonthlySpending(PortfolioProjections.calculatePortfolioPercentile(
                totalSavings.amount().doubleValue(),
                cagr.doubleValue(),
                vol.doubleValue(),
                years,
                0.9d) + expectedFutureIncome.doubleValue());

        var p10Growth = asMonthlySpending(PortfolioProjections.calculatePortfolioPercentile(
                totalSavings.amount().doubleValue(),
                cagr.doubleValue(),
                vol.doubleValue(),
                years,
                0.1d) + expectedFutureIncome.doubleValue());

        var p50Growth = asMonthlySpending(PortfolioProjections.calculatePortfolioPercentile(
                totalSavings.amount().doubleValue(),
                cagr.doubleValue(),
                vol.doubleValue(),
                years,
                0.5d) + expectedFutureIncome.doubleValue());

        // C = Current savings
        // G = Current savings with expected growth percentiles
        // F = future yearly savings + future cash + future prop.
        var ss = List.of(
                this.fireSeries("C+" + years + "y P10 G+F", p10Growth),
                this.fireSeries("C+ " + years + "y P50 G+F", p50Growth),
                this.fireSeries("C+" + years + "y P90 G+F", p90Growth),
                this.fireSeries("C+F", totalSavings
                        .add(new MoneyAmount(expectedFutureIncome, USD))
                        .adjust(monthsInAYear, basePct)),
                this.fireSeries("C", totalSavings.adjust(monthsInAYear, basePct)));

        this.fireChart(ss,
                "Projected FIRE",
                "fire-projected.png");
    }

    private MoneyAmount asMonthlySpending(double portfiolioAmount) {
        return new MoneyAmount(
                BigDecimal.valueOf((portfiolioAmount / 12.0d) * 0.0325d),
                USD);
    }

    public void fireChartBudgets(int months) {

        var budgets = this.budgets(months);

        List<XYSeries> ss = List.of(
                this.fireSeries("Essential-Rent", budgets.essentialWithoutRent()),
                this.fireSeries("Essential+Rent", budgets.essentialWithRent()),
                this.fireSeries("Everything-Rent", budgets.everythingWithoutRent()),
                this.fireSeries("Everything+Rent", budgets.everythingWithRent()),
                this.fireSeries("Curr. Spending", budgets.current()));

        this.fireChart(ss,
                "FIRE " + String.valueOf(months) + " Months",
                "fire-" + String.valueOf(months) + ".png");
    }

    private XYSeries fireSeries(String name, MoneyAmount monthlySpending) {
        var s = new XYSeries(name + " " + this.format.currencyShort(monthlySpending.amount()));

        final var from = new BigDecimal("0.025");
        final var to = new BigDecimal("0.05");
        final var step = new BigDecimal("0.0025");
        Stream.iterate(from, pct -> pct.compareTo(to) <= 0, pct -> pct.add(step))
                .map(portfolioPercent -> this.fireNumber(monthlySpending.amount(), portfolioPercent))
                .forEach(s::add);
        return s;
    }

    public LabeledXYDataItem fireNumber(BigDecimal monthlySpending, BigDecimal portfolioPercent) {
        var fireNumber = monthlySpending
                .multiply(BigDecimal.valueOf(12l), C)
                .divide(portfolioPercent, C);

        return new LabeledXYDataItem(portfolioPercent,
                fireNumber,
                this.format.currencyShort(fireNumber));
    }

    private BigDecimal expectedFutureIncome() {

        final var years = SeriesReader.readInt("retirementHorizon");
        final var futureSavings = SeriesReader.readBigDecimal("futureSavingsByYear")
                .multiply(BigDecimal.valueOf(years), C);

        return SeriesReader.readBigDecimal("futureRealState")
                .add(SeriesReader.readBigDecimal("futureCash"))
                .add(futureSavings);
    }
}
