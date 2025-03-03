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
import java.io.IOException;
import java.math.BigDecimal;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.text.MessageFormat.format;
import java.text.NumberFormat;
import java.time.LocalDate;
import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.reverseOrder;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static org.fede.calculator.money.MathConstants.C;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import static org.fede.calculator.money.Currency.*;
import org.fede.calculator.money.chart.PieChart;
import org.fede.calculator.money.chart.PieItem;
import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.InvestmentEvent;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.SeriesReader;
import org.fede.calculator.money.series.SortedMapMoneyAmountSeries;
import org.fede.calculator.money.series.YearMonth;
import org.fede.util.Pair;
import org.jfree.data.time.TimeTableXYDataset;

/**
 *
 * @author fede
 */
public class Investments {

    private final ZoneId SYSTEM_DEFAULT_ZONE_ID = ZoneId.systemDefault();

    private final Comparator<Investment> COMPARATOR = comparing(Investment::getInitialDate, naturalOrder());

    public static final Map<Currency, String> ETF_NAME = Map.of(
            CSPX, "iShares Core S&P 500",
            EIMI, "iShares Core MSCI EM IMI",
            XRSU, "Xtrackers Russell 2000",
            RTWO, "Russell 2000 Q. Factor",
            MEUD, "Amundi Stoxx Europe 600",
            IWDA, "iShares Core MSCI World"
    );

    private final AnsiFormat DIM = new AnsiFormat(Attribute.DIM());
    private final Map<String, AnsiFormat> ETF_COLOR = Map.of(
            "CSPX", DIM,
            "EIMI", DIM,
            "RTWO", DIM,
            "XRSU", DIM,
            "MEUD", DIM,
            "IWDA", DIM,
            "Cash", DIM
    );

    private final Map<Currency, String> PF_CATEGORIES = Map.ofEntries(
            Map.entry(USD, "USD Bank"),
            Map.entry(UVA, "UVA Bank"),
            Map.entry(ARS, "ARS Bank"));

    private final Map<Currency, String> CATEGORIES = Map.ofEntries(
            Map.entry(CSPX, "Global Eq."),
            Map.entry(EIMI, "Global Eq."),
            Map.entry(MEUD, "Global Eq."),
            Map.entry(XRSU, "Global Eq."),
            Map.entry(RTWO, "Global Eq."),
            Map.entry(CONAAFA, "Dom. Eq."),
            Map.entry(CONBALA, "Dom. Bonds"),
            Map.entry(CAPLUSA, "Dom. Bonds"),
            Map.entry(LECAP, "Dom. Bonds"),
            Map.entry(LETE, "Dom. Bonds"),
            Map.entry(AY24, "Dom. Bonds"),
            Map.entry(ARS, "ARS Cash"));

