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

import java.util.HashSet;
import java.util.Set;
import org.fede.calculator.service.DigitalContentService;
import org.fede.digitalcontent.model.DigitalContent;
import org.fede.digitalcontent.model.OpusType;
import org.fede.digitalcontent.model.Performance;
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
@ContextConfiguration({"file:src/main/webapp/WEB-INF/calculadoras-servlet.xml", "file:src/main/resources/applicationContext-calculadoras.xml"})
public class DigitalCollectionTest {

    @Autowired
    private DigitalContentService digitalContentService;

    public DigitalCollectionTest() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void distRepo() {

        assertNotNull(this.digitalContentService);

        Set<StorageMedium> all = Repository.STORAGE.findAll();
        assertNotNull(all);
        assertFalse(all.isEmpty());

        for (StorageMedium disc : Repository.STORAGE.findAll()) {
            assertNotNull(disc);
            // System.out.println(disc.getName());
        }
    }

    private Set<StorageMedium> storageMediumFor(DigitalContent dc) {
        Set<StorageMedium> set = new HashSet<>();
        for (StorageMedium disc : Repository.STORAGE.findAll()) {
            if (disc.getContents().contains(dc)) {
                set.add(disc);
            }
        }
        return set;
    }

    @Test
    public void singleStorageMediumDigitalContent() {
        for (DigitalContent dc : Repository.DIGITALCONTENT.findAll()) {

            if (storageMediumFor(dc).size() == 1) {
                StorageMedium disc = storageMediumFor(dc).iterator().next();
                if (disc.getContents().size() == 1) {
                    for (Performance p : dc.getPerformances()) {
                        if (p.getOpusType().equals(OpusType.BALLET)
                                || p.getOpusType().equals(OpusType.OPERA)
                                || p.getOpusType().equals(OpusType.ORATORIO)) {
                            System.out.println(disc.getName() + " " + p.getDetailedTitle());
                        }
                    }
                }
            }
        }
    }
}