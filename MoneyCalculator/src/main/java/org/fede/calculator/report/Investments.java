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
package org.fede.calculator.report;

import com.diogonunes.jcolor.Ansi;
import com.diogonunes.jcolor.AnsiFormat;
import com.diogonunes.jcolor.Attribute;
import java.math.BigDecimal;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import java.text.MessageFormat;
import static java.text.MessageFormat.format;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.reverseOrder;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.fede.calculator.money.Currency;
import org.fede.calculator.money.ForeignExchanges;
import org.fede.calculator.money.Inflation;
import org.fede.calculator.money.MathConstants;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.SimpleAggregation;
import static org.fede.calculator.money.Currency.*;
import static org.fede.calculator.money.MathConstants.C;
import org.fede.calculator.chart.BarChart;
import org.fede.calculator.chart.ChartSeriesMapper;
import org.fede.calculator.chart.ChartStyle;
import org.fede.calculator.chart.LabeledXYDataItem;
import org.fede.calculator.chart.Scale;
import org.fede.calculator.chart.ScatterXYChart;
import org.fede.calculator.chart.StackedTimeSeriesChart;
import org.fede.calculator.chart.TimeSeriesChart;
import org.fede.calculator.chart.ValueFormat;
import org.fede.calculator.money.PortfolioProjections;
import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.InvestmentAsset;
import org.fede.calculator.money.series.InvestmentEvent;
import static org.fede.calculator.money.series.InvestmentType.BONO;
import static org.fede.calculator.money.series.InvestmentType.FCI;
import static org.fede.calculator.money.series.InvestmentType.PF;
import static org.fede.calculator.money.series.InvestmentType.USD_CASH;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.SeriesReader;
import org.fede.calculator.money.series.SortedMapMoneyAmountSeries;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import org.fede.calculator.money.series.YearMonthUtil;
import org.fede.util.Pair;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.xy.XYSeries;

/**
 *
 * @author fede
 */
public class Investments {

    private final ZoneId SYSTEM_DEFAULT_ZONE_ID = ZoneId.systemDefault();

