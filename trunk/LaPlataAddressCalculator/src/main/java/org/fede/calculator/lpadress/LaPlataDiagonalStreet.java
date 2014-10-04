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

import java.util.HashMap;
import java.util.Map;

public abstract class LaPlataDiagonalStreet extends LaPlataStreet {

    private static final Map<Integer, DiagonalBoundaryValidator> VALIDATOR = new HashMap<>(8);

    static {
        VALIDATOR.put(73, new DiagonalBoundaryValidator() {
            @Override
            public boolean isValid(int streetNumber) {
                return betweenAnd(streetNumber, 115, 120)
                        || betweenAnd(streetNumber, 1, 31)
                        || betweenAnd(streetNumber, 132, MAX_LIKE_7_STREET);
            }
        });

        VALIDATOR.put(74, new DiagonalBoundaryValidator() {
            @Override
            public boolean isValid(int streetNumber) {
                return betweenAnd(streetNumber, 115, 120)
                        || betweenAnd(streetNumber, 1, 31)
                        || betweenAnd(streetNumber, 132, MAX_LIKE_7_STREET);
            }
        });

        VALIDATOR.put(75, new DiagonalBoundaryValidator() {
            @Override
            public boolean isValid(int streetNumber) {
                return betweenAnd(streetNumber, 14, 24);
            }
        });

        VALIDATOR.put(76, new DiagonalBoundaryValidator() {
            @Override
            public boolean isValid(int streetNumber) {
                return betweenAnd(streetNumber, 14, 24);
            }
        });

        VALIDATOR.put(77, new DiagonalBoundaryValidator() {
            @Override
            public boolean isValid(int streetNumber) {
                return betweenAnd(streetNumber, 1, 11);
            }
        });

        VALIDATOR.put(78, new DiagonalBoundaryValidator() {
            @Override
            public boolean isValid(int streetNumber) {
                return betweenAnd(streetNumber, 1, 11);
            }
        });

        VALIDATOR.put(79, new DiagonalBoundaryValidator() {

            @Override
            public boolean isValid(int streetNumber) {
                return betweenAnd(streetNumber, 115, 120)
                        || betweenAnd(streetNumber, 1, 5);
            }
        });

        VALIDATOR.put(80, new DiagonalBoundaryValidator() {

            @Override
            public boolean isValid(int streetNumber) {
                return betweenAnd(streetNumber, 115, 120)
                        || betweenAnd(streetNumber, 1, 5);
            }
        });
    }

    protected LaPlataDiagonalStreet(int diagonalNumber) {
        super(diagonalNumber);
    }

    @Override
    public String toString() {
        return "Diagonal " + this.getStreetNumber();
    }

    @Override
    protected final boolean isValidBoundary(int streetNumber) {
        DiagonalBoundaryValidator validator = VALIDATOR.get(this.getStreetNumber());
        return validator != null && validator.isValid(streetNumber);
    }
}
