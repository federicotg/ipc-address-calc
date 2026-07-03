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
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author fede
 */
public record MoneyAmount(BigDecimal amount, Currency currency) {

    private static final Map<Currency, MoneyAmount> ZERO_AMOUNTS = HashMap.newHashMap(Currency.values().length);

    public static MoneyAmount zero(Currency currency) {
        return ZERO_AMOUNTS.computeIfAbsent(currency, c -> new MoneyAmount(BigDecimal.ZERO, c));
    }

    public MoneyAmount adjust(BigDecimal divisor, BigDecimal factor) {
        if (this.isZero() || divisor.compareTo(factor) == 0) {
            return this;
        }

        // optimize multiplication by 0
        if (factor.signum() == 0) {
            return MoneyAmount.zero(this.currency);
        }

        // optimize divide and multiply by 1
        if (divisor.compareTo(BigDecimal.ONE) == 0) {
            return new MoneyAmount(
                    this.amount.multiply(factor, MathConstants.C),
                    this.currency);
        }

        if (factor.compareTo(BigDecimal.ONE) == 0) {
            return new MoneyAmount(
                    this.amount.divide(divisor, MathConstants.C),
                    this.currency);
        }

        return new MoneyAmount(this.amount
                .multiply(factor, MathConstants.C)
                .divide(divisor, MathConstants.C), this.currency);
    }

    public MoneyAmount exchange(Currency newCurrency, BigDecimal exchangeRate) {
        if (exchangeRate == null || this.isZero()) {
            return MoneyAmount.zero(newCurrency);
        }

        if (exchangeRate.compareTo(BigDecimal.ONE) == 0) {
            return new MoneyAmount(this.amount, newCurrency);
        }

        return new MoneyAmount(this.amount.multiply(exchangeRate, MathConstants.C), newCurrency);
    }

    public MoneyAmount add(MoneyAmount other) {
        if (other.currency() != this.currency()) {
            throw new IllegalArgumentException("Money amounts must be in the same currency.");
        }
        if (other.isZero()) {
            return this;
        }
        if (this.isZero()) {
            return other;
        }
        return new MoneyAmount(this.amount().add(other.amount(), MathConstants.C), this.currency());
    }

    public MoneyAmount subtract(MoneyAmount other) {
        if (other.currency() != this.currency()) {
            throw new IllegalArgumentException("Money amounts must be in the same currency.");
        }
        if (other.isZero()) {
            return this;
        }
        return new MoneyAmount(this.amount().subtract(other.amount(), MathConstants.C), this.currency());
    }

    public boolean isZero() {
        return this.amount.signum() == 0;
    }

    public MoneyAmount max(MoneyAmount other) {

        if (other.currency() != this.currency()) {
            throw new IllegalArgumentException("Money amounts must be in the same currency.");
        }

        if (this.amount().compareTo(other.amount()) >= 0) {
            return this;
        }
        return other;
    }

    public MoneyAmount min(MoneyAmount other) {
        if (other.currency() != this.currency()) {
            throw new IllegalArgumentException("Money amounts must be in the same currency.");
        }

        if (this.amount().compareTo(other.amount()) >= 0) {
            return other;
        }
        return this;
    }

    @Override
    public int hashCode() {
        return 31 * currency.hashCode()
                + amount.stripTrailingZeros().hashCode();
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        return obj instanceof MoneyAmount other
                && currency == other.currency
                && amount.compareTo(other.amount) == 0;
    }
}
