/*
 * Copyright (C) 2025 fede
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

import com.diogonunes.jcolor.AnsiFormat;
import com.diogonunes.jcolor.Attribute;
import java.math.BigDecimal;
import static java.math.BigDecimal.ONE;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.Series;
import org.fede.calculator.money.SimpleAggregation;
import static org.fede.calculator.money.Currency.USD;
import static org.fede.calculator.money.Inflation.USD_INFLATION;
import static org.fede.calculator.money.MathConstants.C;
import static org.fede.calculator.money.Series.ESSENTIAL;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.SeriesReader;

/**
 *
 * @author fede
 */
public class Fire {

    private final Format format;
    private final Series series;
    private final Console console;

    public Fire(Format format, Series series, Console console) {
        this.format = format;
        this.series = series;
        this.console = console;
    }

    public void fire(int months) {

        this.console.appendLine(this.format.title("F.I.R.E."));

        final var limit = USD_INFLATION.getTo();
        final var futureHealth = SeriesReader.readUSD("futureHealth");
        final var futureRent = SeriesReader.readUSD("futureRent");
        final var futurePension = SeriesReader.readUSD("futurePension");
        final var totalSavings = this.series.realSavings(null).getAmount(limit);
        final var years = SeriesReader.readInt("retirementHorizon");
        final var futureSavings = SeriesReader.readBigDecimal("futureSavingsByYear")
                .multiply(BigDecimal.valueOf(years), C);

        final var essential = this.sumExpenses(ESSENTIAL, months);
        final var discretionary = this.sumExpenses(Series.DISCRETIONARY, months);
        final var irregular = this.sumExpenses(Series.IRREGULAR, months);
        final var other = this.sumExpenses(Series.OTHER, months);

        final var expectedFutureIncome = SeriesReader.readBigDecimal("futureRealState")
                .add(SeriesReader.readBigDecimal("futureCash"))
                .add(futureSavings);

        this.console.appendLine(this.format.subtitle(months + "-Month Average Spending"));

        this.conceptLine("Essential", essential);
        this.conceptLine("Other", other);
        this.conceptLine("Future Rent", futureRent);
        this.conceptLine("Future Health Cost", futureHealth);
        this.conceptLine("Discretionary", discretionary);
        this.conceptLine("Irregular", irregular);
        this.conceptLine("Future Pension", futurePension);

        this.conceptLine("Current Savings", totalSavings);
        
        //this.conceptLine("Future Savings", new MoneyAmount(futureSavings, USD));
        this.conceptLine("Future Income", new MoneyAmount(expectedFutureIncome, USD));

        var expected10YearCAGR = SeriesReader.readPercent("futureReturn").add(ONE, C);
        var expectedGrowth = expected10YearCAGR.pow(years, C);

        this.console.appendLine(
                String.valueOf(years),
                this.format.text(" Year Growth", 20),
                this.format.percent(expectedGrowth.subtract(ONE, C), 6));

        this.console.appendLine(this.format.subtitle("Portfolio Size by Spending and Withdrawal Percent"));

        final var essentialWithoutRent = essential
                .add(futureHealth)
                .add(other)
                .subtract(futurePension);
        final var essentialWithRent = essentialWithoutRent
                .add(futureRent);
        final var everythingWithoutRent = essential
                .add(futureHealth)
                .add(other)
                .subtract(futurePension)
                .add(discretionary)
                .add(irregular);

        final var everythingWithRent = everythingWithoutRent
                .add(futureRent);

        this.conceptLine("Essential - Rent", essentialWithoutRent, "✅");
        this.conceptLine("Everything - Rent", everythingWithoutRent, "✅✅");
        this.conceptLine("Essential + Rent", essentialWithRent, "✅✅");
        this.conceptLine("Everything + Rent", everythingWithRent, "✅✅✅");

        this.console.appendLine("");
        final var step = new BigDecimal("0.0025");
        final var percents = Stream.concat(
                IntStream.range(12, 19)
                        .mapToObj(i -> new BigDecimal(i).multiply(step, C)),
                Stream.of(
                        new BigDecimal("3.66"),
                        new BigDecimal("3.38"),
                        new BigDecimal("3.53"),
                        new BigDecimal("4.42"),
                        new BigDecimal("4.07"))
                        .map(v -> v.movePointLeft(2)))
                .sorted()
                .toList();

        var alreadyThere = Attribute.GREEN_TEXT();
        var withGrowth = Attribute.BRIGHT_YELLOW_TEXT();
        var withGrowthAndIncome = Attribute.YELLOW_TEXT();
        var farAway = Attribute.RED_TEXT();

        this.console.appendLine(
                Stream.concat(
                        Stream.of(this.format.text("Spending", 12)),
                        percents.stream()
                                .map(pct -> this.format.center(this.format.percent(pct), 8)))
                        .collect(Collectors.joining()));
        Stream.concat(
                Stream.of(essentialWithRent, essentialWithoutRent, everythingWithRent, everythingWithoutRent)
                        .map(MoneyAmount::amount),
                IntStream.range(5, 21)
                        .map(i -> i * 200)
                        .mapToObj(BigDecimal::new))
                .sorted()
                .map(monthlySpending
                        -> this.retirementWithdrawalRow(
                        monthlySpending,
                        totalSavings,
                        expectedFutureIncome,
                        expectedGrowth,
                        percents,
                        alreadyThere,
                        withGrowth,
                        withGrowthAndIncome,
                        farAway
                ))
                .forEach(this.console::appendLine);

        this.console.appendLine(this.format.subtitle("Failsafe Around Prominent Market Peaks"));

        this.refLine("pre-1900", "3.95");
        this.refLine("1900-1910", "3.38");
        this.refLine("1911-1928", "3.57");
        this.refLine("1929", "3.25");
        this.refLine("1964-69", "3.66");
        this.refLine("1972/73", "4.07");
        this.refLine("1999-2000", "3.53");
        this.refLine("2008/09", "4.42");

        new References(console, format)
                .refsLabels(
                        List.of("Savings", "Savings+Growth", "Savings+Growth+Income", "Far Away"),
                        List.of(alreadyThere,
                                withGrowth,
                                withGrowthAndIncome,
                                farAway));
    }

