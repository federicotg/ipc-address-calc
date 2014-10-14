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
package org.fede.calculator.money.series;

import java.util.List;

/**
 *
 * Gold: http://www.bundesbank.de/cae/servlet/StatisticDownload?tsId=BBEX3.M.XAU.USD.EA.AC.C06&its_csvFormat=en&its_fileFormat=csv&mode=its
 * 
 * 
 * @author fede
 */
class JSONSeries {

    private String currency;
    private List<JSONDataPoint> data;
    private String interpolation = "NO_INTERPOLATION";

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currnecy) {
        this.currency = currnecy;
    }

    public List<JSONDataPoint> getData() {
        return data;
    }

    public void setData(List<JSONDataPoint> data) {
        this.data = data;
    }

    public String getInterpolation() {
        return interpolation;
    }

    public void setInterpolation(String interpolation) {
        this.interpolation = interpolation;
    }
}
