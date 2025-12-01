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
import static java.math.BigDecimal.ZERO;
import java.math.RoundingMode;
import static java.text.MessageFormat.format;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.joining;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.fede.calculator.money.MoneyAmount;
import static org.fede.calculator.money.Inflation.USD_INFLATION;
import org.fede.calculator.money.series.MoneyAmountSeries;
import java.time.YearMonth;
import static org.fede.calculator.money.MathConstants.C;

/**
 *
 * @author fede
 */
public class Bar {

    private final BigDecimal ONE_PERCENT = BigDecimal.ONE.movePointLeft(2);
    private final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private final AnsiFormat RED = new AnsiFormat(Attribute.RED_BACK(), Attribute.WHITE_TEXT());
    private final AnsiFormat GREEN = new AnsiFormat(Attribute.GREEN_BACK(), Attribute.WHITE_TEXT());

    private final Set<String> DARK_COLORS = Set.of(
            Attribute.BLACK_BACK().toString(),
            Attribute.BRIGHT_BLACK_BACK().toString(),
            Attribute.RED_BACK().toString(),
            Attribute.BLUE_BACK().toString());

    private final Console console;
    private final Format format;

    public Bar(Console console, Format format) {
        this.console = console;
        this.format = format;
    }

    public String percentBar(YearMonth ym, List<AmountAndColor> amounts) {

        final var total = amounts
                .stream()
                .map(AmountAndColor::amount)
                .map(MoneyAmount::amount)
                .reduce(ZERO, BigDecimal::add);

        if (total.signum() == 0) {
            return "";
        }
        final var relativeAmounts = amounts
                .stream()
                .filter(p -> !p.amount().isZero())
                .map(p -> new AmountAndColor(new MoneyAmount(p.amount().amount().divide(total, C).movePointRight(2).setScale(0, RoundingMode.HALF_EVEN), p.amount().currency()), p.color()))
                .collect(Collectors.toList());

        final var relativeTotal = relativeAmounts
                .stream()
                .map(AmountAndColor::amount)
                .map(MoneyAmount::amount)
                .reduce(ZERO, BigDecimal::add);

        if (relativeTotal.compareTo(HUNDRED) != 0) {
            final var first = relativeAmounts.get(0);

            final var firstAmount = first.amount().amount();

            var difference = relativeTotal.subtract(HUNDRED, C).negate(C);

            relativeAmounts.set(0,
                    new AmountAndColor(new MoneyAmount(firstAmount.add(difference, C), first.amount().currency()), first.color()));

        }

        return this.genericBar(ym, relativeAmounts, 1);
    }

    public String genericBar(YearMonth ym, List<AmountAndColor> amounts, int width) {

        final var bars = IntStream.range(0, amounts.size())
                .map(i -> i + 2)
                .mapToObj(i -> format("'{'{0}'}'", i))
                .collect(joining(""));
        final Stream<String> barsStream = amounts.stream().map(p -> this.bar(p.amount().amount(), width, p.amount().amount().signum() < 0 ? Attribute.RED_BACK() : p.color()));
        final Stream<String> ymStream = Stream.of(String.valueOf(ym.getYear()), String.format("%02d", ym.getMonthValue()));

        return format("{0}/{1} ".concat(bars),
                (Object[]) Stream.of(ymStream, barsStream).flatMap(Function.identity()).toArray(String[]::new));
    }

    public String pctBar(BigDecimal value, BigDecimal total) {
        return Optional.of(total)
                .filter(t -> t.signum() != 0)
                .map(t -> pctBar(value.divide(t, C)))
                .orElse("");
    }

    public String pctBar(BigDecimal value) {

        if (value.abs().compareTo(ONE_PERCENT) < 0) {
            return String.format("%10s", "<1 %");
        }

        final var end = value.abs().movePointRight(2).intValue();

        final Attribute attr = value.signum() < 0 ? Attribute.RED_BACK() : Attribute.GREEN_BACK();

        if (end > 100) {

            final var part = " ".repeat(48);

            return format("{0} {1}",
                    this.format.percent(value, 10),
                    Ansi.colorize(part.concat("/-/").concat(part), attr));
        }

        return format("{0} {1}",
                this.format.percent(value, 10),
                Ansi.colorize(" ".repeat(end),
                        attr));
    }

