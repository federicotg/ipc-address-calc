package org.fede.calculator.money.series;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;
import java.util.StringJoiner;
import org.fede.calculator.money.Currency;
import static org.fede.calculator.money.Currency.USD;
import org.fede.calculator.money.ForeignExchanges;
import org.fede.calculator.money.Inflation;
import static org.fede.calculator.money.MathConstants.C;
import org.fede.calculator.money.MoneyAmount;

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
/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
public class InvestmentEvent {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy", timezone = "GMT-3")
    private LocalDate date;
    private BigDecimal amount;
    private BigDecimal fee;
    private Currency currency;
    private BigDecimal transferFee;
    private BigDecimal fx;

    public BigDecimal getTransferFee() {
        return this.transferFee;
    }

    public void setTransferFee(BigDecimal transferFee) {
        this.transferFee = transferFee;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public BigDecimal getFee() {
        return Optional.ofNullable(fee)
                .orElse(BigDecimal.ZERO);
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    @JsonIgnore
    public MoneyAmount getMoneyAmount() {
        return new MoneyAmount(this.getAmount(), this.getCurrency());
    }

   
    private MoneyAmount getFeeMoneyAmount() {
        return new MoneyAmount(this.getFee(), this.getCurrency());
    }

    @JsonIgnore
    public MoneyAmount getFeeMoneyAmount(Currency currency) {
        final var feeAmount = this.fx == null
                ? this.getFeeMoneyAmount()
                : new MoneyAmount(this.getFee().multiply(this.fx, C), USD);
        if (currency == feeAmount.currency()) {
            return feeAmount;
        }
        return ForeignExchanges.getForeignExchange(feeAmount.currency(), currency)
                .exchange(feeAmount, currency, this.date);
    }

    @JsonIgnore
    public MoneyAmount getTransferFeeMoneyAmount(Currency currency) {
        if (this.transferFee == null) {
            return MoneyAmount.zero(currency);
        }
        final var feeAmount = this.fx == null
                ? new MoneyAmount(this.getTransferFee(), this.currency)
                : new MoneyAmount(this.getTransferFee().multiply(this.fx, C), USD);
        if (currency == feeAmount.currency()) {
            return feeAmount;
        }
        return ForeignExchanges.getForeignExchange(feeAmount.currency(), currency)
                .exchange(feeAmount, currency, this.date);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", this.getClass().getSimpleName() + " [", "]")
                .add("currency: " + this.currency)
                .add("amount: " + this.amount.toString())
                .add("date: " + this.date.toString())
                .toString();
    }

    public BigDecimal getFx() {
        return fx;
    }

    public void setFx(BigDecimal fx) {
        this.fx = fx;
    }

    @JsonIgnore
    private MoneyAmount getRealUSDTransferFeeMoneyAmount() {
        return this.real(this.getTransferFeeMoneyAmount(USD));
    }

    @JsonIgnore
    private MoneyAmount getRealUSDFeeMoneyAmount() {
        return this.real(this.getFeeMoneyAmount(USD));
    }

    @JsonIgnore
    public MoneyAmount getMoneyAmount(Currency currency) {

        final var amountValue = this.fx == null
                ? this.getMoneyAmount()
                : new MoneyAmount(this.getAmount().multiply(this.fx, C), USD);
        if (currency == amountValue.currency()) {
            return amountValue;
        }
        return ForeignExchanges.getForeignExchange(amountValue.currency(), currency)
                .exchange(amountValue, currency, this.date);
    }

    private MoneyAmount real(MoneyAmount nominal) {

        return Inflation.usdInflation().adjust(
                nominal,
                YearMonth.from(this.getDate()),
                Inflation.usdInflation().getTo());

    }

    @JsonIgnore
    public MoneyAmount getRealUSDMoneyAmount() {

        return this.real(this.getMoneyAmount(USD));

    }

}
