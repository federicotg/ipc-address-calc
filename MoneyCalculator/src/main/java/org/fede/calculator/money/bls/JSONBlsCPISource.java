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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 *
 * @author fede
 */
public class JSONBlsCPISource implements BlsCPISource {

    private final String name;
    private List<BlsResponse> list;

    public JSONBlsCPISource(String name) {
        this.name = name;
    }

    @Override
    public BlsResponse getResponse(int year) throws IOException {
        if (this.list == null) {
            try (InputStream in = new FileInputStream(System.getProperty("user.home")+"/Sync/app-resources/" + name)) {
                this.list = new ObjectMapper().readValue(in, new TypeReference<List<BlsResponse>>() {
                });
            }
        }

        return this.list.stream()
                .filter(response -> response.getDataPoint(CPI_SERIES_ID, year, 1) != null)
                .findFirst()
                .orElse(null);

    }

}
