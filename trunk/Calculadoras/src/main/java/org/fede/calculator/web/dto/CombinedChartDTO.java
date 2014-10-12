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

/**
 *
 * @author fede
 */
public class CombinedChartDTO extends MonthGroupingChartDTO {

    private boolean pn = false;
    private boolean pr = true;
    private boolean dn = true;
    private boolean dr = true;

    public boolean isPn() {
        return pn;
    }

    public void setPn(boolean pn) {
        this.pn = pn;
    }

    public boolean isPr() {
        return pr;
    }

    public void setPr(boolean pr) {
        this.pr = pr;
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

}
