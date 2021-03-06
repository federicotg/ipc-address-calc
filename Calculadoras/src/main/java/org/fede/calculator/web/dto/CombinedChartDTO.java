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
package org.fede.calculator.web.dto;

import java.util.List;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
public class CombinedChartDTO extends MonthGroupingChartDTO {

    private boolean pn = false;
    private boolean dn = true;
    private boolean dr = true;
    private boolean en = true;
    private boolean er = true;
    private List<String> series;

    public CombinedChartDTO() {
    }

    public CombinedChartDTO(CurrencyLimitsDTO arsLimits) {
        super(arsLimits);
    }

    public boolean isPn() {
        return pn;
    }

    public void setPn(boolean pn) {
        this.pn = pn;
    }

    public boolean isDn() {
        return dn;
    }

    public void setDn(boolean dn) {
        this.dn = dn;
    }

    public boolean isDr() {
        return dr;
    }

    public void setDr(boolean dr) {
        this.dr = dr;
    }

    public boolean isEn() {
        return en;
    }

    public void setEn(boolean en) {
        this.en = en;
    }

    public boolean isEr() {
        return er;
    }

    public void setEr(boolean er) {
        this.er = er;
    }

    public List<String> getSeries() {
        return series;
    }

    public void setSeries(List<String> series) {
        this.series = series;
    }

}
