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
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author fede
 */
public class PieChart {

    private static final Logger LOGGER = LoggerFactory.getLogger(PieChart.class);

    private final PieSectionLabelGenerator labelGenerator;

    public PieChart(boolean showAbsoluteValue) {
        this(NumberFormat.getPercentInstance(Locale.of("es", "AR")), NumberFormat.getCurrencyInstance(Locale.of("es", "AR")), showAbsoluteValue);
    }

    public PieChart(NumberFormat pctFormat, NumberFormat currencyFormat, boolean showAbsoluteValue) {
        pctFormat.setMinimumFractionDigits(2);
        this.labelGenerator = new StandardPieSectionLabelGenerator(
                showAbsoluteValue ? "{0} {1} {2}" : "{0} {2}",
                currencyFormat,
                pctFormat);
    }

    public void create(String chartTitle, List<PieItem> items, String fileName) {

        try {
            DefaultPieDataset<String> ds = new DefaultPieDataset<>();

            for (var item : items) {
                ds.setValue(item.label(), item.value());
            }

            JFreeChart portfolio = ChartFactory.createPieChart(
                    chartTitle,
                    ds);

            portfolio.setAntiAlias(true);
            portfolio.setTextAntiAlias(true);

            var p = (PiePlot) portfolio.getPlot();

            p.setLabelGenerator(this.labelGenerator);
            ChartUtils.saveChartAsPNG(
                    new File(fileName),
                    portfolio,
                    1200,
                    900);
        } catch (IOException ex) {
            LOGGER.error("Error generating pie chart.", ex);
        }
    }
}
