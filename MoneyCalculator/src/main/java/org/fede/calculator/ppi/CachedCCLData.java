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
import java.util.Map;

/**
 *
 * @author fede
 */
public record CachedCCLData(LocalDateTime created, Map<String, BigDecimal> data) {
    
    
     public boolean expired() {
        var now = LocalDateTime.now();
        if (now.getDayOfWeek() != DayOfWeek.SATURDAY && now.getDayOfWeek() != DayOfWeek.SUNDAY) {
            if (this.created.isBefore(now.withHour(18).withMinute(0).withSecond(0))) {
                return Duration.between(this.created, now).toMinutes() > 15;
            } else {
                return Duration.between(this.created, now).toHours() > 12;
            }
        }
        return Duration.between(this.created, now).toDays() > 1;
    }
}
