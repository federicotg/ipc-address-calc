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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.fede.digitalcontent.dto.BoxLabelDTO;
import org.fede.digitalcontent.dto.DigitalContentDTO;
import org.fede.digitalcontent.dto.MediumContentDTO;
import org.fede.digitalcontent.dto.OpusDTO;
import org.fede.digitalcontent.dto.VenueDTO;
import org.fede.digitalcontent.dto.VenueDetailDTO;
import static org.fede.digitalcontent.model.Country.CHINA;
import static org.fede.util.Util.list;
import static org.fede.digitalcontent.model.Country.DENMARK;
import static org.fede.digitalcontent.model.Country.FRANCE;
import static org.fede.digitalcontent.model.Country.SWITZERLAND;
import static org.fede.digitalcontent.model.Country.AUSTRIA;
import static org.fede.digitalcontent.model.Country.NETHERLANDS;
import org.fede.digitalcontent.model.DigitalContent;
import org.fede.digitalcontent.model.DigitalContentRepository;
import org.fede.digitalcontent.model.StorageBox;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.fede.digitalcontent.model.Language;
import org.fede.digitalcontent.model.Opus;
import org.fede.digitalcontent.model.OpusType;
import static org.fede.digitalcontent.model.OpusType.BALLET;
import org.fede.digitalcontent.model.Pair;
import org.fede.digitalcontent.model.Performance;
import org.fede.digitalcontent.model.Person;
import org.fede.digitalcontent.model.Quality;
import org.fede.digitalcontent.model.Repository;
import org.fede.digitalcontent.model.StorageMedium;
import org.fede.digitalcontent.model.Venue;
import org.fede.util.Predicate;

/**
 *
 * @author fede
 */
@Service
@Lazy
public class LazyDigitalContentService implements DigitalContentService {

    protected static final Comparator<OpusDTO> OPUS_COMPARATOR = new Comparator<OpusDTO>() {

        @Override
        public int compare(OpusDTO o1, OpusDTO o2) {
            if (!o1.getType().equals(o2.getType())) {
                return o1.getType().compareTo(o2.getType());
            }
            return o1.getName().compareTo(o2.getName());
        }
    };

    @PostConstruct
    public void initBasicObjects() throws ParseException {

        this.initVenues();
        this.initBallets();
        this.initOperas();
        this.initBalletVideos();
        this.initOperaVideos();

    }

    private void initBallets() {
        new Opus.Builder("Giselle", "Le Corsaire").ballet().by("Adolphe Adam").build();
        new Opus.Builder("Raymonda").ballet().by("Alexander Glazunov").build();
        new Opus.Builder("Spartacus").ballet().by("Aram Khachaturian").build();
        new Opus.Builder("Le Palais de Cristal (Symphony in C)").ballet().by("Bizet").build();
        new Opus.Builder("La Esmeralda", "La Fille du Pharaon").ballet().by("Cesare Pugni").build();
        new Opus.Builder("Marco Spada").ballet().by("Daniel Auber").build();
        new Opus.Builder("The Bright Stream").ballet().by("Dmitri Shostakovich").build();
        new Opus.Builder("Jewels").ballet().by("Fauré").by("Stravinsky").by("Tchaikovsky").build();
        new Opus.Builder("La Sylphide").ballet().by("Jean-Madeleine Schneitzhoeffer.").by("Herman Severin Løvenskiold").build();
        new Opus.Builder("Alice's Adventures In Wonderland", "Winter's Tale").ballet().by("Joby Talbot").build();
        new Opus.Builder("Coppelia").ballet().by("Léo Delibes").build();
        new Opus.Builder("La Source").ballet().by("Leon Minkus").by("Léo Delibes").build();
        new Opus.Builder("Lost Illusions").ballet().by("Leonid Desyatnikov").build();
        new Opus.Builder("Don Quichotte", "La Bayadère").ballet().by("Ludwig Minkus").build();
        new Opus.Builder("Notre Dame de Paris").ballet().by("Maurice Jarre").build();
        new Opus.Builder("Daphnis et Chloé").ballet().by("Ravel").build();
        new Opus.Builder("Romeo and Juliet", "Cinderella").ballet().by("Sergey Prokofiev").build();
        new Opus.Builder("Swan Lake", "The Nutcracker", "The Sleeping Beauty").ballet()
                .by("Tchaikovsky").build();
        new Opus.Builder("La fille mal gardée").ballet().build();
        new Opus.Builder("Legend of Love").by("Arif Malikov").ballet().build();
        new Opus.Builder("Manon").by("Massenet").ballet().build();

        new Opus.Builder("La Valse").by("Ravel").ballet().build();

        new Opus.Builder("Meditation from Thaïs").by("Jules Massenet").ballet().build();

        new Opus.Builder("Voices of Spring").by("Johann Strauss").ballet().build();

        new Opus.Builder("Monotones").by("Erik Satie").ballet().build();
        new Opus.Builder("Marguerite and Armand").by("Franz Liszt").ballet().build();
        new Opus.Builder("Sylvia").by("Léo Delibes").ballet().build();
        new Opus.Builder("La Dame Aux Camelias").by("Chopin").ballet().build();
        new Opus.Builder("A Midsummer Night's Dream").by("Felix Mendelssohn").ballet().build();

        new Opus.Builder("La Creación").by("Joseph Haydn").ballet().build();
        new Opus.Builder("Ondine").by("Hans Werner Henze").ballet().build();

    }

    private void initVenues() {
        new Venue.Builder("Teatro di San Carlo").city("Nápoles").italy()
                .latLong(40.837556d, 14.2496541d)
                .build();
        new Venue.Builder("alla Scala").city("Milano").italy()
                .latLong(45.467402d, 9.189551d)
                .build();
        new Venue.Builder("Teatro Donizetti").city("Bergamo").italy()
                .latLong(45.695151d, 9.671143d)
                .build();
        new Venue.Builder("Teatro Comunale di Firenze").city("Florencia").italy()
                .latLong(43.7746958d, 11.2388874d)
                .build();
        new Venue.Builder("Teatro Comunale di Modena").city("Módena").italy()
                .latLong(44.646842d, 10.930176d)
                .build();

        new Venue.Builder("Teatro Comunale di Bologna").city("Bologna").italy()
                .latLong(44.49649d, 11.350486d)
                .build();

        new Venue.Builder("Teatro Rossini").city("Pesaro").italy()
                .latLong(43.907854d, 12.908755d)
                .build();
        new Venue.Builder("Arena di Verona").city("Verona").italy()
                .latLong(45.438996d, 10.994357d)
                .build();
        new Venue.Builder("Waldbühne").city("Berlín").germany()
                .latLong(52.5158333d, 13.2294058d)
                .build();
        new Venue.Builder("Festspielhaus Baden-Baden").city("Baden-Baden").germany()
                .latLong(48.76679d, 8.232d)
                .build();
        new Venue.Builder("Bayerische Staatsoper").city("Munich").germany()
                .latLong(48.139674d, 11.578718d)
                .build();
        new Venue.Builder("Halle Opera House").city("Halle").germany()
                .latLong(51.486731d, 11.971031d)
                .build();
        new Venue.Builder("Berlin State Opera").city("Berlín").germany()
                .latLong(52.516782d, 13.394691d)
                .build();
        new Venue.Builder("Anhaltisches Theater").city("Dessau").germany()
                .latLong(51.8365371d, 12.2374098d)
                .build();
        new Venue.Builder("Staatsoper Stuttgart").city("Stuttgart").germany()
                .latLong(48.780047d, 9.18472d)
                .build();
        new Venue.Builder("Palacio de las Artes Reina Sofía").city("Valencia").spain()
                .latLong(39.458065d, -0.355897d)
                .build();
        new Venue.Builder("Gran Teatro del Liceo").city("Barcelona").spain()
                .latLong(41.3801765d, 2.1733504d)
                .build();
        new Venue.Builder("Teatro Real").city("Madrid").spain()
                .latLong(40.418299d, -3.710578d)
                .build();
        new Venue.Builder("Bolshói").city("Moscú").russia()
                .latLong(55.760133d, 37.618649d)
                .wiki("https://es.wikipedia.org/wiki/Teatro_Bolsh%C3%B3i")
                .build();
        new Venue.Builder("Mariinsky").city("San Petersburgo").russia()
                .latLong(59.925645d, 30.295997d)
                .build();
        new Venue.Builder("ROH").city("Londres").uk()
                .latLong(51.512921d, -0.122198d)
                .build();
        new Venue.Builder("King's College").city("Cambridge").uk()
                .latLong(52.204278d, 0.116594d)
                .build();
        new Venue.Builder("Glyndebourne").city("Glyndebourne").uk()
                .latLong(50.878292d, 0.06417d)
                .wiki("https://en.wikipedia.org/wiki/Glyndebourne")
                .build();
        new Venue.Builder("The Met").city("New York").usa()
                .latLong(40.7728825d, -73.9841752d)
                .wiki("https://es.wikipedia.org/wiki/Metropolitan_Opera_House")
                .build();
        new Venue.Builder("War Memorial Opera House").city("San Francisco").usa()
                .latLong(37.778546d, -122.420976d)
                .build();
        new Venue.Builder("Musikverein").city("Viena").country(AUSTRIA)
                .latLong(48.2008d, 16.3729d)
                .build();
        new Venue.Builder("Wiener Staatsoper").city("Viena").country(AUSTRIA)
                .latLong(48.203332d, 16.369176d)
                .build();
        new Venue.Builder("Großes Festspielhaus").city("Salzburgo").country(AUSTRIA)
                .latLong(47.798534d, 13.041774d)
                .build();
        new Venue.Builder("Opéra Bastille").city("Paris").country(FRANCE)
                .latLong(48.852438d, 2.370203d)
                .wiki("https://es.wikipedia.org/wiki/%C3%93pera_de_la_Bastilla")
                .build();
        new Venue.Builder("Royal Danish Theatre").city("Copenhagen").country(DENMARK)
                .latLong(55.679459d, 12.586215d)
                .build();
        new Venue.Builder("Sydney Opera House").city("Sydney").australia()
                .latLong(-33.856898d, 151.215281d)
                .build();
        new Venue.Builder("Ópera de Zürich").city("Zürich").country(SWITZERLAND)
                .latLong(47.364946d, 8.546908d)
                .build();
        new Venue.Builder("Ciudad Prohibida").city("Beijing").country(CHINA)
                .latLong(39.9161804d, 116.3970584d)
                .build();

        new Venue.Builder("Aix-en-Provence").city("Aix-en-Provence").country(FRANCE)
                .latLong(43.5262164d, 5.4408571d)
                .build();

        new Venue.Builder("Stopera").city("Amsterdam").country(NETHERLANDS)
                .latLong(52.367493d, 4.901872d)
                .build();

        new Venue.Builder("Los Angeles Opera").city("Los Angeles").usa()
                .latLong(34.056553d, -118.248789d)
                .build();

        new Venue.Builder("Sadler's Wells").city("Londres").uk()
                .latLong(51.5292663d, -0.1062039d)
                .build();

        new Venue.Builder("Teatro Regio").city("Parma").italy()
                .latLong(44.803242d, 10.326742d)
                .build();

        new Venue.Builder("Opéra National du Rhin").city("Strasbourg").country(FRANCE)
                .latLong(48.585726d, 7.752371d)
                .build();

    }

