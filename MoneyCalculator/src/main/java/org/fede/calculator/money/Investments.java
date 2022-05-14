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
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import static java.util.Comparator.comparing;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toList;
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

    public void inv(final Predicate<Investment> everyone, boolean nominal, String currency) {

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
                .sorted(Comparator.comparing(InvestmentDetails::getInvestmentDate))
                .forEach(d -> this.print(d, colWidths));

        this.invHeader(colWidths, false);

        this.console.appendLine("");
        this.console.appendLine(this.format.subtitle("Modified Dietz Returns (Before Fees & Taxes)"));

        final var etfs = this.getAllInvestments()
                .stream()
                .filter(inv -> inv.getType().equals(InvestmentType.ETF))
                .collect(toList());

        final var portfolioMDR = Stream.of(of("Portfolio", new ModifiedDietzReturn(etfs, currency, nominal).get()));

        final var cspxBenchmarkSeries = etfs.stream()
                .map(new BenchmarkInvestmentMapper("CSPX", etfs))
                .collect(toList());

        final var iwdaBenchmarkSeries = etfs.stream()
                .map(new BenchmarkInvestmentMapper("IWDA", etfs))
                .collect(toList());

        final var cashBenchmarkSeries = etfs.stream()
                .map(new BenchmarkInvestmentMapper("USD", etfs))
                .collect(toList());

        final var cspxBenchmark = Stream.of(of("CSPX", new ModifiedDietzReturn(cspxBenchmarkSeries, currency, nominal).get()));
        final var iwdaBenchmark = Stream.of(of("IWDA", new ModifiedDietzReturn(iwdaBenchmarkSeries, currency, nominal).get()));
        final var cashBenchmark = Stream.of(of("Cash", new ModifiedDietzReturn(cashBenchmarkSeries, currency, nominal).get()));

        final var textColWidth = 25;
        this.console.appendLine(this.format.text(" ", textColWidth), this.format.text(" Return", 8), this.format.text("    Annualized", 16));

        final Function<Pair<String, Pair<BigDecimal, BigDecimal>>, String> lineFunction
                = (p) -> format("{0} {1} {2}",
                        this.format.text(ETF_NAME.getOrDefault(p.getFirst(), p.getFirst()), textColWidth, ETF_COLOR.getOrDefault(p.getFirst(), new AnsiFormat(Attribute.BRIGHT_WHITE_TEXT()))),
                        this.format.percent(p.getSecond().getFirst(), 8),
                        this.bar.pctBar(p.getSecond().getSecond()));

        Stream.of(portfolioMDR, cspxBenchmark, iwdaBenchmark, cashBenchmark)
                .reduce(Stream.empty(), Stream::concat)
                .sorted(CMP)
                .map(lineFunction)
                .forEach(this.console::appendLine);

        this.console.appendLine(this.format.subtitle("Modified Dietz Return"));

        IntStream.rangeClosed(2019, LocalDate.now().getYear())
                .mapToObj(year -> Pair.of(String.valueOf(year), new ModifiedDietzReturn(etfs, currency, nominal, LocalDate.of(year, Month.JANUARY, 1), LocalDate.of(year, Month.DECEMBER, 31)).get()))
                .map(lineFunction)
                .forEach(this.console::appendLine);

        final var benchmarkMatrix = Map.of(
                "Portfolio",
                IntStream.rangeClosed(2019, LocalDate.now().getYear())
                        .mapToObj(year -> Pair.of(String.valueOf(year), new ModifiedDietzReturn(etfs, currency, nominal, LocalDate.of(year, Month.JANUARY, 1), LocalDate.of(year, Month.DECEMBER, 31)).get()))
                        .collect(toList()),
                "CSPX",
                IntStream.rangeClosed(2019, LocalDate.now().getYear())
                        .mapToObj(year -> Pair.of(String.valueOf(year), new ModifiedDietzReturn(cspxBenchmarkSeries, currency, nominal, LocalDate.of(year, Month.JANUARY, 1), LocalDate.of(year, Month.DECEMBER, 31)).get()))
                        .collect(toList()),
                "IWDA",
                IntStream.rangeClosed(2019, LocalDate.now().getYear())
                        .mapToObj(year -> Pair.of(String.valueOf(year), new ModifiedDietzReturn(iwdaBenchmarkSeries, currency, nominal, LocalDate.of(year, Month.JANUARY, 1), LocalDate.of(year, Month.DECEMBER, 31)).get()))
                        .collect(toList()),
                "Cash",
                IntStream.rangeClosed(2019, LocalDate.now().getYear())
                        .mapToObj(year -> Pair.of(String.valueOf(year), new ModifiedDietzReturn(cashBenchmarkSeries, currency, nominal, LocalDate.of(year, Month.JANUARY, 1), LocalDate.of(year, Month.DECEMBER, 31)).get()))
                        .collect(toList())
        );

        final var titleRow = benchmarkMatrix.values()
                .stream()
                .findFirst()
                .get()
                .stream()
                .map(Pair::getFirst)
                .map(y -> this.format.text(y, 9)).collect(Collectors.joining());
        
        this.console.appendLine("");
        this.console.appendLine(this.format.text("", 12), titleRow);
        benchmarkMatrix.entrySet()
                .stream()
                .map(e -> this.matrixRow(e.getKey(), e.getValue()))
                .forEach(this.console::appendLine);

    }

    private String matrixRow(String name, List<Pair<String, Pair<BigDecimal, BigDecimal>>> rowData) {

        return Stream.concat(
                Stream.of(this.format.text(name, 12)),
                rowData.stream()
                        .map(rd -> format.percent(rd.getSecond().getSecond(), 9)))
                .collect(Collectors.joining());

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
        var separator = IntStream.rangeClosed(0, Arrays.stream(colWidths).sum() - 10).mapToObj(n -> "=").collect(Collectors.joining());
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
                .sorted(Comparator.comparing(Investment::getInitialDate, Comparator.naturalOrder()))
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
        Comparator<Investment> comparator = Comparator.comparing(Investment::getInitialDate, Comparator.naturalOrder());

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
        Comparator<Investment> comparator = Comparator.comparing(Investment::getInitialDate, Comparator.naturalOrder());

        final var list = this.getAllInvestments();

        new Evolution<Investment>(this.console, this.bar)
                .evo(totalFunction, startFunction, endFunction, classifier, filterPredicate, comparator, list, pct);

    }

}
