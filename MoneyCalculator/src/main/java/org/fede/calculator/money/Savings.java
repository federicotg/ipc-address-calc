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
import static java.math.BigDecimal.ZERO;
import java.text.NumberFormat;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.YearMonth;
import org.fede.util.Pair;

/**
 *
 * @author federicogentile
 */
public class Savings {

    private static final MoneyAmount ZERO_USD = new MoneyAmount(ZERO.setScale(6, MathConstants.RM), "USD");

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

    public void netAvgSavingSpent(int months, String title) {

        this.console.appendLine(this.format.title(title));

        final var agg = new SimpleAggregation(months);
        final var income = agg.average(this.series.realIncome());
        final var netSaving = agg.average(this.series.realNetSavings());
        final var spending = agg.average(this.series.realExpenses(null));

        netSaving.map((ym, ma) -> ZERO_USD.max(ma))
                .map((ym, ma) -> new MoneyAmount(income.getAmountOrElseZero(ym).getAmount().min(ma.getAmount()), ma.getCurrency()))
                .forEach((ym, savingMa) -> this.console.appendLine(this.bar.currencyBar(ym, this.series(ym, spending, income, savingMa), 25)));

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

        this.refs(
                this.format.title("Savings Distribution Evolution"),
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

        this.refs(
                this.format.title("Savings Distribution Percent Evolution"),
                List.of("Cash", "equity", "bonds"),
                List.of(Attribute.BLUE_BACK(), Attribute.RED_BACK(), Attribute.YELLOW_BACK()));
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
}
