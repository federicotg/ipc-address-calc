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
package org.fede.digitalcontent.dto;

import java.util.Date;
import java.util.List;
import org.fede.util.Util;

/**
 *
 * @author fede
 */
public class DigitalContentDTO implements Comparable<DigitalContentDTO>{

    private List<String> boxes;
    private List<String> opusTypes;
    private String title;
    private List<String> musicBy;
    private Date date;
    private List<String> venues;
    private String quality;
    private String language;
    private String subtitles;
    private String format;
    private String imdb;
    private boolean seenByFede;
    private boolean seenByAnaMaria;

    public List<String> getBoxes() {
        return boxes;
    }

    public void setBoxes(List<String> boxes) {
        this.boxes = boxes;
    }
    
    public String getBox(){
        return Util.list(this.boxes);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getMusicBy() {
        return musicBy;
    }

    public void setMusicBy(List<String> musicBy) {
        this.musicBy = musicBy;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<String> getOpusTypes() {
        return opusTypes;
    }

    public void setOpusTypes(List<String> opusTypes) {
        this.opusTypes = opusTypes;
    }

    public List<String> getVenues() {
        return venues;
    }

    public void setVenues(List<String> venues) {
        this.venues = venues;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getSubtitles() {
        return subtitles;
    }

    public void setSubtitles(String subtitles) {
        this.subtitles = subtitles;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getImdb() {
        return imdb;
    }

    public void setImdb(String imdb) {
        this.imdb = imdb;
    }

    public boolean isSeenByFede() {
        return seenByFede;
    }

    public void setSeenByFede(boolean seenByFede) {
        this.seenByFede = seenByFede;
    }

    public boolean isSeenByAnaMaria() {
        return seenByAnaMaria;
    }

    public void setSeenByAnaMaria(boolean seenByAnaMaria) {
        this.seenByAnaMaria = seenByAnaMaria;
    }

    @Override
    public int compareTo(DigitalContentDTO o) {
        if(!this.opusTypes.equals(o.getOpusTypes())){
            return this.opusTypes.get(0).compareTo(o.getOpusTypes().get(0));
        }
        if(!this.musicBy.equals(o.getMusicBy())){
            return this.musicBy.get(0).compareTo(o.getMusicBy().get(0));
        }
        if(!this.title.equals(o.getTitle())){
            return this.title.compareTo(o.getTitle());
        }
        return this.date.compareTo(o.getDate());
    }
    
}
