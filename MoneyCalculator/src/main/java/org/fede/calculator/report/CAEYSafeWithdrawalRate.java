/*
 * Copyright (C) 2026 fede
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
import static java.math.BigDecimal.ONE;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Stream;
import org.fede.calculator.chart.ChartStyle;
import org.fede.calculator.chart.LabeledXYDataItem;
import org.fede.calculator.chart.Scale;
import org.fede.calculator.chart.ScatterXYChart;
import org.fede.calculator.chart.ValueFormat;
import static org.fede.calculator.money.Currency.USD;
import static org.fede.calculator.money.MathConstants.C;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.series.SeriesReader;
import org.jfree.data.xy.XYSeries;

/**
 *
 * @author fede
 */
public class CAEYSafeWithdrawalRate {

    private static final BigDecimal US_EQUITY_SHARE = new BigDecimal("0.75");
    private static final BigDecimal DEV_EX_US_SHARE = new BigDecimal("0.15");
    private static final BigDecimal EMERGING_SHARE = new BigDecimal("0.10");

    private static final BigDecimal PORTFOLIO_MIN = new BigDecimal("500000");
    private static final BigDecimal PORTFOLIO_MAX = new BigDecimal("1000000");
    private static final BigDecimal PORTFOLIO_STEP = new BigDecimal("50000");
    private static final BigDecimal EQUITY_MIN = new BigDecimal("0.70");
    private static final BigDecimal EQUITY_MAX = ONE;
    private static final BigDecimal EQUITY_STEP = new BigDecimal("0.05");
    private static final BigDecimal CAPE_MIN = new BigDecimal("15");
    private static final BigDecimal CAPE_MAX = new BigDecimal("45");
    private static final BigDecimal CAPE_STEP = new BigDecimal("5");
    private static final BigDecimal MONTHS_IN_A_YEAR = new BigDecimal("12");
    private static final NumberFormat PCT_FORMAT2 = NumberFormat.getPercentInstance();
    private static final NumberFormat PCT_FORMAT = NumberFormat.getPercentInstance();

    static {
        PCT_FORMAT2.setMinimumFractionDigits(2);
    }

    private final BigDecimal cashRealYield;
    private final BigDecimal bondsRealYield;
    private final BigDecimal cape;
    private final BigDecimal capeExUs;
    private final BigDecimal capeEmerging;

    private final BigDecimal capeA;
    private final BigDecimal capeB;

    private final LastAmounts last = new LastAmounts();

    public CAEYSafeWithdrawalRate(BigDecimal expectedInflation, BigDecimal bondsNominalYield, BigDecimal cape, BigDecimal capeExUs, BigDecimal capeEmerging) {
        this.cashRealYield = expectedInflation.negate(C);
        this.bondsRealYield = bondsNominalYield.subtract(expectedInflation, C);
        this.cape = cape;
        this.capeExUs = capeExUs;
        this.capeEmerging = capeEmerging;

        final var age = BigDecimal.valueOf(ChronoUnit.MONTHS.between(SeriesReader.readDate("dob"), LocalDate.now())).divide(MONTHS_IN_A_YEAR, C);
        final var yearsLeft = BigDecimal.ONE.movePointRight(2).subtract(age, C);

        this.capeA = new BigDecimal("0.04")
                .min(ONE
                        .divide(yearsLeft, C).multiply(BigDecimal.valueOf(80).movePointLeft(2), C));
        this.capeB = SeriesReader.readPercent("cape.b");

    }

    public CAEYSafeWithdrawalRate() {
        this(SeriesReader.readPercent("expectedInflation"),
                SeriesReader.readPercent("bond10"),
                SeriesReader.readBigDecimal("cape"),
                SeriesReader.readBigDecimal("cape.exus"),
                SeriesReader.readBigDecimal("cape.em"));
    }

