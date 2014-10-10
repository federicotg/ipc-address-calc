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
        YearMonth minTo = this.minimumTo(series);
        
        final int fromYear = maxFrom.getYear();//Math.max(this.getFromYear(), series.getFromYear());
        final int fromMonth = maxFrom.getMonth(); //Math.max(this.getFromMonth(), series.getFromMonth());

        final int toYear = minTo.getYear();//Math.min(this.getToYear(), series.getToYear());
        final int toMonth = minTo.getMonth();//Math.min(this.getToMonth(), series.getToMonth());
       
        
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
    public MoneyAmountSeries adjust(MoneyAmount amount, int referenceYear, int referenceMonth) throws NoSeriesDataFoundException {
        
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

}