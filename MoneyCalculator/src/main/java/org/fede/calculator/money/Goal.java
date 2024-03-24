/*
 * Copyright (C) 2021 federicogentile
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
package org.fede.calculator.money;

import com.diogonunes.jcolor.AnsiFormat;
import com.diogonunes.jcolor.Attribute;
import com.fasterxml.jackson.core.type.TypeReference;
import java.math.BigDecimal;
import static java.math.BigDecimal.ONE;
import static java.text.MessageFormat.format;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import static org.fede.calculator.money.Inflation.USD_INFLATION;
import static org.fede.calculator.money.MathConstants.C;
import static org.fede.calculator.money.MathConstants.RM;
import static org.fede.calculator.money.MathConstants.SCALE;
import org.fede.calculator.money.series.SeriesReader;

/**
 *
 * @author fede
 */
public class Goal {

    private static final TypeReference<Map<String, ExpectedReturnGroup>> TR = new TypeReference<Map<String, ExpectedReturnGroup>>() {
    };

    private static final BigDecimal BUY_FEE = new BigDecimal("200");

    private static final BigDecimal MONTHS_IN_A_YEAR = new BigDecimal("12");

    private static final BigDecimal SELL_FEE = new BigDecimal("0.00726").multiply(new BigDecimal("0.5"), C)
            .add(new BigDecimal("0.00056").multiply(new BigDecimal("0.5"), C));

    private static final BigDecimal CAPITAL_GAINS_TAX_EXTRA_WITHDRAWAL_PCT = ONE.divide(ONE.subtract(new BigDecimal("0.1238"), C), C);

    private static final BigDecimal HEALTH_MONTHLY_COST = new BigDecimal("400");

    private static final Function<BigDecimal, BigDecimal> IBKR_FEE_STRATEGY = new InteractiveBrokersTieredLondonUSDFeeStrategy();

    private final double bbppTaxRate;
    private final Console console;
    private final Format format;
    private final Series series;
    private final Bar bar;

    public Goal(Console console, Format format, Series series, Bar bar, double bbppTaxRate) {
        this.bbppTaxRate = bbppTaxRate;
        this.console = console;
        this.format = format;
        this.series = series;
        this.bar = bar;

    }

//    private void nHighestAtRetirement(double[] values, int n, int retirementYear, int startingYear) {
//        this.nAtRetirement(values, n, retirementYear, startingYear, Comparator.comparing(ReturnPosition::value).reversed());
//    }
    private void nLowestAtRetirement(double[] values, int n, int retirementYear, int startingYear) {
        this.nAtRetirement(values, n, retirementYear, startingYear, Comparator.comparing(ReturnPosition::value));
    }

    private void nAtRetirement(double[] values, int n, int retirementYear, int startingYear, Comparator<ReturnPosition> cmp) {
        var swaps = IntStream.range(0, values.length)
                .mapToObj(i -> new ReturnPosition(values[i], i))
                .sorted(cmp)
                .mapToInt(ReturnPosition::index)
                .toArray();

        for (var i = 0; i < n; i++) {
            double aux = values[retirementYear - startingYear + i];
            values[retirementYear - startingYear + i] = values[swaps[i]];
            values[swaps[i]] = aux;
        }
    }

