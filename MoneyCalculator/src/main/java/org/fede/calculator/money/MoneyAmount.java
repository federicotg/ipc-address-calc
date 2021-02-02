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
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Objects;

/**
 *
 * @author fede
 */
public class MoneyAmount {

    private final String currency;
    private final BigDecimal amount;

    public MoneyAmount(BigDecimal amount, String currencySymbol) {
        this.amount = amount;
        this.currency = currencySymbol;
    }

    public MoneyAmount adjust(BigDecimal divisor, BigDecimal factor) {
        if (this.isZero() || divisor.compareTo(factor) == 0) {
            return this;
        }
        return new MoneyAmount(this.amount.setScale(MathConstants.SCALE, MathConstants.ROUNDING_MODE).divide(divisor, MathConstants.CONTEXT)
                .multiply(factor, MathConstants.CONTEXT), this.currency);
    }

    public MoneyAmount exchange(String newCurrency, BigDecimal exchangeRate) {
        if (this.isZero()) {
            return new MoneyAmount(BigDecimal.ZERO, newCurrency);
        }
        return new MoneyAmount(this.amount.multiply(exchangeRate, MathConstants.CONTEXT), newCurrency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.currency, this.amount);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof MoneyAmount
                && ((MoneyAmount) obj).currency.equals(this.currency)
                && ((MoneyAmount) obj).amount.setScale(5, HALF_UP)
                        .compareTo(this.amount.setScale(5, HALF_UP)) == 0;
    }

    @Override
    public String toString() {
        return this.currency + " " + this.amount.toString();
    }

    public void assertCurrency(String currency) {
        if (!this.currency.equals(currency)) {
            throw new IllegalArgumentException("Unexpected currency.");
        }
    }

    public String getCurrency() {
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
        return new MoneyAmount(this.getAmount().add(other.getAmount(), MathConstants.CONTEXT), this.getCurrency());
    }

    public MoneyAmount subtract(MoneyAmount other) {
        if (!other.getCurrency().equals(this.getCurrency())) {
            throw new IllegalArgumentException("Money amounts must be in the same currency.");
        }
        return new MoneyAmount(this.getAmount().subtract(other.getAmount(), MathConstants.CONTEXT), this.getCurrency());
    }

    public boolean isZero() {
        return this.amount.signum() == 0;
    }

    public void appendTo(StringBuilder sb, NumberFormat nf) {
        sb.append(this.currency).append(" ").append(nf.format(this.amount));
    }
}
