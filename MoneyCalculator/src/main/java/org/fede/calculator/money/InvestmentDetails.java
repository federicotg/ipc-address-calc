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

import java.math.BigDecimal;
import java.time.LocalDate;
import org.fede.calculator.money.series.YearMonth;
import java.time.temporal.ChronoUnit;

/**
 *
 * @author fede
 */
public class InvestmentDetails {

    private final boolean nominal;
    private String investmentCurrency;
    private BigDecimal investmentQuantity;
    private LocalDate investmentDate;
    private YearMonth investmentYM;
    private MoneyAmount investedAmount;
    private MoneyAmount costBasis;
    private MoneyAmount currentAmount;

    public InvestmentDetails(boolean nominal) {
        this.nominal = nominal;
    }

    public InvestmentDetails() {
        this(true);
    }

    public LocalDate getInvestmentDate() {
        return investmentDate;
    }

    public void setInvestmentDate(LocalDate investmentDate) {
        this.investmentDate = investmentDate;
    }

    public MoneyAmount getInvestedAmount() {
        return investedAmount;
    }

    public void setInvestedAmount(MoneyAmount investedAmount) {
        this.investedAmount = investedAmount;
    }

    public String getInvestmentCurrency() {
        return investmentCurrency;
    }

    public void setInvestmentCurrency(String investmentCurrency) {
        this.investmentCurrency = investmentCurrency;
    }

    public MoneyAmount getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(MoneyAmount currentAmount) {
        this.currentAmount = currentAmount;
    }




    public MoneyAmount getGrossCapitalGains() {
        return this.getCurrentAmount().subtract(this.getInvestedAmount());
    }

    public BigDecimal getGrossCapitalGainsPercent() {
        return this.getGrossCapitalGains().getAmount()
                .divide(this.getInvestedAmount().getAmount(), MathConstants.CONTEXT);

    }

    public BigDecimal getCAGR() {
        final var days = days(this.investmentDate);

        if (days.signum() <= 0) {
            return BigDecimal.ZERO;
        }

        final var cumulativeProfit = this.getGrossCapitalGainsPercent();

        final double x = Math.pow(
                BigDecimal.ONE.add(cumulativeProfit).doubleValue(),
                365.0d / days.doubleValue()) - 1.0d;

        if (Double.isNaN(x)) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(x);

    }

    private static BigDecimal days(LocalDate date) {

        return BigDecimal.valueOf(ChronoUnit.DAYS.between(date, LocalDate.now()) + 1);
    }

    public InvestmentDetails asReal() {
        if (!"USD".equals(this.getCurrentAmount().getCurrency())) {
            throw new IllegalArgumentException("No way to turn into real " + this.getCurrentAmount().getCurrency() + ".");
        }

        if (this.nominal) {

            final var real = new InvestmentDetails(false);

            real.setInvestedAmount(this.real(this.getInvestedAmount()));
            real.setCostBasis(this.real(this.getCostBasis()));
            real.setCurrentAmount(this.getCurrentAmount());
            real.setInvestmentDate(this.getInvestmentDate());
            real.setInvestmentCurrency(this.getInvestmentCurrency());
            real.setInvestmentQuantity(this.investmentQuantity);

            return real;
        }

        return this;
    }

    private MoneyAmount real(MoneyAmount nominal) {

        if (this.investmentYM == null) {
            this.investmentYM = YearMonth.of(this.getInvestmentDate().getYear(), this.getInvestmentDate().getMonthValue());
        }

        return Inflation.USD_INFLATION.adjust(nominal, this.investmentYM, Inflation.USD_INFLATION.getTo());
    }

    public void setInvestmentQuantity(BigDecimal investmentQuantity) {
        this.investmentQuantity = investmentQuantity;
    }

    public BigDecimal getInvestmentPrice() {
        return this.investedAmount.getAmount().divide(this.investmentQuantity, MathConstants.CONTEXT);
    }

    public MoneyAmount getCostBasis() {
        return costBasis;
    }

    public void setCostBasis(MoneyAmount costBasis) {
        this.costBasis = costBasis;
    }

}
