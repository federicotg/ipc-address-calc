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

import com.fasterxml.jackson.core.type.TypeReference;
import java.math.BigDecimal;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.text.MessageFormat.format;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.ArrayList;
import static java.util.Comparator.comparing;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import static java.util.stream.Collectors.toList;
import static org.fede.calculator.money.ForeignExchanges.getMoneyAmountForeignExchange;
import static org.fede.calculator.money.MathConstants.CONTEXT;
import org.fede.calculator.money.series.BBPPItem;
import org.fede.calculator.money.series.BBPPTaxBraket;
import org.fede.calculator.money.series.BBPPYear;
import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.InvestmentAsset;
import static org.fede.calculator.money.series.InvestmentType.BONO;
import static org.fede.calculator.money.series.InvestmentType.ETF;
import org.fede.calculator.money.series.SeriesReader;
import org.fede.calculator.money.series.YearMonth;

/**
 *
 * @author fede
 */
public class BBPP {

    private static final MoneyAmount ZERO_USD = new MoneyAmount(ZERO.setScale(6, MathConstants.ROUNDING_MODE), "USD");

    private final Format format;
    private final Series series;
    private final Console console;

    public BBPP(Format format, Series series, Console console) {
        this.format = format;
        this.series = series;
        this.console = console;
    }

