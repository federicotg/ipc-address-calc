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

import java.util.Currency;
import java.util.Date;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.NoSeriesDataFoundException;

/**
 *
 * @author fede
 */
public interface MoneyAmountSeries extends Series {

    MoneyAmount getAmount(Date day) throws NoSeriesDataFoundException;

    MoneyAmount getAmount(int year, int month) throws NoSeriesDataFoundException;

    MoneyAmount getAmount(int year) throws NoSeriesDataFoundException;
    
    void putAmount(int year, int month, MoneyAmount amount);
    
    Currency getCurrency();
    
}