    public String smallPctBar(BigDecimal value) {

        final var maxLengh = 21;
        final var partLength = (maxLengh - 3) / 2;

        final var steps = value.movePointRight(2)
                .abs()
                .divide(BigDecimal.valueOf(5l), C)
                .intValue();

        final var bar = steps < maxLengh
                ? " ".repeat(steps)
                : " ".repeat(partLength)
                + "/-/"
                + " ".repeat(partLength);

        Attribute attr = value.signum() < 0 ? Attribute.RED_BACK() : Attribute.GREEN_BACK();

        return String.format("%-" + maxLengh + "s", Ansi.colorize(bar, attr));
    }

    public String bar(BigDecimal value, int scale) {
        return this.bar(value, scale, " ");
    }

    private String bar(BigDecimal value, int scale, Attribute color) {
        return this.bar(value, scale, color, this.format::currencyShort);
    }

    private String bar(BigDecimal value, int scale, Attribute color, Function<BigDecimal, String> valueFormat) {

        final var valueStr = valueFormat.apply(value).trim();

        final var end = value.abs()
                .divide(BigDecimal.valueOf(scale), C)
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();

        if (end > valueStr.length()) {

            return Ansi.colorize(
                    valueStr + " ".repeat(end - valueStr.length()),
                    color,
                    this.isDarkColor(color)
                    ? Attribute.WHITE_TEXT()
                    : Attribute.BLACK_TEXT());
        }

        return Ansi.colorize(" ".repeat(end), new AnsiFormat(color));
    }

    private boolean isDarkColor(Attribute color) {
        var parts = color.toString().split(";");
        if (parts.length < 3) {
            return DARK_COLORS.contains(color.toString());
        }
        return Integer.parseInt(parts[2]) < 128;
    }

    private String bar(BigDecimal value, int scale, String symbol) {

        final AnsiFormat fmt = value.signum() < 0
                ? RED
                : GREEN;

        return Ansi.colorize(
                symbol.repeat(value.abs().divide(BigDecimal.valueOf(scale), C).setScale(0, RoundingMode.HALF_UP).intValue()),
                fmt);
    }

    public String percentBar(YearMonth ym, MoneyAmount one, MoneyAmount two, MoneyAmount three) {

        final var total = one.add(two).add(three);

        if (total.isZero()) {
            return "";
        }

        var bar1 = this.asPct(one, total);
        var bar2 = this.asPct(two, total);
        var bar3 = this.asPct(three, total);

        if (bar1.add(bar2, C).add(bar3, C).compareTo(HUNDRED) != 0) {
            bar1 = HUNDRED.subtract(bar2.add(bar3, C), C);
        }
        return this.bar(ym, bar1, bar2, bar3, 1);
    }

    public String bar(YearMonth ym, BigDecimal one, BigDecimal two, int scale) {
        return format("{0}/{1} {2}{3}",
                String.valueOf(ym.getYear()),
                String.format("%02d", ym.getMonthValue()),
                this.bar(one, scale, Attribute.BLUE_BACK()),
                this.bar(two, scale, Attribute.RED_BACK()));
    }

    public String bar(YearMonth ym, BigDecimal one, BigDecimal two, BigDecimal three, int scale) {
        return format("{0}/{1} {2}{3}{4}",
                String.valueOf(ym.getYear()),
                String.format("%02d", ym.getMonthValue()),
                this.bar(one, scale, Attribute.BLUE_BACK()),
                this.bar(two, scale, Attribute.RED_BACK()),
                this.bar(three, scale, Attribute.YELLOW_BACK()));
    }

    private BigDecimal asPct(MoneyAmount ma, MoneyAmount total) {
        return ma.amount()
                .divide(total.amount(), C)
                .movePointRight(2)
                .setScale(0, RoundingMode.HALF_UP);
    }

    public void evolution(String name, MoneyAmountSeries s, int scale) {
        final var limit = USD_INFLATION.getTo();

        s.forEach((ym, ma) -> {
            if (ym.compareTo(limit) <= 0) {
                this.console.appendLine(this.genericBar(ym, List.of(new AmountAndColor(ma, Attribute.WHITE_BACK())), scale));
            }
        });

        this.console.appendLine("\n", name, " real USD ", format("{0}/{1}", String.valueOf(limit.getYear()), limit.getMonth()));

    }
}
