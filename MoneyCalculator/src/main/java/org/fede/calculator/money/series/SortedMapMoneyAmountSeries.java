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

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import org.fede.calculator.money.MoneyAmount;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
public class SortedMapMoneyAmountSeries extends MoneyAmountSeriesSupport {

    private final SortedMap<YearMonth, MoneyAmount> values;

    public SortedMapMoneyAmountSeries(final String currency, final SortedMap<YearMonth, MoneyAmount> values) {
        super(currency);
        this.values = values;
    }

    public SortedMapMoneyAmountSeries(final String currency) {
        this(currency, new TreeMap<>());
    }

    @Override
    protected MoneyAmountSeries createNew() {
        return new SortedMapMoneyAmountSeries(this.getCurrency());
    }

    @Override
    public YearMonth getFrom() {
        return this.values.firstKey();
    }

    @Override
    public YearMonth getTo() {
        return this.values.lastKey();
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
        this.values.forEach(consumer);
    }
    
    @Override
    protected boolean hasValue(YearMonth moment) {
        return this.values.containsKey(moment);
    }

    @Override
    public Stream<MoneyAmount> moneyAmountStream() {
        return this.values.values().stream();
    }

}
