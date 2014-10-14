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

import java.util.List;

/**
 *
 * @author fede
 */
public class CanvasJSDatumDTO {

    private String type;
    private String color;
    private boolean showInLegend = false;
    private String name;
    private String legendText;
    private String markerType = "none";
    private List<CanvasJSDatapointDTO> dataPoints;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public List<?> getDataPoints() {
        return dataPoints;
    }

    public void setDataPoints(List<CanvasJSDatapointDTO> dataPoints) {
        this.dataPoints = dataPoints;
    }

    public boolean isShowInLegend() {
        return showInLegend;
    }

    public void setShowInLegend(boolean showInLegend) {
        this.showInLegend = showInLegend;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLegendText() {
        return legendText;
    }

    public void setLegendText(String legendText) {
        this.legendText = legendText;
    }

    public String getMarkerType() {
        return markerType;
    }

    public void setMarkerType(String markerType) {
        this.markerType = markerType;
    }

}
