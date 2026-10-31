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

import com.diogonunes.jcolor.Ansi;
import com.diogonunes.jcolor.AnsiFormat;
import com.diogonunes.jcolor.Attribute;
import java.math.BigDecimal;
import static java.math.BigDecimal.ONE;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import org.fede.calculator.money.Currency;
import org.fede.calculator.money.MoneyAmount;
import static org.fede.calculator.money.Currency.USD;
import org.fede.calculator.money.ForeignExchanges;
import static org.fede.calculator.money.MathConstants.C;
import org.fede.calculator.money.series.SeriesReader;
import static org.fede.calculator.money.series.SeriesReader.readPercent;

/**
 *
 * @author fede
 */
public class Fire {

    private static final DateTimeFormatter DF = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
            .withLocale(Locale.ENGLISH);

    private final Format format;
    private final Series series;
    private final Console console;
    private final LastAmounts last = new LastAmounts();

    public Fire(Format format, Series series, Console console) {
        this.format = format;
        this.series = series;
        this.console = console;
    }

    private MoneyAmount futureHealth() {
        return Future.futureHealth();
    }

    private MoneyAmount currentHealth() {
        return Future.contingencyHealth();
    }

    public void fi() {

        final var budgets = this.budgets();
        final var totalSavings = this.series.currentSavingsUSD();

        this.console.appendLine(this.format.subtitle("Retirement Monthly Average Spending"));

        final var futureHealth = this.futureHealth();
        final var currentHealth = this.currentHealth();

        final var currentCash = this.series.realSavings(AssetClass.CASH).getAmount(YearMonth.now());

        this.conceptLine("Essential", budgets.essential());
        this.conceptLine("Current Health Cost", currentHealth);
        this.conceptLine(" ➞ Future Health Cost", futureHealth);

        this.console.appendLine("");

        this.conceptLine("Future Essential",
                budgets.essential(),
                this.format.currency(
                        ForeignExchanges.getMoneyAmountForeignExchange(USD, Currency.ARS)
                                .apply(budgets.essential(), YearMonth.now()), 18)
        );

        this.console.appendLine(this.format.subtitle("Savings"));

        this.conceptLine("Current", totalSavings);

        var allEquity = this.series.realSavings(AssetClass.EQUITY).getAmount(YearMonth.now());
        var sp500 = this.last.lastAmount(Currency.CSPX);
        var nonSP500 = allEquity.subtract(sp500);

        var totalSavingAndMetals = totalSavings;
        this.console.appendLine("");

        this.compositionLine("SP500", sp500, totalSavingAndMetals);
        this.compositionLine("Other Equity", nonSP500, totalSavingAndMetals);
        this.compositionLine("Cash", currentCash, totalSavingAndMetals);
        this.console.appendLine(
                this.format.text("Deposit", 21),
                this.format.currency(
                        currentCash
                                .add(Future.severance(BigDecimal.valueOf(66).movePointLeft(2)).getTotal())
                                .subtract(new MoneyAmount(BigDecimal.valueOf(60000L), USD)),
                        16));

        this.console.appendLine(this.format.subtitle("Income"));

        final var sev = Future.severance();

        this.coveredCashLine("Current cash", currentCash, budgets.everything());
        this.coveredCashLine(" ? Current cash & sev.", currentCash
                .add(sev.getTotal()), budgets.everything(), false);

    }

    private void spendingReport(String title, BigDecimal wr, MoneyAmount totalPortfolioValue) {
        final int col = 20;
        final var spending = totalPortfolioValue
                .adjust(ONE, wr.divide(BigDecimal.valueOf(12L), C));

        this.console.appendLine(MessageFormat.format("{0}{1} ARS {2}{3}",
                this.format.text(title, col),
                this.format.currency(spending, col),
                this.format.text(
                        this.format.currencyShort(
                                ForeignExchanges.getForeignExchange(USD, Currency.ARS).exchange(spending, Currency.ARS, YearMonth.now()).amount()),
                        8),
                this.format.percent(wr, 6)
        ));

    }

