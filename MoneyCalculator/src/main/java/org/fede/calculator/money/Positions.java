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
import com.diogonunes.jcolor.Attribute;
import java.math.BigDecimal;
import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.ONE;
import java.text.MessageFormat;
import static java.text.MessageFormat.format;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.toList;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import static org.fede.calculator.money.ForeignExchanges.getMoneyAmountForeignExchange;
import static org.fede.calculator.money.Inflation.USD_INFLATION;
import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.InvestmentAsset;
import org.fede.calculator.money.series.YearMonth;
import static org.fede.calculator.money.MathConstants.C;
import org.fede.calculator.money.series.InvestmentEvent;
import org.fede.calculator.money.series.InvestmentType;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.SeriesReader;
import org.fede.util.Pair;
import static org.fede.util.Pair.of;

/**
 *
 * @author fede
 */
public class Positions {

    private static final ZoneId SYSTEM_DEFAULT_ZONE_ID = ZoneId.systemDefault();

    private static final MoneyAmount ZERO_USD = MoneyAmount.zero("USD");

    private static final BigDecimal CAPITAL_GAINS_TAX_RATE = new BigDecimal("0.15");

    private static final Map<String, String> ETF_NAME = Map.of(
            "CSPX", "iShares Core S&P 500",
            "EIMI", "iShares Core MSCI EM IMI",
            "XRSU", "Xtrackers Russell 2000",
            "RTWO", "L&G Russell 2000 Small Cap Quality",
            "MEUD", "Lyxor Core STOXX Europe 600 DR"
    );

    private static final Map<String, Function<Investment, String>> GROUPINGS = Map.of(
            "h", i -> YearMonth.of(i.getInitialDate()).half(),
            "y", i -> Integer.toString(YearMonth.of(i.getInitialDate()).getYear()),
            "m", i -> YearMonth.of(i.getInitialDate()).month(),
            "q", i -> YearMonth.of(i.getInitialDate()).quarter());

    private static final String AVERAGE_KEY = "Avg.";

    private static final Map<Integer, Attribute> PNL_COLORS = Map.of(
            1, Attribute.RED_TEXT(),
            0, Attribute.WHITE_TEXT(),
            -1, Attribute.GREEN_TEXT());

    private final Console console;
    private final Format format;
    private final Series series;
    private final Bar bar;
    private final boolean withFee;

    public Positions(Console console, Format format, Series series, Bar bar, boolean withFee) {
        this.console = console;
        this.format = format;
        this.series = series;
        this.withFee = withFee;
        this.bar = bar;
    }

    public void positions(boolean nominal) {

        if (this.withFee) {
            this.console.appendLine(this.format.title("Positions With Fees"));
        } else {
            this.console.appendLine(this.format.title("Positions Without Fees"));
        }

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
                .filter(Investment::isCurrent)
                .filter(Investment::isETF)
                .map(inv -> ForeignExchanges.exchange(inv, "USD"))
                .map(inv -> nominal ? inv : Inflation.USD_INFLATION.real(inv))
                .collect(groupingBy(Investment::getCurrency))
                .values()
                .stream()
                .map(this::position)
                .collect(toList());

        final var totalMarketValue = positions
                .stream()
                .map(Position::getMarketValue)
                .reduce(ZERO_USD, MoneyAmount::add);

        final var totalCostBasis = positions
                .stream()
                .map(Position::getCostBasis)
                .reduce(ZERO_USD, MoneyAmount::add);

        final var totalPnL = positions
                .stream()
                .map(Position::getUnrealizedPnL)
                .reduce(ZERO_USD, MoneyAmount::add);

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

        this.console.appendLine(separator);
        this.console.appendLine(MessageFormat.format(fmt,
                this.format.text("Total", descWidth),
                this.format.text("", posWidth),
                this.format.text("", lastWidth),
                this.format.currency(totalCostBasis.getAmount(), costWidth),
                this.format.text("", costPct),
                this.format.currency(totalMarketValue.getAmount(), mkvWidth),
                this.format.text("", mkvPctWidth),
                this.format.text("", avgWidth),
                this.format.currencyPL(totalPnL.getAmount(), pnlWidth),
                this.format.percent(totalPnL.getAmount().divide(totalCostBasis.getAmount(), C), pnlPctWidth)));

        this.console.appendLine(this.format.subtitle("Costs"));

        final var realizationCost = this.realizationCost();
        final var wealthTax = wealthTax(nominal);
        final var taxes = List.of(
                Pair.of("Realization", realizationCost),
                Pair.of("Wealth Tax", wealthTax),
                Pair.of("Total", wealthTax.add(realizationCost)));

        taxes.stream().forEach(tax -> this.printTaxLine(tax, totalPnL));

        this.console.appendLine(this.format.subtitle("Fees"));
        this.costs(nominal);
        this.annualCost(nominal);
    }

