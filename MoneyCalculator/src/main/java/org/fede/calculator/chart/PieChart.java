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

import static java.awt.Color.WHITE;
import java.awt.Font;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.fede.calculator.money.series.SeriesReader;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.MultiplePiePlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.util.TableOrder;
import org.jfree.data.category.DefaultCategoryDataset;
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
        this(showAbsoluteValue, false);
    }

    public PieChart(boolean showAbsoluteValue, boolean byLabel) {
        this(
                NumberFormat.getPercentInstance(Locale.of("es", "AR")),
                NumberFormat.getCurrencyInstance(Locale.of("es", "AR")),
                showAbsoluteValue,
                byLabel);
    }

    public PieChart(NumberFormat pctFormat, NumberFormat currencyFormat, boolean showAbsoluteValue, boolean byLabel) {
        this.font = ChartConstants.FONT;
        this.largerFirst = byLabel
                ? Comparator.comparing(PieItem::label)
                : Comparator.comparing((PieItem i) -> i.value().intValue(), Comparator.reverseOrder());

        pctFormat.setMinimumFractionDigits(2);
        this.labelGenerator = new StandardPieSectionLabelGenerator(
                showAbsoluteValue ? "{0} {1} {2}" : "{0} {2}",
                currencyFormat,
                pctFormat);
    }

    public void create(String chartTitle, List<PieItem> items, String fileName) {

        DefaultPieDataset<String> ds = new DefaultPieDataset<>();
        items
                .stream()
                .sorted(this.largerFirst)
                .forEach(item -> ds.setValue(item.label(), item.value()));

        JFreeChart chart = ChartFactory.createPieChart(chartTitle, ds);
        chart.setAntiAlias(SeriesReader.readBoolean("chart.antialias"));
        chart.setBorderVisible(false);
        this.stylePiePlot((PiePlot) chart.getPlot());
        chart.getLegend().setItemFont(this.font);

        final var size = SeriesReader.readInt("chart.piewidth");
        ChartSaver.save(fileName, chart, size, size);
    }

    /*
     * One image with several pies side by side. Each map entry is a pie:
     * key = pie title, value = slices. Prefer a LinkedHashMap (or List.of(Map.entry(...)))
     * so the left-to-right order is stable.
     */
    public void createMultiple(String chartTitle, List<Map.Entry<String, List<PieItem>>> pies, String fileName) {

        // Row-key order is shared across all pies in a MultiplePiePlot, so we
        // keep the caller’s list order instead of sorting each pie by size.
        final var dataset = new DefaultCategoryDataset();
        for (var pie : pies) {
            final var pieTitle = pie.getKey();
            for (var item : pie.getValue()) {
                dataset.addValue(item.value(), item.label(), pieTitle);
            }
        }

        JFreeChart chart = ChartFactory.createMultiplePieChart(
                chartTitle,
                dataset,
                TableOrder.BY_COLUMN,
                true,
                false,
                false);
        chart.setAntiAlias(SeriesReader.readBoolean("chart.antialias"));
        chart.setBorderVisible(false);

        var multi = (MultiplePiePlot) chart.getPlot();
        multi.setOutlineVisible(false);
        multi.setBackgroundPaint(WHITE);
        this.stylePiePlot((PiePlot) multi.getPieChart().getPlot());
        if (chart.getLegend() != null) {
            chart.getLegend().setItemFont(this.font);
        }

        final var size = SeriesReader.readInt("chart.piewidth");
        ChartSaver.save(fileName, chart, size * Math.max(1, pies.size()), size);
    }

    private void stylePiePlot(PiePlot<?> plot) {
        plot.setOutlineVisible(false);
        plot.setBackgroundPaint(WHITE);
        plot.setLabelFont(this.font);
        plot.setLabelGenerator(this.labelGenerator);
        plot.setLabelBackgroundPaint(WHITE);
        plot.setLabelOutlinePaint(WHITE);
        plot.setLabelShadowPaint(WHITE);
    }

}
