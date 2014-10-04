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
package org.fede.calculator.money.series;

import java.math.BigDecimal;

/**
 * Fuente http://cadecac.org.ar/historico01.php
 *
 * @author fede
 */
public final class ARSForexSeries extends ArrayIndexSeries {

    // va de 1908 
    // 1908 es 0 a 11
    // 1909 12 a 23
    // 1910 24 a 35
    private static final BigDecimal[] TABLE = {
        new BigDecimal("2.46"), new BigDecimal("2.46"), new BigDecimal("2.46"), new BigDecimal("3.40"), new BigDecimal("2.46"), new BigDecimal("2.46"), new BigDecimal("2.46"), new BigDecimal("2.45"), new BigDecimal("2.44"), new BigDecimal("2.45"), new BigDecimal("2.45"), new BigDecimal("2.46"),
        new BigDecimal("2.45"), new BigDecimal("2.44"), new BigDecimal("2.44"), new BigDecimal("2.44"), new BigDecimal("2.44"), new BigDecimal("2.44"), new BigDecimal("2.47"), new BigDecimal("2.47"), new BigDecimal("2.46"), new BigDecimal("2.46"), new BigDecimal("2.46"), new BigDecimal("2.47"),
        /*1910*/
        new BigDecimal("2.46"), new BigDecimal("2.46"), new BigDecimal("2.47"), new BigDecimal("2.47"), new BigDecimal("2.47"), new BigDecimal("2.48"), new BigDecimal("2.47"), new BigDecimal("2.47"), new BigDecimal("2.46"), new BigDecimal("2.46"), new BigDecimal("2.46"), new BigDecimal("2.47"),
        new BigDecimal("2.47"), new BigDecimal("2.47"), new BigDecimal("2.47"), new BigDecimal("2.47"), new BigDecimal("2.47"), new BigDecimal("2.47"), new BigDecimal("2.47"), new BigDecimal("2.47"), new BigDecimal("2.47"), new BigDecimal("2.40"), new BigDecimal("2.47"), new BigDecimal("2.46"),
        new BigDecimal("2.47"), new BigDecimal("2.46"), new BigDecimal("2.46"), new BigDecimal("2.46"), new BigDecimal("2.46"), new BigDecimal("2.46"), new BigDecimal("2.46"), new BigDecimal("2.47"), new BigDecimal("2.47"), new BigDecimal("2.47"), new BigDecimal("2.47"), new BigDecimal("2.47"),
        new BigDecimal("2.45"), new BigDecimal("2.45"), new BigDecimal("2.46"), new BigDecimal("2.47"), new BigDecimal("2.47"), new BigDecimal("2.48"), new BigDecimal("2.48"), new BigDecimal("2.49"), new BigDecimal("2.49"), new BigDecimal("2.49"), new BigDecimal("2.40"), new BigDecimal("2.49"),
        new BigDecimal("2.48"), new BigDecimal("2.47"), new BigDecimal("2.47"), new BigDecimal("2.48"), new BigDecimal("2.48"), new BigDecimal("2.48"), new BigDecimal("2.49"), new BigDecimal("2.49"), new BigDecimal("2.49"), new BigDecimal("2.49"), new BigDecimal("2.47"), new BigDecimal("2.46"),
        new BigDecimal("2.46"), new BigDecimal("2.47"), new BigDecimal("2.49"), new BigDecimal("2.48"), new BigDecimal("2.49"), new BigDecimal("2.40"), new BigDecimal("2.43"), new BigDecimal("2.47"), new BigDecimal("2.43"), new BigDecimal("2.43"), new BigDecimal("2.40"), new BigDecimal("2.49"),
        new BigDecimal("2.49"), new BigDecimal("2.47"), new BigDecimal("2.46"), new BigDecimal("2.46"), new BigDecimal("2.40"), new BigDecimal("2.48"), new BigDecimal("2.42"), new BigDecimal("2.41"), new BigDecimal("2.41"), new BigDecimal("2.42"), new BigDecimal("2.41"), new BigDecimal("2.45"),
        new BigDecimal("2.40"), new BigDecimal("2.48"), new BigDecimal("2.46"), new BigDecimal("2.44"), new BigDecimal("2.47"), new BigDecimal("2.48"), new BigDecimal("2.41"), new BigDecimal("2.42"), new BigDecimal("2.43"), new BigDecimal("2.44"), new BigDecimal("2.45"), new BigDecimal("2.47"),
        new BigDecimal("2.49"), new BigDecimal("2.48"), new BigDecimal("2.46"), new BigDecimal("2.44"), new BigDecimal("2.43"), new BigDecimal("2.45"), new BigDecimal("2.46"), new BigDecimal("2.46"), new BigDecimal("2.44"), new BigDecimal("2.42"), new BigDecimal("2.43"), new BigDecimal("2.42"),
        new BigDecimal("2.43"), new BigDecimal("2.43"), new BigDecimal("2.42"), new BigDecimal("2.47"), new BigDecimal("80.40"), new BigDecimal("2.43"), new BigDecimal("2.47"), new BigDecimal("2.47"), new BigDecimal("2.46"), new BigDecimal("2.46"), new BigDecimal("2.42"), new BigDecimal("2.42"),
        /*1920*/
        new BigDecimal("2.42"), new BigDecimal("2.42"), new BigDecimal("2.42"), new BigDecimal("2.45"), new BigDecimal("2.47"), new BigDecimal("2.40"), new BigDecimal("2.46"), new BigDecimal("2.47"), new BigDecimal("2.48"), new BigDecimal("2.46"), new BigDecimal("2.45"), new BigDecimal("3.43"),
        new BigDecimal("2.42"), new BigDecimal("2.47"), new BigDecimal("2.48"), new BigDecimal("3.49"), new BigDecimal("3.41"), new BigDecimal("3.43"), new BigDecimal("3.47"), new BigDecimal("3.40"), new BigDecimal("3.49"), new BigDecimal("3.41"), new BigDecimal("3.48"), new BigDecimal("3.41"),
        new BigDecimal("2.41"), new BigDecimal("2.42"), new BigDecimal("2.40"), new BigDecimal("2.49"), new BigDecimal("2.46"), new BigDecimal("2.40"), new BigDecimal("2.47"), new BigDecimal("2.48"), new BigDecimal("2.43"), new BigDecimal("2.40"), new BigDecimal("2.41"), new BigDecimal("2.44"),
        new BigDecimal("2.42"), new BigDecimal("6.40"), new BigDecimal("21.40"), new BigDecimal("2.45"), new BigDecimal("2.49"), new BigDecimal("2.45"), new BigDecimal("2.47"), new BigDecimal("3.41"), new BigDecimal("3.40"), new BigDecimal("3.41"), new BigDecimal("3.42"), new BigDecimal("3.45"),
        new BigDecimal("3.46"), new BigDecimal("2.42"), new BigDecimal("3.44"), new BigDecimal("3.46"), new BigDecimal("3.47"), new BigDecimal("3.47"), new BigDecimal("3.44"), new BigDecimal("2.40"), new BigDecimal("2.49"), new BigDecimal("2.43"), new BigDecimal("2.43"), new BigDecimal("2.41"),
        new BigDecimal("2.49"), new BigDecimal("2.40"), new BigDecimal("2.40"), new BigDecimal("2.40"), new BigDecimal("2.47"), new BigDecimal("2.47"), new BigDecimal("2.48"), new BigDecimal("2.41"), new BigDecimal("2.43"), new BigDecimal("2.43"), new BigDecimal("2.42"), new BigDecimal("2.42"),
        new BigDecimal("2.42"), new BigDecimal("2.45"), new BigDecimal("2.43"), new BigDecimal("2.49"), new BigDecimal("2.40"), new BigDecimal("2.48"), new BigDecimal("2.46"), new BigDecimal("2.48"), new BigDecimal("2.44"), new BigDecimal("2.46"), new BigDecimal("2.46"), new BigDecimal("2.42"),
        new BigDecimal("2.42"), new BigDecimal("2.48"), new BigDecimal("2.46"), new BigDecimal("2.46"), new BigDecimal("2.46"), new BigDecimal("2.45"), new BigDecimal("2.45"), new BigDecimal("2.44"), new BigDecimal("2.44"), new BigDecimal("2.44"), new BigDecimal("2.44"), new BigDecimal("2.44"),
        new BigDecimal("2.44"), new BigDecimal("2.44"), new BigDecimal("2.44"), new BigDecimal("2.44"), new BigDecimal("2.44"), new BigDecimal("2.45"), new BigDecimal("2.47"), new BigDecimal("2.47"), new BigDecimal("2.48"), new BigDecimal("2.47"), new BigDecimal("2.47"), new BigDecimal("2.47"),
        new BigDecimal("2.47"), new BigDecimal("2.48"), new BigDecimal("2.48"), new BigDecimal("2.48"), new BigDecimal("2.49"), new BigDecimal("2.48"), new BigDecimal("2.48"), new BigDecimal("2.48"), new BigDecimal("2.48"), new BigDecimal("2.44"), new BigDecimal("2.42"), new BigDecimal("2.43"),
        /*1930*/
        new BigDecimal("2.42"), new BigDecimal("2.46"), new BigDecimal("2.42"), new BigDecimal("2.42"), new BigDecimal("2.44"), new BigDecimal("2.43"), new BigDecimal("2.41"), new BigDecimal("2.44"), new BigDecimal("2.42"), new BigDecimal("2.42"), new BigDecimal("2.41"), new BigDecimal("3.46"),
        new BigDecimal("3.48"), new BigDecimal("3.41"), new BigDecimal("3.42"), new BigDecimal("3.42"), new BigDecimal("39.40"), new BigDecimal("3.42"), new BigDecimal("3.42"), new BigDecimal("4.41"), new BigDecimal("4.46"), new BigDecimal("4.46"), new BigDecimal("3.49"), new BigDecimal("3.49"),
        new BigDecimal("3.49"), new BigDecimal("3.49"), new BigDecimal("3.49"), new BigDecimal("3.49"), new BigDecimal("3.49"), new BigDecimal("3.49"), new BigDecimal("3.49"), new BigDecimal("3.49"), new BigDecimal("3.49"), new BigDecimal("3.49"), new BigDecimal("3.49"), new BigDecimal("3.49"),
        new BigDecimal("3.49"), new BigDecimal("3.49"), new BigDecimal("3.47"), new BigDecimal("3.47"), new BigDecimal("3.42"), new BigDecimal("3.43"), new BigDecimal("2.48"), new BigDecimal("2.49"), new BigDecimal("2.42"), new BigDecimal("2.42"), new BigDecimal("3.45"), new BigDecimal("4.44"),
        new BigDecimal("3.47"), new BigDecimal("3.45"), new BigDecimal("4.41"), new BigDecimal("4.41"), new BigDecimal("4.44"), new BigDecimal("4.46"), new BigDecimal("3.48"), new BigDecimal("3.47"), new BigDecimal("3.48"), new BigDecimal("3.48"), new BigDecimal("3.48"), new BigDecimal("3.48"),
        new BigDecimal("3.42"), new BigDecimal("3.41"), new BigDecimal("3.43"), new BigDecimal("3.43"), new BigDecimal("3.40"), new BigDecimal("3.48"), new BigDecimal("3.43"), new BigDecimal("3.47"), new BigDecimal("3.48"), new BigDecimal("3.48"), new BigDecimal("3.45"), new BigDecimal("3.40"),
        new BigDecimal("3.41"), new BigDecimal("3.42"), new BigDecimal("3.44"), new BigDecimal("3.44"), new BigDecimal("3.41"), new BigDecimal("3.48"), new BigDecimal("3.45"), new BigDecimal("3.47"), new BigDecimal("3.40"), new BigDecimal("3.40"), new BigDecimal("3.49"), new BigDecimal("3.48"),
        new BigDecimal("3.43"), new BigDecimal("3.44"), new BigDecimal("3.40"), new BigDecimal("3.40"), new BigDecimal("3.47"), new BigDecimal("3.41"), new BigDecimal("3.43"), new BigDecimal("3.45"), new BigDecimal("3.47"), new BigDecimal("3.47"), new BigDecimal("3.41"), new BigDecimal("3.41"),
        new BigDecimal("3.41"), new BigDecimal("3.41"), new BigDecimal("3.41"), new BigDecimal("3.41"), new BigDecimal("3.45"), new BigDecimal("3.44"), new BigDecimal("3.42"), new BigDecimal("3.43"), new BigDecimal("3.49"), new BigDecimal("3.49"), new BigDecimal("4.41"), new BigDecimal("4.41"),
        new BigDecimal("4.47"), new BigDecimal("4.45"), new BigDecimal("4.43"), new BigDecimal("4.43"), new BigDecimal("4.43"), new BigDecimal("4.42"), new BigDecimal("4.45"), new BigDecimal("44.45"), new BigDecimal("4.47"), new BigDecimal("4.47"), new BigDecimal("4.45"), new BigDecimal("4.41"),
        /*1940*/
        new BigDecimal("4.47"), new BigDecimal("4.49"), new BigDecimal("4.45"), new BigDecimal("4.45"), new BigDecimal("4.45"), new BigDecimal("4.47"), new BigDecimal("4.41"), new BigDecimal("4.41"), new BigDecimal("4.41"), new BigDecimal("4.41"), new BigDecimal("4.46"), new BigDecimal("4.43"),
        new BigDecimal("4.43"), new BigDecimal("4.48"), new BigDecimal("4.44"), new BigDecimal("4.44"), new BigDecimal("4.41"), new BigDecimal("4.41"), new BigDecimal("4.41"), new BigDecimal("4.46"), new BigDecimal("4.40"), new BigDecimal("4.40"), new BigDecimal("4.49"), new BigDecimal("4.44"),
        new BigDecimal("4.44"), new BigDecimal("4.43"), new BigDecimal("4.44"), new BigDecimal("4.44"), new BigDecimal("4.45"), new BigDecimal("4.45"), new BigDecimal("4.43"), new BigDecimal("4.43"), new BigDecimal("4.41"), new BigDecimal("4.41"), new BigDecimal("4.44"), new BigDecimal("4.45"),
        new BigDecimal("4.43"), new BigDecimal("4.42"), new BigDecimal("4.43"), new BigDecimal("4.43"), new BigDecimal("40.40"), new BigDecimal("3.48"), new BigDecimal("4.40"), new BigDecimal("4.40"), new BigDecimal("4.40"), new BigDecimal("4.40"), new BigDecimal("3.48"), new BigDecimal("3.48"),
        new BigDecimal("3.48"), new BigDecimal("4.41"), new BigDecimal("4.41"), new BigDecimal("4.41"), new BigDecimal("4.44"), new BigDecimal("4.44"), new BigDecimal("4.43"), new BigDecimal("4.44"), new BigDecimal("4.43"), new BigDecimal("4.43"), new BigDecimal("4.42"), new BigDecimal("4.45"),
        new BigDecimal("4.45"), new BigDecimal("4.41"), new BigDecimal("4.44"), new BigDecimal("4.44"), new BigDecimal("4.43"), new BigDecimal("4.42"), new BigDecimal("4.42"), new BigDecimal("4.43"), new BigDecimal("4.44"), new BigDecimal("4.44"), new BigDecimal("4.45"), new BigDecimal("4.47"),
        new BigDecimal("4.40"), new BigDecimal("4.48"), new BigDecimal("4.41"), new BigDecimal("4.41"), new BigDecimal("4.49"), new BigDecimal("4.40"), new BigDecimal("4.47"), new BigDecimal("4.46"), new BigDecimal("4.49"), new BigDecimal("4.49"), new BigDecimal("4.41"), new BigDecimal("4.41"),
        new BigDecimal("4.45"), new BigDecimal("4.43"), new BigDecimal("4.40"), new BigDecimal("4.40"), new BigDecimal("4.41"), new BigDecimal("4.45"), new BigDecimal("4.45"), new BigDecimal("5.40"), new BigDecimal("4.40"), new BigDecimal("4.40"), new BigDecimal("4.45"), new BigDecimal("4.40"),
        new BigDecimal("4.40"), new BigDecimal("4.40"), new BigDecimal("4.45"), new BigDecimal("4.45"), new BigDecimal("4.40"), new BigDecimal("6.40"), new BigDecimal("7.40"), new BigDecimal("7.45"), new BigDecimal("11.40"), new BigDecimal("11.40"), new BigDecimal("9.40"), new BigDecimal("9.45"),
        new BigDecimal("9.40"), new BigDecimal("9.45"), new BigDecimal("11.40"), new BigDecimal("11.40"), new BigDecimal("9.40"), new BigDecimal("10.40"), new BigDecimal("10.40"), new BigDecimal("13.40"), new BigDecimal("13.40"), new BigDecimal("13.40"), new BigDecimal("16.40"), new BigDecimal("15.40"),
        /*1950*/
        new BigDecimal("15.40"), new BigDecimal("14.40"), new BigDecimal("13.45"), new BigDecimal("13.45"), new BigDecimal("13.45"), new BigDecimal("13.44"), new BigDecimal("17.45"), new BigDecimal("18.45"), new BigDecimal("19.43"), new BigDecimal("19.43"), new BigDecimal("19.45"), new BigDecimal("16.40"),
        new BigDecimal("16.45"), new BigDecimal("15.40"), new BigDecimal("18.40"), new BigDecimal("19.40"), new BigDecimal("25.40"), new BigDecimal("24.40"), new BigDecimal("24.40"), new BigDecimal("28.40"), new BigDecimal("29.40"), new BigDecimal("27.40"), new BigDecimal("77.40"), new BigDecimal("27.40"),
        new BigDecimal("28.40"), new BigDecimal("26.40"), new BigDecimal("24.40"), new BigDecimal("25.40"), new BigDecimal("23.40"), new BigDecimal("21.45"), new BigDecimal("21.40"), new BigDecimal("20.40"), new BigDecimal("19.40"), new BigDecimal("20.40"), new BigDecimal("21.40"), new BigDecimal("23.45"),
        new BigDecimal("23.45"), new BigDecimal("22.45"), new BigDecimal("22.45"), new BigDecimal("23.45"), new BigDecimal("23.40"), new BigDecimal("23.40"), new BigDecimal("23.45"), new BigDecimal("21.45"), new BigDecimal("22.45"), new BigDecimal("22.45"), new BigDecimal("19.40"), new BigDecimal("20.40"),
        new BigDecimal("22.45"), new BigDecimal("22.40"), new BigDecimal("23.45"), new BigDecimal("25.40"), new BigDecimal("26.40"), new BigDecimal("25.40"), new BigDecimal("26.45"), new BigDecimal("26.45"), new BigDecimal("26.40"), new BigDecimal("26.40"), new BigDecimal("26.45"), new BigDecimal("26.40"),
        new BigDecimal("27.45"), new BigDecimal("28.40"), new BigDecimal("28.40"), new BigDecimal("29.40"), new BigDecimal("31.45"), new BigDecimal("33.45"), new BigDecimal("30.45"), new BigDecimal("31.40"), new BigDecimal("27.40"), new BigDecimal("29.45"), new BigDecimal("32.40"), new BigDecimal("36.40"),
        new BigDecimal("40.40"), new BigDecimal("42.45"), new BigDecimal("40.40"), new BigDecimal("37.40"), new BigDecimal("35.40"), new BigDecimal("32.40"), new BigDecimal("30.40"), new BigDecimal("32.40"), new BigDecimal("30.40"), new BigDecimal("32.40"), new BigDecimal("34.40"), new BigDecimal("36.45"),
        new BigDecimal("37.40"), new BigDecimal("36.40"), new BigDecimal("40.40"), new BigDecimal("37.40"), new BigDecimal("40.45"), new BigDecimal("40.45"), new BigDecimal("42.40"), new BigDecimal("43.45"), new BigDecimal("43.40"), new BigDecimal("39.45"), new BigDecimal("37.40"), new BigDecimal("37.40"),
        new BigDecimal("37.45"), new BigDecimal("38.45"), new BigDecimal("40.40"), new BigDecimal("42.40"), new BigDecimal("42.40"), new BigDecimal("42.40"), new BigDecimal("42.40"), new BigDecimal("46.40"), new BigDecimal("54.45"), new BigDecimal("73.40"), new BigDecimal("71.40"), new BigDecimal("70.40"),
        new BigDecimal("65.40"), new BigDecimal("68.40"), new BigDecimal("68.40"), new BigDecimal("80.40"), new BigDecimal("88.48"), new BigDecimal("84.40"), new BigDecimal("86.40"), new BigDecimal("83.40"), new BigDecimal("82.40"), new BigDecimal("82.40"), new BigDecimal("83.40"), new BigDecimal("83.40"),
        /*1960*/
        new BigDecimal("82.40"), new BigDecimal("82.40"), new BigDecimal("82.40"), new BigDecimal("83.40"), new BigDecimal("82.40"), new BigDecimal("82.40"), new BigDecimal("82.40"), new BigDecimal("82.40"), new BigDecimal("8.40"), new BigDecimal("82.40"), new BigDecimal("82.45"), new BigDecimal("82.45"),
        new BigDecimal("82.45"), new BigDecimal("82.40"), new BigDecimal("82.40"), new BigDecimal("83.40"), new BigDecimal("82.40"), new BigDecimal("82.40"), new BigDecimal("82.45"), new BigDecimal("83.40"), new BigDecimal("83.45"), new BigDecimal("83.45"), new BigDecimal("83.40"), new BigDecimal("84.44"),
        new BigDecimal("83.45"), new BigDecimal("83.40"), new BigDecimal("83.40"), new BigDecimal("99.40"), new BigDecimal("112.40"), new BigDecimal("133.40"), new BigDecimal("118.40"), new BigDecimal("12.43"), new BigDecimal("128.40"), new BigDecimal("139.40"), new BigDecimal("148.40"), new BigDecimal("134.40"),
        new BigDecimal("134.40"), new BigDecimal("135.40"), new BigDecimal("140.40"), new BigDecimal("137.40"), new BigDecimal("138.40"), new BigDecimal("139.40"), new BigDecimal("134.40"), new BigDecimal("134.40"), new BigDecimal("149.40"), new BigDecimal("146.40"), new BigDecimal("140.40"), new BigDecimal("132.40"),
        new BigDecimal("134.45"), new BigDecimal("130.40"), new BigDecimal("138.45"), new BigDecimal("142.40"), new BigDecimal("140.40"), new BigDecimal("156.40"), new BigDecimal("174.45"), new BigDecimal("169.45"), new BigDecimal("161.42"), new BigDecimal("166.45"), new BigDecimal("179.41"), new BigDecimal("192.49"),
        new BigDecimal("215.40"), new BigDecimal("232.48"), new BigDecimal("217.48"), new BigDecimal("227.40"), new BigDecimal("248.40"), new BigDecimal("276.40"), new BigDecimal("285.41"), new BigDecimal("272.44"), new BigDecimal("258.48"), new BigDecimal("233.47"), new BigDecimal("252.40"), new BigDecimal("233.40"),
        new BigDecimal("247.47"), new BigDecimal("238.46"), new BigDecimal("228.40"), new BigDecimal("222.48"), new BigDecimal("234.40"), new BigDecimal("282.40"), new BigDecimal("224.46"), new BigDecimal("227.48"), new BigDecimal("247.45"), new BigDecimal("256.48"), new BigDecimal("266.48"), new BigDecimal("270.40"),
        new BigDecimal("282.42"), new BigDecimal("295.47"), new BigDecimal("345.44"), new BigDecimal("348.48"), new BigDecimal("348.45"), new BigDecimal("349.40"), new BigDecimal("350.40"), new BigDecimal("349.45"), new BigDecimal("349.45"), new BigDecimal("349.40"), new BigDecimal("349.40"), new BigDecimal("350.40"),
        new BigDecimal("350.40"), new BigDecimal("350.40"), new BigDecimal("349.45"), new BigDecimal("349.40"), new BigDecimal("350.40"), new BigDecimal("349.40"), new BigDecimal("349.40"), new BigDecimal("350.40"), new BigDecimal("350.40"), new BigDecimal("350.40"), new BigDecimal("350.40"), new BigDecimal("350.40"),
        new BigDecimal("350.40"), new BigDecimal("349.45"), new BigDecimal("349.40"), new BigDecimal("350.45"), new BigDecimal("351.45"), new BigDecimal("352.40"), new BigDecimal("351.45"), new BigDecimal("351.45"), new BigDecimal("351.45"), new BigDecimal("351.40"), new BigDecimal("351.40"), new BigDecimal("352.45"),
        /*Pesos Ley*/
        /*1970*/
        new BigDecimal("3.4975"), new BigDecimal("3.5000"), new BigDecimal("3.4850"), new BigDecimal("3.4950"), new BigDecimal("3.5125"), new BigDecimal("4.0100"), new BigDecimal("4.0100"), new BigDecimal("4.7500"), new BigDecimal("4.0175"), new BigDecimal("4.1650"), new BigDecimal("4.2900"), new BigDecimal("4.3350"),
        new BigDecimal("4.2750"), new BigDecimal("4.1750"), new BigDecimal("4.3350"), new BigDecimal("4.6350"), new BigDecimal("4.8050"), new BigDecimal("5.2550"), new BigDecimal("5.3750"), new BigDecimal("5.8050"), new BigDecimal("6.9950"), new BigDecimal("8.8500"), new BigDecimal("9.7750"), new BigDecimal("9.4000"),
        new BigDecimal("9.5000"), new BigDecimal("9.7000"), new BigDecimal("10.5000"), new BigDecimal("10.5000"), new BigDecimal("11.0000"), new BigDecimal("11.0000"), new BigDecimal("12.0000"), new BigDecimal("13.0000"), new BigDecimal("13.5000"), new BigDecimal("13.3600"), new BigDecimal("12.1000"), new BigDecimal("11.3000"),
        new BigDecimal("12.1300"), new BigDecimal("11.4500"), new BigDecimal("11.4300"), new BigDecimal("12.5000"), new BigDecimal("12.5000"), new BigDecimal("10.6800"), new BigDecimal("10.1000"), new BigDecimal("10.9500"), new BigDecimal("10.1000"), new BigDecimal("10.6000"), new BigDecimal("10.5800"), new BigDecimal("11.2000"),
        new BigDecimal("11.9000"), new BigDecimal("12.3000"), new BigDecimal("12.4000"), new BigDecimal("13.2300"), new BigDecimal("14.4000"), new BigDecimal("14.9000"), new BigDecimal("16.1600"), new BigDecimal("17.5500"), new BigDecimal("17.7000"), new BigDecimal("20.0500"), new BigDecimal("20.9000"), new BigDecimal("22.0000"),
        new BigDecimal("22.6500"), new BigDecimal("23.4500"), new BigDecimal("28.3500"), new BigDecimal("36.4500"), new BigDecimal("47.0000"), new BigDecimal("53.0000"), new BigDecimal("66.5000"), new BigDecimal("76.0000"), new BigDecimal("110.0000"), new BigDecimal("142.5000"), new BigDecimal("132.5000"), new BigDecimal("127.5000"),
        new BigDecimal("196.0000"), new BigDecimal("270.0000"), new BigDecimal("325.0000"), new BigDecimal("255.0000"), new BigDecimal("245.0000"), new BigDecimal("247.0000"), new BigDecimal("250.0000"), new BigDecimal("263.0000"), new BigDecimal("247.0000"), new BigDecimal("245.5000"), new BigDecimal("273.0000"), new BigDecimal("276.0000"),
        new BigDecimal("3.0000"), new BigDecimal("327.0000"), new BigDecimal("345.0000"), new BigDecimal("375.0000"), new BigDecimal("370.0000"), new BigDecimal("393.0000"), new BigDecimal("413.0000"), new BigDecimal("439.0000"), new BigDecimal("468.0000"), new BigDecimal("513.0000"), new BigDecimal("552.0000"), new BigDecimal("610.0000"),
        new BigDecimal("640.0000"), new BigDecimal("685.0000"), new BigDecimal("721.0000"), new BigDecimal("761.0000"), new BigDecimal("766.0000"), new BigDecimal("787.0000"), new BigDecimal("798.0000"), new BigDecimal("834.0000"), new BigDecimal("860.0000"), new BigDecimal("922.0000"), new BigDecimal("961.0000"), new BigDecimal("1020.0000"),
        new BigDecimal("1050.0000"), new BigDecimal("1115.0000"), new BigDecimal("1172.0000"), new BigDecimal("1207.0000"), new BigDecimal("1269.0000"), new BigDecimal("1324.0000"), new BigDecimal("1374.0000"), new BigDecimal("1427.0000"), new BigDecimal("1469.0000"), new BigDecimal("1532.0000"), new BigDecimal("1589.0000"), new BigDecimal("1643.0000"),
        /*1980*/
        new BigDecimal("1667.0000"), new BigDecimal("1712.0000"), new BigDecimal("1757.0000"), new BigDecimal("1790.0000"), new BigDecimal("1829.0000"), new BigDecimal("1864.0000"), new BigDecimal("1874.0000"), new BigDecimal("1901.0000"), new BigDecimal("1926.0000"), new BigDecimal("1946.0000"), new BigDecimal("1966.0000"), new BigDecimal("1986.0000"),
        new BigDecimal("2016.0000"), new BigDecimal("2247.0000"), new BigDecimal("2338.0000"), new BigDecimal("3096.0000"), new BigDecimal("3229.0000"), new BigDecimal("4974.0000"), new BigDecimal("6490.0000"), new BigDecimal("7466.0000"), new BigDecimal("7455.0000"), new BigDecimal("8190.0000"), new BigDecimal("10683.0000"), new BigDecimal("10722.0000"),
        new BigDecimal("9755.0000"), new BigDecimal("9845.0000"), new BigDecimal("10760.0000"), new BigDecimal("11630.0000"), new BigDecimal("13704.0000"), new BigDecimal("14976.0000"), new BigDecimal("46020.0000"), new BigDecimal("52642.0000"), new BigDecimal("48681.0000"), new BigDecimal("49525.0000"), new BigDecimal("59568.0000"), new BigDecimal("63050.0000"),
        /*1983*/
        new BigDecimal("69500.0000"), new BigDecimal("76100.0000"), new BigDecimal("87300.0000"), new BigDecimal("94500.0000"), new BigDecimal("94600.0000"),
        /*Peso Argentino junio 1983*/
        new BigDecimal("10.2200"), new BigDecimal("12.9000"), new BigDecimal("16.6800"), new BigDecimal("21.8100"), new BigDecimal("25.5500"), new BigDecimal("23.7500"), new BigDecimal("24.6600"),
        new BigDecimal("30.7700"), new BigDecimal("40.5100"), new BigDecimal("53.0000"), new BigDecimal("58.2000"), new BigDecimal("65.8000"), new BigDecimal("73.5000"), new BigDecimal("82.0000"), new BigDecimal("109.5000"), new BigDecimal("122.4000"), new BigDecimal("139.7000"), new BigDecimal("182.0000"), new BigDecimal("206.0000"),
        new BigDecimal("265.0000"), new BigDecimal("354.0000"), new BigDecimal("467.0000"), new BigDecimal("559.0000"), new BigDecimal("675.0000"),
        /*Austral junio 1985*/
        new BigDecimal("0.8650"), new BigDecimal("0.9700"), new BigDecimal("0.9500"), new BigDecimal("0.9000"), new BigDecimal("0.9450"), new BigDecimal("0.8650"), new BigDecimal("0.8675"),
        new BigDecimal("0.8850"), new BigDecimal("0.8700"), new BigDecimal("0.9425"), new BigDecimal("0.9150"), new BigDecimal("0.8925"), new BigDecimal("0.8900"), new BigDecimal("0.9300"), new BigDecimal("1.1800"), new BigDecimal("1.2200"), new BigDecimal("1.2700"), new BigDecimal("1.4450"), new BigDecimal("1.6800"),
        new BigDecimal("1.6200"), new BigDecimal("1.7300"), new BigDecimal("2.0500"), new BigDecimal("2.0800"), new BigDecimal("2.0700"), new BigDecimal("2.1500"), new BigDecimal("2.5700"), new BigDecimal("3.1000"), new BigDecimal("3.6400"), new BigDecimal("4.0600"), new BigDecimal("4.3500"), new BigDecimal("5.1100"),
        new BigDecimal("5.5000"), new BigDecimal("6.1800"), new BigDecimal("6.4000"), new BigDecimal("7.3400"), new BigDecimal("9.2200"), new BigDecimal("12.8600"), new BigDecimal("12.6200"), new BigDecimal("14.4000"), new BigDecimal("14.8800"), new BigDecimal("15.1000"), new BigDecimal("15.6100"), new BigDecimal("16.4100"),
        new BigDecimal("17.7000"), new BigDecimal("27.3000"), new BigDecimal("48.5000"), new BigDecimal("79.0000"), new BigDecimal("240.0000"), new BigDecimal("520.0000"), new BigDecimal("660.0000"), new BigDecimal("662.0000"), new BigDecimal("648.0000"), new BigDecimal("723.0000"), new BigDecimal("1010.0000"), new BigDecimal("1950.0000"),
        /*1990*/
        new BigDecimal("1870.0000"), new BigDecimal("5850.0000"), new BigDecimal("4650.0000"), new BigDecimal("4980.0000"), new BigDecimal("5.0000"), new BigDecimal("5270.0000"), new BigDecimal("55.0000"), new BigDecimal("6270.0000"), new BigDecimal("5260.0000"), new BigDecimal("5570.0000"), new BigDecimal("52.0000"), new BigDecimal("5820.0000"),
        new BigDecimal("93.0000"), new BigDecimal("103.0000"), new BigDecimal("9640.0000"), new BigDecimal("9835.0000"), new BigDecimal("9925.0000"), new BigDecimal("9995.0000"), new BigDecimal("9975.0000"), new BigDecimal("9975.0000"), new BigDecimal("9905.0000"), new BigDecimal("9915.0000"), new BigDecimal("9910.0000"), new BigDecimal("10030.0000"),
        /*Peso 1992*/
        new BigDecimal("0.9901"), new BigDecimal("0.9900"), new BigDecimal("0.9935"), new BigDecimal("0.9896"), new BigDecimal("0.9896"), new BigDecimal("0.9912"), new BigDecimal("0.9920"), new BigDecimal("0.9911"), new BigDecimal("0.9912"), new BigDecimal("0.9912"), new BigDecimal("0.9930"), new BigDecimal("0.9975"),
        new BigDecimal("0.9992"), new BigDecimal("0.9992"), new BigDecimal("0.9998"), new BigDecimal("0.9979"), new BigDecimal("1.2000"), new BigDecimal("0.9984"), new BigDecimal("0.9997"), new BigDecimal("1.1400"), new BigDecimal("1.1400"), new BigDecimal("0.9992"), new BigDecimal("0.9976"), new BigDecimal("0.9985"),
        new BigDecimal("0.9985"), new BigDecimal("1.2000"), new BigDecimal("1.1000"), new BigDecimal("0.9987"), new BigDecimal("0.9982"), new BigDecimal("0.9975"), new BigDecimal("0.9984"), new BigDecimal("0.9992"), new BigDecimal("0.9994"), new BigDecimal("0.9993"), new BigDecimal("1.0000"), new BigDecimal("1.0150"),
        new BigDecimal("1.1200"), new BigDecimal("1.1800"), new BigDecimal("1.0000"), new BigDecimal("1.1200"), new BigDecimal("0.9987"), new BigDecimal("0.9990"), new BigDecimal("0.9995"), new BigDecimal("0.9990"), new BigDecimal("0.9990"), new BigDecimal("1.4000"), new BigDecimal("0.9993"), new BigDecimal("1.1700"),
        new BigDecimal("0.9993"), new BigDecimal("0.9999"), new BigDecimal("0.9998"), new BigDecimal("0.9998"), new BigDecimal("1.1000"), new BigDecimal("1.7000"), new BigDecimal("1.1300"), new BigDecimal("1.2000"), new BigDecimal("1.3000"), new BigDecimal("0.9996"), new BigDecimal("0.9990"), new BigDecimal("1.6000"),
        new BigDecimal("0.9986"), new BigDecimal("0.9994"), new BigDecimal("0.9995"), new BigDecimal("0.9996"), new BigDecimal("0.9994"), new BigDecimal("1.6000"), new BigDecimal("0.9999"), new BigDecimal("0.9997"), new BigDecimal("0.9996"), new BigDecimal("1.2600"), new BigDecimal("1.8000"), new BigDecimal("1.1000"),
        new BigDecimal("0.9989"), new BigDecimal("0.9990"), new BigDecimal("1.8000"), new BigDecimal("1.2000"), new BigDecimal("0.9998"), new BigDecimal("0.9998"), new BigDecimal("1.5000"), new BigDecimal("1.2000"), new BigDecimal("1.2000"), new BigDecimal("1.5000"), new BigDecimal("1.0000"), new BigDecimal("1.9000"),
        new BigDecimal("1.4000"), new BigDecimal("1.9000"), new BigDecimal("0.9988"), new BigDecimal("0.9995"), new BigDecimal("1.2000"), new BigDecimal("1.5000"), new BigDecimal("0.9998"), new BigDecimal("1.5000"), new BigDecimal("1.0000"), new BigDecimal("1.1000"), new BigDecimal("1.8000"), new BigDecimal("1.1500"),
        /*2000*/
        new BigDecimal("0.9995"), new BigDecimal("0.9998"), new BigDecimal("0.9992"), new BigDecimal("0.9988"), new BigDecimal("1.1000"), new BigDecimal("0.9985"), new BigDecimal("0.9991"), new BigDecimal("0.9987"), new BigDecimal("0.9989"), new BigDecimal("1.0000"), new BigDecimal("1.5000"), new BigDecimal("1.1500"),
        new BigDecimal("0.9999"), new BigDecimal("0.9997"), new BigDecimal("1.1000"), new BigDecimal("1.2000"), new BigDecimal("0.9995"), new BigDecimal("1.1100"), new BigDecimal("1.3000"), new BigDecimal("0.9990"), new BigDecimal("0.9990"), new BigDecimal("1.3000"), new BigDecimal("1.4000"), new BigDecimal("1.0150"),
        new BigDecimal("1.8000"), new BigDecimal("2.1500"), new BigDecimal("3.0000"), new BigDecimal("2.9800"), new BigDecimal("3.6000"), new BigDecimal("3.8000"), new BigDecimal("3.7000"), new BigDecimal("3.6300"), new BigDecimal("3.7400"), new BigDecimal("3.5200"), new BigDecimal("3.6400"), new BigDecimal("3.3700"),
        new BigDecimal("3.2100"), new BigDecimal("3.1900"), new BigDecimal("2.9800"), new BigDecimal("2.8200"), new BigDecimal("2.8500"), new BigDecimal("2.8000"), new BigDecimal("2.9200"), new BigDecimal("2.9560"), new BigDecimal("2.9150"), new BigDecimal("2.8650"), new BigDecimal("2.9900"), new BigDecimal("2.9350"),
        new BigDecimal("2.9280"), new BigDecimal("2.9230"), new BigDecimal("2.8600"), new BigDecimal("2.8400"), new BigDecimal("2.9600"), new BigDecimal("2.9580"), new BigDecimal("2.9800"), new BigDecimal("2.9970"), new BigDecimal("2.9840"), new BigDecimal("2.9700"), new BigDecimal("2.9450"), new BigDecimal("2.9790"),
        new BigDecimal("2.9240"), new BigDecimal("2.9400"), new BigDecimal("2.9170"), new BigDecimal("2.9100"), new BigDecimal("2.8800"), new BigDecimal("2.8900"), new BigDecimal("2.8600"), new BigDecimal("2.9100"), new BigDecimal("2.9100"), new BigDecimal("3.0000"), new BigDecimal("2.9600"), new BigDecimal("3.0300"),
        new BigDecimal("3.0600"), new BigDecimal("3.0700"), new BigDecimal("3.0800"), new BigDecimal("3.0480"), new BigDecimal("3.0800"), new BigDecimal("3.0800"), new BigDecimal("3.0700"), new BigDecimal("3.0970"), new BigDecimal("3.1000"), new BigDecimal("3.0890"), new BigDecimal("3.0680"), new BigDecimal("3.0600"),
        new BigDecimal("3.1000"), new BigDecimal("3.1000"), new BigDecimal("3.1000"), new BigDecimal("3.0900"), new BigDecimal("3.0800"), new BigDecimal("3.0900"), new BigDecimal("3.1200"), new BigDecimal("3.1500"), new BigDecimal("3.1500"), new BigDecimal("3.1400"), new BigDecimal("3.1400"), new BigDecimal("3.1400"),
        new BigDecimal("3.1500"), new BigDecimal("3.1600"), new BigDecimal("3.1600"), new BigDecimal("3.1600"), new BigDecimal("3.1100"), new BigDecimal("3.0200"), new BigDecimal("3.0400"), new BigDecimal("3.0300"), new BigDecimal("3.1300"), new BigDecimal("3.3800"), new BigDecimal("3.3700"), new BigDecimal("3.4500"),
        new BigDecimal("3.4880"), new BigDecimal("3.5600"), new BigDecimal("3.7200"), new BigDecimal("3.7100"), new BigDecimal("3.7400"), new BigDecimal("3.7900"), new BigDecimal("3.8300"), new BigDecimal("3.8500"), new BigDecimal("3.8400"), new BigDecimal("3.8200"), new BigDecimal("3.8100"), new BigDecimal("3.8000"),
        new BigDecimal("3.8350"), new BigDecimal("3.8500"), new BigDecimal("3.8700"), new BigDecimal("3.8800"), new BigDecimal("3.9200"), new BigDecimal("3.9300"), new BigDecimal("3.9400"), new BigDecimal("3.9500"), new BigDecimal("3.9600"), new BigDecimal("3.9500"), new BigDecimal("3.9800"), new BigDecimal("3.9700"),
        new BigDecimal("4.0080"), new BigDecimal("4.0300"), new BigDecimal("4.0500"), new BigDecimal("4.0800"), new BigDecimal("4.0900"), new BigDecimal("4.1100"), new BigDecimal("4.1400"), new BigDecimal("4.2000"), new BigDecimal("4.2000"), new BigDecimal("4.2300"), new BigDecimal("4.2800"), new BigDecimal("4.3000"),
        new BigDecimal("4.3370"), new BigDecimal("4.3570"), new BigDecimal("4.3790"), new BigDecimal("4.4100"), new BigDecimal("4.4700"), new BigDecimal("4.5270"), new BigDecimal("4.5850"), new BigDecimal("4.6380"), new BigDecimal("4.6900"), new BigDecimal("4.7660"), new BigDecimal("4.8390"), new BigDecimal("4.9180"),
        new BigDecimal("4.9780"), new BigDecimal("5.0400"), new BigDecimal("5.1200"), new BigDecimal("5.1870"), new BigDecimal("5.2800"), new BigDecimal("5.3880"), new BigDecimal("5.5000"), new BigDecimal("5.6700"), new BigDecimal("5.7900"), new BigDecimal("5.9100"), new BigDecimal("6.1400"), new BigDecimal("6.5200"),
        /* 2014*/
        new BigDecimal("8.0100"), new BigDecimal("7.8700"), new BigDecimal("8.0020"), new BigDecimal("8.0000"), new BigDecimal("8.0800"), new BigDecimal("8.1300"), new BigDecimal("8.2100"), new BigDecimal("8.4010")

    };

    public ARSForexSeries() {
        super(1908, TABLE);
    }

}
