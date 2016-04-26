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

import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import org.fede.calculator.money.NoSeriesDataFoundException;
import org.fede.calculator.service.InvestmentService;
import org.fede.calculator.service.MoneyService;
import org.fede.calculator.web.dto.CurrencyLimitsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
@Lazy
@Controller
@RequestMapping("/secure/investment")
public class InvestmentController {

    private static final Logger LOG = Logger.getLogger(InvestmentController.class.getName());

    @Autowired
    @Lazy
    private InvestmentService investmentService;

    @Resource(name = "usdMoneyService")
    @Lazy
    private MoneyService usdService;

    @Resource(name = "argMoneyService")
    @Lazy
    private MoneyService argService;

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String errorHandler(Exception ex) {
        LOG.log(Level.SEVERE, "errorHandler", ex);

        return "redirect:/secure/";
    }

    @RequestMapping(value = "/dollar", method = RequestMethod.GET)
    public ModelAndView dollarInvestment(
            @RequestParam(name = "current", defaultValue = "false", required = false) boolean current,
            @RequestParam(name = "type", defaultValue = "all", required = false) String type) throws NoSeriesDataFoundException {
        return new ModelAndView("dollarInvestment")
                .addObject("report", this.investmentService.investment("USD", current))
                .addObject("total", current ? this.investmentService.currentInvestment("USD"): null);
    }

    @RequestMapping(value = "/peso", method = RequestMethod.GET)
    public ModelAndView pesoInvestment(
            @RequestParam(name = "current", defaultValue = "false", required = false) boolean current,
            @RequestParam(name = "type", defaultValue = "all", required = false) String type) throws NoSeriesDataFoundException {
        return new ModelAndView("dollarInvestment")
                .addObject("report", this.investmentService.investment("ARS", current))
                .addObject("total", current ? this.investmentService.currentInvestment("ARS") : null);
    }

    @RequestMapping(value = "/savings", method = RequestMethod.GET)
    public ModelAndView savings() throws NoSeriesDataFoundException {

        CurrencyLimitsDTO usdLimits = this.usdService.getLimits();
        CurrencyLimitsDTO argLimits = this.argService.getLimits();

        final Comparator<CurrencyLimitsDTO> comparator = new Comparator<CurrencyLimitsDTO>() {
            @Override
            public int compare(CurrencyLimitsDTO o1, CurrencyLimitsDTO o2) {
                if (o1.getReferenceYear() != o2.getReferenceYear()) {
                    return Integer.compare(o1.getReferenceYear(), o2.getReferenceYear());
                }
                return Integer.compare(o1.getReferenceMonth(), o2.getReferenceMonth());
            }
        };

        CurrencyLimitsDTO limits = null;
        if (comparator.compare(argLimits, usdLimits) <= 0) {
            limits = argLimits;
        } else {
            limits = usdLimits;
        }

        return new ModelAndView("savingsReport")
                .addObject("report", this.investmentService.savings(limits.getReferenceYear(), limits.getReferenceMonth()))
                .addObject("limits", limits);
    }
}
