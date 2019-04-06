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

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.util.stream.Collectors.toList;
import javax.annotation.Resource;
import javax.validation.Valid;
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
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
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

    @Resource(name = "savingsService")
    @Lazy
    private MultiSeriesChartService savingsService;

    @Resource(name = "fciService")
    @Lazy
    private MultiSeriesChartService fciService;

    @Resource(name = "usdMoneyService")
    @Lazy
    private MoneyService referenceMoneyService;

    @Resource(name = "monthlyPeriod")
    @Lazy
    private Map<String, Integer> monthlyPeriods;

    @Resource(name = "incomesSeries")
    private List<ExpenseChartSeriesDTO> incomeSeries;

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public CanvasJSChartDTO errorHandler(Exception ex) {
        LOG.log(Level.SEVERE, "errorHandler", ex);
        CanvasJSChartDTO dto = new CanvasJSChartDTO();
        dto.setSuccessful(false);
        return dto;
    }

    @RequestMapping(value = "", method = GET)
    public String chartList() {
        return "charts";
    }

    private CombinedChartDTO createCombinedChartDTO() {
        return new CombinedChartDTO(referenceMoneyService.getLimits());
    }

    @RequestMapping(value = "unlp", method = GET)
    public ModelAndView unlp() {
        return new ModelAndView("combinedChart")
                .addObject("uri", "unlpCombined")
                .addObject("title", "UNLP")
                .addObject("monthlyPeriods", this.monthlyPeriods)
                .addObject("dto", this.createCombinedChartDTO());
    }

    @RequestMapping(value = "lifia", method = GET)
    public ModelAndView lifia() {
        return new ModelAndView("combinedChart")
                .addObject("uri", "lifiaCombined")
                .addObject("title", "LIFIA")
                .addObject("monthlyPeriods", this.monthlyPeriods)
                .addObject("dto", this.createCombinedChartDTO());
    }

    @RequestMapping(value = "interest", method = GET)
    public ModelAndView interest() {
        return new ModelAndView("combinedChart")
                .addObject("uri", "interestCombined")
                .addObject("title", "Plazo Fijo")
                .addObject("monthlyPeriods", this.monthlyPeriods)
                .addObject("dto", this.createCombinedChartDTO());
    }

    @RequestMapping(value = "lifiaAndUnlp", method = GET)
    public ModelAndView lifiaAndUnlp() {
        return new ModelAndView("combinedChart")
                .addObject("uri", "lifiaAndUnlpCombined")
                .addObject("title", "LIFIA + UNLP")
                .addObject("monthlyPeriods", this.monthlyPeriods)
                .addObject("dto", this.createCombinedChartDTO());
    }

    @RequestMapping(value = "lifiaUnlpAndInterest", method = GET)
    public ModelAndView lifiaUnlpAndInterest() {
        List<String> allSeriesNames = incomeSeries.stream()
                .filter(dto -> dto.getSeriesName() != null && dto.getSeriesName().length() > 0)
                .map(ExpenseChartSeriesDTO::getName)
                .collect(toList());

        final CombinedChartDTO dto = this.createCombinedChartDTO();
        dto.setSeries(allSeriesNames);

        return new ModelAndView("combinedChart")
                .addObject("uri", "lifiaUnlpAndInterestCombined")
                .addObject("title", "Ingresos Combinados")
                .addObject("monthlyPeriods", this.monthlyPeriods)
                .addObject("stackedSeries", allSeriesNames)
                .addObject("dto", dto);
    }

    @RequestMapping(value = "savings", method = GET)
    public ModelAndView savings() {
        return new ModelAndView("combinedChart")
                .addObject("uri", "savingsCombined")
                .addObject("title", "Ahorros")
                .addObject("dto", this.createCombinedChartDTO());
    }

    @RequestMapping(value = "goldSavings", method = GET)
    public ModelAndView goldSavings() {
        return new ModelAndView("simpleChart")
                .addObject("uri", "goldSavingsChart")
                .addObject("title", "Oro");
    }

    @RequestMapping(value = "savingsAndIncomeChange", method = GET)
    public ModelAndView savingsAndIncomeChange() {
        return new ModelAndView("simpleChart")
                .addObject("uri", "savingsAndIncomeChangeChart")
                .addObject("title", "Cambio Promedio Anual");
    }

    @RequestMapping(value = "absa", method = GET)
    public ModelAndView absa() {
        return new ModelAndView("simpleChart")
                .addObject("uri", "absaChart")
                .addObject("title", "ABSA");
    }

    @RequestMapping(value = "savedSalaries", method = GET)
    public ModelAndView savedSalaries() {
        return new ModelAndView("simpleChart")
                .addObject("uri", "savedSalariesChart")
                .addObject("title", "Salarios Promedio Anual Ahorrados");
    }

    @RequestMapping(value = "savingsDetailed", method = GET)
    public ModelAndView savingsDetail() {
        return this.buildExpenseModelAndView("Ahorros", this.savingsService.getSeries(), "savingsDetailChart", 1);
    }
    
    @RequestMapping(value = "expenses", method = GET)
    public ModelAndView expenses() {
        return this.buildExpenseModelAndView("Gastos", this.expenseService.getSeries(), "expensesChart");
    }

    @RequestMapping(value = "consultatio", method = GET)
    public ModelAndView consultatio() {
        return this.buildExpenseModelAndView("Consultatio Plus F.C.I.", this.fciService.getSeries(), "fciChart");
    }

    private ModelAndView buildExpenseModelAndView(String title, List<ExpenseChartSeriesDTO> dtoSeries, String uri) {
        return this.buildExpenseModelAndView(title, dtoSeries, uri, 12);
    }

    private ModelAndView buildExpenseModelAndView(String title, List<ExpenseChartSeriesDTO> dtoSeries, String uri, int months) {
        final ExpenseChartDTO chartDto = new ExpenseChartDTO(this.referenceMoneyService.getLimits());
        chartDto.setMonths(months);
        chartDto.setSeries(dtoSeries.stream().map(dto -> dto.getName()).collect(toList()));
        return new ModelAndView("expenseChart")
                .addObject("uri", uri)
                .addObject("title", title)
                .addObject("monthlyPeriods", this.monthlyPeriods)
                .addObject("series", dtoSeries)
                .addObject("dto", chartDto);
    }

    @RequestMapping(value = "consortiumExpenses", method = GET)
    public ModelAndView consortiumExpenses() {
        return this.buildExpenseModelAndView("Gastos Consorcio", this.consortiumExpenseService.getSeries(), "consortiumExpensesChart");
    }

    @RequestMapping(value = "incomes", method = GET)
    public ModelAndView incomes() {
        return this.buildExpenseModelAndView("Ingresos", this.incomesService.getSeries(), "incomesChart");
    }

    @RequestMapping(value = "expensesPercent", method = GET)
    public ModelAndView expensesPercent() {
        ExpenseChartDTO chartDto = new ExpenseChartDTO(this.referenceMoneyService.getLimits());
        chartDto.setSeries(this.expenseService.getSeries().stream().map(dto -> dto.getName()).collect(toList()));
        return new ModelAndView("expenseChart")
                .addObject("uri", "expensesPercentChart")
                .addObject("title", "Gastos / Ingresos")
                .addObject("monthlyPeriods", this.monthlyPeriods)
                .addObject("series", this.expenseService.getSeriesWithoutTotal())
                .addObject("dto", chartDto);
    }

    private CanvasJSChartDTO notOkResponse() {
        CanvasJSChartDTO notOk = new CanvasJSChartDTO();
        notOk.setSuccessful(false);
        return notOk;
    }

    @ResponseBody
    @RequestMapping(value = "lifiaUnlpAndInterestCombined", method = GET)
    public CanvasJSChartDTO lifiaUnlpAndInterestCombined(
            @ModelAttribute("dto") @Valid CombinedChartDTO dto,
            BindingResult errors) {
        if (errors.hasErrors()) {
            return this.notOkResponse();
        }
        return this.chartService.combinedIncomes(dto.getMonths(), dto.isPn(), dto.isPr(), dto.isDn(), dto.isDr(), dto.isEn(), dto.isEr(), dto.getYear(), dto.getMonth(), dto.getSeries());
    }

    @ResponseBody
    @RequestMapping(value = "savingsCombined", method = GET)
    public CanvasJSChartDTO savingsCombined(@ModelAttribute("dto") @Valid CombinedChartDTO dto, BindingResult errors) {
        if (errors.hasErrors()) {
            return this.notOkResponse();
        }
        return this.chartService.savings(dto.isPn(), dto.isPr(), dto.isDn(), dto.isDr(), dto.isEn(), dto.isEr(), dto.getYear(), dto.getMonth());
    }

    @ResponseBody
    @RequestMapping(value = "incomesChart", method = GET)
    public CanvasJSChartDTO incomesChart(@ModelAttribute("dto") @Valid ExpenseChartDTO dto, BindingResult errors) {
        if (errors.hasErrors()) {
            return this.notOkResponse();
        }
        return this.incomesService.renderAbsoluteChart("Ingresos", dto.getMonths(), dto.getSeries(), dto.getYear(), dto.getMonth(), "USD");
    }

    @ResponseBody
    @RequestMapping(value = "expensesChart", method = GET)
    public CanvasJSChartDTO expensesChart(@ModelAttribute("dto") @Valid ExpenseChartDTO dto, BindingResult errors) {
        if (errors.hasErrors()) {
            return this.notOkResponse();
        }
        return this.expenseService.renderAbsoluteChart("Gastos", dto.getMonths(), dto.getSeries(), dto.getYear(), dto.getMonth(), "USD");
    }

    @ResponseBody
    @RequestMapping(value = "fciChart", method = GET)
    public CanvasJSChartDTO fciChart(@ModelAttribute("dto") @Valid ExpenseChartDTO dto, BindingResult errors) {
        if (errors.hasErrors()) {
            return this.notOkResponse();
        }
        return this.fciService.renderAbsoluteChart("Consultatio Plus F.C.I.", dto.getMonths(), dto.getSeries(), dto.getYear(), dto.getMonth(), "USD");
    }

    @ResponseBody
    @RequestMapping(value = "savingsDetailChart", method = GET)
    public CanvasJSChartDTO savingsChart(@ModelAttribute("dto") @Valid ExpenseChartDTO dto, BindingResult errors) {
        if (errors.hasErrors()) {
            return this.notOkResponse();
        }
        return this.savingsService.renderAbsoluteChart("Ahorros", dto.getMonths(), dto.getSeries(), dto.getYear(), dto.getMonth(), "USD");
    }
    
    @ResponseBody
    @RequestMapping(value = "consortiumExpensesChart", method = GET)
    public CanvasJSChartDTO consortiumExpensesChart(@ModelAttribute("dto") @Valid ExpenseChartDTO dto, BindingResult errors) {
        if (errors.hasErrors()) {
            return this.notOkResponse();
        }
        return this.consortiumExpenseService.renderAbsoluteChart("Gastos del Consorcio", dto.getMonths(), dto.getSeries(), dto.getYear(), dto.getMonth(), "USD");
    }

    @ResponseBody
    @RequestMapping(value = "expensesPercentChart", method = GET)
    public CanvasJSChartDTO expensesPercentChart(@ModelAttribute("dto") @Valid ExpenseChartDTO dto, BindingResult errors) {
        if (errors.hasErrors()) {
            return this.notOkResponse();
        }
        return this.expenseService.renderIncomeRelativeChart("Gastos / Ingresos", dto.getMonths(), dto.getSeries(), "USD");
    }

    @ResponseBody
    @RequestMapping(value = "goldSavingsChart", method = GET)
    public CanvasJSChartDTO goldSavingsChart() {
        return this.chartService.goldIncomeAndSavings();
    }

    @ResponseBody
    @RequestMapping(value = "savingsAndIncomeChangeChart", method = GET)
    public CanvasJSChartDTO savingsAndIncomeChangeChart() {
        return this.chartService.savingsAndIncomeEvolution();
    }

    @ResponseBody
    @RequestMapping(value = "savedSalariesChart", method = GET)
    public CanvasJSChartDTO savedSalariesChart() {
        return this.chartService.savedSalaries();
    }

}
