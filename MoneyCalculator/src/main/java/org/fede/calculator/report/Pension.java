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
import java.math.MathContext;
import java.time.YearMonth;
import org.fede.calculator.money.Currency;
import static org.fede.calculator.money.Currency.USD;
import org.fede.calculator.money.ForeignExchanges;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.series.SeriesReader;

/**
 *
 * @author fede
 */
public class Pension {

    public MoneyAmount discountedCashFlowValue() {

        final var now = YearMonth.now();

        final var futurePension = ForeignExchanges.getForeignExchange(Currency.ARS, USD)
                .exchange(
                        new MoneyAmount(
                                SeriesReader.readBigDecimal("min.pension"), Currency.ARS),
                        USD,
                        now);

        final int retirementAge = SeriesReader.readInt("goal.retirement");
        final int maxAge = SeriesReader.readInt("goal.maxage");
        final var dob = SeriesReader.readDate("dob");

        // Monthly pension → yearly (13 payments)
        final BigDecimal yearlyPension = futurePension.amount()
                .multiply(BigDecimal.valueOf(13));

        // Real discount rate (must be consistent with pension being in real USD)
        final BigDecimal r = SeriesReader.readPercent("pensionDiscountRate");

        // Precise time handling (fractional years)
        final double yearsToRetire = monthsBetween(now, YearMonth.from(dob).plusYears(retirementAge)) / 12.0;
        final int yearsInRetirement = maxAge - retirementAge;

        if (yearsInRetirement <= 0) {
            return new MoneyAmount(BigDecimal.ZERO, USD);
        }

        final MathContext mc = MathContext.DECIMAL64;

        final BigDecimal one = BigDecimal.ONE;
        final BigDecimal onePlusR = one.add(r, mc);

        // (1 + r)^(-T)
        final BigDecimal discountToRetirement
                = BigDecimal.valueOf(Math.pow(onePlusR.doubleValue(), -yearsToRetire));

        // (1 + r)^(-N)
        final BigDecimal discountOverRetirement
                = BigDecimal.valueOf(Math.pow(onePlusR.doubleValue(), -yearsInRetirement));

        final BigDecimal annuityFactor;

        if (r.compareTo(BigDecimal.ZERO) == 0) {
            // Edge case: zero discount rate
            annuityFactor = BigDecimal.valueOf(yearsInRetirement);
        } else {
            // (1 - (1+r)^(-N)) / r
            annuityFactor = one.subtract(discountOverRetirement, mc)
                    .divide(r, mc);
        }

        final BigDecimal pv = yearlyPension
                .multiply(annuityFactor, mc)
                .multiply(discountToRetirement, mc);

        return new MoneyAmount(pv, USD);
    }

    private static long monthsBetween(YearMonth start, YearMonth end) {
        return (end.getYear() - start.getYear()) * 12L
                + (end.getMonthValue() - start.getMonthValue());
    }

}
