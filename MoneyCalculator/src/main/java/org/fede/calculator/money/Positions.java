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
import java.util.function.Function;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.joining;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.InvestmentAsset;
import org.fede.calculator.money.series.YearMonth;
import static org.fede.calculator.money.MathConstants.C;
import org.fede.calculator.money.series.InvestmentEvent;
import org.fede.util.Pair;

/**
 *
 * @author fede
 */
public class Positions {
    
    private static final ZoneId SYSTEM_DEFAULT_ZONE_ID = ZoneId.systemDefault();

    private static final MoneyAmount ZERO_USD = new MoneyAmount(ZERO.setScale(6, MathConstants.RM), "USD");

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

    private final Console console;
    private final Format format;
    private final Series series;
    private final boolean withFee;

    public Positions(Console console, Format format, Series series, boolean withFee) {
        this.console = console;
        this.format = format;
        this.series = series;
        this.withFee = withFee;
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

        final var separator = IntStream.rangeClosed(0, IntStream.of(costPct, descWidth, posWidth, lastWidth, costWidth, mkvWidth, mkvPctWidth, avgWidth, pnlWidth, pnlPctWidth).sum())
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
        this.costs(nominal);
        this.annualCost(nominal);
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
                (i) -> "*",
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
    }

    private Map<Pair<String, String>, Position> positionsBy(List<Investment> investments, Function<Investment, String> groupingFucntion, boolean nominal) {
        return investments
                .stream()
                .filter(Investment::isCurrent)
                .filter(Investment::isETF)
                .map(inv -> ForeignExchanges.exchange(inv, "USD"))
                .map(inv -> nominal ? inv : Inflation.USD_INFLATION.real(inv))
                .collect(groupingBy(i -> Pair.of(i.getCurrency(), groupingFucntion.apply(i))))
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
                .map(avgPrice -> this.colorized(avgPrice, averagesByGroup.get(Pair.of(key.getFirst(), "*")).getAveragePrice().getAmount()))
                .orElseGet(() -> this.format.text("", 9));
    }

    private String colorized(BigDecimal avgPrice, BigDecimal globalAverage) {
        return Ansi.colorize(this.format.currency(avgPrice, 9),
                avgPrice.compareTo(globalAverage) >= 0
                ? Attribute.RED_TEXT()
                : Attribute.GREEN_TEXT());
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
                                .reduce(new MoneyAmount(ZERO, symbol), MoneyAmount::add),
                                now),
                new MoneyAmount(
                        investments.stream()
                                .map(Investment::getIn)
                                .map(InvestmentEvent::getAmount)
                                .reduce(ZERO, BigDecimal::add)
                                .divide(position, C),
                        "USD"));
    }

}
