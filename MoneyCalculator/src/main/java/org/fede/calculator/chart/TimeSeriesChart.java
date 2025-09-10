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
package org.fede.calculator.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import org.fede.calculator.report.ConsoleReports;
import org.fede.calculator.money.Currency;
import static org.fede.calculator.chart.ValueFormat.CURRENCY;
import static org.fede.calculator.chart.ValueFormat.DATE;
import static org.fede.calculator.chart.ValueFormat.NUMBER;
import static org.fede.calculator.chart.ValueFormat.PERCENTAGE;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.SeriesReader;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.ui.RectangleAnchor;
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

    private final Font font;
    private final Stroke stroke;
    private final ChartStyle style;

    public TimeSeriesChart() {
        this(new ChartStyle(ValueFormat.CURRENCY, Scale.LINEAR));
    }

    public TimeSeriesChart(ChartStyle style) {
        this.font = ChartConstants.FONT;
        this.stroke = new BasicStroke(3.0f);
        this.style = style;
    }

    public void create(
            String chartName,
            List<MoneyAmountSeries> series,
            String filename) {
        var c = series.stream()
                .findFirst()
                .map(MoneyAmountSeries::getCurrency)
                .orElse(Currency.USD);
        this.createFromTimeSeries(chartName,
                series.stream().map(ChartSeriesMapper::asTimeSeries).toList(),
                c,
                filename);
    }

    public void createFromTimeSeries(
            String chartName,
            List<TimeSeries> series,
            String filename) {
        this.createFromTimeSeries(chartName, series, Currency.USD, filename);
    }

    public void createFromTimeSeries(
            String chartName,
            List<TimeSeries> series,
            Currency c,
            String filename) {
        try {

            var label = switch (this.style.valueFormat()) {
                case NUMBER ->
                    "";
                case CURRENCY, CURRENCY_DECIMALS ->
                    c.name();
                case PERCENTAGE ->
                    "%";
                case DATE ->
                    "";
            };

            var collection = new TimeSeriesCollection();
            series.stream().forEach(collection::addSeries);

            JFreeChart chart = ChartFactory.createTimeSeriesChart(
                    chartName,
                    "Date",
                    label,
                    collection);
            chart.setAntiAlias(SeriesReader.readBoolean("chart.antialias"));

            var xyPlot = chart.getXYPlot();
            xyPlot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
            xyPlot.setBackgroundPaint(Color.LIGHT_GRAY);
            xyPlot.setRangeGridlinePaint(Color.BLACK);
            xyPlot.setDomainGridlinePaint(Color.BLACK);

            NumberFormat valueFormatter = this.style.valueFormat().format();

            if (this.style.scale() == Scale.LOG) {
                xyPlot.setRangeAxis(new LogarithmicAxis(label));
            }

            ((NumberAxis) xyPlot.getRangeAxis()).setNumberFormatOverride(valueFormatter);

            xyPlot.getRangeAxis().setLabelFont(this.font);
            xyPlot.getRangeAxis().setTickLabelFont(this.font);

            xyPlot.getDomainAxis().setLabelFont(this.font);
            xyPlot.getDomainAxis().setTickLabelFont(this.font);

            var renderer = xyPlot.getRenderer();
            ((AbstractRenderer) renderer).setAutoPopulateSeriesStroke(false);
            renderer.setDefaultStroke(this.stroke);
            chart.getLegend().setItemFont(this.font);
            
            ChartStrategy.currentStrategy().saveChart(
                    filename,
                    chart,
                    SeriesReader.readInt("chart.width"),
                    SeriesReader.readInt("chart.height"));
        } catch (IOException ioEx) {
            LOGGER.error("Error.", ioEx);
        }
    }

}
