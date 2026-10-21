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
import java.time.Month;
import java.time.Period;
import java.util.ArrayList;
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
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.fede.calculator.chart.ChartSaver;
import org.fede.calculator.chart.ChartSeriesMapper;

import org.fede.calculator.criptoya.CriptoYaAPI;
import static org.fede.calculator.money.Currency.ARS;
import static org.fede.calculator.money.Inflation.usdInflation;
import org.fede.calculator.money.series.Investment;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.Set;
import java.util.function.Function;
import static org.fede.calculator.money.Currency.EIMI;
import static org.fede.calculator.money.Currency.RTWO;
import static org.fede.calculator.money.Currency.XRSU;
import static org.fede.calculator.money.Currency.CSPX;
import static org.fede.calculator.money.Currency.RTWOE;
import static org.fede.calculator.money.Currency.MEUS;
import static org.fede.calculator.money.Currency.MEUD;
import static org.fede.calculator.money.Currency.USD;
import org.fede.calculator.chart.ChartStyle;
import org.fede.calculator.chart.Scale;
import org.fede.calculator.chart.TimeSeriesChart;
import org.fede.calculator.chart.ValueFormat;
import org.fede.calculator.money.Currency;
import org.fede.calculator.money.MathConstants;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.Accumulator;
import static org.fede.calculator.money.Currency.EMIM;
import org.fede.calculator.money.SingleHttpClientSupplier;
import org.fede.calculator.money.SlidingWindow;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.SeriesReader;
import org.fede.calculator.money.series.YearMonthUtil;
import org.fede.util.Pair;
import org.jfree.data.time.TimeSeries;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.ParsedLine;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.TerminalBuilder;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
public class ConsoleReports {

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

    private void soldInv() {
        new Investments(this.console, this.format, this.bar, this.series).soldInv();
    }

    private void invReport(String[] args, String paramName) {

        final var params = this.paramsValue(args, paramName);

        final var type = params.getOrDefault("type", "all");

        new Investments(this.console, this.format, this.bar, this.series)
                .inv("all".equalsIgnoreCase(type) ? x -> true : x -> this.investmentFilter(x, type), nominal(params));

    }

    private boolean investmentFilter(Investment i, String type) {

        Map<Currency, Set<Currency>> equivalences = new EnumMap<>(
                Map.of(EIMI, Set.of(EIMI, EMIM),
                        CSPX, Set.of(CSPX, Currency.SXR8),
                        RTWO, Set.of(RTWO, RTWOE),
                        MEUD, Set.of(MEUD, MEUS, Currency.XUSE)
                ));

        return switch (type.toLowerCase()) {
            case "all" ->
                true;
            case "r2k" ->
                i.getCurrency() == XRSU || i.getCurrency() == RTWO || i.getCurrency() == Currency.RTWOE;

            case "us" ->
                !(i.getCurrency() != Currency.SXR8
                && i.getCurrency() != CSPX
                && i.getCurrency() != RTWO
                && i.getCurrency() != RTWOE);

            case "exus" ->
                i.getCurrency() != Currency.SXR8
                && i.getCurrency() != CSPX
                && i.getCurrency() != RTWO
                && i.getCurrency() != RTWOE;
            default ->
                equivalences.getOrDefault(Currency.valueOf(type.toUpperCase()), Set.of(Currency.valueOf(type.toUpperCase())))
                .contains(i.getCurrency());
        };

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

            case "inv-sold" ->
                me::soldInv;

            case "fire" ->
                () -> new Fire(format, series, console)
                .fire();

            case "fi" ->
                () -> new Fire(format, series, console)
                .fiReport();
            case "re" ->
                () -> new Fire(format, series, console)
                .reReport();

            case "savings" ->
                () -> me.savings(args, "savings");

            case "eras" ->
                () -> me.investmentEras(
                nominal(me.paramsValue(args, "eras")),
                Boolean.parseBoolean(me.paramsValue(args, "eras").getOrDefault("cash", "false"))
                );

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

            case "income" ->
                () -> me.income(args);

            case "sev" ->
                me::severance;

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

            case "expenses-grouped" ->
                me::groupedByYear;

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

            case "spend" ->
                () -> me.spend(args, "spend");

            case "spend-avg" ->
                () -> me.spendAvg(args, "spend-avg");

            case "bbpp" ->
                () -> me.bbpp(args, "bbpp");

            case "cedular" ->
                () -> me.cedular(args, "cedular");

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
            case "xirr" ->
                () -> me.xirrReturns(args, "xirr", new PortfolioReturns(series, console, format, bar));
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
                Boolean.parseBoolean(me.paramsValue(args, "sell").getOrDefault("detail", "false")),
                me.paramsValue(args, "sell").getOrDefault("fifo", "isin")
                );

