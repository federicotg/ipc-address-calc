/*
 * Copyright (C) 2016 Federico Tello Gentile <federicotg@gmail.com>
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
package org.fede.calculator;

import java.text.MessageFormat;
import java.util.List;
import org.fede.calculator.money.NoSeriesDataFoundException;
import org.fede.calculator.service.DigitalContentService;
import org.fede.calculator.service.InvestmentService;
import org.fede.calculator.web.dto.InvestmentReportDTO;
import org.fede.digitalcontent.model.DigitalContent;
import org.fede.digitalcontent.model.Repository;
import org.fede.digitalcontent.model.StorageMedium;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("/applicationContext-calculadoras.xml")
public class SingleMediaTest {

    @Autowired
    private DigitalContentService service;
    
    @Autowired
    private InvestmentService investments;

    public SingleMediaTest() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void singles() {

        assertNotNull(this.service);

        /*for (StorageMedium disc : Repository.STORAGE.findAll()) {
            DigitalContent dc = disc.getContents().iterator().next();
            if (disc.getContents().size() == 1 && this.isInOneMedium(dc)) {
                System.out.println(MessageFormat.format("{0}\t{1}", disc.getName(), dc.toString()));
            }
        }*/

    }

    
    @Test
    public void report() throws NoSeriesDataFoundException {
        List<InvestmentReportDTO> report = this.investments.investment("USD");
        for(InvestmentReportDTO dto : report){
            System.out.println(dto.getInflationPct());
        }
    }

    private boolean isInOneMedium(DigitalContent dc) {
        int count = 0;
        for (StorageMedium disc : Repository.STORAGE.findAll()) {
            if (disc.contains(dc)) {
                count++;
            }
        }
        return count == 1;
    }
}
