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
import java.util.Map;
import java.util.SequencedCollection;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author fede
 */
public class JSONIndexSeries extends IndexSeriesSupport {

    private final Map<Integer, Map<Integer, BigDecimal>> data;
    private final YearMonth from;
    private final YearMonth to;

    public JSONIndexSeries(SequencedCollection<JSONDataPoint> data) {
        JSONDataPoint first = data.getFirst();
        this.from = YearMonth.of(first.year(), first.month());
        JSONDataPoint last = data.getLast();
        this.to = YearMonth.of(last.year(), last.month());

        this.data = new ConcurrentHashMap<>();
        for (var dp : data) {
            var yearMap = this.data.computeIfAbsent(dp.year(), y -> new ConcurrentHashMap<>());
            yearMap.put(dp.month(), dp.value());
        }
    }

    @Override
    public BigDecimal getIndex(int year, int month) {

        if (YearMonth.of(year, month).compareTo(this.getTo()) > 0) {
            return this.data.get(this.to.getYear()).get(this.getTo().getMonth());
        }

        return this.data.get(year).get(month);

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

        var y = this.data.get(ym.year());
        if (y == null) {
            y = new ConcurrentHashMap<>();
            this.data.put(ym.year(), y);
        }
        y.put(ym.month(), value);

    }

}
