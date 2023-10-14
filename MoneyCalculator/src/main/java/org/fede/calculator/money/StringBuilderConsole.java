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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 *
 * @author fede
 */
public class StringBuilderConsole implements Console {
    
    
    private final StringBuilder out;

    public StringBuilderConsole() {
        this.out = new StringBuilder(1024);
    }

    @Override
    public void appendLine(String... texts) {
        Arrays.stream(texts)
                .forEach(out::append);
        out.append("\n");
    }

    @Override
    public void printReport(OutputStream out) throws IOException {
        out.write(this.out.toString().getBytes());
        out.flush();
        out.close();
    }

    
}