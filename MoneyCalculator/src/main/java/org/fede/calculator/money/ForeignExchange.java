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

import java.time.LocalDate;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.Series;
import java.time.YearMonth;

/**
 *
 * @author fede
 */
public interface ForeignExchange extends Series {

    MoneyAmount exchange(MoneyAmount amount, Currency targetCurrency, YearMonth ym);

    MoneyAmount exchange(MoneyAmount amount, Currency targetCurrency, LocalDate moment);

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
    MoneyAmountSeries exchange(MoneyAmountSeries series, Currency targetCurrency);

    Currency getTargetCurrency();

    Currency getSourceCurrency();

}
