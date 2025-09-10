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

import static java.text.MessageFormat.format;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.Date;

/**
 *
 * @author fede
 */
public record YearMonth(int year, int month) implements Comparable<YearMonth> {

    private static final Comparator<YearMonth> CMP = Comparator.comparing(YearMonth::year).thenComparing(YearMonth::month);

    public static YearMonth of(LocalDate day) {
        return of(day.getYear(), day.getMonthValue());
    }

    public static YearMonth of(Date day) {
        return of(day.toInstant().atZone(ZoneOffset.systemDefault()).toLocalDate());
    }

    public static YearMonth of(int year, int month) {
        return new YearMonth(year, month);
    }

    @Override
    public int compareTo(YearMonth o) {
        return CMP.compare(this, o);
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int monthsUntil(YearMonth other) {

        return ((other.year - this.year) * 12) + (other.month - this.month);
    }

    public YearMonth next() {
        if (this.month == 12) {
            return YearMonth.of(this.year + 1, 1);
        }
        return YearMonth.of(this.year, this.month + 1);
    }

    public YearMonth prev() {
        if (this.month == 1) {
            return YearMonth.of(this.year - 1, 12);
        }
        return YearMonth.of(this.year, this.month - 1);
    }

    public Date asToDate() {

        return Date.from(
                LocalDate.of(this.getYear(), this.getMonth(), 1)
                        .with(TemporalAdjusters.lastDayOfMonth())
                        .atTime(LocalTime.MAX)
                        .atZone(ZoneId.systemDefault())
                        .toInstant());
    }

    public Date asDate() {
        return Date.from(
                LocalDate.of(this.getYear(), this.getMonth(), 1)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant());
    }

    public YearMonth min(YearMonth other) {
        return this.compareTo(other) < 0 ? this : other;
    }

    public YearMonth max(YearMonth other) {
        return this.compareTo(other) < 0 ? other : this;
    }

    public String half() {
        return format("{0}-H{1}", String.valueOf(this.getYear()), ((this.getMonth() - 1) / 6) + 1);
    }

    public String quarter() {
        return format("{0}-Q{1}", String.valueOf(this.getYear()), ((this.getMonth() - 1) / 3) + 1);
    }

    public String monthString() {
        return format("{0}-{1}", String.valueOf(this.getYear()), (this.getMonth() < 10 ? "0" : "") + String.valueOf(this.getMonth()));
    }

}
