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
import java.util.Calendar;
import java.util.Date;
import org.fede.calculator.money.NoIndexDataFoundException;

/**
 *
 * @author fede
 */
public abstract class IndexSeriesSupport implements IndexSeries {

     private static final BigDecimal MONTHS_IN_A_YEAR = new BigDecimal("12.00000");
    
    @Override
    public final BigDecimal getIndex(Date day) throws NoIndexDataFoundException {
        Calendar cal = Calendar.getInstance();
        cal.setTime(day);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        return this.getIndex(year, month);
    }
    
    @Override
    public BigDecimal getIndex(int year) throws NoIndexDataFoundException {
        BigDecimal sum = BigDecimal.ZERO;

        for (int i = 1; i <= 12; i++) {
            sum = sum.add(this.getIndex(year, i));
        }
        return sum.setScale(5).divide(MONTHS_IN_A_YEAR);
    }

}
