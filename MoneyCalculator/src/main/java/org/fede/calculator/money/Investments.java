/*
 * Copyright (C) 2021 federicogentile
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
import java.math.BigDecimal;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.text.MessageFormat.format;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import static java.util.Comparator.comparing;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.joining;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.InvestmentEvent;
import org.fede.calculator.money.series.InvestmentType;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.SeriesReader;
import org.fede.calculator.money.series.SortedMapMoneyAmountSeries;
import org.fede.calculator.money.series.YearMonth;
import org.fede.util.Pair;
import static org.fede.util.Pair.of;

/**
 *
 * @author fede
 */
public class Investments {

    private static final BigDecimal CAPITAL_GAINS_TAX_RATE = new BigDecimal("0.15");

    private static final MoneyAmount ZERO_USD = new MoneyAmount(ZERO.setScale(6, MathConstants.RM), "USD");

    private static final Comparator<Pair<String, Pair<BigDecimal, BigDecimal>>> CMP = comparing((Pair<String, Pair<BigDecimal, BigDecimal>> p) -> p.getSecond().getSecond()).reversed();

    private static final Map<String, String> ETF_NAME = Map.of(
            "CSPX", "iShares Core S&P 500",
            "IWDA", "iShares Core MSCI World"
    //"VWRA", "Vanguard FTSE All-World",
    //"ISAC", "iShares MSCI ACWI"
    );

    private static final Map<String, AnsiFormat> ETF_COLOR = Map.of(
            "CSPX", new AnsiFormat(Attribute.DIM()),
            "IWDA", new AnsiFormat(Attribute.DIM()),
            "Cash", new AnsiFormat(Attribute.DIM())
    );

    private final Console console;
    private final Format format;
    private final Bar bar;
    private final Series series;
    private final CashInvestmentBuilder cashInvestments;

    public Investments(Console console, Format format, Bar bar, Series series) {
        this.console = console;
        this.format = format;
        this.bar = bar;
        this.series = series;
        this.cashInvestments = new CashInvestmentBuilder(
                SeriesReader.readSeries("/saving/ahorros-dolar-liq.json")
                        .add(SeriesReader.readSeries("/saving/ahorros-dai.json").exchangeInto("USD"))
                        .add(SeriesReader.readSeries("/saving/ahorros-euro.json").exchangeInto("USD")));
    }

    private void investmentReport(final Predicate<Investment> everyone, boolean nominal, String currency) {
        this.console.appendLine(this.format.title(format("{0} Investment Results", nominal ? "Nominal" : "Real")));

        final var ics = new InvestmentCostStrategy(currency);

        final var mw = 13;
        final var colWidths = new int[]{5, 11, 9, mw, mw, mw, 9, 10, 1, 24};

        this.invHeader(colWidths, true);

        final var details = this.getAllInvestments()
                .stream()
                .filter(Investment::isCurrent)
                .filter(inv -> inv.getType().equals(InvestmentType.ETF))
                .filter(everyone)
                .map(ics::details)
                .map(d -> nominal ? d : d.asReal())
                .collect(toList());

        details.stream()
                .sorted(comparing(InvestmentDetails::getInvestmentDate))
                .forEach(d -> this.print(d, colWidths));

        this.invHeader(colWidths, false);

    }

    public void inv(final Predicate<Investment> everyone, boolean nominal, String currency) {

        final var etfs = this.getAllInvestments()
                .stream()
                .filter(inv -> inv.getType().equals(InvestmentType.ETF))
                .collect(toList());

        final var etfsWithFees = etfs.stream()
                .map(this::withFees)
                .collect(toList());

        final var cspxBenchmarkSeries = etfs.stream()
                .map(new BenchmarkInvestmentMapper("CSPX", etfs))
                .collect(toList());

        final var iwdaBenchmarkSeries = etfs.stream()
                .map(new BenchmarkInvestmentMapper("IWDA", etfs))
                .collect(toList());

        final var cashBenchmarkSeries = etfs.stream()
                .map(new BenchmarkInvestmentMapper("USD", etfs))
                .collect(toList());

        final Function<Pair<String, Pair<BigDecimal, BigDecimal>>, String> lineFunction
                = (p) -> format("{0} {1} {2}",
                        this.format.text(ETF_NAME.getOrDefault(p.getFirst(), p.getFirst()), 25, ETF_COLOR.getOrDefault(p.getFirst(), new AnsiFormat(Attribute.BRIGHT_WHITE_TEXT()))),
                        this.format.percent(p.getSecond().getFirst(), 8),
                        this.bar.pctBar(p.getSecond().getSecond()));

        this.investmentReport(everyone, nominal, currency);

        this.benchmarkReport(etfs, etfsWithFees, cspxBenchmarkSeries, iwdaBenchmarkSeries, cashBenchmarkSeries, lineFunction, currency, nominal);

        this.timeWeightedReport(etfs, etfsWithFees, cspxBenchmarkSeries, iwdaBenchmarkSeries, cashBenchmarkSeries, lineFunction, nominal);

        this.yearMatrix(etfs, etfsWithFees, cspxBenchmarkSeries, iwdaBenchmarkSeries, cashBenchmarkSeries, lineFunction, currency, nominal);

    }

