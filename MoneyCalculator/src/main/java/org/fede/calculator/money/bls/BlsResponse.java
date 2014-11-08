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

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 *
 * @author fede
 */
public class BlsResponse {

    private String status;
    private long responseTime;
    private List<String> message;
    @JsonProperty(value = "Results")
    private BlsResult results;

    public boolean isValid() {
        return "REQUEST_SUCCEEDED".equals(this.status);
    }

    public BlsCpiDataPoint getDataPoint(String series, int year) {
        return this.results.getDataPoint(series, year);
    }

    public BlsCpiDataPoint getDataPoint(String series, int year, int month) {
        return this.results.getDataPoint(series, year, month);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }

    public List<String> getMessage() {
        return message;
    }

    public void setMessage(List<String> message) {
        this.message = message;
    }

    public BlsResult getResults() {
        return this.results;
    }

    public void setResults(BlsResult results) {
        this.results = results;
    }

    public int getLastAvailableMonth(String series) {
        return this.results.getLastAvailableMonth(series);
    }
}
