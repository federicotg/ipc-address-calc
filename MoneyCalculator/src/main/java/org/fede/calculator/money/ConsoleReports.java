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

import com.diogonunes.jcolor.Ansi;
import com.diogonunes.jcolor.AnsiFormat;
import com.diogonunes.jcolor.Attribute;
import com.fasterxml.jackson.core.type.TypeReference;
import java.math.BigDecimal;
import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.ONE;
import static org.fede.calculator.money.MathConstants.CONTEXT;
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import static java.util.Map.entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
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
import org.fede.calculator.money.series.BBPPItem;
import org.fede.calculator.money.series.BBPPTaxBraket;
import org.fede.calculator.money.series.BBPPYear;
import org.fede.calculator.money.series.InvestmentEvent;
import org.fede.calculator.money.series.SeriesReader;
import org.fede.calculator.money.series.SortedMapMoneyAmountSeries;
import static org.fede.calculator.money.ForeignExchanges.getMoneyAmountForeignExchange;
import org.fede.calculator.money.series.InvestmentAsset;
import static org.fede.calculator.money.series.SeriesReader.readSeries;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
public class ConsoleReports {

    private static final List<MoneyAmount> PORTFOLIO = List.of(
            new MoneyAmount(BigDecimal.valueOf(70l), "CSPX"),
            new MoneyAmount(BigDecimal.valueOf(10l), "MEUD"),
            new MoneyAmount(BigDecimal.valueOf(10l), "XRSU"),
            new MoneyAmount(BigDecimal.valueOf(10l), "EIMI"));

    private static final Map<String, BigDecimal> INITIAL_VALUES = Map.of(
            "XRSU", new BigDecimal("217.51"),
            "MEUD", new BigDecimal("159.19"),
            "CSPX", new BigDecimal("296.40"),
            "EIMI", new BigDecimal("28.32")
    );

    private static final Map<String, String> ETF_NAME = Map.of(
            "CSPX", "iShares Core S&P 500",
            "EIMI", "iShares Core MSCI EM IMI",
            "XRSU", "Xtrackers Russell 2000",
            "IWDA", "iShares Core MSCI World",
            "VWRA", "Vanguard FTSE All-World",
            "ISAC", "iShares MSCI ACWI",
            "MEUD", "Lyxor Core STOXX Europe 600 DR"
    );

    private static final Map<String, AnsiFormat> ETF_COLOR = Map.of(
            "CSPX", new AnsiFormat(Attribute.DIM()),
            "EIMI", new AnsiFormat(Attribute.DIM()),
            "XRSU", new AnsiFormat(Attribute.DIM()),
            "IWDA", new AnsiFormat(Attribute.RED_TEXT()),
            "VWRA", new AnsiFormat(Attribute.RED_TEXT()),
            "ISAC", new AnsiFormat(Attribute.RED_TEXT()),
            "MEUD", new AnsiFormat(Attribute.DIM())
    );

    private static final double BBPP_FX_GAP_PERCENT = 0.9d;

    private static final BigDecimal IVA = new BigDecimal("1.21");

    private static final BigDecimal TRADING_FEE = new BigDecimal("0.006");

    private static final BigDecimal CAPITAL_GAINS_TAX_RATE = new BigDecimal("0.15");

    private static final MoneyAmount ZERO_USD = new MoneyAmount(ZERO, "USD");

    private static final Pattern PARAM_SEPARATOR = Pattern.compile("=");

    private static final TypeReference<Map<String, BenchmarkItem>> BENCHMARK_TR = new TypeReference<Map<String, BenchmarkItem>>() {
    };

    private static final Collector<BigDecimal, ?, BigDecimal> REDUCER = reducing(ZERO.setScale(MathConstants.SCALE, MathConstants.ROUNDING_MODE), BigDecimal::add);
    private static final Collector<Investment, ?, BigDecimal> MAPPER = mapping(inv -> inv.getMoneyAmount().getAmount().setScale(MathConstants.SCALE, MathConstants.ROUNDING_MODE), REDUCER);

    private static final Comparator<Pair<Pair<String, String>, ?>> TYPE_CURRENCY_COMPARATOR = comparing((Pair<Pair<String, String>, ?> pair) -> pair.getFirst().getFirst())
            .thenComparing(comparing(pair -> pair.getFirst().getSecond()));

    private static final NumberFormat PERCENT_FORMAT = NumberFormat.getPercentInstance();

    private static final LocalDate ETF_START_DATE = LocalDate.of(2019, Month.JULY, 24);

    private static final AnsiFormat BOLD = new AnsiFormat(Attribute.BRIGHT_BLACK_TEXT(), Attribute.BOLD(), Attribute.BRIGHT_WHITE_BACK());

    private final Series series = new Series();
    private final Console console;
    private final Bar bar;
    private final Format format;

    public ConsoleReports(Console console, Format format, Bar bar, House house) {
        this.console = console;
        this.bar = bar;
        this.format = format;
    }

    private void appendLine(String... texts) {
        this.console.appendLine(texts);
    }

    private void investments() {

        appendLine("===< Inversiones actuales agrupadas por moneda >===");

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
                .map(i -> i.getInvestment().getMoneyAmount())
                .map(investedAmount -> getMoneyAmountForeignExchange(investedAmount.getCurrency(), reportCurrency).apply(investedAmount, limit))
                .reduce(MoneyAmount::add);
    }

    private void groupedInvestments() {
        final var reportCurrency = "USD";
        final var limit = USD_INFLATION.getTo();

        appendLine("===< Inversiones Actuales Agrupadas en ", reportCurrency, " ", String.valueOf(limit.getYear()), "/", String.valueOf(limit.getMonth()), " >===");

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

        this.series.getInvestments().stream()
                .filter(Investment::isCurrent)
                .collect(groupingBy(
                        this::assetAllocation,
                        mapping(inv -> getMoneyAmountForeignExchange(inv.getInvestment().getCurrency(), reportCurrency).apply(inv.getInvestment().getMoneyAmount(), limit)
                        .getAmount()
                        .setScale(MathConstants.SCALE, MathConstants.ROUNDING_MODE),
                                REDUCER)))
                .entrySet()
                .stream()
                .map(entry -> this.formatReport(total, new MoneyAmount(entry.getValue(), reportCurrency), entry.getKey()))
                .forEach(this::appendLine);

        total.map(t -> format("-----------------------------\n{0}{1}", this.format.text("Total", 5), this.format.currency(t, 16)))
                .ifPresent(this::appendLine);
    }

    private MoneyAmount fx(Pair<Pair<String, String>, MoneyAmount> p, String reportCurrency) {

        return getMoneyAmountForeignExchange(p.getSecond().getCurrency(), reportCurrency).apply(p.getSecond(), USD_INFLATION.getTo());
    }

