/*
 * Copyright (C) 2025 fede
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

import java.text.MessageFormat;
import java.time.YearMonth;

/**
 *
 * @author fede
 */
public class YearMonthUtil {

    public static YearMonth min(YearMonth a, YearMonth b) {
        return a.isBefore(b) ? a : b;
    }

    public static YearMonth max(YearMonth a, YearMonth b) {
        return a.isBefore(b) ? b : a;
    }

    public static String half(YearMonth ym) {
        return MessageFormat.format("{0}-H{1}", String.valueOf(ym.getYear()), ((ym.getMonthValue() - 1) / 6) + 1);
    }

    public static String quarter(YearMonth ym) {
        return MessageFormat.format("{0}-Q{1}", String.valueOf(ym.getYear()), ((ym.getMonthValue() - 1) / 3) + 1);
    }

    public static String monthString(YearMonth ym) {
        return MessageFormat.format("{0}-{1}", String.valueOf(ym.getYear()), (ym.getMonthValue() < 10 ? "0" : "") + String.valueOf(ym.getMonthValue()));
    }
}
