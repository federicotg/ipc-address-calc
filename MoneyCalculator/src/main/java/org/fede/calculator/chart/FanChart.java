/*
 * Copyright (C) 2026 fede
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
import org.fede.calculator.money.series.SeriesReader;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author fede
 */
public class FanChart {

    private static final Logger LOGGER = LoggerFactory.getLogger(FanChart.class);

    private static final Color BAND_OUTER = new Color(0, 114, 178, 50);
    private static final Color BAND_INNER = new Color(0, 114, 178, 90);
    private static final Color MEDIAN_LINE = new Color(0, 114, 178);

    private final Font font;
    private final Stroke stroke;
    private final ChartStyle style;

    public FanChart() {
        this(new ChartStyle(ValueFormat.CURRENCY, Scale.LINEAR));
    }

    public FanChart(ChartStyle style) {
        this.font = ChartConstants.FONT;
        this.stroke = new BasicStroke(3.0f);
        this.style = style;
    }

    public void create(
            String chartName,
            TimeSeries lower,
            TimeSeries middle,
            TimeSeries upper,
            String filename) {

        final var outerBand = new TimeSeriesCollection();
        outerBand.addSeries(lower);
        outerBand.addSeries(middle);

        final var innerBand = new TimeSeriesCollection();
        innerBand.addSeries(middle);
        innerBand.addSeries(upper);

        final var median = new TimeSeriesCollection();
        median.addSeries(upper);

        final var chart = ChartFactory.createTimeSeriesChart(
                chartName,
                "Date",
                "USD",
                median,
                false,
                true,
                false);
        chart.setAntiAlias(SeriesReader.readBoolean("chart.antialias"));

        final XYPlot plot = chart.getXYPlot();
        plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
        plot.setBackgroundPaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.BLACK);
        plot.setDomainGridlinePaint(Color.BLACK);

        final var outerRenderer = this.differenceRenderer(BAND_OUTER);
        plot.setDataset(0, outerBand);
        plot.setRenderer(0, outerRenderer);

        final var innerRenderer = this.differenceRenderer(BAND_INNER);
        plot.setDataset(1, innerBand);
        plot.setRenderer(1, innerRenderer);

        final var lineRenderer = new XYLineAndShapeRenderer(true, false);
        lineRenderer.setSeriesPaint(0, MEDIAN_LINE);
        lineRenderer.setSeriesStroke(0, this.stroke);
        plot.setDataset(2, median);
        plot.setRenderer(2, lineRenderer);

        plot.setDomainAxis(new DateAxis("Date"));
        plot.getDomainAxis().setLabelFont(this.font);
        plot.getDomainAxis().setTickLabelFont(this.font);

        NumberFormat valueFormatter = (NumberFormat) this.style.valueFormat().format();
        if (plot.getRangeAxis() instanceof NumberAxis rangeAxis) {
            rangeAxis.setNumberFormatOverride(valueFormatter);
        }
        plot.getRangeAxis().setLabelFont(this.font);
        plot.getRangeAxis().setTickLabelFont(this.font);
        chart.getLegend().setItemFont(this.font);

        try {
            ChartStrategy.currentStrategy()
                    .saveChart(
                            filename,
                            chart,
                            SeriesReader.readInt("chart.width"),
                            SeriesReader.readInt("chart.height"));
        } catch (IOException ioEx) {
            LOGGER.error("Error.", ioEx);
        }
    }

    private XYDifferenceRenderer differenceRenderer(Color fill) {
        final var renderer = new XYDifferenceRenderer(fill, fill, false);
        renderer.setSeriesPaint(0, fill);
        renderer.setSeriesPaint(1, fill);
        renderer.setSeriesStroke(0, new BasicStroke(0.0f));
        renderer.setSeriesStroke(1, new BasicStroke(0.0f));
        renderer.setSeriesShape(0, null);
        renderer.setSeriesShape(1, null);
        return renderer;
    }

}
