/*
 * Copyright (C) 2022 federicogentile
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
import static java.math.BigDecimal.ONE;
import static java.text.MessageFormat.format;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.toMap;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import static org.fede.calculator.money.Inflation.USD_INFLATION;
import static org.fede.calculator.money.MathConstants.C;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.YearMonth;
import org.fede.util.Pair;

/**
 *
 * @author federicogentile
 */
public class Savings {

    private static final MoneyAmount ZERO_USD = MoneyAmount.zero("USD");

    private final Format format;
    private final Series series;
    private final Bar bar;
    private final Console console;

    public Savings(Format format, Series series, Bar bar, Console console) {
        this.format = format;
        this.series = series;
        this.bar = bar;
        this.console = console;
    }

    private int getScale(int months) {
        int scale = 25;
        if (months <= 6) {
            scale = 35;
        } else if (months > 24) {
            scale = 20;
        }
        return scale;
    }

    public void netAvgSavingSpent(int months, String title) {

        this.console.appendLine(this.format.title(title));

        final var agg = new SimpleAggregation(months);
        final var income = agg.average(this.series.realIncome());
        final var netSaving = agg.average(this.series.realNetSavings());
        final var spending = agg.average(this.series.realExpenses(null));

        netSaving.map((ym, ma) -> ZERO_USD.max(ma))
                .map((ym, ma) -> new MoneyAmount(income.getAmountOrElseZero(ym).getAmount().min(ma.getAmount()), ma.getCurrency()))
                .forEach((ym, savingMa) -> this.console.appendLine(this.bar.genericBar(ym, this.series(ym, spending, income, savingMa), this.getScale(months))));

        this.savingsRefs(title);

    }

    public Pair<MoneyAmount, MoneyAmount> averageSpendingAndSaving(int months) {
        final var agg = new SimpleAggregation(months);
        final var income = agg.average(this.series.realIncome()).getAmount(Inflation.USD_INFLATION.getTo());
        final var netSaving = agg.average(this.series.realNetSavings()).getAmount(Inflation.USD_INFLATION.getTo());
        return Pair.of(income.subtract(netSaving), netSaving);
    }

    public void netAvgSavingSpentPct(int months, String title) {

        this.console.appendLine(this.format.title(title));

        final var agg = new SimpleAggregation(months);
        final var income = agg.average(this.series.realIncome());
        final var netSaving = agg.average(this.series.realNetSavings());
        final var spending = agg.average(this.series.realExpenses(null));

        netSaving.map((ym, ma) -> ZERO_USD.max(ma))
                .map((ym, ma) -> new MoneyAmount(income.getAmountOrElseZero(ym).getAmount().min(ma.getAmount()), ma.getCurrency()))
                .forEach((ym, savingMa) -> this.console.appendLine(this.bar.percentBar(ym, this.series(ym, spending, income, savingMa))));
        this.savingsRefs(title);
    }

    public void savingsDistributionEvolution() {

        this.console.appendLine(this.format.title("Savings Distribution Evolution"));

        final var cash = this.series.realSavings("LIQ");
        final var eq = this.series.realSavings("EQ");
        final var bo = this.series.realSavings("BO");

        final var nf = NumberFormat.getCurrencyInstance();

        cash.forEach((ym, cashMa) -> this.console.appendLine(
                this.bar.bar(
                        ym,
                        cashMa.getAmount(),
                        eq.getAmountOrElseZero(ym).getAmount(),
                        bo.getAmountOrElseZero(ym).getAmount(),
                        1500,
                        value -> String.format("%13s", nf.format(value)))));

        this.cashEquityBondsRef("Savings Distribution Evolution");

    }

    private void cashEquityBondsRef(String title) {
        this.refs(
                this.format.title(title),
                List.of("Cash", "equity", "bonds"),
                List.of(Attribute.BLUE_BACK(), Attribute.RED_BACK(), Attribute.YELLOW_BACK()));
    }

    public void savingsDistributionPercentEvolution() {

        this.console.appendLine(this.format.title("Savings Distribution Percent Evolution"));

        final var cash = this.series.realSavings("LIQ");
        final var eq = this.series.realSavings("EQ");
        final var bo = this.series.realSavings("BO");

        cash.forEach((ym, cashMa) -> this.console.appendLine(
                this.bar.percentBar(ym, cashMa, eq.getAmountOrElseZero(ym), bo.getAmountOrElseZero(ym))
        ));

        this.cashEquityBondsRef("Savings Distribution Percent Evolution");
    }

