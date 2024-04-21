/*
 * Copyright (C) 2023 federicogentile
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
package org.fede.calculator.ppi;

/**
 *
 * @author federicogentile
 */
public enum SettlementType {
    INMEDIATA("INMEDIATA"),
    A24("A-24HS"),
    A48("A-48HS"),
    A72("A-72HS");

    private final String value;

    private SettlementType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

}
