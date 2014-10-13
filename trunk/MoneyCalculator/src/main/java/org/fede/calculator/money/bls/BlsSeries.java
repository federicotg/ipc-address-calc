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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author fede
 */
public class BlsSeries {

    private String seriesID;
    private List<BlsCpiDataPoint> data;

    public boolean hasId(String seriesId) {
        return seriesId.equals(this.seriesID);
    }

    public BlsCpiDataPoint getDataPoint(int year) {
        return this.getDataPoint(year, 13);
    }

    public BlsCpiDataPoint getDataPoint(int year, int month) {

        final String period = "M"
                + (month < 10 ? "0" : "")
                + String.valueOf(month);

        for (BlsCpiDataPoint dp : this.data) {
            if (dp.matchesYearMonth(year, period)) {
                return dp;
            }
        }
        return null;
    }

    public String getSeriesID() {
        return seriesID;
    }

    public void setSeriesID(String seriesID) {
        this.seriesID = seriesID;
    }

    public List<BlsCpiDataPoint> getData() {
        return data;
    }

    public void setData(List<BlsCpiDataPoint> data) {
        this.data = data;
    }

    public int getLastAvailableMonth() {
        List<String> periods = new ArrayList<>(12);
        for (BlsCpiDataPoint dp : this.data) {
            periods.add(dp.getPeriod());
        }
        Collections.sort(periods);
        return Integer.parseInt(periods.get(periods.size() - 1).substring(1), 10);
    }

}
