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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author fede
 */
public class BoxLabelDTO {

    private String boxName;
    private List<MediumContentDTO> contents = new ArrayList<>();

    public String getBoxName() {
        return boxName;
    }

    public void setBoxName(String boxName) {
        this.boxName = boxName;
    }

    public List<MediumContentDTO> getContents() {
        return contents;
    }

    public void setContents(List<MediumContentDTO> contents) {
        this.contents = contents;
    }
    
    public void addContent(MediumContentDTO content){
        this.contents.add(content);
    }
    
    public void doneContent(){
        Collections.sort(this.contents);
    }

}
