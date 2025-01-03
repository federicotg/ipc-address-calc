/*
 * Copyright (C) 2024 fede
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
package org.fede.calculator.fmp;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

/**
 *
 * @author fede
 */
public record CachedETFData(Exchange exchange, LocalDateTime created, Map<String, FMPPriceData> data) {

    public boolean expired() {

        if (this.exchange.isTrading(ZonedDateTime.now())) {
            // trading: 15 minutes
            return Duration.between(this.created, LocalDateTime.now()).toMinutes() > 15;
        }

        // not trading right now
        if (this.exchange.isTrading(this.created.atZone(ZoneId.systemDefault()))) {
            return true;
        } else {
            return Duration.between(this.created, LocalDateTime.now()).toHours() > 12;
        }
    }

}
