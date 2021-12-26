/*
 * Copyright (C) 2021 federicogentile
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.fede.calculator.money.CashInvestmentBuilder;
import org.fede.calculator.money.MathConstants;
import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.InvestmentAsset;
import org.fede.calculator.money.series.InvestmentEvent;
import org.fede.calculator.money.series.InvestmentType;
import org.fede.calculator.money.series.SeriesReader;
import org.fede.calculator.money.series.YearMonth;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author federicogentile
 */
public class LiquidityTest {

    @Test
    public void liq() {

        final var liq = SeriesReader.readSeries("/saving/ahorros-dolar-liq.json");

        final List<Investment> investments = new CashInvestmentBuilder(liq).cashInvestments();

        for (var ym = liq.getFrom(); ym.compareTo(liq.getTo()) <= 0; ym = ym.next()) {
            var currentSavedUSD = liq.getAmountOrElseZero(ym).getAmount();
            var invested = this.total(investments, ym);
            //System.out.println(ym+" "+currentSavedUSD+" "+invested);
            assertEquals("Saved and invested must be the same.", 0, currentSavedUSD.compareTo(invested));
        }

        //investments.stream().map(Investment::toString).forEach(System.out::println);
        
                
                
        //System.out.println(total(investments, YearMonth.of(2019, 3)));
        
    }

 
    private BigDecimal total(List<Investment> investments, YearMonth ym) {
        return investments.stream()
                .filter(i -> i.isCurrent(ym.asToDate()))
                .map(Investment::getInvestment)
                .map(InvestmentAsset::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
