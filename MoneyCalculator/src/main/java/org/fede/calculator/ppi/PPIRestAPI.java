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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.text.MessageFormat;
import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;
import org.fede.calculator.money.Currency;
import org.fede.calculator.money.InstrumentType;
import org.fede.calculator.money.MathConstants;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.series.SeriesReader;

/**
 *
 * @author federicogentile
 */
public class PPIRestAPI {

    private final String PPI_API = "https://clientapi.portfoliopersonal.com";
    private final String LOGIN = "{0}/api/{1}/Account/LoginApi";
    private final String REFRESH_TOKEN = "{0}/api/{1}/Account/RefreshToken";
    private final String CURRENT_MARKET_DATA = "{0}/api/{1}/MarketData/Current?ticker={2}&type={3}&settlement={4}";
    private final String BALANCES_AND_POSITIONS = "{0}/api/{1}/Account/BalancesAndPositions?accountNumber={2}";
    private final String CASH_BALANCE = "{0}/api/{1}/Account/AvailableBalance?accountNumber={2}";

    private final String VERSION = "1.0";
    private final String AUTHORIZED_CLIENT_HEADER = "AuthorizedClient";
    private final String CLIENT_KEY_HEADER = "ClientKey";
    private final String API_KEY_HEADER = "ApiKey";
    private final String API_SECRET_HEADER = "ApiSecret";
    private final String TOKEN_FILE_PATH = "ppi.token";
    private final String AUTHORIZATION_HEADER_NAME = "Authorization";

    private final TypeReference<List<PPIBalance>> BALANCE_TR = new TypeReference<List<PPIBalance>>() {
    };
    private final TypeReference<List<PPIPosition>> POSITIONS_TR = new TypeReference<List<PPIPosition>>() {
    };

    private final Properties config;
    private final ObjectMapper jsonMapper;
    private final Supplier<HttpClient> clientSupplier;
    private PPIToken token;
    //private final Map<String, Map<InstrumentType, Map<SettlementType, PPIMarketData>>> marketDataCache = new ConcurrentHashMap<>();

    public PPIRestAPI(Supplier<HttpClient> clientSupplier) throws FileNotFoundException, IOException, URISyntaxException, InterruptedException {

        try (var is = new FileInputStream(new File(SeriesReader.SECRETS))) {
            this.config = new Properties();
            this.config.load(is);
        }

        this.jsonMapper = new ObjectMapper();
        this.jsonMapper.registerModule(new JavaTimeModule());
        this.clientSupplier = clientSupplier;
    }

