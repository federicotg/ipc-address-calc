package org.fede.calculator.report;


import java.math.BigDecimal;
import org.fede.calculator.money.Currency;
import org.fede.calculator.money.series.InvestmentType;

/*
 * Copyright (C) 2023 federicogentile
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
/**
 *
 * @author federicogentile
 */
public record InvestmentTypeCurrencyAndAmount(InvestmentType type, Currency currency, BigDecimal amount) {

    public InvestmentTypeCurrencyAndAmount(InvestmentTypeAndCurrency tc, BigDecimal amount) {
        this(tc.type(), tc.currency(), amount);
    }

}