    private void printTaxLine(Pair<String, MoneyAmount> tax, MoneyAmount totalPnL) {
        this.console.appendLine(MessageFormat.format("{0} {1} {2}",
                this.format.text(tax.getFirst(), 13),
                this.format.currency(tax.getSecond().getAmount(), 10),
                this.format.percent(tax.getSecond().getAmount().divide(totalPnL.getAmount(), C))));
    }

    private MoneyAmount realizationCost() {
        return this.series.getInvestments()
                .stream()
                .filter(Investment::isCurrent)
                .filter(Investment::isETF)
                .map(this::unrealizedUSDCapitalGains)
                .reduce(ZERO_USD, MoneyAmount::add);
    }

    private MoneyAmount wealthTax(boolean nominal) {
        final var now = YearMonth.of(LocalDate.now());
        return this.series.getExpense("bbpp", nominal)
                .filter((ym, map) -> ym.compareTo(now) <= 0)
                .reduce(ZERO_USD, MoneyAmount::add);
    }

    private MoneyAmount unrealizedUSDCapitalGains(Investment i) {

        final var initialUSDAmount = Optional.ofNullable(i.getIn().getFx())
                .map(fx -> i.getInitialMoneyAmount().getAmount().multiply(fx, C))
                .map(usd -> new MoneyAmount(usd, "USD"))
                .orElseGet(i::getInitialMoneyAmount);

        final var currentUSDAmount = ForeignExchanges.getMoneyAmountForeignExchange(i.getCurrency(), "USD")
                .apply(i.getInvestment().getMoneyAmount(), Inflation.USD_INFLATION.getTo());

        return currentUSDAmount.subtract(initialUSDAmount)
                .max(ZERO_USD)
                .adjust(ONE, CAPITAL_GAINS_TAX_RATE);

    }

    public void dca(boolean nominal, String type) {

        this.console.appendLine(this.format.title((nominal ? "Nominal" : "Real") + " Dollar Cost Average"));

        final var classifier = GROUPINGS.get(type);
        this.dca(nominal, classifier);

        this.console.appendLine(this.format.subtitle("Costs"));
        this.cost(classifier, nominal);

        this.annualCost(nominal);
    }

    private void dca(boolean nominal, Function<Investment, String> groupingFunction) {

        final var averagesByGroup = this.positionsBy(
                this.series.getInvestments(),
                (i) -> AVERAGE_KEY,
                nominal);

        final var positionByGroup = this.positionsBy(
                this.series.getInvestments(),
                groupingFunction,
                nominal);

        this.console.appendLine(this.format.text("", 11),
                positionByGroup.keySet().stream()
                        .map(Pair::getFirst)
                        .distinct()
                        .sorted()
                        .map(currency -> this.format.text(currency, 9))
                        .collect(joining()));

        positionByGroup.keySet().stream()
                .map(Pair::getSecond)
                .distinct()
                .sorted()
                .map(year -> this.avgPrice(year, positionByGroup, averagesByGroup))
                .forEach(this.console::appendLine);

        averagesByGroup.keySet().stream()
                .map(Pair::getSecond)
                .distinct()
                .map(year -> this.avgPrice(year, averagesByGroup, averagesByGroup))
                .forEach(this.console::appendLine);

        this.console.appendLine(this.format.text("Curr.", 8),
                ETF_NAME.keySet().stream()
                        .sorted()
                        .map(c -> this.currentPice(averagesByGroup, c))
                        .collect(joining()));
    }

    private String currentPice(Map<Pair<String, String>, Position> averagePrices, String currency) {
        final var ma = new MoneyAmount(ONE, currency);
        final var current = ForeignExchanges.getMoneyAmountForeignExchange(ma.getCurrency(), "USD").apply(ma, Inflation.USD_INFLATION.getTo());
        final var average = averagePrices.get(Pair.of(currency, AVERAGE_KEY)).getAveragePrice();
        return this.colorized(current.getAmount(), average.getAmount());
    }

