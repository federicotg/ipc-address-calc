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

import org.fede.calculator.money.series.IndexSeries;
import java.math.BigDecimal;
import org.fede.calculator.money.series.YearMonth;

/**
 *
 * @author fede
 */
public final class CPIInflation extends BaseInflation {

    private final IndexSeries series;
    private final Currency currency;

    public CPIInflation(IndexSeries cpiSeries, Currency currency) {
        this.series = cpiSeries;
        this.currency = currency;
    }

    @Override
    public final MoneyAmount adjust(MoneyAmount amount, int fromYear, int fromMonth, int toYear, int toMonth) {
        amount.assertCurrency(this.currency);
        BigDecimal divisor = this.series.getIndex(fromYear, fromMonth);
        BigDecimal factor = this.series.getIndex(toYear, toMonth);
        return amount.adjust(divisor, factor);
    }

    @Override
    public Currency getCurrency() {
        return this.currency;
    }

    @Override
    public YearMonth getFrom() {
        return this.series.getFrom();
    }

    @Override
    public YearMonth getTo() {
        return this.series.getTo();
    }

}
