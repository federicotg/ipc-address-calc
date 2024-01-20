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
package org.fede.calculator.money;

import com.fasterxml.jackson.core.type.TypeReference;
import java.math.BigDecimal;
import static java.math.BigDecimal.ONE;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import static java.util.stream.Collectors.toMap;
import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.InvestmentAsset;
import org.fede.calculator.money.series.SeenPrice;
import org.fede.calculator.money.series.SeriesReader;
import org.fede.calculator.money.series.YearMonth;
import static org.fede.calculator.money.MathConstants.C;

/**
 *
 * @author fede
 */
public class BenchmarkInvestmentMapper implements Function<Investment, Investment> {

    private static final ZoneId SYSTEM_DEFAULT_ZONE_ID = ZoneId.systemDefault();
    
    private static final TypeReference<Map<String, List<SeenPrice>>> TR = new TypeReference<Map<String, List<SeenPrice>>>() {
    };

    private static final DateTimeFormatter DMY = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private static String dmy(Investment i) {
        return DMY.format(LocalDate.ofInstant(i.getInitialDate().toInstant(), SYSTEM_DEFAULT_ZONE_ID));
    }

    private static BigDecimal price(Investment i) {

        final var fx = Optional.ofNullable(i.getIn().getFx())
                .orElse(BigDecimal.ONE);

        return i.getIn().getAmount().multiply(fx, MathConstants.C)
                .divide(i.getInvestment().getAmount(), MathConstants.C);
    }

    private final String benchmark;
    private final Map<String, BigDecimal> seenUSDPrices;

    public BenchmarkInvestmentMapper(String benchmark, List<Investment> investments) {
        this.benchmark = benchmark;
        this.seenUSDPrices = investments.stream()
                .filter(i -> i.getCurrency().equals(benchmark))
                .collect(toMap(BenchmarkInvestmentMapper::dmy, BenchmarkInvestmentMapper::price, (x, y) -> x));

        this.seenUSDPrices.putAll(
                SeriesReader.read("index/seen-prices.json", TR).getOrDefault(benchmark, Collections.emptyList())
                        .stream()
                        .collect(toMap(SeenPrice::dmy, SeenPrice::price)));

    }

    @Override
    public Investment apply(Investment t) {

        if (t.getCurrency().equals(this.benchmark)) {
            return t;
        }

        var asset = new InvestmentAsset();
        asset.setCurrency(this.benchmark);

        var price = this.seenUSDPrices.get(dmy(t));

        if (price != null) {
            final var fxFactor = Optional.ofNullable(t.getIn().getFx()).orElse(ONE);
            var usdInvested = t.getIn().getAmount().multiply(fxFactor, C);
            asset.setAmount(usdInvested.divide(price, C));
        } else if (this.benchmark.equals("USD")) {
            final var fxFactor = Optional.ofNullable(t.getIn().getFx()).orElse(ONE);
            var usdInvested = t.getIn().getAmount().multiply(fxFactor, C);
            asset.setAmount(usdInvested);
        } else {
            var fx = ForeignExchanges.getMoneyAmountForeignExchange(t.getInitialCurrency(), this.benchmark);
            asset.setAmount(fx.apply(t.getInitialMoneyAmount(), YearMonth.of(t.getInitialDate())).getAmount());
        }
        var answer = new Investment();
        answer.setIn(t.getIn());
        answer.setInvestment(asset);
        answer.setOut(t.getOut());
        answer.setType(t.getType());
        answer.setComment(t.getComment());
        answer.setId(t.getId());
        answer.setInterest(t.getInterest());
        return answer;
    }

}
