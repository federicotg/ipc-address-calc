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
import java.time.LocalDate;
import java.util.function.Supplier;
import org.fede.calculator.money.series.IndexSeries;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.SeriesSupport;
import org.fede.calculator.money.series.SortedMapMoneyAmountSeries;
import java.time.YearMonth;

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
    public MoneyAmount exchange(MoneyAmount amount, Currency targetCurrency, YearMonth ym) {
        if (amount.currency() == targetCurrency) {
            return amount;
        }
        if (this.targetCurrency == targetCurrency) {
            return amount.exchange(targetCurrency, getSeries().getIndex(ym));
        }

        if (this.fromCurrency == targetCurrency) {
            return amount.exchange(targetCurrency, BigDecimal.ONE.divide(getSeries().getIndex(ym), MathConstants.C));
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

        final MoneyAmountSeries answer = new SortedMapMoneyAmountSeries(targetCurrency, series.getName());

        for (YearMonth ym = from; !ym.isAfter(to); ym = ym.plusMonths(1)) {
            answer.putAmount(ym, this.exchange(series.getAmount(ym), targetCurrency, ym));

        }

        return answer;

    }

    @Override
    public MoneyAmount exchange(MoneyAmount amount, Currency targetCurrency, LocalDate moment) {
        return this.exchange(amount, targetCurrency, YearMonth.from(moment));
    }

    @Override
    public MoneyAmountSeries exchange(MoneyAmount amount, Currency targetCurrency) {

        final MoneyAmountSeries answer = new SortedMapMoneyAmountSeries(targetCurrency, targetCurrency.name() + " series");

        for (YearMonth ym = this.getFrom(); !ym.isAfter(this.getTo()); ym = ym.plusMonths(1)) {
            answer.putAmount(ym, this.exchange(amount, targetCurrency, ym));
        }

        return answer;

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
