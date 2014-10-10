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
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.fede.calculator.money.series.IndexSeries;
import org.fede.calculator.money.series.JSONIndexSeries;
import org.fede.calculator.money.series.JSONMoneyAmountSeries;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.YearMonth;

/**
 *
 * @author fede
 */
public class SimpleForeignExchange implements ForeignExchange, MathConstants {

    private static final BigDecimal ONE = BigDecimal.ONE.setScale(SCALE, ROUNDING_MODE);

    private static class CurrencyPair {

        private final Currency from;
        private final Currency to;

        private CurrencyPair(Currency from, Currency to) {
            this.to = to;
            this.from = from;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 23 * hash + Objects.hashCode(this.from);
            hash = 23 * hash + Objects.hashCode(this.to);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof CurrencyPair) {
                final CurrencyPair other = (CurrencyPair) obj;
                return Objects.equals(this.from, other.from) && Objects.equals(this.to, other.to);
            }
            return false;
        }
    }
    private final Map<CurrencyPair, IndexSeries> exchangeRates = new HashMap<>();

    public SimpleForeignExchange() {
        this.exchangeRates.put(
                new CurrencyPair(Currency.getInstance("ARS"), Currency.getInstance("USD")),
                JSONIndexSeries.readSeries("peso-dolar-libre.json"));
    }

    @Override
    public MoneyAmount exchange(MoneyAmount amount, Currency targetCurrency, int year, int month) throws NoSeriesDataFoundException {

        IndexSeries exchangeRate = this.getSeries(targetCurrency, amount.getCurrency());
        if (exchangeRate != null) {
            return amount.exchange(targetCurrency, exchangeRate.getIndex(year, month));
        }

        exchangeRate = this.getSeries(amount.getCurrency(), targetCurrency);
        if (exchangeRate != null) {
            return amount.exchange(targetCurrency, ONE.divide(exchangeRate.getIndex(year, month), ROUNDING_MODE));
        }

        throw new IllegalArgumentException("Unknown currency.");
    }

    private IndexSeries getSeries(Currency c1, Currency c2) {
        return this.exchangeRates.get(new CurrencyPair(c1, c2));
    }

    @Override
    public MoneyAmountSeries exchange(MoneyAmountSeries series, Currency targetCurrency) throws NoSeriesDataFoundException {
        
        YearMonth from = this.getAnySeries(series.getCurrency(), targetCurrency).maximumFrom(series);
        YearMonth to = this.getAnySeries(series.getCurrency(), targetCurrency).minimumTo(series);
        

        final int fromYear = from.getYear();
        final int fromMonth = from.getMonth();
        final int toYear = to.getYear();
        final int toMonth = to.getMonth();

        final MoneyAmountSeries answer = new JSONMoneyAmountSeries(targetCurrency);

        for (int m = fromMonth; m <= 12; m++) {
            //first year
            answer.putAmount(fromYear, m, this.exchange(series.getAmount(fromYear, m), targetCurrency, fromYear, m));
        }
        for (int y = fromYear + 1; y < toYear; y++) {
            for (int m = 1; m <= 12; m++) {
                answer.putAmount(y, m, this.exchange(series.getAmount(y, m), targetCurrency, y, m));
            }
        }
        for (int m = 1; m <= toMonth; m++) {
            //last year
            answer.putAmount(toYear, m, this.exchange(series.getAmount(toYear, m), targetCurrency, toYear, m));
        }
        return answer;

    }

    @Override
    public int getFromMonth(Currency from, Currency to) {
        return this.getAnySeries(from, to).getFromMonth();
    }

    @Override
    public int getToMonth(Currency from, Currency to) {
        return this.getAnySeries(from, to).getToMonth();
    }

    @Override
    public int getFromYear(Currency from, Currency to) {
        return this.getAnySeries(from, to).getFromYear();
    }

    @Override
    public int getToYear(Currency from, Currency to) {
        return this.getAnySeries(from, to).getToYear();
    }

    private IndexSeries getAnySeries(Currency from, Currency to) {
        IndexSeries s = this.getSeries(from, to);
        if (s == null) {
            s = this.getSeries(to, from);
        }
        if (s == null) {
            throw new IllegalArgumentException("Unknown currency.");
        }
        return s;
    }
}
