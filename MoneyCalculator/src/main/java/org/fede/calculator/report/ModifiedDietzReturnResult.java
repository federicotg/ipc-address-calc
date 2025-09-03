/*
 * Copyright (C) 2022 federicogentile
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

/**
 *
 * @author federicogentile
 */
public record ModifiedDietzReturnResult(BigDecimal moneyWeighted, BigDecimal annualizedMoneyWeighted) implements Comparable<ModifiedDietzReturnResult> {

    public BigDecimal getMoneyWeighted() {
        return moneyWeighted;
    }

    public BigDecimal getAnnualizedMoneyWeighted() {
        return annualizedMoneyWeighted;
    }

    @Override
    public int compareTo(ModifiedDietzReturnResult o) {
        return this.moneyWeighted.compareTo(o.getMoneyWeighted());
    }

}
