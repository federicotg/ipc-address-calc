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
package org.fede.calculator.repository;

import com.google.appengine.api.datastore.Key;

/**
 *
 * @author fede
 */
public interface JDORepository {
    
    <T> T save(T entity);
    <T> Iterable<T> findAll(Class<T> clazz);
    <T> T findOne(Class<T> clazz, Key id);
    <T> void delete(Class<T> clazz, Key id);
    <T> void deleteAll(Class<T> clazz);
    <T> T findFirstByName(Class<T> clazz, String name);
    <T> Iterable<T> findByFilter(Class<T> clazz, String filter, String parameterDeclaration, Object param1);
    <T> Iterable<T> findByFilter(Class<T> clazz, String filter, String parameterDeclaration, Object param1, Object param2);
    <T> Iterable<T> findByFilter(Class<T> clazz, String filter, String parameterDeclaration, Object param1, Object param2, Object param3);
    <T> Iterable<T> findByFilter(Class<T> clazz, String filter, String parameterDeclaration, Object[] params);
    
    
}
