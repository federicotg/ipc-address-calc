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
package org.fede.calculator.ppi;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

/**
 *
 * @author fede
 */
public record CachedCCLData(LocalDateTime created, Map<String, BigDecimal> data) {

    public boolean expired() {

        final var nycTime = ZonedDateTime.now().withZoneSameInstant(ZoneId.of("America/New_York"));
        if (this.isTrading(nycTime)) {
            // trading: 15 minutes
            return Duration.between(this.created, LocalDateTime.now()).toMinutes() > 15;
        }
        // not trading right now
        if (this.isTrading(this.created.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("Europe/London")))) {
            return true;
        } else {
            return Duration.between(this.created, LocalDateTime.now()).toHours() > 12;
        }
    }

    private boolean isTrading(ZonedDateTime nycTime) {
        return nycTime.getDayOfWeek() != DayOfWeek.SATURDAY
                && nycTime.getDayOfWeek() != DayOfWeek.SUNDAY
                && nycTime.getHour() >= 9
                && nycTime.getHour() <= 16;
    }
}
