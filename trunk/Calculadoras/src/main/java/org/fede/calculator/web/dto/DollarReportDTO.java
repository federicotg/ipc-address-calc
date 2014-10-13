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
import java.util.Date;

/**
 *
 * @author fede
 */
public class DollarReportDTO {

    private Date then;
    private Date now;
    
    
    private BigDecimal usd;
    
    private BigDecimal nominalPesosThen;
    private BigDecimal realPesosNow;
    private BigDecimal nominalPesosNow;
    

    public BigDecimal getUsd() {
        return usd;
    }

    public void setUsd(BigDecimal usd) {
        this.usd = usd;
    }

    public Date getThen() {
        return then;
    }

    public void setThen(Date then) {
        this.then = then;
    }

    public Date getNow() {
        return now;
    }

    public void setNow(Date now) {
        this.now = now;
    }

    public BigDecimal getNominalPesosThen() {
        return nominalPesosThen;
    }

    public void setNominalPesosThen(BigDecimal nominalPesosThen) {
        this.nominalPesosThen = nominalPesosThen;
    }

    public BigDecimal getRealPesosNow() {
        return realPesosNow;
    }

    public void setRealPesosNow(BigDecimal realPesosNow) {
        this.realPesosNow = realPesosNow;
    }

    public BigDecimal getNominalPesosNow() {
        return nominalPesosNow;
    }

    public void setNominalPesosNow(BigDecimal nominalPesosNow) {
        this.nominalPesosNow = nominalPesosNow;
    }




}