    private void benchmarkReport(
            List<Investment> etfs,
            List<Investment> etfsWithFees,
            List<Investment> cspxBenchmarkSeries,
            List<Investment> iwdaBenchmarkSeries,
            List<Investment> cashBenchmarkSeries,
            Function<Pair<String, Pair<BigDecimal, BigDecimal>>, String> lineFunction,
            String currency,
            boolean nominal) {
        final var textColWidth = 25;
        final var portfolioMDR = Stream.of(of("Portfolio", new ModifiedDietzReturn(etfs, currency, nominal).get()));
        final var portfolioWithFeesMDR = Stream.of(of("Portfolio With Fees", new ModifiedDietzReturn(etfsWithFees, currency, nominal).get()));
        final var cspxBenchmark = Stream.of(of("CSPX", new ModifiedDietzReturn(cspxBenchmarkSeries, currency, nominal).get()));
        final var iwdaBenchmark = Stream.of(of("IWDA", new ModifiedDietzReturn(iwdaBenchmarkSeries, currency, nominal).get()));
        final var cashBenchmark = Stream.of(of("Cash", new ModifiedDietzReturn(cashBenchmarkSeries, currency, nominal).get()));

        this.console.appendLine("");
        this.console.appendLine(this.format.subtitle((nominal ? "Nominal" : "Real") + " Money Weighted MDR"));
        this.console.appendLine(this.format.text(" ", textColWidth), this.format.text(" Return", 8), this.format.text("    Annualized", 16));
        Stream.of(portfolioMDR, cspxBenchmark, iwdaBenchmark, cashBenchmark, portfolioWithFeesMDR)
                .reduce(Stream.empty(), Stream::concat)
                .sorted(CMP)
                .map(lineFunction)
                .forEach(this.console::appendLine);

    }

