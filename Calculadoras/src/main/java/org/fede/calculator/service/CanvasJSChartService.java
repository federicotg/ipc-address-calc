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
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import static org.fede.calculator.money.Inflation.USD_INFLATION;
import static org.fede.calculator.money.Inflation.ARS_INFLATION;
import static org.fede.calculator.money.ForeignExchange.USD_ARS;
import static org.fede.calculator.money.ForeignExchange.USD_XAU;
import org.fede.calculator.money.Inflation;
import org.fede.calculator.money.MathConstants;
import static org.fede.calculator.money.MathConstants.CONTEXT;
import static org.fede.calculator.money.series.JSONMoneyAmountSeries.readSeries;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.NoSeriesDataFoundException;
import org.fede.calculator.money.SimpleAggregation;
import org.fede.calculator.money.series.JSONMoneyAmountSeries;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.MoneyAmountProcessor;
import org.fede.calculator.money.series.YearMonth;
import org.fede.calculator.web.dto.CanvasJSAxisDTO;
import org.fede.calculator.web.dto.CanvasJSChartDTO;
import org.fede.calculator.web.dto.CanvasJSDatapointDTO;
import org.fede.calculator.web.dto.CanvasJSDatumDTO;
import org.fede.calculator.web.dto.CanvasJSTitleDTO;
import org.fede.calculator.web.dto.ExpenseChartSeriesDTO;
import org.springframework.context.annotation.Lazy;

import org.springframework.stereotype.Service;

/**
 *
 * @author fede
 */
@Service
@Lazy
public class CanvasJSChartService implements ChartService, MathConstants {

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

    @Resource(name = "realPesosDatapointAssembler")
    @Lazy
    private CanvasJSDatapointAssembler realPesosDatapointAssembler;

    @Resource(name = "nominalPesosDatapointAssembler")
    @Lazy
    private CanvasJSDatapointAssembler nominalPesosDatapointAssembler;

    @Resource(name = "realUSDDatapointAssembler")
    @Lazy
    private CanvasJSDatapointAssembler realUSDDatapointAssembler;

    @Resource(name = "nominalUSDDatapointAssembler")
    @Lazy
    private CanvasJSDatapointAssembler nominalUSDDatapointAssembler;

    @Resource(name = "realEURDatapointAssembler")
    @Lazy
    private CanvasJSDatapointAssembler realEURDatapointAssembler;

    @Resource(name = "nominalEURDatapointAssembler")
    @Lazy
    private CanvasJSDatapointAssembler nominalEURDatapointAssembler;

    @Resource(name = "incomesSeries")
    private List<ExpenseChartSeriesDTO> incomeSeries;
    
    @Resource(name = "savingsSeries")
    private List<ExpenseChartSeriesDTO> savingsSeries;
    

