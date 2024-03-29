/*
 * Copyright (C) 2020 fede
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
package org.fede.calculator.money.series;

import java.math.BigDecimal;

/**
 *
 * @author fede
 */
public class AnnualHistoricalReturn {

    private int year;
    private BigDecimal totalReturn;

    public AnnualHistoricalReturn(int year, BigDecimal totalReturn) {
        this.year = year;
        this.totalReturn = totalReturn;
    }

    public AnnualHistoricalReturn() {
    }
    
    

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public BigDecimal getTotalReturn() {
        return totalReturn;
    }

    public void setTotalReturn(BigDecimal totalReturn) {
        this.totalReturn = totalReturn;
    }

}
