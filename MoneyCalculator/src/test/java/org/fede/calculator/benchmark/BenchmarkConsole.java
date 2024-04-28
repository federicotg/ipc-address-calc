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
package org.fede.calculator.benchmark;

import java.io.IOException;
import org.fede.calculator.money.Console;
import org.openjdk.jmh.infra.Blackhole;

/**
 *
 * @author fede
 */
public class BenchmarkConsole implements Console {

    private final Blackhole blackhole;

    public BenchmarkConsole(Blackhole blackhole) {
        this.blackhole = blackhole;
    }
    
    @Override
    public void appendLine(String... texts) {
    
        this.blackhole.consume(texts);
        
    }

    @Override
    public void printReport() throws IOException {
        // do nothing
    }
    
}
