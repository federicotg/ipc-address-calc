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
public class InteractiveBrokersTieredLondonUSDFeeStrategy implements Function<BigDecimal, BigDecimal> {

    private static final BigDecimal GBPUSD = new BigDecimal("1.0858");

    private static final BigDecimal IBKR_TIERED_USD_FEE = new BigDecimal("0.0005");
    private static final BigDecimal IBKR_TIERED_USD_MIN_FEE = new BigDecimal("1.7");
    private static final BigDecimal IBKR_TIERED_USD_MAX_FEE = new BigDecimal("39");

    private static final BigDecimal EXCHANGE_FEE = new BigDecimal("0.000045");
    private static final BigDecimal MIN_EXCHANGE_FEE = new BigDecimal("0.1").multiply(GBPUSD, MathConstants.C);
    private static final BigDecimal CLEARING_FEE = new BigDecimal("0.06").multiply(GBPUSD, MathConstants.C);

    @Override
    public BigDecimal apply(BigDecimal t) {

        return t
                .multiply(IBKR_TIERED_USD_FEE, MathConstants.C).max(IBKR_TIERED_USD_MIN_FEE).min(IBKR_TIERED_USD_MAX_FEE)
                .add(t.multiply(EXCHANGE_FEE, MathConstants.C).max(MIN_EXCHANGE_FEE))
                .add(CLEARING_FEE);

    }

}
