/*
 * Copyright (C) 2022 federicogentile
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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 *
 * @author federicogentile
 */
public class CriptoyaAPI {

    private static final ObjectMapper OM = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);

    public BigDecimal euroByEth() throws IOException, InterruptedException {

        return this.buyEthWithArs().divide(this.sellEthInEur(), MathConstants.C);

    }

    public BigDecimal euroByBtc() throws IOException, InterruptedException {

        return this.buyBtcWithArs().divide(this.sellBtcInEur(), MathConstants.C);

    }

    private CriptoyaDTO read(String uri) throws IOException, InterruptedException {
        return OM.readValue(
                HttpClient.newHttpClient()
                        .send(
                                HttpRequest.newBuilder(URI.create(uri)).GET().build(),
                                HttpResponse.BodyHandlers.ofInputStream()).body(), CriptoyaDTO.class);
    }

    private BigDecimal buyEthWithArs() throws IOException, InterruptedException {

        return this.read("https://criptoya.com/api/letsbit/eth/ars").getBid();

    }

    private BigDecimal sellEthInEur() throws IOException, InterruptedException {

        return this.read("https://criptoya.com/api/letsbit/eth/eur").getAsk();

    }

    private BigDecimal buyBtcWithArs() throws IOException, InterruptedException {

        return this.read("https://criptoya.com/api/letsbit/btc/ars").getBid();

    }

    private BigDecimal sellBtcInEur() throws IOException, InterruptedException {

        return this.read("https://criptoya.com/api/letsbit/btc/eur").getAsk();

    }

}
