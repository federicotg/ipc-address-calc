/*
 * Copyright (C) 2016 Federico Tello Gentile <federicotg@gmail.com>
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
package org.fede.calculator.money.series;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.stream.Stream;
import org.fede.calculator.money.MoneyAmount;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
public class SortedMapMoneyAmountSeries extends MoneyAmountSeriesSupport {

    private static final Comparator<Map.Entry<YearMonth, ?>> SERIES_COMPARATOR = Comparator.comparing(Map.Entry::getKey);
    
    private final Map<YearMonth, MoneyAmount> values;
    private YearMonth from;
    private YearMonth to;

    public SortedMapMoneyAmountSeries(final String currency, final Map<YearMonth, MoneyAmount> values) {
        super(currency);
        this.values = values;
        this.from = Optional.ofNullable(values)
                .orElseGet(Collections::emptyMap)
                .keySet()
                .stream()
                .reduce(YearMonth::min)
                .orElse(null);
        this.to = Optional.ofNullable(values)
                .orElseGet(Collections::emptyMap)
                .keySet()
                .stream()
                .reduce(YearMonth::max)
                .orElse(null);

    }

    public SortedMapMoneyAmountSeries(final String currency) {
        this(currency, new HashMap<>());
    }

    @Override
    protected MoneyAmountSeries createNew() {
        return new SortedMapMoneyAmountSeries(this.getCurrency());
    }

    @Override
    public YearMonth getFrom() {
        return this.from;
    }

    @Override
    public YearMonth getTo() {
        return this.to;
    }

    @Override
    protected MoneyAmount getAmountOrNull(YearMonth ym) {
        return this.values.get(ym);
    }

    @Override
    public void putAmount(YearMonth ym, MoneyAmount amount) {
        this.values.put(ym, amount);
        this.from = Optional.ofNullable(this.from)
                .map(v -> v.min(ym))
                .orElse(ym);
        this.to = Optional.ofNullable(this.to)
                .map(v -> v.max(ym))
                .orElse(ym);
    }

    @Override
    public void forEach(BiConsumer<YearMonth, MoneyAmount> consumer) {
        this.values.entrySet()
                .stream()
                .sorted(SERIES_COMPARATOR)
                .forEach(e -> consumer.accept(e.getKey(), e.getValue()));
    }

    @Override
    protected boolean hasValue(YearMonth moment) {
        return this.values.containsKey(moment);
    }

    @Override
    public Stream<MoneyAmount> moneyAmountStream() {
        return this.values.values().stream();
    }

    @Override
    public Stream<MoneyAmount> filter(BiPredicate<YearMonth, MoneyAmount> predicate) {

        return this.values.entrySet().stream()
                .filter(e -> predicate.test(e.getKey(), e.getValue()))
                .map(Map.Entry::getValue);
    }

    @Override
    public Stream<YearMonth> yearMonthStream() {
        return this.values.keySet().stream();
    }

}
