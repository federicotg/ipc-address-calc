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
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import java.util.SequencedCollection;

/**
 *
 * @author fede
 */
public class JSONIndexSeries extends IndexSeriesSupport {

    private final Map<YearMonth, BigDecimal> data;
    private final YearMonth from;
    private final YearMonth to;

    public JSONIndexSeries(SequencedCollection<JSONDataPoint> datapoints) {
        JSONDataPoint first = datapoints.getFirst();
        this.from = first.yearMonth();
        JSONDataPoint last = datapoints.getLast();
        this.to = last.yearMonth();

        this.data = HashMap.newHashMap(datapoints.size());

        for (var d : datapoints) {
            this.data.put(d.yearMonth(), d.value());
        }
    }

    @Override
    public BigDecimal getIndex(YearMonth ym) {
        return this.data.get(YearMonthUtil.min(ym, this.to));

    }

    @Override
    public YearMonth getFrom() {
        return this.from;
    }

    @Override
    public YearMonth getTo() {
        return this.to;
    }

    public void put(YearMonth ym, BigDecimal value) {

        this.data.put(ym, value);

    }

}
