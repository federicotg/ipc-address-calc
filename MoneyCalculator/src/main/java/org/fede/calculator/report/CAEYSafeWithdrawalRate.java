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
import java.util.List;
import java.util.stream.Stream;
import org.fede.calculator.chart.ChartStyle;
import org.fede.calculator.chart.LabeledXYDataItem;
import org.fede.calculator.chart.Scale;
import org.fede.calculator.chart.ScatterXYChart;
import org.fede.calculator.chart.ValueFormat;
import org.fede.calculator.money.Currency;
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
    private static final BigDecimal MONTHS_IN_A_YEAR = new BigDecimal("12");

    private static final MoneyAmount IMMEDIATE_CASH = MoneyAmount.zero(Currency.USD);

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

        var totalEquity = usEquity.add(devExUs).add(emerging).amount();

        var totalPortfolioValue = Stream.of(usEquity, devExUs, emerging, cash, bonds)
                .reduce(MoneyAmount.zero(USD), MoneyAmount::add);

        final var caey = Stream.of(
                ONE.divide(this.cape, C).multiply(usEquity.adjust(totalEquity, ONE).amount(), C),
                ONE.divide(this.capeExUs, C).multiply(devExUs.adjust(totalEquity, ONE).amount(), C),
                ONE.divide(this.capeEmerging, C).multiply(emerging.adjust(totalEquity, ONE).amount(), C))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return SeriesReader.readPercent("cape.a")
                .add(SeriesReader.readPercent("cape.b").multiply(caey, C))
                .add(this.bondsRealYield.multiply(bonds.adjust(totalPortfolioValue.amount(), ONE).amount(), C))
                .add(this.cashRealYield.multiply(cash.adjust(totalPortfolioValue.amount(), ONE).amount(), C));
    }

    public BigDecimal monthlySafeWithdrawal(MoneyAmount portfolio, BigDecimal equityPct) {
        var allocation = this.portfolioFor(portfolio, equityPct);
        return portfolio.amount()
                .multiply(this.capeWR(
                        allocation.usEquity(),
                        allocation.devExUs(),
                        allocation.emerging(),
                        allocation.bonds(),
                        allocation.cash()), C)
                .divide(MONTHS_IN_A_YEAR, C);
    }

    private PortfolioAllocation portfolioFor(MoneyAmount portfolio, BigDecimal equityPct) {
        var equity = portfolio.adjust(ONE, equityPct);
        var cash = portfolio.subtract(equity);

        var bonds = cash.subtract(IMMEDIATE_CASH).max(MoneyAmount.zero(USD));

        return new PortfolioAllocation(
                equity.adjust(ONE, US_EQUITY_SHARE),
                equity.adjust(ONE, DEV_EX_US_SHARE),
                equity.adjust(ONE, EMERGING_SHARE),
                bonds,
                cash.subtract(bonds));
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

    private record PortfolioAllocation(
            MoneyAmount usEquity,
            MoneyAmount devExUs,
            MoneyAmount emerging,
            MoneyAmount bonds,
            MoneyAmount cash) {

    }

}
