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

import java.util.Map;
import java.util.SequencedMap;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.stream.Stream;
import org.fede.calculator.money.Currency;
import org.fede.calculator.money.MoneyAmount;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
public class SortedMapMoneyAmountSeries extends MoneyAmountSeriesSupport {

    private final SequencedMap<YearMonth, MoneyAmount> values;

    public SortedMapMoneyAmountSeries(final Currency currency) {
        super(currency);
        this.values = new TreeMap<>(YearMonth::compareTo);
    }

    @Override
    protected MoneyAmountSeries createNew() {
        return new SortedMapMoneyAmountSeries(this.getCurrency());
    }

    @Override
    public YearMonth getFrom() {
        return this.values.firstEntry().getKey();
    }

    @Override
    public YearMonth getTo() {
        return this.values.lastEntry().getKey();
    }

    @Override
    protected MoneyAmount getAmountOrNull(YearMonth ym) {
        return this.values.get(ym);
    }

    @Override
    public void putAmount(YearMonth ym, MoneyAmount amount) {
        this.values.put(ym, amount);
    }

    @Override
    public void forEach(BiConsumer<YearMonth, MoneyAmount> consumer) {
        this.values.sequencedEntrySet()
                .stream()
                .forEach(e -> consumer.accept(e.getKey(), e.getValue()));
    }

    @Override
    protected boolean hasValue(YearMonth moment) {
        return this.values.containsKey(moment);
    }

    @Override
    public Stream<MoneyAmount> moneyAmountStream() {
        return this.values.sequencedValues().stream();
    }

    @Override
    public Stream<MoneyAmount> filter(BiPredicate<YearMonth, MoneyAmount> predicate) {
        return this.values.sequencedEntrySet()
                .stream()
                .filter(e -> predicate.test(e.getKey(), e.getValue()))
                .map(Map.Entry::getValue);
    }

    @Override
    public Stream<YearMonth> yearMonthStream() {
        return this.values.sequencedKeySet().stream();
    }

}
