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
import java.awt.Stroke;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.fede.calculator.money.ConsoleReports;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.data.time.TimeSeriesCollection;

/**
 *
 * @author fede
 */
public class TimeSeriesChart {

    private final Font font;
    private final Stroke stroke;

    public TimeSeriesChart() {
        this.font = new Font("SansSerif", Font.PLAIN, 16);
        this.stroke = new BasicStroke(3.0f);
    }

    public void create(String chartName, List<MoneyAmountSeries> series, String filename) throws IOException {
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
        renderer.setDefaultStroke(this.stroke);
        chart.getLegend().setItemFont(this.font);

        ChartUtils.saveChartAsPNG(
                new File(ConsoleReports.CHARTS_PREFIX + filename),
                chart,
                1600,
                900,
                null,
                true,
                9);

    }
}
