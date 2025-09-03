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
import java.text.DateFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import org.fede.calculator.report.ConsoleReports;
import org.fede.calculator.money.Currency;
import static org.fede.calculator.chart.ValueFormat.CURRENCY;
import static org.fede.calculator.chart.ValueFormat.DATE;
import static org.fede.calculator.chart.ValueFormat.NUMBER;
import static org.fede.calculator.chart.ValueFormat.PERCENTAGE;
import org.fede.calculator.money.series.SeriesReader;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
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

    private static final String DATE_FORMAT = "dd-MMM-yy";

    private final Font font;
    private final Stroke stroke;
    private final ChartStyle styleX;
    private final ChartStyle styleY;

    public ScatterXYChart(ChartStyle styleX, ChartStyle styleY) {
        this.font = ChartConstants.FONT;
        this.stroke = new BasicStroke(3.0f);
        this.styleX = styleX;
        this.styleY = styleY;
    }

    public void create(
            String chartName,
            Currency currency,
            List<XYSeries> series,
            String xLabel,
            String yLabel,
            String filename) {
        try {

            XYSeriesCollection dataset = new XYSeriesCollection();
            for (var s : series) {
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

            Format valueFormatterX = switch (this.styleX.valueFormat()) {
                case NUMBER ->
                    NumberFormat.getNumberInstance();
                case CURRENCY ->
                    this.currencyFormat(
                    currency);
                case PERCENTAGE ->
                    NumberFormat.getPercentInstance();
                case DATE ->
                    new SimpleDateFormat(DATE_FORMAT);
            };

            Format valueFormatterY = switch (this.styleY.valueFormat()) {
                case NUMBER ->
                    NumberFormat.getNumberInstance();
                case CURRENCY ->
                    this.currencyFormat(
                    currency);
                case PERCENTAGE ->
                    NumberFormat.getPercentInstance();
                case DATE ->
                    new SimpleDateFormat(DATE_FORMAT);
            };

            if (this.styleY.scale() == Scale.LOG) {

                xyPlot.setRangeAxis(new LogarithmicAxis(yLabel));
            }

            if (this.styleX.valueFormat() == DATE) {
                DateAxis dateAxis = new DateAxis("Date");
                dateAxis.setDateFormatOverride((DateFormat) valueFormatterX);
                xyPlot.setDomainAxis(dateAxis);

            } else {
                ((NumberAxis) xyPlot.getDomainAxis()).setNumberFormatOverride((NumberFormat) valueFormatterX);
            }

            if (this.styleY.valueFormat() == DATE) {
                DateAxis dateAxis = new DateAxis("Date");
                dateAxis.setDateFormatOverride((DateFormat) valueFormatterY);
                xyPlot.setRangeAxis(dateAxis);

            } else {
                ((NumberAxis) xyPlot.getRangeAxis()).setNumberFormatOverride((NumberFormat) valueFormatterY);
            }

            xyPlot.getRangeAxis().setLabelFont(this.font);
            xyPlot.getRangeAxis().setTickLabelFont(this.font);
            xyPlot.getDomainAxis().setLabelFont(this.font);
            xyPlot.getDomainAxis().setTickLabelFont(this.font);

            var renderer = xyPlot.getRenderer();

            renderer.setDefaultItemLabelGenerator((XYDataset dataset1, int seriesIndex, int itemIndex) -> {
                XYDataItem item = ((XYSeriesCollection) dataset1).getSeries(seriesIndex).getDataItem(itemIndex);
                if (item instanceof LabeledXYDataItem i) {
                    return i.getLabel();
                }
                return "(" + item.getX() + ", " + item.getY() + ")";
            });
            ((AbstractRenderer) renderer).setAutoPopulateSeriesStroke(false);
            renderer.setDefaultStroke(this.stroke);
            chart.getLegend().setItemFont(this.font);
            renderer.setDefaultItemLabelsVisible(true);
            renderer.setDefaultItemLabelFont(this.font);

            ChartUtils.saveChartAsPNG(
                    new File(ConsoleReports.CHARTS_PREFIX + filename),
                    chart,
                    SeriesReader.readInt("chart.width"),
                    SeriesReader.readInt("chart.height"));
        } catch (IOException ioEx) {
            LOGGER.error("Error.", ioEx);
        }
    }

    private NumberFormat currencyFormat(Currency currency) {
        var nf = NumberFormat.getCurrencyInstance();
        nf.setCurrency(java.util.Currency.getInstance(currency.name()));
        nf.setMaximumFractionDigits(0);
        return nf;
    }
}
