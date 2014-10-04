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
package org.fede.calculator.lpadress;

public class Like44Street extends LaPlataStreet {

    public Like44Street(int streetNumber) {
        super(streetNumber);
    }

    @Override
    protected int getLowerBoundary(int number) {
        final int base = (number / 50) - 5;
        int rawStreet = base > 50 ? base + 1 : base;
        if (rawStreet < 1) {
            return Math.abs(rawStreet) + 115;
        }
        if (rawStreet > 31) {
            return rawStreet + 100;
        }
        return rawStreet;
    }

    @Override
    protected boolean isValidBoundary(int streetNumber) {

        return betweenAnd(streetNumber, 1, 31) ||
                betweenAnd(streetNumber, 115, 120) ||
                betweenAnd(streetNumber, 132, MAX_LIKE_7_STREET);
    }

    @Override
    protected final int getNextStreet(int streetNumber) {
        if (streetNumber == 115) {
            return 1;
        }
        if (betweenAnd(streetNumber, 116, 120)) {
            return streetNumber - 1;
        }
        if (streetNumber == 31) {
            return 132;
        }
        return streetNumber + 1;
    }
}
