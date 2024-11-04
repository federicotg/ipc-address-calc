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

    private final TypeReference<Map<String, ExpectedReturnGroup>> TR = new TypeReference<Map<String, ExpectedReturnGroup>>() {
    };

    private final BigDecimal MONTHS_IN_A_YEAR = new BigDecimal("12");

    private final BigDecimal SELL_FEE = new BigDecimal("0.00726").multiply(new BigDecimal("0.5"), C)
            .add(new BigDecimal("0.00056").multiply(new BigDecimal("0.5"), C));

    private final BigDecimal CAPITAL_GAINS_TAX_PCT = new BigDecimal("0.15");

    private final BigDecimal HEALTH_MONTHLY_COST = new BigDecimal("400");


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
            final int badReturnYears,
            final double badYearSpending,
            final int saveCashYears,
            final double[] returns,
            final double[] deposit,
            final double[] withdrawal,
            final double bbppMin) {

        this.nLowestAtRetirement(returns, badReturnYears, retirement, startingYear);

        double cashAmount = cash;
        double amount = investedAmount;
        double bbpp, gainPct, gainTaxPct, effectiveTaxPct, capitalGainsTaxFactor;
        // depositing
        for (var i = startingYear; i < retirement; i++) {

            // BB.PP.
            bbpp = Math.max(amount - bbppMin, 0.0d) * this.bbppTaxRate;
            final var d = deposit[i - startingYear];

            amount -= bbpp;

            amount = amount * returns[i - startingYear];

            if (i < retirement - saveCashYears) {
                amount += d;
            } else {
                cashAmount += d;
            }
        }

        final var cgtPct = CAPITAL_GAINS_TAX_PCT.doubleValue();

        // withdrawing
        for (var i = retirement; i <= end; i++) {

            // capital gain tax 22 years of gains
            gainPct = IntStream.range(i - retirement, i - retirement + 22)
                    .mapToDouble(y -> returns[y])
                    .reduce(1.0d, (d1, d2) -> d1 * d2);

            gainTaxPct = Math.max(0.0d, (gainPct - 1.0d) * cgtPct);
            effectiveTaxPct = gainTaxPct / gainPct;
            capitalGainsTaxFactor = 1.0d / (1.0d - effectiveTaxPct);

            final var lastYearReturn = returns[i - startingYear - 1];

            // withdrawal strategy: bad year => withdraw less.
            var thisYearWithdrawal = withdrawal[i - startingYear] * (lastYearReturn <= 0.9d ? badYearSpending : 1.0d);

            if (cashAmount >= thisYearWithdrawal && lastYearReturn < 1.0d) {
                // usar cash 
                cashAmount -= thisYearWithdrawal;
            } else {
                // (cashAmount <= thisYearWithdrawal) => sell investments
                amount -= (thisYearWithdrawal * capitalGainsTaxFactor);
            }

            bbpp = Math.max(amount - bbppMin, 0.0d) * this.bbppTaxRate;
            amount -= bbpp * capitalGainsTaxFactor;
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
            final int age,
            final int pension,
            MoneyAmount todaySavings,
            MoneyAmount invested,
            String expected,
            int badReturnYears,
            double badYearSpending,
            int saveCashYears) {

        final var spendingAndSaving = new Savings(this.format, this.series, this.bar, this.console)
                .averageSpendingAndSaving(averageIncomeSpendingMonths);

        final var monthlyDeposit = spendingAndSaving.saving().getAmount();
        final var monthlyWithdraw = spendingAndSaving.spending().getAmount()
                .subtract(new BigDecimal(pension), C);

        this.goal(trials, 
                monthlyDeposit, 
                monthlyWithdraw, 
                inflation, 
                retirementAge, 
                extraCash, 
                age, 
                pension, 
                todaySavings, 
                invested, 
                expected, 
                badReturnYears,
                badYearSpending,
                saveCashYears
                );
    }

    private void goal(
            final int trials,
            final BigDecimal monthlyDeposit,
            final BigDecimal monthlyWithdraw,
            final BigDecimal inflation,
            final int retirementAge,
            final BigDecimal extraCash,
            final int age,
            final int pension,
            MoneyAmount todaySavings,
            MoneyAmount invested,
            String expected,
            int badReturnYears,
            final double badYearSpending,
            final int saveCashYears) {

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
        final var deposit = monthlyDeposit.multiply(MONTHS_IN_A_YEAR, C).doubleValue();

        final var withdraw = (monthlyWithdraw
                .add(HEALTH_MONTHLY_COST, C)
                .multiply(MONTHS_IN_A_YEAR, C)
                .subtract(new BigDecimal(pension * 13), C))
                .multiply(ONE.divide(ONE.subtract(SELL_FEE, C), C), C)
                .doubleValue();

        final var investedAmount = invested.getAmount().doubleValue();

        final var legend = """
    - Cash: {0,number,currency}
    - Invested: {1,number,currency}
    - Saving: {2}
    - Spending: {3}
    - Wealth Tax: {4,number,#.##} %. Min: {5,number,currency}
    - Capital Gains Tax: {6,number,percent}
    - Health Cost: {7,number,currency}
    - Pension: {8,number,currency}
    - Expected Inflation: {9,number,#.##} %
    - Sequence of Returns Risk: {10,number} years
    - Retiring at {11,number} living until {12,number}
    - Saving {13,number,percent} on bad return years
    - Saving cash {14,number} years before retirement
        """;

        final var formattedDeposit = this.format.text(format("{0,number,currency}", monthlyDeposit), 6, new AnsiFormat(Attribute.BRIGHT_GREEN_TEXT()));
        final var formattedWithdrawal = this.format.text(format("{0,number,currency}", monthlyWithdraw), 6, new AnsiFormat(Attribute.BRIGHT_RED_TEXT()));

        final var bbppMin = this.series.bbppSeries()
                .stream()
                .map(bbpp -> bbpp.minimum().divide(bbpp.usd(), MathConstants.C))
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(30000d);

        this.console.appendLine(format(legend,
                cash,
                investedAmount,
                formattedDeposit,
                formattedWithdrawal,
                this.bbppTaxRate * 100.0d,
                bbppMin,
                CAPITAL_GAINS_TAX_PCT,
                HEALTH_MONTHLY_COST,
                pension,
                inflation,
                badReturnYears,
                retirementAge,
                age,
                1.0d - badYearSpending,
                saveCashYears));

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
                        this.expectedReturnSuccesses(new GaussReturnSupplier(entry.getValue().weightedMu(), entry.getValue().weightedSigma(), yearsLeft),
                                trials,
                                startingYear,
                                retirementYear,
                                end,
                                cash,
                                investedAmount,
                                realDeposits,
                                realWithdrawals,
                                bbppMin,
                                badReturnYears, 
                                badYearSpending, 
                                saveCashYears)))
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
                "{0} {1}",
                label,
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
            double bbppMin,
            int badReturnYears,
            final double badYearSpending,
            final int saveCashYears) {

        return IntStream.range(0, trials)
                .mapToDouble(p
                        -> this.goals(
                        startingYear,
                        retirementYear,
                        end,
                        cash,
                        investedAmount,
                        badReturnYears,
                        badYearSpending,
                        saveCashYears,
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

}
