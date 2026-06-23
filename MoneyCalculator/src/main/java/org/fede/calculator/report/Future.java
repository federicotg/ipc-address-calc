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
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.fede.calculator.money.Currency;
import static org.fede.calculator.money.Currency.USD;
import org.fede.calculator.money.ForeignExchanges;
import static org.fede.calculator.money.MathConstants.C;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.series.SeriesReader;

/**
 *
 * @author fede
 */
public class Future {

    private static final String FUTURE_CASH_KEY = "futureCash";

    public static MoneyAmount expectedWealth() {
        final var futureRealStateKey = "futureRealState";
        final var futureCashKey = FUTURE_CASH_KEY;

        final var inflationRate = SeriesReader.readPercent("expectedInflation").doubleValue();
        return IntStream.range(1, 3)
                .mapToObj(index -> Stream.of(futureRealStateKey, futureCashKey).map(k -> new FutureCashFlows(k, index)))
                .flatMap(Function.identity())
                .map(fcf -> presentValue(fcf.name, fcf.index, fcf.name.equals(FUTURE_CASH_KEY) ? inflationRate : 0.0d))
                .reduce(MoneyAmount.zero(USD), MoneyAmount::add)
                .add(new Pension().discountedCashFlowValue());
    }

    private static MoneyAmount presentValue(String key, int index, double discountRate) {

        MoneyAmount amount;
        if (FUTURE_CASH_KEY.equals(key) && index == 2) {
            amount = Future.severance().getTotal();
        } else {
            amount = SeriesReader.readUSD(key + "." + index);
        }

        final var years = SeriesReader.readInt(key + "Years." + index);
        final var probability = SeriesReader.readPercent(key + "Prob." + index);

        return new MoneyAmount(
                BigDecimal.valueOf(amount.adjust(ONE, probability).amount().doubleValue() / Math.pow(1.0d + discountRate, years)),
                USD);
    }

    private record FutureCashFlows(String name, int index) {

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
        final var taxSalaryFactor = BigDecimal.valueOf(65-7).movePointLeft(2);
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
}