    private void savingsRefs(String title) {

        this.refs(
                title,
                List.of("saved", "spent", "other spending"),
                List.of(Attribute.BLUE_BACK(), Attribute.RED_BACK(), Attribute.YELLOW_BACK()));

    }

    private List<Pair<MoneyAmount, Attribute>> series(YearMonth ym, MoneyAmountSeries spending, MoneyAmountSeries income, MoneyAmount savingMa) {
        return List.of(
                Pair.of(spending.getAmountOrElseZero(ym), Attribute.RED_BACK()),
                Pair.of(ZERO_USD.max(
                        income.getAmountOrElseZero(ym)
                                .subtract(savingMa)
                                .subtract(spending.getAmountOrElseZero(ym))), Attribute.YELLOW_BACK()),
                Pair.of(savingMa, Attribute.BLUE_BACK()));
    }

    public void refs(String title, List<String> labels, List<Attribute> colors) {
        this.console.appendLine(this.format.title(title));
        this.console.appendLine("References:");

        this.console.appendLine(IntStream.range(0, labels.size())
                .mapToObj(i -> Ansi.colorize(" ", colors.get(i)) + ": " + labels.get(i))
                .collect(Collectors.joining(", ", "", ".")));
    }

    private List<Pair<MoneyAmount, Attribute>> independenSeries(YearMonth ym, List<MoneyAmountSeries> series, List<Attribute> colors) {

        return IntStream.range(0, series.size())
                .mapToObj(i -> Pair.of(ZERO_USD.max(series.get(i).getAmountOrElseZero(ym)), colors.get(i)))
                .collect(Collectors.toList());
    }

    public void incomeAverageBySource(int months) {

        final var title = format("Average {0}-month income by source", months);
        final var colorList = List.of(Attribute.BLUE_BACK(), Attribute.RED_BACK(), Attribute.YELLOW_BACK(), Attribute.GREEN_BACK());
        this.console.appendLine(this.format.title(title));

        final var agg = new SimpleAggregation(months);

        final var unlp = agg.average(this.series.incomeSource("unlp"));
        final var lifia = agg.average(this.series.incomeSource("lifia"));
        final var despARS = agg.average(this.series.incomeSource("despegar"));
        final var despUSD = agg.average(this.series.incomeSource("despegar-split"));

        unlp.map((ym, ma) -> ZERO_USD.max(ma))
                .forEach((ym, savingMa) -> this.console.appendLine(this.bar.genericBar(ym, this.independenSeries(ym, List.of(unlp, lifia, despARS, despUSD), colorList), 25)));

        this.refs(
                title,
                List.of("UNLP", "LIFIA", "Despegar ARS", "Despegar USD"),
                colorList);

    }

    public void expenseBySource(int months) {

        final var title = format("Average {0}-month expenses by source", months);

        final var colorList = List.of(
                Attribute.BLUE_BACK(),
                Attribute.RED_BACK(),
                Attribute.YELLOW_BACK(),
                Attribute.GREEN_BACK(),
                Attribute.MAGENTA_BACK(),
                Attribute.WHITE_BACK()
        );
        this.console.appendLine(this.format.title(title));

        final var agg = new SimpleAggregation(months);

        final var seriesGroups = this.series.getRealUSDExpensesByType();

        final var ss = seriesGroups.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(e -> e.getValue().stream().reduce(MoneyAmountSeries::add).get())
                .map(agg::average)
                .collect(Collectors.toList());

        final var labels = seriesGroups.entrySet().stream()
                .map(Map.Entry::getKey)
                .sorted()
                .collect(Collectors.toList());

        final var oldestSeries = ss.stream().min(Comparator.comparing(MoneyAmountSeries::getFrom)).get();

        oldestSeries.map((ym, ma) -> ZERO_USD.max(ma))
                .forEach((ym, savingMa) -> this.console.appendLine(this.bar.genericBar(ym, this.independenSeries(ym, ss, colorList), 8)));

        this.refs(title, labels, colorList);

    }

