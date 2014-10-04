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
package org.fede.calculator.money.bls;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author fede
 */
public class BlsRequest {

    private List<String> seriesid;
    private int startyear;
    private int endyear;

    public BlsRequest(String seriesId, int year) {
        this.seriesid = new ArrayList<>(1);
        this.seriesid.add(seriesId);
        this.startyear = year;
        this.endyear = this.startyear;
    }

    public List<String> getSeriesid() {
        return seriesid;
    }

    public void setSeriesid(List<String> seriesid) {
        this.seriesid = seriesid;
    }

    public int getStartyear() {
        return startyear;
    }

    public void setStartyear(int startyear) {
        this.startyear = startyear;
    }

    public int getEndyear() {
        return endyear;
    }

    public void setEndyear(int endyear) {
        this.endyear = endyear;
    }
    
    
}
