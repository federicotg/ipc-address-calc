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

import org.fede.calculator.money.series.DollarCPISeries;
import org.fede.calculator.money.series.IndexSeries;
import org.fede.calculator.money.series.CachedSeries;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import org.fede.calculator.money.series.JSONIndexSeries;

/**
 *
 * @author fede
 */
public class DollarBasedARSInflation implements Inflation {

    private final IndexSeries forex = new JSONIndexSeries("peso-dolar-libre.json");
    private final Inflation inflation = new CPIInflation(new CachedSeries(new DollarCPISeries()), Currency.getInstance("USD"));

    @Override
    public MoneyAmount adjust(MoneyAmount amount, int fromYear, int toYear) throws NoIndexDataFoundException {
        amount.assertCurrency(Currency.getInstance("ARS"));
        final BigDecimal arsToUsdFactor = BigDecimal.ONE.setScale(5).divide(forex.getIndex(fromYear), RoundingMode.HALF_UP);
        final BigDecimal usdToArsFactor = forex.getIndex(toYear);

        MoneyAmount dollars = amount.exchange(Currency.getInstance("USD"), arsToUsdFactor);
        dollars = inflation.adjust(dollars, fromYear, toYear);
        return dollars.exchange(Currency.getInstance("ARS"), usdToArsFactor);
    }

    @Override
    public MoneyAmount adjust(MoneyAmount amount, int fromYear, int fromMonth, int toYear, int toMonth) throws NoIndexDataFoundException {
        amount.assertCurrency(Currency.getInstance("ARS"));

        final BigDecimal arsToUsdFactor = BigDecimal.ONE.setScale(5).divide(forex.getIndex(fromYear, fromMonth), RoundingMode.HALF_UP);
        final BigDecimal usdToArsFactor = forex.getIndex(toYear, toMonth);

        MoneyAmount dollars = amount.exchange(Currency.getInstance("USD"), arsToUsdFactor);
        dollars = inflation.adjust(dollars, fromYear, fromMonth, toYear, toMonth);
        return dollars.exchange(Currency.getInstance("ARS"), usdToArsFactor);
    }

    @Override
    public int getFromYear() {
        return this.forex.getFromYear();
    }

    @Override
    public int getToYear() {
        return this.forex.getToYear();
    }

    @Override
    public Currency getCurrency() {
        return Currency.getInstance("ARS");
    }

}
