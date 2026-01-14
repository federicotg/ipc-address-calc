/*
 * Copyright (C) 2022 fede
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
package org.fede.calculator.report;

import java.math.BigDecimal;
import static java.math.BigDecimal.ONE;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.fede.calculator.money.Currency;
import org.fede.calculator.money.ForeignExchanges;
import org.fede.calculator.money.MathConstants;
import static java.util.stream.Collectors.toMap;
import static org.fede.calculator.money.Currency.USD;
import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.InvestmentAsset;
import org.fede.calculator.money.series.SeriesReader;
import java.time.YearMonth;
import static org.fede.calculator.money.MathConstants.C;
import org.fede.calculator.money.series.InvestmentEvent;
import tools.jackson.core.type.TypeReference;

/**
 *
 * @author fede
 */
public class BenchmarkInvestmentMapper implements Function<Investment, Investment> {

    private final TypeReference<Map<Currency, List<SeenPrice>>> tr = new TypeReference<Map<Currency, List<SeenPrice>>>() {
    };

    private final DateTimeFormatter dmy = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private String dmy(Investment i) {
        return dmy(i.getInitialDate());
    }

    private String sellDmy(Investment i) {
        return dmy(i.getOut().getDate());
    }

    private String dmy(LocalDate d) {
        return dmy.format(d);
    }

    private static BigDecimal price(Investment i) {

        final var fx = Optional.ofNullable(i.getIn().getFx())
                .orElse(BigDecimal.ONE);

        return i.getIn().getAmount().multiply(fx, MathConstants.C)
                .divide(i.getInvestment().getAmount(), MathConstants.C);
    }

    private final Currency benchmark;
    private final Map<String, BigDecimal> seenUSDPrices;

    public BenchmarkInvestmentMapper(Currency benchmark, List<Investment> investments) {
        this.benchmark = benchmark;
        this.seenUSDPrices = investments.stream()
                .filter(i -> i.getCurrency() == benchmark)
                .collect(toMap(this::dmy, BenchmarkInvestmentMapper::price, (x, y) -> x));

        this.seenUSDPrices.putAll(
                SeriesReader.read("index/seen-prices.json", tr).getOrDefault(benchmark, Collections.emptyList())
                        .stream()
                        .collect(toMap(SeenPrice::dmy, SeenPrice::price)));
    }

    @Override
    public Investment apply(Investment t) {

        if (t.getCurrency() == this.benchmark) {
            return t;
        }

        var asset = new InvestmentAsset();
        asset.setCurrency(this.benchmark);

        var price = this.seenUSDPrices.get(dmy(t));

        if (price != null) {
            final var fxFactor = Optional.ofNullable(t.getIn().getFx()).orElse(ONE);
            var usdInvested = t.getIn().getAmount().multiply(fxFactor, C);
            asset.setAmount(usdInvested.divide(price, C));
        } else if (this.benchmark == USD) {
            final var fxFactor = Optional.ofNullable(t.getIn().getFx()).orElse(ONE);
            var usdInvested = t.getIn().getAmount().multiply(fxFactor, C);
            asset.setAmount(usdInvested);
        } else {
            var fx = ForeignExchanges.getMoneyAmountForeignExchange(t.getInitialCurrency(), this.benchmark);
            asset.setAmount(fx.apply(t.getInitialMoneyAmount(), YearMonth.from(t.getInitialDate())).amount());
        }
        var answer = new Investment();
        answer.setIn(t.getIn());
        answer.setInvestment(asset);

        if (t.getOut() != null) {
            if (this.benchmark == USD) {
                var out = new InvestmentEvent();
                out.setCurrency(USD);
                out.setAmount(asset.getAmount());
                out.setDate(t.getOut().getDate());
                out.setTransferFee(t.getOut().getTransferFee());
                out.setFee(t.getOut().getFee());
                out.setFx(t.getOut().getFx());
                answer.setOut(out);
            } else {
                var out = new InvestmentEvent();
                out.setCurrency(USD);
                var sellPrice = this.seenUSDPrices.get(sellDmy(t));
                if (sellPrice == null) {
                    var fx = ForeignExchanges.getMoneyAmountForeignExchange(asset.getCurrency(), USD);
                    out.setAmount(fx.apply(asset.getMoneyAmount(), YearMonth.from(t.getOut().getDate())).amount());
                } else {
                    out.setAmount(asset.getAmount().multiply(sellPrice, C));
                }
                out.setDate(t.getOut().getDate());
                out.setTransferFee(t.getOut().getTransferFee());
                out.setFee(t.getOut().getFee());
                out.setFx(t.getOut().getFx());
                answer.setOut(out);
            }
        }
        answer.setType(t.getType());
        answer.setComment(t.getComment());
        answer.setInterest(t.getInterest());
        return answer;
    }

}
