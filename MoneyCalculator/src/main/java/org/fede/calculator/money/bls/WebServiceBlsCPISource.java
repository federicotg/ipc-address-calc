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
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.fede.calculator.money.NoIndexDataFoundException;

/**
 *
 * @author fede
 */
public class WebServiceBlsCPISource implements BlsCPISource {

    private static final String BLS_REST_API = "http://api.bls.gov/publicAPI/v1/timeseries/data/";
    
    private final ResponseHandler<BlsResponse> responseHandler = new ResponseHandler<BlsResponse>() {

        @Override
        public BlsResponse handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
            return new ObjectMapper().readValue(response.getEntity().getContent(), BlsResponse.class);
        }
    };

    @Override
    public BlsResponse getResponse(int year) throws NoIndexDataFoundException, IOException {
        HttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(BLS_REST_API);
        final BlsRequest blsRequest = new BlsRequest(CPI_SERIES_ID, year);
        StringEntity entity = new StringEntity(new ObjectMapper().writeValueAsString(blsRequest));
        entity.setContentType("application/json");
        post.setEntity(entity);
        BlsResponse blsResponse = client.execute(post, responseHandler);
        if (!blsResponse.isValid()) {
            List<String> messages = blsResponse.getMessage();
            String message = "Invalid webservice response.";
            if (!messages.isEmpty()) {
                message = messages.get(0);
            }
            throw new NoIndexDataFoundException(message);
        }
        return blsResponse;
    }

}
