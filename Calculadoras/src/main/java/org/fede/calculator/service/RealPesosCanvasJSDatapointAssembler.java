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
import static org.fede.calculator.money.Inflation.ARS_INFLATION;
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
@Component("realPesosDatapointAssembler") @Lazy
public class RealPesosCanvasJSDatapointAssembler implements CanvasJSDatapointAssembler {

    @Override
    public List<CanvasJSDatapointDTO> getDatapoints(int months, MoneyAmountSeries originalSeries, int year, int month) throws NoSeriesDataFoundException {

        MoneyAmountSeries sourceSeries = originalSeries.exchangeInto(Currency.getInstance("ARS"));

        final List<CanvasJSDatapointDTO> datapoints = new ArrayList<>();
        new SimpleAggregation(months).average(ARS_INFLATION.adjust(sourceSeries, year, month))
                .forEach(new CanvasJSMoneyAmountProcessor(datapoints));
        return datapoints;
    }

    @Override
    public List<CanvasJSDatapointDTO> getDatapoints(int months, MoneyAmountSeries sourceSeries) throws NoSeriesDataFoundException {
        return this.getDatapoints(months, sourceSeries, 1999, 11);
    }

}
