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
package org.fede.calculator.report;

import org.fede.calculator.chart.TimeSeriesDatapoint;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.text.MessageFormat;
import static java.text.MessageFormat.format;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.fede.calculator.chart.ChartSeriesMapper;

import org.fede.calculator.criptoya.CriptoYaAPI;
import static org.fede.calculator.money.Currency.ARS;
import static org.fede.calculator.money.Inflation.USD_INFLATION;
import org.fede.calculator.money.series.Investment;
import java.time.YearMonth;
import static org.fede.calculator.money.Currency.EIMI;
import static org.fede.calculator.money.Currency.MEUD;
import static org.fede.calculator.money.Currency.RTWO;
import static org.fede.calculator.money.Currency.XRSU;
import static org.fede.calculator.money.Currency.USD;
import org.fede.calculator.chart.ChartStyle;
import org.fede.calculator.chart.PieChart;
import org.fede.calculator.chart.Scale;
import org.fede.calculator.chart.TimeSeriesChart;
import org.fede.calculator.chart.ValueFormat;
import org.fede.calculator.money.Currency;
import org.fede.calculator.money.Inflation;
import org.fede.calculator.money.MathConstants;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.Accumulator;
import org.fede.calculator.money.CPIInflation;
import org.fede.calculator.money.ForeignExchanges;
import org.fede.calculator.money.SingleHttpClientSupplier;
import org.fede.calculator.money.SlidingWindow;
import org.fede.calculator.money.series.JSONDataPoint;
import org.fede.calculator.money.series.JSONIndexSeries;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.SeriesReader;
import org.fede.calculator.money.series.YearMonthUtil;
import org.fede.util.Pair;
import org.jfree.data.time.TimeSeries;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.TerminalBuilder;
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

    private static final List<CmdParam> COMMANDS = commandParams().toList();

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
                () -> me.i(args, "i");

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

            case "lti" ->
                me::ltiReport;

            case "buy" ->
                () -> me.buy(
                new BigDecimal(me.paramsValue(args, "buy").getOrDefault("usd", "0").replaceAll(",", ".")),
                new BigDecimal(me.paramsValue(args, "buy").getOrDefault("eur", "0").replaceAll(",", ".")),
                new BigDecimal(me.paramsValue(args, "buy").getOrDefault("transfer", "50").replaceAll(",", ".")),
                Boolean.parseBoolean(me.paramsValue(args, "buy").getOrDefault("detail", "false"))
                );

            case "sell" ->
                () -> me.sell(
                new BigDecimal(me.paramsValue(args, "sell").getOrDefault("usd", "9970")),
                Boolean.parseBoolean(me.paramsValue(args, "sell").getOrDefault("oversell", "false")),
                Boolean.parseBoolean(me.paramsValue(args, "sell").getOrDefault("detail", "false"))
                );

            case "ppi" ->
                () -> new Investments(console, format, bar, series)
                .ppiTransfer(me.paramsValue(args, "ppi").getOrDefault("type", "full"));

            default ->
                () -> console.appendLine("Unknown parameter.");

        };
    }

    private static Stream<CmdParam> commandParams() {
        return Stream.of(
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
                new CmdParam("i", "y=current m=current g=false"),
                new CmdParam("pa"),
                new CmdParam("house-evo"),
                new CmdParam("expenses-src", "m=12"),
                new CmdParam("fire", "m=12"),
                new CmdParam("p-type-evo"),
                new CmdParam("p-type-evo-pct"),
                new CmdParam("condo"),
                new CmdParam("ccl"),
                new CmdParam("buy", "usd=9970 eur=0 transfer=50 detail=false"),
                new CmdParam("sell", "usd=9970 oversell=false detail=false"),
                new CmdParam("lti"),
                new CmdParam("bbpp-evo"),
                new CmdParam("routes"),
                new CmdParam("help"),
                new CmdParam("balances"),
                new CmdParam("all-charts"),
                new CmdParam("cash"),
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
                new CmdParam("invested", "type=(long*|all|CSPX|MEUD|EIMI|XRSU|fci|etf|pf|pfusd|pfars) group=(m|q*|h|y|all) nominal=false"),
                new CmdParam("mdr", "nominal=false cash=true start=1999 tw=false"),
                new CmdParam("saved-salaries-evo", "months=12"),
                new CmdParam("house", "years=(null|1|2|3|4|5|6|7|8|9|10)"),
                new CmdParam("income-evo", "months=12 ars=false"),
                new CmdParam("bbpp", "year=yyyy"),
                new CmdParam("bbppstatus"),
                new CmdParam("q"),
                new CmdParam("exit"),
                new CmdParam("ppi", "type=group|groupall|full*"),
                new CmdParam("savings-net-change", "m=12"),
                new CmdParam("savings-avg-pct", "m=12"),
                new CmdParam("expenses", "by=(year|half|quarter*|month) type=(taxes|insurance|phone|services|home|entertainment) m=12"),
                new CmdParam("expenses-change", "m=12"),
                new CmdParam("expenses-evo", "type=(full|taxes|insurance|services|home|entertainment) m=12"),
                new CmdParam("savings-evo", "type=(BO|LIQ|EQ)"),
                new CmdParam("dca", "type=(q*|h|y|m)"),
                new CmdParam("pos", "nominal=false egr=false")
        );
    }

    private static void help(ConsoleReports me) {
        COMMANDS.stream()
                .map(param -> format(" - {0} {1}", param.name, param.argsDesc))
                .sorted()
                .forEach(me::appendLine);

    }

    private static void handleCommand(String[] args, ConsoleReports me, Format format, Bar bar, Series series, Console console) throws IOException {

        final var st = System.nanoTime();
        final var params = Arrays.stream(args)
                .map(String::toLowerCase)
                .collect(toSet());

        if (params.isEmpty() || params.contains("help")) {
            help(me);
        } else {

            getAction(args, me, format, bar, series, console).run();
            me.appendLine("");
        }
        console.appendLine(MessageFormat.format("{0}ms", (System.nanoTime() - st) / 1_000_000.0d));
        console.printReport();

    }

    public static void main(String[] args) {
        try {

            final var console = new FastConsole();
            final var format = new Format();
            final var bar = new Bar(console, format);
            final var series = new Series();
            final var me = new ConsoleReports(console, format, bar, series);

            new Positions(console, format, series).checkConsistency();

            if (args.length > 0) {
                handleCommand(args, me, format, bar, series, console);
            } else {

                LineReader reader = LineReaderBuilder.builder()
                        .terminal(TerminalBuilder.builder()
                                .system(true)
                                .build())
                        .parser(new DefaultParser())
                        .variable(LineReader.HISTORY_FILE, Paths.get(CACHE_DIR + "/.command_history"))
                        .completer(
                                new StringsCompleter(
                                        COMMANDS.stream()
                                                .map(CmdParam::name)
                                                .toList()))
                        .build();

                while (true) {

                    String line;
                    try {
                        line = reader.readLine("> "); // prompt
                    } catch (UserInterruptException | EndOfFileException e) {
                        break; // Ctrl+C or Ctrl+D
                    }
                    if (line == null
                            || line.trim().equalsIgnoreCase("q")
                            || line.trim().equalsIgnoreCase("exit")) {
                        break;
                    }
                    handleCommand(line.split("\\s+"), me, format, bar, series, console);

                }
            }
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
            final var initialAmount = BigDecimal.valueOf(10000l);

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

    private void savings(String[] args, String paramName) {
        new Savings(format, series, bar, console)
                .savings(this.paramsValue(args, paramName));
    }

    private void condo() {
        this.bar.evolution(format("Average {0}-month condo expenses.", 12),
                new SlidingWindow(12).average(this.series.getRealUSDCondoExpenses()),
                25);
    }

    private void expenses(String[] args, String type) {
        new Expenses(series, console, bar, format)
                .expenses(this.paramsValue(args, type));
    }

    private void savingsPercentChange(String[] args, String paramName) {
        final var months = Integer.parseInt(this.paramsValue(args, paramName).getOrDefault(MONTHS_PARAM, "1"));
        new Savings(format, series, bar, console)
                .savingsPercentChange(months);
    }

    private void incomeDelta(String[] args, String paramName) {
        final var months = months(this.paramsValue(args, paramName));
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

        this.bar.evolution(format("Average {0}-month expenses.", months), new SlidingWindow(months).average(s), 30);
    }

    private void savingChange(String[] args, String paramName) {

        final var months = Integer.parseInt(this.paramsValue(args, paramName).getOrDefault(MONTHS_PARAM, "1"));

        var scale = switch (months) {
            case 1 ->
                800;
            case 2, 3 ->
                1000;
            case 4, 5, 6 ->
                1500;
            default ->
                240 * months;
        };

        this.appendLine(this.format.title(format("{0}-month Savings Change", months)));
        this.bar.evolution(format("{0}-month Savings Change", months), new SlidingWindow(months)
                .change(this.series.realSavings(null)), scale);
    }

    private void expensesChange(String[] args, String name) {

        final var params = this.paramsValue(args, name);
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

        final var pensionARS = SeriesReader.readEnvironment().getProperty("goal.pension");

        int pensionAsIntValue = 0;
        final var pension = params.get("pension");
        if (pension == null) {
            pensionAsIntValue
                    = ForeignExchanges.getForeignExchange(Currency.ARS, Currency.USD)
                            .exchange(
                                    new MoneyAmount(
                                            new BigDecimal(pensionARS), Currency.ARS),
                                    Currency.USD, YearMonth.now()).amount().intValue();
        } else {
            pensionAsIntValue = Integer.parseInt(pension);
        }

        final var badReturnYears = Integer.parseInt(params.getOrDefault("srr", SeriesReader.readEnvironment().getProperty("goal.badreturns")));
        final var bbppTax = Double.parseDouble(params.getOrDefault("bbpp", SeriesReader.readEnvironment().getProperty("goal.bbpp"))) / 100.0d;

        final var goal = new Goal(this.console, this.format, this.series, this.bar, bbppTax);

        final var todaySavings = this.series.currentSavingsUSD();

        final var invested = this.series.realSavings("EQ").getAmount(Inflation.USD_INFLATION.getTo());

        goal.goal(
                trials,
                months,
                inflation,
                retirementAge,
                BigDecimal.valueOf(extraCash),
                age,
                pensionAsIntValue,
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

    private void i(String[] args, String name) {
        final var params = this.paramsValue(args, name);
        final var year = Optional.ofNullable(params.get("y"))
                .map(Integer::parseInt)
                .orElseGet(USD_INFLATION.getTo()::getYear);
        final var month = Optional.ofNullable(params.get("m"))
                .map(Integer::parseInt)
                .orElseGet(USD_INFLATION.getTo()::getMonthValue);

        final var grouped = Optional.ofNullable(params.get("g"))
                .map(Boolean::parseBoolean)
                .orElse(false);

        new Investments(console, format, bar, series)
                .investments(YearMonth.of(year, month).atEndOfMonth(), grouped);

    }

    private void portfolio(String[] args, String name) {

        final var params = this.paramsValue(args, name);
        final var type = params.getOrDefault("type", "full");
        final var subtype = Optional.ofNullable(params.get("subtype"))
                .map(AssetClass::valueOf)
                .orElse(null);
        final var year = Optional.ofNullable(params.get("y"))
                .map(Integer::parseInt)
                .orElseGet(USD_INFLATION.getTo()::getYear);
        final var month = Optional.ofNullable(params.get("m"))
                .map(Integer::parseInt)
                .orElseGet(USD_INFLATION.getTo()::getMonthValue);
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
                .filter(inv -> inv.getInitialDate().getYear() <= year)
                .filter(inv -> inv.getOut() == null || inv.getOut().getDate().getYear() > year)
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
                .positions(
                        nominal(params),
                        Boolean.parseBoolean(params.getOrDefault("egr", "false"))
                );
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
                .create("Savings", List.of(s, nominal), "savings");
    }

    private void savingsARSEvoChart() throws IOException {

        var s = this.series.realSavings(null).exchangeInto(ARS);
        s.setName("Real USD expressed in ARS");
        new TimeSeriesChart(new ChartStyle(ValueFormat.CURRENCY, Scale.LOG))
                .create("Savings ARS", List.of(s), "savings-ars");
    }

    private void incomeAccChart() {

        final var agg = new Accumulator();

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
                .create("Income", List.of(other, lifia, unlp, despARS, despUSD, otherARS), "income_acc");
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
            var avg = new SlidingWindow(m);
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
                .create(chartName, expenseSeries, "expenses_" + (grouped ? "grouped" : "") + "_" + m);
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
            final var fire = new Fire(format, series, console);

            this.expensesChart(12, true);
            this.incomeAccChart();
            this.savingsEvoChart();
            this.savingsARSEvoChart();
            savings.netAvgSavingSpentChart(12);
            savings.netAvgSavingSpentChart(3);
            inv.mdrChart(true);
            inv.mdrChart(false);
            inv.mdrByYearChart();
            pos.portfolioChartByGeography(false, "pct", USD_INFLATION.getTo().getYear(), USD_INFLATION.getTo().getMonthValue());
            this.savingsChart();
            inv.savingsInvestmentsPercentChart();
            savings.savingsByIncomeChart();
            savings.spendingByRegularIncomeChart();
            savings.spendingByYear();
            inv.savedAndInvestedChart();
            inv.investmentsByClassChart();

            inv.projection(MoneyAmount.zero(USD));
            inv.projection(SeriesReader.readUSD("futureSavingsByYear"));
            inv.projection(SeriesReader.readUSD("futureSavingsByYear2"));
            fire.fireChartFuture();
            fire.fireChartBudgets(12);
            fire.fireChartBudgets(24);
            savings.savingRate(12);
            savings.savingRate(24);
            savings.savingRate(6);
            inv.investmentScatterChart(Currency.CSPX);
            inv.investmentScatterChart(Currency.SXR8);
            inv.investmentScatterChart(Currency.MEUD);
            inv.investmentScatterChart(Currency.MEUS);
            inv.investmentScatterChart(Currency.XRSU);
            inv.investmentScatterChart(Currency.EIMI, ValueFormat.CURRENCY_DECIMALS);
            inv.investmentScatterChart(Currency.EMIM, ValueFormat.CURRENCY_DECIMALS);
            inv.investmentScatterChart(Currency.XUSE, ValueFormat.CURRENCY_DECIMALS);
            inv.investmentScatterChart(Currency.RTWO);
            inv.investmentScatterChart(Currency.RTWOE);
            this.averageSpendingPortfolioPercent();
            //this.averageIncomePortfolioPercent();
            this.inflation();
            inv.benchmarks();

        } catch (IOException ex) {
            LOGGER.error("Error generating charts.", ex);
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

    private void savingsChart() throws IOException {

        var ss = IntStream.of(12, 24, 36)
                .mapToObj(m -> {
                    var s = new SlidingWindow(m).average(this.series.realNetSavings());
                    s.setName(m + " months");
                    return s;
                })
                .toList();

        new TimeSeriesChart().create(
                "Savings Average",
                ss,
                "savings-avg");

    }

    private void ltiReport() {
        this.console.appendLine(this.format.title("LTI"));

        var dealPrice = new BigDecimal("19.50");
        this.console.appendLine(this.ltiLine(2023, new BigDecimal("5.941"), 113, 1354));
        this.console.appendLine(this.ltiLine(2024, new BigDecimal("7.556"), 352, 3588));
        this.console.appendLine(this.ltiLine(2025, new BigDecimal("16.3011123971"), 454, new BigDecimal("4866.52")));
        this.console.appendLine(this.ltiLine(2026, BigDecimal.ZERO, 1, 13107));
        this.console.appendLine(this.ltiLine(2027, dealPrice, 191, 2337));
        this.console.appendLine(this.ltiLine(2028, dealPrice, 192, 2337));
        this.console.appendLine(this.ltiLine(2029, dealPrice, 90, 1100));

    }

    private String ltiLine(int year, BigDecimal despPrice, int phantom, int cash) {
        return this.ltiLine(year, despPrice, phantom, BigDecimal.valueOf(cash));
    }

    private String ltiLine(int year, BigDecimal despPrice, int phantom, BigDecimal cash) {
        return MessageFormat.format("{0} {1}",
                YearMonthUtil.monthString(YearMonth.of(year, 1)),
                this.format.currency(this.gross(despPrice, phantom, cash), 16)
        );
    }

    private MoneyAmount gross(BigDecimal desp, int phantom, BigDecimal cash) {
        return new MoneyAmount(cash, USD)
                .add(new MoneyAmount(desp.multiply(new BigDecimal(phantom)), USD));
    }

    private TimeSeries portfolioSpendingSeries(
            int months,
            MoneyAmountSeries savings,
            MoneyAmountSeries expenses) {
        var averageSpenses = new SlidingWindow(months).average(expenses);
        List<TimeSeriesDatapoint> s = new ArrayList<>();

        var monthsInAYear = BigDecimal.valueOf(12l);
        var ym = YearMonth.of(2015, 1);
        while (ym.compareTo(savings.getTo()) <= 0) {
            s.add(
                    new TimeSeriesDatapoint(
                            ym,
                            averageSpenses.getAmount(ym).amount()
                                    .multiply(monthsInAYear, MathConstants.C)
                                    .divide(savings.getAmount(ym).amount(), MathConstants.C)));
            ym = ym.plusMonths(1);
        }
        return ChartSeriesMapper.asTimeSeries(s, "Avg. " + months + "-month");
    }

    private void averageSpendingPortfolioPercent() {

        MoneyAmountSeries savings = this.series.realSavings(null);

        MoneyAmountSeries expenses = this.series.getRealUSDExpensesByType()
                .values()
                .stream()
                .flatMap(Collection::stream)
                .reduce(MoneyAmountSeries::add)
                .get();

        new TimeSeriesChart(new ChartStyle(ValueFormat.PERCENTAGE, Scale.LINEAR))
                .createFromTimeSeries("Portfolio Spending",
                        List.of(
                                //this.portfolioSpendingSeries(6, savings, expenses),
                                this.portfolioSpendingSeries(12, savings, expenses),
                                this.portfolioSpendingSeries(18, savings, expenses),
                                this.portfolioSpendingSeries(24, savings, expenses),
                                this.portfolioSpendingSeries(36, savings, expenses)
                        ),
                        "portfolio-spending");

    }

    private void buy(BigDecimal usd, BigDecimal eur, BigDecimal transfer, boolean detail) {
        RebalancingReport.equity(format, series, console)
                .buy(
                        new MoneyAmount(usd, USD),
                        new MoneyAmount(eur, Currency.EUR),
                        new MoneyAmount(transfer, USD),
                        detail);
    }

    private void sell(BigDecimal usd, boolean allowOversell, boolean detail) {
        RebalancingReport.equity(format, series, console)
                .sell(new MoneyAmount(usd, USD), allowOversell, detail);
    }
    
    private void inflation() {

        var inflation = new CPIInflation(
                new JSONIndexSeries(
                        Stream.concat(
                                SeriesReader.read("index/bls.json", SeriesReader.INDEX_SERIES_TYPE_REFERENCE).stream(),
                                SeriesReader.read("index/bls-hist.json", SeriesReader.INDEX_SERIES_TYPE_REFERENCE).stream())
                                .sorted(Comparator.comparing(JSONDataPoint::yearMonth))
                                .toList()),
                USD);

        new TimeSeriesChart(new ChartStyle(ValueFormat.CURRENCY, Scale.LINEAR))
                .createFromTimeSeries(
                        "Inflation",
                        List.of(
                                ChartSeriesMapper.asTimeSeries(
                                        inflation.adjust(
                                                new MoneyAmount(
                                                        BigDecimal.ONE.movePointRight(2),
                                                        USD),
                                                YearMonth.now()))),
                        USD,
                        "inflation");
    }

}
