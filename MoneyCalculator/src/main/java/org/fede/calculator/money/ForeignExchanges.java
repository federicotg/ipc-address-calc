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

import java.util.HashMap;
import java.util.Map;
import org.fede.calculator.money.series.IndexSeriesSupport;
import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.InvestmentAsset;
import org.fede.calculator.money.series.InvestmentEvent;
import org.fede.calculator.money.series.InvestmentType;
import org.fede.calculator.money.series.SeriesReader;
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
            () -> SeriesReader.readIndexSeries("index/peso-dolar-libre.json"),
            USD,
            "ARS");

    public static final ForeignExchange USD_LETE = new SimpleForeignExchange(
            () -> IndexSeriesSupport.CONSTANT_SERIES, "USD", "LETE");

    public static final ForeignExchange USD_XAU = new SimpleForeignExchange(
            () -> SeriesReader.readIndexSeries("index/gold.json"),
            "XAU",
            USD);

    public static final ForeignExchange USD_EUR = new SimpleForeignExchange(
            () -> SeriesReader.readIndexSeries("index/USD-EUR.json"),
            "EUR",
            USD);

    public static final ForeignExchange USD_DAI = new SimpleForeignExchange(
            () -> SeriesReader.readIndexSeries("index/USD-DAI.json"),
            "DAI",
            USD);

    public static final ForeignExchange ARS_LECAP = new SimpleForeignExchange(
            () -> IndexSeriesSupport.CONSTANT_SERIES,
            "LECAP",
            "ARS");

    public static final ForeignExchange ARS_CONAAFA = new SimpleForeignExchange(
            () -> SeriesReader.readIndexSeries("index/CONAAFA_AR-peso.json"),
            "CONAAFA",
            "ARS");

    public static final ForeignExchange ARS_CONBALA = new SimpleForeignExchange(
            () -> SeriesReader.readIndexSeries("index/CONBALA_AR-peso.json"),
            "CONBALA",
            "ARS");

    public static final ForeignExchange ARS_CAPLUSA = new SimpleForeignExchange(
            () -> SeriesReader.readIndexSeries("index/CAPLUSA_AR-peso.json"),
            "CAPLUSA",
            "ARS");

    public static final ForeignExchange ARS_UVA = new SimpleForeignExchange(
            () -> SeriesReader.readIndexSeries("index/UVA-peso.json"),
            "UVA",
            "ARS");

    public static final ForeignExchange USD_AY24 = new SimpleForeignExchange(
            () -> SeriesReader.readIndexSeries("index/AY24-USD.json"),
            "AY24", USD);

    public static final ForeignExchange USD_CSPX = new SimpleForeignExchange(
            () -> SeriesReader.readIndexSeries("index/CSPX-USD.json"),
            "CSPX", USD);

    public static final ForeignExchange USD_EIMI = new SimpleForeignExchange(
            () -> SeriesReader.readIndexSeries("index/EIMI-USD.json"),
            "EIMI", USD);

    public static final ForeignExchange USD_XRSU = new SimpleForeignExchange(
            () -> SeriesReader.readIndexSeries("index/XRSU-USD.json"),
            "XRSU", USD);

    public static final ForeignExchange EUR_MEUD = new SimpleForeignExchange(
            () -> SeriesReader.readIndexSeries("index/MEUD-EUR.json"),
            "MEUD", "EUR");

    private static void map(String from, String to, ForeignExchange fx) {
        DIRECT_FOREIGN_EXCHANGES.put(new Pair<>(from, to), fx);
        DIRECT_FOREIGN_EXCHANGES.put(new Pair<>(to, from), fx);
    }

    static {

        // direct conversions
        map("ARS", USD, USD_ARS);
        map("LETE", USD, USD_LETE);
        map(USD, "EUR", USD_EUR);
        map(USD, "DAI", USD_DAI);
        map("XAU", USD, USD_XAU);
        map("ARS", USD, USD_ARS);
        map("ARS", "CONAAFA", ARS_CONAAFA);
        map("ARS", "CONBALA", ARS_CONBALA);
        map("ARS", "CAPLUSA", ARS_CAPLUSA);
        map("ARS", "LECAP", ARS_LECAP);
        map("ARS", "UVA", ARS_UVA);
        map(USD, "AY24", USD_AY24);
        map(USD, "CSPX", USD_CSPX);
        map("EUR", "MEUD", EUR_MEUD);
        map(USD, "XRSU", USD_XRSU);
        map(USD, "EIMI", USD_EIMI);

        INTERMEDIATE_FOREIGN_EXCHANGES.put("UVA", "ARS");
        INTERMEDIATE_FOREIGN_EXCHANGES.put("CONAAFA", "ARS");
        INTERMEDIATE_FOREIGN_EXCHANGES.put("CAPLUSA", "ARS");
        INTERMEDIATE_FOREIGN_EXCHANGES.put("CONBALA", "ARS");
        INTERMEDIATE_FOREIGN_EXCHANGES.put("LECAP", "ARS");
        INTERMEDIATE_FOREIGN_EXCHANGES.put("MEUD", "EUR");
        INTERMEDIATE_FOREIGN_EXCHANGES.put("DAI", "USD");
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
        return new SimpleForeignExchange(() -> IndexSeriesSupport.CONSTANT_SERIES, currency, currency);
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

        answer.setOut(exchangeInto(investment.getOut(), targetCurrency));
        answer.setType(investment.getType());
        answer.setInterest(investment.getInterest());

        // investment in original currency.
        InvestmentAsset asset = new InvestmentAsset();
        asset.setCurrency(investment.getInvestment().getCurrency());
        asset.setAmount(investment.getInvestment().getAmount());
        answer.setInvestment(asset);

        return answer;

    }

    private static InvestmentEvent exchangeInto(InvestmentEvent in, String currency) {
        if (in == null) {
            return null;
        }
        ForeignExchange fx = ForeignExchanges.getForeignExchange(in.getCurrency(), currency);

        final var fee = new MoneyAmount(in.getFee(), in.getCurrency());

        InvestmentEvent answer = new InvestmentEvent();
        MoneyAmount ma = fx.exchange(in.getMoneyAmount(), currency, in.getDate());
        answer.setAmount(ma.getAmount());
        answer.setCurrency(ma.getCurrency());
        answer.setDate(in.getDate());
        answer.setFee(fx.exchange(fee, currency, in.getDate()).getAmount());
        return answer;
    }

}
