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
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import org.fede.calculator.money.Currency;
import org.fede.calculator.money.ForeignExchanges;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.NoSeriesDataFoundException;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
public abstract class MoneyAmountSeriesSupport extends SeriesSupport implements MoneyAmountSeries {

    private final Currency currency;
    private String name;

    protected MoneyAmountSeriesSupport(Currency currency, String name) {
        this.currency = currency;
        this.name = name;
    }

    @Override
    public final MoneyAmount getAmount(LocalDate day) {
        return this.getAmount(YearMonth.from(day));
    }

    @Override
    public final Currency getCurrency() {
        return currency;
    }

    @Override
    public final void forEachNonZero(BiConsumer<YearMonth, MoneyAmount> consumer) {
        this.forEach((ym, amount) -> {
            if (!amount.isZero()) {
                consumer.accept(ym, amount);
            }
        });
    }

    @Override
    public final MoneyAmountSeries exchangeInto(Currency currency) {
        return ForeignExchanges.getForeignExchange(this.getCurrency(), currency)
                .exchange(this, currency);
    }

    protected abstract MoneyAmountSeries createNew();

    @Override
    public final MoneyAmountSeries map(BiFunction<YearMonth, MoneyAmount, MoneyAmount> f) {
        MoneyAmountSeries newSeries = this.createNew();

        this.forEach((ym, amount) -> {
            newSeries.putAmount(ym, f.apply(ym, amount));
        });
        return newSeries;
    }

    @Override
    public final MoneyAmountSeries add(final MoneyAmountSeries other) {

        if (other.getCurrency() != this.getCurrency()) {
            return this.exchangeInto(Currency.USD)
                    .add(other.exchangeInto(Currency.USD));
        }

        if (this.getFrom().compareTo(other.getFrom()) > 0) {
            return other.add(this);
        }

        final YearMonth minStart = YearMonthUtil.min(this.getFrom(), other.getFrom());
        final YearMonth maxEnd = YearMonthUtil.max(this.getTo(), other.getTo());

        final MoneyAmountSeries answer = this.createNew();

        for (var ym = minStart; ym.compareTo(maxEnd) <= 0; ym = ym.plusMonths(1l)) {

            answer.putAmount(
                    ym,
                    this.getAmountOrElseZero(ym)
                            .add(other.getAmountOrElseZero(ym)));

        }
        return answer;
    }

    protected abstract MoneyAmount getAmountOrNull(YearMonth ym);

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

        var current = this.getFrom();
        while (current.compareTo(this.getTo()) <= 0) {
            if (!Objects.equals(this.getAmount(current), other.getAmount(current))) {
                return false;
            }
            current = current.plusMonths(1);
        }
        return true;
    }

    @Override
    public final int hashCode() {

        var current = this.getFrom();
        int valueHash = 13;
        while (current.compareTo(this.getTo()) <= 0) {
            valueHash += 37 * Objects.hash(this.getAmount(current), current);
            current = current.plusMonths(1);
        }

        return 37 * Objects.hash(this.getCurrency(), this.getFrom(), this.getTo()) + valueHash;
    }

    @Override
    public final MoneyAmount getAmount(YearMonth moment) {
        MoneyAmount answer = this.getAmountOrNull(moment);
        if (answer == null) {
            throw new NoSeriesDataFoundException(MessageFormat.format("No data specified for year {0} and month {1}.", moment.getYear(), moment.getMonth()));
        }
        return answer;
    }

    @Override
    public final MoneyAmount getAmountOrElseZero(YearMonth moment) {
        if (this.hasValue(moment)) {
            return this.getAmount(moment);
        }

        return MoneyAmount.zero(this.currency);

    }

    protected abstract boolean hasValue(YearMonth moment);

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

}
