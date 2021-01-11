package org.fede.calculator.money.series;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;
import java.util.StringJoiner;
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
    private String currency;
    

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

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
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

    @Override
    public String toString() {
        return new StringJoiner(", ", this.getClass().getSimpleName() + " [", "]")
                .add("currency: " + this.currency)
                .add("amount: " + this.amount.toString())
                .add("date: " + this.date.toString())
                .toString();
    }

}