    private static final DateTimeFormatter DTF = DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneId.systemDefault());

    private final Comparator<Investment> COMPARATOR = comparing(Investment::getInitialDate, naturalOrder());

    public static final Map<Currency, String> ETF_NAME = Map.of(
            CSPX, "iShares Core S&P 500",
            EIMI, "iShares Core MSCI EM IMI",
            XRSU, "Xtrackers Russell 2000",
            RTWO, "Russell 2000 Q. Factor",
            MEUD, "Amundi Stoxx Europe 600",
            IWDA, "iShares Core MSCI World",
            XUSE, "iShares MSCI World ex-US"
    );

    private final AnsiFormat DIM = new AnsiFormat(Attribute.DIM());
    private final Map<String, AnsiFormat> ETF_COLOR = Map.of(
            "CSPX", DIM,
            "EIMI", DIM,
            "RTWO", DIM,
            "XRSU", DIM,
            "XUSE", DIM,
            "MEUD", DIM,
            "IWDA", DIM,
            "Cash", DIM
    );

    private final Map<Currency, String> PF_CATEGORIES = Map.ofEntries(
            Map.entry(USD, "USD Bank"),
            Map.entry(UVA, "UVA Bank"),
            Map.entry(ARS, "ARS Bank"));

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
        Predicate<Investment> isCurrent = i -> i.isCurrent(LocalDate.now());
        this.investmentReport(
                this.cashInvestments.cashInvestments().stream(),
                i -> true,
                i -> true,
                isCurrent, nominal);
    }

    private void investmentReport(final Predicate<Investment> everyone, boolean nominal) {
        Predicate<Investment> isCurrent = i -> i.isCurrent(LocalDate.now());
        this.investmentReport(this.getInvestments(), everyone, Investment::isETF, isCurrent, nominal);
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
                .sorted(Comparator.comparing(Investment::getInitialDate))
                .map(mapper)
                .toList();
    }

    public void invPF(boolean detail, boolean nominal) {
        final var inv
                = Stream.concat(
                        this.getInvestments()
                                .filter(i -> i.getType() == PF || i.getType() == FCI || i.getType() == BONO),
                        this.getInvestments()
                                .filter(Investment::isETF)
                                .filter(Investment::isPast))
                        .map(i -> ForeignExchanges.exchange(i, USD))
                        .map(i -> this.asReturn(i, nominal))
                        .toList();

        if (detail) {
            this.console.appendLine(this.format.subtitle("Investments"));
        }

        inv.stream()
                .filter(i -> detail)
                .sorted(Comparator.comparing(InvestmentReturn::to))
                .map(this::cdReportLine)
                .forEach(this.console::appendLine);

        var cols = Map.of(
                USD, 9,
                AY24, 10,
                LECAP, 10,
                CONAAFA, 13,
                CONBALA, 9,
                MEUD, 11,
                XRSU, 11,
                LETE, 9
        );

        final var currencies = List.of(ARS, USD, UVA, CSPX, MEUD, XRSU, EIMI, LETE, CONAAFA, CONBALA, CAPLUSA, LECAP, AY24);

        this.console.appendLine(this.format.subtitle("CDs, Bonds, ETFs & FCIs PnL by Year"));

        final var header = this.pfHeader(currencies, cols);

        this.console.appendLine(header);
        final var thisYear = LocalDate.now().getYear();
        for (var year = 2001; year <= thisYear; year++) {
            final int y = year;
            this.console.appendLine(
                    this.format.text(String.valueOf(y), 5),
                    currencies
                            .stream()
                            .map(c -> this.pnl(c, y, inv, cols))
                            .collect(Collectors.joining()));
        }
        this.console.appendLine(header);

    }

    private String pfHeader(List<Currency> currencies, Map<Currency, Integer> cols) {
        return this.format.text("Year", 5)
                + currencies
                        .stream()
                        .map(c -> this.format.center(c.name(), cols.getOrDefault(c, cols.getOrDefault(c, 12))))
                        .collect(Collectors.joining());
    }

    private String pnl(Currency currency, Integer year, List<InvestmentReturn> inv, Map<Currency, Integer> cols) {
        return inv.stream()
                .filter(i -> i.currency() == currency)
                .filter(i -> i.to().getYear() == year)
                .map(InvestmentReturn::profit)
                .reduce(MoneyAmount::add)
                .map(pnl -> this.format.currencyPL(pnl.amount(), cols.getOrDefault(currency, 12)))
                .orElse(this.format.text("", cols.getOrDefault(currency, 12)));
    }

    private String cdReportLine(InvestmentReturn pf) {

        var from = this.format.text(
                DateTimeFormatter.ISO_LOCAL_DATE.format(
                        pf.from()), 12);
        var to = this.format.text(
                DateTimeFormatter.ISO_LOCAL_DATE.format(pf.to())
                + (pf.to().isAfter(LocalDate.now()) ? "*" : ""),
                12);

        var days = this.format.text(String.valueOf(pf.days()), 4);
        var initial = this.format.currency(pf.initialAmount().amount(), 12);
        var endAmount = this.format.currency(pf.endAmount().amount(), 12);

        var profitAmount = pf.profit();
        var profit = this.format.currencyPL(profitAmount.amount(), 12);
        var profitPct = this.format.percent(profitAmount.amount().divide(pf.initialAmount().amount(), C), 9);

        var currency = this.format.text(pf.currency().name(), 9);
        return MessageFormat.format("{0}{1}{2}{3}{4}{5}{6}{7}", currency, from, to, days, initial, endAmount, profit, profitPct);

    }

    private InvestmentReturn asReturn(Investment pf, boolean nominal) {
        var from = pf.getInitialDate();
        var to = pf.getOut() != null
                ? pf.getOut().getDate()
                : LocalDate.now();
        var now = LocalDate.now();
        var realInitialAmount = nominal
                ? pf.getInitialMoneyAmount()
                : Inflation.USD_INFLATION.adjust(pf.getInitialMoneyAmount(), from, now);
        var nominalFinalAmount = pf.getOut() != null
                ? pf.getOut().getMoneyAmount()
                : pf.getInitialMoneyAmount();

        var maxInflationDate = to.compareTo(now) > 0 ? now : to;
        var realFinalAmount = nominal
                ? nominalFinalAmount
                : Inflation.USD_INFLATION.adjust(nominalFinalAmount, maxInflationDate, now);

        return new InvestmentReturn(pf.getCurrency(), from, to, realInitialAmount, realFinalAmount);

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
                this.format.text("", 24),
                this.range()
                        .mapToObj(y -> y == 0 ? "Total" : String.valueOf(y))
                        .map(value -> this.format.center(value, 8))
                        .collect(joining()));

        Stream.of(CSPX, EIMI, XRSU, RTWO, MEUD, IWDA)
                .map(symbol -> Pair.of(symbol, this.annualRealReturn(symbol, 0, nominal)))
                .sorted(Comparator.comparing(Pair::second, Comparator.reverseOrder()))
                .map(p -> this.row(p.first(), nominal))
                .forEach(this.console::appendLine);

    }

    private String row(Currency symbol, boolean nominal) {
        return format("{0}{1}",
                this.format.text(ETF_NAME.get(symbol), 24),
                this.range()
                        .mapToObj(y -> this.format.percent(this.annualRealReturn(symbol, y, nominal), 8))
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
        final var startYm = YearMonthUtil.max(YearMonth.of(Math.max(2019, year) - 1, 12), from);
        final var endYm = year == 0
                ? to
                : YearMonthUtil.min(YearMonth.of(year, 12), to);

        final var fx = ForeignExchanges.getForeignExchange(symbol, USD);
        var startValue = fx.exchange(amount, Currency.USD, startYm);
        var endValue = fx.exchange(amount, Currency.USD, endYm);
        if (!nominal) {
            startValue = Inflation.USD_INFLATION.adjust(startValue, startYm, Inflation.USD_INFLATION.getTo());
            endValue = Inflation.USD_INFLATION.adjust(endValue, endYm, Inflation.USD_INFLATION.getTo());
        }

        return endValue.amount().divide(startValue.amount(), MathConstants.C).subtract(ONE);

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

    public void mdrByYearChart() {

        var to = Inflation.USD_INFLATION.getTo().getYear();
        var inv = Stream.concat(this.series.getInvestments().stream(),
                this.cashInvestments.cashInvestments().stream())
                .toList();

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (var year = SeriesReader.readInt("start.year") + 1; year <= to; year++) {
            var mdr = new ModifiedDietzReturn(
                    inv,
                    false,
                    LocalDate.of(year, Month.JANUARY, 1),
                    LocalDate.of(year, Month.DECEMBER, 31))
                    .get();

            dataset.addValue(mdr.getMoneyWeighted(), "MDR", String.valueOf(year));
        }

        new BarChart(new ChartStyle(ValueFormat.PERCENTAGE, Scale.LINEAR))
                .create("MDR by Year", "Year", dataset, "mdr-by-year");
    }

    public void mdrChart(boolean cagr) {

        var initial = YearMonth.of(
                SeriesReader.readInt("start.year"),
                SeriesReader.readInt("start.month"));

        Function<ModifiedDietzReturnResult, BigDecimal> resultFunction
                = cagr ? ModifiedDietzReturnResult::annualizedMoneyWeighted
                        : ModifiedDietzReturnResult::getMoneyWeighted;

        var inv = Stream.concat(this.series.getInvestments().stream(),
                this.cashInvestments.cashInvestments().stream())
                .toList();

        var start = YearMonthUtil.max(initial,
                inv.stream()
                        .map(Investment::getInitialDate)
                        .map(YearMonth::from)
                        .min(Comparator.naturalOrder())
                        .orElse(initial));

        var nominalWithCash = this.mdrSeries(inv, true, start, resultFunction);

        var realWithCash = this.mdrSeries(inv, false, start, resultFunction);

        List<TimeSeries> mdrSeries = new ArrayList<>();

        mdrSeries.add(ChartSeriesMapper.asTimeSeries(realWithCash, "Real Returns"));
        mdrSeries.add(ChartSeriesMapper.asTimeSeries(nominalWithCash, "Nominal Returns"));
        var savings = this.series.realSavings(null);

        if (!cagr) {

            var investments = this.portfolioValue(false, i -> true);

            var uninvested = savings.map((ym, ma) -> ma.subtract(investments.getAmountOrElseZero(ym)));

            var uninvestedPct = uninvested.yearMonthStream()
                    .filter(ym -> ym.compareTo(start) >= 0)
                    .map(ym -> new TimeSeriesDatapoint(
                    ym,
                    uninvested.getAmount(ym)
                            .adjust(savings.getAmount(ym).amount(), ONE)
                            .amount()))
                    .toList();

            mdrSeries.add(ChartSeriesMapper.asTimeSeries(uninvestedPct, "Uninvested Cash"));

            var globalStocks
                    = this.currencyInvestment("Global Stocks", List.of(CSPX, MEUD, XRSU, RTWO, XUSE, EIMI))
                            .map((ym, ma) -> ma.adjust(savings.getAmount(ym).amount(), ONE));

            mdrSeries.add(ChartSeriesMapper.asTimeSeries(globalStocks));
        }
        new TimeSeriesChart(new ChartStyle(ValueFormat.PERCENTAGE, Scale.LINEAR))
                .createFromTimeSeries(
                        "Modified Dietz Returns" + (cagr ? " CAGR" : ""),
                        mdrSeries,
                        "mdr" + (cagr ? "-cagr" : ""));
    }

    private List<TimeSeriesDatapoint> mdrSeries(
            List<Investment> inv,
            boolean nominal,
            YearMonth start,
            Function<ModifiedDietzReturnResult, BigDecimal> resultFunction) {

        var startLocalDate = start.atEndOfMonth();
        var end = Inflation.USD_INFLATION.getTo();

        final List<TimeSeriesDatapoint> ss = new ArrayList<>((int) start.until(end, ChronoUnit.MONTHS));

        for (var ym = start; ym.compareTo(end) <= 0; ym = ym.plusMonths(1)) {

            var to = ym.atEndOfMonth();
            var realMdr = new ModifiedDietzReturn(
                    inv,
                    nominal,
                    startLocalDate,
                    to)
                    .get();
            ss.add(new TimeSeriesDatapoint(ym, resultFunction.apply(realMdr)));

        }
        return ss;
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
                .map(y -> this.format.center(y, 8))
                .collect(joining());

        final var nameColWidth = ETF_NAME.values()
                .stream()
                .mapToInt(String::length)
                .max()
                .orElse(12);

        this.console.appendLine(this.format.subtitle((nominal ? "Nominal" : "Real") + " Modified Dietz Returns"));

        this.console.appendLine(this.format.center(" ", nameColWidth), titleRow);
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
        return this.format.text(format.percent(value.getMoneyWeighted(), 8), 8, color);
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
                this.format.currency(d.getInvestedAmount().amount(), colWidths[i++]),
                this.format.currency(d.getCurrentAmount().amount(), colWidths[i++]),
                this.format.currencyPL(d.getGrossCapitalGains().amount(), colWidths[i++]),
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
                            : this.bar(ym, elements, SeriesReader.readInt("scale")));
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
                .filter(i -> Objects.isNull(currency) || currency == i.getCurrency())
                .sorted(comparing(Investment::getInitialDate, Comparator.naturalOrder()))
                .toList();

        final var start = inv
                .stream()
                .map(Investment::getIn)
                .map(InvestmentEvent::getDate)
                .map(YearMonth::from)
                //.reduce((left, right) -> left.min(right))
                .reduce(YearMonthUtil::min)
                .get();

        final var end = inv
                .stream()
                .map(i -> Optional.ofNullable(i.getOut()).map(InvestmentEvent::getDate)
                .map(YearMonth::from).orElse(Inflation.USD_INFLATION.getTo()))
                //.reduce((left, right) -> left.max(right))
                .reduce(YearMonthUtil::max)
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

            ym = ym.plusMonths(1);
        }

        return Map.of(
                "invested", investmentSeries,
                "cost", costSeries,
                "total", totalValuesSeries,
                "taxes", taxesValuesSeries);
    }

    private MoneyAmount real(Investment i, YearMonth moment, Function<Investment, MoneyAmount> extrator) {
        return Inflation.USD_INFLATION
                .adjust(extrator.apply(i), YearMonth.from(i.getInitialDate()), moment);
    }

    private MoneyAmount tax(Investment i, Function<Investment, MoneyAmount> invested, Function<Investment, MoneyAmount> total) {

        final var capitalGains = total.apply(i)
                .subtract(invested.apply(i));

        if (capitalGains.amount().signum() > 0) {
            return capitalGains.adjust(ONE, SeriesReader.readPercent("capitalGainsTaxRate"));
        }

        return MoneyAmount.zero(Currency.USD);
    }

    private MoneyAmount asUSD(MoneyAmount ma, LocalDate d) {
        return this.asUSD(ma, YearMonth.from(d));
    }

    private MoneyAmount asUSD(MoneyAmount ma, YearMonth ym) {
        return ForeignExchanges.getMoneyAmountForeignExchange(ma.currency(), Currency.USD).apply(ma, ym);
    }

    private MoneyAmount accum(List<Investment> investments, YearMonth yearMonth, Function<Investment, MoneyAmount> extractor) {

        return investments.stream()
                .filter(i -> YearMonth.from(i.getIn().getDate()).compareTo(yearMonth) <= 0)
                .filter(i -> i.isCurrent(yearMonth.atEndOfMonth()))
                .map(extractor)
                .reduce(MoneyAmount.zero(Currency.USD), MoneyAmount::add);
    }

    public void portfolioEvo(String type, boolean pct) {

        final BiFunction<Investment, YearMonth, MoneyAmount> totalFunction = (i, moment) -> this.asUSD(i.getInvestment().getMoneyAmount(), moment);

        Function<Investment, YearMonth> startFunction = i -> YearMonth.from(i.getIn().getDate());
        Function<Investment, YearMonth> endFunction = i -> Optional.ofNullable(i.getOut())
                .map(InvestmentEvent::getDate)
                .map(YearMonth::from)
                .map(ym -> ym.plusMonths(-1))
                .orElseGet(Inflation.USD_INFLATION::getTo);

        Function<Investment, String> classifier = i
                -> switch (i.getType()) {
            case ETF, FCI ->
                i.getCurrency().name();
            case BONO ->
                i.getType().name();
            case PF ->
                i.getType().toString().concat(" ").concat(i.getCurrency().name());
            case USD, USD_CASH ->
                "USD Cash";
        };
        Predicate<Investment> filterPredicate = i -> Objects.isNull(type) || i.getType().toString().equals(type);

        final var list = this.getAllInvestments();

        new Evolution<Investment>(this.console, this.bar)
                .evo(totalFunction, startFunction, endFunction, classifier, filterPredicate, COMPARATOR, list, pct);
    }

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

        Function<Investment, YearMonth> startFunction = i -> YearMonth.from(i.getIn().getDate());
        Function<Investment, YearMonth> endFunction = i -> Optional.ofNullable(i.getOut())
                .map(InvestmentEvent::getDate)
                .map(YearMonth::from)
                .map(ym -> ym.plusMonths(-1))
                .orElseGet(Inflation.USD_INFLATION::getTo);

        new Evolution<Investment>(this.console, this.bar)
                .evo(totalFunction, startFunction, endFunction, this::classifier, i -> true, COMPARATOR, this.getAllInvestments(), pct);
    }

    private String classifier(Investment i) {

        var categories = Map.ofEntries(
                Map.entry(CSPX, "Global Eq."),
                Map.entry(EIMI, "Global Eq."),
                Map.entry(MEUD, "Global Eq."),
                Map.entry(XUSE, "Global Eq."),
                Map.entry(XRSU, "Global Eq."),
                Map.entry(RTWO, "Global Eq."),
                Map.entry(CONAAFA, "Dom. Eq."),
                Map.entry(CONBALA, "Dom. Bonds"),
                Map.entry(CAPLUSA, "Dom. Bonds"),
                Map.entry(LECAP, "Dom. Bonds"),
                Map.entry(LETE, "Dom. Bonds"),
                Map.entry(AY24, "Dom. Bonds"),
                Map.entry(ARS, "ARS Cash"));

        return switch (i.getType()) {
            case PF ->
                PF_CATEGORIES.getOrDefault(i.getCurrency(), categories.getOrDefault(i.getCurrency(), "unknown"));
            case USD_CASH ->
                "USD Cash";
            case USD ->
                "USD Bank";
            case BONO ->
                "Dom. Bonds";
            case FCI, ETF ->
                categories.getOrDefault(i.getCurrency(), i.getType().toString() + " " + i.getCurrency());
        };

    }

    public void investments(LocalDate moment) {

        final Comparator<InvestmentTypeCurrencyAndAmount> TYPE_CURRENCY_COMPARATOR = comparing(InvestmentTypeCurrencyAndAmount::type)
                .thenComparing(InvestmentTypeCurrencyAndAmount::currency);

        Collector<Investment, ?, BigDecimal> mapper = Collectors.mapping(
                inv -> inv.getMoneyAmount().amount(),
                Collectors.reducing(ZERO, BigDecimal::add));

        this.console.appendLine(this.format.title("Inversiones actuales agrupadas por moneda"));

        this.series.getInvestments()
                .stream()
                .filter(investment -> investment.isCurrent(moment))
                .collect(groupingBy(inv -> new InvestmentTypeAndCurrency(inv.getType(), inv.getCurrency()), mapper))
                .entrySet()
                .stream()
                .map(e -> new InvestmentTypeCurrencyAndAmount(e.getKey(), e.getValue()))
                .sorted(TYPE_CURRENCY_COMPARATOR)
                .map(e -> this.investmentLine(
                e.currency(),
                e.amount(),
                ForeignExchanges.getForeignExchange(e.currency(), USD)
                        .exchange(new MoneyAmount(ONE, e.currency()), USD, moment)))
                .forEach(this.console::appendLine);

        this.console.appendLine(this.investmentLine(EUR, ONE, ForeignExchanges.getForeignExchange(EUR, USD)
                .exchange(new MoneyAmount(ONE, EUR), USD, moment)));
        this.console.appendLine(this.investmentLine(USD, ONE,
                ForeignExchanges.getForeignExchange(USD, ARS)
                        .exchange(new MoneyAmount(ONE, USD), ARS, moment)));

        if (this.series.getInvestments()
                .stream()
                .filter(investment -> investment.isCurrent(moment))
                .anyMatch(i -> i.getCurrency() == MEUD)) {

            this.console.appendLine(this.investmentLine(MEUD, ONE,
                    ForeignExchanges.getForeignExchange(MEUD, EUR)
                            .exchange(new MoneyAmount(ONE, MEUD), EUR, moment)));
        }
    }

    private String investmentLine(Currency currency, BigDecimal quantity, MoneyAmount price) {
        return format("{0}{1} {2}",
                this.format.text(currency.name(), 5),
                this.format.number(quantity, 12),
                this.format.currency(price, 10));
    }

    private record LabelAndMDR(String label, ModifiedDietzReturnResult mdr) {

        public LabelAndMDR(int year, ModifiedDietzReturnResult mdr) {
            this(String.valueOf(year), mdr);
        }
    }

    public void ppiTransfer(String type) {

        final Function<InvestmentEvent, MoneyAmount> grossSaleMapper
                = (InvestmentEvent e) -> e.getRealUSDMoneyAmount()
                        .add(e.getRealUSDFeeMoneyAmount())
                        .add(e.getRealUSDTransferFeeMoneyAmount());

        final SoldAndBought<MoneyAmount> netAmountSummary
                = this.summarize(
                        grossSaleMapper,
                        InvestmentEvent::getRealUSDMoneyAmount,
                        MoneyAmount.zero(USD),
                        MoneyAmount::add);

        final Function<InvestmentEvent, MoneyAmount> feeMapper = e -> e.getRealUSDTransferFeeMoneyAmount().add(e.getRealUSDFeeMoneyAmount());

        final SoldAndBought<MoneyAmount> feeSummary
                = this.summarize(
                        feeMapper,
                        feeMapper,
                        MoneyAmount.zero(USD),
                        MoneyAmount::add);

        final var fees = feeSummary.sold.add(feeSummary.bought);

        this.console.appendLine(this.format.subtitle("Comisiones"));
        this.console.appendLine("Sold ", this.format.currency(feeSummary.sold, 16));
        this.console.appendLine("Bought ", this.format.currency(feeSummary.bought, 16));
        this.console.appendLine("Total:", this.format.currencyPL(fees.amount().negate(), 16),
                " ",
                this.format.percent(fees.amount().divide(netAmountSummary.sold.amount(), C), 6)
        );

        this.console.appendLine(this.format.subtitle("Monto Neto"));
        this.console.appendLine("Sold ", this.format.currency(netAmountSummary.sold, 16));

        this.console.appendLine(this.format.subtitle("Tax"));

        //bna comprador
        final Map<String, BigDecimal> bnaFX = Map.of(
                "2025-04-03", new BigDecimal("1075"),
                "2025-04-08", new BigDecimal("1076.25"),
                "2025-04-14", new BigDecimal("1198"),
                "2025-04-24", new BigDecimal("1174"),
                "2025-05-01", new BigDecimal("1170"),
                "2025-05-09", new BigDecimal("1136"),
                "2025-05-16", new BigDecimal("1142"),
                "2025-05-23", new BigDecimal("1133.5")
        );

        final var taxAmount
                = this.series.getInvestments()
                        .stream()
                        .filter(Investment::isETF)
                        .filter(i -> i.getOut() != null)
                        .map(i -> i.getOut().getMoneyAmount().subtract(this.cost(i)))
                        //.peek(m -> this.console.appendLine(this.format.currency(m, 20)))
                        .reduce(MoneyAmount.zero(USD), MoneyAmount::add)
                        .adjust(BigDecimal.ONE, SeriesReader.readPercent("capitalGainsTaxRate"));

        this.console.appendLine(this.format.currency(taxAmount, 20),
                " ",
                this.format.percent(taxAmount.amount().divide(netAmountSummary.sold.amount(), C))
        );

        final var arsTax = this.series.getInvestments()
                .stream()
                .filter(Investment::isETF)
                .filter(i -> i.getOut() != null)
                .map(i -> this.capitalGainARS(i, bnaFX))
                .reduce(MoneyAmount.zero(Currency.ARS), MoneyAmount::add)
                .adjust(BigDecimal.ONE, SeriesReader.readPercent("capitalGainsTaxRate"));

        this.console.appendLine(this.format.currency(
                arsTax,
                20));

        final var currentTaxAmount = ForeignExchanges.getForeignExchange(Currency.ARS, USD)
                .exchange(arsTax, USD, LocalDate.now());
        this.console.appendLine(
                "Current ",
                this.format.currency(currentTaxAmount, 15),
                " ",
                this.format.percent(currentTaxAmount.amount().divide(netAmountSummary.sold.amount(), C))
        );

        this.console.appendLine(this.format.subtitle("Detail"));

        this.console.appendLine(MessageFormat.format("{0} {1} {2} {3} {4} {5} {6} {7}",
                this.format.text("  Date", 10),
                this.format.text("Curr.", 5),
                this.format.text("Cant.", 8),
                this.format.text("   Amount", 18),
                this.format.text("   Fee", 16),
                this.format.text("   CG USD", 16),
                this.format.text("     TC", 16),
                this.format.text("   CG ARS", 20)
        ));

        final Function<Investment, InvestmentGroup> classifier = switch (type) {
            case "group" ->
                (Investment i) -> new InvestmentGroup(i.getCurrency(), DTF.format(i.getOut().getDate()));
            case "groupall" ->
                (Investment i) -> new InvestmentGroup(i.getCurrency(), "");
            default ->
                null;
        };

        if (classifier != null) {

            Map<InvestmentGroup, Investment> grouped
                    = this.series.getInvestments()
                            .stream()
                            .filter(Investment::isETF)
                            .filter(i -> i.getOut() != null)
                            .collect(Collectors.groupingBy(
                                    classifier,
                                    Collectors.reducing(null, this::union)
                            ));

            grouped.values()
                    .stream()
                    .sorted(Comparator.comparing((Investment i) -> i.getOut().getDate())
                            .thenComparing(Comparator.comparing((Investment i) -> i.getCurrency())))
                    .map(e -> this.sellReport(e, bnaFX))
                    .forEach(this.console::appendLine);
        } else {
            this.series.getInvestments()
                    .stream()
                    .filter(Investment::isETF)
                    .filter(i -> i.getOut() != null)
                    .sorted(Comparator.comparing((Investment i) -> i.getOut().getDate())
                            .thenComparing(Comparator.comparing((Investment i) -> i.getCurrency())))
                    .map(i -> this.sellReport(i, bnaFX))
                    .forEach(this.console::appendLine);
        }
    }

    private record InvestmentGroup(Currency currency, String date) {

    }

    private Investment union(Investment left, Investment right) {

        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }

        var res = new Investment();
        res.setIn(this.union(left.getIn(), right.getIn()));
        res.setOut(this.union(left.getOut(), right.getOut()));

        var c1 = left.getComment();
        var c2 = right.getComment();

        if (c1 == null && c2 == null) {
            res.setComment(null);
        } else {
            res.setComment(c1 == null ? c2 : c1);
        }
        res.setId(left.getId());
        res.setType(left.getType());
        res.setInvestment(this.union(left.getInvestment(), right.getInvestment()));
        return res;
    }

    private InvestmentAsset union(InvestmentAsset left, InvestmentAsset right) {

        InvestmentAsset res = new InvestmentAsset();

        res.setAmount(left.getAmount().add(right.getAmount()));
        res.setCurrency(left.getCurrency());

        return res;
    }

    private InvestmentEvent union(InvestmentEvent left, InvestmentEvent right) {

        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }

        var res = new InvestmentEvent();

        res.setAmount(left.getAmount().add(right.getAmount()));
        res.setCurrency(left.getCurrency());
        res.setDate(left.getDate().compareTo(right.getDate()) <= 0 ? left.getDate() : right.getDate());
        res.setFee(left.getFee().add(right.getFee()));
        res.setFx(left.getFx());

        if (left.getTransferFee() == null) {
            res.setTransferFee(right.getTransferFee());
        } else if (right.getTransferFee() == null) {
            res.setTransferFee(left.getTransferFee());
        } else {
            res.setTransferFee(left.getTransferFee().add(right.getTransferFee()));
        }
        return res;
    }

    private MoneyAmount capitalGainARS(Investment i, Map<String, BigDecimal> bnaFX) {
        return new MoneyAmount(i.getOut()
                .getMoneyAmount()
                .subtract(this.cost(i))
                .adjust(BigDecimal.ONE, bnaFX.get(DTF.format(i.getOut().getDate()))).amount(),
                Currency.ARS);
    }

    private String sellReport(Investment i, Map<String, BigDecimal> bnaFX) {

        return MessageFormat.format("{0} {1} {2} {3} {4} {5} {6} {7}",
                DateTimeFormatter.ISO_DATE.format(
                        i.getOut().getDate()),
                i.getCurrency(),
                this.format.number(i.getInvestment().getAmount(), 8),
                this.format.currency(i.getOut().getMoneyAmount(), 18),
                this.format.currency(i.getOut().getFeeMoneyAmount(), 16),
                this.format.currency(i.getOut().getMoneyAmount().subtract(this.cost(i)), 16),
                this.format.currency(new MoneyAmount(bnaFX.get(DTF.format(i.getOut().getDate())), Currency.ARS), 16),
                this.format.currency(this.capitalGainARS(i, bnaFX), 20)
        );
    }

    private MoneyAmount cost(Investment i) {

        var iva = SeriesReader.readPercent("iva").add(ONE);
        if (i.getIn().getFx() != null) {
            return new MoneyAmount(
                    i.getIn().getFx()
                            .multiply(
                                    i.getIn().getAmount()
                                            .add(i.getIn().getFee()
                                                    .multiply(iva, C)
                                            ),
                                    C),
                    USD
            );
        }
        return i.getIn().getMoneyAmount()
                .add(i.getIn().getFeeMoneyAmount().adjust(BigDecimal.ONE, iva));
    }

    private <T> SoldAndBought<T> summarize(
            Function<InvestmentEvent, T> sellMapper,
            Function<InvestmentEvent, T> buyMapper,
            T identity,
            BinaryOperator<T> reduction) {
        final var limitDate = LocalDateTime.of(2025, Month.APRIL, 2, 0, 0, 0, 0);

        final var lastBuyDate = LocalDateTime.of(2025, Month.JUNE, 30, 0, 0, 0, 0);
        Predicate<Investment> isCurrent = i -> i.isCurrent(LocalDate.now());
        return new SoldAndBought<>(
                this.series.getInvestments()
                        .stream()
                        .filter(Investment::isETF)
                        .map(Investment::getOut)
                        .filter(Objects::nonNull)
                        .map(sellMapper)
                        .reduce(identity, reduction),
                this.series.getInvestments()
                        .stream()
                        .filter(Investment::isETF)
                        .filter(isCurrent)
                        .map(Investment::getIn)
                        .filter(e
                                -> e.getDate()
                                .atTime(23, 59, 59, 999999)
                                .isAfter(limitDate))
                        .filter(e
                                -> e.getDate()
                                .atTime(23, 59, 59, 999999)
                                .isBefore(lastBuyDate))
                        .map(buyMapper)
                        .reduce(identity, reduction));

    }

    public record SoldAndBought<T>(T sold, T bought) {

    }

    public void savedAndInvestedChart() {

        new TimeSeriesChart(new ChartStyle(ValueFormat.CURRENCY, Scale.LOG))
                .create("ETF Real Growth",
                        List.of(
                                this.accumulatedNetContributions(false, Investment::isETF),
                                this.portfolioValue(false, Investment::isETF)),
                        "growth-etf-real");

        final var realContributions = this.accumulatedNetContributions(false, i -> true);

        new TimeSeriesChart()
                .create("Investments Real Growth",
                        List.of(
                                realContributions,
                                this.portfolioValue(false, i -> true)),
                        "growth-all-real");

        var savings = this.series.realSavings(null);
        savings.setName("Real Savings");

        var investments = this.portfolioValue(false, i -> true);

        var uninvested = savings.map((ym, ma) -> ma.subtract(investments.getAmountOrElseZero(ym)));
        uninvested.setName("Cash");

        var income = new SimpleAggregation().sum(this.series.realIncome());
        income.setName("Real Icome");

        new TimeSeriesChart().create("Real Income, Savings & Investments",
                List.of(
                        savings,
                        investments,
                        income,
                        uninvested),
                "savings-investments-real");
    }

    public void savingsInvestmentsPercentChart() {

        final var income = new SimpleAggregation().sum(this.series.realIncome());
        var savings = this.series.realSavings(null);

        var savedIncomePercent = savings.map((ym, s) -> s.adjust(income.getAmount(ym).amount(), ONE));
        savedIncomePercent.setName("% Saved of all Income");

        var investments = this.portfolioValue(false, i -> true);

        var cashSavingsPercent = savings
                .map((ym, ma) -> ma
                .subtract(investments.getAmountOrElseZero(ym))
                .adjust(savings.getAmount(ym).amount(), ONE));

        cashSavingsPercent.setName("% Cash Savings");

        new TimeSeriesChart(new ChartStyle(ValueFormat.PERCENTAGE, Scale.LINEAR))
                .create("Real Income, Savings & Investments",
                        List.of(savedIncomePercent, cashSavingsPercent),
                        "savings-investments-percent");
    }

    public void investmentsByClassChart() {

        var localUSDBonds = List.of(USD, LETE, AY24, CONBALA);
        var localARSBonds = List.of(ARS, CAPLUSA, LECAP, UVA);
        var localStocks = List.of(CONAAFA);
        var globalStocks = List.of(CSPX, MEUD, XRSU, RTWO, XUSE, EIMI);

        var savings = this.series.realSavings(null);
        savings.setName("Real Savings");

        var investments = this.portfolioValue(false, i -> true);

        var cash = savings.map((ym, ma) -> ma.subtract(investments.getAmountOrElseZero(ym)));
        cash.setName("Cash");

        var ss = List.of(
                this.currencyInvestment("Local USD Bonds", localUSDBonds),
                this.currencyInvestment("Local ARS Bonds", localARSBonds),
                this.currencyInvestment("Local Stocks", localStocks),
                this.currencyInvestment("Global Stocks", globalStocks),
                cash);

        new StackedTimeSeriesChart()
                .create("Real Investments by Currency (Grouped)",
                        ss,
                        "investments-by-class-s.png");

        new TimeSeriesChart()
                .create("Real Investments by Currency (Grouped)",
                        ss,
                        "investments-by-class.png");

    }

    private MoneyAmountSeries currencyInvestment(String name, List<Currency> currencies) {
        var res = this.portfolioValue(false, i -> currencies.contains(i.getCurrency()));

        res.setName(name);
        return res;

    }

    private MoneyAmountSeries portfolioValue(boolean nominal, Predicate<Investment> filter) {

        var start = this.series.getInvestments()
                .stream()
                .filter(filter)
                .map(Investment::getIn)
                .map(InvestmentEvent::getDate)
                .map(YearMonth::from)
                .min(YearMonth::compareTo).get();

        var valueSeries = new SortedMapMoneyAmountSeries(USD, (nominal ? "Nominal" : "Real") + " Investments");
        final var end = Inflation.USD_INFLATION.getTo();

        for (YearMonth ym = start; ym.compareTo(end) <= 0; ym = ym.plusMonths(1)) {

            final var moment = ym.atEndOfMonth();
            final var momentYm = ym;

            valueSeries.putAmount(ym,
                    this.series.getInvestments()
                            .stream()
                            .filter(filter)
                            .filter(i -> i.isCurrent(moment))
                            .map(Investment::getInvestment)
                            .map(InvestmentAsset::getMoneyAmount)
                            .map(ma -> ForeignExchanges.getForeignExchange(ma.currency(), USD).exchange(ma, USD, moment))
                            .map(ma -> nominal
                            ? ma
                            : Inflation.USD_INFLATION.adjust(ma, momentYm, end))
                            .reduce(MoneyAmount.zero(USD), MoneyAmount::add));
        }

        return valueSeries;

    }

    private MoneyAmountSeries accumulatedNetContributions(boolean nominal, Predicate<Investment> filter) {

        Map<YearMonth, InvestmentEvent> contributions = this.series.getInvestments()
                .stream()
                .filter(filter)
                .map(i -> ForeignExchanges.exchange(i, USD))
                .map(Investment::getIn)
                .collect(Collectors.groupingBy(
                        ev -> YearMonth.from(ev.getDate()),
                        Collectors.reducing(null, this::union)
                ));

        Map<YearMonth, InvestmentEvent> withdrawals = this.series.getInvestments()
                .stream()
                .filter(filter)
                .filter(i -> i.getOut() != null)
                .map(i -> ForeignExchanges.exchange(i, USD))
                .map(Investment::getOut)
                .collect(Collectors.groupingBy(
                        ev -> YearMonth.from(ev.getDate()),
                        Collectors.reducing(null, this::union)
                ));

        var start = contributions.keySet().stream().min(YearMonth::compareTo).get();

        var accSeries = new SortedMapMoneyAmountSeries(USD, (nominal ? "Nominal" : "Real") + " Contributions");
        var end = YearMonth.now();

        var acc = MoneyAmount.zero(USD);

        Function<InvestmentEvent, MoneyAmount> realFunction = nominal
                ? InvestmentEvent::getMoneyAmount
                : InvestmentEvent::getRealUSDMoneyAmount;

        for (YearMonth ym = start; ym.compareTo(end) <= 0; ym = ym.plusMonths(1)) {
            var contribution = Optional.ofNullable(contributions.get(ym))
                    .map(realFunction)
                    .orElse(MoneyAmount.zero(USD));

            var withdrawal = Optional.ofNullable(withdrawals.get(ym))
                    .map(realFunction)
                    .orElse(MoneyAmount.zero(USD));
            acc = acc.add(contribution.subtract(withdrawal))
                    .max(MoneyAmount.zero(USD));

            accSeries.putAmount(ym, acc);
        }

        return accSeries;
    }

    private BigDecimal cagr() {
        return SeriesReader.readPercent("futureReturn")
                .subtract(SeriesReader.readPercent("expectedInflation"), C);
    }

    public MoneyAmount projectedPortfolioSize(MoneyAmount presentValue, MoneyAmount yearlySavings, double pValue) {
        var years = SeriesReader.readInt("retirementHorizon");
        final var expectedReturn = this.cagr()
                .doubleValue();

        var expectedVolatility = SeriesReader.readPercent("futureVolatility").doubleValue();

        var projection = this.predictedValues(
                Inflation.USD_INFLATION.getTo(),
                years,
                pValue,
                presentValue,
                expectedReturn,
                expectedVolatility, yearlySavings);

        return projection.getAmount(projection.getTo());

    }

    public void projection(MoneyAmount savings) {

        final var expectedReturn = this.cagr()
                .doubleValue();

        var expectedVolatility = SeriesReader.readPercent("futureVolatility").doubleValue();

        var portfolio = this.portfolioValue(false, Investment::isETF);

        var presentValue = portfolio.getAmount(portfolio.getTo());

        var years = SeriesReader.readInt("retirementHorizon");

        var p10 = this.predictedValues(
                portfolio.getTo(),
                years,
                0.1d,
                presentValue,
                expectedReturn,
                expectedVolatility, savings);

        var p50 = this.predictedValues(
                portfolio.getTo(),
                years,
                0.5d,
                presentValue,
                expectedReturn,
                expectedVolatility, savings);

        var p90 = this.predictedValues(
                portfolio.getTo(),
                years,
                0.9d,
                presentValue,
                expectedReturn,
                expectedVolatility, savings);

        new TimeSeriesChart(new ChartStyle(ValueFormat.CURRENCY, Scale.LOG))
                .create("Future Return Saving " + this.format.currency(savings.amount()),
                        List.of(portfolio,
                                p10,
                                p50,
                                p90),
                        "future" + (savings.isZero()
                        ? "-with-0"
                        : "-with-" + savings.amount().intValue()));
    }

    private MoneyAmountSeries predictedValues(
            YearMonth start,
            int years,
            double percentile,
            MoneyAmount initial,
            double carg,
            double volatility,
            MoneyAmount savings) {

        List<MoneyAmountSeries> s = new ArrayList<>(years);

        s.add(this.predictedValues(start, years, percentile, initial, carg, volatility));
        for (int y = 1; y < years; y++) {
            s.add(this.predictedValues(
                    YearMonth.of(start.getYear() + y, start.getMonthValue()),
                    years - y,
                    percentile,
                    savings,
                    carg,
                    volatility));

        }
        return s.stream().reduce(MoneyAmountSeries::add).get();
    }

    private MoneyAmountSeries predictedValues(
            YearMonth start,
            int years,
            double percentile,
            MoneyAmount initial,
            double carg,
            double volatility) {

        //   this.console.appendLine("");
        //  this.console.appendLine(""+start+" "+years + " "+initial);
        var valueSeries = new SortedMapMoneyAmountSeries(USD,
                " P"
                + (int) (percentile * 100d));
        valueSeries.putAmount(start, initial);
        for (var i = 1; i <= years; i++) {

            valueSeries.putAmount(start.getYear() + i, start.getMonthValue(),
                    new MoneyAmount(
                            BigDecimal.valueOf(
                                    PortfolioProjections.calculatePortfolioPercentile(
                                            initial.amount().doubleValue(),
                                            carg,
                                            volatility,
                                            i,
                                            percentile)), USD));

        }
        // this.console.appendLine(""+valueSeries.getFrom() + " "+ valueSeries.getTo());
        return valueSeries;
    }

    public void investmentScatterChart(Currency currency) {
        this.investmentScatterChart(currency, ValueFormat.CURRENCY);
    }

    public void investmentScatterChart(Currency currency, ValueFormat format) {

        final var ss = new XYSeries("Quantity");

        var fx = ForeignExchanges.getForeignExchange(currency, USD);
        Predicate<Investment> isCurrent = i -> i.isCurrent(LocalDate.now());
        var fxFrom = this.series.getInvestments()
                .stream()
                .filter(isCurrent)
                .filter(i -> i.getCurrency() == currency)
                .map(Investment::getInitialDate)
                .map(YearMonth::from)
                .min(Comparator.naturalOrder())
                .get();

        var fxTo = fx.getTo();
        var one = new MoneyAmount(ONE, currency);

        Stream.concat(
                Stream.iterate(fxFrom, ym -> ym.compareTo(fxTo) <= 0, (YearMonth ym) -> ym.plusMonths(1))
                        .map((YearMonth ym) -> new LabeledXYDataItem(
                        ym.atEndOfMonth(),
                        fx.exchange(one, USD, ym).amount(),
                        ""
                )),
                 this.series.getInvestments()
                        .stream()
                        .filter(isCurrent)
                        .filter(i -> i.getCurrency() == currency)
                        .map((Investment i) -> new LabeledXYDataItem(
                        i.getInitialDate(),
                        this.initialMoneyAmountUSD(i).adjust(i.getInvestment().getAmount(), ONE).amount(),
                        i.getInvestment().getAmount().toString()
                )))
                .forEach(ss::add);

        new ScatterXYChart(new ChartStyle(ValueFormat.DATE, Scale.LINEAR),
                new ChartStyle(format, Scale.LINEAR))
                .create(
                        "Investments " + currency.name(),
                        USD,
                        List.of(ss),
                        "Date",
                        "Price",
                        "inv-" + currency.name());
    }

    private MoneyAmount initialMoneyAmountUSD(Investment i) {
        if (i.getIn().getFx() == null) {
            return i.getInitialMoneyAmount();
        }
        return i.getInitialMoneyAmount().adjust(ONE, i.getIn().getFx());
    }
}
