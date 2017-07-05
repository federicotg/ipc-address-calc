/*
 * Copyright (C) 2015 fede
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
package org.fede.calculator.service;

import java.util.List;
import java.util.function.BiConsumer;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.series.YearMonth;
import org.fede.calculator.web.dto.CanvasJSDatapointDTO;

/**
 *
 * @author fede
 */
public class CanvasJSMoneyAmountProcessor implements BiConsumer<YearMonth, MoneyAmount> {

    private final List<CanvasJSDatapointDTO> datapoints;

    public CanvasJSMoneyAmountProcessor(List<CanvasJSDatapointDTO> datapoints) {
        this.datapoints = datapoints;
    }

    @Override
    public void accept(YearMonth yearMonth, MoneyAmount amount) {
        datapoints.add(new CanvasJSDatapointDTO(
                "date-"
                        .concat(String.valueOf(yearMonth.getYear()))
                        .concat("-")
                        .concat(String.valueOf(yearMonth.getMonth() - 1))
                        .concat("-15"), 
                amount.getAmount()));
    }
}
