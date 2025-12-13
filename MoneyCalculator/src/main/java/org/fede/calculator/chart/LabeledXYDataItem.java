/*
 * Copyright (C) 2025 fede
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
package org.fede.calculator.chart;

import java.time.LocalDate;
import java.time.ZoneId;
import org.jfree.data.xy.XYDataItem;

/**
 *
 * @author fede
 */
public class LabeledXYDataItem extends XYDataItem {

    private static final long serialVersionUID = 1L;

    private final String label;

    public LabeledXYDataItem(LocalDate x, LocalDate y, String label) {
        this(
                x.atStartOfDay(ZoneId.systemDefault()) // convert to ZonedDateTime
                        .toInstant() // convert to Instant (UTC)
                        .toEpochMilli(),
                y.atStartOfDay(ZoneId.systemDefault()) // convert to ZonedDateTime
                        .toInstant() // convert to Instant (UTC)
                        .toEpochMilli(),
                label
        );
    }

    public LabeledXYDataItem(LocalDate x, Number y, String label) {
        this(
                x.atStartOfDay(ZoneId.systemDefault()) // convert to ZonedDateTime
                        .toInstant() // convert to Instant (UTC)
                        .toEpochMilli(),
                y,
                label
        );
    }

    public LabeledXYDataItem(Number x, Number y, String label) {
        super(x, y);
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
