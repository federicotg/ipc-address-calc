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
import java.math.RoundingMode;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.fede.calculator.money.series.IndexSeries;
import org.fede.calculator.money.series.JSONIndexSeries;

/**
 *
 * @author fede
 */
public class SimpleForeignExchange implements ForeignExchange {

    private static final BigDecimal ONE = BigDecimal.ONE.setScale(10);

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
        this.exchangeRates.put(new CurrencyPair(Currency.getInstance("ARS"), Currency.getInstance("USD")), new JSONIndexSeries("peso-dolar-libre.json"));
    }

    @Override
    public MoneyAmount exchangeAmountIntoCurrency(MoneyAmount amount, Currency currency, int year, int month) throws NoIndexDataFoundException {

        IndexSeries exchangeRate = this.exchangeRates.get(new CurrencyPair(currency, amount.getCurrency()));
        if (exchangeRate != null) {
            return amount.exchange(currency, exchangeRate.getIndex(year, month));
        }

        exchangeRate = this.exchangeRates.get(new CurrencyPair(amount.getCurrency(), currency));
        if (exchangeRate != null) {
            return amount.exchange(currency, ONE.divide(exchangeRate.getIndex(year, month), RoundingMode.HALF_UP));
        }

        throw new IllegalArgumentException("Unknown currency.");
    }

}