    public void bbpp(int year, boolean ibkr) {

        this.console.appendLine("===< ", format("BB.PP. {0}", String.valueOf(year)), " >===");

        List<BBPPYear> bbppYears = SeriesReader.read("bbpp.json", new TypeReference<List<BBPPYear>>() {
        });

        final var date = Date.from(LocalDate.of(year, Month.DECEMBER, 31).atStartOfDay(ZoneId.systemDefault()).toInstant());

        final var bbpp = bbppYears
                .stream()
                .filter(y -> y.getYear() == year)
                .findAny()
                .get();

        final var ym = YearMonth.of(year, 12);

        final Map<String, Function<MoneyAmount, BigDecimal>> arsFunction = Map.of(
                "ARS", (MoneyAmount item) -> item.getAmount(),
                "LECAP", (MoneyAmount item) -> item.getAmount(),
                "EUR", (MoneyAmount item) -> item.getAmount().multiply(bbpp.getEur(), CONTEXT),
                "USD", (MoneyAmount item) -> item.getAmount().multiply(bbpp.getUsd(), CONTEXT),
                "LETE", (MoneyAmount item) -> item.getAmount().multiply(bbpp.getUsd(), CONTEXT),
                "XRSU", (MoneyAmount item) -> getMoneyAmountForeignExchange(item.getCurrency(), "USD")
                        .apply(item, ym)
                        .getAmount()
                        .multiply(bbpp.getUsd(), CONTEXT),
                "CSPX", (MoneyAmount item) -> getMoneyAmountForeignExchange(item.getCurrency(), "USD")
                        .apply(item, ym)
                        .getAmount()
                        .multiply(bbpp.getUsd(), CONTEXT),
                "EIMI", (MoneyAmount item) -> getMoneyAmountForeignExchange(item.getCurrency(), "USD")
                        .apply(item, ym)
                        .getAmount()
                        .multiply(bbpp.getUsd(), CONTEXT),
                "MEUD", (MoneyAmount item) -> getMoneyAmountForeignExchange(item.getCurrency(), "EUR")
                        .apply(item, ym)
                        .getAmount()
                        .multiply(bbpp.getEur(), CONTEXT));

        final var etfs = this.series.getInvestments()
                .stream()
                .filter(i -> ibkr || i.getComment() == null)
                .filter(i -> i.isCurrent(date))
                .filter(i -> ETF.equals(i.getType()))
                .map(Investment::getInvestment)
                .map(InvestmentAsset::getMoneyAmount)
                .map(ma -> arsFunction.get(ma.getCurrency()).apply(ma))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final var ons = this.series.getInvestments()
                .stream()
                .filter(i -> i.isCurrent(date))
                .filter(i -> BONO.equals(i.getType()))
                .map(Investment::getInvestment)
                .map(InvestmentAsset::getMoneyAmount)
                //.peek(ma -> System.out.println(ma.getCurrency()))
                .map(ma -> arsFunction.get(ma.getCurrency()).apply(ma))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final var etfsItem = new BBPPItem();
        etfsItem.setCurrency("ARS");
        etfsItem.setDomestic(false);
        etfsItem.setExempt(false);
        etfsItem.setHolding(ONE);
        etfsItem.setName("ETFs");
        etfsItem.setValue(etfs);

        final var onsItem = new BBPPItem();
        onsItem.setCurrency("ARS");
        onsItem.setDomestic(true);
        onsItem.setExempt(false);
        onsItem.setHolding(ONE);
        onsItem.setName("ONs");
        onsItem.setValue(ons);

        final var homeCashItem = new BBPPItem();
        homeCashItem.setCurrency("ARS");
        homeCashItem.setDomestic(true);
        homeCashItem.setExempt(false);
        homeCashItem.setHolding(ONE);
        homeCashItem.setName("Home Cash");
        homeCashItem.setValue(BigDecimal.valueOf(15000l));

        bbpp.getItems().add(etfsItem);
        bbpp.getItems().add(onsItem);
        bbpp.getItems().add(homeCashItem);

        final var allArs = bbpp.getItems()
                .stream()
                .filter(i -> ibkr || !i.getName().equals("IBKR USD"))
                .map(i -> this.toARS(i, bbpp.getUsd(), bbpp.getEur()))
                .collect(toList());

        final var totalAmount = allArs
                .stream()
                .map(i -> i.getValue().multiply(i.getHolding(), CONTEXT))
                .reduce(ZERO, BigDecimal::add);

        this.console.appendLine(format("Total amount {0}", this.format.currency(totalAmount)));

        final var taxedDomesticAmount = allArs
                .stream()
                .filter(BBPPItem::isDomestic)
                .filter(i -> !i.isExempt())
                .map(i -> i.getValue().multiply(i.getHolding(), CONTEXT))
                .reduce(ZERO, BigDecimal::add)
                .multiply(new BigDecimal("1.05"), CONTEXT);

        this.console.appendLine(format("Taxed domestic amount {0}", this.format.currency(taxedDomesticAmount)));

        final var taxedForeignAmount = allArs
                .stream()
                .filter(i -> !i.isDomestic())
                .filter(i -> !i.isExempt())
                .map(i -> i.getValue().multiply(i.getHolding(), CONTEXT))
                .reduce(ZERO, BigDecimal::add);

        this.console.appendLine(format("Taxed foreign amount {0}", this.format.currency(taxedForeignAmount)));

        final var taxedTotal = bbpp.getMinimum()
                .negate()
                .add(taxedDomesticAmount, CONTEXT)
                .add(taxedForeignAmount, CONTEXT);

        this.console.appendLine(format("Taxed total {0}", this.format.currency(taxedTotal)));

        final var taxRate = bbpp.getBrakets()
                .stream()
                .sorted(comparing(BBPPTaxBraket::getFrom))
                .filter(b -> b.getFrom().compareTo(totalAmount) <= 0)
                .reduce((left, right) -> right)
                .get()
                .getTax();

        this.console.appendLine(format("Tax rate {0}", this.format.percent(taxRate)));

        final var taxAmount = taxedTotal.multiply(taxRate, CONTEXT);

        final var usdTaxAmount = getMoneyAmountForeignExchange("ARS", "USD")
                .apply(new MoneyAmount(taxAmount, "ARS"), ym);

        this.console.appendLine(format("Tax amount {0} / USD {1}. Advances {2}",
                this.format.currency(taxAmount),
                this.format.currency(usdTaxAmount.getAmount()),
                this.format.currency(taxAmount.divide(BigDecimal.valueOf(5), CONTEXT))));

        this.console.appendLine(format("Monthly tax amount USD {0}", this.format.currency(usdTaxAmount.adjust(BigDecimal.valueOf(12), ONE).getAmount())));

        final var allInvested = this.series.getInvestments()
                .stream()
                .filter(i -> i.isCurrent(date))
                .map(Investment::getInvestment)
                .map(InvestmentAsset::getMoneyAmount)
                .map(ma -> getMoneyAmountForeignExchange(ma.getCurrency(), "USD").apply(ma, ym))
                .reduce(ZERO_USD, MoneyAmount::add);

        final var yearRealIncome = new ArrayList<MoneyAmount>(12);

        this.series.realIncome()
                .forEachNonZero((yearMonth, ma) -> Optional.of(ma).filter(m -> yearMonth.getYear() == year).ifPresent(yearRealIncome::add));

        this.console.appendLine(format("Effective tax rate is {0}. Tax is {1} of investments. Tax is {2} of income.",
                this.format.percent(taxAmount.divide(totalAmount, CONTEXT)),
                this.format.percent(usdTaxAmount.getAmount().divide(allInvested.getAmount(), CONTEXT)),
                this.format.percent(usdTaxAmount.getAmount().divide(yearRealIncome.stream().map(MoneyAmount::getAmount).reduce(ZERO, BigDecimal::add), CONTEXT))));

        this.console.appendLine(this.format.subtitle("Detail"));

        this.console.appendLine(format("{0}{1}{2}{3}", this.format.text("", 16), this.format.text("      Value", 16), this.format.text("    %", 10), this.format.text("      Taxed", 16)));
        allArs.stream()
                .map(i -> format("{0}{1}{2}{3}",
                this.format.text(i.getName(), 16),
                this.format.currency(i.getValue(), 16),
                this.format.percent(i.getHolding(), 10),
                this.format.currency(i.getValue().multiply(i.isExempt() ? ZERO : i.getHolding(), CONTEXT), 16)))
                .forEach(this.console::appendLine);

    }

    private BBPPItem toARS(BBPPItem item, BigDecimal usdValue, BigDecimal eurValue) {
        if (item.getCurrency().equals("ARS")) {
            return item;
        }

        final var newItem = new BBPPItem();
        newItem.setCurrency("ARS");
        newItem.setDomestic(item.isDomestic());
        newItem.setExempt(item.isExempt());
        newItem.setHolding(item.getHolding());
        newItem.setName(item.getName());

        if (item.getCurrency().equals("USD")) {

            newItem.setValue(item.getValue().multiply(usdValue, CONTEXT));

        }
        if (item.getCurrency().equals("EUR")) {

            newItem.setValue(item.getValue().multiply(eurValue, CONTEXT));

        }
        return newItem;

    }

}
