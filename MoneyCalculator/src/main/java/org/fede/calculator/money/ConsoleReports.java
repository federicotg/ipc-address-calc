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
import static java.math.MathContext.DECIMAL64;
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
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static java.text.MessageFormat.format;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import static java.util.Map.entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
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
import org.fede.calculator.money.series.InvestmentAsset;
import org.fede.calculator.money.series.InvestmentEvent;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.AnnualHistoricalReturn;
import org.fede.calculator.money.series.BBPPItem;
import org.fede.calculator.money.series.BBPPTaxBraket;
import org.fede.calculator.money.series.BBPPYear;
import org.fede.calculator.money.series.SeriesReader;
import static org.fede.calculator.money.series.SeriesReader.readSeries;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
public class ConsoleReports {

    private static final BigDecimal COEFFICIENT = new BigDecimal("0.1223");

    private static final BigDecimal REALTOR_FEE = new BigDecimal("0.045");
    private static final BigDecimal STAMP_TAX = new BigDecimal("0.018");
    private static final BigDecimal REGISTER_TAX = new BigDecimal("0.006");
    private static final BigDecimal NOTARY_FEE = new BigDecimal("0.02").multiply(new BigDecimal("1.21", DECIMAL64));

    private static final BigDecimal RUSSELL2000_PCT = new BigDecimal("0.1");
    private static final BigDecimal SP500_PCT = new BigDecimal("0.7");
    private static final BigDecimal EIMI_PCT = new BigDecimal("0.1");
    private static final BigDecimal MEUD_PCT = new BigDecimal("0.1");

    private static final Pattern PARAM_SEPARATOR = Pattern.compile("=");

    private static final TypeReference<List<Investment>> TR = new TypeReference<List<Investment>>() {
    };
    private static final Collector<BigDecimal, ?, BigDecimal> REDUCER = reducing(ZERO.setScale(6, RoundingMode.HALF_UP), BigDecimal::add);
    private static final Collector<Investment, ?, BigDecimal> MAPPER = mapping(inv -> inv.getMoneyAmount().getAmount().setScale(6, RoundingMode.HALF_UP), REDUCER);

    private static final Comparator<Pair<Pair<String, String>, ?>> TYPE_CURRENCY_COMPARATOR = comparing((Pair<Pair<String, String>, ?> pair) -> pair.getFirst().getFirst())
            .thenComparing(comparing(pair -> pair.getFirst().getSecond()));

    private static <T, U, V> Pair<Pair<T, U>, V> ter(T t, U u, V v) {
        return of(of(t, u), v);
    }

    //private final NumberFormat nf = NumberFormat.getNumberInstance();
    private final NumberFormat percentFormat = NumberFormat.getPercentInstance();
    private List<Investment> investments;

    private List<BigDecimal> sp500TotalReturns;
    private List<BigDecimal> russell2000TotalReturns;
    private Map<String, List<MoneyAmountSeries>> realUSDSavingsByType;
    private Map<String, List<MoneyAmountSeries>> realUSDExpensesByType;

    private List<MoneyAmountSeries> incomeSeries;

    private final StringBuilder out;

