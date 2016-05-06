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
import java.util.Date;

/**
 *
 * @author fede
 */
public class InvestmentReportDTO extends InvestmentDTO {

    private String type;
    private Date from;
    private BigDecimal inflationPct;
    private BigDecimal differencePct;
    private String investmentCurrency;

    public InvestmentReportDTO(String type, Date from, Date to, String currency, BigDecimal initialAmount, BigDecimal finalAmount, BigDecimal inflationPct, String investmentCurrency) {
        super(currency, initialAmount, finalAmount, to);
        this.type = type;
        this.from = new Date(from.getTime());
        this.inflationPct = inflationPct;
        this.differencePct = this.getPct().subtract(this.inflationPct);
        this.investmentCurrency = investmentCurrency;
    }

    public Date getFrom() {
        return from;
    }

    public void setFrom(Date from) {
        this.from = new Date(from.getTime());
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getInvestmentCurrency() {
        return investmentCurrency;
    }

    public void setInvestmentCurrency(String investmentCurrency) {
        this.investmentCurrency = investmentCurrency;
    }

}
