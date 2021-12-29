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

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import org.fede.calculator.money.ForeignExchange;
import org.fede.calculator.money.ForeignExchanges;
import static org.fede.calculator.money.MathConstants.CONTEXT;
import org.fede.calculator.money.MoneyAmount;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
public class Investment {

    private static final BigDecimal IVA = new BigDecimal("1.21");

    private static final BigDecimal CCL_FEE_FACTOR = BigDecimal.ONE.subtract(new BigDecimal("0.006"), CONTEXT);

    private String id;
    private InvestmentType type;
    private InvestmentEvent in;
    private InvestmentAsset investment;
    private InvestmentEvent out;

    private BigDecimal interest;
    private String comment;

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

    @JsonIgnore
    public boolean isValid() {
        return this.getType().isValid(this.getIn(), this.getOut(), this.getInvestment());
    }

    @JsonIgnore
    public Date getInitialDate() {
        return this.getIn().getDate();
    }

    @JsonIgnore
    public String getInitialCurrency() {
        return this.getIn().getCurrency();
    }

    @JsonIgnore
    public MoneyAmount getInitialMoneyAmount() {
        return this.getIn().getMoneyAmount();
    }

    @JsonIgnore
    public MoneyAmount getMoneyAmount() {
        return this.getInvestment().getMoneyAmount();
    }

    @JsonIgnore
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
        YearMonth min = YearMonth.of(date).min(fx.getTo());
        return fx.exchange(ma, targetCurrency, min);
    }

    @JsonIgnore
    public boolean isCurrent() {
        return this.isCurrent(new Date());
    }

    @JsonIgnore
    public boolean isCurrent(Date now) {
        return this.getIn().getDate().before(now) && (this.getOut() == null || this.getOut().getDate().after(now));
    }

    @JsonIgnore
    public boolean isPast() {
        final Date now = new Date();
        return this.getIn().getDate().before(now) && this.getOut() != null && (!this.getOut().getDate().after(now));
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

    @JsonIgnore
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * @param comment the comment to set
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean currentInYear(int year) {

        final LocalDate reference = LocalDate.of(year, Month.DECEMBER, 31);
        final LocalDate buyDate = this.getIn().getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        final LocalDate sellDate = Optional.ofNullable(this.getOut())
                .map(e -> e.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                .orElse(LocalDate.of(2099, Month.DECEMBER, 31));

        return (buyDate.isBefore(reference) || buyDate.isEqual(reference))
                && sellDate.isAfter(reference);
    }

    public MoneyAmount getCost() {
        return new MoneyAmount(
                Optional.ofNullable(this.getComment())
                        .map(c -> this.ibkrCost())
                        .orElseGet(this::ppiCost), this.getIn().getCurrency());
    }

    private BigDecimal ppiCost() {
        
        final var totalInvestment = this.getIn().getFee()
                .multiply(IVA, CONTEXT)
                .add(this.getIn().getAmount());

        final var cclFees = totalInvestment
                .divide(CCL_FEE_FACTOR, CONTEXT)
                .divide(CCL_FEE_FACTOR, CONTEXT)
                .subtract(totalInvestment, CONTEXT);

        return this.getIn().getFee()
                .multiply(IVA, CONTEXT)
                .add(cclFees, CONTEXT)
                .add(Optional.ofNullable(this.getIn().getTransferFee()).orElse(BigDecimal.ZERO));
    }

    private BigDecimal ibkrCost() {
        return this.getIn().getFee()
                .add(this.getIn().getTransferFee(), CONTEXT);
    }

}
