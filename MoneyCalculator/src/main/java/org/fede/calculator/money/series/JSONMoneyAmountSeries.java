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
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import org.fede.calculator.money.Inflation;
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

    public JSONMoneyAmountSeries(String currency, List<JSONDataPoint> data) {
        this.currency = Currency.getInstance(currency);
        this.values = new TreeMap<>();
        for (JSONDataPoint dp : data) {
            this.values.put(new YearMonth(dp.getYear(), dp.getMonth()), new MoneyAmount(dp.getValue(), this.currency));
        }
    }

    private JSONMoneyAmountSeries(Currency currency, SortedMap<YearMonth, MoneyAmount> data) {
        this.currency = currency;
        this.values = data;
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
    public MoneyAmountSeries adjust(Inflation inflation, int referenceYear, int referenceMonth) throws NoSeriesDataFoundException {
        int fromYear = Math.max(this.getFromYear(), inflation.getFromYear());
        int fromMonth = Math.max(this.getFromMonth(), inflation.getFromMonth());

        int toYear = Math.min(this.getToYear(), inflation.getToYear());
        int toMonth = Math.min(this.getToMonth(), inflation.getToMonth());
        SortedMap<YearMonth, MoneyAmount> answer = new TreeMap<>();

        for (int m = fromMonth; m <= 12; m++) {
            //first year
            //answer.put(new YearMonth(fromYear, m), inflation.adjust(this.getAmount(fromYear, m), referenceYear, referenceMonth));
            this.addAdjustedValue(inflation, answer, fromYear, m, referenceYear, referenceMonth);
        }
        for (int y = fromYear + 1; y < toYear; y++) {
            for (int m = 1; m <= 12; m++) {
                //answer.put(new YearMonth(y, m), inflation.adjust(this.getAmount(y, m), referenceYear, referenceMonth));
                this.addAdjustedValue(inflation, answer, y, m, referenceYear, referenceMonth);
            }
        }
        for (int m = 1; m <= toMonth; m++) {
            //last year
            //answer.put(new YearMonth(toYear, m), inflation.adjust(this.getAmount(toYear, m), referenceYear, referenceMonth));
            this.addAdjustedValue(inflation, answer, toYear, m, referenceYear, referenceMonth);
        }
        return new JSONMoneyAmountSeries(this.currency, answer);
    }

    private void addAdjustedValue(Inflation inflation, SortedMap<YearMonth, MoneyAmount> answer,
            int year, int month, int referenceYear, int referenceMonth) throws NoSeriesDataFoundException {
        answer.put(new YearMonth(year, month), 
                inflation.adjust(this.getAmount(year, month), year, month, referenceYear, referenceMonth)
        );
    }

}
