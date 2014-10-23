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
package org.fede.calculator.service;

import javax.annotation.PostConstruct;
import org.fede.digitalcontent.model.CityFactory;
import static org.fede.digitalcontent.model.Country.AUSTRALIA;
import static org.fede.digitalcontent.model.Country.AUSTRIA;
import static org.fede.digitalcontent.model.Country.DENMARK;
import static org.fede.digitalcontent.model.Country.FRANCE;
import static org.fede.digitalcontent.model.Country.GERMANY;
import static org.fede.digitalcontent.model.Country.ITALY;
import static org.fede.digitalcontent.model.Country.RUSSIA;
import static org.fede.digitalcontent.model.Country.SWITZERLAND;
import static org.fede.digitalcontent.model.Country.UK;
import static org.fede.digitalcontent.model.Country.USA;
import static org.fede.digitalcontent.model.Country.SPAIN;
import org.fede.digitalcontent.model.StorageBox;
import org.fede.digitalcontent.model.StorageBoxFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;


/**
 *
 * @author fede
 */
@Service
@Lazy
public class LazyDigitalContentService implements DigitalContentService {

    private final StorageBoxFactory storageBoxFactory = new StorageBoxFactory();
    private final CityFactory cityFactory = new CityFactory();
    
    @PostConstruct
    public void initBasicObjects() {
        // cities
        this.cityFactory.createCity("New York", USA);
        this.cityFactory.createCity("Londres", UK);
        this.cityFactory.createCity("Madrid", SPAIN);
        this.cityFactory.createCity("París", FRANCE);
        this.cityFactory.createCity("Moscú", RUSSIA);
        
    }

    @Override
    public Iterable<StorageBox> getAllBoxes() {
        return this.storageBoxFactory.findAll();
    }
    
}
