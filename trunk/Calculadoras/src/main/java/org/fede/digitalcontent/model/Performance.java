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
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

/**
 *
 * @author fede
 */
@PersistenceCapable(detachable = "true")
public class Performance extends PersistentEntity {

    @Persistent
    @Unowned
    private Venue venue;
    @Persistent
    private Date date;
    @Persistent
    private Set<Role> roles;
    @Persistent
    private Set<Opus> opus;
    @Persistent
    private Set<WebResource> resources;

    public Performance() {
    }

    
    
    public Performance(Opus opus, Venue venue, int day, int month, int year) {
        this.opus = new HashSet<>();
        this.opus.add(opus);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month-1);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        this.date = cal.getTime();
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

    public Set<Opus> getOpus() {
        return opus;
    }

    public void setOpus(Set<Opus> opus) {
        this.opus = opus;
    }

    public Set<WebResource> getResources() {
        return resources;
    }

    public void setResources(Set<WebResource> resources) {
        this.resources = resources;
    }

}
