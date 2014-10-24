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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;
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
import org.fede.digitalcontent.model.DigitalContentFactory;
import org.fede.digitalcontent.model.FormatType;
import static org.fede.digitalcontent.model.Language.ITALIAN;
import static org.fede.digitalcontent.model.Language.GERMAN;
import static org.fede.digitalcontent.model.Language.FRENCH;
import static org.fede.digitalcontent.model.Language.RUSSIAN;
import static org.fede.digitalcontent.model.Language.ENGLISH;
import org.fede.digitalcontent.model.OpusFactory;
import org.fede.digitalcontent.model.OpusType;
import static org.fede.digitalcontent.model.OpusType.BALLET;
import static org.fede.digitalcontent.model.OpusType.OPERA;
import org.fede.digitalcontent.model.Pair;
import org.fede.digitalcontent.model.PerformanceFactory;
import org.fede.digitalcontent.model.Person;
import org.fede.digitalcontent.model.PersonFactory;
import org.fede.digitalcontent.model.Quality;
import org.fede.digitalcontent.model.StorageBox;
import org.fede.digitalcontent.model.StorageBoxFactory;
import org.fede.digitalcontent.model.StorageMedium;
import org.fede.digitalcontent.model.StorageMediumFactory;
import org.fede.digitalcontent.model.StorageMediumType;
import org.fede.digitalcontent.model.VenueFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import static org.fede.digitalcontent.model.Factory.*;
import org.fede.digitalcontent.model.Opus;

/**
 *
 * @author fede
 */
@Service
@Lazy
public class LazyDigitalContentService implements DigitalContentService {

    private final DateFormat df = new SimpleDateFormat("dd/MM/yyyy");



