/*
 * Copyright (C) 2023 fede
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

import java.util.List;

/**
 *
 * @author fede
 */
public class PPIBalance {

    private String currency;
    private List<PPIPosition> availability;

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public List<PPIPosition> getAvailability() {
        return availability;
    }

    public void setAvailability(List<PPIPosition> availability) {
        this.availability = availability;
    }

    

}
