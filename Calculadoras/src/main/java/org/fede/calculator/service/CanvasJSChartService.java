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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.fede.calculator.money.Inflation.USD_INFLATION;
import static org.fede.calculator.money.Inflation.ARS_INFLATION;
import org.fede.calculator.money.ForeignExchanges;
import org.fede.calculator.money.Inflation;
import org.fede.calculator.money.MathConstants;
import static org.fede.calculator.money.MathConstants.CONTEXT;
import static org.fede.util.Util.readSeries;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.NoSeriesDataFoundException;
import org.fede.calculator.money.SimpleAggregation;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.YearMonth;
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
public class CanvasJSChartService implements ChartService, MathConstants {

    public CanvasJSChartService(
            CanvasJSDatapointAssembler realPesosDatapointAssembler,
            CanvasJSDatapointAssembler nominalPesosDatapointAssembler,
            CanvasJSDatapointAssembler realUSDDatapointAssembler,
            CanvasJSDatapointAssembler nominalUSDDatapointAssembler,
            CanvasJSDatapointAssembler realEURDatapointAssembler,
            CanvasJSDatapointAssembler nominalEURDatapointAssembler,
            List<ExpenseChartSeriesDTO> incomeSeries,
            List<ExpenseChartSeriesDTO> savingsSeries,
            List<String> colors,
            Map<Integer, String> monthNames) {
        this.realPesosDatapointAssembler = realPesosDatapointAssembler;
        this.nominalPesosDatapointAssembler = nominalPesosDatapointAssembler;
        this.realUSDDatapointAssembler = realUSDDatapointAssembler;
        this.nominalUSDDatapointAssembler = nominalUSDDatapointAssembler;
        this.realEURDatapointAssembler = realEURDatapointAssembler;
        this.nominalEURDatapointAssembler = nominalEURDatapointAssembler;
        this.incomeSeries = incomeSeries;
        this.savingsSeries = savingsSeries;
        this.colors = colors;
        this.monthNames = monthNames;
    }

    private final CanvasJSDatapointAssembler realPesosDatapointAssembler;

    private final CanvasJSDatapointAssembler nominalPesosDatapointAssembler;

    private final CanvasJSDatapointAssembler realUSDDatapointAssembler;

    private final CanvasJSDatapointAssembler nominalUSDDatapointAssembler;

    private final CanvasJSDatapointAssembler realEURDatapointAssembler;

    private final CanvasJSDatapointAssembler nominalEURDatapointAssembler;

    private final List<ExpenseChartSeriesDTO> incomeSeries;

    private final List<ExpenseChartSeriesDTO> savingsSeries;

    private final List<String> colors;

    private final Map<Integer, String> monthNames;
    
    @Override
    public CanvasJSChartDTO combinedIncomes(
            int months,
            boolean pn,
            boolean pr,
            boolean dn,
            boolean dr,
            boolean en,
            boolean er,
            int year,
            int month,
            List<String> seriesNames) throws NoSeriesDataFoundException {

        final StringBuilder sb = new StringBuilder(50);
        MoneyAmountSeries combinedSeries = null;
        for (ExpenseChartSeriesDTO dto : this.incomeSeries) {
            if (seriesNames.contains(dto.getName())) {
                if (combinedSeries == null) {
                    combinedSeries = readSeries(dto.getSeriesName());
                    sb.append(dto.getName());
                } else {
                    combinedSeries = combinedSeries.add(readSeries(dto.getSeriesName()));
                    sb.append(" + ").append(dto.getName());
                }
            }
        }
        return this.createCombinedChart(sb.toString(), combinedSeries,
                months, pn, pr, dn, dr, en, er, year, month);
    }

    @Override
    public CanvasJSChartDTO savings(
            boolean pn,
            boolean pr,
            boolean dn,
            boolean dr,
            boolean en,
            boolean er,
            int year,
            int month) throws NoSeriesDataFoundException {

        MoneyAmountSeries savingsSum = null;
        for (ExpenseChartSeriesDTO dto : this.savingsSeries) {
            if (!dto.isTotal()) {
                MoneyAmountSeries s = readSeries(dto.getSeriesName()).exchangeInto("ARS");
                if (savingsSum == null) {
                    savingsSum = s;
                } else {
                    savingsSum = savingsSum.add(s);
                }
            }
        }

        return this.createCombinedChart(
                "Ahorros",
                savingsSum,
                1, pn, pr, dn, dr, en, er,
                year, month);
    }

