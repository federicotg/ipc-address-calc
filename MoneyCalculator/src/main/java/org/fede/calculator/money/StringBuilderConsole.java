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

import java.io.BufferedOutputStream;
import java.io.IOException;

/**
 *
 * @author fede
 */
public class StringBuilderConsole implements Console {

    private final StringBuilder out;

    public StringBuilderConsole() {
        this.out = new StringBuilder(2048);
    }

    @Override
    public void appendLine(String... texts) {

        for (var text : texts) {
            out.append(text);
        }
        out.append("\n");
    }

    @Override
    public void printReport() throws IOException {
        try (java.io.BufferedOutputStream os = new BufferedOutputStream(System.out)) {
            os.write(this.out.toString().getBytes());
            os.flush();
        }
    }

}
