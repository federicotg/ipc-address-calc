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
package org.fede.calculator.money.series;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;
import java.util.StringJoiner;
import org.fede.calculator.money.ForeignExchange;
import org.fede.calculator.money.ForeignExchanges;
import org.fede.calculator.money.MoneyAmount;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */

public class Investment {

    private String id;
    private InvestmentType type;
    private InvestmentEvent in;
    private InvestmentAsset investment;
    private InvestmentEvent out;

    private BigDecimal interest;

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

    public Date getInitialDate() {
        return this.getIn().getDate();
    }

    public String getInitialCurrency() {
        return this.getIn().getCurrency();
    }

    public MoneyAmount getInitialMoneyAmount() {
        return this.getIn().getMoneyAmount();
    }

    public MoneyAmount getMoneyAmount() {
        return this.getInvestment().getMoneyAmount();
    }

    public String getCurrency() {
        return this.getInvestment().getCurrency();
    }

    public BigDecimal getInterest() {
        return interest;
    }

    public void setInterest(BigDecimal interest) {
        this.interest = interest;
    }

    public MoneyAmount finalAmount(String targetCurrency, Date date) {

        if (this.getOut() != null) {
            return changeCurrency(this.getOut().getMoneyAmount(), targetCurrency, date);
        }

        return changeCurrency(investment.getMoneyAmount(), targetCurrency, date);
        
        // while still invested, add interest rate if any
        /*return changeCurrency(this.getMoneyAmount(), targetCurrency, date)
                .add(
                        changeCurrency(
                                interest(this.getMoneyAmount(), this.getInterest(), this.getInitialDate(), date),
                                targetCurrency,
                                date)
                );*/
    }

    public MoneyAmount initialAmount(String targetCurrency) {

        MoneyAmount amount;
        if (targetCurrency.equals(this.getInitialCurrency())) {
            amount = this.getInitialMoneyAmount();
        } else if (targetCurrency.equals(this.getCurrency())) {
            amount = this.getMoneyAmount();
        } else {
            amount = changeCurrency(this.getInitialMoneyAmount(), targetCurrency, this.getInitialDate());
        }
        return amount;
    }

    private static MoneyAmount changeCurrency(MoneyAmount ma, String targetCurrency, Date date) {
        ForeignExchange fx = ForeignExchanges.getForeignExchange(ma.getCurrency(), targetCurrency);
        YearMonth min = new YearMonth(date).min(fx.getTo());
        return fx.exchange(ma, targetCurrency, min.getYear(), min.getMonth());
    }

//    private static MoneyAmount interest(MoneyAmount investedAmount, BigDecimal interestRate, Date investmentDate, Date currentDate) {
//
//        if (interestRate == null) {
//            return new MoneyAmount(BigDecimal.ZERO, investedAmount.getCurrency());
//        }
//
//        final long days = ChronoUnit.DAYS.between(
//                investmentDate.toInstant().atZone(ZoneOffset.UTC).toLocalDate(),
//                currentDate.toInstant().atZone(ZoneOffset.UTC).toLocalDate());
//
//        if (days <= 0l) {
//            return new MoneyAmount(BigDecimal.ZERO, investedAmount.getCurrency());
//        }
//
//        final BigDecimal interest = (investedAmount.getAmount().multiply(interestRate, CONTEXT).divide(DAYS_IN_ONE_YEAR, CONTEXT))
//                .multiply(BigDecimal.valueOf(days), CONTEXT);
//
//        return new MoneyAmount(interest, investedAmount.getCurrency());
//    }

    public boolean isCurrent() {
        final Date now = new Date();
        return this.getIn().getDate().before(now) && (this.getOut() == null || this.getOut().getDate().after(now));
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", this.getClass().getSimpleName() + " [", "]")
                .add("InvestmentType: " + this.getType().toString())
                .add("\n\tin: " + this.in.toString())
                .add("\n\tinvestment: " + this.investment.toString())
                .add("\n\tout: " + Objects.toString(this.out, "null"))
                .toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
