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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Resource;
import static org.fede.calculator.money.Inflation.USD_INFLATION;
import static org.fede.calculator.money.Inflation.ARS_INFLATION;
import static org.fede.calculator.money.ForeignExchange.USD_ARS;
import static org.fede.calculator.money.ForeignExchange.USD_XAU;
import org.fede.calculator.money.MathConstants;
import static org.fede.calculator.money.MathConstants.CONTEXT;
import static org.fede.calculator.money.series.JSONMoneyAmountSeries.readSeries;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.NoSeriesDataFoundException;
import org.fede.calculator.money.SimpleAggregation;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.MoneyAmountProcessor;
import org.fede.calculator.money.series.YearMonth;
import org.fede.calculator.web.dto.CanvasJSAxisDTO;
import org.fede.calculator.web.dto.CanvasJSChartDTO;
import org.fede.calculator.web.dto.CanvasJSDatapointDTO;
import org.fede.calculator.web.dto.CanvasJSDatumDTO;
import org.fede.calculator.web.dto.CanvasJSTitleDTO;
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
    
    @Resource(name = "realPesosDatapointAssembler") @Lazy
    private CanvasJSDatapointAssembler realPesosDatapointAssembler;

    @Resource(name = "nominalPesosDatapointAssembler") @Lazy
    private CanvasJSDatapointAssembler nominalPesosDatapointAssembler;

    @Resource(name = "realUSDDatapointAssembler") @Lazy
    private CanvasJSDatapointAssembler realUSDDatapointAssembler;

    @Resource(name = "nominalUSDDatapointAssembler") @Lazy
    private CanvasJSDatapointAssembler nominalUSDDatapointAssembler;


    @Override
    public CanvasJSChartDTO unlp(int months, boolean pn, boolean pr, boolean dn, boolean dr, int year, int month) throws NoSeriesDataFoundException {
        return this.createCombinedChart(
                "Sueldo UNLP",
                readSeries("unlp.json"), months, pn, pr, dn, dr, year, month);
    }

    @Override
    public CanvasJSChartDTO lifia(int months, boolean pn, boolean pr, boolean dn, boolean dr, int year, int month) throws NoSeriesDataFoundException {
        return this.createCombinedChart(
                "Sueldo LIFIA",
                readSeries("lifia.json"), months, pn, pr, dn, dr, year, month);
    }

    @Override
    public CanvasJSChartDTO interest(int months, boolean pn, boolean pr, boolean dn, boolean dr, int year, int month) throws NoSeriesDataFoundException {
        return this.createCombinedChart(
                "Plazo Fijo",
                readSeries("plazofijo.json"), months, pn, pr, dn, dr, year, month);
    }

    @Override
    public CanvasJSChartDTO lifiaAndUnlp(int months, boolean pn, boolean pr, boolean dn, boolean dr, int year, int month) throws NoSeriesDataFoundException {
        return this.createCombinedChart("Sueldo LIFIA + UNLP",
                readSeries("unlp.json").add(readSeries("lifia.json")),
                months, pn, pr, dn, dr, year,month);
    }

    @Override
    public CanvasJSChartDTO lifiaUnlpAndInterest(int months, boolean pn, boolean pr, boolean dn, boolean dr,
            int year, int month) throws NoSeriesDataFoundException {
        return this.createCombinedChart("Sueldo LIFIA + UNLP + Plazo Fijo",
                readSeries("unlp.json").add(readSeries("lifia.json").add(readSeries("plazofijo.json"))),
                months, pn, pr, dn, dr, year, month);
    }

    @Override
    public CanvasJSChartDTO savings(boolean pn, boolean pr, boolean dn, boolean dr, int year, int month) throws NoSeriesDataFoundException {
        MoneyAmountSeries ars = readSeries("ahorros-peso.json");
        MoneyAmountSeries usd = USD_ARS.exchange(readSeries("ahorros-dolar.json"), Currency.getInstance("ARS"));
        return this.createCombinedChart("Ahorros",
                usd.add(ars),
                1, pn, pr, dn, dr,
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

        List<CanvasJSDatumDTO> seriesList = new ArrayList<>(4);
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

        Currency xau = Currency.getInstance("XAU");
        Currency usd = Currency.getInstance("USD");
        MoneyAmountSeries ars = USD_XAU.exchange(USD_ARS.exchange(readSeries("ahorros-peso.json"), usd), xau);
        MoneyAmountSeries usdSavings = USD_XAU.exchange(readSeries("ahorros-dolar.json"), xau);
        MoneyAmountSeries income
                = USD_XAU.exchange(
                        USD_ARS.exchange(
                                readSeries("unlp.json").add(readSeries("lifia.json")).add(readSeries("plazofijo.json")),
                                usd), xau);

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
        ars.add(usdSavings).forEach(new CanvasJSMoneyAmountProcessor(datapoints));
        seriesList.add(this.getDatum("line", "gold", "Ahorros", datapoints));

        datapoints = new ArrayList<>();
        income.forEach(new CanvasJSMoneyAmountProcessor(datapoints));
        seriesList.add(this.getDatum("line", "orange", "Ingresos", datapoints));

        return dto;

    }

    @Override
    public CanvasJSChartDTO savedSalaries() throws NoSeriesDataFoundException {
        Currency usd = Currency.getInstance("USD");
        MoneyAmountSeries income = new SimpleAggregation(12).average(USD_ARS.exchange(
                readSeries("unlp.json").add(readSeries("lifia.json")).add(readSeries("plazofijo.json")),
                usd));

        final MoneyAmountSeries savings = USD_ARS.exchange(readSeries("ahorros-peso.json"), usd).add(readSeries("ahorros-dolar.json"));

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
                            "date-".concat(String.valueOf(year)).concat("-").concat(String.valueOf(month - 1)).concat("-28"),
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
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -2);
        final int todayMonth = cal.get(Calendar.MONTH) + 1;
        final int todayYear = cal.get(Calendar.YEAR);

        final MoneyAmount oneDollar = new MoneyAmount(BigDecimal.ONE, "USD");
        final Currency ars = Currency.getInstance("ARS");

        final MoneyAmountSeries historicDollar = ARS_INFLATION.adjust(
                USD_ARS.exchange(
                        USD_INFLATION.adjust(oneDollar, todayYear, todayMonth), ars), todayYear, todayMonth);
        DateFormat df = new SimpleDateFormat("MMM/YYYY");
        CanvasJSChartDTO dto = new CanvasJSChartDTO();
        CanvasJSTitleDTO title = new CanvasJSTitleDTO("Dólar en Pesos de " + df.format(cal.getTime()));
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
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -2);
        final int todayMonth = cal.get(Calendar.MONTH) + 1;
        final int todayYear = cal.get(Calendar.YEAR);

        final Currency usd = Currency.getInstance("USD");
        final MoneyAmount oneTroyOunce = new MoneyAmount(BigDecimal.ONE, "XAU");

        final MoneyAmountSeries historicGold = USD_INFLATION.adjust(USD_XAU.exchange(oneTroyOunce, usd), todayYear, todayMonth);

        DateFormat df = new SimpleDateFormat("MMM/YYYY", Locale.forLanguageTag("es-AR"));
        CanvasJSChartDTO dto = new CanvasJSChartDTO();
        CanvasJSTitleDTO title = new CanvasJSTitleDTO("Onza Troy en USD de " + df.format(cal.getTime()));
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
