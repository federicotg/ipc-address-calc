/*
 * Copyright (C) 2025 fede
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

import java.time.temporal.ChronoUnit;
import java.util.Date;
import org.fede.calculator.money.Currency;
import org.fede.calculator.money.MoneyAmount;

/**
 *
 * @author fede
 */
public record InvestmentReturn(Currency currency, Date from, Date to, MoneyAmount initialAmount, MoneyAmount endAmount) {

    public MoneyAmount profit() {
        return this.endAmount().subtract(this.initialAmount());
    }

    public long days() {
        return ChronoUnit.DAYS.between(this.from().toInstant(), this.to().toInstant());
    }
}
