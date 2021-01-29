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
import static java.math.RoundingMode.HALF_UP;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import static java.util.Map.entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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
import org.fede.calculator.money.series.IndexSeriesSupport;
import org.fede.calculator.money.series.SeriesReader;
import static org.fede.calculator.money.series.SeriesReader.readSeries;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
public class ConsoleReports {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private static final BigDecimal COEFFICIENT = new BigDecimal("0.1223");

    private static final BigDecimal REALTOR_FEE = new BigDecimal("0.045");
    private static final BigDecimal STAMP_TAX = new BigDecimal("0.018");
    private static final BigDecimal REGISTER_TAX = new BigDecimal("0.006");
    private static final BigDecimal IVA = new BigDecimal("1.21");
    private static final BigDecimal NOTARY_FEE = new BigDecimal("0.02")
            .multiply(IVA, CONTEXT);

    private static final BigDecimal TRADING_FEE = new BigDecimal("0.006");

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

    private final NumberFormat percentFormat = NumberFormat.getPercentInstance();
    private List<Investment> investments;

    private List<BigDecimal> sp500TotalReturns;
    private List<BigDecimal> russell2000TotalReturns;
    private Map<String, List<MoneyAmountSeries>> realUSDSavingsByType;
    private Map<String, List<MoneyAmountSeries>> realUSDExpensesByType;

    private List<MoneyAmountSeries> incomeSeries;

    private final StringBuilder out;

