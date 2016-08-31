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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author fede
 */
public class VenueDetailDTO {

    private String name;
    private String imdb;
    private List<PerformanceDTO> performances = new ArrayList<>();
    private String wikipedia;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImdb() {
        return imdb;
    }

    public void setImdb(String imdb) {
        this.imdb = imdb;
    }

    public List<PerformanceDTO> getPerformances() {
        return performances;
    }

    public void setPerformances(List<PerformanceDTO> performances) {
        this.performances = performances;
    }

    public void addPerformance(String title, String year, String imdb) {
        this.performances.add(new PerformanceDTO(title, year, imdb));
    }

    public String getWikipedia() {
        return wikipedia;
    }

    public void setWikipedia(String wikipedia) {
        this.wikipedia = wikipedia;
    }

    public void doneAddingPerformances() {
        Collections.sort(this.performances);
    }
}
