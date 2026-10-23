/*
 * Copyright (C) 2026 fede
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

import java.math.RoundingMode;
import java.text.MessageFormat;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.junit.jupiter.api.Test;

/**
 *
 * @author fede
 */
public class RealNetSavingsComparisonTest {

    @Test
    void printQuantityChangeVsCashFlowSideBySide() {
        final var series = new Series();
        final var byQuantity = series.realNetSavingsByQuantityChange();
        final var byCashFlow = series.realNetSavingsByCashFlow();

        final var from = byQuantity.maximumFrom(byCashFlow);
        final var to = byQuantity.minimumTo(byCashFlow);

        System.out.println(MessageFormat.format(
                "{0}  {1}  {2}  {3}",
                pad("ym", 8),
                pad("quantity", 14),
                pad("cashFlow", 14),
                pad("diff", 14)));
        System.out.println("-".repeat(56));

        for (var ym = from; !ym.isAfter(to); ym = ym.plusMonths(1)) {
            final var q = byQuantity.getAmountOrElseZero(ym).amount();
            final var c = byCashFlow.getAmountOrElseZero(ym).amount();
            final var diff = q.subtract(c);
            System.out.println(MessageFormat.format(
                    "{0}  {1}  {2}  {3}",
                    pad(ym.toString(), 8),
                    pad(q.setScale(2, RoundingMode.HALF_UP).toPlainString(), 14),
                    pad(c.setScale(2, RoundingMode.HALF_UP).toPlainString(), 14),
                    pad(diff.setScale(2, RoundingMode.HALF_UP).toPlainString(), 14)));
        }

        System.out.println("-".repeat(56));
        System.out.println(MessageFormat.format(
                "months: {0}  from {1} to {2}",
                from.until(to, ChronoUnit.MONTHS) + 1,
                from,
                to));
        System.out.println("quantity: " + nameAndRange(byQuantity));
        System.out.println("cashFlow: " + nameAndRange(byCashFlow));
    }

    private static String nameAndRange(MoneyAmountSeries s) {
        return MessageFormat.format("{0} [{1} .. {2}]", s.getName(), s.getFrom(), s.getTo());
    }

    private static String pad(String value, int width) {
        if (value.length() >= width) {
            return value;
        }
        return " ".repeat(width - value.length()) + value;
    }
}
