/*
 * Copyright (C) 2021 federicogentile
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
package org.fede.calculator.money;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import java.util.stream.Stream;
import static org.fede.calculator.money.Inflation.USD_INFLATION;
import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.SeriesReader;
import static org.fede.calculator.money.series.SeriesReader.readSeries;
import org.fede.util.Pair;
import static org.fede.util.Pair.of;

/**
 *
 * @author federicogentile
 */
public class Series {

    private static final TypeReference<List<Investment>> TR = new TypeReference<List<Investment>>() {
    };

    private List<Investment> investments;

    private Map<String, List<MoneyAmountSeries>> realUSDSavingsByType;
    private Map<String, List<MoneyAmountSeries>> realUSDExpensesByType;

    private List<MoneyAmountSeries> incomeSeries;

    private MoneyAmountSeries realNetSavings;

    public List<Investment> getInvestments() {
        if (this.investments == null) {
            this.investments = SeriesReader.read("investments.json", TR);
        }

        return investments;
    }

    public Map<String, List<MoneyAmountSeries>> getRealUSDExpensesByType() {

        if (this.realUSDExpensesByType == null) {

            this.realUSDExpensesByType = Stream.of(
                    of("taxes", "bbpp"),
                    of("taxes", "inmobiliario-43"),
                    of("taxes", "monotributo-angeles"),
                    of("taxes", "municipal-43"),
                    of("taxes", "contadora"),
                    of("phone", "celular-a"),
                    of("phone", "celular-f"),
                    of("phone", "telefono-43"),
                    of("insurance", "emergencia"),
                    of("insurance", "ioma"),
                    of("insurance", "seguro"),
                    of("services", "gas"),
                    of("services", "luz"),
                    of("services", "cablevision"),
                    of("home", "reparaciones"),
                    of("home", "limpieza"),
                    of("home", "expensas"),
                    of("entertainment", "netflix"),
                    of("entertainment", "viajes"),
                    of("entertainment", "xbox"))
                    .collect(groupingBy(
                            Pair::getFirst,
                            mapping(p -> this.asRealUSDSeries("expense/", p.getSecond()),
                                    toList())));
        }

        return realUSDExpensesByType;
    }

    private List<MoneyAmountSeries> savingsSeries() {
        return Stream.of("ahorros-ay24",
                "ahorros-conbala",
                "ahorros-uva",
                "ahorros-dolar-ON",
                "ahorros-lecap",
                "ahorros-lete",
                "ahorros-caplusa",
                "ahorros-dolar-banco",
                "ahorros-peso",
                "ahorros-dolar-liq",
                "ahorros-euro",
                "ahorros-dai",
                //"ahorros-oro",
                "ahorros-cspx",
                "ahorros-eimi",
                "ahorros-meud",
                "ahorros-conaafa",
                "ahorros-xrsu")
                .map(f -> "saving/" + f + ".json")
                .map(SeriesReader::readSeries)
                .collect(toList());
    }

    public MoneyAmountSeries realIncome() {

        return this.getIncomeSeries()
                .stream()
                .reduce(MoneyAmountSeries::add)
                .get();
    }

    public MoneyAmountSeries realNetSavings() {

        if (this.realNetSavings == null) {

            final var limit = USD_INFLATION.getTo();

            this.realNetSavings = this.savingsSeries()
                    .stream()
                    .map(new SimpleAggregation(2)::change)
                    .map(series -> series.exchangeInto("USD"))
                    .map(usdSeries -> USD_INFLATION.adjust(usdSeries, limit))
                    .reduce(MoneyAmountSeries::add)
                    .get();
        }
        return this.realNetSavings;
    }

    public List<MoneyAmountSeries> getIncomeSeries() {

        if (this.incomeSeries == null) {

            final var limit = USD_INFLATION.getTo();
            this.incomeSeries = Stream.of(
                    readSeries("income/lifia.json"),
                    readSeries("income/unlp.json"),
                    readSeries("income/despegar.json"),
                    readSeries("income/despegar-split.json"))
                    .map(is -> is.exchangeInto("USD"))
                    .map(usdSeries -> USD_INFLATION.adjust(usdSeries, limit))
                    .collect(toList());
        }
        return this.incomeSeries;
    }

    public MoneyAmountSeries realSavings(String type) {

        if (this.realUSDSavingsByType == null) {

            this.realUSDSavingsByType = Stream.of(
                    of("BO", "ahorros-ay24"),
                    of("BO", "ahorros-conbala"),
                    of("BO", "ahorros-uva"),
                    of("BO", "ahorros-dolar-ON"),
                    of("BO", "ahorros-lecap"),
                    of("BO", "ahorros-lete"),
                    of("BO", "ahorros-caplusa"),
                    of("LIQ", "ahorros-dolar-banco"),
                    of("LIQ", "ahorros-peso"),
                    of("LIQ", "ahorros-dolar-liq"),
                    of("LIQ", "ahorros-euro"),
                    of("LIQ", "ahorros-dai"),
                    //of("LIQ", "ahorros-oro"),
                    of("EQ", "ahorros-cspx"),
                    of("EQ", "ahorros-eimi"),
                    of("EQ", "ahorros-meud"),
                    of("EQ", "ahorros-conaafa"),
                    of("EQ", "ahorros-xrsu"))
                    .collect(groupingBy(
                            Pair::getFirst,
                            mapping(p -> this.asRealUSDSeries(p.getSecond()),
                                    toList())));
        }

        return this.realUSDSavingsByType.entrySet().stream()
                .filter(e -> type == null || e.getKey().equals(type))
                .map(Map.Entry::getValue)
                .flatMap(Collection::stream)
                .reduce(MoneyAmountSeries::add)
                .get();
    }

    public MoneyAmountSeries realExpenses(String type) {

        return this.getRealUSDExpensesByType().entrySet()
                .stream()
                .filter(e -> type == null || e.getKey().equals(type))
                .map(Map.Entry::getValue)
                .flatMap(Collection::stream)
                .reduce(MoneyAmountSeries::add)
                .get();
    }

    private MoneyAmountSeries asRealUSDSeries(String fileName) {
        return this.asRealUSDSeries("saving/", fileName);
    }

    private MoneyAmountSeries asRealUSDSeries(String prefix, String fileName) {
        var limit = USD_INFLATION.getTo();
        return USD_INFLATION.adjust(
                SeriesReader.readSeries(prefix + fileName + ".json").exchangeInto("USD"),
                limit);
    }

}
