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

import java.util.Date;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import org.fede.calculator.money.MoneyAmount;

/**
 *
 * @author fede
 */
public interface MoneyAmountSeries extends Series {

    MoneyAmount getAmount(Date day);

    MoneyAmount getAmount(int year, int month);

    MoneyAmount getAmount(YearMonth moment);

    MoneyAmount getAmountOrElseZero(YearMonth moment);

    void putAmount(int year, int month, MoneyAmount amount);
    
    void putAmount(YearMonth ym, MoneyAmount amount);

    String getCurrency();

    void forEach(BiConsumer<YearMonth, MoneyAmount> consumer);
    
    void forEachNonZero(BiConsumer<YearMonth, MoneyAmount> consumer);
    
    Stream<MoneyAmount> moneyAmountStream();

    MoneyAmountSeries map(BiFunction<YearMonth, MoneyAmount, MoneyAmount> f);
    
    MoneyAmountSeries add(MoneyAmountSeries other);

    MoneyAmountSeries exchangeInto(String currency);
}
