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
public abstract class SeriesSupport implements Series {

    @Override
    public final YearMonth minimumTo(Series other) {
        YearMonth myTo = new YearMonth(this.getToYear(), this.getToMonth());
        YearMonth otherTo = new YearMonth(other.getToYear(), other.getToMonth());
        if (myTo.compareTo(otherTo) < 0) {
            return myTo;
        }
        return otherTo;
    }

    @Override
    public final YearMonth maximumFrom(Series other) {
        YearMonth myFrom = new YearMonth(this.getFromYear(), this.getFromMonth());
        YearMonth otherFrom = new YearMonth(other.getFromYear(), other.getFromMonth());
        if (myFrom.compareTo(otherFrom) > 0) {
            return myFrom;
        }
        return otherFrom;
    }
}
