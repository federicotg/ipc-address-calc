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
public class CanvasJSChartDTO {

    private boolean successful = true;
    private CanvasJSTitleDTO title;
    private List<CanvasJSDatumDTO> data;
    private CanvasJSAxisDTO axisX;
    private CanvasJSAxisDTO axisY;

    public CanvasJSTitleDTO getTitle() {
        return title;
    }

    public void setTitle(CanvasJSTitleDTO title) {
        this.title = title;
    }

    public List<CanvasJSDatumDTO> getData() {
        return data;
    }

    public void setData(List<CanvasJSDatumDTO> data) {
        this.data = data;
    }

    public CanvasJSAxisDTO getAxisX() {
        return axisX;
    }

    public void setAxisX(CanvasJSAxisDTO axisX) {
        this.axisX = axisX;
    }

    public CanvasJSAxisDTO getAxisY() {
        return axisY;
    }

    public void setAxisY(CanvasJSAxisDTO axisY) {
        this.axisY = axisY;
    }

    public void setXAxisTitle(String title) {
        CanvasJSAxisDTO axis = new CanvasJSAxisDTO();
        axis.setTitle(title);
        this.axisX = axis;
    }

    public void setYAxisTitle(String title) {
        CanvasJSAxisDTO axis = new CanvasJSAxisDTO();
        axis.setTitle(title);
        this.axisY = axis;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

}