    @PostConstruct
    public void initBasicObjects() {
        // cities
        CITY.createCity("New York", USA);
        CITY.createCity("Londres", UK);
        CITY.createCity("Glyndebourne", UK);
        CITY.createCity("Madrid", SPAIN);
        CITY.createCity("París", FRANCE);
        CITY.createCity("Moscú", RUSSIA);
        CITY.createCity("Milano", ITALY);
        CITY.createCity("Nápoles", ITALY);
        CITY.createCity("Salzburgo", AUSTRIA);
        CITY.createCity("Sydney", AUSTRALIA);
        CITY.createCity("Copenhagen", DENMARK);
        CITY.createCity("Madrid", SPAIN);
        CITY.createCity("Verona", ITALY);
        CITY.createCity("Valencia", SPAIN);
        CITY.createCity("Barcelona", SPAIN);
        CITY.createCity("Baden-Baden", GERMANY);
        CITY.createCity("Zürich", SWITZERLAND);
        CITY.createCity("San Petersburgo", RUSSIA);

        final Person puccini = PERSON.createPerson("Giacomo Puccini");
        final Person verdi = PERSON.createPerson("Giuseppe Verdi");
        final Person wagner = PERSON.createPerson("Richard Wagner");
        final Person donizetti = PERSON.createPerson("Gaetano Donizetti");
        final Person mozart = PERSON.createPerson("W. A. Mozart");
        final Person rossini = PERSON.createPerson("Rossini");

        PERSON.createPerson("Bellini");
        PERSON.createPerson("Bizet");
        PERSON.createPerson("Hector Berlioz");
        PERSON.createPerson("Gounoud");
        PERSON.createPerson("Antonin Dvorák");
        PERSON.createPerson("Borodin");
        PERSON.createPerson("A. Ponchielli");
        PERSON.createPerson("Monteverdi");
        PERSON.createPerson("Ravel");
        PERSON.createPerson("Richard Strauss");
        PERSON.createPerson("Rameau");
        PERSON.createPerson("Tchaikovsky");
        PERSON.createPerson("Jules Massenet");
        PERSON.createPerson("Léo Delibes");
        PERSON.createPerson("Leoncavallo");
        PERSON.createPerson("Leoš Janáček");
        PERSON.createPerson("Dmitri Shostakovich");
        PERSON.createPerson("Händel");
        PERSON.createPerson("Zandonai");

        for (String title : new String[]{
            "Macbeth", "Aida", "Rigoletto", "Nabucco", "La Traviata",
            "Il Trovatore", "Simón Boccanegra", "Otello", "Un Ballo in Maschera",
            "Falstaff", "La Forza del Destino", "Don Carlo", "I Due Foscari"}) {
            OPUS.createOpus(title, OPERA, ITALIAN, verdi);
        }

        for (String title : new String[]{
            "Parsifal", "Rienzi", "Das Rheingold", "Die Walküre", "Gotterdammerung",
            "Siegfried", "Tannhauser", "Tristan und Ilsode",
            "Falstaff", "La Forza del Destino"}) {
            OPUS.createOpus(title, OPERA, GERMAN, wagner);
        }

        for (String title : new String[]{
            "Anna Bolena", "Don Pasquale", "L'elisir d'amore", "Lucia de Lammermoor", "Maria Stuarda"}) {
            OPUS.createOpus(title, OPERA, ITALIAN, donizetti);
        }

        new Opus.Builder("La Fille du Regiment")
                .french()
                .opera()
                .by("Gaetano Donizetti")
                .build();
        
        OPUS.createOpus("Carmen", OPERA, FRENCH, PERSON.findById("Bizet"));
        OPUS.createOpus("Les Troyens", OPERA, FRENCH, PERSON.findById("Hector Berlioz"));
        OPUS.createOpus("The Nose", OPERA, FRENCH, PERSON.findById("Dmitri Shostakovich"));
        OPUS.createOpus("Lakme", OPERA, FRENCH, PERSON.findById("Léo Delibes"));
        OPUS.createOpus("Werther", OPERA, FRENCH, PERSON.findById("Jules Massenet"));
        OPUS.createOpus("Prince Igor", OPERA, RUSSIAN, PERSON.findById("Borodin"));
        OPUS.createOpus("I Puritani", OPERA, ITALIAN, PERSON.findById("Bellini"));
        OPUS.createOpus("Messiah", OPERA, ENGLISH, PERSON.findById("Händel"));
        OPUS.createOpus("Eugene Onegin", OPERA, RUSSIAN, PERSON.findById("Tchaikovsky"));
        OPUS.createOpus("Francesca da Rimini", OPERA, ITALIAN, PERSON.findById("Zandonai"));

        for (String title : new String[]{"Così fan tutte", "Don Giovanni", "Il Sogno di Scipione", "La Clemenza di Tito", "Le nozze di Figaro"}) {
            OPUS.createOpus(title, OPERA, ITALIAN, mozart);
        }

        for (String title : new String[]{"La Flauta Mágica", "El rapto en el serrallo"}) {
            OPUS.createOpus(title, OPERA, GERMAN, mozart);
        }

        for (String title : new String[]{"Gianni Schicchi", "Il Trittico", "La Bohème",
            "La Fanciulla del West", "La Rondine", "Madama Butterfly",
            "Manon Lescaut", "Tosca", "Turandot"}) {
            OPUS.createOpus(title, OPERA, ITALIAN, puccini);
        }

        for (String title : new String[]{"Armida", "Il Barbiere di Siviglia", "Il turco in Italia",
            "L'Italiana in Algeri", "La Cenerentola", "La Donna del Lago", "Sigismondo",
            "Zelmira"}) {
            OPUS.createOpus(title, OPERA, ITALIAN, rossini);
        }

        VENUE.createVenue("ROH", CITY.findById("Londres"));
        VENUE.createVenue("The Met", CITY.findById("New York"));
        VENUE.createVenue("Glyndebourne", CITY.findById("Glyndebourne"));
        VENUE.createVenue("ONP", CITY.findById("París"));
        VENUE.createVenue("Royal Danish Theatre", CITY.findById("Copenhagen"));
        VENUE.createVenue("ROH", CITY.findById("Londres"));
        VENUE.createVenue("Palacio de las Artes Reina Sofía", CITY.findById("Valencia"));
        VENUE.createVenue("Gran Teatro del Liceo de Barcelona", CITY.findById("Barcelona"));
        VENUE.createVenue("alla Scala", CITY.findById("Milano"));
        VENUE.createVenue("Festspielhaus Baden-Baden", CITY.findById("Baden-Baden"));
        VENUE.createVenue("Sydney Opera House", CITY.findById("Sydney"));
        VENUE.createVenue("Bolshói", CITY.findById("Moscú"));
        VENUE.createVenue("Mariinski", CITY.findById("San Petersburgo"));
        VENUE.createVenue("Ópera de Zürich", CITY.findById("Zürich"));
        VENUE.createVenue("Teatro di San Carlo", CITY.findById("Nápoles"));

        this.createOperaPerformanceMKV1080("La Gioconda", "ONP", "13/05/2013");
        this.createOperaPerformanceMKV1080("Rusalka", "The Met", "08/02/2014");
        this.createOperaPerformanceMKV1080("I Puritani", "ONP", "09/12/2013");
        this.createOperaPerformanceMKV1080("Prince Igor", "The Met", "01/03/2014");
        this.createOperaPerformanceMKV1080("The Nose", "The Met", "26/10/2013");
        this.createOperaPerformanceMKV1080("Don Pasquale", "Glyndebourne", "06/08/2013");
        this.createOperaPerformanceMKV1080("L'elisir d'amore", "The Met", "13/10/2012");
        this.createOperaPerformanceMKV1080("Maria Stuarda", "The Met", "19/01/2013");
        this.createOperaPerformanceMKV1080("Aida", "The Met", "15/12/2012");
        this.createOperaPerformanceMKV1080("Falstaff", "The Met", "15/12/2013");
        this.createOperaPerformanceMKV1080("La Traviata", "ONP", "17/06/2014");
        this.createOperaPerformanceMKV1080("La Traviata", "Glyndebourne", "10/08/2014");
        this.createOperaPerformanceMKV1080("Macbeth", "París", "04/04/2009");
        this.createOperaPerformanceMKV1080("Macbeth", "The Met", "11/10/2014");
        this.createOperaPerformanceMKV1080("Nabucco", "ROH", "26/04/2013");
        this.createOperaPerformanceMKV1080("Otello", "The Met", "28/10/2012");
        this.createOperaPerformanceMKV1080("Otello", "Teatro di San Carlo", "22/04/2014");
        this.createOperaPerformanceMKV1080("Rigoletto", "Ópera de Zürich", null);
        this.createOperaPerformanceMKV1080("Rigoletto", "The Met", "16/02/2013");
        this.createOperaPerformanceMKV1080("Un Ballo in Maschera", "The Met", "08/12/2012");
        this.createOperaPerformanceMKV1080("Messiah", "Zürich", null);
        this.createOperaPerformanceMKV1080("Les Troyens", "The Met", "05/01/2013");
        this.createOperaPerformanceMKV1080("Werther", "The Met", "15/03/2014");
        this.createOperaPerformanceMKV1080("Lakme", "Sydney Opera House", null);
        this.createOperaPerformanceMKV1080("Cosi Fan Tutte", "The Met", "26/04/2014");
        this.createOperaPerformanceMKV1080("La Clemenza di Tito", "The Met", "01/12/2012");
        this.createOperaPerformanceMKV1080("La Bohème", "ROH", "15/01/2013");
        this.createOperaPerformanceMKV1080("La Fanciulla del West", "ONP", "10/02/2014");
        this.createOperaPerformanceMKV1080("Manon Lescaut", "ROH", "24/06/2014");
        this.createOperaPerformanceMKV1080("Tosca", "The Met", "09/11/2013");
        this.createOperaPerformanceMKV1080("Tosca", "ONP", "16/10/2014");
        this.createOperaPerformanceMKV1080("La Cenerentola", "The Met", "10/05/2014");
        this.createOperaPerformanceMKV1080("La Donna del Lago", "ROH", "27/05/2013");
        this.createOperaPerformanceMKV1080("Eugene Onegin", "ROH", "20/02/2013");
        this.createOperaPerformanceMKV1080("Eugene Onegin", "The Met", "05/10/2013");
        this.createOperaPerformanceMKV1080("Parsifal", "The Met", "02/03/2013");
        this.createOperaPerformanceMKV1080("Francesca da Rimini", "The Met", "16/03/2013");

        StorageMedium bdr1 = STORAGE.createStorageMedium("Un disco", StorageMediumType.BDR);
        StorageBox box1 = STORAGEBOX.createStorageBox("A Box");
        bdr1.setContents(DIGITALCONTENT.findAll());
        Set<StorageMedium> testSet = new HashSet<>();
        testSet.add(bdr1);
        box1.setMedia(testSet);

    }

    private void createOperaPerformanceMKV1080(String title, String theatre, String date) {
        try {
            DIGITALCONTENT.createDigitalContent(
                    PERFORMANCE.createPerformance(
                            OPUS.findById(new Pair<>(title, OPERA)),
                            VENUE.findById(theatre),
                            date != null
                                    ? df.parse(date)
                                    : null),
                    FormatType.MKV, Quality.HD1080);
        } catch (ParseException pEx) {
            throw new RuntimeException(pEx);
        }
    }

    @Override
    public Iterable<StorageBox> getAllBoxes() {
        return STORAGEBOX.findAll();
    }

}
