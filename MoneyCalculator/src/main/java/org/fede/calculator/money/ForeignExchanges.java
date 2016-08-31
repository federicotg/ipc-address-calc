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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.fede.calculator.money.series.IndexSeries;
import org.fede.calculator.money.series.IndexSeriesSupport;
import org.fede.calculator.money.series.SeriesReader;
import org.fede.calculator.money.series.YearMonth;
import org.fede.util.Pair;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
public class ForeignExchanges {

    private static final Map<String, String> INTERMEDIATE_FOREIGN_EXCHANGES = new HashMap<>();

    private static final Map<Pair<String, String>, ForeignExchange> DIRECT_FOREIGN_EXCHANGES = new HashMap<>();

    private static final String USD = "USD";

    public static final ForeignExchange USD_ARS = new SimpleForeignExchange(
            SeriesReader.readIndexSeries("index/peso-dolar-libre.json"),
            USD,
            "ARS");

    public static final ForeignExchange USD_XAU = new SimpleForeignExchange(
            SeriesReader.readIndexSeries("index/gold.json"),
            "XAU",
            USD);

    public static final ForeignExchange USD_EUR = new SimpleForeignExchange(
            SeriesReader.readIndexSeries("index/euro-dolar.json"),
            USD,
            "EUR");

    public static final ForeignExchange ARS_CONAAFA = new SimpleForeignExchange(
            SeriesReader.readIndexSeries("index/CONAAFA_AR-peso.json"),
            "CONAAFA",
            "ARS");

    public static final ForeignExchange ARS_CONBALA = new SimpleForeignExchange(
            SeriesReader.readIndexSeries("index/CONBALA_AR-peso.json"),
            "CONBALA",
            "ARS");

    private static final IndexSeries CONSTANT_INDEX = new IndexSeriesSupport() {
        @Override
        public YearMonth getFrom() {
            return new YearMonth(1, 1);
        }

        @Override
        public YearMonth getTo() {
            return new YearMonth(5000, 12);
        }

        @Override
        public BigDecimal getIndex(int year, int month) {
            return BigDecimal.ONE;
        }

        @Override
        public BigDecimal predictValue(int year, int month) {
            return BigDecimal.ONE;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(this);
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj;
        }

    };

    private static void map(String from, String to, ForeignExchange fx) {
        DIRECT_FOREIGN_EXCHANGES.put(new Pair<>(from, to), fx);
        DIRECT_FOREIGN_EXCHANGES.put(new Pair<>(to, from), fx);
    }

    static {

        // direct conversions
        map("ARS", "USD", USD_ARS);
        map("EUR", "USD", USD_EUR);
        map("XAU", "USD", USD_XAU);
        map("ARS", "USD", USD_ARS);
        map("ARS", "CONAAFA", ARS_CONAAFA);
        map("ARS", "CONBALA", ARS_CONBALA);

        INTERMEDIATE_FOREIGN_EXCHANGES.put("CONAAFA", "ARS");
        INTERMEDIATE_FOREIGN_EXCHANGES.put("CONBALA", "ARS");
        INTERMEDIATE_FOREIGN_EXCHANGES.put("EUR", "USD");
        INTERMEDIATE_FOREIGN_EXCHANGES.put("XAU", "USD");
        INTERMEDIATE_FOREIGN_EXCHANGES.put("ARS", "USD");
    }

    public static ForeignExchange getForeignExchange(String from, String to) {
        if (from.equals(to)) {
            return getIdentityForeignExchange(from);
        }

        ForeignExchange answer = DIRECT_FOREIGN_EXCHANGES.get(new Pair<>(from, to));
        if (answer != null) {
            return answer;
        }

        String intermediate = INTERMEDIATE_FOREIGN_EXCHANGES.get(from);
        if (intermediate == null) {
            intermediate = INTERMEDIATE_FOREIGN_EXCHANGES.get(to);
        }

        if (intermediate == null) {

            throw new IllegalArgumentException("No FX from " + from + " to " + to);
        }

        return new CompoundForeignExchange(
                DIRECT_FOREIGN_EXCHANGES.get(new Pair<>(from, intermediate)),
                getForeignExchange(intermediate, to)
        );
    }

    public static ForeignExchange getIdentityForeignExchange(String currency) {
        return new SimpleForeignExchange(CONSTANT_INDEX, currency, currency);
    }

}
