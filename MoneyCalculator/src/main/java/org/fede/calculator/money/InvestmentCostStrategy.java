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
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.fede.calculator.money.series.Investment;
import static org.fede.calculator.money.MathConstants.CONTEXT;

/**
 *
 * @author fede
 */
public class InvestmentCostStrategy {

    private final BigDecimal cclFeeRate;
    private final BigDecimal buyFeeTaxRate;
    private final BigDecimal sellFeeRate;
    private final BigDecimal sellFeeTaxRate;
    private final BigDecimal capitalGainsTaxRate;
    private final String currency;

    private static final Function<BigDecimal, BigDecimal> PPI_GLOBAL_USD_FEE_STRATEGY = new PPIGlobalUSDFeeStrategy();
    private static final Function<BigDecimal, BigDecimal> PPIGLOBAL_EUR_FEE_STRATEGY = new PPIGlobalEURFeeStrategy();

    private static final Map<String, Function<BigDecimal, BigDecimal>> IBKR = Map.of(
            "lse", new InteractiveBrokersTieredLondonUSDFeeStrategy(),
            "xetra", new InteractiveBrokersTieredXETRAFeeStrategy(),
            "euronext", new InteractiveBrokersTieredEuronextEURFeeStrategy()
    );

    public InvestmentCostStrategy(String currency, BigDecimal brokerFeeRate, BigDecimal ivaRate, BigDecimal capitalGainsTaxRate) {
        this.currency = currency;
        this.cclFeeRate = brokerFeeRate;
        this.buyFeeTaxRate = ivaRate;
        this.sellFeeRate = brokerFeeRate;
        this.sellFeeTaxRate = ivaRate;
        this.capitalGainsTaxRate = capitalGainsTaxRate;
    }

    private Function<BigDecimal, BigDecimal> getFeeStrategy(Investment investment) {

        if (investment.getComment() == null) {

            if ("MEUD".equals(investment.getCurrency())) {
                return PPIGLOBAL_EUR_FEE_STRATEGY;
            }

            return PPI_GLOBAL_USD_FEE_STRATEGY;
        }

        return IBKR.getOrDefault(investment.getComment(), PPI_GLOBAL_USD_FEE_STRATEGY);

    }

    public InvestmentDetails details(Investment investment) {

        final var inv = ForeignExchanges.exchange(investment, this.currency);

        final var investedAmount = inv.getInitialMoneyAmount().getAmount();
        final var buyFee = inv.getIn().getFee();

        final var buyFeeTax = investment.getComment() != null
                ? BigDecimal.ZERO
                : buyFee.multiply(this.buyFeeTaxRate, CONTEXT);

        //final var buyFxFee = BigDecimal.ZERO;
        //final var buyFxFeeTax = BigDecimal.ZERO;
        // pre investment charges
        final var afterCclAmount = investedAmount
                .add(buyFee, CONTEXT)
                .add(buyFeeTax, CONTEXT);
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

        final var sellFee = this.getFeeStrategy(investment).apply(presentValue);
        //presentValue.multiply(this.sellFeeRate, CONTEXT).max(BigDecimal.ZERO);

        //final var sellFeeTax = sellFee.multiply(sellFeeTaxRate, CONTEXT).max(BigDecimal.ZERO);
        final var beforeCclAmount = presentValue
                .subtract(sellFee, CONTEXT);

        final var capitalGains = beforeCclAmount
                .subtract(afterCclAmount, CONTEXT) // o investedAmount
                .multiply(this.capitalGainsTaxRate, CONTEXT)
                .max(BigDecimal.ZERO);

        final var zero = new MoneyAmount(BigDecimal.ZERO, this.currency);

        // ccl
        //final var firstSellCclFee = beforeCclAmount.multiply(this.cclFeeRate, CONTEXT).max(BigDecimal.ZERO);
        //final var secondSellCclFee = beforeCclAmount
        //        .subtract(firstSellCclFee, CONTEXT)
        //        .multiply(this.cclFeeRate, CONTEXT).max(BigDecimal.ZERO);
        final var d = new InvestmentDetails();
        d.setBuyFee(new MoneyAmount(buyFee, this.currency));
        d.setBuyFeeTax(new MoneyAmount(buyFeeTax, this.currency));
        d.setBuyFxFee(zero);
        d.setBuyFxFeeTax(zero);
        d.setCapitalGainsTax(new MoneyAmount(capitalGains, this.currency));

        d.setBuyCclFee(Optional.ofNullable(inv.getIn().getTransferFee())
                .map(transferFee -> new MoneyAmount(transferFee, inv.getIn().getCurrency()))
                .orElseGet(() -> new MoneyAmount(firstCclFee.add(secondCclFee, CONTEXT), this.currency)));

        d.setInvestmentDate(LocalDate.ofInstant(inv.getInitialDate().toInstant(), ZoneId.systemDefault()));
        d.setInvestedAmount(new MoneyAmount(investedAmount, this.currency));

        //d.setSellCclFee(new MoneyAmount(firstSellCclFee.add(secondSellCclFee, CONTEXT), this.currency));
        d.setSellCclFee(zero);

        d.setSellFee(new MoneyAmount(sellFee, this.currency));

        d.setSellFeeTax(zero);
        d.setSellFxFee(zero);
        d.setSellFxFeeTax(zero);

        d.setInvestmentCurrency(inv.getInvestment().getCurrency());
        d.setCurrentAmount(new MoneyAmount(presentValue, this.currency));
        d.setInvestmentQuantity(investment.getInvestment().getAmount());
        return d;
    }
}
