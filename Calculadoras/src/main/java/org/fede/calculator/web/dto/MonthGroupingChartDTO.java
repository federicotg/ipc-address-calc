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

import javax.validation.constraints.Min;

/**
 *
 * @author fede
 */
public class MonthGroupingChartDTO {

    @Min(1)
    private int months = 12;

    private int year = 1999;
    private int month = 11;
    private CurrencyLimitsDTO limit;

    protected MonthGroupingChartDTO() {
    }

    protected MonthGroupingChartDTO(CurrencyLimitsDTO arsLimits) {
        this.limit = arsLimits;
        this.year = arsLimits.getReferenceYear();
        this.month = arsLimits.getReferenceMonth();
    }

    public int getMonths() {
        return months;
    }

    public void setMonths(int months) {
        this.months = months;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public CurrencyLimitsDTO getLimit() {
        return limit;
    }

    public void setLimit(CurrencyLimitsDTO limit) {
        this.limit = limit;
    }

}
