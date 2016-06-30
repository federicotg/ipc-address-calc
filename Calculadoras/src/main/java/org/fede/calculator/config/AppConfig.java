/*
 * Copyright (C) 2016 Federico Tello Gentile <federicotg@gmail.com>
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
package org.fede.calculator.config;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.fede.calculator.service.ARGMoneyService;
import org.fede.calculator.service.CanvasJSChartService;
import org.fede.calculator.service.CanvasJSDatapointAssembler;
import org.fede.calculator.service.CanvasJSMultiSeriesChartService;
import org.fede.calculator.service.ChartService;
import org.fede.calculator.service.DigitalContentService;
import org.fede.calculator.service.InvestmentService;
import org.fede.calculator.service.InvestmentServiceImpl;
import org.fede.calculator.service.LaPlataAddressService;
import org.fede.calculator.service.LaPlataAddressServiceImpl;
import org.fede.calculator.service.LazyDigitalContentService;
import org.fede.calculator.service.MoneyService;
import org.fede.calculator.service.MultiSeriesChartService;
import org.fede.calculator.service.NominalCanvasJSDatapointAssembler;
import org.fede.calculator.service.RealEURCanvasJSDatapointAssembler;
import org.fede.calculator.service.RealPesosCanvasJSDatapointAssembler;
import org.fede.calculator.service.RealUSDCanvasJSDatapointAssembler;
import org.fede.calculator.service.USDMoneyService;
import org.fede.calculator.web.dto.ExpenseChartSeriesDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
@Configuration
public class AppConfig {

    @Bean
    public Map<String, Integer> monthlyPeriod() {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("mensual", 1);
        map.put("bimestral", 2);
        map.put("trimestral", 3);
        map.put("semestral", 6);
        map.put("anual", 12);
        map.put("bienal", 24);
        return map;
    }

    @Bean
    public List<ExpenseChartSeriesDTO> expenseSeries() {
        return Arrays.asList(new ExpenseChartSeriesDTO[]{
            new ExpenseChartSeriesDTO("Cablevisión", "expense/cablevision.json"),
            new ExpenseChartSeriesDTO("Celular A.", "expense/celular-a.json"),
            new ExpenseChartSeriesDTO("Celular F.", "expense/celular-f.json"),
            new ExpenseChartSeriesDTO("Contadora", "expense/contadora.json"),
            new ExpenseChartSeriesDTO("Gas", "expense/gas.json"),
            new ExpenseChartSeriesDTO("Inmobiliario", "expense/inmobiliario-43.json"),
            new ExpenseChartSeriesDTO("IOMA", "expense/ioma.json"),
            new ExpenseChartSeriesDTO("Luz", "expense/luz.json"),
            new ExpenseChartSeriesDTO("Municipal", "expense/municipal-43.json"),
            new ExpenseChartSeriesDTO("Seguro", "expense/seguro.json"),
            new ExpenseChartSeriesDTO("Teléfono", "expense/telefono-43.json"),
            new ExpenseChartSeriesDTO("UDEC", "expense/udec.json"),
            new ExpenseChartSeriesDTO("Expensas", "expense/expensas.json"),
            new ExpenseChartSeriesDTO("XBOX", "expense/xbox.json"),
            new ExpenseChartSeriesDTO("Total", "")
        });
    }

    @Bean
    public List<ExpenseChartSeriesDTO> consortiumExpenseSeries() {
        return Arrays.asList(new ExpenseChartSeriesDTO[]{
            new ExpenseChartSeriesDTO("Administración", "expense/consorcio-administracion.json"),
            new ExpenseChartSeriesDTO("Ascensor", "expense/consorcio-ascensor.json"),
            new ExpenseChartSeriesDTO("Bomba", "expense/consorcio-bomba.json"),
            new ExpenseChartSeriesDTO("Limpieza", "expense/consorcio-limpieza.json"),
            new ExpenseChartSeriesDTO("Luz", "expense/consorcio-luz.json"),
            new ExpenseChartSeriesDTO("Matafuegos", "expense/consorcio-matafuegos.json"),
            new ExpenseChartSeriesDTO("Reparaciones", "expense/consorcio-reparaciones.json"),
            new ExpenseChartSeriesDTO("Seguro", "expense/consorcio-seguros.json"),
            new ExpenseChartSeriesDTO("Agua", "expense/absa.json"),
            new ExpenseChartSeriesDTO("Gasto Adm.", "expense/consorcio-gasto-administrativo.json"),
            new ExpenseChartSeriesDTO("Total", "")
        });

    }

    @Bean
    public List<ExpenseChartSeriesDTO> incomesSeries() {
        return Arrays.asList(new ExpenseChartSeriesDTO[]{
            new ExpenseChartSeriesDTO("UNLP", "income/unlp.json"),
            new ExpenseChartSeriesDTO("LIFIA", "income/lifia.json"),
            new ExpenseChartSeriesDTO("Despegar", "income/despegar.json"),
            new ExpenseChartSeriesDTO("Total", "")
        });
    }

    @Bean
    public List<ExpenseChartSeriesDTO> savingsSeries() {
        return Arrays.asList(new ExpenseChartSeriesDTO[]{
            new ExpenseChartSeriesDTO("Peso", "saving/ahorros-peso.json"),
            new ExpenseChartSeriesDTO("Dolar", "saving/ahorros-dolar.json"),
            new ExpenseChartSeriesDTO("Oro", "saving/ahorros-oro.json"),
            new ExpenseChartSeriesDTO("Acciones Argentina FCI", "saving/ahorros-conaafa.json"),
            new ExpenseChartSeriesDTO("Balance Fund FCI", "saving/ahorros-conbala.json"),
            new ExpenseChartSeriesDTO("Total", "")
        });

    }

    @Bean
    public List<ExpenseChartSeriesDTO> fciSeries() {
        return Arrays.asList(new ExpenseChartSeriesDTO[]{
            new ExpenseChartSeriesDTO("Ahorro Plus Argentina FCI", "fci/CAPLUSA.json"),
            new ExpenseChartSeriesDTO("Renta Nacional FCI", "fci/CRentaNacionalA.json"),
            new ExpenseChartSeriesDTO("Abierto Pymes FCI", "fci/CPYMESA.json"),
            new ExpenseChartSeriesDTO("Renta Fija Argentina FCI", "fci/CAHORROA.json"),
            new ExpenseChartSeriesDTO("Balance Fund FCI", "fci/CBAL01.json"),
            new ExpenseChartSeriesDTO("Deuda Argentina FCI", "fci/CDeudaA.json"),
            new ExpenseChartSeriesDTO("Acciones Argentina FCI", "fci/CGRO01.json"),
            new ExpenseChartSeriesDTO("Renta Variable FCI", "fci/CRVariable.json")
        });

    }

    @Bean
    public CanvasJSDatapointAssembler nominalUSDDatapointAssembler() {
        return new NominalCanvasJSDatapointAssembler("USD");
    }

    @Bean
    public CanvasJSDatapointAssembler nominalPesosDatapointAssembler() {
        return new NominalCanvasJSDatapointAssembler("ARS");
    }

    @Bean
    public CanvasJSDatapointAssembler nominalEURDatapointAssembler() {
        return new NominalCanvasJSDatapointAssembler("EUR");
    }

    @Bean
    public CanvasJSDatapointAssembler realEURDatapointAssembler() {
        return new RealEURCanvasJSDatapointAssembler();
    }

    @Bean
    public CanvasJSDatapointAssembler realPesosDatapointAssembler() {
        return new RealPesosCanvasJSDatapointAssembler();
    }

    @Bean
    public CanvasJSDatapointAssembler realUSDDatapointAssembler() {
        return new RealUSDCanvasJSDatapointAssembler();
    }

    @Bean
    public List<String> colors() {
        return Arrays.asList(new String[]{
            "#000000",
            "#ff0000",
            "#00ff00",
            "#0000ff",
            "#cccccc",
            "#ff00ff",
            "#660066",
            "#009999",
            "#cccc00",
            "#ff6600",
            "#339966",
            "#6600cc",
            "#00ccff",
            "#666699",
            "#ffccff",
            "#993300",
            "#006600",
            "#ff9999"}
        );
    }

    @Bean
    public Map<Integer, String> monthNames() {

        Map<Integer, String> map = new HashMap<>(12, 1.0f);
        map.put(1, "enero");
        map.put(2, "febrero");
        map.put(3, "marzo");
        map.put(4, "abril");
        map.put(5, "mayo");
        map.put(6, "junio");
        map.put(7, "julio");
        map.put(8, "agosto");
        map.put(9, "septiembre");
        map.put(10, "octubre");
        map.put(11, "noviembre");
        map.put(12, "diciembre");
        return map;
    }

    @Bean
    public List<String> investments() {
        return Collections.singletonList("investments.json");
    }

    @Bean
    public MultiSeriesChartService savingsService() {
        return new CanvasJSMultiSeriesChartService(
                this.realPesosDatapointAssembler(),
                this.realUSDDatapointAssembler(),
                this.nominalPesosDatapointAssembler(),
                this.incomesSeries(),
                this.savingsSeries(),
                this.colors());
    }

    @Bean
    public MultiSeriesChartService consortiumExpensesService() {
        return new CanvasJSMultiSeriesChartService(
                this.realPesosDatapointAssembler(),
                this.realUSDDatapointAssembler(),
                this.nominalPesosDatapointAssembler(),
                this.incomesSeries(),
                this.consortiumExpenseSeries(),
                this.colors());
    }

    @Bean
    public MultiSeriesChartService expensesService() {
        return new CanvasJSMultiSeriesChartService(
                this.realPesosDatapointAssembler(),
                this.realUSDDatapointAssembler(),
                this.nominalPesosDatapointAssembler(),
                this.incomesSeries(),
                this.expenseSeries(),
                this.colors());
    }

    @Bean
    public MultiSeriesChartService incomesService() {
        return new CanvasJSMultiSeriesChartService(
                this.realPesosDatapointAssembler(),
                this.realUSDDatapointAssembler(),
                this.nominalPesosDatapointAssembler(),
                this.incomesSeries(),
                this.incomesSeries(),
                this.colors());
    }

    @Bean
    public MultiSeriesChartService fciService() {
        return new CanvasJSMultiSeriesChartService(
                this.realPesosDatapointAssembler(),
                this.realUSDDatapointAssembler(),
                this.nominalPesosDatapointAssembler(),
                this.incomesSeries(),
                this.fciSeries(),
                this.colors());
    }

    @Bean
    public Map<String, List<String>> savingsReportSeries() {
        Map<String, List<String>> map = new HashMap<>();
        map.put("ars", Arrays.asList(new String[]{"saving/ahorros-conaafa.json", "saving/ahorros-conbala.json", "saving/ahorros-peso.json"}));
        map.put("usd", Arrays.asList(new String[]{"saving/ahorros-dolar.json", "saving/ahorros-oro.json"}));
        map.put("fx", Arrays.asList(new String[]{"index/peso-dolar-libre.json"}));
        return map;
    }

    @Bean
    public InvestmentService investmentService() {
        return new InvestmentServiceImpl(
                this.incomesSeries(),
                this.investments(),
                this.savingsReportSeries());
    }

    @Bean
    public MoneyService usdMoneyService() {
        return new USDMoneyService();
    }

    @Bean
    public MoneyService argMoneyService() {
        return new ARGMoneyService();
    }

    @Bean
    public Map<String, MoneyService> moneyServices() {
        Map<String, MoneyService> map = new HashMap<>();
        map.put("USD", this.usdMoneyService());
        map.put("ARS", this.argMoneyService());
        return map;
    }

    @Bean
    public ChartService canvasJSChartService() {
        return new CanvasJSChartService(
                this.realPesosDatapointAssembler(),
                this.nominalPesosDatapointAssembler(),
                this.realUSDDatapointAssembler(),
                this.nominalUSDDatapointAssembler(),
                this.realEURDatapointAssembler(),
                this.nominalEURDatapointAssembler(),
                this.incomesSeries(),
                this.savingsSeries(),
                this.colors(),
                this.monthNames());
    }

    @Bean
    public LaPlataAddressService laPlataAddressService() {
        return new LaPlataAddressServiceImpl();
    }

    @Bean
    DigitalContentService digitalContentService() throws ParseException {
        return new LazyDigitalContentService();
    }

}
