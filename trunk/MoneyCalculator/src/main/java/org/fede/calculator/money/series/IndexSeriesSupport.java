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
import org.fede.calculator.money.NoSeriesDataFoundException;

/**
 *
 * @author fede
 */
public abstract class IndexSeriesSupport implements IndexSeries{

    @Override
    public final BigDecimal getIndex(Date day) throws NoSeriesDataFoundException {
        Calendar cal = Calendar.getInstance();
        cal.setTime(day);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        return this.getIndex(year, month);
    }

   /* @Override
    public IndexSeries adjust(Inflation inflation, int inflateToYear, int inflateToMonth) throws NoIndexDataFoundException {
        int fromYear = Math.max(this.getFromYear(), inflation.getFromYear());
        int fromMonth = Math.max(this.getFromMonth(), inflation.getFromMonth());

        int toYear = Math.min(this.getToYear(), inflation.getToYear());
        int toMonth = Math.min(this.getToMonth(), inflation.getToMonth());
        List<JSONDataPoint> dataPoints = new ArrayList<>((toYear - fromYear) * 12);
        for (int y = fromYear; y < toYear; y++) {
            for (int m = 1; m <= 12; m++) {
                JSONDataPoint dp = new JSONDataPoint(y,m);
                MoneyAmount moneyAmount = new MoneyAmount(this.getIndex(y, m),inflation.getCurrency());
                dp.setValue(inflation.adjust(moneyAmount, y, m, inflateToYear, inflateToMonth).getAmount());
            }
        }
        for (int m = 1; m <= toMonth; m++) {
            //last year
        }
        return new JSONIndexSeries(dataPoints);
    }*/

}
