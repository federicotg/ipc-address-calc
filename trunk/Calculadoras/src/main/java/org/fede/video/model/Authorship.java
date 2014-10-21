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
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;


/**
 *
 * @author fede
 */
@PersistenceCapable
public class Authorship extends PersistentEntity {

    @Persistent
    @Unowned
    private Author author;

    @Persistent
    @Unowned
    private AuthorshipType type;
    
    @Persistent
    private int year;

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public AuthorshipType getType() {
        return type;
    }

    public void setType(AuthorshipType type) {
        this.type = type;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

}
