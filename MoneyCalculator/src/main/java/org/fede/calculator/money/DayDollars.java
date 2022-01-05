/*
 * Copyright (C) 2021 federico
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
import java.text.MessageFormat;
import org.fede.calculator.money.series.InvestmentType;

/**
 *
 * @author federico
 */
public class DayDollars {
    
    private final int year;
    private final InvestmentType type;
    private final String currency;
    private final BigDecimal amount;

    public DayDollars(int year, InvestmentType type, String currency, BigDecimal amount) {
        this.year = year;
        this.type = type;
        this.currency = currency;
        this.amount = amount;
    }
    
    public DayDollars combine(DayDollars other){
        return new DayDollars(year, type, currency, amount.add(other.amount, MathConstants.C));
    }
    
    public String getType(){
        return MessageFormat.format("{0} {1}", this.type.toString(), this.currency);
    }
    
    
    public String getYear(){
        return String.valueOf(this.year);
    }

    public BigDecimal getAmount() {
        return amount;
    }
    
}
