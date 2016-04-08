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
import org.fede.calculator.money.MathConstants;
import org.fede.calculator.money.NoSeriesDataFoundException;

/**
 *
 * @author fede
 */
public class LinearFutureValue implements FutureValue, MathConstants {

    @Override
    public BigDecimal predictValue(IndexSeries series, int year, int month) throws NoSeriesDataFoundException {
        final int yearsBack = 1;
        YearMonth end = series.getTo();
        BigDecimal sum = BigDecimal.ZERO;
        for (YearMonth ym = new YearMonth(end.getYear() - yearsBack, end.getMonth()).next(); ym.compareTo(end) <= 0; ym = ym.next()) {
            sum = sum.add(
                    series.getIndex(ym.getYear(), ym.getMonth()).divide(
                            series.getIndex(ym.getYear() - 1, ym.getMonth()), CONTEXT)
            );
        }
        BigDecimal avgAnnualChange = sum.divide(new BigDecimal(yearsBack * 12), CONTEXT);
        
        int lastYear = end.getYear();
        if(month > end.getMonth()){
            lastYear--;
        }
        BigDecimal lastKnownValueForRequestedMonth = series.getIndex(lastYear, month);
        
        return lastKnownValueForRequestedMonth.multiply(avgAnnualChange);
    }

}
