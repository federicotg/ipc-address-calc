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
import static java.text.MessageFormat.format;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import static java.util.Comparator.comparing;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.toList;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import static org.fede.calculator.money.ForeignExchanges.getMoneyAmountForeignExchange;
import static org.fede.calculator.money.Inflation.USD_INFLATION;
import static org.fede.calculator.money.MathConstants.CONTEXT;
import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.InvestmentEvent;
import org.fede.calculator.money.series.YearMonth;
import org.fede.util.Pair;

/**
 *
 * @author federicogentile
 */
public class PortfolioReturns {

    private final Series series;
    private final Console console;
    private final Format format;
    private final Bar bar;

    public PortfolioReturns(Series series, Console console, Format format, Bar bar) {
        this.series = series;
        this.console = console;
        this.format = format;
        this.bar = bar;
    }

    private static LocalDate min(LocalDate d1, LocalDate d2) {
        return d1.compareTo(d2) <= 0
                ? d1
                : d2;
    }

    private static LocalDate max(LocalDate d1, LocalDate d2) {
        return d1.compareTo(d2) >= 0
                ? d1
                : d2;
    }

    private boolean after(Date d, int year, Month m, int day) {
        return LocalDate.ofInstant(d.toInstant(), ZoneId.systemDefault()).isAfter(LocalDate.of(year, m, day));
    }

    public Map<Integer, Pair<BigDecimal, BigDecimal>> mdrByYear() {

        final Predicate<Investment> since2002 = i -> after(i.getInitialDate(), 2002, Month.JANUARY, 1);
        final var inv = this.series.getInvestments()
                .stream()
                .filter(since2002)
                .collect(toList());

        final var from = inv.stream()
                .map(Investment::getInitialDate)
                .map(Date::toInstant)
                .map(i -> LocalDate.ofInstant(i, ZoneId.systemDefault()))
                .reduce(PortfolioReturns::min)
                .get();

        final var to = inv.stream()
                .map(i -> Optional.ofNullable(i.getOut()).map(InvestmentEvent::getDate).map(Date::toInstant).orElseGet(Instant::now))
                .map(i -> LocalDate.ofInstant(i, ZoneId.systemDefault()))
                .reduce(PortfolioReturns::max)
                .get();

        return this.mdrByYear(inv, from, to, false);
    }

    public void modifiedDietzReturn(Predicate<Investment> criteria, boolean nominal) {

        final var inv = this.series.getInvestments()
                .stream()
                .filter(criteria)
                .collect(toList());

        final var modifiedDietzReturn = new ModifiedDietzReturn(
                inv,
                "USD",
                nominal)
                .get();

        final var from = inv.stream()
                .map(Investment::getInitialDate)
                .map(Date::toInstant)
                .map(i -> LocalDate.ofInstant(i, ZoneId.systemDefault()))
                .reduce(PortfolioReturns::min)
                .get();

        final var to = inv.stream()
                .map(i -> Optional.ofNullable(i.getOut()).map(InvestmentEvent::getDate).map(Date::toInstant).orElseGet(Instant::now))
                .map(i -> LocalDate.ofInstant(i, ZoneId.systemDefault()))
                .reduce(PortfolioReturns::max)
                .get();

        this.console.appendLine("");
        this.console.appendLine(format("From {0} to {1}: {2}. Annualized: {3}", DateTimeFormatter.ISO_LOCAL_DATE.format(from), DateTimeFormatter.ISO_LOCAL_DATE.format(to), this.format.percent(modifiedDietzReturn.getFirst()), this.format.percent(modifiedDietzReturn.getSecond())));

        final Function<Map.Entry<Integer, Pair<BigDecimal, BigDecimal>>, String> lineFunction
                = (p) -> format("{0} {1} {2}",
                        this.format.text(String.valueOf(p.getKey()), 10),
                        this.format.percent(p.getValue().getFirst(), 8),
                        this.bar.pctBar(p.getValue().getSecond()));

        this.mdrByYear(inv, from, to, nominal)
                .entrySet()
                .stream()
                .sorted(comparing(Map.Entry::getKey))
                .map(lineFunction)
                .forEach(this.console::appendLine);

    }

