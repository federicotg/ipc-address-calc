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

/**
 *
 * @author fede
 */
public class OpusFactory extends BaseFactory<Pair<String, OpusType>, Opus> {

    public Opus createOpus(final String title, final OpusType type, final Language language) {
        return this.createInstance(new Pair<>(title, type), new Creator<Opus>() {
            @Override
            public Opus createInstance() {
                return new Opus(title, type, language);
            }
        });
    }
}
