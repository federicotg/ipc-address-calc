package org.fede.calculator.money.series;


import java.math.BigDecimal;
import java.util.Date;
import org.fede.calculator.money.MoneyAmount;


/*
 * Copyright (C) 2016 Federico Tello Gentile <federicotg@gmail.com>
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
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
public class Investment {

    private InvestmentType type;
    private InvestmentEvent in;
    private InvestmentAsset investment;
    private InvestmentEvent out;

    public InvestmentType getType() {
        return type;
    }

    public void setType(InvestmentType type) {
        this.type = type;
    }

    public InvestmentEvent getIn() {
        return in;
    }

    public void setIn(InvestmentEvent in) {
        this.in = in;
    }

    public InvestmentAsset getInvestment() {
        return investment;
    }

    public void setInvestment(InvestmentAsset investment) {
        this.investment = investment;
    }

    public InvestmentEvent getOut() {
        return out;
    }

    public void setOut(InvestmentEvent out) {
        this.out = out;
    }

    public boolean isValid() {
        return this.getType().isValid(this.getIn(), this.getOut(), this.getInvestment());
    }

    public MoneyAmount getInvestedAmount() {
        return this.getIn().getMoneyAmount();
    }

    public boolean isCurrent() {
        return this.getOut() == null || this.getOut().getDate().after(new Date());
    }

    public MoneyAmount getInvestmentValue() {
        return this.getInvestment().getMoneyAmount();
    }

    public Date getInvestmentDate() {
        return this.getIn().getDate();
    }

}