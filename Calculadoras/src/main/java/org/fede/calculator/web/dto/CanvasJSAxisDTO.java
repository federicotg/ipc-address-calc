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

/**
 *
 * @author fede
 */
public class CanvasJSAxisDTO {

    private String title;
    private String valueFormatString;
    private boolean includeZero = true;
    private int labelFontSize = 12;
    private int titleFontSize = 20;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getValueFormatString() {
        return valueFormatString;
    }

    public void setValueFormatString(String valueFormatString) {
        this.valueFormatString = valueFormatString;
    }

    public boolean isIncludeZero() {
        return includeZero;
    }

    public void setIncludeZero(boolean includeZero) {
        this.includeZero = includeZero;
    }

    public int getLabelFontSize() {
        return labelFontSize;
    }

    public void setLabelFontSize(int labelFontSize) {
        this.labelFontSize = labelFontSize;
    }

    public int getTitleFontSize() {
        return titleFontSize;
    }

    public void setTitleFontSize(int titleFontSize) {
        this.titleFontSize = titleFontSize;
    }

}
