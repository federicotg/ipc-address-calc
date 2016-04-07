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
package org.fede.calculator.money;

import java.util.Currency;
import java.util.Date;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.SeriesSupport;
import org.fede.calculator.money.series.YearMonth;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
public class CompoundForeignExchange extends SeriesSupport implements ForeignExchange{
    private ForeignExchange first;
    private ForeignExchange second;
    
    public CompoundForeignExchange(ForeignExchange first, ForeignExchange second){
        this.first = first;
        this.second = second;
    }

    @Override
    public YearMonth getFrom() {
        if(this.first.getFrom().compareTo(this.second.getFrom()) >= 0){
            return this.first.getFrom();
        }
        return this.second.getFrom();
    }

    @Override
    public YearMonth getTo() {
        if(this.first.getTo().compareTo(this.second.getTo()) <= 0){
            return this.first.getTo();
        }
        return this.second.getTo();
    }

    @Override
    public MoneyAmount exchange(MoneyAmount amount, Currency targetCurrency, int referenceYear, int referenceMonth) throws NoSeriesDataFoundException {
        return this.second.exchange(this.first.exchange(amount, Currency.getInstance("USD"), referenceYear, referenceMonth), targetCurrency, referenceYear, referenceMonth);
    }

    @Override
    public MoneyAmount exchange(MoneyAmount amount, Currency targetCurrency, Date moment) throws NoSeriesDataFoundException {
        return this.second.exchange(this.first.exchange(amount, Currency.getInstance("USD"), moment), targetCurrency, moment);
    }

    @Override
    public MoneyAmountSeries exchange(MoneyAmountSeries series, Currency targetCurrency) throws NoSeriesDataFoundException {
        return this.second.exchange(this.first.exchange(series, Currency.getInstance("USD")), targetCurrency);
    }

    @Override
    public MoneyAmountSeries exchange(MoneyAmount amount, Currency targetCurrency) throws NoSeriesDataFoundException {
        return this.second.exchange(this.first.exchange(amount, Currency.getInstance("USD")), targetCurrency);
    }
    
    
}