    private void initOperas() {
        new Opus.Builder("Macbeth",
                "Aida",
                "Rigoletto",
                "Nabucco",
                "La Traviata",
                "Il Trovatore",
                "Simon Boccanegra",
                "Otello",
                "Un Ballo in Maschera",
                "Falstaff",
                "La Forza del Destino",
                "Don Carlo",
                "I Due Foscari")
                .italian().opera().by("Giuseppe Verdi").build();

        new Opus.Builder("Parsifal",
                "Rienzi",
                "Das Rheingold",
                "Die Walküre",
                "Gotterdammerung",
                "Siegfried",
                "Tannhauser",
                "Lohengrin",
                "Tristan und Ilsode")
                .german().opera().by("Richard Wagner").build();

        new Opus.Builder("Anna Bolena",
                "Don Pasquale",
                "L'elisir d'amore",
                "Lucia di Lammermoor",
                "Maria Stuarda")
                .italian().opera().by("Gaetano Donizetti").build();
        new Opus.Builder("La Fille du Regiment").french().opera().by("Gaetano Donizetti").build();

        new Opus.Builder("Fidelio").german().opera().by("Beethoven").build();

        new Opus.Builder("Carmen").french().opera().by("Bizet").build();
        new Opus.Builder("Les Troyens").french().opera().by("Hector Berlioz").build();
        new Opus.Builder("I Puritani", "Norma").italian().opera().by("Vincenzo Bellini").build();
        new Opus.Builder("The Nose").russian().opera().by("Dmitri Shostakovich").build();
        new Opus.Builder("Lakme").french().opera().by("Léo Delibes").build();
        new Opus.Builder("Werther").french().opera().by("Jules Massenet").build();
        new Opus.Builder("Prince Igor").russian().opera().by("Borodin").build();
        new Opus.Builder("Messiah", "Theodora").english().oratorio().by("Händel").build();
        new Opus.Builder("Francesca da Rimini").italian().opera().by("Zandonai").build();
        new Opus.Builder("Rusalka").language(Language.CZECH).opera().by("Dvorak").build();
        new Opus.Builder("L'Orfeo").italian().opera().by("Monteverdi").build();
        new Opus.Builder("Pagliacci").italian().opera().by("Leoncavallo").build();
        new Opus.Builder("The Cunning Little Vixen").language(Language.CZECH).opera().by("Leoš Janáček").build();
        new Opus.Builder("Manon").french().opera().by("Massenet").build();
        new Opus.Builder("Hippolyte et Aricie").french().opera().by("Rameau").build();
        new Opus.Builder("L'enfant et les sortilèges", "L'heure espagnole")
                .french().opera().by("Ravel").build();
        new Opus.Builder("Ariadne auf Naxos", "Capriccio", "Intermezzo", "Salome", "Daphne", "Elektra")
                .german().opera().by("Richard Strauss").build();
        new Opus.Builder("Cherevichki The Tsarina's Slippers", "Eugene Onegin", "Iolanta")
                .russian().opera().by("Tchaikovsky").build();
        new Opus.Builder("Orlando Furioso").italian().opera().by("Vivaldi").build();
        new Opus.Builder("Nixon in China", "Doctor Atomic").english().opera().by("John Adams").build();
        new Opus.Builder("Faust", "Roméo et Juliette").french().opera().by("Gounod").build();
        new Opus.Builder("La Gioconda").french().opera().by("A. Ponchielli").build();
        new Opus.Builder("Le Grand Macabre").german().opera().by("Ligeti").build();

        new Opus.Builder("Admeto",
                "Alcina",
                "Giulio Cesare",
                "Rinaldo",
                "Rodelinda",
                "Tamerlano")
                .italian().opera().by("Händel").build();

        new Opus.Builder("Così Fan Tutte",
                "Don Giovanni",
                "Il Sogno di Scipione",
                "La Clemenza di Tito",
                "Le nozze di Figaro")
                .italian().opera().by("W. A. Mozart").build();

        new Opus.Builder("La Flauta Mágica",
                "El rapto en el serrallo")
                .german().opera().by("W. A. Mozart").build();

        new Opus.Builder("Gianni Schicchi",
                "Il Trittico",
                "La Bohème",
                "La Fanciulla del West",
                "La Rondine",
                "Madama Butterfly",
                "Manon Lescaut",
                "Tosca",
                "Turandot")
                .italian().opera().by("Giacomo Puccini").build();

        new Opus.Builder("Le comte Ory").french().opera().by("Rossini").build();

        new Opus.Builder("Armida",
                "Il Barbiere di Siviglia",
                "Il turco in Italia",
                "L'Italiana in Algeri",
                "La Cenerentola",
                "La Donna del Lago",
                "Sigismondo",
                "Otello ossia Il moro di Venezia",
                "Zelmira")
                .italian().opera().by("Rossini").build();

        new Opus.Builder("Boris Godunov").russian().opera().by("Mussorgsky").build();

        new Opus.Builder("Cavalleria Rusticana").italian().opera().by("Mascagni").build();

        new Opus.Builder("Ascenso y caída de la ciudad de Mahagonny").german().opera().by("Kurt Weill").build();

        new Opus.Builder("Andrea Chénier").german().opera().by("Umberto Giordano").build();

        new Opus.Builder("Les contes d'Hoffmann").french().opera().by("Offenbach").build();
        
        new Opus.Builder("El castillo de Barbazul").language(Language.HUNGARIAN).opera().by("Bartók").build();

    }

