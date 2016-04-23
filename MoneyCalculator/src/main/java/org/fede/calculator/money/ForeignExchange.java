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

import java.util.Date;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.Series;


/**
 *
 * @author fede
 */
public interface ForeignExchange extends Series {

    
    
    MoneyAmount exchange(MoneyAmount amount, String targetCurrency, int referenceYear, int referenceMonth) throws NoSeriesDataFoundException;

    MoneyAmount exchange(MoneyAmount amount, String targetCurrency, Date moment) throws NoSeriesDataFoundException;

    /**
     * Convierte cada money amount de la serie a la moneda especificada según el
     * tipo de cambio de la fecha de cada money amount de la serie. Útil para
     * convertir una serie de valores a su equivalente en otra moneda en el
     * momento que ese valor está asignado.
     *
     * @param series
     * @param targetCurrency
     * @return
     * @throws NoSeriesDataFoundException
     */
    MoneyAmountSeries exchange(MoneyAmountSeries series, String targetCurrency) throws NoSeriesDataFoundException;

    /**
     * Convierte el money amount especificado en la serie de valores donde en
     * valor en cada fecha es la conversión según el tipo de cambio de la fecha.
     * Útil para saber la evolución de un valor nominal según el tipo de cambio
     * en diferentes momentos.
     *
     * @param amount
     * @param targetCurrency
     * @return
     * @throws NoSeriesDataFoundException
     */
    MoneyAmountSeries exchange(MoneyAmount amount, String targetCurrency) throws NoSeriesDataFoundException;
    
    String getTargetCurrency();
    String getSourceCurrency();

}
