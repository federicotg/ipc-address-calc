/*
 * Copyright (C) 2021 federicogentile
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
package org.fede.calculator.money;

import com.diogonunes.jcolor.Ansi;
import com.diogonunes.jcolor.AnsiFormat;
import com.diogonunes.jcolor.Attribute;
import java.math.BigDecimal;
import static java.text.MessageFormat.format;
import java.text.NumberFormat;

/**
 *
 * @author federicogentile
 */
public class Format {

    private static final AnsiFormat PROFIT_FORMAT = new AnsiFormat(Attribute.GREEN_TEXT());
    private static final AnsiFormat LOSS_FORMAT = new AnsiFormat(Attribute.RED_TEXT());
    private static final NumberFormat PERCENT_FORMAT = NumberFormat.getPercentInstance();

    public Format() {
        PERCENT_FORMAT.setMinimumFractionDigits(2);
    }

    public String text(String value, int width, AnsiFormat fmt) {
        return Ansi.colorize(text(value, width), fmt);
    }

    public String text(String value, int width) {
        return String.format("%-" + width + "s", value);
    }

    public String currency(BigDecimal value) {
        return format("{0,number,currency}", value);
    }

    public String currency(BigDecimal value, int width) {
        return String.format("%" + width + "s", currency(value));
    }

    public String number(BigDecimal value) {
        return format("{0,number,0.00}", value);
    }

    public String number(BigDecimal value, int width) {
        return String.format("%" + width + "s", number(value));
    }

    public String currencyPL(BigDecimal value, int width) {

        return Ansi.colorize(String.format("%" + width + "s", currency(value)), value.signum() >= 0 ? PROFIT_FORMAT : LOSS_FORMAT);
    }

    public String currency(MoneyAmount value, int width) {
        return String.format("%" + width + "s", format("{0} {1}", value.getCurrency(), currency(value.getAmount())));
    }

    public String percent(BigDecimal pct, int width) {

        return String.format("%" + width + "s", percent(pct));
    }

    public String percent(BigDecimal pct) {

        return format("{0}", PERCENT_FORMAT.format(pct));
    }

    public String pctNumber(BigDecimal value) {
        return String.format("%3d", value.intValue()).concat("%");

    }
}
