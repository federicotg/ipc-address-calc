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

import java.util.Set;

/**
 * Theatre, studio or outdoor stage where the performance took place.
 *
 * @author fede
 */
public class Venue {

    public static class Builder {

        private final String name;
        private String cityName;
        private Country country;

        public Builder(String name) {
            this.name = name;
        }

        public Builder city(String cityName) {
            this.cityName = cityName;
            return this;
        }

        public Builder spain() {
            this.country = Country.SPAIN;
            return this;
        }

        public Builder uk() {
            this.country = Country.UK;
            return this;
        }

        public Builder russia() {
            this.country = Country.RUSSIA;
            return this;
        }

        public Builder germany() {
            this.country = Country.GERMANY;
            return this;
        }

        public Builder usa() {
            this.country = Country.USA;
            return this;
        }

        public Builder australia() {
            this.country = Country.AUSTRALIA;
            return this;
        }

        public Builder italy() {
            this.country = Country.ITALY;
            return this;
        }

        public Builder country(Country c) {
            this.country = c;
            return this;
        }

        public Venue build() {
            Venue v = new Venue(this.name);

            City city = Repository.CITY.findById(this.cityName);
            if (city == null) {
                city = new City(this.cityName, this.country);
                Repository.CITY.add(city);
            }

            v.setCity(city);
            Repository.VENUE.add(v);
            return v;
        }

    }

    private String name;

    private City city;

    private Set<WebResource> resources;

    private Venue(String name) {
        this.name = name;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<WebResource> getResources() {
        return resources;
    }

    public void setResources(Set<WebResource> resources) {
        this.resources = resources;
    }

}
