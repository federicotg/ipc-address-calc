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

/**
 *
 * @author fede
 */
public class DigitalContentDTO implements Comparable<DigitalContentDTO>{

    private String box;
    private String opusType;
    private String title;
    private String musicBy;
    private Date date;
    private String venue;
    private String quality;
    private String language;
    private String subtitles;
    private String format;
    private String imdb;
    private boolean seenByFede;
    private boolean seenByAnaMaria;

    public String getBox() {
        return box;
    }

    public void setBox(String box) {
        this.box = box;
    }

    public String getOpusType() {
        return opusType;
    }

    public void setOpusType(String opusType) {
        this.opusType = opusType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMusicBy() {
        return musicBy;
    }

    public void setMusicBy(String musicBy) {
        this.musicBy = musicBy;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
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
        if(!this.opusType.equals(o.opusType)){
            return this.opusType.compareTo(o.opusType);
        }
        if(!this.musicBy.equals(o.musicBy)){
            return this.musicBy.compareTo(o.musicBy);
        }
        if(!this.title.equals(o.title)){
            return this.title.compareTo(o.title);
        }
        return this.date.compareTo(o.date);
    }
    
}
