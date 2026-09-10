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
package org.fede.calculator.report;

import static java.math.BigDecimal.ONE;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import java.util.stream.Stream;
import org.fede.calculator.money.Cost;
import org.fede.calculator.money.ForeignExchanges;
import org.fede.calculator.money.Inflation;
import org.fede.calculator.money.MathConstants;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.SeriesReader;
import static org.fede.calculator.money.series.SeriesReader.readSeries;
import org.fede.calculator.money.series.SortedMapMoneyAmountSeries;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import org.fede.calculator.money.SlidingWindow;
import org.fede.util.Pair;
import static org.fede.util.Pair.of;
import static org.fede.calculator.money.Currency.USD;
import tools.jackson.core.type.TypeReference;

/**
 *
 * @author fede
 */
public class Series {

    public static final String OTHER = "1-OTHER";
    public static final String ESSENTIAL = "0-ESSENTIAL";
    public static final String DISCRETIONARY = "2-DISCRETIONARY";
    public static final String IRREGULAR = "3-IRREGULAR";

    private static final TypeReference<List<Investment>> TR = new TypeReference<List<Investment>>() {
    };

    private static final TypeReference<List<BBPPYear>> BBPP_TR = new TypeReference<List<BBPPYear>>() {
    };

    private List<Investment> investments;

    private Map<String, List<MoneyAmountSeries>> realUSDSavingsByType;
    private Map<String, List<MoneyAmountSeries>> realUSDExpensesByType;

    private List<MoneyAmountSeries> incomeSeries;

    private List<MoneyAmountSeries> regularIncomeSeries;

    private MoneyAmountSeries realNetSavings;

    private MoneyAmountSeries realIncome;
    private MoneyAmountSeries realRegularIncome;

    private MoneyAmountSeries realExpense;

    private MoneyAmountSeries realOtherExpenses;

    public List<Investment> getInvestments() {
        if (this.investments == null) {
            this.investments = SeriesReader.read("investments.json", TR);
        }

        return investments;
    }

    public <E> Map<E, Optional<MoneyAmountSeries>> groupedExpenses(Function<SpendingSeries, E> classifier) {
        if (this.realUSDExpensesByType == null) {
            this.getRealUSDExpensesByType();
        }
        return Arrays.stream(SpendingSeries.values())
                .collect(groupingBy(
                        classifier,
                        mapping(this::asSeries, Collectors.reducing(MoneyAmountSeries::add))));
    }

   
    private String essentialDiscretionaryClassification(SpendingSeries s) {
        return switch (s) {
            case BBPP, COMIDA, INMOBILIARIO_43, MONOTRIBUTO, SALUD, LUZ, EXPENSAS, MONOTRIBUTO_ANGELES, GAS, MUNICIPAL_43, REPARACIONES ->
                ESSENTIAL;
            case OTHER, OTHER_USD, DEPARTAMENTOS, INVESTMENTS ->
                IRREGULAR;
            case ATLANTICO, BOX, COLON_CAMUZZI,COLON_EXPENSAS,COLON_EDEA,COLON_FLOW,COLON_OSSE,COLON_MUNICIPAL, CABLEVISION, CELULAR_A, CELULAR_F, COMIDA_DISC, CONTADORA, EMERGENCIA, IOMA, ITAU_UY, NETFLIX, SUSCRIPCIONES_ARS, SUSCRIPCIONES_USD, SELLOS, LIMPIEZA, SANTANDER, SEGURO, TELEFONO_43, VIAJES, VIAJES_USD, XBOX ->
                DISCRETIONARY;
            case UNCLASSIFIED ->
                OTHER;
        };
    }
    
    

    public Map<String, List<MoneyAmountSeries>> getRealUSDExpensesByType() {

        if (this.realUSDExpensesByType == null) {

            final var income = this.realIncome();
            final var netSaving = this.realNetSavings();
            final var spending = this.getRealUSDExpenses()
                    .stream()
                    .reduce(MoneyAmountSeries::add)
                    .get();

            final var otherSpending = income
                    .subtract(spending)
                    .subtract(netSaving);
            otherSpending.setName("Other spending");
            this.realOtherExpenses = otherSpending;

            return Arrays.stream(SpendingSeries.values())
                    .collect(groupingBy(
                            this::essentialDiscretionaryClassification,
                            mapping(this::asSeries, toList())));
        }

        return this.realUSDExpensesByType;
    }

