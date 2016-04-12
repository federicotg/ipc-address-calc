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
package org.fede.calculator.web.dto;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Date;

/**
 *
 * @author fede
 */
public class InvestmentReportDTO {

    private Date from;
    private Date to;
    private String currency;
    private BigDecimal initialAmount;
    private BigDecimal finalAmount;
    private BigDecimal differenceAmount;
    private BigDecimal pct;
    private BigDecimal inflationPct;
    private BigDecimal differencePct;
    private boolean current;

    public InvestmentReportDTO(Date from, Date to, String currency, BigDecimal initialAmount, BigDecimal finalAmount, BigDecimal inflationPct, boolean current) {
        this.from = from;
        this.to = to;
        this.currency = currency;
        this.initialAmount = initialAmount;
        this.finalAmount = finalAmount;
        this.inflationPct = inflationPct;
        
        this.differenceAmount = finalAmount.subtract(initialAmount);
        this.pct = this.differenceAmount.divide(this.initialAmount, MathContext.DECIMAL128);
        this.differencePct = this.pct.subtract(this.inflationPct);
        this.current = current;
    }
    
    
    public Date getFrom() {
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }

    public Date getTo() {
        return to;
    }

    public void setTo(Date to) {
        this.to = to;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getInitialAmount() {
        return initialAmount;
    }

    public void setInitialAmount(BigDecimal initialAmount) {
        this.initialAmount = initialAmount;
    }

    public BigDecimal getFinalAmount() {
        return finalAmount;
    }

    public void setFinalAmount(BigDecimal finalAmount) {
        this.finalAmount = finalAmount;
    }

    public BigDecimal getDifferenceAmount() {
        return differenceAmount;
    }

    public void setDifferenceAmount(BigDecimal differenceAmount) {
        this.differenceAmount = differenceAmount;
    }

    public BigDecimal getPct() {
        return pct;
    }

    public void setPct(BigDecimal pct) {
        this.pct = pct;
    }

    public BigDecimal getInflationPct() {
        return inflationPct;
    }

    public void setInflationPct(BigDecimal inflationPct) {
        this.inflationPct = inflationPct;
    }

    public BigDecimal getDifferencePct() {
        return differencePct;
    }

    public void setDifferencePct(BigDecimal differencePct) {
        this.differencePct = differencePct;
    }

    public boolean isCurrent() {
        return current;
    }

    public void setCurrent(boolean current) {
        this.current = current;
    }
    
    


}
