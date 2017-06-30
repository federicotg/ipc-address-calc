/*
 * Copyright (C) 2015 fede
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

import java.util.List;
import org.fede.calculator.web.dto.CanvasJSChartDTO;
import org.fede.calculator.web.dto.ExpenseChartSeriesDTO;

/**
 *
 * @author fede
 */
public interface MultiSeriesChartService {

    CanvasJSChartDTO renderAbsoluteChart(String title, int months, List<String> series, int year, int month, String currencyCode);

    CanvasJSChartDTO renderIncomeRelativeChart(String title, int months, List<String> series, String currencyCode);

    List<ExpenseChartSeriesDTO> getSeries();

    List<ExpenseChartSeriesDTO> getSeriesWithoutTotal();
}
