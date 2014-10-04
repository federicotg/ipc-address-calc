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
import org.fede.calculator.money.series.CachedSeries;
import org.fede.calculator.money.series.DollarCPISeries;

/**
 *
 * @author fede
 */
public interface Inflation {
    
    public static final Inflation USD_INFLATION = new CPIInflation(new CachedSeries(new DollarCPISeries()), Currency.getInstance("USD"));
    public static final Inflation ARS_INFLATION = new ArgentinaInflation();//new CPIInflation(new ArgentinaCompoundCPISeries(), Currency.getInstance("ARS"));

    MoneyAmount adjust(MoneyAmount amount, int fromYear, int toYear) throws NoIndexDataFoundException;
    MoneyAmount adjust(MoneyAmount amount, int fromYear, int fromMonth, int toYear, int toMonth) throws NoIndexDataFoundException;
    
    int getFromYear();
    int getToYear();
    Currency getCurrency();
    
}
