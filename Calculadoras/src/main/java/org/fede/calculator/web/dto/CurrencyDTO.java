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
package org.fede.calculator.web.dto;

import java.util.Currency;
import static org.fede.calculator.service.MoneyService.ES_AR;

/**
 *
 * @author fede
 */
public class CurrencyDTO {

    private String name;
    private String symbol;
    private String iso4217;

    public CurrencyDTO() {

    }

    public CurrencyDTO(Currency currency) {
        this(currency.getDisplayName(ES_AR),
                currency.getSymbol(ES_AR),
                currency.getCurrencyCode());
    }

    public CurrencyDTO(String name, String symbol, String iso4217) {
        this.name = name;
        this.symbol = symbol;
        this.iso4217 = iso4217;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getIso4217() {
        return iso4217;
    }

    public void setIso4217(String iso4217) {
        this.iso4217 = iso4217;
    }

}
