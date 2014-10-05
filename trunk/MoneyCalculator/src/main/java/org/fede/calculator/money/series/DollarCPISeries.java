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
package org.fede.calculator.money.series;

import org.fede.calculator.money.bls.BlsResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import org.fede.calculator.money.NoIndexDataFoundException;
import org.fede.calculator.money.bls.BlsCPISource;
import static org.fede.calculator.money.bls.BlsCPISource.CPI_SERIES_ID;
import org.fede.calculator.money.bls.CachingBlsSource;
import org.fede.calculator.money.bls.URLConnectionBlsCPISource;

/**
 *
 * @author fede
 */
public final class DollarCPISeries extends IndexSeriesSupport {

    private final BlsCPISource source;

    public DollarCPISeries() {
        this(new CachingBlsSource(new URLConnectionBlsCPISource()));
    }

    public DollarCPISeries(BlsCPISource source) {
        this.source = source;
    }

    @Override
    public BigDecimal getIndex(int year, int month) throws NoIndexDataFoundException {
        try {
            BlsResponse blsResponse = this.source.getResponse(year);
            return blsResponse.getDataPoint(CPI_SERIES_ID, year, month).getValue();

        } catch (IOException ex) {
            throw new NoIndexDataFoundException(ex);
        }
    }

    @Override
    public BigDecimal getIndex(int year) throws NoIndexDataFoundException {
        try {
            BlsResponse blsResponse = this.source.getResponse(year);
            return blsResponse.getDataPoint(CPI_SERIES_ID, year).getValue();

        } catch (IOException ex) {
            throw new NoIndexDataFoundException(ex);
        }
    }

    @Override
    public int getFromYear() {
        return 1913;
    }

    @Override
    public int getToYear() {
        Calendar lastMonth = Calendar.getInstance();
        lastMonth.add(Calendar.MONTH, -1);
        return lastMonth.get(Calendar.YEAR);
    }

}
