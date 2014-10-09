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
package org.fede.calculator.money.series;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.NoSeriesDataFoundException;
import org.fede.calculator.money.json.JSONDataPoint;
import org.fede.calculator.money.json.JSONSeries;

/**
 *
 * @author fede
 */
public class JSONMoneyAmountSeries implements MoneyAmountSeries {

    public static MoneyAmountSeries readSeries(String name) {
        try (InputStream is = JSONIndexSeries.class.getResourceAsStream("/" + name)) {
            JSONSeries series = new ObjectMapper().readValue(is, JSONSeries.class);
            return new JSONMoneyAmountSeries(series.getCurrency(), series.getData());
        } catch (IOException ioEx) {
            throw new IllegalArgumentException("Could not read series named " + name, ioEx);
        }
    }

    private class YearMonth implements Comparable<YearMonth> {

        private final int year;
        private final int month;

        public YearMonth(int year, int month) {
            this.year = year;
            this.month = month;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 83 * hash + this.year;
            hash = 83 * hash + this.month;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof YearMonth
                    && this.year == ((YearMonth) obj).year
                    && this.month == ((YearMonth) obj).month;
        }

        @Override
        public int compareTo(YearMonth o) {
            if (o.year == this.year) {
                return this.month - o.month;
            }
            return this.year - o.year;
        }

    }
    private final Currency currency;
    private final SortedMap<YearMonth, MoneyAmount> values;

    public JSONMoneyAmountSeries(Currency currency) {
        this.currency = currency;
        this.values = new TreeMap<>();

    }

    public JSONMoneyAmountSeries(String currency, List<JSONDataPoint> data) {
        this.currency = Currency.getInstance(currency);
        this.values = new TreeMap<>();
        for (JSONDataPoint dp : data) {
            this.values.put(new YearMonth(dp.getYear(), dp.getMonth()), new MoneyAmount(dp.getValue(), this.currency));
        }
    }

    @Override
    public MoneyAmount getAmount(Date day) throws NoSeriesDataFoundException {

        Calendar cal = Calendar.getInstance();
        cal.setTime(day);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        return this.getAmount(year, month);

    }

    @Override
    public MoneyAmount getAmount(int year, int month) throws NoSeriesDataFoundException {
        return this.values.get(new YearMonth(year, month));
    }

    @Override
    public MoneyAmount getAmount(int year) throws NoSeriesDataFoundException {
        return this.getAmount(year, 1);
    }

    @Override
    public int getFromYear() {
        return this.values.firstKey().year;
    }

    @Override
    public int getToYear() {
        return this.values.lastKey().year;
    }

    @Override
    public int getFromMonth() {
        return this.values.firstKey().month;
    }

    @Override
    public int getToMonth() {
        return this.values.lastKey().month;
    }

    @Override
    public void putAmount(int year, int month, MoneyAmount amount) {
        this.values.put(new YearMonth(year, month), amount);
    }

    @Override
    public Currency getCurrency() {
        return currency;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MoneyAmountSeries)) {
            return false;
        }
        MoneyAmountSeries other = (MoneyAmountSeries) obj;
        boolean equal = this.getCurrency().equals(other.getCurrency())
                && this.getFromMonth() == other.getFromMonth()
                && this.getFromYear() == other.getFromYear()
                && this.getToMonth() == other.getToMonth()
                && this.getToYear() == other.getToYear();
        if (!equal) {
            return false;
        }
        equal = true;
        try {
            for (Iterator<Map.Entry<YearMonth, MoneyAmount>> it = this.values.entrySet().iterator(); it.hasNext();) {
                Map.Entry<YearMonth, MoneyAmount> e = it.next();
                equal &= e.getValue().equals(other.getAmount(e.getKey().year, e.getKey().month));
            }
            return equal;
        } catch (NoSeriesDataFoundException ex) {
            return false;
        }

    }

    @Override
    public void forEach(MoneyAmountSeriesProcessor processor) {
        for (Iterator<Map.Entry<YearMonth, MoneyAmount>> it = this.values.entrySet().iterator(); it.hasNext();) {
            Map.Entry<YearMonth, MoneyAmount> entry = it.next();
            processor.process(entry.getKey().year, entry.getKey().month, entry.getValue());
        }
    }

}
