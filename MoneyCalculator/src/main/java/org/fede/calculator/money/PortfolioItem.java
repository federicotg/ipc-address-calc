/*
 * Copyright (C) 2021 federico
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
import com.diogonunes.jcolor.Attribute;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.fede.calculator.money.series.YearMonth;

/**
 *
 * @author federico
 */
public class PortfolioItem {

    private static final BigDecimal ONE_PERCENT = BigDecimal.ONE.movePointLeft(2);

    private static final NumberFormat PERCENT = NumberFormat.getPercentInstance();

    static {
        PERCENT.setMinimumFractionDigits(2);
    }

    private final MoneyAmount amount;
    private final String type;
    private final MoneyAmount dollarAmount;

    public PortfolioItem(MoneyAmount amount, String type, YearMonth ym) {
        this.amount = amount;
        this.type = type;

        this.dollarAmount = ForeignExchanges.getForeignExchange(this.amount.getCurrency(), "USD")
                .exchange(this.amount, "USD", ym);

    }

    public MoneyAmount getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }

    public MoneyAmount getDollarAmount() {
        return dollarAmount;
    }

    public String asPercentReport(MoneyAmount total) {

        final var pct = this.getDollarAmount().getAmount().divide(total.getAmount(), MathConstants.CONTEXT);

        return MessageFormat.format("{0} {1} {2} {3}",
                String.format("%-9s", this.type),
                String.format("%-7s", this.amount.getCurrency()),
                String.format("%7s", this.pct(pct)),
                pctBar(pct)
        );

    }

    public String asReport(MoneyAmount total) {

        final var pct = this.getDollarAmount().getAmount().divide(total.getAmount(), MathConstants.CONTEXT);
        return MessageFormat.format("{0} {1} {2} {3} {4}",
                String.format("%-9s", this.type),
                String.format("%-7s", this.amount.getCurrency()),
                String.format("%11s", MessageFormat.format("{0,number,currency}", this.getDollarAmount().getAmount())),
                String.format("%7s", this.pct(pct)),
                Ansi.colorize(pctBar(pct), Attribute.BRIGHT_WHITE_BACK())
        );
    }

    private static String pctBar(BigDecimal value) {

        return IntStream.range(0, value.movePointRight(2).intValue())
                .mapToObj(i -> " ")
                .collect(Collectors.joining());

    }

    private String pct(BigDecimal value) {
        return value.compareTo(ONE_PERCENT) < 0
                ? "<1 %"
                : PERCENT.format(value);
    }

}
