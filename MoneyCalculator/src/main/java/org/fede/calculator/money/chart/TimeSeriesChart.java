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

import java.awt.BasicStroke;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.fede.calculator.money.ConsoleReports;
import org.fede.calculator.money.Inflation;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
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
            var collection = new TimeSeriesCollection();
            series.stream().map(MoneyAmountSeries::asTimeSeries).forEach(collection::addSeries);

            JFreeChart chart = ChartFactory.createTimeSeriesChart(
                    chartName,
                    "Date",
                    "USD",
                    collection);
            var xyPlot = chart.getXYPlot();
            xyPlot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
            var renderer = xyPlot.getRenderer();
            ((AbstractRenderer) renderer).setAutoPopulateSeriesStroke(false);
            renderer.setDefaultStroke(new BasicStroke(3.0f));
            chart.getLegend().setItemFont(new Font("SansSerif", Font.PLAIN, 16));
            
            ChartUtils.saveChartAsPNG(
                    new File(ConsoleReports.CHARTS_PREFIX + filename),
                    chart,
                    1600,
                    900,
                    null,
                    true,
                    9);
        } catch (IOException ex) {
            LOGGER.error("Error generating time series chart.", ex);
        }
    }
}
