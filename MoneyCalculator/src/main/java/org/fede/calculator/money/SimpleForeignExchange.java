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
import java.util.Date;
import java.util.Objects;
import java.util.function.Supplier;
import org.fede.calculator.money.series.IndexSeries;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.SeriesSupport;
import org.fede.calculator.money.series.SortedMapMoneyAmountSeries;
import org.fede.calculator.money.series.YearMonth;

/**
 *
 * @author fede
 */
public class SimpleForeignExchange extends SeriesSupport implements ForeignExchange {

    private final Supplier<IndexSeries> exchangeRatesSeriesSupplier;

    private IndexSeries exchangeRatesSeries;

    private final Currency fromCurrency;

    private final Currency targetCurrency;

    public SimpleForeignExchange(Supplier<IndexSeries> exchangeRatesSeriesSupplier,
            Currency fromCurrency,
            Currency targetCurrency) {
        this.exchangeRatesSeriesSupplier = exchangeRatesSeriesSupplier;
        this.fromCurrency = fromCurrency;
        this.targetCurrency = targetCurrency;
    }

    private IndexSeries getSeries() {
        if (this.exchangeRatesSeries == null) {
            this.exchangeRatesSeries = this.exchangeRatesSeriesSupplier.get();
        }
        return this.exchangeRatesSeries;
    }

    @Override
    public YearMonth getFrom() {
        return this.getSeries().getFrom();
    }

    @Override
    public YearMonth getTo() {
        return this.getSeries().getTo();
    }

    @Override
    public MoneyAmount exchange(MoneyAmount amount, Currency targetCurrency, int year, int month) {
        if (amount.getCurrency().equals(targetCurrency)) {
            return amount;
        }
        if (this.targetCurrency.equals(targetCurrency)) {
            return amount.exchange(targetCurrency, getSeries().getIndex(year, month));
        }

        if (this.fromCurrency.equals(targetCurrency)) {
            return amount.exchange(targetCurrency, BigDecimal.ONE.divide(getSeries().getIndex(year, month), MathConstants.C));
        }

        throw new IllegalArgumentException("Unknown currency.");
    }

    @Override
    public MoneyAmountSeries exchange(MoneyAmountSeries series, Currency targetCurrency) {

        YearMonth from = this.getSeries().maximumFrom(series);
        YearMonth to = series.getTo();

        if (from.compareTo(to) > 0) {
            throw new IllegalArgumentException("From cannot be after to.");
        }

        final int fromYear = from.getYear();
        final int fromMonth = from.getMonth();
        final int toYear = to.getYear();
        final int toMonth = to.getMonth();

        final MoneyAmountSeries answer = new SortedMapMoneyAmountSeries(targetCurrency, series.getName());

        for (int m = fromMonth; m <= (fromYear == toYear ? toMonth : 12); m++) {
            //first year
            answer.putAmount(fromYear, m, this.exchange(series.getAmount(fromYear, m), targetCurrency, fromYear, m));
        }
        for (int y = fromYear + 1; y < toYear; y++) {
            for (int m = 1; m <= 12; m++) {
                answer.putAmount(y, m, this.exchange(series.getAmount(y, m), targetCurrency, y, m));
            }
        }
        if (fromYear != toYear) {
            for (int m = 1; m <= toMonth; m++) {
                //last year
                answer.putAmount(toYear, m, this.exchange(series.getAmount(toYear, m), targetCurrency, toYear, m));
            }
        }
        return answer;

    }

    @Override
    public MoneyAmount exchange(MoneyAmount amount, Currency targetCurrency, Date moment) {
        final var ym = YearMonth.of(moment);      
        return this.exchange(amount, targetCurrency, ym.getYear(), ym.getMonth());
    }

    @Override
    public MoneyAmountSeries exchange(MoneyAmount amount, Currency targetCurrency) {

        final int fromYear = this.getFrom().getYear();
        final int fromMonth = this.getFrom().getMonth();
        final int toYear = this.getTo().getYear();
        final int toMonth = this.getTo().getMonth();

        final MoneyAmountSeries answer = new SortedMapMoneyAmountSeries(targetCurrency, targetCurrency.name() + " series");

        for (int m = fromMonth; m <= 12; m++) {
            //first year
            answer.putAmount(fromYear, m, this.exchange(amount, targetCurrency, fromYear, m));
        }
        for (int y = fromYear + 1; y < toYear; y++) {
            for (int m = 1; m <= 12; m++) {
                answer.putAmount(y, m, this.exchange(amount, targetCurrency, y, m));
            }
        }
        for (int m = 1; m <= toMonth; m++) {
            //last year
            answer.putAmount(toYear, m, this.exchange(amount, targetCurrency, toYear, m));
        }
        return answer;

    }

    @Override
    public int hashCode() {
        return Objects.hash(this.fromCurrency, this.targetCurrency, this.getSeries());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SimpleForeignExchange other
                && Objects.equals(this.fromCurrency, other.fromCurrency)
                && Objects.equals(this.targetCurrency, other.targetCurrency)
                && Objects.equals(this.getSeries(), other.getSeries());
    }

    @Override
    public Currency getTargetCurrency() {
        return targetCurrency;
    }

    @Override
    public Currency getSourceCurrency() {
        return this.fromCurrency;
    }

}
