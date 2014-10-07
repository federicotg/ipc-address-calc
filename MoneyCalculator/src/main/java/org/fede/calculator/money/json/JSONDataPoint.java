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
package org.fede.calculator.money.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import org.fede.calculator.money.series.JSONIndexSeries;

/**
 *
 * @author fede
 */
public class JSONDataPoint implements Comparable<JSONDataPoint> {
    private int year;
    private int month;
    private BigDecimal value;

    public static List<JSONDataPoint> readSeries(String name){
         try (InputStream is = JSONIndexSeries.class.getResourceAsStream("/" + name)) {
            return new ObjectMapper().readValue(is, new TypeReference<List<JSONDataPoint>>() {
            });
        } catch (IOException ioEx) {
            throw new IllegalArgumentException("Could not read series named " + name, ioEx);
        }
    }
               
    public JSONDataPoint(){
        
    }
    
    public JSONDataPoint(int year, int month) {
        this.year = year;
        this.month = month;
    }

  
    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    @Override
    public int compareTo(JSONDataPoint o) {
        if(this.year == o.year){
            return this.month - o.month;
        }
        return this.year - o.year;
    }
       
}
