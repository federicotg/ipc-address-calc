/*
 * Copyright (C) 2022 federicogentile
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
package org.fede.calculator.money;

/**
 *
 * @author federicogentile
 */
public class ExpectedReturnGroup {

    private ExpectedReturn usLargeCap;
    private ExpectedReturn usSmallCap;
    private ExpectedReturn em;
    private ExpectedReturn eu;
    private ExpectedReturn inflation;

    public ExpectedReturn getUsLargeCap() {
        return usLargeCap;
    }

    public void setUsLargeCap(ExpectedReturn usLargeCap) {
        this.usLargeCap = usLargeCap;
    }

    public ExpectedReturn getUsSmallCap() {
        return usSmallCap;
    }

    public void setUsSmallCap(ExpectedReturn usSmallCap) {
        this.usSmallCap = usSmallCap;
    }

    public ExpectedReturn getEm() {
        return em;
    }

    public void setEm(ExpectedReturn em) {
        this.em = em;
    }

    public ExpectedReturn getEu() {
        return eu;
    }

    public void setEu(ExpectedReturn eu) {
        this.eu = eu;
    }

    public double mu() {
        return this.getUsLargeCap().getMu() * 0.7d
                + this.getUsSmallCap().getMu() * 0.1d
                + this.getEu().getMu() * 0.1d
                + this.getEm().getMu() * 0.1d;
    }

    public double sigma() {
        return this.getUsLargeCap().getSigma() * 0.7d
                + this.getUsSmallCap().getSigma() * 0.1d
                + this.getEu().getSigma() * 0.1d
                + this.getEm().getSigma() * 0.1d;
    }

    public ExpectedReturn getInflation() {
        return inflation;
    }

    public void setInflation(ExpectedReturn inflation) {
        this.inflation = inflation;
    }

}
