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
 * A box holding a particular set of CD, DVD or BD-R.
 *
 * @author fede
 */

public class StorageBox  {

    private String name;

    private Set<StorageMedium> media;

    StorageBox(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<StorageMedium> getMedia() {
        return media;
    }

    public void setMedia(Set<StorageMedium> media) {
        this.media = media;
    }

    public void addStorageMedium(StorageMedium medium) {
        if (this.media == null) {
            this.media = new HashSet<>();
        }
        this.media.add(medium);
    }
}
