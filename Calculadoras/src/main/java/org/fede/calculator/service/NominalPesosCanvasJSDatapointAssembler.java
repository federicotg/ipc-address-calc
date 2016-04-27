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

/**
 *
 * @author fede
 */
//@Component("nominalPesosDatapointAssembler") @Lazy
public class NominalPesosCanvasJSDatapointAssembler  {

    /*
    @Override
    public List<CanvasJSDatapointDTO> getDatapoints(int months, MoneyAmountSeries originalSeries, int year, int month) throws NoSeriesDataFoundException {
        return this.getDatapoints(months, originalSeries);
    }

    @Override
    public List<CanvasJSDatapointDTO> getDatapoints(int months, MoneyAmountSeries originalSeries) throws NoSeriesDataFoundException {
        MoneyAmountSeries sourceSeries = originalSeries.exchangeInto("ARS");
        final List<CanvasJSDatapointDTO> datapoints = new ArrayList<>();
        new SimpleAggregation(months).average(sourceSeries)
                .forEach(new CanvasJSMoneyAmountProcessor(datapoints));
        return datapoints;
    }*/

}
