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

public final class TolosaLike520Street extends Like44Street {

    public TolosaLike520Street(int streetNumber){
        super(streetNumber);
    }
    
    @Override
    protected int getLowerBoundary(int number) {
    final int base = (number / 100) - 5;
        if (base < 1) {
            return Math.abs(base) + 115;
        }
        if (base > 31) {
            return base + 100;
        }
        return base;
    }

}
