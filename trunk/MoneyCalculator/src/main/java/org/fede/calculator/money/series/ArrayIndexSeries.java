/*
 * Copyright (C) 2014 fede
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
import org.fede.calculator.money.NoIndexDataFoundException;

/**
 *
 * @author fede
 */
public class ArrayIndexSeries extends IndexSeriesSupport {

    private final int firstYear;
    private final BigDecimal[] table;

    protected ArrayIndexSeries(int firstYear, BigDecimal[] table) {
        this.firstYear = firstYear;
        this.table = table;
    }

    @Override
    public final BigDecimal getIndex(int year, int month) throws NoIndexDataFoundException {

        final int index = (year - this.firstYear) * 12 + (month - 1);
        if (index < 0 || index >= this.table.length || this.table[index] == null) {
            throw new NoIndexDataFoundException("No data for specified year and month.");
        }

        return this.table[index];
    }

    @Override
    public final int getFromYear() {
        return this.firstYear;
    }

    @Override
    public final int getToYear() {
        return this.firstYear + (table.length / 12);
    }

}
