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
package org.fede.calculator.report;

import com.diogonunes.jcolor.Ansi;
import com.diogonunes.jcolor.AnsiFormat;
import com.diogonunes.jcolor.Attribute;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.fede.calculator.money.MoneyAmount;

/**
 *
 * @author fede
 */
public class Format {

    private static final Map<String, MessageFormat> FORMATS = new ConcurrentHashMap<>();

    private final BigDecimal ONE_THOUSAND = BigDecimal.ONE.movePointRight(3);
    private final BigDecimal ONE_MILLION = BigDecimal.ONE.movePointRight(6);

    private final AnsiFormat PROFIT_FORMAT = new AnsiFormat(Attribute.GREEN_TEXT());
    private final AnsiFormat LOSS_FORMAT = new AnsiFormat(Attribute.RED_TEXT());
    private final NumberFormat PERCENT_FORMAT = NumberFormat.getPercentInstance();
    private final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance();
    private final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance();
    private final NumberFormat NUMBER_FORMAT2 = NumberFormat.getNumberInstance();
    private final NumberFormat NUMBER_FORMAT4 = NumberFormat.getNumberInstance();

    private final Map<Integer, String> rightAlignedFormat = new ConcurrentHashMap<>();
    private final Map<Integer, String> leftAlignedFormat = new ConcurrentHashMap<>();
    
    public Format() {
        PERCENT_FORMAT.setMinimumFractionDigits(2);
        PERCENT_FORMAT.setMaximumFractionDigits(2);
        CURRENCY_FORMAT.setMinimumFractionDigits(2);
        NUMBER_FORMAT2.setMinimumFractionDigits(2);
        NUMBER_FORMAT4.setMinimumFractionDigits(4);

    }

    private String getRightAlignedFormat(int width) {
        return this.rightAlignedFormat.computeIfAbsent(
                width,
                w -> "%" + String.valueOf(w) + "s");
    }

    private String getLeftAlignedFormat(int width) {
        return this.leftAlignedFormat.computeIfAbsent(
                width,
                w -> "%-" + String.valueOf(w) + "s");
    }

    public String text(String value, int width, AnsiFormat fmt) {
        return Ansi.colorize(this.text(value, width), fmt);
    }

    public String text(String value, int width) {
        return String.format(this.getLeftAlignedFormat(width), value);
    }

    public String center(String text, int width, AnsiFormat fmt) {
        return Ansi.colorize(this.center(text, width), fmt);
    }

    public String center(String text, int width) {
        if (text == null || width <= text.length()) {
            return text;
        }
        int padding = (width - text.length()) / 2;
        int remainder = (width - text.length()) % 2; // handle odd widths
        return " ".repeat(padding + remainder) + text + " ".repeat(padding);
    }

    public static String format(String pattern, Object... o) {
        return FORMATS.computeIfAbsent(pattern, MessageFormat::new)
                .format(o);
    }

    public String currency(BigDecimal value) {
        //return format("{0,number,currency}", value);
        return CURRENCY_FORMAT.format(value);

    }

    public String currency(BigDecimal value, int width) {
        return String.format(this.getRightAlignedFormat(width), currency(value));
    }

    public String number(BigDecimal value) {
        //return format("{0,number,0.00}", value);
        return NUMBER_FORMAT2.format(value);
    }

    public String number2(BigDecimal value) {
        //return format("{0,number,0.##}", value);
        return NUMBER_FORMAT.format(value);
    }

    public String numberLong(BigDecimal value) {
        //return format("{0,number,0.0000}", value);
        return NUMBER_FORMAT4.format(value);
    }

    public String currencyShort(BigDecimal value) {
        if (value.abs().compareTo(ONE_THOUSAND) < 0) {
            return NUMBER_FORMAT.format(value.setScale(0, RoundingMode.HALF_UP));
        }
        if (value.abs().compareTo(ONE_MILLION) < 0) {
            return NUMBER_FORMAT.format(value.movePointLeft(3).setScale(1, RoundingMode.HALF_UP)) + "k";
        }
        return NUMBER_FORMAT.format(value.movePointLeft(6).setScale(1, RoundingMode.HALF_UP)) + "m";
    }

    public String number(BigDecimal value, int width) {
        return String.format(this.getRightAlignedFormat(width), number(value));
    }

    public String currencyPL(BigDecimal value, int width) {

        return Ansi.colorize(String.format(this.getRightAlignedFormat(width), currency(value)), value.signum() >= 0 ? PROFIT_FORMAT : LOSS_FORMAT);
    }

    public String currency(MoneyAmount value, int width) {
        return String.format(this.getRightAlignedFormat(width), format("{0} {1}", value.currency(), currency(value.amount())));
    }

    public String percent(BigDecimal pct, int width) {

        return String.format(this.getRightAlignedFormat(width), percent(pct));
    }

    public String percent(BigDecimal pct) {

        return PERCENT_FORMAT.format(pct);
    }

    public String title(String text) {

        return "\n"
                + this.center(Ansi.colorize(text, Attribute.BRIGHT_WHITE_TEXT(), Attribute.BOLD()), 80)
                + "\n";

    }

    public String subtitle(String title) {

        return "\n\t"
                + Ansi.colorize(format(" {0} ", title), Attribute.BOLD())
                + "\n";

    }
}
