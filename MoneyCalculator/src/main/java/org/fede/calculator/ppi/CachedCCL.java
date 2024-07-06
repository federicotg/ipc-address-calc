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
package org.fede.calculator.ppi;

import org.fede.calculator.fmp.*;
import org.fede.calculator.service.ETF;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.Map;
import org.fede.calculator.service.CCL;

/**
 *
 * @author fede
 */
public class CachedCCL implements CCL {

    private final ObjectMapper om;
    private final CCL ccl;

    public CachedCCL(ObjectMapper om, CCL ccl) {
        this.om = om;
        this.ccl = ccl;
    }

    @Override
    public Map<String, BigDecimal> ccl() {
    
        try {
            var path = Path.of(System.getProperty("user.home") + "/ccl_tmp.json");
            if (Files.exists(path)) {
                var cachedData = this.om.readValue(Files.readAllBytes(path), CachedCCLData.class);
                if (!cachedData.expired()) {
                    return cachedData.data();
                }
            }

            var data = this.ccl.ccl();
            Files.write(path, this.om.writeValueAsBytes(new CachedCCLData(LocalDateTime.now(), data)), StandardOpenOption.CREATE);
            return data;

        } catch (IOException ex) {
            System.err.println(ex.getMessage());
            ex.printStackTrace(System.err);
            return Map.of();
        }

    }

}