    private Map<Pair<String, String>, Position> positionsBy(List<Investment> investments, Function<Investment, String> groupingFunction, boolean nominal) {
        return investments
                .stream()
                .filter(Investment::isCurrent)
                .filter(Investment::isETF)
                .map(inv -> ForeignExchanges.exchange(inv, "USD"))
                .map(inv -> nominal ? inv : Inflation.USD_INFLATION.real(inv))
                .collect(groupingBy(i -> Pair.of(i.getCurrency(), groupingFunction.apply(i))))
                .entrySet()
                .stream()
                .collect(toMap(e -> e.getKey(), e -> this.position(e.getValue())));
    }

    private String avgPrice(String year, Map<Pair<String, String>, Position> positionsByGroup, Map<Pair<String, String>, Position> averagesByGroup) {
        return Stream.concat(
                Stream.of(this.format.text(String.valueOf(year), 8)),
                positionsByGroup.keySet().stream()
                        .map(Pair::getFirst)
                        .distinct()
                        .sorted()
                        .map(currency -> Pair.of(currency, year))
                        .map(key -> this.avgPrice(positionsByGroup, key, averagesByGroup)))
                .collect(joining());

    }

    private String avgPrice(Map<Pair<String, String>, Position> positionsByGroup, Pair<String, String> key, Map<Pair<String, String>, Position> averagesByGroup) {
        return Optional.ofNullable(positionsByGroup.get(key))
                .map(Position::getAveragePrice)
                .map(MoneyAmount::getAmount)
                .map(avgPrice -> this.colorized(avgPrice, averagesByGroup.get(Pair.of(key.getFirst(), AVERAGE_KEY)).getAveragePrice().getAmount()))
                .orElseGet(() -> this.format.text("", 9));
    }

    private String colorized(BigDecimal avgPrice, BigDecimal globalAverage) {
        return Ansi.colorize(
                this.format.currency(avgPrice, 9),
                PNL_COLORS.get(avgPrice.compareTo(globalAverage)));
    }

    private String exchangeClassifier(Investment i) {
        if (i.getComment() == null) {
            return i.getCurrency().equals("MEUD")
                    ? "Saxo €"
                    : "Saxo $";
        }
        return i.getComment().equals("lse")
                ? "IBKR $"
                : "IBKR €";
    }

    private void costs(boolean nominal) {

        final Function<Investment, String> yearClassifier = i -> String.valueOf(YearMonth.of(i.getInitialDate()).getYear());
        final Function<Investment, String> brokerClassifier = i -> i.getComment() == null ? "PPI " : "IBKR";
        final Function<Investment, String> anyClassifier = i -> "All ";
        final Function<Investment, String> etfClassifier = Investment::getCurrency;
        final Function<Investment, String> currencyClassifier = i -> "gettex".equals(i.getComment()) ? "EUR" : "USD";

        this.cost(yearClassifier, nominal);
        this.cost(brokerClassifier, nominal);
        this.cost(etfClassifier, nominal);
        this.cost(currencyClassifier, nominal);
        this.cost(this::exchangeClassifier, nominal);
        this.cost(anyClassifier, nominal);

    }

