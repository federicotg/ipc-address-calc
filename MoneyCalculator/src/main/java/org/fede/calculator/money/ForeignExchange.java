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
import java.util.Date;
import org.fede.calculator.money.series.JSONIndexSeries;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.Series;

/**
 *
 * @author fede
 */
public interface ForeignExchange extends Series {

    public static final ForeignExchange USD_ARS = new SimpleForeignExchange(
            JSONIndexSeries.readSeries("peso-dolar-libre.json"),
            Currency.getInstance("USD"),
            Currency.getInstance("ARS"));
    
    public static final ForeignExchange USD_XAU = new SimpleForeignExchange(
            JSONIndexSeries.readSeries("gold.json"),
            Currency.getInstance("XAU"),
            Currency.getInstance("USD"));

    MoneyAmount exchange(MoneyAmount amount, Currency targetCurrency, int referenceYear, int referenceMonth) throws NoSeriesDataFoundException;
    MoneyAmount exchange(MoneyAmount amount, Currency targetCurrency, Date moment) throws NoSeriesDataFoundException;

    /**
     * Convierte cada money amount de la serie a la moneda especificada según el tipo de cambio de la fecha de cada money amount de la serie.
     * @param series
     * @param targetCurrency
     * @return
     * @throws NoSeriesDataFoundException 
     */
    MoneyAmountSeries exchange(MoneyAmountSeries series, Currency targetCurrency) throws NoSeriesDataFoundException;
    /**
     * Convierte cada money amount de la serie a la moneda especificada según el tipo de cambio de la fecha especificada.
     * @param series
     * @param targetCurrency
     * @param referenceYear
     * @param referenceMonth
     * @return
     * @throws NoSeriesDataFoundException 
     */
    MoneyAmountSeries exchange(MoneyAmountSeries series, Currency targetCurrency, int referenceYear, int referenceMonth) throws NoSeriesDataFoundException;
    
    MoneyAmountSeries exchange(MoneyAmountSeries series, Currency targetCurrency, Date moment) throws NoSeriesDataFoundException;
}
