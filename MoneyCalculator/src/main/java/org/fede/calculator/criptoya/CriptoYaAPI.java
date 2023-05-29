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
package org.fede.calculator.criptoya;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.MessageFormat;
import java.util.Map;
import org.fede.calculator.money.MathConstants;

/**
 *
 * @author federicogentile
 */
public class CriptoYaAPI {

    private static final String API = "https://criptoya.com";

    private final ObjectMapper jsonMapper;

    private Map<String, Map<String, Map<String, BigDecimal>>> fees;

    public CriptoYaAPI() {
        this.jsonMapper = new ObjectMapper();
        this.jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private HttpRequest.Builder requestBuilderFor(String uri) throws URISyntaxException, IOException, InterruptedException {
        return HttpRequest.newBuilder()
                .uri(new URI(uri))
                .header("Content-Type", "application/json")
                .version(HttpClient.Version.HTTP_1_1);

    }

    private BigDecimal blueSell() throws URISyntaxException, IOException, InterruptedException {

        final var req = this.requestBuilderFor(MessageFormat.format("{0}/api/dolar", API))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(req, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {

            final var b = response.body();

            return this.jsonMapper.readValue(b, new TypeReference<Map<String, BigDecimal>>() {
            }).get("blue_bid");

        } else {
            throw new IOException(MessageFormat.format("Could not read criptoya data: {0} {1}", response.statusCode(), response.body()));
        }

    }

    private BigDecimal fee(String exchange, String currency, String network) throws URISyntaxException, IOException, InterruptedException {

        if (this.fees == null) {

            final var req = this.requestBuilderFor(MessageFormat.format("{0}/api/fees", API))
                    .GET()
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(req, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {

                this.fees = this.jsonMapper.readValue(response.body(), new TypeReference<Map<String, Map<String, Map<String, BigDecimal>>>>() {
                });

            } else {
                throw new IOException(MessageFormat.format("Could not read criptoya data: {0} {1}", response.statusCode(), response.body()));
            }
        }
        return this.fees.get(exchange).get(currency).get(network);

    }

    private BigDecimal buyCoin(String exchange, String coin, String fiat, BigDecimal amount) throws URISyntaxException, IOException, InterruptedException {
        return this.fx(exchange, coin, fiat, amount).getTotalAsk();
    }

    private BigDecimal sellCoin(String exchange, String coin, String fiat, BigDecimal amount) throws URISyntaxException, IOException, InterruptedException {
        return this.fx(exchange, coin, fiat, amount).getTotalBid();
    }

    private CriptoYaFx fx(String exchange, String coin, String fiat, BigDecimal amount) throws URISyntaxException, IOException, InterruptedException {

        // "/api/letsbit/usdt/ars/1000"
        final var req = this.requestBuilderFor(MessageFormat.format("{0}/api/{1}/{2}/{3}/{4}", API, exchange, coin.toLowerCase(), fiat.toLowerCase(), amount.setScale(0, RoundingMode.HALF_UP).toString()))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(req, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {

            final var b = response.body();
            return this.jsonMapper.readValue(b, CriptoYaFx.class);

        } else {
            throw new IOException(MessageFormat.format("Could not read criptoya data: {0} {1}", response.statusCode(), response.body()));
        }

    }

    public BigDecimal lbRoute(BigDecimal initialAmount) throws URISyntaxException, IOException, InterruptedException {
        return initialAmount.divide(this.buyCoin("letsbit", "USDT", "USD", initialAmount), MathConstants.C)
                .subtract(this.fee("Letsbit", "USDT", "TRON"))
                .divide(new BigDecimal("1.002"), MathConstants.C);
    }

    public BigDecimal bbRoute(BigDecimal initialAmount) throws URISyntaxException, IOException, InterruptedException {
        return initialAmount.divide(this.buyCoin("buenbit", "DAI", "USD", initialAmount), MathConstants.C)
                .subtract(this.fee("Buenbit", "DAI", "ERC20"))
                .divide(new BigDecimal("1.002"), MathConstants.C);
    }

    public BigDecimal arsLbRoute(BigDecimal initialAmount, BigDecimal blueFee) throws URISyntaxException, IOException, InterruptedException {
        return initialAmount
                .divide(blueFee, MathConstants.C)
                .multiply(this.blueSell(), MathConstants.C)
                .divide(this.buyCoin("letsbit", "usdt", "ars", initialAmount), MathConstants.C)
                .subtract(this.fee("Letsbit", "USDT", "TRON"))
                .divide(new BigDecimal("1.002"), MathConstants.C);
    }

    public BigDecimal arsLbDaiRoute(BigDecimal initialAmount, BigDecimal blueFee) throws URISyntaxException, IOException, InterruptedException {
        return initialAmount
                .divide(blueFee, MathConstants.C)
                .multiply(this.blueSell(), MathConstants.C)
                .divide(this.buyCoin("letsbit", "dai", "ars", initialAmount), MathConstants.C)
                .subtract(this.fee("Letsbit", "DAI", "ERC20"))
                .divide(new BigDecimal("1.002"), MathConstants.C);
    }

    public BigDecimal arsBbRoute(BigDecimal initialAmount, BigDecimal blueFee) throws URISyntaxException, IOException, InterruptedException {
        return initialAmount
                .divide(blueFee, MathConstants.C)
                .multiply(this.blueSell(), MathConstants.C)
                .divide(this.buyCoin("buenbit", "dai", "ars", initialAmount), MathConstants.C)
                .subtract(this.fee("Buenbit", "DAI", "ERC20"))
                .divide(new BigDecimal("1.002"), MathConstants.C);
    }

}
