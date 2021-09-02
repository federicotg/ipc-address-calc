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
public class PPIGlobalUSDFeeStrategy implements Function<BigDecimal, BigDecimal> {

    private final BigDecimal feeRate;
    private final BigDecimal feeTaxRate;

    public PPIGlobalUSDFeeStrategy(BigDecimal feeRate, BigDecimal feeTaxRate) {
        this.feeRate = feeRate;
        this.feeTaxRate = feeTaxRate;
    }
    
    @Override
    public BigDecimal apply(BigDecimal presentValue) {
        
        final var sellFee = presentValue.multiply(this.feeRate, CONTEXT).max(BigDecimal.TEN);

        final var sellFeeTax = sellFee.multiply(this.feeTaxRate, CONTEXT);

        return sellFee.add(sellFeeTax, CONTEXT);
        
    }

    
    
}
