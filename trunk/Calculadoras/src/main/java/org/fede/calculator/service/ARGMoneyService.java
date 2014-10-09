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
import org.fede.calculator.money.NoSeriesDataFoundException;
import org.fede.calculator.web.dto.CurrencyDTO;
import org.fede.calculator.web.dto.CurrencyLimitsDTO;
import org.fede.calculator.web.dto.MoneyDTO;

/**
 *
 * @author fede
 */
public class ARGMoneyService implements MoneyService {

    private static final CurrencyDTO CURRENCY_DTO = new CurrencyDTO(
            ARS_INFLATION.getCurrency().getDisplayName(ES_AR),
            ARS_INFLATION.getCurrency().getSymbol(ES_AR),
            ARS_INFLATION.getCurrency().getCurrencyCode());

    @Override
    public MoneyDTO getMoney(MoneyDTO dto) throws NoSeriesDataFoundException {

        MoneyAmount amount = new MoneyAmount(dto.getAmount(), Currency.getInstance("ARS"));
        MoneyAmount result = ARS_INFLATION.adjust(amount, dto.getFromYear(), dto.getFromMonth(), dto.getToYear(), dto.getToMonth());
        MoneyDTO answer = new MoneyDTO();
        answer.setAmount(result.getAmount());
        answer.setFromMonth(dto.getFromMonth());
        answer.setFromYear(dto.getFromYear());
        answer.setToMonth(dto.getToMonth());
        answer.setToYear(dto.getToYear());
        answer.setValid(true);

        ArgCurrency toCurrency = ArgCurrency.whichCurrency(dto.getToYear(), dto.getToMonth());

        answer.setToCurrencySymbol(toCurrency.getSymbol());
        answer.setCurrency(CURRENCY_DTO);
        return answer;

    }

    @Override
    public CurrencyLimitsDTO getLimits() {
        return new CurrencyLimitsDTO(
                CURRENCY_DTO,
                ARS_INFLATION.getFromYear(), ARS_INFLATION.getToYear());

    }

}
