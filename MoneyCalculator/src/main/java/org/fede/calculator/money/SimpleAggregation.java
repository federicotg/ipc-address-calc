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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.SequencedCollection;
import java.util.function.Function;
import org.fede.calculator.money.series.IndexSeries;
import org.fede.calculator.money.series.JSONDataPoint;
import org.fede.calculator.money.series.JSONIndexSeries;
import org.fede.calculator.money.series.MoneyAmountSeries;

/**
 *
 * @author fede
 */
public class SimpleAggregation implements Aggregation {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(MathConstants.SCALE, MathConstants.RM);

    private final int months;

    public SimpleAggregation(int months) {
        this.months = months;
    }

    private void checkCurrency(String expectedCurrency, MoneyAmount lastValue) {
        if (!expectedCurrency.equals(lastValue.getCurrency())) {
            throw new IllegalArgumentException("All money amounts must be in the same currency before aggregating them.");
        }
    }

    private MoneyAmount avg(SequencedCollection<MoneyAmount> lastValues) {
        return new MoneyAmount(
                lastValues.stream().map(MoneyAmount::getAmount).reduce(ZERO, BigDecimal::add)
                        .divide(new BigDecimal(lastValues.size()), MathConstants.C), lastValues.getFirst().getCurrency());
    }

    private MoneyAmount sum(SequencedCollection<MoneyAmount> lastValues) {
        return new MoneyAmount(
                lastValues.stream().map(MoneyAmount::getAmount).reduce(ZERO, BigDecimal::add),
                lastValues.getFirst().getCurrency());
    }

    private MoneyAmount change(SequencedCollection<MoneyAmount> lastValues) {
        return new MoneyAmount(lastValues.getFirst().getAmount().subtract(lastValues.getLast().getAmount()), lastValues.getFirst().getCurrency());
    }

    private BigDecimal percentChange(SequencedCollection<MoneyAmount> lastValues) {

        final var last = lastValues.getLast().getAmount();
        if (last.signum() == 0) {
            return ZERO;
        }
        return lastValues.getFirst()
                .getAmount()
                .subtract(last)
                .divide(last, MathConstants.C);

    }

    private MoneyAmountSeries aggregate(MoneyAmountSeries series, final Function<SequencedCollection<MoneyAmount>, MoneyAmount> aggregationFunction) {
        final SequencedCollection<MoneyAmount> lastValues = new ArrayList<>();

        final String seriesCurrency = series.getCurrency();

        return series.map((yearMonth, amount) -> {
            checkCurrency(seriesCurrency, amount);

            lastValues.addFirst(amount);
            if (lastValues.size() > months) {
                lastValues.removeLast();
            }
            return aggregationFunction.apply((lastValues));
        });
    }

    @Override
    public MoneyAmountSeries average(MoneyAmountSeries series) {
        return this.aggregate(series, this::avg);
    }

    @Override
    public MoneyAmountSeries sum(MoneyAmountSeries series) {
        return this.aggregate(series, this::sum);
    }

    @Override
    public MoneyAmountSeries change(MoneyAmountSeries series) {
        return this.aggregate(series, this::change);
    }

    @Override
    public IndexSeries percentChange(MoneyAmountSeries series) {

        final var list = new ArrayList<JSONDataPoint>();

        final SequencedCollection<MoneyAmount> lastValues = new ArrayDeque<>();

        series.forEach((ym, ma) -> {

            lastValues.addFirst(ma);
            if (lastValues.size() > months) {
                lastValues.removeLast();
            }

            list.add(new JSONDataPoint(ym.getYear(), ym.getMonth(), this.percentChange(lastValues)));

        });

        return new JSONIndexSeries(list);

    }

}
