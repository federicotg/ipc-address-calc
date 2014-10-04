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

public final class LikeDiagonal80Street extends LaPlataDiagonalStreet {
   
    public LikeDiagonal80Street(int diagonalNumber){
        super(diagonalNumber);
    }
    
    @Override
    public int getLowerBoundary(int number) {
        final int base = (number / 100) - 5;

        if (base <= 0) {
            return Math.abs(base) + 115;
        }

        return base;
    }

    @Override
    protected int getNextStreet(int streetNumber) {
        if (streetNumber == 115) {
            return 1;
        }
        return streetNumber + 1;
    }
}
