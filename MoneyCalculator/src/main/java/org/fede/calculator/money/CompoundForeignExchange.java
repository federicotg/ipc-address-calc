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

import java.time.LocalDate;
import java.util.Objects;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.SeriesSupport;
import java.time.YearMonth;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
public class CompoundForeignExchange extends SeriesSupport implements ForeignExchange {

    private final ForeignExchange first;
    private final ForeignExchange second;

    public CompoundForeignExchange(ForeignExchange first, ForeignExchange second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public YearMonth getFrom() {
        if (this.first.getFrom().compareTo(this.second.getFrom()) >= 0) {
            return this.first.getFrom();
        }
        return this.second.getFrom();
    }

    @Override
    public YearMonth getTo() {
        if (this.first.getTo().compareTo(this.second.getTo()) <= 0) {
            return this.first.getTo();
        }
        return this.second.getTo();
    }

    @Override
    public MoneyAmount exchange(MoneyAmount amount, Currency targetCurrency, YearMonth reference) {
        return this.second.exchange(
                this.first.exchange(amount, this.getFirstTargetCurrency(amount.currency()), reference), targetCurrency, reference);
    }

    @Override
    public MoneyAmount exchange(MoneyAmount amount, Currency targetCurrency, LocalDate moment) {
        return this.second.exchange(this.first.exchange(amount, this.getFirstTargetCurrency(amount.currency()), moment), targetCurrency, moment);
    }

    private Currency getFirstTargetCurrency(Currency currency) {
        return currency == this.first.getTargetCurrency()
                ? this.first.getSourceCurrency()
                : this.first.getTargetCurrency();
    }

    @Override
    public MoneyAmountSeries exchange(MoneyAmountSeries series, Currency targetCurrency) {
        return this.second.exchange(this.first.exchange(series, this.getFirstTargetCurrency(series.getCurrency())), targetCurrency);
    }

    @Override
    public MoneyAmountSeries exchange(MoneyAmount amount, Currency targetCurrency) {
        return this.second.exchange(this.first.exchange(amount, this.getFirstTargetCurrency(amount.currency())), targetCurrency);
    }

    @Override
    public Currency getTargetCurrency() {
        return this.second.getTargetCurrency();
    }

    @Override
    public Currency getSourceCurrency() {
        return this.first.getSourceCurrency();
    }

}
