/*
 * Copyright (C) 2016 Federico Tello Gentile <federicotg@gmail.com>
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

import java.util.Currency;
import java.util.Date;
import static org.fede.calculator.money.ForeignExchanges.getForeignExchange;
import org.fede.calculator.money.NoSeriesDataFoundException;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
public class CurrencyInvestment extends Investment {

    @Override
    public MoneyAmountSeries getMoneyAmountSeries() throws NoSeriesDataFoundException {
        final Currency srcCurrency = Currency.getInstance(this.getInvestment().getCurrency());
        final Currency dstCurrency = Currency.getInstance(this.getIn().getCurrency());

        YearMonth start = new YearMonth(this.getInvestmentDate());
        final YearMonth end = new YearMonth(this.getOut() != null ? this.getOut().getDate() : new Date());
        
        SortedMapMoneyAmountSeries series = new SortedMapMoneyAmountSeries(this.getIn().getCurrency());
        while (start.compareTo(end) <= 0) {
            series.putAmount(start.getYear(), start.getMonth(),
                    getForeignExchange(srcCurrency, dstCurrency)
                    .exchange(this.getInvestment().getMoneyAmount(), dstCurrency, start.getYear(), start.getMonth()));
            start = start.next();
        }
        return series;
    }

    /*    private BigDecimal dailyInterestRate(YearMonth ym) throws NoSeriesDataFoundException {

        MoneyAmount in = this.getInvestedAmount();
        MoneyAmount out = null;
        Date outDate = null;
        if (this.getOut() != null) {
            out = this.getOut().getMoneyAmount();
            outDate = this.getOut().getDate();
        } else {
            out = this.getInvestmentValue();
            outDate = new Date();
        }

        out = ForeignExchanges.getForeignExchange(out.getCurrency(), in.getCurrency()).exchange(out, in.getCurrency(), outDate);
        Calendar outCal = Calendar.getInstance();
        Calendar inCal = Calendar.getInstance();
        outCal.setTime(outDate);
        inCal.setTime(this.getInvestmentDate());

        final int days = daysBetween(inCal, outCal);
        return out.getAmount().subtract(in.getAmount()).divide(new BigDecimal(days), MathContext.DECIMAL128);

    }

    private static int daysBetween(Calendar day1, Calendar day2) {
        Calendar dayOne = (Calendar) day1.clone(),
                dayTwo = (Calendar) day2.clone();

        if (dayOne.get(Calendar.YEAR) == dayTwo.get(Calendar.YEAR)) {
            return Math.abs(dayOne.get(Calendar.DAY_OF_YEAR) - dayTwo.get(Calendar.DAY_OF_YEAR));
        } else {
            if (dayTwo.get(Calendar.YEAR) > dayOne.get(Calendar.YEAR)) {
                //swap them
                Calendar temp = dayOne;
                dayOne = dayTwo;
                dayTwo = temp;
            }
            int extraDays = 0;

            int dayOneOriginalYearDays = dayOne.get(Calendar.DAY_OF_YEAR);

            while (dayOne.get(Calendar.YEAR) > dayTwo.get(Calendar.YEAR)) {
                dayOne.add(Calendar.YEAR, -1);
                // getActualMaximum() important for leap years
                extraDays += dayOne.getActualMaximum(Calendar.DAY_OF_YEAR);
            }

            return extraDays - dayTwo.get(Calendar.DAY_OF_YEAR) + dayOneOriginalYearDays;
        }
    }*/
}
