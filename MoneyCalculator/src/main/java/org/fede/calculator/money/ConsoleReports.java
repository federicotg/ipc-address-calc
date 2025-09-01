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

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.MessageFormat;
import static java.text.MessageFormat.format;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import java.util.stream.Stream;

import org.fede.calculator.criptoya.CriptoYaAPI;
import static org.fede.calculator.money.Currency.ARS;
import static org.fede.calculator.money.Inflation.USD_INFLATION;
import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.YearMonth;
import org.fede.calculator.ppi.PPI;
import static org.fede.calculator.money.Currency.EIMI;
import static org.fede.calculator.money.Currency.MEUD;
import static org.fede.calculator.money.Currency.RTWO;
import static org.fede.calculator.money.Currency.XRSU;
import static org.fede.calculator.money.Currency.USD;
import org.fede.calculator.money.chart.ChartStyle;
import org.fede.calculator.money.chart.PieChart;
import org.fede.calculator.money.chart.Scale;
import org.fede.calculator.money.chart.TimeSeriesChart;
import org.fede.calculator.money.chart.ValueFormat;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.SeriesReader;
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

    public static final String CHARTS_PREFIX = System.getProperty("user.home") + File.separator + "Pictures" + File.separator + "chart-";
    public static final String CACHE_DIR = System.getProperty("user.home") + "/Downloads";

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
            return i.getCurrency() == XRSU || i.getCurrency() == RTWO;
        }
        if ("exus".equalsIgnoreCase(type)) {
            return i.getCurrency() == EIMI || i.getCurrency() == MEUD || i.getCurrency() == Currency.XUSE;
        }

        return i.getCurrency().name().equalsIgnoreCase(type);
    }

    private static Runnable getAction(String[] args, ConsoleReports me, Format format, Bar bar, Series series, Console console) {

        return switch (args[0]) {
            case "i" ->
                new Investments(console, format, bar, series)::investments;

            case "pf" ->
                () -> new Investments(console, format, bar, series)
                .invPF(me.paramsValueDefaultFalse(args, "pf", "detail"), me.paramsValueDefaultFalse(args, "pf", "nominal"));

            case "inv" ->
                () -> me.invReport(args, "inv");

            case "fire" ->
                () -> new Fire(format, series, console)
                .fire(Integer.parseInt(me.paramsValue(args, "fire").getOrDefault("m", "12")));

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
                new PortfolioReturns(series, console, format, bar)::portfolioAllocation;

            case "house-evo" ->
                new House(console, format, bar)::houseCostsEvolution;

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
                new BBPP(format, series, console)::status;

            case "bbpp-evo" ->
                new BBPP(format, series, console)::bbppEvolution;

            case "all-charts" ->
                me::allCharts;

            case "ibkrpos" ->
                () -> me.ibkrPositions(Integer.parseInt(me.paramsValue(args, "ibkrpos").getOrDefault("year", "2024")));

            case "mdr" ->
                () -> me.returns(args, "mdr", new PortfolioReturns(series, console, format, bar));
            case "inv-evo" ->
                () -> me.invEvo(args, "inv-evo");

            case "pos" ->
                () -> me.positions(args, "pos");

            case "dca" ->
                () -> me.dca(args, "dca");
            case "invested" ->
                () -> me.invested(args, "invested");

            case "ccl" ->
                new PPI(console, format, new SingleHttpClientSupplier())::dollar;

            case "routes" ->
                () -> me.routes(args, "routes");

            case "balances" ->
                new PPI(console, format, new SingleHttpClientSupplier())::balances;

            case "cash" ->
                new PPI(console, format, new SingleHttpClientSupplier())::cashBalance;

            case "inv-evo-pct" ->
                () -> me.invEvoPct(args, "inv-evo-pct");

            case "bench" ->
                () -> me.benchmark(args, "bench");

            case "lti" ->
                me::ltiReport;

            case "ppi" ->
                () -> new Investments(console, format, bar, series)
                .ppiTransfer(me.paramsValue(args, "ppi").getOrDefault("type", "full"));

            default ->
                () -> console.appendLine("Unknown parameter.");

        };
    }

    public static void main(String[] args) {
        try {

            final var console = new FastConsole();
            final var format = new Format();
            final var bar = new Bar(console, format);
            final var series = new Series();
            final var me = new ConsoleReports(console, format, bar, series);

            final var params = Arrays.stream(args)
                    .map(String::toLowerCase)
                    .collect(toSet());

            if (params.isEmpty() || params.contains("help")) {

                Stream.of(
                        new CmdParam("goal", format("trials={0} retirement={1} age={2} inflation={3} cash={4} bbpp={5} pension={6} exp={7} m={8} srr={9}",
                                SeriesReader.readEnvironment().getProperty("goal.trials"),
                                SeriesReader.readEnvironment().getProperty("goal.retirement"),
                                SeriesReader.readEnvironment().getProperty("goal.maxage"),
                                SeriesReader.readPercent("expectedInflation"),
                                SeriesReader.readEnvironment().getProperty("goal.cash"),
                                SeriesReader.readEnvironment().getProperty("goal.bbpp"),
                                SeriesReader.readEnvironment().getProperty("goal.pension"),
                                SeriesReader.readEnvironment().getProperty("goal.expectedreturns"),
                                SeriesReader.readInt("goal.months"),
                                SeriesReader.readEnvironment().getProperty("goal.badreturns")
                        )),
                        new CmdParam("savings-change", "m=1"),
                        new CmdParam("savings-change-pct", "m=1"),
                        new CmdParam("i"),
                        new CmdParam("pa"),
                        new CmdParam("house-evo"),
                        new CmdParam("expenses-src", "m=12"),
                        new CmdParam("fire", "m=12"),
                        new CmdParam("p-type-evo"),
                        new CmdParam("p-type-evo-pct"),
                        new CmdParam("condo"),
                        new CmdParam("ccl"),
                        new CmdParam("lti"),
                        new CmdParam("bbpp-evo"),
                        new CmdParam("routes"),
                        new CmdParam("balances"),
                        new CmdParam("all-charts"),
                        new CmdParam("cash"),
                        new CmdParam("bench"),
                        new CmdParam("income-src", "m=12"),
                        new CmdParam("pf", "detail=false nominal=false"),
                        new CmdParam("income-src-pct", "m=12"),
                        new CmdParam("income-acc"),
                        new CmdParam("income-acc-pct"),
                        new CmdParam("savings-avg", "m=12"),
                        new CmdParam("income-table"),
                        new CmdParam("income-year-table"),
                        new CmdParam("savings-dist"),
                        new CmdParam("savings-dist-pct"),
                        new CmdParam("income-avg-change", "m=12"),
                        new CmdParam("income", "by=(year|half|quarter*) months=12"),
                        new CmdParam("savings", "by=(year|half|quarter*)"),
                        new CmdParam("p", "type=(full*|pct) subtype=(all*|equity|bond|commodity|cash) y=current m=current"),
                        new CmdParam("p-evo", "type=(all|ETF|BONO|PF|FCI)"),
                        new CmdParam("p-evo-pct", "type=(all*|ETF|BONO|PF|FCI)"),
                        new CmdParam("inv", "type=(all*|CSPX|MEUD|EIMI|XRSU|exus|r2k) nominal=false"),
                        new CmdParam("inv-evo", "type=(all*|CSPX|MEUD|EIMI|XRSU) nominal=false"),
                        new CmdParam("inv-evo-pct", "curency=(all*|CSPX|MEUD|EIMI|XRSU) nominal=false"),
                        new CmdParam("invested", "type=(long*|all*|CSPX|MEUD|EIMI|XRSU|fci|etf|pf|pfusd|pfars) group=(m|q*|h|y|all) nominal=false"),
                        new CmdParam("mdr", "nominal=false cash=true start=1999 tw=false"),
                        new CmdParam("saved-salaries-evo", "months=12"),
                        new CmdParam("house", "years=(null|1|2|3|4|5|6|7|8|9|10)"),
                        new CmdParam("income-evo", "months=12 ars=false"),
                        new CmdParam("bbpp", "year=2023"),
                        new CmdParam("bbppstatus"),
                        new CmdParam("ppi", "type=group|groupall|full*"),
                        new CmdParam("savings-net-change", "m=12"),
                        new CmdParam("savings-avg-pct", "m=12"),
                        new CmdParam("expenses", "by=(year|half|quarter*|month) type=(taxes|insurance|phone|services|home|entertainment) m=12"),
                        new CmdParam("expenses-change", "m=12"),
                        new CmdParam("expenses-evo", "type=(full|taxes|insurance|services|home|entertainment) m=12"),
                        new CmdParam("savings-evo", "type=(BO|LIQ|EQ)"),
                        new CmdParam("dca", "type=(q*|h|y|m)"),
                        new CmdParam("pos", "nominal=false")
                ).map(param -> format(" - {0} {1}", param.name, param.argsDesc))
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

            final var fee = this.paramsValue(args, param).getOrDefault("fee", "0");

            final var feePct = new BigDecimal(fee).movePointLeft(2);

            final var api = new CriptoYaAPI(new SingleHttpClientSupplier());
            final var initialAmount = new BigDecimal(10000);

            this.console.appendLine(MessageFormat.format("Sending {0}", this.format.currency(new MoneyAmount(initialAmount, USD), 10)));
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
        new Savings(format, series, bar, console)
                .income(this.paramsValue(args, "income"));
    }

    private void house(String[] args, String paramName) {
        final var params = this.paramsValue(args, paramName);

        final var years = params.get("years");

        if (years == null) {
            new House(console, format, bar)
                    .houseIrrecoverableCosts(USD_INFLATION.getTo());
        } else {
            new House(console, format, bar)
                    .houseIrrecoverableCosts(YearMonth.of(2010 + Integer.parseInt(years), 8));
        }
    }

    private void benchmark(String[] args, String param) {
        new Investments(console, format, bar, series)
                .monthly(nominal(this.paramsValue(args, param)));
    }

    private void savings(String[] args, String paramName) {
        new Savings(format, series, bar, console)
                .savings(this.paramsValue(args, paramName));
    }

    private void condo() {
        this.bar.evolution(
                format("Average {0}-month condo expenses.", 12),
                new SimpleAggregation(12).average(this.series.getRealUSDCondoExpenses()),
                25);
    }

    private void expenses(String[] args, String type) {
        new Expenses(series, console, bar, format)
                .expenses(this.paramsValue(args, type));
    }

    private void savingsPercentChange(String[] args, String paramName) {
        final var months = Integer.parseInt(this.paramsValue(args, paramName).getOrDefault(MONTHS_PARAM, "1")) + 1;
        new Savings(format, series, bar, console)
                .savingsPercentChange(months);
    }

    private void incomeDelta(String[] args, String paramName) {
        final var months = Integer.parseInt(this.paramsValue(args, paramName).getOrDefault(MONTHS_PARAM, "12"));
        new Savings(format, series, bar, console)
                .incomeDelta(months);
    }

    private void savingEvolution(String[] args, String paramName) {
        this.appendLine(this.format.title("Savings Evolution"));
        this.bar.evolution(
                "Savings",
                this.series.realSavings(this.paramsValue(args, paramName).get("type")),
                SeriesReader.readInt("scale"));
    }

    private void expenseEvolution(String[] args, String paramName) {
        this.appendLine(this.format.title("Expenses Evolution"));

        final var params = this.paramsValue(args, paramName);

        this.expenseEvolution(params.get("type"), Integer.parseInt(params.getOrDefault(MONTHS_PARAM, "1")));
    }

    private void expenseBySource(String[] args, String paramName) {
        new Expenses(series, console, bar, format)
                .expenseBySource(months(this.paramsValue(args, paramName)));
    }

    private void expenseEvolution(String type, int months) {

        final var s = "full".equalsIgnoreCase(type)
                ? this.series.realExpense()
                : this.series.realExpenses(type);

        this.bar.evolution(format("Average {0}-month expenses.", months), new SimpleAggregation(months).average(s), 30);
    }

    private void savingChange(String[] args, String paramName) {

        final var months = Integer.parseInt(this.paramsValue(args, paramName).getOrDefault(MONTHS_PARAM, "1")) + 1;

        this.appendLine(this.format.title(format("{0}-month Savings Change", months - 1)));
        this.bar.evolution(format("{0}-month Savings Change", months - 1), new SimpleAggregation(months)
                .change(this.series.realSavings(null)), 240 * months);
    }

    private void expensesChange(String[] args, String name) {

        var params = this.paramsValue(args, name);
        final var months = months(params);
        new Expenses(series, console, bar, format)
                .expensesChange(months);
    }

    private void incomeAverageEvolution(String[] args, String paramName) {
        var params = this.paramsValue(args, paramName);
        var months = months(params);
        var ars = Boolean.parseBoolean(params.getOrDefault("ars", "false"));

        new Savings(format, series, bar, console)
                .incomeAverageEvolution(months, ars);
    }

    private Map<String, String> paramsValue(String[] args, String paramName) {
        return Arrays.stream(args)
                .dropWhile(p -> paramName.equals(p))
                .takeWhile(p -> p.contains("="))
                .map(PARAM_SEPARATOR::split)
                .collect(toMap(parts -> parts[0], parts -> parts[1]));
    }

    private boolean paramsValueDefaultFalse(
            String[] args,
            String paramName,
            String key) {
        return Boolean.parseBoolean(this.paramsValue(args, paramName).getOrDefault(key, "false"));
    }

    private void goal(String[] args, String paramName) {

        this.appendLine(this.format.title("Goals"));

        final var params = this.paramsValue(args, paramName);

        final var trials = Integer.parseInt(params.getOrDefault("trials", SeriesReader.readEnvironment().getProperty("goal.trials")));

        final var inflation = Optional.ofNullable(params.get("inflation"))
                .map(BigDecimal::new)
                .orElseGet(() -> SeriesReader.readBigDecimal("expectedInflation"));

        final var retirementAge = Integer.parseInt(params.getOrDefault("retirement", SeriesReader.readEnvironment().getProperty("goal.retirement")));
        final var age = Integer.parseInt(params.getOrDefault("age", SeriesReader.readEnvironment().getProperty("goal.maxage")));
        final var months = Integer.parseInt(params.getOrDefault(MONTHS_PARAM, SeriesReader.readEnvironment().getProperty("goal.months")));
        final var extraCash = Integer.parseInt(params.getOrDefault("cash", SeriesReader.readEnvironment().getProperty("goal.cash")));
        final var expected = params.getOrDefault("exp", SeriesReader.readEnvironment().getProperty("goal.expectedreturns"));
        final var pension = Integer.parseInt(params.getOrDefault("pension", SeriesReader.readEnvironment().getProperty("goal.pension")));
        final var badReturnYears = Integer.parseInt(params.getOrDefault("srr", SeriesReader.readEnvironment().getProperty("goal.badreturns")));
        final var bbppTax = Double.parseDouble(params.getOrDefault("bbpp", SeriesReader.readEnvironment().getProperty("goal.bbpp"))) / 100.0d;

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
                badReturnYears);
    }

    private void bbpp(String[] args, String paramName) {
        final var params = this.paramsValue(args, paramName);

        new BBPP(format, series, console)
                .bbpp(Integer.parseInt(params.getOrDefault("year", SeriesReader.readEnvironment().getProperty("bbpp.year"))));
    }

    private void averageSavedSalaries(String[] args, String name) {
        new Savings(format, series, bar, console)
                .averageSavedSalaries(months(this.paramsValue(args, name)));
    }

    private void monthlySavings(String[] args, String name) {
        new Savings(format, series, bar, console)
                .monthlySavings(months(this.paramsValue(args, name)));
    }

    private void netAvgSavingSpentPct(String[] args, String name) {

        final var months = months(this.paramsValue(args, name));
        final var title = format("Average {0}-month net monthly average savings and spending percent", months);
        new Savings(format, series, bar, console)
                .netAvgSavingSpentPct(months, title);

    }

    private void netAvgSavingSpent(String[] args, String name) {

        final var months = months(this.paramsValue(args, name));
        final var title = format("Average {0}-month net monthly average savings and spending", months);
        new Savings(format, series, bar, console)
                .netAvgSavingSpent(months, title);

    }

    private void incomeAverageBySource(String[] args, String name) {

        final var months = months(this.paramsValue(args, name));
        new Savings(format, series, bar, console)
                .incomeAverageBySource(months);

    }

    private void incomeAveragePctBySource(String[] args, String name) {

        final var months = months(this.paramsValue(args, name));
        new Savings(format, series, bar, console)
                .incomeAverageBySource(months, true);
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
        new Positions(console, format, series)
                .portfolio(type, subtype, year, month);
    }

    private void returns(String[] args, String paranName, PortfolioReturns pr) {

        final var params = this.paramsValue(args, paranName);
        final var timeWeighted = Boolean.parseBoolean(params.getOrDefault("tw", "false"));
        final var withCash = Boolean.parseBoolean(params.getOrDefault("cash", "true"));
        final var startYear = Integer.parseInt(params.getOrDefault("start", SeriesReader.readEnvironment().getProperty("start.year")));
        pr.returns(nominal(params), withCash, startYear, timeWeighted);
    }

    private void ibkrPositions(int year) {

        this.series.getInvestments()
                .stream()
                .filter(Investment::isETF)
                .filter(inv -> Objects.nonNull(inv.getComment()))
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

    private void invEvoPct(String[] args, String paramName) {
        final var params = this.paramsValue(args, paramName);
        final var currency = Optional.ofNullable(params.get("type")).map(Currency::valueOf).orElse(null);
        new Investments(console, format, bar, series)
                .invEvoPct(currency, nominal(params));
    }

    private void positions(String[] args, String paramName) {
        final var params = this.paramsValue(args, paramName);
        new Positions(this.console, this.format, this.series)
                .positions(nominal(params));
    }

    private void dca(String[] args, String paramName) {
        final var params = this.paramsValue(args, paramName);

        final var type = params.getOrDefault("type", "q");
        new Positions(this.console, this.format, this.series)
                .dca(nominal(params), type);
    }

    private void invested(String[] args, String paramName) {
        final var params = this.paramsValue(args, paramName);

        final var group = params.getOrDefault("group", "q");
        final var type = params.getOrDefault("type", "long");
        new Positions(this.console, this.format, this.series)
                .invested(nominal(params), type, group);
    }

    private void invEvo(String[] args, String paramName) {
        final var params = this.paramsValue(args, paramName);
        final var currency = Optional.ofNullable(params.get("type"))
                .map(Currency::valueOf)
                .orElse(null);
        final var nominal = nominal(params);
        new Investments(console, format, bar, series)
                .invEvo(currency, nominal);
    }

    private void portfolioEvo(String[] args, String paramName) {
        this.console.appendLine(this.format.title("Portfolio Evolution"));
        final var params = this.paramsValue(args, paramName);
        final var type = params.get("type");
        new Investments(console, format, bar, series)
                .portfolioEvo(type, "p-evo-pct".equalsIgnoreCase(paramName));
    }

    private void savingsEvoChart() throws IOException {

        var s = this.series.realSavings(null);
        s.setName("Real");
        var nominal = this.series.nominalSavings();
        nominal.setName("Nominal");
        new TimeSeriesChart(new ChartStyle(ValueFormat.CURRENCY, Scale.LOG))
                .create("Savings", List.of(s, nominal), "savings.png");

    }

    private void savingsARSEvoChart() throws IOException {

        var s = this.series.realSavings(null).exchangeInto(ARS);
        s.setName("Real");
        new TimeSeriesChart(new ChartStyle(ValueFormat.CURRENCY, Scale.LOG))
                .create("Savings", List.of(s), "savings-ars.png");
    }

    private void incomeAccChart() {

        final var agg = new SimpleAggregation();

        final var unlp = agg.sum(this.series.incomeSource("unlp"));
        unlp.setName("UNLP");

        final var other = agg.sum(this.series.incomeSource("other-usd"));
        other.setName("Other USD");

        final var otherARS = agg.sum(this.series.incomeSource("other-ars"));
        otherARS.setName("Other ARS");

        final var lifia = agg.sum(this.series.incomeSource("lifia"));
        lifia.setName("LIFIA");

        final var despARS = agg.sum(this.series.incomeSource("despegar"));
        despARS.setName("DESP ARS");

        final var despUSD = agg.sum(this.series.incomeSource("despegar-split"));
        despUSD.setName("DESP USD");

        new TimeSeriesChart()
                .create("Income", List.of(other, lifia, unlp, despARS, despUSD, otherARS), "income_acc.png");
    }

    private void expensesChart(int m, boolean grouped) throws IOException {

        var expenseSeries = grouped
                ? this.series.getRealUSDExpensesByType()
                        .entrySet()
                        .stream()
                        .map(e -> this.sum(e.getKey(), e.getValue()))
                        .toList()
                : this.series.getRealUSDExpenses();

        if (m > 0) {
            var avg = new SimpleAggregation(m);
            expenseSeries = expenseSeries.stream().map(avg::average).toList();
        }

        var chartName = "Expenses";
        if (m > 0) {
            chartName = "Average " + m + " months " + chartName;
        }
        if (grouped) {
            chartName = "Grouped " + chartName;
        }
        new TimeSeriesChart()
                .create(chartName, expenseSeries, "expenses_" + (grouped ? "grouped" : "") + "_" + m + ".png");
    }

    private MoneyAmountSeries sum(String name, List<MoneyAmountSeries> series) {
        var s = series.stream().reduce(MoneyAmountSeries::add).get();
        s.setName(name);
        return s;
    }

    private void allCharts() {
        try {
            final var inv = new Investments(this.console, this.format, this.bar, this.series);
            final var pos = new Positions(console, format, series);
            final var savings = new Savings(format, series, bar, console);
            final var thisYear = LocalDate.now().getYear();

            this.expensesChart(12, true);
            this.incomeAccChart();
            this.savingsEvoChart();
            this.savingsARSEvoChart();
            savings.savingRate(thisYear);
            savings.netAvgSavingSpentChart(12);
            savings.netAvgSavingSpentChart(3);
            inv.mdrChart(true);
            inv.mdrChart(false);
            inv.mdrByYearChart();
            pos.portfolioChart(new PieChart(false), "all", USD_INFLATION.getTo().year(), USD_INFLATION.getTo().month());
            pos.portfolioChart(new PieChart(false), "equity", USD_INFLATION.getTo().year(), USD_INFLATION.getTo().month());
            pos.portfolioChartByGeography(new PieChart(false), "pct", USD_INFLATION.getTo().year(), USD_INFLATION.getTo().month());
            pos.portfolioChartByGeographyBreakUSA(new PieChart(false), "pct", USD_INFLATION.getTo().year(), USD_INFLATION.getTo().month());
            this.savingsChart(12);
            inv.savingsInvestmentsPercentChart();
            savings.savingsByIncomeChart();
            savings.spendingByRegularIncomeChart();
            inv.savedAndInvestedChart();
            inv.investmentsByClassChart();
            inv.projection();
        } catch (IOException ex) {
            LOGGER.error("Error generating chart.", ex);
        }
    }

    public static record CmdParam(String name, String argsDesc) {

        public CmdParam(String name, String argsDesc) {
            this.name = name;
            this.argsDesc = argsDesc;
        }

        public CmdParam(String name) {
            this(name, "");
        }
    }

    private void savingsChart(int m) throws IOException {

        var s = new SimpleAggregation(m)
                .average(this.series.realNetSavings());
        s.setName(m + " months");

        new TimeSeriesChart().create(
                "Savings Average " + m + " months",
                List.of(s),
                "savings-avg-" + (m < 10
                        ? ("0" + m)
                        : ("" + m)) + ".png");

    }

    private void ltiReport() {
        this.console.appendLine(this.format.title("LTI"));

        var dealPrice = new BigDecimal("19.50");
        this.console.appendLine(this.ltiLine(2023, new BigDecimal("5.941"), 113, 1354));
        this.console.appendLine(this.ltiLine(2024, new BigDecimal("7.556"), 352, 3588));
        this.console.appendLine(this.ltiLine(2025, new BigDecimal("16.3011123971"), 454, new BigDecimal("4866.52")));
        this.console.appendLine(this.ltiLine(2026, dealPrice, 430, 4639));
        this.console.appendLine(this.ltiLine(2027, dealPrice, 191, 2337));
        this.console.appendLine(this.ltiLine(2028, dealPrice, 192, 2337));
        this.console.appendLine(this.ltiLine(2029, dealPrice, 90, 1100));

    }

    private String ltiLine(int year, BigDecimal despPrice, int phantom, int cash) {
        return this.ltiLine(year, despPrice, phantom, new BigDecimal(cash));
    }

    private String ltiLine(int year, BigDecimal despPrice, int phantom, BigDecimal cash) {
        return MessageFormat.format("{0} {1}",
                YearMonth.of(year, 1).monthString(),
                this.format.currency(this.gross(despPrice, phantom, cash), 16)
        );
    }

    private MoneyAmount gross(BigDecimal desp, int phantom, BigDecimal cash) {
        return new MoneyAmount(cash, USD)
                .add(new MoneyAmount(desp.multiply(new BigDecimal(phantom)), USD));
    }

}
