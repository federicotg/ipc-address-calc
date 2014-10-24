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

/**
 *
 * @author fede
 * @param <K>
 * @param <T>
 */
public interface Factory<K, T> {
    
    public static final StorageBoxFactory STORAGEBOX = new StorageBoxFactory();
    public static final CityFactory CITY = new CityFactory();
    public static final PersonFactory PERSON = new PersonFactory();
    public static final OpusFactory OPUS = new OpusFactory();
    public static final VenueFactory VENUE = new VenueFactory();
    public static final PerformanceFactory PERFORMANCE = new PerformanceFactory();
    public static final DigitalContentFactory DIGITALCONTENT = new DigitalContentFactory();
    public static final StorageMediumFactory STORAGE = new StorageMediumFactory();
    
    void add(T entity);
    T findById(K id);
    Set<T> findAll();
    Set<T> filter(Predicate<T> predicate);
}
