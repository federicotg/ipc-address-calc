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
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import org.fede.calculator.money.ConsoleReports;
import org.fede.calculator.money.series.SeriesReader;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author fede
 */
public class ScatterXYChart {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScatterXYChart.class);

    private final Font font;
    private final Stroke stroke;

    public ScatterXYChart() {
        this.font = new Font("SansSerif", Font.PLAIN, 18);
        this.stroke = new BasicStroke(3.0f);
    }

    public void create(
            String chartName,
            List<XYSeries> series,
            //List<BigDecimal> xSeries,
            //List<BigDecimal> ySeries,
            String xLabel,
            String yLabel,
            String filename) {
        try {

            //XYSeries series = new XYSeries(chartName);
            //for (var i = 0; i < xSeries.size(); i++) {
            //    series.add(xSeries.get(i), ySeries.get(i));
            //}
            XYSeriesCollection dataset = new XYSeriesCollection();
            for(var s: series){
                dataset.addSeries(s);
            }
            JFreeChart chart = ChartFactory.createScatterPlot(
                    chartName,
                    xLabel,
                    yLabel,
                    dataset);
            var xyPlot = chart.getXYPlot();
            xyPlot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
            xyPlot.setBackgroundPaint(Color.LIGHT_GRAY);
            xyPlot.setRangeGridlinePaint(Color.BLACK);
            xyPlot.setDomainGridlinePaint(Color.BLACK);

            var nf = NumberFormat.getCurrencyInstance();
            ((NumberAxis) xyPlot.getRangeAxis()).setNumberFormatOverride(nf);
            ((NumberAxis) xyPlot.getDomainAxis()).setNumberFormatOverride(nf);

            xyPlot.getRangeAxis().setLabelFont(this.font);
            xyPlot.getRangeAxis().setTickLabelFont(this.font);

            var renderer = xyPlot.getRenderer();
            ((AbstractRenderer) renderer).setAutoPopulateSeriesStroke(false);
            renderer.setDefaultStroke(this.stroke);
            chart.getLegend().setItemFont(this.font);

            ChartUtils.saveChartAsPNG(
                    new File(ConsoleReports.CHARTS_PREFIX + filename),
                    chart,
                    SeriesReader.readInt("chart.width"),
                    SeriesReader.readInt("chart.height"));
        } catch (IOException ioEx) {
            LOGGER.error("Error.", ioEx);
        }
    }
}
