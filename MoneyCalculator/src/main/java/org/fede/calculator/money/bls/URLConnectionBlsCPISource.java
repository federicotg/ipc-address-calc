/*
 * Copyright (C) 2014 fede
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
package org.fede.calculator.money.bls;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import org.fede.calculator.money.NoSeriesDataFoundException;

/**
 *
 * @author fede
 */
public class URLConnectionBlsCPISource implements BlsCPISource {

    private static final String BLS_REST_API = "http://api.bls.gov/publicAPI/v1/timeseries/data/";

    @Override
    public BlsResponse getResponse(int year) throws NoSeriesDataFoundException, IOException {
        
        final BlsRequest blsRequest = new BlsRequest(CPI_SERIES_ID, year);
        String body = new ObjectMapper().writeValueAsString(blsRequest);
        URL url = new URL(BLS_REST_API);
        URLConnection con = url.openConnection();
        con.setConnectTimeout(30000);
        con.setReadTimeout(30000);
        con.setDoOutput(true);
        con.addRequestProperty("Content-Type", "application/json");
        con.addRequestProperty("Content-Length", String.valueOf(body.length()));
        try (OutputStream out = con.getOutputStream()) {
            out.write(body.getBytes("UTF-8"));
            out.flush();
        }
        
        StringBuilder sb;
        try (InputStream is = con.getInputStream()) {
            Reader reader = new InputStreamReader(is, "UTF-8");
            sb = new StringBuilder(1000);
            char[] buffer = new char[4096];
            int read = reader.read(buffer);
            while(read > 0){
                sb.append(buffer, 0, read);
                read = reader.read(buffer);
            }
        }
        return new ObjectMapper().readValue(sb.toString(), BlsResponse.class);

    }

}
