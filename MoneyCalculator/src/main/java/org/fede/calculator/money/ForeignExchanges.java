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

    private static final Map<String, ForeignExchange> FOREIGN_EXCHANGES_BY_CURRENCY = new HashMap<>();

    private static final String USD = "USD";
    
    public static final ForeignExchange USD_ARS = new SimpleForeignExchange(
            JSONIndexSeries.readSeries("peso-dolar-libre.json"),
            USD,
            "ARS");

    public static final ForeignExchange USD_XAU = new SimpleForeignExchange(
            JSONIndexSeries.readSeries("gold.json"),
            "XAU",
            USD);

    public static final ForeignExchange USD_EUR = new SimpleForeignExchange(
            JSONIndexSeries.readSeries("euro-dolar.json"),
            USD,
            "EUR");

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
    }, USD, USD);

    static {
        FOREIGN_EXCHANGES_BY_CURRENCY.put(USD, NO_FX);
        FOREIGN_EXCHANGES_BY_CURRENCY.put("ARS", USD_ARS);
        FOREIGN_EXCHANGES_BY_CURRENCY.put("EUR", USD_EUR);
        FOREIGN_EXCHANGES_BY_CURRENCY.put("XAU", USD_XAU);
    }

    public static ForeignExchange getForeignExchange(String from, String to) {
        if (from.equals(to)) {
            return NO_FX;
        }

        String usd = null;
        String other = null;

        if (to.equals(USD)) {
            usd = to;
            other = from;
        } else if (from.equals(USD)) {
            usd = from;
            other = to;
        }

        if (usd != null) {
            ForeignExchange answer = FOREIGN_EXCHANGES_BY_CURRENCY.get(other);
            if (answer != null) {
                return answer;
            } else {
                throw new IllegalArgumentException("No foreign exchange from " + from + " to " + to);
            }
        }

        return new CompoundForeignExchange(getForeignExchange(from, USD), getForeignExchange(to, USD));

    }

}
