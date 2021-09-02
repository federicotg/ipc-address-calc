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
import static org.fede.calculator.money.MathConstants.CONTEXT;

/**
 *
 * @author fede
 */
public class PPIGlobalEURFeeStrategy implements Function<BigDecimal, BigDecimal> {

    private final BigDecimal feeRate;
    private final BigDecimal feeTaxRate;
    private final BigDecimal fxFeeRate;

    public PPIGlobalEURFeeStrategy(BigDecimal feeRate, BigDecimal feeTaxRate, BigDecimal fxFeeRate) {
        this.feeRate = feeRate;
        this.feeTaxRate = feeTaxRate;
        this.fxFeeRate = fxFeeRate;
    }

    @Override
    public BigDecimal apply(BigDecimal presentValue) {

        final var fee = presentValue.multiply(this.feeRate, CONTEXT);

        final var fxFee = presentValue.multiply(this.fxFeeRate, CONTEXT);

        final var totalFee = fee.add(fxFee, CONTEXT).max(BigDecimal.TEN);
        
        final var feeTax = totalFee.multiply(this.feeTaxRate, CONTEXT);

        return totalFee.add(feeTax, CONTEXT);

    }

}
