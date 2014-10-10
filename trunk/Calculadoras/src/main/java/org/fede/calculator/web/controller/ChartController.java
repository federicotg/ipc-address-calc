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
import javax.annotation.Resource;
import org.fede.calculator.money.NoSeriesDataFoundException;
import org.fede.calculator.service.ChartService;
import org.fede.calculator.web.dto.CanvasJSChartDTO;
import org.fede.calculator.web.dto.LaPlataAddressDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author fede
 */
@Controller
@RequestMapping("/secure/")
public class ChartController {

    private static final Logger LOG = Logger.getLogger(ChartController.class.getName());

    @Resource(name = "chartService")
    private ChartService chartService;

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public CanvasJSChartDTO errorHandler(Exception ex) {
        LOG.log(Level.SEVERE, "errorHandler", ex);
        CanvasJSChartDTO dto = new CanvasJSChartDTO();
        dto.setSuccessful(false);
        return dto;
    }

    @RequestMapping(value = "unlpCombined", method = RequestMethod.GET)
    public CanvasJSChartDTO unlpCombined(
            @RequestParam(value = "months", required = false, defaultValue = "6") int months,
            @RequestParam(value = "pn", required = false, defaultValue = "false") boolean pn,
            @RequestParam(value = "pr", required = false, defaultValue = "true") boolean pr,
            @RequestParam(value = "dn", required = false, defaultValue = "true") boolean dn,
            @RequestParam(value = "dr", required = false, defaultValue = "true") boolean dr)
            throws NoSeriesDataFoundException {
        return this.chartService.unlp(months, pn, pr, dn, dr);
    }

    @RequestMapping(value = "unlp", method = RequestMethod.GET)
    public ModelAndView unlp() {
        ModelAndView mav = new ModelAndView("combinedChart");
        mav.addObject("uri", "unlpCombined");
        mav.addObject("title", "UNLP");
        
        return mav;
    }
@RequestMapping(value = "lifia", method = RequestMethod.GET)
    public ModelAndView lifia() {
        ModelAndView mav = new ModelAndView("combinedChart");
        mav.addObject("uri", "lifiaCombined");
        mav.addObject("title", "LIFIA");
        
        return mav;
    }
    @RequestMapping(value = "lifiaCombined", method = RequestMethod.GET)
    public CanvasJSChartDTO lifiaCombined(@RequestParam(value = "months", required = false, defaultValue = "6") int months,
            @RequestParam(value = "pn", required = false, defaultValue = "false") boolean pn,
            @RequestParam(value = "pr", required = false, defaultValue = "true") boolean pr,
            @RequestParam(value = "dn", required = false, defaultValue = "true") boolean dn,
            @RequestParam(value = "dr", required = false, defaultValue = "true") boolean dr)
            throws NoSeriesDataFoundException {
        return this.chartService.lifia(months, pn, pr, dn, dr);
    }

}