    private String formatReport(Optional<MoneyAmount> total, MoneyAmount subtotal, String type) {

        return format("{0}{1}{2}",
                this.format.text(type, 5),
                this.format.currency(subtotal, 16),
                this.bar.pctBar(total.map(tot -> subtotal.getAmount().divide(tot.getAmount(), CONTEXT)).orElse(ZERO)));
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

        appendLine("");
        appendLine("\t", Ansi.colorize(format(" {0} ", title), BOLD));
        appendLine("");
    }

    private void income(String[] args, String paramName) {
        this.income(Integer.parseInt(this.paramsValue(args, paramName).getOrDefault("months", "12")));

        final var totalIncome = this.series.getIncomeSeries()
                .stream()
                .flatMap(MoneyAmountSeries::moneyAmountStream)
                .collect(reducing(MoneyAmount::add))
                .orElse(ZERO_USD)
                .getAmount();

        this.appendLine(format("Total income: {0}", this.format.currency(totalIncome)));

    }

    private void income(int months) {
        final var limit = USD_INFLATION.getTo();
        final var averageRealUSDIncome = this.series.getIncomeSeries()
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
                this.format.currency(averageRealUSDIncome.getAmount()));

        final var savingPct = new MoneyAmount(averageRealUSDIncome.getAmount().multiply(new BigDecimal("0.5"), CONTEXT), averageRealUSDIncome.getCurrency());

        this.appendLine("50% saving: ",
                averageRealUSDIncome.getCurrency(),
                " ",
                this.format.currency(savingPct.getAmount()),
                " / ",
                this.format.currency(ForeignExchanges.getMoneyAmountForeignExchange(savingPct.getCurrency(), "ARS").apply(savingPct, limit).getAmount()));

