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

import java.math.BigDecimal;
import org.fede.calculator.money.NoSeriesDataFoundException;

/**
 *
 * @author fede
 */
public class ArgentinaCompoundCPISeries extends IndexSeriesSupport {

    private final IndexSeries cqpSeries = SeriesReader.readIndexSeries("index/cqp.json");
    private final IndexSeries indecSeries = SeriesReader.readIndexSeries("index/indec.json");
    private static final BigDecimal CORRECTION_FACTOR = new BigDecimal("1.84856478083063");

    @Override
    public BigDecimal getIndex(int year, int month) {

        if (year < 2006 || (year == 2006 && month < 12)) {
            return indecSeries.getIndex(year, month);
        } else {
            return this.cqpSeries.getIndex(year, month).multiply(CORRECTION_FACTOR);
        }
    }

    @Override
    public BigDecimal predictValue(int year, int month) {
        if (year < 2006 || (year == 2006 && month < 12)) {
            throw new NoSeriesDataFoundException("Can't predict INDEC");
        }
       // System.out.println("Arg Prediction "+year+" / "+month);
        return this.cqpSeries.predictValue(year, month);
    }

    @Override
    public YearMonth getFrom() {
        return this.indecSeries.getFrom();
    }

    @Override
    public YearMonth getTo() {
        return this.cqpSeries.getTo();
    }

}