    private void initBalletVideos() throws ParseException {
        new DigitalContent.Builder("Giselle").ballet().atRoh().on("29/04/2006").hd720().mkv()
                .seenByAnaMaria()
                .seenByElsa()
                .discBox(2, 1)
                .imdb("http://www.imdb.com/title/tt1596783/")
                .build();
        new DigitalContent.Builder("Giselle").ballet().atRoh().on("27/01/2014").fullHD().mkv().discBox(7, 15).build();
        new DigitalContent.Builder("Le Corsaire").ballet().atBolshoi().on("11/03/2012").hd720().mkv().discBox(9, 3)
                .imdb("http://www.imdb.com/title/tt2798336/")
                .seenByAnaMaria()
                .seenByFede()
                .build();
        new DigitalContent.Builder("Raymonda").ballet().atBolshoi().on("24/06/2012").hd720().mkv()
                .discBox(6, 12)
                .discBox(8, 12)
                .discBox(11, 12)
                .seenByFede()
                .imdb("http://www.imdb.com/title/tt2798390/")
                .build();
        new DigitalContent.Builder("Spartacus").ballet().atBolshoi().on("01/07/2008").hd720().mkv().discBox(8, 3)
                .imdb("http://www.imdb.com/title/tt1890538/")
                .seenByAnaMaria()
                .build();
        new DigitalContent.Builder("Spartacus").ballet().atBolshoi().on("20/10/2013").fullHD().mkv()
                .discBox(4, 11)
                .seenByAnaMaria()
                .build();
        new DigitalContent.Builder("Le Palais de Cristal (Symphony in C)").ballet().atParis().on("06/03/2014").fullHD().mkv()
                .discBox(6, 4)
                .build();
        new DigitalContent.Builder("La Esmeralda").ballet().atBolshoi().on("09/10/2011").hd720().mkv()
                .discBox(2, 11)
                .discBox(3, 11)
                .imdb("http://www.imdb.com/title/tt2798094/")
                .build();
        new DigitalContent.Builder("La Fille du Pharaon").ballet().atBolshoi().on("24/11/2012").fullHD().mkv()
                .discBox(10, 11)
                .discBox(9, 12)
                .discBox(10, 12)
                .build();
        new DigitalContent.Builder("Marco Spada").ballet().atBolshoi().on("30/03/2014").fullHD().mkv()
                .discBox(9, 4)
                .discBox(10, 15)
                .build();
        new DigitalContent.Builder("The Bright Stream").ballet().atBolshoi().on("29/04/2012").hd720().mkv()
                .discBox(3, 6)
                .seenByFede()
                .imdb("http://www.imdb.com/title/tt2798354/")
                .build();
        new DigitalContent.Builder("Jewels").ballet().atBolshoi().on("19/01/2014").fullHD().mkv()
                .discBox(4, 15)
                .seenByAnaMaria()
                .build();
        new DigitalContent.Builder("La Sylphide").ballet().atBolshoi().on("29/09/2012").hd720().mkv()
                .discBox(7, 6)
                .seenByAnaMaria()
                .build();
        new DigitalContent.Builder("Alice's Adventures In Wonderland")
                .ballet().atRoh().on("28/03/2013").fullHD().mkv()
                .seenByAnaMaria()
                .seenByElsa()
                .discBox(12, 11)
                .discBox(3, 12)
                .discBox(4, 12)
                .build();
        new DigitalContent.Builder("Winter's Tale").ballet().atRoh().on("28/04/2014").fullHD().mkv()
                .seenByAnaMaria()
                .discBox(9, 4)
                .discBox(8, 4)
                .build();
        new DigitalContent.Builder("Coppelia").ballet().atBolshoi().on("29/05/2011").hd720().mkv()
                .discBox(7, 3)
                .seenByAnaMaria()
                .seenByElsa()
                .imdb("http://www.imdb.com/title/tt1833535/")
                .build();
        new DigitalContent.Builder("La Source").ballet().atParis().on("11/04/2011").hd720().mkv().discBox(4, 7).build();
        new DigitalContent.Builder("Lost Illusions").ballet().atBolshoi().on("20/10/2013").fullHD().mkv().discBox(4, 15)
                .seenByAnaMaria()
                .build();
        new DigitalContent.Builder("Don Quichotte").ballet().atParis().on("18/12/2012").fullHD().mkv()
                .discBox(1, 11)
                .discBox(2, 11)
                .seenByFede()
                .build();
        new DigitalContent.Builder("Don Quichotte").ballet().atRoh().on("16/10/2013").fullHD().mkv().discBox(6, 15)
                .seenByAnaMaria()
                .build();
        new DigitalContent.Builder("La Bayadère").ballet().atParis().on("22/03/2012").hd720().mkv()
                .discBox(2, 11)
                .discBox(3, 11)
                .seenByAnaMaria()
                .seenByElsa()
                .build();
        new DigitalContent.Builder("La Bayadère").ballet().atBolshoi().on("27/01/2013").fullHD().mkv()
                .discBox(11, 14)
                .seenByAnaMaria().build();

        new DigitalContent.Builder("La Bayadère").ballet().atMariinsky().on("27/01/2013").fullHD().mkv()
                .seenByAnaMaria()
                .discBox(2, 16)
                .discBox(1, 3)
                .build();

        new DigitalContent.Builder("Notre Dame de Paris").ballet().atBolshoi().on("14/02/2013").fullHD().mkv()
                .discBox(2, 13)
                .build();
        new DigitalContent.Builder("Daphnis et Chloé").ballet().atBolshoi().on("06/03/2014").fullHD().mkv().discBox(10, 15).build();
        new DigitalContent.Builder("Romeo and Juliet").ballet().atRoh().on("10/01/2012").fullHD().mkv()
                .discBox(9, 1)
                .discBox(8, 1)
                .seenByElsa()
                .seenByAnaMaria()
                .build();
        new DigitalContent.Builder("Romeo and Juliet").ballet().atBolshoi().on("12/05/2013").fullHD().mkv()
                .discBox(8, 1)
                .discBox(11, 14)
                .build();
        new DigitalContent.Builder("Swan Lake").ballet().atBolshoi().on("19/06/2011").hd720().mkv()
                .discBox(8, 2)
                .discBox(9, 2)
                .seenByFede()
                .seenByElsa()
                .build();
        new DigitalContent.Builder("Swan Lake").ballet().atMariinsky().on("06/06/2013").fullHD().mkv()
                .discBox(6, 13)
                .discBox(12, 13)
                .discBox(4, 14)
                .seenByAnaMaria()
                .build();
        new DigitalContent.Builder("The Nutcracker").ballet().atBolshoi().on("18/12/2011").hd720().mkv()
                .discBox(7, 3)
                .imdb("http://www.imdb.com/title/tt2798230/")
                .seenByAnaMaria()
                .seenByElsa()
                .build();
        new DigitalContent.Builder("The Nutcracker").ballet().atRoh().on("12/12/2013").fullHD().mkv()
                .discBox(5, 15)
                .seenByAnaMaria()
                .seenByFede()
                .build();

        new DigitalContent.Builder("The Nutcracker").ballet().atMariinsky().on("01/01/2012").fullHD().mkv()
                .discBox(8, 5)
                .build();

        new DigitalContent.Builder("The Sleeping Beauty").ballet().atBolshoi().on("20/11/2011").hd720().mkv().discBox(4, 5)
                .imdb("http://www.imdb.com/title/tt2798186/")
                .build();
        new DigitalContent.Builder("The Sleeping Beauty").ballet().atRoh().on("15/12/2011").hd720().mkv()
                .discBox(7, 2)
                .discBox(9, 2)
                .discBox(8, 2)
                .discBox(2, 3)
                .build();
        new DigitalContent.Builder("The Sleeping Beauty").ballet().atRoh().on("19/03/2014").fullHD().mkv().discBox(10, 4)
                .seenByAnaMaria()
                .seenByFede()
                .seenByElsa()
                .build();
        new DigitalContent.Builder("La fille mal gardée").ballet().atRoh().on("16/05/2012").hd720().mkv().discBox(8, 3)
                .seenByFede()
                .build();
        new DigitalContent.Builder("Legend of Love").ballet().atBolshoi().on("26/10/2014").fullHD().mkv()
                .discBox(8, 2)
                .discBox(10, 2)
                .build();
        new DigitalContent.Builder("Don Quichotte").ballet().atAllaScala().on("25/09/2014").engSubs().mkv().fullHD()
                .discBox(7, 1)
                .discBox(9, 1)
                .build();

        new DigitalContent.Builder("Swan Lake").ballet().atZurich().on("05/12/2010").mkv().hd720()
                .imdb("http://www.imdb.com/title/tt1754561/")
                .discBox(6, 1)
                .build();

        new DigitalContent.Builder("Cinderella").ballet().at("Stopera").on("01/01/2012").mkv().fullHD()
                .discBox(8, 1)
                .build();
        new DigitalContent.Builder("Cinderella").ballet().atParis().on("01/01/2007").mkv().fullHD()
                .discBox(1, 16)
                .build();

        Venue roh = Repository.VENUE.findById("ROH");
        Opus laValse = Repository.OPUS.findById(new Pair<>("La Valse", BALLET));
        Opus monotones = Repository.OPUS.findById(new Pair<>("Monotones", BALLET));
        Opus mAndA = Repository.OPUS.findById(new Pair<>("Marguerite and Armand", BALLET));
        Opus meditations = Repository.OPUS.findById(new Pair<>("Meditation from Thaïs", BALLET));
        Opus voices = Repository.OPUS.findById(new Pair<>("Voices of Spring", BALLET));

        Date date = new SimpleDateFormat("dd/MM/yyyy").parse("01/02/2013");

        Set<Performance> ashtonCelebration = new HashSet<>();

        ashtonCelebration.add(new Performance(laValse, roh, date));
        ashtonCelebration.add(new Performance(monotones, roh, date));
        ashtonCelebration.add(new Performance(meditations, roh, date));
        ashtonCelebration.add(new Performance(voices, roh, date));
        ashtonCelebration.add(new Performance(mAndA, roh, date));

        new DigitalContent.Builder(ashtonCelebration).fullHD().discBox(2, 16).mkv().build();

        new DigitalContent.Builder("Sylvia").ballet().atRoh().on("01/01/2005").mkv().fullHD()
                .discBox(6, 5)
                .build();

        new DigitalContent.Builder("The Nutcracker").ballet().atBolshoi().on("21/12/2014").mkv().fullHD()
                .discBox(3, 16)
                .seenByFede()
                .seenByAnaMaria()
                .build();

        new DigitalContent.Builder("Coppelia").ballet().atParis().on("01/01/2011").mkv().fullHD()
                .discBox(8, 5)
                .build();

        new DigitalContent.Builder("La Dame Aux Camelias").ballet().atParis().on("01/07/2008").mkv().fullHD()
                .discBox(0, 0) // parte 1
                .discBox(5, 1) // parte 2 
                .build();

        new DigitalContent.Builder("A Midsummer Night's Dream").ballet().at("Sadler's Wells").on("01/02/1999").fullHD().mkv()
                .discBox(5, 16)
                .build();

        new DigitalContent.Builder("La Creación").ballet().at("Opéra National du Rhin").on("29/03/2014").fullHD().mkv()
                .discBox(5, 16)
                .build();

        new DigitalContent.Builder("Ondine").ballet().atRoh().on("01/01/2009").fullHD().mkv()
                .discBox(3, 3) // parte 3
                .discBox(11, 16)
                .build();

    }

