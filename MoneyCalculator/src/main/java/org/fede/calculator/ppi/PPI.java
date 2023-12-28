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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import org.fede.calculator.criptoya.CriptoYaAPI;
import org.fede.calculator.money.Console;
import org.fede.calculator.money.Format;
import static org.fede.calculator.money.InstrumentType.*;
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

    private static final BigDecimal LETES_USD_NEW = new BigDecimal("0.01");
    private static final BigDecimal LETES_ARS_NEW = new BigDecimal("0.002");

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

    private Pair<String, Future<MoneyAmount>> criptoYaItem(String desc, CriptoYaAPI.CriptoYaFXParams params, ExecutorService executor) {
        return Pair.of(this.dim(desc, LABEL_WIDTH), executor.submit(() -> this.getCriptoYaApi().exchangeRate(params)));
    }

    private Pair<String, Future<MoneyAmount>> ppiItem(String key, PPIRestAPI.PPIFXParams params, ExecutorService executor) {
        return Pair.of(key, executor.submit(() -> this.getApi().exchangeRate(params)));
    }

    public void dollar() {

        this.console.appendLine(this.format.title("Dólar"));

        try {

            final var letra1 = "XE4";

            final var newFee = new PPIFXFee(LETES_USD_NEW, LETES_ARS_NEW);
            final var bondsFee = new PPIFXFee(new BigDecimal(0.006), new BigDecimal(0.006));

            List<Pair<String, Future<MoneyAmount>>> futures = new ArrayList<>(32);
            try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {

                futures.add(this.ppiItem("C a D AL30 CI", new PPIFXParams("AL30C", "AL30D", BONOS, INMEDIATA, "USD", bondsFee), executor));
                futures.add(this.ppiItem("C a D GD30 CI", new PPIFXParams("GD30C", "GD30D", BONOS, INMEDIATA, "USD", bondsFee), executor));

                //futures.add(this.ppiItem("C a D AL30 48", new PPIFXParams("AL30C", "AL30D", BONOS, A48, "USD", bondsFee), executor));
                //futures.add(this.ppiItem("C a D GD30 48", new PPIFXParams("GD30C", "GD30D", BONOS, A48, "USD", bondsFee), executor));

                futures.add(this.ppiItem("CCL " + letra1 + " CI", new PPIFXParams(letra1 + "C", "X18E4", LETRAS, INMEDIATA, newFee), executor));
                futures.add(this.ppiItem("CCL AL30 CI", new PPIFXParams("AL30C", "AL30", BONOS, INMEDIATA, newFee), executor));
                futures.add(this.ppiItem("CCL GD30 CI", new PPIFXParams("GD30C", "GD30", BONOS, INMEDIATA, newFee), executor));

                //futures.add(this.ppiItem("CCL AL30 48", new PPIFXParams("AL30C", "AL30", BONOS, A48, newFee), executor));
                //futures.add(this.ppiItem("CCL GD30 48", new PPIFXParams("GD30C", "GD30", BONOS, A48, newFee), executor));

                futures.add(this.ppiItem("MEP " + letra1 + " CI", new PPIFXParams(letra1 + "D", "X18E4", LETRAS, INMEDIATA, newFee), executor));
                futures.add(this.ppiItem("C a D " + letra1 + " CI", new PPIFXParams(letra1 + "C", letra1 + "D", LETRAS, INMEDIATA, "USD", newFee), executor));
                //futures.add(this.ppiItem("C a D " + letra1 + " 48", new PPIFXParams(letra1 + "C", letra1 + "D", LETRAS, A48, "USD", newFee), executor));

                //futures.add(this.criptoYaItem("BuenBit USDT (Venta)", new CriptoYaFXParams("Buenbit", "USDT", "USD", "ARS", "ARS"), executor));
                //futures.add(this.criptoYaItem("BuenBit DAI (Venta)", new CriptoYaFXParams("Buenbit", "DAI", "USD", "ARS", "ARS"), executor));
                //futures.add(this.criptoYaItem("Letsbit USDT (Venta)", new CriptoYaFXParams("Letsbit", "USDT", "USD", "ARS", "ARS"), executor));
                //futures.add(this.criptoYaItem("Letsbit DAI (Venta)", new CriptoYaFXParams("Letsbit", "DAI", "USD", "ARS", "ARS"), executor));
                futures.add(Pair.of(this.blue("Blue (Venta)", LABEL_WIDTH), executor.submit(() -> new MoneyAmount(this.getCriptoYaApi().blueSell(), "ARS"))));
            }

            var results = new ArrayList<Pair<String, MoneyAmount>>(futures.size());
            for (var f : futures) {
                results.add(Pair.of(f.first(), f.second().get()));
            }

            results.stream()
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
