/*
 * Copyright (C) 2025 fede
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
package org.fede.calculator.chart;

import java.time.LocalDate;
import java.util.List;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.report.TimeSeriesDatapoint;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeTableXYDataset;

/**
 *
 * @author fede
 */
public class ChartSeriesMapper {

    public static TimeSeries asTimeSeries(List<TimeSeriesDatapoint> series, String name) {
        final TimeSeries ts = new TimeSeries(name);

        series.stream().forEach(dp -> ts.add(day(dp.ym().atEndOfMonth()), dp.value()));
        return ts;
    }

    public static TimeSeries asTimeSeries(MoneyAmountSeries series) {
        final TimeSeries ts = new TimeSeries(series.getName());
        series.forEach((ym, ma) -> ts.add(day(ym.atEndOfMonth()), ma.amount()));
        return ts;
    }

    public static TimeTableXYDataset asTimeTableXYDataset(List<MoneyAmountSeries> series) {
        var dataset = new TimeTableXYDataset();

        for (var s : series) {
            s.forEach((ym, ma) -> dataset.add(day(ym.atEndOfMonth()), ma.amount(), s.getName(), false));
        }

        return dataset;
    }

    private static Day day(LocalDate d) {
        return new Day(d.getDayOfMonth(), d.getMonthValue(), d.getYear());
    }

}
