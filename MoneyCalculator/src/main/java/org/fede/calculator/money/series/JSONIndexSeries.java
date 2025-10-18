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
import java.util.Map;
import java.util.SequencedCollection;
import java.util.stream.Collectors;

/**
 *
 * @author fede
 */
public class JSONIndexSeries extends IndexSeriesSupport {

    private final Map<YearMonth, BigDecimal> data;
    private final YearMonth from;
    private final YearMonth to;

    public JSONIndexSeries(SequencedCollection<JSONDataPoint> data) {
        JSONDataPoint first = data.getFirst();
        this.from = first.yearMonth();
        JSONDataPoint last = data.getLast();
        this.to = last.yearMonth();

        this.data = data.stream()
                .collect(Collectors.toMap(JSONDataPoint::yearMonth, JSONDataPoint::value));

    }

    @Override
    public BigDecimal getIndex(YearMonth ym) {
        if (ym.compareTo(this.getTo()) > 0) {
            return this.data.get(this.to);
        }
        return this.data.get(ym);

    }

    @Override
    public BigDecimal getIndex(int year, int month) {

        return this.getIndex(YearMonth.of(year, month));
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