    public BigDecimal capeWR(
            MoneyAmount usEquity,
            MoneyAmount devExUs,
            MoneyAmount emerging,
            MoneyAmount bonds,
            MoneyAmount cash) {

        final var caey = this.caey(usEquity, devExUs, emerging);

        final var totalPortfolioValue = Stream.of(usEquity, devExUs, emerging, cash, bonds)
                .reduce(MoneyAmount.zero(USD), MoneyAmount::add);

        return this.capeA
                .add(this.capeB.multiply(caey, C))
                .add(this.bondsRealYield.multiply(bonds.adjust(totalPortfolioValue.amount(), ONE).amount(), C))
                .add(this.cashRealYield.multiply(cash.adjust(totalPortfolioValue.amount(), ONE).amount(), C));
    }

    public BigDecimal monthlySafeWithdrawal(MoneyAmount portfolio, BigDecimal equityPct) {
        final var allocation = this.portfolioFor(portfolio, equityPct);
        return this.monthlySafeWithdrawal(
                allocation.usEquity(),
                allocation.devExUs(),
                allocation.emerging(),
                allocation.bonds(),
                allocation.cash());
    }

    public BigDecimal monthlySafeWithdrawal(
            MoneyAmount usEquity,
            MoneyAmount devExUs,
            MoneyAmount emerging,
            MoneyAmount bonds,
            MoneyAmount cash) {

        final var totalPortfolioValue = Stream.of(usEquity, devExUs, emerging, cash, bonds)
                .reduce(MoneyAmount.zero(USD), MoneyAmount::add);

        return totalPortfolioValue.amount()
                .multiply(this.capeWR(usEquity, devExUs, emerging, bonds, cash), C)
                .divide(MONTHS_IN_A_YEAR, C);
    }

    private PortfolioAllocation portfolioFor(MoneyAmount portfolio, BigDecimal equityPct) {
        final var equity = portfolio.adjust(ONE, equityPct);
        final var cash = portfolio.subtract(equity);
        return new PortfolioAllocation(
                equity.adjust(ONE, US_EQUITY_SHARE),
                equity.adjust(ONE, DEV_EX_US_SHARE),
                equity.adjust(ONE, EMERGING_SHARE),
                MoneyAmount.zero(USD),
                cash);
    }

    public void monthlySafeWithdrawalChart(String filename) {
        final var series = new XYSeries("Monthly Safe Withdrawal");
        final var currencyFormat = ValueFormat.CURRENCY.format();

        Stream.iterate(PORTFOLIO_MIN, amount -> amount.compareTo(PORTFOLIO_MAX) <= 0, amount -> amount.add(PORTFOLIO_STEP))
                .flatMap(amount -> Stream.iterate(EQUITY_MIN, equityPct -> equityPct.compareTo(EQUITY_MAX) <= 0, equityPct -> equityPct.add(EQUITY_STEP))
                .map(equityPct -> {
                    var portfolio = new MoneyAmount(amount, USD);
                    var monthly = this.monthlySafeWithdrawal(portfolio, equityPct);
                    return new LabeledXYDataItem(
                            amount,
                            equityPct,
                            currencyFormat.format(monthly));
                }))
                .forEach(series::add);

        new ScatterXYChart(
                new ChartStyle(ValueFormat.CURRENCY, Scale.LINEAR),
                new ChartStyle(ValueFormat.PERCENTAGE, Scale.LINEAR))
                .create(
                        this.reportTitle(),
                        List.of(series),
                        "Portfolio (USD)",
                        "Equity %",
                        filename);
    }

