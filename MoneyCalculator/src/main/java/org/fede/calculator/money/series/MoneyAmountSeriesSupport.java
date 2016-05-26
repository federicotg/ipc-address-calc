/*
 * Copyright (C) 2016 Federico Tello Gentile <federicotg@gmail.com>
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
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import org.fede.calculator.money.ForeignExchanges;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.NoSeriesDataFoundException;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
public abstract class MoneyAmountSeriesSupport extends SeriesSupport implements MoneyAmountSeries {

    private final String currency;

    protected MoneyAmountSeriesSupport(String currency) {
        this.currency = currency;
    }

    @Override
    public final MoneyAmount getAmount(Date day) throws NoSeriesDataFoundException {

        Calendar cal = Calendar.getInstance();
        cal.setTime(day);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        return this.getAmount(year, month + 1);
    }

    @Override
    public final String getCurrency() {
        return currency;
    }

    @Override
    public final void forEachNonZero(final MoneyAmountProcessor processor) throws NoSeriesDataFoundException {
        this.forEach((int year, int month, MoneyAmount amount) -> {
            if (!amount.isZero()) {
                processor.process(year, month, amount);
            }
        });
    }

    @Override
    public final MoneyAmountSeries exchangeInto(String currency) throws NoSeriesDataFoundException {
        return ForeignExchanges.getForeignExchange(this.getCurrency(), currency).exchange(this, currency);
    }

    protected abstract MoneyAmountSeries createNew();

    @Override
    public final MoneyAmountSeries map(final MoneyAmountTransform transform) throws NoSeriesDataFoundException {
        final MoneyAmountSeries answer = this.createNew();

        this.forEach((int year, int month, MoneyAmount amount) -> {
            answer.putAmount(
                    year,
                    month,
                    transform.transform(year, month, amount));
        });

        return answer;
    }

    @Override
    public final MoneyAmountSeries add(final MoneyAmountSeries other) throws NoSeriesDataFoundException {

        if (!other.getCurrency().equals(this.getCurrency())) {
            String usd = "USD";
            return this.exchangeInto(usd).add(other.exchangeInto(usd));
        }

        if (this.getFrom().compareTo(other.getFrom()) > 0) {
            return other.add(this);
        }

        final YearMonth otherStart = other.getFrom();
        final YearMonth otherEnd = other.getTo();

        final MoneyAmountSeries answer = this.createNew();
        //this empieza antes o son iguales
        this.forEach((int thisYear, int thisMonth, MoneyAmount amount) -> {
            YearMonth now = new YearMonth(thisYear, thisMonth);
            if (now.compareTo(otherStart) < 0 || now.compareTo(otherEnd) > 0) {
                answer.putAmount(thisYear, thisMonth, amount);
            } else {
                answer.putAmount(thisYear, thisMonth, amount.add(other.getAmount(thisYear, thisMonth)));
            }
        });

        // si el otro termina después tengo que copiar sus valores al resultado.
        final YearMonth thisEnd = this.getTo();

        other.forEach((int year, int month, MoneyAmount amount) -> {
            final YearMonth otherNow = new YearMonth(year, month);
            if (otherNow.compareTo(thisEnd) > 0) {
                answer.putAmount(year, month, amount);
            }
        });

        return answer;
    }

    protected abstract MoneyAmount getAmountOrNull(int year, int month);

    @Override
    public final MoneyAmount getAmount(int year, int month) throws NoSeriesDataFoundException {
        MoneyAmount answer = this.getAmountOrNull(year, month);
        if (answer == null) {
            throw new NoSeriesDataFoundException(MessageFormat.format("No data specified for year {0} and month {1}.", year, month));
        }
        return answer;
    }

    @Override
    public final boolean equals(Object obj) {
        if (!(obj instanceof MoneyAmountSeries)) {
            return false;
        }
        final MoneyAmountSeries other = (MoneyAmountSeries) obj;
        boolean equal = this.getCurrency().equals(other.getCurrency())
                && Objects.equals(this.getFrom(), other.getFrom())
                && Objects.equals(this.getTo(), other.getTo());
        if (!equal) {
            return false;
        }
        final boolean[] holder = new boolean[]{true};
        try {

            this.forEach((int year, int month, MoneyAmount amount) -> {
                holder[0] &= amount.equals(other.getAmount(year, month));
            });

            return holder[0];
        } catch (NoSeriesDataFoundException ex) {

            System.err.println("Not eq 2");

            return false;
        }

    }

    @Override
    public final int hashCode() {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(this.getCurrency());
        hash = 37 * hash + Objects.hashCode(this.getFrom());
        hash = 37 * hash + Objects.hashCode(this.getTo());

        final int[] holder = new int[]{3};

        try {
            this.forEach((int year, int month, MoneyAmount amount) -> {
                holder[0] += 37 * Objects.hashCode(amount);
            });
        } catch (NoSeriesDataFoundException ex) {

        }

        return 37 * hash + holder[0];
    }

    @Override
    public final MoneyAmount getAmount(YearMonth moment) throws NoSeriesDataFoundException {
        return this.getAmount(moment.getYear(), moment.getMonth());
    }

    @Override
    public final MoneyAmount getAmountOrElseZero(YearMonth moment) throws NoSeriesDataFoundException {
        if (this.hasValue(moment)) {
            return this.getAmount(moment);
        }
        return new MoneyAmount(BigDecimal.ZERO, this.currency);
    }

    protected abstract boolean hasValue(YearMonth moment);
}
