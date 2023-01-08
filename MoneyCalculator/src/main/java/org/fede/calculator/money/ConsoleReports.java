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

import com.diogonunes.jcolor.Attribute;
import java.math.BigDecimal;
import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.ONE;
import java.text.NumberFormat;
import java.util.Comparator;
import static java.util.Comparator.comparing;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static java.text.MessageFormat.format;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import static java.util.Map.entry;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.InvestmentType;
import org.fede.calculator.money.series.YearMonth;
import org.fede.util.Pair;
import static org.fede.util.Pair.of;
import static org.fede.calculator.money.Inflation.USD_INFLATION;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.SeriesReader;
import static org.fede.calculator.money.ForeignExchanges.getMoneyAmountForeignExchange;
import static org.fede.calculator.money.MathConstants.C;
import org.fede.calculator.money.series.InvestmentAsset;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
public class ConsoleReports {

    private static final MoneyAmount ZERO_USD = new MoneyAmount(ZERO.setScale(6, MathConstants.RM), "USD");

    private static final Pattern PARAM_SEPARATOR = Pattern.compile("=");

    private static final String TRIALS = "60000";
    private static final String RETIREMENT = "66";
    private static final String AGE = "99";
    private static final String INFLATION = "2.6";
    private static final String CASH = "0";
    private static final String TAX = "true";
    private static final String EXPECTED_RETRUNS = "all";
    private static final String BBPP = "2.25";
    private static final String BBPP_MIN = "64000";
    private static final String PENSION = "100";

    private static final Collector<BigDecimal, ?, BigDecimal> REDUCER = reducing(ZERO.setScale(MathConstants.SCALE, MathConstants.RM), BigDecimal::add);
    private static final Collector<Investment, ?, BigDecimal> MAPPER = mapping(inv -> inv.getMoneyAmount().getAmount().setScale(MathConstants.SCALE, MathConstants.RM), REDUCER);

    private static final Comparator<Pair<Pair<String, String>, ?>> TYPE_CURRENCY_COMPARATOR = comparing((Pair<Pair<String, String>, ?> pair) -> pair.getFirst().getFirst())
            .thenComparing(comparing(pair -> pair.getFirst().getSecond()));

    private static boolean nominal(Map<String, String> params) {
        return Boolean.parseBoolean(params.getOrDefault("nominal", "false"));
    }

    private static int months(Map<String, String> params) {
        return Integer.parseInt(params.getOrDefault("months", "12"));
    }

    private final Series series;
    private final Console console;
    private final Bar bar;
    private final Format format;

    public ConsoleReports(Console console, Format format, Bar bar, Series series) {
        this.console = console;
        this.bar = bar;
        this.format = format;
        this.series = series;
    }

    private void appendLine(String... texts) {
        this.console.appendLine(texts);
    }

    private void investments() {

        this.console.appendLine(this.format.title("Inversiones actuales agrupadas por moneda"));

        final NumberFormat sixDigits = NumberFormat.getNumberInstance();
        sixDigits.setMinimumFractionDigits(6);

        this.series.getInvestments().stream()
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
        return this.series.getInvestments().stream()
                .filter(predicate)
                .map(Investment::getInvestment)
                .map(InvestmentAsset::getMoneyAmount)
                .map(investedAmount -> getMoneyAmountForeignExchange(investedAmount.getCurrency(), reportCurrency).apply(investedAmount, limit))
                .reduce(MoneyAmount::add);
    }

    private void groupedInvestments() {
        final var reportCurrency = "USD";
        final var limit = USD_INFLATION.getTo();

        appendLine("Inversiones Actuales Agrupadas en ", reportCurrency, " ", String.valueOf(limit.getYear()), "/", String.valueOf(limit.getMonth()));

        final var total = this.total(Investment::isCurrent, reportCurrency, limit);
        this.series.getInvestments().stream()
                .filter(Investment::isCurrent)
                .collect(groupingBy(in -> of(in.getType().toString(), in.getCurrency()), MAPPER))
                .entrySet()
                .stream()
                .map(e -> of(e.getKey(), new MoneyAmount(e.getValue(), e.getKey().getSecond())))
                .map(p -> of(p.getFirst(), this.fx(p, reportCurrency)))
                .sorted((p, q) -> q.getSecond().getAmount().compareTo(p.getSecond().getAmount()))
                .map(pair -> this.formatReport(total, pair.getSecond(), pair.getFirst().getFirst()))
                .forEach(this::appendLine);

        total.map(t -> format("-----------------------------\n{0}{1}", this.format.text("Total", 5), this.format.currency(t, 16)))
                .ifPresent(this::appendLine);
    }

