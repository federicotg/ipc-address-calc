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
import static java.math.BigDecimal.ONE;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import org.fede.calculator.money.Currency;
import static org.fede.calculator.money.Currency.USD;
import org.fede.calculator.money.ForeignExchanges;
import org.fede.calculator.money.Inflation;
import static org.fede.calculator.money.MathConstants.C;
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
    public Date getInitialDate() {
        return this.getIn().getDate();
    }

    @JsonIgnore
    public Currency getInitialCurrency() {
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
    public MoneyAmount getRealUSDInitialMoneyAmount() {

        return this.getIn().getRealUSDMoneyAmount();
    }

    @JsonIgnore
    public Currency getCurrency() {
        return this.getInvestment().getCurrency();
    }

    public BigDecimal getInterest() {
        return interest;
    }

    public void setInterest(BigDecimal interest) {
        this.interest = interest;
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
                .orElseGet(() -> LocalDate.of(2099, Month.DECEMBER, 31));

        return (buyDate.isBefore(reference) || buyDate.isEqual(reference))
                && sellDate.isAfter(reference);
    }

    public MoneyAmount getRealUSDCost() {
        final var now = Inflation.USD_INFLATION.getTo();
        final var then = YearMonth.of(this.getInitialDate());
        final var cost = this.getCost();

        MoneyAmount ma = null;
        if (this.getIn().getCurrency() != USD && this.getIn().getFx() != null) {

            ma = new MoneyAmount(
                    new MoneyAmount(this.getIn().getFee()
                            .add(this.getIn().getTransferFee()),
                            this.getIn().getCurrency())
                            .adjust(BigDecimal.ONE, this.getIn().getFx()).amount(), Currency.USD);

        } else {
            ma = ForeignExchanges.getMoneyAmountForeignExchange(cost.currency(), USD)
                    .apply(cost, then);
        }

        return Inflation.USD_INFLATION.adjust(
                ma,
                then,
                now);
    }

    public MoneyAmount getCost() {
        return new MoneyAmount(
                Optional.ofNullable(this.getComment())
                        .map(c -> this.ibkrCost())
                        .orElseGet(this::ppiCost), this.getIn().getCurrency());
    }

    private BigDecimal ccl() {
        var iva = SeriesReader.readPercent("iva").add(ONE);

        final var totalInvestment = this.getIn().getFee()
                .multiply(iva, C)
                .add(this.getIn().getAmount());

        
        BigDecimal cclFeeFactor = BigDecimal.ONE.subtract(new BigDecimal("0.006"), C);

        return totalInvestment
                .divide(cclFeeFactor, C)
                .divide(cclFeeFactor, C)
                .subtract(totalInvestment, C);
    }

    private BigDecimal ppiCost() {
        var iva = SeriesReader.readPercent("iva").add(ONE);

        return this.getIn().getFee()
                .multiply(iva, C)
                .add(Optional.ofNullable(this.getIn().getTransferFee()).orElseGet(this::ccl));
    }

    private BigDecimal ibkrCost() {
        return this.getIn().getFee()
                .add(this.getIn().getTransferFee(), C);
    }

    public boolean isETF() {
        return this.getType() == InvestmentType.ETF;
    }

}
