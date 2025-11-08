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
import java.util.EnumMap;
import java.util.Map;

/**
 *
 * @author fede
 */
public record MoneyAmount(BigDecimal amount, Currency currency) {

    private static final Map<Currency, MoneyAmount> ZERO_AMOUNTS;

    static {
        ZERO_AMOUNTS = new EnumMap<>(Currency.class);
        for (Currency c : Currency.values()) {
            ZERO_AMOUNTS.put(c, new MoneyAmount(BigDecimal.ZERO, c));
        }
    }

    public static MoneyAmount zero(Currency currency) {
        return ZERO_AMOUNTS.get(currency);
    }

    public MoneyAmount adjust(BigDecimal divisor, BigDecimal factor) {
        if (this.isZero() || divisor.compareTo(factor) == 0) {
            return this;
        }
        return new MoneyAmount(this.amount
                .multiply(factor, MathConstants.C)
                .divide(divisor, MathConstants.C), this.currency);
    }

    public MoneyAmount exchange(Currency newCurrency, BigDecimal exchangeRate) {
        if (this.isZero()) {
            return MoneyAmount.zero(newCurrency);
        }
        return new MoneyAmount(this.amount.multiply(exchangeRate, MathConstants.C), newCurrency);
    }

    public void assertCurrency(Currency currency) {
        if (this.currency != currency) {
            throw new IllegalArgumentException("Unexpected currency.  " + this.currency + " is not " + currency);
        }
    }

    public MoneyAmount add(MoneyAmount other) {
        if (other.currency() != this.currency()) {
            throw new IllegalArgumentException("Money amounts must be in the same currency.");
        }
        return new MoneyAmount(this.amount().add(other.amount(), MathConstants.C), this.currency());
    }

    public MoneyAmount subtract(MoneyAmount other) {
        if (other.currency() != this.currency()) {
            throw new IllegalArgumentException("Money amounts must be in the same currency.");
        }
        return new MoneyAmount(this.amount().subtract(other.amount(), MathConstants.C), this.currency());
    }

    public boolean isZero() {
        return this.amount.signum() == 0;
    }

    public MoneyAmount max(MoneyAmount other) {
        //this.assertCurrency(other.currency());

        if (this.amount().compareTo(other.amount()) >= 0) {
            return this;
        }
        return other;
    }

    public MoneyAmount min(MoneyAmount other) {
        //this.assertCurrency(other.currency());

        if (this.amount().compareTo(other.amount()) >= 0) {
            return other;
        }
        return this;
    }
}
