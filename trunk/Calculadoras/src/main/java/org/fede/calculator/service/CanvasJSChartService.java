/*
 * Copyright (C) 2014 fede
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
import java.util.Collections;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.fede.calculator.money.ForeignExchange;
import org.fede.calculator.money.Inflation;
import static org.fede.calculator.money.Inflation.USD_INFLATION;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.NoSeriesDataFoundException;
import org.fede.calculator.money.SimpleAverage;
import org.fede.calculator.money.series.JSONMoneyAmountSeries;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.MoneyAmountProcessor;
import org.fede.calculator.web.dto.CanvasJSAxisDTO;
import org.fede.calculator.web.dto.CanvasJSChartDTO;
import org.fede.calculator.web.dto.CanvasJSDatapointDTO;
import org.fede.calculator.web.dto.CanvasJSDatumDTO;
import org.fede.calculator.web.dto.CanvasJSTitleDTO;
import org.springframework.stereotype.Service;

/**
 *
 * @author fede
 */
@Service
public class CanvasJSChartService implements ChartService {

    private static final Map<Integer, String> MONTH_NAMES = new HashMap<>();

    static {
        MONTH_NAMES.put(1, "enero");
        MONTH_NAMES.put(2, "febrero");
        MONTH_NAMES.put(3, "marzo");
        MONTH_NAMES.put(4, "abril");
        MONTH_NAMES.put(5, "mayo");
        MONTH_NAMES.put(6, "junio");
        MONTH_NAMES.put(7, "julio");
        MONTH_NAMES.put(8, "agosto");
        MONTH_NAMES.put(9, "septiembre");
        MONTH_NAMES.put(10, "octubre");
        MONTH_NAMES.put(11, "noviembre");
        MONTH_NAMES.put(12, "diciembre");
    }

    @Override
    public CanvasJSChartDTO historicDollarValue(int todayYear, int todayMonth) throws NoSeriesDataFoundException {
        CanvasJSChartDTO dto = new CanvasJSChartDTO();
        CanvasJSTitleDTO title = new CanvasJSTitleDTO("Precio del Dólar (en pesos de " + MONTH_NAMES.get(todayMonth) + " " + todayYear + ")");
        dto.setTitle(title);
        dto.setXAxisTitle("Año");
        CanvasJSAxisDTO yAxis = new CanvasJSAxisDTO();
        yAxis.setTitle("Pesos");
        yAxis.setValueFormatString("$0");
        dto.setAxisY(yAxis);
        CanvasJSDatumDTO datum = new CanvasJSDatumDTO();
        datum.setType("area");
        datum.setColor("salmon");
        dto.setData(Collections.singletonList(datum));
        final List<CanvasJSDatapointDTO> datapoints = new ArrayList<>();
        datum.setDataPoints(datapoints);

        final MoneyAmount oneDollar = new MoneyAmount(BigDecimal.ONE, "USD");
        final Currency ars = Currency.getInstance("ARS");

        MoneyAmountSeries result = Inflation.ARS_INFLATION.adjust(
                ForeignExchange.INSTANCE.exchange(
                        USD_INFLATION.adjust(oneDollar, todayYear, todayMonth), ars), todayYear, todayMonth);

        result.forEach(new MoneyAmountProcessor() {

            @Override
            public void process(int year, int month, MoneyAmount amount) {
                CanvasJSDatapointDTO dataPoint = new CanvasJSDatapointDTO(
                        "date-".concat(String.valueOf(year)).concat("-").concat(String.valueOf(month - 1)).concat("-1"), amount.getAmount());
                datapoints.add(dataPoint);
            }
        });

        return dto;
    }

    @Override
    public CanvasJSChartDTO unlp(int months, boolean pn, boolean pr, boolean dn, boolean dr) throws NoSeriesDataFoundException {
        return this.createCombinedChart(JSONMoneyAmountSeries.readSeries("unlp.json"), months, pn, pr, dn, dr);
    }

    @Override
    public CanvasJSChartDTO lifia(int months, boolean pn, boolean pr, boolean dn, boolean dr) throws NoSeriesDataFoundException {
        return this.createCombinedChart(JSONMoneyAmountSeries.readSeries("lifia.json"), months, pn, pr, dn, dr);
    }

