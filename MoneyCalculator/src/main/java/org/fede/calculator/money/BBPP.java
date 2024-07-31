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
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import java.text.MessageFormat;
import static java.text.MessageFormat.format;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import static org.fede.calculator.money.ForeignExchanges.getMoneyAmountForeignExchange;
import org.fede.calculator.money.series.BBPPItem;
import org.fede.calculator.money.series.BBPPYear;
import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.InvestmentAsset;
import static org.fede.calculator.money.series.InvestmentType.BONO;
import org.fede.calculator.money.series.SeriesReader;
import org.fede.calculator.money.series.YearMonth;
import static org.fede.calculator.money.MathConstants.C;
import org.fede.calculator.money.series.MoneyAmountSeries;

/**
 *
 * @author fede
 */
public class BBPP {

    private static class BBPPResult {

        private int year;
        private BigDecimal totalAmount;
        private BigDecimal taxedDomesticAmount;
        private BigDecimal taxedForeignAmount;
        private BigDecimal taxedTotal;
        //private BigDecimal taxRate;
        private BigDecimal taxAmount;
        private MoneyAmount usdTaxAmount;
        private MoneyAmount allInvested;
        private BigDecimal yearRealIncome;
        private List<BBPPItem> allArs;
        private MoneyAmount usdPaidAmount;
        private MoneyAmount minimum;
        private MoneyAmount taxedTotalUSD;
    }

    private static final MoneyAmount ZERO_USD = MoneyAmount.zero("USD");

    private final Format format;
    private final Series series;
    private final Console console;

    private MoneyAmountSeries bbppExpenseSeries;

    public BBPP(Format format, Series series, Console console) {
        this.format = format;
        this.series = series;
        this.console = console;
    }

    public void bbppEvolution(boolean ibkr) {

        this.console.appendLine(this.format.title("BB.PP. Evolution"));

        this.console.appendLine(
                this.format.text("Year", 5),
                this.format.text("     Amount", 14),
                this.format.text("   Amount USD", 16),
                this.format.text("    Advance", 14),
                this.format.text("     Paid", 14),
                this.format.text("    Minimum", 17),
                this.format.text("  Taxed Fiscal", 17),
                this.format.text(" Ef. rate", 9),
                this.format.text("  Inv. %", 9),
                this.format.text(" Income %", 9));

        final List<BBPPYear> bbppYears = this.series.bbppSeries();

        bbppYears.stream()
                .map(BBPPYear::year)
                .map(y -> this.bbppResult(bbppYears, y, ibkr))
                .sorted(Comparator.comparing(r -> r.year))
                .forEach(this::bbppEvoReport);
    }

    private void bbppEvoReport(BBPPResult bbpp) {

        this.console.appendLine(format("{0}{1}{2}{3}{4}{5}{6}{7}{8}{9}",
                this.format.text(String.valueOf(bbpp.year), 5),
                this.format.currency(bbpp.taxAmount, 14),
                this.format.currency(bbpp.usdTaxAmount, 16),
                this.format.currency(bbpp.taxAmount.divide(BigDecimal.valueOf(5), C), 14),
                this.format.currency(bbpp.usdPaidAmount, 14),
                this.format.currency(bbpp.minimum, 17),
                this.format.currency(bbpp.taxedTotalUSD, 17),
                this.format.percent(bbpp.taxAmount.divide(bbpp.totalAmount, C), 9),
                this.format.percent(bbpp.usdTaxAmount.getAmount().divide(bbpp.allInvested.getAmount(), C), 9),
                this.format.percent(bbpp.usdTaxAmount.getAmount().divide(bbpp.yearRealIncome, C), 9)));

    }

    private BBPPItem toArs(BBPPItem i, Map<String, Function<MoneyAmount, BigDecimal>> arsFunction) {
        if (arsFunction.containsKey(i.currency())) {
            return new BBPPItem(i.name(),
                    arsFunction.get(i.currency()).apply(new MoneyAmount(i.value(), i.currency())),
                    i.holding(), i.domestic(), i.exempt(), "ARS");
        }
        return i;
    }