    public void savingsIncomeTable() {

        final int[] years = new int[]{1, 2, 4, 6, 8, 10, 12, 14, 16};

        final var incomes = IntStream.of(years)
                .mapToObj(i -> Map.entry(i, this.incomeAverage(i)))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        final var savings = IntStream.of(years)
                .mapToObj(i -> Map.entry(i, this.savingsAverage(i)))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        this.console.appendLine(this.format.title("Average Income / Spending"));
        this.console.appendLine(
                this.row(Stream.concat(
                        Stream.of("Years"),
                        IntStream.of(years).mapToObj(y -> format("-= {0} =-", y)))));
        this.console.appendLine(
                this.row(Stream.concat(
                        Stream.of("Income"),
                        IntStream.of(years)
                                .mapToObj(incomes::get)
                                .map(MoneyAmount::getAmount)
                                .map(this.format::currency))));
        this.console.appendLine(
                this.row(
                        Stream.concat(
                                Stream.of("Savings"),
                                IntStream.of(years)
                                        .mapToObj(savings::get)
                                        .map(MoneyAmount::getAmount)
                                        .map(this.format::currency))));
        this.console.appendLine(
                this.row(
                        Stream.concat(
                                Stream.of("Spending"),
                                IntStream.of(years)
                                        .mapToObj(y -> incomes.get(y).subtract(savings.get(y)))
                                        .map(MoneyAmount::getAmount)
                                        .map(this.format::currency))));
        this.console.appendLine(this.row(Stream.concat(Stream.of("Saving %"),
                IntStream.of(years)
                        .mapToObj(y -> savings.get(y).getAmount().divide(incomes.get(y).getAmount().subtract(ONE, C), C))
                        .map(this.format::percent))));
    }

    private MoneyAmount incomeAverage(int years) {

        return this.series.getIncomeSeries()
                .stream()
                .collect(reducing(MoneyAmountSeries::add))
                .map(new SimpleAggregation(years * 12)::average)
                .map(allRealUSDIncome -> allRealUSDIncome.getAmount(USD_INFLATION.getTo()))
                .orElse(ZERO_USD);
    }

    private MoneyAmount savingsAverage(int years) {
        return new SimpleAggregation(years * 12)
                .average(this.series.realNetSavings())
                .getAmount(USD_INFLATION.getTo());
    }

    public void yearSavingsIncomeTable() {

        final int[] years = IntStream.rangeClosed(1999, USD_INFLATION.getTo().getYear()).toArray();

        final var incomes = IntStream.of(years)
                .mapToObj(i -> Map.entry(i, this.yearIncome(i)))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        final var savings = IntStream.of(years)
                .mapToObj(i -> Map.entry(i, this.yearSavings(i)))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        this.console.appendLine(this.format.title("Income / Spending by Year"));

        this.console.appendLine(this.row(Stream.of("-= Year =-", "Income", "Sav.", "Spend.", "Sav. %", "Sav./Spend.")));

        IntStream.of(years)
                .mapToObj(y -> this.row(Stream.of(format("-= {0} =-", String.valueOf(y) + (y == USD_INFLATION.getTo().getYear() ? "*" : "")),
                this.format.currency(incomes.get(y).getAmount()),
                this.format.currency(savings.get(y).getAmount()),
                this.format.currency(incomes.get(y).subtract(savings.get(y)).getAmount()),
                format("{0}", this.format.percent(savings.get(y).getAmount()
                        .divide(incomes.get(y).getAmount()
                                .subtract(ONE, C), C))),
                this.format.number(savings.get(y).getAmount().divide(incomes.get(y).subtract(savings.get(y)).getAmount(), C)))))
                .forEach(this.console::appendLine);
    }

    private MoneyAmount yearIncome(int year) {

        final var months = year < USD_INFLATION.getTo().getYear()
                ? 12
                : USD_INFLATION.getTo().getMonth();

        return this.series.getIncomeSeries()
                .stream()
                .map(s -> s.filter((ym, ma) -> ym.getYear() == year))
                .flatMap(Function.identity())
                .reduce(ZERO_USD, MoneyAmount::add)
                .adjust(BigDecimal.valueOf(months), ONE);
    }

    private MoneyAmount yearSavings(int year) {

        final var months = year < USD_INFLATION.getTo().getYear()
                ? 12
                : USD_INFLATION.getTo().getMonth();

        return this.series.realNetSavings()
                .filter((ym, ma) -> ym.getYear() == year)
                .reduce(ZERO_USD, MoneyAmount::add)
                .adjust(BigDecimal.valueOf(months), ONE);
    }

    private String row(Stream<String> values) {
        return values
                .map(this::cell)
                .collect(joining());
    }

    private String cell(String value) {
        return String.format("%12s", value);
    }
}
