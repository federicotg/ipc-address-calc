/*
 * Copyright (C) 2020 fede
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

import java.math.BigDecimal;
import org.fede.calculator.money.Currency;

/**
 * {
 * "name": "43", "value": "167842.80", "holding": 0.5, "domestic": true,
 * "exempt": true, "currency":"ARS" }
 *
 * @author fede
 */
public record BBPPItem(
        String name,
        BigDecimal value,
        BigDecimal holding,
        boolean domestic,
        boolean exempt,
        Currency currency) {

}
