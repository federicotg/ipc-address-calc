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
import java.util.ArrayList;
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
import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.InvestmentEvent;
import org.fede.calculator.money.series.SeriesReader;
import org.fede.calculator.money.series.YearMonth;
import static org.fede.calculator.money.MathConstants.C;

/**
 *
 * @author fede
 */
public class PortfolioReturns {

    private final Series series;
    private final Console console;
    private final Format format;
    private final Bar bar;
    private final CashInvestmentBuilder cashInvestments;

    public PortfolioReturns(Series series, Console console, Format format, Bar bar) {
        this.series = series;
        this.console = console;
        this.format = format;
        this.bar = bar;
        this.cashInvestments = new CashInvestmentBuilder(() ->
                SeriesReader.readSeries("/saving/ahorros-dolar-liq.json")
                        .add(SeriesReader.readSeries("/saving/ahorros-dolar-banco.json").exchangeInto("USD"))
                        .add(SeriesReader.readSeries("/saving/ahorros-dai.json").exchangeInto("USD"))
                        .add(SeriesReader.readSeries("/saving/ahorros-euro.json").exchangeInto("USD")));
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

    private Map<Integer, ModifiedDietzReturnResult> mdrByYear(Function<ModifiedDietzReturn, ModifiedDietzReturnResult> returnTypeFunction) {

        final Predicate<Investment> sinceYear = i -> after(i.getInitialDate(), 1999, Month.JANUARY, 1);
        final var inv = Stream.concat(
                this.cashInvestments.cashInvestments().stream(),
                this.series.getInvestments().stream())
                .filter(sinceYear)
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

        return this.mdrByYear(inv, from, to, false, returnTypeFunction);
    }

    public void modifiedDietzReturn(Predicate<Investment> criteria, boolean nominal, boolean withCash, Function<ModifiedDietzReturn, ModifiedDietzReturnResult> returnTypeFunction) {

        final var inv = Stream.concat(
                withCash ? this.cashInvestments.cashInvestments().stream() : Stream.empty(),
                this.series.getInvestments().stream())
                .filter(criteria)
                .collect(toList());

        final var modifiedDietzReturn = returnTypeFunction.apply(new ModifiedDietzReturn(
                inv,
                "USD",
                nominal));

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

        List<BigDecimal> monthyMDR = new ArrayList<>(60);
        for (var ym = YearMonth.of(from); ym.compareTo(YearMonth.of(to)) < 0; ym = ym.next()) {

            var next = ym.next();

            final var st = LocalDate.ofInstant(ym.asToDate().toInstant(), ZoneId.systemDefault()).plusDays(1);

            final var fn = LocalDate.ofInstant(next.asToDate().toInstant(), ZoneId.systemDefault());

            monthyMDR.add(
                    returnTypeFunction.apply(
                            new ModifiedDietzReturn(
                                    inv,
                                    "USD",
                                    nominal,
                                    st,
                                    fn)).getMoneyWeighted());

        }

        this.console.appendLine(
                format(
                        "From {0} to {1}.",
                        DateTimeFormatter.ISO_LOCAL_DATE.format(from),
                        DateTimeFormatter.ISO_LOCAL_DATE.format(to)));

        this.console.appendLine(
                format(
                        "\tReturn: {0}. Annualized {1}.",
                        this.format.percent(modifiedDietzReturn.getMoneyWeighted()),
                        this.format.percent(modifiedDietzReturn.getAnnualizedMoneyWeighted())));

        final Function<Map.Entry<Integer, ModifiedDietzReturnResult>, String> lineFunction
                = (p) -> format("{0} {1} {2}",
                        this.format.text(String.valueOf(p.getKey()), 10),
                        this.format.percent(p.getValue().getMoneyWeighted(), 8),
                        this.bar.pctBar(p.getValue().getAnnualizedMoneyWeighted()));

        this.console.appendLine("");
        this.console.appendLine(this.format.text(" ", 10), this.format.text(" Return", 10), this.format.text("    Annualized", 8));

        this.mdrByYear(inv, from, to, nominal, returnTypeFunction)
                .entrySet()
                .stream()
                .sorted(comparing(Map.Entry::getKey))
                .map(lineFunction)
                .forEach(this.console::appendLine);

    }

    private Map<Integer, ModifiedDietzReturnResult> mdrByYear(List<Investment> inv, LocalDate from, LocalDate to, boolean nominal, Function<ModifiedDietzReturn, ModifiedDietzReturnResult> returnTypeFunction) {

        return IntStream.rangeClosed(from.getYear(), to.getYear())
                .boxed()
                .collect(Collectors.toMap(
                        year -> year,
                        year -> returnTypeFunction.apply(new ModifiedDietzReturn(inv, "USD", nominal, LocalDate.of(year, Month.JANUARY, 1), LocalDate.of(year, Month.DECEMBER, 31)))));
    }

    public void returns(boolean nominal, boolean withCash, int startYear, boolean timeWeighted) {

        this.console.appendLine(this.format.title((nominal ? "Nominal " : "Real ") + (timeWeighted ? "Time Weighted " : "Money Weighted ") + "Returns" + (withCash ? "" : " Without Cash")));

        final Predicate<Investment> sinceYear = i -> after(i.getInitialDate(), startYear, Month.JANUARY, 1);

        this.modifiedDietzReturn(sinceYear, nominal, withCash, timeWeighted 
                ? ModifiedDietzReturn::monthlyLinked 
                : ModifiedDietzReturn::get);

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
                usdInvested.getAmount().multiply(BigDecimal.valueOf(daysInvestedInYear), C));

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

    public void portfolioAllocation(boolean timeWeighted) {

        this.console.appendLine(this.format.title((timeWeighted ? "Time Weighted " : "Money Weighted ") + "Return"));

        final var mdrByYear = this.mdrByYear(timeWeighted ? ModifiedDietzReturn::monthlyLinked : ModifiedDietzReturn::get);

        Stream.concat(this.cashInvestments.cashInvestments().stream(), this.series.getInvestments().stream())
                .flatMap(this::asDayDollarsByYear)
                .collect(groupingBy(
                        DayDollars::getYear,
                        groupingBy(DayDollars::getType, reducing(DayDollars::combine))))
                .entrySet()
                .stream()
                .sorted(comparing(Map.Entry::getKey))
                .forEach(e -> this.allocationYear(e.getKey(), e.getValue(), mdrByYear));
    }

    private void allocationYear(String year, Map<String, Optional<DayDollars>> byType, Map<Integer, ModifiedDietzReturnResult> mdrByYear) {
        this.console.appendLine("     ~=", year, "=~");

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
                .map(ModifiedDietzReturnResult::getMoneyWeighted)
                .map(this.format::percent)
                .map(pct -> format("Return {0}\n", pct))
                .ifPresent(this.console::appendLine);

    }

}
