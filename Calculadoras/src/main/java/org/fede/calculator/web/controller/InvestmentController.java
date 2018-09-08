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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import org.fede.calculator.service.InvestmentService;
import org.fede.calculator.service.MoneyService;
import org.fede.calculator.web.dto.CurrencyLimitsDTO;
import org.fede.calculator.web.dto.DetailedInvestmentReportDTO;
import org.fede.calculator.web.dto.InvestmentDTO;
import org.fede.calculator.web.dto.InvestmentReportDTO;
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

    private static final Comparator<Map.Entry<String, InvestmentDTO>> SUBTOTAL_COMPARATOR = Comparator.comparing(entry -> entry.getValue().getRelativePct());

    private static final Map<String, Comparator<InvestmentReportDTO>> COMPARATORS = Collections.unmodifiableMap(
            Map.of(
                    "from", Comparator.comparing(InvestmentReportDTO::getFrom),
                    "investment", Comparator.comparing(InvestmentReportDTO::getInitialAmount).reversed(),
                    "return", Comparator.comparing(InvestmentReportDTO::getFinalAmount).reversed(),
                    //"absDif", Comparator.comparing(InvestmentReportDTO::getDifferenceAmount).reversed(),
                    "pctDif", Comparator.comparing(InvestmentReportDTO::getPct).reversed(),
                    //"realAbsDif", Comparator.comparing(InvestmentReportDTO::getDifferencePct).reversed(),
                    "realPctDif", Comparator.comparing(InvestmentReportDTO::getDiffPct).reversed()
            ));

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

    private static <T> T min(T left, T right, Comparator<T> comparator) {
        return comparator.compare(left, right) < 0 ? left : right;
    }

    @RequestMapping(value = "/past", method = RequestMethod.GET)
    public ModelAndView pastInvestment(@RequestParam(name = "sort", required = false, defaultValue = "from") String sort) {
        return new ModelAndView("dollarInvestment")
                .addObject("reportARS", this.sorted(this.investmentService.pastInvestmentsReport("ARS"), sort))
                .addObject("reportUSD", this.sorted(this.investmentService.pastInvestmentsReport("USD"), sort));
    }

    private DetailedInvestmentReportDTO sorted(DetailedInvestmentReportDTO report, String sort) {
        report.sorted(COMPARATORS.get(sort), SUBTOTAL_COMPARATOR);
        return report;
    }

    @RequestMapping(value = "/current", method = RequestMethod.GET)
    public ModelAndView currentInvestment(@RequestParam(name = "sort", required = false, defaultValue = "from") String sort) {

        return new ModelAndView("dollarInvestment")
                .addObject("reportARS", this.sorted(this.investmentService.currentInvestmentsReport("ARS"), sort))
                .addObject("reportUSD", this.sorted(this.investmentService.currentInvestmentsReport("USD"), sort));
    }

    @RequestMapping(value = "/savings", method = RequestMethod.GET)
    public ModelAndView savings() {

        final CurrencyLimitsDTO limits = min(
                this.argService.getLimits(),
                this.usdService.getLimits(),
                Comparator.comparing(CurrencyLimitsDTO::getReferenceYear)
                        .thenComparing(Comparator.comparing(CurrencyLimitsDTO::getReferenceMonth)));

        return new ModelAndView("savingsReport")
                .addObject("report", this.investmentService.savings(limits.getReferenceYear(), limits.getReferenceMonth()))
                .addObject("limits", limits);
    }
}