    private BBPPYear bbpp(List<BBPPYear> bbppYears, int year, boolean ibkr) {

        final var date = Date.from(LocalDate.of(year, Month.DECEMBER, 31).atStartOfDay(ZoneId.systemDefault()).toInstant());

        final var bbpp = bbppYears
                .stream()
                .filter(y -> y.year() == year)
                .findAny()
                .get();

        final var ym = YearMonth.of(year, 12);

        final Map<String, Function<MoneyAmount, BigDecimal>> arsFunction = Map.of("ARS", (MoneyAmount item) -> item.getAmount(),
                "LECAP", (MoneyAmount item) -> item.getAmount(),
                "EUR", (MoneyAmount item) -> item.getAmount().multiply(bbpp.eur(), C),
                "USD", (MoneyAmount item) -> item.getAmount().multiply(bbpp.usd(), C),
                "LETE", (MoneyAmount item) -> item.getAmount().multiply(bbpp.usd(), C),
                "XRSU", (MoneyAmount item) -> getMoneyAmountForeignExchange(item.getCurrency(), "USD")
                        .apply(item, ym)
                        .getAmount()
                        .multiply(bbpp.usd(), C),
                "RTWO", (MoneyAmount item) -> getMoneyAmountForeignExchange(item.getCurrency(), "USD")
                        .apply(item, ym)
                        .getAmount()
                        .multiply(bbpp.usd(), C),
                "CSPX", (MoneyAmount item) -> getMoneyAmountForeignExchange(item.getCurrency(), "USD")
                        .apply(item, ym)
                        .getAmount()
                        .multiply(bbpp.usd(), C),
                "EIMI", (MoneyAmount item) -> getMoneyAmountForeignExchange(item.getCurrency(), "USD")
                        .apply(item, ym)
                        .getAmount()
                        .multiply(bbpp.usd(), C),
                "MEUD", (MoneyAmount item) -> getMoneyAmountForeignExchange(item.getCurrency(), "EUR")
                        .apply(item, ym)
                        .getAmount()
                        .multiply(bbpp.eur(), C));

        final var etfs = this.series.getInvestments()
                .stream()
                .filter(i -> year < 2023)
                .filter(i -> ibkr || i.getComment() == null)
                .filter(i -> i.isCurrent(date))
                .filter(Investment::isETF)
                .map(Investment::getInvestment)
                .map(InvestmentAsset::getMoneyAmount)
                .map(ma -> arsFunction.get(ma.getCurrency()).apply(ma))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final var ons = this.series.getInvestments()
                .stream()
                .filter(i -> i.isCurrent(date))
                .filter(i -> BONO == i.getType())
                .map(Investment::getInvestment)
                .map(InvestmentAsset::getMoneyAmount)
                .map(ma -> arsFunction.get(ma.getCurrency()).apply(ma))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (etfs.signum() > 0) {
            bbpp.items().add(new BBPPItem("ETFs", etfs, ONE, false, false, "ARS"));
        }
        bbpp.items().add(new BBPPItem("ONs", ons, ONE, true, false, "ARS"));

        return new BBPPYear(bbpp.year(), bbpp.brakets(), bbpp.minimum(), bbpp.usd(), bbpp.eur(), bbpp.tax(),
                bbpp.items()
                        .stream()
                        .map(i -> this.toArs(i, arsFunction))
                        .toList());
    }