    public void re(boolean detail) {

        final var unlp = this.series.incomeSource(IncomeSeries.UNLP);
        final var initialMonth = unlp.yearMonthStream()
                .filter(ym -> !unlp.getAmountOrElseZero(ym).isZero())
                .findFirst()
                .get().plusMonths(-1);
        final var worked = initialMonth.until(YearMonth.now(), ChronoUnit.MONTHS);

        final BigDecimal[] result = BigDecimal.valueOf((30L * 12L) - worked)
                .divideAndRemainder(BigDecimal.valueOf(12L));

        this.console.appendLine(MessageFormat.format(
                "Worked since {3} {0} years and {1} months. {2} months left ({4} years and {5} months).",
                Math.floorDiv(worked, 12),
                Math.floorMod(worked, 12),
                (30L * 12L) - worked,
                initialMonth.toString(),
                result[0],
                result[1])
        );

        final var budgets = this.budgets();
        final var now = YearMonth.now();
        final var equity = this.last.last();
        final var cash = this.series.realSavings(AssetClass.CASH).getAmount(now);
        final var bonds = this.series.realSavings(AssetClass.BONDS).getAmountOrElseZero(now);
        final var swr = new CAEYSafeWithdrawalRate();
        final var calc = swr.detail(equity.us(), equity.exUs(), equity.em(), bonds, cash);
        final var currentSavings = this.series.currentSavingsUSD();

        if (detail) {
            this.withdrawalDetail(calc, currentSavings);
        }

        this.spendingReport("Current", calc.withdrawalRate(), calc.virtualPortfolio());

        final var percents = this.percents(calc.withdrawalRate());

        this.console.appendLine("");

        final var alreadyThere = Attribute.GREEN_TEXT();
        final var withGrowth = Attribute.BRIGHT_YELLOW_TEXT();
        final var farAway = Attribute.RED_TEXT();

        this.console.appendLine(
                Stream.concat(
                        Stream.of(this.format.text("Spending", 12)),
                        percents.stream()
                                .map(pct -> this.format.center(this.format.percent(pct), 8)))
                        .collect(Collectors.joining()));
        this.spendingAmounts(budgets)
                .stream()
                .map(monthlySpending
                        -> this.retirementWithdrawalRow(
                                monthlySpending,
                                currentSavings,
                                calc.virtualPortfolio(),
                                percents,
                                alreadyThere,
                                withGrowth,
                                farAway,
                                budgets
                        ))
                .forEach(this.console::appendLine);

        this.conceptLine("Essential", budgets.essential(), "✅");
        this.conceptLine("Everything", budgets.everything(), "✅✅");

    }

    public void fiReport() {
        this.console.appendLine(this.format.title("F.I.R.E."));
        this.fi();

    }

    public void reReport(boolean detail) {
        this.console.appendLine(this.format.title("F.I.R.E."));
        this.re(detail);

    }

    public void fire(boolean detail) {

        this.console.appendLine(this.format.title("F.I.R.E."));

        this.fi();
        this.re(detail);

    }

    private void withdrawalDetail(CAEYSafeWithdrawalRate.WithdrawalDetail calc, MoneyAmount portfolio) {
        final var green = new AnsiFormat(Attribute.GREEN_TEXT());
        final var yellow = new AnsiFormat(Attribute.BRIGHT_YELLOW_TEXT());

        this.console.appendLine(this.format.subtitle("SWR"));
        this.paramLine("a", this.format.percent(calc.a(), 12));
        this.paramLine("b", this.format.number(calc.b(), 12));
        this.paramLine("CAPE US", this.format.number(calc.cape(), 12) + this.format.percent(calc.usWeight(), 10));
        this.paramLine("CAPE ex-US", this.format.number(calc.capeExUs(), 12) + this.format.percent(calc.exUsWeight(), 10));
        this.paramLine("CAPE EM", this.format.number(calc.capeEmerging(), 12) + this.format.percent(calc.emWeight(), 10));
        this.paramLine("CAPE", this.format.number(calc.blendedCape(), 12));
        this.paramLine("a + b / CAPE", this.format.percent(calc.capeFormula(), 12));
        this.paramLine("Expected inflation", this.format.percent(calc.expectedInflation(), 12));
        this.paramLine("Bond nominal yield", this.format.percent(calc.bondNominalYield(), 12));
        this.paramLine("Cash real yield", this.format.percent(calc.cashRealYield(), 12) + this.format.percent(calc.cashWeight(), 10));
        this.paramLine("Bonds real yield", this.format.percent(calc.bondsRealYield(), 12) + this.format.percent(calc.bondsWeight(), 10));
        this.paramLine("Discount rate", this.format.percent(calc.discountRate(), 12));
        this.paramLine("Withdrawal rate", this.format.percent(calc.withdrawalRate(), 12));
        this.paramLine("Future cash flows", this.format.currency(calc.futureIncome(), 22));
        this.console.appendLine(
                this.format.text("Portfolio", 28, green),
                Ansi.colorize(this.format.currency(portfolio, 22), green));
        this.console.appendLine(
                this.format.text("Virtual portfolio", 28, yellow),
                Ansi.colorize(this.format.currency(calc.virtualPortfolio(), 22), yellow));
    }

    private void paramLine(String label, String value) {
        this.console.appendLine(this.format.text(label, 28), value);
    }

    private void compositionLine(String categoryName, MoneyAmount amount, MoneyAmount total) {
        this.console.appendLine(MessageFormat.format(
                "{0} {1} {2}",
                this.format.text(categoryName, 20),
                this.format.currency(amount, 16),
                this.format.percent(amount.amount().divide(total.amount(), C), 12)));
    }

    private void coveredCashLine(String name, MoneyAmount amount, MoneyAmount essential) {
        this.coveredCashLine(name, amount, essential, true);
    }

