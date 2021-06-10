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
import java.math.MathContext;
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

    
    private LocalDate inventmentDate;
    private YearMonth investmentYM;

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

    private MoneyAmount currentAmount;

    public InvestmentDetails(boolean nominal) {
        this.nominal = nominal;
    }

    public InvestmentDetails() {
        this(true);
    }

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

    public MoneyAmount getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(MoneyAmount currentAmount) {
        this.currentAmount = currentAmount;
    }

    public MoneyAmount getFees() {
        return this.getBuyCclFee()
                .add(this.getBuyFee())
                .add(this.getBuyFxFee())
                .add(this.getSellCclFee())
                .add(this.getSellFee())
                .add(this.getSellFxFee());

    }

    public BigDecimal getFeePercent() {
        return this.getFees().getAmount()
                .divide(this.getCurrentAmount().getAmount(), MathConstants.CONTEXT);
    }

    public BigDecimal getTaxPercent() {
        return this.getTaxes().getAmount()
                .divide(this.getCurrentAmount().getAmount(), MathConstants.CONTEXT);
    }

    public MoneyAmount getTaxes() {
        return this.getBuyFeeTax()
                .add(this.getBuyFxFeeTax())
                .add(this.getCapitalGainsTax())
                .add(this.getSellFeeTax())
                .add(this.getSellFxFeeTax());

    }

    public MoneyAmount getNetCapitalGains() {
        return this.getCurrentAmount()
                .subtract(this.getSellCclFee())
                .subtract(this.getSellFee())
                .subtract(this.getSellFeeTax())
                .subtract(this.getSellFxFee())
                .subtract(this.getSellFxFeeTax())
                .subtract(this.getCapitalGainsTax())
                .subtract(this.getInvestedAmount());
    }

    public BigDecimal getNetCapitalGainsPercent() {
        return this.getNetCapitalGains().getAmount()
                .divide(this.getInvestedAmount().getAmount(), MathConstants.CONTEXT);

    }

    public MoneyAmount getGrossCapitalGains() {
        return this.getCurrentAmount().subtract(this.getInvestedAmount());
    }

    public BigDecimal getGrossCapitalGainsPercent() {
        return this.getGrossCapitalGains().getAmount()
                .divide(this.getInvestedAmount().getAmount(), MathConstants.CONTEXT);

    }

    public BigDecimal getCAGR() {
        final var days = days(this.inventmentDate);

        if (days.signum() <= 0) {
            return BigDecimal.ZERO;
        }

        final var cumulativeProfit = this.getNetCapitalGainsPercent();

        final double x = Math.pow(
                BigDecimal.ONE.add(cumulativeProfit).doubleValue(),
                365.0d / days.doubleValue()) - 1.0d;

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

            real.setBuyCclFee(this.real(this.getBuyCclFee()));
            real.setBuyFee(this.real(this.getBuyFee()));
            real.setBuyFeeTax(this.real(this.getBuyFeeTax()));
            real.setBuyFxFee(this.real(this.getBuyFxFee()));
            real.setBuyFxFeeTax(this.real(this.getBuyFxFeeTax()));
            real.setInvestedAmount(this.real(this.getInvestedAmount()));

            real.setCapitalGainsTax(this.getCapitalGainsTax());
            real.setCurrentAmount(this.getCurrentAmount());
            real.setInventmentDate(this.getInventmentDate());
            real.setInvestmentCurrency(this.getInvestmentCurrency());
            real.setSellCclFee(this.getSellCclFee());
            real.setSellFee(this.getSellFee());
            real.setSellFeeTax(this.getSellFeeTax());
            real.setSellFxFee(this.getSellFxFee());
            real.setSellFxFeeTax(this.getSellFxFeeTax());
            real.setInvestmentQuantity(this.investmentQuantity);

            return real;
        }

        return this;
    }

    private MoneyAmount real(MoneyAmount nominal) {

        if (this.investmentYM == null) {
            this.investmentYM = YearMonth.of(this.getInventmentDate().getYear(), this.getInventmentDate().getMonthValue());
        }

        return Inflation.USD_INFLATION.adjust(
                nominal, this.investmentYM.getYear(), this.investmentYM.getMonth(),
                Inflation.USD_INFLATION.getTo().getYear(), Inflation.USD_INFLATION.getTo().getMonth());
    }

    public void setInvestmentQuantity(BigDecimal investmentQuantity) {
        this.investmentQuantity = investmentQuantity;
    }

    public BigDecimal getInvestmentPrice(){
        return this.investedAmount.getAmount().divide(this.investmentQuantity, MathConstants.CONTEXT);
    }
}
