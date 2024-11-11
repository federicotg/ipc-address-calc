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

import static java.awt.Color.WHITE;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.fede.calculator.money.ConsoleReports;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

/**
 *
 * @author fede
 */
public class PieChart {

    private final Font font;
    private final PieSectionLabelGenerator labelGenerator;
    private final Comparator<PieItem> largerFirst;

    public PieChart(boolean showAbsoluteValue) {
        this(
                NumberFormat.getPercentInstance(Locale.of("es", "AR")),
                NumberFormat.getCurrencyInstance(Locale.of("es", "AR")),
                showAbsoluteValue);
    }

    public PieChart(NumberFormat pctFormat, NumberFormat currencyFormat, boolean showAbsoluteValue) {
        font = new Font("SansSerif", Font.PLAIN, 16);
        this.largerFirst = Comparator.comparing((PieItem i) -> i.value().intValue(), Comparator.reverseOrder());
        pctFormat.setMinimumFractionDigits(2);
        this.labelGenerator = new StandardPieSectionLabelGenerator(
                showAbsoluteValue ? "{0} {1} {2}" : "{0} {2}",
                currencyFormat,
                pctFormat);
    }

    public void create(String chartTitle, List<PieItem> items, String fileName) throws IOException {

        DefaultPieDataset<String> ds = new DefaultPieDataset<>();

        items
                .stream()
                .sorted(this.largerFirst)
                .forEach(item -> ds.setValue(item.label(), item.value()));

        JFreeChart chart = ChartFactory.createPieChart(chartTitle, ds);
        chart.setBorderVisible(false);
        var p = (PiePlot) chart.getPlot();
        p.setOutlineVisible(false);
        p.setBackgroundPaint(WHITE);
        p.setLabelFont(this.font);
        p.setLabelGenerator(this.labelGenerator);
        p.setLabelBackgroundPaint(WHITE);
        p.setLabelOutlinePaint(WHITE);
        p.setLabelShadowPaint(WHITE);
        chart.getLegend().setItemFont(this.font);

        ChartUtils.saveChartAsPNG(
                new File(ConsoleReports.CHARTS_PREFIX + fileName),
                chart,
                1200,
                900,
                null,
                true,
                9);

    }

}
