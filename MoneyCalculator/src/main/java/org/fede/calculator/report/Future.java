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
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.fede.calculator.chart.ChartStyle;
import org.fede.calculator.chart.LabeledXYDataItem;
import org.fede.calculator.chart.Scale;
import org.fede.calculator.chart.ScatterXYChart;
import org.fede.calculator.chart.StackedTimeSeriesChart;
import org.fede.calculator.chart.ValueFormat;
import org.fede.calculator.money.Currency;
import static org.fede.calculator.money.Currency.USD;
import org.fede.calculator.money.ForeignExchanges;
import org.fede.calculator.money.Inflation;
import static org.fede.calculator.money.MathConstants.C;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.series.MoneyAmountItem;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.SeriesReader;
import org.fede.calculator.money.series.SortedMapMoneyAmountSeries;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.xy.XYSeries;
import tools.jackson.core.type.TypeReference;

/**
 *
 * @author fede
 */
public class Future {

    private final Series series;

    public Future(Series series) {
        this.series = series;
    }

    private static final MoneyAmount ZERO_USD = MoneyAmount.zero(USD);

    private static final TypeReference<List<CashFlow>> FLOWS_TR = new TypeReference<>() {
    };

    public static MoneyAmount expectedWealth() {
        return SeriesReader.read("flows.json", FLOWS_TR)
                .stream()
                .map(Future::presentValue)
                .reduce(ZERO_USD, MoneyAmount::add);
    }

    private static MoneyAmount presentValue(CashFlow flow) {
        final var now = YearMonth.now();

        if (flow.end().isBefore(now)) {
            return ZERO_USD;
        }

        final var start = flow.start().isBefore(now) ? now : flow.start();
        final long monthsToFirst = ChronoUnit.MONTHS.between(now, start);
        final long payments = ChronoUnit.MONTHS.between(start, flow.end()) + 1;

        final var monthlyUsd = asUSD(new MoneyAmount(flow.amount(), flow.currency()), now);
        final double rate = flow.rate().doubleValue();

        if (rate == 0.0d) {
            return monthlyUsd.adjust(ONE, BigDecimal.valueOf(payments));
        }

        // PV = A * (1+r)^(-T/12) * (1 - d^N) / (1 - d),  d = (1+r)^(-1/12)
        final double firstDiscount = Math.pow(1.0d + rate, -monthsToFirst / 12.0d);
        final double monthlyDiscount = Math.pow(1.0d + rate, -1.0d / 12.0d);
        final double annuityFactor = (1.0d - Math.pow(monthlyDiscount, payments)) / (1.0d - monthlyDiscount);

        return new MoneyAmount(
                BigDecimal.valueOf(monthlyUsd.amount().doubleValue() * firstDiscount * annuityFactor),
                USD);
    }

    private static MoneyAmount asUSD(MoneyAmount amount, YearMonth ym) {
        if (amount.currency() == USD) {
            return amount;
        }
        return ForeignExchanges.getForeignExchange(amount.currency(), USD)
                .exchange(amount, USD, ym);
    }

    private record CashFlow(
            String name,
            BigDecimal amount,
            Currency currency,
            BigDecimal rate,
            YearMonth start,
            YearMonth end) {

    }

    public static Severance severance() {

        if (SeriesReader.readBoolean("useFullSalaryForSeverance")) {
            return severance(BigDecimal.ONE);

        }
        return severance(BigDecimal.valueOf(67).movePointLeft(2));
    }

