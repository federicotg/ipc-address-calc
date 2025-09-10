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
import com.diogonunes.jcolor.Attribute;
import java.io.IOException;
import java.math.BigDecimal;
import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.ONE;
import java.text.MessageFormat;
import static java.text.MessageFormat.format;
import java.text.NumberFormat;
import java.util.Comparator;
import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.reducing;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.fede.calculator.money.Currency;
import org.fede.calculator.money.ForeignExchanges;
import org.fede.calculator.money.Inflation;
import org.fede.calculator.money.MoneyAmount;
import static org.fede.calculator.money.Currency.ARS;
import static org.fede.calculator.money.Currency.IWDA;
import static org.fede.calculator.money.Currency.USD;
import static org.fede.calculator.money.Currency.UVA;
import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.InvestmentAsset;
import org.fede.calculator.money.series.YearMonth;
import static org.fede.calculator.money.MathConstants.C;
import org.fede.calculator.chart.PieChart;
import org.fede.calculator.chart.PieItem;
import org.fede.calculator.money.series.InvestmentEvent;
import static org.fede.calculator.money.series.InvestmentType.ETF;
import static org.fede.calculator.money.series.InvestmentType.FCI;
import static org.fede.calculator.money.series.InvestmentType.PF;
import static org.fede.calculator.money.series.InvestmentType.BONO;

import org.fede.calculator.money.series.SeriesReader;
import org.fede.util.Pair;
import static org.fede.util.Pair.of;

/**
 *
 * @author fede
 */
public class Positions {

    private final String AVERAGE_KEY = "Avg.";

    private final Console console;
    private final Format format;
    private final Series series;

    public Positions(Console console, Format format, Series series) {
        this.console = console;
        this.format = format;
        this.series = series;
    }

