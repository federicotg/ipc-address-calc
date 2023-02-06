/*
 * Copyright (C) 2021 fede
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

import java.time.LocalDate;
import java.time.ZoneId;
import org.fede.calculator.money.series.Investment;

/**
 *
 * @author fede
 */
public class InvestmentCostStrategy {

    private static final ZoneId SYSTEM_DEFAULT_ZONE_ID = ZoneId.systemDefault();
    
    private final String currency;

    public InvestmentCostStrategy(String currency) {
        this.currency = currency;
    }

    public InvestmentDetails details(Investment investment) {

        final var inv = ForeignExchanges.exchange(investment, this.currency);

        final var investedAmount = inv.getInitialMoneyAmount().getAmount();

        //post sell fee
        final var presentValue = ForeignExchanges.getMoneyAmountForeignExchange(inv.getMoneyAmount().getCurrency(), this.currency)
                .apply(inv.getMoneyAmount(), Inflation.USD_INFLATION.getTo())
                .getAmount();

        // ccl
        final var d = new InvestmentDetails();

        d.setCostBasis(inv.getCost());

        d.setInvestmentDate(LocalDate.ofInstant(inv.getInitialDate().toInstant(), SYSTEM_DEFAULT_ZONE_ID));
        d.setInvestedAmount(new MoneyAmount(investedAmount, this.currency));
        d.setInvestmentCurrency(inv.getInvestment().getCurrency());
        d.setCurrentAmount(new MoneyAmount(presentValue, this.currency));
        d.setInvestmentQuantity(investment.getInvestment().getAmount());
        return d;
    }
}
