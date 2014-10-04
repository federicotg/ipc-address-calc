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
import java.util.HashMap;
import java.util.Map;
import org.fede.calculator.money.NoIndexDataFoundException;

/**
 *
 * @author fede
 */
public final class CachedSeries extends IndexSeriesSupport {


    private class IndexKey {

        private final int year;
        private final int month;

        private IndexKey(int year, int month) {
            this.year = year;
            this.month = month;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 53 * hash + this.year;
            hash = 53 * hash + this.month;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof IndexKey
                    && ((IndexKey) obj).year == this.year
                    && ((IndexKey) obj).month == this.month;
        }

    }

    private final IndexSeries source;
    private final Map<IndexKey, BigDecimal> cache;

    public CachedSeries(IndexSeries source) {
        this.source = source;
        this.cache = new HashMap<>();
    }

    @Override
    public final BigDecimal getIndex(int year, int month) throws NoIndexDataFoundException {
        IndexKey key = new IndexKey(year, month);
        BigDecimal answer = this.cache.get(key);
        if (answer == null) {
            answer = this.source.getIndex(year, month);
            this.cache.put(key, answer);
        }
        return answer;
    }

    @Override
    public BigDecimal getIndex(int year) throws NoIndexDataFoundException {
        IndexKey key = new IndexKey(year, 13);
        BigDecimal answer = this.cache.get(key);
        if (answer == null) {
            answer = this.source.getIndex(year);
            this.cache.put(key, answer);
        }
        return answer;
    }
    
        @Override
    public int getFromYear() {
        return this.source.getFromYear();
    }

    @Override
    public int getToYear() {
        return this.source.getToYear();
    }

    
}
