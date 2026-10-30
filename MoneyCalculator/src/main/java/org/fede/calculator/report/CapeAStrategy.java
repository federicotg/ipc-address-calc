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

import java.math.BigDecimal;
import org.fede.calculator.money.series.SeriesReader;

/**
 * Strategy for the base CAPE intercept {@code a} in {@code a + b / CAPE}.
 * Does not include pension, future-income or long-term-care deltas.
 *
 * @author fede
 */
@FunctionalInterface
public interface CapeAStrategy {

    static CapeAStrategy fromConfig() {
        final String name = SeriesReader.readEnvironment().getProperty("cape.a.strategy");
        if (name == null || name.isBlank()) {
            return new CurrentCapeAStrategy();
        }
        return switch (name.trim().toLowerCase()) {
            case "current" ->
                new CurrentCapeAStrategy();
            case "ern" ->
                new ERNCapeAStrategy();
            default ->
                throw new IllegalArgumentException(
                        "Unknown cape.a.strategy '" + name + "'. Use current or ern.");
        };
    }

    BigDecimal baseCapeA(BigDecimal yearsLeft);
}