    private void yearMatrix(
            List<Investment> etfs,
            List<Investment> etfsWithFees,
            List<Investment> cspxBenchmarkSeries,
            List<Investment> iwdaBenchmarkSeries,
            List<Investment> cashBenchmarkSeries,
            Function<Pair<String, Pair<BigDecimal, BigDecimal>>, String> lineFunction,
            String currency,
            boolean nominal) {

        final var textColWidth = 25;

        this.console.appendLine(this.format.subtitle((nominal ? "Nominal" : "Real") + " Yearly Modified Dietz Returns"));
        this.console.appendLine(this.format.text(" ", textColWidth), this.format.text(" Return", 8), this.format.text("    Annualized", 16));

        IntStream.rangeClosed(2019, LocalDate.now().getYear())
                .mapToObj(year -> Pair.of(String.valueOf(year), new ModifiedDietzReturn(etfs, currency, nominal, LocalDate.of(year, Month.JANUARY, 1), LocalDate.of(year, Month.DECEMBER, 31)).get()))
                .map(lineFunction)
                .forEach(this.console::appendLine);

        final var thisYear = Inflation.USD_INFLATION.getTo().getYear();

        final var benchmarkMatrix = Map.of(
                "Portfolio",
                IntStream.rangeClosed(2019, thisYear)
                        .mapToObj(year -> Pair.of(String.valueOf(year), new ModifiedDietzReturn(etfs, currency, nominal, LocalDate.of(year, Month.JANUARY, 1), LocalDate.of(year, Month.DECEMBER, 31)).get()))
                        .collect(toList()),
                "Portfolio With Fees",
                IntStream.rangeClosed(2019, thisYear)
                        .mapToObj(year -> Pair.of(String.valueOf(year), new ModifiedDietzReturn(etfsWithFees, currency, nominal, LocalDate.of(year, Month.JANUARY, 1), LocalDate.of(year, Month.DECEMBER, 31)).get()))
                        .collect(toList()),
                "CSPX",
                IntStream.rangeClosed(2019, thisYear)
                        .mapToObj(year -> Pair.of(String.valueOf(year), new ModifiedDietzReturn(cspxBenchmarkSeries, currency, nominal, LocalDate.of(year, Month.JANUARY, 1), LocalDate.of(year, Month.DECEMBER, 31)).get()))
                        .collect(toList()),
                "IWDA",
                IntStream.rangeClosed(2019, thisYear)
                        .mapToObj(year -> Pair.of(String.valueOf(year), new ModifiedDietzReturn(iwdaBenchmarkSeries, currency, nominal, LocalDate.of(year, Month.JANUARY, 1), LocalDate.of(year, Month.DECEMBER, 31)).get()))
                        .collect(toList()),
                "Cash",
                IntStream.rangeClosed(2019, thisYear)
                        .mapToObj(year -> Pair.of(String.valueOf(year), new ModifiedDietzReturn(cashBenchmarkSeries, currency, nominal, LocalDate.of(year, Month.JANUARY, 1), LocalDate.of(year, Month.DECEMBER, 31)).get()))
                        .collect(toList())
        );

        final var titleRow = benchmarkMatrix.values()
                .stream()
                .findFirst()
                .get()
                .stream()
                .map(Pair::getFirst)
                .map(y -> this.format.text(y, 9)).collect(joining());

        this.console.appendLine("");
        this.console.appendLine(this.format.text("", textColWidth), titleRow);
        benchmarkMatrix.entrySet()
                .stream()
                .sorted(comparing(Map.Entry::getKey, Comparator.naturalOrder()))
                .map(e -> this.matrixRow(e.getKey(), e.getValue(), benchmarkMatrix))
                .forEach(this.console::appendLine);

    }

    private void timeWeightedReport(
            List<Investment> etfs,
            List<Investment> etfsWithFees,
            List<Investment> cspxBenchmarkSeries,
            List<Investment> iwdaBenchmarkSeries,
            List<Investment> cashBenchmarkSeries,
            Function<Pair<String, Pair<BigDecimal, BigDecimal>>, String> lineFunction,
            boolean nominal) {
        this.console.appendLine(this.format.subtitle((nominal ? "Nominal" : "Real") + " Time Weighted MDR"));

        this.console.appendLine(this.format.text(" ", 25), this.format.text(" Return", 8), this.format.text("    Annualized", 16));
        Stream.of(
                Pair.of(
                        "CSPX",
                        new ModifiedDietzReturn(cspxBenchmarkSeries, "USD", nominal).monthlyLinked()),
                Pair.of(
                        "Portfolio",
                        new ModifiedDietzReturn(etfs, "USD", nominal).monthlyLinked()),
                Pair.of(
                        "Portfolio With Fees",
                        new ModifiedDietzReturn(etfsWithFees, "USD", nominal).monthlyLinked()),
                Pair.of(
                        "IWDA",
                        new ModifiedDietzReturn(iwdaBenchmarkSeries, "USD", nominal).monthlyLinked()),
                Pair.of(
                        "Cash",
                        new ModifiedDietzReturn(cashBenchmarkSeries, "USD", nominal).monthlyLinked()))
                .sorted(CMP)
                .map(lineFunction)
                .forEach(this.console::appendLine);

    }

    private String matrixRow(
            String name,
            List<Pair<String, Pair<BigDecimal, BigDecimal>>> rowData,
            Map<String, List<Pair<String, Pair<BigDecimal, BigDecimal>>>> benchmarkMatrix) {

        final var iwdaList = benchmarkMatrix.get("IWDA");
        final var cspxList = benchmarkMatrix.get("CSPX");

        return Stream.concat(
                Stream.of(this.format.text(name, 20, ETF_COLOR.getOrDefault(name, new AnsiFormat(Attribute.BRIGHT_WHITE_TEXT())))),
                IntStream.range(0, rowData.size())
                        .mapToObj(i -> Pair.of(i, rowData.get(i).getSecond().getSecond()))
                        .map(pair
                                -> format.text(
                                format.percent(
                                        pair.getSecond(), 9),
                                9,
                                ETF_COLOR.containsKey(name)
                                ? new AnsiFormat(Attribute.BRIGHT_WHITE_TEXT())
                                : color(
                                        pair.getSecond(),
                                        iwdaList.get(pair.getFirst()).getSecond().getSecond(),
                                        cspxList.get(pair.getFirst()).getSecond().getSecond())
                        )))
                .collect(joining());

    }

