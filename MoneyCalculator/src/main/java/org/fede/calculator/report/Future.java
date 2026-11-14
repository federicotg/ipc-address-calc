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

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import static java.math.BigDecimal.ONE;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.fede.calculator.chart.ChartStyle;
import org.fede.calculator.chart.Scale;
import org.fede.calculator.chart.StackedTimeSeriesChart;
import org.fede.calculator.chart.ValueFormat;
import org.fede.calculator.money.Currency;
import static org.fede.calculator.money.Currency.USD;
import org.fede.calculator.money.ForeignExchanges;
import static org.fede.calculator.money.MathConstants.C;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.series.MoneyAmountItem;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.SeriesReader;
import org.fede.calculator.money.series.SortedMapMoneyAmountSeries;
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

    public static MoneyAmount expectedWealth(BigDecimal discountRate) {
        return expectedWealthDetails(discountRate)
                .stream()
                .map(CashFlowPresentValue::presentValue)
                .reduce(ZERO_USD, MoneyAmount::add);
    }

    public static List<CashFlow> usdFlows() {
        final var now = YearMonth.now();
        return SeriesReader.read(SeriesReader.readEnvironment().getProperty("flows"), FLOWS_TR)
                .stream()
                .sorted(Comparator.comparing(CashFlow::start))
                .map(cf
                        -> new CashFlow(
                                cf.name,
                                asUSD(cf.adjustmentStrategy == null
                                        ? new MoneyAmount(cf.amount, cf.currency)
                                        : cf.adjustmentStrategy
                                                .adjust(new MoneyAmount(cf.amount, cf.currency)), now)
                                        .amount(),
                                USD,
                                cf.start,
                                cf.end,
                                null, 
                                cf.certain))
                .toList();

    }

    public static List<CashFlowPresentValue> expectedWealthDetails(BigDecimal discountRate) {
        return expectedWealthDetails(discountRate, false);
    }

    
    public static List<CashFlowPresentValue> expectedWealthDetails(BigDecimal discountRate, boolean certain) {
        return SeriesReader.read(SeriesReader.readEnvironment().getProperty("flows"), FLOWS_TR)
                .stream()
                .filter(cf -> certain ? cf.certain() : true)
                .sorted(Comparator.comparing(CashFlow::start))
                .map(flow -> new CashFlowPresentValue(flow.name(), presentValue(flow, discountRate)))
                .toList();
    }

    public static MoneyAmount presentValueOfAnnuity(
            MoneyAmount monthlyAmount,
            YearMonth start,
            YearMonth end,
            BigDecimal discountRate) {
        return presentValue(
                new CashFlow("", monthlyAmount.amount(), monthlyAmount.currency(), start, end, null, false),
                discountRate);
    }

    private static MoneyAmount presentValue(CashFlow flow, BigDecimal discountRate) {
        final var now = YearMonth.now();

        if (flow.end().isBefore(now)) {
            return ZERO_USD;
        }

        final var start = flow.start().isBefore(now) ? now : flow.start();
        final long monthsToFirst = ChronoUnit.MONTHS.between(now, start);
        final long payments = ChronoUnit.MONTHS.between(start, flow.end()) + 1;

        final var monthlyUsd = asUSD(flow.adjustedAmount(), now);

        if (discountRate.signum() == 0) {
            return monthlyUsd.adjust(ONE, BigDecimal.valueOf(payments));
        }

        final double rate = discountRate.doubleValue();

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

    public record CashFlow(
            String name,
            BigDecimal amount,
            Currency currency,
            YearMonth start,
            YearMonth end,
            @JsonProperty("adjustment_strategy") AdjustmentStrategy adjustmentStrategy,
            boolean certain) {

        private MoneyAmount adjustedAmount() {
            final var nominal = new MoneyAmount(this.amount, this.currency);
            if (this.adjustmentStrategy == null) {
                return nominal;
            }
            return this.adjustmentStrategy.adjust(nominal);
        }
    }

    public record CashFlowPresentValue(String name, MoneyAmount presentValue) {

    }

    public static Severance severance() {
        return severance(salary());
    }

    public static Severance severance(MoneyAmount salary) {
        if (SeriesReader.readBoolean("useFullSalaryForSeverance")) {
            return severance(salary, BigDecimal.ONE);
        }
        return severance(salary, BigDecimal.valueOf(67).movePointLeft(2));
    }

    private static MoneyAmount salary() {
        var salaryCashFlow = SeriesReader.read("flows.json", FLOWS_TR)
                .stream().filter(cf -> cf.name.equals("Severance")).findFirst().get();
        return ForeignExchanges.getForeignExchange(salaryCashFlow.currency, USD)
                .exchange(
                        new MoneyAmount(salaryCashFlow.amount(), salaryCashFlow.currency), USD, YearMonth.now());

    }

    public static Severance severance(BigDecimal maxSalaryFactor) {

        return severance(salary(), maxSalaryFactor);
    }

    public static Severance severance(MoneyAmount salary, BigDecimal maxSalaryFactor) {
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
        final var salaryUsd = asUSD(salary, YearMonth.now()).amount();

        return new Severance(
                salaryUsd,
                salaryUsd
                        .multiply(taxedSalaries, C)
                        .multiply(taxSalaryFactor, C),
                salaryUsd
                        .multiply(toppedSalaries, C)
                        .multiply(maxSalaryFactor, C));
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
        allSpeding.remove(SpendingSeries.DEPARTAMENTOS);

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

    
}
