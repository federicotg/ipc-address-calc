/*
 * Copyright (C) 2025 fede
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
import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
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
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StackedXYAreaRenderer2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author fede
 */
public class StackedTimeSeriesChart {

    private static final Logger LOGGER = LoggerFactory.getLogger(StackedTimeSeriesChart.class);

    private final Font font;
    private final Stroke stroke;
    private final ChartStyle style;

    public StackedTimeSeriesChart() {
        this(ValueFormat.CURRENCY);
    }

    public StackedTimeSeriesChart(ValueFormat valueFormat) {
        this(new ChartStyle(valueFormat, Scale.LINEAR));
    }

    public StackedTimeSeriesChart(ChartStyle style) {
        this.font = ChartConstants.FONT;
        this.stroke = new BasicStroke(3.0f);
        this.style = style;
    }

    public void create(
            String chartName,
            List<MoneyAmountSeries> series,
            String filename) {
        try {

            var c = series.stream()
                    .findFirst()
                    .map(MoneyAmountSeries::getCurrency)
                    .orElse(Currency.USD);

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

            NumberFormat valueFormatter = this.style.valueFormat().format();
            
            JFreeChart chart = ChartFactory.createXYAreaChart(
                    chartName,
                    "Date",
                    label,
                    ChartSeriesMapper.asTimeTableXYDataset(series)
            );

            chart.setAntiAlias(SeriesReader.readBoolean("chart.antialias"));
            
            // Customize plot
            XYPlot plot = (XYPlot) chart.getPlot();

            plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
            plot.setBackgroundPaint(Color.LIGHT_GRAY);
            plot.setRangeGridlinePaint(Color.BLACK);
            plot.setDomainGridlinePaint(Color.BLACK);
            if (this.style.scale() == Scale.LOG) {
                plot.setRangeAxis(new LogarithmicAxis(label));
            }

            ((NumberAxis) plot.getRangeAxis()).setNumberFormatOverride(valueFormatter);

            plot.getRangeAxis().setLabelFont(this.font);
            plot.getRangeAxis().setTickLabelFont(this.font);

            var renderer = new StackedXYAreaRenderer2();

            renderer.setDefaultStroke(this.stroke);
            plot.setRenderer(renderer);

            plot.setDomainAxis(new DateAxis("Date"));

            plot.getDomainAxis().setLabelFont(this.font);
            plot.getDomainAxis().setTickLabelFont(this.font);
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