    private double goals(
            final int startingYear,
            final int retirement,
            final int end,
            final double cash,
            final double investedAmount,
            final double[] returns,
            final double[] deposit,
            final double[] withdrawal,
            final double bbppMin) {

        this.nLowestAtRetirement(returns, 3, retirement, startingYear);

        double cashAmount = cash;
        double amount = investedAmount;
        double bbpp;
        // depositing
        for (var i = startingYear; i < retirement; i++) {

            // BB.PP.
            bbpp = Math.max(amount - bbppMin, 0.0d) * this.bbppTaxRate;
            final var d = deposit[i - startingYear];

            amount -= bbpp;

            amount = amount * returns[i - startingYear] + d;
        }

        final var cgt = CAPITAL_GAINS_TAX_EXTRA_WITHDRAWAL_PCT.doubleValue();

        // withdrawing
        for (var i = retirement; i <= end; i++) {

            // withdrawal strategy: bad year => withdraw 20% less.
            final var lastYearReturn = returns[i - startingYear - 1];

            var thisYearWithdrawal = withdrawal[i - startingYear] * (lastYearReturn <= 0.8d ? 0.8d : 1.0d);

            if (cashAmount > thisYearWithdrawal) {
                // sobra cash
                cashAmount -= thisYearWithdrawal;
            } else {
                // (cashAmount <= thisYearWithdrawal) => sell investments

                //thisYearWithdrawal = thisYearWithdrawal - cashAmount;
                //cashAmount = 0.0d;
                amount -= thisYearWithdrawal;

                // BB.PP.
                bbpp = Math.max(amount - bbppMin, 0.0d) * this.bbppTaxRate;
                amount -= bbpp * cgt;
            }
            // yearly returns
            amount *= returns[i - startingYear];
        }
        return amount + cashAmount;
    }

    public void goal(
            final int trials,
            final int averageIncomeSpendingMonths,
            final BigDecimal inflation,
            final int retirementAge,
            final BigDecimal extraCash,
            final boolean afterTax,
            final int age,
            final int pension,
            MoneyAmount todaySavings,
            MoneyAmount invested,
            String expected) {

        final var spendingandSaving = new Savings(this.format, this.series, this.bar, this.console)
                .averageSpendingAndSaving(averageIncomeSpendingMonths);

        final var monthlyDeposit = spendingandSaving.saving().getAmount();
        final var monthlyWithdraw = spendingandSaving.spending().getAmount()
                .subtract(new BigDecimal(pension), C);

        this.goal(trials, monthlyDeposit, monthlyWithdraw, inflation, retirementAge, extraCash, afterTax, age, pension, todaySavings, invested, expected);
    }