    private void refLine(String label, String pct) {
        this.console.appendLine(
                this.format.text(label, 16),
                this.format.percent(new BigDecimal(pct).movePointLeft(2), 6));
    }

    private MoneyAmount sumExpenses(String type, int months) {

        return this.series.getRealUSDExpensesByType()
                .get(type)
                .stream()
                .reduce(MoneyAmountSeries::add)
                .map(new SimpleAggregation(months)::average)
                .get()
                .getAmountOrElseZero(USD_INFLATION.getTo());

    }
    
     private void conceptLine(String label, MoneyAmount value) {
         this.conceptLine(label, value, "");
     }
    
    private void conceptLine(String label, MoneyAmount value, String extra) {
        this.console.appendLine(
                this.format.text(label, 20),
                this.format.currency(value, 12),
                " ", extra);
    }

    private String retirementWithdrawalRow(
            BigDecimal monthlySpending,
            MoneyAmount currentSavings,
            BigDecimal expectedFutureSavings,
            BigDecimal expected10YearGrowth,
            List<BigDecimal> percents,
            Attribute alreadyThere,
            Attribute withGrowth,
            Attribute withGrowthAndIncome,
            Attribute farAway) {
        var annualSpending = monthlySpending.multiply(new BigDecimal(12l), C);
        return this.format.text(this.format.currency(monthlySpending), 12)
                + percents.stream()
                        .map(percent -> annualSpending.divide(percent, C))
                        .map(portfolioLevel -> this.coloredAmount(
                        portfolioLevel,
                        currentSavings,
                        expectedFutureSavings,
                        expected10YearGrowth,
                        alreadyThere,
                        withGrowth,
                        withGrowthAndIncome,
                        farAway))
                        .collect(Collectors.joining());
    }

    private String coloredAmount(
            BigDecimal amount,
            MoneyAmount currentSavings,
            BigDecimal expectedFutureSavings,
            BigDecimal expected10YearGrowth,
            Attribute alreadyThere,
            Attribute withGrowth,
            Attribute withGrowthAndIncome,
            Attribute farAway) {

        var cols = 8;

        if (amount.compareTo(currentSavings.amount()) <= 0) {
            return this.format.center(this.format.currencyShort(amount), cols, new AnsiFormat(alreadyThere));
        } else if (amount.compareTo(currentSavings.amount()
                .multiply(expected10YearGrowth, C)) <= 0) {
            return this.format.center(this.format.currencyShort(amount), cols, new AnsiFormat(withGrowth));
        } else if (amount.compareTo(currentSavings.amount()
                .multiply(expected10YearGrowth, C)
                .add(expectedFutureSavings, C)) <= 0) {
            return this.format.center(this.format.currencyShort(amount), cols, new AnsiFormat(withGrowthAndIncome));
        } else {
            return this.format.center(this.format.currencyShort(amount), cols, new AnsiFormat(farAway));
        }
    }
}
