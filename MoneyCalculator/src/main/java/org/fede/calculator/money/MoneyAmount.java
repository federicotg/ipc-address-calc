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
import static java.math.RoundingMode.HALF_UP;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author fede
 */
public record MoneyAmount(BigDecimal amount, Currency currency) {

    private static final Map<Currency, MoneyAmount> ZERO_AMOUNTS = Arrays.stream(Currency.values())
            .map(c -> new MoneyAmount(BigDecimal.ZERO, c))
            .collect(Collectors.toMap(
                    MoneyAmount::currency,
                    Function.identity(),
                    (a, b) -> a,
                    () -> new EnumMap<>(Currency.class)
            ));

    public static MoneyAmount zero(String currency) {
        return zero(Currency.valueOf(currency));
    }

    public static MoneyAmount zero(Currency currency) {
        return ZERO_AMOUNTS.get(currency);
    }

    public MoneyAmount adjust(BigDecimal divisor, BigDecimal factor) {
        if (this.isZero() || divisor.compareTo(factor) == 0) {
            return this;
        }
        return new MoneyAmount(this.amount.setScale(MathConstants.SCALE, MathConstants.RM).divide(divisor, MathConstants.C)
                .multiply(factor, MathConstants.C), this.currency);
    }

    public MoneyAmount exchange(String newCurrency, BigDecimal exchangeRate) {
        return this.exchange(Currency.valueOf(newCurrency), exchangeRate);
    }

    public MoneyAmount exchange(Currency newCurrency, BigDecimal exchangeRate) {
        if (this.isZero()) {
            return MoneyAmount.zero(newCurrency);
        }
        return new MoneyAmount(this.amount.multiply(exchangeRate, MathConstants.C), newCurrency);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof MoneyAmount other)
                && other.currency.equals(this.currency)
                && other.amount.setScale(5, HALF_UP)
                        .compareTo(this.amount.setScale(5, HALF_UP)) == 0;
    }

    public void assertCurrency(Currency currency) {
        if (!this.currency.equals(currency)) {
            throw new IllegalArgumentException("Unexpected currency.  " + this.currency + " is not " + currency);
        }
    }

    public Currency getCurrency() {
        return this.currency;
    }

    public MoneyAmount movePoint(int n) {
        return new MoneyAmount(this.amount.movePointRight(n), this.currency);
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public MoneyAmount add(MoneyAmount other) {
        if (!other.getCurrency().equals(this.getCurrency())) {
            throw new IllegalArgumentException("Money amounts must be in the same currency.");
        }
        return new MoneyAmount(this.getAmount().add(other.getAmount(), MathConstants.C), this.getCurrency());
    }

    public MoneyAmount subtract(MoneyAmount other) {
        if (!other.getCurrency().equals(this.getCurrency())) {
            throw new IllegalArgumentException("Money amounts must be in the same currency.");
        }
        return new MoneyAmount(this.getAmount().subtract(other.getAmount(), MathConstants.C), this.getCurrency());
    }

    public boolean isZero() {
        return this.amount.signum() == 0;
    }

    public void appendTo(StringBuilder sb, NumberFormat nf) {
        sb.append(this.currency).append(" ").append(nf.format(this.amount));
    }

    public MoneyAmount max(MoneyAmount other) {
        this.assertCurrency(other.getCurrency());

        if (this.getAmount().compareTo(other.getAmount()) >= 0) {
            return this;
        }
        return other;
    }

    public MoneyAmount min(MoneyAmount other) {
        this.assertCurrency(other.getCurrency());

        if (this.getAmount().compareTo(other.getAmount()) >= 0) {
            return other;
        }
        return this;
    }
}
