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

import com.fasterxml.jackson.core.type.TypeReference;
import java.math.BigDecimal;
import static java.math.BigDecimal.ONE;
import static java.text.MessageFormat.format;
import java.util.Arrays;
import static java.util.Comparator.comparing;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import static java.util.stream.Collectors.toList;
import java.util.stream.IntStream;
import static org.fede.calculator.money.Inflation.USD_INFLATION;
import org.fede.calculator.money.series.AnnualHistoricalReturn;
import org.fede.calculator.money.series.SeriesReader;
import org.fede.calculator.money.series.YearMonth;
import static org.fede.calculator.money.MathConstants.C;
import static org.fede.calculator.money.MathConstants.RM;
import static org.fede.calculator.money.MathConstants.SCALE;

/**
 *
 * @author fede
 */
public class Goal {

    private static final double RUSSELL2000_PCT = 0.1d;
    private static final double SP500_PCT = 0.7d;
    private static final double EIMI_PCT = 0.1d;
    private static final double MEUD_PCT = 0.1d;

    private static final double CSPX_FEE = 0.0007d;
    private static final double XRSU_FEE = 0.003d;
    private static final double EIMI_FEE = 0.0018d;
    private static final double MEUD_FEE = 0.0007d;

    private static final int END_AGE_STD = 6;

    private static final BigDecimal BUY_FEE = new BigDecimal("225.00");
    
    private static final BigDecimal SELL_FEE = new BigDecimal("0.01926")
            .multiply(new BigDecimal("0.5"), C)
            .add(new BigDecimal("0.00096")
            .multiply(new BigDecimal("0.5"), C),C);

    private static final BigDecimal CAPITAL_GAINS_TAX_EXTRA_WITHDRAWAL_PCT = ONE.divide(ONE.subtract(new BigDecimal("0.15"), C), C);

    private final double bbppTaxRate;
    private final double bbppMin;

    private List<BigDecimal> sp500TotalReturns;
    private List<BigDecimal> russell2000TotalReturns;

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
            amount -=  Math.max(amount - this.bbppMin, 0.0d) * this.bbppTaxRate;

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

    private AnnualHistoricalReturn real(AnnualHistoricalReturn nominal) {
        return new AnnualHistoricalReturn(
                nominal.getYear(),
                Inflation.USD_INFLATION.adjust(
                        new MoneyAmount(nominal.getTotalReturn(), "USD"),
                        YearMonth.of(nominal.getYear(), 12),
                        YearMonth.of(nominal.getYear() - 1, 12)).getAmount());
    }

