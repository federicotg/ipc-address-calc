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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.fede.digitalcontent.dto.DigitalContentDTO;
import org.fede.digitalcontent.model.Country;
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
import org.fede.digitalcontent.model.Repository;
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

        this.initVenues();
        this.initBallets();
        this.initOperas();
        this.initBalletVideos();

        new DigitalContent.Builder("La Gioconda")
                .opera().atParis().on("13/05/2013").fullHD().spaSubs().mkv()
                .seenByFede().discBox(15, 0).build();

        new DigitalContent.Builder("Rusalka")
                .opera().atTheMet().on("08/02/2014").fullHD().engSubs().mkv()
                .discBox(15, 0).build();

        new DigitalContent.Builder("I Puritani")
                .opera().atParis().on("09/12/2013").fullHD().spaSubs().mkv()
                .discBox(15, 0).build();

        new DigitalContent.Builder("Prince Igor")
                .opera().atTheMet().on("01/03/2014").fullHD().engSubs().mkv()
                .discBox(15, 0).build();

        new DigitalContent.Builder("The Nose")
                .opera().atTheMet().on("26/10/2013").fullHD().engSubs().mkv()
                .discBox(15, 0).build();

        new DigitalContent.Builder("Don Pasquale")
                .opera().atGlyndebourne().on("06/08/2013").fullHD().engSubs().mkv()
                .discBox(15, 0).build();

        new DigitalContent.Builder("L'elisir d'amore")
                .opera().atTheMet().on("13/10/2012").fullHD().engSubs().mkv()
                .discBox(15, 0).build();

        new DigitalContent.Builder("Maria Stuarda")
                .opera().atTheMet().on("19/01/2013").fullHD().engSubs().mkv()
                .discBox(15, 0).build();

        new DigitalContent.Builder("Aida")
                .opera().atTheMet().on("15/12/2012").fullHD().engSubs().mkv()
                .discBox(15, 0).build();

        new DigitalContent.Builder("Falstaff")
                .opera().atTheMet().on("15/12/2013").fullHD().engSubs().mkv()
                .discBox(15, 0).build();

        new DigitalContent.Builder("La Traviata")
                .opera().atParis().on("17/06/2014").fullHD().engSubs().mkv()
                .discBox(15, 0).build();

        new DigitalContent.Builder("La Traviata")
                .opera().atGlyndebourne().on("10/08/2014").fullHD().engSubs().mkv()
                .discBox(15, 0).build();

        new DigitalContent.Builder("Macbeth")
                .opera().atParis().on("04/04/2009").fullHD().engSubs().mkv()
                .discBox(15, 0).build();

        new DigitalContent.Builder("Macbeth")
                .opera().atTheMet().on("11/10/2014").fullHD().engSubs().mkv()
                .discBox(15, 0).build();

        new DigitalContent.Builder("Nabucco")
                .opera().atRoh().on("26/04/2013").fullHD().engSubs().mkv()
                .discBox(15, 0).build();

        new DigitalContent.Builder("Otello")
                .opera().atTheMet().on("28/10/2012").fullHD().engSubs().mkv()
                .discBox(15, 0).build();

        new DigitalContent.Builder("Otello").opera()
                .at("Teatro di San Carlo").on("22/04/2014").fullHD().engSubs().mkv()
                .discBox(15, 0).build();

        new DigitalContent.Builder("Rigoletto").opera()
                .at("Ópera de Zürich").on("01/01/1901").fullHD().engSubs().mkv()
                .discBox(15, 0).build();

        new DigitalContent.Builder("Rigoletto").opera()
                .atTheMet().on("16/02/2013").fullHD().engSubs().mkv()
                .discBox(15, 0).build();

        new DigitalContent.Builder("Un Ballo in Maschera").opera()
                .atTheMet().on("08/12/2012").fullHD().engSubs().mkv()
                .discBox(15, 0).build();

        new DigitalContent.Builder("Messiah").oratorio()
                .at("Ópera de Zürich").on("01/01/1901").fullHD().engSubs().mkv()
                .discBox(15, 0).build();

        new DigitalContent.Builder("Les Troyens").opera()
                .atTheMet().on("05/01/2013").fullHD().engSubs().mkv()
                .discBox(15, 0).build();

        new DigitalContent.Builder("Werther").opera()
                .atTheMet().on("15/03/2014").fullHD().engSubs().mkv()
                .discBox(15, 0).build();

        new DigitalContent.Builder("Lakme").opera()
                .at("Sydney Opera House").on("01/01/1901").fullHD().engSubs().mkv()
                .discBox(15, 0).build();

        new DigitalContent.Builder("Così Fan Tutte").opera()
                .atTheMet().on("26/04/2014").fullHD().engSubs().mkv()
                .discBox(15, 0).build();

        new DigitalContent.Builder("La Clemenza di Tito").opera()
                .atTheMet().on("01/12/2012").fullHD().engSubs().mkv()
                .discBox(15, 0).build();

        new DigitalContent.Builder("La Bohème").opera()
                .atRoh().on("15/01/2013").fullHD().engSubs().mkv()
                .discBox(15, 0).build();

        new DigitalContent.Builder("La Fanciulla del West").opera()
                .atParis().on("10/02/2014").fullHD().engSubs().mkv()
                .discBox(15, 0).build();

        new DigitalContent.Builder("Manon Lescaut").opera()
                .atRoh().on("24/06/2014").fullHD().engSubs().mkv()
                .discBox(15, 0).build();

        new DigitalContent.Builder("Tosca").opera()
                .atTheMet().on("09/11/2013").fullHD().engSubs().mkv()
                .discBox(15, 0).build();

        new DigitalContent.Builder("Tosca").opera()
                .atParis().on("16/10/2014").fullHD().engSubs().mkv()
                .discBox(15, 0).build();

        new DigitalContent.Builder("La Cenerentola").opera()
                .atTheMet().on("10/05/2014").fullHD().engSubs().mkv()
                .discBox(15, 0).build();

        new DigitalContent.Builder("La Donna del Lago").opera()
                .atRoh().on("27/05/2013").fullHD().engSubs().mkv()
                .discBox(15, 0).build();

        new DigitalContent.Builder("Eugene Onegin").opera()
                .atRoh().on("20/02/2013").fullHD().engSubs().mkv()
                .discBox(15, 0).build();

        new DigitalContent.Builder("Eugene Onegin").opera()
                .atTheMet().on("05/10/2013").fullHD().engSubs().mkv()
                .discBox(15, 0).build();

        new DigitalContent.Builder("Parsifal").opera()
                .atTheMet().on("02/03/2013").fullHD().engSubs().mkv()
                .discBox(15, 0).build();

        new DigitalContent.Builder("Francesca da Rimini").opera()
                .atTheMet().on("16/03/2013").fullHD().engSubs().mkv()
                .discBox(15, 0).build();
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

    }

    private void initVenues() {
        new Venue.Builder("Teatro di San Carlo").city("Nápoles").italy().build();
        new Venue.Builder("alla Scala").city("Milano").italy().build();
        new Venue.Builder("Teatro Donizetti").city("Bergamo").italy().build();
        new Venue.Builder("Teatro Comunale di Firenze").city("Florencia").italy().build();

        new Venue.Builder("Palacio de las Artes Reina Sofía").city("Valencia").spain().build();
        new Venue.Builder("Gran Teatro del Liceo").city("Barcelona").spain().build();
        new Venue.Builder("Teatro Real").city("Madrid").spain().build();

        new Venue.Builder("Waldbühne").city("Berlín").germany().build();
        new Venue.Builder("Festspielhaus Baden-Baden").city("Baden-Baden").germany().build();

        new Venue.Builder("Bolshói").city("Moscú").russia().build();
        new Venue.Builder("Mariinsky").city("San Petersburgo").russia().build();

        new Venue.Builder("ROH").city("Londres").uk().build();
        new Venue.Builder("Glyndebourne").city("Glyndebourne").uk().build();

        new Venue.Builder("The Met").city("New York").usa().build();
        new Venue.Builder("War Memorial Opera House").city("San Francisco").usa().build();

        new Venue.Builder("ONP").city("Paris").country(FRANCE).build();

        new Venue.Builder("Royal Danish Theatre").city("Copenhagen").country(DENMARK).build();

        new Venue.Builder("Sydney Opera House").city("Sydney").australia().build();

        new Venue.Builder("Ópera de Zürich").city("Zürich").country(SWITZERLAND).build();

        new Venue.Builder("Musikverein").city("Viena").country(Country.AUSTRIA).build();
    }

    private void initOperas() {
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
        new Opus.Builder("Eugene Onegin").russian().opera().by("Tchaikovsky").build();
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
                .discBox(0, 3)
                .imdb("http://www.imdb.com/title/tt1596783/")
                .build();
        new DigitalContent.Builder("Giselle").ballet().atRoh().on("27/01/2014").fullHD().mkv().seenByAnaMaria().discBox(0, 15).build();
        new DigitalContent.Builder("Le Corsaire").ballet().atBolshoi().on("11/03/2012").hd720().mkv().discBox(0, 3)
                .imdb("http://www.imdb.com/title/tt2798336/")
                .build();
        new DigitalContent.Builder("Raymonda").ballet().atBolshoi().on("24/06/2012").hd720().mkv().discBox(0, 12)
                .seenByFede()
                .imdb("http://www.imdb.com/title/tt2798390/")
                .build();
        new DigitalContent.Builder("Spartacus").ballet().atBolshoi().on("01/07/08").hd720().mkv().discBox(0, 3)
                .imdb("http://www.imdb.com/title/tt1890538/")
                .seenByAnaMaria()
                .build();
        new DigitalContent.Builder("Spartacus").ballet().atBolshoi().on("20/10/13").fullHD().mkv()
                .discBox(0, 11)
                .seenByAnaMaria()
                .build();
        new DigitalContent.Builder("Le Palais de Cristal (Symphony in C)").ballet().atParis().on("06/03/2014").fullHD().mkv()
                .discBox(0, 4)
                .build();

        new DigitalContent.Builder("La Esmeralda").ballet().atBolshoi().on("09/10/2011").hd720().mkv().discBox(0, 11)
                .imdb("http://www.imdb.com/title/tt2798094/")
                .build();

        new DigitalContent.Builder("La Fille du Pharaon").ballet().atBolshoi().on("24/11/2012").fullHD().mkv()
                .discBox(0, 11)
                .discBox(0, 12)
                .build();
        new DigitalContent.Builder("Marco Spada").ballet().atBolshoi().on("30/03/2014").fullHD().mkv()
                .discBox(0, 4)
                .discBox(0, 15)
                .imdb("").build();
        new DigitalContent.Builder("The Bright Stream").ballet().atBolshoi().on("29/04/2012").hd720().mkv()
                .discBox(0, 6)
                .seenByFede()
                .imdb("http://www.imdb.com/title/tt2798354/")
                .build();
        new DigitalContent.Builder("Jewels").ballet().atBolshoi().on("19/01/2014").fullHD().mkv()
                .discBox(0, 15)
                .seenByAnaMaria()
                .build();
        new DigitalContent.Builder("La Sylphide").ballet().atBolshoi().on("29/09/2012").hd720().mkv()
                .discBox(0, 6)
                .seenByAnaMaria()
                .build();
        new DigitalContent.Builder("Alice's Adventures In Wonderland")
                .ballet().atRoh().on("28/03/2013").fullHD().mkv()
                .discBox(11, 0)
                .discBox(12, 0)
                .build();
        new DigitalContent.Builder("Winter's Tale").ballet().atRoh().on("28/04/14").fullHD().mkv()
                .discBox(4, 0)
                .build();
        new DigitalContent.Builder("Coppelia").ballet().atBolshoi().on("29/05/2011").hd720().mkv().discBox(3, 0)
                .imdb("http://www.imdb.com/title/tt1833535/")
                .build();
        new DigitalContent.Builder("La Source").ballet().atParis().on("11/04/2011").hd720().mkv().discBox(7, 0).build();
        new DigitalContent.Builder("Lost Illusions").ballet().atBolshoi().on("20/10/2013").fullHD().mkv().discBox(15, 0).build();

        new DigitalContent.Builder("Don Quichotte").ballet().atBolshoi().on("18/12/2012").fullHD().mkv().discBox(11, 0).build();
        new DigitalContent.Builder("Don Quichotte").ballet().atParis().on("16/10/2013").fullHD().mkv().discBox(15, 0).build();

        new DigitalContent.Builder("La Bayadère").ballet().atBolshoi().on("22/03/2012").hd720().mkv().discBox(11, 0).build();
        new DigitalContent.Builder("La Bayadère").ballet().atBolshoi().on("27/01/2013").fullHD().mkv().discBox(14, 0).build();

        new DigitalContent.Builder("Notre Dame de Paris").ballet().atBolshoi().on("14/02/2013").fullHD().mkv().discBox(13, 0).build();
        new DigitalContent.Builder("Daphnis et Chloé").ballet().atBolshoi().on("06/03/2014").fullHD().mkv().discBox(15, 0).build();
        new DigitalContent.Builder("Romeo and Juliet").ballet().atRoh().on("10/01/2012").fullHD().mkv().discBox(3, 0).build();
        new DigitalContent.Builder("Romeo and Juliet").ballet().atBolshoi().on("12/05/2013").fullHD().mkv().discBox(14, 0).build();
        new DigitalContent.Builder("Swan Lake").ballet().atBolshoi().on("19/06/2011").hd720().mkv().discBox(2, 0).build();
        new DigitalContent.Builder("Swan Lake").ballet().at("Mariinsky").on("06/06/2013").fullHD().mkv().discBox(13, 0).discBox(14, 0).build();
        new DigitalContent.Builder("The Nutcracker").ballet().atBolshoi().on("18/12/2011").hd720().mkv().discBox(3, 0).build();
        new DigitalContent.Builder("The Nutcracker").ballet().atRoh().on("12/12/2013").fullHD().mkv().discBox(15, 0).build();

        new DigitalContent.Builder("The Sleeping Beauty").ballet().atBolshoi().on("20/11/2011").hd720().mkv().discBox(5, 0).build();
        new DigitalContent.Builder("The Sleeping Beauty").ballet().atRoh().on("15/12/2011").hd720().mkv().discBox(2, 0).build();
        new DigitalContent.Builder("The Sleeping Beauty").ballet().atRoh().on("19/03/2014").fullHD().mkv().discBox(4, 0).build();
        new DigitalContent.Builder("La fille mal gardée").ballet().atRoh().on("16/05/2012").hd720().mkv().discBox(3, 0).build();

    }

    @Override
    public Iterable<StorageBox> getAllBoxes() {
        return Repository.STORAGEBOX.findAll();
    }

    @Override
    public List<DigitalContentDTO> getDigitalContentReport() {
        Set<DigitalContent> allContent = DigitalContentRepository.DIGITALCONTENT.findAll();
        List<DigitalContentDTO> answer = new ArrayList<>(allContent.size());
        for (DigitalContent dc : allContent) {
            DigitalContentDTO dto = new DigitalContentDTO();
            List<String> boxNames = new ArrayList<>();
            for(StorageBox box : Repository.STORAGEBOX.boxesContaining(dc)){
                boxNames.add(box.getName());
            }
            dto.setBox(list(boxNames));
            dto.setDate(dc.getDate());
            dto.setFormat(dc.getFormat().name());
            dto.setImdb(dc.getImdb());
            dto.setLanguage(list(dc.getLanguages()));
            dto.setMusicBy(list(dc.getMusicComposers()));
            dto.setOpusType(list(dc.getOpusTypes()));
            dto.setQuality(dc.getQuality().toString());
            dto.setSeenByAnaMaria(dc.isSeenBy(Repository.PERSON.findById("Ana María")));
            dto.setSeenByFede(dc.isSeenBy(Repository.PERSON.findById("Federico")));
            dto.setSubtitles(dc.getSubtitle() != null ? dc.getSubtitle().toString() : "");
            dto.setTitle(list(dc.getTitles()));
            dto.setVenue(list(dc.getVenues()));
            answer.add(dto);
        }
        Collections.sort(answer);
        return answer;

    }

    private static <T> String list(Collection<T> elements) {
        return list(elements, ", ");

    }

    private static <T> String list(Collection<T> elements, String separator) {
        StringBuilder sb = new StringBuilder(elements.size() * 10);
        for (Iterator<T> it = elements.iterator(); it.hasNext();) {
            sb.append(it.next().toString());
            if (it.hasNext()) {
                sb.append(separator);
            }
        }
        return sb.toString();
    }
}
