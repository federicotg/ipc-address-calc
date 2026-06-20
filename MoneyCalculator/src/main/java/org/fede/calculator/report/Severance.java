package org.fede.calculator.report;

import java.math.BigDecimal;
import org.fede.calculator.money.Currency;
import org.fede.calculator.money.MathConstants;
import org.fede.calculator.money.MoneyAmount;


/**
 *
 * @author fede
 */
public record Severance (

     BigDecimal salary,
     BigDecimal taxedAmount,
     BigDecimal untaxedAmount){

    public MoneyAmount getTotal() {
        return new MoneyAmount(taxedAmount.add(untaxedAmount, MathConstants.C), Currency.USD);
    }
}