    private CanvasJSChartDTO createCombinedChart(
            String titleText,
            MoneyAmountSeries series,
            int months,
            boolean pn,
            boolean pr,
            boolean dn,
            boolean dr,
            boolean en,
            boolean er,
            int year,
            int month) throws NoSeriesDataFoundException {
        CanvasJSChartDTO dto = new CanvasJSChartDTO();
        CanvasJSTitleDTO title = new CanvasJSTitleDTO(titleText);
        dto.setTitle(title);
        dto.setXAxisTitle("Año");
        CanvasJSAxisDTO yAxis = new CanvasJSAxisDTO();
        yAxis.setTitle("Monto");
        yAxis.setValueFormatString("$0");
        dto.setAxisY(yAxis);

        List<CanvasJSDatumDTO> seriesList = new ArrayList<>(6);
        if (pr) {
            seriesList.add(this.getDatum("line", colors.get(0), "Pesos Reales", this.realPesosDatapointAssembler.getDatapoints(months, series, year, month)));
        }
        if (pn) {
            seriesList.add(this.getDatum("line", colors.get(1), "Pesos Nominales", this.nominalPesosDatapointAssembler.getDatapoints(months, series)));
        }
        if (dr) {
            seriesList.add(this.getDatum("line", colors.get(2), "USD Reales", this.realUSDDatapointAssembler.getDatapoints(months, series, year, month)));
        }
        if (dn) {
            seriesList.add(this.getDatum("line", colors.get(3), "USD Nominales", this.nominalUSDDatapointAssembler.getDatapoints(months, series)));
        }
        if (er) {
            seriesList.add(this.getDatum("line", colors.get(4), "EUR Reales", this.realEURDatapointAssembler.getDatapoints(months, series, year, month)));
        }
        if (en) {
            seriesList.add(this.getDatum("line", colors.get(5), "EUR Nominales", this.nominalEURDatapointAssembler.getDatapoints(months, series)));
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

    @Override
    public CanvasJSChartDTO goldIncomeAndSavings() throws NoSeriesDataFoundException {

        MoneyAmountSeries savings = Util.sumSeries(this.savingsSeries).exchangeInto("XAU");
        MoneyAmountSeries income = Util.sumSeries(this.incomeSeries).exchangeInto("XAU");

        CanvasJSChartDTO dto = new CanvasJSChartDTO();
        CanvasJSTitleDTO title = new CanvasJSTitleDTO("Oro");
        dto.setTitle(title);
        dto.setXAxisTitle("Año");
        CanvasJSAxisDTO yAxis = new CanvasJSAxisDTO();
        yAxis.setTitle("Onza Troy");
        //yAxis.setValueFormatString("0");
        dto.setAxisY(yAxis);

        List<CanvasJSDatumDTO> seriesList = new ArrayList<>(2);
        dto.setData(seriesList);

        List<CanvasJSDatapointDTO> datapoints = new ArrayList<>();
        savings.forEach(new CanvasJSMoneyAmountProcessor(datapoints));
        seriesList.add(this.getDatum("line", this.colors.get(0), "Ahorros", datapoints));

        datapoints = new ArrayList<>();
        income.forEach(new CanvasJSMoneyAmountProcessor(datapoints));
        seriesList.add(this.getDatum("line", this.colors.get(1), "Ingresos", datapoints));

        return dto;

    }

    @Override
    public CanvasJSChartDTO savingsAndIncomeEvolution() throws NoSeriesDataFoundException {

        YearMonth lastInflationData = Inflation.USD_INFLATION.getTo();

        MoneyAmountSeries lastYearSavings = new SimpleAggregation(12)
                .change(
                        Inflation.USD_INFLATION.adjust(
                                Util.sumSeries(this.savingsSeries).exchangeInto("USD"),
                                lastInflationData.getYear(),
                                lastInflationData.getMonth()));

        MoneyAmountSeries lastYearIncome = new SimpleAggregation(12)
                .change(
                        new SimpleAggregation(12).sum(
                                Inflation.USD_INFLATION.adjust(
                                        Util.sumSeries(this.incomeSeries).exchangeInto("USD"),
                                        lastInflationData.getYear(),
                                        lastInflationData.getMonth()
                                )));

        CanvasJSChartDTO dto = new CanvasJSChartDTO();
        CanvasJSTitleDTO title = new CanvasJSTitleDTO("Cambio Anual");
        dto.setTitle(title);
        dto.setXAxisTitle("Año");
        CanvasJSAxisDTO yAxis = new CanvasJSAxisDTO();
        yAxis.setTitle("USD Reales");
        //yAxis.setValueFormatString("0");
        dto.setAxisY(yAxis);

        List<CanvasJSDatumDTO> seriesList = new ArrayList<>(2);
        dto.setData(seriesList);

        List<CanvasJSDatapointDTO> datapoints = new ArrayList<>();
        lastYearSavings.forEach(new CanvasJSMoneyAmountProcessor(datapoints));
        seriesList.add(this.getDatum("line", this.colors.get(0), "Ahorro Anual", datapoints));

        datapoints = new ArrayList<>();
        lastYearIncome.forEach(new CanvasJSMoneyAmountProcessor(datapoints));
        seriesList.add(this.getDatum("line", this.colors.get(1), "Ingreso Anual", datapoints));

        return dto;

    }

    @Override
    public CanvasJSChartDTO savedSalaries() throws NoSeriesDataFoundException {

        MoneyAmountSeries income = new SimpleAggregation(12).average(Util.sumSeries(this.incomeSeries).exchangeInto("USD"));

        final MoneyAmountSeries savings = Util.sumSeries(this.savingsSeries);

        CanvasJSChartDTO dto = new CanvasJSChartDTO();
        CanvasJSTitleDTO title = new CanvasJSTitleDTO("Sueldos Ahorrados");
        dto.setTitle(title);
        dto.setXAxisTitle("Año");
        CanvasJSAxisDTO yAxis = new CanvasJSAxisDTO();
        yAxis.setTitle("Sueldos");
        dto.setAxisY(yAxis);

        List<CanvasJSDatumDTO> seriesList = new ArrayList<>(1);
        dto.setData(seriesList);

        final List<CanvasJSDatapointDTO> datapoints = new ArrayList<>();
        income.forEachNonZero((int year, int month, MoneyAmount sueldo) -> {
            YearMonth ym = new YearMonth(year, month);
            if (ym.compareTo(savings.getFrom()) >= 0 && ym.compareTo(savings.getTo()) <= 0) {
                CanvasJSDatapointDTO dataPoint = new CanvasJSDatapointDTO(
                        "date-".concat(String.valueOf(year)).concat("-").concat(String.valueOf(month - 1)).concat("-15"),
                        savings.getAmount(year, month).getAmount().divide(sueldo.getAmount(), CONTEXT)
                );
                datapoints.add(dataPoint);
            }
        });
        seriesList.add(this.getDatum("line", this.colors.get(0), "Sueldos", datapoints));

        return dto;

    }

    @Override
    public CanvasJSChartDTO hisotricDollar() throws NoSeriesDataFoundException {
        YearMonth latestData = Inflation.USD_INFLATION.getTo();
        final int todayMonth = latestData.getMonth();
        final int todayYear = latestData.getYear();
        final MoneyAmount oneDollar = new MoneyAmount(BigDecimal.ONE, "USD");

        /**
         * - tomo USD 1.00, - lo ajusto por la inflación de USA y obtengo una
         * serie, - cada valor de la serie lo paso a pesos según valor dolar de
         * cada momento, - cada valor lo paso a pesos de hoy.
         *
         */
        final MoneyAmountSeries historicDollar = ARS_INFLATION.adjust(
                ForeignExchanges.USD_ARS.exchange(
                        USD_INFLATION.adjust(oneDollar, todayYear, todayMonth), "ARS"), todayYear, todayMonth);

        CanvasJSChartDTO dto = new CanvasJSChartDTO();
        CanvasJSTitleDTO title = new CanvasJSTitleDTO("Dólar en Pesos de " + this.monthNames.get(todayMonth) + "/" + todayYear);
        dto.setTitle(title);
        dto.setXAxisTitle("Año");
        CanvasJSAxisDTO yAxis = new CanvasJSAxisDTO();
        yAxis.setValueFormatString("$0");
        yAxis.setTitle("Pesos Reales");
        dto.setAxisY(yAxis);

        List<CanvasJSDatumDTO> seriesList = new ArrayList<>(1);
        dto.setData(seriesList);

        final List<CanvasJSDatapointDTO> datapoints = new ArrayList<>();
        historicDollar.forEach(new CanvasJSMoneyAmountProcessor(datapoints));
        seriesList.add(this.getDatum("area", this.colors.get(0), "Dólar", datapoints));
        return dto;
    }

    @Override
    public CanvasJSChartDTO historicGold() throws NoSeriesDataFoundException {

        final YearMonth latestUSDCPIData = USD_INFLATION.getTo();

        final int todayMonth = latestUSDCPIData.getMonth();
        final int todayYear = latestUSDCPIData.getYear();

        final MoneyAmount oneTroyOunce = new MoneyAmount(BigDecimal.ONE, "XAU");

        final MoneyAmountSeries historicGold = USD_INFLATION.adjust(ForeignExchanges.USD_XAU.exchange(oneTroyOunce, "USD"), todayYear, todayMonth);

        CanvasJSChartDTO dto = new CanvasJSChartDTO();
        CanvasJSTitleDTO title = new CanvasJSTitleDTO("Onza Troy en USD de " + this.monthNames.get(todayMonth) + " / " + todayYear);
        dto.setTitle(title);
        dto.setXAxisTitle("Año");
        CanvasJSAxisDTO yAxis = new CanvasJSAxisDTO();
        yAxis.setValueFormatString("USD 0");
        yAxis.setTitle("USD Reales");
        dto.setAxisY(yAxis);

        List<CanvasJSDatumDTO> seriesList = new ArrayList<>(1);
        dto.setData(seriesList);

        final List<CanvasJSDatapointDTO> datapoints = new ArrayList<>();
        historicGold.forEach(new CanvasJSMoneyAmountProcessor(datapoints));
        seriesList.add(this.getDatum("area", this.colors.get(0), "Oro", datapoints));
        return dto;
    }

}
