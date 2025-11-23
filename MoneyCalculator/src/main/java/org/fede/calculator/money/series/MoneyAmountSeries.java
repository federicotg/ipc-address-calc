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

import static java.math.BigDecimal.ONE;
import java.time.YearMonth;
import java.time.LocalDate;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.stream.Stream;
import org.fede.calculator.money.Currency;
import org.fede.calculator.money.MoneyAmount;

/**
 *
 * @author fede
 */
public interface MoneyAmountSeries extends Series {

    MoneyAmount getAmount(LocalDate day);

    MoneyAmount getAmount(YearMonth moment);

    MoneyAmount getAmountOrElseZero(YearMonth moment);

    default void putAmount(int year, int month, MoneyAmount amount) {
        this.putAmount(YearMonth.of(year, month), amount);
    }

    void putAmount(YearMonth ym, MoneyAmount amount);

    Currency getCurrency();

    void forEach(BiConsumer<YearMonth, MoneyAmount> consumer);

    void forEachNonZero(BiConsumer<YearMonth, MoneyAmount> consumer);

    Stream<MoneyAmount> moneyAmountStream();

    Stream<YearMonth> yearMonthStream();
    
    Stream<MoneyAmountItem> items();

    MoneyAmountSeries map(BiFunction<YearMonth, MoneyAmount, MoneyAmount> f);

    Stream<MoneyAmount> filter(BiPredicate<YearMonth, MoneyAmount> predicate);

    MoneyAmountSeries add(MoneyAmountSeries other);

    default MoneyAmountSeries subtract(MoneyAmountSeries other) {
        final var negativeFactor = ONE.negate();
        return this.add(other.map((ym, ma) -> ma.adjust(ONE, negativeFactor)));
    }

    MoneyAmountSeries exchangeInto(Currency currency);

    String getName();

    void setName(String name);

}
