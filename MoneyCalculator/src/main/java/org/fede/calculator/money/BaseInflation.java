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
package org.fede.calculator.money;

import java.util.Calendar;
import java.util.Date;
import org.fede.calculator.money.series.JSONMoneyAmountSeries;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.SeriesSupport;
import org.fede.calculator.money.series.YearMonth;

/**
 *
 * @author fede
 */
abstract class BaseInflation extends SeriesSupport implements Inflation {

    @Override
    public final MoneyAmountSeries adjust(MoneyAmountSeries series, int referenceYear, int referenceMonth) throws NoSeriesDataFoundException {
        YearMonth maxFrom = this.maximumFrom(series);
        YearMonth minTo = series.getTo();
        
        final int fromYear = maxFrom.getYear();
        final int fromMonth = maxFrom.getMonth();

        final int toYear = minTo.getYear();
        final int toMonth = minTo.getMonth();

        final MoneyAmountSeries answer = new JSONMoneyAmountSeries(this.getCurrency());

        for (int m = fromMonth; m <= 12; m++) {
            //first year
            answer.putAmount(fromYear, m, this.adjust(series.getAmount(fromYear, m), fromYear, m, referenceYear, referenceMonth));
        }
        for (int y = fromYear + 1; y < toYear; y++) {
            for (int m = 1; m <= 12; m++) {
                answer.putAmount(y, m, this.adjust(series.getAmount(y, m), y, m, referenceYear, referenceMonth));
            }
        }
        for (int m = 1; m <= toMonth; m++) {
            //last year
            answer.putAmount(toYear, m, this.adjust(series.getAmount(toYear, m), toYear, m, referenceYear, referenceMonth));
        }
        return answer;

    }

    @Override
    public final MoneyAmountSeries adjust(MoneyAmount amount, int referenceYear, int referenceMonth) throws NoSeriesDataFoundException {

        final int fromYear = this.getFrom().getYear();
        final int fromMonth = this.getFrom().getMonth();

        final int toYear = this.getTo().getYear();
        final int toMonth = this.getTo().getMonth();

        MoneyAmountSeries answer = new JSONMoneyAmountSeries(this.getCurrency());

        for (int m = fromMonth; m <= 12; m++) {
            //first year
            answer.putAmount(fromYear, m, this.adjust(amount, referenceYear, referenceMonth, fromYear, m));
        }
        for (int y = fromYear + 1; y < toYear; y++) {
            for (int m = 1; m <= 12; m++) {
                answer.putAmount(y, m, this.adjust(amount, referenceYear, referenceMonth, y, m));
            }
        }
        for (int m = 1; m <= toMonth; m++) {
            //last year
            answer.putAmount(toYear, m, this.adjust(amount, referenceYear, referenceMonth, toYear, m));
        }
        return answer;

    }

    @Override
    public final MoneyAmountSeries adjust(MoneyAmountSeries series, Date moment) throws NoSeriesDataFoundException {
        Calendar cal = Calendar.getInstance();
        cal.setTime(moment);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        return this.adjust(series, year, month + 1);
    }

    @Override
    public final MoneyAmount adjust(MoneyAmount amount, Date from, Date to) throws NoSeriesDataFoundException {
        Calendar cal = Calendar.getInstance();
        cal.setTime(from);
        int yearFrom = cal.get(Calendar.YEAR);
        int monthFrom = cal.get(Calendar.MONTH);
        cal.setTime(to);
        int yearTo = cal.get(Calendar.YEAR);
        int monthTo = cal.get(Calendar.MONTH);
        return this.adjust(amount, yearFrom, monthFrom + 1, yearTo, monthTo + 1);
    }

}
