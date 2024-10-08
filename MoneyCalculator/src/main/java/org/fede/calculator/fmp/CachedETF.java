/*
 * Copyright (C) 2024 fede
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
package org.fede.calculator.fmp;

import org.fede.calculator.service.ETF;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author fede
 */
public class CachedETF implements ETF {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CachedETF.class);

    private final ObjectMapper om;
    private final ETF etf;

    public CachedETF(ObjectMapper om, ETF etf) {
        this.om = om;
        this.etf = etf;
    }

    @Override
    public Map<String, ExchangeTradedFundData> etfs() {

        try {
            var path = Path.of(System.getProperty("user.home") + "/etfs_tmp.json");
            if (Files.exists(path)) {
                var cachedData = this.om.readValue(Files.readAllBytes(path), CachedETFData.class);
                if (!cachedData.expired()) {
                    return cachedData.data();
                }
            }

            var data = this.etf.etfs();
            Files.write(path, this.om.writeValueAsBytes(new CachedETFData(LocalDateTime.now(), data)), StandardOpenOption.CREATE);
            return data;

        } catch (IOException ex) {
            LOGGER.error("Unexpected error.", ex);
            return Map.of();
        }

    }

}
