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

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Currency;
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

    private final ThreadLocal<Calendar> calendar = new ThreadLocal<Calendar>() {

        @Override
        protected Calendar initialValue() {
            return Calendar.getInstance();
        }

    };

    private final Currency currency;

    protected MoneyAmountSeriesSupport(Currency currency) {
        this.currency = currency;
    }

    @Override
    public final MoneyAmount getAmount(Date day) throws NoSeriesDataFoundException {

        Calendar cal = this.calendar.get();
        cal.setTime(day);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        return this.getAmount(year, month + 1);
    }

    @Override
    public final Currency getCurrency() {
        return currency;
    }

    @Override
    public final void forEachNonZero(final MoneyAmountProcessor processor) throws NoSeriesDataFoundException {
        this.forEach(new MoneyAmountProcessor() {
            @Override
            public void process(int year, int month, MoneyAmount amount) throws NoSeriesDataFoundException {

                if (!amount.isZero()) {
                    processor.process(year, month, amount);
                }
            }
        });
    }

    @Override
    public final MoneyAmountSeries exchangeInto(Currency currency) throws NoSeriesDataFoundException {
        return ForeignExchanges.getForeignExchange(this.getCurrency(), currency).exchange(this, currency);
    }

    protected abstract MoneyAmountSeries createNew();

    @Override
    public final MoneyAmountSeries map(final MoneyAmountTransform transform) throws NoSeriesDataFoundException {
        //MoneyAmountSeries answer = new JSONMoneyAmountSeries(this.getCurrency());
        final MoneyAmountSeries answer = this.createNew();

        this.forEach(new MoneyAmountProcessor() {
            @Override
            public void process(int year, int month, MoneyAmount amount) throws NoSeriesDataFoundException {
                answer.putAmount(
                        year,
                        month,
                        transform.transform(year, month, amount));
            }
        });

        /* for (Iterator<Map.Entry<YearMonth, MoneyAmount>> it = this.values.entrySet().iterator(); it.hasNext();) {
            Map.Entry<YearMonth, MoneyAmount> entry = it.next();
            answer.putAmount(
                    entry.getKey().getYear(),
                    entry.getKey().getMonth(),
                    transform.transform(entry.getKey().getYear(), entry.getKey().getMonth(), entry.getValue()));
        }*/
        return answer;
    }

    @Override
    public final MoneyAmountSeries add(final MoneyAmountSeries other) throws NoSeriesDataFoundException {

        if (!other.getCurrency().equals(this.getCurrency())) {
            Currency usd = Currency.getInstance("USD");
            return this.exchangeInto(usd).add(other.exchangeInto(usd));
        }

        if (this.getFrom().compareTo(other.getFrom()) > 0) {
            return other.add(this);
        }

        final YearMonth otherStart = other.getFrom();
        final YearMonth otherEnd = other.getTo();

        final MoneyAmountSeries answer = this.createNew();//new JSONMoneyAmountSeries(this.getCurrency());
        //this empieza antes o son iguales
        this.forEach(new MoneyAmountProcessor() {

            @Override
            public void process(int thisYear, int thisMonth, MoneyAmount amount) throws NoSeriesDataFoundException {
                YearMonth now = new YearMonth(thisYear, thisMonth);
                if (now.compareTo(otherStart) < 0 || now.compareTo(otherEnd) > 0) {
                    answer.putAmount(thisYear, thisMonth, amount);
                } else {
                    answer.putAmount(thisYear, thisMonth, amount.add(other.getAmount(thisYear, thisMonth)));
                }
            }
        });

        // si el otro termina despues tengo que copiar sus valores al resultado.
        final YearMonth thisEnd = this.getTo();

        other.forEach(new MoneyAmountProcessor() {

            @Override
            public void process(int year, int month, MoneyAmount amount) throws NoSeriesDataFoundException {
                final YearMonth otherNow = new YearMonth(year, month);
                if (otherNow.compareTo(thisEnd) > 0) {
                    answer.putAmount(year, month, amount);
                }
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

            this.forEach(new MoneyAmountProcessor() {
                @Override
                public void process(int year, int month, MoneyAmount amount) throws NoSeriesDataFoundException {
                    holder[0] &= amount.equals(other.getAmount(year, month));
                }
            });

            /*for (Iterator<Map.Entry<YearMonth, MoneyAmount>> it = this.values.entrySet().iterator(); it.hasNext();) {
                Map.Entry<YearMonth, MoneyAmount> e = it.next();
                equal &= e.getValue().equals(other.getAmount(e.getKey().getYear(), e.getKey().getMonth()));
            }*/
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
            this.forEach(new MoneyAmountProcessor() {
                @Override
                public void process(int year, int month, MoneyAmount amount) throws NoSeriesDataFoundException {
                    holder[0] += 37 * Objects.hashCode(amount);
                }
            });
        } catch (NoSeriesDataFoundException ex) {
            
        }
        
        return 37 * hash + holder[0];
    }
}
