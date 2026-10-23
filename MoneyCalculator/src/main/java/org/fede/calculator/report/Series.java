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
import org.fede.calculator.money.series.InvestmentType;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.SeriesReader;
import org.fede.calculator.money.series.SortedMapMoneyAmountSeries;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import org.fede.calculator.money.SlidingWindow;
import static org.fede.calculator.money.Currency.USD;
import static org.fede.calculator.report.SavingsSeries.*;
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

    private final Map<SpendingSeries, MoneyAmountSeries> spendingByType = new EnumMap<>(SpendingSeries.class);

    private final Map<IncomeSeries, MoneyAmountSeries> incomeByType = new EnumMap<>(IncomeSeries.class);

    private final Map<SavingsSeries, MoneyAmountSeries> nominalSavingsByType = new EnumMap<>(SavingsSeries.class);

    private final Map<SavingsSeries, MoneyAmountSeries> realSavingsByType = new EnumMap<>(SavingsSeries.class);

    private MoneyAmountSeries realNetSavingsByCashFlow;

    private MoneyAmountSeries realNetSavingsByQuantityChange;

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
            this.realIncome.setName("Real Income");
        }
        return this.realIncome;
    }

    public MoneyAmountSeries realRegularIncome() {
        if (this.realRegularIncome == null) {
            this.realRegularIncome = this.getRegularIncomeSeries()
                    .stream()
                    .reduce(MoneyAmountSeries::add)
                    .get();
            this.realRegularIncome.setName("Real Regular Income");
        }
        return this.realRegularIncome;
    }

    public MoneyAmountSeries realNetSavings() {
        return this.realNetSavingsByCashFlow();
    }

    public MoneyAmountSeries realNetSavingsByCashFlow() {
        if (this.realNetSavingsByCashFlow == null) {
            final var limit = Inflation.usdInflation().getTo();
            // para ahorros que son dolar , que no tienen cotizació en ForeignExchages
            final var balances = this.realNetSavingsFromQuantityChange(s -> !s.usesInvestmentCashFlow());
            
            // para ETFs, FCI, DAI, UVA, cosas que tienen precio que varía.
            final var investedCashFlows = Inflation.usdInflation()
                    .adjust(this.investmentNetCashFlows(), limit);
            this.realNetSavingsByCashFlow = balances.add(investedCashFlows);
            this.realNetSavingsByCashFlow.setName("Real Net Savings");
        }
        return this.realNetSavingsByCashFlow;
    }

    public MoneyAmountSeries realNetSavingsByQuantityChange() {
        if (this.realNetSavingsByQuantityChange == null) {
            this.realNetSavingsByQuantityChange = this.realNetSavingsFromQuantityChange(s -> true);
            this.realNetSavingsByQuantityChange.setName("Real Net Savings (quantity change)");
        }
        return this.realNetSavingsByQuantityChange;
    }

    private MoneyAmountSeries realNetSavingsFromQuantityChange(Predicate<SavingsSeries> filter) {
        final var limit = Inflation.usdInflation().getTo();
        return Arrays.stream(SavingsSeries.values())
                .filter(filter)
                .map(s -> SeriesReader.readSeries("saving/" + s.getSeriesName() + ".json"))
                .map(new SlidingWindow(1)::change)
                .map(series -> series.exchangeInto(USD))
                .map(usdSeries -> Inflation.usdInflation().adjust(usdSeries, limit))
                .reduce(MoneyAmountSeries::add)
                .orElseThrow();
    }

    private MoneyAmountSeries investmentNetCashFlows() {
        final var negation = ONE.negate(MathConstants.C);
        final var zero = MoneyAmount.zero(USD);
        final var byMonth = new HashMap<YearMonth, MoneyAmount>();
        final var inflationFrom = Inflation.usdInflation().getFrom();
        final var inflationTo = Inflation.usdInflation().getTo();

        for (var inv : this.getInvestments()) {
            if (!this.hasInvestmentCashFlow(inv)) {
                continue;
            }
            final var in = inv.getIn();
            final var inYm = YearMonth.from(in.getDate());
            if (!inYm.isBefore(inflationFrom) && !inYm.isAfter(inflationTo)) {
                byMonth.merge(inYm, in.getMoneyAmount(USD), MoneyAmount::add);
            }
            if (inv.getOut() != null) {
                final var out = inv.getOut();
                final var outYm = YearMonth.from(out.getDate());
                if (!outYm.isBefore(inflationFrom) && !outYm.isAfter(inflationTo)) {
                    byMonth.merge(
                            outYm,
                            out.getMoneyAmount(USD).adjust(ONE, negation),
                            MoneyAmount::add);
                }
            }
        }

        final var series = new SortedMapMoneyAmountSeries(USD, "Investment net cash flows");
        final var from = byMonth.keySet().stream().min(YearMonth::compareTo).orElse(inflationTo);
        for (var ym = from; !ym.isAfter(inflationTo); ym = ym.plusMonths(1)) {
            series.putAmount(ym, byMonth.getOrDefault(ym, zero));
        }
        return series;
    }

    private boolean hasInvestmentCashFlow(Investment inv) {
        final var type = inv.getType();
        return type == InvestmentType.ETF
                || type == InvestmentType.FCI
                || type == InvestmentType.BONO;
    }

    public MoneyAmountSeries incomeSource(IncomeSeries series) {
        return this.asIncomeSeries(series);
    }

    public Map<IncomeSeries, MoneyAmountSeries> allIncome() {
        return Arrays.stream(IncomeSeries.values())
                .collect(Collectors.toMap(Function.identity(), this::asIncomeSeries));
    }

    public <E> Map<E, List<MoneyAmountSeries>> incomeSeriesBy(Function<IncomeSeries, E> classifier) {
        return Arrays.stream(IncomeSeries.values())
                .collect(groupingBy(
                        classifier,
                        mapping(this::asIncomeSeries, toList())));
    }

    public List<MoneyAmountSeries> getIncomeSeries() {
        return Arrays.stream(IncomeSeries.values())
                .map(this::asIncomeSeries)
                .toList();
    }

    public List<MoneyAmountSeries> getRegularIncomeSeries() {
        return Arrays.stream(IncomeSeries.values())
                .filter(IncomeSeries::isRegular)
                .map(this::asIncomeSeries)
                .toList();
    }

    private MoneyAmountSeries asIncomeSeries(IncomeSeries s) {
        return this.incomeByType.computeIfAbsent(s, key -> {
            final var series = this.asRealUSDSeries("income/", key.getSeriesName());
            series.setName(key.getDisplayName());
            return series;
        });
    }

    public MoneyAmountSeries nominalSavings() {
        final var series = Arrays.stream(SavingsSeries.values())
                .map(this::asNominalSavingsSeries)
                .reduce(MoneyAmountSeries::add)
                .orElseThrow();
        series.setName("Nominal Savings");
        return series;
    }

    public MoneyAmount currentSavingsUSD() {
        return this.nominalSavings().getAmount(Inflation.usdInflation().getTo());
    }

    public MoneyAmountSeries realSavings() {
        final var series = Arrays.stream(SavingsSeries.values())
                .map(this::asRealSavingsSeries)
                .reduce(MoneyAmountSeries::add)
                .orElseThrow();
        series.setName("Real Savings");
        return series;
    }

    public MoneyAmountSeries realSavings(AssetClass type) {
        final var series = Arrays.stream(SavingsSeries.values())
                .filter(s -> s.getAssetClass() == type)
                .map(this::asRealSavingsSeries)
                .reduce(MoneyAmountSeries::add)
                .orElseThrow();
        series.setName(switch (type) {
            case CASH ->
                "Cash Savings";
            case EQUITY ->
                "Equity Savings";
            case BONDS ->
                "Bonds Savings";
        });
        return series;
    }

    public Map<SavingsSeries, MoneyAmountSeries> allRealSavings() {
        return Arrays.stream(SavingsSeries.values())
                .collect(Collectors.toMap(Function.identity(), this::asRealSavingsSeries));
    }

    public <E> Map<E, List<MoneyAmountSeries>> savingsSeriesBy(Function<SavingsSeries, E> classifier) {
        return Arrays.stream(SavingsSeries.values())
                .collect(groupingBy(
                        classifier,
                        mapping(this::asRealSavingsSeries, toList())));
    }

    private MoneyAmountSeries asNominalSavingsSeries(SavingsSeries s) {
        return this.nominalSavingsByType.computeIfAbsent(s, key -> this.readSeriesInUSD("saving/", key.getSeriesName()));
    }

    private MoneyAmountSeries asRealSavingsSeries(SavingsSeries s) {
        return this.realSavingsByType.computeIfAbsent(s, key
                -> Inflation.usdInflation().adjust(this.asNominalSavingsSeries(key), Inflation.usdInflation().getTo()));
    }

    public MoneyAmountSeries realOtherExpenses() {
        return this.asSeries(SpendingSeries.UNCLASSIFIED);
    }

    public MoneyAmountSeries realCash() {
        return Stream.of(PESO, DOLAR_BANCO, DOLAR_LIQ, EURO, UVA, CAPLUSA, DOLAR_PF, EURO_LIQ, DAI)
                .map(this::asRealSavingsSeries)
                .reduce(MoneyAmountSeries::add)
                .orElseThrow();
    }

    public MoneyAmountSeries realInvested() {
        return Stream.of(CSPX, EIMI, RTWO, MEUD, XRSU, XUSE, AY24, CONAAFA, CONBALA, DOLAR_ON, LECAP, LETE)
                .map(this::asRealSavingsSeries)
                .reduce(MoneyAmountSeries::add)
                .orElseThrow();
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

    public MoneyAmountSeries realRegularExpenses(boolean average) {
        return Arrays.stream(SpendingSeries.values())
                .filter(s -> SpendingFrequency.of(s) == SpendingFrequency.REGULAR)
                .map(s -> this.asSeries(s, average))
                .reduce(MoneyAmountSeries::add)
                .orElseThrow();
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

}