    private void initOperaVideos() throws ParseException {

        new DigitalContent.Builder("La Gioconda").opera().atParis().on("13/05/2013").fullHD().spaSubs().mkv().seenByFede()
                .discBox(1, 14)
                .discBox(2, 14)
                .discBox(5, 14)
                .build();
        new DigitalContent.Builder("Rusalka").opera().atTheMet().on("08/02/2014").fullHD().engSubs().mkv().discBox(9, 15)
                .starringFleming()
                .imdb("http://www.imdb.com/title/tt3451542/").build();
        new DigitalContent.Builder("I Puritani").opera().atTheMet().on("01/01/2007").dvd().spaSubs().iso()
                .discBox(12, 14)
                .imdb("http://www.imdb.com/title/tt0966101/").build();
        new DigitalContent.Builder("I Puritani").opera().atParis().on("09/12/2013").fullHD().spaSubs().mkv().seenByFede()
                .discBox(5, 11)
                .build();
        new DigitalContent.Builder("Norma").opera().at("Bayerische Staatsoper").on("01/01/2007").dvd().spaSubs().iso()
                .discBox(4, 3) // vol 1
                .discBox(3, 3) // vol 2
                .build();
        new DigitalContent.Builder("Faust").opera().atRoh().on("01/01/2004").dvd().spaSubs().iso().discBox(2, 5).discBox(3, 5)
                .imdb("http://www.imdb.com/title/tt1954436/")
                .seenByFede()
                .build();
        new DigitalContent.Builder("Carmen").opera().atTheMet().on("16/01/2010").dvd().spaSubs().iso().seenByFede()
                .discBox(2, 1)
                .imdb("http://www.imdb.com/title/tt1669004/").build();
        new DigitalContent.Builder("Prince Igor").opera().atTheMet().on("01/03/2014").fullHD().spaSubs().mkv().discBox(3, 15)
                .imdb("http://www.imdb.com/title/tt3565332/").build();
        new DigitalContent.Builder("The Nose").opera().atTheMet().on("26/10/2013").fullHD().engSubs().mkv().discBox(6, 15)
                .imdb("http://www.imdb.com/title/tt3114272/").build();
        new DigitalContent.Builder("Anna Bolena").opera().at("Wiener Staatsoper").on("01/01/2011").dvd().spaSubs().iso()
                .discBox(1, 7)
                .discBox(2, 7)
                .starringNetrebko()
                .imdb("http://www.imdb.com/title/tt1878854/").build();
        new DigitalContent.Builder("Don Pasquale").opera().atGlyndebourne().on("06/08/2013").fullHD().engSubs().mkv()
                .discBox(4, 11)
                .build();
        new DigitalContent.Builder("Don Pasquale").opera().atAllaScala().on("01/01/1994").dvd().spaSubs().iso().discBox(1, 15).build();
        new DigitalContent.Builder("L'elisir d'amore").opera().atTheMet().on("13/10/2012").fullHD().engSubs().mkv()
                .starringNetrebko()
                .seenByFede()
                .discBox(11, 11)
                .discBox(8, 12)
                .discBox(10, 12)
                .imdb("http://www.imdb.com/title/tt2303779/").build();
        new DigitalContent.Builder("La Fille du Regiment").opera().atRoh().on("01/01/2007").dvd().spaSubs().iso()
                .discBox(5, 13)
                .imdb("http://www.imdb.com/title/tt1320091/").build();
        new DigitalContent.Builder("Lucia di Lammermoor").opera().at("Teatro Donizetti").on("01/01/2006").dvd().spaSubs().iso().seenByFede()
                .discBox(3, 7)
                .build();
        new DigitalContent.Builder("Maria Stuarda").opera().atTheMet().on("19/01/2013").fullHD().engSubs().mkv().seenByFede()
                .discBox(8, 7)
                .discBox(2, 12)
                .imdb("http://www.imdb.com/title/tt2651570/").build();
        new DigitalContent.Builder("Faust").opera().atTheMet().on("10/12/2011").fullHD().engSubs().mkv()
                .discBox(10, 13)
                .imdb("http://www.imdb.com/title/tt1878497/").build();
        new DigitalContent.Builder("Aida").opera().atAllaScala().on("01/01/2006").dvd().spaSubs().iso()
                .discBox(3, 2)
                .discBox(4, 2)
                .imdb("http://www.imdb.com/title/tt1399162/").build();
        new DigitalContent.Builder("Aida").opera().atTheMet().on("15/12/2012").fullHD().engSubs().mkv()
                .discBox(6, 12)
                .discBox(9, 12)
                .imdb("http://www.imdb.com/title/tt2573426/").build();
        new DigitalContent.Builder("Don Carlo").opera().atRoh().on("01/01/2008").dvd().engSubs().mkv().discBox(7, 2)
                .imdb("http://www.imdb.com/title/tt2074354/").build();
        new DigitalContent.Builder("Falstaff").opera().at("Teatro Comunale di Firenze").on("01/01/2006").dvd().spaSubs().iso().seenByFede().discBox(2, 2)
                .imdb("http://www.imdb.com/title/tt2199446/").build();
        new DigitalContent.Builder("Falstaff").opera().atGlyndebourne().on("01/01/2009").fullHD().engSubs().mkv()
                .discBox(6, 14)
                .discBox(8, 14)
                .build();
        new DigitalContent.Builder("Falstaff").opera().atTheMet().on("15/12/2013").fullHD().engSubs().mkv()
                .discBox(5, 11)
                .imdb("http://www.imdb.com/title/tt3385804/").build();
        new DigitalContent.Builder("I Due Foscari").opera().at("Teatro di San Carlo").on("01/01/2001").dvd().spaSubs().iso().discBox(1, 6)
                .imdb("http://www.imdb.com/title/tt0401393/").build();

        new DigitalContent.Builder("La Forza del Destino").opera().at("Teatro Comunale di Firenze").on("01/01/2007").dvd().spaSubs().iso()
                .discBox(12, 14)
                .imdb("http://www.imdb.com/title/tt2196658/").build();
        new DigitalContent.Builder("La Traviata").opera().at("Großes Festspielhaus").on("01/01/2005").dvd().spaSubs().iso()
                .seenByFede()
                .discBox(2, 4)
                .starringNetrebko()
                .imdb("http://www.imdb.com/title/tt0475165/").build();
        new DigitalContent.Builder("La Traviata").opera().atAllaScala().on("01/01/2007").hd720().mkv()
                .discBox(3, 14)
                .imdb("http://www.imdb.com/title/tt2316799/").build();
        new DigitalContent.Builder("La Traviata").opera().atParis().on("17/06/2014").fullHD().spaSubs().mkv()
                .discBox(7, 4)
                .discBox(8, 4)
                .discBox(11, 15)
                .build();
        new DigitalContent.Builder("La Traviata").opera().atGlyndebourne().on("10/08/2014").fullHD().engSubs().mkv().seenByFede().discBox(12, 15).build();

        new DigitalContent.Builder("La Traviata").opera().atRoh().on("17/06/2009").hd720().spaSubs().mkv()
                .starringFleming()
                .discBox(5, 5).build();

        new DigitalContent.Builder("Macbeth").opera().atParis().on("04/04/2009").fullHD().spaSubs().mkv().discBox(9, 15)
                .imdb("http://www.imdb.com/title/tt2705154/").build();
        new DigitalContent.Builder("Macbeth").opera().atTheMet().on("11/10/2014").fullHD()
                .starringNetrebko()
                .spaSubs().mkv()
                .discBox(10, 2)
                .build();
        new DigitalContent.Builder("Nabucco").opera().atRoh().on("26/04/2013").fullHD().spaSubs().mkv().seenByFede()
                .discBox(3, 14)
                .discBox(4, 14)
                .discBox(6, 14)
                .build();
        new DigitalContent.Builder("Otello").opera().at("Gran Teatro del Liceo").on("18/02/2006").fullHD().spaSubs().mkv().discBox(1, 4).build();
        new DigitalContent.Builder("Otello").opera().atTheMet().on("28/10/2012").fullHD().engSubs().mkv()
                .discBox(7, 11)
                .discBox(9, 12)
                .discBox(11, 12)
                .imdb("http://www.imdb.com/title/tt2395995/").build();
        new DigitalContent.Builder("Otello").opera().at("Teatro di San Carlo").on("22/04/2014").fullHD().engSubs().mkv()
                .discBox(7, 4)
                .discBox(8, 4)
                .build();
        new DigitalContent.Builder("Rigoletto").opera().atZurich().on("01/01/2006").fullHD().spaSubs().mkv()
                .discBox(10, 14)
                .imdb("http://www.imdb.com/title/tt2289502/").build();
        new DigitalContent.Builder("Rigoletto").opera().atTheMet().on("16/02/2013").fullHD().engSubs().mkv().seenByFede()
                .discBox(1, 12)
                .discBox(7, 12)
                .discBox(10, 12)
                .imdb("http://www.imdb.com/title/tt2710572/").build();
        new DigitalContent.Builder("Simon Boccanegra").opera().atRoh().on("01/01/2010").dvd().spaSubs().iso()
                .discBox(1, 13)
                .discBox(2, 13)
                .imdb("http://www.imdb.com/title/tt1691418/").build();
        new DigitalContent.Builder("Un Ballo in Maschera").opera().atTheMet().on("08/12/2012").fullHD().engSubs().mkv()
                .discBox(8, 12)
                .discBox(9, 12)
                .imdb("http://www.imdb.com/title/tt2560540/").build();
        new DigitalContent.Builder("Admeto").opera().at("Halle Opera House").on("01/01/2006").dvd().spaSubs().iso()
                .discBox(8, 14)
                .discBox(9, 14)
                .imdb("http://www.imdb.com/title/tt2651146/").build();
        new DigitalContent.Builder("Alcina").opera().at("Staatsoper Stuttgart").on("01/01/1999").dvd().spaSubs().mkv().discBox(8, 4)
                .imdb("http://www.imdb.com/title/tt0354364/").build();
        new DigitalContent.Builder("Giulio Cesare").opera().atParis().on("01/01/2011").dvd().spaSubs().iso()
                .seenByFede()
                .discBox(6, 13)
                .discBox(7, 13)
                .build();
        new DigitalContent.Builder("Messiah").oratorio().atZurich().on("01/01/2009").fullHD().spaSubs().mkv().seenByFede()
                .discBox(8, 15)
                .imdb("http://www.imdb.com/title/tt1414536/").build();
        new DigitalContent.Builder("Messiah").oratorio().at("King's College").on("01/01/2009").dvd().spaSubs().iso().seenByFede()
                .discBox(3, 12)
                .discBox(7, 11)
                .build();
        new DigitalContent.Builder("Rinaldo").opera().at("Bayerische Staatsoper").on("01/01/2001").dvd().spaSubs().iso()
                .discBox(7, 6)
                .discBox(8, 6)
                .imdb("http://www.imdb.com/title/tt0345861/").build();
        new DigitalContent.Builder("Rodelinda").opera().atGlyndebourne().on("01/01/1998").dvd().spaSubs().iso().discBox(2, 6)
                .imdb("http://www.imdb.com/title/tt0255555/").build();
        new DigitalContent.Builder("Tamerlano").opera().at("Halle Opera House").on("01/01/2001").dvd().spaSubs().iso()
                .discBox(1, 12)
                .discBox(2, 12)
                .build();
        new DigitalContent.Builder("Theodora").oratorio().atGlyndebourne().on("01/01/1996").dvd().engSubs().mkv()
                .discBox(12, 15)
                .imdb("http://www.imdb.com/title/tt0253802/")
                .build();
        new DigitalContent.Builder("Les Troyens").opera().atTheMet().on("05/01/2013").fullHD().engSubs().mkv()
                .discBox(11, 11)
                .discBox(5, 12)
                .discBox(7, 12)
                .imdb("http://www.imdb.com/title/tt2661764/").build();
        new DigitalContent.Builder("Nixon in China").opera().atTheMet().on("01/01/2011").dvd().spaSubs().iso().discBox(4, 4)
                .imdb("http://www.imdb.com/title/tt1736150/").build();
        new DigitalContent.Builder("Manon").opera().atTheMet().on("07/04/2012").fullHD().engSubs().mkv().seenByFede()
                .discBox(6, 11).imdb("http://www.imdb.com/title/tt2239809/").build();
        new DigitalContent.Builder("Werther").opera().atTheMet().on("15/03/2014").fullHD().spaSubs().mkv()
                .discBox(11, 15)
                .discBox(9, 4)
                .imdb("http://www.imdb.com/title/tt3599730/").build();
        new DigitalContent.Builder("Lakme").opera().at("Sydney Opera House").on("01/01/2011").fullHD().spaSubs().mkv().discBox(3, 15)
                .imdb("http://www.imdb.com/title/tt2757396/").build();
        new DigitalContent.Builder("Pagliacci").opera().at("Arena di Verona").on("01/01/2006").dvd().spaSubs().iso().seenByFede()
                .discBox(1, 9)
                .build();
        new DigitalContent.Builder("The Cunning Little Vixen").opera().atGlyndebourne().on("10/06/2012").fullHD().engSubs().mkv().discBox(4, 7).build();
        new DigitalContent.Builder("Manon").opera().at("Berlin State Opera").on("01/01/2007").dvd().spaSubs().iso().discBox(5, 2)
                .discBox(6, 2)
                .imdb("http://www.imdb.com/title/tt1431725/").build();
        new DigitalContent.Builder("L'Orfeo").opera().atZurich().on("01/01/2007").dvd().spaSubs().iso().discBox(2, 15).build();
        new DigitalContent.Builder("Così Fan Tutte").opera().atTheMet().on("26/04/2014").fullHD().spaSubs().mkv()
                .seenByFede()
                .discBox(6, 4).build();
        new DigitalContent.Builder("Così Fan Tutte").opera().at("Wiener Staatsoper").on("01/01/1996").dvd().spaSubs().iso().seenByFede()
                .discBox(4, 9)
                .discBox(5, 9)
                .imdb("http://www.imdb.com/title/tt0428432/").build();
        new DigitalContent.Builder("Don Giovanni").opera().at("Großes Festspielhaus").on("01/01/2006").dvd().spaSubs().iso()
                .discBox(5, 7)
                .discBox(6, 7)
                .imdb("http://www.imdb.com/title/tt0864960/").build();
        new DigitalContent.Builder("Don Giovanni").opera().atRoh().on("01/01/2008").fullHD().engSubs().mkv().seenByFede()
                .discBox(8, 2)
                .discBox(2, 3)
                .imdb("http://www.imdb.com/title/tt2374452/").build();
        new DigitalContent.Builder("El rapto en el serrallo").opera().at("Bayerische Staatsoper").on("01/01/1980").fullHD().spaSubs().mkv()
                .discBox(4, 11)
                .imdb("http://www.imdb.com/title/tt0254303/").build();
        new DigitalContent.Builder("Il Sogno di Scipione").opera().at("Großes Festspielhaus").on("01/01/2006").dvd().spaSubs().iso().seenByFede().discBox(10, 3)
                .imdb("http://www.imdb.com/title/tt1039909/").build();
        new DigitalContent.Builder("La Clemenza di Tito").opera().atTheMet().on("01/12/2012").fullHD().engSubs().mkv().seenByFede()
                .discBox(8, 11)
                .discBox(9, 11)
                .imdb("http://www.imdb.com/title/tt2367900/").build();
        new DigitalContent.Builder("La Flauta Mágica").opera().at("Großes Festspielhaus").on("01/08/2012").fullHD().spaSubs().mkv().seenByFede()
                .discBox(7, 7)
                .build();
        new DigitalContent.Builder("Le nozze di Figaro").opera().atGlyndebourne().on("01/01/1999").dvd().engSubs().mkv()
                .starringFleming()
                .discBox(11, 14)
                .build();
        new DigitalContent.Builder("Le nozze di Figaro").opera().atRoh().on("01/01/2006").fullHD().spaSubs().mkv().seenByFede()
                .discBox(3, 1)
                .discBox(4, 1)
                .imdb("http://www.imdb.com/title/tt1229379/").build();
        new DigitalContent.Builder("Le nozze di Figaro").opera().at("Großes Festspielhaus").on("01/01/2006").dvd().spaSubs().mkv()
                .discBox(11, 14)
                .imdb("http://www.imdb.com/title/tt0838187/").build();
        new DigitalContent.Builder("Le nozze di Figaro").opera().atGlyndebourne().on("17/08/2012").fullHD().engSubs().mkv()
                .discBox(8, 14)
                .build();
        
        new DigitalContent.Builder("La Flauta Mágica").opera().atRoh().on("01/01/2003").dvd().spaSubs().iso().seenByFede()
                .discBox(10, 1)
                .imdb("http://www.imdb.com/title/tt0383058/").build();
        
        new DigitalContent.Builder("Gianni Schicchi").opera().atGlyndebourne().on("01/01/2004").dvd().spaSubs().iso().seenByFede()
                .discBox(1, 2)
                .build();
        new DigitalContent.Builder("Il Trittico").opera().at("Teatro Comunale di Modena").on("01/01/2007").dvd().spaSubs().iso().discBox(2, 15).build();
        new DigitalContent.Builder("La Bohème").opera().at("Teatro Real").on("01/01/2006").dvd().spaSubs().iso().seenByFede()
                .discBox(2, 1)
                .build();
        new DigitalContent.Builder("La Bohème").opera().atRoh().on("15/01/2013").fullHD().spaSubs().mkv().discBox(3, 4).build();
        new DigitalContent.Builder("La Fanciulla del West").opera().atParis().on("10/02/2014").fullHD().spaSubs().mkv().discBox(11, 15).discBox(12, 15).build();
        new DigitalContent.Builder("La Rondine").opera().atTheMet().on("01/01/2009").dvd().spaSubs().iso().seenByFede().discBox(8, 8)
                .imdb("http://www.imdb.com/title/tt1347918/").build();
        new DigitalContent.Builder("Madama Butterfly").opera().at("Arena di Verona").on("01/01/2004").dvd().spaSubs().iso().discBox(1, 1).build();
        new DigitalContent.Builder("Manon Lescaut").opera().atRoh().on("01/01/1983").dvd().spaSubs().iso().discBox(2, 15)
                .imdb("http://www.imdb.com/title/tt0254549/").build();
        new DigitalContent.Builder("Tosca").opera().atAllaScala().on("01/01/2000").dvd().spaSubs().iso().discBox(3, 8)
                .imdb("http://www.imdb.com/title/tt2385259/").build();
        new DigitalContent.Builder("Tosca").opera().atRoh().on("17/07/2011").dvd().engSubs().mkv().seenByFede().discBox(6, 2).build();
        new DigitalContent.Builder("Tosca").opera().atTheMet().on("09/11/2013").fullHD().engSubs().mkv().discBox(5, 15)
                .imdb("http://www.imdb.com/title/tt3315900/")
                .seenByFede()
                .build();
        new DigitalContent.Builder("Tosca").opera().atParis().on("16/10/2014").fullHD().spaSubs().mkv().discBox(8, 2).build();
        //new DigitalContent.Builder("Turandot").opera().atTheMet().on("01/01/1987").fullHD().spaSubs().mkv().discBox(3, 13).imdb("http://www.imdb.com/title/tt0220822/").build();
        new DigitalContent.Builder("Turandot").opera().at("Ciudad Prohibida").on("01/01/1999").dvd().spaSubs().mkv().discBox(5, 2)
                .imdb("http://www.imdb.com/title/tt0205483/").build();

        new DigitalContent.Builder("L'enfant et les sortilèges").opera().atGlyndebourne().on("19/08/2012").fullHD().engSubs().mkv().seenByFede().discBox(3, 6).build();
        new DigitalContent.Builder("L'heure espagnole").opera().atGlyndebourne().on("19/08/2012").fullHD().engSubs().mkv().seenByFede().discBox(3, 6).build();
        new DigitalContent.Builder("Ariadne auf Naxos").opera().atGlyndebourne().on("04/06/2013").fullHD().engSubs().mkv().seenByFede()
                .discBox(7, 14)
                .discBox(10, 14)
                .build();
        new DigitalContent.Builder("Capriccio").opera().atParis().on("01/01/2004").dvd().spaSubs().iso()
                .discBox(8, 5)
                .imdb("http://www.imdb.com/title/tt2353871/").build();
        new DigitalContent.Builder("Intermezzo").opera().atGlyndebourne().on("01/01/1983").fullHD().engSubs().iso()
                .discBox(7, 9)
                .imdb("http://www.imdb.com/title/tt0240617/").build();
        new DigitalContent.Builder("Salome").opera().atRoh().on("01/01/2008").dvd().spaSubs().iso().seenByFede().discBox(4, 6)
                .imdb("http://www.imdb.com/title/tt2254086/").build();
        new DigitalContent.Builder("Armida").opera().atTheMet().on("01/01/2010").dvd().spaSubs().iso().seenByFede()
                .discBox(5, 3).discBox(6, 3)
                .imdb("http://www.imdb.com/title/tt1670805/").build();
        new DigitalContent.Builder("Il Barbiere di Siviglia").opera().at("Teatro Real").on("01/01/2005").dvd().spaSubs().iso().discBox(8, 9)
                .imdb("http://www.imdb.com/title/tt1974283/").build();
        new DigitalContent.Builder("Il turco in Italia").opera().atZurich().on("01/01/2001").dvd().spaSubs().iso().seenByFede().discBox(1, 2)
                .imdb("http://www.imdb.com/title/tt0363150/").build();
        new DigitalContent.Builder("L'Italiana in Algeri").opera().atTheMet().on("01/01/1986").dvd().spaSubs()
                .iso()
                .discBox(1, 14)
                .discBox(2, 14)
                .imdb("http://www.imdb.com/title/tt1214968/").build();
        new DigitalContent.Builder("La Cenerentola").opera().atTheMet().on("01/01/2009").dvd().spaSubs().iso().seenByFede()
                .discBox(4, 8)
                .discBox(5, 8)
                .imdb("http://www.imdb.com/title/tt1497524/").build();
        new DigitalContent.Builder("La Cenerentola").opera().atTheMet().on("10/05/2014").fullHD().spaSubs().mkv()
                .discBox(7, 4)
                .discBox(10, 15)
                .build();
        new DigitalContent.Builder("La Donna del Lago").opera().atRoh().on("27/05/2013").fullHD().engSubs().mkv().discBox(7, 15).build();
        new DigitalContent.Builder("Sigismondo").opera().at("Teatro Rossini").on("01/01/2010").dvd().spaSubs().iso()
                .discBox(11, 13)
                .discBox(12, 13)
                .build();
        new DigitalContent.Builder("Zelmira").opera().at("Teatro Rossini").on("01/01/2010").dvd().spaSubs().iso()
                .discBox(8, 13)
                .discBox(9, 13)
                .imdb("http://www.imdb.com/title/tt2701902/").build();
        new DigitalContent.Builder("Cherevichki The Tsarina's Slippers").opera().atRoh().on("01/01/2009").fullHD().spaSubs().mkv()
                .discBox(7, 14)
                .imdb("http://www.imdb.com/title/tt2066085/").build();
        new DigitalContent.Builder("Eugene Onegin").opera().atTheMet().on("01/01/2007").dvd().spaSubs().iso()
                .discBox(5, 6)
                .discBox(6, 6)
                .imdb("http://www.imdb.com/title/tt1021778/").build();
        new DigitalContent.Builder("Eugene Onegin").opera().atRoh().on("20/02/2013").fullHD().spaSubs().mkv()
                .discBox(3, 14)
                .discBox(10, 14)
                .build();
        new DigitalContent.Builder("Eugene Onegin").opera().atTheMet().on("05/10/2013").fullHD().engSubs().mkv().discBox(10, 4)
                .imdb("http://www.imdb.com/title/tt2883544/").build();
        new DigitalContent.Builder("Orlando Furioso").opera().at("War Memorial Opera House").on("01/01/1990").dvd().spaSubs().iso()
                .discBox(6, 9)
                .imdb("http://www.imdb.com/title/tt0240782/").build();
        new DigitalContent.Builder("Das Rheingold").opera().at("Royal Danish Theatre").on("01/01/2006").dvd().spaSubs().iso().seenByFede().discBox(3, 4)
                .imdb("http://www.imdb.com/title/tt2199364/").build();
        new DigitalContent.Builder("Das Rheingold").opera().at("Palacio de las Artes Reina Sofía").on("01/01/2009").dvd().spaSubs().mkv().discBox(8, 11).build();
        new DigitalContent.Builder("Die Walküre").opera().at("Royal Danish Theatre").on("01/01/2006").dvd().spaSubs().iso().seenByFede()
                .discBox(4, 4)
                .discBox(5, 4)
                .imdb("http://www.imdb.com/title/tt2199396/").build();
        new DigitalContent.Builder("Die Walküre").opera().at("Palacio de las Artes Reina Sofía").on("01/01/2009").dvd().spaSubs().mkv().discBox(8, 11)
                .imdb("http://www.imdb.com/title/tt1565010/").build();
        new DigitalContent.Builder("Gotterdammerung").opera().at("Royal Danish Theatre").on("01/01/2006").dvd().spaSubs().iso().seenByFede().discBox(5, 4)
                .imdb("http://www.imdb.com/title/tt2199386/").build();
        new DigitalContent.Builder("Gotterdammerung").opera().at("Palacio de las Artes Reina Sofía").on("01/01/2009").dvd().spaSubs().mkv().discBox(8, 11)
                .imdb("http://www.imdb.com/title/tt2368889/").build();
        new DigitalContent.Builder("Lohengrin").opera().at("Bayerische Staatsoper").on("01/01/2009").dvd().spaSubs().iso()
                .discBox(6, 8)
                .discBox(7, 8)
                .imdb("http://www.imdb.com/title/tt1570624/").build();
        new DigitalContent.Builder("Parsifal").opera().atTheMet().on("02/03/2013").fullHD().engSubs().mkv().seenByFede()
                .discBox(9, 11)
                .discBox(10, 11)
                .discBox(4, 12)
                .imdb("http://www.imdb.com/title/tt2749072/").build();
        new DigitalContent.Builder("Rienzi").opera().at("Berlin State Opera").on("01/01/2010").dvd().spaSubs().iso().seenByFede()
                .discBox(2, 9)
                .discBox(3, 9)
                .imdb("http://www.imdb.com/title/tt1961481/").build();
        new DigitalContent.Builder("Siegfried").opera().at("Royal Danish Theatre").on("01/01/2006").dvd().spaSubs().iso().seenByFede().discBox(4, 4)
                .imdb("http://www.imdb.com/title/tt2199376/").build();
        new DigitalContent.Builder("Siegfried").opera().at("Palacio de las Artes Reina Sofía").on("01/01/2009").dvd().spaSubs().mkv().discBox(8, 11)
                .imdb("http://www.imdb.com/title/tt2369295/").build();
        new DigitalContent.Builder("Tannhauser").opera().at("Festspielhaus Baden-Baden").on("01/01/2008").fullHD().spaSubs().mkv().discBox(10, 4)
                .imdb("http://www.imdb.com/title/tt2222864/").build();
        new DigitalContent.Builder("Tristan und Ilsode").opera().at("Anhaltisches Theater").on("01/01/2007").dvd().spaSubs().iso()
                .discBox(1, 8)
                .discBox(2, 8)
                .build();
        new DigitalContent.Builder("Francesca da Rimini").opera().atTheMet().on("16/03/2013").fullHD().engSubs().mkv()
                .discBox(12, 11)
                .discBox(7, 12)
                .discBox(8, 12)
                .discBox(10, 12)
                .imdb("http://www.imdb.com/title/tt2777536/").build();
        new DigitalContent.Builder("I Due Foscari").opera().atRoh().on("27/10/2014").fullHD().spaSubs().mkv()
                .discBox(7, 1)
                .discBox(1, 1)
                .build();
        new DigitalContent.Builder("La Fanciulla del West").opera().atTheMet().on("08/01/2011").spaSubs().dvd().dvdFormat().box("La Fanciulla del West")
                .seenByFede()
                .build();

        /* --- */
        Set<Performance> siegfriedAtTheMet2012 = new DigitalContent.Builder("Siegfried")
                .opera()
                .atTheMet()
                .on("01/01/2011")
                .spaSubs()
                .br()
                .box("Der Ring des Nibelungen")
                .seenByFede()
                .build().getPerformances();

        new DigitalContent.Builder(siegfriedAtTheMet2012).mkv().fullHD().spaSubs()
                .discBox(0, 0) // 1 y 2
                .discBox(4, 3) // 3
                .build();

        /* --- */
        
        Set<Performance> gotterdammerungAtTheMet2012 = new DigitalContent.Builder("Gotterdammerung").opera().atTheMet().on("01/01/2012").spaSubs().br().box("Der Ring des Nibelungen").seenByFede().build().getPerformances();
        new DigitalContent.Builder(gotterdammerungAtTheMet2012).mkv().fullHD().spaSubs()
                .discBox(1, 5) // 1 y 3
                .discBox(4, 3) // 2
                .build();
        
        /* --- */
        Set<Performance> walkureAtTheMet2012 = new DigitalContent.Builder("Die Walküre").opera().atTheMet().on("01/01/2011").spaSubs().br().box("Der Ring des Nibelungen").seenByFede().build().getPerformances();
        new DigitalContent.Builder(walkureAtTheMet2012).mkv().fullHD().spaSubs()
                .discBox(5, 1) // parte 3
                .discBox(10, 1) // parte 2
                .discBox(1, 2) // parte 1
                .build();
        
        /* --- */
        
        new DigitalContent.Builder("Das Rheingold").opera().atTheMet().on("01/01/2010").spaSubs().br().box("Der Ring des Nibelungen").seenByFede().build();

        new DigitalContent.Builder("La Bohème").opera().at("Großes Festspielhaus").on("01/07/2012").spaSubs().br().box("La Bohème")
                .starringNetrebko()
                .seenByFede()
                .build();
        new DigitalContent.Builder("Lucia di Lammermoor").opera().atTheMet().on("07/02/2009").spaSubs().br()
                .starringNetrebko()
                .box("Lucia di Lammermoor").build();

        new DigitalContent.Builder("Le nozze di Figaro").opera().at("Sydney Opera House").on("18/08/2010").spaSubs().br().seenByFede().box("Le nozze di Figaro").build();
        new DigitalContent.Builder("Turandot").opera().atTheMet().on("07/11/2009 ").spaSubs().br().seenByFede().box("Turandot").build();
        new DigitalContent.Builder("Don Pasquale").opera().atTheMet().on("13/11/2010 ").spaSubs().br().seenByFede()
                .starringNetrebko()
                .box("Don Pasquale").build();

        new DigitalContent.Builder("Roméo et Juliette").opera().at("Großes Festspielhaus").on("02/08/2008").spaSubs().br().box("Roméo et Juliette")
                .build();

        new DigitalContent.Builder("Carmen").opera().atTheMet().on("01/11/2014").spaSubs().mkv().fullHD().discBox(1, 1)
                .build();

        new DigitalContent.Builder("Le Grand Macabre").opera().at("Gran Teatro del Liceo").on("01/11/2011").spaSubs().iso().dvd()
                .discBox(7, 1)
                .build();

        new DigitalContent.Builder("L'elisir d'amore").opera().atRoh().on("26/11/2014").hd720().spaSubs().mkv()
                .discBox(1, 3)
                .build();

        new DigitalContent.Builder("Otello ossia Il moro di Venezia").opera().atZurich().on("01/01/2011").dvd().spaSubs().mkv()
                .discBox(6, 1)
                .build();

        new DigitalContent.Builder("Salome").opera().at("Teatro Comunale di Bologna").on("01/01/2010").fullHD().spaSubs().mkv()
                .discBox(1, 16)
                .build();

        new DigitalContent.Builder("Elektra").opera().at("Aix-en-Provence").on("01/07/2013").dvd().spaSubs().iso()
                .discBox(6, 1)
                .build();

        new DigitalContent.Builder("Fidelio").opera().atAllaScala().on("07/12/2014").fullHD().spaSubs().mkv()
                .discBox(1, 3)
                .build();

        new DigitalContent.Builder("Elektra").opera().at("Großes Festspielhaus").on("01/01/2010").fullHD().spaSubs().mkv()
                .imdb("http://www.imdb.com/title/tt1787688/")
                .seenByFede()
                .discBox(5, 5)
                .build();

        new DigitalContent.Builder("Il Trovatore").opera().at("Berlin State Opera").on("01/01/2014").fullHD().spaSubs().mkv()
                .starringNetrebko()
                .discBox(7, 5)
                .discBox(6, 5)
                .build();

        new DigitalContent.Builder("L'Orfeo").opera().at("Teatro Real").on("01/01/2008").dvd().spaSubs().iso()
                .discBox(7, 16)
                .build();

        new DigitalContent.Builder("Boris Godunov").opera().atTheMet().on("23/10/2010").hd720().engSubs().mkv()
                .discBox(7, 5)
                .build();

        new DigitalContent.Builder("Le comte Ory").opera().atTheMet().on("09/04/2011").hd720().engSubs().mkv()
                .discBox(2, 16)
                .build();

        new DigitalContent.Builder("Rigoletto").opera().at("Wiener Staatsoper").on("20/12/2014").fullHD().spaSubs().mkv()
                .discBox(9, 16) // parte 3
                .discBox(7, 16) // parte 2
                .discBox(8, 16) // parte 1
                .build();

        new DigitalContent.Builder("La Traviata").opera().at("Los Angeles Opera").on("01/01/2006").fullHD().spaSubs().mkv()
                .starringFleming()
                .discBox(4, 16)
                .discBox(3, 16)
                .build();

        new DigitalContent.Builder("Il Barbiere di Siviglia").opera().at("Teatro Regio").on("01/01/2011").fullHD().spaSubs().mkv()
                .discBox(10, 16) // parte 2
                .discBox(9, 16) // parte 1
                .build();

        new DigitalContent.Builder("Rodelinda").opera().atTheMet().on("03/12/2011").fullHD().spaSubs().mkv()
                .starringFleming()
                .discBox(4, 16)
                .discBox(6, 16)
                .discBox(7, 16)
                .build();

        new DigitalContent.Builder("Cavalleria Rusticana").opera().at("Teatro Real").on("01/01/2007").fullHD().spaSubs().mkv()
                .discBox(6, 16)
                .build();

        new DigitalContent.Builder("Pagliacci").opera().at("Teatro Real").on("01/01/2007").fullHD().spaSubs().mkv()
                .discBox(8, 16)
                .build();

        //new DigitalContent.Builder("Ascenso y caída de la ciudad de Mahagonny").opera().at("Teatro Real").on("01/09/2010").fullHD().spaSubs().mkv()
        //        .discBox(0, 0)
        //        .build();

        new DigitalContent.Builder("Andrea Chénier").opera().atRoh().on("29/01/2015").fullHD().spaSubs().mkv()
                .discBox(11, 16) // 1 2 y 4
                .discBox(10, 16) //3 5 6
                .seenByFede()
                .build();

        new DigitalContent.Builder("Les contes d'Hoffmann").opera().atTheMet().on("31/01/2015").fullHD().spaSubs().mkv()
                .discBox(10, 1) // parte 2
                .discBox(1, 2) // 3
                .discBox(3, 3) // 1
                .build();

        new DigitalContent.Builder("El castillo de Barbazul").opera().atTheMet().on("14/02/2015").fullHD().spaSubs().mkv()
                .discBox(0, 0) 
                .build();

        new DigitalContent.Builder("Iolanta").opera().atTheMet().on("14/02/2015").fullHD().spaSubs().mkv()
                .discBox(0, 0) 
                .build();
    }

