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

import java.util.Set;
import java.util.stream.Stream;

/**
 *
 * @author fede
 * @param <K>
 * @param <T>
 */
public interface Repository<K, T> {
    
    public static final StorageBoxRepository STORAGEBOX = new StorageBoxRepository();
    public static final CityRepository CITY = new CityRepository();
    public static final PersonRepository PERSON = new PersonRepository();
    public static final OpusRepository OPUS = new OpusRepository();
    public static final VenueRepository VENUE = new VenueRepository();
    public static final PerformanceRepository PERFORMANCE = new PerformanceRepository();
    public static final DigitalContentRepository DIGITALCONTENT = new DigitalContentRepository();
    public static final StorageMediumRepository STORAGE = new StorageMediumRepository();
    
    void add(T entity);
    T findById(K id);
    Stream<T> stream();
}
