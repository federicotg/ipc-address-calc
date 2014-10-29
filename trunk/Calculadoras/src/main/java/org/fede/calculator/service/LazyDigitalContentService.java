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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.fede.digitalcontent.dto.BoxLabelDTO;
import org.fede.digitalcontent.dto.DigitalContentDTO;
import org.fede.digitalcontent.dto.MediumContentDTO;
import org.fede.digitalcontent.model.Country;
import static org.fede.util.Util.list;
import static org.fede.digitalcontent.model.Country.DENMARK;
import static org.fede.digitalcontent.model.Country.FRANCE;
import static org.fede.digitalcontent.model.Country.SWITZERLAND;
import org.fede.digitalcontent.model.DigitalContent;
import org.fede.digitalcontent.model.DigitalContentRepository;
import org.fede.digitalcontent.model.StorageBox;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.fede.digitalcontent.model.Language;
import org.fede.digitalcontent.model.Opus;
import org.fede.digitalcontent.model.OpusType;
import org.fede.digitalcontent.model.Performance;
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

    @PostConstruct
    public void initBasicObjects() throws ParseException {

        this.initVenues();
        this.initBallets();
        this.initOperas();
        this.initBalletVideos();
        this.initOperaVideos();
        this.initMisc();

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
        new Opus.Builder("Romeo and Juliet").ballet().by("Sergey Prokofiev").build();
        new Opus.Builder("Swan Lake", "The Nutcracker", "The Sleeping Beauty").ballet()
                .by("Tchaikovsky").build();
        new Opus.Builder("La fille mal gardée").ballet().build();
        new Opus.Builder("Legend of Love").by("Arif Malikov").ballet().build();

    }

    private void initVenues() {
        new Venue.Builder("Teatro di San Carlo").city("Nápoles").italy().build();
        new Venue.Builder("alla Scala").city("Milano").italy().build();
        new Venue.Builder("Teatro Donizetti").city("Bergamo").italy().build();
        new Venue.Builder("Teatro Comunale di Firenze").city("Florencia").italy().build();
        new Venue.Builder("Teatro Comunale di Modena").city("Módena").italy().build();
        new Venue.Builder("Teatro Rossini").city("Pesaro").italy().build();
        new Venue.Builder("Arena di Verona").city("Verona").italy().build();

        new Venue.Builder("Waldbühne").city("Berlín").germany().build();
        new Venue.Builder("Festspielhaus Baden-Baden").city("Baden-Baden").germany().build();
        new Venue.Builder("Bayerische Staatsoper").city("Munich").germany().build();
        new Venue.Builder("Halle Opera House").city("Halle").germany().build();
        new Venue.Builder("Berlin State Opera").city("Berlín").germany().build();
        new Venue.Builder("Anhaltisches Theater").city("Dessau").germany().build();
        new Venue.Builder("Staatsoper Stuttgart").city("Stuttgart").germany().build();

        new Venue.Builder("Palacio de las Artes Reina Sofía").city("Valencia").spain().build();
        new Venue.Builder("Gran Teatro del Liceo").city("Barcelona").spain().build();
        new Venue.Builder("Teatro Real").city("Madrid").spain().build();

        new Venue.Builder("Bolshói").city("Moscú").russia().build();
        new Venue.Builder("Mariinsky").city("San Petersburgo").russia().build();

        new Venue.Builder("ROH").city("Londres").uk().build();
        new Venue.Builder("King's College").city("Cambridge").uk().build();
        new Venue.Builder("Glyndebourne").city("Glyndebourne").uk().build();

        new Venue.Builder("The Met").city("New York").usa().build();
        new Venue.Builder("War Memorial Opera House").city("San Francisco").usa().build();

        new Venue.Builder("Musikverein").city("Viena").country(Country.AUSTRIA).build();
        new Venue.Builder("Wiener Staatsoper").city("Viena").country(Country.AUSTRIA).build();
        new Venue.Builder("Großes Festspielhaus").city("Salzburgo").country(Country.AUSTRIA).build();

        new Venue.Builder("ONP").city("Paris").country(FRANCE).build();

        new Venue.Builder("Royal Danish Theatre").city("Copenhagen").country(DENMARK).build();

        new Venue.Builder("Sydney Opera House").city("Sydney").australia().build();

        new Venue.Builder("Ópera de Zürich").city("Zürich").country(SWITZERLAND).build();

        new Venue.Builder("Ciudad Prohibida").city("Beijing").country(Country.CHINA).build();

    }

    private void initMisc() throws ParseException {
        new Opus.Builder("Wagner's Dream")
                .english().type(OpusType.DOCUMENTARY).build();
        new DigitalContent.Builder("Wagner's Dream").type(OpusType.DOCUMENTARY).atTheMet().on("01/01/2012").dvd().mkv()
                .discBox(8, 11)
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
                "Lucia de Lammermoor",
                "Maria Stuarda")
                .italian().opera().by("Gaetano Donizetti").build();
        new Opus.Builder("La Fille du Regiment").french().opera().by("Gaetano Donizetti").build();

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
        new Opus.Builder("The Cunning Little Vixen").italian().opera().by("Leoš Janáček").build();
        new Opus.Builder("Manon").french().opera().by("Massenet").build();
        new Opus.Builder("Hippolyte et Aricie").french().opera().by("Rameau").build();
        new Opus.Builder("L'enfant et les sortilèges", "L'heure espagnole")
                .french().opera().by("Ravel").build();
        new Opus.Builder("Ariadne auf Naxos", "Capriccio", "Intermezzo", "Salome")
                .german().opera().by("Richard Strauss").build();
        new Opus.Builder("Cherevichki The Tsarina's Slippers", "Eugene Onegin")
                .russian().opera().by("Tchaikovsky").build();
        new Opus.Builder("Orlando Furioso").italian().opera().by("Vivaldi").build();
        new Opus.Builder("Nixon in China").english().opera().by("John Adams").build();
        new Opus.Builder("Faust").french().opera().by("Gounod").build();
        new Opus.Builder("La Gioconda").french().opera().by("A. Ponchielli").build();

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

        new Opus.Builder("Armida",
                "Il Barbiere di Siviglia",
                "Il turco in Italia",
                "L'Italiana in Algeri",
                "La Cenerentola",
                "La Donna del Lago",
                "Sigismondo",
                "Zelmira")
                .italian().opera().by("Rossini").build();

    }

    private void initBalletVideos() throws ParseException {
        new DigitalContent.Builder("Giselle").ballet().atRoh().on("29/04/2006").hd720().mkv()
                .seenByAnaMaria()
                .discBox(1, 3)
                .imdb("http://www.imdb.com/title/tt1596783/")
                .build();
        new DigitalContent.Builder("Giselle").ballet().atRoh().on("27/01/2014").fullHD().mkv().discBox(7, 15).build();
        new DigitalContent.Builder("Le Corsaire").ballet().atBolshoi().on("11/03/2012").hd720().mkv().discBox(9, 3)
                .imdb("http://www.imdb.com/title/tt2798336/")
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
                .ballet().atRoh().on("28/03/2013").fullHD().mkv().seenByAnaMaria()
                .discBox(12, 11)
                .discBox(3, 12)
                .discBox(4, 12)
                .build();
        new DigitalContent.Builder("Winter's Tale").ballet().atRoh().on("28/04/2014").fullHD().mkv()
                .seenByAnaMaria()
                .discBox(9, 4)
                .build();
        new DigitalContent.Builder("Coppelia").ballet().atBolshoi().on("29/05/2011").hd720().mkv()
                .discBox(7, 3)
                .seenByAnaMaria()
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

        new DigitalContent.Builder("La Bayadère").ballet().atBolshoi().on("22/03/2012").hd720().mkv()
                .discBox(2, 11)
                .discBox(3, 11)
                .seenByAnaMaria().build();
        new DigitalContent.Builder("La Bayadère").ballet().atBolshoi().on("27/01/2013").fullHD().mkv()
                .discBox(11, 14)
                .seenByAnaMaria().build();

        new DigitalContent.Builder("Notre Dame de Paris").ballet().atBolshoi().on("14/02/2013").fullHD().mkv()
                .discBox(2, 13)
                .build();
        new DigitalContent.Builder("Daphnis et Chloé").ballet().atBolshoi().on("06/03/2014").fullHD().mkv().discBox(10, 15).build();
        new DigitalContent.Builder("Romeo and Juliet").ballet().atRoh().on("10/01/2012").fullHD().mkv().discBox(1, 3)
                .seenByAnaMaria()
                .build();
        new DigitalContent.Builder("Romeo and Juliet").ballet().atBolshoi().on("12/05/2013").fullHD().mkv()
                .discBox(11, 14)
                .build();
        new DigitalContent.Builder("Swan Lake").ballet().atBolshoi().on("19/06/2011").hd720().mkv()
                .discBox(8, 2)
                .discBox(9, 2)
                .seenByFede()
                .build();
        new DigitalContent.Builder("Swan Lake").ballet().at("Mariinsky").on("06/06/2013").fullHD().mkv()
                .discBox(6, 13)
                .discBox(12, 13)
                .discBox(4, 14)
                .seenByAnaMaria()
                .build();
        new DigitalContent.Builder("The Nutcracker").ballet().atBolshoi().on("18/12/2011").hd720().mkv()
                .discBox(7, 3)
                .imdb("http://www.imdb.com/title/tt2798230/")
                .seenByAnaMaria()
                .build();
        new DigitalContent.Builder("The Nutcracker").ballet().atRoh().on("12/12/2013").fullHD().mkv().discBox(5, 15).build();

        new DigitalContent.Builder("The Sleeping Beauty").ballet().atBolshoi().on("20/11/2011").hd720().mkv().discBox(4, 5)
                .imdb("http://www.imdb.com/title/tt2798186/")
                .build();
        new DigitalContent.Builder("The Sleeping Beauty").ballet().atRoh().on("15/12/2011").hd720().mkv()
                .discBox(7, 2)
                .discBox(9, 2)
                .discBox(10, 2)
                .discBox(2, 3)
                .build();
        new DigitalContent.Builder("The Sleeping Beauty").ballet().atRoh().on("19/03/2014").fullHD().mkv().discBox(10, 4)
                .seenByAnaMaria()
                .seenByFede()
                .build();
        new DigitalContent.Builder("La fille mal gardée").ballet().atRoh().on("16/05/2012").hd720().mkv().discBox(8, 3)
                .seenByFede()
                .build();

        new DigitalContent.Builder("Legend of Love").ballet().atBolshoi().on("26/10/2014").fullHD().mkv().discBox(0, 0).build();

    }

    private void initOperaVideos() throws ParseException {

        new DigitalContent.Builder("La Gioconda").opera().atParis().on("13/05/2013").fullHD().spaSubs().mkv().seenByFede()
                .discBox(1, 14)
                .discBox(2, 14)
                .discBox(5, 14)
                .build();
        new DigitalContent.Builder("Rusalka").opera().atTheMet().on("08/02/2014").fullHD().engSubs().mkv().discBox(9, 15).imdb("http://www.imdb.com/title/tt3451542/").build();
        new DigitalContent.Builder("I Puritani").opera().atTheMet().on("01/01/2007").dvd().spaSubs().iso()
                .discBox(12, 14)
                .imdb("http://www.imdb.com/title/tt0966101/").build();
        new DigitalContent.Builder("I Puritani").opera().atParis().on("09/12/2013").fullHD().spaSubs().mkv().seenByFede()
                .discBox(5, 11)
                .build();
        new DigitalContent.Builder("Norma").opera().at("Bayerische Staatsoper").on("01/01/2007").dvd().spaSubs().iso()
                .discBox(3, 3).discBox(4, 3)
                .build();
        new DigitalContent.Builder("Faust").opera().atRoh().on("01/01/2004").dvd().spaSubs().iso().discBox(2, 5).discBox(3, 5).imdb("http://www.imdb.com/title/tt1954436/").build();
        new DigitalContent.Builder("Carmen").opera().atTheMet().on("16/01/2010").dvd().spaSubs().iso().seenByFede()
                .discBox(6, 1)
                .discBox(7, 1)
                .imdb("http://www.imdb.com/title/tt1669004/").build();
        new DigitalContent.Builder("Prince Igor").opera().atTheMet().on("01/03/2014").fullHD().spaSubs().mkv().discBox(3, 15).imdb("http://www.imdb.com/title/tt3565332/").build();
        new DigitalContent.Builder("The Nose").opera().atTheMet().on("26/10/2013").fullHD().engSubs().mkv().discBox(6, 15).imdb("http://www.imdb.com/title/tt3114272/").build();
        new DigitalContent.Builder("Anna Bolena").opera().at("Wiener Staatsoper").on("01/01/2011").dvd().spaSubs().iso()
                .discBox(1, 7).discBox(2, 7)
                .imdb("http://www.imdb.com/title/tt1878854/").build();
        new DigitalContent.Builder("Don Pasquale").opera().atGlyndebourne().on("06/08/2013").fullHD().engSubs().mkv()
                .discBox(4, 11)
                .build();
        new DigitalContent.Builder("Don Pasquale").opera().at("alla Scala").on("01/01/1994").dvd().spaSubs().iso().discBox(1, 15).build();
        new DigitalContent.Builder("L'elisir d'amore").opera().atTheMet().on("13/10/2012").fullHD().engSubs().mkv()
                .discBox(11, 11)
                .discBox(8, 12)
                .discBox(10, 12)
                .imdb("http://www.imdb.com/title/tt2303779/").build();
        new DigitalContent.Builder("La Fille du Regiment").opera().atRoh().on("01/01/2007").dvd().spaSubs().iso()
                .discBox(5, 13)
                .imdb("http://www.imdb.com/title/tt1320091/").build();
        new DigitalContent.Builder("Lucia de Lammermoor").opera().at("Teatro Donizetti").on("01/01/2006").dvd().spaSubs().iso().seenByFede()
                .discBox(3, 7)
                .build();
        new DigitalContent.Builder("Maria Stuarda").opera().atTheMet().on("19/01/2013").fullHD().engSubs().mkv().seenByFede()
                .discBox(8, 7)
                .discBox(2, 12)
                .imdb("http://www.imdb.com/title/tt2651570/").build();
        new DigitalContent.Builder("Faust").opera().atTheMet().on("10/12/2011").fullHD().engSubs().mkv()
                .discBox(10, 13)
                .imdb("http://www.imdb.com/title/tt1878497/").build();
        new DigitalContent.Builder("Aida").opera().at("alla Scala").on("01/01/2006").dvd().spaSubs().iso()
                .discBox(3, 2)
                .discBox(4, 2)
                .imdb("http://www.imdb.com/title/tt1399162/").build();
        new DigitalContent.Builder("Aida").opera().atTheMet().on("15/12/2012").fullHD().engSubs().mkv()
                .discBox(6, 12)
                .discBox(9, 12)
                .imdb("http://www.imdb.com/title/tt2573426/").build();
        new DigitalContent.Builder("Don Carlo").opera().atRoh().on("01/01/2008").fullHD().engSubs().mkv().discBox(7, 2).imdb("http://www.imdb.com/title/tt2074354/").build();
        new DigitalContent.Builder("Falstaff").opera().at("Teatro Comunale di Firenze").on("01/01/2006").dvd().spaSubs().iso().seenByFede().discBox(2, 2).imdb("http://www.imdb.com/title/tt2199446/").build();
        new DigitalContent.Builder("Falstaff").opera().atGlyndebourne().on("01/01/2009").fullHD().engSubs().mkv()
                .discBox(6, 14)
                .discBox(8, 14)
                .build();
        new DigitalContent.Builder("Falstaff").opera().atTheMet().on("15/12/2013").fullHD().engSubs().mkv()
                .discBox(5, 11)
                .imdb("http://www.imdb.com/title/tt3385804/").build();
        new DigitalContent.Builder("I Due Foscari").opera().at("Teatro di San Carlo").on("01/01/2001").dvd().spaSubs().iso().discBox(1, 6).imdb("http://www.imdb.com/title/tt0401393/").build();
        new DigitalContent.Builder("Il Trovatore").opera().at("Wiener Staatsoper").on("01/01/1978").dvd().spaSubs().iso()
                .discBox(5, 5)
                .discBox(6, 5)
                .imdb("http://www.imdb.com/title/tt0429194/").build();
        new DigitalContent.Builder("La Forza del Destino").opera().at("Teatro Comunale di Firenze").on("01/01/2007").dvd().spaSubs().iso()
                .discBox(12, 14)
                .imdb("http://www.imdb.com/title/tt2196658/").build();
        new DigitalContent.Builder("La Traviata").opera().at("Großes Festspielhaus").on("27/06/1905").dvd().spaSubs().iso().seenByFede().discBox(2, 4).imdb("http://www.imdb.com/title/tt0475165/").build();
        new DigitalContent.Builder("La Traviata").opera().at("alla Scala").on("01/01/2007").hd720().mkv()
                .discBox(3, 14)
                .imdb("http://www.imdb.com/title/tt2316799/").build();
        new DigitalContent.Builder("La Traviata").opera().atParis().on("17/06/2014").fullHD().spaSubs().mkv()
                .discBox(7, 4)
                .discBox(8, 4)
                .discBox(11, 15)
                .build();
        new DigitalContent.Builder("La Traviata").opera().atGlyndebourne().on("10/08/2014").fullHD().engSubs().mkv().seenByFede().discBox(0, 0).build();
        new DigitalContent.Builder("Macbeth").opera().atParis().on("04/04/2009").fullHD().spaSubs().mkv().discBox(9, 15).imdb("http://www.imdb.com/title/tt2705154/").build();
        new DigitalContent.Builder("Macbeth").opera().atTheMet().on("11/10/2014").fullHD().spaSubs().mkv().discBox(0, 0).build();
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
        new DigitalContent.Builder("Rigoletto").opera().at("Ópera de Zürich").on("01/01/2006").fullHD().spaSubs().mkv()
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
        new DigitalContent.Builder("Alcina").opera().at("Staatsoper Stuttgart").on("21/06/1905").fullHD().spaSubs().mkv().discBox(8, 4).imdb("http://www.imdb.com/title/tt0354364/").build();
        new DigitalContent.Builder("Giulio Cesare").opera().atParis().on("01/01/2011").dvd().spaSubs().iso()
                .discBox(6, 13)
                .discBox(7, 13)
                .build();
        new DigitalContent.Builder("Messiah").oratorio().at("Ópera de Zürich").on("01/01/2009").fullHD().spaSubs().mkv().seenByFede()
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
        new DigitalContent.Builder("Rodelinda").opera().atGlyndebourne().on("01/01/1998").dvd().spaSubs().iso().discBox(2, 6).imdb("http://www.imdb.com/title/tt0255555/").build();
        new DigitalContent.Builder("Tamerlano").opera().at("Halle Opera House").on("01/01/2001").dvd().spaSubs().iso()
                .discBox(1, 12)
                .discBox(2, 12)
                .build();
        new DigitalContent.Builder("Theodora").oratorio().atGlyndebourne().on("01/01/1996").dvd().engSubs().mkv().discBox(0, 0).imdb("http://www.imdb.com/title/tt0253802/").build();
        new DigitalContent.Builder("Les Troyens").opera().atTheMet().on("05/01/2013").fullHD().engSubs().mkv()
                .discBox(11, 11)
                .discBox(5, 12)
                .discBox(7, 12)
                .imdb("http://www.imdb.com/title/tt2661764/").build();
        new DigitalContent.Builder("Nixon in China").opera().atTheMet().on("01/01/2011").dvd().spaSubs().iso().discBox(4, 4).imdb("http://www.imdb.com/title/tt1736150/").build();
        new DigitalContent.Builder("Manon").opera().atTheMet().on("07/04/2012").fullHD().engSubs().mkv().seenByFede()
                .discBox(6, 11).imdb("http://www.imdb.com/title/tt2239809/").build();
        new DigitalContent.Builder("Werther").opera().atTheMet().on("15/03/2014").fullHD().spaSubs().mkv()
                .discBox(11, 15)
                .discBox(9, 4)
                .imdb("http://www.imdb.com/title/tt3599730/").build();
        new DigitalContent.Builder("Lakme").opera().at("Sydney Opera House").on("01/01/2011").fullHD().spaSubs().mkv().discBox(3, 15).imdb("http://www.imdb.com/title/tt2757396/").build();
        new DigitalContent.Builder("Pagliacci").opera().at("Arena di Verona").on("01/01/2006").dvd().spaSubs().iso().seenByFede()
                .discBox(1, 9)
                .build();
        new DigitalContent.Builder("The Cunning Little Vixen").opera().atGlyndebourne().on("10/06/2012").fullHD().engSubs().mkv().discBox(4, 7).build();
        new DigitalContent.Builder("Manon").opera().at("Berlin State Opera").on("01/01/2007").dvd().spaSubs().iso().discBox(5, 2)
                .discBox(6, 2)
                .imdb("http://www.imdb.com/title/tt1431725/").build();
        new DigitalContent.Builder("L'Orfeo").opera().at("Ópera de Zürich").on("01/01/2007").dvd().spaSubs().iso().discBox(2, 15).build();
        new DigitalContent.Builder("Così Fan Tutte").opera().atTheMet().on("26/04/2014").fullHD().spaSubs().mkv().discBox(6, 4).build();
        new DigitalContent.Builder("Così Fan Tutte").opera().at("Wiener Staatsoper").on("01/01/1996").dvd().spaSubs().iso().seenByFede()
                .discBox(4, 9)
                .discBox(5, 9)
                .imdb("http://www.imdb.com/title/tt0428432/").build();
        new DigitalContent.Builder("Don Giovanni").opera().at("Großes Festspielhaus").on("01/01/2006").dvd().spaSubs().iso()
                .discBox(5, 7)
                .discBox(6, 7)
                .imdb("http://www.imdb.com/title/tt0864960/").build();
        new DigitalContent.Builder("Don Giovanni").opera().atRoh().on("01/01/2008").fullHD().engSubs().mkv().seenByFede()
                .discBox(10, 2)
                .discBox(2, 3)
                .imdb("http://www.imdb.com/title/tt2374452/").build();
        new DigitalContent.Builder("El rapto en el serrallo").opera().at("Bayerische Staatsoper").on("01/01/1980").fullHD().spaSubs().mkv()
                .discBox(4, 11)
                .imdb("http://www.imdb.com/title/tt0254303/").build();
        new DigitalContent.Builder("Il Sogno di Scipione").opera().at("Großes Festspielhaus").on("01/01/2006").dvd().spaSubs().iso().seenByFede().discBox(10, 3).imdb("http://www.imdb.com/title/tt1039909/").build();
        new DigitalContent.Builder("La Clemenza di Tito").opera().atTheMet().on("01/12/2012").fullHD().engSubs().mkv().seenByFede()
                .discBox(8, 11)
                .discBox(9, 11)
                .imdb("http://www.imdb.com/title/tt2367900/").build();
        new DigitalContent.Builder("La Flauta Mágica").opera().at("Großes Festspielhaus").on("01/08/2012").fullHD().spaSubs().mkv().seenByFede()
                .discBox(7, 7)
                .build();
        new DigitalContent.Builder("Le nozze di Figaro").opera().atGlyndebourne().on("01/01/1999").fullHD().engSubs().mkv()
                .discBox(11, 14)
                .build();
        new DigitalContent.Builder("Le nozze di Figaro").opera().atRoh().on("01/01/2006").dvd().spaSubs().iso().seenByFede()
                .discBox(3, 1)
                .discBox(4, 1)
                .imdb("http://www.imdb.com/title/tt1229379/").build();
        new DigitalContent.Builder("Le nozze di Figaro").opera().at("Großes Festspielhaus").on("01/01/2006").fullHD().spaSubs().mkv()
                .discBox(11, 14)
                .imdb("http://www.imdb.com/title/tt0838187/").build();
        new DigitalContent.Builder("Le nozze di Figaro").opera().atGlyndebourne().on("17/08/2012").fullHD().engSubs().mkv()
                .discBox(8, 14)
                .build();
        new DigitalContent.Builder("La Flauta Mágica").opera().atRoh().on("01/01/2003").dvd().spaSubs().iso().seenByFede()
                .discBox(5, 1).imdb("http://www.imdb.com/title/tt0383058/").build();
        new DigitalContent.Builder("Gianni Schicchi").opera().atGlyndebourne().on("01/01/2004").dvd().spaSubs().iso().seenByFede().discBox(10, 1).build();
        new DigitalContent.Builder("Il Trittico").opera().at("Teatro Comunale di Modena").on("01/01/2007").dvd().spaSubs().iso().discBox(2, 15).build();
        new DigitalContent.Builder("La Bohème").opera().at("Teatro Real").on("01/01/2006").dvd().spaSubs().iso().seenByFede()
                .discBox(8, 1)
                .discBox(9, 1)
                .build();
        new DigitalContent.Builder("La Bohème").opera().atRoh().on("15/01/2013").fullHD().spaSubs().mkv().discBox(3, 4).build();
        new DigitalContent.Builder("La Fanciulla del West").opera().atTheMet().on("08/01/2011").dvd().spaSubs().iso().seenByFede().discBox(0, 0).imdb("http://www.imdb.com/title/tt1736154/").build();
        new DigitalContent.Builder("La Fanciulla del West").opera().atParis().on("10/02/2014").fullHD().spaSubs().mkv().discBox(11, 15).discBox(0, 0).build();
        new DigitalContent.Builder("La Rondine").opera().atTheMet().on("01/01/2009").dvd().spaSubs().iso().seenByFede().discBox(8, 8).imdb("http://www.imdb.com/title/tt1347918/").build();
        new DigitalContent.Builder("Madama Butterfly").opera().at("Arena di Verona").on("01/01/2004").dvd().spaSubs().iso().discBox(1, 1).build();
        new DigitalContent.Builder("Manon Lescaut").opera().atRoh().on("01/01/1983").dvd().spaSubs().iso().discBox(2, 15).imdb("http://www.imdb.com/title/tt0254549/").build();
        new DigitalContent.Builder("Manon Lescaut").opera().atRoh().on("24/06/2014").fullHD().mkv().discBox(0, 0).build();
        new DigitalContent.Builder("Tosca").opera().at("alla Scala").on("22/06/1905").dvd().spaSubs().iso().discBox(6, 2).imdb("http://www.imdb.com/title/tt2385259/").build();
        new DigitalContent.Builder("Tosca").opera().atRoh().on("17/07/2011").fullHD().engSubs().mkv().seenByFede().discBox(3, 8).build();
        new DigitalContent.Builder("Tosca").opera().atTheMet().on("09/11/2013").fullHD().engSubs().mkv().discBox(5, 15).imdb("http://www.imdb.com/title/tt3315900/").build();
        new DigitalContent.Builder("Tosca").opera().atParis().on("16/10/2014").fullHD().spaSubs().mkv().discBox(0, 0).build();
        new DigitalContent.Builder("Turandot").opera().atTheMet().on("09/06/1905").fullHD().spaSubs().mkv()
                .discBox(3, 13)
                .imdb("http://www.imdb.com/title/tt0220822/").build();
        new DigitalContent.Builder("Turandot").opera().at("Ciudad Prohibida").on("21/06/1905").fullHD().spaSubs().mkv().discBox(5, 2).imdb("http://www.imdb.com/title/tt0205483/").build();
        new DigitalContent.Builder("Hippolyte et Aricie").opera().atGlyndebourne().on("29/06/2013").fullHD().engSubs().mkv().discBox(1, 15).build();
        new DigitalContent.Builder("L'enfant et les sortilèges").opera().atGlyndebourne().on("19/08/2012").fullHD().engSubs().mkv().seenByFede().discBox(3, 6).build();
        new DigitalContent.Builder("L'heure espagnole").opera().atGlyndebourne().on("19/08/2012").fullHD().engSubs().mkv().seenByFede().discBox(3, 6).build();
        new DigitalContent.Builder("Ariadne auf Naxos").opera().atGlyndebourne().on("04/06/2013").fullHD().engSubs().mkv().seenByFede()
                .discBox(7, 14)
                .discBox(10, 14)
                .build();
        new DigitalContent.Builder("Capriccio").opera().atParis().on("01/01/2004").dvd().spaSubs().iso()
                .discBox(7, 5)
                .discBox(8, 5)
                .imdb("http://www.imdb.com/title/tt2353871/").build();
        new DigitalContent.Builder("Intermezzo").opera().atGlyndebourne().on("01/01/1983").fullHD().engSubs().iso()
                .discBox(7, 9)
                .imdb("http://www.imdb.com/title/tt0240617/").build();
        new DigitalContent.Builder("Salome").opera().atRoh().on("01/01/2008").dvd().spaSubs().iso().seenByFede().discBox(4, 6).imdb("http://www.imdb.com/title/tt2254086/").build();
        new DigitalContent.Builder("Armida").opera().atTheMet().on("01/01/2010").dvd().spaSubs().iso().seenByFede()
                .discBox(5, 3).discBox(6, 3)
                .imdb("http://www.imdb.com/title/tt1670805/").build();
        new DigitalContent.Builder("Il Barbiere di Siviglia").opera().atGlyndebourne().on("01/01/1981").dvd().spaSubs().iso().discBox(2, 1).imdb("http://www.imdb.com/title/tt0259878/").build();
        new DigitalContent.Builder("Il Barbiere di Siviglia").opera().at("Teatro Real").on("01/01/2005").dvd().spaSubs().iso().discBox(8, 9).imdb("http://www.imdb.com/title/tt1974283/").build();
        new DigitalContent.Builder("Il turco in Italia").opera().at("Ópera de Zürich").on("01/01/2001").dvd().spaSubs().iso().seenByFede().discBox(1, 2).imdb("http://www.imdb.com/title/tt0363150/").build();
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
        new DigitalContent.Builder("Eugene Onegin").opera().atTheMet().on("05/10/2013").fullHD().engSubs().mkv().discBox(10, 4).imdb("http://www.imdb.com/title/tt2883544/").build();
        new DigitalContent.Builder("Orlando Furioso").opera().at("War Memorial Opera House").on("01/01/1990").dvd().spaSubs().iso()
                .discBox(6, 9)
                .imdb("http://www.imdb.com/title/tt0240782/").build();
        new DigitalContent.Builder("Das Rheingold").opera().at("Royal Danish Theatre").on("01/01/2006").dvd().spaSubs().iso().seenByFede().discBox(3, 4).imdb("http://www.imdb.com/title/tt2199364/").build();
        new DigitalContent.Builder("Das Rheingold").opera().at("Palacio de las Artes Reina Sofía").on("01/01/2009").dvd().spaSubs().mkv().discBox(0, 0).build();
        new DigitalContent.Builder("Die Walküre").opera().at("Royal Danish Theatre").on("01/01/2006").dvd().spaSubs().iso().seenByFede()
                .discBox(4, 4)
                .discBox(5, 4)
                .imdb("http://www.imdb.com/title/tt2199396/").build();
        new DigitalContent.Builder("Die Walküre").opera().at("Palacio de las Artes Reina Sofía").on("01/01/2009").dvd().spaSubs().mkv().discBox(0, 0).imdb("http://www.imdb.com/title/tt1565010/").build();
        new DigitalContent.Builder("Gotterdammerung").opera().at("Royal Danish Theatre").on("01/01/2006").dvd().spaSubs().iso().seenByFede().discBox(5, 4).imdb("http://www.imdb.com/title/tt2199386/").build();
        new DigitalContent.Builder("Gotterdammerung").opera().at("Palacio de las Artes Reina Sofía").on("01/01/2009").dvd().spaSubs().mkv().discBox(0, 0).imdb("http://www.imdb.com/title/tt2368889/").build();
        new DigitalContent.Builder("Lohengrin").opera().at("Bayerische Staatsoper").on("01/01/2009").dvd().spaSubs().iso()
                .discBox(6, 8)
                .discBox(7, 8)
                .imdb("http://www.imdb.com/title/tt1570624/").build();
        new DigitalContent.Builder("Parsifal").opera().atTheMet().on("02/03/2013").fullHD().engSubs().mkv().seenByFede()
                .discBox(10, 11)
                .discBox(4, 12)
                .imdb("http://www.imdb.com/title/tt2749072/").build();
        new DigitalContent.Builder("Rienzi").opera().at("Berlin State Opera").on("01/01/2010").dvd().spaSubs().iso().seenByFede()
                .discBox(2, 9)
                .discBox(3, 9)
                .imdb("http://www.imdb.com/title/tt1961481/").build();
        new DigitalContent.Builder("Siegfried").opera().at("Royal Danish Theatre").on("01/01/2006").dvd().spaSubs().iso().seenByFede().discBox(4, 4).imdb("http://www.imdb.com/title/tt2199376/").build();
        new DigitalContent.Builder("Siegfried").opera().at("Palacio de las Artes Reina Sofía").on("01/01/2009").dvd().spaSubs().mkv().discBox(0, 0).imdb("http://www.imdb.com/title/tt2369295/").build();
        new DigitalContent.Builder("Tannhauser").opera().at("Festspielhaus Baden-Baden").on("01/01/2008").fullHD().spaSubs().mkv().discBox(10, 4).imdb("http://www.imdb.com/title/tt2222864/").build();
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
        dto.setSeenByFede(dc.isSeenBy(Repository.PERSON.findById("Federico")));
        dto.setSubtitles(dc.getSubtitle() != null ? dc.getSubtitle().toString() : "");
        dto.setTitles(toString(dc.getTitles()));
        dto.setVenues(toString(dc.getVenues()));
        return dto;
    }

    public static List<String> toString(Collection<?> col) {
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
            MediumContentDTO mDto = new MediumContentDTO();
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
}
