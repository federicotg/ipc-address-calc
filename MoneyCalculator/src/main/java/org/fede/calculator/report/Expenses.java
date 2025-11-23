/*
 * Copyright (C) 2023 federicogentile
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

import com.diogonunes.jcolor.Attribute;
import java.math.BigDecimal;
import static java.math.BigDecimal.ZERO;
import java.text.MessageFormat;
import static java.text.MessageFormat.format;
import java.util.Comparator;
import static java.util.Comparator.comparing;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;
import org.fede.calculator.money.Currency;
import org.fede.calculator.money.MoneyAmount;
import static org.fede.calculator.money.Inflation.USD_INFLATION;
import org.fede.calculator.money.series.MoneyAmountSeries;
import java.time.YearMonth;
import java.util.function.BiConsumer;
import org.fede.calculator.money.SlidingWindow;
import org.fede.calculator.money.series.SeriesReader;
import org.fede.calculator.money.series.SortedMapMoneyAmountSeries;
import org.fede.calculator.money.series.YearMonthUtil;

/**
 *
 * @author federicogentile
 */
public class Expenses {

    private final Series series;
    private final Console console;
    private final Bar bar;
    private final Format format;

    public Expenses(Series series, Console console, Bar bar, Format format) {
        this.series = series;
        this.console = console;
        this.bar = bar;
        this.format = format;
    }

    public void expensesChange(int months) {

        this.console.appendLine(this.format.title("Expenses Change"));

        this.bar.evolution(format("{0}-month average expenses change", months),
                new SlidingWindow(1)
                        .change(new SlidingWindow(months)
                                .average(this.series.realExpenses(null))), 3);
    }

    public void expenses(Map<String, String> params) {

        Runnable otherwise = () -> {

            final String exp = params.get("type");
            final int months = Integer.parseInt(params.getOrDefault("m", "12"));

            this.console.appendLine(this.format.title(format("Real USD expenses in the last {0} months", months)));

            final List<TypeAndAmount> list = this.series.getRealUSDExpensesByType()
                    .entrySet()
                    .stream()
                    .filter(p -> exp == null || exp.equals(p.getKey()))
                    .map(e -> new TypeAndAmount(e.getKey(), this.aggregate(e.getValue(), s -> this.lastMonths(s, months)).amount()))
                    .toList();

            final var total = list.stream()
                    .map(TypeAndAmount::amount)
                    .reduce(ZERO, BigDecimal::add);

            list.stream()
                    .sorted(comparing(TypeAndAmount::amount, Comparator.reverseOrder()))
                    .map(e -> format("{0}{1}{2}{3}",
                    this.format.text(e.type(), 13),
                    this.format.text(" USD ", 4),
                    this.format.currency(e.amount(), 10),
                    this.bar.pctBar(e.amount(), total)))
                    .forEach(this.console::appendLine);

            this.console.appendLine(format("-----------------------------\n{0} USD {1}",
                    this.format.text("Total", 5),
                    this.format.currency(total, 10)));

            //this.otherCashSpending();
        };

        new By().by(
                params, 
                this::quarterExpenses, 
                this::halfExpenses, 
                this::yearlyExpenses,
                this::monthlyExpenses,
                otherwise);
    }

    private MoneyAmount aggregate(List<MoneyAmountSeries> mas, Function<MoneyAmountSeries, MoneyAmount> aggregation) {
        return mas.stream()
                .map(aggregation)
                .reduce(MoneyAmount.zero(Currency.USD), MoneyAmount::add);
    }

    private MoneyAmount lastMonths(MoneyAmountSeries s, int months) {

        var ym = USD_INFLATION.getTo();
        var amount = MoneyAmount.zero(Currency.USD);

        for (var i = 0; i < months; i++) {
            amount = amount.add(s.getAmountOrElseZero(ym));
            ym = ym.plusMonths(-1);
        }

        return amount;

    }

    private void quarterExpenses() {
        new Group(console, format, bar)
                .group("Quarterly expenses", this.series.realExpense(), null, YearMonthUtil::quarter, 3);
    }

    private void monthlyExpenses() {
        new Group(console, format, bar)
                .group("Monthly expenses", this.series.realExpense(), null, YearMonthUtil::monthString, 1);
    }

    private void yearlyExpenses() {
        new Group(console, format, bar)
                .group("Yearly expenses", this.series.realExpense(), null, ym -> String.valueOf(ym.getYear()), 12);
    }

    private void halfExpenses() {
        new Group(console, format, bar)
                .group("Half expenses", this.series.realExpense(), null, YearMonthUtil::half, 6);
    }

    public void expenseBySource(int months) {
        this.expenseBySource(months, this.series.getRealUSDExpensesByType());

    }