    @Override
    public CanvasJSChartDTO combinedIncomes(int months, boolean pn, boolean pr, boolean dn, boolean dr, boolean en, boolean er,
            int year, int month, List<String> seriesNames) throws NoSeriesDataFoundException {

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
    public CanvasJSChartDTO savings(boolean pn, boolean pr, boolean dn, boolean dr, boolean en, boolean er, int year, int month) throws NoSeriesDataFoundException {
        MoneyAmountSeries ars = readSeries("ahorros-peso.json");
        MoneyAmountSeries usd = USD_ARS.exchange(readSeries("ahorros-dolar.json"), Currency.getInstance("ARS"));
        MoneyAmountSeries gold = USD_ARS.exchange(
                USD_XAU.exchange(readSeries("ahorros-oro.json"), Currency.getInstance("USD")),
                Currency.getInstance("ARS"));
        return this.createCombinedChart("Ahorros",
                usd.add(ars).add(gold),
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
            seriesList.add(this.getDatum("line", "blue", "Pesos Reales", this.realPesosDatapointAssembler.getDatapoints(months, series, year, month)));
        }
        if (pn) {
            seriesList.add(this.getDatum("line", "red", "Pesos Nominales", this.nominalPesosDatapointAssembler.getDatapoints(months, series)));
        }
        if (dr) {
            seriesList.add(this.getDatum("line", "green", "USD Reales", this.realUSDDatapointAssembler.getDatapoints(months, series, year, month)));
        }
        if (dn) {
            seriesList.add(this.getDatum("line", "black", "USD Nominales", this.nominalUSDDatapointAssembler.getDatapoints(months, series)));
        }

        if (er) {
            seriesList.add(this.getDatum("line", "gold", "EUR Reales", this.realEURDatapointAssembler.getDatapoints(months, series, year, month)));
        }
        if (en) {
            seriesList.add(this.getDatum("line", "pink", "EUR Nominales", this.nominalEURDatapointAssembler.getDatapoints(months, series)));
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

    /**
     * 
     * @param dtos
     * @return 
     */
    private MoneyAmountSeries sumSeries(List<ExpenseChartSeriesDTO> dtos) throws NoSeriesDataFoundException{
        
        List<String> seriesNames = new ArrayList<>(dtos.size());
        for(ExpenseChartSeriesDTO dto : dtos){
            seriesNames.add(dto.getSeriesName());
        }
        return JSONMoneyAmountSeries.sumSeries(seriesNames.toArray(new String[seriesNames.size()]));
        
    }
    
    @Override
    public CanvasJSChartDTO goldIncomeAndSavings() throws NoSeriesDataFoundException {

        Currency xau = Currency.getInstance("XAU");
        Currency usd = Currency.getInstance("USD");
        
        /*MoneyAmountSeries arsSavings = USD_XAU.exchange(USD_ARS.exchange(readSeries("ahorros-peso.json"), usd), xau);
        MoneyAmountSeries usdSavings = USD_XAU.exchange(readSeries("ahorros-dolar.json"), xau);
        MoneyAmountSeries goldSavings = readSeries("ahorros-oro.json");*/
        
        MoneyAmountSeries savings = USD_XAU.exchange(this.sumSeries(this.savingsSeries), xau);
        
        /*MoneyAmountSeries income
                = USD_XAU.exchange(
                        USD_ARS.exchange(
                                readSeries("unlp.json").add(readSeries("lifia.json")).add(readSeries("plazofijo.json")),
                                usd), xau);*/
        
        MoneyAmountSeries income = USD_XAU.exchange(USD_ARS.exchange(this.sumSeries(this.incomeSeries), usd), xau);

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
        seriesList.add(this.getDatum("line", "gold", "Ahorros", datapoints));

        datapoints = new ArrayList<>();
        income.forEach(new CanvasJSMoneyAmountProcessor(datapoints));
        seriesList.add(this.getDatum("line", "orange", "Ingresos", datapoints));

        return dto;

    }

    @Override
    public CanvasJSChartDTO savedSalaries() throws NoSeriesDataFoundException {

        Currency usd = Currency.getInstance("USD");
        MoneyAmountSeries income = new SimpleAggregation(12).average(USD_ARS.exchange(sumSeries(this.incomeSeries), usd));

        //MoneyAmountSeries gold = USD_XAU.exchange(readSeries("ahorros-oro.json"), Currency.getInstance("USD"));

        //final MoneyAmountSeries savings = gold.add(USD_ARS.exchange(readSeries("ahorros-peso.json"), usd).add(readSeries("ahorros-dolar.json")));
        final MoneyAmountSeries savings = this.sumSeries(this.savingsSeries);

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
        income.forEachNonZero(new MoneyAmountProcessor() {

            @Override
            public void process(int year, int month, MoneyAmount sueldo) throws NoSeriesDataFoundException {
                YearMonth ym = new YearMonth(year, month);
                if (ym.compareTo(savings.getFrom()) >= 0 && ym.compareTo(savings.getTo()) <= 0) {
                    CanvasJSDatapointDTO dataPoint = new CanvasJSDatapointDTO(
                            "date-".concat(String.valueOf(year)).concat("-").concat(String.valueOf(month - 1)).concat("-15"),
                            savings.getAmount(year, month).getAmount().divide(sueldo.getAmount(), CONTEXT)
                    );
                    datapoints.add(dataPoint);
                }
            }
        });
        seriesList.add(this.getDatum("line", "red", "Sueldos", datapoints));

        return dto;

    }

    @Override
    public CanvasJSChartDTO hisotricDollar() throws NoSeriesDataFoundException {
        YearMonth latestData = Inflation.USD_INFLATION.getTo();
        final int todayMonth = latestData.getMonth();
        final int todayYear = latestData.getYear();
        final MoneyAmount oneDollar = new MoneyAmount(BigDecimal.ONE, "USD");
        final Currency ars = Currency.getInstance("ARS");

        final MoneyAmountSeries historicDollar = ARS_INFLATION.adjust(
                USD_ARS.exchange(
                        USD_INFLATION.adjust(oneDollar, todayYear, todayMonth), ars), todayYear, todayMonth);

        CanvasJSChartDTO dto = new CanvasJSChartDTO();
        CanvasJSTitleDTO title = new CanvasJSTitleDTO("Dólar en Pesos de " + MONTH_NAMES.get(todayMonth) + "/" + todayYear);
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
        seriesList.add(this.getDatum("area", "darkgreen", "Dólar", datapoints));
        return dto;
    }

    @Override
    public CanvasJSChartDTO historicGold() throws NoSeriesDataFoundException {

        final YearMonth latestUSDCPIData = USD_INFLATION.getTo();

        final int todayMonth = latestUSDCPIData.getMonth();
        final int todayYear = latestUSDCPIData.getYear();

        final Currency usd = Currency.getInstance("USD");
        final MoneyAmount oneTroyOunce = new MoneyAmount(BigDecimal.ONE, "XAU");

        final MoneyAmountSeries historicGold = USD_INFLATION.adjust(USD_XAU.exchange(oneTroyOunce, usd), todayYear, todayMonth);

        CanvasJSChartDTO dto = new CanvasJSChartDTO();
        CanvasJSTitleDTO title = new CanvasJSTitleDTO("Onza Troy en USD de " + MONTH_NAMES.get(todayMonth) + " / " + todayYear);
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
        seriesList.add(this.getDatum("area", "gold", "Oro", datapoints));
        return dto;
    }

}
