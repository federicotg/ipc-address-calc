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
package org.fede.calculator.web.controller;

import javax.annotation.Resource;
import org.fede.calculator.money.NoSeriesDataFoundException;
import org.fede.calculator.service.ChartService;
import org.fede.calculator.web.dto.CanvasJSChartDTO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author fede
 */
@Controller
public class ChartController {

    @Resource(name = "chartService")
    private ChartService chartService;
    
    @RequestMapping(value = "/historicDollarValue", method = RequestMethod.GET)
    public CanvasJSChartDTO historicDollarValue(@RequestParam("year") int year, @RequestParam(value = "month") int month) throws NoSeriesDataFoundException {
        return this.chartService.historicDollarValue(year, month);
    }
    
    @RequestMapping(value = "/charts", method = RequestMethod.GET)
    public String show() {
        return "chart";
    }
    
}
