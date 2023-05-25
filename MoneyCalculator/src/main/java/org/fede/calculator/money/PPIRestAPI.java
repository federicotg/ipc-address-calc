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
package org.fede.calculator.money;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.text.MessageFormat;
import java.util.Properties;

/**
 *
 * @author federicogentile
 */
public class PPIRestAPI {

    private static final String PPI_API = "https://clientapi.portfoliopersonal.com";
    private static final String LOGIN = "{0}/api/{1}/Account/LoginApi";
    private static final String REFRESH_TOKEN = "{0}/api/{1}/Account/RefreshToken";
    private static final String CURRENT_MARKET_DATA = "{0}/api/{1}/MarketData/Current?ticker={2}&type={3}&settlement={4}";

    private static final String VERSION = "1.0";
    private static final String AUTHORIZED_CLIENT_HEADER = "AuthorizedClient";
    private static final String CLIENT_KEY_HEADER = "ClientKey";
    private static final String API_KEY_HEADER = "ApiKey";
    private static final String API_SECRET_HEADER = "ApiSecret";
    private static final String TOKEN_FILE_PATH = "ppi.token";
    private static final String AUTHORIZATION_HEADER_NAME = "Authorization";

    private final Properties config;
    private final ObjectMapper jsonMapper;
    private PPIToken token;

    public PPIRestAPI() throws FileNotFoundException, IOException, URISyntaxException, InterruptedException {

        try (var is = new FileInputStream(new File(System.getenv("HOME") + File.separator + "Sync" + File.separator + "ppi-secrets.properties"))) {
            this.config = new Properties();
            this.config.load(is);
        }

        this.jsonMapper = new ObjectMapper();
        this.jsonMapper.registerModule(new JavaTimeModule());

    }

    private PPIToken login() throws IOException, InterruptedException, URISyntaxException {

        final var loginRequest = HttpRequest.newBuilder()
                .uri(new URI(MessageFormat.format(LOGIN, PPI_API, VERSION)))
                .header(AUTHORIZED_CLIENT_HEADER, this.config.getProperty("ppi.authotizedclient"))
                .header(CLIENT_KEY_HEADER, this.config.getProperty("ppi.clientkey"))
                .header(API_KEY_HEADER, this.config.getProperty("ppi.public"))
                .header("Content-Type", "application/json")
                .version(HttpClient.Version.HTTP_1_1)
                .header(API_SECRET_HEADER, this.config.getProperty("ppi.private"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
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
                .header(AUTHORIZED_CLIENT_HEADER, this.config.getProperty("ppi.authotizedclient"))
                .header(CLIENT_KEY_HEADER, this.config.getProperty("ppi.clientkey"))
                .header("Content-Type", "application/json")
                .header(AUTHORIZATION_HEADER_NAME, token.asAuthorizationHeader())
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
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

        if (this.token == null) {
            this.token = this.login();
        } else {
            this.token = this.refresh(this.token);
        }

        return this.token;

    }

    private PPIMarketData marketData(String ticker, InstrumentType type, SettlementType settlement) throws URISyntaxException, IOException, InterruptedException {

        final var req = HttpRequest.newBuilder()
                .uri(new URI(MessageFormat.format(CURRENT_MARKET_DATA, PPI_API, VERSION, ticker, type.toString(), settlement.toString())))
                .header(AUTHORIZED_CLIENT_HEADER, this.config.getProperty("ppi.authotizedclient"))
                .header(CLIENT_KEY_HEADER, this.config.getProperty("ppi.clientkey"))
                .header(AUTHORIZATION_HEADER_NAME, this.getToken().asAuthorizationHeader())
                .header("Content-Type", "application/json")
                .version(HttpClient.Version.HTTP_1_1)
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(req, BodyHandlers.ofString());

        if (response.statusCode() == 200) {

            return this.jsonMapper.readValue(response.body(), PPIMarketData.class);

        } else {
            throw new IOException(MessageFormat.format("Could read market data: {0} {1}", response.statusCode(), response.body()));
        }

    }

    public MoneyAmount exchangeRate(
            String tickerUSD,
            String tickerARS,
            InstrumentType type,
            SettlementType settlement) throws URISyntaxException, IOException, InterruptedException {

        final var usdPrice = this.marketData(tickerUSD, type, settlement).getPrice();
        final var arsPrice = this.marketData(tickerARS, type, settlement).getPrice();
        return new MoneyAmount(arsPrice.divide(usdPrice, MathConstants.C), "ARS");
    }
}
