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

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A particular DVD, CD or BD-R holding some digital content.
 *
 * @author fede
 */
public class StorageMedium {

    private static final BigDecimal MAX_CAPACITY = new BigDecimal("24822165814");

    private final String name;

    private long size;

    private final Set<DigitalContent> contents;

    StorageMedium(String name) {
        this.name = name;
        this.contents = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public Set<DigitalContent> getContents() {
        return contents;
    }

    public void addContent(DigitalContent content) {
        this.contents.add(content);
    }

    public boolean contains(DigitalContent dc) {
        return this.contents.contains(dc);
    }

    @Override
    public String toString() {
        return this.getName() + " " + contents.toString();
    }

    public void setSize(long bytes) {
        this.size = bytes;
    }

    public long getSize() {
        return size;
    }

    public BigDecimal getUsedCapacity() {
        return new BigDecimal(this.getSize())
                .setScale(4)
                .divide(MAX_CAPACITY, MathContext.DECIMAL128);

    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof StorageMedium
                && Objects.equals(((StorageMedium) obj).getName(), this.getName());
    }

}
