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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.fede.calculator.money.NoSeriesDataFoundException;

/**
 *
 * @author fede
 */
public class CachingBlsSource implements BlsCPISource {

    private final BlsCPISource source;

    private final Map<Integer, BlsResponse> cache;

    public CachingBlsSource(BlsCPISource source) {
        this.source = source;
        this.cache = new HashMap<>();
    }

    @Override
    public BlsResponse getResponse(int year) throws IOException {
        BlsResponse response = this.cache.get(year);
        if (response == null) {
            response = this.source.getResponse(year);
            if (response != null) {
                this.cache.put(year, response);
            }
        }
        return response;
    }

}
