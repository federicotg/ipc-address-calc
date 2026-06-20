/*
 * Copyright (C) 2026 fede
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
package org.fede.calculator.report;

import java.time.YearMonth;
import java.util.EnumMap;
import java.util.Map;
import org.fede.calculator.money.Currency;
import org.fede.calculator.money.ForeignExchanges;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.series.SeriesReader;

/**
 *
 * @author fede
 */
public class LastAmounts {

    private static final Map<Currency, String> SERIES = new EnumMap<>(Map.of(
            Currency.CSPX, "ahorros-cspx",
            Currency.EIMI, "ahorros-eimi",
            Currency.XRSU, "ahorros-xrsu",
            Currency.RTWO, "ahorros-rtwo",
            Currency.MEUD, "ahorros-meud",
            Currency.XUSE, "ahorros-xuse"
    ));

    private final Currency currency;

    public LastAmounts(Currency currency) {
        this.currency = currency;
    }

    public LastAmounts() {
        this(Currency.USD);
    }

    public MoneyAmount lastAmount(Currency c) {
        return this.lastAmount(SERIES.get(c), YearMonth.now());
    }

    private MoneyAmount lastAmount(String seriesName, YearMonth ym) {
        var amount = last(seriesName, ym);
        return ForeignExchanges.getMoneyAmountForeignExchange(amount.currency(), this.currency)
                .apply(amount, ym);
    }

    public Equity last() {

        return new Equity(
                this.lastAmount(Currency.CSPX)
                        .add(this.lastAmount(Currency.RTWO))
                        .add(this.lastAmount(Currency.XRSU)),
                this.lastAmount(Currency.MEUD)
                        .add(this.lastAmount(Currency.XUSE)),
                this.lastAmount(Currency.EIMI));

    }

    public static MoneyAmount last(String seriesName, YearMonth ym) {
        return SeriesReader.readSeries("saving/" + seriesName + ".json").getAmountOrElseZero(ym);

    }

    public static MoneyAmount lastUSD(String seriesName, YearMonth ym) {
        var amount = last(seriesName, ym);
        return ForeignExchanges.getMoneyAmountForeignExchange(amount.currency(), Currency.USD)
                .apply(amount, ym);
    }

}
