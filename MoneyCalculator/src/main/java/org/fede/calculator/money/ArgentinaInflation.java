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

import java.util.Currency;
import org.fede.calculator.money.series.ArgentinaCompoundCPISeries;

/**
 *
 * @author fede
 */
public class ArgentinaInflation implements Inflation {

    private final Inflation basicInflation = new CPIInflation(new ArgentinaCompoundCPISeries(), Currency.getInstance("ARS"));

    @Override
    public MoneyAmount adjust(MoneyAmount amount, int fromYear, int toYear) throws NoSeriesDataFoundException {
        return adjust(amount, fromYear, 12, toYear, 12);
    }

    @Override
    public MoneyAmount adjust(MoneyAmount amount, int fromYear, int fromMonth, int toYear, int toMonth) throws NoSeriesDataFoundException {
        amount.assertCurrency(Currency.getInstance("ARS"));

        MoneyAmount uncorrectedValue = this.basicInflation.adjust(amount, fromYear, fromMonth, toYear, toMonth);

        return new MoneyAmount(ArgCurrency.convertTo(uncorrectedValue.getAmount(), fromYear, fromMonth, toYear, toMonth), Currency.getInstance("ARS"));

    }

    @Override
    public int getFromYear() {
        return this.basicInflation.getFromYear();
    }

    @Override
    public int getToYear() {
        return this.basicInflation.getToYear();
    }

    @Override
    public Currency getCurrency() {
        return Currency.getInstance("ARS");
    }

    @Override
    public int getFromMonth() {
        return this.basicInflation.getFromMonth();
    }

    @Override
    public int getToMonth() {
        return this.basicInflation.getToMonth();
    }

}
