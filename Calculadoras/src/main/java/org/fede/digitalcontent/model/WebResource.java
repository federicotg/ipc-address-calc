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

import java.util.Objects;

/**
 *
 * @author fede
 */
public class WebResource {

    private final String uri;

    private final WebResourceType type;

    WebResource(String uri, WebResourceType type) {
        this.uri = uri;
        this.type = type;
    }

    public String getUri() {
        return uri;
    }

//    public void setUri(String uri) {
//        this.uri = uri;
//    }
    public WebResourceType getType() {
        return type;
    }

//    public void setType(WebResourceType type) {
//        this.type = type;
//    }
    @Override
    public int hashCode() {
        return 23 * 3 + Objects.hashCode(this.uri);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof WebResource
                && Objects.equals(this.uri, ((WebResource) obj).uri);
    }

}