    private AnsiFormat color(BigDecimal value, BigDecimal iwda, BigDecimal cspx) {

        var upper = cspx;
        var lower = iwda;
        if (iwda.compareTo(cspx) > 0) {
            upper = iwda;
            lower = cspx;
        }

        if (value.compareTo(lower) <= 0) {
            return new AnsiFormat(Attribute.RED_TEXT());
        }
        if (value.compareTo(upper) <= 0) {
            return new AnsiFormat(Attribute.YELLOW_TEXT());
        }
        return new AnsiFormat(Attribute.GREEN_TEXT());
    }

    private void print(InvestmentDetails d, int[] colWidths) {

        var i = 0;
        this.console.appendLine(
                this.format.text(d.getInvestmentCurrency(), colWidths[i++]),
                this.format.text(DateTimeFormatter.ISO_LOCAL_DATE.format(d.getInvestmentDate()), colWidths[i++]),
                this.format.currency(d.getInvestmentPrice(), colWidths[i++]),
                this.format.currency(d.getInvestedAmount().getAmount(), colWidths[i++]),
                this.format.currency(d.getCurrentAmount().getAmount(), colWidths[i++]),
                this.format.currencyPL(d.getGrossCapitalGains().getAmount(), colWidths[i++]),
                this.format.percent(d.getGrossCapitalGainsPercent(), colWidths[i++]),
                this.format.percent(d.getCAGR(), colWidths[i++]),
                this.format.text(" ", colWidths[i++]),
                this.format.text(this.bar.smallPctBar(d.getCAGR()), colWidths[i++]));
    }

    private void invHeader(int[] colWidths, boolean top) {
        var separator = IntStream.rangeClosed(0, Arrays.stream(colWidths).sum() - 10).mapToObj(n -> "=").collect(joining());
        var i = 0;
        if (!top) {
            this.console.appendLine(separator);
        }
        this.console.appendLine(
                this.format.text(" ETF", colWidths[i++]),
                this.format.text("  Date", colWidths[i++]),
                this.format.text("  Price", colWidths[i++]),
                this.format.text("   Investment", colWidths[i++]),
                this.format.text("    Current", colWidths[i++]),
                this.format.text("     Profit", colWidths[i++]),
                this.format.text("    %", colWidths[i++]),
                this.format.text("    CAGR", colWidths[i++]),
                this.format.text("", colWidths[i++]),
                this.format.text("", colWidths[i++] - 2));
        if (top) {
            this.console.appendLine(separator);
        }
    }

    private void invEvo(String title, String currency, boolean nominal, boolean pct) {

        this.console.appendLine(format(title, nominal ? "Nominal" : "Real"));

        final var m = this.investmentEvolution(currency, nominal);

        final var inv = m.get("invested");
        final var cost = m.get("cost");
        final var total = m.get("total");
        final var taxes = m.get("taxes");

        total.forEach((ym, cashMa) -> {

            final var investment = inv.getAmount(ym);
            final var costAmount = cost.getAmount(ym);
            final var totalAmount = total.getAmount(ym);
            final var capitalGains = totalAmount.subtract(investment).subtract(costAmount);
            final var taxAmount = taxes.getAmount(ym);
            final var netCapitalGains = capitalGains.subtract(taxAmount);

            final var elements = List.of(
                    Pair.of(investment, Attribute.WHITE_BACK()),
                    Pair.of(costAmount, Attribute.YELLOW_BACK()),
                    Pair.of(netCapitalGains, Attribute.GREEN_BACK()),
                    Pair.of(taxAmount, Attribute.CYAN_BACK()));

            this.console.appendLine(
                    pct
                            ? this.pctBar(ym, elements)
                            : this.bar(ym, elements, 800));
        });

        this.console.appendLine(format(title, nominal ? "Nominal" : "Real"));

        this.ref();

    }

