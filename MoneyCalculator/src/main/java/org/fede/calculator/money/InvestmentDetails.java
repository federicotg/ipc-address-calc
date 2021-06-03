/*
 * Copyright (C) 2021 fede
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

import java.time.LocalDate;
import org.fede.calculator.money.series.YearMonth;

/**
 *
 * @author fede
 */
public class InvestmentDetails {

    private String investmentCurrency;

    private LocalDate inventmentDate;

    private MoneyAmount buyCclFee;
    private MoneyAmount buyFee;
    private MoneyAmount buyFeeTax;
    private MoneyAmount buyFxFee;
    private MoneyAmount buyFxFeeTax;

    private MoneyAmount investedAmount;

    private MoneyAmount sellCclFee;
    private MoneyAmount sellFee;
    private MoneyAmount sellFeeTax;
    private MoneyAmount sellFxFee;
    private MoneyAmount sellFxFeeTax;
    private MoneyAmount capitalGainsTax;

    public LocalDate getInventmentDate() {
        return inventmentDate;
    }

    public void setInventmentDate(LocalDate inventmentDate) {
        this.inventmentDate = inventmentDate;
    }

    public MoneyAmount getBuyCclFee() {
        return buyCclFee;
    }

    public void setBuyCclFee(MoneyAmount buyCclFee) {
        this.buyCclFee = buyCclFee;
    }

    public MoneyAmount getSellCclFee() {
        return sellCclFee;
    }

    public void setSellCclFee(MoneyAmount sellCclFee) {
        this.sellCclFee = sellCclFee;
    }

    public MoneyAmount getBuyFee() {
        return buyFee;
    }

    public void setBuyFee(MoneyAmount buyFee) {
        this.buyFee = buyFee;
    }

    public MoneyAmount getBuyFeeTax() {
        return buyFeeTax;
    }

    public void setBuyFeeTax(MoneyAmount buyFeeTax) {
        this.buyFeeTax = buyFeeTax;
    }

    public MoneyAmount getBuyFxFee() {
        return buyFxFee;
    }

    public void setBuyFxFee(MoneyAmount buyFxFee) {
        this.buyFxFee = buyFxFee;
    }

    public MoneyAmount getBuyFxFeeTax() {
        return buyFxFeeTax;
    }

    public void setBuyFxFeeTax(MoneyAmount buyFxFeeTax) {
        this.buyFxFeeTax = buyFxFeeTax;
    }

    public MoneyAmount getInvestedAmount() {
        return investedAmount;
    }

    public void setInvestedAmount(MoneyAmount investedAmount) {
        this.investedAmount = investedAmount;
    }

    public MoneyAmount getSellFee() {
        return sellFee;
    }

    public void setSellFee(MoneyAmount sellFee) {
        this.sellFee = sellFee;
    }

    public MoneyAmount getSellFeeTax() {
        return sellFeeTax;
    }

    public void setSellFeeTax(MoneyAmount sellFeeTax) {
        this.sellFeeTax = sellFeeTax;
    }

    public MoneyAmount getSellFxFee() {
        return sellFxFee;
    }

    public void setSellFxFee(MoneyAmount sellFxFee) {
        this.sellFxFee = sellFxFee;
    }

    public MoneyAmount getSellFxFeeTax() {
        return sellFxFeeTax;
    }

    public void setSellFxFeeTax(MoneyAmount sellFxFeeTax) {
        this.sellFxFeeTax = sellFxFeeTax;
    }

    public MoneyAmount getCapitalGainsTax() {
        return capitalGainsTax;
    }

    public void setCapitalGainsTax(MoneyAmount capitalGainsTax) {
        this.capitalGainsTax = capitalGainsTax;
    }

    public String getInvestmentCurrency() {
        return investmentCurrency;
    }

    public void setInvestmentCurrency(String investmentCurrency) {
        this.investmentCurrency = investmentCurrency;
    }

    public MoneyAmount getFees() {
        return this.getBuyCclFee()
                .add(this.getBuyFee())
                .add(this.getBuyFxFee())
                .add(this.getSellCclFee())
                .add(this.getSellFee())
                .add(this.getSellFxFee());

    }

    public MoneyAmount getTaxes() {
        return this.getBuyFeeTax()
                .add(this.getBuyFxFeeTax())
                .add(this.getCapitalGainsTax())
                .add(this.getSellFeeTax())
                .add(this.getSellFxFeeTax());

    }

}
