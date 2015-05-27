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
import java.util.Currency;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Resource;
import org.fede.calculator.money.ForeignExchange;
import static org.fede.calculator.money.MathConstants.CONTEXT;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.NoSeriesDataFoundException;
import org.fede.calculator.money.series.JSONMoneyAmountSeries;
import static org.fede.calculator.money.series.JSONMoneyAmountSeries.readSeries;
import org.fede.calculator.money.series.MoneyAmountProcessor;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.web.dto.CanvasJSAxisDTO;
import org.fede.calculator.web.dto.CanvasJSChartDTO;
import org.fede.calculator.web.dto.CanvasJSDatapointDTO;
import org.fede.calculator.web.dto.CanvasJSDatumDTO;
import org.fede.calculator.web.dto.CanvasJSTitleDTO;
import org.fede.calculator.web.dto.ExpenseChartSeriesDTO;
import org.springframework.context.annotation.Lazy;

/**
 *
 * @author fede
 */
public class CanvasJSMultiSeriesChartService implements MultiSeriesChartService {

    @Resource(name = "realPesosDatapointAssembler")
    @Lazy
    private CanvasJSDatapointAssembler realPesosDatapointAssembler;

    @Resource(name = "nominalPesosDatapointAssembler")
    @Lazy
    private CanvasJSDatapointAssembler nominalPesosDatapointAssembler;

    private static final String TOTAL_SERIES_NAME = "Total";

    private List<ExpenseChartSeriesDTO> series;

    @Override
    public List<ExpenseChartSeriesDTO> getSeries() {
        return series;
    }

    
    @Override
    public List<ExpenseChartSeriesDTO> getSeriesWithoutTotal() {
        List<ExpenseChartSeriesDTO> list = new ArrayList<>(this.getSeries());
        for(Iterator<ExpenseChartSeriesDTO> it = list.iterator();it.hasNext();){
            if(TOTAL_SERIES_NAME.equals(it.next().getName())){
                it.remove();
            }
        }
        return list;
    }
    
    @Override
    public void setSeries(List<ExpenseChartSeriesDTO> expenseSeries) {
        this.series = expenseSeries;
    }

    @Override
    public CanvasJSChartDTO renderAbsoluteChart(String chartTitle, int months, List<String> seriesNames, int year, int month) throws NoSeriesDataFoundException {

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

                    MoneyAmountSeries eachSeries = JSONMoneyAmountSeries.readSeries(s.getSeriesName());
                    if (collectTotal) {
                        if (totalSeries == null) {
                            totalSeries = eachSeries;
                        } else {
                            totalSeries = totalSeries.add(BaseCanvasJSDatapointAssembler.dollarToPesosIfNeeded(eachSeries));
                        }
                    }

                    seriesList.add(this.getDatum(
                            "line",
                            s.getColor(),
                            s.getName(),
                            this.realPesosDatapointAssembler.getDatapoints(months, eachSeries, year, month)
                    ));
                }
            }
            if (collectTotal && totalSeries != null) {
                seriesList.add(this.getDatum(
                        "line",
                        totalColor,
                        "Total",
                        this.realPesosDatapointAssembler.getDatapoints(months, totalSeries, year, month)
                ));
            }
        }

        CanvasJSChartDTO dto = new CanvasJSChartDTO();
        CanvasJSTitleDTO title = new CanvasJSTitleDTO(chartTitle);
        dto.setTitle(title);
        dto.setXAxisTitle("Fecha");
        CanvasJSAxisDTO yAxis = new CanvasJSAxisDTO();
        yAxis.setTitle("Pesos Reales");
        yAxis.setValueFormatString("$0");
        dto.setAxisY(yAxis);
        dto.setData(seriesList);
        return dto;
    }

    @Override
    public CanvasJSChartDTO renderIncomeRelativeChart(String chartTitle, int months, List<String> seriesNames) throws NoSeriesDataFoundException {

        List<CanvasJSDatumDTO> seriesList = new ArrayList<>(1);
        if (seriesNames != null && !seriesNames.isEmpty()) {

            MoneyAmountSeries sumSeries = null;
            for (ExpenseChartSeriesDTO s : this.series) {
                if (!TOTAL_SERIES_NAME.equals(s.getName()) && seriesNames.contains(s.getName())) {
                    MoneyAmountSeries eachSeries = JSONMoneyAmountSeries.readSeries(s.getSeriesName());
                    if (eachSeries.getCurrency().equals(Currency.getInstance("USD"))) {
                        eachSeries = ForeignExchange.USD_ARS.exchange(eachSeries, Currency.getInstance("ARS"));
                    }
                    if (sumSeries == null) {
                        sumSeries = eachSeries;
                    } else {
                        sumSeries = sumSeries.add(eachSeries);
                    }
                }
            }

            final MoneyAmountSeries totalIncome = readSeries("unlp.json").add(readSeries("lifia.json")).add(readSeries("plazofijo.json"));
            final MoneyAmountSeries percentSeries = new JSONMoneyAmountSeries(sumSeries.getCurrency());
            sumSeries.forEach(new MoneyAmountProcessor() {

                @Override
                public void process(int year, int month, MoneyAmount expensesSum) throws NoSeriesDataFoundException {
                    percentSeries.putAmount(
                            year,
                            month,
                            new MoneyAmount(
                                    expensesSum.getAmount().divide(totalIncome.getAmount(year, month).getAmount(), CONTEXT),
                                    totalIncome.getCurrency()));
                }
            });

            seriesList.add(this.getDatum(
                    "line",
                    "red",
                    "Gastos / Ingresos",
                    this.nominalPesosDatapointAssembler.getDatapoints(months, percentSeries)
            ));
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
