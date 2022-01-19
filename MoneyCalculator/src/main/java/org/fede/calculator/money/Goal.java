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

import java.math.BigDecimal;
import static java.math.BigDecimal.ONE;
import java.math.RoundingMode;
import static java.text.MessageFormat.format;
import java.util.ArrayList;
import java.util.Arrays;
import static java.util.Comparator.comparing;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Supplier;
import static java.util.stream.Collectors.toList;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import static org.fede.calculator.money.Inflation.USD_INFLATION;
import org.fede.calculator.money.series.YearMonth;
import static org.fede.calculator.money.MathConstants.C;
import static org.fede.calculator.money.MathConstants.RM;
import static org.fede.calculator.money.MathConstants.SCALE;

/**
 *
 * @author fede
 */
public class Goal {

    private static final double US_NOMINAL_EXPECTED_RETURN = 6.25d;
    private static final double US_EXPECTED_RETURN_STDDEV = 14.21d;

    private static final double EX_US_NOMINAL_EXPECTED_RETURN = 6.92d;
    private static final double EX_US_EXPECTED_RETURN_STDDEV = 13.12d;

    private static final double CSPX_FEE = 0.0007d;

    private static final int END_AGE_STD = 5;

    private static final BigDecimal BUY_FEE = new BigDecimal("200");

    private static final BigDecimal SELL_FEE = new BigDecimal("0.00726").multiply(new BigDecimal("0.5"), C)
            .add(new BigDecimal("0.00056").multiply(new BigDecimal("0.5"), C));

    private static final BigDecimal CAPITAL_GAINS_TAX_EXTRA_WITHDRAWAL_PCT = ONE.divide(ONE.subtract(new BigDecimal("0.135"), C), C);

    private static final Function<BigDecimal, BigDecimal> IBKR_FEE_STRATEGY = new InteractiveBrokersTieredLondonUSDFeeStrategy();

    private final double bbppTaxRate;
    private final double bbppMin;

    private final Console console;
    private final Format format;

    public Goal(Console console, Format format, double bbppTaxRate, double bbppMin) {
        this.bbppTaxRate = bbppTaxRate;
        this.bbppMin = bbppMin;
        this.console = console;
        this.format = format;

    }

    private boolean goals(
            final int startingYear,
            final int retirement,
            final int end,
            final double cash,
            final double investedAmount,
            final double[] returns,
            final double[] deposit,
            final double[] withdraw) {

        double cashAmount = cash;
        double amount = investedAmount;
        // depositing
        for (var i = startingYear; i < retirement; i++) {

            // BB.PP.
            amount -= Math.max(amount - this.bbppMin, 0.0d) * this.bbppTaxRate;

            amount = amount * returns[i - startingYear] + deposit[i - startingYear];
        }
        // withdrawing
        for (var i = retirement; i <= end; i++) {

            amount -= withdraw[i - startingYear];

            // BB.PP.
            amount -= Math.max(amount - this.bbppMin, 0.0d) * this.bbppTaxRate;

            if (amount > 0.0d) {
                amount *= returns[i - startingYear];
            } else {
                cashAmount += amount;
                amount = 0.0d;
            }
            if (cashAmount <= 0.0d) {
                return false;
            }
        }

        return amount + cashAmount > 0.0d;
    }

    private double gauss(double mean, double std) {
        return mean + ThreadLocalRandom.current().nextGaussian() * std;
    }

    private int gauss(int mean, int std) {

        return (int) Math.round(gauss((double) mean, (double) std));
    }

