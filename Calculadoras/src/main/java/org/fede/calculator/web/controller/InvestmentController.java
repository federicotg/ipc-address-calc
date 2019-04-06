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

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.util.stream.Collectors.toMap;
import java.util.stream.Stream;
import javax.annotation.Resource;
import org.fede.calculator.service.InvestmentService;
import org.fede.calculator.service.MoneyService;
import org.fede.calculator.web.dto.CurrencyLimitsDTO;
import org.fede.calculator.web.dto.DetailedInvestmentReportDTO;
import org.fede.calculator.web.dto.InvestmentDTO;
import org.fede.calculator.web.dto.InvestmentReportDTO;
import org.fede.util.Pair;
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

    private static final String URI_TEMPLATE = "./{2}?sort={0}&filter={1}";

    private static final Comparator<Map.Entry<String, InvestmentDTO>> SUBTOTAL_COMPARATOR = Comparator.comparing(entry -> entry.getValue().getRelativePct());

    private static final Map<String, Comparator<InvestmentReportDTO>> COMPARATORS = Collections.unmodifiableMap(
            Map.of(
                "currency", Comparator.comparing(InvestmentReportDTO::getInvestmentCurrency),
                    "from", Comparator.comparing(InvestmentReportDTO::getFrom),
                    "to", Comparator.comparing(InvestmentReportDTO::getTo, Comparator.nullsFirst(Comparator.naturalOrder())),
                    "investment", Comparator.comparing(InvestmentReportDTO::getInitialAmount).reversed(),
                    "return", Comparator.comparing(InvestmentReportDTO::getFinalAmount).reversed(),
                    "pctDif", Comparator.comparing(InvestmentReportDTO::getPct).reversed(),
                    "realPctDif", Comparator.comparing(InvestmentReportDTO::getDiffPct).reversed()
            ));

    private static final Logger LOG = Logger.getLogger(InvestmentController.class.getName());

    private static Map<String, String> filteringUris(DetailedInvestmentReportDTO arsReport, String sort, String path) {
        return Stream.concat(arsReport.getSubtotals().keySet().stream(), Stream.of("all"))
                .map(key -> Pair.of(key, MessageFormat.format(URI_TEMPLATE, sort, key, path)))
                .collect(toMap(Pair::getFirst, Pair::getSecond));
    }

    private static Map<String, String> sortingUris(String filter, String path) {
        return COMPARATORS.entrySet()
                .stream()
                .map(entry -> Pair.of(entry.getKey(), MessageFormat.format(URI_TEMPLATE, entry.getKey(), filter, path)))
                .collect(toMap(Pair::getFirst, Pair::getSecond));
    }

    @Autowired
    @Lazy
    private InvestmentService investmentService;

    @Resource(name = "usdMoneyService")
    @Lazy
    private MoneyService usdService;

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String errorHandler(Exception ex) {
        LOG.log(Level.SEVERE, "errorHandler", ex);

        return "redirect:/secure/";
    }

    @RequestMapping(value = "/past", method = RequestMethod.GET)
    public ModelAndView pastInvestment(@RequestParam(name = "sort", required = false, defaultValue = "from") String sort,
            @RequestParam(name = "filter", required = false, defaultValue = "all") String filter) {
        final DetailedInvestmentReportDTO usdReport = this.investmentService.pastInvestmentsReport("USD")
                .filtered(filter)
                .sorted(COMPARATORS.get(sort), SUBTOTAL_COMPARATOR);
        return new ModelAndView("dollarInvestment")
                //.addObject("reportARS", arsReport)
                .addObject("reportUSD", usdReport)
                .addObject("sortingUris", sortingUris(filter, "past"))
                .addObject("filteringUris", filteringUris(usdReport, sort, "past"));
    }

    @RequestMapping(value = "/current", method = RequestMethod.GET)
    public ModelAndView currentInvestment(
            @RequestParam(name = "sort", required = false, defaultValue = "from") String sort,
            @RequestParam(name = "filter", required = false, defaultValue = "all") String filter) {
        final DetailedInvestmentReportDTO usdReport = this.investmentService.currentInvestmentsReport("USD")
                .filtered(filter)
                .sorted(COMPARATORS.get(sort), SUBTOTAL_COMPARATOR);
        return new ModelAndView("dollarInvestment")
                .addObject("reportUSD", usdReport)
                .addObject("sortingUris", sortingUris(filter, "current"))
                .addObject("filteringUris", filteringUris(usdReport, sort, "current"));
    }

    @RequestMapping(value = "/savings", method = RequestMethod.GET)
    public ModelAndView savings() {
        final CurrencyLimitsDTO limits = this.usdService.getLimits();

        return new ModelAndView("savingsReport")
                .addObject("report", this.investmentService.savings(limits.getReferenceYear(), limits.getReferenceMonth()))
                .addObject("limits", limits);
    }
}