    private String bar(YearMonth ym, List<Pair<MoneyAmount, Attribute>> elements, int scale) {
        return this.bar.currencyBar(ym, elements, scale);
    }

    private String pctBar(YearMonth ym, List<Pair<MoneyAmount, Attribute>> elements) {
        return this.bar.percentBar(ym, elements);
    }

    public void invEvo(String currency, boolean nominal) {

        this.invEvo("===< {0} Investment Evolution >===", currency, nominal, false);

    }

    public void invEvoPct(String currency, boolean nominal) {

        this.invEvo("===< {0} Investment Evolution Percent >===", currency, nominal, true);
    }

    private void ref() {
        this.console.appendLine("");
        this.console.appendLine("References:");
        this.console.appendLine(Ansi.colorize(" ", Attribute.WHITE_BACK()),
                ": investment, ",
                Ansi.colorize(" ", Attribute.YELLOW_BACK()),
                ": cost, ",
                Ansi.colorize(" ", Attribute.GREEN_BACK()),
                ": profit, ",
                Ansi.colorize(" ", Attribute.RED_BACK()),
                ": loss, ",
                Ansi.colorize(" ", Attribute.CYAN_BACK()),
                ": tax.");

    }

    private Map<String, MoneyAmountSeries> investmentEvolution(String currency, boolean nominal) {

        final var inv = this.getAllInvestments()
                .stream()
                .filter(i -> Objects.equals(i.getType(), InvestmentType.ETF))
                .filter(i -> Objects.isNull(currency) || Objects.equals(currency, i.getCurrency()))
                .sorted(comparing(Investment::getInitialDate, Comparator.naturalOrder()))
                .collect(toList());

        final var start = inv
                .stream()
                .map(Investment::getIn)
                .map(InvestmentEvent::getDate)
                .map(YearMonth::of)
                .reduce((left, right) -> left.min(right))
                .get();

        final var end = inv
                .stream()
                .map(i -> Optional.ofNullable(i.getOut()).map(InvestmentEvent::getDate).map(YearMonth::of).orElse(Inflation.USD_INFLATION.getTo()))
                .reduce((left, right) -> left.max(right))
                .get();

        final var investmentSeries = new SortedMapMoneyAmountSeries("USD");
        final var costSeries = new SortedMapMoneyAmountSeries("USD");
        final var totalValuesSeries = new SortedMapMoneyAmountSeries("USD");
        final var taxesValuesSeries = new SortedMapMoneyAmountSeries("USD");

        var ym = start;
        while (ym.compareTo(end) <= 0) {

            final var moment = ym;

            final Function<Investment, MoneyAmount> invested = i -> this.asUSD(i.getIn().getMoneyAmount(), i.getInitialDate());
            final Function<Investment, MoneyAmount> realInvested = i -> this.real(i, moment, invested);

            investmentSeries.putAmount(ym, accum(inv, ym, nominal ? invested : realInvested));

            final Function<Investment, MoneyAmount> cost = i -> this.asUSD(i.getCost(), i.getInitialDate());
            final Function<Investment, MoneyAmount> realCost = i -> this.real(i, moment, cost);

            costSeries.putAmount(ym, accum(inv, ym, nominal ? cost : realCost));

            final Function<Investment, MoneyAmount> total = i -> this.asUSD(i.getInvestment().getMoneyAmount(), moment);
            totalValuesSeries.putAmount(ym, accum(inv, ym, total));

            final Function<Investment, MoneyAmount> taxes = i -> this.tax(i, invested, total);

            taxesValuesSeries.putAmount(ym, accum(inv, ym, taxes));

            ym = ym.next();
        }

        return Map.of(
                "invested", investmentSeries,
                "cost", costSeries,
                "total", totalValuesSeries,
                "taxes", taxesValuesSeries);
    }

    private MoneyAmount real(Investment i, YearMonth moment, Function<Investment, MoneyAmount> extrator) {
        return Inflation.USD_INFLATION
                .adjust(extrator.apply(i), YearMonth.of(i.getInitialDate()), moment);
    }

