/*
 * Copyright (C) 2022 fede
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

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 *
 * @author fede
 */
public class GaussReturnSupplier implements Supplier<double[]> {

    private final double mean;
    private final double std;
    private final int years;

    public GaussReturnSupplier(double mean, double std, int years) {
        this.mean = mean;
        this.std = std;
        this.years = years;
    }

    private double gauss() {
        return this.mean + ThreadLocalRandom.current().nextGaussian() * this.std;
    }

    @Override
    public double[] get() {
        
        return IntStream.range(0, this.years)
                .mapToDouble(i -> gauss())
                .map(r -> 1.0d + (r / 100.0d))
                .toArray();
    
    }
    
    

}
