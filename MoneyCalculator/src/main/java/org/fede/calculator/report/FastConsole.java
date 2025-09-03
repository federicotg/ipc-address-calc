/*
 * Copyright (C) 2025 fede
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
package org.fede.calculator.report;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author fede
 */
public class FastConsole implements Console {

    private static final Logger LOGGER = LoggerFactory.getLogger(FastConsole.class);

    private final BufferedWriter out;

    public FastConsole() {
        try {
            this.out = new BufferedWriter(new OutputStreamWriter(System.out, "UTF-8"), 16*1024);
        } catch (IOException ioEx) {
            LOGGER.error("Could not initialize output buffer.", ioEx);
            throw new RuntimeException(ioEx);
        }
    }

    @Override
    public void appendLine(String... texts) {
        try {
            for (var t : texts) {
                this.out.write(t);
            }
            this.out.newLine();
        } catch (IOException ioEx) {
            LOGGER.error("Could not write output.", ioEx);
        }
    }

    @Override
    public void printReport() throws IOException {
        this.out.flush();
    }

}
