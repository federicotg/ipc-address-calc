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

import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.validation.Valid;
import org.fede.calculator.money.NoSeriesDataFoundException;
import org.fede.calculator.service.MoneyService;
import org.fede.calculator.web.dto.CurrencyLimitsDTO;
import org.fede.calculator.web.dto.MoneyDTO;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author fede
 */
@Lazy
@Controller
public class MoneyCalculator {

    private static final Logger LOG = Logger.getLogger(MoneyCalculator.class.getName());

    @Resource(name = "argMoneyService")
    @Lazy
    private MoneyService arsMoneyService;

    @Resource(name = "usdMoneyService")
    @Lazy
    private MoneyService usdMoneyService;

    @Resource(name = "moneyServices")
    @Lazy
    private Map<String, MoneyService> moneyServices;

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public MoneyDTO errorHandler(Exception ex) {
        LOG.log(Level.SEVERE, "Error al calcular IPC", ex);
        MoneyDTO dto = new MoneyDTO();
        dto.setValid(false);
        dto.setMessage("El monto o la fecha ingresados son válidos.");
        return dto;
    }

    @RequestMapping(value = "/money", method = RequestMethod.GET)
    public ModelAndView showMoneyForm() {
        return new ModelAndView(
                "money",
                "limits", Arrays.asList(new CurrencyLimitsDTO[]{usdMoneyService.getLimits(), arsMoneyService.getLimits()}))
                .addObject("dto", new MoneyDTO());
    }

    @RequestMapping(value = "/money", method = RequestMethod.POST)
    public MoneyDTO computeMoney(
            @RequestBody @Valid MoneyDTO money,
            BindingResult result) throws NoSeriesDataFoundException {

        if (result.hasErrors()) {
            MoneyDTO dto = new MoneyDTO();
            dto.setValid(false);
            dto.setMessage("El monto ingresado no es válido.");
            return dto;
        }

        return this.moneyServices.get(money.getCurrency().getIso4217()).getMoney(money);
    }

}
