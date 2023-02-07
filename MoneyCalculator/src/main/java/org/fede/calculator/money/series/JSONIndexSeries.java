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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author fede
 */
public class JSONIndexSeries extends IndexSeriesSupport {

    private final Map<Integer, Map<Integer, BigDecimal>> data;
    private final YearMonth from;
    private final YearMonth to;

    public JSONIndexSeries(List<JSONDataPoint> data) {
        JSONDataPoint first = data.get(0);
        this.from = YearMonth.of(first.getYear(), first.getMonth());
        JSONDataPoint last = data.get(data.size() - 1);
        this.to = YearMonth.of(last.getYear(), last.getMonth());

        this.data = new ConcurrentHashMap<>(data.size());
        for (var dp : data) {
            var yearMap = this.data.computeIfAbsent(dp.getYear(), y -> new ConcurrentHashMap<>(12));
            yearMap.put(dp.getMonth(), dp.getValue());
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

}
