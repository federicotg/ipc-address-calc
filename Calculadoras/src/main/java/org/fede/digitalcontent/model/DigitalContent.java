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
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

/**
 *
 * @author fede
 */
@PersistenceCapable(detachable = "true")
public class DigitalContent extends PersistentEntity {

    @Persistent
    @Unowned
    private Performance performance;

    @Persistent
    @Unowned
    private Quality quality;

    @Persistent
    @Unowned
    private FormatType format;

    @Persistent
    private String subtitle;

    public DigitalContent() {
    }

    public DigitalContent(Performance performance, Quality quality, FormatType format) {
        this.performance = performance;
        this.quality = quality;
        this.format = format;
    }

    public Quality getQuality() {
        return quality;
    }

    public void setQuality(Quality quality) {
        this.quality = quality;
    }

    public FormatType getFormat() {
        return format;
    }

    public void setFormat(FormatType format) {
        this.format = format;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public Performance getPerformance() {
        return performance;
    }

    public void setPerformance(Performance performance) {
        this.performance = performance;
    }

}
