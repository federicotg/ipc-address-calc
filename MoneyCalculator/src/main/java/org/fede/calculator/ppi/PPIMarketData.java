/*
 * Copyright (C) 2023 federicogentile
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
package org.fede.calculator.ppi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.Instant;

/**
 *
 * {
 * "date": "2022-02-24T12:31:04.581Z", "price": 0, "volume": 0, "openingPrice":
 * 0, "max": 0, "min": 0 }
 *
 * @author federicogentile
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PPIMarketData(
        Instant date,
        BigDecimal price,
        long volume,
        BigDecimal openingPrice,
        BigDecimal max,
        BigDecimal min) {

}
