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

import java.util.Currency;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.NoSeriesDataFoundException;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
public class SortedMapMoneyAmountSeries extends MoneyAmountSeriesSupport {

    private SortedMap<YearMonth, MoneyAmount> values;


    public SortedMapMoneyAmountSeries(final Currency currency, final SortedMap<YearMonth, MoneyAmount> values) {
        super(currency);
        this.values = values;
    }

    public SortedMapMoneyAmountSeries(final String currency) {
        this(Currency.getInstance(currency), new TreeMap<YearMonth, MoneyAmount>());
    }
    
    public SortedMapMoneyAmountSeries(final Currency currency) {
        this(currency, new TreeMap<YearMonth, MoneyAmount>());
    }
    
    /*public SortedMapMoneyAmountSeries(final String currency, final YearMonth from, final YearMonth to, final int[] values) {
        this(currency);
        if (from.monthsUntil(to) != values.length) {
            throw new IllegalArgumentException(
                    MessageFormat.format(
                            "There must be one value for each month. There were {0} months and {1} values.",
                            from.monthsUntil(to),
                            values.length));
        }
        YearMonth ym = from;
        int i = 0;
        while(ym.compareTo(to) <= 0){
            this.values.put(ym, new MoneyAmount(new BigDecimal(values[i++]), this.getCurrency()));
            ym = ym.next();
        }
    }*/

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
    protected MoneyAmount getAmountOrNull(int year, int month) {
        return this.values.get(new YearMonth(year, month));
    }

    @Override
    public void putAmount(int year, int month, MoneyAmount amount) {
        this.values.put(new YearMonth(year, month), amount);
    }

    @Override
    public void forEach(MoneyAmountProcessor processor) throws NoSeriesDataFoundException {
        for (Iterator<Map.Entry<YearMonth, MoneyAmount>> it = this.values.entrySet().iterator(); it.hasNext();) {
            Map.Entry<YearMonth, MoneyAmount> entry = it.next();
            processor.process(entry.getKey().getYear(), entry.getKey().getMonth(), entry.getValue());
        }
    }

}
