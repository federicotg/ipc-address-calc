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

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Collection;
import java.util.TreeSet;

/**
 *
 * @author fede
 */
public class MediumContentDTO implements Comparable<MediumContentDTO> {

    private static final BigDecimal BYTES_IN_A_GB = new BigDecimal(1024 * 1024 * 1024);
    
    private String mediumName;
    private long size;
    private Collection<OpusDTO> opus = new TreeSet<>();

    public String getMediumName() {
        return mediumName;
    }

    public void setMediumName(String mediumName) {
        this.mediumName = mediumName;
    }

    public Collection<OpusDTO> getOpus() {
        return opus;
    }

    public void setOpus(Collection<OpusDTO> opus) {
        this.opus = opus;
    }

    @Override
    public int compareTo(MediumContentDTO o) {
        return this.mediumName.compareTo(o.getMediumName());
    }

    public void addOpus(String name, String type) {
        this.opus.add(new OpusDTO(name, type));
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
    
    public BigDecimal getSizeInGB(){
        return new BigDecimal(this.size).setScale(6).divide(BYTES_IN_A_GB, MathContext.DECIMAL32);
    }

}
