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
package org.fede.video.model;

import com.google.appengine.datanucleus.annotations.Unowned;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

/**
 *
 * @author fede
 */
@PersistenceCapable
public class Video extends PersistentEntity {

    @Persistent
    private List<Opus> opus;

    @Persistent
    private int quality;

    @Persistent
    @Unowned
    private FormatType format;

    @Persistent
    @Unowned
    private Venue venue;

    @Persistent
    private List<StorageMedium> storageMedia;

    @Persistent
    private String subtitle;

    @Persistent
    private Set<WebResource> resources;

    @Persistent
    private Date date;

    @Persistent
    private Set<Role> roles;

    public List<Opus> getOpus() {
        return opus;
    }

    public void setOpus(List<Opus> opus) {
        this.opus = opus;
    }
            
    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public FormatType getFormat() {
        return format;
    }

    public void setFormat(FormatType format) {
        this.format = format;
    }

    public Venue getVenue() {
        return venue;
    }

    public void setVenue(Venue venue) {
        this.venue = venue;
    }

    public List<StorageMedium> getStorageMedia() {
        return storageMedia;
    }

    public void setStorageMedia(List<StorageMedium> storageMedia) {
        this.storageMedia = storageMedia;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public Set<WebResource> getResources() {
        return resources;
    }

    public void setResources(Set<WebResource> resources) {
        this.resources = resources;
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

}
