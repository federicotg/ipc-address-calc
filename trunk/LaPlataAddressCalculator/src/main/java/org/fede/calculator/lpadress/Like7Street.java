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

public final class Like7Street extends LaPlataStreet {

    public Like7Street(int streetNumber) {
        super(streetNumber);
    }

    @Override
    public int getLowerBoundary(int number) {
        final int base = (number / 50) + 32;
        if (this.getStreetNumber() >= 132) {
            if (base == 51 || base == 53) {
                // 51 y 53 no existen pasando 31
                return base - 1;
            }
            return base;
        }

        if (betweenAnd(this.getStreetNumber(), 27, 31)) {
            if (base == 51 && number > 975) {
                return base + 1;
            } else {
                return base;
            }
        }

        int rawStreet = base < 52 ? base : base + 1;
        if (rawStreet > 99) {
            return rawStreet + 500;
        }

        return rawStreet;
    }

    @Override
    protected boolean isValidBoundary(int streetNumber) {
        return betweenAnd(streetNumber, 500, 531) ||
                betweenAnd(streetNumber, 32, 99) ||
                betweenAnd(streetNumber, 600, 708);
    }

    @Override
    protected int getNextStreet(int streetNumber) {

        if (this.getStreetNumber() >= 132 && (streetNumber == 50 || streetNumber == 52)) {
            return streetNumber + 2;
        }

        if (streetNumber == 51) {
            // entre 27 y 31 existe la avenida 52
            return betweenAnd(this.getStreetNumber(), 27, 31) ? 52 : 53;

        }
        if (streetNumber == 531) {
            return 32;
        }
        if (streetNumber == 99) {
            return 600;
        }
        return streetNumber + 1;
    }
}
