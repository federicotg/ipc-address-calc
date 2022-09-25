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

import java.math.BigDecimal;
import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.ONE;
import java.text.MessageFormat;
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
import org.fede.calculator.money.series.InvestmentType;
import org.fede.calculator.money.series.YearMonth;
import static org.fede.calculator.money.MathConstants.C;
import org.fede.calculator.money.series.InvestmentEvent;
import org.fede.util.Pair;

/**
 *
 * @author fede
 */
public class Positions {

    private static final MoneyAmount ZERO_USD = new MoneyAmount(ZERO.setScale(6, MathConstants.RM), "USD");

    private static final Map<String, String> ETF_NAME = Map.of(
            "CSPX", "iShares Core S&P 500",
            "EIMI", "iShares Core MSCI EM IMI",
            "XRSU", "Xtrackers Russell 2000",
            "RTWO", "L&G Russell 2000 Small Cap Quality",
            "MEUD", "Lyxor Core STOXX Europe 600 DR"
    );

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

    public void positions(String symbol, boolean nominal) {

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
                .filter(inv -> inv.getType().equals(InvestmentType.ETF))
                .filter(inv -> symbol == null || inv.getCurrency().equals(symbol))
                .map(inv -> ForeignExchanges.exchange(inv, "USD"))
                .map(inv -> nominal ? inv : Inflation.USD_INFLATION.real(inv))
                .collect(groupingBy(Investment::getCurrency))
                .values()
                .stream()
                .map(this::position)
                .collect(toList());

        final var positionsByYear = this.series.getInvestments()
                .stream()
                .filter(Investment::isCurrent)
                .filter(inv -> inv.getType().equals(InvestmentType.ETF))
                .filter(inv -> symbol == null || inv.getCurrency().equals(symbol))
                .map(inv -> ForeignExchanges.exchange(inv, "USD"))
                .map(inv -> nominal ? inv : Inflation.USD_INFLATION.real(inv))
                .collect(groupingBy(i -> Pair.of(i.getCurrency(), YearMonth.of(i.getInitialDate()).getYear())))
                .entrySet()
                .stream()
                .collect(toMap(e -> e.getKey(), e -> this.position(e.getValue())));

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

        this.console.appendLine(this.format.subtitle("Average Prices"));

        this.console.appendLine(this.format.text("", 9),
                positionsByYear.keySet().stream()
                        .map(Pair::getFirst)
                        .distinct()
                        .sorted()
                        .map(currency -> this.format.text(currency, 9))
                        .collect(joining()));

        positionsByYear.keySet().stream()
                .mapToInt(Pair::getSecond)
                .distinct()
                .sorted()
                .mapToObj(year -> this.avgPrice(year, positionsByYear))
                .forEach(this.console::appendLine);

        this.costs(symbol, nominal);

    }

    private String avgPrice(int year, Map<Pair<String, Integer>, Position> positionsByYear) {
        return Stream.concat(
                Stream.of(this.format.text(String.valueOf(year), 5)),
                positionsByYear.keySet().stream()
                        .map(Pair::getFirst)
                        .distinct()
                        .sorted()
                        .map(currency -> Pair.of(currency, year))
                        .map(key -> this.avgPrice(positionsByYear, key)))
                .collect(joining());

    }

    private String avgPrice(Map<Pair<String, Integer>, Position> positionsByYear, Pair<String, Integer> key) {
        return Optional.ofNullable(positionsByYear.get(key))
                .map(Position::getAveragePrice)
                .map(MoneyAmount::getAmount)
                .map(avgPrice -> this.format.currency(avgPrice, 9))
                .orElseGet(() -> this.format.text("", 9));
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

    public void costs(String symbol, boolean nominal) {

        this.console.appendLine(this.format.subtitle("Costs"));

        final Function<Investment, String> yearClassifier = i -> String.valueOf(YearMonth.of(i.getInitialDate()).getYear());
        final Function<Investment, String> brokerClassifier = i -> i.getComment() == null ? "PPI " : "IBKR";
        final Function<Investment, String> anyClassifier = i -> "All ";
        final Function<Investment, String> etfClassifier = Investment::getCurrency;
        final Function<Investment, String> currencyClassifier = i -> "gettex".equals(i.getComment()) ? "EUR" : "USD";

        this.cost(yearClassifier, symbol, nominal);
        this.cost(brokerClassifier, symbol, nominal);
        this.cost(etfClassifier, symbol, nominal);
        this.cost(currencyClassifier, symbol, nominal);
        this.cost(this::exchangeClassifier, symbol, nominal);
        this.cost(anyClassifier, symbol, nominal);

    }

    private void cost(Function<Investment, String> classifier, String symbol, boolean nominal) {
        final var inv = this.by(symbol, nominal, classifier, Investment::getInitialMoneyAmount);
        final var cost = this.by(symbol, nominal, classifier, Investment::getCost);

        inv
                .keySet()
                .stream()
                .sorted()
                .forEach(e -> this.costReport(e, inv, cost));

        this.console.appendLine("");
    }

    private void costReport(String label, Map<String, MoneyAmount> m1, Map<String, MoneyAmount> m2) {
        this.console.appendLine(label,
                this.format.currency(m1.get(label).getAmount(), 13),
                this.format.currency(m2.get(label).getAmount(), 13),
                this.format.percent(m2.get(label).getAmount().divide(m1.get(label).getAmount(), C), 8));
    }

    private Map<String, MoneyAmount> by(String symbol, boolean nominal, Function<Investment, String> classifier, Function<Investment, MoneyAmount> func) {
        return this.series.getInvestments()
                .stream()
                .filter(Investment::isCurrent)
                .filter(inv -> inv.getType().equals(InvestmentType.ETF))
                .filter(inv -> symbol == null || inv.getCurrency().equals(symbol))
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
