/*
 * Copyright (C) 2020 fede
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

import java.math.BigDecimal;
import java.util.List;

/**
 *
 * @author fede
 */
public class BBPPYear {

    private int year;
    private List<BBPPTaxBraket> brakets;
    private BigDecimal minimum;
    private BigDecimal usd;
    private List<BBPPItem> items;

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public BigDecimal getUsd() {
        return usd;
    }

    public void setUsd(BigDecimal usd) {
        this.usd = usd;
    }

    
    
    public List<BBPPTaxBraket> getBrakets() {
        return brakets;
    }

    public void setBrakets(List<BBPPTaxBraket> brakets) {
        this.brakets = brakets;
    }

    public BigDecimal getMinimum() {
        return minimum;
    }

    public void setMinimum(BigDecimal minimum) {
        this.minimum = minimum;
    }

    public List<BBPPItem> getItems() {
        return items;
    }

    public void setItems(List<BBPPItem> items) {
        this.items = items;
    }

}