    private CanvasJSChartDTO createCombinedChart(MoneyAmountSeries series, int months, boolean pn, boolean pr, boolean dn, boolean dr) throws NoSeriesDataFoundException {
        CanvasJSChartDTO dto = new CanvasJSChartDTO();
        CanvasJSTitleDTO title = new CanvasJSTitleDTO("Sueldo promedio " + months + " meses");
        dto.setTitle(title);
        dto.setXAxisTitle("Año");
        CanvasJSAxisDTO yAxis = new CanvasJSAxisDTO();
        yAxis.setTitle("Monto");
        yAxis.setValueFormatString("$0");
        dto.setAxisY(yAxis);

        List<CanvasJSDatumDTO> seriesList = new ArrayList<>(4);
        if (pr) {
            seriesList.add(this.getDatum("line", "blue", "Pesos Reales", this.getRealPesosDatapoints(months, series)));
        }
        if (pn) {
            seriesList.add(this.getDatum("line", "red", "Pesos Nominales", this.getNominalPesosDatapoints(months, series)));
        }
        if (dr) {
            seriesList.add(this.getDatum("line", "green", "USD Reales", this.getRealUSDDatapoints(months, series)));
        }
        if (dn) {
            seriesList.add(this.getDatum("line", "black", "USD Nominales", this.getNominalUSDDatapoints(months, series)));
        }

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

    private List<CanvasJSDatapointDTO> getRealPesosDatapoints(int months, MoneyAmountSeries sourceSeries) throws NoSeriesDataFoundException {
        final List<CanvasJSDatapointDTO> datapoints = new ArrayList<>();
        MoneyAmountSeries series = new SimpleAverage(months).average(Inflation.ARS_INFLATION.adjust(sourceSeries, 1999, 11));
        series.forEach(new MoneyAmountProcessorImpl(datapoints));
        return datapoints;
    }

    private List<CanvasJSDatapointDTO> getNominalPesosDatapoints(int months, MoneyAmountSeries sourceSeries) throws NoSeriesDataFoundException {
        final List<CanvasJSDatapointDTO> datapoints = new ArrayList<>();
        MoneyAmountSeries series = new SimpleAverage(months).average(sourceSeries);
        series.forEach(new MoneyAmountProcessorImpl(datapoints));
        return datapoints;
    }

    private List<CanvasJSDatapointDTO> getNominalUSDDatapoints(int months, MoneyAmountSeries sourceSeries) throws NoSeriesDataFoundException {
        final List<CanvasJSDatapointDTO> datapoints = new ArrayList<>();
        MoneyAmountSeries series = new SimpleAverage(months).average(
                ForeignExchange.INSTANCE.exchange(
                        sourceSeries, Currency.getInstance("USD")
                ));
        series.forEach(new MoneyAmountProcessorImpl(datapoints));
        return datapoints;
    }

    private List<CanvasJSDatapointDTO> getRealUSDDatapoints(int months, MoneyAmountSeries sourceSeries) throws NoSeriesDataFoundException {
        final List<CanvasJSDatapointDTO> datapoints = new ArrayList<>();
        MoneyAmountSeries series = new SimpleAverage(months).average(
                Inflation.USD_INFLATION.adjust(
                        ForeignExchange.INSTANCE.exchange(
                                sourceSeries, Currency.getInstance("USD")
                        ), 1999, 11));
        series.forEach(new MoneyAmountProcessorImpl(datapoints));
        return datapoints;
    }

    private static class MoneyAmountProcessorImpl implements MoneyAmountProcessor {

        private final List<CanvasJSDatapointDTO> datapoints;

        public MoneyAmountProcessorImpl(List<CanvasJSDatapointDTO> datapoints) {
            this.datapoints = datapoints;
        }

        @Override
        public void process(int year, int month, MoneyAmount amount) {
            CanvasJSDatapointDTO dataPoint = new CanvasJSDatapointDTO(
                    "date-".concat(String.valueOf(year)).concat("-").concat(String.valueOf(month - 1)).concat("-1"), amount.getAmount());
            datapoints.add(dataPoint);
        }
    }

}