    private Map<Integer, Pair<BigDecimal, BigDecimal>> mdrByYear(List<Investment> inv, LocalDate from, LocalDate to, boolean nominal) {

        return IntStream.rangeClosed(from.getYear(), to.getYear())
                .boxed()
                .collect(Collectors.toMap(
                        year -> year,
                        year -> new ModifiedDietzReturn(inv, "USD", nominal, LocalDate.of(year, Month.JANUARY, 1), LocalDate.of(year, Month.DECEMBER, 31)).get()));
    }

    public void returns(boolean nominal) {

        final Predicate<Investment> since2002 = i -> after(i.getInitialDate(), 2002, Month.JANUARY, 1);

        this.modifiedDietzReturn(since2002, nominal);

    }

    private DayDollars dayDollarsInYear(int year, Investment i) {

        final var yearStart = LocalDate.of(year, Month.JANUARY, 1);
        final var yearEnd = LocalDate.of(year, Month.DECEMBER, 31);

        final var investmentStart = LocalDate.ofInstant(i.getIn().getDate().toInstant(), ZoneId.systemDefault());

        final var investmentEnd = Optional.ofNullable(i.getOut())
                .map(InvestmentEvent::getDate)
                .map(Date::toInstant)
                .map(instant -> LocalDate.ofInstant(instant, ZoneId.systemDefault()))
                .orElse(LocalDate.now());

        final var to = min(yearEnd, investmentEnd);

        final var daysInvestedInYear = ChronoUnit.DAYS.between(
                max(yearStart, investmentStart),
                to.plusDays(1));

        final var usdInvested = getMoneyAmountForeignExchange(i.getCurrency(), "USD")
                .apply(i.getMoneyAmount(), YearMonth.of(to.getYear(), to.getMonthValue()));

        return new DayDollars(
                year,
                i.getType(),
                i.getCurrency(),
                usdInvested.getAmount().multiply(BigDecimal.valueOf(daysInvestedInYear), CONTEXT));

    }

    private Stream<DayDollars> asDayDollarsByYear(Investment i) {

        return IntStream.rangeClosed(
                YearMonth.of(i.getIn().getDate()).getYear(),
                Optional.ofNullable(i.getOut())
                        .map(InvestmentEvent::getDate)
                        .map(YearMonth::of)
                        .map(YearMonth::getYear)
                        .orElse(USD_INFLATION.getTo().getYear()))
                .mapToObj(year -> this.dayDollarsInYear(year, i));

    }

    public void portfolioAllocation() {

        Map<String, Map<String, Optional<DayDollars>>> dayDollarsByYear = this.series.getInvestments()
                .stream()
                .flatMap(this::asDayDollarsByYear)
                .collect(groupingBy(
                        DayDollars::getYear,
                        groupingBy(DayDollars::getType, reducing(DayDollars::combine))));

        final var mdrByYear = this.mdrByYear();

        dayDollarsByYear.entrySet()
                .stream()
                .sorted(comparing(Map.Entry::getKey))
                .forEach(e -> this.allocationYear(e.getKey(), e.getValue(), mdrByYear));

    }

    private void allocationYear(String year, Map<String, Optional<DayDollars>> byType, Map<Integer, Pair<BigDecimal, BigDecimal>> mdrByYear) {
        this.console.appendLine("Year: ", year);

        final var total = byType.values()
                .stream()
                .flatMap(Optional::stream)
                .map(DayDollars::getAmount)
                .reduce(ZERO, BigDecimal::add);

        byType.values()
                .stream()
                .flatMap(Optional::stream)
                .sorted(comparing(DayDollars::getAmount).reversed())
                .map(d -> format("\t{0} {1}",
                String.format("%-11s", d.getType()),
                this.bar.pctBar(d.getAmount(), total)))
                .forEach(this.console::appendLine);

        Optional.ofNullable(mdrByYear.get(Integer.parseInt(year)))
                .map(Pair::getFirst)
                .map(this.format::percent)
                .map(pct -> format("Modified Dietz Return {0}\n", pct))
                .ifPresent(this.console::appendLine);

    }

}
