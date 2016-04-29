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
import org.fede.calculator.money.Inflation;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.NoSeriesDataFoundException;

/**
 *
 * @author fede
 */
public enum InterpolationStrategy {

    NO_INTERPOLATION {
        @Override
        public MoneyAmount interpolate(MoneyAmount lastValue, YearMonth lastValueYearMonth, String currency) throws NoSeriesDataFoundException {
            throw new NoSeriesDataFoundException("No value for specified year and month.");
        }
    }, LAST_VALUE_INTERPOLATION {
        @Override
        public MoneyAmount interpolate(MoneyAmount lastValue, YearMonth lastValueYearMonth, String currency) throws NoSeriesDataFoundException {
            return lastValue;
        }
    }, ZERO_VALUE_INTERPOLATION {
        @Override
        public MoneyAmount interpolate(MoneyAmount lastValue, YearMonth lastValueYearMonth, String currency) throws NoSeriesDataFoundException {
            return new MoneyAmount(BigDecimal.ZERO, currency);
        }
    }, ARS_INFLATION_INTERPOLATION {
        @Override
        public MoneyAmount interpolate(MoneyAmount lastValue, YearMonth lastValueYearMonth, String currency) throws NoSeriesDataFoundException {
            return inflationInterpolation(Inflation.ARS_INFLATION, lastValue, lastValueYearMonth, currency);
        }
    }, USD_INFLATION_INTERPOLATION {
        @Override
        public MoneyAmount interpolate(MoneyAmount lastValue, YearMonth lastValueYearMonth, String currency) throws NoSeriesDataFoundException {
            return inflationInterpolation(Inflation.USD_INFLATION, lastValue, lastValueYearMonth, currency);
        }
    };

    public abstract MoneyAmount interpolate(MoneyAmount lastValue, YearMonth lastValueYearMonth, String currency) throws NoSeriesDataFoundException;

    protected MoneyAmount inflationInterpolation(Inflation inflation, MoneyAmount lastValue, YearMonth lastValueYearMonth, String currency) throws NoSeriesDataFoundException {
        YearMonth nextYm = lastValueYearMonth.next();
        return inflation.adjust(
                lastValue, lastValueYearMonth.getYear(),
                lastValueYearMonth.getMonth(),
                nextYm.getYear(),
                nextYm.getMonth());
    }

}
