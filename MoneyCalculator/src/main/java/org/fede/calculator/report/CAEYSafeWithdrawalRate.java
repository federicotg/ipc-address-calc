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
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Stream;
import org.fede.calculator.chart.ChartStyle;
import org.fede.calculator.chart.LabeledXYDataItem;
import org.fede.calculator.chart.Scale;
import org.fede.calculator.chart.ScatterXYChart;
import org.fede.calculator.chart.ValueFormat;
import static org.fede.calculator.money.Currency.USD;
import org.fede.calculator.money.ForeignExchanges;
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

    private final BigDecimal cashRealYield;
    private final BigDecimal bondsRealYield;
    private final BigDecimal cape;
    private final BigDecimal capeExUs;
    private final BigDecimal capeEmerging;

    public CAEYSafeWithdrawalRate(BigDecimal expectedInflation, BigDecimal bondsNominalYield, BigDecimal cape, BigDecimal capeExUs, BigDecimal capeEmerging) {
        this.cashRealYield = expectedInflation.negate(C);
        this.bondsRealYield = bondsNominalYield.subtract(expectedInflation, C);
        this.cape = cape;
        this.capeExUs = capeExUs;
        this.capeEmerging = capeEmerging;
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

        final var caey = this.caey(usEquity, devExUs, emerging, bonds, cash);

        final var totalPortfolioValue = Stream.of(usEquity, devExUs, emerging, cash, bonds)
                .reduce(MoneyAmount.zero(USD), MoneyAmount::add);

        return SeriesReader.readPercent("cape.a")
                .add(SeriesReader.readPercent("cape.b").multiply(caey, C))
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
                        "CAEY Safe Monthly Withdrawal",
                        List.of(series),
                        "Portfolio (USD)",
                        "Equity %",
                        filename);
    }

    public void monthlySafeWithdrawalByCapeChart(String filename) {
        var now = YearMonth.now();
        var series = new Series();

        var currentlyEstimated = this.currentlyEstimatedSavings();

        var usEquity = this.lastUSDAmount("ahorros-cspx", now)
                .add(this.lastUSDAmount("ahorros-rtwo", now))
                .add(this.lastUSDAmount("ahorros-xrsu", now));

        var devExUs = this.lastUSDAmount("ahorros-meud", now)
                .add(this.lastUSDAmount("ahorros-xuse", now));

        var emerging = this.lastUSDAmount("ahorros-eimi", now);

        var cash = series.realSavings("LIQ").getAmount(now)
                .add(currentlyEstimated);

        var bonds = series.realSavings("BO").getAmountOrElseZero(now);

        final var expectedInflation = SeriesReader.readPercent("expectedInflation");
        final var bondsNominalYield = SeriesReader.readPercent("bond10");
        final var currencyFormat = ValueFormat.CURRENCY.format();

        final var byCape = new XYSeries("Monthly Safe Withdrawal by CAPE");
        Stream.iterate(CAPE_MIN, cape -> cape.compareTo(CAPE_MAX) <= 0, cape -> cape.add(CAPE_STEP))
                .forEach(cape -> {
                    var swr = new CAEYSafeWithdrawalRate(expectedInflation, bondsNominalYield, cape, cape, cape);
                    var monthly = swr.monthlySafeWithdrawal(usEquity, devExUs, emerging, bonds, cash);
                    byCape.add(new LabeledXYDataItem(
                            cape,
                            monthly,
                            currencyFormat.format(monthly)));
                });

        final var currentSwr = new CAEYSafeWithdrawalRate();
        final var currentCape = SeriesReader.readBigDecimal("cape");
        final var currentMonthly = currentSwr.monthlySafeWithdrawal(usEquity, devExUs, emerging, bonds, cash);
        final var current = new XYSeries("Current CAPE");
        current.add(new LabeledXYDataItem(
                currentCape,
                currentMonthly,
                currencyFormat.format(currentMonthly)));

        new ScatterXYChart(
                new ChartStyle(ValueFormat.NUMBER, Scale.LINEAR),
                new ChartStyle(ValueFormat.CURRENCY, Scale.LINEAR))
                .create(
                        "CAEY Safe Monthly Withdrawal by CAPE",
                        List.of(byCape, current),
                        "CAPE",
                        "Monthly Withdrawal (USD)",
                        filename);
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

    private MoneyAmount lastUSDAmount(String seriesName, YearMonth ym) {
        var amount = SeriesReader.readSeries("saving/".concat(seriesName).concat(".json")).getAmountOrElseZero(ym);
        return ForeignExchanges.getMoneyAmountForeignExchange(amount.currency(), USD)
                .apply(amount, ym);
    }

    private record PortfolioAllocation(
            MoneyAmount usEquity,
            MoneyAmount devExUs,
            MoneyAmount emerging,
            MoneyAmount bonds,
            MoneyAmount cash) {

    }

    public BigDecimal caey(MoneyAmount usEquity,
            MoneyAmount devExUs,
            MoneyAmount emerging,
            MoneyAmount bonds,
            MoneyAmount cash) {

        final var totalEquity = usEquity.add(devExUs).add(emerging).amount();

        return Stream.of(
                ONE.divide(this.cape, C).multiply(usEquity.adjust(totalEquity, ONE).amount(), C),
                ONE.divide(this.capeExUs, C).multiply(devExUs.adjust(totalEquity, ONE).amount(), C),
                ONE.divide(this.capeEmerging, C).multiply(emerging.adjust(totalEquity, ONE).amount(), C))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
