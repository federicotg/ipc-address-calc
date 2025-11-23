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
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;

/**
 *
 * @author fede
 */
public abstract class IndexSeriesSupport extends SeriesSupport implements IndexSeries {

    public static final IndexSeries CONSTANT_SERIES = new IndexSeriesSupport() {
        @Override
        public YearMonth getFrom() {
            return YearMonth.of(1, 1);
        }

        @Override
        public YearMonth getTo() {
            return YearMonth.of(5000, 12);
        }

        @Override
        public BigDecimal getIndex(YearMonth ym) {
            return BigDecimal.ONE;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(this);
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj;
        }

    };

    @Override
    public final BigDecimal getIndex(LocalDate day) {
        return this.getIndex(YearMonth.from(day));
    }

}
