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

import org.fede.util.Pair;

/**
 *
 * @author fede
 */
public class LatLon extends Pair<Double, Double> {

    public LatLon(Double lat, Double lon) {
        super(lat, lon);
    }

    public Double getLat() {
        return getFirst();
    }

    public void setLat(Double lat) {
        this.setFirst(lat);
    }

    public Double getLon() {
        return getSecond();
    }

    public void setLon(Double lon) {
        this.setSecond(lon);
    }

}
