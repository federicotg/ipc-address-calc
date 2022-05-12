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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.InvestmentAsset;
import org.fede.calculator.money.series.YearMonth;

/**
 *
 * @author fede
 */
public class BenchmarkInvestmentMapper implements Function<Investment, Investment> {

    private static final DateTimeFormatter DMY = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private static String dmy(Investment i) {
        return DMY.format(LocalDate.ofInstant(i.getInitialDate().toInstant(), ZoneId.systemDefault()));
    }

    private static BigDecimal price(Investment i) {

        final var fx = Optional.ofNullable(i.getIn().getFx())
                .orElse(BigDecimal.ONE);

        return i.getIn().getAmount().multiply(fx, MathConstants.C)
                .divide(i.getInvestment().getAmount(), MathConstants.C);
    }

    private final String benchmark;
    private final Map<String, BigDecimal> seenUSDPrices;

    public BenchmarkInvestmentMapper(List<Investment> investments) {
        this("CSPX", investments);
    }

    private BenchmarkInvestmentMapper(String benchmark, List<Investment> investments) {
        this.benchmark = benchmark;
        this.seenUSDPrices = investments.stream()
                .filter(i -> i.getCurrency().equals(benchmark))
                .collect(Collectors.toMap(BenchmarkInvestmentMapper::dmy, BenchmarkInvestmentMapper::price, (x, y) -> x));

        // CSPX
        this.seenUSDPrices.put("02-09-2019", new BigDecimal("288.22"));
        this.seenUSDPrices.put("29-10-2019", new BigDecimal("301.77"));
        this.seenUSDPrices.put("19-05-2020", new BigDecimal("296.00"));
        this.seenUSDPrices.put("17-03-2021", new BigDecimal("399.16"));
        this.seenUSDPrices.put("26-03-2021", new BigDecimal("398.88"));
        this.seenUSDPrices.put("05-08-2021", new BigDecimal("449.31"));
        this.seenUSDPrices.put("25-08-2021", new BigDecimal("457.24"));
        this.seenUSDPrices.put("28-09-2021", new BigDecimal("444.13"));
        this.seenUSDPrices.put("12-04-2022", new BigDecimal("456.71"));
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
            final var fxFactor = Optional.ofNullable(t.getIn().getFx()).orElse(BigDecimal.ONE);
            var usdInvested = t.getIn().getAmount().multiply(fxFactor, MathConstants.C);
            asset.setAmount(usdInvested.divide(price, MathConstants.C));
        } else {
            var fx = ForeignExchanges.getMoneyAmountForeignExchange(t.getInitialCurrency(), this.benchmark);
            asset.setAmount(fx.apply(t.getInitialMoneyAmount(), YearMonth.of(t.getInitialDate())).getAmount());
        }
        var answer = new Investment();
        answer.setIn(t.getIn());
        answer.setInvestment(asset);
        answer.setOut(t.getOut());
        return answer;
    }

}
