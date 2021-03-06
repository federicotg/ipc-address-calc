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

import org.fede.util.Pair;
import java.util.Objects;

/**
 *
 * @author fede
 * @param <A>
 * @param <B>
 * @param <C>
 */
public class Terna<A, B, C> extends Pair<A, B> {

    private C third;

    public Terna(A first, B second, C third) {
        super(first, second);
        this.third = third;
    }

    public C getThird() {
        return third;
    }

    public void setThird(C third) {
        this.third = third;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Terna
                && Objects.equals(this.getFirst(), ((Pair) obj).getFirst())
                && Objects.equals(this.getSecond(), ((Pair) obj).getSecond())
                && Objects.equals(this.third, ((Terna) obj).third);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getFirst(), this.getSecond(), this.getThird());
    }

}
