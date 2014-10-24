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

/**
 *
 * @author fede
 */
@Service
@Lazy
public class LazyDigitalContentService implements DigitalContentService {

    private final DateFormat df = new SimpleDateFormat("dd/MM/yyyy");

    private final StorageBoxFactory storageBoxFactory = new StorageBoxFactory();
    private final CityFactory cityFactory = new CityFactory();
    private final PersonFactory personFactory = new PersonFactory();
    private final OpusFactory opusFactory = new OpusFactory();
    private final VenueFactory venueFactory = new VenueFactory();
    private final PerformanceFactory performanceFactory = new PerformanceFactory();
    private final DigitalContentFactory dcFactory = new DigitalContentFactory();
    private final StorageMediumFactory storageFactory = new StorageMediumFactory();
    private final StorageBoxFactory boxFactory = new StorageBoxFactory();

    @PostConstruct
    public void initBasicObjects() {
        // cities
        this.cityFactory.createCity("New York", USA);
        this.cityFactory.createCity("Londres", UK);
        this.cityFactory.createCity("Glyndebourne", UK);
        this.cityFactory.createCity("Madrid", SPAIN);
        this.cityFactory.createCity("París", FRANCE);
        this.cityFactory.createCity("Moscú", RUSSIA);
        this.cityFactory.createCity("Milano", ITALY);
        this.cityFactory.createCity("Nápoles", ITALY);
        this.cityFactory.createCity("Salzburgo", AUSTRIA);
        this.cityFactory.createCity("Sydney", AUSTRALIA);
        this.cityFactory.createCity("Copenhagen", DENMARK);
        this.cityFactory.createCity("Madrid", SPAIN);
        this.cityFactory.createCity("Verona", ITALY);
        this.cityFactory.createCity("Valencia", SPAIN);
        this.cityFactory.createCity("Barcelona", SPAIN);
        this.cityFactory.createCity("Baden-Baden", GERMANY);
        this.cityFactory.createCity("Zürich", SWITZERLAND);
        this.cityFactory.createCity("San Petersburgo", RUSSIA);

        final Person puccini = this.personFactory.createPerson("Giacomo Puccini");
        final Person verdi = this.personFactory.createPerson("Giuseppe Verdi");
        final Person wagner = this.personFactory.createPerson("Richard Wagner");
        final Person donizetti = this.personFactory.createPerson("Gaetano Donizetti");
        final Person mozart = this.personFactory.createPerson("W. A. Mozart");
        final Person rossini = this.personFactory.createPerson("Rossini");

        this.personFactory.createPerson("Bellini");
        this.personFactory.createPerson("Bizet");
        this.personFactory.createPerson("Hector Berlioz");
        this.personFactory.createPerson("Gounoud");
        this.personFactory.createPerson("Antonin Dvorák");
        this.personFactory.createPerson("Borodin");
        this.personFactory.createPerson("A. Ponchielli");
        this.personFactory.createPerson("Monteverdi");
        this.personFactory.createPerson("Ravel");
        this.personFactory.createPerson("Richard Strauss");
        this.personFactory.createPerson("Rameau");
        this.personFactory.createPerson("Tchaikovsky");
        this.personFactory.createPerson("Jules Massenet");
        this.personFactory.createPerson("Léo Delibes");
        this.personFactory.createPerson("Leoncavallo");
        this.personFactory.createPerson("Leoš Janáček");
        this.personFactory.createPerson("Dmitri Shostakovich");
        this.personFactory.createPerson("Händel");
        this.personFactory.createPerson("Zandonai");

        for (String title : new String[]{
            "Macbeth", "Aida", "Rigoletto", "Nabucco", "La Traviata",
            "Il Trovatore", "Simón Boccanegra", "Otello", "Un Ballo in Maschera",
            "Falstaff", "La Forza del Destino", "Don Carlo", "I Due Foscari"}) {
            this.opusFactory.createOpus(title, OPERA, ITALIAN, verdi);
        }

        for (String title : new String[]{
            "Parsifal", "Rienzi", "Das Rheingold", "Die Walküre", "Gotterdammerung",
            "Siegfried", "Tannhauser", "Tristan und Ilsode",
            "Falstaff", "La Forza del Destino"}) {
            this.opusFactory.createOpus(title, OPERA, GERMAN, wagner);
        }

        for (String title : new String[]{
            "Anna Bolena", "Don Pasquale", "L'elisir d'amore", "Lucia de Lammermoor", "Maria Stuarda"}) {
            this.opusFactory.createOpus(title, OPERA, ITALIAN, donizetti);
        }

        this.opusFactory.createOpus("La Fille du Regiment", OPERA, FRENCH, donizetti);
        this.opusFactory.createOpus("Carmen", OPERA, FRENCH, this.personFactory.findById("Bizet"));
        this.opusFactory.createOpus("Les Troyens", OPERA, FRENCH, this.personFactory.findById("Hector Berlioz"));
        this.opusFactory.createOpus("The Nose", OPERA, FRENCH, this.personFactory.findById("Dmitri Shostakovich"));
        this.opusFactory.createOpus("Lakme", OPERA, FRENCH, this.personFactory.findById("Léo Delibes"));
        this.opusFactory.createOpus("Werther", OPERA, FRENCH, this.personFactory.findById("Jules Massenet"));
        this.opusFactory.createOpus("Prince Igor", OPERA, RUSSIAN, this.personFactory.findById("Borodin"));
        this.opusFactory.createOpus("I Puritani", OPERA, ITALIAN, this.personFactory.findById("Bellini"));
        this.opusFactory.createOpus("Messiah", OPERA, ENGLISH, this.personFactory.findById("Händel"));
        this.opusFactory.createOpus("Eugene Onegin", OPERA, RUSSIAN, this.personFactory.findById("Tchaikovsky"));
        this.opusFactory.createOpus("Francesca da Rimini", OPERA, ITALIAN, this.personFactory.findById("Zandonai"));

        for (String title : new String[]{"Così fan tutte", "Don Giovanni", "Il Sogno di Scipione", "La Clemenza di Tito", "Le nozze di Figaro"}) {
            this.opusFactory.createOpus(title, OPERA, ITALIAN, mozart);
        }

        for (String title : new String[]{"La Flauta Mágica", "El rapto en el serrallo"}) {
            this.opusFactory.createOpus(title, OPERA, GERMAN, mozart);
        }

        for (String title : new String[]{"Gianni Schicchi", "Il Trittico", "La Bohème",
            "La Fanciulla del West", "La Rondine", "Madama Butterfly",
            "Manon Lescaut", "Tosca", "Turandot"}) {
            this.opusFactory.createOpus(title, OPERA, ITALIAN, puccini);
        }

        for (String title : new String[]{"Armida", "Il Barbiere di Siviglia", "Il turco in Italia",
            "L'Italiana in Algeri", "La Cenerentola", "La Donna del Lago", "Sigismondo",
            "Zelmira"}) {
            this.opusFactory.createOpus(title, OPERA, ITALIAN, rossini);
        }

        this.venueFactory.createVenue("ROH", this.cityFactory.findById("Londres"));
        this.venueFactory.createVenue("The Met", this.cityFactory.findById("New York"));
        this.venueFactory.createVenue("Glyndebourne", this.cityFactory.findById("Glyndebourne"));
        this.venueFactory.createVenue("ONP", this.cityFactory.findById("París"));
        this.venueFactory.createVenue("Royal Danish Theatre", this.cityFactory.findById("Copenhagen"));
        this.venueFactory.createVenue("ROH", this.cityFactory.findById("Londres"));
        this.venueFactory.createVenue("Palacio de las Artes Reina Sofía", this.cityFactory.findById("Valencia"));
        this.venueFactory.createVenue("Gran Teatro del Liceo de Barcelona", this.cityFactory.findById("Barcelona"));
        this.venueFactory.createVenue("alla Scala", this.cityFactory.findById("Milano"));
        this.venueFactory.createVenue("Festspielhaus Baden-Baden", this.cityFactory.findById("Baden-Baden"));
        this.venueFactory.createVenue("Sydney Opera House", this.cityFactory.findById("Sydney"));
        this.venueFactory.createVenue("Bolshói", this.cityFactory.findById("Moscú"));
        this.venueFactory.createVenue("Mariinski", this.cityFactory.findById("San Petersburgo"));
        this.venueFactory.createVenue("Ópera de Zürich", this.cityFactory.findById("Zürich"));
        this.venueFactory.createVenue("Teatro di San Carlo", this.cityFactory.findById("Nápoles"));

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

        StorageMedium bdr1 = this.storageFactory.createStorageMedium("Un disco", StorageMediumType.BDR);
        StorageBox box1 = this.storageBoxFactory.createStorageBox("A Box");
        bdr1.setContents(this.dcFactory.findAll());
        Set<StorageMedium> testSet = new HashSet<>();
        testSet.add(bdr1);
        box1.setMedia(testSet);

    }

    private void createOperaPerformanceMKV1080(String title, String theatre, String date) {
        try {
            this.dcFactory.createDigitalContent(
                    this.performanceFactory.createPerformance(
                            this.opusFactory.findById(new Pair<>(title, OPERA)),
                            this.venueFactory.findById(theatre),
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
        return this.storageBoxFactory.findAll();
    }

}