    public void positions(boolean nominal) {

        final var now = new Date();
        this.console.appendLine(this.format.title("Positions Without Fees"));

        final var descWidth = 36;
        final var posWidth = 4;
        final var lastWidth = 10;
        final var costWidth = 14;
        final var costPct = 9;
        final var mkvWidth = 14;
        final var mkvPctWidth = 8;
        final var avgWidth = 10;
        final var pnlWidth = 14;
        final var pnlPctWidth = 9;

        final var separator = IntStream.rangeClosed(0, costPct + descWidth + posWidth + lastWidth + costWidth + mkvWidth + mkvPctWidth + avgWidth + pnlWidth + pnlPctWidth)
                .mapToObj(n -> "=")
                .collect(joining());

        final var fmt = " {0}{1}{2}{3}{4}{5}{6}{7}{8}{9}";

        this.console.appendLine(MessageFormat.format(fmt,
                this.format.text("       Fund", descWidth),
                this.format.text(" Pos.", posWidth),
                this.format.text("    Last", lastWidth),
                this.format.text("   Cost Basis", costWidth),
                this.format.text("    %", costPct),
                this.format.text(" Market Value", mkvWidth),
                this.format.text("   % ", mkvPctWidth),
                this.format.text("Avg. Price", avgWidth),
                this.format.text("       P&L", pnlWidth),
                this.format.text("    %", pnlPctWidth)));

        this.console.appendLine(separator);

        final var positions = this.series.getInvestments()
                .stream()
                .filter(investment -> investment.isCurrent(now))
                .filter(Investment::isETF)
                .map(inv -> ForeignExchanges.exchange(inv, USD))
                .map(inv -> nominal ? inv : Inflation.USD_INFLATION.real(inv))
                .collect(groupingBy(Investment::getCurrency))
                .values()
                .stream()
                .map(this::position)
                .toList();

        final var nominalPositions = this.series.getInvestments()
                .stream()
                .filter(investment -> investment.isCurrent(now))
                .filter(Investment::isETF)
                .map(inv -> ForeignExchanges.exchange(inv, USD))
                .collect(groupingBy(Investment::getCurrency))
                .values()
                .stream()
                .map(this::position)
                .toList();

        final var totalMarketValue = positions
                .stream()
                .map(Position::getMarketValue)
                .reduce(MoneyAmount.zero(USD), MoneyAmount::add);

        final var totalCostBasis = this.costBasis(positions);

        final var totalPnL = this.unrealizedPnL(positions);

        positions
                .stream()
                .sorted(comparing((Position p) -> p.getMarketValue().getAmount(), reverseOrder()))
                .map(p -> MessageFormat.format(
                fmt,
                this.format.text(p.getFundName(), descWidth),
                String.format("%" + posWidth + "d", p.getPosition().intValue()),
                this.format.currency(p.getLast().getAmount(), lastWidth),
                this.format.currency(p.getCostBasis().getAmount(), costWidth),
                this.format.percent(p.getCostBasis().getAmount().divide(totalCostBasis.getAmount(), C), costPct),
                this.format.currency(p.getMarketValue().getAmount(), mkvWidth),
                this.format.percent(p.getMarketValue().getAmount().divide(totalMarketValue.getAmount(), C), mkvPctWidth),
                this.format.currency(p.getAveragePrice().getAmount(), avgWidth),
                this.format.currencyPL(p.getUnrealizedPnL().getAmount(), pnlWidth),
                this.format.percent(p.getUnrealizedPnL().getAmount().divide(p.getCostBasis().getAmount(), C), pnlPctWidth)))
                .forEach(this.console::appendLine);

        this.console.appendLine(MessageFormat.format(fmt,
                this.format.text("Unrealized", descWidth),
                this.format.text("", posWidth),
                this.format.text("", lastWidth),
                this.format.currency(totalCostBasis.getAmount(), costWidth),
                this.format.text("", costPct),
                this.format.currency(totalMarketValue.getAmount(), mkvWidth),
                this.format.text("", mkvPctWidth),
                this.format.text("", avgWidth),
                this.format.currencyPL(totalPnL.getAmount(), pnlWidth),
                this.format.percent(totalPnL.getAmount().divide(totalCostBasis.getAmount(), C), pnlPctWidth)));

        var iva = SeriesReader.readPercent("iva").add(ONE);

        final var realizedList = this.series.getInvestments()
                .stream()
                .filter(Investment::isETF)
                .filter(investment -> investment.getOut() != null)
                .map(inv -> ForeignExchanges.exchange(inv, USD))
                .map(inv -> nominal ? inv : Inflation.USD_INFLATION.real(inv))
                .map(i
                        -> new BoughtSold(
                        i.getInitialMoneyAmount()
                                .add(i.getIn().getFeeMoneyAmount()
                                        .adjust(ONE, i.getComment() == null
                                                ? iva
                                                : ONE)),
                        i.getOut().getMoneyAmount()))
                .toList();

        final var realized = realizedList
                .stream()
                .map(BoughtSold::capitalGain)
                .reduce(MoneyAmount.zero(USD), MoneyAmount::add);

        final var realizedInvested = realizedList.stream()
                .map(BoughtSold::bought)
                .map(MoneyAmount::amount)
                .reduce(ZERO, BigDecimal::add);

        final var gainPct = realized.getAmount().divide(realizedInvested, C);

        this.console.appendLine(MessageFormat.format(" Realized {0} {1}", this.format.currencyPL(
                realized.amount(), 110), this.format.percent(gainPct, 8)));

        this.console.appendLine(MessageFormat.format(" Total {0}", this.format.currencyPL(
                realized.add(totalPnL).getAmount(), 113)));

        this.console.appendLine(format.subtitle("EGR"));

        this.egrReportLine(" Average", nominalPositions);

        final var n = 10;

        final var oldestNPositions = this.series.getInvestments()
                .stream()
                .filter(investment -> investment.isCurrent(now))
                .filter(Investment::isETF)
                .sorted(Comparator.comparing(Investment::getInitialDate))
                .limit(n)
                .map(inv -> ForeignExchanges.exchange(inv, USD))
                .collect(groupingBy(Investment::getCurrency))
                .values()
                .stream()
                .map(this::position)
                .toList();

        this.egrReportLine(" Oldest " + n, oldestNPositions);

        final var oldestNPositionsByCurrency = this.series.getInvestments()
                .stream()
                .filter(investment -> investment.isCurrent(now))
                .filter(Investment::isETF)
                .map(inv -> ForeignExchanges.exchange(inv, USD))
                .collect(groupingBy(Investment::getCurrency));

        oldestNPositionsByCurrency.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e
                        -> this.egrReportLine(
                        " Oldest " + n + " " + e.getKey().name(),
                        List.of(this.position(e.getValue()
                                .stream()
                                .sorted(Comparator.comparing(Investment::getInitialDate))
                                .limit(n).toList()
                        ))));
    }

    private void egrReportLine(String title, List<Position> positions) {
        final var egr = this.egr(positions);
        final var tax = egr.multiply(SeriesReader.readPercent("capitalGainsTaxRate"));

        this.console.appendLine(
                format.text(title + " EGR", 20),
                this.format.percent(egr, 6),
                format.text(" Tax", 5),
                this.format.percent(tax, 6),
                ". Saving on $2.5k/mo: ",
                format.currency(new BigDecimal("2500").multiply(tax, C), 9));
    }

    // EGR = ratio de ganancia embebida
    private BigDecimal egr(List<Position> positions) {
        return unrealizedPnL(positions).getAmount()
                .divide(costBasis(positions).amount(), C);
    }

    private MoneyAmount unrealizedPnL(List<Position> positions) {
        return positions
                .stream()
                .map(Position::getUnrealizedPnL)
                .reduce(MoneyAmount.zero(USD), MoneyAmount::add);
    }

    private MoneyAmount costBasis(List<Position> positions) {

        return positions
                .stream()
                .map(Position::getCostBasis)
                .reduce(MoneyAmount.zero(USD), MoneyAmount::add);
    }

    private record BoughtSold(MoneyAmount bought, MoneyAmount sold) {

        public MoneyAmount capitalGain() {
            return this.sold.subtract(bought);
        }
    }

    public void dca(boolean nominal, String type) {

        final Map<String, Function<Investment, String>> groupings = Map.of(
                "h", i -> YearMonth.of(i.getInitialDate()).half(),
                "y", i -> Integer.toString(YearMonth.of(i.getInitialDate()).getYear()),
                "m", i -> YearMonth.of(i.getInitialDate()).monthString(),
                "q", i -> YearMonth.of(i.getInitialDate()).quarter());

        this.console.appendLine(this.format.title((nominal ? "Nominal" : "Real") + " Dollar Cost Average"));
        var now = new Date();
        final var classifier = groupings.get(type);
        this.dca(nominal, classifier, now);

    }

    public void invested(boolean nominal, String type, String group) {
        this.console.appendLine(this.format.subtitle("Inv"));
        this.netInvested(nominal, type, group);
    }

    private void dca(boolean nominal, Function<Investment, String> groupingFunction, Date now) {

        final var averagesByGroup = this.positionsBy(
                this.series.getInvestments(),
                (i) -> AVERAGE_KEY,
                nominal,
                now);

        final var positionByGroup = this.positionsBy(
                this.series.getInvestments(),
                groupingFunction,
                nominal,
                now);

        this.console.appendLine(this.format.text("", 8),
                positionByGroup.keySet().stream()
                        .map(CurrencyAndGroupKey::currency)
                        .distinct()
                        .sorted()
                        .map(currency -> this.format.center(currency.name(), 9))
                        .collect(joining()));

        positionByGroup.keySet().stream()
                .map(CurrencyAndGroupKey::groupKey)
                .distinct()
                .sorted()
                .map(year -> this.avgPrice(year, positionByGroup, averagesByGroup))
                .forEach(this.console::appendLine);

        averagesByGroup.keySet().stream()
                .map(CurrencyAndGroupKey::groupKey)
                .distinct()
                .map(year -> this.avgPrice(year, averagesByGroup, averagesByGroup))
                .forEach(this.console::appendLine);

        this.console.appendLine(this.format.text("Curr.", 8),
                Investments.ETF_NAME.keySet()
                        .stream()
                        .filter(c -> c != IWDA)
                        .sorted()
                        .map(this::currentPice)
                        .collect(joining()));
    }

    private void netInvested(boolean nominal, String type, String group) {

        final Map<String, Function<CashFlow, String>> groupings = Map.of(
                "h", i -> YearMonth.of(i.date()).half(),
                "y", i -> Integer.toString(YearMonth.of(i.date()).getYear()),
                "m", i -> YearMonth.of(i.date()).monthString(),
                "q", i -> YearMonth.of(i.date()).quarter(),
                "all", i -> ""
        );

        Predicate<Investment> filter = switch (type) {
            case "long" ->
                this::isLong;
            case "etf" ->
                (Investment i) -> i.getType() == ETF;
            case "fci" ->
                (Investment i) -> i.getType() == FCI;
            case "pf" ->
                (Investment i) -> i.getType() == PF;
            case "pfusd" ->
                (Investment i) -> i.getType() == PF && i.getCurrency() == USD;
            case "pfars" ->
                (Investment i) -> i.getType() == PF && i.getCurrency() == ARS;
            case "all" ->
                i -> true;
            default ->
                (Investment i) -> i.getCurrency().name().equals(type);
        };

        final Function<CashFlow, String> groupingFunction = groupings.get(group);

        final Map<String, BigDecimal> grouped = this.series.getInvestments()
                .stream()
                .filter(filter)
                .map(inv -> ForeignExchanges.exchange(inv, USD))
                .map(inv -> nominal ? inv : Inflation.USD_INFLATION.real(inv))
                .flatMap(i
                        -> i.getOut() == null
                ? Stream.of(new CashFlow(i.getIn().getDate(), i.getIn().getAmount()))
                : Stream.of(
                        new CashFlow(i.getOut().getDate(), i.getOut().getAmount().negate()),
                        new CashFlow(i.getIn().getDate(), i.getIn().getAmount())
                ))
                .collect(Collectors.groupingBy(
                        groupingFunction,
                        Collectors.mapping(CashFlow::amount,
                                Collectors.reducing(ZERO, BigDecimal::add)
                        )));

        grouped.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> this.console.appendLine(
                e.getKey() + " " + this.format.currencyPL(e.getValue(), 20)
        ));
    }

    private boolean isLong(Investment i) {
        return i.getType() == ETF
                || i.getType() == FCI
                || i.getType() == BONO
                || i.getCurrency() == UVA
                || (i.getType() == PF && i.getCurrency() == USD);
    }

    private record CashFlow(Date date, BigDecimal amount) {

    }

    private MoneyAmount current(Currency currency) {
        final var ma = new MoneyAmount(ONE, currency);
        return ForeignExchanges.getMoneyAmountForeignExchange(ma.getCurrency(), USD).apply(ma, Inflation.USD_INFLATION.getTo());
    }

    private String currentPice(Currency currency) {
        return Ansi.colorize(this.format.currency(this.current(currency).getAmount(), 9), Attribute.WHITE_TEXT());
    }

    private Map<CurrencyAndGroupKey, Position> positionsBy(List<Investment> investments, Function<Investment, String> groupingFunction, boolean nominal, Date now) {
        return investments
                .stream()
                .filter(investment -> investment.isCurrent(now))
                .filter(Investment::isETF)
                .map(inv -> ForeignExchanges.exchange(inv, USD))
                .map(inv -> nominal ? inv : Inflation.USD_INFLATION.real(inv))
                .collect(groupingBy(i -> new CurrencyAndGroupKey(i.getCurrency(), groupingFunction.apply(i))))
                .entrySet()
                .stream()
                .collect(toMap(e -> e.getKey(), e -> this.position(e.getValue())));
    }

    private String avgPrice(String year, Map<CurrencyAndGroupKey, Position> positionsByGroup, Map<CurrencyAndGroupKey, Position> averagesByGroup) {
        return Stream.concat(
                Stream.of(this.format.text(String.valueOf(year), 8)),
                positionsByGroup.keySet().stream()
                        .map(CurrencyAndGroupKey::currency)
                        .distinct()
                        .sorted()
                        .map(currency -> new CurrencyAndGroupKey(currency, year))
                        .map(key -> this.avgPrice(positionsByGroup, key, averagesByGroup)))
                .collect(joining());
    }

    private String avgPrice(Map<CurrencyAndGroupKey, Position> positionsByGroup, CurrencyAndGroupKey key, Map<CurrencyAndGroupKey, Position> averagesByGroup) {
        return Optional.ofNullable(positionsByGroup.get(key))
                .map(Position::getAveragePrice)
                .map(MoneyAmount::getAmount)
                .map(avgPrice -> this.colorized(avgPrice, averagesByGroup.get(new CurrencyAndGroupKey(key.currency(), AVERAGE_KEY)).getAveragePrice().getAmount(), this.current(key.currency()).getAmount()))
                .orElseGet(() -> this.format.text("", 9));
    }

    private String colorized(BigDecimal avgPrice, BigDecimal globalAverage, BigDecimal current) {
        Attribute color;
        if (avgPrice.compareTo(globalAverage) >= 0 && avgPrice.compareTo(current) >= 0) {
            color = Attribute.RED_TEXT();
        } else if (avgPrice.compareTo(globalAverage) > 0 || avgPrice.compareTo(current) > 0) {
            color = Attribute.YELLOW_TEXT();
        } else {
            color = Attribute.GREEN_TEXT();
        }

        return Ansi.colorize(
                this.format.currency(avgPrice, 9),
                color);
    }

    private Position position(List<Investment> investments) {
        final var symbol = investments.stream().findAny().get().getCurrency();

        final var position = investments.stream()
                .map(Investment::getInvestment)
                .map(InvestmentAsset::getAmount)
                .reduce(ZERO, BigDecimal::add);

        final var now = YearMonth.of(new Date());

        return new Position(
                Investments.ETF_NAME.get(symbol),
                position,
                ForeignExchanges.getMoneyAmountForeignExchange(symbol, USD)
                        .apply(new MoneyAmount(ONE, symbol), now),
                investments.stream()
                        .map(i -> i.getIn().getMoneyAmount())
                        .reduce(MoneyAmount.zero(USD), MoneyAmount::add),
                ForeignExchanges.getMoneyAmountForeignExchange(symbol, USD)
                        .apply(investments.stream()
                                .map(Investment::getMoneyAmount)
                                .reduce(MoneyAmount.zero(symbol), MoneyAmount::add),
                                now),
                new MoneyAmount(
                        investments.stream()
                                .map(Investment::getIn)
                                .map(InvestmentEvent::getAmount)
                                .reduce(ZERO, BigDecimal::add)
                                .divide(position, C),
                        USD));
    }

    public void portfolioChartSeries(String subtype) throws IOException {

        var from = YearMonth.of(2001, 10);
        var to = YearMonth.of(new Date());

        var chart = new PieChart(false, true);

        for (var y = from; y.compareTo(to) <= 0; y = y.next()) {
            this.portfolioChart(chart, subtype, y.year(), y.month());
        }
    }

    public void portfolioChartByGeography(PieChart chart, String type, int year, int month) throws IOException {
        final var ym = YearMonth.of(year, month);

        final var us = new PieItem(
                "USA",
                this.lastUSDAmount("ahorros-cspx", ym)
                        .add(this.lastUSDAmount("ahorros-rtwo", ym))
                        .add(this.lastUSDAmount("ahorros-xrsu", ym))
                        .amount());

        final var exUS = new PieItem(
                "MSCI World ex USA",
                this.lastUSDAmount("ahorros-xuse", ym)
                        .add(this.lastUSDAmount("ahorros-meud", ym)).amount());

        final var em = new PieItem("MSCI Emerging Markets IMI", this.lastUSDAmount("ahorros-eimi", ym).amount());

        final var cash = new PieItem(
                "Cash",
                this.lastUSDAmount("ahorros-dolar-banco", ym)
                        .add(this.lastUSDAmount("ahorros-dolar-liq", ym))
                        .add(this.lastUSDAmount("ahorros-peso", ym))
                        .add(this.lastUSDAmount("ahorros-euro", ym))
                        .amount());

        final var pctFormat = NumberFormat.getPercentInstance(Locale.of("es", "AR"));
        pctFormat.setMinimumFractionDigits(2);

        chart
                .create(
                        MessageFormat.format("Portfolio {0}", ym.monthString()),
                        List.of(cash, us, exUS, em),
                        MessageFormat.format("portfolio-geo-{1}-{0}", ym.monthString(), type));

    }

    public void portfolioChartByGeographyBreakUSA(PieChart chart, String type, int year, int month) throws IOException {
        final var ym = YearMonth.of(year, month);

        final var sp500 = new PieItem(
                "S&P 500",
                this.lastUSDAmount("ahorros-cspx", ym)
                        .amount());

        final var r2k = new PieItem(
                "Russell 2000",
                this.lastUSDAmount("ahorros-rtwo", ym)
                        .add(this.lastUSDAmount("ahorros-xrsu", ym))
                        .amount()
        );

        final var exUS = new PieItem(
                "MSCI World ex USA",
                this.lastUSDAmount("ahorros-xuse", ym)
                        .add(this.lastUSDAmount("ahorros-meud", ym)).amount());

        final var em = new PieItem("MSCI Emerging Markets IMI", this.lastUSDAmount("ahorros-eimi", ym).amount());

        final var cash = new PieItem(
                "Cash",
                this.lastUSDAmount("ahorros-dolar-banco", ym)
                        .add(this.lastUSDAmount("ahorros-dolar-liq", ym))
                        .add(this.lastUSDAmount("ahorros-peso", ym))
                        .add(this.lastUSDAmount("ahorros-euro", ym))
                        .amount());

        final var pctFormat = NumberFormat.getPercentInstance(Locale.of("es", "AR"));
        pctFormat.setMinimumFractionDigits(2);

        chart
                .create(
                        MessageFormat.format("Portfolio {0}", ym.monthString()),
                        List.of(cash, sp500, r2k, exUS, em),
                        MessageFormat.format("portfolio-geo-break-{1}-{0}", ym.monthString(), type));

    }

    private MoneyAmount lastUSDAmount(String seriesName, YearMonth ym) {
        var amount = this.lastAmount(seriesName, ym).get();
        return ForeignExchanges.getMoneyAmountForeignExchange(amount.getCurrency(), USD)
                .apply(amount, ym);
    }

    public void portfolioChart(PieChart chart, String subtype, int year, int month) throws IOException {
        final var ym = YearMonth.of(year, month);
        final var items = this.portfolioItems(subtype, year, month);

        if (items.stream().map(PortfolioItem::getAmount).anyMatch(Predicate.not(MoneyAmount::isZero))) {

            final var pctFormat = NumberFormat.getPercentInstance(Locale.of("es", "AR"));
            pctFormat.setMinimumFractionDigits(2);

            chart
                    .create(
                            MessageFormat.format("Portfolio {1} {0}", ym.monthString(), subtype),
                            items.stream()
                                    .map(item -> new PieItem(
                                    Investments.ETF_NAME.getOrDefault(item.getAmount().getCurrency(), item.getAmount().getCurrency().name()),
                                    item.getDollarAmount().amount())).toList(),
                            MessageFormat.format("portfolio-{1}-{0}.png", ym.monthString(), subtype)
                    );
        }

    }

    public void portfolio(String type, String subtype, int year, int month) {

        final var items = this.portfolioItems(subtype, year, month);

        final var total = items.stream()
                .map(PortfolioItem::getDollarAmount)
                .reduce(MoneyAmount.zero(USD), MoneyAmount::add);

        final var pct = "pct".equals(type);

        items.stream()
                .map(i -> pct ? i.asPercentReport(total) : i.asReport(total))
                .forEach(this.console::appendLine);

        if (!pct) {
            this.console.appendLine("--------------------------------------");
            this.console.appendLine(format("Total {0}", this.format.currency(total.getAmount())));
        }
    }

    private List<PortfolioItem> portfolioItems(String subtype, int year, int month) {
        final var ym = YearMonth.of(year, month);

        final Map<String, Map<String, Optional<MoneyAmount>>> grouped
                = Stream.of(
                        of("BOND", this.lastAmount("ahorros-ay24", ym)),
                        of("BOND", this.lastAmount("ahorros-conbala", ym)),
                        of("BOND", this.lastAmount("ahorros-uva", ym)),
                        of("BOND", this.lastAmount("ahorros-dolar-ON", ym)),
                        of("BOND", this.lastAmount("ahorros-lecap", ym)),
                        of("BOND", this.lastAmount("ahorros-lete", ym)),
                        of("BOND", this.lastAmount("ahorros-dolar-pf", ym)),
                        of("BOND", this.lastAmount("ahorros-caplusa", ym)),
                        of("CASH", this.lastAmount("ahorros-dolar-banco", ym)),
                        of("CASH", this.lastAmount("ahorros-peso", ym)),
                        of("CASH", this.lastAmount("ahorros-dolar-liq", ym)),
                        of("CASH", this.lastAmount("ahorros-euro", ym)),
                        of("CASH", this.lastAmount("ahorros-dai", ym)),
                        of("EQUITY", this.lastAmount("ahorros-cspx", ym)),
                        of("EQUITY", this.lastAmount("ahorros-eimi", ym)),
                        of("EQUITY", this.lastAmount("ahorros-rtwo", ym)),
                        of("EQUITY", this.lastAmount("ahorros-meud", ym)),
                        of("EQUITY", this.lastAmount("ahorros-conaafa", ym)),
                        of("EQUITY", this.lastAmount("ahorros-xrsu", ym)),
                        of("EQUITY", this.lastAmount("ahorros-xuse", ym)))
                        .filter(p -> "all".equals(subtype) || p.first().equalsIgnoreCase(subtype))
                        .collect(groupingBy(
                                Pair::first,
                                groupingBy(
                                        p -> p.second().get().getCurrency().name(),
                                        mapping(
                                                p -> p.second().get(),
                                                reducing(MoneyAmount::add)))));

        return grouped
                .entrySet()
                .stream()
                .flatMap(e -> this.item(e.getKey(), e.getValue(), ym))
                .sorted(comparing((PortfolioItem::getDollarAmount), comparing(MoneyAmount::getAmount)).reversed())
                .toList();

    }

    private Stream<PortfolioItem> item(String type, Map<String, Optional<MoneyAmount>> amounts, YearMonth ym) {

        return amounts.values()
                .stream()
                .flatMap(Optional::stream)
                .filter(Predicate.not(MoneyAmount::isZero))
                .map(amount -> new PortfolioItem(amount, type, ym));
    }

    private Supplier<MoneyAmount> lastAmount(String seriesName, YearMonth ym) {
        return () -> SeriesReader.readSeries("saving/".concat(seriesName).concat(".json")).getAmountOrElseZero(ym);
    }

    private record CurrencyAndGroupKey(Currency currency, String groupKey) {

    }

}