    private ConsoleReports(StringBuilder out) {
        //this.nf.setMaximumFractionDigits(2);
        this.percentFormat.setMinimumFractionDigits(2);
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

        final MoneyAmountSeries cashSeries = SeriesReader.readSeries("saving/ahorros-dolar-liq.json");

        final MoneyAmount cash = cashSeries.getAmount(cashSeries.getTo());

        final var total = this.total(Investment::isCurrent, reportCurrency, limit).map(t -> t.add(cash));
        Stream.concat(getInvestments().stream()
                .filter(Investment::isCurrent)
                .collect(groupingBy(in -> of(in.getType().toString(), in.getCurrency()), MAPPER))
                .entrySet()
                .stream()
                .map(e -> of(e.getKey(), new MoneyAmount(e.getValue(), e.getKey().getSecond())))
                .map(p -> of(p.getFirst(), this.fx(p, reportCurrency))),
                Stream.of(ter("CASH", "USD", cash)))
                .sorted((p, q) -> q.getSecond().getAmount().compareTo(p.getSecond().getAmount()))
                .map(pair -> this.formatReport(total, pair.getSecond(), pair.getFirst().getFirst(), pair.getFirst().getSecond()))
                .forEach(this::appendLine);
        total
                .map(m -> format("Total: {0} -> {1,number,currency}", m.getCurrency(), m.getAmount()))
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

        final MoneyAmountSeries cashSeries = SeriesReader.readSeries("saving/ahorros-dolar-liq.json");

        final MoneyAmount cash = cashSeries.getAmount(cashSeries.getTo());

        final Optional<MoneyAmount> total = this.total(Investment::isCurrent, reportCurrency, limit).map(tot -> tot.add(cash));

        final Investment i = new Investment();
        final InvestmentEvent in = new InvestmentEvent();
        final InvestmentAsset asset = new InvestmentAsset();
        in.setAmount(cash.getAmount());
        in.setCurrency(cash.getCurrency());
        in.setDate(Date.from(LocalDate.now().minusYears(1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        i.setIn(in);
        asset.setAmount(cash.getAmount());
        asset.setCurrency(cash.getCurrency());
        i.setInvestment(asset);
        i.setType(InvestmentType.USD);

        Stream.concat(Stream.of(i), getInvestments().stream())
                .filter(Investment::isCurrent)
                .collect(groupingBy(
                        this::assetAllocation,
                        mapping(inv -> ForeignExchanges.getForeignExchange(inv.getInvestment().getCurrency(), reportCurrency)
                        .exchange(inv.getInvestment().getMoneyAmount(), reportCurrency, limit.getYear(), limit.getMonth())
                        .getAmount()
                        .setScale(6, RoundingMode.HALF_UP),
                                REDUCER)))
                .entrySet()
                .stream()
                .map(entry -> this.formatReport(total, new MoneyAmount(entry.getValue(), reportCurrency), entry.getKey(), reportCurrency))
                .forEach(this::appendLine);
        total
                .map(m -> format("Total: {0} -> {1,number,currency}", m.getCurrency(), m.getAmount()))
                .ifPresent(this::appendLine);
    }

    private MoneyAmount fx(Pair<Pair<String, String>, MoneyAmount> p, String reportCurrency) {

        final var limit = USD_INFLATION.getTo();

        return ForeignExchanges.getForeignExchange(p.getSecond().getCurrency(), reportCurrency).exchange(p.getSecond(), reportCurrency, limit.getYear(), limit.getMonth());
    }

    private String formatReport(Optional<MoneyAmount> total, MoneyAmount subtotal, String type, String currency) {
        return format("{0} {1}: {2,number,currency}. {3}", type, currency, subtotal.getAmount(),
                percentFormat
                        .format(total.map(tot -> subtotal.getAmount().divide(tot.getAmount(), MathConstants.CONTEXT))
                                .orElse(ZERO)));
    }

    private void pastInvestmentsProfit() {

        appendLine("===< Ganancia Inversiones Finalizadas en USD reales >===");
        this.pastInvestmentsRealProfit("CONBALA", FCI);
        this.pastInvestmentsRealProfit("CAPLUSA", FCI);
        this.pastInvestmentsRealProfit("CONAAFA", FCI);
        this.pastInvestmentsRealProfit("USD", PF);
        this.pastInvestmentsRealProfit("UVA", PF);
        this.pastInvestmentsRealProfit("ARS", PF);
        this.pastInvestmentsRealProfit("LECAP", BONO);
        this.pastInvestmentsRealProfit("LETE", BONO);
        this.pastInvestmentsRealProfit("AY24", BONO);
        this.pastInvestmentsRealProfit("USD", BONO);
    }

    private void pastInvestmentsRealProfit() {
        this.investmentsRealProfit(null, null, Investment::isPast, true);
    }

    private void globalInvestmentsRealProfit() {
        this.investmentsRealProfit(null, null, (inv) -> true, true);
    }

    private void currentInvestmentsRealProfit(String[] args, String type) {
        final var params = this.paramsValue(args, type);

        final var action = params.getOrDefault("type", "current");

        final Map<String, Runnable> actions = Map.ofEntries(
                entry("on", () -> this.currentInvestmentsRealProfit("USD", BONO)),
                entry("pf", () -> this.currentInvestmentsRealProfit("USD", PF)),
                entry("gold", () -> this.currentInvestmentsRealProfit("XAU", XAU)),
                entry("current", () -> this.currentInvestmentsRealProfit((String) null, (InvestmentType) null)),
                entry("cspx", () -> this.currentInvestmentsRealProfit("CSPX", ETF)),
                entry("eimi", () -> this.currentInvestmentsRealProfit("EIMI", ETF)),
                entry("meud", () -> this.currentInvestmentsRealProfit("MEUD", ETF)),
                entry("xrsu", () -> this.currentInvestmentsRealProfit("XRSU", ETF)));

        actions.getOrDefault(action, () -> this.appendLine("Unknown type: ", action)).run();

    }

    private void currentInvestmentsRealProfit(String currency, InvestmentType type) {
        this.investmentsRealProfit(currency, type, Investment::isCurrent, false);
    }

    private void pastInvestmentsRealProfit(String currency, InvestmentType type) {
        this.investmentsRealProfit(currency, type, Investment::isPast, true);
    }

    private void investmentsRealProfit(final String currency, final InvestmentType type, final Predicate<Investment> predicate, final boolean totalOnly) {

        final var currencyText = Optional.ofNullable(currency).map(c -> format(" {0}", c)).orElse("");

        if (!totalOnly) {
            if (type == null) {
                appendLine("===< Ganancia en Inversiones Actuales", currencyText, " en USD reales >===");

            } else {
                appendLine("===< Ganancia en Inversiones Actuales en ", type.toString(), currencyText, " en USD reales >===");
            }
        }

        final var tax = new BigDecimal("0.15");
        final var fee = new BigDecimal("0.0193");

        final var realProfits = this.getInvestments().stream()
                .filter(predicate)
                .filter(i -> type == null || i.getType().equals(type))
                .filter(i -> currency == null || i.getCurrency().equals(currency))
                .sorted(comparing(Investment::getInitialDate))
                .map(i -> ETF.equals(i.getType()) ? new RealProfit(i, tax, fee) : new RealProfit(i))
                .collect(toList());

        realProfits
                .stream()
                .filter(i -> !totalOnly)
                .map(RealProfit::toString)
                .forEach(this::appendLine);

        this.totalRealProfitReportLine(realProfits, type, totalOnly, currencyText + " gross", RealProfit::getRealInitialAmount, RealProfit::getRealProfit);

        this.totalRealProfitReportLine(realProfits, type, totalOnly, currencyText + " net  ", RealProfit::getRealInitialAmount, RealProfit::getAfterFeesAndTaxesProfit);

    }

    private void totalRealProfitReportLine(
            List<RealProfit> realProfits,
            InvestmentType type,
            boolean totalOnly,
            String currencyText,
            Function<RealProfit, MoneyAmount> initialFunction,
            Function<RealProfit, MoneyAmount> totalFunction) {

        final BigDecimal total = this.totalSum(realProfits, initialFunction);
        final BigDecimal profit = this.totalSum(realProfits, totalFunction);

        final BigDecimal pct = total.compareTo(ZERO) > 0
                ? profit.divide(total, MathConstants.CONTEXT)
                : ZERO;

        this.appendLine(format("{4} {0,number,currency} => {5,number,currency} {1,number,currency} {2} {3}",
                total,
                profit,
                this.percentFormat.format(pct),
                RealProfit.plusMinus(pct),
                Stream.of(type, currencyText)
                        .filter(t -> totalOnly)
                        .filter(Objects::nonNull)
                        .map(Object::toString)
                        .collect(joining("", "", " ")),
                total.add(profit)));

    }

    private BigDecimal totalSum(List<RealProfit> realProfits, Function<RealProfit, MoneyAmount> totalFunction) {

        return realProfits
                .stream()
                .map(totalFunction)
                .map(MoneyAmount::getAmount)
                .collect(reducing(ZERO, BigDecimal::add));
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
                        .orElseGet(() -> new MoneyAmount(ZERO, "USD"))
                        .getAmount();
        
        this.appendLine(format("Total income: {0,number,currency}",totalIncome));
        
        
        
    }

    private List<MoneyAmountSeries> getIncomeSeries() {

        if (this.incomeSeries == null) {

            final var limit = USD_INFLATION.getTo();
            this.incomeSeries = Stream.of(readSeries("income/lifia.json"), readSeries("income/unlp.json"), readSeries("income/despegar.json"))
                    .map(incomeSeries -> incomeSeries.exchangeInto("USD"))
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
                .orElseGet(() -> new MoneyAmount(ZERO, "USD"));

        this.appendLine(format("===< Average {0}-month income in {1}/{2} real USD >===", 
                months, 
                limit.getMonth(), 
                String.valueOf(limit.getYear())));

        this.appendLine("\tIncome: ",
                averageRealUSDIncome.getCurrency(),
                " ",
                format("{0,number,currency}", averageRealUSDIncome.getAmount()));

        final var savingPct = new MoneyAmount(averageRealUSDIncome.getAmount().multiply(new BigDecimal("0.3")), averageRealUSDIncome.getCurrency());

        this.appendLine("30% saving: ",
                averageRealUSDIncome.getCurrency(),
                " ",
                format("{0,number,currency}", savingPct.getAmount()),
                " / ",
                format("{0,number,currency}",
                        ForeignExchanges.getForeignExchange(savingPct.getCurrency(), "ARS").exchange(savingPct, "ARS", limit.getYear(), limit.getMonth()).getAmount()));
    
        appendLine(format("Saved salaries {0}", 
                this.realSavings(null).getAmount(limit).getAmount()
                        .divide(averageRealUSDIncome.getAmount(), DECIMAL64)));
    
    }

    public static void main(String[] args) {
        try {

            final var me = new ConsoleReports(new StringBuilder(1024));

            final Map<String, Runnable> actions = Map.ofEntries(
                    entry("past", me::pastInvestmentsProfit),
                    entry("i", me::investments),
                    entry("gi", me::groupedInvestments),
                    entry("ti", me::listStockByTpe),
                    entry("current", () -> me.currentInvestmentsRealProfit(args, "current")),
                    entry("allpast", me::pastInvestmentsRealProfit),
                    entry("global", me::globalInvestmentsRealProfit),
                    //savings
                    entry("savings", me::savings),
                    entry("savings-evo", () -> me.savingEvolution(args, "savings-evo")),
                    entry("savings-change", me::savingChange),
                    entry("savings-dist", me::savingsDistributionEvolution),
                    entry("savings-dist-pct", me::savingsDistributionPercentEvolution),
                    //income
                    entry("income", () -> me.income(args, "income")),
                    entry("income-evo", me::incomeEvolution),
                    entry("income-avg-evo", () -> me.incomeAverageEvolution(args, "income-avg-evo")),
                    //house cost
                    entry("house", () -> me.houseIrrecoverableCosts(USD_INFLATION.getTo())),
                    entry("house1", () -> me.houseIrrecoverableCosts(new YearMonth(2011, 8))),
                    entry("house3", () -> me.houseIrrecoverableCosts(new YearMonth(2013, 8))),
                    entry("house5", () -> me.houseIrrecoverableCosts(new YearMonth(2015, 8))),
                    //expenses
                    entry("expenses", () -> me.expenses(args, "expenses")),
                    entry("expenses-evo", () -> me.expenseEvolution(args, "expenses-evo")),
                    entry("expenses-change", me::expensesChange),
                    //goal
                    entry("goal", () -> me.goal(args, "goal")),
                    entry("bbpp", () -> me.bbpp(args, "bbpp"))
            );

            final var params = Arrays.stream(args)
                    .map(String::toLowerCase)
                    .collect(toSet());

            if (params.contains("help")) {

                final var help = Map.of(
                        "goal", "trials=100000 period=10 retirement=65 w=1000 d=500 inflation=2 cash=cash sp500=false",
                        "current", "type=(current* | on | pf | gold | cspx | eimi | meud | xrsu)",
                        "income", "months=12",
                        "income-avg-evo", "months=12",
                        "expenses", "type=(taxes | insurance | phone | services | home | entertainment) months=12",
                        "expenses-evo", "type=(taxes | insurance | phone | services | home | entertainment)",
                        "savings-evo", "type=(BO | LIQ | EQ)"
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

        // total savings
        final var limit = USD_INFLATION.getTo();

        final var totalSavings = this.realSavings(null).getAmount(limit);

        // total income
        final var totalIncome = this.realIncome()
                .moneyAmountStream()
                .reduce(MoneyAmount::add)
                .get();

        final var months = this.realIncome().getFrom().monthsUntil(limit);

        final var avgSalary = totalIncome.getAmount().divide(BigDecimal.valueOf(months), DECIMAL64);

        // % saved
        appendLine(format("Income USD {0,number,currency}\nSavings USD {1,number,currency} {2}\nAverage salary {3,number,currency}\nSaved salaries {4}",
                totalIncome.getAmount(),
                totalSavings.getAmount(),
                this.percentFormat.format(totalSavings.getAmount().divide(totalIncome.getAmount(), DECIMAL64)),
                avgSalary,
                totalSavings.getAmount().divide(avgSalary, DECIMAL64)));
        
        
        //saved salaries
    }

    private MoneyAmount applyCoefficient(YearMonth ym, MoneyAmount amount) {
        return new MoneyAmount(amount.getAmount().multiply(COEFFICIENT, DECIMAL64), amount.getCurrency());
    }

    private void houseIrrecoverableCosts(YearMonth timeLimit) {

        final var limit = USD_INFLATION.getTo();

        final var proportionalExpenses = SeriesReader.readSeries("expense/consorcio-reparaciones.json")
                .map(this::applyCoefficient);

        final var realExpensesInUSD = Stream.concat(
                Stream.of("expense/inmobiliario-43.json", "expense/seguro.json", "expense/reparaciones.json").map(SeriesReader::readSeries),
                Stream.of(proportionalExpenses))
                .reduce(MoneyAmountSeries::add)
                .map(expenses -> expenses.exchangeInto("USD"))
                .map(usdExpenses -> Inflation.USD_INFLATION.adjust(usdExpenses, limit.getYear(), limit.getMonth()))
                .map(s -> s.map((ym, amount) -> this.limit(timeLimit, ym, amount)))
                .map(MoneyAmountSeries::moneyAmountStream)
                .orElseGet(Stream::empty)
                .reduce(MoneyAmount::add)
                .orElseGet(() -> new MoneyAmount(ZERO, "USD"));

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
                REALTOR_FEE.add(STAMP_TAX, DECIMAL64)
                        .add(REGISTER_TAX, DECIMAL64)
                        .add(NOTARY_FEE, DECIMAL64),
                DECIMAL64);

        final var start = new YearMonth(2010, 8);
        final var realInitialCost = USD_INFLATION.adjust(new MoneyAmount(nominalInitialCost, "USD"),
                start.getYear(), start.getMonth(),
                limit.getYear(), limit.getMonth());

        final var months = BigDecimal.valueOf(start.monthsUntil(timeLimit));
        final var years = months.divide(BigDecimal.valueOf(12), DECIMAL64);

        // interest rate cost
        final var opportunityCost = new MoneyAmount(
                nominalInitialCost
                        .add(nominalTransactionCost, DECIMAL64)
                        .multiply(ONE.add(rate, DECIMAL64).pow(years.intValue(), DECIMAL64), DECIMAL64)
                        .subtract(nominalInitialCost, DECIMAL64), "USD");

        final var totalRealExpense = realExpensesInUSD.add(opportunityCost);

        this.appendLine(format("===< Costo de {0}/{1} a {2}/{3} con retorno anual de {4} >===",
                start.getMonth(),
                String.valueOf(start.getYear()),
                timeLimit.getMonth(),
                String.valueOf(timeLimit.getYear()),
                percentFormat.format(rate)));

        this.appendLine(format("USD reales {0}/{1}", limit.getMonth(), String.valueOf(limit.getYear())));
        this.appendLine(format("\tTotal USD {0,number,currency} {1}",
                totalRealExpense.getAmount(),
                percentFormat.format(totalRealExpense.getAmount().divide(realInitialCost.getAmount(), DECIMAL64))));

        final var monthlyCost = totalRealExpense.getAmount().divide(months, DECIMAL64);
        this.appendLine(format("\tMensual USD {0,number,currency} {1} - ARS {2,number,currency}",
                monthlyCost,
                percentFormat.format(monthlyCost.divide(realInitialCost.getAmount(), DECIMAL64)),
                ForeignExchanges.getForeignExchange("USD", "ARS")
                        .exchange(new MoneyAmount(monthlyCost, "USD"), "ARS", limit.getYear(), limit.getMonth())
                        .getAmount()));

        final var yearlyCost = totalRealExpense.getAmount().divide(years, DECIMAL64);
        this.appendLine(format("\tAnual USD {0,number,currency} {0}\n",
                yearlyCost,
                percentFormat.format(yearlyCost.divide(realInitialCost.getAmount(), DECIMAL64))));

    }

    private void expenses(String[] args, String type) {

        final var params = this.paramsValue(args, type);

        final String exp = params.get("type");
        final int months = Integer.parseInt(params.getOrDefault("months", "12"));

        this.appendLine("Real USD expenses in the last ",
                String.valueOf(months),
                " months.");

        this.getRealUSDExpensesByType()
                .entrySet()
                .stream()
                .filter(p -> exp == null || exp.equals(p.getKey()))
                .map(e -> of(e.getKey(), this.asRealUSD(e.getValue(), s -> this.lastMonths(s, months)).getAmount()))
                .sorted(comparing(Pair::getSecond))
                .forEach(e -> this.appendLine(e.getFirst(), " USD ", format("{0,number,currency} ", e.getSecond())));
    }

    private MoneyAmount asRealUSD(List<MoneyAmountSeries> mas, Function<MoneyAmountSeries, MoneyAmount> aggregation) {
        return mas.stream()
                .map(aggregation)
                .reduce(new MoneyAmount(ZERO, "USD"), MoneyAmount::add);

    }

    private MoneyAmount lastMonths(MoneyAmountSeries s, int months) {

        var ym = Inflation.USD_INFLATION.getTo();
        var amount = new MoneyAmount(ZERO, "USD");

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
        return Inflation.USD_INFLATION.adjust(
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

        return this.getRealUSDExpensesByType().entrySet().stream()
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
                String.format("%14s", format("{0,number,currency} ", mo.getAmount())),
                " ",
                this.bar(mo.getAmount(), scale));
    }

    private void evolution(String name, MoneyAmountSeries s, int scale) {
        var limit = USD_INFLATION.getTo();

        s.forEach((ym, ma) -> this.evolutionReport(ym, ma, scale));

        appendLine("\n", name, " real USD ", format("{0}/{1}", String.valueOf(limit.getYear()), limit.getMonth()));

    }

    private void savingsDistributionEvolution() {
        final var cash = this.realSavings("LIQ");
        final var eq = this.realSavings("EQ");
        final var bo = this.realSavings("BO");

        cash.forEach((ym, cashMa) -> appendLine(
                this.bar(ym, cashMa.getAmount(), eq.getAmountOrElseZero(ym).getAmount(), bo.getAmountOrElseZero(ym).getAmount(), 1500)));
    }

    private void savingsDistributionPercentEvolution() {
        final var cash = this.realSavings("LIQ");
        final var eq = this.realSavings("EQ");
        final var bo = this.realSavings("BO");

        cash.forEach((ym, cashMa) -> appendLine(
                this.percentBar(ym, cashMa, eq.getAmountOrElseZero(ym), bo.getAmountOrElseZero(ym))
        ));
    }

    private BigDecimal asPct(MoneyAmount ma, MoneyAmount total) {
        return ma.getAmount()
                .divide(total.getAmount(), DECIMAL64)
                .movePointRight(2)
                .setScale(0, RoundingMode.HALF_UP);
    }

    private String percentBar(YearMonth ym, MoneyAmount one, MoneyAmount two, MoneyAmount three) {

        final var total = one.add(two).add(three);

        if (total.getAmount().signum() == 0) {
            return "";
        }

        final var hundred = BigDecimal.valueOf(100);

        var bar1 = this.asPct(one, total);
        var bar2 = this.asPct(two, total);
        var bar3 = this.asPct(three, total);

        if (bar1.add(bar2, DECIMAL64).add(bar3, DECIMAL64).compareTo(hundred) != 0) {

            bar1 = hundred.subtract(bar2.add(bar3, DECIMAL64), DECIMAL64);

        }

        return this.bar(ym, bar1, bar2, bar3, 1);

    }

    private String bar(YearMonth ym, BigDecimal one, BigDecimal two, BigDecimal three, int scale) {
        return format("{0}/{1} {2}{3}{4}",
                String.valueOf(ym.getYear()),
                String.format("%02d", ym.getMonth()),
                this.bar(one, scale, "#"),
                this.bar(two, scale, "+"),
                this.bar(three, scale, "%"));
    }

    private void savingEvolution(String[] args, String paramName) {

        this.evolution("Savings", this.realSavings(this.paramsValue(args, paramName).get("type")), 2500);
    }

    private void expenseEvolution(String[] args, String paramName) {
        this.expenseEvolution(this.paramsValue(args, paramName).get("type"));
    }

    private void expenseEvolution(String type) {

        this.evolution("Expenses", this.realExpenses(type), 15);
    }

    private void savingChange() {

        this.evolution("Savings change", new SimpleAggregation(2)
                .change(this.realSavings(null)), 100);

    }

    private void expensesChange() {

        this.evolution("12-month average expenses change",
                new SimpleAggregation(2)
                        .change(new SimpleAggregation(12)
                                .average(this.realExpenses(null))), 5);
    }

    private MoneyAmountSeries realIncome() {

        return this.getIncomeSeries().stream()
                .reduce(MoneyAmountSeries::add)
                .get();
    }

    private void incomeEvolution() {

        this.evolution("Income", this.realIncome(), 100);
    }

    private void incomeAverageEvolution(String[] args, String paramName) {
        var params = this.paramsValue(args, paramName);

        var months = Integer.parseInt(params.getOrDefault("months", "12"));

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

        return IntStream.range(0, value.abs().divide(BigDecimal.valueOf(scale), DECIMAL64).setScale(0, RoundingMode.HALF_UP).intValue())
                .mapToObj(x -> symbol)
                .collect(joining());
    }

    private List<BigDecimal> randomPeriods(List<List<BigDecimal>> allReturns, int periods) {
        return ThreadLocalRandom.current().ints(periods, 0, allReturns.size())
                .mapToObj(allReturns::get)
                .flatMap(Collection::stream)
                .collect(toList());
    }

    private Map<String, String> paramsValue(String[] args, String paramName) {
        return Arrays.stream(args)
                .dropWhile(p -> paramName.equals(p))
                .takeWhile(p -> p.contains("="))
                .map(PARAM_SEPARATOR::split)
                .collect(toMap(parts -> parts[0], parts -> parts[1]));
    }

    private void goal(String[] args, String paramName) {

        final var params = this.paramsValue(args, paramName);

        final var trials = Integer.parseInt(params.getOrDefault("trials", "100000"));
        final var periodYears = Integer.parseInt(params.getOrDefault("period", "10"));
        final var deposit = Integer.parseInt(params.getOrDefault("d", "500"));
        final var withdraw = Integer.parseInt(params.getOrDefault("w", "1000"));
        final var inflation = Integer.parseInt(params.getOrDefault("inflation", "2"));
        final var retirementAge = Integer.parseInt(params.getOrDefault("retirement", "65"));
        final var extraCash = Integer.parseInt(params.getOrDefault("cash", "0"));
        final var onlySP50 = Boolean.parseBoolean(params.getOrDefault("sp500", "false"));

        final var buySellFee = ONE.setScale(6)
                .add(new BigDecimal("0.006").multiply(new BigDecimal("1.21", DECIMAL64)));

        this.goal(trials, periodYears, deposit, withdraw, inflation, retirementAge, buySellFee, BigDecimal.valueOf(extraCash), onlySP50);
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
            final boolean onlySP500) {

        final var tr = new TypeReference<List<AnnualHistoricalReturn>>() {
        };

        this.sp500TotalReturns = SeriesReader.read("index/sp-total-return.json", tr)
                .stream()
                .sorted(comparing(AnnualHistoricalReturn::getYear))
                .map(AnnualHistoricalReturn::getTotalReturn)
                .map(r -> ONE.setScale(6).add(r.setScale(6).movePointLeft(2), DECIMAL64))
                .collect(toList());

        this.russell2000TotalReturns = SeriesReader.read("index/russell2000.json", tr)
                .stream()
                .sorted(comparing(AnnualHistoricalReturn::getYear))
                .map(AnnualHistoricalReturn::getTotalReturn)
                .map(r -> ONE.setScale(6).add(r.setScale(6).movePointLeft(2), DECIMAL64))
                .collect(toList());

        final var to = Inflation.USD_INFLATION.getTo();

        final var todaySavings = this.realSavings(null).getAmount(to);

        final var invested = this.realSavings("EQ").getAmount(to);

        final var cash = todaySavings.getAmount()
                .subtract(invested.getAmount(), DECIMAL64)
                .add(extraCash, DECIMAL64);

        final var inflationRate = ONE.setScale(6)
                .add(BigDecimal.valueOf(inflation).setScale(6).movePointLeft(2), DECIMAL64);

        final var deposit = BigDecimal.valueOf(monthlyDeposit * 13).divide(buySellFee, DECIMAL64);
        final var withdraw = BigDecimal.valueOf(monthlyWithdraw * 12).multiply(buySellFee, DECIMAL64);;

        final var investedAmount = invested.getAmount();

        appendLine("Cash: ", format("{0,number,currency}", cash),
                ", invested: ", format("{0,number,currency}", invested.getAmount()));
        appendLine("Saving ", format("{0,number,currency}", monthlyDeposit),
                ", spending ", format("{0,number,currency}", monthlyWithdraw));
        appendLine("Expected inflation: ", String.valueOf(inflation), "%",
                ", retiring at ", String.valueOf(retirementAge)
        );

        final int startingYear = to.getYear();
        final var end = 2078;
        final var yearsLeft = end - startingYear + 1;

        final var periods = Math.round((float) yearsLeft / periodYears);

        final var inflationFactors = IntStream.range(0, yearsLeft)
                .mapToObj(year -> inflationRate.pow(year, DECIMAL64))
                .collect(toList());

        final var realDeposits = inflationFactors.stream()
                .limit(1978 + retirementAge - startingYear)
                .map(f -> f.multiply(deposit, DECIMAL64))
                .collect(toList());

        final var realWithdrawals = inflationFactors.stream()
                .map(f -> f.multiply(withdraw, DECIMAL64))
                .collect(toList());

        final var allSP500Periods = this.periods(this.sp500TotalReturns, periodYears, 0.85d);
        final var allRussell2000Periods = this.periods(this.russell2000TotalReturns, periodYears, 0.8d);
        final var allEIMIPeriods = this.periods(this.sp500TotalReturns, periodYears, 0.75d);
        final var allMEUDPeriods = this.periods(this.russell2000TotalReturns, periodYears, 0.70d);

        final var successes = IntStream.range(0, trials)
                .mapToObj(i -> this.balanceProportions(periods, allSP500Periods, allRussell2000Periods, allEIMIPeriods, allMEUDPeriods, onlySP500))
                .filter(randomReturns -> this.goals(startingYear, 1978 + retirementAge, end, cash, investedAmount, randomReturns, realDeposits, realWithdrawals))
                .count();

        appendLine(format("\nSimulating {0} {1}-year periods.", trials, periodYears));

        appendLine(format("{0}/{1} ", successes, trials),
                this.percentFormat.format((double) successes / (double) trials));

    }

    private List<BigDecimal> balanceProportions(int periods,
            List<List<BigDecimal>> allSP500Periods,
            List<List<BigDecimal>> allRussell2000Periods,
            List<List<BigDecimal>> allEIMIPeriods,
            List<List<BigDecimal>> allMEUDPeriods,
            boolean onlySP500) {

        final var sp500Periods = this.randomPeriods(allSP500Periods, periods);
        final var russell2000Periods = onlySP500
                ? sp500Periods
                : this.randomPeriods(allRussell2000Periods, periods);
        final var eimiPeriods = onlySP500
                ? sp500Periods
                : this.randomPeriods(allEIMIPeriods, periods);
        final var meudPeriods = onlySP500
                ? sp500Periods
                : this.randomPeriods(allMEUDPeriods, periods);

        return IntStream.range(0, sp500Periods.size())
                .mapToObj(i
                        -> sp500Periods.get(i).multiply(SP500_PCT, DECIMAL64)
                        .add(russell2000Periods.get(i).multiply(RUSSELL2000_PCT, DECIMAL64), DECIMAL64)
                        .add(meudPeriods.get(i).multiply(MEUD_PCT, DECIMAL64), DECIMAL64)
                        .add(eimiPeriods.get(i).multiply(EIMI_PCT, DECIMAL64), DECIMAL64))
                .collect(toList());
    }

    /**
     * Me quedo con el keepWorsePct % peor.
     */
    private List<List<BigDecimal>> periods(List<BigDecimal> returns, final int years, double keepWorsePct) {

        var periods = IntStream.range(0, returns.size() - years + 1)
                .mapToObj(start -> returns.stream().skip(start).limit(years).collect(toList()))
                .sorted(comparing(this::sum))
                .collect(toList());

        periods = periods.stream()
                .limit(Math.round(periods.size() * keepWorsePct))
                .collect(toList());

        return periods;

    }

    private BigDecimal sum(List<BigDecimal> l) {
        return l.stream().reduce(BigDecimal::add).get();
    }

    private boolean goals(
            final int startingYear,
            final int retirement,
            final int end,
            final BigDecimal cash,
            final BigDecimal investedAmount,
            final List<BigDecimal> returns,
            final List<BigDecimal> deposit,
            final List<BigDecimal> withdraw) {

        BigDecimal cashAmount = cash;
        BigDecimal amount = investedAmount;
        // depositing
        for (var i = startingYear; i < retirement; i++) {

            amount = amount.multiply(returns.get(i - startingYear), DECIMAL64)
                    .add(deposit.get(i - startingYear), DECIMAL64);
        }
        // withdrawing
        for (var i = retirement; i <= end; i++) {

            amount = amount.subtract(withdraw.get(i - startingYear), DECIMAL64);

            if (amount.signum() > 0) {
                amount = amount.multiply(returns.get(i - startingYear), DECIMAL64);
            } else {
                cashAmount = cashAmount.add(amount, DECIMAL64);
                amount = ZERO;
            }
            if (cashAmount.signum() <= 0) {
                return false;
            }
        }

        return amount.add(cashAmount, DECIMAL64).signum() > 0;
    }

    public List<Investment> getInvestments() {
        if (this.investments == null) {
            this.investments = SeriesReader.read("investments.json", TR);
        }

        return investments;
    }

    private void bbpp(String[] args, String paramName) {

        final var year = Integer.parseInt(this.paramsValue(args, paramName).getOrDefault("year", "2020"));

        List<BBPPYear> bbppYears = SeriesReader.read("bbpp.json", new TypeReference<List<BBPPYear>>() {
        });

        var date = Date.from(LocalDate.of(year, Month.DECEMBER, 31).atStartOfDay(ZoneId.systemDefault()).toInstant());

        final var bbpp = bbppYears
                .stream()
                .filter(y -> y.getYear() == year)
                .findAny()
                .get();

        final var etfs = this.getInvestments()
                .stream()
                .filter(i -> i.isCurrent(date))
                .filter(i -> ETF.equals(i.getType()))
                .map(Investment::getInvestment)
                .map(i -> i.getMoneyAmount())
                .map(ma -> ForeignExchanges.getForeignExchange(ma.getCurrency(), "USD").exchange(ma, "USD", year, 12))
                .reduce(new MoneyAmount(ZERO, "USD"), MoneyAmount::add);

        final var ons = this.getInvestments()
                .stream()
                .filter(i -> i.isCurrent(date))
                .filter(i -> BONO.equals(i.getType()))
                .map(Investment::getInvestment)
                .map(i -> i.getMoneyAmount())
                .map(ma -> ForeignExchanges.getForeignExchange(ma.getCurrency(), "USD").exchange(ma, "USD", year, 12))
                .reduce(new MoneyAmount(ZERO, "USD"), MoneyAmount::add);

        final var etfsItem = new BBPPItem();
        etfsItem.setCurrency(etfs.getCurrency());
        etfsItem.setDomestic(false);
        etfsItem.setExempt(false);
        etfsItem.setHolding(ONE);
        etfsItem.setName("ETFs");
        etfsItem.setValue(etfs.getAmount());

        final var onsItem = new BBPPItem();
        onsItem.setCurrency(ons.getCurrency());
        onsItem.setDomestic(true);
        onsItem.setExempt(false);
        onsItem.setHolding(ONE);
        onsItem.setName("ONs");
        onsItem.setValue(ons.getAmount());

        bbpp.getItems().add(etfsItem);
        bbpp.getItems().add(onsItem);

        final var allArs = bbpp.getItems()
                .stream()
                .map(i -> this.toARS(i, bbpp.getUsd(), bbpp.getEur()))
                .collect(toList());

        final var totalAmount = allArs
                .stream()
                .map(i -> i.getValue().multiply(i.getHolding(), DECIMAL64))
                .reduce(ZERO, BigDecimal::add);

        appendLine(format("Total amount {0,number,currency}", totalAmount));

        final var taxedDomesticAmount = allArs
                .stream()
                .filter(BBPPItem::isDomestic)
                .filter(i -> !i.isExempt())
                .map(i -> i.getValue().multiply(i.getHolding(), DECIMAL64))
                .reduce(ZERO, BigDecimal::add)
                .multiply(new BigDecimal("1.05"), DECIMAL64);

        appendLine(format("Taxed domestic amount {0,number,currency}", taxedDomesticAmount));

        final var taxedForeignAmount = allArs
                .stream()
                .filter(i -> !i.isDomestic())
                .filter(i -> !i.isExempt())
                .map(i -> i.getValue().multiply(i.getHolding(), DECIMAL64))
                .reduce(ZERO, BigDecimal::add);

        appendLine(format("Taxed foreign amount {0,number,currency}", taxedForeignAmount));

        final var taxedTotal = bbpp.getMinimum()
                .negate()
                .add(taxedDomesticAmount, DECIMAL64)
                .add(taxedForeignAmount, DECIMAL64);

        appendLine(format("Taxed total {0,number,currency}", taxedTotal));

        final var taxRate = bbpp.getBrakets()
                .stream()
                .sorted(comparing(BBPPTaxBraket::getFrom))
                .filter(b -> b.getFrom().compareTo(totalAmount) <= 0)
                .reduce((left, right) -> right)
                .get()
                .getTax();

        appendLine(format("Tax rate {0}", this.percentFormat.format(taxRate)));

        final var taxAmount = taxedTotal.multiply(taxRate, DECIMAL64);

        final var usdTaxAmount = ForeignExchanges.getForeignExchange("ARS", "USD")
                .exchange(new MoneyAmount(taxAmount, "ARS"), "USD", year, 12);

        appendLine(format("Tax amount {0,number,currency} / USD {1,number,currency}",
                taxAmount,
                usdTaxAmount.getAmount()));

        appendLine(format("Monthly tax amount USD {0,number,currency}", usdTaxAmount.adjust(BigDecimal.valueOf(12), ONE).getAmount()));

        final var allInvested = this.getInvestments()
                .stream()
                .filter(i -> i.isCurrent(date))
                .map(Investment::getInvestment)
                .map(i -> i.getMoneyAmount())
                .map(ma -> ForeignExchanges.getForeignExchange(ma.getCurrency(), "USD").exchange(ma, "USD", year, 12))
                .reduce(new MoneyAmount(ZERO, "USD"), MoneyAmount::add);

        appendLine(format("Effective tax rate is {0}. Tax is {1} of investments.",
                this.percentFormat.format(taxAmount.divide(totalAmount, DECIMAL64)),
                this.percentFormat.format(usdTaxAmount.getAmount().divide(allInvested.getAmount(), DECIMAL64))));

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

            newItem.setValue(item.getValue().multiply(usdValue, DECIMAL64));

        }
        if (item.getCurrency().equals("EUR")) {

            newItem.setValue(item.getValue().multiply(eurValue, DECIMAL64));

        }
        return newItem;

    }

}