    private ConsoleReports(StringBuilder out) {
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
                        .format(total.map(tot -> subtotal.getAmount().divide(tot.getAmount(), CONTEXT))
                                .orElse(ZERO)));
    }

    private void globalInvestmentsProfit(boolean nominal) {
        appendLine("===< Past and Current Investments Profit in ", nominal ? "nominal" : "real", " USD >===");
        this.investmentsProfit(null, null, (inv) -> true, true, nominal);
    }

    private void currentInvestmentsProfit(String currency, InvestmentType type, boolean nominal) {
        this.investmentsProfit(currency, type, Investment::isCurrent, false, nominal);

//        appendLine(format("\nClosing fee and tax: {0}. Capital gains tax rate: {1}",
//                percentFormat.format(TRADING_FEE.multiply(IVA, CONTEXT)
//                        .add(TRADING_FEE, CONTEXT)
//                        .add(TRADING_FEE, CONTEXT)),
//                "15 %"));
        appendLine("");
        appendLine("Investment: invested amount ignoring openning fees and taxes");
        appendLine("Current: currently invested amount");
        appendLine("Profit: capital gains before fees and taxes");
        appendLine("Net Profit: capital gains after fees and taxes if closing today");
        appendLine("CAGR: annualized real return after fees and taxes / target > 2.25 %");
        appendLine("Fee: opening fee and taxes, closing and ccl fees and taxes");
        appendLine("Tax: capital gains tax as of today.");

    }

    private void pastInvestmentsProfit(String currency, InvestmentType type, boolean nominal) {
        this.investmentsProfit(currency, type, Investment::isPast, false, nominal);
    }

    private void investmentsProfit(final String currency, final InvestmentType type, final Predicate<Investment> predicate, final boolean totalOnly, boolean nominal) {

        final var currencyText = Optional.ofNullable(currency).map(c -> format(" {0}", c)).orElse("");

        if (!totalOnly) {
            if (type == null) {
                appendLine("===< Ganancia en Inversiones", currencyText, " en USD ", nominal ? "nominales" : "reales", " >===");
            } else {
                appendLine("===< Ganancia en Inversiones en ", type.toString(), " ", currencyText, " en USD ", nominal ? "nominales" : "reales", " >===");
            }
        }

        final var tax = new BigDecimal("0.15");
        final var fee = TRADING_FEE.multiply(IVA, CONTEXT)
                .add(TRADING_FEE, CONTEXT)
                .add(TRADING_FEE, CONTEXT);

        final var inflation = nominal
                ? new CPIInflation(IndexSeriesSupport.CONSTANT_SERIES, "USD")
                : Inflation.USD_INFLATION;

        final var realProfits = this.getInvestments().stream()
                .filter(predicate)
                .filter(i -> type == null || i.getType().equals(type))
                .filter(i -> currency == null || i.getCurrency().equals(currency))
                .sorted(comparing(Investment::getInitialDate))
                .map(i -> this.asReport(i, tax, fee, inflation))
                .collect(toList());

        if (!totalOnly) {
            appendLine("\n   Date       Investment   Current      Profit     %        Net Profit    %      CAGR                        Fee       %        Tax       %");
        }

        realProfits
                .stream()
                .filter(i -> !totalOnly)
                .map(InvestmentReport::toString)
                .forEach(this::appendLine);

        final var totalFee = realProfits.stream()
                .map(InvestmentReport::feeAmount)
                .map(MoneyAmount::getAmount)
                .reduce(ZERO, BigDecimal::add);

        final var totalTax = realProfits.stream()
                .map(InvestmentReport::capitalGainsTax)
                .map(MoneyAmount::getAmount)
                .reduce(ZERO, BigDecimal::add);

        final var currentTotal = realProfits.stream()
                .map(InvestmentReport::getCurrentValue)
                .map(MoneyAmount::getAmount)
                .reduce(ZERO, BigDecimal::add);

        appendLine("\nAfter fee investment  Before fee/tax current     Profit     %");
        this.totalRealProfitReportLine(realProfits, type, totalOnly, currencyText, InvestmentReport::getNetRealInvestment, InvestmentReport::getGrossRealProfit);

        appendLine("\nTotal investment      After fee/tax current      Profit     %");
        this.totalRealProfitReportLine(realProfits, type, totalOnly, currencyText, InvestmentReport::getGrossRealInvestment, InvestmentReport::getNetRealProfit);

        appendLine(format("\nTotal fee {0,number,currency} ({2}). Total tax {1,number,currency} ({3}).",
                totalFee,
                totalTax,
                this.percentFormat.format(totalFee.divide(currentTotal, CONTEXT)),
                this.percentFormat.format(totalTax.divide(currentTotal, CONTEXT))
        ));

    }

    private InvestmentReport asReport(Investment i, BigDecimal tax, BigDecimal fee, Inflation inflation) {
        if (i.getType().equals(ETF) && i.getOut() == null) {
            return new InvestmentReport(inflation, i, tax, fee, IVA);
        }
        return new InvestmentReport(inflation, i, ZERO, ZERO, ONE);
    }

    private static String plusMinus(BigDecimal pct) {

        final var sign = pct.signum() >= 0
                ? "+"
                : "-";

        return IntStream.range(0, pct.abs().movePointRight(2).setScale(0, HALF_UP).intValue())
                .mapToObj(index -> sign)
                .collect(joining());
    }

    private void totalRealProfitReportLine(
            List<InvestmentReport> realProfits,
            InvestmentType type,
            boolean totalOnly,
            String currencyText,
            Function<InvestmentReport, MoneyAmount> initialFunction,
            Function<InvestmentReport, MoneyAmount> totalFunction) {

        final BigDecimal total = this.totalSum(realProfits, initialFunction);
        final BigDecimal profit = this.totalSum(realProfits, totalFunction);

        final BigDecimal pct = total.compareTo(ZERO) > 0
                ? profit.divide(total, CONTEXT)
                : ZERO;

        this.appendLine(format("{4} {0,number,currency}            {5,number,currency}          {1,number,currency}  {2}  {3}",
                total,
                profit,
                this.percentFormat.format(pct),
                plusMinus(pct),
                Stream.of(type, currencyText)
                        .filter(t -> totalOnly)
                        .filter(Objects::nonNull)
                        .map(Object::toString)
                        .collect(joining("", "", " ")),
                total.add(profit)));
    }

    private BigDecimal totalSum(List<InvestmentReport> realProfits, Function<InvestmentReport, MoneyAmount> totalFunction) {

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

        this.appendLine(format("Total income: {0,number,currency}", totalIncome));

    }

    private List<MoneyAmountSeries> getIncomeSeries() {

        if (this.incomeSeries == null) {

            final var limit = USD_INFLATION.getTo();
            this.incomeSeries = Stream.of(readSeries("income/lifia.json"), readSeries("income/unlp.json"), readSeries("income/despegar.json"))
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
                .orElseGet(() -> new MoneyAmount(ZERO, "USD"));

        this.appendLine(format("===< Average {0}-month income in {1}/{2} real USD >===",
                months,
                limit.getMonth(),
                String.valueOf(limit.getYear())));

        this.appendLine("\tIncome: ",
                averageRealUSDIncome.getCurrency(),
                " ",
                format("{0,number,currency}", averageRealUSDIncome.getAmount()));

        final var savingPct = new MoneyAmount(averageRealUSDIncome.getAmount().multiply(new BigDecimal("0.3"), CONTEXT), averageRealUSDIncome.getCurrency());

        this.appendLine("30% saving: ",
                averageRealUSDIncome.getCurrency(),
                " ",
                format("{0,number,currency}", savingPct.getAmount()),
                " / ",
                format("{0,number,currency}",
                        ForeignExchanges.getForeignExchange(savingPct.getCurrency(), "ARS").exchange(savingPct, "ARS", limit.getYear(), limit.getMonth()).getAmount()));

        appendLine(format("Saved salaries {0}",
                this.realSavings(null).getAmount(limit).getAmount()
                        .divide(averageRealUSDIncome.getAmount(), CONTEXT)));

    }

    private void invReport(String[] args, String paranName) {

        final var params = this.paramsValue(args, paranName);

        final var nominal = Boolean.parseBoolean(params.getOrDefault("nominal", "false"));

        final var type = params.getOrDefault("type", "current");

        final var subtype = params.getOrDefault("subtype", "all");

        final Runnable defaultAction = () -> this.appendLine(format("Unknown type: \"{0}\" subtype: \"{1}\".", type, subtype));

        if (type.equals("current")) {

            final Map<String, Runnable> actions = Map.ofEntries(
                    entry("on", () -> this.currentInvestmentsProfit("USD", BONO, nominal)),
                    entry("pf", () -> this.currentInvestmentsProfit("USD", PF, nominal)),
                    entry("gold", () -> this.currentInvestmentsProfit("XAU", XAU, nominal)),
                    entry("all", () -> this.currentInvestmentsProfit((String) null, (InvestmentType) null, nominal)),
                    entry("cspx", () -> this.currentInvestmentsProfit("CSPX", ETF, nominal)),
                    entry("eimi", () -> this.currentInvestmentsProfit("EIMI", ETF, nominal)),
                    entry("meud", () -> this.currentInvestmentsProfit("MEUD", ETF, nominal)),
                    entry("xrsu", () -> this.currentInvestmentsProfit("XRSU", ETF, nominal)));

            actions.getOrDefault(subtype, defaultAction).run();
        } else if (type.equals("past")) {

            final Map<String, Runnable> actions = Map.of(
                    "conbala", () -> this.pastInvestmentsProfit("CONBALA", FCI, nominal),
                    "caplusa", () -> this.pastInvestmentsProfit("CAPLUSA", FCI, nominal),
                    "conaafa", () -> this.pastInvestmentsProfit("CONAAFA", FCI, nominal),
                    "usd", () -> {
                        this.pastInvestmentsProfit("USD", PF, nominal);
                        this.pastInvestmentsProfit("USD", BONO, nominal);
                    },
                    "uva", () -> this.pastInvestmentsProfit("UVA", PF, nominal),
                    "ars", () -> this.pastInvestmentsProfit("ARS", PF, nominal),
                    "lecap", () -> this.pastInvestmentsProfit("LECAP", BONO, nominal),
                    "lete", () -> this.pastInvestmentsProfit("LETE", BONO, nominal),
                    "all", () -> this.pastInvestmentsProfit(null, null, nominal),
                    "ay24", () -> this.pastInvestmentsProfit("AY24", BONO, nominal));

            actions.getOrDefault(subtype, defaultAction).run();

        } else if (type.equals("global")) {
            this.globalInvestmentsProfit(nominal);
        } else {
            defaultAction.run();
        }

    }

    public static void main(String[] args) {
        try {

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
                    entry("income-year", me::yearlyIncome),
                    entry("income-half", me::halfIncome),
                    entry("income-quarter", me::quarterIncome),
                    entry("income-avg-evo", () -> me.incomeAverageEvolution(args, "income-avg-evo")),
                    //house cost
                    entry("house", () -> me.houseIrrecoverableCosts(USD_INFLATION.getTo())),
                    entry("house1", () -> me.houseIrrecoverableCosts(YearMonth.of(2011, 8))),
                    entry("house3", () -> me.houseIrrecoverableCosts(YearMonth.of(2013, 8))),
                    entry("house5", () -> me.houseIrrecoverableCosts(YearMonth.of(2015, 8))),
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

            if (params.isEmpty() || params.contains("help")) {

                final var help = Map.ofEntries(
                        entry("goal", "trials=100000 period=20 retirement=60 w=1000 d=600 inflation=2 cash=0 sp500=true tax=true"),
                        entry("savings-change", "months=1"),
                        entry("savings-change-pct", "months=1"),
                        entry("income", "months=12"),
                        entry("inv", "type=(current*|past|global) subtype=(all*|cspx|meud|xrsu|eimi|ay24|ars|usd|lete|lecap|gold|on|uva|conbala|conaafa|caplusa|pf) nominal=false"),
                        entry("saved-salaries-evo", "months=12"),
                        entry("income-avg-evo", "months=12"),
                        entry("bbpp", "year=2020"),
                        entry("savings-avg-net-change", "months=12"),
                        entry("savings-avg-net-pct", "months=12"),
                        entry("savings-avg-spent-pct", "months=12"),
                        entry("expenses", "type=(taxes|insurance|phone|services|home|entertainment) months=12"),
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

        appendLine(format("Income USD {0,number,currency}\nSavings USD {1,number,currency} {2}\nAverage salary {3,number,currency}\nSaved salaries {4}",
                totalIncome.getAmount(),
                totalSavings.getAmount(),
                this.percentFormat.format(totalSavings.getAmount().divide(totalIncome.getAmount(), CONTEXT)),
                avgSalary,
                totalSavings.getAmount().divide(avgSalary, CONTEXT)));

        final var minSalary = BigDecimal.valueOf(300);

        final var salariesOver500 = this.realIncome()
                .filter((ym, ma) -> ma.getAmount().compareTo(minSalary) >= 0)
                .collect(toList());

        final var totalIncomeOver500 = salariesOver500.stream()
                .reduce(new MoneyAmount(ZERO, "USD"), MoneyAmount::add);

        final var averageSalaryOver500 = totalIncomeOver500.getAmount()
                .divide(BigDecimal.valueOf(salariesOver500.size()), CONTEXT);

        final var futureExpenseFactor = BigDecimal.valueOf(5).movePointLeft(1);
        
        final var m = totalSavings.getAmount().divide(averageSalaryOver500.multiply(futureExpenseFactor, CONTEXT), CONTEXT);
        
        final var yearAndMonth = m.divideAndRemainder(BigDecimal.valueOf(12), CONTEXT);
        
        
        appendLine(format(
                "Projected {0} years and {1} months of income spending {2} of current average salaries over USD {3}.",
                yearAndMonth[0],
                yearAndMonth[1].setScale(0, MathConstants.ROUNDING_MODE),
                this.percentFormat.format(futureExpenseFactor),
                minSalary)
        
        );

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
                percentFormat.format(rate)));

        this.appendLine(format("USD reales {0}/{1}", limit.getMonth(), String.valueOf(limit.getYear())));
        this.appendLine(format("\tTotal USD {0,number,currency} {1}",
                totalRealExpense.getAmount(),
                percentFormat.format(totalRealExpense.getAmount().divide(realInitialCost.getAmount(), CONTEXT))));

        final var monthlyCost = totalRealExpense.getAmount().divide(months, CONTEXT);
        this.appendLine(format("\tMensual USD {0,number,currency} {1} - ARS {2,number,currency}",
                monthlyCost,
                percentFormat.format(monthlyCost.divide(realInitialCost.getAmount(), CONTEXT)),
                ForeignExchanges.getForeignExchange("USD", "ARS")
                        .exchange(new MoneyAmount(monthlyCost, "USD"), "ARS", limit.getYear(), limit.getMonth())
                        .getAmount()));

        final var yearlyCost = totalRealExpense.getAmount().divide(years, CONTEXT);
        this.appendLine(format("\tAnual USD {0,number,currency} {1}\n",
                yearlyCost,
                percentFormat.format(yearlyCost.divide(realInitialCost.getAmount(), CONTEXT))));

    }

    private void expenses(String[] args, String type) {

        final var params = this.paramsValue(args, type);

        final String exp = params.get("type");
        final int months = Integer.parseInt(params.getOrDefault("months", "12"));

        this.appendLine(format("===< Real USD expenses in the last {0} months >===", months));

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
        this.evolutionReport(ym, mo, scale, "{0,number,currency} ");
    }

    private void evolutionReport(YearMonth ym, MoneyAmount mo, int scale, String format) {
        this.appendLine(
                format("{0}/{1}", String.valueOf(ym.getYear()), String.format("%02d", ym.getMonth())),
                " ",
                String.format("%14s", format(format, mo.getAmount())),
                " ",
                this.bar(mo.getAmount(), scale));
    }

    private void percentEvolutionReport(YearMonth ym, BigDecimal mo) {
        this.appendLine(
                format("{0}/{1}", String.valueOf(ym.getYear()), String.format("%02d", ym.getMonth())),
                " ",
                String.format("%8s", this.percentFormat.format(mo)),
                " ",
                this.bar(mo.movePointRight(2), 1));
    }

    private void numericEvolution(String name, MoneyAmountSeries s, int scale) {
        var limit = USD_INFLATION.getTo();

        s.forEach((ym, ma) -> this.evolutionReport(ym, ma, scale, "{0}"));

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
                        value -> String.format("%11s", nf.format(value)))));

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

    private void expensesChange() {

        appendLine("===< Expenses Change >===");

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

    private List<BigDecimal> randomPeriods(List<List<BigDecimal>> allReturns, int periods, BigDecimal fee) {
        return ThreadLocalRandom.current().ints(periods, 0, allReturns.size())
                .mapToObj(allReturns::get)
                .flatMap(Collection::stream)
                .map(value -> value.subtract(fee, CONTEXT))
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

        appendLine("===< Goals >===");

        final var params = this.paramsValue(args, paramName);

        final var trials = Integer.parseInt(params.getOrDefault("trials", "100000"));
        final var periodYears = Integer.parseInt(params.getOrDefault("period", "20"));
        final var deposit = Integer.parseInt(params.getOrDefault("d", "600"));
        final var withdraw = Integer.parseInt(params.getOrDefault("w", "1000"));
        final var inflation = Integer.parseInt(params.getOrDefault("inflation", "3"));
        final var retirementAge = Integer.parseInt(params.getOrDefault("retirement", "60"));
        final var extraCash = Integer.parseInt(params.getOrDefault("cash", "0"));
        final var onlySP500 = Boolean.parseBoolean(params.getOrDefault("sp500", "true"));
        final var afterTax = Boolean.parseBoolean(params.getOrDefault("tax", "true"));

        final var buySellFee = ONE.setScale(6)
                .add(TRADING_FEE.multiply(IVA, CONTEXT), CONTEXT)
                .add(TRADING_FEE, CONTEXT)
                .add(TRADING_FEE, CONTEXT);

        this.goal(trials, periodYears, deposit, withdraw, inflation, retirementAge, buySellFee, BigDecimal.valueOf(extraCash), onlySP500, afterTax);
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
            final boolean afterTax) {

        final var tr = new TypeReference<List<AnnualHistoricalReturn>>() {
        };

        this.sp500TotalReturns = SeriesReader.read("index/sp-total-return.json", tr)
                .stream()
                .sorted(comparing(AnnualHistoricalReturn::getYear))
                .map(AnnualHistoricalReturn::getTotalReturn)
                .map(r -> ONE.setScale(6).add(r.setScale(6).movePointLeft(2), CONTEXT))
                .collect(toList());

        this.russell2000TotalReturns = SeriesReader.read("index/russell2000.json", tr)
                .stream()
                .sorted(comparing(AnnualHistoricalReturn::getYear))
                .map(AnnualHistoricalReturn::getTotalReturn)
                .map(r -> ONE.setScale(6).add(r.setScale(6).movePointLeft(2), CONTEXT))
                .collect(toList());

        final var to = Inflation.USD_INFLATION.getTo();

        final var todaySavings = this.realSavings(null).getAmount(to);

        final var invested = this.realSavings("EQ").getAmount(to);

        final var cash = todaySavings.getAmount()
                .subtract(invested.getAmount(), CONTEXT)
                .add(extraCash, CONTEXT);

        final var inflationRate = ONE.setScale(6)
                .add(BigDecimal.valueOf(inflation).setScale(6).movePointLeft(2), CONTEXT);

        final var deposit = BigDecimal.valueOf(monthlyDeposit * 13).divide(buySellFee, CONTEXT);
        final var withdraw = BigDecimal.valueOf(monthlyWithdraw * 12)
                .multiply(buySellFee, CONTEXT)
                .multiply(afterTax ? new BigDecimal("1.1") : ONE, CONTEXT);

        final var investedAmount = invested.getAmount();

        appendLine(format("Cash: {0,number,currency}, invested: {1,number,currency}", cash, invested.getAmount()));
        appendLine(format("Saving {0,number,currency}, spending {1,number,currency}", monthlyDeposit, monthlyWithdraw), afterTax ? " after tax." : ".");
        appendLine(format("Expected {0}% inflation, retiring at {1}.", inflation, retirementAge));

        final int startingYear = to.getYear();
        final var end = 2078;
        final var yearsLeft = end - startingYear + 1;

        final var periods = (int) Math.ceil((float) yearsLeft / periodYears);

        final var inflationFactors = IntStream.range(0, yearsLeft)
                .mapToObj(year -> inflationRate.pow(year, CONTEXT))
                .collect(toList());

        final var realDeposits = inflationFactors.stream()
                .limit(1978 + retirementAge - startingYear)
                .map(f -> f.multiply(deposit, CONTEXT))
                .collect(toList());

        final var realWithdrawals = inflationFactors.stream()
                .map(f -> f.multiply(withdraw, CONTEXT))
                .collect(toList());

        final var allSP500Periods = this.periods(this.sp500TotalReturns, periodYears, 0.85d);
        final var allRussell2000Periods = this.periods(this.russell2000TotalReturns, periodYears, 0.8d);
        final var allEIMIPeriods = this.periods(this.sp500TotalReturns, periodYears, 0.75d);
        final var allMEUDPeriods = this.periods(this.sp500TotalReturns, periodYears, 0.70d);

        final var sp500Fee = new BigDecimal("0.0007");
        final var russellFee = new BigDecimal("0.003");
        final var eimiFee = new BigDecimal("0.0018");
        final var meudFee = new BigDecimal("0.0007");

        final var successes = IntStream.range(0, trials)
                .mapToObj(i -> this.balanceProportions(periods, allSP500Periods, allRussell2000Periods, allEIMIPeriods, allMEUDPeriods, onlySP500, sp500Fee, russellFee, eimiFee, meudFee))
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
            boolean onlySP500,
            BigDecimal sp500Fee,
            BigDecimal russellFee,
            BigDecimal eimiFee,
            BigDecimal meudFee) {

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

        return IntStream.range(0, sp500Periods.size())
                .mapToObj(i
                        -> sp500Periods.get(i).multiply(SP500_PCT, CONTEXT)
                        .add(russell2000Periods.get(i).multiply(RUSSELL2000_PCT, CONTEXT), CONTEXT)
                        .add(meudPeriods.get(i).multiply(MEUD_PCT, CONTEXT), CONTEXT)
                        .add(eimiPeriods.get(i).multiply(EIMI_PCT, CONTEXT), CONTEXT))
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
        return l.stream().reduce(ZERO, BigDecimal::add);
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

            amount = amount.multiply(returns.get(i - startingYear), CONTEXT)
                    .add(deposit.get(i - startingYear), CONTEXT);
        }
        // withdrawing
        for (var i = retirement; i <= end; i++) {

            amount = amount.subtract(withdraw.get(i - startingYear), CONTEXT);

            if (amount.signum() > 0) {
                amount = amount.multiply(returns.get(i - startingYear), CONTEXT);
            } else {
                cashAmount = cashAmount.add(amount, CONTEXT);
                amount = ZERO;
            }
            if (cashAmount.signum() <= 0) {
                return false;
            }
        }

        return amount.add(cashAmount, CONTEXT).signum() > 0;
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
                .map(i -> i.getValue().multiply(i.getHolding(), CONTEXT))
                .reduce(ZERO, BigDecimal::add);

        appendLine(format("Total amount {0,number,currency}", totalAmount));

        final var taxedDomesticAmount = allArs
                .stream()
                .filter(BBPPItem::isDomestic)
                .filter(i -> !i.isExempt())
                .map(i -> i.getValue().multiply(i.getHolding(), CONTEXT))
                .reduce(ZERO, BigDecimal::add)
                .multiply(new BigDecimal("1.05"), CONTEXT);

        appendLine(format("Taxed domestic amount {0,number,currency}", taxedDomesticAmount));

        final var taxedForeignAmount = allArs
                .stream()
                .filter(i -> !i.isDomestic())
                .filter(i -> !i.isExempt())
                .map(i -> i.getValue().multiply(i.getHolding(), CONTEXT))
                .reduce(ZERO, BigDecimal::add);

        appendLine(format("Taxed foreign amount {0,number,currency}", taxedForeignAmount));

        final var taxedTotal = bbpp.getMinimum()
                .negate()
                .add(taxedDomesticAmount, CONTEXT)
                .add(taxedForeignAmount, CONTEXT);

        appendLine(format("Taxed total {0,number,currency}", taxedTotal));

        final var taxRate = bbpp.getBrakets()
                .stream()
                .sorted(comparing(BBPPTaxBraket::getFrom))
                .filter(b -> b.getFrom().compareTo(totalAmount) <= 0)
                .reduce((left, right) -> right)
                .get()
                .getTax();

        appendLine(format("Tax rate {0}", this.percentFormat.format(taxRate)));

        final var taxAmount = taxedTotal.multiply(taxRate, CONTEXT);

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

        final var yearRealIncome = new ArrayList<MoneyAmount>(12);

        this.realIncome()
                .forEachNonZero((ym, ma) -> Optional.of(ma).filter(m -> ym.getYear() == year).ifPresent(yearRealIncome::add));

        appendLine(format("Effective tax rate is {0}. Tax is {1} of investments. Tax is {2} of income.",
                this.percentFormat.format(taxAmount.divide(totalAmount, CONTEXT)),
                this.percentFormat.format(usdTaxAmount.getAmount().divide(allInvested.getAmount(), CONTEXT)),
                this.percentFormat.format(usdTaxAmount.getAmount().divide(yearRealIncome.stream().map(MoneyAmount::getAmount).reduce(ZERO, BigDecimal::add), CONTEXT))));
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

        this.group("Net yearly savings", this.realNetSavings(), this.realIncome(), ym -> String.valueOf(ym.getYear()));
    }

    private void yearlyIncome() {
        this.group("Yearly income", this.realIncome(), null, ym -> String.valueOf(ym.getYear()));
    }

    private String half(YearMonth ym) {
        return format("{0}-H{1}", String.valueOf(ym.getYear()), ((ym.getMonth() - 1) / 6) + 1);
    }

    private String quarter(YearMonth ym) {
        return format("{0}-Q{1}", String.valueOf(ym.getYear()), ((ym.getMonth() - 1) / 3) + 1);
    }

    private void halfSavings() {

        this.group("Net half savings", this.realNetSavings(), this.realIncome(), this::half);
    }

    private void halfIncome() {
        this.group("Half income", this.realIncome(), null, this::half);
    }

    private void quarterSavings() {

        this.group("Net quarter savings", this.realNetSavings(), this.realIncome(), this::quarter);
    }

    private void quarterIncome() {
        this.group("Quarter income", this.realIncome(), null, this::quarter);
    }

    private void group(String title, MoneyAmountSeries series, MoneyAmountSeries comparisonSeries, Function<YearMonth, String> classifier) {
        appendLine("===< " + title + " >===");

        final Map<String, MoneyAmount> byYear = new HashMap<>(32, 0.75f);

        series.forEachNonZero((ym, ma) -> byYear.merge(classifier.apply(ym), ma, MoneyAmount::add));

        final Map<String, MoneyAmount> comparisonByYear = new HashMap<>(32, 0.75f);

        if (comparisonSeries != null) {
            comparisonSeries.forEachNonZero((ym, ma) -> comparisonByYear.merge(classifier.apply(ym), ma, MoneyAmount::add));
        }

        final var nf = NumberFormat.getCurrencyInstance();

        byYear.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .forEach(e -> this.appendLine(format("{0} {1} {2} {3}",
                e.getKey(),
                String.format("%11s", nf.format(e.getValue().getAmount())),
                Optional.ofNullable(comparisonByYear.get(e.getKey()))
                        .map(comp -> this.pctNumber(e.getValue().getAmount().divide(comp.getAmount(), CONTEXT).movePointRight(2)))
                        .orElse(""),
                this.bar(e.getValue().getAmount(), 500))));
    }

    private MoneyAmountSeries realNetSavings() {

        final var limit = USD_INFLATION.getTo();

        return this.savingsSeries()
                .stream()
                .map(new SimpleAggregation(2)::change)
                .map(series -> series.exchangeInto("USD"))
                .map(usdSeries -> USD_INFLATION.adjust(usdSeries, limit.getYear(), limit.getMonth()))
                .reduce(MoneyAmountSeries::add)
                .get();
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

    private Pair<Integer, MoneyAmount> realUSDMoneyAmount(InvestmentEvent ev, boolean withdrawal) {

        final var amount = withdrawal
                ? new MoneyAmount(ev.getMoneyAmount().getAmount().negate(), ev.getMoneyAmount().getCurrency())
                : ev.getMoneyAmount().add(ev.getFeeMoneyAmount());

        final var fx = ForeignExchanges.getForeignExchange(ev.getCurrency(), "USD");
        final var usdAmount = fx.exchange(amount, "USD", ev.getDate());

        return Pair.of(YearMonth.of(ev.getDate()).getYear(), USD_INFLATION.adjust(usdAmount, ev.getDate(), USD_INFLATION.getTo().asToDate()));

    }

}
