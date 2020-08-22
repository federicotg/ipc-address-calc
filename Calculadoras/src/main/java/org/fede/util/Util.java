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
package org.fede.util;

import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.joining;

import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.SeriesReader;
import org.fede.calculator.web.dto.ExpenseChartSeriesDTO;

/**
 *
 * @author fede
 */
public class Util {

    public static <T> String list(Collection<T> elements) {
        return list(elements, ", ");
    }

    public static <T> String list(Collection<T> elements, String separator) {
        return elements.stream().map(T::toString).collect(joining(separator));
    }

    public static MoneyAmountSeries sumSeries(String currency, List<ExpenseChartSeriesDTO> dtos) {
        return sumSeries(currency, dtos.stream().map(ExpenseChartSeriesDTO::getSeriesName).toArray(String[]::new));
    }

    public static MoneyAmountSeries sumSeries(List<ExpenseChartSeriesDTO> dtos) {
        return sumSeries("USD", dtos);
    }

    public static MoneyAmountSeries sumSeries(String currency, String... names) {
        if (names.length == 0) {
            throw new IllegalArgumentException("You must at least read one series");
        }
        MoneyAmountSeries answer = null;
        for (String seriesName : names) {
            if (seriesName != null && seriesName.length() > 0) {
                MoneyAmountSeries s = SeriesReader.readSeries(seriesName).exchangeInto(currency);
                answer = answer == null ? s : answer.add(s);
            }
        }
        return answer;
    }


}
