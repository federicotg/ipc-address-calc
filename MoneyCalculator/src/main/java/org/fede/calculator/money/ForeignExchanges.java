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
package org.fede.calculator.money;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;
import org.fede.calculator.money.series.IndexSeriesSupport;
import org.fede.calculator.money.series.JSONIndexSeries;
import org.fede.calculator.money.series.YearMonth;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
public class ForeignExchanges {

    private static final Map<Currency, ForeignExchange> FOREIGN_EXCHANGES_BY_CURRENCY = new HashMap<>();

    public static final ForeignExchange USD_ARS = new SimpleForeignExchange(
            JSONIndexSeries.readSeries("peso-dolar-libre.json"),
            Currency.getInstance("USD"),
            Currency.getInstance("ARS"));

    public static final ForeignExchange USD_XAU = new SimpleForeignExchange(
            JSONIndexSeries.readSeries("gold.json"),
            Currency.getInstance("XAU"),
            Currency.getInstance("USD"));

    public static final ForeignExchange USD_EUR = new SimpleForeignExchange(
            JSONIndexSeries.readSeries("euro-dolar.json"),
            Currency.getInstance("USD"),
            Currency.getInstance("EUR"));

    public static final ForeignExchange NO_FX = new SimpleForeignExchange(new IndexSeriesSupport() {
        @Override
        public YearMonth getFrom() {
            return new YearMonth(1, 1);
        }

        @Override
        public YearMonth getTo() {
            return new YearMonth(5000, 12);
        }

        @Override
        public BigDecimal getIndex(int year, int month) throws NoSeriesDataFoundException {
            return BigDecimal.ONE;
        }

        @Override
        public BigDecimal predictValue(int year, int month) throws NoSeriesDataFoundException {
            return BigDecimal.ONE;
        }
    }, Currency.getInstance("USD"), Currency.getInstance("USD"));

    static {
        FOREIGN_EXCHANGES_BY_CURRENCY.put(Currency.getInstance("USD"), NO_FX);
        FOREIGN_EXCHANGES_BY_CURRENCY.put(Currency.getInstance("ARS"), USD_ARS);
        FOREIGN_EXCHANGES_BY_CURRENCY.put(Currency.getInstance("EUR"), USD_EUR);
        FOREIGN_EXCHANGES_BY_CURRENCY.put(Currency.getInstance("XAU"), USD_XAU);
    }

    public static ForeignExchange getForeignExchange(Currency from, Currency to) {
        if (from.equals(to)) {
            return NO_FX;
        }

        Currency usd = null;
        Currency other = null;

        if (to.equals(Currency.getInstance("USD"))) {
            usd = to;
            other = from;
        } else if (from.equals(Currency.getInstance("USD"))) {
            usd = from;
            other = to;
        }

        if (usd != null) {
            ForeignExchange answer = FOREIGN_EXCHANGES_BY_CURRENCY.get(other);
            if (answer != null) {
                return answer;
            } else {
                throw new IllegalArgumentException("No foreign exchange from " + from.getSymbol() + " to " + to.getSymbol());
            }
        }

        return new CompoundForeignExchange(getForeignExchange(from, Currency.getInstance("USD")), getForeignExchange(to, Currency.getInstance("USD")));

    }

}
