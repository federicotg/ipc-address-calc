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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author fede
 */
public abstract class BaseFactory<K,T> implements Factory<K, T>{
     
    private final Map<K, T> instances = new HashMap<>();
    
    protected final T createInstance(K key, Creator<T> creator){
        T answer = this.instances.get(key);
        if (answer == null) {
            answer = creator.createInstance();
            this.instances.put(key, answer);
        }
        return answer;
    }

    @Override
    public final T findById(K id) {
        return this.instances.get(id);
    }

    @Override
    public Set<T> findAll() {
        return new HashSet<>(this.instances.values());
    }
    
}
