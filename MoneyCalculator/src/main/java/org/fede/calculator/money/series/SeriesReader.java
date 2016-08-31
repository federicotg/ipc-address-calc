/*
 * Copyright (C) 2016 Federico Tello Gentile <federico.gentile@despegar.com>
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
package org.fede.calculator.money.series;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Federico Tello Gentile <federico.gentile@despegar.com>
 */
public class SeriesReader {

    private static final ObjectMapper OM = new ObjectMapper();

    private static final Map<String, JSONIndexSeries> CACHE = new HashMap<>();

    private static final TypeReference<List<JSONDataPoint>> INDEX_SERIES_TYPE_REFERENCE = new TypeReference<List<JSONDataPoint>>() {
    };

    public static IndexSeries readIndexSeries(String name) {
        return CACHE.computeIfAbsent(name, seriesName -> new JSONIndexSeries(read(seriesName, INDEX_SERIES_TYPE_REFERENCE)));
    }

    public static <T> T read(String name, TypeReference<T> typeReference) {
        try (InputStream in = SeriesReader.class.getResourceAsStream("/" + name);) {
            return OM.readValue(in, typeReference);
        } catch (IOException ioEx) {
            throw new IllegalArgumentException("Could not read series from resource " + name, ioEx);
        }
    }

}
