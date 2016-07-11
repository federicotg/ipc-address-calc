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
package org.fede.calculator.money;

import java.math.BigDecimal;

/*
 Peso  Moneda  Nacional	Peso Ley	Peso Argentino	Austral	Peso
 Peso Moneda nacional	1	0,01	0,000001	0,000000001	1E-013
 Peso Ley	100	1	0,0001	0,0000001	0,00000000001
 Peso Argentino	1.000.000	10.000	1	0,001	0
 Austral	1.000.000.000	10.000.000	1.000	1	0,0001
 Peso	10.000.000.000.000	100.000.000.000	10.000.000	10.000	1
 Peso  Moneda  Nacional	Peso Ley	Peso Argentino	Austral	Peso
 Peso Moneda nacional	0	-2	-6	-9	-13
 Peso Ley	2	0	-4	-7	-11
 Peso Argentino	6	4	0	-3	-7
 Austral	9	7	3	0	-4
 Peso	13	11	7	4	0
 */
public enum ArgCurrency {

    MONEDA_NACIONAL(0, "Peso Moneda Nacional", "m$n", "ARM"),
    LEY(1, "Peso Ley 18.188", "$", "ARL"),
    ARGENTINO(2, "Peso Argentino", "$a", "ARP"),
    AUSTRAL(3, "Austral", "\u20B3", "ARA"),
    PESO(4, "Peso", "$", "ARS");

    private final int[][] zeroes = {
        {0, -2, -6, -9, -13},
        {2, 0, -4, -7, -11},
        {6, 4, 0, -3, -7},
        {9, 7, 3, 0, -4},
        {13, 11, 7, 4, 0}
    };

    private final int id;
    private final String symbol;
    private final String name;
    private final String iso4217;

    ArgCurrency(int id, String name, String symbol, String iso4217) {
        this.id = id;
        this.name = name;
        this.iso4217 = iso4217;
        this.symbol = symbol;
    }

    public static ArgCurrency whichCurrency(int year, int month) {
        if (year < 1970) {
            //Peso Moneda Nacional
            return MONEDA_NACIONAL;
        }
        if (year < 1983 || (year == 1983 && month < 6)) {
            //Peso Ley
            return LEY;
        }
        if (year < 1985 || (year == 1985 && month < 6)) {
            //Peso Argentino
            return ARGENTINO;
        }
        if (year < 1992) {
            //Austral
            return AUSTRAL;
        }
        // Peso
        return ArgCurrency.PESO;
    }

    public static BigDecimal convertTo(BigDecimal value, int fromYear, int fromMonth, int toYear, int toMonth) {
        return whichCurrency(fromYear, fromMonth).convertTo(value, whichCurrency(toYear, toMonth));
    }

    private BigDecimal convertTo(BigDecimal value, ArgCurrency to) {
        final int n = this.zeroes[this.id][to.id];
        if (n != 0) {
            return value.movePointRight(n);
        }
        return value;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public String getIso4217() {
        return iso4217;
    }

}
