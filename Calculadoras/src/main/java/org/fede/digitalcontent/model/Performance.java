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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

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

    public Performance(Opus opus, Venue venue, Date date) {
        this.opus = opus;
        this.date = date;
        this.venue = venue;
        this.roles = new HashSet<>();
        this.resources = new HashSet<>();
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

    private Set<WebResource> getResources() {
        return resources;
    }

    public void setResources(Set<WebResource> resources) {
        this.resources = resources;
    }

    public void addSinger(Person singer) {
        this.roles.add(new Role(singer, RoleType.SINGER));
    }

    public void addImdb(String uri) {
        this.resources.add(new WebResource(uri, WebResourceType.IMDB));
    }

    public void addViewer(Person viewer) {
        this.roles.add(new Role(viewer, RoleType.VIEWER));
    }

    public String getImdb() {
        for (WebResource r : this.getResources()) {
            if (r.getType().equals(WebResourceType.IMDB)) {
                return r.getUri();
            }
        }
        return null;
    }

    public Language getLanguage() {
        return this.opus.getLanguage();
    }

    public Stream<Person> getMusicComposers() {
        return this.opus.getMusicComposers();
    }

    public OpusType getOpusType() {
        return this.opus.getType();
    }

    public boolean isSeenBy(Person p) {
        for (Role r : this.roles) {
            if (r.getType().equals(RoleType.VIEWER) && r.getPerson().equals(p)) {
                return true;
            }
        }
        return false;
    }

    public String getTitle() {
        return this.opus.getTitle();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.opus);
        hash = 67 * hash + Objects.hashCode(this.venue);
        hash = 67 * hash + Objects.hashCode(this.date);
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
        final Performance other = (Performance) obj;
        if (!Objects.equals(this.opus, other.opus)) {
            return false;
        }
        if (!Objects.equals(this.venue, other.venue)) {
            return false;
        }
        return Objects.equals(this.date, other.date);
    }

    @Override
    public String toString() {
        return this.opus.toString() 
                + (this.venue == null ? "": " @ " + this.venue.toString()) 
                + (this.date == null ? "" : " on " + this.date.toString());
    }

    public boolean includesComposer(String name) {
        return this.getMusicComposers().anyMatch(composer -> composer.getName().equals(name));
//        for (Person composer : this.getMusicComposers()) {
//            if (composer.getName().equals(name)) {
//                return true;
//            }
//        }
//        return false;
    }

    public String getDetailedTitle() {
        DateFormat yearFormat = new SimpleDateFormat("yyyy");

        Venue v = this.getVenue();
        Date d = this.getDate();
        StringBuilder sb = new StringBuilder(30);
        sb.append(this.getTitle())
                .append(v != null ? " @ ":"")
                .append(v != null ? v.getName() : "")
                .append(d != null ? " (":"")
                .append(d != null ? yearFormat.format(d) : "")
                .append(d != null ? ")":"");
        return sb.toString();
    }
}
