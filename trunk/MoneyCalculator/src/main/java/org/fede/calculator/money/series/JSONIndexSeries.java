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
package org.fede.calculator.money.series;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import org.fede.calculator.money.NoSeriesDataFoundException;

/**
 *
 * @author fede
 */
public class JSONIndexSeries extends IndexSeriesSupport implements IndexSeries {

    public static IndexSeries readSeries(String name) {
        try (InputStream is = JSONIndexSeries.class.getResourceAsStream("/" + name)) {
            List<JSONDataPoint> data = new ObjectMapper().readValue(is, new TypeReference<List<JSONDataPoint>>() {
            });
            return new JSONIndexSeries(data);
        } catch (IOException ioEx) {
            throw new IllegalArgumentException("Could not read series named " + name, ioEx);
        }
    }

    private final List<JSONDataPoint> data;

    public JSONIndexSeries(List<JSONDataPoint> data) {
        this.data = data;
    }

    @Override
    public BigDecimal getIndex(int year, int month) throws NoSeriesDataFoundException {
        int index = Collections.binarySearch(data, new JSONDataPoint(year, month));
        if (index < 0 || index >= this.data.size()) {
            return this.predictValue(year, month);
        }
        return this.data.get(index).getValue();
    }

    @Override
    public YearMonth getFrom() {
        JSONDataPoint point = this.data.get(0);
        return new YearMonth(point.getYear(), point.getMonth());
    }

    @Override
    public YearMonth getTo() {
        JSONDataPoint point = this.data.get(this.data.size() - 1);
        return new YearMonth(point.getYear(), point.getMonth());
    }

    @Override
    public BigDecimal predictValue(int year, int month) throws NoSeriesDataFoundException {
        if (new YearMonth(year, month).compareTo(this.getTo()) > 0) {
            //return this.data.get(this.data.size() - 1).getValue();
            return new LinearFutureValue().predictValue(this, year, month);
        }
        throw new NoSeriesDataFoundException("No data for specified year and month.");
    }

}
