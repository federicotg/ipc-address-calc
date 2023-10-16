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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 * @author fede
 */
public class ByteArrayConsole implements Console {

    private static final byte[] EOL = System.lineSeparator().getBytes();
    private final ByteArrayOutputStream out;

    public ByteArrayConsole() {
        this.out = new ByteArrayOutputStream(4096);
    }

    @Override
    public void appendLine(String... texts) {

        for (var text : texts) {
            out.writeBytes(text.getBytes());
        }
        out.writeBytes(EOL);
    }

    @Override
    public void printReport() throws IOException {
        this.out.writeTo(System.out);

    }

}
