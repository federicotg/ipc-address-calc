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
import org.fede.calculator.money.NoSeriesDataFoundException;
import org.fede.calculator.money.bls.BlsCPISource;
import static org.fede.calculator.money.bls.BlsCPISource.CPI_SERIES_ID;
import org.fede.calculator.money.bls.BlsCpiDataPoint;
import org.fede.calculator.money.bls.CachingBlsSource;
import org.fede.calculator.money.bls.JSONBlsCPISource;

/**
 *
 * @author fede
 */
public final class DollarCPISeries extends IndexSeriesSupport {

    private static final YearMonth FROM = new YearMonth(1913, 1);

    private final BlsCPISource source;

    public DollarCPISeries() {
        this(new CachingBlsSource(new JSONBlsCPISource("bls.json")));
    }

    public DollarCPISeries(BlsCPISource source) {
        this.source = source;
    }

    @Override
    public BigDecimal getIndex(int year, int month) throws NoSeriesDataFoundException {
        try {
            BlsResponse blsResponse = this.source.getResponse(year);
            if (blsResponse == null || !blsResponse.isValid()) {
                return this.predictValue(year, month);
            }
            BlsCpiDataPoint dp = blsResponse.getDataPoint(CPI_SERIES_ID, year, month);
            if(dp == null){
                return this.predictValue(year, month);
            }
            return dp.getValue();

        } catch (IOException ex) {
            throw new NoSeriesDataFoundException(ex);
        }
    }

    @Override
    public YearMonth getFrom() {
        return FROM;
    }

    @Override
    public YearMonth getTo() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        try {

            BlsResponse resp = this.source.getResponse(year);
            while (resp == null) {
                year = year - 1;
                resp = this.source.getResponse(year);
            }

            return new YearMonth(year, resp.getLastAvailableMonth(CPI_SERIES_ID));
        } catch (NoSeriesDataFoundException | IOException ex) {
            return new YearMonth(year - 1, 11);
        }
    }

    @Override
    public BigDecimal predictValue(int year, int month) throws NoSeriesDataFoundException {
        if (year < 1913) {
            throw new NoSeriesDataFoundException("No data for specified year and month.");
        }
        return new LinearFutureValue().predictValue(this, year, month);
    }

}
