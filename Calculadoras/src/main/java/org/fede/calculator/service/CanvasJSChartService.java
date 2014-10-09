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
package org.fede.calculator.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.fede.calculator.money.ForeignExchange;
import org.fede.calculator.money.Inflation;
import static org.fede.calculator.money.Inflation.ARS_INFLATION;
import static org.fede.calculator.money.Inflation.USD_INFLATION;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.NoSeriesDataFoundException;
import org.fede.calculator.web.dto.CanvasJSAxisDTO;
import org.fede.calculator.web.dto.CanvasJSChartDTO;
import org.fede.calculator.web.dto.CanvasJSDatapointDTO;
import org.fede.calculator.web.dto.CanvasJSDatumDTO;
import org.fede.calculator.web.dto.CanvasJSTitleDTO;
import org.springframework.stereotype.Service;

/**
 *
 * @author fede
 */
@Service
public class CanvasJSChartService implements ChartService {

    private static final Map<Integer, String> MONTH_NAMES = new HashMap<>();

    static {
        MONTH_NAMES.put(1, "enero");
        MONTH_NAMES.put(2, "febrero");
        MONTH_NAMES.put(3, "marzo");
        MONTH_NAMES.put(4, "abril");
        MONTH_NAMES.put(5, "mayo");
        MONTH_NAMES.put(6, "junio");
        MONTH_NAMES.put(7, "julio");
        MONTH_NAMES.put(8, "agosto");
        MONTH_NAMES.put(9, "septiembre");
        MONTH_NAMES.put(10, "octubre");
        MONTH_NAMES.put(11, "noviembre");
        MONTH_NAMES.put(12, "diciembre");
    }

    @Override
    public CanvasJSChartDTO historicDollarValue(int todayYear, int todayMonth) throws NoSeriesDataFoundException {
        CanvasJSChartDTO dto = new CanvasJSChartDTO();
        CanvasJSTitleDTO title = new CanvasJSTitleDTO("Precio del Dólar (en pesos de "+MONTH_NAMES.get(todayMonth)+" "+todayYear+")");
        dto.setTitle(title);
        dto.setXAxisTitle("Año");
        CanvasJSAxisDTO yAxis = new CanvasJSAxisDTO();
        yAxis.setTitle("Pesos");
        yAxis.setValueFormatString("$0");
        dto.setAxisY(yAxis);
        CanvasJSDatumDTO datum = new CanvasJSDatumDTO();
        datum.setType("area");
        datum.setColor("salmon");
        dto.setData(Collections.singletonList(datum));
        List<CanvasJSDatapointDTO> datapoints = new ArrayList<>();
        datum.setDataPoints(datapoints);

        final MoneyAmount oneDollar = new MoneyAmount(BigDecimal.ONE, "USD");
        final Currency ars = Currency.getInstance("ARS");

        for (int year = Inflation.ARS_INFLATION.getFromYear(); year < Inflation.ARS_INFLATION.getToYear(); year++) {
            for (int month = 1; month <= 12; month++) {
                MoneyAmount oneDollarBackThen = USD_INFLATION.adjust(oneDollar, todayYear, todayMonth, year, month);
                MoneyAmount pesosBackThen = ForeignExchange.INSTANCE.exchange(oneDollarBackThen, ars, year, month);
                MoneyAmount ma = ARS_INFLATION.adjust(pesosBackThen, year, month, todayYear, todayMonth);
                CanvasJSDatapointDTO dataPoint = new CanvasJSDatapointDTO(
                        "date-" + year + "-" + String.valueOf(month - 1) + "-1", ma.getAmount());
                datapoints.add(dataPoint);
            }
        }
        return dto;
    }

}
