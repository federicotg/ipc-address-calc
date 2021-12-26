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
import com.fasterxml.jackson.core.type.TypeReference;
import java.math.BigDecimal;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.text.MessageFormat.format;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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
import static java.util.stream.Collectors.toMap;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import static org.fede.calculator.money.Inflation.USD_INFLATION;
import static org.fede.calculator.money.MathConstants.CONTEXT;
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
 * @author federicogentile
 */
public class Investments {

    private static final BigDecimal IVA = new BigDecimal("1.21");

    private static final BigDecimal CAPITAL_GAINS_TAX_RATE = new BigDecimal("0.15");

    private static final BigDecimal TRADING_FEE = new BigDecimal("0.006");

    private static final MoneyAmount ZERO_USD = new MoneyAmount(ZERO, "USD");

    private static final TypeReference<Map<String, BenchmarkItem>> BENCHMARK_TR = new TypeReference<Map<String, BenchmarkItem>>() {
    };

    private static final Comparator<Pair<String, Pair<BigDecimal, BigDecimal>>> CMP = comparing((Pair<String, Pair<BigDecimal, BigDecimal>> p) -> p.getSecond().getSecond()).reversed();

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

    private static final LocalDate ETF_START_DATE = LocalDate.of(2019, Month.JULY, 24);

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
        this.cashInvestments = new CashInvestmentBuilder(SeriesReader.readSeries("/saving/ahorros-dolar-liq.json"));
    }

    public void inv(final Predicate<Investment> everyone, boolean nominal, String currency) {

        this.console.appendLine(this.format.title(format("{0} Investment Results", nominal ? "Nominal" : "Real")));

        final var ics = new InvestmentCostStrategy(currency, TRADING_FEE, IVA.subtract(ONE, CONTEXT), CAPITAL_GAINS_TAX_RATE);

        final var mw = 13;
        final var colWidths = new int[]{5, 11, 9, mw, mw, mw, 9, mw, 9, 10, 1, 24, 10, 7, 10, 7};

        this.invHeader(colWidths);

        final var details = this.getAllInvestments()
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

        this.console.appendLine(this.format.subtitle("Total"));

        this.console.appendLine(
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

        this.console.appendLine(
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

        final var etfs = this.getAllInvestments().stream().filter(inv -> inv.getType().equals(InvestmentType.ETF)).collect(toList());

        final var modifiedDietzReturn = new ModifiedDietzReturn(etfs, currency, nominal).get();

        this.console.appendLine(this.format.subtitle("Benchmark (Before Fees & Taxes)"));

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

        final var textColWidth = 30;
        this.console.appendLine(this.format.text(" ", textColWidth), this.format.text(" Return", 8), this.format.text("    Annualized", 16));

        final Function<Pair<String, Pair<BigDecimal, BigDecimal>>, String> lineFunction
                = (p) -> format("{0} {1} {2}",
                        this.format.text(ETF_NAME.getOrDefault(p.getFirst(), p.getFirst()), textColWidth, ETF_COLOR.getOrDefault(p.getFirst(), new AnsiFormat(Attribute.BRIGHT_WHITE_TEXT()))),
                        this.format.percent(p.getSecond().getFirst(), 8),
                        this.bar.pctBar(p.getSecond().getSecond()));

        Stream.of(benchmarksStream, modelPortfolioStream, portfolioTWCAGRStream)
                .reduce(Stream.empty(), Stream::concat)
                .sorted(CMP)
                .map(lineFunction)
                .forEach(this.console::appendLine);

        this.console.appendLine(this.format.subtitle("Modified Dietz Return"));

        IntStream.rangeClosed(2019, LocalDate.now().getYear())
                .mapToObj(year -> Pair.of(String.valueOf(year), new ModifiedDietzReturn(etfs, currency, nominal, LocalDate.of(year, Month.JANUARY, 1), LocalDate.of(year, Month.DECEMBER, 31)).get()))
                .map(lineFunction)
                .forEach(this.console::appendLine);
    }

    private BenchmarkItem benchmarkItem(boolean nominal, Map.Entry<String, BigDecimal> e) {

        final var oneNominal = new MoneyAmount(ONE, e.getKey());
        final var usd = ForeignExchanges.getMoneyAmountForeignExchange(e.getKey(), "USD").apply(oneNominal, USD_INFLATION.getTo());
        final var item = new BenchmarkItem(e.getValue(), usd.getAmount());
        return nominal
                ? item
                : real(item);
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
        this.console.appendLine(
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

    private static Pair<BigDecimal, BigDecimal> cagr(BigDecimal initial, BigDecimal current, LocalDate since) {
        final var days = (double) ChronoUnit.DAYS.between(since, LocalDate.now());
        final var profit = current.divide(initial, CONTEXT).subtract(ONE, CONTEXT);
        final double x = Math.pow(
                BigDecimal.ONE.add(profit).doubleValue(),
                365.0d / days) - 1.0d;
        return Pair.of(profit, BigDecimal.valueOf(x));
    }

    private void invHeader(int[] colWidths) {
        var separator = IntStream.rangeClosed(0, Arrays.stream(colWidths).sum() - 10).mapToObj(n -> "=").collect(Collectors.joining());
        var i = 0;
        this.console.appendLine(separator);
        this.console.appendLine(
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
        this.console.appendLine(separator);
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

    private void invEvo(String title, String currency, boolean nominal, boolean pct) {

        this.console.appendLine(format(title, nominal ? "Nominal" : "Real"));

        final var m = this.investmentEvolution(currency, nominal);

        final var inv = m.get("invested");
        final var fee = m.get("fees");
        final var total = m.get("total");
        final var taxes = m.get("taxes");

        total.forEach((ym, cashMa) -> {

            final var investment = inv.getAmount(ym);
            final var feeAmount = fee.getAmount(ym);
            final var totalAmount = total.getAmount(ym);
            final var capitalGains = totalAmount.subtract(investment).subtract(feeAmount);
            final var taxAmount = taxes.getAmount(ym);
            final var netCapitalGains = capitalGains.subtract(taxAmount);

            final var elements = List.of(
                    Pair.of(investment, Attribute.WHITE_BACK()),
                    Pair.of(feeAmount, Attribute.YELLOW_BACK()),
                    Pair.of(netCapitalGains, Attribute.GREEN_BACK()),
                    Pair.of(taxAmount, Attribute.CYAN_BACK()));

            this.console.appendLine(
                    pct
                            ? this.pctBar(ym, elements)
                            : this.bar(ym, elements, 950));
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
                ": fees, ",
                Ansi.colorize(" ", Attribute.GREEN_BACK()),
                ": profits, ",
                Ansi.colorize(" ", Attribute.RED_BACK()),
                ": losses, ",
                Ansi.colorize(" ", Attribute.CYAN_BACK()),
                ": taxes.");

    }

    private Map<String, MoneyAmountSeries> investmentEvolution(String currency, boolean nominal) {

        final var inv = this.getAllInvestments()
                .stream()
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

        final var end = inv
                .stream()
                .map(i -> Optional.ofNullable(i.getOut()).map(InvestmentEvent::getDate).map(YearMonth::of).orElse(Inflation.USD_INFLATION.getTo()))
                .reduce((left, right) -> left.max(right))
                .get();

        final var investmentSeries = new SortedMapMoneyAmountSeries("USD");
        final var feeSeries = new SortedMapMoneyAmountSeries("USD");
        final var totalValuesSeries = new SortedMapMoneyAmountSeries("USD");
        final var taxesValuesSeries = new SortedMapMoneyAmountSeries("USD");

        var ym = start;
        while (ym.compareTo(end) <= 0) {

            final var moment = ym;

            final Function<Investment, MoneyAmount> invested = i -> this.asUSD(i.getIn().getMoneyAmount(), i.getInitialDate());
            final Function<Investment, MoneyAmount> realInvested = i -> this.real(i, moment, invested);

            investmentSeries.putAmount(ym, accum(inv, ym, nominal ? invested : realInvested));

            final Function<Investment, MoneyAmount> fee = i -> this.asUSD(i.getIn().getFeeMoneyAmount(), i.getInitialDate());
            final Function<Investment, MoneyAmount> realFee = i -> this.real(i, moment, fee);

            feeSeries.putAmount(ym, accum(inv, ym, nominal ? fee : realFee));

            final Function<Investment, MoneyAmount> total = i -> this.asUSD(i.getInvestment().getMoneyAmount(), moment);
            totalValuesSeries.putAmount(ym, accum(inv, ym, total));

            final Function<Investment, MoneyAmount> taxes = i -> this.tax(i, invested, total);

            taxesValuesSeries.putAmount(ym, accum(inv, ym, taxes));

            ym = ym.next();
        }

        return Map.of(
                "invested", investmentSeries,
                "fees", feeSeries,
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
                .orElse(Inflation.USD_INFLATION.getTo());

        Function<Investment, String> classifier = i -> i.getType().toString() + " " + i.getCurrency();

        Predicate<Investment> filterPredicate = i -> Objects.isNull(type) || i.getType().toString().equals(type);
        Comparator<Investment> comparator = Comparator.comparing(Investment::getInitialDate, Comparator.naturalOrder());

        final var list = this.getAllInvestments();

        new Evolution<Investment>(this.console, this.bar)
                .evo(totalFunction, startFunction, endFunction, classifier, filterPredicate, comparator, list, pct);

    }
    
    private List<Investment> getAllInvestments(){
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
                .orElse(Inflation.USD_INFLATION.getTo());

        final var categories = Map.ofEntries(
                Map.entry("CSPX", "Global Eq."),
                Map.entry("EIMI", "Global Eq."),
                Map.entry("MEUD", "Global Eq."),
                Map.entry("XRSU", "Global Eq."),
                Map.entry("XAU", "Com."),
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
