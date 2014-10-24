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
import java.util.Set;

/**
 *
 * @author fede
 */
public class Opus {

    public static class Builder {

        private Opus opus;

        public Builder(String title) {
            this.opus = new Opus();
            this.opus.setTitle(title);
        }

        public Builder opera() {
            this.opus.setType(OpusType.OPERA);
            return this;
        }

        public Builder ballet() {
            this.opus.setType(OpusType.BALLET);
            return this;
        }

        public Builder type(OpusType type) {
            this.opus.setType(type);
            return this;
        }

        public Builder by(String name) {
            this.opus.addPerson(RoleType.COMPOSER, Factory.PERSON.findById(name));
            return this;
        }

        public Builder italian() {
            this.opus.setLanguage(Language.ITALIAN);
            return this;
        }

        public Builder french() {
            this.opus.setLanguage(Language.FRENCH);
            return this;
        }

        public Builder german() {
            this.opus.setLanguage(Language.GERMAN);
            return this;
        }

        public Builder language(Language lang) {
            this.opus.setLanguage(lang);
            return this;
        }

        public Builder wikipedia(String uri) {
            this.opus.addWebResource(WebResourceType.WIKIPEDIA, uri);
            return this;
        }

        public Builder imdb(String uri) {
            this.opus.addWebResource(WebResourceType.IMDB, uri);
            return this;
        }

        public Opus build() {
            Factory.OPUS.add(opus);
            return this.opus;
        }

    }

    private String title;

    private OpusType type;

    private Language language;

    private Set<Role> authors;

    private Set<WebResource> resources;

    Opus() {
    }

    Opus(String title, OpusType type, Language lang) {
        this.title = title;
        this.type = type;
        this.language = lang;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public OpusType getType() {
        return type;
    }

    public void setType(OpusType type) {
        this.type = type;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public Set<Role> getAuthors() {
        return authors;
    }

    public void setAuthors(Set<Role> authors) {
        this.authors = authors;
    }

    public Set<WebResource> getResources() {
        return resources;
    }

    public void setResources(Set<WebResource> resources) {
        this.resources = resources;
    }

    public void addPerson(RoleType roleType, Person person) {
        if (this.authors == null) {
            this.authors = new HashSet<>();
        }
        this.authors.add(new Role(person, roleType));
    }

    public void addWebResource(WebResourceType type, String uri) {
        if (this.resources == null) {
            this.resources = new HashSet<>();
        }
        this.resources.add(new WebResource(uri, type));
    }

}
