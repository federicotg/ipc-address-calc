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

public final class Like52Street extends Like44Street{

    public Like52Street(){
        super(52);
    }

    @Override
    protected boolean isValidBoundary(int streetNumber) {
        return betweenAnd(streetNumber, 115, 120) ||
                    betweenAnd(streetNumber, 27, 31) ||
                    betweenAnd(streetNumber, 132, MAX_LIKE_7_STREET);
    }
    
    
}
