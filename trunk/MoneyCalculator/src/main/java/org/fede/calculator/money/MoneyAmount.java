/*
 * Copyright (C) 2014 fede
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
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 *
 * @author fede
 */
public class MoneyAmount {

    private Currency currency;
    private BigDecimal amount;

    public MoneyAmount(BigDecimal amount, Currency currency) {
        this.amount = amount;
        this.currency = currency;
    }

    public MoneyAmount(BigDecimal amount, String currencySymbol) {
        this(amount, Currency.getInstance(currencySymbol));
    }

    public MoneyAmount adjust(BigDecimal divisor, BigDecimal factor) {
        return new MoneyAmount(
                this.amount.setScale(20).divide(divisor, RoundingMode.HALF_UP)
                .multiply(factor), this.currency);
    }

    public MoneyAmount exchange(Currency newCurrency, BigDecimal exchangeRate) {
        return new MoneyAmount(this.amount.multiply(exchangeRate), newCurrency);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.currency);
        hash = 37 * hash + Objects.hashCode(this.amount);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof MoneyAmount
                && ((MoneyAmount) obj).currency.equals(this.currency)
                && ((MoneyAmount) obj).amount.setScale(2, RoundingMode.HALF_UP)
                .compareTo(this.amount.setScale(2, RoundingMode.HALF_UP)) == 0;
    }

    @Override
    public String toString() {
        return this.currency.toString() + " " + this.amount.toString();
    }

    public void assertCurrency(Currency currency) {
        if (!this.currency.equals(currency)) {
            throw new IllegalArgumentException("Unexpected currency.");
        }
    }
    
    public MoneyAmount movePoint(int n){
        return new MoneyAmount(this.amount.movePointRight(n), this.currency);
    }
    
    public MoneyAmount movePointLeft(int n){
        return new MoneyAmount(this.amount.movePointLeft(n), this.currency);
    }

    public BigDecimal getAmount() {
        return amount;
    }

}
