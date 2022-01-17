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
import java.util.function.Function;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.joining;
import java.util.stream.IntStream;
import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.InvestmentAsset;
import org.fede.calculator.money.series.InvestmentType;
import org.fede.calculator.money.series.YearMonth;
import static org.fede.calculator.money.MathConstants.C;

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
            "MEUD", "Lyxor Core STOXX Europe 600 DR"
    );

    private final Console console;
    private final Format format;
    private final Series series;
    private final Bar bar;

    public Positions(Console console, Format format, Series series, Bar bar) {
        this.console = console;
        this.format = format;
        this.series = series;
        this.bar = bar;
    }

    public void positions(String symbol, boolean nominal) {

        final var descWidth = 32;
        final var posWidth = 4;
        final var lastWidth = 11;
        final var costWidth = 14;
        final var costPct = 9;
        final var mkvWidth = 14;
        final var avgWidth = 12;
        final var pnlWidth = 14;
        final var pnlPctWidth = 9;

        final var separator = IntStream.rangeClosed(0, IntStream.of(costPct,descWidth, posWidth, lastWidth, costWidth, mkvWidth, avgWidth, pnlWidth, pnlPctWidth).sum())
                .mapToObj(n -> "=")
                .collect(joining());

        final var fmt = " {0}{1}{2}{3}{4}{5}{6}{7}{8}";

        this.console.appendLine(MessageFormat.format(fmt,
                this.format.text("       Fund", descWidth),
                this.format.text(" Pos.", posWidth),
                this.format.text("     Last", lastWidth),
                this.format.text("   Cost Basis", costWidth),
                this.format.text("    %", costPct),
                this.format.text(" Market Value", mkvWidth),
                this.format.text("  Avg. Price", avgWidth),
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
                .map(p -> MessageFormat.format(fmt,
                this.format.text(p.getFundName(), descWidth),
                String.format("%" + posWidth + "d", p.getPosition().intValue()),
                this.format.currency(p.getLast().getAmount(), lastWidth),
                this.format.currency(p.getCostBasis().getAmount(), costWidth),
                this.format.percent(p.getCostBasis().getAmount().divide(totalCostBasis.getAmount(), C), costPct),
                this.format.currency(p.getMarketValue().getAmount(), mkvWidth),
                this.format.currency(p.getAveragePrice().getAmount(), avgWidth),
                this.format.currency(p.getUnrealizedPnL().getAmount(), pnlWidth),
                this.format.percent(p.getUnrealizedPnL().getAmount().divide(totalPnL.getAmount(), C), pnlPctWidth)))
                .forEach(this.console::appendLine);

        this.console.appendLine(separator);
        this.console.appendLine(MessageFormat.format(fmt,
                this.format.text("Total", descWidth),
                this.format.text("", posWidth),
                this.format.text("", lastWidth),
                this.format.currency(totalCostBasis.getAmount(), costWidth),
                this.format.text("", costPct),
                this.format.currency(totalMarketValue.getAmount(), mkvWidth),
                this.format.text("", avgWidth),
                this.format.currency(totalPnL.getAmount(), pnlWidth),
                this.format.text("", pnlPctWidth)));

        this.costs(symbol, nominal);

    }

    public void costs(String symbol, boolean nominal) {
        final Function<Investment, MoneyAmount> costFunc = Investment::getCost;

        final Function<Investment, MoneyAmount> totalInvestedFunc = Investment::getInitialMoneyAmount;

        this.console.appendLine(this.format.subtitle("Costs"));

        final Function<Investment, String> yearClassifier = i -> String.valueOf(YearMonth.of(i.getInitialDate()).getYear());
        final Function<Investment, String> brokerClassifier = i -> i.getComment() == null ? "PPI " : "IBKR";
        final Function<Investment, String> anyClassifier = i -> "All ";
        final Function<Investment, String> etfClassifier = Investment::getCurrency;

        final var invByYear = this.by(symbol, nominal, yearClassifier, totalInvestedFunc);
        final var costByYear = this.by(symbol, nominal, yearClassifier, costFunc);

        final var invByBroker = this.by(symbol, nominal, brokerClassifier, totalInvestedFunc);
        final var costByBroker = this.by(symbol, nominal, brokerClassifier, costFunc);

        final var invByAll = this.by(symbol, nominal, anyClassifier, totalInvestedFunc);
        final var costByAll = this.by(symbol, nominal, anyClassifier, costFunc);

        final var invByEtf = this.by(symbol, nominal, etfClassifier, totalInvestedFunc);
        final var costByEtf = this.by(symbol, nominal, etfClassifier, costFunc);

        invByYear
                .keySet()
                .stream()
                .sorted()
                .forEach(e -> this.costReport(e, invByYear, costByYear));

        this.console.appendLine();

        invByBroker
                .keySet()
                .stream()
                .sorted()
                .forEach(e -> this.costReport(e, invByBroker, costByBroker));

        this.console.appendLine();
        invByEtf
                .keySet()
                .stream()
                .sorted()
                .forEach(e -> this.costReport(e, invByEtf, costByEtf));

        this.console.appendLine();
        invByAll
                .keySet()
                .stream()
                .sorted()
                .forEach(e -> this.costReport(e, invByAll, costByAll));

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
                        .map(i -> i.getIn().getMoneyAmount().add(i.getCost()))
                        .reduce(ZERO_USD, MoneyAmount::add),
                ForeignExchanges.getMoneyAmountForeignExchange(symbol, "USD")
                        .apply(investments.stream()
                                .map(Investment::getMoneyAmount)
                                .reduce(new MoneyAmount(ZERO, symbol), MoneyAmount::add),
                                now),
                new MoneyAmount(
                        investments.stream()
                                .map(i -> this.price(i).multiply(i.getInvestment().getAmount(), C).divide(position, C))
                                .reduce(ZERO, BigDecimal::add),
                        "USD"));
    }

    private BigDecimal price(Investment i) {
        return i.getIn().getMoneyAmount().add(i.getCost()).getAmount()
                .divide(i.getInvestment().getAmount(), C);
    }
}
