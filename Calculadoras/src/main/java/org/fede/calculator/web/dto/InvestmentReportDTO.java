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
import java.math.RoundingMode;
import java.util.Date;

/**
 *
 * @author fede
 */
public class InvestmentReportDTO extends InvestmentDTO {

    private final String type;
    private final Date from;
    private final BigDecimal inflationPct;
    private final BigDecimal differencePct;
    private final String investmentCurrency;
    private final BigDecimal realInvestedAmount;

    public InvestmentReportDTO(String type,
            Date from, 
            Date to, 
            String currency, 
            BigDecimal initialAmount, 
            BigDecimal finalAmount, 
            BigDecimal inflationPct, 
            String investmentCurrency, 
            BigDecimal realProfitPercent, 
            BigDecimal realInvestedAmount) {
        super(currency, initialAmount, finalAmount, to);
        this.type = type;
        this.from = new Date(from.getTime());
        this.inflationPct = inflationPct;
        this.differencePct = realProfitPercent;//this.getPct().subtract(this.inflationPct);
        this.investmentCurrency = investmentCurrency;
        this.realInvestedAmount = realInvestedAmount;
    }

    public Date getFrom() {
        return from;
    }

   
    public BigDecimal getInflationPct() {
        return inflationPct;
    }

   
    public BigDecimal getDifferencePct() {
        return differencePct;
    }

   
    public String getType() {
        return type;
    }


    public String getInvestmentCurrency() {
        return investmentCurrency;
    }

   
    public BigDecimal getDiffPct(){
        return this.getDifferencePct().divide(this.realInvestedAmount, MathContext.DECIMAL64).setScale(4, RoundingMode.HALF_UP);
    }

    public BigDecimal getRealInvestedAmount() {
        return realInvestedAmount;
    }

}
