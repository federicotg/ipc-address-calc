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

public final class LaPlataAddress {

    private LaPlataStreet street;
    private int number;
    private int lowerBoundary;
    private int upperBoundary;
    private final boolean valid;

    public static LaPlataAddress createAddressFromString(String addressString) {

        final String input = addressString.trim().toUpperCase();

        if (input.length() < 3) {
            return createStreetAddress(-1, -9);
        }
        final boolean diagonal = input.charAt(0) == 'D';
        final boolean tolosa = input.charAt(0) == 'T';

        String streetAndNumber = input.substring(diagonal || tolosa ? 1 : 0).trim();
        int space = streetAndNumber.indexOf(' ');
        if (space == -1) {
            return createStreetAddress(-1, -9);
        }
        try {
            int street = Integer.parseInt(streetAndNumber.substring(0, space));
            int number = Integer.parseInt(streetAndNumber.substring(space + 1, streetAndNumber.length()));
            if (tolosa) {
                return createTolosaAddress(street, number);
            } else if (diagonal) {
                return createDiagonalAddress(street, number);
            } else {
                return createStreetAddress(street, number);
            }
        } catch (NumberFormatException nfEx) {
            return createStreetAddress(-1, -9);
        }

    }

    public static LaPlataAddress createDiagonalAddress(
            int diagonalNumber, int number) {
        return new LaPlataAddress(LaPlataStreet.diagonalForNumber(diagonalNumber), number);
    }

    public static LaPlataAddress createStreetAddress(
            int streetNumber, int number) {
        return new LaPlataAddress(LaPlataStreet.streetForNumber(streetNumber), number);
    }

    public static LaPlataAddress createTolosaAddress(
            int streetNumber, int number) {
        return new LaPlataAddress(LaPlataStreet.tolosaStreetForNumber(streetNumber), number);
    }

    private LaPlataAddress(LaPlataStreet street, int number) {
        if (street == null || number <= 0) {
            this.valid = false;
        } else {
            this.street = street;
            this.number = number;
            this.valid = this.initializeBoundaries();
        }

    }

    private boolean initializeBoundaries() {
        int[] boundaries = this.street.getBoundaries(this.number);
        if (boundaries != null) {
            this.lowerBoundary = boundaries[0];
            this.upperBoundary = boundaries[1];
            return true;
        }

        return false;
    }

    public int getLowerBoundary() {
        return lowerBoundary;
    }

    public int getUpperBoundary() {
        return upperBoundary;
    }

    public boolean isValid() {
        return this.valid;
    }

    public LaPlataStreet getStreet() {
        return street;
    }

    public int getNumber() {
        return number;
    }
    
    

    @Override
    public String toString() {
        if (this.isValid()) {
            return new StringBuilder(50)
                    .append(this.street.toString())
                    .append(" Nº ")
                    .append(this.number)
                    .append(" (")
                    .append(this.lowerBoundary)
                    .append(" y ")
                    .append(this.upperBoundary)
                    .append(")").toString();
        }

        return "Dirección Inválida";
    }
}
