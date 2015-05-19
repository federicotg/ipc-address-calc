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

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import org.fede.calculator.money.ForeignExchange;
import static org.fede.calculator.money.ForeignExchange.USD_ARS;
import static org.fede.calculator.money.Inflation.USD_INFLATION;
import org.fede.calculator.money.NoSeriesDataFoundException;
import org.fede.calculator.money.SimpleAggregation;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.web.dto.CanvasJSDatapointDTO;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 *
 * @author fede
 */
@Component("realEURDatapointAssembler") @Lazy
public class RealEURCanvasJSDatapointAssembler implements CanvasJSDatapointAssembler {

    @Override
    public List<CanvasJSDatapointDTO> getDatapoints(int months, MoneyAmountSeries sourceSeries, int year, int month) throws NoSeriesDataFoundException {

        final List<CanvasJSDatapointDTO> datapoints = new ArrayList<>();
        MoneyAmountSeries exchanged = sourceSeries;
        if (sourceSeries.getCurrency().equals(Currency.getInstance("ARS"))) {
            exchanged = USD_ARS.exchange(sourceSeries, Currency.getInstance("USD"));
        }
        MoneyAmountSeries series = 
                USD_INFLATION.adjust(exchanged, year, month);
        
        
        MoneyAmountSeries euroSeries = ForeignExchange.USD_EUR.exchange(series, Currency.getInstance("EUR"));
        new SimpleAggregation(months).average(euroSeries)
                .forEach(new CanvasJSMoneyAmountProcessor(datapoints));
        return datapoints;
    }
    
    @Override
    public List<CanvasJSDatapointDTO> getDatapoints(int months, MoneyAmountSeries sourceSeries) throws NoSeriesDataFoundException {
        return this.getDatapoints(months, sourceSeries, 1999, 11);
    }

}
