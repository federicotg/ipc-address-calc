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
    private final AmountAndVariationDTO pesosForDollar = new AmountAndVariationDTO();
    private final SavingsReportElementDTO nominal = new SavingsReportElementDTO();
    private final SavingsReportElementDTO real = new SavingsReportElementDTO();

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
        return pesosForDollar.getAmount();
    }

    public void setPesosForDollar(BigDecimal pesosForDollar) {
        this.pesosForDollar.setAmount(pesosForDollar);
    }

    public BigDecimal getNominalDollars() {
        return this.nominal.getDollars().getAmount();
    }

    public void setNominalDollars(BigDecimal nominalDollars) {
        this.nominal.getDollars().setAmount(nominalDollars);
    }

    public BigDecimal getNominalPesos() {
        return this.nominal.getPesos().getAmount();
    }

    public void setNominalPesos(BigDecimal nominalPesos) {
        this.nominal.getPesos().setAmount(nominalPesos);
    }

    public BigDecimal getTotalNominalPesos() {
        return this.nominal.getTotalPesos().getAmount();
    }

    public void setTotalNominalPesos(BigDecimal totalNominalPesos) {
        this.nominal.getTotalPesos().setAmount(totalNominalPesos);
    }

    public BigDecimal getTotalNominalDollars() {
        return this.nominal.getTotalDollars().getAmount();
    }

    public void setTotalNominalDollars(BigDecimal totalNominalDollars) {
        this.nominal.getTotalDollars().setAmount(totalNominalDollars);
    }

    public BigDecimal getNov99Dollars() {
        return this.real.getDollars().getAmount();
    }

    public void setNov99Dollars(BigDecimal nov99Dollars) {
        this.real.getDollars().setAmount(nov99Dollars);
    }


    public BigDecimal getTotalNov99Dollars() {
        return this.real.getTotalDollars().getAmount();
    }

    public void setTotalNov99Dollars(BigDecimal totalNov99Dollars) {
        this.real.getTotalDollars().setAmount(totalNov99Dollars);
    }

    public BigDecimal getPesosForDollarPctVar() {
        return this.pesosForDollar.getVariation();
    }

    public void setPesosForDollarPctVar(BigDecimal pesosForDollarPctVar) {
        this.pesosForDollar.setVariation(pesosForDollarPctVar);
    }

    public BigDecimal getTotalNominalPesosPctVar() {
        return this.nominal.getTotalPesos().getVariation();
    }

    public void setTotalNominalPesosPctVar(BigDecimal totalNominalPesosPctVar) {
        this.nominal.getTotalPesos().setVariation(totalNominalPesosPctVar);
    }

    public BigDecimal getTotalNominalDollarsPctVar() {
        return this.nominal.getTotalDollars().getVariation();
    }

    public void setTotalNominalDollarsPctVar(BigDecimal totalNominalDollarsPctVar) {
        this.nominal.getTotalDollars().setVariation(totalNominalDollarsPctVar);
    }

    public BigDecimal getTotalNov99DollarsPctVar() {
        return this.real.getTotalDollars().getVariation();
    }

    public void setTotalNov99DollarsPctVar(BigDecimal totalNov99DollarsPctVar) {
        this.real.getTotalDollars().setVariation(totalNov99DollarsPctVar);
    }

    public BigDecimal getNominalIncomePesos() {
        return this.nominal.getIncomePesos().getAmount();
    }

    public void setNominalIncomePesos(BigDecimal nominalIncomePesos) {
        this.nominal.getIncomePesos().setAmount(nominalIncomePesos);
    }

    public BigDecimal getNominalIncomeDollars() {
        return this.nominal.getIncomeDollars().getAmount();
    }

    public void setNominalIncomeDollars(BigDecimal nominalIncomeDollars) {
        this.nominal.getIncomeDollars().setAmount(nominalIncomeDollars);
    }

    public BigDecimal getNov99IncomeDollars() {
        return this.real.getIncomeDollars().getAmount();
    }

    public void setNov99IncomeDollars(BigDecimal nov99IncomeDollars) {
        this.real.getIncomeDollars().setAmount(nov99IncomeDollars);
    }

    public BigDecimal getNov99DollarPctSaved() {
        return this.real.getDollarsSaved();
    }

    public void setNov99DollarPctSaved(BigDecimal nov99DollarPctSaved) {
        this.real.setDollarsSaved(nov99DollarPctSaved);
    }

    public BigDecimal getNominalPesosPctSaved() {
        return this.nominal.getPesosSaved();
    }

    public void setNominalPesosPctSaved(BigDecimal nominalPesosPctSaved) {
        this.nominal.setPesosSaved(nominalPesosPctSaved);
    }

    public BigDecimal getNominalDollarPctSaved() {
        return this.nominal.getDollarsSaved();
    }

    public void setNominalDollarPctSaved(BigDecimal nominalDollarPctSaved) {
        this.nominal.setDollarsSaved(nominalDollarPctSaved);
    }

}
