package org.fede.calculator;

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
import java.math.BigDecimal;
import java.util.Date;
import org.fede.calculator.money.series.IndexSeries;
import org.fede.calculator.money.NoSeriesDataFoundException;
import org.fede.calculator.money.NoSeriesDataFoundException;
import org.fede.calculator.money.series.Series;
import org.fede.calculator.money.series.YearMonth;

/**
 *
 * @author fede
 */
public class MockSeries implements IndexSeries{

    private int calls = 0;
    @Override
    public BigDecimal getIndex(Date day) throws NoSeriesDataFoundException {
        this.calls++;
        return BigDecimal.ONE;
    }

    @Override
    public BigDecimal getIndex(int year, int month) throws NoSeriesDataFoundException {
        this.calls++;
        return BigDecimal.ONE;
    }

    public int getCalls(){
        return this.calls;
    }

    @Override
    public YearMonth minimumTo(Series other) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public YearMonth maximumFrom(Series other) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public YearMonth getFrom() {
        return new YearMonth(1900, 1);
    }

    @Override
    public YearMonth getTo() {
        return new YearMonth(4000, 12);
    }

    @Override
    public BigDecimal predictValue(int year, int month) throws NoSeriesDataFoundException {
        return BigDecimal.ZERO;
    }
    
}
