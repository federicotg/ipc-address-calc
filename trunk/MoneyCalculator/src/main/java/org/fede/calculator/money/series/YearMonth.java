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

/**
 *
 * @author fede
 */
public class YearMonth implements Comparable<YearMonth> {

    private final int year;
    private final int month;

    public YearMonth(int year, int month) {
        this.year = year;
        this.month = month;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + this.year;
        hash = 83 * hash + this.month;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof YearMonth && this.year == ((YearMonth) obj).year && this.month == ((YearMonth) obj).month;
    }

    @Override
    public int compareTo(YearMonth o) {
        if (o.year == this.year) {
            return this.month - o.month;
        }
        return this.year - o.year;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int monthsUntil(YearMonth other) {
        if (this.compareTo(other) < 0) {
            return ((other.getYear() - this.getYear()) * 12) + (other.getMonth() - this.getMonth());
        }
        return 0;
    }

    public YearMonth next() {
        if (this.month == 12) {
            return new YearMonth(this.year + 1, 1);
        }
        return new YearMonth(this.year, this.month + 1);
    }
}