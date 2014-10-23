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

import java.util.Date;

/**
 *
 * @author fede
 */
public class PerformanceFactory extends BaseFactory<Terna<Opus, Venue, Date>, Performance> {

    public Performance createPerformance(final Opus opus, final Venue venue, final Date date) {
        return this.createInstance(new Terna<>(opus, venue, date), new Creator<Performance>() {

            @Override
            public Performance createInstance() {
                return new Performance(opus, venue, date);
            }
        });
    }

}
