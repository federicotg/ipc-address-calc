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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A box holding a particular set of CD, DVD or BD-R.
 *
 * @author fede
 */
public class StorageBox {

    private final String name;

    private final Set<StorageMedium> media;

    StorageBox(String name) {
        this.name = name;
        this.media = new HashSet<>();
    }

    public String getName() {
        return name;
    }

//    public void setName(String name) {
//        this.name = name;
//    }
    public Set<StorageMedium> getMedia() {
        return media;
    }

//    public void setMedia(Set<StorageMedium> media) {
//        this.media = media;
//    }
    public void addStorageMedium(StorageMedium medium) {
        this.media.add(medium);
    }

    public boolean contains(DigitalContent dc) {
        return this.media.stream().anyMatch(m -> m.contains(dc));
//        boolean answer = false;
//        for (StorageMedium m : this.media) {
//            answer |= m.contains(dc);
//        }
//        return answer;
    }

    @Override
    public String toString() {
        return "[" + this.media.toString() + "]";
    }

    @Override
    public int hashCode() {
        return 37 * 5 + Objects.hashCode(this.name);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof StorageBox
                && Objects.equals(this.name, ((StorageBox) obj).name);
    }

    public long size() {
        return Optional.ofNullable(this.media)
                .map(set -> set.stream())
                .orElse(Stream.empty())
                .mapToLong(StorageMedium::getSize)
                .sum();
    }

}