    private final AnsiFormat BRIGHT_WHITE_TEXT = new AnsiFormat(Attribute.BRIGHT_WHITE_TEXT());
    private final AnsiFormat GREEN_TEXT = new AnsiFormat(Attribute.GREEN_TEXT());
    private final AnsiFormat YELLOW_TEXT = new AnsiFormat(Attribute.YELLOW_TEXT());
    private final AnsiFormat RED_TEXT = new AnsiFormat(Attribute.RED_TEXT());

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
        this.cashInvestments = new CashInvestmentBuilder(()
                -> SeriesReader.readSeries("/saving/ahorros-dolar-liq.json")
                        .add(SeriesReader.readSeries("/saving/ahorros-dolar-banco.json"))
                        .add(SeriesReader.readSeries("/saving/ahorros-dai.json").exchangeInto(Currency.USD))
                        .add(SeriesReader.readSeries("/saving/ahorros-euro.json").exchangeInto(Currency.USD)));
    }

    public void cashInv(boolean nominal) {
        this.investmentReport(
                this.cashInvestments.cashInvestments().stream(),
                i -> true,
                i -> true,
                Investment::isCurrent, nominal);
    }

    private void investmentReport(final Predicate<Investment> everyone, boolean nominal) {
        this.investmentReport(this.getInvestments(), everyone, Investment::isETF, Investment::isCurrent, nominal);
    }

    public void invGainsChart() throws IOException {
        this.invGainsChart(new PieChart(true));
    }

    public void invGainsChart(PieChart chart) throws IOException {

        var now = new Date();
        Map<Integer, BigDecimal> gainsByYear = this.series.getInvestments()
                .stream()
                .filter(investment -> investment.isCurrent(now))
                .filter(Investment::isETF)
                .map(new InvestmentCostStrategy(Currency.USD)::details)
                .map(InvestmentDetails::asReal)
                .collect(
                        Collectors.groupingBy(detail -> detail.getInvestmentDate().getYear(),
                                Collectors.reducing(
                                        BigDecimal.ZERO,
                                        detail -> detail.getGrossCapitalGains().amount(),
                                        BigDecimal::add)));

        chart.create(
                "Gains by Year",
                gainsByYear.entrySet().stream().map(e -> new PieItem(String.valueOf(e.getKey()), e.getValue())).toList(),
                "gains-by-year.png");
    }

    private void investmentReport(
            Stream<Investment> invStream,
            final Predicate<Investment> everyone,
            final Predicate<Investment> etf,
            final Predicate<Investment> current,
            boolean nominal) {
        this.console.appendLine(this.format.title(format("{0} Investment Results", nominal ? "Nominal" : "Real")));

        final var ics = new InvestmentCostStrategy(Currency.USD);

        final var mw = 13;
        final var colWidths = new int[]{5, 11, 9, mw, mw, mw, 9, 10, 1, 24};

        this.invHeader(colWidths, true);

        final var details = invStream
                .filter(current)
                .filter(etf)
                .filter(everyone)
                .map(ics::details)
                .map(d -> nominal ? d : d.asReal())
                .toList();

        details.stream()
                .sorted(comparing(InvestmentDetails::getInvestmentDate))
                .forEach(d -> this.print(d, colWidths));

        this.invHeader(colWidths, false);

    }

    private List<Investment> benchmark(List<Investment> etfs, Currency benchmark) {

        final var allEtfs = this.getInvestments()
                .filter(Investment::isETF)
                .toList();

        final var mapper = new BenchmarkInvestmentMapper(benchmark, allEtfs);

        return etfs.stream()
                .map(mapper)
                .toList();
    }

    public void inv(final Predicate<Investment> everyone, boolean nominal) {

        final var etfs = this.getInvestments()
                .filter(Investment::isETF)
                .filter(everyone)
                .toList();

        final var cspxBenchmarkSeries = benchmark(etfs, CSPX);
        final var iwdaBenchmarkSeries = benchmark(etfs, IWDA);
        final var cashBenchmarkSeries = benchmark(etfs, USD);

        this.investmentReport(everyone, nominal);
        this.yearMatrix(etfs, cspxBenchmarkSeries, iwdaBenchmarkSeries, cashBenchmarkSeries, nominal);

        this.console.appendLine(this.format.subtitle(nominal ? "Nominal Returns" : "Real Returns"));

        this.console.appendLine(
                this.format.text("", 28),
                this.range()
                        .mapToObj(y -> y == 0 ? "Total" : this.format.text(String.valueOf(y), 9))
                        .collect(joining()));

        Stream.of(CSPX, EIMI, XRSU, RTWO, MEUD, IWDA)
                .map(symbol -> Pair.of(symbol, this.annualRealReturn(symbol, 0, nominal)))
                .sorted(Comparator.comparing(Pair::second, Comparator.reverseOrder()))
                .map(p -> this.row(p.first(), nominal))
                .forEach(this.console::appendLine);

    }

    private String row(Currency symbol, boolean nominal) {
        return format("{0}{1}",
                this.format.text(ETF_NAME.get(symbol), 25),
                this.range()
                        .mapToObj(y -> this.format.percent(this.annualRealReturn(symbol, y, nominal), 9))
                        .collect(joining())
        );
    }

    private BigDecimal annualRealReturn(Currency symbol, int year, boolean nominal) {

        final var from = Inflation.USD_INFLATION.getFrom();
        final var to = Inflation.USD_INFLATION.getTo();

        if (year != 0 && (from.getYear() > year || to.getYear() < year)) {
            return BigDecimal.ZERO;
        }

        final var amount = new MoneyAmount(ONE, symbol);
        final var startYm = YearMonth.of(Math.max(2019, year) - 1, 12).max(from);
        final var endYm = year == 0
                ? to
                : YearMonth.of(year, 12).min(to);

        final var fx = ForeignExchanges.getForeignExchange(symbol, USD);
        var startValue = fx.exchange(amount, Currency.USD, startYm);
        var endValue = fx.exchange(amount, Currency.USD, endYm);
        if (!nominal) {
            startValue = Inflation.USD_INFLATION.adjust(startValue, startYm, Inflation.USD_INFLATION.getTo());
            endValue = Inflation.USD_INFLATION.adjust(endValue, endYm, Inflation.USD_INFLATION.getTo());
        }

        return endValue.getAmount().divide(startValue.getAmount(), MathConstants.C).subtract(ONE);

    }

    private LabelAndMDR item(List<Investment> series, boolean nominal, int year) {
        if (year == 0) {
            return new LabelAndMDR(
                    "Total",
                    new ModifiedDietzReturn(series, nominal).get());
        }

        return new LabelAndMDR(
                String.valueOf(year),
                new ModifiedDietzReturn(series, nominal, LocalDate.of(year, JANUARY, 1), LocalDate.of(year, DECEMBER, 31)).get());
    }

    private IntStream range() {
        return IntStream.concat(IntStream.rangeClosed(2019, Inflation.USD_INFLATION.getTo().getYear()), IntStream.of(0));
    }

    private void matrix(Map<String, List<LabelAndMDR>> matrix, boolean nominal) {

        final var titleRow = matrix.values()
                .stream()
                .findFirst()
                .get()
                .stream()
                .map(LabelAndMDR::label)
                .map(y -> this.format.text(y, 9))
                .collect(joining());

        final var nameColWidth = ETF_NAME.values()
                .stream()
                .mapToInt(String::length)
                .max()
                .orElse(12);

        this.console.appendLine(this.format.subtitle((nominal ? "Nominal" : "Real") + " Modified Dietz Returns"));

        this.console.appendLine(this.format.text(" ", nameColWidth + 4), titleRow);
        matrix
                .entrySet()
                .stream()
                .sorted(comparing(m -> m.getValue().stream().skip(m.getValue().size() - 1).findFirst().get().mdr(), reverseOrder()))
                .map(e -> this.matrixRow(e.getKey(), e.getValue().stream().map(LabelAndMDR::mdr).toList(), matrix))
                .forEach(this.console::appendLine);

    }

    private void yearMatrix(
            List<Investment> etfs,
            List<Investment> cspxBenchmarkSeries,
            List<Investment> iwdaBenchmarkSeries,
            List<Investment> cashBenchmarkSeries,
            boolean nominal) {

        final Map<String, List<LabelAndMDR>> benchmarkMatrix = Map.of(
                "Portfolio",
                this.range()
                        .mapToObj(year -> item(etfs, nominal, year))
                        .toList(),
                "CSPX",
                this.range()
                        .mapToObj(year -> item(cspxBenchmarkSeries, nominal, year))
                        .toList(),
                "IWDA",
                this.range()
                        .mapToObj(year -> item(iwdaBenchmarkSeries, nominal, year))
                        .toList(),
                "Cash",
                this.range()
                        .mapToObj(year -> item(cashBenchmarkSeries, nominal, year))
                        .toList()
        );
        this.matrix(benchmarkMatrix, nominal);

    }

    private String matrixRow(
            String name,
            List<ModifiedDietzReturnResult> rowData,
            Map<String, List<LabelAndMDR>> benchmarkMatrix) {

        final List<ModifiedDietzReturnResult> iwdaList = Optional.ofNullable(benchmarkMatrix.get("IWDA"))
                .orElseGet(Collections::emptyList)
                .stream()
                .map(LabelAndMDR::mdr)
                .toList();

        final List<ModifiedDietzReturnResult> cspxList = Optional.ofNullable(benchmarkMatrix.get("CSPX"))
                .orElseGet(Collections::emptyList)
                .stream()
                .map(LabelAndMDR::mdr)
                .toList();

        final var useBenchmarks = !iwdaList.isEmpty() && !cspxList.isEmpty();

        Map<String, String> currencies = ETF_NAME.keySet()
                .stream()
                .collect(Collectors.toMap(Currency::name, ETF_NAME::get));

        final var nameColWidth = currencies.values().stream().mapToInt(String::length).max().orElse(12);

        return Stream.concat(
                Stream.of(this.format.text(currencies.getOrDefault(name, name), nameColWidth, ETF_COLOR.getOrDefault(name, BRIGHT_WHITE_TEXT))),
                IntStream.range(0, rowData.size())
                        .mapToObj(i -> new LabelAndMDR(i, rowData.get(i)))
                        .map(pair -> coloredPercent(pair.mdr(), color(name, pair.mdr(), useBenchmarks ? iwdaList.get(Integer.parseInt(pair.label())) : null, useBenchmarks ? cspxList.get(Integer.parseInt(pair.label())) : null))))
                .collect(joining());
    }

    private String coloredPercent(ModifiedDietzReturnResult value, AnsiFormat color) {
        return this.format.text(format.percent(value.getMoneyWeighted(), 9), 9, color);
    }

    private AnsiFormat color(String name, ModifiedDietzReturnResult value, ModifiedDietzReturnResult iwda, ModifiedDietzReturnResult cspx) {

        if (iwda == null || cspx == null) {
            return ETF_COLOR.containsKey(name)
                    ? BRIGHT_WHITE_TEXT
                    : color(value.getMoneyWeighted(), ZERO, new BigDecimal("0.001"));

        }
        return ETF_COLOR.containsKey(name)
                ? BRIGHT_WHITE_TEXT
                : color(value.getMoneyWeighted(), iwda.getMoneyWeighted(), cspx.getMoneyWeighted());
    }

    private AnsiFormat color(BigDecimal value, BigDecimal iwda, BigDecimal cspx) {

        final var upper = cspx.max(iwda);
        final var lower = iwda.min(cspx);

        if (value.compareTo(lower) < 0) {
            return RED_TEXT;
        }
        if (value.compareTo(upper) < 0) {
            return YELLOW_TEXT;
        }
        return GREEN_TEXT;
    }

    private void print(InvestmentDetails d, int[] colWidths) {

        var i = 0;
        this.console.appendLine(
                this.format.text(d.getInvestmentCurrency().name(), colWidths[i++]),
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

    private void invEvo(String title, Currency currency, boolean nominal, boolean pct) {

        this.console.appendLine(this.format.title(format(title, nominal ? "Nominal" : "Real")));

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
                    new AmountAndColor(investment, Attribute.WHITE_BACK()),
                    new AmountAndColor(costAmount, Attribute.YELLOW_BACK()),
                    new AmountAndColor(netCapitalGains, Attribute.GREEN_BACK()),
                    new AmountAndColor(taxAmount, Attribute.CYAN_BACK()));

            this.console.appendLine(
                    pct
                            ? this.pctBar(ym, elements)
                            : this.bar(ym, elements, ConsoleReports.SCALE));
        });

        this.ref();

    }

    private String bar(YearMonth ym, List<AmountAndColor> elements, int scale) {
        return this.bar.genericBar(ym, elements, scale);
    }

    private String pctBar(YearMonth ym, List<AmountAndColor> elements) {
        return this.bar.percentBar(ym, elements);
    }

    public void invEvo(Currency currency, boolean nominal) {
        this.invEvo("===< {0} Investment Evolution >===", currency, nominal, false);
    }

    public void invEvoPct(Currency currency, boolean nominal) {

        this.invEvo("===< {0} Investment Evolution Percent >===", currency, nominal, true);
    }

    private void ref() {
        this.console.appendLine("");
        this.console.appendLine("References:");
        this.console.appendLine(Ansi.colorize(" ", Attribute.WHITE_BACK()),
                "investment ",
                Ansi.colorize(" ", Attribute.YELLOW_BACK()),
                "cost ",
                Ansi.colorize(" ", Attribute.GREEN_BACK()),
                "profit ",
                Ansi.colorize(" ", Attribute.RED_BACK()),
                "loss ",
                Ansi.colorize(" ", Attribute.CYAN_BACK()),
                "tax");
    }

    private Map<String, MoneyAmountSeries> investmentEvolution(Currency currency, boolean nominal) {

        final var inv = this.getInvestments()
                .filter(Investment::isETF)
                .filter(i -> Objects.isNull(currency) || Objects.equals(currency, i.getCurrency()))
                .sorted(comparing(Investment::getInitialDate, Comparator.naturalOrder()))
                .toList();

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

        final var investmentSeries = new SortedMapMoneyAmountSeries(Currency.USD, "investment");
        final var costSeries = new SortedMapMoneyAmountSeries(Currency.USD, "costs");
        final var totalValuesSeries = new SortedMapMoneyAmountSeries(Currency.USD, "total");
        final var taxesValuesSeries = new SortedMapMoneyAmountSeries(Currency.USD, "taxes");

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
            return capitalGains.adjust(ONE, new BigDecimal("0.15"));
        }

        return MoneyAmount.zero(Currency.USD);
    }

    private MoneyAmount asUSD(MoneyAmount ma, Date d) {
        return this.asUSD(ma, YearMonth.of(d));
    }

    private MoneyAmount asUSD(MoneyAmount ma, YearMonth ym) {
        return ForeignExchanges.getMoneyAmountForeignExchange(ma.getCurrency(), Currency.USD).apply(ma, ym);
    }

    private MoneyAmount accum(List<Investment> investments, YearMonth yearMonth, Function<Investment, MoneyAmount> extractor) {

        return investments.stream()
                .filter(i -> YearMonth.of(i.getIn().getDate()).compareTo(yearMonth) <= 0)
                .filter(i -> i.isCurrent(yearMonth.asToDate()))
                .map(extractor)
                .reduce(MoneyAmount.zero(Currency.USD), MoneyAmount::add);
    }

    public void portfolioEvo(String type, boolean pct) {

        final BiFunction<Investment, YearMonth, MoneyAmount> totalFunction = (i, moment) -> this.asUSD(i.getInvestment().getMoneyAmount(), moment);

        Function<Investment, YearMonth> startFunction = i -> YearMonth.of(i.getIn().getDate());
        Function<Investment, YearMonth> endFunction = i -> Optional.ofNullable(i.getOut())
                .map(InvestmentEvent::getDate)
                .map(YearMonth::of)
                .map(YearMonth::prev)
                .orElseGet(Inflation.USD_INFLATION::getTo);

        Function<Investment, String> classifier = i -> i.getType().toString().concat(" ").concat(i.getCurrency().name());

        Predicate<Investment> filterPredicate = i -> Objects.isNull(type) || i.getType().toString().equals(type);

        final var list = this.getAllInvestments();

        new Evolution<Investment>(this.console, this.bar)
                .evo(totalFunction, startFunction, endFunction, classifier, filterPredicate, COMPARATOR, list, pct);
    }
    
    
