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
import org.fede.calculator.money.ArgCurrency;
import static org.fede.calculator.money.Inflation.ARS_INFLATION;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.web.dto.CurrencyDTO;
import org.fede.calculator.web.dto.CurrencyLimitsDTO;
import org.fede.calculator.web.dto.MoneyDTO;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
public class ARGMoneyService implements MoneyService {

    private static final CurrencyDTO CURRENCY_DTO = new CurrencyDTO(Currency.getInstance(ARS_INFLATION.getCurrency()));

    @Override
    public MoneyDTO getMoney(MoneyDTO dto) {

        MoneyDTO answer = new MoneyDTO();
        answer.setAmount(ARS_INFLATION.adjust(
                new MoneyAmount(dto.getAmount(), "ARS"),
                dto.getFromYear(),
                dto.getFromMonth(),
                dto.getToYear(),
                dto.getToMonth()).getAmount());
        answer.setFromMonth(dto.getFromMonth());
        answer.setFromYear(dto.getFromYear());
        answer.setToMonth(dto.getToMonth());
        answer.setToYear(dto.getToYear());
        answer.setValid(true);
        answer.setToCurrencySymbol(ArgCurrency.whichCurrency(dto.getToYear(), dto.getToMonth()).getSymbol());
        answer.setCurrency(CURRENCY_DTO);
        return answer;
    }

    @Override
    public CurrencyLimitsDTO getLimits() {
        return new CurrencyLimitsDTO(
                CURRENCY_DTO,
                ARS_INFLATION.getFrom().getYear(), ARS_INFLATION.getTo().getYear(),
                ARS_INFLATION.getTo().getYear(), ARS_INFLATION.getTo().getMonth());
    }

}
