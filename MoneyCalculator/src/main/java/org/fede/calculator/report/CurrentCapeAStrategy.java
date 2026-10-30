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
import static org.fede.calculator.money.MathConstants.C;

/**
 * Spend-down oriented intercept: {@code min(2%, 80% / yearsLeft)}.
 *
 * @author fede
 */
public final class CurrentCapeAStrategy implements CapeAStrategy {

    private static final BigDecimal CAP = new BigDecimal("0.02");
    private static final BigDecimal SPEND_FRACTION = BigDecimal.valueOf(80).movePointLeft(2);

    @Override
    public BigDecimal baseCapeA(BigDecimal yearsLeft) {
        return CAP.min(ONE.divide(yearsLeft, C).multiply(SPEND_FRACTION, C));
    }
}
