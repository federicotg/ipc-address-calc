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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

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

    @RequestMapping(value = "/report", method = RequestMethod.GET)
    public ModelAndView fullReport() {
        return new ModelAndView("dcReport", "list", this.dcService.getFullReport());
    }

    @RequestMapping(value = "/report/box/{box}", method = RequestMethod.GET)
    public ModelAndView boxReport(@PathVariable String box) {
        return new ModelAndView("dcReport", "list", this.dcService.getBoxReport(box));
    }

    @RequestMapping(value = "/report/composer/{name}", method = RequestMethod.GET)
    public ModelAndView composerReport(@PathVariable String name) {
        return new ModelAndView("dcReport", "list", this.dcService.getComposerReport(name));
    }

    @RequestMapping(value = "/report/opus/{name}", method = RequestMethod.GET)
    public ModelAndView opusReport(@PathVariable String name) {
        return new ModelAndView("dcReport", "list", this.dcService.getOpusReport(name));
    }

    @RequestMapping(value = "/report/type/{type}", method = RequestMethod.GET)
    public ModelAndView opusTypeReport(@PathVariable String type) {
        return new ModelAndView("dcReport", "list", this.dcService.getOpusTypeReport(type));
    }

    @RequestMapping(value = "/report/venue/{name}", method = RequestMethod.GET)
    public ModelAndView venueReport(@PathVariable String name) {
        return new ModelAndView("dcReport", "list", this.dcService.getVenueReport(name));
    }

}