    private MoneyAmount tax(Investment i, Function<Investment, MoneyAmount> invested, Function<Investment, MoneyAmount> total) {

        final var capitalGains = total.apply(i)
                .subtract(invested.apply(i));

        if (capitalGains.getAmount().signum() > 0) {
            return capitalGains.adjust(ONE, CAPITAL_GAINS_TAX_RATE);
        }

        return ZERO_USD;
    }

    private MoneyAmount asUSD(MoneyAmount ma, Date d) {
        return this.asUSD(ma, YearMonth.of(d));
    }

    private MoneyAmount asUSD(MoneyAmount ma, YearMonth ym) {
        return ForeignExchanges.getMoneyAmountForeignExchange(ma.getCurrency(), "USD").apply(ma, ym);
    }

    private MoneyAmount accum(List<Investment> investments, YearMonth yearMonth, Function<Investment, MoneyAmount> extractor) {

        return investments.stream()
                .filter(i -> YearMonth.of(i.getIn().getDate()).compareTo(yearMonth) <= 0)
                .filter(i -> i.isCurrent(yearMonth.asToDate()))
                .map(extractor)
                .reduce(ZERO_USD, MoneyAmount::add);
    }

    public void portfolioEvo(String type, boolean pct) {

        final BiFunction<Investment, YearMonth, MoneyAmount> totalFunction = (i, moment) -> this.asUSD(i.getInvestment().getMoneyAmount(), moment);

        Function<Investment, YearMonth> startFunction = i -> YearMonth.of(i.getIn().getDate());
        Function<Investment, YearMonth> endFunction = i -> Optional.ofNullable(i.getOut())
                .map(InvestmentEvent::getDate)
                .map(YearMonth::of)
                .map(YearMonth::prev)
                .orElse(Inflation.USD_INFLATION.getTo());

        Function<Investment, String> classifier = i -> i.getType().toString() + " " + i.getCurrency();

        Predicate<Investment> filterPredicate = i -> Objects.isNull(type) || i.getType().toString().equals(type);
        Comparator<Investment> comparator = comparing(Investment::getInitialDate, Comparator.naturalOrder());

        final var list = this.getAllInvestments();

        new Evolution<Investment>(this.console, this.bar)
                .evo(totalFunction, startFunction, endFunction, classifier, filterPredicate, comparator, list, pct);
    }

    private List<Investment> getAllInvestments() {
        return Stream.concat(
                this.series.getInvestments().stream(),
                this.cashInvestments.cashInvestments().stream())
                .collect(toList());
    }

    public void portfolioTypeEvo(boolean pct) {

        final BiFunction<Investment, YearMonth, MoneyAmount> totalFunction = (i, moment) -> this.asUSD(i.getInvestment().getMoneyAmount(), moment);

        Function<Investment, YearMonth> startFunction = i -> YearMonth.of(i.getIn().getDate());
        Function<Investment, YearMonth> endFunction = i -> Optional.ofNullable(i.getOut())
                .map(InvestmentEvent::getDate)
                .map(YearMonth::of)
                .map(YearMonth::prev)
                .orElse(Inflation.USD_INFLATION.getTo());

        final var categories = Map.ofEntries(
                Map.entry("CSPX", "Global Eq."),
                Map.entry("EIMI", "Global Eq."),
                Map.entry("MEUD", "Global Eq."),
                Map.entry("XRSU", "Global Eq."),
                Map.entry("CONAAFA", "Dom. Eq."),
                Map.entry("CONBALA", "Dom. Bonds"),
                Map.entry("CAPLUSA", "Dom. Bonds"),
                Map.entry("LECAP", "Dom. Bonds"),
                Map.entry("LETE", "Dom. Bonds"),
                Map.entry("AY24", "Dom. Bonds"),
                Map.entry("UVA", "Dom. Bonds"),
                Map.entry("USD", "Cash"),
                Map.entry("ARS", "Cash"));

        Function<Investment, String> classifier = i -> categories.getOrDefault(i.getCurrency(), "unknown");

        Predicate<Investment> filterPredicate = i -> true;
        Comparator<Investment> comparator = comparing(Investment::getInitialDate, Comparator.naturalOrder());

        final var list = this.getAllInvestments();

        new Evolution<Investment>(this.console, this.bar)
                .evo(totalFunction, startFunction, endFunction, classifier, filterPredicate, comparator, list, pct);

    }

