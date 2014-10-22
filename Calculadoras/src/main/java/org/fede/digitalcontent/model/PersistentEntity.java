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

import com.google.appengine.api.datastore.Key;
import java.util.Objects;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Version;
import javax.jdo.annotations.VersionStrategy;

/**
 *
 * @author fede
 */
@PersistenceCapable(detachable = "true")
@Inheritance(strategy = InheritanceStrategy.SUBCLASS_TABLE)
@Version(strategy = VersionStrategy.VERSION_NUMBER, column = "VERSION")
public abstract class PersistentEntity {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key id;

    public PersistentEntity() {
    }

    
    public Key getId() {
        return id;
    }

    public void setId(Key id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        return 53 * 7 + Objects.hashCode(this.id);
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null
                && this.getClass().equals(obj.getClass())
                && Objects.equals(this.id, ((PersistentEntity) obj).id);
    }

}
