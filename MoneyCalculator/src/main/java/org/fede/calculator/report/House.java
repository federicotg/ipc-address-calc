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
package org.fede.calculator.report;

import java.math.BigDecimal;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.text.MessageFormat.format;
import java.util.stream.Stream;
import org.fede.calculator.money.Currency;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.SimpleAggregation;
import static org.fede.calculator.money.Currency.USD;
import static org.fede.calculator.money.ForeignExchanges.getMoneyAmountForeignExchange;
import static org.fede.calculator.money.Inflation.USD_INFLATION;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.SeriesReader;
import org.fede.calculator.money.series.SortedMapMoneyAmountSeries;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import static org.fede.calculator.money.MathConstants.C;

/**
 *
 * @author fede
 */
public class House {

    private final BigDecimal COEFFICIENT = new BigDecimal("0.1223");

    private final BigDecimal REALTOR_FEE = new BigDecimal("0.045");
    private final BigDecimal STAMP_TAX = new BigDecimal("0.018");
    private final BigDecimal REGISTER_TAX = new BigDecimal("0.006");

    private final Console console;
    private final Format format;
    private final Bar bar;

    public House(Console console, Format format, Bar bar) {
        this.console = console;
        this.format = format;
        this.bar = bar;
    }

    public void houseCostsEvolution() {

        final var limit = USD_INFLATION.getTo();

        final var nominalInitialCost = SeriesReader.readBigDecimal("houseNominalCost");

        final var ivaFactor = SeriesReader.readPercent("iva").add(ONE, C);
        final var notaryFee = SeriesReader.readPercent("house.notary").multiply(ivaFactor);

        final var nominalTransactionCost = nominalInitialCost
                .multiply(REALTOR_FEE
                        .add(STAMP_TAX)
                        .add(REGISTER_TAX)
                        .add(notaryFee));
        
        //this.console.appendLine(" "+nominalTransactionCost);

        final var start = YearMonth.of(2010, 8);
        final var realInitialCost = USD_INFLATION.adjust(new MoneyAmount(nominalTransactionCost, USD),
                start,
                limit);

        final var initialCostSeries = new SortedMapMoneyAmountSeries(USD, "costs");
        initialCostSeries.putAmount(start, realInitialCost);
        initialCostSeries.putAmount(YearMonth.of(2010, 9), MoneyAmount.zero(USD));
        initialCostSeries.putAmount(YearMonth.of(2010, 10), MoneyAmount.zero(USD));
        initialCostSeries.putAmount(YearMonth.of(2010, 11), MoneyAmount.zero(USD));
        initialCostSeries.putAmount(YearMonth.of(2010, 12), MoneyAmount.zero(USD));
        initialCostSeries.putAmount(YearMonth.of(2011, 1), MoneyAmount.zero(USD));

        final var proportionalExpenses = SeriesReader.readSeries("expense/consorcio-reparaciones.json")
                .map((ym, amount) -> amount.adjust(ONE, COEFFICIENT));

        final var ongoingExpenses = Stream.concat(
                Stream.of("expense/inmobiliario-43.json", "expense/seguro.json", "expense/reparaciones.json")
                        .map(SeriesReader::readSeries),
                Stream.of(proportionalExpenses))
                .reduce(MoneyAmountSeries::add)
                .map(expenses -> expenses.exchangeInto(USD))
                .map(usdExpenses -> USD_INFLATION.adjust(usdExpenses, limit))
                .get();

        final var allExpenses = new SimpleAggregation(1200).sum(ongoingExpenses.add(initialCostSeries));

        final var initialExpenseYM = allExpenses.getFrom();

        this.bar.evolution("House cost evolution",
                allExpenses.map((ym, ma) -> ma.adjust(BigDecimal.valueOf(initialExpenseYM.until(ym, ChronoUnit.MONTHS) + 1), ONE)), 120);

    }

