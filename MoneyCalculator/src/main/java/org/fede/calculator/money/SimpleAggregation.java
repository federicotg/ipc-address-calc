/*
 * Copyright (C) 2014 fede
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

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.MoneyAmountTransform;

/**
 *
 * @author fede
 */
public class SimpleAggregation implements Aggregation, MathConstants {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(SCALE, ROUNDING_MODE);

    private static interface AggregationFunction {

        MoneyAmount apply(List<MoneyAmount> lastValues);
    }

    private final int months;

    public SimpleAggregation(int months) {
        this.months = months;
    }

    private MoneyAmount avg(List<MoneyAmount> lastValues) {
        BigDecimal sum = ZERO;
        for (MoneyAmount lastValue : lastValues) {
            sum = sum.add(lastValue.getAmount());
        }
        return new MoneyAmount(sum.divide(new BigDecimal(lastValues.size()), CONTEXT), lastValues.get(0).getCurrency());
    }

    private MoneyAmount sum(List<MoneyAmount> lastValues) {

        BigDecimal sum = ZERO;
        for (MoneyAmount lastValue : lastValues) {
            sum = sum.add(lastValue.getAmount());
        }
        return new MoneyAmount(sum, lastValues.get(0).getCurrency());
    }
    
    private MoneyAmount change(List<MoneyAmount> lastValues){
        return new MoneyAmount(lastValues.get(0).getAmount().subtract(lastValues.get(lastValues.size()-1).getAmount()), lastValues.get(0).getCurrency());
    }

    private MoneyAmountSeries aggregate(MoneyAmountSeries series, final AggregationFunction aggregationFunction) {
        final LinkedList<MoneyAmount> lastValues = new LinkedList<>();
        return series.map(new MoneyAmountTransform() {
            @Override
            public MoneyAmount transform(int year, int month, MoneyAmount amount) {
                lastValues.addFirst(amount);
                if(lastValues.size()>months){
                    lastValues.removeLast();
                }
                return aggregationFunction.apply((lastValues));
            }
        });
    }

    @Override
    public MoneyAmountSeries average(MoneyAmountSeries series) {
        return this.aggregate(series, new AggregationFunction() {

            @Override
            public MoneyAmount apply(List<MoneyAmount> lastValues) {
                return avg(lastValues);
            }
        });
    }

    @Override
    public MoneyAmountSeries sum(MoneyAmountSeries series) {
        return this.aggregate(series, new AggregationFunction() {

            @Override
            public MoneyAmount apply(List<MoneyAmount> lastValues) {
                return sum(lastValues);
            }
        });
    }

    @Override
    public MoneyAmountSeries change(MoneyAmountSeries series) {
        return this.aggregate(series, new AggregationFunction() {

            @Override
            public MoneyAmount apply(List<MoneyAmount> lastValues) {
                return change(lastValues);
            }
        });
    }
    
}
