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
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author fede
 */
public class PerformanceRepository extends BaseRepository<Terna<Opus, Venue, Date>, Performance> {

    @Override
    public void add(Performance entity) {
        this.add(new Terna<>(entity.getOpus(), entity.getVenue(), entity.getDate()), entity);
    }

    public Set<Performance> findByVenue(Venue venue) {

        return this.stream().filter(p -> p.getVenue().equals(venue)).collect(Collectors.toSet());

    }
}
