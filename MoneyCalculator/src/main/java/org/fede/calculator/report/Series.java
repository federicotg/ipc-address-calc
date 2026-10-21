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
import java.util.Arrays;
import java.util.EnumMap;
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

    private static final TypeReference<List<Investment>> TR = new TypeReference<List<Investment>>() {
    };

    private static final TypeReference<List<BBPPYear>> BBPP_TR = new TypeReference<List<BBPPYear>>() {
    };

    private List<Investment> investments;

    private Map<String, List<MoneyAmountSeries>> realUSDSavingsByType;

    private final Map<SpendingSeries, MoneyAmountSeries> spendingByType = new EnumMap<>(SpendingSeries.class);

    private List<MoneyAmountSeries> incomeSeries;

    private List<MoneyAmountSeries> regularIncomeSeries;

    private MoneyAmountSeries realNetSavings;

    private MoneyAmountSeries realIncome;
    private MoneyAmountSeries realRegularIncome;

    private MoneyAmountSeries realExpense;

    public List<Investment> getInvestments() {
        if (this.investments == null) {
            this.investments = SeriesReader.read("investments.json", TR);
        }

        return investments;
    }

    public <E> Map<E, Optional<MoneyAmountSeries>> summedExpensesBy(Function<SpendingSeries, E> classifier) {
        return this.summedExpensesBy(classifier, false);
    }

    public <E> Map<E, Optional<MoneyAmountSeries>> summedExpensesBy(Function<SpendingSeries, E> classifier, boolean average) {
        var map = Arrays.stream(SpendingSeries.values())
                .collect(groupingBy(
                        classifier,
                        mapping(s -> this.asSeries(s, average), Collectors.reducing(MoneyAmountSeries::add))));
        for (var e : map.entrySet()) {
            e.getValue().ifPresent(series -> series.setName(e.getKey().toString()));
        }
        return map;
    }

    public <E> Map<E, List<MoneyAmountSeries>> expenseSeriesBy(Function<SpendingSeries, E> classifier) {
        return Arrays.stream(SpendingSeries.values())
                .collect(groupingBy(
                        classifier,
                        mapping(this::asSeries, toList())));
    }

    public Map<SpendingSeries, MoneyAmountSeries> allSpending() {
        return Arrays.stream(SpendingSeries.values())
                .collect(Collectors.toMap(Function.identity(), this::asSeries));
    }

    private MoneyAmountSeries asSeries(SpendingSeries s, boolean average) {
        final var series = this.asSeries(s);
        if (!average) {
            return series;
        }
        return new SlidingWindow(s.getAverageMonths()).average(series);
    }

    private MoneyAmountSeries asSeries(SpendingSeries s) {
        if (s == SpendingSeries.UNCLASSIFIED) {
            return this.ensureUnclassified();
        }
        return this.spendingByType.computeIfAbsent(s, key -> switch (key) {
            case INVESTMENTS ->
                this.investingExpenses();
            case UNCLASSIFIED ->
                throw new IllegalStateException("UNCLASSIFIED is handled by ensureUnclassified");
            default ->
                this.asRealUSDSeries("expense/", key.getSeriesName());
        });
    }

    private MoneyAmountSeries ensureUnclassified() {
        final var cached = this.spendingByType.get(SpendingSeries.UNCLASSIFIED);
        if (cached != null) {
            return cached;
        }

        final var classified = Arrays.stream(SpendingSeries.values())
                .filter(s -> s != SpendingSeries.UNCLASSIFIED)
                .map(this::asSeries)
                .reduce(MoneyAmountSeries::add)
                .orElseThrow();

        final var otherSpending = this.realIncome()
                .subtract(classified)
                .subtract(this.realNetSavings());
        otherSpending.setName("Other spending");
        this.spendingByType.put(SpendingSeries.UNCLASSIFIED, otherSpending);
        return otherSpending;
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
        return this.asSeries(SpendingSeries.UNCLASSIFIED);
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

    public MoneyAmountSeries realExpenses(ExpenseTier type) {
        return this.realExpenses(type, false);
    }

    public MoneyAmountSeries realExpenses(ExpenseTier type, boolean average) {
        if (type == null) {
            return Arrays.stream(SpendingSeries.values())
                    .map(s -> this.asSeries(s, average))
                    .reduce(MoneyAmountSeries::add)
                    .orElseThrow();
        }
        return this.summedExpensesBy(ExpenseTier::of, average)
                .get(type)
                .orElseThrow();
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
