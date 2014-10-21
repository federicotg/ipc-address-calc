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
import java.util.Iterator;
import javax.annotation.Resource;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import org.springframework.stereotype.Repository;

/**
 *
 * @author fede
 */
@Repository
public abstract class BasicJDORepository<T> implements JDORepository<T, Key> {

    @Resource(name = "transactionAwarePersistenceManagerFactoryProxy")
    private PersistenceManagerFactory persistenceManagerFactory;

    private Class<T> clazz;

    protected BasicJDORepository(Class<T> clazz) {
        this.clazz = clazz;
    }

    private PersistenceManager getPersistenceManager() {
        return this.persistenceManagerFactory.getPersistenceManager();
    }

    @Override
    public <S extends T> S save(S entity) {
        return this.getPersistenceManager().makePersistent(entity);
    }

    @Override
    public Iterable<T> findAll() {
        Query query = this.getPersistenceManager().newQuery(clazz);
        return (Iterable<T>) query.execute();
    }

    @Override
    public T findOne(Key id) {
        Query query = this.getPersistenceManager().newQuery(clazz, " id = idParam");
        query.declareParameters("Key idParam");
        Iterator<T> it = ((Iterable<T>) query.execute(id)).iterator();
        if (it.hasNext()) {
            return it.next();
        }
        return null;
    }

    @Override
    public boolean exists(Key id) {
        return this.findOne(id) != null;
    }

    @Override
    public void delete(Key id) {
        T entity = this.findOne(id);
        if (entity != null) {
            this.getPersistenceManager().deletePersistent(entity);
        }
    }

}
