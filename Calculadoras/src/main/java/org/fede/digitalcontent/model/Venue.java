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

import java.util.HashSet;
import java.util.Objects;
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
        private Double lat;
        private Double lon;
        private String wikipedia;

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

        public Builder latLong(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
            return this;
        }

        public Builder wiki(String uri) {
            this.wikipedia = uri;
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
            if (lat != null && lon != null) {
                v.setLatLon(new LatLon(this.lat, this.lon));
            }
            if (this.wikipedia != null) {
                v.addWikipedia(this.wikipedia);
            }

            Repository.VENUE.add(v);
            return v;
        }

    }

    private final String name;

    private City city;

    private Set<WebResource> resources = new HashSet<>();

    private LatLon latLon;

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

//    public void setName(String name) {
//        this.name = name;
//    }
    public Set<WebResource> getResources() {
        return resources;
    }

    public void setResources(Set<WebResource> resources) {
        this.resources = resources;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + Objects.hashCode(this.name);
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
        final Venue other = (Venue) obj;
        return Objects.equals(this.name, other.name);
    }

    public LatLon getLatLon() {
        return latLon;
    }

    public void setLatLon(LatLon latLon) {
        this.latLon = latLon;
    }

    public void addWikipedia(String uri) {
        this.resources.add(new WebResource(uri, WebResourceType.WIKIPEDIA));
    }

    public String getWikipedia() {
        for (WebResource r : this.resources) {
            if (r.getType() == WebResourceType.WIKIPEDIA) {
                return r.getUri();
            }
        }
        return null;
    }

    public String getCountryName() {
        return this.getCity().getCountryName();
    }

    public String getCityName() {
        return this.getCity().getName();
    }
}
