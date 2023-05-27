/*
 * Copyright (C) 2023 federicogentile
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
package org.fede.calculator.ppi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.text.MessageFormat;
import java.time.Instant;

/**
 *
 * {
 * "creationDate": "2022-01-18T18:25:36.269Z", "expirationDate":
 * "2022-01-18T18:25:36.269Z", "accessToken": "string", "expires": 0,
 * "refreshToken": "string", "tokenType": "string" }
 *
 * @author federicogentile
 */
public class PPIToken extends PPIRefreshTokenRequest {

    private Instant creationDate;
    private Instant expirationDate;
    private String accessToken;
    private int expires;
    private String tokenType;

    public Instant getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Instant creationDate) {
        this.creationDate = creationDate;
    }

    public Instant getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Instant expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public int getExpires() {
        return expires;
    }

    public void setExpires(int expires) {
        this.expires = expires;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    @JsonIgnore
    public boolean isValid() {
        return this.expirationDate.compareTo(Instant.now().plusSeconds(60)) > 0;
    }

    public String asAuthorizationHeader() {
        return MessageFormat.format("Bearer {0}", this.accessToken);
    }
}