    public static Severance severance(BigDecimal maxSalaryFactor) {
        final long totalMonths = YearMonth.of(2015, Month.DECEMBER)
                .until(YearMonth.now(), ChronoUnit.MONTHS);

        final long years = Math.floorDiv(totalMonths, 12);

        final long months = Math.floorMod(totalMonths, 12);

        final var toppedSalaries = BigDecimal.valueOf(years)
                .add((months > 3
                        ? BigDecimal.ONE
                        : BigDecimal.ZERO));

        final var currentMonth = LocalDate.now().getMonthValue();

        final var notice = years < 5 ? ONE : BigDecimal.TWO;
        final var severanceMonth = ONE;

        final var vacationsFactor = BigDecimal.valueOf(28)
                .divide(BigDecimal.valueOf(25), C);

        final var vacationsPart = BigDecimal.valueOf(
                ChronoUnit.DAYS.between(LocalDate.now().withMonth(1).withDayOfMonth(1), LocalDate.now()))
                .divide(BigDecimal.valueOf(365L), C)
                .multiply(vacationsFactor, C);
        final var sacPart = BigDecimal.valueOf(
                ChronoUnit.DAYS.between(LocalDate.now().withMonth(currentMonth > 6 ? 7 : 1).withDayOfMonth(1), LocalDate.now()))
                .divide(BigDecimal.valueOf(180L), C)
                .divide(BigDecimal.TWO, C);
        final var sacFactor = ONE
                .add(ONE
                        .divide(BigDecimal.valueOf(12), C), C);

        final var taxedSalaries = notice
                .add(severanceMonth, C)
                .add(vacationsPart, C)
                .multiply(sacFactor, C)
                .add(sacPart, C);

        // 65% - 7% => 35% ganacias + 7% payroll taxes
        final var taxSalaryFactor = BigDecimal.valueOf(65 - 7).movePointLeft(2);
        final var salary = SeriesReader.readBigDecimal("salary");

        return new Severance(
                salary,
                salary
                        .multiply(taxedSalaries, C)
                        .multiply(taxSalaryFactor, C),
                salary
                        .multiply(toppedSalaries, C)
                        .multiply(maxSalaryFactor, C));
    }

    private static MoneyAmount health(String key) {
        var ars = new MoneyAmount(
                SeriesReader.readBigDecimal(key)
                        .multiply(BigDecimal.TWO, C)
                        .multiply(ONE.add(SeriesReader.readPercent("iva"), C)),
                Currency.ARS);

        return ForeignExchanges.getForeignExchange(Currency.ARS, USD)
                .exchange(ars, USD, YearMonth.now());
    }

    public static MoneyAmount futureHealth() {
        return health("futureHealth");
    }

    public static MoneyAmount contingencyHealth() {
        return health("currentHealth");
    }

    private MoneyAmountSeries ema(MoneyAmountSeries series, int n) {

        MoneyAmountSeries result = new SortedMapMoneyAmountSeries(series.getCurrency(),
                series.getName() + " EMA(" + n + ")");

        final BigDecimal alpha = BigDecimal.TWO
                .divide(BigDecimal.valueOf(n).add(BigDecimal.ONE, C), C);

        final BigDecimal oneMinusAlpha = BigDecimal.ONE
                .subtract(alpha, C);

        final MoneyAmountItem[] items = series.items().toArray(MoneyAmountItem[]::new);

        MoneyAmount ema = items[0].amount();

        for (var i = 1; i < items.length; i++) {
            ema = items[i].amount().adjust(ONE, alpha)
                    .add(ema.adjust(ONE, oneMinusAlpha));

            result.putAmount(items[i].ym(), ema);

        }
        return result;
    }

