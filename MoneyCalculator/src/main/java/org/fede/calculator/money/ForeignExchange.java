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

/**
 *
 * @author fede
 */
public interface ForeignExchange {

    public static final ForeignExchange INSTANCE = new SimpleForeignExchange();

    MoneyAmount exchangeAmountIntoCurrency(MoneyAmount amount, Currency currency, int year, int month) throws NoSeriesDataFoundException;
    int getFromYear(Currency from, Currency to);
    int getToYear(Currency from, Currency to);
}
