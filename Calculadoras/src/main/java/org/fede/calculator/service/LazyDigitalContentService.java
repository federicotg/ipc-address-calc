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
import javax.annotation.PostConstruct;
import static org.fede.digitalcontent.model.Country.DENMARK;
import static org.fede.digitalcontent.model.Country.FRANCE;
import static org.fede.digitalcontent.model.Country.SWITZERLAND;
import org.fede.digitalcontent.model.DigitalContent;
import org.fede.digitalcontent.model.StorageBox;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import static org.fede.digitalcontent.model.Repository.*;
import org.fede.digitalcontent.model.Language;
import org.fede.digitalcontent.model.Opus;
import org.fede.digitalcontent.model.Venue;

/**
 *
 * @author fede
 */
@Service
@Lazy
public class LazyDigitalContentService implements DigitalContentService {

    @PostConstruct
    public void initBasicObjects() throws ParseException {
       
        new Venue.Builder("ROH").city("Londres").uk();
        new Venue.Builder("Glyndebourne").city("Glyndebourne").uk();
        new Venue.Builder("The Met").city("New York").usa();
        new Venue.Builder("ONP").city("Paris").country(FRANCE);
        new Venue.Builder("Royal Danish Theatre").city("Copenhagen").country(DENMARK);
        new Venue.Builder("Palacio de las Artes Reina Sofía").city("Valencia").spain();
        new Venue.Builder("Gran Teatro del Liceo de Barcelona").city("Barcelona").spain();
        new Venue.Builder("alla Scala").city("Milano").italy();
        new Venue.Builder("Festspielhaus Baden-Baden").city("Baden-Baden").germany();
        new Venue.Builder("Sydney Opera House").city("Sydney").australia();
        new Venue.Builder("Bolshói").city("Moscú").russia();
        new Venue.Builder("Mariinski").city("San Petersburgo").russia();
        new Venue.Builder("Teatro di San Carlo").city("Nápoles").italy();
        new Venue.Builder("Ópera de Zürich").city("Zürich").country(SWITZERLAND);
        
 
        new Opus.Builder("Macbeth",
                "Aida",
                "Rigoletto",
                "Nabucco",
                "La Traviata",
                "Il Trovatore",
                "Simón Boccanegra",
                "Otello",
                "Un Ballo in Maschera",
                "Falstaff",
                "La Forza del Destino",
                "Don Carlo",
                "I Due Foscari")
                .italian().opera().by("Giuseppe Verdi");

        new Opus.Builder("Parsifal",
                "Rienzi",
                "Das Rheingold",
                "Die Walküre",
                "Gotterdammerung",
                "Siegfried",
                "Tannhauser",
                "Tristan und Ilsode",
                "Falstaff",
                "La Forza del Destino")
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
        new Opus.Builder("Messiah").english().oratorio().by("Händel").build();
        new Opus.Builder("Eugene Onegin").russian().opera().by("Tchaikovsky").build();
        new Opus.Builder("Francesca da Rimini").italian().opera().by("Zandonai").build();
        new Opus.Builder("Rusalka").language(Language.CZECH).opera().by("Dvorak").build();

        new Opus.Builder("Così fan tutte",
                "Don Giovanni",
                "Il Sogno di Scipione",
                "La Clemenza di Tito",
                "Le nozze di Figaro")
                .italian().opera().by("W. A. Mozart");

        new Opus.Builder("La Flauta Mágica", "El rapto en el serrallo")
                .german().opera().by("W. A. Mozart");

        new Opus.Builder("Gianni Schicchi",
                "Il Trittico",
                "La Bohème",
                "La Fanciulla del West",
                "La Rondine",
                "Madama Butterfly",
                "Manon Lescaut",
                "Tosca",
                "Turandot")
                .italian().opera().by("Giacomo Puccini");

        new Opus.Builder("Armida",
                "Il Barbiere di Siviglia",
                "Il turco in Italia",
                "L'Italiana in Algeri",
                "La Cenerentola",
                "La Donna del Lago",
                "Sigismondo",
                "Zelmira")
                .italian().opera().by("Rossini");

        new DigitalContent.Builder("La Gioconda")
                .opera()
                .atParis()
                .on("13/05/2013")
                .fullHD()
                .spaSubs()
                .mkv()
                .seenByFede()
                .bdr("15-01")
                .build();

        new DigitalContent.Builder("Rusalka")
                .opera().atTheMet().on("08/02/2014").fullHD().engSubs().mkv().bdr("15-01").build();

        new DigitalContent.Builder("I Puritani")
                .opera().atParis().on("09/12/2013").fullHD().spaSubs().mkv().bdr("15-01").build();
        
        new DigitalContent.Builder("Prince Igor")
                .opera().atTheMet().on("01/03/2014").fullHD().engSubs().mkv().bdr("15-01").build();
        
        new DigitalContent.Builder("The Nose")
                .opera().atTheMet().on("26/10/2013").fullHD().engSubs().mkv().bdr("15-01").build();
        
        new DigitalContent.Builder("Don Pasquale")
                .opera().atGlyndebourne().on("06/08/2013").fullHD().engSubs().mkv().bdr("15-01").build();
       
        new DigitalContent.Builder("L'elisir d'amore")
                .opera().atTheMet().on("13/10/2012").fullHD().engSubs().mkv().bdr("15-01").build();
        
        new DigitalContent.Builder("Maria Stuarda")
                .opera().atTheMet().on("19/01/2013").fullHD().engSubs().mkv().bdr("15-01").build();
        
        new DigitalContent.Builder("Aida")
                .opera().atTheMet().on("15/12/2012").fullHD().engSubs().mkv().bdr("15-01").build();
        
        new DigitalContent.Builder("Falstaff")
                .opera().atTheMet().on("15/12/2013").fullHD().engSubs().mkv().bdr("15-01").build();
        
        new DigitalContent.Builder("La Traviata")
                .opera().atParis().on("17/06/2014").fullHD().engSubs().mkv().bdr("15-01").build();
        
        new DigitalContent.Builder("La Traviata")
                .opera().atGlyndebourne().on("10/08/2014").fullHD().engSubs().mkv().bdr("15-01").build();
        
        new DigitalContent.Builder("Macbeth")
                .opera().atParis().on("04/04/2009").fullHD().engSubs().mkv().bdr("15-01").build();
        
        new DigitalContent.Builder("Macbeth")
                .opera().atTheMet().on("11/10/2014").fullHD().engSubs().mkv().bdr("15-01").build();
        
        new DigitalContent.Builder("Nabucco")
                .opera().atRoh().on("26/04/2013").fullHD().engSubs().mkv().bdr("15-01").build();
        
        
        
       /* this.createOperaPerformanceMKV1080("Otello", "The Met", "28/10/2012");
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
        this.createOperaPerformanceMKV1080("Francesca da Rimini", "The Met", "16/03/2013");*/

    }

    @Override
    public Iterable<StorageBox> getAllBoxes() {
        return STORAGEBOX.findAll();
    }

}
