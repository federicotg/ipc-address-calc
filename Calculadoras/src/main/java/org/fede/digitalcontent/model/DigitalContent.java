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
package org.fede.digitalcontent.model;

import org.fede.util.Pair;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import static org.fede.digitalcontent.model.Repository.OPUS;
import static org.fede.digitalcontent.model.Repository.VENUE;

/**
 *
 * @author fede
 */
public class DigitalContent {

    public static class Builder {

        private final DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        private final NumberFormat nf = NumberFormat.getIntegerInstance();

        private String title;
        private Set<Performance> performances;
        private String venue;
        private String date;
        private FormatType formatType;
        private Quality quality;
        private OpusType opusType;
        private Language subtitle;
        private final Set<String> singers;
        private final Set<String> viewers;
        private final Set<String> imdb;
        private final Set<Pair<String, String>> discs;

        private Builder() {
            this.singers = new HashSet<>();
            this.viewers = new HashSet<>();
            this.imdb = new HashSet<>();
            this.discs = new HashSet<>();
            nf.setMinimumIntegerDigits(2);
        }

        public Builder(Set<Performance> performances) {
            this();
            this.performances = performances;
        }

        public Builder(String title) {
            this();
            this.title = title;
        }

        public Builder at(String venue) {
            this.venue = venue;
            return this;
        }

        public Builder atAllaScala() {
            this.venue = "alla Scala";
            return this;
        }

        public Builder atZurich() {
            this.venue = "Ópera de Zürich";
            return this;
        }

        public Builder atMariinsky() {
            this.venue = "Mariinsky";
            return this;
        }

        public Builder atRoh() {
            this.venue = "ROH";
            return this;
        }

        public Builder atTheMet() {
            this.venue = "The Met";
            return this;
        }

        public Builder atParis() {
            this.venue = "Opéra Bastille";
            return this;
        }

        public Builder seenByFede() {
            this.viewers.add("Federico");
            return this;
        }

        public Builder seenByAnaMaria() {
            this.viewers.add("Ana María");
            return this;
        }

        public Builder seenByElsa() {
            this.viewers.add("Elsa");
            return this;
        }

        public Builder seenByCristina() {
            this.viewers.add("Cristina");
            return this;
        }

        public Builder atBolshoi() {
            this.venue = "Bolshói";
            return this;
        }

        public Builder atGlyndebourne() {
            this.venue = "Glyndebourne";
            return this;
        }

        public Builder starringNetrebko() {
            this.singers.add("Anna Netrebko");
            return this;
        }

        public Builder starringFleming() {
            this.singers.add("Reneé Fleming");
            return this;
        }

        public Builder on(String date) {
            this.date = date;
            return this;
        }

        public Builder opera() {
            this.opusType = OpusType.OPERA;
            return this;
        }

        public Builder ballet() {
            this.opusType = OpusType.BALLET;
            return this;
        }

        public Builder oratorio() {
            this.opusType = OpusType.ORATORIO;
            return this;
        }

        public Builder episode() {
            this.opusType = OpusType.EPISODE;
            return this;
        }
        
        public Builder sport() {
            this.opusType = OpusType.SPORT;
            return this;
        }
        
        public Builder game() {
            this.opusType = OpusType.GAME;
            return this;
        }
        
        
        public Builder type(OpusType type) {
            this.opusType = type;
            return this;
        }

        public Builder mkv() {
            this.formatType = FormatType.MKV;
            return this;
        }

        public Builder br() {
            this.formatType = FormatType.BLURAY;
            this.quality = Quality.HD1080;
            return this;
        }

        public Builder iso() {
            this.formatType = FormatType.ISO;
            return this;
        }

        public Builder dvdFormat() {
            this.formatType = FormatType.DVD;
            return this;
        }

        public Builder flac() {
            this.formatType = FormatType.FLAC;
            return this;
        }

        public Builder engSubs() {
            this.subtitle = Language.ENGLISH;
            return this;
        }

        public Builder spaSubs() {
            this.subtitle = Language.SPANISH;
            return this;
        }

        public Builder fullHD() {
            this.quality = Quality.HD1080;
            return this;
        }

        public Builder hd720() {
            this.quality = Quality.HD720;
            return this;
        }

        public Builder dvd() {
            this.quality = Quality.DVD480;
            return this;
        }

        public Builder box(String boxName) {
            this.discs.add(new Pair<>(this.title, boxName));
            return this;
        }

        public Builder discBox(int disc, int box) {

            final String boxStr = nf.format(box);
            final String discStr = box + "-" + nf.format(disc);

            this.discs.add(new Pair<>(discStr, boxStr));

            return this;
        }

        public Builder imdb(String uri) {
            this.imdb.add(uri);
            return this;
        }

