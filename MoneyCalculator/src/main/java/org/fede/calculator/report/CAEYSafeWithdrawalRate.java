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
import java.util.function.Function;
import java.util.stream.Stream;
import org.fede.calculator.chart.ChartStyle;
import org.fede.calculator.chart.LabeledXYDataItem;
import org.fede.calculator.chart.Scale;
import org.fede.calculator.chart.ScatterXYChart;
import org.fede.calculator.chart.ValueFormat;
import org.fede.calculator.money.Currency;
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

    private static final MoneyAmount ZERO_USD = MoneyAmount.zero(USD);
    private static final BigDecimal CAPE_MIN = BigDecimal.valueOf(15L);
    private static final BigDecimal CAPE_MAX = BigDecimal.valueOf(45L);
    private static final BigDecimal CAPE_STEP = BigDecimal.valueOf(5L);
    private static final BigDecimal MONTHS_IN_A_YEAR = BigDecimal.valueOf(12L);
    private static final NumberFormat PCT_FORMAT2 = NumberFormat.getPercentInstance();
    private static final int RETIREMENT_AGE = 65;
    private static final int LIFE_EXPECTANCY = 90;

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

    public CAEYSafeWithdrawalRate(
            BigDecimal expectedInflation,
            BigDecimal bondsNominalYield,
            BigDecimal cape,
            BigDecimal capeExUs,
            BigDecimal capeEmerging) {
        this(
                expectedInflation,
                bondsNominalYield,
                cape,
                capeExUs,
                capeEmerging,
                SeriesReader.readPercent("cape.b"));
    }

    public CAEYSafeWithdrawalRate(
            BigDecimal expectedInflation,
            BigDecimal bondsNominalYield,
            BigDecimal cape,
            BigDecimal capeExUs,
            BigDecimal capeEmerging,
            BigDecimal capeB) {
        this.cashRealYield = expectedInflation.negate(C);
        this.bondsRealYield = bondsNominalYield.subtract(expectedInflation, C);
        this.cape = cape;
        this.capeExUs = capeExUs;
        this.capeEmerging = capeEmerging;

        final var dob = SeriesReader.readDate("dob");

        final var age = BigDecimal.valueOf(ChronoUnit.MONTHS.between(dob, LocalDate.now())).divide(MONTHS_IN_A_YEAR, C);
        final var yearsLeft = BigDecimal.ONE.movePointRight(2).subtract(age, C);

        final var deltaA = deltaA(age);
        this.capeA = new BigDecimal("0.02")
                .min(ONE
                        .divide(yearsLeft, C).multiply(BigDecimal.valueOf(80).movePointLeft(2), C))
                .add(deltaA, C);

        this.capeB = capeB;

    }

    private BigDecimal deltaA(BigDecimal age) {
        final var now = YearMonth.now();
        final var pensionDiscountRate = SeriesReader.readPercent("pensionDiscountRate");

        final var futureOneYearMinPension = ForeignExchanges.getForeignExchange(Currency.ARS, USD)
                .exchange(
                        new MoneyAmount(
                                SeriesReader.readBigDecimal("min.pension"), Currency.ARS),
                        USD,
                        now).adjust(ONE, MONTHS_IN_A_YEAR.add(ONE, C));

        final var yearsToStartPension = BigDecimal.valueOf(RETIREMENT_AGE)
                .subtract(age, C)
                .max(BigDecimal.ZERO);

        final int yearsReceivingPension = LIFE_EXPECTANCY - RETIREMENT_AGE;

        // 1. Calcular la base de la tasa: (1 + r)
        final var rateBase = ONE.add(pensionDiscountRate, C);

        // 2. Calcular la potencia con exponente positivo: (1 + r)^years
        final var positivePower = rateBase.pow(yearsReceivingPension, C);

        // 3. Traer al presente simulando el exponente negativo: (1 + r)^-years = 1 / (1 + r)^years
        final var presentValueFactorTerm = ONE.divide(positivePower, C);

        // 4. Calcular el numerador de la anualidad: 1 - (1 + r)^-years
        final var numerator = ONE.subtract(presentValueFactorTerm, C);

        // 5. Calcular el factor de anualidad final: [1 - (1 + r)^-years] / r
        final var annuityFactor = numerator.divide(pensionDiscountRate, C);

        final var discountFactor = BigDecimal.valueOf(
                1.0 / Math.pow(
                        rateBase.doubleValue(),
                        yearsToStartPension.doubleValue()));

        // 6. Multiplicar el factor por el monto anual de la pensión
        final var presentPensionValue = annuityFactor
                .multiply(discountFactor, C)
                .multiply(futureOneYearMinPension.amount(), C);

        final var cashAmount = LastAmounts.lastCashUSD(now);
        return presentPensionValue
                .divide(this.last.last().total().add(cashAmount).amount(), C)
                .multiply(pensionDiscountRate, C);

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
                .reduce(ZERO_USD, MoneyAmount::add);

        return this.capeA
                .add(this.capeB.multiply(caey, C))
                .add(this.bondsRealYield.multiply(bonds.adjust(totalPortfolioValue.amount(), ONE).amount(), C))
                .add(this.cashRealYield.multiply(cash.adjust(totalPortfolioValue.amount(), ONE).amount(), C));
    }

    private SafeWithdrawal monthlySafeWithdrawal(
            MoneyAmount usEquity,
            MoneyAmount devExUs,
            MoneyAmount emerging,
            MoneyAmount bonds,
            MoneyAmount cash) {

        final var totalPortfolioValue = Stream.of(usEquity, devExUs, emerging, cash, bonds)
                .reduce(ZERO_USD, MoneyAmount::add);

        final var rate = this.capeWR(usEquity, devExUs, emerging, bonds, cash);
        final var amount = totalPortfolioValue.amount()
                .multiply(rate, C)
                .divide(MONTHS_IN_A_YEAR, C);

        return new SafeWithdrawal(rate, amount);

    }

    private String swrChartLabel(SafeWithdrawal monthly, Function<BigDecimal, String> currencyFormatter) {
        return MessageFormat.format("{0} {1}",
                currencyFormatter.apply(monthly.amount()),
                ValueFormat.PERCENTAGE.format().format(monthly.rate()));
    }

    public void monthlySafeWithdrawalByCapeChart(String filename, BigDecimal capeBValue, Function<BigDecimal, String> currencyFormatter) {
        final var now = YearMonth.now();
        final var series = new Series();

        final var currentlyEstimated = this.currentlyEstimatedSavings();

        final var equity = last.last();
        final var cash = series.realSavings("LIQ").getAmount(now)
                .add(currentlyEstimated);

        final var bonds = series.realSavings("BO").getAmountOrElseZero(now);

        final var expectedInflation = SeriesReader.readPercent("expectedInflation");
        final var bondsNominalYield = SeriesReader.readPercent("bond10");

        final var byCape = new XYSeries("Monthly Safe Withdrawal by CAPE");
        Stream.iterate(CAPE_MIN, capeVal -> capeVal.compareTo(CAPE_MAX) <= 0, capeVal -> capeVal.add(CAPE_STEP))
                .forEach(capeVal -> {
                    var swr = new CAEYSafeWithdrawalRate(
                            expectedInflation,
                            bondsNominalYield,
                            capeVal,
                            capeVal,
                            capeVal,
                            capeBValue
                    );
                    var monthly = swr.monthlySafeWithdrawal(equity.us(), equity.exUs(), equity.em(), bonds, cash);
                    byCape.add(new LabeledXYDataItem(
                            capeVal,
                            monthly.amount(),
                            this.swrChartLabel(monthly, currencyFormatter)));
                });

        // current 
        final var currentSwr = new CAEYSafeWithdrawalRate(
                SeriesReader.readPercent("expectedInflation"),
                SeriesReader.readPercent("bond10"),
                SeriesReader.readBigDecimal("cape"),
                SeriesReader.readBigDecimal("cape.exus"),
                SeriesReader.readBigDecimal("cape.em"),
                capeBValue);

        final var currentCape = ONE.divide(currentSwr.caey(equity.us(), equity.exUs(), equity.em()), C);
        final var currentMonthly = currentSwr.monthlySafeWithdrawal(equity.us(), equity.exUs(), equity.em(), bonds, cash);

        final var current = this.xySeries("Current", currentCape, currentMonthly, currencyFormatter);

        // current + total wealth SW
        final var additionalWealth = Future.expectedWealth();

        final var usTotal = equity.us().add(additionalWealth.adjust(ONE, equity.usWeight()));
        final var exUsTotal = equity.exUs().add(additionalWealth.adjust(ONE, equity.exUsWeight()));
        final var emTotal = equity.em().add(additionalWealth.adjust(ONE, equity.emWeight()));

        final var totalWealthMonthly = currentSwr.monthlySafeWithdrawal(usTotal, exUsTotal, emTotal, bonds, cash);
        final var totalWealth = this.xySeries("Total Wealth", currentCape, totalWealthMonthly, currencyFormatter);

        // total wealth with less cash
        final var minCash = new MoneyAmount(BigDecimal.valueOf(60000L), USD)
                .min(cash.add(bonds));

        final var totalWealthLessCash = additionalWealth.add(cash).add(bonds).subtract(minCash);

        final var totalWealthWithLessCashMonthly = currentSwr.monthlySafeWithdrawal(
                equity.us().add(totalWealthLessCash.adjust(ONE, equity.usWeight())),
                equity.exUs().add(totalWealthLessCash.adjust(ONE, equity.exUsWeight())),
                equity.em().add(totalWealthLessCash.adjust(ONE, equity.emWeight())),
                ZERO_USD,
                minCash);

        final var lessCash = this.xySeries("Total Wealth (less cash)",
                currentCape, totalWealthWithLessCashMonthly, currencyFormatter);

        // all equity
        final var allCash = cash.add(bonds);
        final var usWithCash = equity.us().add(allCash.adjust(ONE, equity.usWeight()));
        final var exUswithCash = equity.exUs().add(allCash.adjust(ONE, equity.exUsWeight()));
        final var emWithCash = equity.em().add(allCash.adjust(ONE, equity.emWeight()));

        final var allEquityMonthly = currentSwr.monthlySafeWithdrawal(
                usWithCash,
                exUswithCash,
                emWithCash,
                ZERO_USD,
                ZERO_USD);

        final var allEquity = this.xySeries("All Equity", currentCape, allEquityMonthly, currencyFormatter);

        new ScatterXYChart(
                new ChartStyle(ValueFormat.NUMBER, Scale.LINEAR),
                new ChartStyle(ValueFormat.CURRENCY, Scale.LINEAR))
                .create(
                        this.reportTitle(capeBValue),
                        List.of(byCape, current, allEquity, totalWealth, lessCash),
                        "CAPE",
                        "Monthly Withdrawal (USD)",
                        filename);
    }

    private XYSeries xySeries(String seriesName, BigDecimal xValue, SafeWithdrawal yValue, Function<BigDecimal, String> currencyFormatter) {
        final var series = new XYSeries(seriesName);
        series.add(new LabeledXYDataItem(
                xValue,
                yValue.amount(),
                this.swrChartLabel(yValue, currencyFormatter)
        ));
        return series;
    }

    private String reportTitle(BigDecimal b) {
        return MessageFormat.format(
                "Safe Withdrawal ({0} + {1} / {2})",
                PCT_FORMAT2.format(this.capeA),
                NumberFormat.getNumberInstance().format(b),
                ONE.divide(this.caey(this.last.last()), C));
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

    private BigDecimal caey(
            MoneyAmount usEquity,
            MoneyAmount devExUs,
            MoneyAmount emerging) {
        return this.caey(new Equity(usEquity, devExUs, emerging));
    }

    private BigDecimal caey(Equity eq) {
        return ONE.divide(this.cape, C).multiply(eq.usWeight(), C)
                .add(ONE.divide(this.capeExUs, C).multiply(eq.exUsWeight(), C))
                .add(ONE.divide(this.capeEmerging, C).multiply(eq.emWeight(), C));
    }

    private record SafeWithdrawal(BigDecimal rate, BigDecimal amount) {

    }

}
