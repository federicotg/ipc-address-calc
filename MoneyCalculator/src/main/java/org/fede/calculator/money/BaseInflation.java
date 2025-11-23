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

import java.time.LocalDate;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.SeriesSupport;
import org.fede.calculator.money.series.SortedMapMoneyAmountSeries;
import java.time.YearMonth;

/**
 *
 * @author fede
 */
abstract class BaseInflation extends SeriesSupport implements Inflation {

    @Override
    public final MoneyAmountSeries adjust(MoneyAmountSeries series, YearMonth reference) {
        final YearMonth maxFrom = this.maximumFrom(series);
        final YearMonth minTo = series.getTo();

        final MoneyAmountSeries answer = new SortedMapMoneyAmountSeries(this.getCurrency(), series.getName());

        for (YearMonth ym = maxFrom; !ym.isAfter(minTo); ym = ym.plusMonths(1)) {
            answer.putAmount(ym, this.adjust(series.getAmount(ym), ym, reference));

        }

        return answer;
    }

    @Override
    public final MoneyAmountSeries adjust(MoneyAmount amount, YearMonth reference) {

        MoneyAmountSeries answer = new SortedMapMoneyAmountSeries(this.getCurrency(), amount.currency().name() + " series");

        for (YearMonth ym = this.getFrom(); !ym.isAfter(this.getTo()); ym = ym.plusMonths(1)) {
            answer.putAmount(ym, this.adjust(amount, reference, ym));

        }

        return answer;

    }

    @Override
    public final MoneyAmount adjust(MoneyAmount amount, LocalDate from, LocalDate to) {
        return this.adjust(amount, YearMonth.from(from), YearMonth.from(to));
    }

}
