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

import java.util.Date;
import java.util.Set;

/**
 *
 * @author fede
 */
public class Performance {

    private Opus opus;

    private Venue venue;

    private Date date;

    private Set<Role> roles;

    private Set<WebResource> resources;


    Performance(Opus opus, Venue venue, Date date) {
        this.opus = opus;
        this.date = date;
        this.venue = venue;
    }

    public Venue getVenue() {
        return venue;
    }

    public void setVenue(Venue venue) {
        this.venue = venue;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public Opus getOpus() {
        return opus;
    }

    public void setOpus(Opus opus) {
        this.opus = opus;
    }

    public Set<WebResource> getResources() {
        return resources;
    }

    public void setResources(Set<WebResource> resources) {
        this.resources = resources;
    }

    public void addSinger(Person singer){
        this.roles.add(new Role(singer, RoleType.SINGER));
    }
    
    public void addViewer(Person viewer){
        this.roles.add(new Role(viewer, RoleType.VIEWER));
    }
}
