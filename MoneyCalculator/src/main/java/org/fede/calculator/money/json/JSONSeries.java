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
package org.fede.calculator.money.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.fede.calculator.money.series.JSONIndexSeries;

/**
 *
 * @author fede
 */
public class JSONSeries {

    public static JSONSeries readSeries(String name) {
        try (InputStream is = JSONIndexSeries.class.getResourceAsStream("/" + name)) {
            return new ObjectMapper().readValue(is, JSONSeries.class);
        } catch (IOException ioEx) {
            throw new IllegalArgumentException("Could not read series named " + name, ioEx);
        }
    }

    private String currency;
    private List<JSONDataPoint> data;

    private JSONSeries(String name) {

    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrnecy(String currnecy) {
        this.currency = currnecy;
    }

    public List<JSONDataPoint> getData() {
        return data;
    }

    public void setData(List<JSONDataPoint> data) {
        this.data = data;
    }

}
