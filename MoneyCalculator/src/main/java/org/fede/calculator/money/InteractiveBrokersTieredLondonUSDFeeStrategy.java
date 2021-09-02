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

    @Override
    public BigDecimal apply(BigDecimal t) {

        final var ibkrTieredUSDFee = new BigDecimal("0.0005");
        final var ibkrTieredUSDMinFee = new BigDecimal("1.7");
        final var ibkrTieredUSDMaxFee = new BigDecimal("39");

        final var exchangeFee = new BigDecimal("0.000045");
        final var minFee = new BigDecimal("0.13836");
        final var clearingFee = new BigDecimal("0.083016");

        return t
                .multiply(ibkrTieredUSDFee, MathConstants.CONTEXT)
                .max(ibkrTieredUSDMinFee)
                .min(ibkrTieredUSDMaxFee)
                .add(t.multiply(exchangeFee, MathConstants.CONTEXT).max(minFee).add(clearingFee));

    }

}
