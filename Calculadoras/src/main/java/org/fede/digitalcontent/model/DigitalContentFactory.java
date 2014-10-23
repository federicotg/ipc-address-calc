/*
 * Copyright (C) 2014 fede
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
package org.fede.digitalcontent.model;

import java.util.Collections;
import java.util.Set;

/**
 *
 * @author fede
 */
public class DigitalContentFactory extends BaseFactory<Pair<Set<Performance>, FormatType>, DigitalContent> {

    public DigitalContent createDigitalContent(
            final Performance performance,
            final FormatType format,
            final Quality quality) {
        return this.createDigitalContent(Collections.singleton(performance), format, quality);
    }

    public DigitalContent createDigitalContent(
            final Set<Performance> performances,
            final FormatType format,
            final Quality quality) {
        return this.createInstance(new Pair<>(performances, format), new Creator<DigitalContent>() {

            @Override
            public DigitalContent createInstance() {
                return new DigitalContent(performances, quality, format);
            }
        });
    }

}
