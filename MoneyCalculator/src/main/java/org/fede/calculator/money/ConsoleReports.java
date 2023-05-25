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

import java.io.BufferedOutputStream;
import java.math.BigDecimal;
import static java.math.BigDecimal.ZERO;
import java.text.NumberFormat;
import java.util.List;
import java.util.Optional;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static java.text.MessageFormat.format;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import static java.util.Map.entry;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import static java.util.stream.Collectors.joining;
import java.util.stream.Stream;

import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.YearMonth;
import static org.fede.calculator.money.Inflation.USD_INFLATION;
import static org.fede.calculator.money.MathConstants.C;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
public class ConsoleReports {

    private static final String MONTHS_PARAM = "m";

    //private static final MoneyAmount ZERO_USD = MoneyAmount.zero("USD");
    private static final Pattern PARAM_SEPARATOR = Pattern.compile("=");

    private static final String TRIALS = "60000";
    private static final String RETIREMENT = "66";
    private static final String AGE = "99";
    private static final String INFLATION = "2.6";
    private static final String CASH = "0";
    private static final String TAX = "true";
    private static final String EXPECTED_RETRUNS = "all";
    private static final String BBPP = "2.25";
    private static final String PENSION = "100";

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
            return i.getCurrency().equals("XRSU") || i.getCurrency().equals("RTWO");
        }
        if ("exus".equalsIgnoreCase(type)) {
            return i.getCurrency().equals("EIMI") || i.getCurrency().equals("MEUD");
        }

        return i.getCurrency().equalsIgnoreCase(type);
    }

    private static Map<String, Runnable> getActions(String[] args, ConsoleReports me, Format format, Bar bar, Series series, Console console) {
        return Map.ofEntries(
                entry("i", new Investments(console, format, bar, series)::investments),
                entry("gi", new Positions(console, format, series, bar)::groupedInvestments),
                entry("ti", new Positions(console, format, series, bar)::listStockByType),
                entry("inv", () -> me.invReport(args, "inv")),
                entry("savings", () -> me.savings(args, "savings")),
                entry("savings-evo", () -> me.savingEvolution(args, "savings-evo")),
                entry("savings-change", () -> me.savingChange(args, "savings-change")),
                entry("savings-change-pct", () -> me.savingsPercentChange(args, "savings-change-pct")),
                entry("savings-net-change", () -> me.monthlySavings(args, "savings-net-change")),
                entry("savings-avg-pct", () -> me.netAvgSavingSpentPct(args, "savings-avg-pct")),
                entry("savings-avg", () -> me.netAvgSavingSpent(args, "savings-avg")),
                entry("savings-dist", new Savings(format, series, bar, console)::savingsDistributionEvolution),
                entry("savings-dist-pct", new Savings(format, series, bar, console)::savingsDistributionPercentEvolution),
                entry("saved-salaries-evo", () -> me.averageSavedSalaries(args, "saved-salaries-evo")),
                entry("income", () -> me.income(args)),
                entry("income-table", new Savings(format, series, bar, console)::savingsIncomeTable),
                entry("income-year-table", new Savings(format, series, bar, console)::yearSavingsIncomeTable),
                entry("income-evo", () -> me.incomeAverageEvolution(args, "income-evo")),
                entry("income-src", () -> me.incomeAverageBySource(args, "income-src")),
                entry("income-avg-change", () -> me.incomeDelta(args, "income-avg-change")),
                entry("p", () -> me.portfolio(args, "p")),
                entry("p-evo", () -> me.portfolioEvo(args, "p-evo")),
                entry("p-evo-pct", () -> me.portfolioEvo(args, "p-evo-pct")),
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
                entry("ppi", () -> me.ppi(args, "ppi")),
                entry("inv-evo-pct", () -> me.invEvoPct(args, "inv-evo-pct")),
                entry("bench", () -> me.benchmark(args, "bench"))
        );
    }

    public static void main(String[] args) {
        try {

            final var console = new Console();
            final var format = new Format();
            final var bar = new Bar(console, format);
            final var series = new Series();
            final var me = new ConsoleReports(console, format, bar, series);

            final var params = Arrays.stream(args)
                    .map(String::toLowerCase)
                    .collect(toSet());

            if (params.isEmpty() || params.contains("help")) {

                final var help = Map.ofEntries(
                        entry("goal", format("trials={0} retirement={1} age={2} inflation={3} cash={4} tax={5} bbpp={6} pension={7} exp={8} months={9}",
                                TRIALS,
                                RETIREMENT,
                                AGE,
                                INFLATION,
                                CASH,
                                TAX,
                                BBPP,
                                PENSION,
                                EXPECTED_RETRUNS,
                                36)),
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
                        entry("expenses", "by=(year|half|quarter) type=(taxes|insurance|phone|services|home|entertainment) months=12"),
                        entry("expenses-change", "type=(full|tracked*) months=12"),
                        entry("expenses-evo", "type=(full|taxes|insurance|phone|services|home|entertainment) months=12"),
                        entry("savings-evo", "type=(BO|LIQ|EQ)"),
                        entry("dca", "type=(q*|h|y|m)"),
                        entry("pos", "nominal=false")
                );
                final Map<String, Runnable> actions = getActions(args, me, format, bar, series, console);
                Stream.concat(
                        actions.keySet()
                                .stream()
                                .filter(action -> !help.keySet().contains(action))
                                .map(action -> format(" - {0}", action)),
                        help.entrySet().stream().map(e -> format(" - {0} {1}", e.getKey(), e.getValue())))
                        .sorted()
                        .forEach(me::appendLine);
            } else {

                final Map<String, Runnable> actions = getActions(args, me, format, bar, series, console);
                actions.entrySet()
                        .stream()
                        .filter(e -> params.isEmpty() || params.contains(e.getKey().toLowerCase()))
                        .map(Map.Entry::getValue)
                        .forEach(r -> {
                            r.run();
                            me.appendLine("");
                        });
            }
            console.printReport(new BufferedOutputStream(System.out));
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            ex.printStackTrace(System.err);
        }
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

    private void ppi(String[] args, String param) {
        this.console.appendLine(this.format.title("CCL PPI"));

        try {
            final var api = new PPIRestAPI();
            this.console.appendLine(
                    this.format.text("Letras Inmediato", 20),
                    this.format.currency(api.exchangeRate("S3Y3C", "S31Y3", InstrumentType.LETRAS, SettlementType.INMEDIATA), 10));
            this.console.appendLine(
                    this.format.text("GD30 Inmediato", 20),
                    this.format.currency(api.exchangeRate("GD30C", "GD30", InstrumentType.BONOS, SettlementType.INMEDIATA), 10));
            this.console.appendLine(
                    this.format.text("GD30 a 48 horas", 20),
                    this.format.currency(api.exchangeRate("GD30C", "GD30", InstrumentType.BONOS, SettlementType.A48), 10));
        } catch (Exception ex) {
            System.err.println("Exception " + ex.getClass().toString()+" "+ ex.getMessage());
            ex.printStackTrace(System.err);
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
        this.bar.evolution("Savings", this.series.realSavings(this.paramsValue(args, paramName).get("type")), 2000);
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

        final var series = "full".equalsIgnoreCase(type)
                ? this.series.realExpense()
                : this.series.realExpenses(type);

        this.bar.evolution(format("Average {0}-month expenses.", months), new SimpleAggregation(months).average(series), 18);
    }

    private void savingChange(String[] args, String paramName) {

        final var months = Integer.parseInt(this.paramsValue(args, paramName).getOrDefault(MONTHS_PARAM, "1")) + 1;

        this.appendLine(this.format.title(format("{0}-month Savings Change", months - 1)));
        this.bar.evolution(format("{0}-month Savings Change", months - 1), new SimpleAggregation(months)
                .change(this.series.realSavings(null)), 50 * months);
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
        final var retirementAge = Integer.parseInt(params.getOrDefault("retirement", RETIREMENT));
        final var age = Integer.parseInt(params.getOrDefault("age", AGE));
        final var months = Integer.parseInt(params.getOrDefault(MONTHS_PARAM, "36"));
        final var extraCash = Integer.parseInt(params.getOrDefault("cash", CASH));
        final var afterTax = Boolean.parseBoolean(params.getOrDefault("tax", TAX));
        final var expected = params.getOrDefault("exp", EXPECTED_RETRUNS);
        final var pension = Integer.parseInt(params.getOrDefault("pension", PENSION));

        final var bbppTax = afterTax
                ? Double.parseDouble(params.getOrDefault("bbpp", BBPP)) / 100.0d
                : 0.0d;

        final var goal = new Goal(this.console, this.format, this.series, this.bar, bbppTax);

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
        final var currency = params.get("type");
        final var nominal = nominal(params);
        new Investments(console, format, bar, series).invEvo(currency, nominal);
    }

    private void portfolioEvo(String[] args, String paramName) {
        this.console.appendLine(this.format.title("Portfolio Evolution"));
        final var params = this.paramsValue(args, paramName);
        final var type = params.get("type");
        new Investments(console, format, bar, series).portfolioEvo(type, "p-evo-pct".equalsIgnoreCase(paramName));
    }

}
