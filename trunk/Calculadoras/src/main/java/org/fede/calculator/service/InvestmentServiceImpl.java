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
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import org.fede.calculator.money.ForeignExchange;
import org.fede.calculator.money.Inflation;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.NoSeriesDataFoundException;
import org.fede.calculator.money.series.JSONMoneyAmountSeries;
import org.fede.calculator.money.series.MoneyAmountProcessor;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.web.dto.DollarReportDTO;
import org.springframework.stereotype.Service;

/**
 *
 * @author fede
 */
@Service
public class InvestmentServiceImpl implements InvestmentService {

    @Override
    public List<DollarReportDTO> dollar() throws NoSeriesDataFoundException {
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        final Date moment = cal.getTime();

        final MoneyAmountSeries dolares = JSONMoneyAmountSeries.readSeries("dolares.json");
        final Currency peso = Currency.getInstance("ARS");

        final List<DollarReportDTO> answer = new ArrayList<>();

        dolares.forEachNonZero(new MoneyAmountProcessor() {
            @Override
            public void process(int year, int month, MoneyAmount dollar) throws NoSeriesDataFoundException {
                DollarReportDTO dto = new DollarReportDTO();
                dto.setUsd(dollar.getAmount());
                answer.add(dto);
                Date thenDate = createDate(year, month);
                dto.setThen(thenDate);
                MoneyAmount pesos = ForeignExchange.INSTANCE.exchange(dollar, peso, thenDate);
                dto.setNominalPesosThen(pesos.getAmount());
                BigDecimal realPesos =Inflation.ARS_INFLATION.adjust(pesos, thenDate, moment).getAmount();
                dto.setRealPesosNow(realPesos);
                dto.setNow(moment);
                dto.setNominalPesosNow(ForeignExchange.INSTANCE.exchange(dollar, peso, moment).getAmount());
                
            }
        });
        return answer;
    }

    private Date createDate(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
    }

}
