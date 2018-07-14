/*
 * Copyright (C) 2015 fede
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
package org.fede.calculator.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import static org.fede.calculator.money.MathConstants.CONTEXT;
import org.fede.calculator.money.MoneyAmount;
import static org.fede.util.Util.readSeries;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.SortedMapMoneyAmountSeries;
import org.fede.calculator.web.dto.CanvasJSAxisDTO;
import org.fede.calculator.web.dto.CanvasJSChartDTO;
import org.fede.calculator.web.dto.CanvasJSDatapointDTO;
import org.fede.calculator.web.dto.CanvasJSDatumDTO;
import org.fede.calculator.web.dto.CanvasJSTitleDTO;
import org.fede.calculator.web.dto.ExpenseChartSeriesDTO;
import org.fede.util.Util;

/**
 *
 * @author fede
 */
public class CanvasJSMultiSeriesChartService implements MultiSeriesChartService {

    private static final String TOTAL_SERIES_NAME = "Total";

    private final CanvasJSDatapointAssembler realPesosDatapointAssembler;

    private final CanvasJSDatapointAssembler realUSDDatapointAssembler;

    private final CanvasJSDatapointAssembler nominalPesosDatapointAssembler;

    private final List<ExpenseChartSeriesDTO> series;

    private final List<ExpenseChartSeriesDTO> incomeSeries;

    private final List<String> colors;

    public CanvasJSMultiSeriesChartService(
            CanvasJSDatapointAssembler realPesosAssembler,
            CanvasJSDatapointAssembler realUSDAssembler,
            CanvasJSDatapointAssembler nominalPesosAssembler,
            List<ExpenseChartSeriesDTO> incomeSeries,
            List<ExpenseChartSeriesDTO> series,
            List<String> colors) {
        this.nominalPesosDatapointAssembler = nominalPesosAssembler;
        this.colors = colors;
        this.incomeSeries = incomeSeries;
        this.series = series;
        this.realPesosDatapointAssembler = realPesosAssembler;
        this.realUSDDatapointAssembler = realUSDAssembler;
    }

    @Override
    public List<ExpenseChartSeriesDTO> getSeries() {
        return series;
    }

    @Override
    public List<ExpenseChartSeriesDTO> getSeriesWithoutTotal() {

        return this.getSeries().stream()
                .filter(dto -> !TOTAL_SERIES_NAME.equals(dto.getName()))
                .collect(Collectors.toList());
    }

    private CanvasJSDatapointAssembler getAssemblerFor(String currency) {
        Map<String, CanvasJSDatapointAssembler> assemblers = new HashMap<>();

        assemblers.put("USD", realUSDDatapointAssembler);
        assemblers.put("ARS", realPesosDatapointAssembler);

        return assemblers.get(currency);
    }

    @Override
    public CanvasJSChartDTO renderAbsoluteChart(
            String chartTitle,
            int months,
            List<String> seriesNames,
            int year,
            int month,
            String currencyCode) {

        List<CanvasJSDatumDTO> seriesList = new ArrayList<>();

        if (seriesNames != null) {

            MoneyAmountSeries totalSeries = null;
            final boolean collectTotal = seriesNames.contains(TOTAL_SERIES_NAME);

            Iterator<String> colorIterator = this.colors.iterator();

            for (ExpenseChartSeriesDTO s : this.series) {

                if (!TOTAL_SERIES_NAME.equals(s.getName()) && seriesNames.contains(s.getName())) {

                    MoneyAmountSeries eachSeries = readSeries(s.getSeriesName()).exchangeInto(currencyCode);

                    if (collectTotal) {
                        if (totalSeries == null) {
                            totalSeries = eachSeries;
                        } else {
                            totalSeries = totalSeries.add(eachSeries);
                        }
                    }

                    seriesList.add(this.getDatum(
                            "line",
                            colorIterator.next(),
                            s.getName(),
                            this.getAssemblerFor(currencyCode).getDatapoints(months, eachSeries, year, month)
                    ));
                }
            }
            if (collectTotal && totalSeries != null) {
                seriesList.add(this.getDatum(
                        "line",
                        colorIterator.next(),
                        "Total",
                        this.getAssemblerFor(currencyCode).getDatapoints(months, totalSeries, year, month)
                ));
            }
        }

        CanvasJSChartDTO dto = new CanvasJSChartDTO();
        CanvasJSTitleDTO title = new CanvasJSTitleDTO(chartTitle);
        dto.setTitle(title);
        dto.setXAxisTitle("Fecha");
        CanvasJSAxisDTO yAxis = new CanvasJSAxisDTO();
        yAxis.setTitle(currencyCode + " Reales");
        yAxis.setValueFormatString("$0");
        dto.setAxisY(yAxis);
        dto.setData(seriesList);
        return dto;
    }

    @Override
    public CanvasJSChartDTO renderIncomeRelativeChart(String chartTitle, int months, List<String> seriesNames, String currencyCode) {
        List<CanvasJSDatumDTO> seriesList = new ArrayList<>(1);
        if (seriesNames != null && !seriesNames.isEmpty()) {

            MoneyAmountSeries sumSeries = null;
            for (ExpenseChartSeriesDTO s : this.series) {
                if (!TOTAL_SERIES_NAME.equals(s.getName()) && seriesNames.contains(s.getName())) {
                    MoneyAmountSeries eachSeries = readSeries(s.getSeriesName()).exchangeInto(currencyCode);

                    if (sumSeries == null) {
                        sumSeries = eachSeries;
                    } else {
                        sumSeries = sumSeries.add(eachSeries);
                    }
                }
            }

            final MoneyAmountSeries totalIncome = Util.sumSeries("ARS", this.incomeSeries);
            final MoneyAmountSeries percentSeries = new SortedMapMoneyAmountSeries(sumSeries.getCurrency());
            BigDecimal oneCent = new BigDecimal("0.01");
            
            sumSeries.forEach((yearMonth, expensesSum) -> {
                if (totalIncome.getTo().compareTo(yearMonth) >= 0) {

                    MoneyAmount incomeValue = totalIncome.getAmount(yearMonth);
                    //if (incomeValue != null) {
                    percentSeries.putAmount(
                            yearMonth,
                            new MoneyAmount(
                                    expensesSum.getAmount().divide(oneCent.max(incomeValue.getAmount()), CONTEXT),
                                    totalIncome.getCurrency()));
                }
            });

            seriesList.add(this.getDatum(
                    "line",
                    this.colors.get(0),
                    "Gastos / Ingresos",
                    this.nominalPesosDatapointAssembler.getDatapoints(months, percentSeries)));
        }

        CanvasJSChartDTO dto = new CanvasJSChartDTO();
        CanvasJSTitleDTO title = new CanvasJSTitleDTO(chartTitle);
        dto.setTitle(title);
        dto.setXAxisTitle("Fecha");
        CanvasJSAxisDTO yAxis = new CanvasJSAxisDTO();
        yAxis.setTitle("Pesos Reales");
        yAxis.setValueFormatString("#%");
        dto.setAxisY(yAxis);
        dto.setData(seriesList);
        return dto;
    }

    private CanvasJSDatumDTO getDatum(String type, String color, String name, List<CanvasJSDatapointDTO> datapoints) {
        CanvasJSDatumDTO datum = new CanvasJSDatumDTO();
        datum.setType(type);
        datum.setColor(color);
        datum.setLegendText(name);
        datum.setShowInLegend(true);
        datum.setName(name);
        datum.setDataPoints(datapoints);
        return datum;
    }

}
