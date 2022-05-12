/*
 * Copyright (C) 2022 fede
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
package org.fede.calculator.money;

import java.util.function.Function;
import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.InvestmentAsset;
import org.fede.calculator.money.series.YearMonth;

/**
 *
 * @author fede
 */
public class BenchmarkInvestmentMapper implements Function<Investment, Investment>{

    private final String benchmark;

    public BenchmarkInvestmentMapper(String benchmark) {
        this.benchmark = benchmark;
    }
    
    @Override
    public Investment apply(Investment t) {
        
        if(t.getCurrency().equals(this.benchmark)){
            return t;
        }
        
        var fx = ForeignExchanges.getMoneyAmountForeignExchange(t.getInitialCurrency(), this.benchmark);
        var asset = new InvestmentAsset();
        asset.setCurrency(this.benchmark);
        asset.setAmount(fx.apply(t.getInitialMoneyAmount(), YearMonth.of(t.getInitialDate())).getAmount());
        var answer = new Investment();
        answer.setIn(t.getIn());
        answer.setInvestment(asset);
        answer.setOut(t.getOut());
        return answer;
    }
    
}
