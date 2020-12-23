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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.fede.calculator.service.ChartService;
import org.fede.calculator.web.dto.CanvasJSChartDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author fede
 */
@Lazy
@Controller
@RequestMapping("/chart")
public class PublicChartController {

    private static final Logger LOG = Logger.getLogger(PublicChartController.class.getName());

    @Autowired
    @Lazy
    private ChartService chartService;

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public CanvasJSChartDTO errorHandler(Exception ex) {
        LOG.log(Level.SEVERE, "errorHandler", ex);
        CanvasJSChartDTO dto = new CanvasJSChartDTO();
        dto.setSuccessful(false);
        return dto;
    }

    @RequestMapping(value = "/", method = GET)
    public String chartList() {
        return "publicCharts";
    }

    @RequestMapping(value = "realDollarsForTroyOunce", method = GET)
    public ModelAndView realGoldPrice() {
        return new ModelAndView("simpleChart")
                .addObject("uri", "realDollarsForTroyOunceChart")
                .addObject("title", "Oro en Dólares Reales");
    }


    @ResponseBody
    @RequestMapping(value = "realDollarsForTroyOunceChart", method = GET)
    public CanvasJSChartDTO realDollarsForTroyOunce() {
        return this.chartService.historicGold();
    }

}
