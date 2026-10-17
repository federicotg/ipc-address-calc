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
public enum ExpenseTier {
    ESSENTIAL,
    DISCRETIONARY,
    LUXURY;

    public static ExpenseTier of(SpendingSeries s) {
        return switch (s) {
            case UNCLASSIFIED, BBPP, COMIDA, EMERGENCIA, EXPENSAS, GAS, INMOBILIARIO_43, IOMA, LUZ, MONOTRIBUTO, MONOTRIBUTO_ANGELES, MUNICIPAL_43, REPARACIONES, SALUD, CABLEVISION, CELULAR_A, CELULAR_F, EXTRAORDINARY ->
                ESSENTIAL;
            case INVESTMENTS, TELEFONO_43, ANGELES_OTHER_DISC, COMIDA_DISC, LIMPIEZA, SELLOS, CONTADORA, OTHER_USD, OTHER, VIAJES ->
                DISCRETIONARY;
            case ATLANTICO, SANTANDER, BOX, COLON_CAMUZZI, COLON_EDEA, COLON_EXPENSAS, COLON_FLOW, COLON_MUNICIPAL, COLON_OSSE, DEPARTAMENTOS, ITAU_UY, NETFLIX, SEGURO, SUSCRIPCIONES_ARS, SUSCRIPCIONES_USD, VIAJES_USD, XBOX ->
                LUXURY;
        };
    }
}