    private BBPPResult bbppResult(List<BBPPYear> bbppYears, final int year, final boolean ibkr) {

        final var ym = YearMonth.of(year, 12);

        final var date = Date.from(LocalDate.of(year, Month.DECEMBER, 31).atStartOfDay(ZoneId.systemDefault()).toInstant());

        final var bbpp = this.bbpp(bbppYears, year, ibkr);

        final var result = new BBPPResult();

        result.allArs = bbpp.items()
                .stream()
                .filter(i -> ibkr || !i.name().equals("IBKR USD"))
                .map(i -> this.toARS(i, bbpp.usd(), bbpp.eur()))
                .filter(bbppItem -> bbppItem.value().compareTo(ZERO) != 0)
                .toList();

        result.totalAmount = result.allArs
                .stream()
                .map(i -> i.value().multiply(i.holding(), C))
                .reduce(ZERO, BigDecimal::add);

        result.taxedDomesticAmount = result.allArs
                .stream()
                .filter(BBPPItem::domestic)
                .filter(Predicate.not(BBPPItem::exempt))
                .map(i -> i.value().multiply(i.holding(), C))
                .reduce(ZERO, BigDecimal::add)
                .multiply(new BigDecimal("1.05"), C);

        result.taxedForeignAmount = result.allArs
                .stream()
                .filter(Predicate.not(BBPPItem::domestic))
                .filter(Predicate.not(BBPPItem::exempt))
                .map(i -> i.value().multiply(i.holding(), C))
                .reduce(ZERO, BigDecimal::add);

        result.taxedTotal = bbpp.minimum()
                .negate()
                .add(result.taxedDomesticAmount, C)
                .add(result.taxedForeignAmount, C)
                .max(ZERO);

        result.taxAmount = bbpp.tax();

        final var usdFxYearMonth = Inflation.USD_INFLATION.getTo().min(YearMonth.of(ym.getYear() + 1, 6));

        result.usdTaxAmount = getMoneyAmountForeignExchange("ARS", "USD")
                .apply(new MoneyAmount(result.taxAmount, "ARS"), usdFxYearMonth);

        result.allInvested = this.series.getInvestments()
                .stream()
                .filter(i -> i.isCurrent(date))
                .map(Investment::getInvestment)
                .map(InvestmentAsset::getMoneyAmount)
                .map(ma -> getMoneyAmountForeignExchange(ma.getCurrency(), "USD").apply(ma, ym))
                .reduce(ZERO_USD, MoneyAmount::add);

        final var incomeMonths = Stream.concat(
                IntStream.range(6, 12).mapToObj(m -> YearMonth.of(year, m)),
                IntStream.range(1, 5).mapToObj(m -> YearMonth.of(year + 1, m)))
                .collect(Collectors.toSet());

        final var yearRealIncomeList = new ArrayList<MoneyAmount>(12);
        this.series.realIncome()
                .forEachNonZero((yearMonth, ma) -> Optional.of(ma).filter(m -> incomeMonths.contains(yearMonth)).ifPresent(yearRealIncomeList::add));

        final int incomeSize = yearRealIncomeList.size();

        var income = yearRealIncomeList.stream()
                .map(MoneyAmount::getAmount)
                .reduce(ZERO, BigDecimal::add);

        if (incomeSize < 12) {
            income = income
                    .divide(BigDecimal.valueOf(incomeSize), C)
                    .multiply(BigDecimal.valueOf(12l), C);
        }

        result.yearRealIncome = income;
        result.year = year;
        result.usdPaidAmount = this.getBBPPExpenseSeries()
                .filter((yearMonth, ma) -> incomeMonths.contains(yearMonth))
                .reduce(ZERO_USD, MoneyAmount::add);

        result.minimum = new MoneyAmount(bbpp.minimum().divide(bbpp.usd(), C), "USD");
        result.taxedTotalUSD = new MoneyAmount(result.taxedTotal.divide(bbpp.usd(), C), "USD");

        return result;
    }

    private MoneyAmountSeries getBBPPExpenseSeries() {
        if (this.bbppExpenseSeries == null) {
            this.bbppExpenseSeries = SeriesReader.readSeries("expense/bbpp.json")
                    .exchangeInto("USD");
        }
        return this.bbppExpenseSeries;
    }

    public void bbpp(int year, boolean ibkr) {
        this.bbppReport(this.bbppResult(this.series.bbppSeries(), year, ibkr));
        this.console.appendLine(this.format.subtitle("Official"));
        this.officialReport(year, ibkr);
    }