    private MoneyAmountSeries asSeries(SpendingSeries s) {
        return switch (s) {
            case INVESTMENTS ->
                this.investingExpenses();
            case UNCLASSIFIED ->
                this.realOtherExpenses;
            default ->
                this.asRealUSDSeries("expense/", s.getSeriesName());
        };

    }

    private List<MoneyAmountSeries> getRealUSDExpenses() {
        var list = new ArrayList<MoneyAmountSeries>(SpendingSeries.values().length);
        for (var spending : SpendingSeries.values()) {
            switch (spending) {
                case INVESTMENTS ->
                    list.add(this.investingExpenses());
                case UNCLASSIFIED -> {
                }
                default ->
                    list.add(this.asRealUSDSeries("expense/", spending.getSeriesName()));
            }
        }
        return list;
    }

    public MoneyAmountSeries investingExpenses() {

        final List<Cost> buyCost
                = this.getInvestments()
                        .stream()
                        .filter(Investment::isETF)
                        .map(inv -> ForeignExchanges.exchange(inv, USD))
                        .map(Inflation.usdInflation()::real)
                        .map(i -> new Cost(YearMonth.from(i.getIn().getDate()), i.getCost(USD)))
                        .toList();

        final var iva = SeriesReader.readPercent("iva").add(ONE);

        final List<Cost> sellCost
                = this.getInvestments()
                        .stream()
                        .filter(Investment::isETF)
                        .filter(i -> i.getOut() != null)
                        .map(inv -> ForeignExchanges.exchange(inv, USD))
                        .map(Inflation.usdInflation()::real)
                        .map(i
                                -> new Cost(
                                        YearMonth.from(i.getOut().getDate()),
                                        i.getOut().getFeeMoneyAmount(USD).adjust(ONE, i.getComment() == null ? iva : ONE)
                                                .add(i.getOut().getTransferFeeMoneyAmount(USD))))
                        .toList();

        final var zero = MoneyAmount.zero(USD);
        final var feesByMonth = Stream.concat(buyCost.stream(), sellCost.stream())
                .collect(
                        Collectors.groupingBy(
                                Cost::ym,
                                Collectors.reducing(zero, Cost::amount, MoneyAmount::add)));

        final var expenseSeries = new SortedMapMoneyAmountSeries(USD, "investing");

        for (YearMonth ym = YearMonth.of(2016, 1); ym.until(Inflation.usdInflation().getTo(), ChronoUnit.MONTHS) >= 0; ym = ym.plusMonths(1)) {
            expenseSeries.putAmount(ym, feesByMonth.getOrDefault(ym, zero));
        }

        return expenseSeries;

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
                "ahorros-euro-liq",
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
            final var negationFactor = ONE.negate(MathConstants.C);
            this.realExpense = this.realIncome()
                    .add(this.realNetSavings().map((ym, ma) -> ma.adjust(ONE, negationFactor)));
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

    public MoneyAmountSeries realRegularIncome() {
        if (this.realRegularIncome == null) {
            this.realRegularIncome = this.getRegularIncomeSeries()
                    .stream()
                    .reduce(MoneyAmountSeries::add)
                    .get();
        }
        return this.realRegularIncome;
    }

    public MoneyAmountSeries realNetSavings() {

        if (this.realNetSavings == null) {

            final var limit = Inflation.usdInflation().getTo();

            this.realNetSavings = this.savingsSeries()
                    .map(new SlidingWindow(1)::change)
                    .map(series -> series.exchangeInto(USD))
                    .map(usdSeries -> Inflation.usdInflation().adjust(usdSeries, limit))
                    .reduce(MoneyAmountSeries::add)
                    .get();
        }
        return this.realNetSavings;
    }

    public MoneyAmountSeries incomeSource(String name) {
        return Inflation.usdInflation().adjust(
                readSeries("income/" + name + ".json")
                        .exchangeInto(USD),
                Inflation.usdInflation().getTo());
    }

    public List<MoneyAmountSeries> getIncomeSeries() {

        if (this.incomeSeries == null) {

            final var limit = Inflation.usdInflation().getTo();
            this.incomeSeries = Stream.of(
                    readSeries("income/lifia.json"),
                    readSeries("income/unlp.json"),
                    readSeries("income/other-ars.json"),
                    readSeries("income/other-usd.json"),
                    readSeries("income/other-eur.json"),
                    readSeries("income/despegar.json"),
                    readSeries("income/despegar-split.json"))
                    .map(is -> is.exchangeInto(USD))
                    .map(usdSeries -> Inflation.usdInflation().adjust(usdSeries, limit))
                    .toList();
        }
        return this.incomeSeries;
    }

    public List<MoneyAmountSeries> getRegularIncomeSeries() {

        if (this.regularIncomeSeries == null) {

            final var limit = Inflation.usdInflation().getTo();
            this.regularIncomeSeries = Stream.of(
                    readSeries("income/lifia.json"),
                    readSeries("income/unlp.json"),
                    readSeries("income/despegar.json"),
                    readSeries("income/despegar-split.json"))
                    .map(is -> is.exchangeInto(USD))
                    .map(usdSeries -> Inflation.usdInflation().adjust(usdSeries, limit))
                    .toList();
        }
        return this.regularIncomeSeries;
    }

    public MoneyAmountSeries nominalSavings() {
        return this.savingsSeriesNames()
                .map(Pair::second)
                .map(name -> this.readSeriesInUSD("saving/", name))
                .reduce(MoneyAmountSeries::add)
                .get();
    }

    public MoneyAmount currentSavingsUSD() {
        return this.nominalSavings().getAmount(Inflation.usdInflation().getTo());
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

    public MoneyAmountSeries realOtherExpenses() {
        if (this.realOtherExpenses == null) {
            // initializes realOtherExpenses
            this.getRealUSDExpensesByType();
        }
        return this.realOtherExpenses;

    }

    public MoneyAmountSeries realCash() {
        return Stream.of(
                "ahorros-peso",
                "ahorros-dolar-banco",
                "ahorros-dolar-liq",
                "ahorros-euro",
                "ahorros-uva",
                "ahorros-caplusa",
                "ahorros-dolar-pf",
                "ahorros-euro-liq",
                "ahorros-dai")
                .map(this::asRealUSDSeries)
                .reduce(MoneyAmountSeries::add)
                .get();
    }

    public MoneyAmountSeries ripteInRealUSD() {

        var ripte = this.asRealUSDSeries("income/", "ripte");
        ripte.setName("RIPTE Real USD");
        return ripte;
    }

    public MoneyAmountSeries realInvested() {
        return Stream.of(
                "ahorros-cspx",
                "ahorros-eimi",
                "ahorros-rtwo",
                "ahorros-meud",
                "ahorros-xrsu",
                "ahorros-xuse",
                "ahorros-ay24",
                "ahorros-conaafa",
                "ahorros-conbala",
                "ahorros-dolar-ON",
                "ahorros-lecap",
                "ahorros-lete")
                .map(this::asRealUSDSeries)
                .reduce(MoneyAmountSeries::add)
                .get();
    }

    public MoneyAmountSeries realExpenses(String type) {

        return this.getRealUSDExpensesByType().entrySet()
                .stream()
                .filter(e -> type == null || e.getKey().equals(type))
                .filter(e -> !e.getKey().equals(OTHER))
                .map(Map.Entry::getValue)
                .flatMap(Collection::stream)
                .reduce(MoneyAmountSeries::add)
                .get();
    }

    private MoneyAmountSeries asRealUSDSeries(String fileName) {
        return this.asRealUSDSeries("saving/", fileName);
    }

    private MoneyAmountSeries asRealUSDSeries(String prefix, String fileName) {
        return Inflation.usdInflation().adjust(this.readSeriesInUSD(prefix, fileName), Inflation.usdInflation().getTo());
    }

    private MoneyAmountSeries readSeriesInUSD(String prefix, String fileName) {
        return SeriesReader.readSeries(prefix + fileName + ".json")
                .exchangeInto(USD);
    }

    public List<BBPPYear> bbppSeries() {
        return SeriesReader.read("bbpp.json", BBPP_TR);
    }

    private Stream<Pair<String, String>> savingsSeriesNames() {
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
                of("LIQ", "ahorros-euro-liq"),
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
