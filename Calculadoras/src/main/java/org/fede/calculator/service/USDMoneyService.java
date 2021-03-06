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

import java.util.Currency;
import static org.fede.calculator.money.Inflation.USD_INFLATION;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.web.dto.CurrencyDTO;
import org.fede.calculator.web.dto.CurrencyLimitsDTO;
import org.fede.calculator.web.dto.MoneyDTO;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
public class USDMoneyService implements MoneyService {

    private static final CurrencyDTO CURRENCY_DTO = new CurrencyDTO(Currency.getInstance(USD_INFLATION.getCurrency()));

    @Override
    public MoneyDTO getMoney(MoneyDTO dto) {

        MoneyDTO answer = new MoneyDTO();
        answer.setAmount(USD_INFLATION.adjust(
                new MoneyAmount(dto.getAmount(), "USD"),
                dto.getFromYear(),
                dto.getFromMonth(),
                dto.getToYear(),
                dto.getToMonth()).getAmount());
        answer.setFromMonth(dto.getFromMonth());
        answer.setFromYear(dto.getFromYear());
        answer.setToMonth(dto.getToMonth());
        answer.setToYear(dto.getToYear());
        answer.setValid(true);
        answer.setCurrency(CURRENCY_DTO);
        answer.setToCurrencySymbol(Currency.getInstance("USD").getSymbol(ES_AR));
        return answer;

    }

    @Override
    public CurrencyLimitsDTO getLimits() {

        return new CurrencyLimitsDTO(CURRENCY_DTO, USD_INFLATION.getFrom().getYear(), USD_INFLATION.getTo().getYear(),
                USD_INFLATION.getTo().getYear(), USD_INFLATION.getTo().getMonth());
    }

}
