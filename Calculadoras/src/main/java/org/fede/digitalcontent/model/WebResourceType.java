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

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;


/**
 * Possible web resources are wikipedia, imdb, etc.
 * @author fede
 */
@PersistenceCapable(detachable = "true")
public class WebResourceType extends PersistentEntity {

    @Persistent
    private String name;

    public WebResourceType() {
    }

    public WebResourceType(String name) {
        this.name = name;
    }

    
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
