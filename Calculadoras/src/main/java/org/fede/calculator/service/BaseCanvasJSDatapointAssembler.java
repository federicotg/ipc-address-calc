/*
 * Copyright (C) 2015 fede
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
package org.fede.calculator.service;

import java.util.Currency;
import static org.fede.calculator.money.ForeignExchange.USD_ARS;
import org.fede.calculator.money.NoSeriesDataFoundException;
import org.fede.calculator.money.series.MoneyAmountSeries;

/**
 *
 * @author fede
 */
public class BaseCanvasJSDatapointAssembler {

    public static MoneyAmountSeries dollarToPesosIfNeeded(MoneyAmountSeries originalSeries) throws NoSeriesDataFoundException {
        if (originalSeries.getCurrency().equals(Currency.getInstance("USD"))) {
            return USD_ARS.exchange(originalSeries, Currency.getInstance("ARS"));
        }
        return originalSeries;
    }
}
