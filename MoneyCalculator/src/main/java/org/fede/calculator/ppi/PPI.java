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

import com.diogonunes.jcolor.AnsiFormat;
import com.diogonunes.jcolor.Attribute;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.fede.calculator.criptoya.CriptoYaAPI;
import org.fede.calculator.money.Console;
import org.fede.calculator.money.Format;
import static org.fede.calculator.money.InstrumentType.*;
import org.fede.calculator.money.MathConstants;
import org.fede.calculator.money.MoneyAmount;
import static org.fede.calculator.money.SettlementType.*;
import org.fede.util.Pair;

/**
 *
 * @author federicogentile
 */
public class PPI {

    private static final BigDecimal BONDS_FEE = new BigDecimal("0.006");
    private static final BigDecimal LETES_FEE = new BigDecimal("0.0015");
    private static final BigDecimal SMALL_FACE_FEE = new BigDecimal("0.03");

    private static final AnsiFormat DIM_WHITE_TEXT = new AnsiFormat(Attribute.DIM());
    private static final AnsiFormat BLUE_TEXT = new AnsiFormat(Attribute.BRIGHT_GREEN_TEXT());

    private final Console console;
    private final Format format;
    private PPIRestAPI api;
    private CriptoYaAPI criptoya;
    private final Supplier<HttpClient> clientSupplier;

    public PPI(Console console, Format format, Supplier<HttpClient> clientSupplier) {
        this.console = console;
        this.format = format;
        this.clientSupplier = clientSupplier;
    }

    private MoneyAmount netOfPPIFees(MoneyAmount gross, BigDecimal fee) {
        return gross.adjust(
                BigDecimal.ONE,
                BigDecimal.ONE.subtract(fee.add(fee, MathConstants.C), MathConstants.C));
    }

    private MoneyAmount netOfSimpleFees(MoneyAmount gross, BigDecimal fee) {
        return gross.adjust(
                BigDecimal.ONE,
                BigDecimal.ONE.subtract(fee, MathConstants.C));
    }

    private PPIRestAPI getApi() throws IOException, URISyntaxException, InterruptedException {
        if (this.api == null) {
            this.api = new PPIRestAPI(this.clientSupplier);
        }
        return this.api;
    }

    private CriptoYaAPI getCriptoYaApi() throws IOException, URISyntaxException, InterruptedException {
        if (this.criptoya == null) {
            this.criptoya = new CriptoYaAPI(this.clientSupplier);
        }
        return this.criptoya;
    }

    private String dim(String text, int width) {
        return this.format.text(text, width, DIM_WHITE_TEXT);
    }

    private String blue(String text, int width) {
        return this.format.text(text, width, BLUE_TEXT);
    }

    public void dollar() {

        this.console.appendLine(this.format.title("Dólar"));

        try {
            final var labelWidth = 22;

            final var blue = new MoneyAmount(this.getCriptoYaApi().blueSell(), "ARS");
            
            final var letra1 = "SL3";
            final var letra2 = "XL3";
            Stream.of(
                    Pair.of(letra1, this.netOfPPIFees(this.getApi().exchangeRate(letra1 + "C", "S31L3", LETRAS, INMEDIATA), LETES_FEE)),
                    //Pair.of(letra2, this.netOfPPIFees(this.getApi().exchangeRate(letra2 + "C", "X18L3", LETRAS, INMEDIATA), LETES_FEE)),
                    Pair.of("C a D " + letra1, this.netOfPPIFees(this.getApi().exchangeRate(letra1 + "C", letra1 + "D", LETRAS, INMEDIATA, "USD"), LETES_FEE)),
                    //Pair.of("C a D " + letra2, this.netOfPPIFees(this.getApi().exchangeRate(letra2 + "C", letra2 + "D", LETRAS, INMEDIATA, "USD"), LETES_FEE)),
                    Pair.of(this.dim("GD30 Inmediato", labelWidth), this.netOfPPIFees(this.getApi().exchangeRate("GD30C", "GD30", BONOS, INMEDIATA), BONDS_FEE)),
                    Pair.of(this.dim("GD30 a 48 horas", labelWidth), this.netOfPPIFees(this.getApi().exchangeRate("GD30C", "GD30", BONOS, A48), BONDS_FEE)),
                    Pair.of(this.blue("Blue (Venta)", labelWidth), blue),
                    Pair.of(this.blue("Blue Small (Venta)", labelWidth), this.netOfSimpleFees(blue, SMALL_FACE_FEE)),
                    Pair.of(this.dim("DAI Buenbit (Compra)", labelWidth), new MoneyAmount(this.getCriptoYaApi().buyCoin("Buenbit", "dai", "ars", BigDecimal.ONE), "ARS")),
                    Pair.of(this.dim("DAI Letsbit (Compra)", labelWidth), new MoneyAmount(this.getCriptoYaApi().buyCoin("Letsbit", "dai", "ars", BigDecimal.ONE), "ARS")),
                    Pair.of(this.dim("USDT Buenbit (Compra)", labelWidth), new MoneyAmount(this.getCriptoYaApi().buyCoin("Buenbit", "usdt", "ars", BigDecimal.ONE), "ARS")),
                    Pair.of(this.dim("USDT Letsbit (Compra)", labelWidth), new MoneyAmount(this.getCriptoYaApi().buyCoin("Letsbit", "usdt", "ars", BigDecimal.ONE), "ARS"))
            ).sorted(Comparator.comparing(p -> p.getSecond().getAmount(), Comparator.reverseOrder()))
                    .forEach(p -> this.console.appendLine(this.format.text(p.getFirst(), labelWidth), this.format.currency(p.getSecond(), 10)));

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
                    .filter(pos -> pos.getSettlement().equals(INMEDIATA.toString()))
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
