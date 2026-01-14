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
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import org.fede.calculator.money.series.IndexSeriesSupport;
import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.InvestmentAsset;
import org.fede.calculator.money.series.InvestmentEvent;
import org.fede.calculator.money.series.InvestmentType;
import org.fede.calculator.money.series.SeriesReader;
import java.time.YearMonth;
import java.util.EnumMap;
import static org.fede.calculator.money.Currency.*;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
public class ForeignExchanges {

    private static final Map<FromTo, BiFunction<MoneyAmount, YearMonth, MoneyAmount>> FX_FUNCTION_CACHE = new ConcurrentHashMap<>();

    private static final Map<Currency, ForeignExchange> IDENTITY_FX = new EnumMap<>(Currency.class);

    private static final Map<Currency, Currency> INTERMEDIATE_FOREIGN_EXCHANGES = new EnumMap<>(
            Map.ofEntries(
                    Map.entry(UVA, ARS),
                    Map.entry(CONAAFA, ARS),
                    Map.entry(CAPLUSA, ARS),
                    Map.entry(CONBALA, ARS),
                    Map.entry(LECAP, ARS),
                    Map.entry(MEUD, EUR),
                    Map.entry(CSPX, USD),
                    Map.entry(EIMI, USD),
                    Map.entry(IWDA, USD),
                    Map.entry(DAI, USD),
                    Map.entry(ARS, USD),
                    Map.entry(EMIM, EIMI),
                    Map.entry(SXR8, CSPX),
                    Map.entry(MEUS, MEUD),
                    Map.entry(RTWOE, RTWO)
            ));

    private static final Map<FromTo, ForeignExchange> DIRECT_FOREIGN_EXCHANGES;

    public static final ForeignExchange USD_ARS = new SimpleForeignExchange(
            () -> SeriesReader.readIndexSeries("index/peso-dolar-libre.json"),
            USD,
            ARS);

    public static final ForeignExchange USD_LETE = new SimpleForeignExchange(
            () -> IndexSeriesSupport.CONSTANT_SERIES, USD, LETE);

    public static final ForeignExchange USD_EUR = new SimpleForeignExchange(
            () -> SeriesReader.readIndexSeries("index/USD-EUR.json"),
            EUR,
            USD);

    public static final ForeignExchange USD_DAI = new SimpleForeignExchange(
            () -> SeriesReader.readIndexSeries("index/USD-DAI.json"),
            DAI,
            USD);

    public static final ForeignExchange ARS_LECAP = new SimpleForeignExchange(
            () -> IndexSeriesSupport.CONSTANT_SERIES,
            LECAP,
            ARS);

    public static final ForeignExchange ARS_CONAAFA = new SimpleForeignExchange(
            () -> SeriesReader.readIndexSeries("index/CONAAFA_AR-peso.json"),
            CONAAFA, ARS);

    public static final ForeignExchange ARS_CONBALA = new SimpleForeignExchange(
            () -> SeriesReader.readIndexSeries("index/CONBALA_AR-peso.json"),
            CONBALA, ARS);

    public static final ForeignExchange ARS_CAPLUSA = new SimpleForeignExchange(
            () -> SeriesReader.readIndexSeries("index/CAPLUSA_AR-peso.json"),
            CAPLUSA, ARS);

    public static final ForeignExchange ARS_UVA = new SimpleForeignExchange(
            () -> SeriesReader.readIndexSeries("index/UVA-peso.json"),
            UVA, ARS);

    public static final ForeignExchange USD_AY24 = new SimpleForeignExchange(
            () -> SeriesReader.readIndexSeries("index/AY24-USD.json"),
            AY24, USD);

    public static final ForeignExchange USD_CSPX = new SimpleForeignExchange(
            () -> SeriesReader.readIndexSeries("index/CSPX-USD.json"),
            CSPX, USD);

    public static final ForeignExchange USD_EIMI = new SimpleForeignExchange(
            () -> SeriesReader.readIndexSeries("index/EIMI-USD.json"),
            EIMI, USD);

    public static final ForeignExchange USD_XRSU = new SimpleForeignExchange(
            () -> SeriesReader.readIndexSeries("index/XRSU-USD.json"),
            XRSU, USD);

    public static final ForeignExchange USD_RTWO = new SimpleForeignExchange(
            () -> SeriesReader.readIndexSeries("index/RTWO-USD.json"),
            RTWO, USD);

    public static final ForeignExchange USD_XUSE = new SimpleForeignExchange(
            () -> SeriesReader.readIndexSeries("index/XUSE-USD.json"),
            XUSE, USD);

    public static final ForeignExchange USD_IWDA = new SimpleForeignExchange(
            () -> SeriesReader.readIndexSeries("index/IWDA-USD.json"),
            IWDA, USD);

    public static final ForeignExchange EUR_MEUD = new SimpleForeignExchange(
            () -> SeriesReader.readIndexSeries("index/MEUD-EUR.json"),
            MEUD, EUR);

    private static void map(Map<FromTo, ForeignExchange> temporalMap, Currency from, Currency to, ForeignExchange fx) {
        temporalMap.put(new FromTo(from, to), fx);
        temporalMap.put(new FromTo(to, from), fx);
    }

