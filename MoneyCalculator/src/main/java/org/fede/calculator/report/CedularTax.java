/*
 * Copyright (C) 2026 Federico Tello Gentile <federicotg@gmail.com>
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
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.fede.calculator.money.Currency;
import static org.fede.calculator.money.Currency.ARS;
import static org.fede.calculator.money.Currency.EUR;
import static org.fede.calculator.money.Currency.USD;
import static org.fede.calculator.money.MathConstants.C;
import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.InvestmentEvent;

/**
 *
 * @author fede
 */
public class CedularTax {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.15");

    private static final Map<LocalDate, BigDecimal> USD_OFFICIAL = Map.ofEntries(
            Map.entry(LocalDate.of(2025, 4, 3), new BigDecimal("1094.50")),
            Map.entry(LocalDate.of(2025, 4, 8), new BigDecimal("1096.00")),
            Map.entry(LocalDate.of(2025, 4, 14), new BigDecimal("1230.00")),
            Map.entry(LocalDate.of(2025, 4, 24), new BigDecimal("1190.00")),
            Map.entry(LocalDate.of(2025, 5, 1), new BigDecimal("1190.00")),
            Map.entry(LocalDate.of(2025, 5, 9), new BigDecimal("1150.00")),
            Map.entry(LocalDate.of(2025, 5, 16), new BigDecimal("1160.00")),
            Map.entry(LocalDate.of(2025, 5, 23), new BigDecimal("1150.00")));

    private static final Map<LocalDate, BigDecimal> EUR_OFFICIAL = Map.of();

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Format format;
    private final Series series;
    private final Console console;

    public CedularTax(Format format, Series series, Console console) {
        this.format = format;
        this.series = series;
        this.console = console;
    }

    public void report(int year) {
        final var sales = this.sales(year);

        this.console.appendLine(this.format.title(MessageFormat.format("Impuesto cedular {0}", year)));

        if (sales.isEmpty()) {
            this.console.appendLine(MessageFormat.format("No hubo ventas de ETF en {0}.", year));
            return;
        }

        this.console.appendLine(MessageFormat.format("Operaciones: {0}", sales.size()));
        this.console.appendLine("El costo incluye la comisión de compra. La venta es neta de comisión. El transfer fee no entra en el costo.");

        this.console.appendLine(
                this.format.text("Compra", 12),
                this.format.text("Venta", 12),
                this.format.text("ETF", 8),
                this.format.text("Moneda", 8),
                this.format.text(right("Costo", 16), 16),
                this.format.text(right("Venta neta", 16), 16),
                this.format.text(right("Ganancia", 16), 16),
                this.format.text(right("TC", 14), 14),
                this.format.text(right("Ganancia ARS", 18), 18),
                this.format.text(right("Impuesto ARS", 18), 18));

        sales.forEach(this::appendSale);

        final var gainPesos = sales.stream()
                .map(Sale::gainPesos)
                .reduce(ZERO, BigDecimal::add);
        final var taxable = gainPesos.max(ZERO);
        final var tax = taxable.multiply(TAX_RATE, C);

        this.console.appendLine(this.format.subtitle("Total"));
        this.console.appendLine(MessageFormat.format("Ganancia neta en pesos {0}", this.format.currencyPL(gainPesos, 18)));
        this.console.appendLine(MessageFormat.format("Base imponible {0}", this.format.currency(taxable, 18)));
        this.console.appendLine(MessageFormat.format("Impuesto a pagar (15%) {0}", this.format.currency(tax, 18)));
    }

    private void appendSale(Sale sale) {
        this.console.appendLine(
                this.format.text(DATE.format(sale.buyDate()), 12),
                this.format.text(DATE.format(sale.sellDate()), 12),
                this.format.text(sale.etf(), 8),
                this.format.text(sale.currency().name(), 8),
                this.format.currency(sale.cost(), 16),
                this.format.currency(sale.netProceeds(), 16),
                this.format.currencyPL(sale.gain(), 16),
                this.format.number(sale.exchangeRate(), 14),
                this.format.currencyPL(sale.gainPesos(), 18),
                this.format.currencyPL(sale.taxPesos(), 18));
    }

    private List<Sale> sales(int year) {
        return this.series.getInvestments()
                .stream()
                .filter(Investment::isETF)
                .filter(investment -> investment.getOut() != null)
                .filter(investment -> investment.getOut().getDate().getYear() == year)
                .map(this::sale)
                .sorted(Comparator.comparing(Sale::sellDate)
                        .thenComparing(Sale::buyDate)
                        .thenComparing(Sale::etf))
                .toList();
    }

    private Sale sale(Investment investment) {
        final var buy = investment.getIn();
        final var sell = investment.getOut();
        if (buy.getCurrency() != sell.getCurrency()) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "La compra del {0} y la venta del {1} de {2} no están en la misma moneda ({3} y {4}).",
                    DATE.format(buy.getDate()),
                    DATE.format(sell.getDate()),
                    investment.getInvestment().getCurrency(),
                    buy.getCurrency(),
                    sell.getCurrency()));
        }

        final var currency = buy.getCurrency();
        final var cost = cost(buy);
        final var netProceeds = netProceeds(sell);
        final var gain = netProceeds.subtract(cost, C);
        final var exchangeRate = this.officialRate(currency, sell.getDate());
        final var gainPesos = gain.multiply(exchangeRate, C);
        final var taxPesos = gainPesos.multiply(TAX_RATE, C);

        return new Sale(
                buy.getDate(),
                sell.getDate(),
                investment.getInvestment().getCurrency().name(),
                currency,
                cost,
                netProceeds,
                gain,
                exchangeRate,
                gainPesos,
                taxPesos);
    }

    private static String right(String value, int width) {
        return String.format("%" + width + "s", value);
    }

    private static BigDecimal cost(InvestmentEvent buy) {
        return buy.getAmount().add(buy.getFee(), C);
    }

    private static BigDecimal netProceeds(InvestmentEvent sell) {
        return sell.getAmount().subtract(sell.getFee(), C);
    }

    private BigDecimal officialRate(Currency currency, LocalDate saleDate) {
        if (currency == ARS) {
            return ONE;
        }
        final var rates = switch (currency) {
            case USD ->
                USD_OFFICIAL;
            case EUR ->
                EUR_OFFICIAL;
            default ->
                throw new IllegalArgumentException("No hay tipo de cambio oficial para " + currency);
        };
        final var rate = rates.get(saleDate);
        if (rate == null) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "No hay tipo de cambio oficial de {0} para el {1}.",
                    currency,
                    DATE.format(saleDate)));
        }
        return rate;
    }

    private record Sale(
            LocalDate buyDate,
            LocalDate sellDate,
            String etf,
            Currency currency,
            BigDecimal cost,
            BigDecimal netProceeds,
            BigDecimal gain,
            BigDecimal exchangeRate,
            BigDecimal gainPesos,
            BigDecimal taxPesos) {

    }

}
