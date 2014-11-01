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

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.fede.calculator.money.NoSeriesDataFoundException;
import org.fede.calculator.service.InvestmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author fede
 */
@Controller
@RequestMapping("/secure/investment")
public class InvestmentController {

    private static final Logger LOG = Logger.getLogger(InvestmentController.class.getName());

    @Autowired
    private InvestmentService investmentService;

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String errorHandler(Exception ex) {
        LOG.log(Level.SEVERE, "errorHandler", ex);

        return "redirect:/secure/";
    }

    @RequestMapping(value = "/dollar", method = RequestMethod.GET)
    public ModelAndView dollarInvestment() throws NoSeriesDataFoundException {
        ModelAndView mav = new ModelAndView("dollarInvestment");
        mav.addObject("report", this.investmentService.dollar());
        mav.addObject("moment", new Date());
        return mav;
    }

    @RequestMapping(value = "/savings", method = RequestMethod.GET)
    public ModelAndView savings() throws NoSeriesDataFoundException {
        return new ModelAndView("savingsReport", "report", this.investmentService.savings());
    }
}