    public Map<ExpenseTier, MoneyAmountSeries> retirementSpending() {
        final var allSpeding = this.series.allSpending();
        allSpeding.remove(SpendingSeries.EXTRAORDINARY);
        allSpeding.remove(SpendingSeries.INVESTMENTS);

        final var healthCostSeries = Inflation.usdInflation().adjust(futureHealth(), YearMonth.now());

        // add future health cost
        allSpeding.put(SpendingSeries.SALUD,
                allSpeding.get(SpendingSeries.SALUD).add(healthCostSeries));

        for (var s : allSpeding.keySet()) {
            allSpeding.put(s, this.ema(allSpeding.get(s), s.getAverageMonths()));
        }

        final Map<ExpenseTier, Optional<MoneyAmountSeries>> grouped = allSpeding.keySet()
                .stream()
                .collect(
                        Collectors.groupingBy(ExpenseTier::of,
                                Collectors.mapping(allSpeding::get,
                                        Collectors.reducing(MoneyAmountSeries::add))));

        final var essential = grouped.get(ExpenseTier.ESSENTIAL).get();
        essential.setName("Essential");
        final var discretionary = grouped.get(ExpenseTier.DISCRETIONARY).get();
        discretionary.setName("Discretionary");
        final var luxury = grouped.get(ExpenseTier.LUXURY).get();
        luxury.setName("Luxury");
        return Map.of(
                ExpenseTier.ESSENTIAL, essential,
                ExpenseTier.DISCRETIONARY, discretionary,
                ExpenseTier.LUXURY, luxury
        );
    }

    public void retirementSpendingEvolution() {

        var map = this.retirementSpending();
        new StackedTimeSeriesChart(
                new ChartStyle(ValueFormat.CURRENCY, Scale.LINEAR))
                .create("Retirement Spending",
                        List.of(
                                map.get(ExpenseTier.ESSENTIAL),
                                map.get(ExpenseTier.DISCRETIONARY),
                                map.get(ExpenseTier.LUXURY)),
                        "spending-retirement");

    }

    public void retirementWithdrawalByCapitalGains() {

        final var map = this.retirementSpending();
        final var now = YearMonth.now();

        final var essential = map.get(ExpenseTier.ESSENTIAL).getAmount(now);
        final var essentialPlusDiscretionary = essential
                .add(map.get(ExpenseTier.DISCRETIONARY).getAmount(now));
        final var all = essentialPlusDiscretionary
                .add(map.get(ExpenseTier.LUXURY).getAmount(now));

        final var spendingLevels = List.of(
                Map.entry(ExpenseTier.ESSENTIAL, essential),
                Map.entry(ExpenseTier.DISCRETIONARY, essentialPlusDiscretionary),
                Map.entry(ExpenseTier.LUXURY, all));
        final var taxRates = List.of(
                BigDecimal.ZERO,
                BigDecimal.valueOf(15).movePointLeft(2),
                BigDecimal.valueOf(35).movePointLeft(2));

        final var costBasisPct = SeriesReader.readPercent("costBasisPct");
        final var format = new Format();
        final var percentFormat = ValueFormat.PERCENTAGE.format();

        final var ss = spendingLevels.stream()
                .map(level -> {
                    final var xySeries = new XYSeries(level.getKey().name());
                    for (var taxRate : taxRates) {
                        final var sell = adjustForCapitalGains(level.getValue(), taxRate, costBasisPct);
                        xySeries.add(new LabeledXYDataItem(
                                level.getValue().amount(),
                                taxRate,
                                format.currencyShort(sell.amount())));
                    }
                    return xySeries;
                })
                .toList();

        new ScatterXYChart(
                new ChartStyle(ValueFormat.CURRENCY, Scale.LINEAR),
                new ChartStyle(ValueFormat.PERCENTAGE, Scale.LINEAR))
                .create(
                        "Retirement Withdrawal by Capital Gains",
                        "Cost basis " + percentFormat.format(costBasisPct),
                        ss,
                        "Spending",
                        "Capital Gains Tax",
                        RectangleEdge.TOP,
                        "retirement-withdrawal-capital-gains");
    }

    public static MoneyAmount adjustForCapitalGains(
            MoneyAmount netAmount,
            BigDecimal capitalGainsTaxRate,
            BigDecimal costBasisPct) {

        final var divisor = ONE.subtract(capitalGainsTaxRate
                .multiply(ONE.subtract(costBasisPct, C), C), C);

        return netAmount.adjust(divisor, ONE);
    }
}
