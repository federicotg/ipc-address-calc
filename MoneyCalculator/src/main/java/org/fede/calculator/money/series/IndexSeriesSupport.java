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
import java.util.Date;
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
        public BigDecimal getIndex(int year, int month) {
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

    private int hashValue = 0;

    @Override
    public final BigDecimal getIndex(Date day) {
        final var ym = YearMonth.of(day);
        return this.getIndex(ym.year(), ym.month());
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof IndexSeries other) {

            final int months = this.getFrom().monthsUntil(this.getTo());
            YearMonth ym = this.getFrom();
            int i = 0;

            boolean equal = this.getFrom().equals(other.getFrom()) && this.getTo().equals(other.getTo());
            while (equal && i < months) {
                equal &= this.getIndex(ym.getYear(), ym.getMonth()).compareTo(
                        other.getIndex(ym.getYear(), ym.getMonth())) == 0;
                ym = ym.next();
            }
            return equal;
        }
        return false;

    }

    @Override
    public int hashCode() {
        if (this.hashValue == 0) {

            this.hashValue = 7;
            this.hashValue = 11 * this.hashValue + Objects.hashCode(this.getFrom());
            this.hashValue = 11 * this.hashValue + Objects.hashCode(this.getTo());

            final int months = this.getFrom().monthsUntil(this.getTo());
            YearMonth ym = this.getFrom();
            for (int i = 0; i < months; i++) {
                this.hashValue = 11 * this.hashValue + Objects.hashCode(this.getIndex(ym.getYear(), ym.getMonth()));
                ym = ym.next();
            }
        }
        return this.hashValue;
    }

}