    public void goal(
            final int trials,
            final int periodYears,
            final int monthlyDeposit,
            final int monthlyWithdraw,
            final int inflation,
            final int retirementAge,
            final BigDecimal extraCash,
            final boolean onlySP500,
            final boolean afterTax,
            final int age,
            final int pension,
            MoneyAmount todaySavings,
            MoneyAmount invested) {

        final var tr = new TypeReference<List<AnnualHistoricalReturn>>() {
        };

        this.sp500TotalReturns = SeriesReader.read("index/sp-total-return.json", tr)
                .stream()
                .sorted(comparing(AnnualHistoricalReturn::getYear))
                .map(this::real)
                .map(AnnualHistoricalReturn::getTotalReturn)
                .map(r -> ONE.setScale(MathConstants.SCALE, RM).add(r.setScale(SCALE, RM).movePointLeft(2), C))
                .collect(toList());

        this.russell2000TotalReturns = SeriesReader.read("index/russell2000.json", tr)
                .stream()
                .sorted(comparing(AnnualHistoricalReturn::getYear))
                .map(this::real)
                .map(AnnualHistoricalReturn::getTotalReturn)
                .map(r -> ONE.setScale(SCALE, RM).add(r.setScale(SCALE, RM).movePointLeft(2), C))
                .collect(toList());

        final var to = USD_INFLATION.getTo();

        final var cash = todaySavings.getAmount()
                .subtract(invested.getAmount(), C)
                .add(extraCash, C).doubleValue();

        final var inflationRate = ONE.setScale(SCALE, RM)
                .add(BigDecimal.valueOf(inflation).setScale(SCALE, RM).movePointLeft(2), C).doubleValue();

        final var deposit = BigDecimal.valueOf(monthlyDeposit * 13).subtract(BUY_FEE, C).doubleValue();
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

        final var allSP500Periods = this.periods(this.sp500TotalReturns, periodYears, 0.85d);
        final var allRussell2000Periods = this.periods(this.russell2000TotalReturns, periodYears, 0.9d);
        final var allEIMIPeriods = this.periods(this.sp500TotalReturns, periodYears, 0.8d);
        final var allMEUDPeriods = this.periods(this.sp500TotalReturns, periodYears, 0.8d);

        final var successes = IntStream.range(0, trials)
                .parallel()
                .mapToObj(i -> this.balanceProportions(periods, allSP500Periods, allRussell2000Periods, allEIMIPeriods, allMEUDPeriods, onlySP500, CSPX_FEE, XRSU_FEE, EIMI_FEE, MEUD_FEE))
                .filter(randomReturns
                        -> this.goals(
                        startingYear,
                        1978 + retirementAge,
                        gauss(end, END_AGE_STD),
                        cash,
                        investedAmount,
                        randomReturns,
                        realDeposits,
                        realWithdrawals))
                .count();

        this.console.appendLine(format("\nSimulating {0} {1}-year periods.", trials, periodYears));

        this.console.appendLine(format("{0}/{1} {2}", successes, trials, this.format.percent(BigDecimal.valueOf((double) successes / (double) trials))));

    }

    private double[] randomPeriods(List<double[]> allReturns, int periods, double fee) {
        return ThreadLocalRandom.current().ints(periods, 0, allReturns.size())
                .mapToObj(allReturns::get)
                .flatMapToDouble(Arrays::stream)
                .map(value -> value - fee)
                .toArray();
    }

    private double[] balanceProportions(int periods,
            List<double[]> allSP500Periods,
            List<double[]> allRussell2000Periods,
            List<double[]> allEIMIPeriods,
            List<double[]> allMEUDPeriods,
            boolean onlySP500,
            double sp500Fee,
            double russellFee,
            double eimiFee,
            double meudFee) {

        final var sp500Periods = this.randomPeriods(allSP500Periods, periods, sp500Fee);

        final var russell2000Periods = onlySP500
                ? sp500Periods
                : this.randomPeriods(allRussell2000Periods, periods, russellFee);

        final var eimiPeriods = onlySP500
                ? sp500Periods
                : this.randomPeriods(allEIMIPeriods, periods, eimiFee);

        final var meudPeriods = onlySP500
                ? sp500Periods
                : this.randomPeriods(allMEUDPeriods, periods, meudFee);

        return IntStream.range(0, sp500Periods.length)
                .mapToDouble(i -> sp500Periods[i] * SP500_PCT
                + russell2000Periods[i] * RUSSELL2000_PCT
                + meudPeriods[i] * MEUD_PCT
                + eimiPeriods[i] * EIMI_PCT)
                .toArray();
    }

    /**
     * Me quedo con el keepWorsePct % peor.
     */
    private List<double[]> periods(List<BigDecimal> returns, final int years, double keepWorsePct) {

        var periods = IntStream.range(0, returns.size() - years + 1)
                .mapToObj(start -> returns.stream().skip(start).limit(years).mapToDouble(BigDecimal::doubleValue).toArray())
                .sorted(comparing(this::sum))
                .collect(toList());

        if (keepWorsePct > 0.0d) {
            periods = periods.stream()
                    .limit(Math.round(periods.size() * keepWorsePct))
                    .collect(toList());
        }
        return periods;

    }

    private double sum(double[] l) {
        return Arrays.stream(l).sum();
    }

}
