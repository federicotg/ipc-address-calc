package org.fede.calculator.money.series;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;
import java.util.StringJoiner;
import org.fede.calculator.money.Currency;
import static org.fede.calculator.money.Currency.USD;
import org.fede.calculator.money.ForeignExchanges;
import org.fede.calculator.money.Inflation;
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
    private Date date;
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
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

    @JsonIgnore
    public MoneyAmount getFeeMoneyAmount() {
        return new MoneyAmount(this.getFee(), this.getCurrency());
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

    public MoneyAmount getRealUSDTransferFeeMoneyAmount() {
        return this.real(new MoneyAmount(this.getTransferFee(), this.getCurrency()));
    }
    
    public MoneyAmount getRealUSDFeeMoneyAmount() {
        return this.real(this.getFeeMoneyAmount());

//        final var now = Inflation.USD_INFLATION.getTo();
//        final var toUSD = ForeignExchanges.getMoneyAmountForeignExchange(this.getCurrency(), USD);
//        MoneyAmount ma = null;
//        //var logger = LoggerFactory.getLogger(ConsoleReports.class);
//
//        if (this.getCurrency() != Currency.USD && this.fx != null) {
//            ma = new MoneyAmount(this.getFeeMoneyAmount().adjust(BigDecimal.ONE, this.fx).amount(), Currency.USD);
//
//            //logger.error("ma {} curr {}  fx {} => new ma {}", this.getMoneyAmount(), this.currency, this.fx, ma);
//        } else {
//            ma = toUSD.apply(this.getFeeMoneyAmount(), YearMonth.of(this.getDate()));
//        }
//
//        return Inflation.USD_INFLATION.adjust(
//                toUSD
//                        .apply(ma, YearMonth.of(this.getDate())),
//                YearMonth.of(this.getDate()),
//                now);
    }

    private MoneyAmount real(MoneyAmount nominal) {
        final var now = Inflation.USD_INFLATION.getTo();
        final var toUSD = ForeignExchanges.getMoneyAmountForeignExchange(this.getCurrency(), USD);
        MoneyAmount ma = null;
        //var logger = LoggerFactory.getLogger(ConsoleReports.class);

        if (this.getCurrency() != Currency.USD && this.fx != null) {
            ma = new MoneyAmount(nominal.adjust(BigDecimal.ONE, this.fx).amount(), Currency.USD);

            //logger.error("ma {} curr {}  fx {} => new ma {}", this.getMoneyAmount(), this.currency, this.fx, ma);
        } else {
            ma = toUSD.apply(nominal, YearMonth.of(this.getDate()));
        }

        return Inflation.USD_INFLATION.adjust(
                toUSD
                        .apply(ma, YearMonth.of(this.getDate())),
                YearMonth.of(this.getDate()),
                now);

    }

    public MoneyAmount getRealUSDMoneyAmount() {

        return this.real(this.getMoneyAmount());

//        final var now = Inflation.USD_INFLATION.getTo();
//        final var toUSD = ForeignExchanges.getMoneyAmountForeignExchange(this.getCurrency(), USD);
//        MoneyAmount ma = null;
//        //var logger = LoggerFactory.getLogger(ConsoleReports.class);
//
//        if (this.getCurrency() != Currency.USD && this.fx != null) {
//            ma = new MoneyAmount(this.getMoneyAmount().adjust(BigDecimal.ONE, this.fx).amount(), Currency.USD);
//
//            //logger.error("ma {} curr {}  fx {} => new ma {}", this.getMoneyAmount(), this.currency, this.fx, ma);
//        } else {
//            ma = toUSD.apply(this.getMoneyAmount(), YearMonth.of(this.getDate()));
//        }
//
//        return Inflation.USD_INFLATION.adjust(
//                toUSD
//                        .apply(ma, YearMonth.of(this.getDate())),
//                YearMonth.of(this.getDate()),
//                now);
    }

}
