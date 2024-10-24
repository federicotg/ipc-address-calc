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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.math.BigDecimal;
import java.text.MessageFormat;
import static java.text.MessageFormat.format;
import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import static java.util.Map.entry;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import java.util.stream.Stream;

import org.fede.calculator.criptoya.CriptoYaAPI;
import static org.fede.calculator.money.Inflation.USD_INFLATION;
import static org.fede.calculator.money.MathConstants.C;
import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.YearMonth;
import org.fede.calculator.ppi.PPI;
import org.fede.calculator.fmp.CachedETF;
import org.fede.calculator.service.ETF;
import org.fede.calculator.fmp.ExchangeTradedFunds;
import static org.fede.calculator.money.Currency.CSPX;
import static org.fede.calculator.money.Currency.EIMI;
import static org.fede.calculator.money.Currency.MEUD;
import static org.fede.calculator.money.Currency.RTWO;
import static org.fede.calculator.money.Currency.XRSU;
import org.fede.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
public class ConsoleReports {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleReports.class);

    private static final String MONTHS_PARAM = "m";

    private static final Pattern PARAM_SEPARATOR = Pattern.compile("=");

    private static final String TRIALS = "10000";
    private static final String RETIREMENT = "65";
    private static final String AGE = "100";

    // https://fred.stlouisfed.org/series/EXPINF30YR
    private static final String INFLATION = "2.31951";
    private static final String CASH = "0";
    private static final String EXPECTED_RETRUNS = "all";
    private static final String BBPP = "0.5";
    private static final String PENSION = "150";
    private static final String BAD_RETURN_YEARS = "3";
    private static final String BAD_YEAR_SPENDING = "0.85";
    private static final String SAVE_CASH_YEARS_BEFORE_RETIREMENT = "6";
    
    public static final int SCALE = 2500;

    private static boolean nominal(Map<String, String> params) {
        return Boolean.parseBoolean(params.getOrDefault("nominal", "false"));
    }

    private static int months(Map<String, String> params) {
        return Integer.parseInt(params.getOrDefault(MONTHS_PARAM, "12"));
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
            return i.getCurrency().equals(XRSU) || i.getCurrency().equals(RTWO);
        }
        if ("exus".equalsIgnoreCase(type)) {
            return i.getCurrency().equals(EIMI) || i.getCurrency().equals(MEUD);
        }

        return i.getCurrency().name().equalsIgnoreCase(type);
    }

    private static Runnable getAction(String[] args, ConsoleReports me, Format format, Bar bar, Series series, Console console) {

        return switch (args[0]) {
            case "i" ->
                new Investments(console, format, bar, series)::investments;

            case "gi" ->
                new Positions(console, format, series, bar)::groupedInvestments;

            case "ti" ->
                new Positions(console, format, series, bar)::listStockByType;

            case "inv" ->
                () -> me.invReport(args, "inv");

            case "savings" ->
                () -> me.savings(args, "savings");

            case "savings-evo" ->
                () -> me.savingEvolution(args, "savings-evo");

            case "savings-change" ->
                () -> me.savingChange(args, "savings-change");

            case "savings-change-pct" ->
                () -> me.savingsPercentChange(args, "savings-change-pct");

            case "savings-net-change" ->
                () -> me.monthlySavings(args, "savings-net-change");

            case "savings-avg-pct" ->
                () -> me.netAvgSavingSpentPct(args, "savings-avg-pct");

            case "savings-avg" ->
                () -> me.netAvgSavingSpent(args, "savings-avg");

            case "savings-dist" ->
                new Savings(format, series, bar, console)::savingsDistributionEvolution;

            case "savings-dist-pct" ->
                new Savings(format, series, bar, console)::savingsDistributionPercentEvolution;

            case "saved-salaries-evo" ->
                () -> me.averageSavedSalaries(args, "saved-salaries-evo");

            case "income" ->
                () -> me.income(args);

            case "income-table" ->
                new Savings(format, series, bar, console)::savingsIncomeTable;

            case "income-year-table" ->
                new Savings(format, series, bar, console)::yearSavingsIncomeTable;

            case "income-evo" ->
                () -> me.incomeAverageEvolution(args, "income-evo");

            case "income-src" ->
                () -> me.incomeAverageBySource(args, "income-src");

            case "income-src-pct" ->
                () -> me.incomeAveragePctBySource(args, "income-src-pct");

            case "income-acc" ->
                new Savings(format, series, bar, console)::incomeAccumBySource;

            case "income-acc-pct" ->
                new Savings(format, series, bar, console)::incomeAccumBySourcePct;

            case "income-avg-change" ->
                () -> me.incomeDelta(args, "income-avg-change");

            case "p" ->
                () -> me.portfolio(args, "p");

            case "p-evo" ->
                () -> me.portfolioEvo(args, "p-evo");

            case "p-evo-pct" ->
                () -> me.portfolioEvo(args, "p-evo-pct");

            case "p-type-evo" ->
                () -> new Investments(console, format, bar, series).portfolioTypeEvo(false);

            case "p-type-evo-pct" ->
                () -> new Investments(console, format, bar, series).portfolioTypeEvo(true);

            case "pa" ->
                () -> new PortfolioReturns(series, console, format, bar).portfolioAllocation();

            case "house-evo" ->
                () -> new House(console, format, bar).houseCostsEvolution();

            case "house" ->
                () -> me.house(args, "house");

            case "expenses" ->
                () -> me.expenses(args, "expenses");

            case "condo" ->
                () -> me.condo();

            case "expenses-evo" ->
                () -> me.expenseEvolution(args, "expenses-evo");

            case "expenses-src" ->
                () -> me.expenseBySource(args, "expenses-src");

            case "expenses-change" ->
                () -> me.expensesChange(args, "expenses-change");

            case "goal" ->
                () -> me.goal(args, "goal");

            case "bbpp" ->
                () -> me.bbpp(args, "bbpp");

            case "bbppstatus" ->
                () -> new BBPP(format, series, console).status();

            case "bbpp-evo" ->
                new BBPP(format, series, console)::bbppEvolution;

            case "ibkr" ->
                () -> me.ibkrCSV();

            case "ibkrpos" ->
                () -> me.ibkrPositions(Integer.parseInt(me.paramsValue(args, "ibkrpos").getOrDefault("year", "2024")));

            case "mdr" ->
                () -> me.returns(args, "mdr", new PortfolioReturns(series, console, format, bar));

            case "mdr-by-currency" ->
                new PortfolioReturns(series, console, format, bar)::mdrByCurrency;

            case "inv-evo" ->
                () -> me.invEvo(args, "inv-evo");

            case "pos" ->
                () -> me.positions(args, "pos");

            case "dca" ->
                () -> me.dca(args, "dca");

            case "ccl" ->
                () -> new PPI(console, format, new SingleHttpClientSupplier()).dollar();

            case "routes" ->
                () -> me.routes(args, "routes");

            case "balances" ->
                () -> new PPI(console, format, new SingleHttpClientSupplier()).balances();

            case "cash" ->
                () -> new PPI(console, format, new SingleHttpClientSupplier()).cashBalance();

            case "inv-evo-pct" ->
                () -> me.invEvoPct(args, "inv-evo-pct");

            case "bench" ->
                () -> me.benchmark(args, "bench");

            case "etf" ->
                () -> me.etf();

            default ->
                () -> console.appendLine("Unknown parameter.");

        };
    }

    public static void main(String[] args) {
        try {

            final var console = new ByteArrayConsole();
            final var format = new Format();
            final var bar = new Bar(console, format);
            final var series = new Series();
            final var me = new ConsoleReports(console, format, bar, series);

            final var params = Arrays.stream(args)
                    .map(String::toLowerCase)
                    .collect(toSet());

            if (params.isEmpty() || params.contains("help")) {

                final var help = Map.ofEntries(
                        entry("goal", format("trials={0} retirement={1} age={2} inflation={3} cash={4} bbpp={5} pension={6} exp={7} m={8} srr={9} bys={10} crr={11}",
                                TRIALS,
                                RETIREMENT,
                                AGE,
                                INFLATION,
                                CASH,
                                BBPP,
                                PENSION,
                                EXPECTED_RETRUNS,
                                36,
                                BAD_RETURN_YEARS,
                                BAD_YEAR_SPENDING,
                                SAVE_CASH_YEARS_BEFORE_RETIREMENT)),
                        entry("savings-change", "m=1"),
                        entry("i", ""),
                        entry("ti", ""),
                        entry("gi", ""),
                        entry("pa", ""),
                        entry("house-evo", ""),
                        entry("expenses-src", "m=12"),
                        entry("p-type-evo", ""),
                        entry("p-type-evo-pct", ""),
                        entry("condo", ""),
                        entry("ccl", ""),
                        entry("bbpp-evo", ""),
                        entry("routes", ""),
                        entry("balances", ""),
                        entry("cash", ""),
                        entry("bench", ""),
                        entry("ibkr", ""),
                        entry("mdr-by-currency", ""),
                        entry("income-src", "m=12"),
                        entry("income-src-pct", "m=12"),
                        entry("income-acc", ""),
                        entry("income-acc-pct", ""),
                        entry("savings-avg", "m=12"),
                        entry("income-table", ""),
                        entry("income-year-table", ""),
                        entry("savings-dist", ""),
                        entry("savings-dist-pct", ""),
                        entry("income-avg-change", "m=12"),
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
                        entry("bbpp", "year=2023"),
                        entry("bbppstatus", ""),
                        entry("savings-net-change", "m=12"),
                        entry("savings-avg-pct", "m=12"),
                        entry("expenses", "by=(year|half|quarter) type=(taxes|insurance|phone|services|home|entertainment) m=12"),
                        entry("expenses-change", "type=(full|tracked*) m=12"),
                        entry("expenses-evo", "type=(full|taxes|insurance|phone|services|home|entertainment) m=12"),
                        entry("savings-evo", "type=(BO|LIQ|EQ)"),
                        entry("dca", "type=(q*|h|y|m)"),
                        entry("etf", ""),
                        entry("pos", "nominal=false")
                );

                help.entrySet().stream().map(e -> format(" - {0} {1}", e.getKey(), e.getValue()))
                        .sorted()
                        .forEach(me::appendLine);
            } else {

                getAction(args, me, format, bar, series, console).run();
                me.appendLine("");
            }
            console.printReport();
        } catch (Exception ex) {
            LOGGER.error("Unexpected error.", ex);
        }
    }

    private void routes(String[] args, String param) {
        this.console.appendLine(this.format.title("Exit Routes"));

        try {

            final var fee = this.paramsValue(args, param).getOrDefault("fee", "3");

            final var feePct = new BigDecimal(fee).movePointLeft(2);

            final var api = new CriptoYaAPI(new SingleHttpClientSupplier());
            final var initialAmount = new BigDecimal(3000);

            this.console.appendLine(MessageFormat.format("Sending {0}", this.format.currency(new MoneyAmount(initialAmount, Currency.USD), 10)));
            final var blueFee = BigDecimal.ONE.add(feePct);
            Stream.of(
                    Pair.of("USD > Letsbit > USDT > TRON > Kraken", api.lbRoute(initialAmount)),
                    Pair.of("USD > Buenbit > DAI > ERC20 > Kraken", api.bbRoute(initialAmount)),
                    Pair.of("USD > BuenBit > USDT > Polygon > Kraken", api.bbPolygonRoute(initialAmount)),
                    Pair.of("USD > ARS > Letsbit > USDT > TRON > Kraken", api.arsLbRoute(initialAmount, blueFee)),
                    Pair.of("USD > ARS > Letsbit > DAI > ERC20 > Kraken", api.arsLbDaiRoute(initialAmount, blueFee)),
                    Pair.of("USD > ARS > Buenbit > USDT > Polygon > Kraken", api.arsBbPolygonRoute(initialAmount, blueFee)))
                    .sorted(Comparator.comparing(Pair::second, Comparator.reverseOrder()))
                    .forEach(p -> this.printRoute(p.first(), initialAmount, p.second()));

            this.console.appendLine("");
            this.console.appendLine(MessageFormat.format("Considering blue fee of {0}", this.format.percent(feePct)));

        } catch (Exception ex) {
            LOGGER.error("Unexpected error.", ex);
        }
    }

    private void printRoute(String name, BigDecimal initialAmount, BigDecimal result) {
        this.console.appendLine(
                MessageFormat.format("{0}{1}{2}",
                        this.format.text(name, 50),
                        this.format.currency(result, 12),
                        this.format.percent(BigDecimal.ONE.subtract(result.divide(initialAmount, MathConstants.C)).negate(), 9)
                ));
    }

    private void income(String[] args) {
        new Savings(format, series, bar, console).income(this.paramsValue(args, "income"));
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
        new Savings(format, series, bar, console).savings(this.paramsValue(args, paramName));
    }

    private void condo() {
        this.bar.evolution(
                format("Average {0}-month condo expenses.", 12),
                new SimpleAggregation(12).average(this.series.getRealUSDCondoExpenses()),
                25);
    }

    private void expenses(String[] args, String type) {
        new Expenses(series, console, bar, format).expenses(this.paramsValue(args, type));
    }

    private void savingsPercentChange(String[] args, String paramName) {
        final var months = Integer.parseInt(this.paramsValue(args, paramName).getOrDefault(MONTHS_PARAM, "1")) + 1;
        new Savings(format, series, bar, console).savingsPercentChange(months);
    }

    private void incomeDelta(String[] args, String paramName) {
        final var months = Integer.parseInt(this.paramsValue(args, paramName).getOrDefault(MONTHS_PARAM, "12"));
        new Savings(format, series, bar, console).incomeDelta(months);
    }

    private void savingEvolution(String[] args, String paramName) {
        this.appendLine(this.format.title("Savings Evolution"));
        this.bar.evolution("Savings", this.series.realSavings(this.paramsValue(args, paramName).get("type")), ConsoleReports.SCALE);
    }

    private void expenseEvolution(String[] args, String paramName) {
        this.appendLine(this.format.title("Expenses Evolution"));

        final var params = this.paramsValue(args, paramName);

        this.expenseEvolution(params.get("type"), Integer.parseInt(params.getOrDefault(MONTHS_PARAM, "1")));
    }

    private void expenseBySource(String[] args, String paramName) {
        new Expenses(series, console, bar, format).expenseBySource(months(this.paramsValue(args, paramName)));
    }

    private void expenseEvolution(String type, int months) {

        final var s = "full".equalsIgnoreCase(type)
                ? this.series.realExpense()
                : this.series.realExpenses(type);

        this.bar.evolution(format("Average {0}-month expenses.", months), new SimpleAggregation(months).average(s), 18);
    }

    private void savingChange(String[] args, String paramName) {

        final var months = Integer.parseInt(this.paramsValue(args, paramName).getOrDefault(MONTHS_PARAM, "1")) + 1;

        this.appendLine(this.format.title(format("{0}-month Savings Change", months - 1)));
        this.bar.evolution(format("{0}-month Savings Change", months - 1), new SimpleAggregation(months)
                .change(this.series.realSavings(null)), 100 * months);
    }

    private void expensesChange(String[] args, String name) {

        var params = this.paramsValue(args, name);
        final var months = months(params);
        final var type = params.getOrDefault("type", "tracked");
        new Expenses(series, console, bar, format).expensesChange(type, months);
    }

    private void incomeAverageEvolution(String[] args, String paramName) {
        var params = this.paramsValue(args, paramName);
        var months = months(params);
        var ars = Boolean.parseBoolean(params.getOrDefault("ars", "false"));

        new Savings(format, series, bar, console).incomeAverageEvolution(months, ars);
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
        final var badYearSpending = new BigDecimal(params.getOrDefault("bys", BAD_YEAR_SPENDING)).doubleValue();
        final var retirementAge = Integer.parseInt(params.getOrDefault("retirement", RETIREMENT));
        final var age = Integer.parseInt(params.getOrDefault("age", AGE));
        final var months = Integer.parseInt(params.getOrDefault(MONTHS_PARAM, "36"));
        final var extraCash = Integer.parseInt(params.getOrDefault("cash", CASH));
        final var expected = params.getOrDefault("exp", EXPECTED_RETRUNS);
        final var pension = Integer.parseInt(params.getOrDefault("pension", PENSION));
        final var badReturnYears = Integer.parseInt(params.getOrDefault("srr", BAD_RETURN_YEARS));
        final var saveCashYears = Integer.parseInt(params.getOrDefault("crr", SAVE_CASH_YEARS_BEFORE_RETIREMENT));
        final var bbppTax = Double.parseDouble(params.getOrDefault("bbpp", BBPP)) / 100.0d;

        final var goal = new Goal(this.console, this.format, this.series, this.bar, bbppTax);

        final var todaySavings = this.series.realSavings(null).getAmount(Inflation.USD_INFLATION.getTo());

        final var invested = this.series.realSavings("EQ").getAmount(Inflation.USD_INFLATION.getTo());

        goal.goal(
                trials,
                months,
                inflation,
                retirementAge,
                BigDecimal.valueOf(extraCash),
                age,
                pension,
                todaySavings,
                invested,
                expected,
                badReturnYears,
                badYearSpending,
                saveCashYears);
    }

    private void bbpp(String[] args, String paramName) {
        final var params = this.paramsValue(args, paramName);

        new BBPP(format, series, console)
                .bbpp(Integer.parseInt(params.getOrDefault("year", "2023")));
    }

    private void averageSavedSalaries(String[] args, String name) {
        new Savings(format, series, bar, console).averageSavedSalaries(months(this.paramsValue(args, name)));
    }

    private void monthlySavings(String[] args, String name) {
        new Savings(format, series, bar, console).monthlySavings(months(this.paramsValue(args, name)));
    }

    private void netAvgSavingSpentPct(String[] args, String name) {

        final var months = months(this.paramsValue(args, name));
        final var title = format("Average {0}-month net monthly average savings and spending percent", months);
        new Savings(format, series, bar, console).netAvgSavingSpentPct(months, title);

    }

    private void netAvgSavingSpent(String[] args, String name) {

        final var months = months(this.paramsValue(args, name));
        final var title = format("Average {0}-month net monthly average savings and spending", months);
        new Savings(format, series, bar, console).netAvgSavingSpent(months, title);

    }

    private void incomeAverageBySource(String[] args, String name) {

        final var months = months(this.paramsValue(args, name));
        new Savings(format, series, bar, console).incomeAverageBySource(months);

    }

    private void incomeAveragePctBySource(String[] args, String name) {

        final var months = months(this.paramsValue(args, name));
        new Savings(format, series, bar, console).incomeAverageBySource(months, true);

    }

    private void portfolio(String[] args, String name) {

        final var params = this.paramsValue(args, name);
        final var type = params.getOrDefault("type", "full");
        final var subtype = params.getOrDefault("subtype", "all");
        final var year = Optional.ofNullable(params.get("y"))
                .map(Integer::parseInt)
                .orElseGet(USD_INFLATION.getTo()::getYear);
        final var month = Optional.ofNullable(params.get("m"))
                .map(Integer::parseInt)
                .orElseGet(USD_INFLATION.getTo()::getMonth);
        new Positions(console, format, series, bar)
                .portfolio(type, subtype, year, month);

    }

    private void returns(String[] args, String paranName, PortfolioReturns pr) {

        final var params = this.paramsValue(args, paranName);
        final var timeWeighted = Boolean.parseBoolean(params.getOrDefault("tw", "false"));
        final var withCash = Boolean.parseBoolean(params.getOrDefault("cash", "true"));
        final var startYear = Integer.parseInt(params.getOrDefault("start", "1999"));

        pr.returns(nominal(params), withCash, startYear, timeWeighted);

    }

    private void ibkrCSV() {

        this.series.getInvestments().stream()
                .filter(Investment::isETF)
                .filter(inv -> inv.getComment() == null)
                .map(this::assetRow)
                .forEach(this::appendLine);
    }

    private void ibkrPositions(int year) {

        this.series.getInvestments()
                .stream()
                .filter(Investment::isETF)
                .filter(inv -> inv.getComment() != null)
                .filter(inv -> YearMonth.of(inv.getInitialDate()).year() <= year)
                .filter(inv -> inv.getOut() == null || YearMonth.of(inv.getOut().getDate()).year() > year)
                .collect(
                        Collectors.groupingBy(
                                Investment::getCurrency,
                                Collectors.mapping(
                                        inv -> inv.getInvestment().getAmount(),
                                        Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))))
                .entrySet()
                .stream()
                .map(e -> MessageFormat.format("{0} {1}", e.getKey(), format.number(e.getValue())))
                .sorted()
                .forEach(console::appendLine);
    }

    private String assetRow(Investment inv) {

        final var isins = Map.of(
                EIMI, "IE00BKM4GZ66",
                XRSU, "IE00BJZ2DD79",
                CSPX, "IE00B5BMR087",
                MEUD, "LU0908500753");

        final DateTimeFormatter mdy = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());

        final var numberFormat = NumberFormat.getInstance(Locale.US);
        numberFormat.setGroupingUsed(false);
        numberFormat.setMaximumFractionDigits(2);
        numberFormat.setMinimumFractionDigits(2);

        return List.of("A",
                inv.getInvestment().getCurrency().name(),
                isins.get(inv.getInvestment().getCurrency()),
                "USD",
                mdy.format(inv.getIn().getDate().toInstant()),
                "BUY",
                "Investment",
                "ETF",
                numberFormat.format(inv.getInvestment().getAmount()),
                numberFormat.format(inv.getIn().getAmount().divide(inv.getInvestment().getAmount(), C)),
                numberFormat.format(inv.getIn().getFeeMoneyAmount().getAmount()))
                .stream()
                .collect(joining(","));
    }

    private void invEvoPct(String[] args, String paramName) {
        final var params = this.paramsValue(args, paramName);
        final var currency = Optional.ofNullable(params.get("type")).map(Currency::valueOf).orElse(null);
        new Investments(console, format, bar, series).invEvoPct(currency, nominal(params));
    }

    private void positions(String[] args, String paramName) {
        final var params = this.paramsValue(args, paramName);
        new Positions(this.console, this.format, this.series, this.bar)
                .positions(nominal(params));
    }

    private void dca(String[] args, String paramName) {
        final var params = this.paramsValue(args, paramName);

        final var type = params.getOrDefault("type", "q");
        new Positions(this.console, this.format, this.series, this.bar)
                .dca(nominal(params), type);
    }

    private void invEvo(String[] args, String paramName) {
        final var params = this.paramsValue(args, paramName);
        final var currency = Optional.ofNullable(params.get("type")).map(Currency::valueOf).orElse(null);
        final var nominal = nominal(params);
        new Investments(console, format, bar, series).invEvo(currency, nominal);
    }

    private void portfolioEvo(String[] args, String paramName) {
        this.console.appendLine(this.format.title("Portfolio Evolution"));
        final var params = this.paramsValue(args, paramName);
        final var type = params.get("type");
        new Investments(console, format, bar, series).portfolioEvo(type, "p-evo-pct".equalsIgnoreCase(paramName));
    }

    private String currecySymbol(String etf) {
        if (etf.equals(ETF.MEUD)) {
            return "€";
        }
        return "$";
    }

    private void etf() {

        try {
            var om = new ObjectMapper()
                    .setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE)
                    .registerModule(new JavaTimeModule());
            var etfs = new CachedETF(om, new ExchangeTradedFunds(om, new SingleHttpClientSupplier())).etfs();

            etfs.values()
                    .stream()
                    .map(etf -> MessageFormat.format("{0} {2} {1}", format.text(etf.symbol(), 8), etf.price(), this.currecySymbol(etf.symbol())))
                    .sorted()
                    .forEach(console::appendLine);

            this.console.appendLine(
                    MessageFormat.format(
                            "{0} $ {1}",
                            format.text("USD/EUR", 8),
                            format.numberLong(etfs.get(ETF.MEUS).price().divide(etfs.get(ETF.MEUD).price(), MathConstants.C))));
        } catch (Exception ex) {
            LOGGER.error("Error reading ETFs ", ex);
        }
    }

}
