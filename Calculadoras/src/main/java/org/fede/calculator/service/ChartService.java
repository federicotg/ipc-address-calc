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
package org.fede.calculator.service;

import org.fede.calculator.money.NoSeriesDataFoundException;
import org.fede.calculator.web.dto.CanvasJSChartDTO;

/**
 *
 * @author fede
 */
public interface ChartService {

    CanvasJSChartDTO historicDollarValue(int year, int month) throws NoSeriesDataFoundException;

    CanvasJSChartDTO unlp(int months, boolean pn, boolean pr, boolean dn, boolean dr) throws NoSeriesDataFoundException;

    CanvasJSChartDTO lifia(int months, boolean pn, boolean pr, boolean dn, boolean dr) throws NoSeriesDataFoundException;
    
    CanvasJSChartDTO lifiaAndUnlp(int months, boolean pn, boolean pr, boolean dn, boolean dr) throws NoSeriesDataFoundException;
    
    CanvasJSChartDTO savings(boolean pn, boolean pr, boolean dn, boolean dr) throws NoSeriesDataFoundException;

}
