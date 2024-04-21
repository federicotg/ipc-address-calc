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
package org.fede.calculator.criptoya;

import java.math.BigDecimal;

/**
 *
 * {
 * "ask": 485.7, "totalAsk": 485.7, "bid": 469.25, "totalBid": 469.25, "time":
 * 1685300338 }
 *
 * @author federicogentile
 */
public record CriptoYaFx(BigDecimal totalAsk, BigDecimal totalBid) {

}
