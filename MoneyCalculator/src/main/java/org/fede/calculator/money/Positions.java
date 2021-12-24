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
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.InvestmentAsset;
import org.fede.calculator.money.series.InvestmentType;
import org.fede.calculator.money.series.YearMonth;
import static org.fede.calculator.money.MathConstants.CONTEXT;

/**
 *
 * @author federicogentile
 */
public class Positions {

    private static final MoneyAmount ZERO_USD = new MoneyAmount(ZERO, "USD");

    private static final Map<String, String> ETF_NAME = Map.of(
            "CSPX", "iShares Core S&P 500",
            "EIMI", "iShares Core MSCI EM IMI",
            "XRSU", "Xtrackers Russell 2000",
            "MEUD", "Lyxor Core STOXX Europe 600 DR"
    );

    private final Console console;
    private final Format format;
    private final Series series;

    public Positions(Console console, Format format, Series series) {
        this.console = console;
        this.format = format;
        this.series = series;
    }

    public void positions(String symbol, boolean nominal) {

        final var descWidth = 32;
        final var posWidth = 6;
        final var lastWidth = 14;
        final var costWidth = 17;
        final var mkvWidth = 17;
        final var avgWidth = 15;
        final var pnlWidth = 17;
        final var pnlPctWidth = 10;
        
        var separator = IntStream.rangeClosed(0, IntStream.of(descWidth, posWidth, lastWidth, costWidth, mkvWidth, avgWidth, pnlWidth, pnlPctWidth).sum())
                .mapToObj(n -> "=")
                .collect(Collectors.joining());

        this.console.appendLine(separator);
       
        this.console.appendLine(MessageFormat.format("{0}{1}{2}{3}{4}{5}{6}{7}",
                this.format.text("        Fund", descWidth),
                this.format.text(" Pos.", posWidth),
                this.format.text("      Last", lastWidth),
                this.format.text("      Cost Basis", costWidth),
                this.format.text("    Market Value", mkvWidth),
                this.format.text("     Avg. Price", avgWidth),
                this.format.text("        P&L", pnlWidth),
                this.format.text("    P&L %", pnlPctWidth)));

        this.console.appendLine(separator);

        final var positions = this.series.getInvestments()
                .stream()
                .filter(Investment::isCurrent)
                .filter(inv -> inv.getType().equals(InvestmentType.ETF))
                .filter(inv -> symbol == null || inv.getCurrency().equals(symbol))
                .map(inv -> ForeignExchanges.exchange(inv, "USD"))
                .map(inv -> nominal ? inv : Inflation.USD_INFLATION.real(inv))
                .collect(Collectors.groupingBy(Investment::getCurrency))
                .values()
                .stream()
                .map(this::position)
                .collect(Collectors.toList());

        positions
                .stream()
                .sorted(Comparator.comparing((Position p) -> p.getMarketValue().getAmount(), Comparator.reverseOrder()))
                .map(p -> MessageFormat.format(" {0}{1}{2}{3}{4}{5}{6}{7}",
                this.format.text(p.getFundName(), descWidth),
                this.format.number(p.getPosition(), posWidth),
                this.format.currency(p.getLast(), lastWidth),
                this.format.currency(p.getCostBasis(), costWidth),
                this.format.currency(p.getMarketValue(), mkvWidth),
                this.format.currency(p.getAveragePrice(), avgWidth),
                this.format.currency(p.getUnrealizedPnL(), pnlWidth),
                this.format.percent(p.getUnrealizedPnLPct(), pnlPctWidth)))
                .forEach(this.console::appendLine);

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

        this.console.appendLine(separator);
        this.console.appendLine(
                MessageFormat.format("{0}{1}{2}{3}{4}{5}{6}{7}",
                        this.format.text("Total", descWidth),
                        this.format.text("", posWidth),
                        this.format.text("", lastWidth),
                        this.format.currency(totalCostBasis, costWidth),
                        this.format.currency(totalMarketValue, mkvWidth),
                        this.format.text("", avgWidth),
                        this.format.currency(totalPnL, pnlWidth),
                        this.format.percent(totalPnL.getAmount().divide(totalCostBasis.getAmount(), CONTEXT), pnlPctWidth)));
    }

    private Position position(List<Investment> investments) {
        final var symbol = investments.stream().findAny().get().getCurrency();

        final var position = investments.stream()
                .map(Investment::getInvestment)
                .map(InvestmentAsset::getAmount)
                .reduce(ZERO, BigDecimal::add);

        return new Position(
                ETF_NAME.get(symbol),
                position,
                ForeignExchanges.getMoneyAmountForeignExchange(symbol, "USD").apply(new MoneyAmount(ONE, symbol), YearMonth.of(new Date())),
                new MoneyAmount(
                        investments.stream()
                                .map(Investment::getIn)
                                .map(ie -> ie.getAmount().add(ie.getFee(), CONTEXT).add(ie.getTransferFee(), CONTEXT))
                                .reduce(ZERO, BigDecimal::add),
                        "USD"),
                ForeignExchanges.getMoneyAmountForeignExchange(symbol, "USD")
                        .apply(new MoneyAmount(
                                investments.stream()
                                        .map(Investment::getInvestment)
                                        .map(InvestmentAsset::getAmount)
                                        .reduce(ZERO, BigDecimal::add),
                                symbol),
                                YearMonth.of(new Date())),
                new MoneyAmount(
                        investments.stream()
                                .map(i -> this.price(i).multiply(i.getInvestment().getAmount(), CONTEXT).divide(position, CONTEXT))
                                .reduce(ZERO, BigDecimal::add),
                        "USD"));

    }

    private BigDecimal price(Investment i) {
        return i.getIn()
                .getAmount()
                .add(i.getIn().getFee(), CONTEXT)
                .add(i.getIn().getTransferFee(), CONTEXT)
                .divide(i.getInvestment().getAmount(), CONTEXT);
    }
}
