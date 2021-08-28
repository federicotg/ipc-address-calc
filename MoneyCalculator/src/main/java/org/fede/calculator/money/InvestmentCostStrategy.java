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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import org.fede.calculator.money.series.Investment;
import static org.fede.calculator.money.MathConstants.CONTEXT;

/**
 *
 * @author fede
 */
public class InvestmentCostStrategy {

    private final BigDecimal cclFeeRate;
    private final BigDecimal buyFeeTaxRate;
    private final BigDecimal buyFxFeeRate;
    private final BigDecimal buyFxFeeTaxRate;
    private final BigDecimal sellFeeRate;
    private final BigDecimal sellFeeTaxRate;
    private final BigDecimal sellFxFeeRate;
    private final BigDecimal sellFxFeeTaxRate;
    private final BigDecimal capitalGainsTaxRate;
    private final String currency;

    public InvestmentCostStrategy(String currency, BigDecimal brokerFeeRate, BigDecimal fxFeeRate, BigDecimal ivaRate, BigDecimal capitalGainsTaxRate) {
        this.currency = currency;
        this.cclFeeRate = brokerFeeRate;
        this.buyFeeTaxRate = ivaRate;
        this.buyFxFeeRate = fxFeeRate;
        this.buyFxFeeTaxRate = ivaRate;
        this.sellFeeRate = brokerFeeRate;
        this.sellFeeTaxRate = ivaRate;
        this.sellFxFeeRate = fxFeeRate;
        this.sellFxFeeTaxRate = ivaRate;
        this.capitalGainsTaxRate = capitalGainsTaxRate;
    }

    public InvestmentDetails details(Investment investment) {
        final var inv = ForeignExchanges.exchange(investment, this.currency);

        final var investedAmount = inv.getInitialMoneyAmount().getAmount();
        final var buyFee = inv.getIn().getFee();

        final var buyFeeTax = buyFee.multiply(this.buyFeeTaxRate, CONTEXT);

        final var buyFxFee = BigDecimal.ZERO;
        final var buyFxFeeTax = buyFxFee.multiply(this.buyFxFeeTaxRate, CONTEXT);

        // pre investment charges
        final var afterCclAmount = investedAmount
                .add(buyFee, CONTEXT)
                .add(buyFeeTax, CONTEXT)
                .add(buyFxFee, CONTEXT)
                .add(buyFxFeeTax, CONTEXT);
        //1st ccl
        final var firstCclFee = afterCclAmount
                .divide(BigDecimal.ONE.subtract(this.cclFeeRate, CONTEXT), CONTEXT)
                .subtract(afterCclAmount, CONTEXT);

        final var secondCclFee = afterCclAmount
                .add(firstCclFee, CONTEXT)
                .divide(BigDecimal.ONE.subtract(this.cclFeeRate, CONTEXT), CONTEXT)
                .subtract(afterCclAmount.add(firstCclFee, CONTEXT), CONTEXT);

        //post sell fee
        final var limit = Inflation.USD_INFLATION.getTo();

        final var presentValue = ForeignExchanges.getForeignExchange(inv.getMoneyAmount().getCurrency(), this.currency)
                .exchange(inv.getMoneyAmount(), this.currency, limit.getYear(), limit.getMonth())
                .getAmount();

        final var sellFee = presentValue.multiply(this.sellFeeRate, CONTEXT).max(BigDecimal.ZERO);

        final var sellFeeTax = sellFee.multiply(sellFeeTaxRate, CONTEXT).max(BigDecimal.ZERO);

        final var sellFxFee = "MEUD".equals(inv.getCurrency())
                ? investedAmount.multiply(sellFxFeeRate, CONTEXT).max(BigDecimal.ZERO)
                : BigDecimal.ZERO;

        final var sellFxFeeTax = sellFxFee
                .multiply(this.sellFxFeeTaxRate, CONTEXT)
                .max(BigDecimal.ZERO);

        final var beforeCclAmount = presentValue
                .subtract(sellFee, CONTEXT)
                .subtract(sellFeeTax, CONTEXT)
                .subtract(sellFxFee, CONTEXT)
                .subtract(sellFxFeeTax, CONTEXT);

        final var capitalGains = beforeCclAmount
                .subtract(afterCclAmount, CONTEXT) // o investedAmount
                .multiply(this.capitalGainsTaxRate, CONTEXT)
                .max(BigDecimal.ZERO);

        // ccl
        //final var firstSellCclFee = beforeCclAmount.multiply(this.cclFeeRate, CONTEXT).max(BigDecimal.ZERO);
        //final var secondSellCclFee = beforeCclAmount
        //        .subtract(firstSellCclFee, CONTEXT)
        //        .multiply(this.cclFeeRate, CONTEXT).max(BigDecimal.ZERO);
        final var d = new InvestmentDetails();
        d.setBuyFee(new MoneyAmount(buyFee, this.currency));
        d.setBuyFeeTax(new MoneyAmount(buyFeeTax, this.currency));
        d.setBuyFxFee(new MoneyAmount(buyFxFee, this.currency));
        d.setBuyFxFeeTax(new MoneyAmount(buyFxFeeTax, this.currency));
        d.setCapitalGainsTax(new MoneyAmount(capitalGains, this.currency));

        d.setBuyCclFee(Optional.ofNullable(inv.getIn().getTransferFee())
                .map(transferFee -> new MoneyAmount(transferFee, inv.getIn().getCurrency()))
                .orElseGet(() -> new MoneyAmount(firstCclFee.add(secondCclFee, CONTEXT), this.currency)));

        d.setInvestmentDate(LocalDate.ofInstant(inv.getInitialDate().toInstant(), ZoneId.systemDefault()));
        d.setInvestedAmount(new MoneyAmount(investedAmount, this.currency));

        //d.setSellCclFee(new MoneyAmount(firstSellCclFee.add(secondSellCclFee, CONTEXT), this.currency));
        d.setSellCclFee(new MoneyAmount(BigDecimal.ZERO, this.currency));
        
        d.setSellFee(new MoneyAmount(sellFee, this.currency));
        d.setSellFeeTax(new MoneyAmount(sellFeeTax, this.currency));
        d.setSellFxFee(new MoneyAmount(sellFxFee, this.currency));
        d.setSellFxFeeTax(new MoneyAmount(sellFxFeeTax, this.currency));
        d.setInvestmentCurrency(inv.getInvestment().getCurrency());
        d.setCurrentAmount(new MoneyAmount(presentValue, this.currency));
        d.setInvestmentQuantity(investment.getInvestment().getAmount());
        return d;
    }
}
