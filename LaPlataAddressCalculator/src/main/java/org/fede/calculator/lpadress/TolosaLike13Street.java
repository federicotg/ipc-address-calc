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

public final class TolosaLike13Street extends LaPlataStreet {

    public TolosaLike13Street(int streetNumber){
        super(streetNumber);
    }
    
    @Override
    protected int getLowerBoundary(int number) {
        final int base = (number / 100);
        if(base == 0){
            return 32;
        }
        return 531 - base;
    }


    @Override
    protected boolean isValidBoundary(int streetNumber) {
        return betweenAnd(streetNumber, MAX_LIKE_51_STREET, 531) || streetNumber == 32;
    }

    @Override
    protected int getNextStreet(int streetNumber) {
        if(streetNumber == 32){
            return 531;
        }
        return streetNumber + 1;
    }

}
