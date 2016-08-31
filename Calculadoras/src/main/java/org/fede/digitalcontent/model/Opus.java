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
import java.util.stream.Stream;

/**
 *
 * @author fede
 */
public class Opus {

    public static class Builder {

        private Opus[] opuses;

        public Builder(String... titles) {
            this.opuses = new Opus[titles.length];
            int i = 0;
            for (String title : titles) {
                this.opuses[i] = new Opus(title);
                i++;
            }
        }

        public Builder(String title) {
            this(new String[]{title});
        }

        public Builder opera() {
            for (Opus opus : this.opuses) {
                opus.setType(OpusType.OPERA);
            }
            return this;
        }

        public Builder episode() {
            for (Opus opus : this.opuses) {
                opus.setType(OpusType.EPISODE);
            }
            return this;
        }

        public Builder sport() {
            for (Opus opus : this.opuses) {
                opus.setType(OpusType.SPORT);
            }
            return this;
        }

        public Builder game() {
            for (Opus opus : this.opuses) {
                opus.setType(OpusType.GAME);
            }
            return this;
        }

        public Builder ballet() {
            for (Opus opus : this.opuses) {
                opus.setType(OpusType.BALLET);
            }
            return this;
        }

        public Builder movie() {
            for (Opus opus : this.opuses) {
                opus.setType(OpusType.MOVIE);
            }
            return this;
        }

        public Builder oratorio() {
            for (Opus opus : this.opuses) {
                opus.setType(OpusType.ORATORIO);
            }
            return this;
        }

        public Builder type(OpusType type) {
            for (Opus opus : this.opuses) {
                opus.setType(type);
            }
            return this;
        }

        public Builder by(String name) {

            Person by = Repository.PERSON.findById(name);
            if (by == null) {
                by = new Person(name);
                Repository.PERSON.add(by);
            }

            for (Opus opus : this.opuses) {
                opus.addPerson(RoleType.COMPOSER, by);
            }
            return this;
        }

        public Builder italian() {
            for (Opus opus : this.opuses) {
                opus.setLanguage(Language.ITALIAN);
            }
            return this;
        }

        public Builder french() {
            for (Opus opus : this.opuses) {
                opus.setLanguage(Language.FRENCH);
            }
            return this;
        }

        public Builder german() {
            for (Opus opus : this.opuses) {
                opus.setLanguage(Language.GERMAN);
            }
            return this;
        }

        public Builder russian() {
            for (Opus opus : this.opuses) {
                opus.setLanguage(Language.RUSSIAN);
            }
            return this;
        }

        public Builder english() {
            for (Opus opus : this.opuses) {
                opus.setLanguage(Language.ENGLISH);
            }
            return this;
        }

        public Builder language(Language lang) {
            for (Opus opus : this.opuses) {
                opus.setLanguage(lang);
            }
            return this;
        }

        public Builder wikipedia(String uri) {
            for (Opus opus : this.opuses) {
                opus.addWebResource(WebResourceType.WIKIPEDIA, uri);
            }
            return this;
        }

        public Builder imdb(String uri) {
            for (Opus opus : this.opuses) {
                opus.addWebResource(WebResourceType.IMDB, uri);
            }
            return this;
        }

        public Opus[] build() {
            for (Opus opus : this.opuses) {
                Repository.OPUS.add(opus);
            }
            return this.opuses;
        }

    }

    private final String title;

    private OpusType type;

    private Language language;

    private final Set<Role> authors;

    private final Set<WebResource> resources;

    private Opus(String title) {
        this.title = title;
        this.authors = new HashSet<>();
        this.resources = new HashSet<>();
    }

    public String getTitle() {
        return title;
    }

    /*public void setTitle(String title) {
        this.title = title;
    }*/
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

    /*public void setAuthors(Set<Role> authors) {
        this.authors = authors;
    }*/
    public Set<WebResource> getResources() {
        return resources;
    }

    /*public void setResources(Set<WebResource> resources) {
        this.resources = resources;
    }*/
    public void addPerson(RoleType roleType, Person person) {
        this.authors.add(new Role(person, roleType));
    }

    public void addWebResource(WebResourceType type, String uri) {
        this.resources.add(new WebResource(uri, type));
    }

    public Stream<Person> getMusicComposers() {
        return this.authors.stream()
                .filter(r -> r.getType().equals(RoleType.COMPOSER))
                .map(r -> r.getPerson());

//        Set<Person> answer = new HashSet<>();
//        for (Role r : this.authors) {
//            if (r.getType().equals(RoleType.COMPOSER)) {
//                answer.add(r.getPerson());
//            }
//        }
//        return answer;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.title, this.type);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Opus
                && Objects.equals(this.title, ((Opus) obj).title)
                && Objects.equals(this.type, ((Opus) obj).type);
    }

    @Override
    public String toString() {
        return this.title;
    }

}
