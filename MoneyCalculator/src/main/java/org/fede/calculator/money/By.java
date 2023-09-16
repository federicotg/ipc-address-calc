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

        var action = switch (params.get("by")) {
            case "quarter":
                yield quarter;
            case "half":
                yield half;
            case "year":
                yield year;
            default:
                yield otherwise;
        };
        action.run();

    }

    public void by(Map<String, String> params, Runnable quarter, Runnable half, Runnable year, Runnable month, Runnable otherwise) {

        this.by(params, quarter, half, year, "month".equals(params.get("by")) ? month : otherwise);
    }

}
