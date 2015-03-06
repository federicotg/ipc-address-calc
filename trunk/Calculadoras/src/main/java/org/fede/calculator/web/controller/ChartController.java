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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.validation.Valid;
import org.fede.calculator.money.NoSeriesDataFoundException;
import org.fede.calculator.service.ChartService;
import org.fede.calculator.web.dto.CanvasJSChartDTO;
import org.fede.calculator.web.dto.CombinedChartDTO;
import org.fede.calculator.web.dto.ExpenseChartDTO;
import org.fede.calculator.web.dto.ExpenseChartSeriesDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author fede
 */
@Lazy
@Controller
@RequestMapping("/secure/")
public class ChartController {

    private static final Logger LOG = Logger.getLogger(ChartController.class.getName());

    @Autowired @Lazy
    private ChartService chartService;

    @Resource(name = "monthlyPeriod")
    private Map<String, Integer> monthlyPeriods;

    @Resource(name = "expenseSeries")
    private List<ExpenseChartSeriesDTO> expenseSeries;
    
    @Resource(name = "consortiumExpenseSeries")
    private List<ExpenseChartSeriesDTO> consortiumExpenseSeries;

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public CanvasJSChartDTO errorHandler(Exception ex) {
        LOG.log(Level.SEVERE, "errorHandler", ex);
        CanvasJSChartDTO dto = new CanvasJSChartDTO();
        dto.setSuccessful(false);
        return dto;
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String chartList() {
        return "charts";
    }

    @RequestMapping(value = "unlp", method = RequestMethod.GET)
    public ModelAndView unlp() {
        ModelAndView mav = new ModelAndView("combinedChart");
        mav.addObject("uri", "unlpCombined");
        mav.addObject("title", "UNLP");
        mav.addObject("monthlyPeriods", this.monthlyPeriods);
        mav.addObject("dto", new CombinedChartDTO());
        return mav;
    }

    @RequestMapping(value = "lifia", method = RequestMethod.GET)
    public ModelAndView lifia() {
        ModelAndView mav = new ModelAndView("combinedChart");
        mav.addObject("uri", "lifiaCombined");
        mav.addObject("title", "LIFIA");
        mav.addObject("monthlyPeriods", this.monthlyPeriods);
        mav.addObject("dto", new CombinedChartDTO());
        return mav;
    }

    @RequestMapping(value = "interest", method = RequestMethod.GET)
    public ModelAndView interest() {
        ModelAndView mav = new ModelAndView("combinedChart");
        mav.addObject("uri", "interestCombined");
        mav.addObject("title", "Plazo Fijo");
        mav.addObject("monthlyPeriods", this.monthlyPeriods);
        mav.addObject("dto", new CombinedChartDTO());
        return mav;
    }

    @RequestMapping(value = "lifiaAndUnlp", method = RequestMethod.GET)
    public ModelAndView lifiaAndUnlp() {
        ModelAndView mav = new ModelAndView("combinedChart");
        mav.addObject("uri", "lifiaAndUnlpCombined");
        mav.addObject("title", "LIFIA + UNLP");
        mav.addObject("monthlyPeriods", this.monthlyPeriods);
        mav.addObject("dto", new CombinedChartDTO());
        return mav;
    }

    @RequestMapping(value = "lifiaUnlpAndInterest", method = RequestMethod.GET)
    public ModelAndView lifiaUnlpAndInterest() {
        ModelAndView mav = new ModelAndView("combinedChart");
        mav.addObject("uri", "lifiaUnlpAndInterestCombined");
        mav.addObject("title", "LIFIA + UNLP + Plazo Fijo");
        mav.addObject("monthlyPeriods", this.monthlyPeriods);
        mav.addObject("dto", new CombinedChartDTO());
        return mav;
    }

    @RequestMapping(value = "savings", method = RequestMethod.GET)
    public ModelAndView savings() {
        ModelAndView mav = new ModelAndView("combinedChart");
        mav.addObject("uri", "savingsCombined");
        mav.addObject("title", "Ahorros");
        mav.addObject("dto", new CombinedChartDTO());
        return mav;
    }

    @RequestMapping(value = "goldSavings", method = RequestMethod.GET)
    public ModelAndView goldSavings() {
        ModelAndView mav = new ModelAndView("simpleChart");
        mav.addObject("uri", "goldSavingsChart");
        mav.addObject("title", "Oro");
        return mav;
    }

    @RequestMapping(value = "absa", method = RequestMethod.GET)
    public ModelAndView absa() {
        ModelAndView mav = new ModelAndView("simpleChart");
        mav.addObject("uri", "absaChart");
        mav.addObject("title", "ABSA");
        return mav;
    }
    
    @RequestMapping(value = "savedSalaries", method = RequestMethod.GET)
    public ModelAndView savedSalaries() {
        ModelAndView mav = new ModelAndView("simpleChart");
        mav.addObject("uri", "savedSalariesChart");
        mav.addObject("title", "Salarios Promedio Anual Ahorrados");
        return mav;
    }

    @RequestMapping(value = "expenses", method = RequestMethod.GET)
    public ModelAndView expenses() {
       return this.buildExpenseModelAndView("Gastos", this.expenseSeries);
    }

    
    private ModelAndView buildExpenseModelAndView(String title, List<ExpenseChartSeriesDTO> dtoSeries){
        ExpenseChartDTO chartDto = new ExpenseChartDTO();
        List<String> series = new ArrayList<>(dtoSeries.size());
        for (ExpenseChartSeriesDTO e : dtoSeries) {
            series.add(e.getName());
        }
        chartDto.setSeries(series);
        return new ModelAndView("expenseChart")
                .addObject("uri", "expensesChart")
                .addObject("title", title)
                .addObject("monthlyPeriods", this.monthlyPeriods)
                .addObject("series", dtoSeries)
                .addObject("dto", chartDto);
        
    }
    
    @RequestMapping(value = "consortiumExpenses", method = RequestMethod.GET)
    public ModelAndView consortiumExpenses() {
        return this.buildExpenseModelAndView("Gastos Consorcio", this.consortiumExpenseSeries);
    }
    
    @RequestMapping(value = "expensesPercent", method = RequestMethod.GET)
    public ModelAndView expensesPercent() {
        ModelAndView mav = new ModelAndView("expenseChart");
        mav.addObject("uri", "expensesPercentChart");
        mav.addObject("title", "Gastos / Ingresos");
        mav.addObject("monthlyPeriods", this.monthlyPeriods);
        mav.addObject("series", this.expenseSeries);

        ExpenseChartDTO chartDto = new ExpenseChartDTO();
        List<String> series = new ArrayList<>(this.expenseSeries.size());
        for (ExpenseChartSeriesDTO e : this.expenseSeries) {
            series.add(e.getName());
        }
        chartDto.setSeries(series);
        mav.addObject("dto", chartDto);
        return mav;
    }

    @ResponseBody
    @RequestMapping(value = "unlpCombined", method = RequestMethod.GET)
    public CanvasJSChartDTO unlpCombined(@ModelAttribute("dto") @Valid CombinedChartDTO dto, BindingResult errors)
            throws NoSeriesDataFoundException {
        if (errors.hasErrors()) {
            CanvasJSChartDTO notOk = new CanvasJSChartDTO();
            notOk.setSuccessful(false);
            return notOk;
        }
        return this.chartService.unlp(dto.getMonths(), dto.isPn(), dto.isPr(), dto.isDn(), dto.isDr());
    }

    @ResponseBody
    @RequestMapping(value = "lifiaCombined", method = RequestMethod.GET)
    public CanvasJSChartDTO lifiaCombined(@ModelAttribute("dto") @Valid CombinedChartDTO dto, BindingResult errors)
            throws NoSeriesDataFoundException {
        if (errors.hasErrors()) {
            CanvasJSChartDTO notOk = new CanvasJSChartDTO();
            notOk.setSuccessful(false);
            return notOk;
        }
        return this.chartService.lifia(dto.getMonths(), dto.isPn(), dto.isPr(), dto.isDn(), dto.isDr());
    }

    @ResponseBody
    @RequestMapping(value = "interestCombined", method = RequestMethod.GET)
    public CanvasJSChartDTO interestCombined(@ModelAttribute("dto") @Valid CombinedChartDTO dto, BindingResult errors)
            throws NoSeriesDataFoundException {
        if (errors.hasErrors()) {
            CanvasJSChartDTO notOk = new CanvasJSChartDTO();
            notOk.setSuccessful(false);
            return notOk;
        }
        return this.chartService.interest(dto.getMonths(), dto.isPn(), dto.isPr(), dto.isDn(), dto.isDr());
    }

    @ResponseBody
    @RequestMapping(value = "lifiaAndUnlpCombined", method = RequestMethod.GET)
    public CanvasJSChartDTO lifiaAndUnlpCombined(@ModelAttribute("dto") @Valid CombinedChartDTO dto, BindingResult errors)
            throws NoSeriesDataFoundException {
        if (errors.hasErrors()) {
            CanvasJSChartDTO notOk = new CanvasJSChartDTO();
            notOk.setSuccessful(false);
            return notOk;
        }
        return this.chartService.lifiaAndUnlp(dto.getMonths(), dto.isPn(), dto.isPr(), dto.isDn(), dto.isDr());
    }

    @ResponseBody
    @RequestMapping(value = "lifiaUnlpAndInterestCombined", method = RequestMethod.GET)
    public CanvasJSChartDTO lifiaUnlpAndInterestCombined(@ModelAttribute("dto") @Valid CombinedChartDTO dto, BindingResult errors)
            throws NoSeriesDataFoundException {
        if (errors.hasErrors()) {
            CanvasJSChartDTO notOk = new CanvasJSChartDTO();
            notOk.setSuccessful(false);
            return notOk;
        }
        return this.chartService.lifiaUnlpAndInterest(dto.getMonths(), dto.isPn(), dto.isPr(), dto.isDn(), dto.isDr());
    }

    @ResponseBody
    @RequestMapping(value = "savingsCombined", method = RequestMethod.GET)
    public CanvasJSChartDTO savingsCombined(@ModelAttribute("dto") @Valid CombinedChartDTO dto, BindingResult errors)
            throws NoSeriesDataFoundException {
        if (errors.hasErrors()) {
            CanvasJSChartDTO notOk = new CanvasJSChartDTO();
            notOk.setSuccessful(false);
            return notOk;
        }
        return this.chartService.savings(dto.isPn(), dto.isPr(), dto.isDn(), dto.isDr());
    }

    @ResponseBody
    @RequestMapping(value = "expensesChart", method = RequestMethod.GET)
    public CanvasJSChartDTO expensesChart(@ModelAttribute("dto") @Valid ExpenseChartDTO dto, BindingResult errors)
            throws NoSeriesDataFoundException {
        if (errors.hasErrors()) {
            CanvasJSChartDTO notOk = new CanvasJSChartDTO();
            notOk.setSuccessful(false);
            return notOk;
        }
        return this.chartService.expenses(dto.getMonths(), dto.getSeries());
    }

    @ResponseBody
    @RequestMapping(value = "expensesPercentChart", method = RequestMethod.GET)
    public CanvasJSChartDTO expensesPercentChart(@ModelAttribute("dto") @Valid ExpenseChartDTO dto, BindingResult errors)
            throws NoSeriesDataFoundException {
        if (errors.hasErrors()) {
            CanvasJSChartDTO notOk = new CanvasJSChartDTO();
            notOk.setSuccessful(false);
            return notOk;
        }
        return this.chartService.expensesPercentage(dto.getMonths(), dto.getSeries());
    }

    @ResponseBody
    @RequestMapping(value = "goldSavingsChart", method = RequestMethod.GET)
    public CanvasJSChartDTO goldSavingsChart()
            throws NoSeriesDataFoundException {
        return this.chartService.goldIncomeAndSavings();
    }

    @ResponseBody
    @RequestMapping(value = "savedSalariesChart", method = RequestMethod.GET)
    public CanvasJSChartDTO savedSalariesChart()
            throws NoSeriesDataFoundException {
        return this.chartService.savedSalaries();
    }
    

}
