/*
 * Copyright (C) 2021 fede
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
import java.util.function.Function;
import static org.fede.calculator.money.MathConstants.C;

/**
 *
 * @author fede
 */
public class PPIGlobalEURFeeStrategy implements Function<BigDecimal, BigDecimal> {

    private static final BigDecimal FEE_RATE = new BigDecimal("0.006");
    private static final BigDecimal FEE_TAX_RATE = new BigDecimal("0.21");
    private static final BigDecimal FX_FEE_RATE = new BigDecimal("0.0025");

    @Override
    public BigDecimal apply(BigDecimal presentValue) {

        final var fee = presentValue.multiply(FEE_RATE, C);

        final var fxFee = presentValue.multiply(FX_FEE_RATE, C);

        final var totalFee = fee.add(fxFee, C).max(BigDecimal.TEN);
        
        final var feeTax = totalFee.multiply(FEE_TAX_RATE, C);

        return totalFee.add(feeTax, C);

    }

}
