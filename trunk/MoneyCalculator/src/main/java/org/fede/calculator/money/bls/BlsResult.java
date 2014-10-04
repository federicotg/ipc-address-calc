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

import java.util.List;

/**
 *
 * @author fede
 */
public class BlsResult {

    private List<BlsSeries> series;

    public BlsCpiDataPoint getDataPoint(String seriesId, int year) {
        //return this.series.stream().filter( s -> s.hasId(seriesId)).findFirst().get().getDataPoint(year);
        for (BlsSeries s : this.series) {
            if (s.hasId(seriesId)) {
                return s.getDataPoint(year);
            }
        }
        return null;
    }

    public BlsCpiDataPoint getDataPoint(String seriesId, int year, int month) {

        //return this.series.stream().filter( s -> s.hasId(seriesId)).findFirst().get().getDataPoint(year, month);
        for (BlsSeries s : this.series) {
            if (s.hasId(seriesId)) {
                return s.getDataPoint(year, month);
            }
        }
        return null;
    }

    public List<BlsSeries> getSeries() {
        return series;
    }

    public void setSeries(List<BlsSeries> series) {
        this.series = series;
    }

}
