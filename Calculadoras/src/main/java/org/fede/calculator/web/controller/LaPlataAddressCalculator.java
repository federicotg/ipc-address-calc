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

import java.util.logging.Logger;
import javax.validation.Valid;
import org.fede.calculator.service.LaPlataAddressService;
import org.fede.calculator.web.dto.LaPlataAddressDTO;
import org.springframework.beans.factory.annotation.Autowired;
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
@Controller
public class LaPlataAddressCalculator {

    @Autowired
    private LaPlataAddressService addressService;
    
    private static final Logger LOG = Logger.getLogger(LaPlataAddressCalculator.class.getName());

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public LaPlataAddressDTO errorHandler(Exception ex){
        LOG.throwing(LaPlataAddressCalculator.class.getName(), "errorHandler", ex);
        return new LaPlataAddressDTO("La dirección ingresada no es válida.", "");
    }
    
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView computeAddressForm() {
        return new ModelAndView("address", "address", new LaPlataAddressDTO());
    }

    @RequestMapping(value = "/address", method = RequestMethod.POST)
    public LaPlataAddressDTO computeAddress(
            @RequestBody @Valid LaPlataAddressDTO address,
            BindingResult result) {

        if (result.hasErrors()) {
            return new LaPlataAddressDTO(result.getFieldError("originalText").getDefaultMessage(), address.getOriginalText());
        }
        LaPlataAddressDTO answer = this.addressService.getAddress(address);
        if (!answer.isValid()) {
            return new LaPlataAddressDTO("La dirección ingresada no es válida.", address.getOriginalText());
        }
        return answer;
    }
}
