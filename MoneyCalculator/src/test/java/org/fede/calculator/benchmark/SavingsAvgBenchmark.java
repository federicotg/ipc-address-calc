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

import org.fede.calculator.money.Bar;
import org.fede.calculator.money.Format;
import org.fede.calculator.money.Savings;
import org.fede.calculator.money.Series;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Warmup;

/**
 *
 * @author fede
 */
public class SavingsAvgBenchmark {

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Fork(value = 1, warmups = 1)
    @Warmup(iterations = 2)
    public void init() {

        var console = new BenchmarkConsole();
        var format = new Format();
        
        new Savings(format, new Series(), new Bar(console, format), console).netAvgSavingSpent(12, "Title");
    }

    public static void main(String[] args) {
        var instance = new SavingsAvgBenchmark();

        for (var i = 0; i < 100; i++) {
            instance.init();
        }
    }

}
