/*
 * Copyright (C) 2026 fede
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
package org.fede.calculator.report;

import java.math.BigDecimal;
import org.fede.calculator.money.MathConstants;
import org.fede.calculator.money.MoneyAmount;

/**
 *
 * @author fede
 */
public class Equity {

    private final MoneyAmount us;
    private final MoneyAmount exUs;
    private final MoneyAmount em;

    private final MoneyAmount total;
    private final BigDecimal usWeight;
    private final BigDecimal exUsWeight;
    private final BigDecimal emWeight;

    public Equity(MoneyAmount us, MoneyAmount exUs, MoneyAmount em) {
        this.us = us;
        this.exUs = exUs;
        this.em = em;
        final var totalValue = us.add(em).add(exUs);
        this.total = totalValue;
        this.usWeight = us.amount().divide(totalValue.amount(), MathConstants.C);
        this.exUsWeight = exUs.amount().divide(totalValue.amount(), MathConstants.C);
        this.emWeight = em.amount().divide(totalValue.amount(), MathConstants.C);

    }

    public MoneyAmount us() {
        return us;
    }

    public MoneyAmount em() {
        return em;
    }

    public MoneyAmount exUs() {
        return exUs;
    }

    public MoneyAmount total() {
        return total;
    }

    public BigDecimal usWeight() {
        return usWeight;
    }

    public BigDecimal exUsWeight() {
        return exUsWeight;
    }

    public BigDecimal emWeight() {
        return this.emWeight;
    }
}
