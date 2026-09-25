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
import java.text.MessageFormat;
import java.time.YearMonth;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author fede
 */
public class JSONIndexSeries extends IndexSeriesSupport {

    private final Map<YearMonth, BigDecimal> data;
    private final YearMonth from;
    private final YearMonth to;
    private final BigDecimal lastValue;

    public JSONIndexSeries(Collection<JSONDataPoint> datapoints) {
        if (datapoints.isEmpty()) {
            throw new IllegalArgumentException("Index series must not be empty.");
        }

        this.data = HashMap.newHashMap(datapoints.size());
        YearMonth min = null;
        YearMonth max = null;

        for (var d : datapoints) {
            var ym = d.yearMonth();
            if (this.data.put(ym, d.value()) != null) {
                throw new IllegalArgumentException(
                        MessageFormat.format("Duplicate data point for {0}.", ym));
            }
            if (min == null || ym.isBefore(min)) {
                min = ym;
            }
            if (max == null || ym.isAfter(max)) {
                max = ym;
            }
        }
        this.from = min;
        this.to = max;
        this.lastValue = this.data.get(this.to);
    }

    @Override
    public BigDecimal getIndex(YearMonth ym) {
        return this.data.getOrDefault(ym, this.lastValue);

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
