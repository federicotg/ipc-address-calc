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
    
    DEPARTAMENTOS("departamentos"),
    CABLEVISION("cablevision"),
    SANTANDER("santander"),
    BOX("box"),
    COMIDA_DISC("comida-disc"),
    OTHER("other"),
    OTHER_USD("other-usd"),
    REPARACIONES("reparaciones"),
    LIMPIEZA("limpieza"),
    EXPENSAS("expensas"),
    NETFLIX("netflix"),
    SUSCRIPCIONES_USD("suscripciones-usd"),
    SUSCRIPCIONES_ARS("suscripciones-ars"),
    VIAJES("viajes"),
    VIAJES_USD("viajes-usd"),
    XBOX("xbox"),
    ATLANTICO("atlantico"),
    ITAU_UY("itau-uy"),
    INVESTMENTS("investments"),
    UNCLASSIFIED("unclassified");

    private final String seriesName;

    SpendingSeries(String seriesName) {
        this.seriesName = seriesName;
    }

    public String getSeriesName() {
        return seriesName;
    }

}
