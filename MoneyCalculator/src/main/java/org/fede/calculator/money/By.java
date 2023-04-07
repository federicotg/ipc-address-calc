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
package org.fede.calculator.money;

import java.util.Map;

/**
 *
 * @author federicogentile
 */
public class By {

    public void by(Map<String, String> params, Runnable quarter, Runnable half, Runnable year, Runnable otherwise) {
        final var by = params.get("by");
        if ("quarter".equals(by)) {
            quarter.run();
        } else if ("half".equals(by)) {
            half.run();
        } else if ("year".equals(by)) {
            year.run();
        } else {
            otherwise.run();
        }
    }

    public void by(Map<String, String> params, Runnable quarter, Runnable half, Runnable year, Runnable month, Runnable otherwise) {
        final var by = params.get("by");
        Runnable monthly = () -> {
            if ("month".equals(by)) {
                month.run();
            } else {
                otherwise.run();
            }
        };
        this.by(params, quarter, half, year, monthly);
    }
    
    
    
}
