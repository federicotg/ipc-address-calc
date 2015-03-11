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
import org.fede.calculator.service.MultiSeriesChartService;
import org.fede.calculator.service.MoneyService;
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

    @Autowired
    @Lazy
    private ChartService chartService;

    @Resource(name = "expensesService")
    @Lazy
    private MultiSeriesChartService expenseService;

    @Resource(name = "consortiumExpensesService")
    @Lazy
    private MultiSeriesChartService consortiumExpenseService;
    
    @Resource(name = "incomesService")
    @Lazy
    private MultiSeriesChartService incomesService;
    

    @Resource(name = "argMoneyService")
    @Lazy
    private MoneyService arsMoneyService;

    @Resource(name = "monthlyPeriod")
    @Lazy
    private Map<String, Integer> monthlyPeriods;

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

    private CombinedChartDTO createCombinedChartDTO() {
        return new CombinedChartDTO(arsMoneyService.getLimits());
    }

    @RequestMapping(value = "unlp", method = RequestMethod.GET)
    public ModelAndView unlp() {
        return new ModelAndView("combinedChart")
                .addObject("uri", "unlpCombined")
                .addObject("title", "UNLP")
                .addObject("monthlyPeriods", this.monthlyPeriods)
                .addObject("dto", this.createCombinedChartDTO());
    }

    @RequestMapping(value = "lifia", method = RequestMethod.GET)
    public ModelAndView lifia() {
        return new ModelAndView("combinedChart")
                .addObject("uri", "lifiaCombined")
                .addObject("title", "LIFIA")
                .addObject("monthlyPeriods", this.monthlyPeriods)
                .addObject("dto", this.createCombinedChartDTO());
    }

    @RequestMapping(value = "interest", method = RequestMethod.GET)
    public ModelAndView interest() {
        return new ModelAndView("combinedChart")
                .addObject("uri", "interestCombined")
                .addObject("title", "Plazo Fijo")
                .addObject("monthlyPeriods", this.monthlyPeriods)
                .addObject("dto", this.createCombinedChartDTO());
    }

    @RequestMapping(value = "lifiaAndUnlp", method = RequestMethod.GET)
    public ModelAndView lifiaAndUnlp() {
        return new ModelAndView("combinedChart")
                .addObject("uri", "lifiaAndUnlpCombined")
                .addObject("title", "LIFIA + UNLP")
                .addObject("monthlyPeriods", this.monthlyPeriods)
                .addObject("dto", this.createCombinedChartDTO());
    }

    @RequestMapping(value = "lifiaUnlpAndInterest", method = RequestMethod.GET)
    public ModelAndView lifiaUnlpAndInterest() {
        return new ModelAndView("combinedChart")
                .addObject("uri", "lifiaUnlpAndInterestCombined")
                .addObject("title", "LIFIA + UNLP + Plazo Fijo")
                .addObject("monthlyPeriods", this.monthlyPeriods)
                .addObject("dto", this.createCombinedChartDTO());
    }

    @RequestMapping(value = "savings", method = RequestMethod.GET)
    public ModelAndView savings() {
        return new ModelAndView("combinedChart")
                .addObject("uri", "savingsCombined")
                .addObject("title", "Ahorros")
                .addObject("dto", this.createCombinedChartDTO());
    }

    @RequestMapping(value = "goldSavings", method = RequestMethod.GET)
    public ModelAndView goldSavings() {
        return new ModelAndView("simpleChart")
                .addObject("uri", "goldSavingsChart")
                .addObject("title", "Oro");
    }

    @RequestMapping(value = "absa", method = RequestMethod.GET)
    public ModelAndView absa() {
        return new ModelAndView("simpleChart")
                .addObject("uri", "absaChart")
                .addObject("title", "ABSA");
    }

    @RequestMapping(value = "savedSalaries", method = RequestMethod.GET)
    public ModelAndView savedSalaries() {
        return new ModelAndView("simpleChart")
                .addObject("uri", "savedSalariesChart")
                .addObject("title", "Salarios Promedio Anual Ahorrados");
    }

    @RequestMapping(value = "expenses", method = RequestMethod.GET)
    public ModelAndView expenses() {
        return this.buildExpenseModelAndView("Gastos", this.expenseService.getSeries(), "expensesChart");
    }

    private ModelAndView buildExpenseModelAndView(String title, List<ExpenseChartSeriesDTO> dtoSeries, String uri) {
        ExpenseChartDTO chartDto = new ExpenseChartDTO(this.arsMoneyService.getLimits());
        List<String> series = new ArrayList<>(dtoSeries.size());
        for (ExpenseChartSeriesDTO e : dtoSeries) {
            series.add(e.getName());
        }
        chartDto.setSeries(series);
        return new ModelAndView("expenseChart")
                .addObject("uri", uri)
                .addObject("title", title)
                .addObject("monthlyPeriods", this.monthlyPeriods)
                .addObject("series", dtoSeries)
                .addObject("dto", chartDto);

    }

    @RequestMapping(value = "consortiumExpenses", method = RequestMethod.GET)
    public ModelAndView consortiumExpenses() {
        return this.buildExpenseModelAndView("Gastos Consorcio", this.consortiumExpenseService.getSeries(), "consortiumExpensesChart");
    }
    
    @RequestMapping(value = "incomes", method = RequestMethod.GET)
    public ModelAndView incomes() {
        return this.buildExpenseModelAndView("Ingresos", this.incomesService.getSeries(), "incomesChart");
    }

    @RequestMapping(value = "expensesPercent", method = RequestMethod.GET)
    public ModelAndView expensesPercent() {
        

        ExpenseChartDTO chartDto = new ExpenseChartDTO(this.arsMoneyService.getLimits());
        List<String> series = new ArrayList<>();
        for (ExpenseChartSeriesDTO e : this.expenseService.getSeries()) {
            series.add(e.getName());
        }
        chartDto.setSeries(series);
        return new ModelAndView("expenseChart")
                .addObject("uri", "expensesPercentChart")
                .addObject("title", "Gastos / Ingresos")
                .addObject("monthlyPeriods", this.monthlyPeriods)
                .addObject("series", this.expenseService.getSeries())
                .addObject("dto", chartDto);
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
        return this.chartService.unlp(dto.getMonths(), dto.isPn(), dto.isPr(), dto.isDn(), dto.isDr(), dto.getYear(), dto.getMonth());
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
        return this.chartService.lifia(dto.getMonths(), dto.isPn(), dto.isPr(), dto.isDn(), dto.isDr(), dto.getYear(), dto.getMonth());
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
        return this.chartService.interest(dto.getMonths(), dto.isPn(), dto.isPr(), dto.isDn(), dto.isDr(), dto.getYear(), dto.getMonth());
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
        return this.chartService.lifiaAndUnlp(dto.getMonths(), dto.isPn(), dto.isPr(), dto.isDn(), dto.isDr(), dto.getYear(), dto.getMonth());
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
        return this.chartService.lifiaUnlpAndInterest(dto.getMonths(), dto.isPn(), dto.isPr(), dto.isDn(), dto.isDr(), dto.getYear(), dto.getMonth());
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
        return this.chartService.savings(dto.isPn(), dto.isPr(), dto.isDn(), dto.isDr(), dto.getYear(), dto.getMonth());
    }

    
    @ResponseBody
    @RequestMapping(value = "incomesChart", method = RequestMethod.GET)
    public CanvasJSChartDTO incomesChart(@ModelAttribute("dto") @Valid ExpenseChartDTO dto, BindingResult errors)
            throws NoSeriesDataFoundException {
        if (errors.hasErrors()) {
            CanvasJSChartDTO notOk = new CanvasJSChartDTO();
            notOk.setSuccessful(false);
            return notOk;
        }
        return this.incomesService.renderAbsoluteChart("Ingresos", dto.getMonths(), dto.getSeries(), dto.getYear(), dto.getMonth());
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
        return this.expenseService.renderAbsoluteChart("Gastos", dto.getMonths(), dto.getSeries(), dto.getYear(), dto.getMonth());
    }

    @ResponseBody
    @RequestMapping(value = "consortiumExpensesChart", method = RequestMethod.GET)
    public CanvasJSChartDTO consortiumExpensesChart(@ModelAttribute("dto") @Valid ExpenseChartDTO dto, BindingResult errors)
            throws NoSeriesDataFoundException {
        if (errors.hasErrors()) {
            CanvasJSChartDTO notOk = new CanvasJSChartDTO();
            notOk.setSuccessful(false);
            return notOk;
        }
        return this.consortiumExpenseService.renderAbsoluteChart("Gastos del Consorcio", dto.getMonths(), dto.getSeries(), dto.getYear(), dto.getMonth());
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
        return this.expenseService.renderIncomeRelativeChart("Gastos / Ingresos", dto.getMonths(), dto.getSeries());
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