    private void goal(
            final int trials,
            final BigDecimal monthlyDeposit,
            final BigDecimal monthlyWithdraw,
            final BigDecimal inflation,
            final int retirementAge,
            final BigDecimal extraCash,
            final boolean afterTax,
            final int age,
            final int pension,
            MoneyAmount todaySavings,
            MoneyAmount invested,
            String expected) {

        final var retirementYear = 1978 + retirementAge;
        final int startingYear = USD_INFLATION.getTo().getYear();
        final var end = 1978 + age;
        final var yearsLeft = 2088 - startingYear;

        final var cash = todaySavings.getAmount()
                .subtract(invested.getAmount(), C)
                .add(extraCash, C).doubleValue();

        final var inflationRate = ONE.setScale(SCALE, RM)
                .add(inflation.setScale(SCALE, RM).movePointLeft(2), C).doubleValue();

        final var inflationFactors = IntStream.range(0, yearsLeft)
                .mapToDouble(year -> Math.pow(inflationRate, year))
                .toArray();

        //this.nHighestAtRetirement(inflationFactors, 2, retirementYear, startingYear);
        final var yearBuyTransactions = BigDecimal.TEN;

        final var yearDeposit = monthlyDeposit
                .multiply(MONTHS_IN_A_YEAR, C)
                .subtract(BUY_FEE, C);

        final var yearIBKRFee = IBKR_FEE_STRATEGY
                .apply(yearDeposit.divide(yearBuyTransactions, C))
                .multiply(yearBuyTransactions, C);

        final var deposit = yearDeposit.subtract(yearIBKRFee, C).doubleValue();

        final var withdraw = (monthlyWithdraw
                .add(HEALTH_MONTHLY_COST, C)
                .multiply(MONTHS_IN_A_YEAR, C)
                .subtract(new BigDecimal(pension * 13), C))
                .multiply(ONE.divide(ONE.subtract(SELL_FEE, C), C), C)
                .multiply(afterTax ? CAPITAL_GAINS_TAX_EXTRA_WITHDRAWAL_PCT : ONE, C).doubleValue();

        final var investedAmount = invested.getAmount().doubleValue();

        this.console.appendLine(format("Cash: {0,number,currency}, invested: {1,number,currency}", cash, investedAmount));

        final var formattedDeposit = this.format.text(format("{0,number,currency}", monthlyDeposit), 6, new AnsiFormat(Attribute.BRIGHT_GREEN_TEXT()));
        final var formattedWithdrawal = this.format.text(format("{0,number,currency}", monthlyWithdraw), 6, new AnsiFormat(Attribute.BRIGHT_RED_TEXT()));

        this.console.appendLine(format(
                "Saving {0}, spending {1}{2}",
                formattedDeposit,
                formattedWithdrawal,
                afterTax ? " after tax." : "."));
        if (pension > 0) {
            this.console.appendLine(format("Considering {0,number,currency} pension.", pension));
        }

        this.console.appendLine(format("Considering {0,number,currency} health insurance.", HEALTH_MONTHLY_COST));

        this.console.appendLine(format("Expected {0}% inflation, retiring at {1}, until age {2}.", inflation, retirementAge, age));

        final var bbppMin = this.series.bbppSeries()
                .stream()
                .map(bbpp -> bbpp.getMinimum().divide(bbpp.getUsd(), MathConstants.C))
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(30000d);

        this.console.appendLine(format("BB.PP min. {0,number,currency}.", bbppMin));

        final var realDeposits = Arrays.stream(inflationFactors)
                .map(f -> f * deposit)
                .toArray();

        final var realWithdrawals = Arrays.stream(inflationFactors)
                .map(f -> f * withdraw)
                .toArray();
        final Map<String, ExpectedReturnGroup> expectedReturns = SeriesReader.read("/index/expected-returns.json", TR);

        List<SuccessCount> results = expectedReturns.entrySet()
                .parallelStream()
                .filter(entry -> "all".equals(expected) || entry.getKey().equals(expected))
                .map(entry
                        -> new SuccessCount(
                        entry.getKey(),
                        this.expectedReturnSuccesses(new GaussReturnSupplier(entry.getValue().mu(), entry.getValue().sigma(), yearsLeft),
                                trials,
                                startingYear,
                                retirementYear,
                                end,
                                cash,
                                investedAmount,
                                realDeposits,
                                realWithdrawals,
                                bbppMin)))
                .toList();

        results.stream()
                .sorted(Comparator.comparing(SuccessCount::success))
                .map(pair
                        -> this.report(pair.name(), pair.success(), trials))
                .forEach(this.console::appendLine);

        final long averageSuccesses = (long) results.stream()
                .mapToLong(SuccessCount::success)
                .average()
                .getAsDouble();
        this.console.appendLine(this.format.subtitle("Average"));
        this.console.appendLine(this.report("Average", averageSuccesses, trials));

    }

    private String report(String label, long successes, int trials) {
        return format(
                "{0} {1}/{2} {3}",
                label, successes,
                trials,
                this.format.text(
                        this.format.percent(BigDecimal.valueOf((double) successes / (double) trials)),
                        6,
                        new AnsiFormat(Attribute.BOLD())));
    }

    private long expectedReturnSuccesses(
            Supplier<double[]> returnsSuplier,
            int trials,
            int startingYear,
            int retirementYear,
            int end,
            double cash,
            double investedAmount,
            double[] realDeposits,
            double[] realWithdrawals,
            double bbppMin) {

        return IntStream.range(0, trials)
                .mapToDouble(p
                        -> this.goals(
                        startingYear,
                        retirementYear,
                        end,
                        cash,
                        investedAmount,
                        returnsSuplier.get(),
                        realDeposits,
                        realWithdrawals,
                        bbppMin))
                .filter(d -> d > 0.0d)
                .count();
    }

    private record SuccessCount(String name, long success) {

    }

    private record ReturnPosition(Double value, int index) {

    }
;

}
