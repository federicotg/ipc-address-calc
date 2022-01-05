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

/**
 *
 * @author fede
 */
public class InteractiveBrokersTieredEuronextEURFeeStrategy implements Function<BigDecimal, BigDecimal> {

    private static final BigDecimal IBKR_TIERED_EUR_FEE = new BigDecimal("0.0005");
    private static final BigDecimal IBKR_TIERED_EUR_MIN_FEE = new BigDecimal("1.25");
    private static final BigDecimal IBKR_TIERED_EUR_MAX_FEE = new BigDecimal("29");
    private static final BigDecimal EXCHANGE_FEE = new BigDecimal("0.000060");
    private static final BigDecimal MIN_EXCHANGE_FEE = new BigDecimal("0.75");
    private static final BigDecimal CLEARING_FEE = new BigDecimal("0.1");

    @Override
    public BigDecimal apply(BigDecimal t) {

        return t.multiply(IBKR_TIERED_EUR_FEE, MathConstants.C).max(IBKR_TIERED_EUR_MIN_FEE).min(IBKR_TIERED_EUR_MAX_FEE)
                .add(t.multiply(EXCHANGE_FEE, MathConstants.C).max(MIN_EXCHANGE_FEE))
                .add(CLEARING_FEE);
    }

}