    private PPIToken login() throws IOException, InterruptedException, URISyntaxException {

        final var loginRequest = HttpRequest.newBuilder()
                .uri(new URI(MessageFormat.format(LOGIN, PPI_API, VERSION)))
                .header(AUTHORIZED_CLIENT_HEADER, this.config.getProperty("ppi.authorizedclient"))
                .header(CLIENT_KEY_HEADER, this.config.getProperty("ppi.clientkey"))
                .header(API_KEY_HEADER, this.config.getProperty("ppi.public"))
                .header("Content-Type", "application/json")
                .version(HttpClient.Version.HTTP_1_1)
                .header(API_SECRET_HEADER, this.config.getProperty("ppi.private"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = this.clientSupplier.get()
                .send(loginRequest, BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            final var newToken = this.jsonMapper.readValue(response.body(), PPIToken.class);

            final var tokenFile = this.getTokenFile();

            try (var os = new FileOutputStream(tokenFile)) {
                jsonMapper.writeValue(os, newToken);
                return newToken;
            }

        } else {
            throw new IOException(MessageFormat.format("Could not perform log in: {0} {1}", response.statusCode(), response.body()));
        }
    }

    private PPIToken refresh(PPIToken token) throws IOException, InterruptedException, URISyntaxException {

        final var body = jsonMapper.writeValueAsString(new PPIRefreshTokenRequest(token.getRefreshToken()));

        final var loginRequest = HttpRequest.newBuilder()
                .uri(new URI(MessageFormat.format(REFRESH_TOKEN, PPI_API, VERSION)))
                .header(AUTHORIZED_CLIENT_HEADER, this.config.getProperty("ppi.authorizedclient"))
                .header(CLIENT_KEY_HEADER, this.config.getProperty("ppi.clientkey"))
                .header("Content-Type", "application/json")
                .header(AUTHORIZATION_HEADER_NAME, token.asAuthorizationHeader())
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = this.clientSupplier.get()
                .send(loginRequest, BodyHandlers.ofString());

        if (response.statusCode() == 200) {

            final var refreshedToken = this.jsonMapper.readValue(response.body(), PPIToken.class);

            final var tokenFile = this.getTokenFile();

            if (tokenFile.exists()) {
                tokenFile.delete();
            }

            try (var os = new FileOutputStream(tokenFile)) {
                jsonMapper.writeValue(os, refreshedToken);
                return refreshedToken;
            }

        } else {
            throw new IOException(MessageFormat.format("Could not perform refresh: {0} {1}", response.statusCode(), response.body()));
        }

    }

    private File getTokenFile() {
        return new File(System.getenv("HOME") + File.separator + TOKEN_FILE_PATH);
    }

    private PPIToken getToken() throws IOException, InterruptedException, URISyntaxException {

        if (this.token == null) {
            final var tokenFile = this.getTokenFile();
            if (tokenFile.exists()) {
                this.token = this.jsonMapper.readValue(tokenFile, PPIToken.class);
            }
        }

        if (this.token != null && this.token.isValid()) {
            return this.token;
        }

        this.token = this.login();
        return this.token;
    }

    private HttpRequest.Builder requestBuilderFor(String uri) throws URISyntaxException, IOException, InterruptedException {
        return HttpRequest.newBuilder()
                .uri(new URI(uri))
                .header(AUTHORIZED_CLIENT_HEADER, this.config.getProperty("ppi.authorizedclient"))
                .header(CLIENT_KEY_HEADER, this.config.getProperty("ppi.clientkey"))
                .header(AUTHORIZATION_HEADER_NAME, this.getToken().asAuthorizationHeader())
                .header("Content-Type", "application/json")
                .version(HttpClient.Version.HTTP_1_1);

    }

    private PPIMarketData marketData(String ticker, InstrumentType type, SettlementType settlement) throws URISyntaxException, IOException, InterruptedException {

//        final var answer = this.marketDataCache.computeIfAbsent(ticker, t -> new ConcurrentHashMap<>())
//                .computeIfAbsent(type, t -> new ConcurrentHashMap<>())
//                .get(settlement);
//
//        if (answer != null) {
//            System.out.print("+");
//            return answer;
//        }

        //System.out.print("-");
        final var req = this.requestBuilderFor(MessageFormat.format(CURRENT_MARKET_DATA, PPI_API, VERSION, ticker, type.toString(), settlement.toString()))
                .GET()
                .build();

        HttpResponse<String> response = this.clientSupplier.get()
                .send(req, BodyHandlers.ofString());

        if (response.statusCode() == 200) {

            final var newAnswer = this.jsonMapper.readValue(response.body(), PPIMarketData.class);

//            this.marketDataCache.computeIfAbsent(ticker, t -> new ConcurrentHashMap<>())
//                    .computeIfAbsent(type, t -> new ConcurrentHashMap<>())
//                    .put(settlement, newAnswer);

            return newAnswer;

        } else {
            throw new IOException(MessageFormat.format("Could not read market data: {0} {1}", response.statusCode(), response.body()));
        }

    }

    public List<PPIBalance> balancesAndPositions() throws URISyntaxException, IOException, InterruptedException {

        final var req = this.requestBuilderFor(MessageFormat.format(BALANCES_AND_POSITIONS, PPI_API, VERSION, this.config.get("ppi.accountnumber")))
                .GET()
                .build();

        HttpResponse<String> response = this.clientSupplier.get()
                .send(req, BodyHandlers.ofString());

        if (response.statusCode() == 200) {

            return this.jsonMapper.readValue(response.body(), BALANCE_TR);

        } else {
            throw new IOException(MessageFormat.format("Could not balances data: {0} {1}", response.statusCode(), response.body()));
        }

    }

    public List<PPIPosition> cashBalance() throws URISyntaxException, IOException, InterruptedException {

        final var req = this.requestBuilderFor(MessageFormat.format(CASH_BALANCE, PPI_API, VERSION, this.config.get("ppi.accountnumber")))
                .GET()
                .build();

        HttpResponse<String> response = this.clientSupplier.get()
                .send(req, BodyHandlers.ofString());

        if (response.statusCode() == 200) {

            return this.jsonMapper.readValue(response.body(), POSITIONS_TR);

        } else {
            throw new IOException(MessageFormat.format("Could not balances data: {0} {1}", response.statusCode(), response.body()));
        }

    }

    public MoneyAmount exchangeRate(PPIFXParams params) throws URISyntaxException, IOException, InterruptedException {
        final var netUSD = BigDecimal.ONE.add(params.fees().usdFee(), MathConstants.C);
        final var netSecond = BigDecimal.ONE.subtract(this.secondFee(params.fees(), params.currency()), MathConstants.C);
        final var usdPrice = this.marketData(params.tickerUSD(), params.type(), params.settlement()).price()
                .multiply(netUSD, MathConstants.C);
        final var arsPrice = this.marketData(params.tickerARS(), params.type(), params.settlement()).price()
                .multiply(netSecond, MathConstants.C);
        return new MoneyAmount(arsPrice.divide(usdPrice, MathConstants.C), Currency.valueOf(params.currency()));
    }

    private BigDecimal secondFee(PPIFXFee fees, String currency) {
        return "ARS".equals(currency)
                ? fees.arsFee()
                : fees.usdFee();
    }

    public record PPIFXFee(BigDecimal usdFee, BigDecimal arsFee) {

        public PPIFXFee(BigDecimal fee) {
            this(fee, fee);
        }

    }

    public record PPIFXParams(
            String tickerUSD,
            String tickerARS,
            InstrumentType type,
            SettlementType settlement,
            String currency,
            PPIFXFee fees) {

        public PPIFXParams(String tickerUSD, String tickerARS, InstrumentType type, SettlementType settlement, PPIFXFee fees) {
            this(tickerUSD, tickerARS, type, settlement, "ARS", fees);
        }

    }

}