            default ->
                () -> console.appendLine("Unknown parameter.");

        };
    }

    private static CommandArgument arg(String name, String... values) {
        return new CommandArgument(name, List.of(values));
    }

    private static CommandArgument bool(String name) {
        return new CommandArgument(name, List.of("true", "false"));
    }

    private static CommandArgument currencies(String name, List<String> nonCurrencyvValues) {

        return new CommandArgument(name, Stream.concat(
                nonCurrencyvValues.stream(),
                Arrays.stream(Currency.values()).map(Enum::name)).toList());
    }

    private static CommandArgument etfs(String name, List<String> nonCurrencyvValues) {

        return new CommandArgument(name, Stream.concat(
                nonCurrencyvValues.stream(),
                Stream.of(CSPX, XRSU, EIMI, EMIM, MEUD, MEUS, RTWO, RTWOE, Currency.XUSE)
                        .map(Enum::name)).toList());
    }

    private static CommandArgument spending(String name) {
        return new CommandArgument(name,
                Arrays.stream(SpendingSeries.values()).map(Enum::name).toList());
    }

    private static Stream<CmdParam> commandParams() {
        return Stream.of(
                new CmdParam("savings-change", "m=1", arg("m")),
                new CmdParam("savings-change-pct", "m=1", arg("m")),
                new CmdParam("i", "y=current m=current g=false", arg("y"), arg("m"), bool("g")),
                new CmdParam("pa"),
                new CmdParam("house-evo"),
                new CmdParam("fire"),
                new CmdParam("fi"),
                new CmdParam("re"),
                new CmdParam("p-type-evo"),
                new CmdParam("p-type-evo-pct"),
                new CmdParam("ccl"),
                new CmdParam("buy", "usd=9970 eur=0 transfer=50 detail=false",
                        arg("usd"), arg("eur"), arg("transfer"), bool("detail")),
                new CmdParam("sell", "usd=9970 oversell=false detail=false fifo=symbol",
                        arg("usd"), bool("oversell"), bool("detail"), arg("fifo", "symbol", "isin")),
                new CmdParam("lti"),
                new CmdParam("bbpp-evo"),
                new CmdParam("routes"),
                new CmdParam("help"),
                new CmdParam("balances"),
                new CmdParam("all-charts"),
                new CmdParam("cash"),
                new CmdParam("sev"),
                new CmdParam("income-src", "m=12", arg("m")),
                new CmdParam("pf", "detail=false nominal=false", bool("detail"), bool("nominal")),
                new CmdParam("eras", "nominal=false cash=false", bool("nominal"), bool("cash")),
                new CmdParam("income-src-pct", "m=12", arg("m")),
                new CmdParam("income-acc"),
                new CmdParam("income-acc-pct"),
                new CmdParam("savings-avg", "m=12", arg("m")),
                new CmdParam("income-table"),
                new CmdParam("income-year-table"),
                new CmdParam("savings-dist"),
                new CmdParam("savings-dist-pct"),
                new CmdParam("income-avg-change", "m=12", arg("m")),
                new CmdParam("income", "by=(year|half|quarter*) months=12",
                        arg("by", "year", "half", "quarter"), arg("months")),
                new CmdParam("savings", "by=(year|half|quarter*)",
                        arg("by", "year", "half", "quarter")),
                new CmdParam("p", "type=(full*|pct) subtype=(all*|equity|bond|commodity|cash) y=current m=current",
                        arg("type", "full", "pct"),
                        arg("subtype", "all", "equity", "bond", "commodity", "cash"),
                        arg("y"), arg("m")),
                new CmdParam("p-evo", "type=(all|ETF|BONO|PF|FCI)",
                        arg("type", "all", "ETF", "BONO", "PF", "FCI")),
                new CmdParam("p-evo-pct", "type=(all*|ETF|BONO|PF|FCI)",
                        arg("type", "all", "ETF", "BONO", "PF", "FCI")),
                new CmdParam("inv", "type=(all*|CSPX|MEUD|EIMI|XRSU|exus|r2k|us) nominal=false",
                        etfs("type", List.of("all", "exus", "r2k", "us")),
                        bool("nominal")),
                new CmdParam("inv-evo", "type=(CSPX|MEUD|EIMI|XRSU) nominal=false",
                        etfs("type", List.of()),
                        bool("nominal")),
                new CmdParam("inv-evo-pct", "type=(CSPX|MEUD|EIMI|XRSU) nominal=false",
                        etfs("type", List.of()),
                        bool("nominal")),
                new CmdParam("invested", "type=(long*|all|CSPX|MEUD|EIMI|XRSU|fci|etf|pf|pfusd|pfars) group=(m|q*|h|y|all)",
                        currencies("type",
                                List.of("long", "all", "fci", "etf", "pf", "pfusd", "pfars")),
                        arg("group", "m", "q", "h", "y", "all")),
                new CmdParam("mdr", "nominal=false cash=true start=1999 tw=false",
                        bool("nominal"), bool("cash"), arg("start"), bool("tw")),
                new CmdParam("xirr", "nominal=false cash=true start=1999",
                        bool("nominal"), bool("cash"), arg("start")),
                new CmdParam("house", "years=(null|1|2|3|4|5|6|7|8|9|10)",
                        arg("years", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10")),
                new CmdParam("income-evo", "months=12 ars=false", arg("months"), bool("ars")),
                new CmdParam("bbpp", "year=yyyy", arg("year")),
                new CmdParam("cedular", "year=yyyy", arg("year")),
                new CmdParam("bbppstatus"),
                new CmdParam("q"),
                new CmdParam("expenses-grouped"),
                new CmdParam("exit"),
                new CmdParam("savings-net-change", "m=12", arg("m")),
                new CmdParam("savings-avg-pct", "m=12", arg("m")),
                new CmdParam("expenses", "by=(year|half|quarter*|month) type=(ESSENTIAL|DISCRETIONARY|LUXURY) m=12",
                        arg("by", "year", "half", "quarter", "month"),
                        arg("type", "ESSENTIAL", "DISCRETIONARY", "LUXURY"),
                        arg("m")),
                new CmdParam("spend", "m=12 in=(SERIES,...) ex=(SERIES,...)",
                        arg("m"),
                        spending("in"),
                        spending("ex")),
                new CmdParam("spend-avg", "m=12 n=12 t=10 ex=UNCLASSIFIED",
                        arg("m"),
                        arg("n"),
                        arg("t"),
                        spending("ex")),
                new CmdParam("savings-evo", "type=(BO|LIQ|EQ)",
                        arg("type", "BO", "LIQ", "EQ")),
                new CmdParam("dca", "type=(q*|h|y|m)",
                        arg("type", "q", "h", "y", "m")),
                new CmdParam("pos", "nominal=false egr=false", bool("nominal"), bool("egr"))
        );
    }

    private static void help(ConsoleReports me) {
        COMMANDS.stream()
                .map(param -> format(" - {0} {1}", param.name, param.argsDesc))
                .sorted()
                .forEach(me::appendLine);

    }

    private static void handleCommand(String[] args, ConsoleReports me, Format format, Bar bar, Series series, Console console) throws IOException {

        final var params = Arrays.stream(args)
                .map(String::toLowerCase)
                .collect(toSet());

        if (params.isEmpty() || params.contains("help")) {
            help(me);
        } else {

            getAction(args, me, format, bar, series, console).run();
            me.appendLine("");
        }
        console.printReport();

    }

    public static void main(String[] args) {
        try {

            final var console = new FastConsole();
            final var format = new Format();
            final var bar = new Bar(console, format);
            final var series = new Series();
            final var me = new ConsoleReports(console, format, bar, series);

            if (args.length > 0) {
                handleCommand(args, me, format, bar, series, console);
                ChartSaver.awaitPending();
            } else {

                LineReader reader = LineReaderBuilder.builder()
                        .terminal(TerminalBuilder.builder()
                                .system(true)
                                .build())
                        .parser(new DefaultParser())
                        .variable(LineReader.HISTORY_FILE, Paths.get(CACHE_DIR + "/.command_history"))
                        .completer(new CommandCompleter(COMMANDS))
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
                    ChartSaver.awaitPending();

                }
                new Positions(console, format, series).checkConsistency();
            }

        } catch (Exception ex) {
            System.err.println("Unexpected error. " + ex.getMessage());
            ex.printStackTrace(System.err);
        } finally {
            ChartSaver.shutdown();
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
            System.err.println("Unexpected error. " + ex.getMessage());
            ex.printStackTrace(System.err);
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
                    .houseIrrecoverableCosts(usdInflation().getTo());
        } else {
            new House(console, format, bar)
                    .houseIrrecoverableCosts(YearMonth.of(2010 + Integer.parseInt(years), 8));
        }
    }

    private void savings(String[] args, String paramName) {
        new Savings(format, series, bar, console)
                .savings(this.paramsValue(args, paramName));
    }

    private void expenses(String[] args, String type) {
        new Expenses(series, console, bar, format)
                .expenses(this.paramsValue(args, type));
    }

    private void spend(String[] args, String type) {
        new Expenses(series, console, bar, format)
                .spending(this.paramsValue(args, type));
    }

    private void spendAvg(String[] args, String type) {
        new Expenses(series, console, bar, format)
                .spendingAverageTop(this.paramsValue(args, type));
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

    private void bbpp(String[] args, String paramName) {
        final var params = this.paramsValue(args, paramName);

        new BBPP(format, series, console)
                .bbpp(Integer.parseInt(params.getOrDefault("year", SeriesReader.readEnvironment().getProperty("bbpp.year"))));
    }

    private void cedular(String[] args, String paramName) {
        final var year = Optional.ofNullable(this.paramsValue(args, paramName).get("year"))
                .map(Integer::parseInt)
                .orElseGet(() -> YearMonth.now().getYear() - 1);
        new CedularTax(this.format, this.series, this.console).report(year);
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
                .orElseGet(usdInflation().getTo()::getYear);
        final var month = Optional.ofNullable(params.get("m"))
                .map(Integer::parseInt)
                .orElseGet(usdInflation().getTo()::getMonthValue);

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
                .orElseGet(usdInflation().getTo()::getYear);
        final var month = Optional.ofNullable(params.get("m"))
                .map(Integer::parseInt)
                .orElseGet(usdInflation().getTo()::getMonthValue);
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

    private void xirrReturns(String[] args, String paranName, PortfolioReturns pr) {

        final var params = this.paramsValue(args, paranName);
        final var withCash = Boolean.parseBoolean(params.getOrDefault("cash", "true"));
        final var startYear = Integer.parseInt(params.getOrDefault("start", SeriesReader.readEnvironment().getProperty("start.year")));
        pr.xirrReturns(nominal(params), withCash, startYear);
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
                .invested(type, group);
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

        final var otherEur = agg.sum(this.series.incomeSource("other-eur"));
        otherEur.setName("Other €");

        final var otherARS = agg.sum(this.series.incomeSource("other-ars"));
        otherARS.setName("Other ARS");

        final var lifia = agg.sum(this.series.incomeSource("lifia"));
        lifia.setName("LIFIA");

        final var despARS = agg.sum(this.series.incomeSource("despegar"));
        despARS.setName("DESP ARS");

        final var despUSD = agg.sum(this.series.incomeSource("despegar-split"));
        despUSD.setName("DESP USD");

        new TimeSeriesChart()
                .create("Income", List.of(other, otherEur, lifia, unlp, despARS, despUSD, otherARS), "income_acc");
    }

    private void expensesChart(boolean grouped) throws IOException {

        var expenseSeries = grouped
                ? this.series.summedExpensesBy(ExpenseTier::of, true)
                        .values()
                        .stream()
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .toList()
                : List.of(this.series.realExpenses(null, true));

        var chartName = "Average ";

        if (grouped) {
            chartName = "Grouped " + chartName;
        }
        new TimeSeriesChart()
                .create(chartName, expenseSeries, "spending-" + (grouped ? "grouped" : ""));
    }

    private void spendingCharts(Savings savings) throws IOException {
        // spending-grouped
        this.expensesChart(true);

        // spending-savings
        savings.netAvgSavingSpentChart(12);

        // spending-portfolio-retirement
        // spending-portfolio
        this.averageSpendingPortfolioPercent();

        // spending-retirement
        this.futurePortfolioSpending();

        new Expenses(this.series, this.console, this.bar, this.format)
                .regularExpensesByYearChart(savings);

    }

    private void allCharts() {
        try {
            final var inv = new Investments(this.console, this.format, this.bar, this.series);
            final var pos = new Positions(console, format, series);
            final var savings = new Savings(format, series, bar, console);

            this.spendingCharts(savings);

            this.incomeAccChart();
            this.savingsEvoChart();
            this.savingsARSEvoChart();

            savings.spendingByRegularIncomeChart();

            inv.mdrChart(true);
            inv.mdrChart(false);
            inv.mdrByYearChart();

            inv.etfCompositionChart();

            // pie
            pos.portfolioChartByGeography(false, "pct", usdInflation().getTo().getYear(), usdInflation().getTo().getMonthValue());

            this.savingsChart();
            inv.savingsInvestmentsPercentChart();
            savings.savingsByIncomeChart();
            savings.spendingByYear();
            inv.savedAndInvestedChart();
            inv.investmentsByClassChart();

            final var swr = new CAEYSafeWithdrawalRate();

            Function<BigDecimal, String> shortFormat = this.format::currencyShort;

            swr.monthlySafeWithdrawalByCapeChart("swr-by-cape-050", new BigDecimal("0.5"), shortFormat);
            swr.monthlySafeWithdrawalByCapeChart("swr-by-cape-075", new BigDecimal("0.75"), shortFormat);

            savings.savingRate(12);

            inv.benchmarks();
            this.averageMonthsSavedInCash();
            pos.investedChart(false, "all");

            EtfPriceChart etfPriceChart = new EtfPriceChart(this.series);

            etfPriceChart.create(Currency.CSPX);
            etfPriceChart.create(Currency.RTWO);
            etfPriceChart.create(Currency.XUSE);
            etfPriceChart.create(Currency.MEUD);
            etfPriceChart.create(Currency.EIMI);
            etfPriceChart.create(Currency.XRSU);

        } catch (IOException ex) {

            System.err.println("Error generating charts. " + ex.getMessage());
            ex.printStackTrace(System.err);
        }
    }

    public static record CommandArgument(String name, List<String> values) {

        public CommandArgument(String name) {
            this(name, List.of());
        }
    }

    public static record CmdParam(String name, String argsDesc, List<CommandArgument> arguments) {

        public CmdParam(String name, String argsDesc, CommandArgument... arguments) {
            this(name, argsDesc, List.of(arguments));
        }

        public CmdParam(String name, String argsDesc) {
            this(name, argsDesc, List.of());
        }

        public CmdParam(String name) {
            this(name, "", List.of());
        }
    }

    private static final class CommandCompleter implements Completer {

        private final Map<String, CmdParam> byName;

        CommandCompleter(List<CmdParam> commands) {
            this.byName = commands.stream()
                    .collect(toMap(CmdParam::name, Function.identity(), (a, b) -> a));
        }

        @Override
        public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
            if (line.wordIndex() <= 0) {
                this.byName.keySet()
                        .forEach(name -> candidates.add(new Candidate(name)));
                return;
            }

            final var command = this.byName.get(line.words().get(0));
            if (command == null) {
                return;
            }

            for (var argument : command.arguments()) {
                if (argument.values().isEmpty()) {
                    final var key = argument.name() + "=";
                    // complete=false so JLine does not append a trailing space
                    // and the user can keep typing the value.
                    candidates.add(new Candidate(key, key, null, null, null, null, false));
                } else {
                    for (var value : argument.values()) {
                        candidates.add(new Candidate(argument.name() + "=" + value));
                    }
                }
            }
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
            String prefix,
            int months,
            MoneyAmountSeries savings,
            MoneyAmountSeries expenses) {
        final var averageSpenses = months == 0 ? expenses : new SlidingWindow(months).average(expenses);
        final List<TimeSeriesDatapoint> s = new ArrayList<>();

        final var monthsInAYear = BigDecimal.valueOf(12l);
        var ym = YearMonth.of(2015, 1);
        while (ym.compareTo(savings.getTo()) <= 0) {
            s.add(
                    new TimeSeriesDatapoint(
                            ym,
                            averageSpenses
                                    .getAmount(ym)
                                    .amount()
                                    .multiply(monthsInAYear, MathConstants.C)
                                    .divide(savings.getAmount(ym).amount(), MathConstants.C)));
            ym = ym.plusMonths(1);
        }

        final var label = months == 0
                ? " Average"
                : " Avg. " + months + "-month";

        return ChartSeriesMapper.asTimeSeries(s, prefix + label);
    }

    private void averageSpendingPortfolioPercent() {

        final MoneyAmountSeries savings = this.series.realSavings(null);

        final MoneyAmountSeries actualExpenses = this.series.realExpenses(null, true);

        final var future = new Future(this.series);

        new TimeSeriesChart(new ChartStyle(ValueFormat.PERCENTAGE, Scale.LINEAR))
                .createFromTimeSeries("Portfolio Spending",
                        List.of(this.portfolioSpendingSeries("Actual", 0, savings, actualExpenses)),
                        "spending-portfolio");

        final var retirementSpendingMap = future.retirementSpending();
        final var essential = retirementSpendingMap.get(ExpenseTier.ESSENTIAL);
        final var total = retirementSpendingMap.get(ExpenseTier.DISCRETIONARY)
                .add(essential)
                .add(retirementSpendingMap.get(ExpenseTier.LUXURY));

        new TimeSeriesChart(new ChartStyle(ValueFormat.PERCENTAGE, Scale.LINEAR))
                .createFromTimeSeries("Portfolio Retirement Spending",
                        List.of(
                                this.portfolioSpendingSeries("Total", 0, savings, total),
                                this.portfolioSpendingSeries("Essential", 0, savings, essential)
                        ),
                        "spending-portfolio-retirement");

    }

    private void futurePortfolioSpending() {
        new Future(this.series).retirementSpendingEvolution();
    }

    private void buy(BigDecimal usd, BigDecimal eur, BigDecimal transfer, boolean detail) {
        RebalancingReport.equity(format, series, console)
                .buy(
                        new MoneyAmount(usd, USD),
                        new MoneyAmount(eur, Currency.EUR),
                        new MoneyAmount(transfer, USD),
                        detail);
    }

    private void sell(BigDecimal usd, boolean allowOversell, boolean detail, String fifo) {
        if (!"isin".equalsIgnoreCase(fifo) && !"symbol".equalsIgnoreCase(fifo)) {
            throw new IllegalArgumentException("fifo must be isin or symbol.");
        }
        RebalancingReport.equity(format, series, console)
                .sell(new MoneyAmount(usd, USD), allowOversell, detail, "isin".equalsIgnoreCase(fifo));
    }

    private void severance() {

        this.console.appendLine(this.format.subtitle("Floor"));

        Severance sev = Future.severance(BigDecimal.valueOf(67).movePointLeft(2));
        this.console.appendLine(this.format.text("Salary", 8), this.format.currency(new MoneyAmount(sev.salary(), USD), 18));

        this.printSev(sev);

        this.console.appendLine(this.format.subtitle("Full"));
        this.printSev(Future.severance(BigDecimal.ONE));
    }

    private void printSev(Severance sev) {
        this.console.appendLine(this.format.text("Taxed", 8), this.format.currency(new MoneyAmount(sev.taxedAmount(), USD), 18));
        this.console.appendLine(this.format.text("Untaxed", 8), this.format.currency(new MoneyAmount(sev.untaxedAmount(), USD), 18));
        this.console.appendLine(this.format.text("Total", 8), this.format.currency(sev.getTotal(), 18));

    }

    private record InvestmentEra(String name, YearMonth from, YearMonth to) {

        public boolean contains(Investment i) {
            var investment = YearMonth.from(i.getInitialDate());
            return investment.compareTo(from) >= 0
                    && investment.compareTo(to) <= 0;
        }
    }

    private void investmentEras(boolean nominal, boolean cash) {

        this.console.appendLine(this.format.title("Investment Eras"));

        final var cib = new CashInvestmentBuilder(()
                -> SeriesReader.readSeries("saving/ahorros-dolar-liq.json")
                        .add(SeriesReader.readSeries("saving/ahorros-dolar-banco.json"))
                        .add(SeriesReader.readSeries("saving/ahorros-peso.json").exchangeInto(USD))
                        .add(SeriesReader.readSeries("saving/ahorros-dai.json").exchangeInto(USD))
                        .add(SeriesReader.readSeries("saving/ahorros-euro.json").exchangeInto(USD))
                        .add(SeriesReader.readSeries("saving/ahorros-euro-liq.json").exchangeInto(USD))
        );

        // non overlapping
        Stream.of(
                new InvestmentEra("Convertibility",
                        YearMonth.of(1999, Month.NOVEMBER),
                        YearMonth.of(2001, Month.DECEMBER)),
                new InvestmentEra("PF Era",
                        YearMonth.of(2002, Month.JANUARY),
                        YearMonth.of(2011, Month.JULY)),
                new InvestmentEra("Cepo",
                        YearMonth.of(2011, Month.AUGUST),
                        YearMonth.of(2016, Month.MARCH)),
                new InvestmentEra("FCI",
                        YearMonth.of(2016, Month.APRIL),
                        YearMonth.of(2019, Month.JUNE)),
                new InvestmentEra("Transition",
                        YearMonth.of(2019, Month.JULY),
                        YearMonth.of(2020, Month.JULY)),
                new InvestmentEra("ETF",
                        YearMonth.of(2020, Month.AUGUST),
                        YearMonth.now())
        ).forEach(era -> this.era(era, nominal, cash, cib));

    }

    private void era(InvestmentEra era, boolean nominal, boolean withCash, CashInvestmentBuilder cib) {

        var investments = withCash
                ? Stream.concat(this.series.getInvestments().stream(),
                        cib.cashInvestments().stream())
                        .toList()
                : this.series.getInvestments();

        final var mdr = new ModifiedDietzReturn(investments.stream().filter(era::contains).toList(), nominal)
                .get()
                .getMoneyWeighted()
                .add(BigDecimal.ONE);

        final var days = ChronoUnit.DAYS.between(era.from.atDay(1), era.to.atEndOfMonth());

        final double years = days / 365.2425;
        final double cagr = Math.pow(mdr.doubleValue(), 1.0 / years) - 1.0;

        final var cagrBd = BigDecimal.valueOf(cagr);

        final Period p = Period.between(era.from.atDay(1), era.to.atEndOfMonth());

        this.console.appendLine(MessageFormat.format("{0} Over {1} years and {2} months CAGR: {3}",
                this.format.text(era.name, 15),
                p.getYears(),
                this.format.text(String.valueOf(p.getMonths()), 2),
                this.format.percent(cagrBd, 7)));

    }

    private void averageMonthsSavedInCash() {
        final var realCash = this.series.realCash();
        final var realInvested = this.series.realInvested();

        final MoneyAmountSeries avgSpending = this.series.realExpenses(null, true);

        new TimeSeriesChart(new ChartStyle(ValueFormat.NUMBER, Scale.LINEAR))
                .createFromTimeSeries(
                        "Years of Spending Covered",
                        List.of(
                                this.relativeSeries("Years Saved in Cash", avgSpending, realCash),
                                this.relativeSeries("Years Invested", avgSpending, realInvested)
                        ),
                        USD,
                        "covered-years");
    }

    private TimeSeries relativeSeries(String title, MoneyAmountSeries regularExpenses, MoneyAmountSeries realSavings) {
        return this.relativeSeries(
                title,
                regularExpenses,
                realSavings,
                12,
                YearMonthUtil.min(realSavings.getTo(), regularExpenses.getTo()));
    }

    private TimeSeries relativeSeries(String title, MoneyAmountSeries regularExpenses, MoneyAmountSeries realSavings, int months, YearMonth to) {

        final List<TimeSeriesDatapoint> s = new ArrayList<>();
        var ym = YearMonth.of(2008, 1);
        var monthsInAYear = BigDecimal.valueOf(months);

        while (ym.compareTo(to) <= 0) {
            s.add(
                    new TimeSeriesDatapoint(
                            ym,
                            realSavings
                                    .getAmountOrElseZero(ym)
                                    .amount()
                                    .divide(regularExpenses.getAmount(ym).amount(), MathConstants.C)
                                    .divide(monthsInAYear, MathConstants.C)));
            ym = ym.plusMonths(1);
        }
        return ChartSeriesMapper.asTimeSeries(s, title);

    }

    public void groupedByYear() {
        for (var year = 2011; year <= YearMonth.now().getYear(); year++) {
            this.groupedByYear(year);
        }

    }

    public void groupedByYear(int year) {

        this.console.appendLine(this.format.title("Expenses " + year));

        Map<ExpenseTier, Optional<MoneyAmountSeries>> map = this.series.summedExpensesBy(ExpenseTier::of);

        for (var e : map.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
            this.console.appendLine(
                    this.format.text(e.getKey().name(), 12),
                    this.format.currency(this.yearSum(year, e.getValue()), 15));
        }

    }

    private MoneyAmount yearSum(int year, Optional<MoneyAmountSeries> ma) {
        return ma
                .map(ss
                        -> IntStream.rangeClosed(1, 12)
                                .mapToObj(m -> YearMonth.of(year, m))
                                .map(ss::getAmountOrElseZero)
                                .reduce(MoneyAmount.zero(USD), MoneyAmount::add))
                .orElse(MoneyAmount.zero(USD));

    }

}