    @Override
    public Iterable<StorageBox> getAllBoxes() {
        return Repository.STORAGEBOX.findAll();
    }

    @Override
    public List<DigitalContentDTO> getFullReport() {
        return this.filteredReport(DigitalContentRepository.DIGITALCONTENT.findAll(), new Predicate<DigitalContent>() {
            @Override
            public boolean test(DigitalContent t) {
                return true;
            }
        });
    }

    private List<DigitalContentDTO> filteredReport(Collection<DigitalContent> elements, Predicate<DigitalContent> p) {
        List<DigitalContentDTO> answer = new ArrayList<>(elements.size());
        for (DigitalContent dc : elements) {
            if (p.test(dc)) {
                answer.add(toDto(dc));
            }
        }
        Collections.sort(answer);
        return answer;
    }

    private static DigitalContentDTO toDto(DigitalContent dc) {
        DigitalContentDTO dto = new DigitalContentDTO();

        List<String> boxNames = new ArrayList<>();
        for (StorageBox box : Repository.STORAGEBOX.boxesContaining(dc)) {
            boxNames.add(box.getName());
        }

        dto.setBoxes(boxNames);
        dto.setDate(dc.getDate());
        dto.setFormat(dc.getFormat().name());
        dto.setImdb(dc.getImdb());
        dto.setLanguage(list(dc.getLanguages()));
        dto.setMusicBy(toString(dc.getMusicComposers()));
        dto.setOpusTypes(toString(dc.getOpusTypes()));
        dto.setQuality(dc.getQuality().toString());
        dto.setSeenByAnaMaria(dc.isSeenBy(Repository.PERSON.findById("Ana María")));
        dto.setSeenByElsa(dc.isSeenBy(Repository.PERSON.findById("Elsa")));
        dto.setSeenByFede(dc.isSeenBy(Repository.PERSON.findById("Federico")));
        dto.setSubtitles(dc.getSubtitle() != null ? dc.getSubtitle().toString() : "");
        dto.setTitles(toString(dc.getTitles()));
        dto.setVenues(toString(dc.getVenues()));
        return dto;
    }

