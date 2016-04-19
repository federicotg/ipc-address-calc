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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import static org.fede.calculator.money.MathConstants.CONTEXT;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.NoSeriesDataFoundException;
import static org.fede.util.Util.readSeries;
import org.fede.calculator.money.series.MoneyAmountProcessor;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.SortedMapMoneyAmountSeries;
import org.fede.calculator.web.dto.CanvasJSAxisDTO;
import org.fede.calculator.web.dto.CanvasJSChartDTO;
import org.fede.calculator.web.dto.CanvasJSDatapointDTO;
import org.fede.calculator.web.dto.CanvasJSDatumDTO;
import org.fede.calculator.web.dto.CanvasJSTitleDTO;
import org.fede.calculator.web.dto.ExpenseChartSeriesDTO;
import org.fede.util.Util;
import org.springframework.context.annotation.Lazy;

/**
 *
 * @author fede
 */
public class CanvasJSMultiSeriesChartService implements MultiSeriesChartService {

    @Resource(name = "realPesosDatapointAssembler")
    @Lazy
    private CanvasJSDatapointAssembler realPesosDatapointAssembler;

    @Resource(name = "realUSDDatapointAssembler")
    @Lazy
    private CanvasJSDatapointAssembler realUSDDatapointAssembler;

    @Resource(name = "nominalPesosDatapointAssembler")
    @Lazy
    private CanvasJSDatapointAssembler nominalPesosDatapointAssembler;

    private static final String TOTAL_SERIES_NAME = "Total";

    private List<ExpenseChartSeriesDTO> series;
    
    @Resource(name = "incomesSeries")
    private List<ExpenseChartSeriesDTO> incomeSeries;

    @Override
    public List<ExpenseChartSeriesDTO> getSeries() {
        return series;
    }

    @Override
    public List<ExpenseChartSeriesDTO> getSeriesWithoutTotal() {
        List<ExpenseChartSeriesDTO> list = new ArrayList<>(this.getSeries());
        for (Iterator<ExpenseChartSeriesDTO> it = list.iterator(); it.hasNext();) {
            if (TOTAL_SERIES_NAME.equals(it.next().getName())) {
                it.remove();
            }
        }
        return list;
    }

    @Override
    public void setSeries(List<ExpenseChartSeriesDTO> expenseSeries) {
        this.series = expenseSeries;
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
            String currencyCode) throws NoSeriesDataFoundException {

        List<CanvasJSDatumDTO> seriesList = new ArrayList<>();

        if (seriesNames != null) {

            MoneyAmountSeries totalSeries = null;
            final boolean collectTotal = seriesNames.contains(TOTAL_SERIES_NAME);
            String totalColor = "red";

            for (ExpenseChartSeriesDTO s : this.series) {

                if (TOTAL_SERIES_NAME.equals(s.getName())) {
                    totalColor = s.getColor();
                }

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
                            s.getColor(),
                            s.getName(),
                            this.getAssemblerFor(currencyCode).getDatapoints(months, eachSeries, year, month)
                    ));
                }
            }
            if (collectTotal && totalSeries != null) {
                seriesList.add(this.getDatum(
                        "line",
                        totalColor,
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
    public CanvasJSChartDTO renderIncomeRelativeChart(String chartTitle, int months, List<String> seriesNames, String currencyCode) throws NoSeriesDataFoundException {
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

            final MoneyAmountSeries totalIncome = Util.sumSeries(this.incomeSeries);
            final MoneyAmountSeries percentSeries = new SortedMapMoneyAmountSeries(sumSeries.getCurrency());
            sumSeries.forEach(new MoneyAmountProcessor() {

                @Override
                public void process(int year, int month, MoneyAmount expensesSum) throws NoSeriesDataFoundException {
                    MoneyAmount incomeValue = totalIncome.getAmount(year, month);
                    if (incomeValue != null) {
                        percentSeries.putAmount(
                                year,
                                month,
                                new MoneyAmount(
                                        expensesSum.getAmount().divide(incomeValue.getAmount(), CONTEXT),
                                        totalIncome.getCurrency()));
                    }
                }
            });

            seriesList.add(this.getDatum(
                    "line",
                    "red",
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