    private Investment withFees(Investment i) {

        var answer = new Investment();
        answer.setInvestment(i.getInvestment());
        answer.setType(i.getType());
        answer.setComment(i.getComment());
        answer.setId(i.getId());
        answer.setInterest(i.getInterest());
        answer.setOut(i.getOut());

        final var in = new InvestmentEvent();

        in.setAmount(i.getIn().getAmount()
                .add(i.getIn().getFee(), MathConstants.C));
        in.setCurrency(i.getIn().getCurrency());
        in.setDate(i.getIn().getDate());
        in.setFee(i.getIn().getFee());
        in.setFx(i.getIn().getFx());
        in.setTransferFee(i.getIn().getTransferFee());
        answer.setIn(in);
        return answer;
    }

    public void monthly(boolean nominal) {

        final var currency = "USD";
        final var etfs = this.getAllInvestments()
                .stream()
                .filter(inv -> inv.getType().equals(InvestmentType.ETF))
                .collect(toList());

        final var etfsWithFees = etfs.stream().map(this::withFees)
                .collect(toList());

        final var cspxBenchmarkSeries = etfs.stream()
                .map(new BenchmarkInvestmentMapper("CSPX", etfs))
                .collect(toList());

        final var iwdaBenchmarkSeries = etfs.stream()
                .map(new BenchmarkInvestmentMapper("IWDA", etfs))
                .collect(toList());

        final var cashBenchmarkSeries = etfs.stream()
                .map(new BenchmarkInvestmentMapper("USD", etfs))
                .collect(toList());

        this.console.appendLine("Month;Portfolio;Portfolio With Fees;CSPX;IWDA;Cash");

        Map<YearMonth, BigDecimal[]> results = new LinkedHashMap<>();

        final var initialInvestment = new BigDecimal("10000");
        results.put(YearMonth.of(2019, 6), new BigDecimal[]{initialInvestment, initialInvestment, initialInvestment, initialInvestment, initialInvestment});

        for (var ym = YearMonth.of(2019, 6); ym.compareTo(Inflation.USD_INFLATION.getTo()) < 0; ym = ym.next()) {

            var next = ym.next();

            final var st = LocalDate.ofInstant(ym.asToDate().toInstant(), ZoneId.systemDefault()).plusDays(1);

            final var fn = LocalDate.ofInstant(next.asToDate().toInstant(), ZoneId.systemDefault());

            final var portfolio = ONE.add(new ModifiedDietzReturn(
                    etfs,
                    currency,
                    nominal,
                    st,
                    fn).get().getFirst(), MathConstants.C);

            final var portfolioWithCost = ONE.add(new ModifiedDietzReturn(
                    etfsWithFees,
                    currency,
                    nominal,
                    st,
                    fn).get().getFirst(), MathConstants.C);

            final var cspx = ONE.add(new ModifiedDietzReturn(cspxBenchmarkSeries, currency, nominal, st, fn).get().getFirst(), MathConstants.C);
            final var iwda = ONE.add(new ModifiedDietzReturn(iwdaBenchmarkSeries, currency, nominal, st, fn).get().getFirst(), MathConstants.C);
            final var cash = ONE.add(new ModifiedDietzReturn(cashBenchmarkSeries, currency, nominal, st, fn).get().getFirst(), MathConstants.C);

            final var month = YearMonth.of(Date.from(fn.atStartOfDay().toInstant(ZoneOffset.ofHours(-3))));

            final var prev = results.get(month.prev());

            results.put(month, new BigDecimal[]{
                prev[0].multiply(portfolio, MathConstants.C),
                prev[1].multiply(portfolioWithCost, MathConstants.C),
                prev[2].multiply(cspx, MathConstants.C),
                prev[3].multiply(iwda, MathConstants.C),
                prev[4].multiply(cash, MathConstants.C)});

        }

        results.forEach((month, arr)
                -> this.console.appendLine(
                        format("{0}-{1};{2};{3};{4};{5};{6}",
                                this.format.text(String.valueOf(month.getYear()), 4),
                                this.format.text((month.getMonth() < 10 ? "0" : "") + String.valueOf(month.getMonth()), 2),
                                this.format.currency(arr[0], 10),
                                this.format.currency(arr[1], 10),
                                this.format.currency(arr[2], 10),
                                this.format.currency(arr[3], 10),
                                this.format.currency(arr[4], 10))));
    }

}
