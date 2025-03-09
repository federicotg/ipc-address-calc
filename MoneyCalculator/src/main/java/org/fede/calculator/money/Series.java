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
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import java.util.stream.Stream;
import static org.fede.calculator.money.Inflation.USD_INFLATION;
import org.fede.calculator.money.series.BBPPYear;
import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.SeriesReader;
import static org.fede.calculator.money.series.SeriesReader.readSeries;
import org.fede.calculator.money.series.SortedMapMoneyAmountSeries;
import org.fede.calculator.money.series.YearMonth;
import org.fede.util.Pair;
import static org.fede.util.Pair.of;

/**
 *
 * @author fede
 */
public class Series {

    private static final TypeReference<List<Investment>> TR = new TypeReference<List<Investment>>() {
    };

    private static final TypeReference<List<BBPPYear>> BBPP_TR = new TypeReference<List<BBPPYear>>() {
    };

    private List<Investment> investments;

    private Map<String, List<MoneyAmountSeries>> realUSDSavingsByType;
    private Map<String, List<MoneyAmountSeries>> realUSDExpensesByType;

    private MoneyAmountSeries realUSDCondoExpenses;

    private List<MoneyAmountSeries> incomeSeries;

    private MoneyAmountSeries realNetSavings;

    private MoneyAmountSeries realIncome;
    private MoneyAmountSeries realExpense;

    public List<Investment> getInvestments() {
        if (this.investments == null) {
            this.investments = SeriesReader.read("investments.json", TR);
        }

        return investments;
    }

    public MoneyAmountSeries getRealUSDCondoExpenses() {
        if (this.realUSDCondoExpenses == null) {
            this.realUSDCondoExpenses = Stream.of(
                    "consorcio-administracion",
                    "consorcio-ascensor",
                    "consorcio-bomba",
                    "consorcio-gasto-administrativo",
                    "consorcio-limpieza",
                    "consorcio-internet",
                    "consorcio-luz",
                    "consorcio-matafuegos",
                    "consorcio-reparaciones",
                    "consorcio-seguros",
                    "absa")
                    .map(p -> this.asRealUSDSeries("expense/", p))
                    .reduce(MoneyAmountSeries::add)
                    .get();
        }
        return this.realUSDCondoExpenses;
    }

    public Map<String, List<MoneyAmountSeries>> getRealUSDExpensesByType() {

        if (this.realUSDExpensesByType == null) {

            this.realUSDExpensesByType = Stream.of(
                    of("taxes", "bbpp"),
                    of("taxes", "inmobiliario-43"),
                    of("taxes", "monotributo-angeles"),
                    of("taxes", "monotributo"),
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
                    of("services", "santander"),
                    of("services", "cablevision"),
                    of("other", "cafe"),
                    of("other", "other"),
                    of("other", "other-usd"),
                    of("home", "reparaciones"),
                    of("services", "limpieza"),
                    of("home", "expensas"),
                    of("entertainment", "netflix"),
                    of("entertainment", "netflix-usd"),
                    of("entertainment", "viajes"),
                    of("entertainment", "viajes-usd"),
                    of("entertainment", "xbox"))
                    .collect(groupingBy(
                            Pair::first,
                            mapping(p -> this.asRealUSDSeries("expense/", p.second()),
                                    Collectors.toList())));
            this.realUSDExpensesByType.put("investing",
                    List.of(
                            this.investingExpenses(),
                            this.asRealUSDSeries("expense/", "atlantico"),
                            this.asRealUSDSeries("expense/", "itau-uy")
                    ));
        }

        return realUSDExpensesByType;
    }

    public List<MoneyAmountSeries> getRealUSDExpenses() {

        return Stream.concat(
                Stream.of(
                        "bbpp",
                        "inmobiliario-43",
                        "monotributo-angeles",
                        "monotributo",
                        "municipal-43",
                        "contadora",
                        "celular-a",
                        "celular-f",
                        "telefono-43",
                        "emergencia",
                        "ioma",
                        "seguro",
                        "gas",
                        "luz",
                        "cablevision",
                        "santander",
                        "cafe",
                        "other",
                        "other-usd",
                        "reparaciones",
                        "limpieza",
                        "expensas",
                        "netflix",
                        "netflix-usd",
                        "viajes",
                        "xbox",
                        "atlantico",
                        "itau-uy")
                        .map(p -> this.asRealUSDSeries("expense/", p)),
                Stream.of(this.investingExpenses()))
                .collect(Collectors.toList());

    }

    private MoneyAmountSeries investingExpenses() {

        final var zero = MoneyAmount.zero(Currency.USD);
        Map<YearMonth, MoneyAmount> buyFeesBymonth
                = this.getInvestments()
                        .stream()
                        .filter(Investment::isETF)
                        .map(inv -> ForeignExchanges.exchange(inv, Currency.USD))
                        .map(inv -> Inflation.USD_INFLATION.real(inv))
                        .collect(
                                Collectors.groupingBy(
                                        i -> YearMonth.of(i.getIn().getDate()),
                                        Collectors.reducing(zero, Investment::getCost, MoneyAmount::add)));

        final var expenseSeries = new SortedMapMoneyAmountSeries(Currency.USD, "investing");

        for (YearMonth ym = YearMonth.of(2016, 1); ym.monthsUntil(Inflation.USD_INFLATION.getTo()) >= 0; ym = ym.next()) {
            expenseSeries.putAmount(ym, buyFeesBymonth.getOrDefault(ym, zero));
        }

        return expenseSeries;

    }

