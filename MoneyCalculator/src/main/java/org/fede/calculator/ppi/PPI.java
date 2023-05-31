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
package org.fede.calculator.ppi;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.stream.Stream;
import org.fede.calculator.criptoya.CriptoYaAPI;
import org.fede.calculator.money.Console;
import org.fede.calculator.money.Format;
import org.fede.calculator.money.InstrumentType;
import org.fede.calculator.money.MathConstants;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.SettlementType;
import org.fede.util.Pair;

/**
 *
 * @author federicogentile
 */
public class PPI {

    private static final BigDecimal BONDS_FEE = new BigDecimal("0.006");
    private static final BigDecimal LETES_FEE = new BigDecimal("0.0015");

    private final Console console;
    private final Format format;
    private PPIRestAPI api;
    private CriptoYaAPI criptoya;

    public PPI(Console console, Format format) {
        this.console = console;
        this.format = format;
    }

    private MoneyAmount netOfFees(MoneyAmount gross, BigDecimal fee) {
        return gross.adjust(
                BigDecimal.ONE,
                BigDecimal.ONE.subtract(fee.add(fee, MathConstants.C), MathConstants.C));
    }

    private PPIRestAPI getApi() throws IOException, URISyntaxException, InterruptedException {
        if (this.api == null) {
            this.api = new PPIRestAPI();
        }
        return this.api;
    }

    private CriptoYaAPI getCriptoYaApi() throws IOException, URISyntaxException, InterruptedException {
        if (this.criptoya == null) {
            this.criptoya = new CriptoYaAPI();
        }
        return this.criptoya;
    }

    public void ccl() {

        this.console.appendLine(this.format.title("CCL PPI"));

        try {

            Stream.of(
                    Pair.of("Letras Inmediato", this.netOfFees(this.getApi().exchangeRate("S3Y3C", "S31Y3", InstrumentType.LETRAS, SettlementType.INMEDIATA), LETES_FEE)),
                    Pair.of("GD30 Inmediato", this.netOfFees(this.getApi().exchangeRate("GD30C", "GD30", InstrumentType.BONOS, SettlementType.INMEDIATA), BONDS_FEE)),
                    Pair.of("GD30 a 48 horas", this.netOfFees(this.getApi().exchangeRate("GD30C", "GD30", InstrumentType.BONOS, SettlementType.A48), BONDS_FEE)),
                    Pair.of("Blue compra", new MoneyAmount(this.getCriptoYaApi().blueSell(), "ARS")),
                    Pair.of("DAI compra Buenbit", new MoneyAmount(this.getCriptoYaApi().buyCoin("Buenbit", "dai", "ars", BigDecimal.ONE), "ARS")),
                    Pair.of("DAI compra Letsbit", new MoneyAmount(this.getCriptoYaApi().buyCoin("Letsbit", "dai", "ars", BigDecimal.ONE), "ARS")),
                    Pair.of("USDT compra Buenbit", new MoneyAmount(this.getCriptoYaApi().buyCoin("Buenbit", "usdt", "ars", BigDecimal.ONE), "ARS")),
                    Pair.of("USDT compra Letsbit", new MoneyAmount(this.getCriptoYaApi().buyCoin("Letsbit", "usdt", "ars", BigDecimal.ONE), "ARS"))
            ).sorted(Comparator.comparing(p -> p.getSecond().getAmount(), Comparator.reverseOrder()))
                    .forEach(p -> this.console.appendLine(this.format.text(p.getFirst(), 20), this.format.currency(p.getSecond(), 10)));

        } catch (Exception ex) {
            System.err.println("Exception " + ex.getClass().toString() + " " + ex.getMessage());
            ex.printStackTrace(System.err);
        }
    }

    public void balances() {
        this.console.appendLine(this.format.title("Balances"));
        try {

            this.getApi().balancesAndPositions().stream()
                    .forEach(this::showBalance);

        } catch (Exception ex) {
            System.err.println("Exception " + ex.getClass().toString() + " " + ex.getMessage());
            ex.printStackTrace(System.err);
        }

    }

    public void cashBalance() {
        this.console.appendLine(this.format.title("Cash Balance"));
        try {

            this.getApi().cashBalance().stream()
                    //.filter(pos -> pos.getSettlement().equals(SettlementType.INMEDIATA))
                    .forEach(this::showPosition);

        } catch (Exception ex) {
            System.err.println("Exception " + ex.getClass().toString() + " " + ex.getMessage());
            ex.printStackTrace(System.err);
        }

    }

    private void showBalance(PPIBalance balance) {
        this.console.appendLine(this.format.subtitle(balance.getCurrency()));
        balance.getAvailability().stream().forEach(this::showPosition);

    }

    private void showPosition(PPIPosition position) {
        this.console.appendLine(
                MessageFormat.format("{0} {1} {2} {3}",
                        this.format.text(position.getSymbol(), 4),
                        this.format.text(position.getName(), 40),
                        this.format.number(position.getAmount(), 10),
                        this.format.text(position.getSettlement(), 10)
                )
        );
    }
}
