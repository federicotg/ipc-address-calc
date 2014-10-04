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
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import org.springframework.format.annotation.NumberFormat;

/**
 *
 * @author fede
 */
public class MoneyDTO {

    @Min(value = 0)
    @NumberFormat(style = NumberFormat.Style.NUMBER)
    private BigDecimal amount = BigDecimal.ONE;
    @Min(value = 1900)
    private int fromYear = 2014;
    @Min(1)
    @Max(12)
    private int fromMonth = 1;
    @Min(value = 1900)
    private int toYear = 1999;
    @Min(1)
    @Max(12)
    private int toMonth = 1;
    private boolean valid;
    private String message;
    private String fromCurrencySymbol;
    private String toCurrencySymbol;
    private String fromIso4217;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public int getFromYear() {
        return fromYear;
    }

    public void setFromYear(int fromYear) {
        this.fromYear = fromYear;
    }

    public int getFromMonth() {
        return fromMonth;
    }

    public void setFromMonth(int fromMonth) {
        this.fromMonth = fromMonth;
    }

    public int getToYear() {
        return toYear;
    }

    public void setToYear(int toYear) {
        this.toYear = toYear;
    }

    public int getToMonth() {
        return toMonth;
    }

    public void setToMonth(int toMonth) {
        this.toMonth = toMonth;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFromCurrencySymbol() {
        return fromCurrencySymbol;
    }

    public void setFromCurrencySymbol(String fromCurrencySymbol) {
        this.fromCurrencySymbol = fromCurrencySymbol;
    }

    public String getToCurrencySymbol() {
        return toCurrencySymbol;
    }

    public void setToCurrencySymbol(String toCurrencySymbol) {
        this.toCurrencySymbol = toCurrencySymbol;
    }

    public String getFromIso4217() {
        return fromIso4217;
    }

    public void setFromIso4217(String fromIso4217) {
        this.fromIso4217 = fromIso4217;
    }

}
