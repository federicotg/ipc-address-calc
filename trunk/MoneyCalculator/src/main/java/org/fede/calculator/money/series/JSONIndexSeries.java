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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import org.fede.calculator.money.NoSeriesDataFoundException;
import org.fede.calculator.money.json.JSONDataPoint;

/**
 *
 * @author fede
 */
public class JSONIndexSeries extends IndexSeriesSupport implements IndexSeries {

    private final List<JSONDataPoint> data;

    public JSONIndexSeries(List<JSONDataPoint> data) {
        this.data = data;
    }

    @Override
    public BigDecimal getIndex(int year, int month) throws NoSeriesDataFoundException {
        int index = Collections.binarySearch(data, new JSONDataPoint(year, month));
        if (index < 0 || index >= this.data.size()) {
            throw new NoSeriesDataFoundException("No data for specified year and month.");
        }
        return this.data.get(index).getValue();
    }

    @Override
    public BigDecimal getIndex(int year) throws NoSeriesDataFoundException {
        return this.getIndex(year, 12);
    }

    @Override
    public int getFromYear() {
        return this.data.get(0).getYear();
    }

    @Override
    public int getToYear() {
        return this.data.get(this.data.size() - 1).getYear();
    }

    @Override
    public int getFromMonth() {
        return this.data.get(0).getMonth();
    }

    @Override
    public int getToMonth() {
        return this.data.get(this.data.size() - 1).getMonth();
    }

   /*@Override
    public IndexSeries adjust(Inflation inflation) {
        return null;
    }*/


}