    private void expenseBySource(int months, Map<String, List<MoneyAmountSeries>> source) {

        final var title = format("Average {0}-month expenses by source", months);

        final var colorList = List.of(
                Attribute.BLUE_BACK(),
                Attribute.RED_BACK(),
                Attribute.YELLOW_BACK(),
                Attribute.GREEN_BACK());
        this.console.appendLine(this.format.title(title));

        final var agg = new SlidingWindow(months);

        final var seriesGroups = source;

        final var ss = seriesGroups.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getValue().stream().reduce(MoneyAmountSeries::add).get())
                .map(agg::average)
                .toList();

        final var labels = seriesGroups.entrySet().stream()
                .map(Map.Entry::getKey)
                .sorted()
                .toList();

        final var oldestSeries = ss.stream().min(Comparator.comparing(MoneyAmountSeries::getFrom)).get();

        final var scale = switch (months) {
            case 1 ->
                80;
            case 2 ->
                60;
            case 3 ->
                40;
            default ->
                20;
        };

        oldestSeries.map((ym, ma) -> MoneyAmount.zero(Currency.USD).max(ma))
                .forEach((ym, savingMa) -> this.console.appendLine(this.bar.genericBar(ym, this.independenSeries(ym, ss, colorList), scale)));

        new References(console, format).refs(title, labels, colorList);

    }

    private List<AmountAndColor> independenSeries(YearMonth ym, List<MoneyAmountSeries> series, List<Attribute> colors) {

        return IntStream.range(0, series.size())
                .mapToObj(i -> new AmountAndColor(MoneyAmount.zero(Currency.USD).max(series.get(i).getAmountOrElseZero(ym)), colors.get(i)))
                .toList();
    }

    private void otherCashSpending() {

        this.console.appendLine(this.format.subtitle("Untracked Credit Card Spending"));

        final var taxRate = new BigDecimal("0.012");

        var totalCreditCardSpending = new SortedMapMoneyAmountSeries(Currency.USD, "Credit Card");
        SeriesReader.readSeries("expense/sellos.json")
                .exchangeInto(Currency.USD)
                .forEach((ym, ma) -> totalCreditCardSpending.putAmount(ym, ma.adjust(taxRate, BigDecimal.ONE)));

        // sumar gastos conocidos que se hacen con la tarjeta
        final var seriesStart = totalCreditCardSpending.getFrom();

        // sumar gastos cash conocidos
        var cardUnknowSpending = this.shiftMonth(
                real(totalCreditCardSpending)
                        .subtract(this.realKnownCCSpending(seriesStart)));

        cardUnknowSpending.forEachNonZero((ym, ma) -> this.console.appendLine(
                MessageFormat.format(
                        "{0} {1}",
                        ym, this.format.currency(ma, 20))));

    }

    private MoneyAmountSeries shiftMonth(MoneyAmountSeries s) {
        var shifted = new SortedMapMoneyAmountSeries(Currency.USD, "Shifted " + s.getName());
        s.forEach((ym, ma) -> shifted.putAmount(ym.plusMonths(-1), ma));
        return shifted;
    }

    private MoneyAmountSeries realKnownCCSpending(YearMonth seriesStart) {
        var knownCCSpending = new SortedMapMoneyAmountSeries(Currency.USD, "Known Credit Card");
        
        
        SeriesReader.readSeries("expense/other-usd.json")
                .add(SeriesReader.readSeries("expense/cablevision.json"))
                .add(SeriesReader.readSeries("expense/comida-disc.json"))
                .add(SeriesReader.readSeries("expense/gas.json"))
                .add(SeriesReader.readSeries("expense/luz.json"))
                .add(SeriesReader.readSeries("expense/terceros.json"))// terceros
                .add(SeriesReader.readSeries("expense/municipal-43.json"))
                .add(SeriesReader.readSeries("expense/other.json"))
                .add(SeriesReader.readSeries("expense/reparaciones.json"))
                .add(SeriesReader.readSeries("expense/sellos.json"))
                .add(SeriesReader.readSeries("expense/telefono-43.json"))
                .add(SeriesReader.readSeries("expense/viajes.json"))
                .add(SeriesReader.readSeries("expense/viajes-usd.json"))
                .add(SeriesReader.readSeries("expense/suscripciones-ars.json"))
                .add(SeriesReader.readSeries("expense/suscripciones-usd.json"))
                .exchangeInto(Currency.USD)
                .forEach(this.from(knownCCSpending, seriesStart));
        return this.real(knownCCSpending);
    }

    private BiConsumer<YearMonth, MoneyAmount> from(MoneyAmountSeries filtered, YearMonth from) {
        return (ym, ma) -> filtered
                .putAmount(
                        ym,
                        ym.isBefore(from)
                        ? MoneyAmount.zero(Currency.USD)
                        : ma);
    }

    private MoneyAmountSeries real(MoneyAmountSeries s) {
        return USD_INFLATION.adjust(s, USD_INFLATION.getTo());
    }
}
