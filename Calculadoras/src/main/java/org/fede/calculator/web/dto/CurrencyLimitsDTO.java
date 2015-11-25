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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 *
 * @author fede
 */
public class CurrencyLimitsDTO {

    private static final List<MonthDTO> MONTHS = Arrays.asList(new MonthDTO[]{
        new MonthDTO(1, "enero"),
        new MonthDTO(2, "febrero"),
        new MonthDTO(3, "marzo"),
        new MonthDTO(4, "abril"),
        new MonthDTO(5, "mayo"),
        new MonthDTO(6, "junio"),
        new MonthDTO(7, "julio"),
        new MonthDTO(8, "agosto"),
        new MonthDTO(9, "septiembre"),
        new MonthDTO(10, "octubre"),
        new MonthDTO(11, "noviembre"),
        new MonthDTO(12, "diciembre")
    });
    private CurrencyDTO currency;
    private int yearFrom;
    private int yearTo;
    private List<MonthDTO> months;
    private int referenceYear;
    private int referenceMonth;

    public CurrencyLimitsDTO(CurrencyDTO currency, int yearFrom, int yearTo, int referenceYear, int referenceMonth) {
        this.currency = currency;
        this.yearFrom = yearFrom;
        this.yearTo = yearTo;
        this.months = MONTHS;
        this.referenceMonth = referenceMonth;
        this.referenceYear= referenceYear;
    }

    public CurrencyDTO getCurrency() {
        return currency;
    }

    public void setCurrency(CurrencyDTO currency) {
        this.currency = currency;
    }

    public int getYearFrom() {
        return yearFrom;
    }

    public void setYearFrom(int yearFrom) {
        this.yearFrom = yearFrom;
    }

    public int getYearTo() {
        return yearTo;
    }

    public void setYearTo(int yearTo) {
        this.yearTo = yearTo;
    }

    public List<MonthDTO> getMonths() {
        return months;
    }

    public void setMonths(List<MonthDTO> months) {
        this.months = months;
    }

    public int getReferenceYear() {
        return referenceYear;
    }

    public int getReferenceMonth() {
        return referenceMonth;
    }

    public void setReferenceYear(int referenceYear) {
        this.referenceYear = referenceYear;
    }

    public void setReferenceMonth(int referenceMonth) {
        this.referenceMonth = referenceMonth;
    }

    public String getReferenceDateString(){
        DateFormat df = new SimpleDateFormat("MMM yyyy");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, this.referenceYear);
        cal.set(Calendar.MONTH, this.referenceMonth - 1);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return df.format(cal.getTime());
    }
}