    public void monthlySafeWithdrawalByCapeChart(String filename) {
        var now = YearMonth.now();
        var series = new Series();

        var currentlyEstimated = this.currentlyEstimatedSavings();

        var equity = last.last();
        var cash = series.realSavings("LIQ").getAmount(now)
                .add(currentlyEstimated);

        var bonds = series.realSavings("BO").getAmountOrElseZero(now);

        final var expectedInflation = SeriesReader.readPercent("expectedInflation");
        final var bondsNominalYield = SeriesReader.readPercent("bond10");
        final var currencyFormat = ValueFormat.CURRENCY.format();

        final var byCape = new XYSeries("Monthly Safe Withdrawal by CAPE");
        Stream.iterate(CAPE_MIN, capeVal -> capeVal.compareTo(CAPE_MAX) <= 0, capeVal -> capeVal.add(CAPE_STEP))
                .forEach(capeVal -> {
                    var swr = new CAEYSafeWithdrawalRate(expectedInflation, bondsNominalYield, capeVal, capeVal, capeVal);
                    var monthly = swr.monthlySafeWithdrawal(equity.us(), equity.exUs(), equity.em(), bonds, cash);
                    byCape.add(new LabeledXYDataItem(
                            capeVal,
                            monthly,
                            currencyFormat.format(monthly)));
                });

        // current 
        final var currentSwr = new CAEYSafeWithdrawalRate();
        final var currentCape = ONE.divide(currentSwr.caey(equity.us(), equity.exUs(), equity.em()), C);
        final var currentMonthly = currentSwr.monthlySafeWithdrawal(equity.us(), equity.exUs(), equity.em(), bonds, cash);
        final var current = new XYSeries("Current SWR");
        current.add(new LabeledXYDataItem(
                currentCape,
                currentMonthly,
                currencyFormat.format(currentMonthly)));

        // current + total wealth SW
        var additionalWealth = Future.expectedWealth();

        var usTotal = equity.us().add(additionalWealth.adjust(ONE, equity.usWeight()));
        var exUsTotal = equity.exUs().add(additionalWealth.adjust(ONE, equity.exUsWeight()));
        var emTotal = equity.em().add(additionalWealth.adjust(ONE, equity.emWeight()));

        final var totalWealthMonthly = currentSwr.monthlySafeWithdrawal(usTotal, exUsTotal, emTotal, bonds, cash);
        final var totalWealth = new XYSeries("Total Wealth SWR");
        totalWealth.add(new LabeledXYDataItem(
                currentCape,
                totalWealthMonthly,
                currencyFormat.format(totalWealthMonthly)));

        // all cash
        var allCash = cash.add(bonds);
        var usWithCash = equity.us().add(allCash.adjust(ONE, equity.usWeight()));
        var exUswithCash = equity.exUs().add(allCash.adjust(ONE, equity.exUsWeight()));
        var emWithCash = equity.em().add(allCash.adjust(ONE, equity.emWeight()));

        final var allEquityMonthly = currentSwr.monthlySafeWithdrawal(
                usWithCash,
                exUswithCash,
                emWithCash,
                MoneyAmount.zero(USD), 
                MoneyAmount.zero(USD));
        final var allEquity = new XYSeries("All Equity SWR");
        allEquity.add(new LabeledXYDataItem(
                currentCape,
                allEquityMonthly,
                currencyFormat.format(allEquityMonthly)));

        new ScatterXYChart(
                new ChartStyle(ValueFormat.NUMBER, Scale.LINEAR),
                new ChartStyle(ValueFormat.CURRENCY, Scale.LINEAR))
                .create(
                        this.reportTitle(),
                        List.of(byCape, current, totalWealth, allEquity),
                        "CAPE",
                        "Monthly Withdrawal (USD)",
                        filename);
    }

    private String reportTitle() {
        return MessageFormat.format(
                "Safe Withdrawal ({0} + {1} x CAEY)",
                PCT_FORMAT2.format(this.capeA),
                PCT_FORMAT.format(this.capeB));
    }

    private MoneyAmount currentlyEstimatedSavings() {
        return SeriesReader.readUSD("xau")
                .adjust(
                        BigDecimal.TWO,
                        SeriesReader.readBigDecimal("currentGoldTrOz")
                                .multiply(BigDecimal.valueOf(75)
                                        .movePointLeft(2),
                                        C));
    }

    private record PortfolioAllocation(
            MoneyAmount usEquity,
            MoneyAmount devExUs,
            MoneyAmount emerging,
            MoneyAmount bonds,
            MoneyAmount cash) {

    }

    private BigDecimal caey(
            MoneyAmount usEquity,
            MoneyAmount devExUs,
            MoneyAmount emerging) {

        final var totalEquity = usEquity.add(devExUs).add(emerging).amount();

        return Stream.of(
                ONE.divide(this.cape, C).multiply(usEquity.adjust(totalEquity, ONE).amount(), C),
                ONE.divide(this.capeExUs, C).multiply(devExUs.adjust(totalEquity, ONE).amount(), C),
                ONE.divide(this.capeEmerging, C).multiply(emerging.adjust(totalEquity, ONE).amount(), C))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