        appendLine(format("Saved salaries {0}",
                this.series.realSavings(null).getAmount(limit).getAmount()
                        .divide(averageRealUSDIncome.getAmount(), CONTEXT)));

    }

    private void invReport(String[] args, String paramName) {

        final var params = this.paramsValue(args, paramName);

        final var nominal = Boolean.parseBoolean(params.getOrDefault("nominal", "false"));

        final var type = params.getOrDefault("type", "all");
        final var currency = params.getOrDefault("currency", "USD");

        this.inv("all".equalsIgnoreCase(type) ? x -> true : x -> x.getCurrency().equalsIgnoreCase(type), nominal, currency);

    }

    public static void main(String[] args) {
        try {

            PERCENT_FORMAT.setMinimumFractionDigits(2);

            final var console = new Console();
            final var format = new Format();
            final var bar = new Bar(console, format);
            final var house = new House(console, format, bar);
            final var me = new ConsoleReports(console, format, bar, house);

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
                    entry("house-evo", () -> house.houseCostsEvolution()),
                    entry("house", () -> house.houseIrrecoverableCosts(USD_INFLATION.getTo())),
                    entry("house1", () -> house.houseIrrecoverableCosts(YearMonth.of(2011, 8))),
                    entry("house3", () -> house.houseIrrecoverableCosts(YearMonth.of(2013, 8))),
                    entry("house5", () -> house.houseIrrecoverableCosts(YearMonth.of(2015, 8))),
                    //expenses
                    entry("expenses", () -> me.expenses(args, "expenses")),
                    entry("expenses-evo", () -> me.expenseEvolution(args, "expenses-evo")),
                    entry("expenses-change", () -> me.expensesChange(args, "expenses-change")),
                    //goal
                    entry("goal", () -> me.goal(args, "goal")),
                    entry("bbpp", () -> me.bbpp(args, "bbpp")),
                    entry("income-avg-change", () -> me.incomeDelta(args, "income-avg-change")),
                    entry("ibkr", () -> me.ibkrCSV()),
                    entry("mdr", () -> me.returns(args, "mdr")),
                    entry("inv-evo", () -> me.invEvo(args, "inv-evo")),
                    entry("inv-evo-pct", () -> me.invEvoPct(args, "inv-evo-pct"))
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
                        entry("mdr", "nominal=false"),
                        entry("expenses-evo", "type=(taxes|insurance|phone|services|home|entertainment) months=12"),
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
            console.printReport(System.out);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            ex.printStackTrace(System.err);
        }
    }

    private void savings() {

        appendLine("===< Historical Real USD Savings Stats >===");

        // total savings
        final var limit = USD_INFLATION.getTo();

        final var totalSavings = this.series.realSavings(null).getAmount(limit);

        // total income
        final var totalIncome = this.series.realIncome()
                .moneyAmountStream()
                .reduce(MoneyAmount::add)
                .get();

        final var months = this.series.realIncome().getFrom().monthsUntil(limit);

        final var avgSalary = totalIncome.getAmount().divide(BigDecimal.valueOf(months), CONTEXT);

        appendLine(format("Income USD {0}\nSavings USD {1} {2}\nAverage salary {3}\nSaved salaries {4}",
                this.format.currency(totalIncome.getAmount()),
                this.format.currency(totalSavings.getAmount()),
                this.format.percent(totalSavings.getAmount().divide(totalIncome.getAmount(), CONTEXT)),
                this.format.currency(avgSalary),
                totalSavings.getAmount().divide(avgSalary, CONTEXT)));

        //ingreso promedio de N meses
        final var agg = new SimpleAggregation(YearMonth.of(2012, 1).monthsUntil(USD_INFLATION.getTo()));

        final var averagIncome = agg.average(this.series.realIncome()).getAmount(USD_INFLATION.getTo());

        // ahorro promedio de N meses
        final var averagNetSavings = agg.average(this.series.realNetSavings()).getAmount(USD_INFLATION.getTo());

        final var m = totalSavings.getAmount().divide(averagIncome.subtract(averagNetSavings).getAmount(), CONTEXT);

        final var yearAndMonth = m.divideAndRemainder(BigDecimal.valueOf(12), CONTEXT);

        appendLine(format(
                "Projected {0} years and {1} months of USD {3} income (equivalent to {2} of historical real income).",
                yearAndMonth[0],
                yearAndMonth[1].setScale(0, MathConstants.ROUNDING_MODE),
                this.format.percent(ONE.subtract(averagNetSavings.getAmount().divide(averagIncome.getAmount(), CONTEXT), CONTEXT)),
                averagIncome.subtract(averagNetSavings).getAmount()));

    }

    private void expenses(String[] args, String type) {

        final var params = this.paramsValue(args, type);

        final String exp = params.get("type");
        final int months = Integer.parseInt(params.getOrDefault("months", "12"));

        this.appendLine(format("===< Real USD expenses in the last {0} months >===", months));

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

        appendLine("===< Savings Distribution Evolution >===");

        final var cash = this.series.realSavings("LIQ");
        final var eq = this.series.realSavings("EQ");
        final var bo = this.series.realSavings("BO");

        final var nf = NumberFormat.getCurrencyInstance();

        cash.forEach((ym, cashMa) -> appendLine(
                this.bar.bar(
                        ym,
                        cashMa.getAmount(),
                        eq.getAmountOrElseZero(ym).getAmount(),
                        bo.getAmountOrElseZero(ym).getAmount(),
                        1500,
                        value -> String.format("%13s", nf.format(value)))));

        appendLine("===< Savings Distribution Evolution >===");
        appendLine("");
        appendLine("References:");

        this.reference();

    }

    private void reference() {
        appendLine(Ansi.colorize(" ", Attribute.BLUE_BACK()),
                ": cash, ",
                Ansi.colorize(" ", Attribute.RED_BACK()),
                ": equity, ",
                Ansi.colorize(" ", Attribute.YELLOW_BACK()),
                ": bonds.");
    }

    private void savingsDistributionPercentEvolution() {

        appendLine("===< Savings Distribution Percent Evolution >===");

        final var cash = this.series.realSavings("LIQ");
        final var eq = this.series.realSavings("EQ");
        final var bo = this.series.realSavings("BO");

        cash.forEach((ym, cashMa) -> appendLine(
                this.bar.percentBar(ym, cashMa, eq.getAmountOrElseZero(ym), bo.getAmountOrElseZero(ym))
        ));

        appendLine("===< Savings Distribution Percent Evolution >===");
        appendLine("");
        appendLine("References:");

        this.reference();
    }

    private void savingEvolution(String[] args, String paramName) {
        appendLine("===< Savings Evolution >===");
        this.bar.evolution("Savings", this.series.realSavings(this.paramsValue(args, paramName).get("type")), 2000);
    }

    private void expenseEvolution(String[] args, String paramName) {
        appendLine("===< Expenses Evolution >===");

        final var params = this.paramsValue(args, paramName);

        this.expenseEvolution(params.get("type"), Integer.parseInt(params.getOrDefault("months", "1")));
    }

    private void expenseEvolution(String type, int months) {

        this.bar.evolution(format("Average {0}-month expenses.", months), new SimpleAggregation(months).average(this.series.realExpenses(type)), 25);
    }

    private void savingChange(String[] args, String paramName) {

        final var months = Integer.parseInt(this.paramsValue(args, paramName).getOrDefault("months", "1")) + 1;

        appendLine(format("===< {0}-month Savings Change >===", months - 1));
        this.bar.evolution(format("{0}-month Savings Change", months - 1), new SimpleAggregation(months)
                .change(this.series.realSavings(null)), 50 * months);

    }

    private void savingsPercentChange(String[] args, String paramName) {

        final var months = Integer.parseInt(this.paramsValue(args, paramName).getOrDefault("months", "1")) + 1;

        appendLine(format("===< {0}-month Savings Change >===", months - 1));
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

        final var months = Integer.parseInt(this.paramsValue(args, name).getOrDefault("months", "12"));

        appendLine("===< Expenses Change >===");

        this.bar.evolution(format("{0}-month average expenses change", months),
                new SimpleAggregation(2)
                        .change(new SimpleAggregation(months)
                                .average(this.series.realExpenses(null))), 5);
    }

    private void incomeEvolution() {

        appendLine("===< Income evolution >===");
        this.bar.evolution("Income", this.series.realIncome(), 30);
    }

    private void incomeAverageEvolution(String[] args, String paramName) {
        var params = this.paramsValue(args, paramName);

        var months = Integer.parseInt(params.getOrDefault("months", "12"));

        appendLine(format("===< Average {0}-month income evolution >===", months));

        this.incomeAverageEvolution(months);

    }

    private void incomeAverageEvolution(int months) {

        this.bar.evolution(format("Average {0}-month income", months),
                new SimpleAggregation(months)
                        .average(this.series.realIncome()),
                30);
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

        final var goal = new Goal(this.console, this.format, bbppTax * BBPP_FX_GAP_PERCENT, bbppTax / 10.0d, 1.0d - bbppTax);

        
//                
//                ONE.setScale(MathConstants.SCALE)
//                .add(TRADING_FEE.multiply(IVA, CONTEXT), CONTEXT)
//                .add(TRADING_FEE, CONTEXT)
//                .add(TRADING_FEE, CONTEXT);

        final var todaySavings = this.series.realSavings(null).getAmount(Inflation.USD_INFLATION.getTo());

        final var invested = this.series.realSavings("EQ").getAmount(Inflation.USD_INFLATION.getTo());

        goal.goal(
                trials,
                periodYears,
                deposit,
                withdraw,
                inflation,
                retirementAge,
                BigDecimal.valueOf(extraCash),
                onlySP500,
                afterTax,
                age,
                pension,
                todaySavings,
                invested);
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

        final var ym = YearMonth.of(year, 12);

        final Map<String, Function<MoneyAmount, BigDecimal>> arsFunction = Map.of(
                "ARS", (MoneyAmount item) -> item.getAmount(),
                "LECAP", (MoneyAmount item) -> item.getAmount(),
                "EUR", (MoneyAmount item) -> item.getAmount().multiply(bbpp.getEur(), CONTEXT),
                "USD", (MoneyAmount item) -> item.getAmount().multiply(bbpp.getUsd(), CONTEXT),
                "LETE", (MoneyAmount item) -> item.getAmount().multiply(bbpp.getUsd(), CONTEXT),
                "XRSU", (MoneyAmount item) -> getMoneyAmountForeignExchange(item.getCurrency(), "USD")
                        .apply(item, ym)
                        .getAmount()
                        .multiply(bbpp.getUsd(), CONTEXT),
                "CSPX", (MoneyAmount item) -> getMoneyAmountForeignExchange(item.getCurrency(), "USD")
                        .apply(item, ym)
                        .getAmount()
                        .multiply(bbpp.getUsd(), CONTEXT),
                "EIMI", (MoneyAmount item) -> getMoneyAmountForeignExchange(item.getCurrency(), "USD")
                        .apply(item, ym)
                        .getAmount()
                        .multiply(bbpp.getUsd(), CONTEXT),
                "MEUD", (MoneyAmount item) -> getMoneyAmountForeignExchange(item.getCurrency(), "EUR")
                        .apply(item, ym)
                        .getAmount()
                        .multiply(bbpp.getEur(), CONTEXT));

        final var etfs = this.series.getInvestments()
                .stream()
                .filter(i -> i.isCurrent(date))
                .filter(i -> ETF.equals(i.getType()))
                .map(Investment::getInvestment)
                .map(i -> i.getMoneyAmount())
                .map(ma -> arsFunction.get(ma.getCurrency()).apply(ma))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final var ons = this.series.getInvestments()
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

        appendLine(format("Total amount {0}", this.format.currency(totalAmount)));

        final var taxedDomesticAmount = allArs
                .stream()
                .filter(BBPPItem::isDomestic)
                .filter(i -> !i.isExempt())
                .map(i -> i.getValue().multiply(i.getHolding(), CONTEXT))
                .reduce(ZERO, BigDecimal::add)
                .multiply(new BigDecimal("1.05"), CONTEXT);

        appendLine(format("Taxed domestic amount {0}", this.format.currency(taxedDomesticAmount)));

        final var taxedForeignAmount = allArs
                .stream()
                .filter(i -> !i.isDomestic())
                .filter(i -> !i.isExempt())
                .map(i -> i.getValue().multiply(i.getHolding(), CONTEXT))
                .reduce(ZERO, BigDecimal::add);

        appendLine(format("Taxed foreign amount {0}", this.format.currency(taxedForeignAmount)));

        final var taxedTotal = bbpp.getMinimum()
                .negate()
                .add(taxedDomesticAmount, CONTEXT)
                .add(taxedForeignAmount, CONTEXT);

        appendLine(format("Taxed total {0}", this.format.currency(taxedTotal)));

        final var taxRate = bbpp.getBrakets()
                .stream()
                .sorted(comparing(BBPPTaxBraket::getFrom))
                .filter(b -> b.getFrom().compareTo(totalAmount) <= 0)
                .reduce((left, right) -> right)
                .get()
                .getTax();

        appendLine(format("Tax rate {0}", this.format.percent(taxRate)));

        final var taxAmount = taxedTotal.multiply(taxRate, CONTEXT);

        final var usdTaxAmount = getMoneyAmountForeignExchange("ARS", "USD").apply(new MoneyAmount(taxAmount, "ARS"), ym);

        appendLine(format("Tax amount {0} / USD {1}",
                this.format.currency(taxAmount),
                this.format.currency(usdTaxAmount.getAmount())));

        appendLine(format("Monthly tax amount USD {0}", this.format.currency(usdTaxAmount.adjust(BigDecimal.valueOf(12), ONE).getAmount())));

        final var allInvested = this.series.getInvestments()
                .stream()
                .filter(i -> i.isCurrent(date))
                .map(Investment::getInvestment)
                .map(InvestmentAsset::getMoneyAmount)
                .map(ma -> getMoneyAmountForeignExchange(ma.getCurrency(), "USD").apply(ma, ym))
                .reduce(ZERO_USD, MoneyAmount::add);

        final var yearRealIncome = new ArrayList<MoneyAmount>(12);

        this.series.realIncome()
                .forEachNonZero((yearMonth, ma) -> Optional.of(ma).filter(m -> yearMonth.getYear() == year).ifPresent(yearRealIncome::add));

        appendLine(format("Effective tax rate is {0}. Tax is {1} of investments. Tax is {2} of income.",
                this.format.percent(taxAmount.divide(totalAmount, CONTEXT)),
                this.format.percent(usdTaxAmount.getAmount().divide(allInvested.getAmount(), CONTEXT)),
                this.format.percent(usdTaxAmount.getAmount().divide(yearRealIncome.stream().map(MoneyAmount::getAmount).reduce(ZERO, BigDecimal::add), CONTEXT))));

        this.subtitle("Detail");

        appendLine(format("{0}{1}{2}{3}", this.format.text("", 16), this.format.text("      Value", 16), this.format.text("    %", 10), this.format.text("      Taxed", 16)));
        allArs.stream()
                .map(i -> format("{0}{1}{2}{3}",
                this.format.text(i.getName(), 16),
                this.format.currency(i.getValue(), 16),
                this.format.percent(i.getHolding(), 10),
                this.format.currency(i.getValue().multiply(i.isExempt() ? ZERO : i.getHolding(), CONTEXT), 16)))
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

        final var savings = new SimpleAggregation(months).average(this.series.realSavings(null));
        final var income = new SimpleAggregation(months).average(this.series.realIncome());

        this.bar.evolution(
                title,
                income.map((ym, ma) -> new MoneyAmount(savings.getAmountOrElseZero(ym).getAmount().divide(ONE.max(ma.getAmount()), CONTEXT), ma.getCurrency())),
                2);
    }

    private void monthlySavings() {
        appendLine("===< Net monthly savings >===");

        this.bar.evolution("Net savings", this.series.realNetSavings(), 100);
    }

    private void yearlySavings() {

        this.group("Net yearly savings", this.series.realNetSavings(), this.series.realIncome(), ym -> String.valueOf(ym.getYear()), 12);
    }

    private void yearlyIncome() {
        this.group("Yearly income", this.series.realIncome(), null, ym -> String.valueOf(ym.getYear()), 12);
    }

    private String half(YearMonth ym) {
        return format("{0}-H{1}", String.valueOf(ym.getYear()), ((ym.getMonth() - 1) / 6) + 1);
    }

    private String quarter(YearMonth ym) {
        return format("{0}-Q{1}", String.valueOf(ym.getYear()), ((ym.getMonth() - 1) / 3) + 1);
    }

    private void halfSavings() {

        this.group("Net half savings", this.series.realNetSavings(), this.series.realIncome(), this::half, 6);
    }

    private void halfIncome() {
        this.group("Half income", this.series.realIncome(), null, this::half, 6);
    }

    private void quarterSavings() {

        this.group("Net quarter savings", this.series.realNetSavings(), this.series.realIncome(), this::quarter, 3);
    }

    private void quarterIncome() {
        this.group("Quarter income", this.series.realIncome(), null, this::quarter, 3);
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
                this.format.currency(e.getValue().getAmount().divide(BigDecimal.valueOf(Math.min(months, counts.get(e.getKey()))), CONTEXT), 11),
                Optional.ofNullable(comparisonByYear.get(e.getKey()))
                        .map(comp -> this.format.pctNumber(e.getValue().getAmount().divide(comp.getAmount(), CONTEXT).movePointRight(2)))
                        .orElse(""),
                this.bar.bar(e.getValue().getAmount().divide(BigDecimal.valueOf(Math.min(months, counts.get(e.getKey()))), CONTEXT), 50))));
    }

    private void monthlySavings(String[] args, String name) {

        final var months = Integer.parseInt(this.paramsValue(args, name).getOrDefault("months", "12"));

        final var title = format("Average {0}-month net monthly savings", months);

        appendLine("===< ", title, " >===");

        this.bar.evolution(title,
                new SimpleAggregation(months).average(this.series.realNetSavings()),
                40);
    }

    private void netAvgSavingPct(String[] args, String name) {

        final var months = Integer.parseInt(this.paramsValue(args, name).getOrDefault("months", "12"));

        final var title = format("===< Average {0}-month net monthly average savings percent >===", months);

        appendLine(title);

        final var agg = new SimpleAggregation(months);
        final var income = agg.average(this.series.realIncome());
        final var netSaving = agg.average(this.series.realNetSavings());

        netSaving.map((ym, ma) -> this.positiveOrZero(ma))
                .map((ym, ma) -> new MoneyAmount(income.getAmountOrElseZero(ym).getAmount().min(ma.getAmount()), ma.getCurrency()))
                .forEach((ym, savingMa) -> appendLine(this.bar.percentBar(ym, savingMa, income.getAmountOrElseZero(ym).subtract(savingMa))));

        appendLine(title);
        appendLine("");
        appendLine("References:");
        appendLine(Ansi.colorize(" ", Attribute.BLUE_BACK()),
                ": saved, ",
                Ansi.colorize(" ", Attribute.RED_BACK()),
                ": spent. ");

    }

    private void netAvgSavingSpentPct(String[] args, String name) {

        final var months = Integer.parseInt(this.paramsValue(args, name).getOrDefault("months", "12"));

        final var title = format("===< Average {0}-month net monthly average savings and spending percent >===", months);

        appendLine(title);

        final var agg = new SimpleAggregation(months);
        final var income = agg.average(this.series.realIncome());
        final var netSaving = agg.average(this.series.realNetSavings());
        final var spending = agg.average(this.series.realExpenses(null));

        netSaving.map((ym, ma) -> this.positiveOrZero(ma))
                .map((ym, ma) -> new MoneyAmount(income.getAmountOrElseZero(ym).getAmount().min(ma.getAmount()), ma.getCurrency()))
                .forEach((ym, savingMa) -> appendLine(
                this.bar.percentBar(ym,
                        savingMa,
                        spending.getAmountOrElseZero(ym),
                        this.positiveOrZero(
                                income.getAmountOrElseZero(ym)
                                        .subtract(savingMa)
                                        .subtract(spending.getAmountOrElseZero(ym))))));

        appendLine(title);
        appendLine("");
        appendLine("References:");

        appendLine(Ansi.colorize(" ", Attribute.BLUE_BACK()),
                ": saved, ",
                Ansi.colorize(" ", Attribute.RED_BACK()),
                ": spent, ",
                Ansi.colorize(" ", Attribute.YELLOW_BACK()),
                ": other spending.");

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
        this.appendLine(
                this.row(
                        Stream.concat(
                                Stream.of("Saving %"),
                                IntStream.of(years)
                                        .mapToObj(y -> savings.get(y).getAmount().divide(incomes.get(y).getAmount().subtract(ONE, CONTEXT), CONTEXT))
                                        .map(a -> format("{0}", this.format.percent(a))))));
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
                this.format.currency(incomes.get(y).getAmount()),
                this.format.currency(savings.get(y).getAmount()),
                this.format.currency(incomes.get(y).subtract(savings.get(y)).getAmount()),
                format("{0}", this.format.percent(
                        savings.get(y).getAmount()
                                .divide(incomes.get(y).getAmount()
                                        .subtract(ONE, CONTEXT), CONTEXT))))))
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

        return this.series.realNetSavings().filter((ym, ma) -> ym.getYear() == year)
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

    private MoneyAmount lastAmount(String seriesName, YearMonth ym) {
        return SeriesReader.readSeries("saving/".concat(seriesName).concat(".json")).getAmountOrElseZero(ym);
    }

    private void portfolioAllocation() {

        Map<String, Map<String, Optional<DayDollars>>> dayDollarsByYear = this.series.getInvestments()
                .stream()
                .flatMap(this::asDayDollarsByYear)
                .collect(groupingBy(
                        DayDollars::getYear,
                        groupingBy(DayDollars::getType, reducing(DayDollars::combine))));

        final var mdrByYear = this.mdrByYear();

        dayDollarsByYear.entrySet()
                .stream()
                .sorted(comparing(Map.Entry::getKey))
                .forEach(e -> this.allocationYear(e.getKey(), e.getValue(), mdrByYear));

    }

    private void allocationYear(String year, Map<String, Optional<DayDollars>> byType, Map<Integer, Pair<BigDecimal, BigDecimal>> mdrByYear) {
        this.appendLine("Year: ", year);

        final var total = byType.values()
                .stream()
                .flatMap(Optional::stream)
                .map(DayDollars::getAmount)
                .reduce(ZERO, BigDecimal::add);

        byType.values()
                .stream()
                .flatMap(Optional::stream)
                .sorted(comparing(DayDollars::getAmount).reversed())
                .map(d -> format("\t{0} {1}",
                String.format("%-11s", d.getType()),
                this.bar.pctBar(d.getAmount(), total)))
                .forEach(this::appendLine);

        Optional.ofNullable(mdrByYear.get(Integer.parseInt(year)))
                .map(Pair::getFirst)
                .map(this.format::percent)
                .map(pct -> format("Modified Dietz Return {0}\n", pct))
                .ifPresent(this::appendLine);

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

        final var usdInvested = getMoneyAmountForeignExchange(i.getCurrency(), "USD")
                .apply(i.getMoneyAmount(), YearMonth.of(to.getYear(), to.getMonthValue()));

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

    private void invHeader(int[] colWidths) {
        var separator = IntStream.rangeClosed(0, Arrays.stream(colWidths).sum() - 10).mapToObj(n -> "=").collect(Collectors.joining());
        var i = 0;
        this.appendLine(separator);
        this.appendLine(
                this.format.text(" ETF", colWidths[i++]),
                this.format.text("  Date", colWidths[i++]),
                this.format.text("  Price", colWidths[i++]),
                this.format.text("   Investment", colWidths[i++]),
                this.format.text("    Current", colWidths[i++]),
                this.format.text("     Profit", colWidths[i++]),
                this.format.text("    %", colWidths[i++]),
                this.format.text("  Net Profit", colWidths[i++]),
                this.format.text("    %", colWidths[i++]),
                this.format.text("", colWidths[i++]),
                this.format.text("", colWidths[i++]),
                this.format.text("CAGR", colWidths[i++] - 2),
                this.format.text("Fee", colWidths[i++] - 3),
                this.format.text("%", colWidths[i++]),
                this.format.text("Tax", colWidths[i++] - 3),
                this.format.text("%", colWidths[i++]));
        this.appendLine(separator);
    }

    private BigDecimal total(List<InvestmentDetails> details, Function<InvestmentDetails, MoneyAmount> f) {

        return details.stream()
                .map(f)
                .map(MoneyAmount::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal moneyWeightedGrossReturn(InvestmentDetails details, BigDecimal totalInvested) {
        return details.getGrossCapitalGains()
                .getAmount()
                .divide(details.getInvestedAmount().getAmount(), CONTEXT)
                .multiply(details.getInvestedAmount().getAmount().divide(totalInvested, CONTEXT), CONTEXT);
    }

    private BigDecimal moneyWeightedNetReturn(InvestmentDetails details, BigDecimal totalInvested) {
        return details.getNetCapitalGains()
                .getAmount()
                .divide(details.getInvestedAmount().getAmount(), CONTEXT)
                .multiply(details.getInvestedAmount().getAmount().divide(totalInvested, CONTEXT), CONTEXT);
    }

    private void title(String text) {
        appendLine();
        appendLine(Ansi.colorize(text, Attribute.BRIGHT_WHITE_TEXT(), Attribute.BOLD()));
        appendLine();
    }

    private void inv(final Predicate<Investment> everyone, boolean nominal, String currency) {

        this.title(format("{0} Investment Results", nominal ? "Nominal" : "Real"));

        final var ics = new InvestmentCostStrategy(currency, TRADING_FEE, IVA.subtract(ONE, CONTEXT), CAPITAL_GAINS_TAX_RATE);

        final var mw = 13;
        final var colWidths = new int[]{5, 11, 9, mw, mw, mw, 9, mw, 9, 10, 1, 24, 10, 7, 10, 7};

        this.invHeader(colWidths);

        final var details = this.series.getInvestments()
                .stream()
                .filter(Investment::isCurrent)
                .filter(inv -> inv.getType().equals(InvestmentType.ETF))
                .filter(everyone)
                .map(ics::details)
                .map(d -> nominal ? d : d.asReal())
                .collect(toList());

        details.stream()
                .forEach(d -> this.print(d, colWidths));

        this.invHeader(colWidths);

        final var totalGrossGains = this.total(details, InvestmentDetails::getGrossCapitalGains);
        final var totalNetGains = this.total(details, InvestmentDetails::getNetCapitalGains);
        final var totalCurrent = this.total(details, InvestmentDetails::getCurrentAmount);
        final var totalTax = this.total(details, InvestmentDetails::getTaxes);
        final var totalFee = this.total(details, InvestmentDetails::getFees);
        final var totalInvested = this.total(details, InvestmentDetails::getInvestedAmount);

        final var grossMoneyWeightedReturn = details.stream()
                .map(d -> this.moneyWeightedGrossReturn(d, totalInvested))
                .reduce(ZERO, BigDecimal::add);

        final var netMoneyWeightedReturn = details.stream()
                .map(d -> this.moneyWeightedNetReturn(d, totalInvested))
                .reduce(ZERO, BigDecimal::add);

        this.subtitle("Total");

        this.appendLine(
                this.format.text("   Investment", mw),
                this.format.text("    Current", mw),
                this.format.text("     Profit", mw),
                this.format.text("   %", 8),
                this.format.text("  Net Profit", mw),
                this.format.text("   %", 8),
                this.format.text("     Fee", 12),
                this.format.text("   %", 8),
                this.format.text("     Tax", 12),
                this.format.text("   %", 8));

        this.appendLine(
                this.format.currency(totalInvested, mw),
                this.format.currency(totalCurrent, mw),
                this.format.currencyPL(totalGrossGains, mw),
                this.format.percent(grossMoneyWeightedReturn, 8),
                this.format.currencyPL(totalNetGains, mw),
                this.format.percent(netMoneyWeightedReturn, 8),
                this.format.currency(totalFee, 12),
                this.format.percent(totalFee.divide(totalCurrent, CONTEXT), 8),
                this.format.currency(totalTax, 12),
                this.format.percent(totalTax.divide(totalCurrent, CONTEXT), 8));

        final var etfs = this.series.getInvestments().stream().filter(inv -> inv.getType().equals(InvestmentType.ETF)).collect(toList());

        final var modifiedDietzReturn = new ModifiedDietzReturn(etfs, currency, nominal).get();

        this.subtitle("Benchmark (Before Fees & Taxes)");

        final var benchmarks = SeriesReader.read("index/benchmarks.json", BENCHMARK_TR)
                .entrySet()
                .stream()
                .collect(toMap(
                        Map.Entry::getKey,
                        e -> nominal ? e.getValue() : real(e.getValue())));

        INITIAL_VALUES.entrySet()
                .stream()
                .map(e -> Pair.of(e.getKey(), this.benchmarkItem(nominal, e)))
                .forEach(pair -> benchmarks.put(pair.getFirst(), pair.getSecond()));

        final var benchmarksStream = benchmarks.entrySet()
                .stream()
                .map(e -> of(e.getKey(), this.benchmarkCAGR(e.getValue())));

        final var portfolioTWCAGRStream = Stream.of(of("Portfolio", modifiedDietzReturn));
        final var modelPortfolioStream = Stream.of(of("Model", this.modelPortfolioCAGR(nominal)));

        Comparator<Pair<String, Pair<BigDecimal, BigDecimal>>> cmp = comparing((Pair<String, Pair<BigDecimal, BigDecimal>> p) -> p.getSecond().getSecond()).reversed();

        final var textColWidth = 30;
        appendLine(this.format.text(" ", textColWidth), this.format.text(" Return", 8), this.format.text("    Annualized", 16));

        final Function<Pair<String, Pair<BigDecimal, BigDecimal>>, String> lineFunction
                = (p) -> format("{0} {1} {2}",
                        this.format.text(ETF_NAME.getOrDefault(p.getFirst(), p.getFirst()), textColWidth, ETF_COLOR.getOrDefault(p.getFirst(), new AnsiFormat(Attribute.BRIGHT_WHITE_TEXT()))),
                        this.format.percent(p.getSecond().getFirst(), 8),
                        this.bar.pctBar(p.getSecond().getSecond()));

        Stream.of(benchmarksStream, modelPortfolioStream, portfolioTWCAGRStream)
                .reduce(Stream.empty(), Stream::concat)
                .sorted(cmp)
                .map(lineFunction)
                .forEach(this::appendLine);

        this.subtitle("Modified Dietz Return");

        IntStream.rangeClosed(2019, LocalDate.now().getYear())
                .mapToObj(year -> Pair.of(String.valueOf(year), new ModifiedDietzReturn(etfs, currency, nominal, LocalDate.of(year, Month.JANUARY, 1), LocalDate.of(year, Month.DECEMBER, 31)).get()))
                .map(lineFunction)
                .forEach(this::appendLine);
    }

    private BenchmarkItem benchmarkItem(boolean nominal, Map.Entry<String, BigDecimal> e) {

        final var oneNominal = new MoneyAmount(ONE, e.getKey());
        final var usd = ForeignExchanges.getMoneyAmountForeignExchange(e.getKey(), "USD").apply(oneNominal, USD_INFLATION.getTo());
        final var item = new BenchmarkItem(e.getValue(), usd.getAmount());
        return nominal
                ? item
                : real(item);
    }

    private static Pair<BigDecimal, BigDecimal> cagr(BigDecimal initial, BigDecimal current, LocalDate since) {
        final var days = (double) ChronoUnit.DAYS.between(since, LocalDate.now());
        final var profit = current.divide(initial, CONTEXT).subtract(ONE, CONTEXT);
        final double x = Math.pow(
                BigDecimal.ONE.add(profit).doubleValue(),
                365.0d / days) - 1.0d;
        return Pair.of(profit, BigDecimal.valueOf(x));
    }

    private Pair<BigDecimal, BigDecimal> modelPortfolioCAGR(boolean nominal) {

        final var initial = PORTFOLIO.stream()
                .map(ma -> new MoneyAmount(ma.getAmount().multiply(INITIAL_VALUES.get(ma.getCurrency()), CONTEXT), ma.getCurrency().equals("MEUD") ? "EUR" : "USD"))
                .map(ma -> ForeignExchanges.getMoneyAmountForeignExchange(ma.getCurrency(), "USD").apply(ma, YearMonth.of(2019, 7)))
                .map(MoneyAmount::getAmount)
                .reduce(ZERO, BigDecimal::add);

        final var current = PORTFOLIO.stream()
                .map(ma -> ForeignExchanges.getMoneyAmountForeignExchange(ma.getCurrency(), "USD").apply(ma, Inflation.USD_INFLATION.getTo()))
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

    private Pair<BigDecimal, BigDecimal> benchmarkCAGR(BenchmarkItem item) {
        return cagr(item.getInitial(), item.getCurrent(), ETF_START_DATE);
    }

    private void print(InvestmentDetails d, int[] colWidths) {

        var i = 0;
        this.appendLine(
                this.format.text(d.getInvestmentCurrency(), colWidths[i++]),
                this.format.text(DateTimeFormatter.ISO_LOCAL_DATE.format(d.getInvestmentDate()), colWidths[i++]),
                this.format.currency(d.getInvestmentPrice(), colWidths[i++]),
                this.format.currency(d.getInvestedAmount().getAmount(), colWidths[i++]),
                this.format.currency(d.getCurrentAmount().getAmount(), colWidths[i++]),
                this.format.currencyPL(d.getGrossCapitalGains().getAmount(), colWidths[i++]),
                this.format.percent(d.getGrossCapitalGainsPercent(), colWidths[i++]),
                this.format.currencyPL(d.getNetCapitalGains().getAmount(), colWidths[i++]),
                this.format.percent(d.getNetCapitalGainsPercent(), colWidths[i++]),
                this.format.percent(d.getCAGR(), colWidths[i++]),
                this.format.text(" ", colWidths[i++]),
                this.format.text(this.bar.smallPctBar(d.getCAGR()), colWidths[i++]),
                this.format.currency(d.getFees().getAmount(), colWidths[i++]),
                this.format.percent(d.getFeePercent(), colWidths[i++]),
                this.format.currency(d.getTaxes().getAmount(), colWidths[i++]),
                this.format.percent(d.getTaxPercent(), colWidths[i++]));
    }

    private boolean after(Date d, int year, Month m, int day) {
        return LocalDate.ofInstant(d.toInstant(), ZoneId.systemDefault()).isAfter(LocalDate.of(year, m, day));
    }

    private void returns(String[] args, String paranName) {

        final var params = this.paramsValue(args, paranName);

        final var nominal = Boolean.parseBoolean(params.getOrDefault("nominal", "false"));

        final Predicate<Investment> since2002 = i -> after(i.getInitialDate(), 2002, Month.JANUARY, 1);

        this.modifiedDietzReturn(since2002, nominal);

    }

    private Map<Integer, Pair<BigDecimal, BigDecimal>> mdrByYear() {

        final Predicate<Investment> since2002 = i -> after(i.getInitialDate(), 2002, Month.JANUARY, 1);
        final var inv = this.series.getInvestments()
                .stream()
                .filter(since2002)
                .collect(toList());

        final var from = inv.stream()
                .map(Investment::getInitialDate)
                .map(Date::toInstant)
                .map(i -> LocalDate.ofInstant(i, ZoneId.systemDefault()))
                .reduce(ConsoleReports::min)
                .get();

        final var to = inv.stream()
                .map(i -> Optional.ofNullable(i.getOut()).map(InvestmentEvent::getDate).map(Date::toInstant).orElseGet(Instant::now))
                .map(i -> LocalDate.ofInstant(i, ZoneId.systemDefault()))
                .reduce(ConsoleReports::max)
                .get();

        return this.mdrByYear(inv, from, to, false);
    }

    private void modifiedDietzReturn(Predicate<Investment> criteria, boolean nominal) {

        final var inv = this.series.getInvestments()
                .stream()
                .filter(criteria)
                .collect(toList());

        final var modifiedDietzReturn = new ModifiedDietzReturn(
                inv,
                "USD",
                nominal)
                .get();

        final var from = inv.stream()
                .map(Investment::getInitialDate)
                .map(Date::toInstant)
                .map(i -> LocalDate.ofInstant(i, ZoneId.systemDefault()))
                .reduce(ConsoleReports::min)
                .get();

        final var to = inv.stream()
                .map(i -> Optional.ofNullable(i.getOut()).map(InvestmentEvent::getDate).map(Date::toInstant).orElseGet(Instant::now))
                .map(i -> LocalDate.ofInstant(i, ZoneId.systemDefault()))
                .reduce(ConsoleReports::max)
                .get();

        appendLine("");
        appendLine(format("From {0} to {1}: {2}. Annualized: {3}", DateTimeFormatter.ISO_LOCAL_DATE.format(from), DateTimeFormatter.ISO_LOCAL_DATE.format(to), this.format.percent(modifiedDietzReturn.getFirst()), this.format.percent(modifiedDietzReturn.getSecond())));

        final Function<Map.Entry<Integer, Pair<BigDecimal, BigDecimal>>, String> lineFunction
                = (p) -> format("{0} {1} {2}",
                        this.format.text(String.valueOf(p.getKey()), 10),
                        this.format.percent(p.getValue().getFirst(), 8),
                        this.bar.pctBar(p.getValue().getSecond()));

        this.mdrByYear(inv, from, to, nominal)
                .entrySet()
                .stream()
                .sorted(comparing(Map.Entry::getKey))
                .map(lineFunction)
                .forEach(this::appendLine);

    }

    private Map<Integer, Pair<BigDecimal, BigDecimal>> mdrByYear(List<Investment> inv, LocalDate from, LocalDate to, boolean nominal) {

        return IntStream.rangeClosed(from.getYear(), to.getYear())
                .boxed()
                .collect(Collectors.toMap(
                        year -> year,
                        year -> new ModifiedDietzReturn(inv, "USD", nominal, LocalDate.of(year, Month.JANUARY, 1), LocalDate.of(year, Month.DECEMBER, 31)).get()));
    }

    // increase in real USD -  rolling N months
    private void incomeDelta(String[] args, String paramName) {

        final var months = Integer.parseInt(this.paramsValue(args, paramName).getOrDefault("months", "12"));

        final var title = format("{0}-month real USD income change over {0}-month real income average.", months);
        this.title(title);

        final var allIncomeSeries = this.series.getIncomeSeries().stream().reduce(MoneyAmountSeries::add).get();

        final var agg = new SimpleAggregation(months);

        final var average = agg.average(allIncomeSeries);

        final var change = agg.change(average);

        average.forEachNonZero((ym, ch) -> percentEvolutionReport(ym, change.getAmount(ym).getAmount().divide(average.getAmount(ym).getAmount(), CONTEXT)));

        this.title(title);

    }

    private void ibkrCSV() {

        this.series.getInvestments().stream()
                .filter(inv -> inv.getType().equals(InvestmentType.ETF))
                .filter(inv -> inv.getComment() == null)
                .map(this::assetRow)
                .forEach(this::appendLine);

        final var eur = this.series.getInvestments().stream()
                .filter(inv -> inv.getType().equals(InvestmentType.ETF))
                .filter(inv -> inv.getComment() == null)
                .filter(inv -> "MEUD".equals(inv.getInvestment().getCurrency()))
                .map(inv -> inv.getIn().getAmount().add(inv.getIn().getFee(), CONTEXT))
                .reduce(ZERO, BigDecimal::add);

        final var usd = this.series.getInvestments().stream()
                .filter(inv -> inv.getType().equals(InvestmentType.ETF))
                .filter(inv -> inv.getComment() == null)
                .filter(inv -> !"MEUD".equals(inv.getInvestment().getCurrency()))
                .map(inv -> inv.getIn().getAmount().add(inv.getIn().getFee(), CONTEXT))
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

        final var cusips = Map.of(
                "EIMI", "BKM4GZ6",
                "XRSU", "BWBXSH4",
                "CSPX", "B50YWZ5",
                "MEUD", "LU0908500753");

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

        return List.of(
                "A",
                codes.get(inv.getInvestment().getCurrency()),
                isins.get(inv.getInvestment().getCurrency()),
                currencies.getOrDefault(inv.getInvestment().getCurrency(), "USD"),
                mdy.format(inv.getIn().getDate().toInstant()),
                "BUY",
                "Investment",
                "ETF",
                numberFormat.format(inv.getInvestment().getAmount()),
                numberFormat.format(
                        currencyConverter.get(inv.getInvestment().getCurrency())
                                .apply(new MoneyAmount(inv.getIn().getAmount().divide(inv.getInvestment().getAmount(), CONTEXT), inv.getInvestment().getCurrency()), YearMonth.of(inv.getIn().getDate()))),
                numberFormat.format(
                        currencyConverter.get(inv.getInvestment().getCurrency())
                                .apply(inv.getIn().getFeeMoneyAmount(), YearMonth.of(inv.getIn().getDate()))))
                .stream()
                .collect(Collectors.joining(","));
    }

    private void invEvoPct(String[] args, String paramName) {
        final var params = this.paramsValue(args, paramName);

        final var currency = params.get("currency");

        appendLine("===< Investment Evolution Percent >===");

        final var m = this.investmentEvolution(currency);

        final var inv = m.get("invested");
        final var fee = m.get("fees");
        final var total = m.get("total");

        total.forEach((ym, cashMa) -> appendLine(
                this.bar.percentBar(
                        ym,
                        List.of(Pair.of(inv.getAmount(ym), Attribute.WHITE_BACK()),
                                Pair.of(fee.getAmount(ym), Attribute.YELLOW_BACK()),
                                Pair.of(total.getAmount(ym)
                                        .subtract(inv.getAmount(ym).add(fee.getAmount(ym))), Attribute.GREEN_BACK())))));

        appendLine("===< Investment Evolution Percent >===");
        appendLine("");
        appendLine("References:");

        appendLine(Ansi.colorize(" ", Attribute.WHITE_BACK()),
                ": investment, ",
                Ansi.colorize(" ", Attribute.YELLOW_BACK()),
                ": fees, ",
                Ansi.colorize(" ", Attribute.GREEN_BACK()),
                ": profits, ",
                Ansi.colorize(" ", Attribute.RED_BACK()),
                ": losses.");

    }

    private void invEvo(String[] args, String paramName) {
        final var params = this.paramsValue(args, paramName);

        final var currency = params.get("currency");

        appendLine("===< Investment Evolution >===");

        final var m = this.investmentEvolution(currency);

        final var inv = m.get("invested");
        final var fee = m.get("fees");
        final var total = m.get("total");

        total.forEach((ym, cashMa) -> {

            final var investment = inv.getAmount(ym);
            final var feeAmount = fee.getAmount(ym);
            final var totalAmount = total.getAmount(ym);
            final var capitalGains = totalAmount.subtract(investment).subtract(feeAmount);
            final var netCapitalGains = capitalGains.getAmount().signum() > 0
                    ? capitalGains.adjust(ONE, ONE.subtract(CAPITAL_GAINS_TAX_RATE, CONTEXT))
                    : capitalGains;
            final var taxAmount = capitalGains.getAmount().signum() > 0
                    ? capitalGains.adjust(ONE, CAPITAL_GAINS_TAX_RATE)
                    : ZERO_USD;

            appendLine(
                    this.bar.currencyBar(
                            ym,
                            List.of(Pair.of(investment, Attribute.WHITE_BACK()),
                                    Pair.of(feeAmount, Attribute.YELLOW_BACK()),
                                    Pair.of(netCapitalGains, Attribute.GREEN_BACK()),
                                    Pair.of(taxAmount, Attribute.CYAN_BACK())),
                            800));
        });

        appendLine("===< Investment Evolution >===");
        appendLine("");
        appendLine("References:");

        appendLine(Ansi.colorize(" ", Attribute.WHITE_BACK()),
                ": investment, ",
                Ansi.colorize(" ", Attribute.YELLOW_BACK()),
                ": fees, ",
                Ansi.colorize(" ", Attribute.GREEN_BACK()),
                ": profits, ",
                Ansi.colorize(" ", Attribute.RED_BACK()),
                ": losses, ",
                Ansi.colorize(" ", Attribute.CYAN_BACK()),
                ": taxes.");
        
    }

    private Map<String, MoneyAmountSeries> investmentEvolution(String currency) {

        final var inv = this.series.getInvestments().stream()
                .filter(i -> i.getType().equals(InvestmentType.ETF))
                .filter(i -> Objects.isNull(currency) || Objects.equals(currency, i.getCurrency()))
                .sorted(Comparator.comparing(Investment::getInitialDate, Comparator.naturalOrder()))
                .collect(toList());

        final var start = inv
                .stream()
                .map(Investment::getIn)
                .map(InvestmentEvent::getDate)
                .map(YearMonth::of)
                .reduce((left, right) -> left.min(right))
                .get();

        final var investmentSeries = new SortedMapMoneyAmountSeries("USD");
        final var feeSeries = new SortedMapMoneyAmountSeries("USD");
        final var totalValuesSeries = new SortedMapMoneyAmountSeries("USD");

        var ym = start;
        while (ym.compareTo(Inflation.USD_INFLATION.getTo()) <= 0) {

            final var moment = ym;

            investmentSeries.putAmount(ym, accum(inv, ym, i -> i.getIn().getMoneyAmount()));

            feeSeries.putAmount(ym, accum(inv, ym, i -> i.getIn().getFeeMoneyAmount()));
            totalValuesSeries.putAmount(ym, accum(inv, ym, i -> asUSD(i.getInvestment().getMoneyAmount(), moment)));

            ym = ym.next();
        }

        return Map.of(
                "invested", investmentSeries,
                "fees", feeSeries,
                "total", totalValuesSeries);
    }

    private MoneyAmount asUSD(MoneyAmount ma, YearMonth ym) {
        return ForeignExchanges.getMoneyAmountForeignExchange(ma.getCurrency(), "USD").apply(ma, ym);
    }

    private MoneyAmount accum(List<Investment> investments, YearMonth yearMonth, Function<Investment, MoneyAmount> extractor) {

        return investments.stream()
                .filter(i -> YearMonth.of(i.getIn().getDate()).compareTo(yearMonth) <= 0)
                .map(i -> Pair.of(extractor.apply(i), YearMonth.of(i.getInitialDate())))
                .map(p -> this.asUSD(p.getFirst(), p.getSecond()))
                .reduce(ZERO_USD, MoneyAmount::add);
    }
    
}