    public void goal(
            final int trials,
            final int monthlyDeposit,
            final int monthlyWithdraw,
            final int inflation,
            final int retirementAge,
            final BigDecimal extraCash,
            final boolean afterTax,
            final int age,
            final int pension,
            MoneyAmount todaySavings,
            MoneyAmount invested,
            boolean expected) {

        final var to = USD_INFLATION.getTo();

        final var cash = todaySavings.getAmount()
                .subtract(invested.getAmount(), C)
                .add(extraCash, C).doubleValue();

        final var inflationRate = ONE.setScale(SCALE, RM)
                .add(BigDecimal.valueOf(inflation).setScale(SCALE, RM).movePointLeft(2), C).doubleValue();

        final var yearBuyTransactions = BigDecimal.TEN;

        final var yearDeposit = BigDecimal.valueOf(monthlyDeposit * 13)
                .subtract(BUY_FEE, C);

        final var yearIBKRFee = IBKR_FEE_STRATEGY
                .apply(yearDeposit.divide(yearBuyTransactions, C))
                .multiply(yearBuyTransactions, C);

        final var deposit = yearDeposit.subtract(yearIBKRFee, C).doubleValue();

        final var withdraw = BigDecimal.valueOf((monthlyWithdraw * 12) - (pension * 13))
                .multiply(ONE.divide(ONE.subtract(SELL_FEE, C), C), C)
                .multiply(afterTax ? CAPITAL_GAINS_TAX_EXTRA_WITHDRAWAL_PCT : ONE, C).doubleValue();

        final var investedAmount = invested.getAmount().doubleValue();

        this.console.appendLine(format("Cash: {0,number,currency}, invested: {1,number,currency}", cash, investedAmount));
        this.console.appendLine(format(
                "Saving {0,number,currency}, spending {1,number,currency} {2}",
                monthlyDeposit,
                monthlyWithdraw,
                afterTax ? " after tax." : "."));
        if (pension > 0) {
            this.console.appendLine(format("Considering {0,number,currency} pension.", pension));
        }
        this.console.appendLine(format("Expected {0}% inflation, retiring at {1}, until age {2} +/-{3}.", inflation, retirementAge, age, END_AGE_STD));

        final var retirementYear = 1978 + retirementAge;

        final var periodYears = retirementYear - YearMonth.of(new Date()).getYear();

        final int startingYear = to.getYear();
        final var end = 1978 + age;
        final var yearsLeft = 100;

        final var periods = (int) Math.ceil((float) yearsLeft / periodYears);

        final var inflationFactors = IntStream.range(0, 180)
                .mapToDouble(year -> Math.pow(inflationRate, year))
                .toArray();

        final var realDeposits = Arrays.stream(inflationFactors)
                .map(f -> f * deposit)
                .toArray();

        final var realWithdrawals = Arrays.stream(inflationFactors)
                .map(f -> f * withdraw)
                .toArray();

        long successes;
        if (expected) {
            successes = this.expectedReturnSuccesses(
                    new GaussReturnSupplier(
                            US_NOMINAL_EXPECTED_RETURN * 0.8d + EX_US_NOMINAL_EXPECTED_RETURN * 0.2d,
                            US_EXPECTED_RETURN_STDDEV * 0.8d + EX_US_EXPECTED_RETURN_STDDEV * 0.2d,
                            periodYears * periods),
                    trials,
                    startingYear,
                    retirementYear,
                    end,
                    age,
                    investedAmount,
                    realDeposits,
                    realWithdrawals);

        } else {
            successes = this.historicReturnSuccesses(
                    new HistoricalReturnSupplier("index/sp-total-return.json"),
                    periodYears,
                    trials,
                    periods,
                    startingYear,
                    retirementYear,
                    end,
                    age,
                    investedAmount,
                    realDeposits,
                    realWithdrawals);
        }
        this.console.appendLine(format("\nSimulating {0} {1}-year periods.", trials, periodYears));

        this.console.appendLine(format("{0}/{1} {2}", successes, trials, this.format.percent(BigDecimal.valueOf((double) successes / (double) trials))));

    }

    private long historicReturnSuccesses(
            Supplier<List<BigDecimal>> returnsSuplier,
            int periodYears,
            int trials,
            int periods,
            int startingYear,
            int retirementYear,
            int end,
            int cash,
            double investedAmount,
            double[] realDeposits,
            double[] realWithdrawals) {

        final var sp500TotalReturns = returnsSuplier.get();

        final double keepWorsePct = 0.75d;

        final var fullYearsPeriods = this.periods(
                sp500TotalReturns,
                periodYears,
                keepWorsePct);

        final var halfYearsPeriods = this.periods(
                sp500TotalReturns,
                BigDecimal.valueOf(periodYears).divide(BigDecimal.valueOf(2l), RoundingMode.CEILING).intValue(),
                keepWorsePct);

        final var allPeriods = new ArrayList<double[]>(fullYearsPeriods.size() + halfYearsPeriods.size());

        for (var half : halfYearsPeriods) {
            for (var full : fullYearsPeriods) {

                allPeriods.add(DoubleStream.concat(Arrays.stream(half), Arrays.stream(full)).toArray());
            }
        }

        return IntStream.range(0, trials)
                .parallel()
                .mapToObj(i -> this.randomPeriods(allPeriods, periods, CSPX_FEE))
                .filter(randomReturns
                        -> this.goals(
                        startingYear,
                        retirementYear,
                        gauss(end, END_AGE_STD),
                        cash,
                        investedAmount,
                        randomReturns,
                        realDeposits,
                        realWithdrawals))
                .count();
    }

    private long expectedReturnSuccesses(
            Supplier<double[]> returnsSuplier,
            int trials,
            int startingYear,
            int retirementYear,
            int end,
            int cash,
            double investedAmount,
            double[] realDeposits,
            double[] realWithdrawals) {

        return IntStream.range(0, trials)
                .parallel()
                .mapToObj(i -> returnsSuplier.get())
                .filter(randomReturns
                        -> this.goals(
                        startingYear,
                        retirementYear,
                        gauss(end, END_AGE_STD),
                        cash,
                        investedAmount,
                        randomReturns,
                        realDeposits,
                        realWithdrawals))
                .count();
    }

    private double[] randomPeriods(List<double[]> allReturns, int periods, double fee) {
        return ThreadLocalRandom.current().ints(periods, 0, allReturns.size())
                .mapToObj(allReturns::get)
                .flatMapToDouble(Arrays::stream)
                .map(value -> value - fee)
                .toArray();
    }

    /**
     * Me quedo con el keepWorsePct % peor.
     */
    private List<double[]> periods(List<BigDecimal> returns, final int years, double keepWorsePct) {

        final var periodCount = returns.size() - years + 1;

        return IntStream.range(0, periodCount)
                .mapToObj(start -> returns.stream().skip(start).limit(years))
                .map(l -> l.mapToDouble(BigDecimal::doubleValue).toArray())
                .sorted(comparing(this::sum))
                .limit(Math.round(periodCount * keepWorsePct))
                .collect(toList());
    }

    private double sum(double[] l) {
        return Arrays.stream(l).sum();
    }

}