        public DigitalContent build() throws ParseException {

            DigitalContent dc = new DigitalContent();
            dc.setFormat(formatType);
            dc.setQuality(quality);
            dc.setSubtitle(subtitle);

            if (this.title != null) {

                Opus opus = OPUS.findById(new Pair<>(this.title, this.opusType));
                Venue place = VENUE.findById(this.venue);
                Date moment = date == null?null:df.parse(date);
                if (opus == null){// || place == null || moment == null || formatType == null) {
                    throw new IllegalArgumentException("Opus, Venue, Date and FormatType are required.");
                }

                Performance perf = new Performance(opus, place, moment);
                for (String singerName : this.singers) {
                    Person singer = Repository.PERSON.findById(singerName);
                    if (singer == null) {
                        singer = new Person(singerName);
                        Repository.PERSON.add(singer);
                    }
                    perf.addSinger(singer);
                }
                for (String viewerName : this.viewers) {
                    Person viewer = Repository.PERSON.findById(viewerName);
                    if (viewer == null) {
                        viewer = new Person(viewerName);
                        Repository.PERSON.add(viewer);
                    }
                    perf.addViewer(viewer);
                }
                for (String uri : this.imdb) {
                    perf.addImdb(uri);
                }
                Repository.PERFORMANCE.add(perf);
                Set<Performance> set = new HashSet<>();
                set.add(perf);
                dc.setPerformances(set);
            } else {
                for (Performance perf : this.performances) {
                    Repository.PERFORMANCE.add(perf);
                }
                dc.setPerformances(this.performances);
            }

            for (Pair<String, String> discBox : this.discs) {

                StorageMedium medium = Repository.STORAGE.findById(discBox.getFirst());
                if (medium == null) {
                    medium = new StorageMedium(discBox.getFirst());
                    Repository.STORAGE.add(medium);
                }
                medium.addContent(dc);
                StorageBox storageBox = Repository.STORAGEBOX.findById(discBox.getSecond());
                if (storageBox == null) {
                    storageBox = new StorageBox(discBox.getSecond());
                    Repository.STORAGEBOX.add(storageBox);
                }
                storageBox.addStorageMedium(medium);
            }
            Repository.DIGITALCONTENT.add(dc);
            return dc;
        }
    }

    private Set<Performance> performances;

    private Quality quality;

    private FormatType format;

    private Language subtitle;

    private DigitalContent() {
    }

    public Quality getQuality() {
        return quality;
    }

    public void setQuality(Quality quality) {
        this.quality = quality;
    }

    public FormatType getFormat() {
        return format;
    }

    public void setFormat(FormatType format) {
        this.format = format;
    }

    public Language getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(Language subtitle) {
        this.subtitle = subtitle;
    }

    public Set<Performance> getPerformances() {
        return performances;
    }

    public void setPerformances(Set<Performance> performances) {
        this.performances = performances;
    }

    public Date getDate() {
        return this.performances.iterator().next().getDate();
    }

    public String getImdb() {
        return this.performances.iterator().next().getImdb();
    }

    public Set<Language> getLanguages() {
        Set<Language> langs = new HashSet<>();
        for (Performance p : this.performances) {
            Language l = p.getLanguage();
            if (l != null) {
                langs.add(l);
            }
        }
        return langs;
    }

    public Set<Person> getMusicComposers() {
        Set<Person> composers = new HashSet<>();
        for (Performance p : this.performances) {
            composers.addAll(p.getMusicComposers());
        }
        return composers;
    }

    public Set<OpusType> getOpusTypes() {
        Set<OpusType> types = new HashSet<>();
        for (Performance p : this.performances) {
            types.add(p.getOpusType());
        }
        return types;
    }

    public boolean isSeenBy(Person person) {
        boolean seen = true;
        for (Performance p : this.performances) {
            seen &= p.isSeenBy(person);
        }
        return seen;
    }

    public Set<String> getTitles() {
        Set<String> titles = new HashSet<>();
        for (Performance p : this.performances) {
            titles.add(p.getTitle());
        }
        return titles;
    }

    public Set<Opus> getOpuses() {
        Set<Opus> answer = new HashSet<>();
        for (Performance p : this.performances) {
            answer.add(p.getOpus());
        }
        return answer;
    }

    public Set<String> getDetailedTitles() {
        Set<String> titles = new HashSet<>();
        for (Performance p : this.performances) {
            titles.add(p.getDetailedTitle());
        }
        return titles;
    }

    public Set<Venue> getVenues() {
        Set<Venue> venues = new HashSet<>();
        for (Performance p : this.performances) {
            venues.add(p.getVenue());
        }
        return venues;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.performances);
        hash = 97 * hash + Objects.hashCode(this.quality);
        hash = 97 * hash + Objects.hashCode(this.format);
        hash = 97 * hash + Objects.hashCode(this.subtitle);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DigitalContent other = (DigitalContent) obj;
        if (!Objects.equals(this.performances, other.performances)) {
            return false;
        }
        if (this.quality != other.quality) {
            return false;
        }
        if (this.format != other.format) {
            return false;
        }
        return this.subtitle == other.subtitle;
    }

    @Override
    public String toString() {
        return performances.toString();
    }

    public boolean includesOpus(String opusName) {
        for (Performance p : this.performances) {
            if (p.getOpus().getTitle().equals(opusName)) {
                return true;
            }
        }
        return false;
    }

    public boolean includesVenue(String venueName) {
        for (Performance p : this.performances) {
            if (p.getVenue().getName().equals(venueName)) {
                return true;
            }
        }
        return false;
    }

    public boolean includesComposer(String name) {
        for (Performance p : this.performances) {
            if (p.includesComposer(name)) {
                return true;
            }
        }
        return false;
    }

}