    private void cost(Function<Investment, String> classifier, boolean nominal) {

        final var inv = this.by(nominal, classifier, Investment::getInitialMoneyAmount);
        final var cost = this.by(nominal, classifier, Investment::getCost);
        final var totalInv = inv.values().stream().map(MoneyAmount::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        inv
                .keySet()
                .stream()
                .sorted()
                .forEach(e -> this.costReport(e, inv, cost, totalInv));
        this.console.appendLine("");

    }

    private void annualCost(boolean nominal) {

        final Function<Investment, String> any = i -> "all";

        final var totalCost = this.by(nominal, any, Investment::getCost)
                .values()
                .stream()
                .map(MoneyAmount::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        final var totalInv = this.by(nominal, any, Investment::getInitialMoneyAmount)
                .values()
                .stream()
                .map(MoneyAmount::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final var startDate = this.series.getInvestments()
                .stream()
                .filter(Investment::isCurrent)
                .filter(Investment::isETF)
                .map(Investment::getInitialDate)
                .min(Comparator.naturalOrder())
                .map(Date::toInstant)
                .map(i -> LocalDate.ofInstant(i, SYSTEM_DEFAULT_ZONE_ID))
                .get();

        final var days = ChronoUnit.DAYS.between(startDate, LocalDate.now());

        this.console.appendLine(MessageFormat.format("{0} {1}",
                "Cost per year",
                this.format.percent(
                        new BigDecimal("0.00104").add(
                                new BigDecimal(Math.pow(totalCost.divide(totalInv, C).add(ONE, C).doubleValue(), 365.0d / (double) days) - 1.0d))), C));

    }

    private void costReport(String label, Map<String, MoneyAmount> m1, Map<String, MoneyAmount> m2, BigDecimal totalinv) {
        this.console.appendLine(label,
                this.format.currency(m1.get(label).getAmount(), 13),
                this.format.percent(m1.get(label).getAmount().divide(totalinv, C), 9),
                this.format.currency(m2.get(label).getAmount(), 11),
                this.format.percent(m2.get(label).getAmount().divide(m1.get(label).getAmount(), C), 8));
    }

    private Map<String, MoneyAmount> by(boolean nominal, Function<Investment, String> classifier, Function<Investment, MoneyAmount> func) {
        return this.series.getInvestments()
                .stream()
                .filter(Investment::isCurrent)
                .filter(Investment::isETF)
                .map(inv -> ForeignExchanges.exchange(inv, "USD"))
                .map(inv -> nominal ? inv : Inflation.USD_INFLATION.real(inv))
                .collect(groupingBy(classifier,
                        mapping(func, reducing(ZERO_USD, MoneyAmount::add))));
    }

    private Position position(List<Investment> investments) {
        final var symbol = investments.stream().findAny().get().getCurrency();

        final var position = investments.stream()
                .map(Investment::getInvestment)
                .map(InvestmentAsset::getAmount)
                .reduce(ZERO, BigDecimal::add);

        final var now = YearMonth.of(new Date());

        return new Position(
                ETF_NAME.get(symbol),
                position,
                ForeignExchanges.getMoneyAmountForeignExchange(symbol, "USD").apply(new MoneyAmount(ONE, symbol), now),
                investments.stream()
                        .map(i -> i.getIn().getMoneyAmount().add(this.withFee ? i.getCost() : ZERO_USD))
                        .reduce(ZERO_USD, MoneyAmount::add),
                ForeignExchanges.getMoneyAmountForeignExchange(symbol, "USD")
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
                        "USD"));
    }

    public void portfolio(String type, String subtype, int year, int month) {

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
                        of("EQUITY", this.lastAmount("ahorros-cspx", ym)),
                        of("EQUITY", this.lastAmount("ahorros-eimi", ym)),
                        of("EQUITY", this.lastAmount("ahorros-rtwo", ym)),
                        of("EQUITY", this.lastAmount("ahorros-meud", ym)),
                        of("EQUITY", this.lastAmount("ahorros-conaafa", ym)),
                        of("EQUITY", this.lastAmount("ahorros-xrsu", ym)))
                        .filter(p -> "all".equals(subtype) || p.getFirst().equalsIgnoreCase(subtype))
                        .collect(groupingBy(
                                Pair::getFirst,
                                groupingBy(
                                        p -> p.getSecond().get().getCurrency(),
                                        mapping(
                                                p -> p.getSecond().get(),
                                                reducing(MoneyAmount::add)))));

        final var items = grouped
                .entrySet()
                .stream()
                .flatMap(e -> this.item(e.getKey(), e.getValue(), ym))
                .sorted(comparing((PortfolioItem::getDollarAmount), comparing(MoneyAmount::getAmount)).reversed())
                .collect(toList());

        final var total = items.stream()
                .map(PortfolioItem::getDollarAmount)
                .reduce(ZERO_USD, MoneyAmount::add);

        final var pct = "pct".equals(type);

        items.stream()
                .map(i -> pct ? i.asPercentReport(total) : i.asReport(total))
                .forEach(this.console::appendLine);

        if (!pct) {
            this.console.appendLine("--------------------------------------");
            this.console.appendLine(format("Total {0}", this.format.currency(total.getAmount())));
        }
    }

    private Stream<PortfolioItem> item(String type, Map<String, Optional<MoneyAmount>> amounts, YearMonth ym) {

        return amounts.values()
                .stream()
                .flatMap(Optional::stream)
                .filter(ma -> !ma.isZero())
                .map(amount -> new PortfolioItem(amount, type, ym));
    }

    private Supplier<MoneyAmount> lastAmount(String seriesName, YearMonth ym) {
        return () -> SeriesReader.readSeries("saving/".concat(seriesName).concat(".json")).getAmountOrElseZero(ym);
    }

    public void listStockByType() {

        final var reportCurrency = "USD";
        final var limit = USD_INFLATION.getTo();
        final var limitStr = String.valueOf(limit.getMonth()) + "/" + String.valueOf(limit.getYear());

        this.console.appendLine(this.format.title(format("Inversiones Actuales en {0} por tipo. ", limitStr)));

        final Optional<MoneyAmount> total = this.total(Investment::isCurrent, reportCurrency, limit);

        this.series.getInvestments().stream()
                .filter(Investment::isCurrent)
                .collect(groupingBy(
                        this::assetAllocation,
                        mapping(inv -> getMoneyAmountForeignExchange(inv.getInvestment().getCurrency(), reportCurrency).apply(inv.getInvestment().getMoneyAmount(), limit)
                        .getAmount()
                        .setScale(MathConstants.SCALE, MathConstants.RM),
                                reducing(ZERO, BigDecimal::add))))
                .entrySet()
                .stream()
                .map(entry -> this.formatReport(total, new MoneyAmount(entry.getValue(), reportCurrency), entry.getKey()))
                .forEach(this.console::appendLine);

        total.map(t -> format("-----------------------------\n{0} {1}", this.format.text("Total", 5), this.format.currency(t, 16)))
                .ifPresent(this.console::appendLine);
    }

    private String assetAllocation(Investment investment) {
        final Set<String> equities = Set.of("CSPX", "EIMI", "MEUD", "XRSU", "RTWO");
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

    private Optional<MoneyAmount> total(Predicate<Investment> predicate, String reportCurrency, YearMonth limit) {
        return this.series.getInvestments().stream()
                .filter(predicate)
                .map(Investment::getInvestment)
                .map(InvestmentAsset::getMoneyAmount)
                .map(investedAmount -> getMoneyAmountForeignExchange(investedAmount.getCurrency(), reportCurrency).apply(investedAmount, limit))
                .reduce(MoneyAmount::add);
    }

    public void groupedInvestments() {
        final var reportCurrency = "USD";
        final var limit = USD_INFLATION.getTo();

        this.console.appendLine("Inversiones Actuales Agrupadas en ", reportCurrency, " ", String.valueOf(limit.getYear()), "/", String.valueOf(limit.getMonth()));

        final var total = this.total(Investment::isCurrent, reportCurrency, limit);
        this.series.getInvestments().stream()
                .filter(Investment::isCurrent)
                .collect(groupingBy(in -> of(in.getType().toString(), in.getCurrency()),
                        mapping(inv -> inv.getMoneyAmount().getAmount(),
                                reducing(ZERO, BigDecimal::add))))
                .entrySet()
                .stream()
                .map(e -> of(e.getKey(), new MoneyAmount(e.getValue(), e.getKey().getSecond())))
                .map(p -> of(p.getFirst(), this.fx(p, reportCurrency)))
                .sorted((p, q) -> q.getSecond().getAmount().compareTo(p.getSecond().getAmount()))
                .map(pair -> this.formatReport(total, pair.getSecond(), pair.getFirst().getFirst()))
                .forEach(this.console::appendLine);

        total.map(t -> format("-----------------------------\n{0}{1}", this.format.text("Total", 5), this.format.currency(t, 16)))
                .ifPresent(this.console::appendLine);
    }

    private MoneyAmount fx(Pair<Pair<String, String>, MoneyAmount> p, String reportCurrency) {

        return getMoneyAmountForeignExchange(p.getSecond().getCurrency(), reportCurrency).apply(p.getSecond(), USD_INFLATION.getTo());
    }

    private String formatReport(Optional<MoneyAmount> total, MoneyAmount subtotal, String type) {

        return format("{0}{1}{2}",
                this.format.text(type, 5),
                this.format.currency(subtotal, 16),
                this.bar.pctBar(total.map(tot -> subtotal.getAmount().divide(tot.getAmount(), C)).orElse(ZERO)));
    }

}
