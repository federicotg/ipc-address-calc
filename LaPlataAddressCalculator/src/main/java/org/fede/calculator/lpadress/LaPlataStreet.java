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

public abstract class LaPlataStreet {

    protected static final int MAX_LIKE_7_STREET = 312;
    protected static final int MAX_LIKE_51_STREET = 502;
    private static final Map<Integer, LaPlataStreet> DIAGONAL;
    private static final Map<Integer, LaPlataStreet> SPECIAL_STREET;

    static {
        SPECIAL_STREET = new HashMap<>(3);
        DIAGONAL = new HashMap(8);

        DIAGONAL.put(73, new LikeDiagonal80Street(73));
        DIAGONAL.put(74, new LikeDiagonal80Street(74));
        DIAGONAL.put(79, new LikeDiagonal80Street(79));
        DIAGONAL.put(80, new LikeDiagonal80Street(80));

        DIAGONAL.put(75, new LikeDiagonal75Street(75));
        DIAGONAL.put(76, new LikeDiagonal75Street(76));

        DIAGONAL.put(77, new LikeDiagonal78Street(77));
        DIAGONAL.put(78, new LikeDiagonal78Street(78));
        
        SPECIAL_STREET.put(51, new Like53Street(51));
        SPECIAL_STREET.put(52, new Like52Street());
        SPECIAL_STREET.put(53, new Like53Street((53)));
        
    }

    public static final LaPlataStreet diagonalForNumber(int number) {
        return DIAGONAL.get(number);
    }

    protected static boolean betweenAnd(int value, int lower, int upper) {
        return value >= lower && value <= upper;
    }

    public static final LaPlataStreet tolosaStreetForNumber(int number) {
        if (betweenAnd(number, 1, 31) 
                || betweenAnd(number, 115, 120) 
                || betweenAnd(number, 132, MAX_LIKE_7_STREET)) {
            return new TolosaLike13Street(number);
        }
        return streetForNumber(number);
    }

    public static final LaPlataStreet streetForNumber(int number) {
        LaPlataStreet st = SPECIAL_STREET.get(number);
        if(st != null){
            return st;
        }
        
        if (betweenAnd(number, 1, 31) 
                || betweenAnd(number, 115, 120) 
                || betweenAnd(number, 132, MAX_LIKE_7_STREET)) {
            return new Like7Street(number);
        } else if (betweenAnd(number, 32, 99) || betweenAnd(number, 600, 708)) {
            return new Like44Street(number);
        } else if (betweenAnd(number, MAX_LIKE_51_STREET, 531)) {
            return new TolosaLike520Street(number);
        }
        return null;
    }
    
    private final int streetNumber;

    public LaPlataStreet(int streetNumber) {
        this.streetNumber = streetNumber;
    }

    public final int[] getBoundaries(int number) {
        int st = this.getLowerBoundary(number);
        if (this.isValidBoundary(st)) {
            int[] answer = new int[]{st, this.getNextStreet(st)};
            if (answer[0] > answer[1]) {
                int aux = answer[0];
                answer[0] = answer[1];
                answer[1] = aux;
            }
            return answer;
        }
        return null;
    }

    @Override
    public String toString() {
        return String.valueOf(this.streetNumber);
    }

    protected abstract int getLowerBoundary(int number);

    protected abstract boolean isValidBoundary(int streetNumber);

    protected abstract int getNextStreet(int streetNumber);

    public final int getStreetNumber() {
        return this.streetNumber;
    }
}
