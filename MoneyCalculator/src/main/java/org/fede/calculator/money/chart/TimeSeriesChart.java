/*
 * Copyright (C) 2024 fede
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
package org.fede.calculator.money.chart;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.fede.calculator.money.Inflation;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author fede
 */
public class TimeSeriesChart {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeSeriesChart.class);

    public void create(String chartName, List<MoneyAmountSeries> series, String filename) {
        try {
            final var to = Inflation.USD_INFLATION.getTo();
            var collection = new TimeSeriesCollection();
            for (var s : series) {
                final TimeSeries ts = new TimeSeries(s.getName());

                s.forEach((ym, ma) -> {
                    if (ym.compareTo(to) <= 0) {
                        ts.add(new Day(ym.asToDate()), ma.amount());
                    }
                });
                collection.addSeries(ts);
            }

            JFreeChart chart = ChartFactory.createTimeSeriesChart(
                    chartName,
                    "Date",
                    "USD",
                    collection);
            chart.setAntiAlias(true);
            chart.setTextAntiAlias(true);
            ChartUtils.saveChartAsPNG(
                    new File(filename),
                    chart,
                    1200,
                    900);
        } catch (IOException ex) {
            LOGGER.error("Error generating time series chart.", ex);
        }
    }
}
