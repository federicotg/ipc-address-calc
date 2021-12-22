/*
 * Copyright (C) 2021 federicogentile
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

/**
 *
 * @author federicogentile
 */
public class Position {

    private final String symbol;
    private final String fundName;
    private final BigDecimal position;
    private final MoneyAmount last;
    private final MoneyAmount costBasis;
    private final MoneyAmount marketValue;
    private final MoneyAmount averagePrice;
    private final MoneyAmount unrealizedPnL;

    public Position(String symbol, String fundName, BigDecimal position, MoneyAmount last, MoneyAmount costBasis, MoneyAmount marketValue, MoneyAmount averagePrice) {
        this.symbol = symbol;
        this.fundName = fundName;
        this.position = position;
        this.last = last;
        this.costBasis = costBasis;
        this.marketValue = marketValue;
        this.averagePrice = averagePrice;
        this.unrealizedPnL = new MoneyAmount(marketValue.getAmount().subtract(costBasis.getAmount(), MathConstants.CONTEXT), marketValue.getCurrency());
    }

    public String getSymbol() {
        return symbol;
    }

    public String getFundName() {
        return fundName;
    }

    public BigDecimal getPosition() {
        return position;
    }

    public MoneyAmount getLast() {
        return last;
    }

    public MoneyAmount getCostBasis() {
        return costBasis;
    }

    public MoneyAmount getMarketValue() {
        return marketValue;
    }

    public MoneyAmount getAveragePrice() {
        return averagePrice;
    }

    public MoneyAmount getUnrealizedPnL() {
        return unrealizedPnL;
    }

}
