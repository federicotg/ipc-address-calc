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
package org.fede.calculator.money;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.SequencedCollection;
import java.util.function.BiFunction;
import java.util.stream.Gatherers;
import java.util.stream.Stream;
import org.fede.calculator.money.series.IndexSeries;
import org.fede.calculator.money.series.JSONDataPoint;
import org.fede.calculator.money.series.JSONIndexSeries;
import org.fede.calculator.money.series.MoneyAmountItem;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.SortedMapMoneyAmountSeries;

/**
 *
 * @author fede
 */
public class SlidingWindow implements Aggregator, Differencer {

    private final int months;

    public SlidingWindow(int months) {
        this.months = months;
    }

    /**
     * For every year and month the value is the average over the previous
     * window.
     *
     * @param series
     * @return
     */
    @Override
    public MoneyAmountSeries average(MoneyAmountSeries series) {

        return this.aggregate(series, "{0} avg(" + months + ")", this::avg);

    }

    /**
     * For every year and month the value is the sum of the previous window.
     *
     * @param series
     * @return
     */
    @Override
    public MoneyAmountSeries sum(MoneyAmountSeries series) {

        return this.aggregate(series, "{0} sum(" + months + ")", this::sum);

    }

    private MoneyAmountSeries aggregate(
            MoneyAmountSeries series,
            String nameFormat,
            BiFunction<SequencedCollection<MoneyAmountItem>, Currency, MoneyAmount> operation) {
        final Currency seriesCurrency = series.getCurrency();
        MoneyAmountSeries result = new SortedMapMoneyAmountSeries(seriesCurrency, MessageFormat.format(nameFormat, series.getName()));
        paddedStream(series)
                .gather(Gatherers.windowSliding(months))
                .forEach(window -> result.putAmount(window.getLast().ym(), operation.apply(window, seriesCurrency)));

        return result;
    }

    /**
     * The difference in absolute value compared to the value right before the
     * start of the window.
     *
     * @param series
     * @return
     */
    @Override
    public MoneyAmountSeries change(MoneyAmountSeries series) {

        MoneyAmountSeries result = new SortedMapMoneyAmountSeries(series.getCurrency(), series.getName() + " change (" + months + ")");
        paddedStream(series)
                .gather(Gatherers.windowSliding(months + 1))
                .forEach(window
                        -> result.putAmount(
                        window.getLast().ym(),
                        window.getLast().amount().subtract(window.getFirst().amount())
                ));

        return result;

    }

    /**
     * The difference in percentage compared to the value right before the start
     * of the window.
     *
     * @param series
     * @return
     */
    @Override
    public IndexSeries percentChange(MoneyAmountSeries series) {

        return new JSONIndexSeries(
                paddedStream(series)
                        .gather(Gatherers.windowSliding(months + 1))
                        .map(
                                window
                                -> window.getFirst().amount().amount().signum() == 0
                                ? new JSONDataPoint(window.getLast().ym(), BigDecimal.ZERO)
                                : new JSONDataPoint(
                                        window.getLast()
                                                .ym(),
                                        window.getLast()
                                                .amount()
                                                .subtract(window.getFirst().amount())
                                                .amount()
                                                .divide(window.getFirst().amount().amount(), MathConstants.C)))
                        .toList()
        );

    }

    private MoneyAmount avg(SequencedCollection<MoneyAmountItem> lastValues, Currency currency) {
        return this.sum(lastValues, currency)
                .adjust(new BigDecimal(lastValues.size()), BigDecimal.ONE);

    }

    private MoneyAmount sum(SequencedCollection<MoneyAmountItem> lastValues, Currency c) {
        BigDecimal total = BigDecimal.ZERO;
        for (var item : lastValues) {
            total = total.add(item.amount().amount());
        }
        return new MoneyAmount(total, c);
    }

    private static Stream<MoneyAmountItem> paddedStream(MoneyAmountSeries series) {

        // pad with zeros after the end of the values and up until 
        // the last inflation data available.
        var ym = series.getTo();
        Stream.Builder<MoneyAmountItem> sb = Stream.builder();
        while (ym.isBefore(Inflation.USD_INFLATION.getTo())) {
            ym = ym.plusMonths(1);
            sb.add(new MoneyAmountItem(ym, MoneyAmount.zero(series.getCurrency())));
        }

        return Stream.concat(series.items(), sb.build());

    }
}
