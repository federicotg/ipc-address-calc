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
import org.fede.calculator.money.series.DollarCPISeries;
import org.fede.calculator.money.series.MoneyAmountSeries;

/**
 *
 * @author fede
 */
public interface Inflation {
    
    public static final Inflation USD_INFLATION = new CPIInflation(new DollarCPISeries(), Currency.getInstance("USD"));
    public static final Inflation ARS_INFLATION = new ArgentinaInflation();

    /**
     * Ajusta por inflación un monto.
     * @param amount el monto a ajustar. Es el valor nominal.
     * @param fromYear el año en el que está basado el monto
     * @param fromMonth el mes del año en el que está basado el monto.
     * @param toYear el año al que se quiere llevar el monto
     * @param toMonth el mes del año al que se quiere llevar el monto.
     * @return el monto ajustado por inflación. Queda expresado en valores de toYear y toMonth
     * @throws NoSeriesDataFoundException 
     */
    MoneyAmount adjust(MoneyAmount amount, int fromYear, int fromMonth, int toYear, int toMonth) throws NoSeriesDataFoundException;
    
    /**
     * Ajusta por inflación todos los valores de la serie
     * @param series la serie a ajustar.
     * @param referenceYear el año al que se quiere llevar los valores de la serie.
     * @param referenceMonth el mes.
     * @return una serie de valores expresados en valores de referenceYear y referenceMonth
     * @throws NoSeriesDataFoundException 
     */
    MoneyAmountSeries adjust(MoneyAmountSeries series, int referenceYear, int referenceMonth) throws NoSeriesDataFoundException;
    
    /**
     * Calcula el valor ajustado por inflación del monto especificado en cada mes que se pueda.
     * @param amount el monto a ajustar
     * @param referenceYear el año del monto
     * @param referenceMonth el mes dle monto
     * @return el valor especificado expresado en valores ajustados para cada mes de la serie.
     * @throws NoSeriesDataFoundException 
     */
    public MoneyAmountSeries adjust(MoneyAmount amount, int referenceYear, int referenceMonth) throws NoSeriesDataFoundException;
    
    int getFromYear();
    int getToYear();
    int getFromMonth();
    int getToMonth();
    Currency getCurrency();
    
}
