/*
 * Copyright (C) 2018 Federico Tello Gentile <federicotg@gmail.com>
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
package org.fede.calculator.web.dto;

import java.math.BigDecimal;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
public class SavingsReportElementDTO {

    private final SavingsReportCurrencyElementDTO pesos = new SavingsReportCurrencyElementDTO();
    private final SavingsReportCurrencyElementDTO dollars = new SavingsReportCurrencyElementDTO();
    

    public AmountAndVariationDTO getDollars() {
        return dollars.getValue();
    }

    public void setDollars(AmountAndVariationDTO dollars) {
        this.dollars.setValue(dollars);
    }

    public AmountAndVariationDTO getPesos() {
        return pesos.getValue();
    }

    public void setPesos(AmountAndVariationDTO pesos) {
        this.pesos.setValue(pesos);
    }

    public AmountAndVariationDTO getIncomePesos() {
        return this.pesos.getIncome();
    }

    public void setIncomePesos(AmountAndVariationDTO incomePesos) {
        this.pesos.setIncome(incomePesos);
    }

    public AmountAndVariationDTO getIncomeDollars() {
        return this.dollars.getIncome();
    }

    public void setIncomeDollars(AmountAndVariationDTO incomeDollars) {
        this.dollars.setIncome(incomeDollars);
    }

    public AmountAndVariationDTO getTotalPesos() {
        return this.pesos.getTotal();
    }

    public void setTotalPesos(AmountAndVariationDTO totalPesos) {
        this.pesos.setTotal(totalPesos);
    }

    public AmountAndVariationDTO getTotalDollars() {
        return this.dollars.getTotal();
    }

    public void setTotalDollars(AmountAndVariationDTO totalDollars) {
        this.dollars.setTotal(totalDollars);
    }

    public BigDecimal getPesosSaved() {
        return this.pesos.getSavings();
    }

    public void setPesosSaved(BigDecimal pesosSaved) {
        this.pesos.setSavings(pesosSaved);
    }

    public BigDecimal getDollarsSaved() {
        return this.dollars.getSavings();
    }

    public void setDollarsSaved(BigDecimal dollarsSaved) {
        this.dollars.setSavings(dollarsSaved);
    }



}