    private String assetAllocation(Investment investment) {
        final Set<String> equities = Set.of("CSPX", "EIMI", "MEUD", "XRSU", "RTWO");
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

    private void listStockByType() {

        final var reportCurrency = "USD";
        final var limit = USD_INFLATION.getTo();
        final var limitStr = String.valueOf(limit.getMonth()) + "/" + String.valueOf(limit.getYear());

        this.appendLine(this.format.title(format("Inversiones Actuales en {0} por tipo. ", limitStr)));

        final Optional<MoneyAmount> total = this.total(Investment::isCurrent, reportCurrency, limit);

        this.series.getInvestments().stream()
                .filter(Investment::isCurrent)
                .collect(groupingBy(
                        this::assetAllocation,
                        mapping(inv -> getMoneyAmountForeignExchange(inv.getInvestment().getCurrency(), reportCurrency).apply(inv.getInvestment().getMoneyAmount(), limit)
                        .getAmount()
                        .setScale(MathConstants.SCALE, MathConstants.RM),
                                REDUCER)))
                .entrySet()
                .stream()
                .map(entry -> this.formatReport(total, new MoneyAmount(entry.getValue(), reportCurrency), entry.getKey()))
                .forEach(this::appendLine);

        total.map(t -> format("-----------------------------\n{0} {1}", this.format.text("Total", 5), this.format.currency(t, 16)))
                .ifPresent(this::appendLine);
    }

    private MoneyAmount fx(Pair<Pair<String, String>, MoneyAmount> p, String reportCurrency) {

        return getMoneyAmountForeignExchange(p.getSecond().getCurrency(), reportCurrency).apply(p.getSecond(), USD_INFLATION.getTo());
    }

    private String formatReport(Optional<MoneyAmount> total, MoneyAmount subtotal, String type) {

        return format("{0}{1}{2}",
                this.format.text(type, 5),
                this.format.currency(subtotal, 16),
                this.bar.pctBar(total.map(tot -> subtotal.getAmount().divide(tot.getAmount(), C)).orElse(ZERO)));
    }

    private void income(String[] args, String paramName) {

        final var params = this.paramsValue(args, paramName);

        final var by = params.get("by");

        if ("quarter".equals(by)) {
            this.quarterIncome();

        } else if ("half".equals(by)) {
            this.halfIncome();
        } else if ("year".equals(by)) {
            this.yearlyIncome();
        } else {

            this.income(months(params));

            final var totalIncome = this.series.getIncomeSeries()
                    .stream()
                    .flatMap(MoneyAmountSeries::moneyAmountStream)
                    .collect(reducing(MoneyAmount::add))
                    .orElse(ZERO_USD)
                    .getAmount();

            this.appendLine(format("Total income: {0}", this.format.currency(totalIncome)));
        }
    }

    private void income(int months) {
        final var limit = USD_INFLATION.getTo();
        final var averageRealUSDIncome = this.series.getIncomeSeries()
                .stream()
                .collect(reducing(MoneyAmountSeries::add))
                .map(new SimpleAggregation(months)::average)
                .map(allRealUSDIncome -> allRealUSDIncome.getAmount(limit.min(allRealUSDIncome.getTo())))
                .orElse(ZERO_USD);

        this.appendLine(this.format.title(format("Average {0}-month income in {1}/{2} real USD",
                months,
                limit.getMonth(),
                String.valueOf(limit.getYear()))));

        this.appendLine("\tIncome: ",
                averageRealUSDIncome.getCurrency(),
                " ",
                this.format.currency(averageRealUSDIncome.getAmount()));

        final var savingPct = new MoneyAmount(averageRealUSDIncome.getAmount().multiply(new BigDecimal("0.5"), C), averageRealUSDIncome.getCurrency());

        this.appendLine("50% saving: ",
                averageRealUSDIncome.getCurrency(),
                " ",
                this.format.currency(savingPct.getAmount()),
                " / ",
                this.format.currency(ForeignExchanges.getMoneyAmountForeignExchange(savingPct.getCurrency(), "ARS").apply(savingPct, limit).getAmount()));

        appendLine(format("Saved salaries {0}",
                this.series.realSavings(null).getAmount(limit).getAmount()
                        .divide(averageRealUSDIncome.getAmount(), C)));

    }

    private void invReport(String[] args, String paramName) {

        final var params = this.paramsValue(args, paramName);

        final var type = params.getOrDefault("type", "all");

        new Investments(this.console, this.format, this.bar, this.series)
                .inv("all".equalsIgnoreCase(type) ? x -> true : x -> this.investmentFilter(x, type), nominal(params));

    }

    private boolean investmentFilter(Investment i, String type) {
        if ("all".equalsIgnoreCase(type)) {
            return true;
        }
        if ("r2k".equalsIgnoreCase(type)) {
            return i.getCurrency().equals("XRSU") || i.getCurrency().equals("RTWO");
        }
        if ("exus".equalsIgnoreCase(type)) {
            return i.getCurrency().equals("EIMI") || i.getCurrency().equals("MEUD");
        }

        return i.getCurrency().equalsIgnoreCase(type);
    }

    public static void main(String[] args) {
        try {

            final var console = new Console();
            final var format = new Format();
            final var bar = new Bar(console, format);
            final var series = new Series();
            final var me = new ConsoleReports(console, format, bar, series);

            final Map<String, Runnable> actions = Map.ofEntries(
                    entry("i", me::investments),
                    entry("gi", me::groupedInvestments),
                    entry("ti", me::listStockByType),
                    entry("inv", () -> me.invReport(args, "inv")),
                    entry("savings", () -> me.savings(args, "savings")),
                    entry("savings-evo", () -> me.savingEvolution(args, "savings-evo")),
                    entry("savings-change", () -> me.savingChange(args, "savings-change")),
                    entry("savings-change-pct", () -> me.savingsPercentChange(args, "savings-change-pct")),
                    entry("savings-net-change", () -> me.monthlySavings(args, "savings-net-change")),
                    entry("savings-avg-pct", () -> me.netAvgSavingSpentPct(args, "savings-avg-pct")),
                    entry("savings-avg", () -> me.netAvgSavingSpent(args, "savings-avg")),
                    entry("savings-dist", me::savingsDistributionEvolution),
                    entry("savings-dist-pct", me::savingsDistributionPercentEvolution),
                    entry("saved-salaries-evo", () -> me.averageSavedSalaries(args, "saved-salaries-evo")),
                    entry("income", () -> me.income(args, "income")),
                    entry("income-table", me::savingsIncomeTable),
                    entry("income-year-table", me::yearSavingsIncomeTable),
                    entry("income-evo", () -> me.incomeAverageEvolution(args, "income-evo")),
                    entry("income-src", () -> me.incomeAverageBySource(args, "income-src")),
                    entry("income-avg-change", () -> me.incomeDelta(args, "income-avg-change")),
                    entry("p", () -> me.portfolio(args, "p")),
                    entry("p-evo", () -> me.portfolioEvo(args, "p-evo")),
                    entry("p-evo-pct", () -> me.portfolioEvoPct(args, "p-evo-pct")),
                    entry("p-type-evo", () -> new Investments(console, format, bar, series).portfolioTypeEvo(false)),
                    entry("p-type-evo-pct", () -> new Investments(console, format, bar, series).portfolioTypeEvo(true)),
                    entry("pa", () -> new PortfolioReturns(series, console, format, bar).portfolioAllocation()),
                    entry("house-evo", () -> new House(console, format, bar).houseCostsEvolution()),
                    entry("house", () -> me.house(args, "house")),
                    entry("expenses", () -> me.expenses(args, "expenses")),
                    entry("condo", () -> me.condo()),
                    entry("expenses-evo", () -> me.expenseEvolution(args, "expenses-evo")),
                    entry("expenses-src", () -> me.expenseBySource(args, "expenses-src")),
                    entry("expenses-change", () -> me.expensesChange(args, "expenses-change")),
                    entry("goal", () -> me.goal(args, "goal")),
                    entry("bbpp", () -> me.bbpp(args, "bbpp")),
                    entry("bbpp-evo", () -> me.bbppEvo(args, "bbpp-evo")),
                    entry("ibkr", () -> me.ibkrCSV()),
                    entry("mdr", () -> me.returns(args, "mdr", new PortfolioReturns(series, console, format, bar))),
                    entry("mdr-by-currency", new PortfolioReturns(series, console, format, bar)::mdrByCurrency),
                    entry("inv-evo", () -> me.invEvo(args, "inv-evo")),
                    entry("pos", () -> me.positions(args, "pos")),
                    entry("dca", () -> me.dca(args, "dca")),
                    entry("inv-evo-pct", () -> me.invEvoPct(args, "inv-evo-pct")),
                    entry("bench", () -> me.benchmark(args, "bench"))
            );

            final var params = Arrays.stream(args)
                    .map(String::toLowerCase)
                    .collect(toSet());

            if (params.isEmpty() || params.contains("help")) {

                final var help = Map.ofEntries(
                        entry("goal", format("trials={0} retirement={1} age={2} inflation={3} cash={4} tax={5} bbpp={6} bbppmin={7} pension={8} exp={9}",
                                TRIALS,
                                RETIREMENT,
                                AGE,
                                INFLATION,
                                CASH,
                                TAX,
                                BBPP,
                                BBPP_MIN,
                                PENSION,
                                EXPECTED_RETRUNS)),
                        entry("savings-change", "months=1"),
                        entry("savings-change-pct", "months=1"),
                        entry("income", "by=(year|half|quarter) months=12"),
                        entry("savings", "by=(year|half|quarter)"),
                        entry("p", "type=(full*|pct) subtype=(all*|equity|bond|commodity|cash) y=current m=current"),
                        entry("p-evo", "type=(all|ETF|BONO|PF|FCI)"),
                        entry("p-evo-pct", "type=(all|ETF|BONO|PF|FCI)"),
                        entry("inv", "type=(all|CSPX|MEUD|EIMI|XRSU|exus|r2k) nominal=false"),
                        entry("inv-evo", "type=(all|CSPX|MEUD|EIMI|XRSU) nominal=false"),
                        entry("inv-evo-pct", "curency=(all|CSPX|MEUD|EIMI|XRSU) nominal=false"),
                        entry("mdr", "nominal=false cash=true start=1999 tw=false"),
                        entry("saved-salaries-evo", "months=12"),
                        entry("house", "years=(null|1|2|3|4|5|6|7|8|9|10)"),
                        entry("income-evo", "months=12 ars=false"),
                        entry("bbpp", "year=2021 ibkr=false"),
                        entry("savings-net-change", "months=12"),
                        entry("savings-avg-pct", "months=12"),
                        entry("expenses", "type=(taxes|insurance|phone|services|home|entertainment) months=12"),
                        entry("expenses-change", "months=12"),
                        entry("expenses-evo", "type=(taxes|insurance|phone|services|home|entertainment) months=12"),
                        entry("savings-evo", "type=(BO|LIQ|EQ)"),
                        entry("dca", "type=(q*|h|y|m)"),
                        entry("pos", "nominal=false fees=false")
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
            console.printReport(System.out);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            ex.printStackTrace(System.err);
        }
    }

    private void house(String[] args, String paramName) {
        final var params = this.paramsValue(args, paramName);

        final var years = params.get("years");

        if (years == null) {
            new House(console, format, bar).houseIrrecoverableCosts(USD_INFLATION.getTo());
        } else {
            new House(console, format, bar).houseIrrecoverableCosts(YearMonth.of(2010 + Integer.parseInt(years), 8));
        }
    }

    private void benchmark(String[] args, String param) {
        new Investments(console, format, bar, series).monthly(nominal(this.paramsValue(args, param)));
    }

    private void savings(String[] args, String paramName) {

        final var params = this.paramsValue(args, paramName);
        final var by = params.get("by");

        if ("quarter".equals(by)) {
            this.quarterSavings();
        } else if ("half".equals(by)) {
            this.halfSavings();
        } else if ("year".equals(by)) {
            this.yearlySavings();
        } else {

            this.appendLine(this.format.title("Historical Real USD Savings Stats"));

            // total savings
            final var limit = USD_INFLATION.getTo();

            final var totalSavings = this.series.realSavings(null).getAmount(limit);

            // total income
            final var totalIncome = this.series.realIncome()
                    .moneyAmountStream()
                    .reduce(ZERO_USD, MoneyAmount::add);

            final var months = this.series.realIncome().getFrom().monthsUntil(limit);

            final var avgSalary = totalIncome.getAmount().divide(BigDecimal.valueOf(months), C);

            appendLine(format("Income USD {0}\nSavings USD {1} {2}\nAverage salary {3}\nSaved salaries {4}",
                    this.format.currency(totalIncome.getAmount()),
                    this.format.currency(totalSavings.getAmount()),
                    this.format.percent(totalSavings.getAmount().divide(totalIncome.getAmount(), C)),
                    this.format.currency(avgSalary),
                    totalSavings.getAmount().divide(avgSalary, C)));

            //ingreso promedio de N meses
            final var agg = new SimpleAggregation(YearMonth.of(2012, 1).monthsUntil(USD_INFLATION.getTo()));

            final var averageIncome = agg.average(this.series.realIncome()).getAmount(USD_INFLATION.getTo());

            // ahorro promedio de N meses
            final var averagNetSavings = agg.average(this.series.realNetSavings()).getAmount(USD_INFLATION.getTo());

            final var m = totalSavings.getAmount().divide(averageIncome.subtract(averagNetSavings).getAmount(), C);

            final var yearAndMonth = m.divideAndRemainder(BigDecimal.valueOf(12), C);

            appendLine(format("Projected {0} years and {1} months of USD {3} income (equivalent to {2} of historical real income).",
                    yearAndMonth[0],
                    yearAndMonth[1].setScale(0, MathConstants.RM),
                    this.format.percent(ONE.subtract(averagNetSavings.getAmount().divide(averageIncome.getAmount(), C), C)),
                    averageIncome.subtract(averagNetSavings).getAmount()));

            final var unlp = SeriesReader.readSeries("income/unlp.json");
            final var despegar = SeriesReader.readSeries("income/despegar.json");

            final var totalYears = Math.round((double) unlp.getFrom().next().monthsUntil(unlp.getTo()) / 12.0d);
            final var simultaneousYears = Math.round((double) despegar.getFrom().monthsUntil(despegar.getTo()) / 12.0d);

            final var simultaneousPercent = new BigDecimal("0.82").divide(new BigDecimal("30"), MathConstants.C);

            final var yearsLeft = 1978 + 65 - LocalDate.now().getYear();

            appendLine(format("Retirement: {0} last 120 average salaries plus {1} best UNLP salary.",
                    this.format.percent(BigDecimal.valueOf(totalYears).multiply(new BigDecimal("0.015"), MathConstants.C)),
                    this.format.percent(simultaneousPercent
                            .multiply(BigDecimal.valueOf(simultaneousYears), MathConstants.C))));

            appendLine(format("Projected: {0} last 120 average salaries plus {1} best UNLP salary.",
                    this.format.percent(BigDecimal.valueOf(totalYears + yearsLeft).multiply(new BigDecimal("0.015"), MathConstants.C)),
                    this.format.percent(simultaneousPercent
                            .multiply(BigDecimal.valueOf(simultaneousYears + yearsLeft), MathConstants.C))));

        }

    }

    private void condo() {
        this.bar.evolution(
                format("Average {0}-month condo expenses.", 12),
                new SimpleAggregation(12).average(this.series.getRealUSDCondoExpenses()),
                25);
    }

    private void expenses(String[] args, String type) {

        final var params = this.paramsValue(args, type);

        final String exp = params.get("type");
        final int months = months(params);

        this.appendLine(this.format.title(format("Real USD expenses in the last {0} months", months)));

        final var list = this.series.getRealUSDExpensesByType()
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
                this.format.text(e.getFirst(), 13),
                this.format.text(" USD ", 4),
                this.format.currency(e.getSecond(), 10),
                this.bar.pctBar(e.getSecond(), total)))
                .forEach(this::appendLine);

        this.appendLine(format("-----------------------------\n{0} USD {1}",
                this.format.text("Total", 5),
                this.format.currency(total, 10)));

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

    private void percentEvolutionReport(YearMonth ym, BigDecimal ma) {

        this.appendLine(
                format("{0}/{1}", String.valueOf(ym.getYear()), String.format("%02d", ym.getMonth())),
                " ",
                this.format.percent(ma, 8),
                " ",
                this.bar.bar(ma.movePointRight(2), 1));
    }

    private void savingsDistributionEvolution() {
        new Savings(format, series, bar, console).savingsDistributionEvolution();
    }

    private void savingsDistributionPercentEvolution() {

        new Savings(format, series, bar, console).savingsDistributionPercentEvolution();

    }

    private void savingEvolution(String[] args, String paramName) {
        this.appendLine(this.format.title("Savings Evolution"));
        this.bar.evolution("Savings", this.series.realSavings(this.paramsValue(args, paramName).get("type")), 2000);
    }

    private void expenseEvolution(String[] args, String paramName) {
        this.appendLine(this.format.title("Expenses Evolution"));

        final var params = this.paramsValue(args, paramName);

        this.expenseEvolution(params.get("type"), Integer.parseInt(params.getOrDefault("months", "1")));
    }

    private void expenseBySource(String[] args, String paramName) {
        final var months = this.months(args, paramName);
        final var title = format("Average {0}-month expenses by source", months);

        final var colorList = List.of(
                Attribute.BLUE_BACK(),
                Attribute.RED_BACK(),
                Attribute.YELLOW_BACK(),
                Attribute.GREEN_BACK(),
                Attribute.MAGENTA_BACK(),
                Attribute.WHITE_BACK()
        );
        this.appendLine(this.format.title(title));

        final var agg = new SimpleAggregation(months);

        final var seriesGroups = this.series.getRealUSDExpensesByType();

        final var series = seriesGroups.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(e -> e.getValue().stream().reduce(MoneyAmountSeries::add).get())
                .map(agg::average)
                .collect(Collectors.toList());

        final var labels = seriesGroups.entrySet().stream()
                .map(e -> e.getKey())
                .sorted()
                .collect(Collectors.toList());

        final var oldestSeries = series.stream().min(Comparator.comparing(s -> s.getFrom())).get();

        oldestSeries.map((ym, ma) -> ZERO_USD.max(ma))
                .forEach((ym, savingMa) -> appendLine(this.bar.currencyBar(ym, this.independenSeries(ym, series, colorList), 8)));

        new Savings(format, this.series, bar, console).refs(
                title,
                labels,
                colorList);

    }

    private void expenseEvolution(String type, int months) {

        this.bar.evolution(format("Average {0}-month expenses.", months), new SimpleAggregation(months).average(this.series.realExpenses(type)), 18);
    }

    private void savingChange(String[] args, String paramName) {

        final var months = Integer.parseInt(this.paramsValue(args, paramName).getOrDefault("months", "1")) + 1;

        this.appendLine(this.format.title(format("{0}-month Savings Change", months - 1)));
        this.bar.evolution(format("{0}-month Savings Change", months - 1), new SimpleAggregation(months)
                .change(this.series.realSavings(null)), 50 * months);

    }

    private void savingsPercentChange(String[] args, String paramName) {

        final var months = Integer.parseInt(this.paramsValue(args, paramName).getOrDefault("months", "1")) + 1;

        this.appendLine(this.format.title(format("{0}-month Savings Change", months - 1)));
        final var s = new SimpleAggregation(months)
                .percentChange(this.series.realSavings(null));

        var ym = s.getFrom();
        var limit = s.getTo();

        while (ym.compareTo(limit) <= 0) {
            this.percentEvolutionReport(ym, s.getIndex(ym.getYear(), ym.getMonth()));
            ym = ym.next();
        }
    }

    private void expensesChange(String[] args, String name) {

        final var months = this.months(args, name);

        this.appendLine(this.format.title("Expenses Change"));

        this.bar.evolution(format("{0}-month average expenses change", months),
                new SimpleAggregation(2)
                        .change(new SimpleAggregation(months)
                                .average(this.series.realExpenses(null))), 5);
    }

    private void incomeAverageEvolution(String[] args, String paramName) {
        var params = this.paramsValue(args, paramName);
        var months = months(params);
        var ars = Boolean.parseBoolean(params.getOrDefault("ars", "false"));
        this.appendLine(this.format.title(format("Average {0}-month income evolution", months)));
        this.incomeAverageEvolution(months, ars);

    }

    private void incomeAverageEvolution(int months, boolean ars) {

        int baseBarSize = 30;
        
        if(months < 6){
            baseBarSize = 50;
        }
        
        final var s = ars
                ? this.series.realIncome().exchangeInto("ARS")
                : this.series.realIncome();

        final var barSize = ars
                ? Math.round((float) (baseBarSize - 10) / ForeignExchanges.USD_ARS.exchange(new MoneyAmount(ONE, "ARS"), "USD", Inflation.USD_INFLATION.getTo()).getAmount().floatValue())
                : baseBarSize;

        this.bar.evolution(format("Average {0}-month income {1}", months, ars ? "ARS" : "USD"),
                new SimpleAggregation(months).average(s),
                barSize);
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
        this.appendLine(this.format.title("Goals"));

        final var params = this.paramsValue(args, paramName);

        final var trials = Integer.parseInt(params.getOrDefault("trials", TRIALS));
        final var inflation = new BigDecimal(params.getOrDefault("inflation", INFLATION));
        final var retirementAge = Integer.parseInt(params.getOrDefault("retirement", RETIREMENT));
        final var age = Integer.parseInt(params.getOrDefault("age", AGE));
        final var months = Integer.parseInt(params.getOrDefault("months", "36"));
        final var extraCash = Integer.parseInt(params.getOrDefault("cash", CASH));
        final var afterTax = Boolean.parseBoolean(params.getOrDefault("tax", TAX));
        final var expected = params.getOrDefault("exp", EXPECTED_RETRUNS);
        final var pension = Integer.parseInt(params.getOrDefault("pension", PENSION));

        final var bbppTax = afterTax
                ? Double.parseDouble(params.getOrDefault("bbpp", BBPP)) / 100.0d
                : 0.0d;

        final var bbppTaxMin = afterTax
                ? Double.parseDouble(params.getOrDefault("bbppmin", BBPP_MIN))
                : 0.0d;

        final var goal = new Goal(this.console, this.format, this.series, this.bar, bbppTax, bbppTaxMin);

        final var todaySavings = this.series.realSavings(null).getAmount(Inflation.USD_INFLATION.getTo());

        final var invested = this.series.realSavings("EQ").getAmount(Inflation.USD_INFLATION.getTo());

        goal.goal(
                trials,
                months,
                inflation,
                retirementAge,
                BigDecimal.valueOf(extraCash),
                afterTax,
                age,
                pension,
                todaySavings,
                invested,
                expected);
    }

    private void bbpp(String[] args, String paramName) {
        final var params = this.paramsValue(args, paramName);

        new BBPP(format, series, console)
                .bbpp(Integer.parseInt(params.getOrDefault("year", "2021")), Boolean.parseBoolean(params.getOrDefault("ibkr", "false")));
    }

    private void bbppEvo(String[] args, String paramName) {
        final var params = this.paramsValue(args, paramName);

        new BBPP(format, series, console)
                .bbppEvolution(Boolean.parseBoolean(params.getOrDefault("ibkr", "false")));
    }

    private void averageSavedSalaries(String[] args, String name) {

        final var months = this.months(args, name);

        final var title = format("Average {0}-month real USD saved salaries", months);
        this.appendLine(this.format.title(title));

        final var savings = new SimpleAggregation(months).average(this.series.realSavings(null));
        final var income = new SimpleAggregation(months).average(this.series.realIncome());

        this.bar.evolution(title,
                income.map((ym, ma) -> new MoneyAmount(savings.getAmountOrElseZero(ym).getAmount().divide(ONE.max(ma.getAmount()), C), ma.getCurrency())),
                2);
    }

    private void yearlySavings() {

        this.group("Net yearly savings", this.series.realNetSavings(), this.series.realIncome(), ym -> String.valueOf(ym.getYear()), 12);
    }

    private void yearlyIncome() {
        this.group("Yearly income", this.series.realIncome(), null, ym -> String.valueOf(ym.getYear()), 12);
    }

    private void halfSavings() {

        this.group("Net half savings", this.series.realNetSavings(), this.series.realIncome(), YearMonth::half, 6);
    }

    private void halfIncome() {
        this.group("Half income", this.series.realIncome(), null, YearMonth::half, 6);
    }

    private void quarterSavings() {

        this.group("Net quarter savings", this.series.realNetSavings(), this.series.realIncome(), YearMonth::quarter, 3);
    }

    private void quarterIncome() {
        this.group("Quarter income", this.series.realIncome(), null, YearMonth::quarter, 3);
    }

    private void group(String title, MoneyAmountSeries series, MoneyAmountSeries comparisonSeries, Function<YearMonth, String> classifier, int months) {

        this.console.appendLine(this.format.title(title));

        final Map<String, MoneyAmount> byYear = new HashMap<>(32, 0.75f);

        series.forEachNonZero((ym, ma) -> byYear.merge(classifier.apply(ym), ma, MoneyAmount::add));

        final Map<String, Long> counts = series.yearMonthStream()
                .collect(groupingBy(classifier, counting()));

        final Map<String, MoneyAmount> comparisonByYear = new HashMap<>(32, 0.75f);

        if (comparisonSeries != null) {
            comparisonSeries.forEachNonZero((ym, ma) -> comparisonByYear.merge(classifier.apply(ym), ma, MoneyAmount::add));
        }

        byYear.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .forEach(e -> this.appendLine(format("{0} {1} {2} {3}",
                e.getKey(),
                this.format.currency(e.getValue().getAmount().divide(BigDecimal.valueOf(Math.min(months, counts.get(e.getKey()))), C), 11),
                Optional.ofNullable(comparisonByYear.get(e.getKey()))
                        .map(comp -> this.format.pctNumber(e.getValue().getAmount().divide(comp.getAmount(), C).movePointRight(2)))
                        .orElse(""),
                this.bar.bar(e.getValue().getAmount().divide(BigDecimal.valueOf(Math.min(months, counts.get(e.getKey()))), C), 50))));
    }

    private void monthlySavings(String[] args, String name) {

        final var months = this.months(args, name);

        final var title = format("Average {0}-month net monthly savings", months);

        this.appendLine(this.format.title(title));

        this.bar.evolution(title,
                new SimpleAggregation(months).average(this.series.realNetSavings()),
                100);
    }

    private int months(String[] args, String name) {
        return Integer.parseInt(this.paramsValue(args, name).getOrDefault("months", "12"));
    }

    private void netAvgSavingSpentPct(String[] args, String name) {

        final var months = this.months(args, name);

        final var title = format("Average {0}-month net monthly average savings and spending percent", months);

        new Savings(format, series, bar, console).netAvgSavingSpentPct(months, title);

    }

    private List<Pair<MoneyAmount, Attribute>> independenSeries(YearMonth ym, List<MoneyAmountSeries> series, List<Attribute> colors) {

        return IntStream.range(0, series.size())
                .mapToObj(i -> Pair.of(ZERO_USD.max(series.get(i).getAmountOrElseZero(ym)), colors.get(i)))
                .collect(Collectors.toList());
    }

    private void netAvgSavingSpent(String[] args, String name) {

        final var months = this.months(args, name);

        final var title = format("Average {0}-month net monthly average savings and spending", months);

        new Savings(format, series, bar, console).netAvgSavingSpent(months, title);

    }

    private void incomeAverageBySource(String[] args, String name) {

        final var months = this.months(args, name);
        final var title = format("Average {0}-month income by source", months);
        final var colorList = List.of(Attribute.BLUE_BACK(), Attribute.RED_BACK(), Attribute.YELLOW_BACK(), Attribute.GREEN_BACK());
        this.appendLine(this.format.title(title));

        final var agg = new SimpleAggregation(months);

        final var unlp = agg.average(this.series.incomeSource("unlp"));
        final var lifia = agg.average(this.series.incomeSource("lifia"));
        final var despARS = agg.average(this.series.incomeSource("despegar"));
        final var despUSD = agg.average(this.series.incomeSource("despegar-split"));

        unlp.map((ym, ma) -> ZERO_USD.max(ma))
                .forEach((ym, savingMa) -> appendLine(this.bar.currencyBar(ym, this.independenSeries(ym, List.of(unlp, lifia, despARS, despUSD), colorList), 25)));

        new Savings(format, series, bar, console).refs(
                title,
                List.of("UNLP", "LIFIA", "Despegar ARS", "Despegar USD"),
                colorList);

    }

    private void savingsIncomeTable() {

        final int[] years = new int[]{1, 2, 4, 6, 8, 10, 12, 14, 16};

        final var incomes = IntStream.of(years)
                .mapToObj(i -> Map.entry(i, this.incomeAverage(i)))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        final var savings = IntStream.of(years)
                .mapToObj(i -> Map.entry(i, this.savingsAverage(i)))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        this.appendLine(this.format.title("Average Income / Spending"));
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
                                .map(this.format::currency))));
        this.appendLine(
                this.row(
                        Stream.concat(
                                Stream.of("Savings"),
                                IntStream.of(years)
                                        .mapToObj(savings::get)
                                        .map(MoneyAmount::getAmount)
                                        .map(this.format::currency))));
        this.appendLine(
                this.row(
                        Stream.concat(
                                Stream.of("Spending"),
                                IntStream.of(years)
                                        .mapToObj(y -> incomes.get(y).subtract(savings.get(y)))
                                        .map(MoneyAmount::getAmount)
                                        .map(this.format::currency))));
        this.appendLine(this.row(Stream.concat(Stream.of("Saving %"),
                IntStream.of(years)
                        .mapToObj(y -> savings.get(y).getAmount().divide(incomes.get(y).getAmount().subtract(ONE, C), C))
                        .map(this.format::percent))));
    }

    private void yearSavingsIncomeTable() {

        final int[] years = IntStream.rangeClosed(1999, USD_INFLATION.getTo().getYear()).toArray();

        final var incomes = IntStream.of(years)
                .mapToObj(i -> Map.entry(i, this.yearIncome(i)))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        final var savings = IntStream.of(years)
                .mapToObj(i -> Map.entry(i, this.yearSavings(i)))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        this.appendLine(this.format.title("Income / Spending by Year"));

        this.appendLine(this.row(Stream.of("-= Year =-", "Income", "Sav.", "Spend.", "Sav. %", "Sav./Spend.")));

        IntStream.of(years)
                .mapToObj(y -> this.row(Stream.of(format("-= {0} =-", String.valueOf(y) + (y == USD_INFLATION.getTo().getYear() ? "*" : "")),
                this.format.currency(incomes.get(y).getAmount()),
                this.format.currency(savings.get(y).getAmount()),
                this.format.currency(incomes.get(y).subtract(savings.get(y)).getAmount()),
                format("{0}", this.format.percent(savings.get(y).getAmount()
                        .divide(incomes.get(y).getAmount()
                                .subtract(ONE, C), C))),
                this.format.number(savings.get(y).getAmount().divide(incomes.get(y).subtract(savings.get(y)).getAmount(), C)))))
                .forEach(this::appendLine);
    }

    private MoneyAmount incomeAverage(int years) {

        return this.series.getIncomeSeries()
                .stream()
                .collect(reducing(MoneyAmountSeries::add))
                .map(new SimpleAggregation(years * 12)::average)
                .map(allRealUSDIncome -> allRealUSDIncome.getAmount(USD_INFLATION.getTo()))
                .orElse(ZERO_USD);
    }

    private MoneyAmount savingsAverage(int years) {
        return new SimpleAggregation(years * 12)
                .average(this.series.realNetSavings())
                .getAmount(USD_INFLATION.getTo());
    }

    private MoneyAmount yearIncome(int year) {

        final var months = year < USD_INFLATION.getTo().getYear()
                ? 12
                : USD_INFLATION.getTo().getMonth();

        return this.series.getIncomeSeries()
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

        return this.series.realNetSavings()
                .filter((ym, ma) -> ym.getYear() == year)
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
                        of("EQUITY", this.lastAmount("ahorros-cspx", ym)),
                        of("EQUITY", this.lastAmount("ahorros-eimi", ym)),
                        of("EQUITY", this.lastAmount("ahorros-rtwo", ym)),
                        of("EQUITY", this.lastAmount("ahorros-meud", ym)),
                        of("EQUITY", this.lastAmount("ahorros-conaafa", ym)),
                        of("EQUITY", this.lastAmount("ahorros-xrsu", ym)))
                        .filter(p -> "all".equals(subtype) || p.getFirst().equalsIgnoreCase(subtype))
                        .collect(groupingBy(
                                Pair::getFirst,
                                groupingBy(
                                        p -> p.getSecond().get().getCurrency(),
                                        mapping(
                                                p -> p.getSecond().get(),
                                                reducing(MoneyAmount::add)))));

        final var items = grouped
                .entrySet()
                .stream()
                .flatMap(e -> this.item(e.getKey(), e.getValue(), ym))
                .sorted(comparing((PortfolioItem::getDollarAmount), comparing(MoneyAmount::getAmount)).reversed())
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
            this.appendLine(format("Total {0}", this.format.currency(total.getAmount())));
        }
    }

    private Stream<PortfolioItem> item(String type, Map<String, Optional<MoneyAmount>> amounts, YearMonth ym) {

        return amounts.values()
                .stream()
                .flatMap(Optional::stream)
                .filter(ma -> !ma.isZero())
                .map(amount -> new PortfolioItem(amount, type, ym));
    }

    private Supplier<MoneyAmount> lastAmount(String seriesName, YearMonth ym) {
        return () -> SeriesReader.readSeries("saving/".concat(seriesName).concat(".json")).getAmountOrElseZero(ym);
    }

    private void returns(String[] args, String paranName, PortfolioReturns pr) {

        final var params = this.paramsValue(args, paranName);

        final var timeWeighted = Boolean.parseBoolean(params.getOrDefault("tw", "false"));
        final var withCash = Boolean.parseBoolean(params.getOrDefault("cash", "true"));
        final var startYear = Integer.parseInt(params.getOrDefault("start", "1999"));

        pr.returns(nominal(params), withCash, startYear, timeWeighted);

    }

    // increase in real USD -  rolling N months
    private void incomeDelta(String[] args, String paramName) {

        final var months = Integer.parseInt(this.paramsValue(args, paramName).getOrDefault("months", "12"));

        final var title = format("{0}-month real USD income change over {0}-month real income average.", months);
        this.console.appendLine(this.format.title(title));

        final var allIncomeSeries = this.series.getIncomeSeries().stream().reduce(MoneyAmountSeries::add).get();

        final var agg = new SimpleAggregation(months);

        final var average = agg.average(allIncomeSeries);

        final var change = agg.change(average);

        average.forEachNonZero((ym, ch) -> percentEvolutionReport(ym, change.getAmount(ym).getAmount().divide(average.getAmount(ym).getAmount(), C)));

        this.console.appendLine(this.format.title(title));

    }

    private void ibkrCSV() {

        this.series.getInvestments().stream()
                .filter(Investment::isETF)
                .filter(inv -> inv.getComment() == null)
                .map(this::assetRow)
                .forEach(this::appendLine);

        final var eur = this.series.getInvestments().stream()
                .filter(Investment::isETF)
                .filter(inv -> inv.getComment() == null)
                .filter(inv -> "MEUD".equals(inv.getInvestment().getCurrency()))
                .map(inv -> inv.getIn().getAmount().add(inv.getIn().getFee(), C))
                .reduce(ZERO, BigDecimal::add);

        final var usd = this.series.getInvestments().stream()
                .filter(Investment::isETF)
                .filter(inv -> inv.getComment() == null)
                .filter(inv -> !"MEUD".equals(inv.getInvestment().getCurrency()))
                .map(inv -> inv.getIn().getAmount().add(inv.getIn().getFee(), C))
                .reduce(ZERO, BigDecimal::add);

        this.appendLine(format("€ {0} - USD {1}", eur, usd));
    }

    private String assetRow(Investment inv) {

        final var isins = Map.of(
                "EIMI", "IE00BKM4GZ66",
                "XRSU", "IE00BJZ2DD79",
                "CSPX", "IE00B5BMR087",
                "MEUD", "LU0908500753");
        final var currencies = Map.of("MEUD", "EUR");

        final Map<String, BiFunction<MoneyAmount, YearMonth, BigDecimal>> currencyConverter = Map.of(
                "MEUD", (ma, ym) -> ForeignExchanges.USD_EUR.exchange(ma, "EUR", ym).getAmount(),
                "XRSU", (ma, ym) -> ma.getAmount(),
                "EIMI", (ma, ym) -> ma.getAmount(),
                "CSPX", (ma, ym) -> ma.getAmount()
        );

        final var codes = Map.of(
                "CSPX", "CSSPXz",
                "EIMI", "EIMIz",
                "XRSU", "XRS2z",
                "MEUD", "MEUD");

        final DateTimeFormatter mdy = DateTimeFormatter.ofPattern("M/d/yyyy").withZone(ZoneId.systemDefault());

        final var numberFormat = NumberFormat.getInstance(Locale.US);
        numberFormat.setGroupingUsed(false);
        numberFormat.setMaximumFractionDigits(2);
        numberFormat.setMinimumFractionDigits(2);

        return List.of("A",
                codes.get(inv.getInvestment().getCurrency()),
                isins.get(inv.getInvestment().getCurrency()),
                currencies.getOrDefault(inv.getInvestment().getCurrency(), "USD"),
                mdy.format(inv.getIn().getDate().toInstant()),
                "BUY",
                "Investment",
                "ETF",
                numberFormat.format(inv.getInvestment().getAmount()),
                numberFormat.format(currencyConverter.get(inv.getInvestment().getCurrency())
                        .apply(new MoneyAmount(inv.getIn().getAmount().divide(inv.getInvestment().getAmount(), C), inv.getInvestment().getCurrency()), YearMonth.of(inv.getIn().getDate()))),
                numberFormat.format(
                        currencyConverter.get(inv.getInvestment().getCurrency())
                                .apply(inv.getIn().getFeeMoneyAmount(), YearMonth.of(inv.getIn().getDate()))))
                .stream()
                .collect(joining(","));
    }

    private void invEvoPct(String[] args, String paramName) {
        final var params = this.paramsValue(args, paramName);

        final var currency = params.get("currency");

        new Investments(console, format, bar, series).invEvoPct(currency, nominal(params));
    }

    private void positions(String[] args, String paramName) {
        final var params = this.paramsValue(args, paramName);

        final var withFee = Boolean.parseBoolean(params.getOrDefault("fees", "false"));
        new Positions(this.console, this.format, this.series, withFee)
                .positions(nominal(params));
    }

    private void dca(String[] args, String paramName) {
        final var params = this.paramsValue(args, paramName);

        final var type = params.getOrDefault("type", "q");
        new Positions(this.console, this.format, this.series, false)
                .dca(nominal(params), type);
    }

    private void invEvo(String[] args, String paramName) {
        final var params = this.paramsValue(args, paramName);

        final var currency = params.get("type");
        final var nominal = nominal(params);

        new Investments(console, format, bar, series).invEvo(currency, nominal);

    }

    private void portfolioEvo(String[] args, String paramName) {
        this.console.appendLine(this.format.title("Portfolio Evolution"));
        final var params = this.paramsValue(args, paramName);

        final var type = params.get("type");

        new Investments(console, format, bar, series).portfolioEvo(type, false);

    }

    private void portfolioEvoPct(String[] args, String paramName) {

        this.console.appendLine(this.format.title("Portfolio Evolution"));

        final var params = this.paramsValue(args, paramName);

        final var type = params.get("type");

        new Investments(console, format, bar, series).portfolioEvo(type, true);

    }

}
