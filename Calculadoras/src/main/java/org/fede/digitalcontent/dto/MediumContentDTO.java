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
package org.fede.digitalcontent.dto;

import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author fede
 */
public class MediumContentDTO implements Comparable<MediumContentDTO> {

    private String mediumName;
    private Set<OpusDTO> opus = new TreeSet<>();

    public String getMediumName() {
        return mediumName;
    }

    public void setMediumName(String mediumName) {
        this.mediumName = mediumName;
    }

    public Set<OpusDTO> getOpus() {
        return opus;
    }

    public void setOpus(Set<OpusDTO> opus) {
        this.opus = opus;
    }

    @Override
    public int compareTo(MediumContentDTO o) {
        return this.mediumName.compareTo(o.getMediumName());
    }

    public void addOpus(String name, String type) {
        this.opus.add(new OpusDTO(name, type));
    }

}
