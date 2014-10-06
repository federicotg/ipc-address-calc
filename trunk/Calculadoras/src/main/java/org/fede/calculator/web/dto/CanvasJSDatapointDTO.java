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
package org.fede.calculator.web.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 *
 * @author fede
 */
public class CanvasJSDatapointDTO {

    public String x;
    //@JsonFormat(locale = "en-US", shape = JsonFormat.Shape.NUMBER_FLOAT, pattern = "#.####")
    public BigDecimal y;

    public CanvasJSDatapointDTO(String x, BigDecimal y) {
        this.x = x;
        this.y = y.setScale(4, RoundingMode.HALF_UP);
    }

   
    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public BigDecimal getY() {
        return y;
    }

    public void setY(BigDecimal y) {
        this.y = y.setScale(4, RoundingMode.HALF_UP);
    }

}
