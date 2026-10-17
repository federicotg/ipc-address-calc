/*
 * Copyright (C) 2026 fede
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
package org.fede.calculator.report;

/**
 *
 * @author fede
 */
public enum SpendingSeries {

    BBPP("bbpp"),
    INMOBILIARIO_43("inmobiliario-43"),
    MONOTRIBUTO_ANGELES("monotributo-angeles"),
    MONOTRIBUTO("monotributo"),
    MUNICIPAL_43("municipal-43"),
    CONTADORA("contadora"),
    CELULAR_A("celular-a"),
    CELULAR_F("celular-f"),
    TELEFONO_43("telefono-43"),
    EMERGENCIA("emergencia"),
    IOMA("ioma"),
    COMIDA("comida"),
    SELLOS("sellos"),
    SALUD("salud"),
    SEGURO("seguro"),
    GAS("gas"),
    LUZ("luz"),

    COLON_OSSE("colon-osse"),
    COLON_EDEA("colon-edea"),
    COLON_MUNICIPAL("colon-municipal"),
    COLON_FLOW("colon-flow"),
    COLON_CAMUZZI("colon-camuzzi"),
    COLON_EXPENSAS("colon-expensas"),

    DEPARTAMENTOS("departamentos", 36),
    CABLEVISION("cablevision"),
    SANTANDER("santander"),
    BOX("box"),
    COMIDA_DISC("comida-disc"),

    ANGELES_OTHER_DISC("angeles-other-disc"),

    OTHER("other", 36),
    OTHER_USD("other-usd", 36),
    REPARACIONES("reparaciones", 36),
    LIMPIEZA("limpieza"),
    EXPENSAS("expensas"),
    NETFLIX("netflix"),
    SUSCRIPCIONES_USD("suscripciones-usd"),
    SUSCRIPCIONES_ARS("suscripciones-ars"),
    VIAJES("viajes"),
    VIAJES_USD("viajes-usd", 36),
    XBOX("xbox"),
    ATLANTICO("atlantico"),
    ITAU_UY("itau-uy"),
    INVESTMENTS("investments"),
    EXTRAORDINARY("extraordinary", 60),
    UNCLASSIFIED("unclassified");

    private static final int DEFAULT_AVERAGE_MONTHS = 12;

    private final String seriesName;
    private final int averageMonths;

    SpendingSeries(String seriesName) {
        this(seriesName, DEFAULT_AVERAGE_MONTHS);
    }

    SpendingSeries(String seriesName, int averageMonths) {
        this.seriesName = seriesName;
        this.averageMonths = averageMonths;
    }

    public String getSeriesName() {
        return seriesName;
    }

    public int getAverageMonths() {
        return averageMonths;
    }

}
