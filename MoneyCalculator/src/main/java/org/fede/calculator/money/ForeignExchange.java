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

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.fede.calculator.money.series.IndexSeriesSupport;
import org.fede.calculator.money.series.JSONIndexSeries;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.Series;
import org.fede.calculator.money.series.YearMonth;

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

    public static final ForeignExchange USD_EUR = new SimpleForeignExchange(
            JSONIndexSeries.readSeries("euro-dolar.json"),
            Currency.getInstance("USD"),
            Currency.getInstance("EUR"));
    
    
    public static final ForeignExchange NO_FX = new SimpleForeignExchange(new IndexSeriesSupport() {
        @Override
        public YearMonth getFrom() {
            return new YearMonth(1, 1);
        }

        @Override
        public YearMonth getTo() {
            return new YearMonth(5000, 12);
        }

        @Override
        public BigDecimal getIndex(int year, int month) throws NoSeriesDataFoundException {
            return BigDecimal.ONE;
        }

        @Override
        public BigDecimal predictValue(int year, int month) throws NoSeriesDataFoundException {
            return BigDecimal.ONE;
        }
    }, Currency.getInstance("USD"), Currency.getInstance("USD"));
    
    
    MoneyAmount exchange(MoneyAmount amount, Currency targetCurrency, int referenceYear, int referenceMonth) throws NoSeriesDataFoundException;

    MoneyAmount exchange(MoneyAmount amount, Currency targetCurrency, Date moment) throws NoSeriesDataFoundException;

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
    MoneyAmountSeries exchange(MoneyAmountSeries series, Currency targetCurrency) throws NoSeriesDataFoundException;

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
    MoneyAmountSeries exchange(MoneyAmount amount, Currency targetCurrency) throws NoSeriesDataFoundException;

    /**
     * Convierte cada money amount de la serie a la moneda especificada según el
     * tipo de cambio de la fecha especificada. Útil para convertir toda una
     * serie de valores a un tipo de cambio fijo de un momento dado.
     *
     * @param series
     * @param targetCurrency
     * @param referenceYear
     * @param referenceMonth
     * @return
     * @throws NoSeriesDataFoundException
     */
   // MoneyAmountSeries exchange(MoneyAmountSeries series, Currency targetCurrency, int referenceYear, int referenceMonth) throws NoSeriesDataFoundException;

    //MoneyAmountSeries exchange(MoneyAmountSeries series, Currency targetCurrency, Date moment) throws NoSeriesDataFoundException;
}
