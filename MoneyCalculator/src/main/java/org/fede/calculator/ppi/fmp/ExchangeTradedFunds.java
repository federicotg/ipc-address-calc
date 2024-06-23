/*
 * Copyright (C) 2024 fede
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
package org.fede.calculator.ppi.fmp;

import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 *
 * @author fede
 */
public class ExchangeTradedFunds implements ETF {

    private static final String LIST_URI = "https://financialmodelingprep.com/api/v3/etf/list?apikey={0}";
    //private static final String USDEUR = "https://financialmodelingprep.com/api/v3/fx/EURUSD?apikey={0}";

    private static final Set<String> ETFS = Set.of(MEUS, CSPX, EIMI, XRSU, MEUD, IWDA, RTWO);

//    private static final TypeReference<List<ForeignExchange>> FX_TR = new TypeReference<List<ForeignExchange>>() {
//    };
    private final Supplier<HttpClient> clientSupplier;
    private final ObjectMapper om;
    private final Properties config;

    public ExchangeTradedFunds(
            ObjectMapper om,
            Supplier<HttpClient> clientSupplier) throws IOException {
        this.clientSupplier = clientSupplier;
        this.om = om;
        try (var is = new FileInputStream(new File(System.getenv("HOME") + File.separator + "Sync" + File.separator + "ppi-secrets.properties"))) {
            this.config = new Properties();
            this.config.load(is);
        }
    }

    private HttpRequest.Builder requestBuilderFor(String uri) throws URISyntaxException, IOException, InterruptedException {
        return HttpRequest.newBuilder()
                .uri(new URI(uri))
                .header("Content-Type", "application/json")
                .version(HttpClient.Version.HTTP_1_1);

    }


//    private ForeignExchange usdEur() throws IOException, URISyntaxException, InterruptedException {
//
//        var apiKey = this.config.getProperty("fmg.apikey");
//
//        final var req = this.requestBuilderFor(MessageFormat.format(USDEUR, apiKey))
//                .GET()
//                .build();
//
//        HttpResponse<String> response = this.clientSupplier.get()
//                .send(req, HttpResponse.BodyHandlers.ofString());
//        if (response.statusCode() == 200) {
//
//            return this.om.readValue(response.body(), FX_TR).stream().findFirst().orElseThrow();
//
//        } else {
//            throw new IOException(MessageFormat.format("Could not read FX data: {0} {1}", response.statusCode(), response.body()));
//        }
//
//    }
    @Override
    public Map<String, ExchangeTradedFundData> etfs() {
        try {
            final var apiKey = this.config.getProperty("fmg.apikey");

            final var req = this.requestBuilderFor(MessageFormat.format(LIST_URI, apiKey))
                    .GET()
                    .build();

            HttpResponse<InputStream> response = this.clientSupplier.get()
                    .send(req, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() == 200) {

                var etfs = new ArrayList<ExchangeTradedFundData>(ETFS.size());

                try (var jsonParser = om.getFactory().createParser(response.body())) {
                    JsonToken token = jsonParser.nextToken();
                    while (token != null && etfs.size() < ETFS.size()) {
                        if (token == JsonToken.START_OBJECT) {
                            var etf = jsonParser.readValueAs(ExchangeTradedFundData.class);
                            if (ETFS.contains(etf.symbol())) {
                                etfs.add(etf);
                            }
                        }
                        token = jsonParser.nextToken();
                    }
                }

                return etfs.stream().collect(Collectors.toMap(ExchangeTradedFundData::symbol, Function.identity()));

            } else {
                throw new IOException(MessageFormat.format("Could not read ETF data: {0} {1}", response.statusCode(), response.body()));
            }
        } catch (IOException | InterruptedException | URISyntaxException ex) {
            System.err.println(ex.getMessage());
            ex.printStackTrace(System.err);
            return Map.of();
        }
    }

}