    private void bbppReport(BBPPResult bbpp) {

        this.console.appendLine(this.format.title(format("BB.PP. {0}", String.valueOf(bbpp.year))));
        this.console.appendLine(format("Total amount {0}", this.format.currency(bbpp.totalAmount)));

        this.console.appendLine(format("Taxed domestic amount {0} (+5%)", this.format.currency(bbpp.taxedDomesticAmount)));
        this.console.appendLine(format("Taxed foreign amount {0}", this.format.currency(bbpp.taxedForeignAmount)));
        this.console.appendLine(format("Taxed total {0}", this.format.currency(bbpp.taxedTotal)));
 
        this.console.appendLine(format("Tax amount {0} / USD {1}. Advances {2}",
                this.format.currency(bbpp.taxAmount),
                this.format.currency(bbpp.usdTaxAmount.getAmount()),
                this.format.currency(bbpp.taxAmount.divide(BigDecimal.valueOf(5), C))));

        this.console.appendLine(format("Monthly tax amount USD {0}", this.format.currency(bbpp.usdTaxAmount.adjust(BigDecimal.valueOf(12), ONE).getAmount())));

        this.console.appendLine(format("Effective tax rate is {0}. Tax is {1} of investments. Tax is {2} of income.",
                this.format.percent(bbpp.taxAmount.divide(bbpp.totalAmount, C)),
                this.format.percent(bbpp.usdTaxAmount.getAmount().divide(bbpp.allInvested.getAmount(), C)),
                this.format.percent(bbpp.usdTaxAmount.getAmount().divide(bbpp.yearRealIncome, C))));

        this.console.appendLine(this.format.subtitle("Detail"));

        this.console.appendLine(format("{0}{1}{2}{3}", this.format.text("", 16), this.format.text("      Value", 16), this.format.text("    %", 10), this.format.text("      Taxed", 16)));
        bbpp.allArs.stream()
                .map(i -> format("{0}{1}{2}{3}",
                this.format.text(i.name(), 16),
                this.format.currency(i.value(), 16),
                this.format.percent(i.holding(), 10),
                this.format.currency(i.value().multiply(i.exempt() ? ZERO : i.holding(), C), 16)))
                .forEach(this.console::appendLine);
    }

    private BBPPItem toARS(BBPPItem item, BigDecimal usdValue, BigDecimal eurValue) {
        if (item.currency().equals("ARS")) {
            return item;
        }

        final var value = switch (item.currency()) {
            case "USD" ->
                item.value().multiply(usdValue, C);
            case "EUR" ->
                item.value().multiply(eurValue, C);
            default ->
                item.value();
        };

        final var newItem = new BBPPItem(item.name(), value, item.holding(), item.domestic(), item.exempt(), "ARS");

        return newItem;

    }

    private void officialReport(int year, boolean ibkr) {

        //final var date = Date.from(LocalDate.of(year, Month.DECEMBER, 31).atStartOfDay(ZoneId.systemDefault()).toInstant());
        final var bbpp = this.bbpp(this.series.bbppSeries(), year, ibkr);

        final var domestic = bbpp.items()
                .stream()
                .filter(BBPPItem::domestic)
                .filter(Predicate.not(BBPPItem::exempt))
                .map(i -> i.value().multiply(i.holding(), C))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .multiply(new BigDecimal("1.05"), C);

        final var foreign = bbpp.items()
                .stream()
                .filter(i -> ibkr || !i.name().equals("IBKR USD"))
                .filter(Predicate.not(BBPPItem::domestic))
                .filter(Predicate.not(BBPPItem::exempt))
                .map(i -> this.toARS(i, bbpp.usd(), bbpp.eur()))
                .map(i -> i.value().multiply(i.holding(), C))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.console.appendLine(MessageFormat.format(
                "Total de bienes sujeto a impuesto {0}",
                this.format.currency(domestic.add(foreign, C))));

        this.console.appendLine(MessageFormat.format(
                "Mínimo no imponible {0}",
                this.format.currency(bbpp.minimum().min(domestic.add(foreign, C)))));

        var base = ZERO.max(domestic.add(foreign, C).subtract(bbpp.minimum(), C));

        this.console.appendLine(MessageFormat.format(
                "Base imponible {0}",
                this.format.currency(base)));

        if (bbpp.tax() != null) {
            this.console.appendLine(MessageFormat.format(
                    "Impuesto determinado {0}",
                    this.format.currency(bbpp.tax())));
        }
    }
}
