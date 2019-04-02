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
import java.util.Optional;
import org.fede.calculator.money.series.IndexSeries;
import org.fede.calculator.money.series.IndexSeriesSupport;
import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.InvestmentAsset;
import org.fede.calculator.money.series.InvestmentEvent;
import org.fede.calculator.money.series.InvestmentType;
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

    public static final ForeignExchange ARS_CAPLUSA = new SimpleForeignExchange(
            SeriesReader.readIndexSeries("index/CAPLUSA_AR-peso.json"),
            "CAPLUSA",
            "ARS");

    public static final ForeignExchange ARS_UVA = new SimpleForeignExchange(
            SeriesReader.readIndexSeries("index/UVA-peso.json"),
            "UVA",
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
        map("ARS", "CAPLUSA", ARS_CAPLUSA);
        map("ARS", "UVA", ARS_UVA);

        INTERMEDIATE_FOREIGN_EXCHANGES.put("UVA", "ARS");
        INTERMEDIATE_FOREIGN_EXCHANGES.put("CONAAFA", "ARS");
        INTERMEDIATE_FOREIGN_EXCHANGES.put("CAPLUSA", "ARS");
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

    public static Investment exchange(Investment investment, String targetCurrency) {

        Investment answer = new Investment();

        answer.setId(investment.getId());

        if (investment.getType().equals(InvestmentType.USD)) {
            InvestmentEvent usdIn = new InvestmentEvent();
            usdIn.setCurrency(targetCurrency);
            usdIn.setDate(investment.getInitialDate());
            usdIn.setAmount(investment.getInvestment().getAmount());
            answer.setIn(usdIn);
        } else {
            answer.setIn(exchangeInto(investment.getIn(), targetCurrency));
        }

        final ForeignExchange fx = ForeignExchanges.getForeignExchange(investment.getInvestment().getCurrency(), targetCurrency);

        YearMonth ym = Optional.ofNullable(investment.getOut()).map(InvestmentEvent::getDate).map(YearMonth::new).orElse(fx.getTo());

        answer.setOut(exchangeInto(investment.getOut(), targetCurrency));
        answer.setType(investment.getType());
        answer.setInterest(investment.getInterest());
        InvestmentAsset asset = new InvestmentAsset();
        asset.setCurrency(targetCurrency);
        asset.setAmount(fx.exchange(investment.getInvestment().getMoneyAmount(), targetCurrency, ym.getYear(), ym.getMonth()).getAmount());

        answer.setInvestment(asset);

        return answer;

    }

    private static InvestmentEvent exchangeInto(InvestmentEvent in, String currency) {
        if (in == null) {
            return null;
        }
        ForeignExchange fx = ForeignExchanges.getForeignExchange(in.getCurrency(), currency);

        InvestmentEvent answer = new InvestmentEvent();
        MoneyAmount ma = fx.exchange(in.getMoneyAmount(), currency, in.getDate());
        answer.setAmount(ma.getAmount());
        answer.setCurrency(ma.getCurrency());
        answer.setDate(in.getDate());
        return answer;
    }

}