    private void coveredCashLine(String name, MoneyAmount amount, MoneyAmount essential, boolean singular) {
        final var coveredMonths = amount
                .adjust(essential.amount(), ONE)
                .amount();

        final BigDecimal[] result = coveredMonths
                .divideAndRemainder(BigDecimal.valueOf(12L));

        final var moment = LocalDate.now()
                .plusDays(coveredMonths.multiply(BigDecimal.valueOf(30L)).intValue());

        this.console.appendLine(
                MessageFormat.format(
                        "{3} " + (singular ? "covers" : "cover") + " essentials for {0} years and {1} months until {2}, age {4}.",
                        result[0],
                        result[1].setScale(0, RoundingMode.FLOOR),
                        DF.format(moment),
                        name,
                        Period.between(SeriesReader.readDate("dob"), moment).getYears()
                ));
    }

    private List<BigDecimal> percents(BigDecimal withdrawalRate) {
        final var step = BigDecimal.valueOf(25L).movePointLeft(4);
        return Stream.concat(
                LongStream.range(15L, 20L)
                        .mapToObj(i -> BigDecimal.valueOf(i).multiply(step, C)),
                Stream.concat(
                        Stream.of(withdrawalRate),
                        Stream.of(
                                BigDecimal.valueOf(366L),
                                BigDecimal.valueOf(338L),
                                BigDecimal.valueOf(353L),
                                BigDecimal.valueOf(442L),
                                BigDecimal.valueOf(407L))
                                .map(v -> v.movePointLeft(4))))
                .map(BigDecimal::stripTrailingZeros)
                .distinct()
                .sorted()
                .toList();
    }

    private List<BigDecimal> spendingAmounts(SpendingBudgets budgets) {

        final var fireFrom = SeriesReader.readInt("fire.from");
        final var fireTo = SeriesReader.readInt("fire.to");

        return Stream.concat(
                budgets.asStream(),
                IntStream.iterate(fireFrom, i -> i <= fireTo, i -> i += 100)
                        .mapToObj(BigDecimal::valueOf))
                .sorted()
                .toList();
    }

    private SpendingBudgets budgets() {

        var spendingTiers = new Future(series).retirementSpending();
        final var essential = spendingTiers.get(ExpenseTier.ESSENTIAL).getAmount(YearMonth.now());
        final var everything = spendingTiers.get(ExpenseTier.DISCRETIONARY).getAmount(YearMonth.now())
                .add(essential)
                .add(spendingTiers.get(ExpenseTier.LUXURY).getAmount(YearMonth.now()));

        final var capitalGainsTaxRate = readPercent("capitalGainsTaxRate");
        final var costBasisPct = readPercent("costBasisPct");

        return new SpendingBudgets(
                Future.adjustForCapitalGains(essential, capitalGainsTaxRate, costBasisPct),
                Future.adjustForCapitalGains(everything, capitalGainsTaxRate, costBasisPct)
        );
    }

    private void conceptLine(String label, MoneyAmount value) {
        this.conceptLine(label, value, "");
    }

    private void conceptLine(String label, MoneyAmount value, String extra) {
        this.console.appendLine(
                this.format.text(label, 30),
                this.format.currency(value, 12),
                " ", extra);
    }

    private String retirementWithdrawalRow(
            BigDecimal monthlySpending,
            MoneyAmount currentPortfolioSize,
            MoneyAmount plusCurrentlyEstimated,
            List<BigDecimal> percents,
            Attribute alreadyThere,
            Attribute withGrowth,
            Attribute farAway,
            SpendingBudgets budgets) {
        final var annualSpendingFromPortfolio = monthlySpending
                .multiply(BigDecimal.valueOf(12l), C);

        final Attribute textStyle = budgets
                .asStream()
                .anyMatch(b -> b.compareTo(monthlySpending) == 0)
                ? Attribute.BOLD()
                : Attribute.DIM();

        return this.format.text(
                this.format.currency(monthlySpending),
                12,
                new AnsiFormat(textStyle))
                + percents.stream()
                        .map(percent -> annualSpendingFromPortfolio.divide(percent, C))
                        .map(portfolioLevel -> this.coloredAmount(
                                portfolioLevel,
                                currentPortfolioSize,
                                plusCurrentlyEstimated,
                                alreadyThere,
                                withGrowth,
                                farAway))
                        .collect(Collectors.joining());
    }

    private String coloredAmount(
            BigDecimal amount,
            MoneyAmount currentPortfolioSize,
            MoneyAmount plusCurrentlyEstimated,
            Attribute alreadyThere,
            Attribute withGrowth,
            Attribute farAway) {

        final var cols = 8;

        if (amount.compareTo(currentPortfolioSize.amount()) <= 0) {
            return this.format.center(this.format.currencyShort(amount), cols, new AnsiFormat(alreadyThere));
        } else if (amount.compareTo(plusCurrentlyEstimated.amount()) <= 0) {
            return this.format.center(this.format.currencyShort(amount), cols, new AnsiFormat(withGrowth));
        } else {
            return this.format.center(this.format.currencyShort(amount), cols, new AnsiFormat(farAway));
        }
    }

   
}
