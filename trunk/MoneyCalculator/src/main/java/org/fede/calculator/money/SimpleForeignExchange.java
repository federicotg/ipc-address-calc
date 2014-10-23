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

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import static org.fede.calculator.money.MathConstants.ROUNDING_MODE;
import static org.fede.calculator.money.MathConstants.SCALE;
import org.fede.calculator.money.series.IndexSeries;
import org.fede.calculator.money.series.JSONMoneyAmountSeries;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.SeriesSupport;
import org.fede.calculator.money.series.YearMonth;

/**
 *
 * @author fede
 */
public class SimpleForeignExchange extends SeriesSupport implements ForeignExchange {

    private static final BigDecimal ONE = BigDecimal.ONE.setScale(SCALE, ROUNDING_MODE);

    private final IndexSeries exchangeRatesSeries;

    private final Currency fromCurrency;

    private final Currency targetCurrency;

    public SimpleForeignExchange(IndexSeries exchangeRatesSeries,
            Currency fromCurrency,
            Currency targetCurrency) {
        this.exchangeRatesSeries = exchangeRatesSeries;
        this.fromCurrency = fromCurrency;
        this.targetCurrency = targetCurrency;
    }

    @Override
    public YearMonth getFrom() {
        return this.exchangeRatesSeries.getFrom();
    }

    @Override
    public YearMonth getTo() {
        return this.exchangeRatesSeries.getTo();
    }

    @Override
    public MoneyAmount exchange(MoneyAmount amount, Currency targetCurrency, int year, int month) throws NoSeriesDataFoundException {
        if(amount.getCurrency().equals(targetCurrency)){
            return amount;
        }
        if (this.targetCurrency.equals(targetCurrency)) {
            return amount.exchange(targetCurrency, exchangeRatesSeries.getIndex(year, month));
        }

        if (this.fromCurrency.equals(targetCurrency)) {
            return amount.exchange(targetCurrency, ONE.divide(exchangeRatesSeries.getIndex(year, month), ROUNDING_MODE));
        }

        throw new IllegalArgumentException("Unknown currency.");
    }

    @Override
    public MoneyAmountSeries exchange(MoneyAmountSeries series, Currency targetCurrency) throws NoSeriesDataFoundException {

        YearMonth from = this.exchangeRatesSeries.maximumFrom(series);
        YearMonth to = this.exchangeRatesSeries.minimumTo(series);

        final int fromYear = from.getYear();
        final int fromMonth = from.getMonth();
        final int toYear = to.getYear();
        final int toMonth = to.getMonth();

        final MoneyAmountSeries answer = new JSONMoneyAmountSeries(targetCurrency);

        for (int m = fromMonth; m <= 12; m++) {
            //first year
            answer.putAmount(fromYear, m, this.exchange(series.getAmount(fromYear, m), targetCurrency, fromYear, m));
        }
        for (int y = fromYear + 1; y < toYear; y++) {
            for (int m = 1; m <= 12; m++) {
                answer.putAmount(y, m, this.exchange(series.getAmount(y, m), targetCurrency, y, m));
            }
        }
        for (int m = 1; m <= toMonth; m++) {
            //last year
            answer.putAmount(toYear, m, this.exchange(series.getAmount(toYear, m), targetCurrency, toYear, m));
        }
        return answer;

    }

   /* @Override
    public MoneyAmountSeries exchange(MoneyAmountSeries series, Currency targetCurrency, int referenceYear, int referenceMonth) throws NoSeriesDataFoundException {

        YearMonth from = this.exchangeRatesSeries.maximumFrom(series);
        YearMonth to = this.exchangeRatesSeries.minimumTo(series);

        final int fromYear = from.getYear();
        final int fromMonth = from.getMonth();
        final int toYear = to.getYear();
        final int toMonth = to.getMonth();

        final MoneyAmountSeries answer = new JSONMoneyAmountSeries(targetCurrency);

        for (int m = fromMonth; m <= 12; m++) {
            //first year
            answer.putAmount(fromYear, m, this.exchange(series.getAmount(fromYear, m), targetCurrency, referenceYear, referenceMonth));
        }
        for (int y = fromYear + 1; y < toYear; y++) {
            for (int m = 1; m <= 12; m++) {
                answer.putAmount(y, m, this.exchange(series.getAmount(y, m), targetCurrency, referenceYear, referenceMonth));
            }
        }
        for (int m = 1; m <= toMonth; m++) {
            //last year
            answer.putAmount(toYear, m, this.exchange(series.getAmount(toYear, m), targetCurrency, referenceYear, referenceMonth));
        }
        return answer;

    }*/

    @Override
    public MoneyAmount exchange(MoneyAmount amount, Currency targetCurrency, Date moment) throws NoSeriesDataFoundException {
        Calendar cal = Calendar.getInstance();
        cal.setTime(moment);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        return this.exchange(amount, targetCurrency, year, month + 1);
    }

  /*  @Override
    public MoneyAmountSeries exchange(MoneyAmountSeries series, Currency targetCurrency, Date moment) throws NoSeriesDataFoundException {
        Calendar cal = Calendar.getInstance();
        cal.setTime(moment);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        return this.exchange(series, targetCurrency, year, month + 1);
    }*/

    @Override
    public MoneyAmountSeries exchange(MoneyAmount amount, Currency targetCurrency) throws NoSeriesDataFoundException {

        final int fromYear = this.getFrom().getYear();
        final int fromMonth = this.getFrom().getMonth();
        final int toYear = this.getTo().getYear();
        final int toMonth = this.getTo().getMonth();

        final MoneyAmountSeries answer = new JSONMoneyAmountSeries(targetCurrency);

        for (int m = fromMonth; m <= 12; m++) {
            //first year
            answer.putAmount(fromYear, m, this.exchange(amount, targetCurrency, fromYear, m));
        }
        for (int y = fromYear + 1; y < toYear; y++) {
            for (int m = 1; m <= 12; m++) {
                answer.putAmount(y, m, this.exchange(amount, targetCurrency, y, m));
            }
        }
        for (int m = 1; m <= toMonth; m++) {
            //last year
            answer.putAmount(toYear, m, this.exchange(amount, targetCurrency, toYear, m));
        }
        return answer;
        
    }

}