//    public void portfolioEvoChart(){
//        
//        TimeTableXYDataset ds = new TimeTableXYDataset();
//        ds.
//        
//    }

    private Stream<Investment> getInvestments() {

        return this.series.getInvestments().stream();
    }

    private List<Investment> getAllInvestments() {
        return Stream.concat(
                this.getInvestments(),
                this.cashInvestments.cashInvestments().stream())
                .toList();
    }

    public void portfolioTypeEvo(boolean pct) {

        final BiFunction<Investment, YearMonth, MoneyAmount> totalFunction = (i, moment) -> this.asUSD(i.getInvestment().getMoneyAmount(), moment);

        Function<Investment, YearMonth> startFunction = i -> YearMonth.of(i.getIn().getDate());
        Function<Investment, YearMonth> endFunction = i -> Optional.ofNullable(i.getOut())
                .map(InvestmentEvent::getDate)
                .map(YearMonth::of)
                .map(YearMonth::prev)
                .orElseGet(Inflation.USD_INFLATION::getTo);

        new Evolution<Investment>(this.console, this.bar)
                .evo(totalFunction, startFunction, endFunction, this::classifier, i -> true, COMPARATOR, this.getAllInvestments(), pct);
    }

    private String classifier(Investment i) {

        return switch (i.getType()) {
            case PF ->
                PF_CATEGORIES.getOrDefault(i.getCurrency(), CATEGORIES.getOrDefault(i.getCurrency(), "unknown"));
            case USD_CASH ->
                "USD Cash";
            case USD ->
                "USD Bank";
            case BONO ->
                "Dom. Bonds";
            case FCI, ETF ->
                CATEGORIES.getOrDefault(i.getCurrency(), i.getType().toString() + " " + i.getCurrency());
        };

    }

    public void monthly(boolean nominal) {

        final var currency = USD;
        final var etfs = this.getInvestments()
                .filter(Investment::isETF)
                .toList();

        this.console.appendLine("Month;Portfolio;CSPX;IWDA;Cash");

        final Map<YearMonth, BigDecimal[]> results = new LinkedHashMap<>();

        final var initialInvestment = new BigDecimal("10000");
        results.put(YearMonth.of(2019, 6), new BigDecimal[]{initialInvestment, initialInvestment, initialInvestment, initialInvestment, initialInvestment});

        for (var ym = YearMonth.of(2019, 6); ym.compareTo(Inflation.USD_INFLATION.getTo()) < 0; ym = ym.next()) {

            var next = ym.next();

            final var st = LocalDate.ofInstant(ym.asToDate().toInstant(), SYSTEM_DEFAULT_ZONE_ID).plusDays(1);

            final var fn = LocalDate.ofInstant(next.asToDate().toInstant(), SYSTEM_DEFAULT_ZONE_ID);

            final var portfolio = ONE.add(new ModifiedDietzReturn(
                    etfs,
                    currency,
                    nominal,
                    st,
                    fn).get().getMoneyWeighted(), C);

            final var cspx = ONE.add(new ModifiedDietzReturn(this.benchmark(etfs, CSPX), currency, nominal, st, fn).get().getMoneyWeighted(), C);
            final var iwda = ONE.add(new ModifiedDietzReturn(this.benchmark(etfs, IWDA), currency, nominal, st, fn).get().getMoneyWeighted(), C);
            final var cash = ONE.add(new ModifiedDietzReturn(this.benchmark(etfs, USD), currency, nominal, st, fn).get().getMoneyWeighted(), C);

            final var month = YearMonth.of(Date.from(fn.atStartOfDay().toInstant(ZoneOffset.ofHours(-3))));

            final var prev = results.get(month.prev());

            results.put(month, new BigDecimal[]{
                prev[0].multiply(portfolio, C),
                prev[1].multiply(cspx, C),
                prev[2].multiply(iwda, C),
                prev[3].multiply(cash, C)});
        }

        results.forEach((month, arr)
                -> this.console.appendLine(
                        format("{0}-{1};{2};{3};{4};{5}",
                                this.format.text(String.valueOf(month.getYear()), 4),
                                this.format.text((month.getMonth() < 10 ? "0" : "") + String.valueOf(month.getMonth()), 2),
                                this.format.currency(arr[0], 10),
                                this.format.currency(arr[1], 10),
                                this.format.currency(arr[2], 10),
                                this.format.currency(arr[3], 10))));
    }

    public void investments() {

        final Comparator<InvestmentTypeCurrencyAndAmount> TYPE_CURRENCY_COMPARATOR = comparing(InvestmentTypeCurrencyAndAmount::type)
                .thenComparing(InvestmentTypeCurrencyAndAmount::currency);

        Collector<Investment, ?, BigDecimal> mapper = Collectors.mapping(
                inv -> inv.getMoneyAmount().getAmount(),
                Collectors.reducing(ZERO, BigDecimal::add));

        this.console.appendLine(this.format.title("Inversiones actuales agrupadas por moneda"));

        final NumberFormat sixDigits = NumberFormat.getNumberInstance();
        sixDigits.setMinimumFractionDigits(6);
        var now = new Date();
        this.series.getInvestments().stream()
                .filter(investment -> investment.isCurrent(now))
                .collect(groupingBy(inv -> new InvestmentTypeAndCurrency(inv.getType(), inv.getCurrency()), mapper))
                .entrySet()
                .stream()
                .map(e -> new InvestmentTypeCurrencyAndAmount(e.getKey(), e.getValue()))
                .sorted(TYPE_CURRENCY_COMPARATOR)
                .map(e -> format("{0} {2}: {1}", e.type(), sixDigits.format(e.amount()), e.currency()))
                .forEach(this.console::appendLine);
    }

    private record LabelAndMDR(String label, ModifiedDietzReturnResult mdr) {

        public LabelAndMDR(int year, ModifiedDietzReturnResult mdr) {
            this(String.valueOf(year), mdr);
        }
    }

    public void brokerDetailedChart(PieChart chart) throws IOException {
        var now = new Date();
        Map<String, List<Investment>> byBroker = this.getInvestments()
                .filter(Investment::isETF)
                .filter(investment -> investment.isCurrent(now))
                .collect(Collectors.groupingBy(i -> i.getComment() == null ? "PPI" : "IBKR"));

        chart.create(
                "Investments By Broker",
                byBroker.entrySet().stream().flatMap(e -> this.brokerDetailedItem(e.getKey(), e.getValue())).toList(),
                "brokers-detail.png");
    }

    public void brokerChart(PieChart chart) throws IOException {
        var now = new Date();
        Map<String, List<Investment>> byBroker = this.getInvestments()
                .filter(Investment::isETF)
                .filter(investment -> investment.isCurrent(now))
                .collect(Collectors.groupingBy(i -> i.getComment() == null ? "PPI" : "IBKR"));

        chart.create(
                "Investments By Broker",
                byBroker.entrySet().stream().flatMap(e -> this.brokerItem(e.getKey(), e.getValue())).toList(),
                "brokers.png");

    }

    private Stream<PieItem> brokerDetailedItem(String broker, List<Investment> investments) {

        final var now = Inflation.USD_INFLATION.getTo();

        var invested = investments.stream()
                .map(Investment::getRealUSDInitialMoneyAmount)
                .reduce(MoneyAmount::add).map(MoneyAmount::amount).get();

        var currentValue = investments.stream()
                .map(Investment::getMoneyAmount)
                .map(ma -> ForeignExchanges.getMoneyAmountForeignExchange(ma.currency(), USD).apply(ma, now))
                .reduce(MoneyAmount::add).map(MoneyAmount::amount).get();

        var costValue = investments.stream()
                .map(Investment::getRealUSDCost)
                .reduce(MoneyAmount::add).map(MoneyAmount::amount).get();

        return Stream.of(
                new PieItem("Invested " + broker, invested),
                new PieItem("Cost " + broker, costValue),
                new PieItem("Gains " + broker, currentValue.subtract(invested)));

    }

    private Stream<PieItem> brokerItem(String broker, List<Investment> investments) {

        final var now = Inflation.USD_INFLATION.getTo();

        var currentValue = investments.stream()
                .map(Investment::getMoneyAmount)
                .map(ma -> ForeignExchanges.getMoneyAmountForeignExchange(ma.currency(), USD).apply(ma, now))
                .reduce(MoneyAmount::add).map(MoneyAmount::amount).get();

        return Stream.of(
                new PieItem("Current Value " + broker, currentValue));

    }
}
