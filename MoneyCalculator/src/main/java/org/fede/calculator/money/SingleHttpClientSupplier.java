/*
 * Copyright (C) 2023 fede
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
package org.fede.calculator.money;

import java.net.http.HttpClient;
import java.util.function.Supplier;

/**
 *
 * @author fede
 */
public class SingleHttpClientSupplier implements Supplier<HttpClient> {

    private HttpClient client;

    @Override
    public HttpClient get() {
        if (this.client == null) {
            this.client = HttpClient.newHttpClient();
        }
        return this.client;
    }

}
