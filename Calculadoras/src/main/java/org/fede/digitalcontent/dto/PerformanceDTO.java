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

/**
 *
 * @author fede
 */
public class PerformanceDTO implements Comparable<PerformanceDTO> {

    private String opusTitle;
    private String year;
    private String imdb;

    public PerformanceDTO(String opusTitle, String year, String imdb) {
        this.opusTitle = opusTitle;
        this.year = year;
        this.imdb = imdb;
    }

    public String getOpusTitle() {
        return opusTitle;
    }

    public void setOpusTitle(String opusTitle) {
        this.opusTitle = opusTitle;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getImdb() {
        return imdb;
    }

    public void setImdb(String imdb) {
        this.imdb = imdb;
    }

    @Override
    public int compareTo(PerformanceDTO o) {
        if (!this.year.equals(o.getYear())) {
            return this.year.compareTo(o.getYear());
        }
        return this.opusTitle.compareTo(o.getOpusTitle());
    }

}
