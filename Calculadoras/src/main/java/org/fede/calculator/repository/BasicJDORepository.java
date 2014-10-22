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
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author fede
 */
@Repository("basicRepository")
public class BasicJDORepository implements JDORepository {

    @Resource(name = "transactionAwarePersistenceManagerFactoryProxy")
    private PersistenceManagerFactory persistenceManagerFactory;

    private PersistenceManager getPersistenceManager() {
        return this.persistenceManagerFactory.getPersistenceManager();
    }

    @Override
    @Transactional
    public <T> T save(T entity) {
        return this.getPersistenceManager().makePersistent(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public <T> Iterable<T> findAll(Class<T> clazz) {
        Query query = this.getPersistenceManager().newQuery(clazz);
        return (Iterable<T>) query.execute();
    }

    @Override
    @Transactional(readOnly = true)
    public <T> T findOne(Class<T> clazz, Key id) {
        Query query = this.getPersistenceManager().newQuery(clazz, "id == idParam");
        query.declareParameters("Key idParam");
        Iterator<T> it = ((Iterable<T>) query.execute(id)).iterator();
        if (it.hasNext()) {
            return it.next();
        }
        return null;
    }

    @Override
    @Transactional
    public <T> void delete(Class<T> clazz, Key id) {
        T entity = this.findOne(clazz, id);
        if (entity != null) {
            this.getPersistenceManager().deletePersistent(entity);
        }
    }

    @Override
    @Transactional
    public <T> void deleteAll(Class<T> clazz) {
        for (T entity : this.findAll(clazz)) {
            this.getPersistenceManager().deletePersistent(entity);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public <T> Iterable<T> findByFilter(Class<T> clazz, String filter, String parameterDeclaration, Object param1) {
        return this.findByFilter(clazz, filter, parameterDeclaration, new Object[]{param1});
    }

    @Override
    @Transactional(readOnly = true)
    public <T> Iterable<T> findByFilter(Class<T> clazz, String filter, String parameterDeclaration, Object param1, Object param2) {
        return this.findByFilter(clazz, filter, parameterDeclaration, new Object[]{param1, param2});
    }

    @Override
    @Transactional(readOnly = true)
    public <T> Iterable<T> findByFilter(Class<T> clazz, String filter, String parameterDeclaration, Object param1, Object param2, Object param3) {
        return this.findByFilter(clazz, filter, parameterDeclaration, new Object[]{param1, param2, param3});
    }

    @Override
    @Transactional(readOnly = true)
    public <T> Iterable<T> findByFilter(Class<T> clazz, String filter, String parameterDeclaration, Object[] params) {
        Query query = this.getPersistenceManager().newQuery(clazz, filter);
        query.declareParameters(parameterDeclaration);
        return (Iterable<T>) query.executeWithArray(params);
    }

    @Override
    @Transactional(readOnly = true)
    public <T> T findFirstByName(Class<T> clazz, String name) {
        Iterator<T> result = this.findByFilter(clazz, "name == nameParam", "String nameParam", name).iterator();
        if (result.hasNext()) {
            return result.next();
        }
        return null;
    }

}
