/*
 * Copyright (C) 2026 fede
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

import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.fede.calculator.money.ForeignExchanges;
import org.fede.calculator.money.Inflation;
import org.fede.calculator.money.MoneyAmount;
import static org.fede.calculator.money.Currency.USD;
import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.InvestmentType;
import java.time.YearMonth;

/**
 * Real USD P&amp;L of every investment lot: realized (closed) and unrealized
 * (open), without double-counting rollovers.
 *
 * @author fede
 */
public class InvestmentPnL {

    private static final MoneyAmount ZERO_USD = MoneyAmount.zero(USD);

    private final Console console;
    private final Format format;
    private final Series series;

    public InvestmentPnL(Console console, Format format, Series series) {
        this.console = console;
        this.format = format;
        this.series = series;
    }

    public void report() {
        final var now = LocalDate.now();
        final var items = this.series.getInvestments()
                .stream()
                .map(inv -> this.toItem(inv, now))
                .toList();

        final var realized = sum(items, true);
        final var unrealized = sum(items, false);
        final var total = realized.add(unrealized);

        this.console.appendLine(this.format.title("Real USD Investment P&L"));
        this.console.appendLine(MessageFormat.format(
                " Global {0}  (realized {1}  unrealized {2})",
                this.format.currencyPL(total.amount(), 14),
                this.format.currencyPL(realized.amount(), 14),
                this.format.currencyPL(unrealized.amount(), 14)));

        this.byType(items);
        this.byYear(items);
        this.byTypeAndYear(items);
    }

    private void byType(List<PnLItem> items) {
        this.console.appendLine(this.format.subtitle("By Type"));
        this.printHeader();
        this.printSeparator();

        Arrays.stream(InvestmentType.values())
                .sorted(Comparator.comparing(Enum::name))
                .forEach(type -> {
                    final var realized = sum(items, type, true);
                    final var unrealized = sum(items, type, false);
                    if (!realized.isZero() || !unrealized.isZero()) {
                        this.printRow(type.name(), realized, unrealized);
                    }
                });

        this.printSeparator();
        this.printRow("Total", sum(items, true), sum(items, false));
    }

    private void byYear(List<PnLItem> items) {
        this.console.appendLine(this.format.subtitle("By Year"));
        this.printHeader();
        this.printSeparator();

        items.stream()
                .map(PnLItem::year)
                .distinct()
                .sorted()
                .forEach(year -> this.printRow(
                String.valueOf(year),
                sum(items, year, true),
                sum(items, year, false)));

        this.printSeparator();
        this.printRow("Total", sum(items, true), sum(items, false));
    }

    private void byTypeAndYear(List<PnLItem> items) {
        this.console.appendLine(this.format.subtitle("By Type and Year"));

        final Map<InvestmentType, List<PnLItem>> byType = items.stream()
                .collect(Collectors.groupingBy(PnLItem::type, () -> new EnumMap<>(InvestmentType.class), Collectors.toList()));

        byType.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(Enum::name)))
                .forEach(entry -> {
                    this.console.appendLine("");
                    this.console.appendLine(this.format.text(entry.getKey().name(), 8));
                    this.printHeader();
                    this.printSeparator();

                    entry.getValue()
                            .stream()
                            .map(PnLItem::year)
                            .distinct()
                            .sorted()
                            .forEach(year -> this.printRow(
                            String.valueOf(year),
                            sum(entry.getValue(), year, true),
                            sum(entry.getValue(), year, false)));

                    this.printSeparator();
                    this.printRow(
                            "Total",
                            sum(entry.getValue(), true),
                            sum(entry.getValue(), false));
                });
    }

    private void printHeader() {
        this.console.appendLine(MessageFormat.format("{0}{1}{2}{3}",
                this.format.text(" ", 8),
                this.format.center("Realized", 14),
                this.format.center("Unrealized", 14),
                this.format.center("Total", 14)));
    }

    private void printSeparator() {
        this.console.appendLine(IntStream.range(0, 50)
                .mapToObj(i -> "-")
                .collect(Collectors.joining()));
    }

    private void printRow(String label, MoneyAmount realized, MoneyAmount unrealized) {
        final var total = realized.add(unrealized);
        this.console.appendLine(MessageFormat.format("{0}{1}{2}{3}",
                this.format.text(label, 8),
                this.plOrBlank(realized),
                this.plOrBlank(unrealized),
                this.format.currencyPL(total.amount(), 14)));
    }

    private String plOrBlank(MoneyAmount amount) {
        return amount.isZero()
                ? this.format.text("", 14)
                : this.format.currencyPL(amount.amount(), 14);
    }

    private PnLItem toItem(Investment investment, LocalDate now) {
        final var usd = ForeignExchanges.exchange(investment, USD);
        final var inflation = Inflation.usdInflation();
        final var realIn = inflation.adjust(
                usd.getInitialMoneyAmount(),
                usd.getInitialDate(),
                now);

        final boolean realized = investment.getOut() != null;
        final MoneyAmount realEnd;
        final int year;

        if (realized) {
            realEnd = inflation.adjust(
                    usd.getOut().getMoneyAmount(),
                    usd.getOut().getDate(),
                    now);
            year = usd.getOut().getDate().getYear();
        } else {
            realEnd = this.currentMarketValueUSD(investment);
            year = now.getYear();
        }

        return new PnLItem(
                investment.getType(),
                year,
                realized,
                realEnd.subtract(realIn));
    }

    private MoneyAmount currentMarketValueUSD(Investment investment) {
        final YearMonth to = Inflation.usdInflation().getTo();
        return ForeignExchanges.getMoneyAmountForeignExchange(
                investment.getMoneyAmount().currency(),
                USD)
                .apply(investment.getMoneyAmount(), to);
    }

    private static MoneyAmount sum(List<PnLItem> items, boolean realized) {
        return items.stream()
                .filter(i -> i.realized() == realized)
                .map(PnLItem::amount)
                .reduce(ZERO_USD, MoneyAmount::add);
    }

    private static MoneyAmount sum(List<PnLItem> items, InvestmentType type, boolean realized) {
        return items.stream()
                .filter(i -> i.type() == type)
                .filter(i -> i.realized() == realized)
                .map(PnLItem::amount)
                .reduce(ZERO_USD, MoneyAmount::add);
    }

    private static MoneyAmount sum(List<PnLItem> items, int year, boolean realized) {
        return items.stream()
                .filter(i -> i.year() == year)
                .filter(i -> i.realized() == realized)
                .map(PnLItem::amount)
                .reduce(ZERO_USD, MoneyAmount::add);
    }

    private record PnLItem(InvestmentType type, int year, boolean realized, MoneyAmount amount) {
    }
}
