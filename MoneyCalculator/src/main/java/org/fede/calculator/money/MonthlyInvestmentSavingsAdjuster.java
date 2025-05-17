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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import static org.fede.calculator.money.Currency.USD;
import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.InvestmentType;
import org.fede.calculator.money.series.YearMonth;

/**
 *
 * @author fede
 */
public class MonthlyInvestmentSavingsAdjuster {

    private static final MoneyAmount ZERO = MoneyAmount.zero(Currency.USD);

    private final Map<YearMonth, MoneyAmount> differences;

    public MonthlyInvestmentSavingsAdjuster(Series series) {

        final Map<YearMonth, List<Investment>> groupedByYearMonth = series.getInvestments()
                .stream()
                .filter(i -> i.isETF() || i.getType() == InvestmentType.FCI)
                .collect(Collectors.groupingBy(i -> YearMonth.of(i.getInitialDate())));

        this.differences = groupedByYearMonth.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> this.differences(e.getKey(), e.getValue())));
    }

    public MoneyAmount difference(YearMonth ym) {
        return this.differences.getOrDefault(ym, ZERO);
    }

    private MoneyAmount differences(YearMonth ym, List<Investment> investments) {

        return new MoneyAmount(investments
                .stream()
                .filter(i -> i.getOut() == null || YearMonth.of(i.getOut().getDate()).compareTo(ym) > 0)
                .map(i -> this.eomPriceUSD(i, ym).subtract(this.buyPriceUSD(i, ym), MathConstants.C))
                .reduce(BigDecimal.ZERO, BigDecimal::add), USD);
    }

    private BigDecimal buyPriceUSD(Investment i, YearMonth ym) {
        return ForeignExchanges.getForeignExchange(i.getIn().getCurrency(), USD)
                .exchange(i.getInitialMoneyAmount(), USD, ym)
                .getAmount();
    }

    private BigDecimal eomPriceUSD(Investment i, YearMonth ym) {
        return ForeignExchanges.getForeignExchange(i.getCurrency(), USD)
                .exchange(i.getInvestment().getMoneyAmount(), USD, ym)
                .getAmount();
    }
}
