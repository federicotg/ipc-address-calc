/*
 * Copyright (C) 2017 Federico Tello Gentile <federicotg@gmail.com>
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
package org.fede.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import org.fede.calculator.money.MathConstants;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
public class BigDecimalAverageCollector implements Collector<BigDecimal, BigDecimalAverageCollector.BigDecimalAccumulator, BigDecimal> {

    @Override
    public Supplier<BigDecimalAccumulator> supplier() {
        return BigDecimalAccumulator::new;
    }

    @Override
    public BiConsumer<BigDecimalAccumulator, BigDecimal> accumulator() {
        return BigDecimalAccumulator::add;
    }

    @Override
    public BinaryOperator<BigDecimalAccumulator> combiner() {
        return BigDecimalAccumulator::combine;
    }

    @Override
    public Function<BigDecimalAccumulator, BigDecimal> finisher() {
        return BigDecimalAccumulator::getAverage;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Collections.emptySet();
    }

    public static class BigDecimalAccumulator {

        private BigDecimal sum;
        private BigDecimal count;

        public BigDecimalAccumulator() {
            this.sum = BigDecimal.ZERO;
            this.count = BigDecimal.ZERO;
        }

        public BigDecimalAccumulator(BigDecimal sum, BigDecimal count) {
            this.sum = sum;
            this.count = count;
        }

        public BigDecimal getSum() {
            return sum;
        }

        public BigDecimal getCount() {
            return count;
        }

        BigDecimal getAverage() {
            return BigDecimal.ZERO.compareTo(count) == 0
                    ? BigDecimal.ZERO
                    : sum.divide(count, 6, RoundingMode.HALF_UP);
        }

        BigDecimalAccumulator combine(BigDecimalAccumulator another) {
            return new BigDecimalAccumulator(
                    sum.add(another.getSum(), MathConstants.CONTEXT),
                    count.add(another.getCount(), MathConstants.CONTEXT)
            );
        }

        void add(BigDecimal successRate) {
            count = count.add(BigDecimal.ONE, MathConstants.CONTEXT);
            sum = sum.add(successRate, MathConstants.CONTEXT);
        }
    }

}
