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

import javax.annotation.Resource;
import org.fede.calculator.repository.JDORepository;
import org.fede.digitalcontent.model.City;
import org.fede.digitalcontent.model.Country;
import org.fede.digitalcontent.model.DigitalContent;
import org.fede.digitalcontent.model.FormatType;
import org.fede.digitalcontent.model.Opus;
import org.fede.digitalcontent.model.OpusType;
import org.fede.digitalcontent.model.Performance;
import org.fede.digitalcontent.model.Person;
import org.fede.digitalcontent.model.Quality;
import org.fede.digitalcontent.model.Role;
import org.fede.digitalcontent.model.RoleType;
import org.fede.digitalcontent.model.StorageBox;
import org.fede.digitalcontent.model.StorageMedium;
import org.fede.digitalcontent.model.StorageMediumType;
import org.fede.digitalcontent.model.Venue;
import org.fede.digitalcontent.model.WebResource;
import org.fede.digitalcontent.model.WebResourceType;
import org.springframework.stereotype.Service;

/**
 *
 * @author fede
 */
@Service
public class DigitalContentServiceImpl implements DigitalContentService {

    @Resource(name = "basicRepository")
    private JDORepository repo;

    
   
    @Override
    public void initBasicObjects() {

        this.repo.deleteAll(OpusType.class);
        this.repo.deleteAll(WebResourceType.class);
        this.repo.deleteAll(RoleType.class);
        this.repo.deleteAll(FormatType.class);
        this.repo.deleteAll(StorageMediumType.class);
        this.repo.deleteAll(Quality.class);
        this.repo.deleteAll(City.class);
        this.repo.deleteAll(Country.class);
        this.repo.deleteAll(Person.class);
        this.repo.deleteAll(WebResource.class);
        this.repo.deleteAll(Opus.class);
        this.repo.deleteAll(Role.class);
        this.repo.deleteAll(Performance.class);
        this.repo.deleteAll(Venue.class);
        this.repo.deleteAll(StorageMedium.class);
        this.repo.deleteAll(DigitalContent.class);
        this.repo.deleteAll(StorageBox.class);
        

        this.repo.save(new Country("Estados Unidos"));
        this.repo.save(new Country("Francia"));
        this.repo.save(new Country("Italia"));
        this.repo.save(new Country("Alemania"));
        this.repo.save(new Country("Reino Unido"));
        this.repo.save(new Country("Australia"));
        this.repo.save(new Country("Austria"));
        this.repo.save(new Country("Suiza"));
        this.repo.save(new Country("Rusia"));

        final Quality q1080 = new Quality("1080p");
        this.repo.save(q1080);
        this.repo.save(new Quality("720p"));
        this.repo.save(new Quality("Lossless Audio"));

        this.repo.save(new City("Londres", this.repo.findFirstByName(Country.class, "Reino Unido")));
        this.repo.save(new City("Milano", this.repo.findFirstByName(Country.class, "Italia")));
        this.repo.save(new City("New York", this.repo.findFirstByName(Country.class, "Estados Unidos")));
        this.repo.save(new City("Moscú", this.repo.findFirstByName(Country.class, "Rusia")));
        this.repo.save(new City("París", this.repo.findFirstByName(Country.class, "Francia")));
        this.repo.save(new City("Sydney", this.repo.findFirstByName(Country.class, "Australia")));

        this.repo.save(new Venue("ROH", this.repo.findFirstByName(City.class, "Londres")));
        this.repo.save(new Venue("The Met", this.repo.findFirstByName(City.class, "New York")));
        this.repo.save(new Venue("Alla Scalla", this.repo.findFirstByName(City.class, "Milano")));
        this.repo.save(new Venue("Opera de París", this.repo.findFirstByName(City.class, "París")));

        final OpusType opera =new OpusType("Ópera");
        this.repo.save(opera);
        this.repo.save(new OpusType("Ballet"));
        this.repo.save(new OpusType("Concierto"));

        final FormatType mkv = new FormatType("MKV");
        this.repo.save(mkv);
        this.repo.save(new FormatType("ISO"));
        this.repo.save(new FormatType("FLAC"));

        this.repo.save(new RoleType("Espectador"));
        final RoleType musicComposer = new RoleType("Compositor");
        this.repo.save(musicComposer);
        this.repo.save(new RoleType("Cantante"));

        this.repo.save(new StorageMediumType("CD"));
        this.repo.save(new StorageMediumType("DVD"));
        this.repo.save(new StorageMediumType("DVD+R DL"));
        this.repo.save(new StorageMediumType("BD-R"));

        this.repo.save(new WebResourceType("IMDB"));
        this.repo.save(new WebResourceType("Wikipedia"));

        this.repo.save(new Person("Bizet"));
        this.repo.save(new Person("Giacomo Puccini"));
        this.repo.save(new Person("Giuseppe Verdi"));

        
        final Opus traviata = new Opus("La Traviata", opera);
        traviata.addPerson(musicComposer, this.repo.findFirstByName(Person.class, "Giuseppe Verdi"));

        final Opus tosca = new Opus("Tosca", opera);
        tosca.addPerson(musicComposer, this.repo.findFirstByName(Person.class, "Giacomo Puccini"));

        final Opus carmen = new Opus("Carmen", opera);
        carmen.addPerson(musicComposer, this.repo.findFirstByName(Person.class, "Bizet"));
        carmen.addWebResource(this.repo.findFirstByName(WebResourceType.class, "Wikipedia"), "https://es.wikipedia.org/wiki/Carmen_%28%C3%B3pera%29");

        this.repo.save(traviata);
        this.repo.save( tosca);
        this.repo.save( carmen);

        Performance carmenTheMet = new Performance(carmen, this.repo.findFirstByName(Venue.class, "The Met"), 16, 1, 2010);
        Performance traviataParis = new Performance(traviata, this.repo.findFirstByName(Venue.class, "Opera de París"), 17, 6, 2014);
        Performance toscaRoh = new Performance(tosca, this.repo.findFirstByName(Venue.class, "ROH"), 17, 7, 2011);

        this.repo.save(carmenTheMet);
        this.repo.save(traviataParis);
        this.repo.save(toscaRoh);
        
        StorageMedium aBDR = new StorageMedium();
        aBDR.setName("12-05");
        aBDR.setType(this.repo.findFirstByName(StorageMediumType.class, "BD-R"));

        aBDR.addContent(
                new DigitalContent(
                        carmenTheMet,
                        q1080,
                        mkv));
        aBDR.addContent(
                new DigitalContent(
                        traviataParis,
                        q1080,
                        mkv));
        aBDR.addContent(
                new DigitalContent(
                        toscaRoh,
                        q1080,
                        mkv));

        StorageBox box = new StorageBox("12");
        box.addStorageMedium(aBDR);
        this.repo.save(box);
    }

    @Override
    public Iterable<StorageBox> getAllBoxes() {
        return this.repo.findAll(StorageBox.class);
    }

}