    public void houseIrrecoverableCosts(YearMonth timeLimit) {

        final var limit = USD_INFLATION.getTo();

        final var proportionalExpenses = SeriesReader.readSeries("expense/consorcio-reparaciones.json")
                .map((ym, amount) -> amount.adjust(ONE, COEFFICIENT));

        final var realExpensesInUSD = Stream.concat(
                Stream.of("expense/inmobiliario-43.json", "expense/seguro.json", "expense/reparaciones.json").map(SeriesReader::readSeries),
                Stream.of(proportionalExpenses))
                .reduce(MoneyAmountSeries::add)
                .map(expenses -> expenses.exchangeInto(USD))
                .map(usdExpenses -> USD_INFLATION.adjust(usdExpenses, limit))
                .map(s -> s.map((ym, amount) -> this.limit(timeLimit, ym, amount)))
                .map(MoneyAmountSeries::moneyAmountStream)
                .orElseGet(Stream::empty)
                .reduce(MoneyAmount::add)
                .orElse(MoneyAmount.zero(USD));

        this.buyVsRent(realExpensesInUSD, ZERO, timeLimit);
        this.buyVsRent(realExpensesInUSD, new BigDecimal("0.02"), timeLimit);
        this.buyVsRent(realExpensesInUSD, new BigDecimal("0.03"), timeLimit);
    }

    private MoneyAmount limit(YearMonth timeLimit, YearMonth ym, MoneyAmount amount) {
        return ym.compareTo(timeLimit) <= 0 ? amount : MoneyAmount.zero(amount.currency());
    }

    private void buyVsRent(MoneyAmount realExpensesInUSD, BigDecimal rate, YearMonth timeLimit) {
        final var limit = USD_INFLATION.getTo();

        final var ivaFactor = SeriesReader.readPercent("iva").add(ONE, C);
        final var notaryFee = SeriesReader.readPercent("house.notary").multiply(ivaFactor);

        final var nominalInitialCost = SeriesReader.readBigDecimal("houseNominalCost");
        final var nominalTransactionCost = nominalInitialCost
                .multiply(REALTOR_FEE
                        .add(STAMP_TAX, C)
                        .add(REGISTER_TAX)
                        .add(notaryFee));

        //this.console.appendLine(" "+nominalTransactionCost);
        
        final var start = YearMonth.of(2010, 8);
        final var realInitialCost = USD_INFLATION.adjust(new MoneyAmount(nominalInitialCost, Currency.USD),
                start,
                limit);

        final var months = BigDecimal.valueOf(start.until(timeLimit, ChronoUnit.MONTHS));
        final var years = months.divide(BigDecimal.valueOf(12), C);

        // interest rate cost
        final var opportunityCost = new MoneyAmount(
                nominalInitialCost
                        .add(nominalTransactionCost, C)
                        .multiply(ONE.add(rate, C).pow(years.intValue(), C), C)
                        .subtract(nominalInitialCost, C), Currency.USD);

        final var totalRealExpense = realExpensesInUSD.add(opportunityCost);

        this.console.appendLine(format("===< Costo de {0}/{1} a {2}/{3} con retorno anual de {4} >===",
                start.getMonth(),
                String.valueOf(start.getYear()),
                timeLimit.getMonth(),
                String.valueOf(timeLimit.getYear()),
                this.format.percent(rate)));

        this.console.appendLine(format("USD reales {0}/{1}", limit.getMonth(), String.valueOf(limit.getYear())));
        this.console.appendLine(format("\tTotal USD {0} {1}",
                this.format.currency(totalRealExpense.amount()),
                this.format.percent(totalRealExpense.amount().divide(realInitialCost.amount(), C))));

        final var monthlyCost = totalRealExpense.amount().divide(months, C);
        this.console.appendLine(format("\tMensual USD {0} {1} - ARS {2}",
                this.format.currency(monthlyCost),
                this.format.percent(monthlyCost.divide(realInitialCost.amount(), C)),
                this.format.currency(getMoneyAmountForeignExchange(USD, Currency.ARS)
                        .apply(new MoneyAmount(monthlyCost, USD), limit)
                        .amount())));

        final var yearlyCost = totalRealExpense.amount().divide(years, C);
        this.console.appendLine(format("\tAnual USD {0} {1}\n",
                this.format.currency(yearlyCost),
                this.format.percent(yearlyCost.divide(realInitialCost.amount(), C))));

    }

}
