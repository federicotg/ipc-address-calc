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
package org.fede.calculator.fmp;

import org.fede.calculator.service.ETF;
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
import org.fede.calculator.money.series.SeriesReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author fede
 */
public class FinancialModelingPrep implements ETF {

    private static final Logger LOGGER = LoggerFactory.getLogger(FinancialModelingPrep.class);

    private static final String LIST_URI = "https://financialmodelingprep.com/stable/batch-etf-quotes?apikey={0}";

    // "https://financialmodelingprep.com/api/v3/etf/list?apikey={0}";
    
    private static final Set<String> ETFS = Set.of(MEUS, CSPX, EIMI, XRSU, MEUD, IWDA, RTWO);

    private final Supplier<HttpClient> clientSupplier;
    private final ObjectMapper om;
    private final Properties config;

    public FinancialModelingPrep(
            ObjectMapper om,
            Supplier<HttpClient> clientSupplier) throws IOException {
        this.clientSupplier = clientSupplier;
        this.om = om;
        try (var is = new FileInputStream(new File(SeriesReader.SECRETS))) {
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

    @Override
    public Map<String, FMPPriceData> etfs() {
        try {
            final var apiKey = this.config.getProperty("fmg.apikey");

            final var req = this.requestBuilderFor(MessageFormat.format(LIST_URI, apiKey))
                    .GET()
                    .build();

            HttpResponse<InputStream> response = this.clientSupplier.get()
                    .send(req, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() == 200) {

                var etfs = new ArrayList<FMPPriceData>(ETFS.size());

                try (var jsonParser = om.getFactory().createParser(response.body())) {
                    JsonToken token = jsonParser.nextToken();
                    while (token != null && etfs.size() < ETFS.size()) {
                        if (token == JsonToken.START_OBJECT) {
                            var etf = jsonParser.readValueAs(FMPPriceData.class);
                            if (ETFS.contains(etf.symbol())) {
                                etfs.add(etf);
                            }
                        }
                        token = jsonParser.nextToken();
                    }
                }

                return etfs.stream().collect(Collectors.toMap(FMPPriceData::symbol, Function.identity()));

            } else {
                throw new IOException(MessageFormat.format("Could not read ETF data: {0} {1}", response.statusCode(), response.body()));
            }
        } catch (IOException | InterruptedException | URISyntaxException ex) {
            LOGGER.error("Unexpected error.", ex);
            return Map.of();
        }
    }

}
