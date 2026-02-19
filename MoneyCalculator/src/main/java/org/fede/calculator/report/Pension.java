/*
 * Copyright (C) 2025 fede
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
import java.time.Month;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.function.Function;
import org.fede.calculator.money.Currency;
import static org.fede.calculator.money.Currency.ARS;
import static org.fede.calculator.money.Currency.USD;
import org.fede.calculator.money.ForeignExchanges;
import org.fede.calculator.money.Inflation;
import org.fede.calculator.money.MathConstants;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.series.SeriesReader;
import static org.fede.calculator.money.series.SeriesReader.readSeries;

/**
 *
 * @author fede
 */
public class Pension {

    private boolean isSAC(YearMonth ym) {
        return ym.getMonthValue() == 12
                || ym.getMonthValue() == 7;
    }

    public MoneyAmount discountedCashFlowValue() {

        final var futurePension = ForeignExchanges.getForeignExchange(Currency.ARS, USD)
                        .exchange(
                                new MoneyAmount(
                                        SeriesReader.readBigDecimal("goal.pension"), Currency.ARS),
                                USD, YearMonth.now());
        final var retirementAge = SeriesReader.readInt("goal.retirement");
        final var maxAge = SeriesReader.readInt("goal.maxage");
        final var dob = SeriesReader.readDate("dob");

        final var yearlyPension = futurePension.amount().doubleValue() * 13.0d;

        double sum = 0.0d;
        final double discountRate = 1.0d + SeriesReader.readPercent("pensionDiscountRate").doubleValue();
        final var retirementYear = dob.getYear() + retirementAge;
        final var yearsToRetire = retirementYear - YearMonth.now().getYear();
        final var endYear = dob.getYear() + maxAge - YearMonth.now().getYear();

        for (int i = yearsToRetire; i < endYear; i++) {
            sum += yearlyPension / Math.pow(discountRate, i);
        }
        return new MoneyAmount(new BigDecimal(sum), USD);
    }

    public MoneyAmount value() {
        final var unlp = readSeries("income/unlp.json");
        final var despegar = readSeries("income/despegar.json")
                .add(readSeries("income/despegar-split.json")
                        .exchangeInto(ARS));

        final var simultaneousYears = Math.round((double) despegar.getFrom().until(unlp.getTo(), ChronoUnit.MONTHS) / 12.0d);

        final var simultaneousPercent = BigDecimal.valueOf(82).movePointLeft(2)
                .divide(BigDecimal.valueOf(30l), MathConstants.C);

        final var pctUNLP = simultaneousPercent
                .multiply(BigDecimal.valueOf(simultaneousYears), MathConstants.C);

        final var bestArsUNLP = unlp
                .filter((ym, m) -> !this.isSAC(ym) && ym.isBefore(YearMonth.of(2024, 10)))
                .max(Comparator.comparing(MoneyAmount::amount))
                .orElse(MoneyAmount.zero(ARS));

        final Function<MoneyAmount, MoneyAmount> toARS = ma -> ForeignExchanges.getForeignExchange(USD, ARS)
                .exchange(ma, ARS, unlp.getTo());

        final Function<MoneyAmount, MoneyAmount> toUSD = ma -> ForeignExchanges.getForeignExchange(ARS, USD)
                .exchange(ma, USD, Inflation.USD_INFLATION.getTo());

        final Function<MoneyAmount, MoneyAmount> real = ma -> Inflation.USD_INFLATION
                .adjust(ma, unlp.getTo(), Inflation.USD_INFLATION.getTo());

        final var extra = toUSD
                .andThen(real)
                .andThen(toARS)
                .apply(bestArsUNLP)
                .adjust(BigDecimal.ONE, pctUNLP);

        final var adjustedDespegar = Inflation.ANSES.adjust(despegar, Inflation.ANSES.getTo());

        var total10YearsSalary = MoneyAmount.zero(ARS);
        for (var ym = YearMonth.now().plusMonths(-120); !ym.isAfter(adjustedDespegar.getTo()); ym = ym.plusMonths(1)) {

            total10YearsSalary = total10YearsSalary
                    .add(
                            switch (ym.getMonthValue()) {
                        case 6, 7 -> // aguinaldo
                            adjustedDespegar.getAmount(YearMonth.of(ym.getYear(), Month.MAY));
                        case 12 -> // aguinaldo
                            adjustedDespegar.getAmount(YearMonth.of(ym.getYear(), Month.NOVEMBER));
                        case 3 -> // bono
                            adjustedDespegar.getAmount(YearMonth.of(ym.getYear(), Month.FEBRUARY));
                        case 9 -> // bono
                            adjustedDespegar.getAmount(YearMonth.of(ym.getYear(), Month.AUGUST));
                        default ->
                            adjustedDespegar.getAmount(ym);
                    });

        }

        // divido por 240 para considerar la mitad.
        final var adjusted10YearAverageSalary = total10YearsSalary
                .adjust(BigDecimal.valueOf(120l * 2l), BigDecimal.ONE);

        // prestación básica universal: 
        final var pbu = new MoneyAmount(SeriesReader.readBigDecimal("pbu"), ARS);

        // prestación adicional por permanencia
        // 1.5% every year. 30 years = 45%.
        final var pap = adjusted10YearAverageSalary
                .adjust(BigDecimal.ONE, BigDecimal.valueOf(45).movePointLeft(2));

        // hay un máximo
        final var pension = pbu.add(pap).add(extra)
                .min(new MoneyAmount(SeriesReader.readBigDecimal("max.pension"), ARS));

        return ForeignExchanges.getForeignExchange(ARS, USD)
                .exchange(pension, USD, Inflation.USD_INFLATION.getTo());

    }
}
