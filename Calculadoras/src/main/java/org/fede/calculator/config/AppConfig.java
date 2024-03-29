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
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toList;
import java.util.stream.Stream;
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
import org.fede.calculator.service.RealUSDCanvasJSDatapointAssembler;
import org.fede.calculator.service.USDMoneyService;
import org.fede.calculator.web.dto.ExpenseChartSeriesDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import static java.util.Collections.unmodifiableMap;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
@Configuration
public class AppConfig {

    public static <K, V> Map.Entry<K, V> entry(K key, V value) {
        return new AbstractMap.SimpleEntry<>(key, value);
    }

    public static <K, U> Collector<Map.Entry<K, U>, ?, Map<K, U>> entriesToLinkedHashMap() {
        return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1, LinkedHashMap::new);
    }

    @Bean
    public Map<String, Integer> monthlyPeriod() {
        return unmodifiableMap(
                Stream.of(
                        entry("mensual", 1),
                        entry("bimestral", 2),
                        entry("trimestral", 3),
                        entry("semestral", 6),
                        entry("anual", 12),
                        entry("bienal", 24))
                        .collect(entriesToLinkedHashMap()));
    }

    @Bean
    public List<ExpenseChartSeriesDTO> expenseSeries() {
        return Stream.of(
                new ExpenseChartSeriesDTO("Cablevisión", "expense/cablevision.json"),
                new ExpenseChartSeriesDTO("Celular A.", "expense/celular-a.json"),
                new ExpenseChartSeriesDTO("Celular F.", "expense/celular-f.json"),
                new ExpenseChartSeriesDTO("Contadora", "expense/contadora.json"),
                new ExpenseChartSeriesDTO("BB.PP.", "expense/bbpp.json"),
                new ExpenseChartSeriesDTO("Gas", "expense/gas.json"),
                new ExpenseChartSeriesDTO("Inmobiliario", "expense/inmobiliario-43.json"),
                new ExpenseChartSeriesDTO("IOMA", "expense/ioma.json"),
                new ExpenseChartSeriesDTO("Luz", "expense/luz.json"),
                new ExpenseChartSeriesDTO("Municipal", "expense/municipal-43.json"),
                new ExpenseChartSeriesDTO("Seguro", "expense/seguro.json"),
                new ExpenseChartSeriesDTO("Teléfono", "expense/telefono-43.json"),
                new ExpenseChartSeriesDTO("Emergencia", "expense/emergencia.json"),
                new ExpenseChartSeriesDTO("Expensas", "expense/expensas.json"),
                new ExpenseChartSeriesDTO("Reparaciones", "expense/reparaciones.json"),
                new ExpenseChartSeriesDTO("XBOX", "expense/xbox.json"),
                new ExpenseChartSeriesDTO("Netflix", "expense/netflix.json"),
                new ExpenseChartSeriesDTO("Viajes", "expense/viajes.json"),
                new ExpenseChartSeriesDTO("Limpieza", "expense/limpieza.json"),
                new ExpenseChartSeriesDTO("Monotributo-A", "expense/monotributo-angeles.json"),
                new ExpenseChartSeriesDTO("Total", "")
        ).collect(toList());
    }

    @Bean
    public List<ExpenseChartSeriesDTO> consortiumExpenseSeries() {
        return Stream.of(
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
        ).collect(toList());

    }

    @Bean
    public List<ExpenseChartSeriesDTO> incomesSeries() {
        return Stream.of(
                new ExpenseChartSeriesDTO("UNLP", "income/unlp.json"),
                new ExpenseChartSeriesDTO("LIFIA", "income/lifia.json"),
                new ExpenseChartSeriesDTO("Despegar", "income/despegar.json"),
                new ExpenseChartSeriesDTO("Despegar-split", "income/despegar-split.json"),
                new ExpenseChartSeriesDTO("Total", "")
        ).collect(toList());
    }

    @Bean
    public List<ExpenseChartSeriesDTO> savingsSeries() {
        return Stream.of(
                new ExpenseChartSeriesDTO("Peso", "saving/ahorros-peso.json"),
                new ExpenseChartSeriesDTO("Dolar Liq.", "saving/ahorros-dolar-liq.json"),
                new ExpenseChartSeriesDTO("Dolar Banco", "saving/ahorros-dolar-banco.json"),
                new ExpenseChartSeriesDTO("€", "saving/ahorros-euro.json"),
                new ExpenseChartSeriesDTO("DAI", "saving/ahorros-dai.json"),
                new ExpenseChartSeriesDTO("Dolar ON", "saving/ahorros-dolar-ON.json"),
                new ExpenseChartSeriesDTO("Oro", "saving/ahorros-oro.json"),
                new ExpenseChartSeriesDTO("Acciones Argentina FCI", "saving/ahorros-conaafa.json"),
                new ExpenseChartSeriesDTO("LETE", "saving/ahorros-lete.json"),
                new ExpenseChartSeriesDTO("LECAP", "saving/ahorros-lecap.json"),
                new ExpenseChartSeriesDTO("AY24", "saving/ahorros-ay24.json"),
                new ExpenseChartSeriesDTO("CSPX", "saving/ahorros-cspx.json"),
                new ExpenseChartSeriesDTO("EIMI", "saving/ahorros-eimi.json"),
                new ExpenseChartSeriesDTO("XRSU", "saving/ahorros-xrsu.json"),
                new ExpenseChartSeriesDTO("MEUD", "saving/ahorros-meud.json"),
                new ExpenseChartSeriesDTO("Balance Fund FCI", "saving/ahorros-conbala.json"),
                new ExpenseChartSeriesDTO("Ahorro Plus FCI", "saving/ahorros-caplusa.json"),
                new ExpenseChartSeriesDTO("UVA", "saving/ahorros-uva.json"),
                new ExpenseChartSeriesDTO("Total", "")
        ).collect(toList());

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
    public CanvasJSDatapointAssembler realUSDDatapointAssembler() {
        return new RealUSDCanvasJSDatapointAssembler();
    }

    @Bean
    public List<String> colors() {
        final var colors = Stream.of(
                "#00ffcc", "#262b4d", "#7f0000", "#000033", "#330700",
                "#1d734b", "#b32d98",
                "#734139", "#bfd0ff", "#ff4400", "#7f3300",
                "#ffb380", "#33210d", "#ffaa00",
                "#8c5e00", "#736039", "#ffe680",
                "#eeff00", "#838c00", "#919973",
                "#3b5900", "#8cff40", "#60bf6c",
                "#003329", "#ff9180",
                "#60bfb9", "#00ccff", "#005c73",
                "#0077b3", "#0088ff", "#4d5766",
                "#362699", "#ff0044",
                "#7960bf", "#6600ff", "#ee00ff",
                "#ffbffb", "#66335c",
                "#ff0088", "#33001b", "#8c6977",
                "#731d34", "#ffbfc8")
                .collect(toList());

        Collections.shuffle(colors);

        return colors;
    }

    @Bean
    public Map<Integer, String> monthNames() {
        return Collections.unmodifiableMap(Stream.of(
                entry(1, "enero"),
                entry(2, "febrero"),
                entry(3, "marzo"),
                entry(4, "abril"),
                entry(5, "mayo"),
                entry(6, "junio"),
                entry(7, "julio"),
                entry(8, "agosto"),
                entry(9, "septiembre"),
                entry(10, "octubre"),
                entry(11, "noviembre"),
                entry(12, "diciembre")).
                collect(entriesToLinkedHashMap()));

    }

    @Bean
    public List<String> investments() {
        return Collections.singletonList("investments.json");
    }

    @Bean
    public MultiSeriesChartService savingsService() {
        return new CanvasJSMultiSeriesChartService(
                this.realUSDDatapointAssembler(),
                this.nominalPesosDatapointAssembler(),
                this.incomesSeries(),
                this.savingsSeries(),
                this.colors());
    }

    @Bean
    public MultiSeriesChartService consortiumExpensesService() {
        return new CanvasJSMultiSeriesChartService(
                this.realUSDDatapointAssembler(),
                this.nominalPesosDatapointAssembler(),
                this.incomesSeries(),
                this.consortiumExpenseSeries(),
                this.colors());
    }

    @Bean
    public MultiSeriesChartService expensesService() {
        return new CanvasJSMultiSeriesChartService(
                this.realUSDDatapointAssembler(),
                this.nominalPesosDatapointAssembler(),
                this.incomesSeries(),
                this.expenseSeries(),
                this.colors());
    }

    @Bean
    public MultiSeriesChartService incomesService() {
        return new CanvasJSMultiSeriesChartService(
                this.realUSDDatapointAssembler(),
                this.nominalPesosDatapointAssembler(),
                this.incomesSeries(),
                this.incomesSeries(),
                this.colors());
    }

    @Bean
    public Map<String, List<String>> savingsReportSeries() {
        Map<String, List<String>> map = new HashMap<>();
        map.put("ars", Arrays.asList(new String[]{
            "saving/ahorros-conaafa.json",
            "saving/ahorros-conbala.json",
            "saving/ahorros-peso.json",
            "saving/ahorros-caplusa.json",
            "saving/ahorros-uva.json",
            "saving/ahorros-lecap.json"
        }));
        map.put("usd", Arrays.asList(new String[]{
            "saving/ahorros-lete.json",
            "saving/ahorros-ay24.json",
            "saving/ahorros-cspx.json",
            "saving/ahorros-eimi.json",
            "saving/ahorros-xrsu.json",
            "saving/ahorros-meud.json",
            "saving/ahorros-dolar-banco.json",
            "saving/ahorros-euro.json",
            "saving/ahorros-dai.json",
            "saving/ahorros-dolar-ON.json",
            "saving/ahorros-dolar-liq.json",
            "saving/ahorros-oro.json"}));
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
    public Map<String, MoneyService> moneyServices() {
        Map<String, MoneyService> map = new HashMap<>();
        map.put("USD", this.usdMoneyService());
        return map;
    }

    @Bean
    public ChartService canvasJSChartService() {
        return new CanvasJSChartService(
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
    public DigitalContentService digitalContentService() throws ParseException {
        return new LazyDigitalContentService();
    }

}
