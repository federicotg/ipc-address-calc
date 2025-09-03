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
package org.fede.calculator.report;

/**
 *
 * @author federicogentile
 */
public record ExpectedReturnGroup(
        ExpectedReturn usLargeCap,
        ExpectedReturn usSmallCap,
        ExpectedReturn em,
        ExpectedReturn eu,
        ExpectedReturn inflation) {

    public double weightedMu() {
        return this.usLargeCap().mu() * 0.65d
                + this.usSmallCap().mu() * 0.1d
                + this.eu().mu() * 0.15d
                + this.em().mu() * 0.1d;
    }

    public double weightedSigma() {
        return this.usLargeCap().sigma() * 0.65d
                + this.usSmallCap().sigma() * 0.1d
                + this.eu().sigma() * 0.15d
                + this.em().sigma() * 0.1d;
    }

}
