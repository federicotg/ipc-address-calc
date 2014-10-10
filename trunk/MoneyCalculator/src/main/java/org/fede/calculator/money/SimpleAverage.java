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
package org.fede.calculator.money;

import java.math.BigDecimal;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.MoneyAmountTransform;

/**
 *
 * @author fede
 */
public class SimpleAverage implements Average, MathConstants {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(SCALE, ROUNDING_MODE);
    
    private final int months;

    public SimpleAverage(int months) {
        this.months = months;
    }

    private MoneyAmount avg(MoneyAmount lastValues[]) {
        int total = 0;
        BigDecimal sum = ZERO;
        for (MoneyAmount lastValue : lastValues) {
            if (lastValue != null) {
                total++;
                sum = sum.add(lastValue.getAmount());
            }
        }
        return new MoneyAmount(sum.divide(new BigDecimal(total), CONTEXT), lastValues[0].getCurrency());
    }

    @Override
    public MoneyAmountSeries average(MoneyAmountSeries series) {
        if(this.months == 1){
            return series;
        }
        final MoneyAmount lastValues[] = new MoneyAmount[this.months];
        final int index[] = new int[]{0};

        return series.map(new MoneyAmountTransform() {

            @Override
            public MoneyAmount transform(int year, int month, MoneyAmount amount) {
                lastValues[index[0]] = amount;
                index[0] = (index[0] + 1) % months;
                return avg(lastValues);
            }
        });
    }

}
