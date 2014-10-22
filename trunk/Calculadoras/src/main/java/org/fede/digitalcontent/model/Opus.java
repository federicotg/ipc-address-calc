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

import com.google.appengine.datanucleus.annotations.Unowned;
import java.util.HashSet;
import java.util.Set;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

/**
 *
 * @author fede
 */
@PersistenceCapable(detachable = "true")
public class Opus extends PersistentEntity {

    @Persistent
    private String title;

    @Persistent
    @Unowned
    private OpusType type;
    @Persistent
    private String language;

    @Persistent
    private Set<Role> authors;

    @Persistent
    private Set<WebResource> resources;

    public Opus() {
    }

    public Opus(String title, OpusType type) {
        this.title = title;
        this.type = type;
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

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
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
