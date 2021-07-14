/*
 * Copyright (C) 2019 Federico Tello Gentile <federicotg@gmail.com>
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
import java.io.PrintStream;
import java.math.BigDecimal;
import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.ONE;
import static org.fede.calculator.money.MathConstants.CONTEXT;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Comparator;
import static java.util.Comparator.comparing;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static java.text.MessageFormat.format;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import static java.util.Map.entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.InvestmentType;
import static org.fede.calculator.money.series.InvestmentType.*;
import org.fede.calculator.money.series.YearMonth;
import org.fede.util.Pair;
import static org.fede.util.Pair.of;
import static org.fede.calculator.money.Inflation.USD_INFLATION;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.AnnualHistoricalReturn;
import org.fede.calculator.money.series.BBPPItem;
import org.fede.calculator.money.series.BBPPTaxBraket;
import org.fede.calculator.money.series.BBPPYear;
import org.fede.calculator.money.series.InvestmentEvent;
import org.fede.calculator.money.series.SeriesReader;
import static org.fede.calculator.money.series.SeriesReader.readSeries;
import org.fede.calculator.money.series.SortedMapMoneyAmountSeries;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
public class ConsoleReports {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal ONE_PERCENT = BigDecimal.ONE.movePointLeft(2);

    private static final BigDecimal COEFFICIENT = new BigDecimal("0.1223");

    private static final BigDecimal REALTOR_FEE = new BigDecimal("0.045");
    private static final BigDecimal STAMP_TAX = new BigDecimal("0.018");
    private static final BigDecimal REGISTER_TAX = new BigDecimal("0.006");
    private static final BigDecimal IVA = new BigDecimal("1.21");
    private static final BigDecimal NOTARY_FEE = new BigDecimal("0.02")
            .multiply(IVA, CONTEXT);

    private static final BigDecimal TRADING_FEE = new BigDecimal("0.006");
    private static final BigDecimal TRADING_FX_FEE = new BigDecimal("0.0025");

    private static final BigDecimal CAPITAL_GAINS_TAX_RATE = new BigDecimal("0.15");
    private static final double RUSSELL2000_PCT = new BigDecimal("0.1").doubleValue();
    private static final double SP500_PCT = 0.7d;
    private static final double EIMI_PCT = 0.1d;
    private static final double MEUD_PCT = 0.1d;

    private static final double CSPX_FEE = 0.0007d;
    private static final double XRSU_FEE = 0.003d;
    private static final double EIMI_FEE = 0.0018d;
    private static final double MEUD_FEE = 0.0007d;

    private static final int RETIREMENT_AGE_STD = 3;
    private static final int END_AGE_STD = 6;

    private static final double BBPP_FX_GAP_PERCENT = 0.9d;
    private static final BigDecimal CAPITAL_GAINS_TAX_EXTRA_WITHDRAWAL_PCT = new BigDecimal("1.15");

    private static final MoneyAmount ZERO_USD = new MoneyAmount(ZERO, "USD");

    private static final Pattern PARAM_SEPARATOR = Pattern.compile("=");

    private static final TypeReference<List<Investment>> TR = new TypeReference<List<Investment>>() {
    };

    private static final TypeReference<Map<String, BenchmarkItem>> BENCHMARK_TR = new TypeReference<Map<String, BenchmarkItem>>() {
    };

    private static final Collector<BigDecimal, ?, BigDecimal> REDUCER = reducing(ZERO.setScale(6, MathConstants.ROUNDING_MODE), BigDecimal::add);
    private static final Collector<Investment, ?, BigDecimal> MAPPER = mapping(inv -> inv.getMoneyAmount().getAmount().setScale(6, MathConstants.ROUNDING_MODE), REDUCER);

    private static final Comparator<Pair<Pair<String, String>, ?>> TYPE_CURRENCY_COMPARATOR = comparing((Pair<Pair<String, String>, ?> pair) -> pair.getFirst().getFirst())
            .thenComparing(comparing(pair -> pair.getFirst().getSecond()));

    private static final NumberFormat PERCENT_FORMAT = NumberFormat.getPercentInstance();

    private List<Investment> investments;

    private List<BigDecimal> sp500TotalReturns;
    private List<BigDecimal> russell2000TotalReturns;
    private Map<String, List<MoneyAmountSeries>> realUSDSavingsByType;
    private Map<String, List<MoneyAmountSeries>> realUSDExpensesByType;

    private List<MoneyAmountSeries> incomeSeries;

    private MoneyAmountSeries realNetSavings;

    private final StringBuilder out;

    private double bbppMean;
    private double bbppVar;
    private double bbppMinFactor;

    private ConsoleReports(StringBuilder out) {
        this.out = out;
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

    private void appendLine(String... texts) {
        Arrays.stream(texts)
                .forEach(out::append);
        out.append("\n");
    }

    private void investments() {

        appendLine("===< Inversiones actuales agrupadas por moneda >===");

        final NumberFormat sixDigits = NumberFormat.getNumberInstance();
        sixDigits.setMinimumFractionDigits(6);

        getInvestments().stream()
                .filter(Investment::isCurrent)
                .collect(groupingBy(inv -> of(inv.getType().toString(), inv.getCurrency()), MAPPER))
                .entrySet()
                .stream()
                .map(e -> of(e.getKey(), e.getValue()))
                .sorted(TYPE_CURRENCY_COMPARATOR)
                .map(e -> format("{0} {2}: {1}", e.getFirst().getFirst(), sixDigits.format(e.getSecond()), e.getFirst().getSecond()))
                .forEach(this::appendLine);
    }

    private Optional<MoneyAmount> total(Predicate<Investment> predicate, String reportCurrency, YearMonth limit) {
        return getInvestments().stream()
                .filter(predicate)
                .map(i -> i.getInvestment().getMoneyAmount())
                .map(investedAmount -> ForeignExchanges.getForeignExchange(investedAmount.getCurrency(), reportCurrency).exchange(investedAmount, reportCurrency, limit.getYear(), limit.getMonth()))
                .reduce(MoneyAmount::add);
    }

    private void groupedInvestments() {
        final var reportCurrency = "USD";
        final var limit = USD_INFLATION.getTo();

        appendLine("===< Inversiones Actuales Agrupadas en ", reportCurrency, " ", String.valueOf(limit.getYear()), "/", String.valueOf(limit.getMonth()), " >===");

        final var total = this.total(Investment::isCurrent, reportCurrency, limit);
        getInvestments().stream()
                .filter(Investment::isCurrent)
                .collect(groupingBy(in -> of(in.getType().toString(), in.getCurrency()), MAPPER))
                .entrySet()
                .stream()
                .map(e -> of(e.getKey(), new MoneyAmount(e.getValue(), e.getKey().getSecond())))
                .map(p -> of(p.getFirst(), this.fx(p, reportCurrency)))
                .sorted((p, q) -> q.getSecond().getAmount().compareTo(p.getSecond().getAmount()))
                .map(pair -> this.formatReport(total, pair.getSecond(), pair.getFirst().getFirst()))
                .forEach(this::appendLine);

        total.map(t -> format("-----------------------------\n{0}{1}", text("Total", 5), currency(t, 16)))
                .ifPresent(this::appendLine);
    }

    private String assetAllocation(Investment investment) {
        final Set<String> equities = Set.of("CSPX", "EIMI", "MEUD", "XRSU");
        final Set<String> bonds = Set.of("LECAP", "LETE", "UVA", "AY24");

        if (equities.contains(investment.getInvestment().getCurrency())) {
            return "EQ";
        }
        if (investment.getType().equals(InvestmentType.BONO)
                || investment.getType().equals(InvestmentType.PF)
                || bonds.contains(investment.getInvestment().getCurrency())) {
            return "BO";
        }

        return "CASH";

    }

    private void listStockByTpe() {

        final var reportCurrency = "USD";
        final var limit = USD_INFLATION.getTo();
        final var limitStr = String.valueOf(limit.getMonth()) + "/" + String.valueOf(limit.getYear());

        appendLine("===< Inversiones Actuales en ", reportCurrency, " por tipo. ", limitStr, " >===");

        final Optional<MoneyAmount> total = this.total(Investment::isCurrent, reportCurrency, limit);

        getInvestments().stream()
                .filter(Investment::isCurrent)
                .collect(groupingBy(
                        this::assetAllocation,
                        mapping(inv -> ForeignExchanges.getForeignExchange(inv.getInvestment().getCurrency(), reportCurrency)
                        .exchange(inv.getInvestment().getMoneyAmount(), reportCurrency, limit.getYear(), limit.getMonth())
                        .getAmount()
                        .setScale(6, MathConstants.ROUNDING_MODE),
                                REDUCER)))
                .entrySet()
                .stream()
                .map(entry -> this.formatReport(total, new MoneyAmount(entry.getValue(), reportCurrency), entry.getKey()))
                .forEach(this::appendLine);

        total.map(t -> format("-----------------------------\n{0}{1}", text("Total", 5), currency(t, 16)))
                .ifPresent(this::appendLine);
    }

    private MoneyAmount fx(Pair<Pair<String, String>, MoneyAmount> p, String reportCurrency) {

        final var limit = USD_INFLATION.getTo();

        return ForeignExchanges.getForeignExchange(p.getSecond().getCurrency(), reportCurrency).exchange(p.getSecond(), reportCurrency, limit.getYear(), limit.getMonth());
    }

    private String formatReport(Optional<MoneyAmount> total, MoneyAmount subtotal, String type) {

        return format("{0}{1}{2}",
                text(type, 5),
                currency(subtotal, 16),
                pctBar(total.map(tot -> subtotal.getAmount().divide(tot.getAmount(), CONTEXT)).orElse(ZERO)));
    }

    private BenchmarkItem real(BenchmarkItem item) {

        BenchmarkItem answer = new BenchmarkItem();
        answer.setCurrent(item.getCurrent());
        answer.setInitial(USD_INFLATION.adjust(
                new MoneyAmount(item.getInitial(), "USD"),
                2019,
                7,
                USD_INFLATION.getTo().getYear(),
                USD_INFLATION.getTo().getYear()).getAmount());
        return answer;
    }

    private void subtitle(String title) {

        final var line = IntStream.range(0, title.length() + 4)
                .mapToObj(i -> "-")
                .collect(joining());

        appendLine("");
        appendLine("\t<", line, ">");
        appendLine("\t<- ", title, " ->");
        appendLine("\t<", line, ">");
    }

    private void printReport(PrintStream out) {
        out.println(this.out.toString());
    }

    private void income(String[] args, String paramName) {
        this.income(Integer.parseInt(this.paramsValue(args, paramName).getOrDefault("months", "12")));

        final var totalIncome = this.getIncomeSeries()
                .stream()
                .flatMap(MoneyAmountSeries::moneyAmountStream)
                .collect(reducing(MoneyAmount::add))
                .orElse(ZERO_USD)
                .getAmount();

        this.appendLine(format("Total income: {0}", currency(totalIncome)));

    }

    private List<MoneyAmountSeries> getIncomeSeries() {

        if (this.incomeSeries == null) {

            final var limit = USD_INFLATION.getTo();
            this.incomeSeries = Stream.of(
                    readSeries("income/lifia.json"),
                    readSeries("income/unlp.json"),
                    readSeries("income/despegar.json"),
                    readSeries("income/despegar-split.json"))
                    .map(is -> is.exchangeInto("USD"))
                    .map(usdSeries -> USD_INFLATION.adjust(usdSeries, limit.getYear(), limit.getMonth()))
                    .collect(toList());
        }
        return this.incomeSeries;
    }

    private void income(int months) {
        final var limit = USD_INFLATION.getTo();
        final var averageRealUSDIncome = this.getIncomeSeries()
                .stream()
                .collect(reducing(MoneyAmountSeries::add))
                .map(new SimpleAggregation(months)::average)
                .map(allRealUSDIncome -> allRealUSDIncome.getAmount(allRealUSDIncome.getTo()))
                .orElse(ZERO_USD);

        this.appendLine(format("===< Average {0}-month income in {1}/{2} real USD >===",
                months,
                limit.getMonth(),
                String.valueOf(limit.getYear())));

        this.appendLine("\tIncome: ",
                averageRealUSDIncome.getCurrency(),
                " ",
                currency(averageRealUSDIncome.getAmount()));

        final var savingPct = new MoneyAmount(averageRealUSDIncome.getAmount().multiply(new BigDecimal("0.3"), CONTEXT), averageRealUSDIncome.getCurrency());

        this.appendLine("30% saving: ",
                averageRealUSDIncome.getCurrency(),
                " ",
                currency(savingPct.getAmount()),
                " / ",
                currency(ForeignExchanges.getForeignExchange(savingPct.getCurrency(), "ARS").exchange(savingPct, "ARS", limit.getYear(), limit.getMonth()).getAmount()));

        appendLine(format("Saved salaries {0}",
                this.realSavings(null).getAmount(limit).getAmount()
                        .divide(averageRealUSDIncome.getAmount(), CONTEXT)));

    }

    private void invReport(String[] args, String paranName) {

        final var params = this.paramsValue(args, paranName);

        final var nominal = Boolean.parseBoolean(params.getOrDefault("nominal", "false"));

        final var type = params.getOrDefault("type", "all");
        final var currency = params.getOrDefault("currency", "USD");

        this.inv("all".equalsIgnoreCase(type) ? x -> true : x -> x.getCurrency().equalsIgnoreCase(type), nominal, currency);

    }

    public static void main(String[] args) {
        try {

            PERCENT_FORMAT.setMinimumFractionDigits(2);
            final var me = new ConsoleReports(new StringBuilder(1024));

            final Map<String, Runnable> actions = Map.ofEntries(
                    entry("i", me::investments),
                    entry("gi", me::groupedInvestments),
                    entry("ti", me::listStockByTpe),
                    entry("inv", () -> me.invReport(args, "inv")),
                    //savings
                    entry("savings", me::savings),
                    entry("savings-evo", () -> me.savingEvolution(args, "savings-evo")),
                    entry("savings-change", () -> me.savingChange(args, "savings-change")),
                    entry("savings-change-pct", () -> me.savingsPercentChange(args, "savings-change-pct")),
                    entry("savings-net-change", me::monthlySavings),
                    entry("savings-year", me::yearlySavings),
                    entry("savings-half", me::halfSavings),
                    entry("savings-quarter", me::quarterSavings),
                    entry("savings-avg-net-change", () -> me.monthlySavings(args, "savings-avg-net-change")),
                    entry("savings-avg-net-pct", () -> me.netAvgSavingPct(args, "savings-avg-net-pct")),
                    entry("savings-avg-spent-pct", () -> me.netAvgSavingSpentPct(args, "savings-avg-spent-pct")),
                    entry("savings-dist", me::savingsDistributionEvolution),
                    entry("savings-dist-pct", me::savingsDistributionPercentEvolution),
                    entry("saved-salaries-evo", () -> me.averageSavedSalaries(args, "saved-salaries-evo")),
                    //income
                    entry("income", () -> me.income(args, "income")),
                    entry("income-evo", me::incomeEvolution),
                    entry("income-table", me::savingsIncomeTable),
                    entry("income-year-table", me::yearSavingsIncomeTable),
                    entry("income-year", me::yearlyIncome),
                    entry("income-half", me::halfIncome),
                    entry("income-quarter", me::quarterIncome),
                    entry("p", () -> me.portfolio(args, "p")),
                    entry("pa", () -> me.portfolioAllocation()),
                    entry("income-avg-evo", () -> me.incomeAverageEvolution(args, "income-avg-evo")),
                    //house cost
                    entry("house-evo", () -> me.houseCostsEvolution()),
                    entry("house", () -> me.houseIrrecoverableCosts(USD_INFLATION.getTo())),
                    entry("house1", () -> me.houseIrrecoverableCosts(YearMonth.of(2011, 8))),
                    entry("house3", () -> me.houseIrrecoverableCosts(YearMonth.of(2013, 8))),
                    entry("house5", () -> me.houseIrrecoverableCosts(YearMonth.of(2015, 8))),
                    //expenses
                    entry("expenses", () -> me.expenses(args, "expenses")),
                    entry("expenses-evo", () -> me.expenseEvolution(args, "expenses-evo")),
                    entry("expenses-change", () -> me.expensesChange(args, "expenses-change")),
                    //goal
                    entry("goal", () -> me.goal(args, "goal")),
                    entry("bbpp", () -> me.bbpp(args, "bbpp"))
            );

            final var params = Arrays.stream(args)
                    .map(String::toLowerCase)
                    .collect(toSet());

            if (params.isEmpty() || params.contains("help")) {

                final var help = Map.ofEntries(
                        entry("goal", "trials=100000 period=20 retirement=63 age=97 w=1000 d=850 inflation=3 cash=0 sp500=true tax=true bbpp=2.25 pension=50"),
                        entry("savings-change", "months=1"),
                        entry("savings-change-pct", "months=1"),
                        entry("income", "months=12"),
                        entry("p", "type=(full*|pct) subtype=(all*|equity|bond|commodity|cash) y=current m=current"),
                        entry("inv", "type=(all|CSPX|MEUD|EIMI|XRSU) nominal=false currency=USD"),
                        entry("saved-salaries-evo", "months=12"),
                        entry("income-avg-evo", "months=12"),
                        entry("bbpp", "year=2020"),
                        entry("savings-avg-net-change", "months=12"),
                        entry("savings-avg-net-pct", "months=12"),
                        entry("savings-avg-spent-pct", "months=12"),
                        entry("expenses", "type=(taxes|insurance|phone|services|home|entertainment) months=12"),
                        entry("expenses-change", "months=12"),
                        entry("expenses-evo", "type=(taxes|insurance|phone|services|home|entertainment)"),
                        entry("savings-evo", "type=(BO|LIQ|EQ)")
                );

                Stream.concat(
                        actions.keySet()
                                .stream()
                                .filter(action -> !help.keySet().contains(action))
                                .map(action -> format(" - {0}", action)),
                        help.entrySet().stream().map(e -> format(" - {0} {1}", e.getKey(), e.getValue())))
                        .sorted()
                        .forEach(me::appendLine);
            } else {

                actions.entrySet()
                        .stream()
                        .filter(e -> params.isEmpty() || params.contains(e.getKey().toLowerCase()))
                        .map(Map.Entry::getValue)
                        .forEach(r -> {
                            r.run();
                            me.appendLine("");
                        });
            }
            me.printReport(System.out);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            ex.printStackTrace(System.err);
        }
    }

    private void savings() {

        appendLine("===< Historical Real USD Savings Stats >===");

        // total savings
        final var limit = USD_INFLATION.getTo();

        final var totalSavings = this.realSavings(null).getAmount(limit);

        // total income
        final var totalIncome = this.realIncome()
                .moneyAmountStream()
                .reduce(MoneyAmount::add)
                .get();

        final var months = this.realIncome().getFrom().monthsUntil(limit);

        final var avgSalary = totalIncome.getAmount().divide(BigDecimal.valueOf(months), CONTEXT);

        appendLine(format("Income USD {0}\nSavings USD {1} {2}\nAverage salary {3}\nSaved salaries {4}",
                currency(totalIncome.getAmount()),
                currency(totalSavings.getAmount()),
                percent(totalSavings.getAmount().divide(totalIncome.getAmount(), CONTEXT)),
                currency(avgSalary),
                totalSavings.getAmount().divide(avgSalary, CONTEXT)));

        //ingreso promedio de N meses
        final var agg = new SimpleAggregation(YearMonth.of(2012, 1).monthsUntil(USD_INFLATION.getTo()));

        final var averagIncome = agg.average(this.realIncome()).getAmount(USD_INFLATION.getTo());

        // ahorro promedio de N meses
        final var averagNetSavings = agg.average(this.realNetSavings()).getAmount(USD_INFLATION.getTo());

        final var m = totalSavings.getAmount().divide(averagIncome.subtract(averagNetSavings).getAmount(), CONTEXT);

        final var yearAndMonth = m.divideAndRemainder(BigDecimal.valueOf(12), CONTEXT);

        appendLine(format(
                "Projected {0} years and {1} months of USD {3} income (equivalent to {2} of historical real income).",
                yearAndMonth[0],
                yearAndMonth[1].setScale(0, MathConstants.ROUNDING_MODE),
                percent(ONE.subtract(averagNetSavings.getAmount().divide(averagIncome.getAmount(), CONTEXT), CONTEXT)),
                averagIncome.subtract(averagNetSavings).getAmount()));

    }

    private void houseCostsEvolution() {

        final var limit = USD_INFLATION.getTo();

        final var nominalInitialCost = new BigDecimal("96000");
        final var nominalTransactionCost = nominalInitialCost.multiply(
                REALTOR_FEE.add(STAMP_TAX, CONTEXT)
                        .add(REGISTER_TAX, CONTEXT)
                        .add(NOTARY_FEE, CONTEXT),
                CONTEXT);

        final var start = YearMonth.of(2010, 8);
        final var realInitialCost = USD_INFLATION.adjust(new MoneyAmount(nominalTransactionCost, "USD"),
                start.getYear(), start.getMonth(),
                limit.getYear(), limit.getMonth());

        final var initialCostSeries = new SortedMapMoneyAmountSeries(
                "USD",
                new TreeMap<>(
                        Map.of(
                                start, realInitialCost,
                                YearMonth.of(2010, 9), ZERO_USD,
                                YearMonth.of(2010, 10), ZERO_USD,
                                YearMonth.of(2010, 11), ZERO_USD,
                                YearMonth.of(2010, 12), ZERO_USD,
                                YearMonth.of(2011, 1), ZERO_USD
                        )));

        final var proportionalExpenses = SeriesReader.readSeries("expense/consorcio-reparaciones.json")
                .map((ym, amount) -> amount.adjust(ONE, COEFFICIENT));

        final var ongoingExpenses = Stream.concat(
                Stream.of("expense/inmobiliario-43.json", "expense/seguro.json", "expense/reparaciones.json")
                        .map(SeriesReader::readSeries),
                Stream.of(proportionalExpenses))
                .reduce(MoneyAmountSeries::add)
                .map(expenses -> expenses.exchangeInto("USD"))
                .map(usdExpenses -> USD_INFLATION.adjust(usdExpenses, limit.getYear(), limit.getMonth()))
                .get();

        final var allExpenses = new SimpleAggregation(1200).sum(ongoingExpenses.add(initialCostSeries));

        final var initialExpenseYM = allExpenses.getFrom();

        this.evolution("House cost evolution",
                allExpenses.map((ym, ma) -> ma.adjust(BigDecimal.valueOf(initialExpenseYM.monthsUntil(ym) + 1), ONE)), 120);

    }

    private void houseIrrecoverableCosts(YearMonth timeLimit) {

        final var limit = USD_INFLATION.getTo();

        final var proportionalExpenses = SeriesReader.readSeries("expense/consorcio-reparaciones.json")
                .map((ym, amount) -> amount.adjust(ONE, COEFFICIENT));

        final var realExpensesInUSD = Stream.concat(
                Stream.of("expense/inmobiliario-43.json", "expense/seguro.json", "expense/reparaciones.json").map(SeriesReader::readSeries),
                Stream.of(proportionalExpenses))
                .reduce(MoneyAmountSeries::add)
                .map(expenses -> expenses.exchangeInto("USD"))
                .map(usdExpenses -> USD_INFLATION.adjust(usdExpenses, limit.getYear(), limit.getMonth()))
                .map(s -> s.map((ym, amount) -> this.limit(timeLimit, ym, amount)))
                .map(MoneyAmountSeries::moneyAmountStream)
                .orElseGet(Stream::empty)
                .reduce(MoneyAmount::add)
                .orElse(ZERO_USD);

        this.buyVsRent(realExpensesInUSD, ZERO, timeLimit);
        this.buyVsRent(realExpensesInUSD, new BigDecimal("0.02"), timeLimit);
        this.buyVsRent(realExpensesInUSD, new BigDecimal("0.03"), timeLimit);
    }

    private MoneyAmount limit(YearMonth timeLimit, YearMonth ym, MoneyAmount amount) {
        return ym.compareTo(timeLimit) <= 0 ? amount : new MoneyAmount(ZERO, amount.getCurrency());
    }

    private void buyVsRent(MoneyAmount realExpensesInUSD, BigDecimal rate, YearMonth timeLimit) {
        final var limit = USD_INFLATION.getTo();

        final var nominalInitialCost = new BigDecimal("96000");
        final var nominalTransactionCost = nominalInitialCost.multiply(
                REALTOR_FEE.add(STAMP_TAX, CONTEXT)
                        .add(REGISTER_TAX, CONTEXT)
                        .add(NOTARY_FEE, CONTEXT),
                CONTEXT);

        final var start = YearMonth.of(2010, 8);
        final var realInitialCost = USD_INFLATION.adjust(new MoneyAmount(nominalInitialCost, "USD"),
                start.getYear(), start.getMonth(),
                limit.getYear(), limit.getMonth());

        final var months = BigDecimal.valueOf(start.monthsUntil(timeLimit));
        final var years = months.divide(BigDecimal.valueOf(12), CONTEXT);

        // interest rate cost
        final var opportunityCost = new MoneyAmount(
                nominalInitialCost
                        .add(nominalTransactionCost, CONTEXT)
                        .multiply(ONE.add(rate, CONTEXT).pow(years.intValue(), CONTEXT), CONTEXT)
                        .subtract(nominalInitialCost, CONTEXT), "USD");

        final var totalRealExpense = realExpensesInUSD.add(opportunityCost);

        this.appendLine(format("===< Costo de {0}/{1} a {2}/{3} con retorno anual de {4} >===",
                start.getMonth(),
                String.valueOf(start.getYear()),
                timeLimit.getMonth(),
                String.valueOf(timeLimit.getYear()),
                percent(rate)));

        this.appendLine(format("USD reales {0}/{1}", limit.getMonth(), String.valueOf(limit.getYear())));
        this.appendLine(format("\tTotal USD {0} {1}",
                currency(totalRealExpense.getAmount()),
                percent(totalRealExpense.getAmount().divide(realInitialCost.getAmount(), CONTEXT))));

        final var monthlyCost = totalRealExpense.getAmount().divide(months, CONTEXT);
        this.appendLine(format("\tMensual USD {0} {1} - ARS {2}",
                currency(monthlyCost),
                percent(monthlyCost.divide(realInitialCost.getAmount(), CONTEXT)),
                currency(ForeignExchanges.getForeignExchange("USD", "ARS")
                        .exchange(new MoneyAmount(monthlyCost, "USD"), "ARS", limit.getYear(), limit.getMonth())
                        .getAmount())));

        final var yearlyCost = totalRealExpense.getAmount().divide(years, CONTEXT);
        this.appendLine(format("\tAnual USD {0} {1}\n",
                currency(yearlyCost),
                percent(yearlyCost.divide(realInitialCost.getAmount(), CONTEXT))));

    }

    private void expenses(String[] args, String type) {

        final var params = this.paramsValue(args, type);

        final String exp = params.get("type");
        final int months = Integer.parseInt(params.getOrDefault("months", "12"));

        this.appendLine(format("===< Real USD expenses in the last {0} months >===", months));

        final var list = this.getRealUSDExpensesByType()
                .entrySet()
                .stream()
                .filter(p -> exp == null || exp.equals(p.getKey()))
                .map(e -> of(e.getKey(), this.aggregate(e.getValue(), s -> this.lastMonths(s, months)).getAmount()))
                .collect(toList());

        final var total = list.stream()
                .map(Pair::getSecond)
                .reduce(ZERO, BigDecimal::add);

        list.stream()
                .sorted(comparing((Pair<String, BigDecimal> p) -> p.getSecond()).reversed())
                .map(e -> format("{0}{1}{2}{3}",
                text(e.getFirst(), 13),
                text(" USD ", 4),
                currency(e.getSecond(), 10),
                pctBar(e.getSecond(), total)))
                .forEach(this::appendLine);

        this.appendLine(format("-----------------------------\n{0} USD {1}",
                text("Total", 5),
                currency(total, 10)));

    }

    private MoneyAmount aggregate(List<MoneyAmountSeries> mas, Function<MoneyAmountSeries, MoneyAmount> aggregation) {
        return mas.stream()
                .map(aggregation)
                .reduce(ZERO_USD, MoneyAmount::add);
    }

    private MoneyAmount lastMonths(MoneyAmountSeries s, int months) {

        var ym = USD_INFLATION.getTo();
        var amount = ZERO_USD;

        for (var i = 0; i < months; i++) {
            amount = amount.add(s.getAmountOrElseZero(ym));
            ym = ym.prev();
        }

        return amount;

    }

    private MoneyAmountSeries asRealUSDSeries(String fileName) {
        return this.asRealUSDSeries("saving/", fileName);
    }

    private MoneyAmountSeries asRealUSDSeries(String prefix, String fileName) {
        var limit = USD_INFLATION.getTo();
        return USD_INFLATION.adjust(
                SeriesReader.readSeries(prefix + fileName + ".json").exchangeInto("USD"),
                limit.getYear(),
                limit.getMonth());
    }

    private MoneyAmountSeries realSavings(String type) {

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
                    of("LIQ", "ahorros-oro"),
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
                .map(e -> e.getValue())
                .flatMap(Collection::stream)
                .reduce(MoneyAmountSeries::add)
                .get();
    }

    private MoneyAmountSeries realExpenses(String type) {

        return this.getRealUSDExpensesByType().entrySet()
                .stream()
                .filter(e -> type == null || e.getKey().equals(type))
                .map(e -> e.getValue())
                .flatMap(Collection::stream)
                .reduce(MoneyAmountSeries::add)
                .get();
    }

    private void evolutionReport(YearMonth ym, MoneyAmount mo, int scale) {
        this.appendLine(
                format("{0}/{1}", String.valueOf(ym.getYear()), String.format("%02d", ym.getMonth())),
                " ",
                currency(mo, 14),
                " ",
                this.bar(mo.getAmount(), scale));
    }

    private void percentEvolutionReport(YearMonth ym, BigDecimal mo) {
        this.appendLine(
                format("{0}/{1}", String.valueOf(ym.getYear()), String.format("%02d", ym.getMonth())),
                " ",
                percent(mo, 8),
                " ",
                this.bar(mo.movePointRight(2), 1));
    }

    private void numericEvolution(String name, MoneyAmountSeries s, int scale) {
        var limit = USD_INFLATION.getTo();

        s.forEach((ym, ma) -> this.evolutionReport(ym, ma, scale));

        appendLine("\n", name, " ", format("{0}/{1}", String.valueOf(limit.getYear()), limit.getMonth()));
    }

    private void evolution(String name, MoneyAmountSeries s, int scale) {
        var limit = USD_INFLATION.getTo();

        s.forEach((ym, ma) -> this.evolutionReport(ym, ma, scale));

        appendLine("\n", name, " real USD ", format("{0}/{1}", String.valueOf(limit.getYear()), limit.getMonth()));

    }

    private void savingsDistributionEvolution() {

        appendLine("===< Savings Distribution Evolution >===");

        final var cash = this.realSavings("LIQ");
        final var eq = this.realSavings("EQ");
        final var bo = this.realSavings("BO");

        final var nf = NumberFormat.getCurrencyInstance();

        cash.forEach((ym, cashMa) -> appendLine(
                this.bar(
                        ym,
                        cashMa.getAmount(),
                        eq.getAmountOrElseZero(ym).getAmount(),
                        bo.getAmountOrElseZero(ym).getAmount(),
                        1500,
                        value -> String.format("%13s", nf.format(value)))));

        appendLine("===< Savings Distribution Evolution >===");
        appendLine("");
        appendLine("References:");
        appendLine("#: cash, +: equity, %: bonds.");

    }

    private void savingsDistributionPercentEvolution() {

        appendLine("===< Savings Distribution Percent Evolution >===");

        final var cash = this.realSavings("LIQ");
        final var eq = this.realSavings("EQ");
        final var bo = this.realSavings("BO");

        cash.forEach((ym, cashMa) -> appendLine(
                this.percentBar(ym, cashMa, eq.getAmountOrElseZero(ym), bo.getAmountOrElseZero(ym))
        ));

        appendLine("===< Savings Distribution Percent Evolution >===");
        appendLine("");
        appendLine("References:");
        appendLine("#: cash, +: equity, %: bonds.");

    }

    private BigDecimal asPct(MoneyAmount ma, MoneyAmount total) {
        return ma.getAmount()
                .divide(total.getAmount(), CONTEXT)
                .movePointRight(2)
                .setScale(0, RoundingMode.HALF_UP);
    }

    private String percentBar(YearMonth ym, MoneyAmount one, MoneyAmount two) {
        final var total = one.add(two);

        if (total.getAmount().signum() == 0) {
            return "";
        }

        var bar1 = this.asPct(one, total);
        var bar2 = this.asPct(two, total);

        if (bar1.add(bar2, CONTEXT).compareTo(HUNDRED) != 0) {

            bar1 = HUNDRED.subtract(bar2, CONTEXT);

        }

        return this.bar(ym, bar1, bar2, 1, this::pctNumber);
    }

    private String percentBar(YearMonth ym, MoneyAmount one, MoneyAmount two, MoneyAmount three) {

        final var total = one.add(two).add(three);

        if (total.getAmount().signum() == 0) {
            return "";
        }

        var bar1 = this.asPct(one, total);
        var bar2 = this.asPct(two, total);
        var bar3 = this.asPct(three, total);

        if (bar1.add(bar2, CONTEXT).add(bar3, CONTEXT).compareTo(HUNDRED) != 0) {

            bar1 = HUNDRED.subtract(bar2.add(bar3, CONTEXT), CONTEXT);

        }

        return this.bar(ym, bar1, bar2, bar3, 1, this::pctNumber);

    }

    private String pctNumber(BigDecimal value) {
        return String.format("%3d", value.intValue()).concat("%");

    }

    private String bar(YearMonth ym, BigDecimal one, BigDecimal two, int scale, Function<BigDecimal, String> format) {
        return format("{0}/{1} [{2},{4}] {3}{5}",
                String.valueOf(ym.getYear()),
                String.format("%02d", ym.getMonth()),
                format.apply(one),
                this.bar(one, scale, "#"),
                format.apply(two),
                this.bar(two, scale, "+"));
    }

    private String bar(YearMonth ym, BigDecimal one, BigDecimal two, BigDecimal three, int scale, Function<BigDecimal, String> format) {
        return format("{0}/{1} [{2},{4},{6}] {3}{5}{7}",
                String.valueOf(ym.getYear()),
                String.format("%02d", ym.getMonth()),
                format.apply(one),
                this.bar(one, scale, "#"),
                format.apply(two),
                this.bar(two, scale, "+"),
                format.apply(three),
                this.bar(three, scale, "%"));
    }

    private void savingEvolution(String[] args, String paramName) {
        appendLine("===< Savings Evolution >===");
        this.evolution("Savings", this.realSavings(this.paramsValue(args, paramName).get("type")), 2500);
    }

    private void expenseEvolution(String[] args, String paramName) {
        appendLine("===< Expenses Evolution >===");
        this.expenseEvolution(this.paramsValue(args, paramName).get("type"));
    }

    private void expenseEvolution(String type) {

        this.evolution("Expenses", this.realExpenses(type), 15);
    }

    private void savingChange(String[] args, String paramName) {

        final var months = Integer.parseInt(this.paramsValue(args, paramName).getOrDefault("months", "1")) + 1;

        appendLine(format("===< {0}-month Savings Change >===", months - 1));
        this.evolution(format("{0}-month Savings Change", months - 1), new SimpleAggregation(months)
                .change(this.realSavings(null)), 50 * months);

    }

    private void savingsPercentChange(String[] args, String paramName) {

        final var months = Integer.parseInt(this.paramsValue(args, paramName).getOrDefault("months", "1")) + 1;

        appendLine(format("===< {0}-month Savings Change >===", months - 1));
        final var s = new SimpleAggregation(months)
                .percentChange(this.realSavings(null));

        var ym = s.getFrom();
        var limit = s.getTo();

        while (ym.compareTo(limit) <= 0) {

            this.percentEvolutionReport(ym, s.getIndex(ym.getYear(), ym.getMonth()));

            ym = ym.next();
        }

        //appendLine("\n", name, " real USD ", format("{0}/{1}", String.valueOf(limit.getYear()), limit.getMonth()));
    }

    private void expensesChange(String[] args, String name) {

        final var months = Integer.parseInt(this.paramsValue(args, name).getOrDefault("months", "12"));

        appendLine("===< Expenses Change >===");

        this.evolution(format("{0}-month average expenses change", months),
                new SimpleAggregation(2)
                        .change(new SimpleAggregation(months)
                                .average(this.realExpenses(null))), 5);
    }

    private MoneyAmountSeries realIncome() {

        return this.getIncomeSeries().stream()
                .reduce(MoneyAmountSeries::add)
                .get();
    }

    private void incomeEvolution() {

        appendLine("===< Income evolution >===");
        this.evolution("Income", this.realIncome(), 100);
    }

    private void incomeAverageEvolution(String[] args, String paramName) {
        var params = this.paramsValue(args, paramName);

        var months = Integer.parseInt(params.getOrDefault("months", "12"));

        appendLine(format("===< Income average {0}-month evolution >===", months));

        this.incomeAverageEvolution(months);

    }

    private void incomeAverageEvolution(int months) {

        this.evolution("Income " + String.valueOf(months) + "-month average",
                new SimpleAggregation(months)
                        .average(this.realIncome()),
                100);
    }

    private String bar(BigDecimal value, int scale) {

        final var symbol = value.signum() < 0 ? "-" : "+";

        return this.bar(value, scale, symbol);
    }

    private String bar(BigDecimal value, int scale, String symbol) {

        return IntStream.range(0, value.abs().divide(BigDecimal.valueOf(scale), CONTEXT).setScale(0, RoundingMode.HALF_UP).intValue())
                .mapToObj(x -> symbol)
                .collect(joining());
    }

    private double[] randomPeriods(List<double[]> allReturns, int periods, double fee) {
        return ThreadLocalRandom.current().ints(periods, 0, allReturns.size())
                .mapToObj(allReturns::get)
                .flatMapToDouble(Arrays::stream)
                .map(value -> value - fee)
                .toArray();
    }

    private Map<String, String> paramsValue(String[] args, String paramName) {
        return Arrays.stream(args)
                .dropWhile(p -> paramName.equals(p))
                .takeWhile(p -> p.contains("="))
                .map(PARAM_SEPARATOR::split)
                .collect(toMap(parts -> parts[0], parts -> parts[1]));
    }

    private void goal(String[] args, String paramName) {

        // trials=100000 period=20 retirement=63 age=97 w=1000 d=850 inflation=3 cash=-50000 sp500=true tax=true bbpp=2.25
        appendLine("===< Goals >===");

        final var params = this.paramsValue(args, paramName);

        final var trials = Integer.parseInt(params.getOrDefault("trials", "100000"));
        final var periodYears = Integer.parseInt(params.getOrDefault("period", "20"));
        final var deposit = Integer.parseInt(params.getOrDefault("d", "850"));
        final var withdraw = Integer.parseInt(params.getOrDefault("w", "1000"));
        final var inflation = Integer.parseInt(params.getOrDefault("inflation", "3"));
        final var retirementAge = Integer.parseInt(params.getOrDefault("retirement", "63"));
        final var age = Integer.parseInt(params.getOrDefault("age", "97"));
        final var extraCash = Integer.parseInt(params.getOrDefault("cash", "0"));
        final var onlySP500 = Boolean.parseBoolean(params.getOrDefault("sp500", "true"));
        final var afterTax = Boolean.parseBoolean(params.getOrDefault("tax", "true"));
        final var pension = Integer.parseInt(params.getOrDefault("pension", "50"));

        final var bbppTax = afterTax
                ? Double.parseDouble(params.getOrDefault("bbpp", "2.25")) / 100.0d
                : 0.0d;

        this.bbppMean = bbppTax * BBPP_FX_GAP_PERCENT;
        this.bbppVar = bbppTax / 10.0d;
        this.bbppMinFactor = 1.0d - bbppTax;

        final var buySellFee = ONE.setScale(6)
                .add(TRADING_FEE.multiply(IVA, CONTEXT), CONTEXT)
                .add(TRADING_FEE, CONTEXT)
                .add(TRADING_FEE, CONTEXT);

        this.goal(
                trials,
                periodYears,
                deposit,
                withdraw,
                inflation,
                retirementAge,
                buySellFee,
                BigDecimal.valueOf(extraCash),
                onlySP500,
                afterTax,
                age,
                pension);
    }

    private void goal(
            final int trials,
            final int periodYears,
            final int monthlyDeposit,
            final int monthlyWithdraw,
            final int inflation,
            final int retirementAge,
            final BigDecimal buySellFee,
            final BigDecimal extraCash,
            final boolean onlySP500,
            final boolean afterTax,
            final int age,
            final int pension) {

        final var tr = new TypeReference<List<AnnualHistoricalReturn>>() {
        };

        this.sp500TotalReturns = SeriesReader.read("index/sp-total-return.json", tr)
                .stream()
                .sorted(comparing(AnnualHistoricalReturn::getYear))
                .map(AnnualHistoricalReturn::getTotalReturn)
                .map(r -> ONE.setScale(6, MathConstants.ROUNDING_MODE).add(r.setScale(6, MathConstants.ROUNDING_MODE).movePointLeft(2), CONTEXT))
                .collect(toList());

        this.russell2000TotalReturns = SeriesReader.read("index/russell2000.json", tr)
                .stream()
                .sorted(comparing(AnnualHistoricalReturn::getYear))
                .map(AnnualHistoricalReturn::getTotalReturn)
                .map(r -> ONE.setScale(6, MathConstants.ROUNDING_MODE).add(r.setScale(6, MathConstants.ROUNDING_MODE).movePointLeft(2), CONTEXT))
                .collect(toList());

        final var to = USD_INFLATION.getTo();

        final var todaySavings = this.realSavings(null).getAmount(to);

        final var invested = this.realSavings("EQ").getAmount(to);

        final var cash = todaySavings.getAmount()
                .subtract(invested.getAmount(), CONTEXT)
                .add(extraCash, CONTEXT).doubleValue();

        final var inflationRate = ONE.setScale(6, MathConstants.ROUNDING_MODE)
                .add(BigDecimal.valueOf(inflation).setScale(6, MathConstants.ROUNDING_MODE).movePointLeft(2), CONTEXT).doubleValue();

        final var deposit = BigDecimal.valueOf(monthlyDeposit * 12).divide(buySellFee, CONTEXT).doubleValue();
        final var withdraw = BigDecimal.valueOf((monthlyWithdraw - pension) * 12)
                .multiply(buySellFee, CONTEXT)
                .multiply(afterTax ? CAPITAL_GAINS_TAX_EXTRA_WITHDRAWAL_PCT : ONE, CONTEXT).doubleValue();

        final var investedAmount = invested.getAmount().doubleValue();

        appendLine(format("Cash: {0,number,currency}, invested: {1,number,currency}", cash, investedAmount));
        appendLine(format(
                "Saving {0,number,currency}, spending {1,number,currency}",
                monthlyDeposit,
                monthlyWithdraw),
                afterTax ? " after tax." : ".");
        if (pension > 0) {
            appendLine(format("Considering {0,number,currency} pension.", pension));
        }
        appendLine(format("Expected {0}% inflation, retiring at {1}, until age {2} +/-{4}.", inflation, retirementAge, age, RETIREMENT_AGE_STD, END_AGE_STD));

        final int startingYear = to.getYear();
        final var end = 1978 + age;
        final var yearsLeft = 100;

        final var periods = (int) Math.ceil((float) yearsLeft / periodYears);

        final var inflationFactors = IntStream.range(0, 180)
                .mapToDouble(year -> Math.pow(inflationRate, year))
                .toArray();

        final var realDeposits = Arrays.stream(inflationFactors)
                .map(f -> f * deposit)
                .toArray();

        final var realWithdrawals = Arrays.stream(inflationFactors)
                .map(f -> f * withdraw)
                .toArray();

        final var allSP500Periods = this.periods(this.sp500TotalReturns, periodYears, 0.9d);
        final var allRussell2000Periods = this.periods(this.russell2000TotalReturns, periodYears, 0.9d);
        final var allEIMIPeriods = this.periods(this.sp500TotalReturns, periodYears, 0.75d);
        final var allMEUDPeriods = this.periods(this.sp500TotalReturns, periodYears, 0.70d);

        final var successes = IntStream.range(0, trials)
                .parallel()
                .mapToObj(i -> this.balanceProportions(periods, allSP500Periods, allRussell2000Periods, allEIMIPeriods, allMEUDPeriods, onlySP500, CSPX_FEE, XRSU_FEE, EIMI_FEE, MEUD_FEE))
                .filter(randomReturns
                        -> this.goals(
                        startingYear,
                        1978 + retirementAge,
                        gauss(end, END_AGE_STD),
                        cash,
                        investedAmount,
                        randomReturns,
                        realDeposits,
                        realWithdrawals))
                .count();

        appendLine(format("\nSimulating {0} {1}-year periods.", trials, periodYears));

        appendLine(format("{0}/{1} ", successes, trials),
                percent(BigDecimal.valueOf((double) successes / (double) trials)));

    }

    private double[] balanceProportions(int periods,
            List<double[]> allSP500Periods,
            List<double[]> allRussell2000Periods,
            List<double[]> allEIMIPeriods,
            List<double[]> allMEUDPeriods,
            boolean onlySP500,
            double sp500Fee,
            double russellFee,
            double eimiFee,
            double meudFee) {

        final var sp500Periods = this.randomPeriods(allSP500Periods, periods, sp500Fee);

        final var russell2000Periods = onlySP500
                ? sp500Periods
                : this.randomPeriods(allRussell2000Periods, periods, russellFee);

        final var eimiPeriods = onlySP500
                ? sp500Periods
                : this.randomPeriods(allEIMIPeriods, periods, eimiFee);

        final var meudPeriods = onlySP500
                ? sp500Periods
                : this.randomPeriods(allMEUDPeriods, periods, meudFee);

        return IntStream.range(0, sp500Periods.length)
                .mapToDouble(i -> sp500Periods[i] * SP500_PCT
                + russell2000Periods[i] * RUSSELL2000_PCT
                + meudPeriods[i] * MEUD_PCT
                + eimiPeriods[i] * EIMI_PCT)
                .toArray();
    }

    /**
     * Me quedo con el keepWorsePct % peor.
     */
    private List<double[]> periods(List<BigDecimal> returns, final int years, double keepWorsePct) {

        var periods = IntStream.range(0, returns.size() - years + 1)
                .mapToObj(start -> returns.stream().skip(start).limit(years).mapToDouble(BigDecimal::doubleValue).toArray())
                .sorted(comparing(this::sum))
                .collect(toList());

        if (keepWorsePct > 0.0d) {
            periods = periods.stream()
                    .limit(Math.round(periods.size() * keepWorsePct))
                    .collect(toList());
        }
        return periods;

    }

    private double sum(double[] l) {
        return Arrays.stream(l).sum();
    }

    private static double gauss(double mean, double std) {
        return mean + ThreadLocalRandom.current().nextGaussian() * std;
    }

    private static int gauss(int mean, int std) {

        return (int) Math.round(gauss((double) mean, (double) std));
    }

    private double bbppFactor() {

        return Math.max(
                this.bbppMinFactor,
                Math.min(
                        1.0d,
                        1.0d - gauss(this.bbppMean, this.bbppVar)));

    }

    private boolean goals(
            final int startingYear,
            final int retirement,
            final int end,
            final double cash,
            final double investedAmount,
            final double[] returns,
            final double[] deposit,
            final double[] withdraw) {

//        if(retirement - 1978 < 50 || end - 1978 < 50){
//            System.out.println("ret: " + (retirement - 1978) + " end: " + (end - 1978));
//        }
        double cashAmount = cash;
        double amount = investedAmount;
        // depositing
        for (var i = startingYear; i < retirement; i++) {

            // BB.PP.
            amount *= bbppFactor();

            amount = amount * returns[i - startingYear] + deposit[i - startingYear];
        }
        // withdrawing
        for (var i = retirement; i <= end; i++) {

            // BB.PP.
            amount *= bbppFactor();

            amount -= withdraw[i - startingYear];

            if (amount > 0.0d) {
                amount *= returns[i - startingYear];
            } else {
                cashAmount += amount;
                amount = 0.0d;
            }
            if (cashAmount <= 0.0d) {
                return false;
            }
        }

        return amount + cashAmount > 0.0d;
    }

    public List<Investment> getInvestments() {
        if (this.investments == null) {
            this.investments = SeriesReader.read("investments.json", TR);
        }

        return investments;
    }

    private void bbpp(String[] args, String paramName) {

        final var year = Integer.parseInt(this.paramsValue(args, paramName).getOrDefault("year", "2020"));

        appendLine("===< ", format("BB.PP. {0}", String.valueOf(year)), " >===");

        List<BBPPYear> bbppYears = SeriesReader.read("bbpp.json", new TypeReference<List<BBPPYear>>() {
        });

        final var date = Date.from(LocalDate.of(year, Month.DECEMBER, 31).atStartOfDay(ZoneId.systemDefault()).toInstant());

        final var bbpp = bbppYears
                .stream()
                .filter(y -> y.getYear() == year)
                .findAny()
                .get();

        final Map<String, Function<MoneyAmount, BigDecimal>> arsFunction = Map.of(
                "ARS", (MoneyAmount item) -> item.getAmount(),
                "LECAP", (MoneyAmount item) -> item.getAmount(),
                "EUR", (MoneyAmount item) -> item.getAmount().multiply(bbpp.getEur(), CONTEXT),
                "USD", (MoneyAmount item) -> item.getAmount().multiply(bbpp.getUsd(), CONTEXT),
                "LETE", (MoneyAmount item) -> item.getAmount().multiply(bbpp.getUsd(), CONTEXT),
                "XRSU", (MoneyAmount item)
                -> ForeignExchanges.getForeignExchange(item.getCurrency(), "USD")
                        .exchange(item, "USD", year, 12)
                        .getAmount()
                        .multiply(bbpp.getUsd(), CONTEXT),
                "CSPX", (MoneyAmount item)
                -> ForeignExchanges.getForeignExchange(item.getCurrency(), "USD")
                        .exchange(item, "USD", year, 12)
                        .getAmount()
                        .multiply(bbpp.getUsd(), CONTEXT),
                "EIMI", (MoneyAmount item)
                -> ForeignExchanges.getForeignExchange(item.getCurrency(), "USD")
                        .exchange(item, "USD", year, 12)
                        .getAmount()
                        .multiply(bbpp.getUsd(), CONTEXT),
                "MEUD", (MoneyAmount item)
                -> ForeignExchanges.getForeignExchange(item.getCurrency(), "EUR")
                        .exchange(item, "EUR", year, 12)
                        .getAmount()
                        .multiply(bbpp.getEur(), CONTEXT)
        );

        final var etfs = this.getInvestments()
                .stream()
                .filter(i -> i.isCurrent(date))
                .filter(i -> ETF.equals(i.getType()))
                .map(Investment::getInvestment)
                .map(i -> i.getMoneyAmount())
                .map(ma -> arsFunction.get(ma.getCurrency()).apply(ma))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final var ons = this.getInvestments()
                .stream()
                .filter(i -> i.isCurrent(date))
                .filter(i -> BONO.equals(i.getType()))
                .map(Investment::getInvestment)
                .map(i -> i.getMoneyAmount())
                //.peek(ma -> System.out.println(ma.getCurrency()))
                .map(ma -> arsFunction.get(ma.getCurrency()).apply(ma))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final var etfsItem = new BBPPItem();
        etfsItem.setCurrency("ARS");
        etfsItem.setDomestic(false);
        etfsItem.setExempt(false);
        etfsItem.setHolding(ONE);
        etfsItem.setName("ETFs");
        etfsItem.setValue(etfs);

        final var onsItem = new BBPPItem();
        onsItem.setCurrency("ARS");
        onsItem.setDomestic(true);
        onsItem.setExempt(false);
        onsItem.setHolding(ONE);
        onsItem.setName("ONs");
        onsItem.setValue(ons);

        final var homeCashItem = new BBPPItem();
        homeCashItem.setCurrency("ARS");
        homeCashItem.setDomestic(true);
        homeCashItem.setExempt(false);
        homeCashItem.setHolding(ONE);
        homeCashItem.setName("Home Cash");
        homeCashItem.setValue(BigDecimal.valueOf(5000l));

        bbpp.getItems().add(etfsItem);
        bbpp.getItems().add(onsItem);
        bbpp.getItems().add(homeCashItem);

        final var allArs = bbpp.getItems()
                .stream()
                .map(i -> this.toARS(i, bbpp.getUsd(), bbpp.getEur()))
                .collect(toList());

        final var totalAmount = allArs
                .stream()
                .map(i -> i.getValue().multiply(i.getHolding(), CONTEXT))
                .reduce(ZERO, BigDecimal::add);

        appendLine(format("Total amount {0}", currency(totalAmount)));

        final var taxedDomesticAmount = allArs
                .stream()
                .filter(BBPPItem::isDomestic)
                .filter(i -> !i.isExempt())
                .map(i -> i.getValue().multiply(i.getHolding(), CONTEXT))
                .reduce(ZERO, BigDecimal::add)
                .multiply(new BigDecimal("1.05"), CONTEXT);

        appendLine(format("Taxed domestic amount {0}", currency(taxedDomesticAmount)));

        final var taxedForeignAmount = allArs
                .stream()
                .filter(i -> !i.isDomestic())
                .filter(i -> !i.isExempt())
                .map(i -> i.getValue().multiply(i.getHolding(), CONTEXT))
                .reduce(ZERO, BigDecimal::add);

        appendLine(format("Taxed foreign amount {0}", currency(taxedForeignAmount)));

        final var taxedTotal = bbpp.getMinimum()
                .negate()
                .add(taxedDomesticAmount, CONTEXT)
                .add(taxedForeignAmount, CONTEXT);

        appendLine(format("Taxed total {0}", currency(taxedTotal)));

        final var taxRate = bbpp.getBrakets()
                .stream()
                .sorted(comparing(BBPPTaxBraket::getFrom))
                .filter(b -> b.getFrom().compareTo(totalAmount) <= 0)
                .reduce((left, right) -> right)
                .get()
                .getTax();

        appendLine(format("Tax rate {0}", percent(taxRate)));

        final var taxAmount = taxedTotal.multiply(taxRate, CONTEXT);

        final var usdTaxAmount = ForeignExchanges.getForeignExchange("ARS", "USD")
                .exchange(new MoneyAmount(taxAmount, "ARS"), "USD", year, 12);

        appendLine(format("Tax amount {0} / USD {1}",
                currency(taxAmount),
                currency(usdTaxAmount.getAmount())));

        appendLine(format("Monthly tax amount USD {0}", currency(usdTaxAmount.adjust(BigDecimal.valueOf(12), ONE).getAmount())));

        final var allInvested = this.getInvestments()
                .stream()
                .filter(i -> i.isCurrent(date))
                .map(Investment::getInvestment)
                .map(i -> i.getMoneyAmount())
                .map(ma -> ForeignExchanges.getForeignExchange(ma.getCurrency(), "USD").exchange(ma, "USD", year, 12))
                .reduce(ZERO_USD, MoneyAmount::add);

        final var yearRealIncome = new ArrayList<MoneyAmount>(12);

        this.realIncome()
                .forEachNonZero((ym, ma) -> Optional.of(ma).filter(m -> ym.getYear() == year).ifPresent(yearRealIncome::add));

        appendLine(format("Effective tax rate is {0}. Tax is {1} of investments. Tax is {2} of income.",
                percent(taxAmount.divide(totalAmount, CONTEXT)),
                percent(usdTaxAmount.getAmount().divide(allInvested.getAmount(), CONTEXT)),
                percent(usdTaxAmount.getAmount().divide(yearRealIncome.stream().map(MoneyAmount::getAmount).reduce(ZERO, BigDecimal::add), CONTEXT))));

        this.subtitle("Detail");

        appendLine(format("{0}{1}{2}{3}", text("", 16), text("      Value", 16), text("    %", 10), text("      Taxed", 16)));
        allArs.stream()
                .map(i -> format("{0}{1}{2}{3}",
                text(i.getName(), 16),
                currency(i.getValue(), 16),
                percent(i.getHolding(), 10),
                currency(i.getValue().multiply(i.isExempt() ? ZERO : i.getHolding(), CONTEXT), 16)))
                .forEach(this::appendLine);

    }

    private BBPPItem toARS(BBPPItem item, BigDecimal usdValue, BigDecimal eurValue) {
        if (item.getCurrency().equals("ARS")) {
            return item;
        }

        final var newItem = new BBPPItem();
        newItem.setCurrency("ARS");
        newItem.setDomestic(item.isDomestic());
        newItem.setExempt(item.isExempt());
        newItem.setHolding(item.getHolding());
        newItem.setName(item.getName());

        if (item.getCurrency().equals("USD")) {

            newItem.setValue(item.getValue().multiply(usdValue, CONTEXT));

        }
        if (item.getCurrency().equals("EUR")) {

            newItem.setValue(item.getValue().multiply(eurValue, CONTEXT));

        }
        return newItem;

    }

    private void averageSavedSalaries(String[] args, String name) {

        final var months = Integer.parseInt(this.paramsValue(args, name).getOrDefault("months", "12"));

        final var title = format("Average {0}-month real USD saved salaries", months);
        appendLine("===< ", title, " >===");

        final var savings = new SimpleAggregation(months).average(this.realSavings(null));
        final var income = new SimpleAggregation(months).average(this.realIncome());

        this.numericEvolution(
                title,
                income.map((ym, ma) -> new MoneyAmount(savings.getAmountOrElseZero(ym).getAmount().divide(ONE.max(ma.getAmount()), CONTEXT), ma.getCurrency())),
                2);
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
                "ahorros-oro",
                "ahorros-cspx",
                "ahorros-eimi",
                "ahorros-meud",
                "ahorros-conaafa",
                "ahorros-xrsu")
                .map(f -> "saving/" + f + ".json")
                .map(SeriesReader::readSeries)
                .collect(toList());
    }

    private void monthlySavings() {
        appendLine("===< Net monthly savings >===");

        this.evolution("Net savings", this.realNetSavings(), 100);
    }

    private void yearlySavings() {

        this.group("Net yearly savings", this.realNetSavings(), this.realIncome(), ym -> String.valueOf(ym.getYear()), 12);
    }

    private void yearlyIncome() {
        this.group("Yearly income", this.realIncome(), null, ym -> String.valueOf(ym.getYear()), 12);
    }

    private String half(YearMonth ym) {
        return format("{0}-H{1}", String.valueOf(ym.getYear()), ((ym.getMonth() - 1) / 6) + 1);
    }

    private String quarter(YearMonth ym) {
        return format("{0}-Q{1}", String.valueOf(ym.getYear()), ((ym.getMonth() - 1) / 3) + 1);
    }

    private void halfSavings() {

        this.group("Net half savings", this.realNetSavings(), this.realIncome(), this::half, 6);
    }

    private void halfIncome() {
        this.group("Half income", this.realIncome(), null, this::half, 6);
    }

    private void quarterSavings() {

        this.group("Net quarter savings", this.realNetSavings(), this.realIncome(), this::quarter, 3);
    }

    private void quarterIncome() {
        this.group("Quarter income", this.realIncome(), null, this::quarter, 3);
    }

    private void group(String title, MoneyAmountSeries series, MoneyAmountSeries comparisonSeries, Function<YearMonth, String> classifier, int months) {
        appendLine("===< " + title + " >===");

        final Map<String, MoneyAmount> byYear = new HashMap<>(32, 0.75f);

        series.forEachNonZero((ym, ma) -> byYear.merge(classifier.apply(ym), ma, MoneyAmount::add));

        final Map<String, Long> counts = series.yearMonthStream()
                .collect(Collectors.groupingBy(classifier, Collectors.counting()));

        final Map<String, MoneyAmount> comparisonByYear = new HashMap<>(32, 0.75f);

        if (comparisonSeries != null) {
            comparisonSeries.forEachNonZero((ym, ma) -> comparisonByYear.merge(classifier.apply(ym), ma, MoneyAmount::add));
        }

        byYear.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .forEach(e -> this.appendLine(format("{0} {1} {2} {3}",
                e.getKey(),
                currency(e.getValue().getAmount().divide(BigDecimal.valueOf(Math.min(months, counts.get(e.getKey()))), CONTEXT), 11),
                Optional.ofNullable(comparisonByYear.get(e.getKey()))
                        .map(comp -> this.pctNumber(e.getValue().getAmount().divide(comp.getAmount(), CONTEXT).movePointRight(2)))
                        .orElse(""),
                this.bar(e.getValue().getAmount().divide(BigDecimal.valueOf(Math.min(months, counts.get(e.getKey()))), CONTEXT), 50))));
    }

    private MoneyAmountSeries realNetSavings() {

        if (this.realNetSavings == null) {

            final var limit = USD_INFLATION.getTo();

            this.realNetSavings = this.savingsSeries()
                    .stream()
                    .map(new SimpleAggregation(2)::change)
                    .map(series -> series.exchangeInto("USD"))
                    .map(usdSeries -> USD_INFLATION.adjust(usdSeries, limit.getYear(), limit.getMonth()))
                    .reduce(MoneyAmountSeries::add)
                    .get();
        }
        return this.realNetSavings;
    }

    private void monthlySavings(String[] args, String name) {

        final var months = Integer.parseInt(this.paramsValue(args, name).getOrDefault("months", "12"));

        final var title = format("Average {0}-month net monthly savings", months);

        appendLine("===< ", title, " >===");

        this.evolution(title,
                new SimpleAggregation(months).average(this.realNetSavings()),
                50);
    }

    private void netAvgSavingPct(String[] args, String name) {

        final var months = Integer.parseInt(this.paramsValue(args, name).getOrDefault("months", "12"));

        final var title = format("===< Average {0}-month net monthly average savings percent >===", months);

        appendLine(title);

        final var agg = new SimpleAggregation(months);
        final var income = agg.average(this.realIncome());
        final var netSaving = agg.average(this.realNetSavings());

        netSaving.map((ym, ma) -> this.positiveOrZero(ma))
                .map((ym, ma) -> new MoneyAmount(income.getAmountOrElseZero(ym).getAmount().min(ma.getAmount()), ma.getCurrency()))
                .forEach((ym, savingMa) -> appendLine(this.percentBar(ym, savingMa, income.getAmountOrElseZero(ym).subtract(savingMa))));

        appendLine(title);
        appendLine("");
        appendLine("References:");
        appendLine("#: saved, +: spent.");

    }

    private void netAvgSavingSpentPct(String[] args, String name) {

        final var months = Integer.parseInt(this.paramsValue(args, name).getOrDefault("months", "12"));

        final var title = format("===< Average {0}-month net monthly average savings and spending percent >===", months);

        appendLine(title);

        final var agg = new SimpleAggregation(months);
        final var income = agg.average(this.realIncome());
        final var netSaving = agg.average(this.realNetSavings());
        final var spending = agg.average(this.realExpenses(null));

        netSaving.map((ym, ma) -> this.positiveOrZero(ma))
                .map((ym, ma) -> new MoneyAmount(income.getAmountOrElseZero(ym).getAmount().min(ma.getAmount()), ma.getCurrency()))
                .forEach((ym, savingMa) -> appendLine(
                this.percentBar(ym,
                        savingMa,
                        spending.getAmountOrElseZero(ym),
                        this.positiveOrZero(
                                income.getAmountOrElseZero(ym)
                                        .subtract(savingMa)
                                        .subtract(spending.getAmountOrElseZero(ym))))));

        appendLine(title);
        appendLine("");
        appendLine("References:");
        appendLine("#: saved, +: spent, %: other spending.");

    }

    private MoneyAmount positiveOrZero(MoneyAmount ma) {
        return new MoneyAmount(ZERO.max(ma.getAmount()), ma.getCurrency());
    }

    private void savingsIncomeTable() {

        final int[] years = new int[]{1, 2, 4, 6, 8, 10, 12, 14, 16};

        final var incomes = IntStream.of(years)
                .mapToObj(i -> Map.entry(i, this.incomeAverage(i)))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        final var savings = IntStream.of(years)
                .mapToObj(i -> Map.entry(i, this.savingsAverage(i)))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        this.appendLine("==< Average Income / Spending >==");
        this.appendLine(
                this.row(Stream.concat(
                        Stream.of("Years"),
                        IntStream.of(years).mapToObj(y -> format("-= {0} =-", y)))));
        this.appendLine(
                this.row(Stream.concat(
                        Stream.of("Income"),
                        IntStream.of(years)
                                .mapToObj(incomes::get)
                                .map(MoneyAmount::getAmount)
                                .map(ConsoleReports::currency))));
        this.appendLine(
                this.row(
                        Stream.concat(
                                Stream.of("Savings"),
                                IntStream.of(years)
                                        .mapToObj(savings::get)
                                        .map(MoneyAmount::getAmount)
                                        .map(ConsoleReports::currency))));
        this.appendLine(
                this.row(
                        Stream.concat(
                                Stream.of("Spending"),
                                IntStream.of(years)
                                        .mapToObj(y -> incomes.get(y).subtract(savings.get(y)))
                                        .map(MoneyAmount::getAmount)
                                        .map(ConsoleReports::currency))));
        this.appendLine(
                this.row(
                        Stream.concat(
                                Stream.of("Saving %"),
                                IntStream.of(years)
                                        .mapToObj(y -> savings.get(y).getAmount().divide(incomes.get(y).getAmount().subtract(ONE, CONTEXT), CONTEXT))
                                        .map(a -> format("{0}", percent(a))))));
    }

    private void yearSavingsIncomeTable() {

        final int[] years = IntStream.rangeClosed(1999, USD_INFLATION.getTo().getYear()).toArray();

        final var incomes = IntStream.of(years)
                .mapToObj(i -> Map.entry(i, this.yearIncome(i)))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        final var savings = IntStream.of(years)
                .mapToObj(i -> Map.entry(i, this.yearSavings(i)))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        this.appendLine("==<  Income / Spending by Year >==");

        this.appendLine(this.row(Stream.of("-= Year =-", "Income", "Savings", "Spending", "Saving %")));

        IntStream.of(years)
                .mapToObj(y -> this.row(Stream.of(
                format("-= {0} =-", String.valueOf(y) + (y == USD_INFLATION.getTo().getYear() ? "*" : "")),
                currency(incomes.get(y).getAmount()),
                currency(savings.get(y).getAmount()),
                currency(incomes.get(y).subtract(savings.get(y)).getAmount()),
                format("{0}", percent(
                        savings.get(y).getAmount()
                                .divide(incomes.get(y).getAmount()
                                        .subtract(ONE, CONTEXT), CONTEXT))))))
                .forEach(this::appendLine);

    }

    private MoneyAmount incomeAverage(int years) {

        return this.getIncomeSeries()
                .stream()
                .collect(reducing(MoneyAmountSeries::add))
                .map(new SimpleAggregation(years * 12)::average)
                .map(allRealUSDIncome -> allRealUSDIncome.getAmount(USD_INFLATION.getTo()))
                .orElse(ZERO_USD);

    }

    private MoneyAmount savingsAverage(int years) {
        return new SimpleAggregation(years * 12)
                .average(this.realNetSavings())
                .getAmount(USD_INFLATION.getTo());
    }

    private MoneyAmount yearIncome(int year) {

        final var months = year < USD_INFLATION.getTo().getYear()
                ? 12
                : USD_INFLATION.getTo().getMonth();

        return this.getIncomeSeries()
                .stream()
                .map(s -> s.filter((ym, ma) -> ym.getYear() == year))
                .flatMap(Function.identity())
                .reduce(ZERO_USD, MoneyAmount::add)
                .adjust(BigDecimal.valueOf(months), ONE);
    }

    private MoneyAmount yearSavings(int year) {

        final var months = year < USD_INFLATION.getTo().getYear()
                ? 12
                : USD_INFLATION.getTo().getMonth();

        return this.realNetSavings().filter((ym, ma) -> ym.getYear() == year)
                .reduce(ZERO_USD, MoneyAmount::add)
                .adjust(BigDecimal.valueOf(months), ONE);

    }

    private String row(Stream<String> values) {
        return values
                .map(this::cell)
                .collect(joining());
    }

    private String cell(String value) {
        return String.format("%12s", value);
    }

    private void portfolio(String[] args, String name) {

        final var params = this.paramsValue(args, name);

        final var type = params.getOrDefault("type", "full");
        final var subtype = params.getOrDefault("subtype", "all");

        final var limit = USD_INFLATION.getTo();

        final var year = Optional.ofNullable(params.get("y"))
                .map(Integer::parseInt)
                .orElseGet(limit::getYear);
        final var month = Optional.ofNullable(params.get("m"))
                .map(Integer::parseInt)
                .orElseGet(limit::getMonth);

        final var ym = YearMonth.of(year, month);

        final Map<String, Map<String, Optional<MoneyAmount>>> grouped
                = Stream.of(
                        of("BOND", this.lastAmount("ahorros-ay24", ym)),
                        of("BOND", this.lastAmount("ahorros-conbala", ym)),
                        of("BOND", this.lastAmount("ahorros-uva", ym)),
                        of("BOND", this.lastAmount("ahorros-dolar-ON", ym)),
                        of("BOND", this.lastAmount("ahorros-lecap", ym)),
                        of("BOND", this.lastAmount("ahorros-lete", ym)),
                        of("BOND", this.lastAmount("ahorros-caplusa", ym)),
                        of("CASH", this.lastAmount("ahorros-dolar-banco", ym)),
                        of("CASH", this.lastAmount("ahorros-peso", ym)),
                        of("CASH", this.lastAmount("ahorros-dolar-liq", ym)),
                        of("CASH", this.lastAmount("ahorros-euro", ym)),
                        of("CASH", this.lastAmount("ahorros-dai", ym)),
                        of("COMMODITY", this.lastAmount("ahorros-oro", ym)),
                        of("EQUITY", this.lastAmount("ahorros-cspx", ym)),
                        of("EQUITY", this.lastAmount("ahorros-eimi", ym)),
                        of("EQUITY", this.lastAmount("ahorros-meud", ym)),
                        of("EQUITY", this.lastAmount("ahorros-conaafa", ym)),
                        of("EQUITY", this.lastAmount("ahorros-xrsu", ym)))
                        .filter(p -> "all".equals(subtype) || p.getFirst().equalsIgnoreCase(subtype))
                        .collect(groupingBy(
                                Pair::getFirst,
                                groupingBy(
                                        p -> p.getSecond().getCurrency(),
                                        mapping(
                                                Pair::getSecond,
                                                reducing(MoneyAmount::add)))));

        final var items = grouped.entrySet().stream()
                .flatMap(e -> this.item(e.getKey(), e.getValue(), ym))
                .sorted(comparing((PortfolioItem pi) -> pi.getDollarAmount().getAmount()).reversed())
                .collect(toList());

        final var total = items.stream()
                .map(PortfolioItem::getDollarAmount)
                .reduce(ZERO_USD, MoneyAmount::add);

        final var pct = "pct".equals(type);

        items.stream()
                .map(i -> pct ? i.asPercentReport(total) : i.asReport(total))
                .forEach(this::appendLine);

        if (!pct) {
            this.appendLine("--------------------------------------");
            this.appendLine(format("Total {0}", currency(total.getAmount())));
        }
    }

    private Stream<PortfolioItem> item(String type, Map<String, Optional<MoneyAmount>> amounts, YearMonth ym) {

        return amounts.values()
                .stream()
                .flatMap(Optional::stream)
                .filter(ma -> !ma.isZero())
                .map(amount -> new PortfolioItem(amount, type, ym));
    }

    private MoneyAmount lastAmount(String seriesName, YearMonth ym) {
        return SeriesReader.readSeries("saving/".concat(seriesName).concat(".json")).getAmountOrElseZero(ym);
    }

    private void portfolioAllocation() {

        Map<String, Map<String, Optional<DayDollars>>> dayDollarsByYear = this.getInvestments()
                .stream()
                .flatMap(this::asDayDollarsByYear)
                .collect(groupingBy(
                        DayDollars::getYear,
                        groupingBy(DayDollars::getType, reducing(DayDollars::combine))));

        dayDollarsByYear.entrySet()
                .stream()
                .sorted(comparing(Map.Entry::getKey))
                .forEach(e -> this.allocationYear(e.getKey(), e.getValue()));

    }

    private void allocationYear(String year, Map<String, Optional<DayDollars>> byType) {
        this.appendLine("Year: ", year);

        final var total = byType.values()
                .stream()
                .flatMap(Optional::stream)
                .map(DayDollars::getAmount)
                .reduce(ZERO, BigDecimal::add);

        byType.values()
                .stream()
                .flatMap(Optional::stream)
                .sorted(comparing((DayDollars d) -> d.getAmount()).reversed())
                .map(d -> format("\t{0} {1}",
                String.format("%-11s", d.getType()),
                pctBar(d.getAmount(), total)))
                .forEach(this::appendLine);
        this.appendLine("");

    }

    private Stream<DayDollars> asDayDollarsByYear(Investment i) {

        return IntStream.rangeClosed(
                YearMonth.of(i.getIn().getDate()).getYear(),
                Optional.ofNullable(i.getOut())
                        .map(InvestmentEvent::getDate)
                        .map(YearMonth::of)
                        .map(YearMonth::getYear)
                        .orElse(USD_INFLATION.getTo().getYear()))
                .mapToObj(year -> this.dayDollarsInYear(year, i));

    }

    private DayDollars dayDollarsInYear(int year, Investment i) {

        final var yearStart = LocalDate.of(year, Month.JANUARY, 1);
        final var yearEnd = LocalDate.of(year, Month.DECEMBER, 31);

        final var investmentStart = LocalDate.ofInstant(i.getIn().getDate().toInstant(), ZoneId.systemDefault());

        final var investmentEnd = Optional.ofNullable(i.getOut())
                .map(InvestmentEvent::getDate)
                .map(Date::toInstant)
                .map(instant -> LocalDate.ofInstant(instant, ZoneId.systemDefault()))
                .orElse(LocalDate.now());

        final var to = min(yearEnd, investmentEnd);

        final var daysInvestedInYear = ChronoUnit.DAYS.between(
                max(yearStart, investmentStart),
                to.plusDays(1));

        final var usdInvested = ForeignExchanges.getForeignExchange(i.getCurrency(), "USD")
                .exchange(i.getMoneyAmount(), "USD", to.getYear(), to.getMonthValue());

        return new DayDollars(
                year,
                i.getType(),
                i.getCurrency(),
                usdInvested.getAmount().multiply(BigDecimal.valueOf(daysInvestedInYear), CONTEXT));

    }

    private static LocalDate min(LocalDate d1, LocalDate d2) {
        return d1.compareTo(d2) <= 0
                ? d1
                : d2;
    }

    private static LocalDate max(LocalDate d1, LocalDate d2) {
        return d1.compareTo(d2) >= 0
                ? d1
                : d2;
    }

    private static String text(String value, int width) {
        return String.format("%-" + width + "s", value);
    }

    private static String currency(BigDecimal value) {
        return format("{0,number,currency}", value);
    }

    private static String currency(BigDecimal value, int width) {
        return String.format("%" + width + "s", currency(value));
    }

    private static String currency(MoneyAmount value, int width) {
        return String.format("%" + width + "s", format("{0} {1}", value.getCurrency(), currency(value.getAmount())));
    }

    private static String percent(BigDecimal pct, int width) {

        return String.format("%" + width + "s", percent(pct));
    }
    
    private static String percent(BigDecimal pct) {

        return format("{0}", PERCENT_FORMAT.format(pct));
    }

    private static String pctBar(BigDecimal value, BigDecimal total) {
        return Optional.of(total)
                .filter(t -> t.signum() != 0)
                .map(t -> pctBar(value.divide(t, CONTEXT)))
                .orElse("");
    }

    private static String pctBar(BigDecimal value) {

        final var symbol = value.signum() >= 0 ? "#" : "%";

        if (value.abs().compareTo(ONE_PERCENT) < 0) {
            return String.format("%10s", "<1 %");
        }

        final var end = value.abs().movePointRight(2).intValue();

        if (end > 100) {

            final var part = IntStream.range(0, 48)
                    .mapToObj(i -> symbol)
                    .collect(joining());

            return format("{0} {1}",
                    percent(value, 10),
                    part + "/-/" + part);

        }

        return format("{0} {1}",
                percent(value, 10),
                IntStream.range(0, end)
                        .mapToObj(i -> symbol)
                        .collect(joining()));
    }

    private static String smallPctBar(BigDecimal value) {
        final var symbol = value.signum() < 0
                ? "-"
                : "+";

        final var steps = value.movePointRight(2)
                .abs()
                .divide(BigDecimal.TEN, CONTEXT)
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();

        final var stream = steps < 15
                ? IntStream.range(0, steps).mapToObj(x -> symbol)
                : Stream.concat(
                        Stream.concat(
                                IntStream.range(0, 6).mapToObj(x -> symbol),
                                Stream.of("/-/")),
                        IntStream.range(0, 6).mapToObj(x -> symbol));

        return String.format("%-15s", stream.collect(joining()));
    }

    private void inv(final Predicate<Investment> everyone, boolean nominal, String currency) {

        appendLine();
        appendLine(format("{0} Investment Results", nominal ? "Nominal" : "Real"));
        appendLine();

        final var ics = new InvestmentCostStrategy(currency, TRADING_FEE, TRADING_FX_FEE, new BigDecimal("0.21"), CAPITAL_GAINS_TAX_RATE);

        final var mw = 13;
        final var colWidths = new int[]{5, 11, 9, mw, mw, mw, 9, mw, 9, 10, 1, 15, 10, 7, 11, 7};
        var separator = IntStream.rangeClosed(0, Arrays.stream(colWidths).sum()).mapToObj(n -> "=").collect(Collectors.joining());
        var i = 0;
        this.appendLine(separator);
        this.appendLine(
                text(" ETF", colWidths[i++]),
                text("  Date", colWidths[i++]),
                text("  Price", colWidths[i++]),
                text("   Investment", colWidths[i++]),
                text("    Current", colWidths[i++]),
                text("     Profit", colWidths[i++]),
                text("    %", colWidths[i++]),
                text("  Net Profit", colWidths[i++]),
                text("    %", colWidths[i++]),
                text(" ", colWidths[i++]),
                text("CAGR", colWidths[i++]),
                text("", colWidths[i++]),
                text("   Fee", colWidths[i++]),
                text("%", colWidths[i++]),
                text("   Tax", colWidths[i++]),
                text("%", colWidths[i++]));
        this.appendLine(separator);
        this.getInvestments()
                .stream()
                .filter(Investment::isCurrent)
                .filter(inv -> inv.getType().equals(InvestmentType.ETF))
                .filter(everyone)
                .map(ics::details)
                .map(d -> nominal ? d : d.asReal())
                .forEach(d -> this.print(d, colWidths));

        final var benchmarks = SeriesReader.read("index/benchmarks.json", BENCHMARK_TR)
                .entrySet()
                .stream()
                .collect(toMap(
                        Map.Entry::getKey,
                        e -> nominal ? e.getValue() : real(e.getValue())));

        final var totalGrossGains = this.total(ics, everyone, InvestmentDetails::getGrossCapitalGains, nominal);
        final var totalNetGains = this.total(ics, everyone, InvestmentDetails::getNetCapitalGains, nominal);
        final var totalCurrent = this.total(ics, everyone, InvestmentDetails::getCurrentAmount, nominal);
        final var totalTax = this.total(ics, everyone, InvestmentDetails::getTaxes, nominal);
        final var totalFee = this.total(ics, everyone, InvestmentDetails::getFees, nominal);
        final var totalInvested = this.total(ics, everyone, InvestmentDetails::getInvestedAmount, nominal);

        final var totalCAGR = this.details(ics, everyone, id -> id.getCAGR().multiply(id.getInvestedAmount().getAmount(), CONTEXT), nominal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final var weightedCAGR = totalCAGR.divide(totalInvested, CONTEXT);

        this.subtitle("Total");

        this.appendLine(
                text("   Investment", mw),
                text("    Current", mw),
                text("     Profit", mw),
                text("     %", 9),
                text("  Net Profit", mw),
                text("    %", 9),
                text("    CAGR", 9),
                text("     Fee", 12),
                text("   %", 8),
                text("     Tax", 12),
                text("   %", 8));

        this.appendLine(
                currency(totalInvested, mw),
                currency(totalCurrent, mw),
                currency(totalGrossGains, mw),
                percent(totalGrossGains.divide(totalInvested, CONTEXT), 9),
                currency(totalNetGains, mw),
                percent(totalNetGains.divide(totalInvested, CONTEXT), 9),
                percent(weightedCAGR, 9),
                currency(totalFee, 12),
                percent(totalFee.divide(totalCurrent, CONTEXT), 8),
                currency(totalTax, 12),
                percent(totalTax.divide(totalCurrent, CONTEXT), 8));

        this.subtitle("Benchmark");

        final var benchmarksStream = benchmarks.entrySet()
                .stream()
                .map(e -> of(e.getKey(), this.benchmarkCAGR(e.getValue())));

        final var portfolioStream = Stream.of(of("Portfolio", weightedCAGR));
        final var modelPortfolioStream = Stream.of(of("Model", this.modelPortfolioCAGR(nominal)));

        Stream.concat(Stream.concat(benchmarksStream, portfolioStream), modelPortfolioStream)
                .sorted(comparing((Pair<String, BigDecimal> p) -> p.getSecond()).reversed())
                .map(p -> format("{0} {1}", text(p.getFirst(), 10), pctBar(p.getSecond())))
                .forEach(this::appendLine);
    }

    private static BigDecimal cagr(BigDecimal initial, BigDecimal current, LocalDate since) {
        final var days = (double) ChronoUnit.DAYS.between(since, LocalDate.now());
        final var profit = current.divide(initial, CONTEXT).subtract(ONE, CONTEXT);
        final double x = Math.pow(
                BigDecimal.ONE.add(profit).doubleValue(),
                365.0d / days) - 1.0d;
        return BigDecimal.valueOf(x);
    }

    private BigDecimal modelPortfolioCAGR(boolean nominal) {

        final var initialValues = Map.of(
                "XRSU", new BigDecimal("217.51"),
                "MEUD", new BigDecimal("159.19"),
                "CSPX", new BigDecimal("296.40"),
                "EIMI", new BigDecimal("28.32")
        );

        final var portfolio
                = List.of(
                        new MoneyAmount(BigDecimal.valueOf(70l), "CSPX"),
                        new MoneyAmount(BigDecimal.valueOf(10l), "MEUD"),
                        new MoneyAmount(BigDecimal.valueOf(10l), "XRSU"),
                        new MoneyAmount(BigDecimal.valueOf(10l), "EIMI"));

        final var initial = portfolio.stream()
                .map(ma -> new MoneyAmount(ma.getAmount().multiply(initialValues.get(ma.getCurrency()), CONTEXT), ma.getCurrency().equals("MEUD") ? "EUR" : "USD"))
                .map(ma -> ForeignExchanges.getForeignExchange(ma.getCurrency(), "USD").exchange(ma, "USD", 2019, 7))
                .map(MoneyAmount::getAmount)
                .reduce(ZERO, BigDecimal::add);

        final var current = portfolio.stream()
                .map(ma -> ForeignExchanges.getForeignExchange(ma.getCurrency(), "USD").exchange(ma, "USD", Inflation.USD_INFLATION.getTo().getYear(), Inflation.USD_INFLATION.getTo().getMonth()))
                .map(MoneyAmount::getAmount)
                .reduce(ZERO, BigDecimal::add);

        var i = new BenchmarkItem();
        i.setInitial(initial);
        i.setCurrent(current);
        if (!nominal) {
            i = this.real(i);
        }
        return this.benchmarkCAGR(i);

    }

    private BigDecimal benchmarkCAGR(BenchmarkItem item) {
        return cagr(item.getInitial(), item.getCurrent(), LocalDate.of(2019, Month.JULY, 24));
    }

    private <T> Stream<T> details(InvestmentCostStrategy ics, Predicate<Investment> predicate, Function<InvestmentDetails, T> f, boolean nominal) {
        return this.getInvestments()
                .stream()
                .filter(Investment::isCurrent)
                .filter(i -> i.getType().equals(InvestmentType.ETF))
                .filter(predicate)
                .map(ics::details)
                .map(d -> nominal ? d : d.asReal())
                .map(f);
    }

    private BigDecimal total(InvestmentCostStrategy ics, Predicate<Investment> predicate, Function<InvestmentDetails, MoneyAmount> f, boolean nominal) {

        return this.details(ics, predicate, f, nominal)
                .map(MoneyAmount::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void print(InvestmentDetails d, int[] colWidths) {

        var i = 0;
        this.appendLine(
                text(d.getInvestmentCurrency(), colWidths[i++]),
                text(DateTimeFormatter.ISO_LOCAL_DATE.format(d.getInvestmentDate()), colWidths[i++]),
                currency(d.getInvestmentPrice(), colWidths[i++]),
                currency(d.getInvestedAmount().getAmount(), colWidths[i++]),
                currency(d.getCurrentAmount().getAmount(), colWidths[i++]),
                currency(d.getGrossCapitalGains().getAmount(), colWidths[i++]),
                percent(d.getGrossCapitalGainsPercent(), colWidths[i++]),
                currency(d.getNetCapitalGains().getAmount(), colWidths[i++]),
                percent(d.getNetCapitalGainsPercent(), colWidths[i++]),
                percent(d.getCAGR(), colWidths[i++]),
                text(" ", colWidths[i++]),
                text(smallPctBar(d.getCAGR()), colWidths[i++]),
                currency(d.getFees().getAmount(), colWidths[i++]),
                percent(d.getFeePercent(), colWidths[i++]),
                currency(d.getTaxes().getAmount(), colWidths[i++]),
                percent(d.getTaxPercent(), colWidths[i++]));
    }
}
