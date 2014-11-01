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
import org.fede.calculator.money.series.YearMonth;

/**
 *
 * @author fede
 */
public class SavingsReportDTO implements Comparable<SavingsReportDTO> {

    public SavingsReportDTO(int year, int month) {
        this.moment = new YearMonth(year, month);
    }
   
    
    private YearMonth moment;

    private BigDecimal pesosForDollar;
    private BigDecimal pesosForDollarPctVar;

    private BigDecimal nominalDollars;
    private BigDecimal nominalDollarsPctVar;
    private BigDecimal nominalPesos;
    private BigDecimal nominalPesosPctVar;

    private BigDecimal totalNominalPesos;
    private BigDecimal totalNominalPesosPctVar;
    private BigDecimal totalNominalDollars;
    private BigDecimal totalNominalDollarsPctVar;

    private BigDecimal nov99Dollars;
    private BigDecimal nov99DollarsPctVar;
    private BigDecimal nov99Pesos;
    private BigDecimal nov99PesosPctVar;

    private BigDecimal totalNov99Dollars;
    private BigDecimal totalNov99DollarsPctVar;
    private BigDecimal totalNov99Pesos;
    private BigDecimal totalNov99PesosPctVar;
    

    @Override
    public int compareTo(SavingsReportDTO o) {
        return this.moment.compareTo(o.getMoment());
    }

    public YearMonth getMoment() {
        return moment;
    }

    public void setMoment(YearMonth moment) {
        this.moment = moment;
    }

    public BigDecimal getPesosForDollar() {
        return pesosForDollar;
    }

    public void setPesosForDollar(BigDecimal pesosForDollar) {
        this.pesosForDollar = pesosForDollar;
    }

    public BigDecimal getNominalDollars() {
        return nominalDollars;
    }

    public void setNominalDollars(BigDecimal nominalDollars) {
        this.nominalDollars = nominalDollars;
    }

    public BigDecimal getNominalPesos() {
        return nominalPesos;
    }

    public void setNominalPesos(BigDecimal nominalPesos) {
        this.nominalPesos = nominalPesos;
    }

    public BigDecimal getTotalNominalPesos() {
        return totalNominalPesos;
    }

    public void setTotalNominalPesos(BigDecimal totalNominalPesos) {
        this.totalNominalPesos = totalNominalPesos;
    }

    public BigDecimal getTotalNominalDollars() {
        return totalNominalDollars;
    }

    public void setTotalNominalDollars(BigDecimal totalNominalDollars) {
        this.totalNominalDollars = totalNominalDollars;
    }

    public BigDecimal getNov99Dollars() {
        return nov99Dollars;
    }

    public void setNov99Dollars(BigDecimal nov99Dollars) {
        this.nov99Dollars = nov99Dollars;
    }

    public BigDecimal getNov99Pesos() {
        return nov99Pesos;
    }

    public void setNov99Pesos(BigDecimal nov99Pesos) {
        this.nov99Pesos = nov99Pesos;
    }

    public BigDecimal getTotalNov99Dollars() {
        return totalNov99Dollars;
    }

    public void setTotalNov99Dollars(BigDecimal totalNov99Dollars) {
        this.totalNov99Dollars = totalNov99Dollars;
    }

    public BigDecimal getTotalNov99Pesos() {
        return totalNov99Pesos;
    }

    public void setTotalNov99Pesos(BigDecimal totalNov99Pesos) {
        this.totalNov99Pesos = totalNov99Pesos;
    }

    public BigDecimal getPesosForDollarPctVar() {
        return pesosForDollarPctVar;
    }

    public void setPesosForDollarPctVar(BigDecimal pesosForDollarPctVar) {
        this.pesosForDollarPctVar = pesosForDollarPctVar;
    }

    public BigDecimal getNominalDollarsPctVar() {
        return nominalDollarsPctVar;
    }

    public void setNominalDollarsPctVar(BigDecimal nominalDollarsPctVar) {
        this.nominalDollarsPctVar = nominalDollarsPctVar;
    }

    public BigDecimal getNominalPesosPctVar() {
        return nominalPesosPctVar;
    }

    public void setNominalPesosPctVar(BigDecimal nominalPesosPctVar) {
        this.nominalPesosPctVar = nominalPesosPctVar;
    }

    public BigDecimal getTotalNominalPesosPctVar() {
        return totalNominalPesosPctVar;
    }

    public void setTotalNominalPesosPctVar(BigDecimal totalNominalPesosPctVar) {
        this.totalNominalPesosPctVar = totalNominalPesosPctVar;
    }

    public BigDecimal getTotalNominalDollarsPctVar() {
        return totalNominalDollarsPctVar;
    }

    public void setTotalNominalDollarsPctVar(BigDecimal totalNominalDollarsPctVar) {
        this.totalNominalDollarsPctVar = totalNominalDollarsPctVar;
    }

    public BigDecimal getNov99DollarsPctVar() {
        return nov99DollarsPctVar;
    }

    public void setNov99DollarsPctVar(BigDecimal nov99DollarsPctVar) {
        this.nov99DollarsPctVar = nov99DollarsPctVar;
    }

    public BigDecimal getNov99PesosPctVar() {
        return nov99PesosPctVar;
    }

    public void setNov99PesosPctVar(BigDecimal nov99PesosPctVar) {
        this.nov99PesosPctVar = nov99PesosPctVar;
    }

    public BigDecimal getTotalNov99DollarsPctVar() {
        return totalNov99DollarsPctVar;
    }

    public void setTotalNov99DollarsPctVar(BigDecimal totalNov99DollarsPctVar) {
        this.totalNov99DollarsPctVar = totalNov99DollarsPctVar;
    }

    public BigDecimal getTotalNov99PesosPctVar() {
        return totalNov99PesosPctVar;
    }

    public void setTotalNov99PesosPctVar(BigDecimal totalNov99PesosPctVar) {
        this.totalNov99PesosPctVar = totalNov99PesosPctVar;
    }

}