    static {

        final Map<FromTo, ForeignExchange> temporalMap = new HashMap<>();
        // direct conversions
        map(temporalMap, ARS, USD, USD_ARS);
        map(temporalMap, LETE, USD, USD_LETE);
        map(temporalMap, USD, EUR, USD_EUR);
        map(temporalMap, USD, DAI, USD_DAI);
        map(temporalMap, ARS, CONAAFA, ARS_CONAAFA);
        map(temporalMap, ARS, CONBALA, ARS_CONBALA);
        map(temporalMap, ARS, CAPLUSA, ARS_CAPLUSA);
        map(temporalMap, ARS, LECAP, ARS_LECAP);
        map(temporalMap, ARS, UVA, ARS_UVA);
        map(temporalMap, USD, AY24, USD_AY24);
        map(temporalMap, USD, CSPX, USD_CSPX);
        map(temporalMap, USD, IWDA, USD_IWDA);
        map(temporalMap, EUR, MEUD, EUR_MEUD);
        map(temporalMap, USD, XRSU, USD_XRSU);
        map(temporalMap, USD, RTWO, USD_RTWO);
        map(temporalMap, USD, XUSE, USD_XUSE);
        map(temporalMap, USD, EIMI, USD_EIMI);

        // direct conversion for alternative currency ETFs
        map(temporalMap, EIMI, EMIM, ForeignExchanges.getIdentityForeignExchange(EIMI));
        map(temporalMap, CSPX, SXR8, ForeignExchanges.getIdentityForeignExchange(CSPX));
        map(temporalMap, MEUD, MEUS, ForeignExchanges.getIdentityForeignExchange(MEUD));
        map(temporalMap, RTWO, RTWOE, ForeignExchanges.getIdentityForeignExchange(RTWO));

        DIRECT_FOREIGN_EXCHANGES = Collections.unmodifiableMap(temporalMap);

    }

    public static BiFunction<MoneyAmount, YearMonth, MoneyAmount> getMoneyAmountForeignExchange(Currency from, Currency to) {
        return FX_FUNCTION_CACHE.computeIfAbsent(new FromTo(from, to), ForeignExchanges::getMoneyAmountForeignExchange);
    }

    private static BiFunction<MoneyAmount, YearMonth, MoneyAmount> getMoneyAmountForeignExchange(FromTo fromTo) {
        return (amount, ym) -> getForeignExchange(fromTo.from(), fromTo.to()).exchange(amount, fromTo.to(), ym);
    }

    public static ForeignExchange getForeignExchange(Currency from, Currency to) {

        if (from == to) {
            return IDENTITY_FX.computeIfAbsent(to, ForeignExchanges::getIdentityForeignExchange);
        }

        ForeignExchange answer = DIRECT_FOREIGN_EXCHANGES.get(new FromTo(from, to));
        if (answer != null) {
            return answer;
        }

        Currency intermediate = INTERMEDIATE_FOREIGN_EXCHANGES.get(from);
        if (intermediate == null) {
            intermediate = INTERMEDIATE_FOREIGN_EXCHANGES.get(to);
        }

        if (intermediate == null) {

            throw new IllegalArgumentException("No FX from " + from + " to " + to);
        }

        return new CompoundForeignExchange(
                DIRECT_FOREIGN_EXCHANGES.get(new FromTo(from, intermediate)),
                getForeignExchange(intermediate, to)
        );
    }

    public static ForeignExchange getIdentityForeignExchange(Currency currency) {
        return new SimpleForeignExchange(() -> IndexSeriesSupport.CONSTANT_SERIES, currency, currency);
    }

    public static Investment exchange(Investment investment, Currency targetCurrency) {

        Investment answer = new Investment();

        answer.setComment(investment.getComment());

        if (investment.getType() == InvestmentType.USD) {
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

    private static InvestmentEvent exchangeInto(InvestmentEvent in, Currency currency) {
        if (in == null) {
            return null;
        }
        ForeignExchange fx = ForeignExchanges.getForeignExchange(in.getCurrency(), currency);

        final var fee = new MoneyAmount(in.getFee(), in.getCurrency());

        InvestmentEvent answer = new InvestmentEvent();
        MoneyAmount ma = fx(in.getFx(), fx, in.getMoneyAmount(), currency, in.getDate());
        answer.setAmount(ma.amount());
        answer.setCurrency(ma.currency());
        answer.setDate(in.getDate());
        answer.setFee(fx(in.getFx(), fx, fee, currency, in.getDate()).amount());
        answer.setTransferFee(
                Optional.ofNullable(in.getTransferFee())
                        .map(trfee -> fx(
                        in.getFx(),
                        fx,
                        new MoneyAmount(trfee, in.getCurrency()), currency, in.getDate()).amount())
                        .orElse(null));
        answer.setFx(in.getFx());
        return answer;
    }

    private static MoneyAmount fx(BigDecimal optionalFxRate, ForeignExchange fx, MoneyAmount ma, Currency currency, LocalDate date) {
        if (USD != currency || optionalFxRate == null) {

            return fx.exchange(ma, currency, date);
        }
        return new MoneyAmount(ma.amount().multiply(optionalFxRate, MathConstants.C), USD);
    }

    private record FromTo(Currency from, Currency to) {

    }
}