    private static List<String> toString(Collection<?> col) {
        List<String> answer = new ArrayList<>(col.size());
        for (Object o : col) {
            answer.add(o.toString());
        }
        return answer;
    }

    @Override
    public List<DigitalContentDTO> getBoxReport(final String boxName) {
        return this.filteredReport(DigitalContentRepository.DIGITALCONTENT.findAll(), new Predicate<DigitalContent>() {
            @Override
            public boolean test(DigitalContent dc) {
                Set<StorageBox> boxes = Repository.STORAGEBOX.boxesContaining(dc);
                for (StorageBox box : boxes) {
                    if (box.getName().equals(boxName)) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    @Override
    public List<DigitalContentDTO> getComposerReport(final String composerName) {
        return this.filteredReport(DigitalContentRepository.DIGITALCONTENT.findAll(), new Predicate<DigitalContent>() {
            @Override
            public boolean test(DigitalContent dc) {
                return dc.includesComposer(composerName);
            }
        });
    }

    @Override
    public List<DigitalContentDTO> getOpusReport(final String opusName) {
        return this.filteredReport(DigitalContentRepository.DIGITALCONTENT.findAll(), new Predicate<DigitalContent>() {
            @Override
            public boolean test(DigitalContent dc) {
                return dc.includesOpus(opusName);
            }
        });
    }

    @Override
    public List<DigitalContentDTO> getVenueReport(final String venueName) {
        return this.filteredReport(DigitalContentRepository.DIGITALCONTENT.findAll(), new Predicate<DigitalContent>() {
            @Override
            public boolean test(DigitalContent dc) {
                return dc.includesVenue(venueName);
            }
        });
    }

    @Override
    public List<DigitalContentDTO> getOpusTypeReport(String name) {
        final OpusType type = OpusType.valueOf(name);
        return this.filteredReport(DigitalContentRepository.DIGITALCONTENT.findAll(), new Predicate<DigitalContent>() {

            @Override
            public boolean test(DigitalContent dc) {
                return dc.getOpusTypes().contains(type);
            }
        });
    }

    @Override
    public BoxLabelDTO getBoxLabel(String boxName) {
        return this.getBoxLabel(Repository.STORAGEBOX.findById(boxName));
    }

    private BoxLabelDTO getBoxLabel(StorageBox box) {
        BoxLabelDTO dto = new BoxLabelDTO();
        dto.setBoxName(box.getName());
        for (StorageMedium medium : box.getMedia()) {
            final MediumContentDTO mDto = new MediumContentDTO();
            mDto.setMediumName(medium.getName());
            for (DigitalContent dc : medium.getContents()) {
                for (Performance p : dc.getPerformances()) {
                    mDto.addOpus(p.getDetailedTitle(), p.getOpusType().name());
                }
            }
            dto.addContent(mDto);
        }
        dto.doneContent();
        return dto;
    }

    @Override
    public List<BoxLabelDTO> getEveryBoxLabel() {
        Set<StorageBox> boxes = Repository.STORAGEBOX.findAll();
        List<BoxLabelDTO> list = new ArrayList<>(boxes.size());
        for (StorageBox box : boxes) {
            list.add(this.getBoxLabel(box));
        }
        Collections.sort(list);
        return list;
    }

    @Override
    public List<VenueDTO> getVenues() {
        Set<Venue> allVenues = Repository.VENUE.findAll();
        List<VenueDTO> list = new ArrayList<>(allVenues.size());
        for (Venue v : allVenues) {
            VenueDTO dto = new VenueDTO();
            dto.setName(v.getName());
            dto.setCountry(v.getCountryName());
            dto.setCity(v.getCityName());
            dto.setLatLon(v.getLatLon());
            dto.setWikipedia(v.getWikipedia());
            list.add(dto);
        }
        return list;
    }

    @Override
    public VenueDetailDTO getVenueDetail(String venueName) {
        DateFormat yearFormat = new SimpleDateFormat("yyyy");
        Venue venue = Repository.VENUE.findById(venueName);
        VenueDetailDTO dto = new VenueDetailDTO();
        dto.setName(venueName);
        for (Performance p : Repository.PERFORMANCE.findByVenue(venue)) {
            dto.addPerformance(p.getTitle(), yearFormat.format(p.getDate()), p.getImdb());
        }
        dto.doneAddingPerformances();
        dto.setWikipedia(venue.getWikipedia());
        return dto;
    }

    @Override
    public List<OpusDTO> unseenBy(String personName) {
        Set<DigitalContent> allContent = Repository.DIGITALCONTENT.findAll();
        Person p = Repository.PERSON.findById(personName);
        Set<Opus> allOpuses = Repository.OPUS.findAll();
        for (DigitalContent dc : allContent) {
            if (dc.isSeenBy(p)) {
                allOpuses.removeAll(dc.getOpuses());
            }
        }
        List<OpusDTO> answer = new ArrayList<>(allOpuses.size());
        for (Opus o : allOpuses) {
            answer.add(new OpusDTO(o.getTitle(), o.getType().name()));
        }
        Collections.sort(answer, OPUS_COMPARATOR);

        return answer;
    }

    @Override
    public List<OpusDTO> unavailableInHD() {

        Set<Opus> notInHD = Repository.OPUS.findAll();
        for (DigitalContent dc : Repository.DIGITALCONTENT.findAll()) {
            if (dc.getQuality() == Quality.HD720 || dc.getQuality() == Quality.HD1080) {
                notInHD.removeAll(dc.getOpuses());
            }
        }

        List<OpusDTO> answer = new ArrayList<>(notInHD.size());
        for (Opus o : notInHD) {
            answer.add(new OpusDTO(o.getTitle(), o.getType().name()));
        }
        Collections.sort(answer, OPUS_COMPARATOR);
        return answer;
    }

}
