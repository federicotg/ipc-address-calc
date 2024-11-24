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

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 *
 * @author fede
 */
public enum Exchange {

    LSE {
        @Override
        public boolean isTrading(ZonedDateTime zdt) {
            return this.isTrading(zdt, "Europe/London", 8, 16);
        }

    }, NYSE {
        @Override
        public boolean isTrading(ZonedDateTime zdt) {
            return this.isTrading(zdt, "America/New_York", 9, 16);
        }

    };

    public abstract boolean isTrading(ZonedDateTime zdt);

    protected final boolean isTrading(ZonedDateTime zdt, String zoneId, int initialHour, int finalHour) {
        final var localTime = zdt.withZoneSameInstant(ZoneId.of(zoneId));

        return localTime.getDayOfWeek() != DayOfWeek.SATURDAY
                && localTime.getDayOfWeek() != DayOfWeek.SUNDAY
                && localTime.getHour() >= initialHour
                && localTime.getHour() <= finalHour;

    }

}
