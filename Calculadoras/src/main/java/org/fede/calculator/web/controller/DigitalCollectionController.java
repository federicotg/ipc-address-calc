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
import org.fede.calculator.service.DigitalContentService;
import org.fede.digitalcontent.model.StorageBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 * @author fede
 */
@Controller
@RequestMapping("/dc")
public class DigitalCollectionController {
    private static final Logger LOG = Logger.getLogger(DigitalCollectionController.class.getName());

    @Autowired
    private DigitalContentService dcService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String dcIndex() {
        return "dcIndex";
    }
    
    @RequestMapping(value = "/initSampleData", method = RequestMethod.GET)
    public String initSampleData() {
        this.dcService.initBasicObjects();
        return "dcIndex";
    }
    
    @RequestMapping(value = "/allBoxes", method = RequestMethod.GET)
    public Iterable<StorageBox> allBoxes() {
        return this.dcService.getAllBoxes();
    }
    
}
