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
import static java.math.BigDecimal.ONE;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.fede.calculator.criptoya.CriptoYaAPI;
import org.fede.calculator.money.Console;
import org.fede.calculator.money.Format;
import static org.fede.calculator.money.InstrumentType.*;
import static org.fede.calculator.money.MathConstants.C;
import org.fede.calculator.money.MoneyAmount;
import static org.fede.calculator.money.SettlementType.*;
import static org.fede.calculator.ppi.PPIRestAPI.PPIFXFee;
import static org.fede.calculator.ppi.PPIRestAPI.PPIFXParams;
import org.fede.util.Pair;

/**
 *
 * @author federicogentile
 */
public class PPI {

    private static final int LABEL_WIDTH = 30;

    private static final BigDecimal BONDS_FEE = new BigDecimal("0.006");
    private static final BigDecimal LETES_FEE = new BigDecimal("0.0015");
    private static final BigDecimal LETES_USD_NEW = new BigDecimal("0.01");
    private static final BigDecimal LETES_ARS_NEW = new BigDecimal("0.002");
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

    private Pair<String, MoneyAmount> item(String desc, BigDecimal value, String currency) {
        return Pair.of(this.dim(desc, LABEL_WIDTH), new MoneyAmount(value, currency));
    }

    public void dollar() {

        this.console.appendLine(this.format.title("Dólar"));

        try {

            final var blue = new MoneyAmount(this.getCriptoYaApi().blueSell(), "ARS");

            final var letra1 = "SO3";

            /*System.out.println(this.getCriptoYaApi().buyCoin("Buenbit", "USDT", "USD", ONE));
            System.out.println(this.getCriptoYaApi().sellCoin("Buenbit", "USDT", "ARS", ONE));*/
            final var oldFee = new PPIFXFee(LETES_FEE);
            final var newFee = new PPIFXFee(LETES_USD_NEW, LETES_ARS_NEW);

            final List<Pair<String, PPIRestAPI.PPIFXParams>> ppiRequests = List.of(
                    Pair.of("CCL " + letra1, new PPIFXParams(letra1 + "C", "S31O3", LETRAS, INMEDIATA, oldFee)),
                    Pair.of("CCL " + letra1 + " nov.", new PPIFXParams(letra1 + "C", "S31O3", LETRAS, INMEDIATA, newFee)),
                    Pair.of("MEP " + letra1, new PPIFXParams(letra1 + "D", "S31O3", LETRAS, INMEDIATA, oldFee)),
                    Pair.of("MEP " + letra1 + " nov.", new PPIFXParams(letra1 + "D", "S31O3", LETRAS, INMEDIATA, newFee)),
                    Pair.of("C a D " + letra1, new PPIFXParams(letra1 + "C", "S31O3", LETRAS, INMEDIATA, oldFee)),
                    Pair.of("C a D " + letra1 + " nov.", new PPIFXParams(letra1 + "C", letra1 + "D", LETRAS, INMEDIATA, "USD", oldFee)));

            List<Future<Pair<String, MoneyAmount>>> ppiFutures = new ArrayList<>(ppiRequests.size());
            try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {

                for (var p : ppiRequests) {
                    ppiFutures.add(executor.submit(() -> Pair.of(p.first(), this.getApi().exchangeRate(p.second()))));
                }
            }

            List<Pair<String, MoneyAmount>> results
                    = Stream.of(
                            Pair.of(this.blue("Blue (Venta)", LABEL_WIDTH), blue),
                            item("BuenBit USDT (Venta)", this.getCriptoYaApi().sellCoin("Buenbit", "USDT", "ARS", ONE).divide(this.getCriptoYaApi().buyCoin("Buenbit", "USDT", "USD", ONE), C), "ARS"),
                            item("BuenBit DAI (Venta)", this.getCriptoYaApi().sellCoin("Buenbit", "DAI", "ARS", ONE).divide(this.getCriptoYaApi().buyCoin("Buenbit", "DAI", "USD", ONE), C), "ARS"),
                            item("Letsbit USDT (Venta)", this.getCriptoYaApi().sellCoin("Letsbit", "USDT", "ARS", ONE).divide(this.getCriptoYaApi().buyCoin("Letsbit", "USDT", "USD", ONE), C), "ARS"),
                            item("Letsbit DAI (Venta)", this.getCriptoYaApi().sellCoin("Letsbit", "DAI", "ARS", ONE).divide(this.getCriptoYaApi().buyCoin("Letsbit", "DAI", "USD", ONE), C), "ARS")
                    /*item("BuenBit USDT (Compra)", this.getCriptoYaApi().buyCoin("Buenbit", "USDT", "ARS", ONE).divide(this.getCriptoYaApi().sellCoin("Buenbit", "USDT", "USD", ONE), C), "ARS"),
                    item("BuenBit DAI (Compra)", this.getCriptoYaApi().buyCoin("Buenbit", "DAI", "ARS", ONE).divide(this.getCriptoYaApi().sellCoin("Buenbit", "DAI", "USD", ONE), C), "ARS"),
                    item("Letsbit USDT (Compra)", this.getCriptoYaApi().buyCoin("Letsbit", "USDT", "ARS", ONE).divide(this.getCriptoYaApi().sellCoin("Letsbit", "USDT", "USD", ONE), C), "ARS"),
                    item("Letsbit DAI (Compra)", this.getCriptoYaApi().buyCoin("Letsbit", "DAI", "ARS", ONE).divide(this.getCriptoYaApi().sellCoin("Letsbit", "DAI", "USD", ONE), C), "ARS"),
                    
                    item("USD BuenBit USDT (Compra)", this.getCriptoYaApi().buyCoin("Buenbit", "USDT", "ARS", ONE).divide(this.getCriptoYaApi().sellCoin("Buenbit", "USDT", "USD", ONE), C), "ARS")*/
                    ).toList();

            var ppiResults = new ArrayList<Pair<String, MoneyAmount>>(ppiFutures.size());
            for (var f : ppiFutures) {
                ppiResults.add(f.get());
            }
            Stream.concat(ppiResults.stream(), results.stream())
                    .sorted(Comparator.comparing(p -> p.second().getAmount(), Comparator.reverseOrder()))
                    .forEach(p -> this.console.appendLine(this.format.text(p.first(), LABEL_WIDTH), this.format.currency(p.second(), 10)));

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
                    //.filter(pos -> pos.getSettlement().equals(INMEDIATA.toString()))
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