    public MoneyAmountSeries getExpense(String name, boolean nominal) {
        if (nominal) {
            return this.readSeriesInUSD("expense/", name);
        }
        return this.asRealUSDSeries("expense/", name);
    }

    private Stream<MoneyAmountSeries> savingsSeries() {
        return Stream.of("ahorros-ay24",
                "ahorros-conbala",
                "ahorros-uva",
                "ahorros-dolar-ON",
                "ahorros-lecap",
                "ahorros-lete",
                "ahorros-caplusa",
                "ahorros-dolar-banco",
                "ahorros-dolar-pf",
                "ahorros-peso",
                "ahorros-dolar-liq",
                "ahorros-euro",
                "ahorros-dai",
                "ahorros-cspx",
                "ahorros-rtwo",
                "ahorros-xuse",
                "ahorros-eimi",
                "ahorros-meud",
                "ahorros-conaafa",
                "ahorros-xrsu")
                .map(f -> "saving/" + f + ".json")
                .map(SeriesReader::readSeries);
    }

    public MoneyAmountSeries realExpense() {
        if (this.realExpense == null) {
            final var negationFactor = BigDecimal.ONE.negate(MathConstants.C);
            this.realExpense = this.realIncome()
                    .add(this.realNetSavings().map((ym, ma) -> ma.adjust(BigDecimal.ONE, negationFactor)));
        }
        return this.realExpense;
    }

    public MoneyAmountSeries realIncome() {
        if (this.realIncome == null) {
            this.realIncome = this.getIncomeSeries()
                    .stream()
                    .reduce(MoneyAmountSeries::add)
                    .get();
        }
        return this.realIncome;
    }

    public MoneyAmountSeries realNetSavings() {

        if (this.realNetSavings == null) {

            final var limit = USD_INFLATION.getTo();

            final var adjuster = new MonthlyInvestmentSavingsAdjuster(this);

            this.realNetSavings = this.savingsSeries()
                    .map(new SimpleAggregation(2)::change)
                    .map(series -> series.exchangeInto(Currency.USD))
                    .map(usdSeries -> USD_INFLATION.adjust(usdSeries, limit))
                    .reduce(MoneyAmountSeries::add)
                    .get()
                    .map((ym, ma) -> ma.subtract(USD_INFLATION.adjust(adjuster.difference(ym), ym, limit)));
        }
        return this.realNetSavings;
    }

    public MoneyAmountSeries incomeSource(String name) {
        return USD_INFLATION.adjust(
                readSeries("income/" + name + ".json")
                        .exchangeInto(Currency.USD),
                Inflation.USD_INFLATION.getTo());
    }

    public List<MoneyAmountSeries> getIncomeSeries() {

        if (this.incomeSeries == null) {

            final var limit = USD_INFLATION.getTo();
            this.incomeSeries = Stream.of(
                    readSeries("income/lifia.json"),
                    readSeries("income/unlp.json"),
                    readSeries("income/other-ars.json"),
                    readSeries("income/other-usd.json"),
                    readSeries("income/despegar.json"),
                    readSeries("income/despegar-split.json"))
                    .map(is -> is.exchangeInto(Currency.USD))
                    .map(usdSeries -> USD_INFLATION.adjust(usdSeries, limit))
                    .toList();
        }
        return this.incomeSeries;
    }

    public MoneyAmountSeries nominalSavings() {
        return this.savingsSeriesNames()
                .map(Pair::second)
                .map(name -> this.readSeriesInUSD("saving/", name))
                .reduce(MoneyAmountSeries::add)
                .get();
    }

    public MoneyAmountSeries realSavings(String type) {

        if (this.realUSDSavingsByType == null) {

            this.realUSDSavingsByType = this.savingsSeriesNames()
                    .collect(groupingBy(
                            Pair::first,
                            mapping(p -> this.asRealUSDSeries(p.second()),
                                    Collectors.toList())));
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
        return USD_INFLATION.adjust(this.readSeriesInUSD(prefix, fileName), USD_INFLATION.getTo());
    }

    private MoneyAmountSeries readSeriesInUSD(String prefix, String fileName) {
        return SeriesReader.readSeries(prefix.concat(fileName).concat(".json")).exchangeInto(Currency.USD);
    }

    public List<BBPPYear> bbppSeries() {
        return SeriesReader.read("bbpp.json", BBPP_TR);
    }
    
    private Stream<Pair<String, String>> savingsSeriesNames(){
        return Stream.of(
            of("BO", "ahorros-ay24"),
            of("BO", "ahorros-conbala"),
            of("BO", "ahorros-uva"),
            of("BO", "ahorros-dolar-ON"),
            of("BO", "ahorros-lecap"),
            of("BO", "ahorros-lete"),
            of("BO", "ahorros-caplusa"),
            of("LIQ", "ahorros-dolar-banco"),
            of("LIQ", "ahorros-dolar-pf"),
            of("LIQ", "ahorros-peso"),
            of("LIQ", "ahorros-dolar-liq"),
            of("LIQ", "ahorros-euro"),
            of("LIQ", "ahorros-dai"),
            of("EQ", "ahorros-cspx"),
            of("EQ", "ahorros-eimi"),
            of("EQ", "ahorros-rtwo"),
            of("EQ", "ahorros-xuse"),
            of("EQ", "ahorros-meud"),
            of("EQ", "ahorros-conaafa"),
            of("EQ", "ahorros-xrsu"));
    }

}
