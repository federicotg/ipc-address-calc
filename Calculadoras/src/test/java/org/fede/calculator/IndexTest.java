/*
 * Copyright (C) 2016 Federico Tello Gentile <federicotg@gmail.com>
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
package org.fede.calculator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.MessageFormat;
import java.util.List;
import org.fede.calculator.money.ForeignExchanges;
import static org.fede.calculator.money.ForeignExchanges.getForeignExchange;
import org.fede.calculator.money.Inflation;
import static org.fede.calculator.money.Inflation.ARS_INFLATION;
import static org.fede.calculator.money.Inflation.USD_INFLATION;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.NoSeriesDataFoundException;
import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.SortedMapMoneyAmountSeries;
import org.fede.calculator.money.series.YearMonth;
import org.fede.util.Util;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
public class IndexTest {

    public IndexTest() {
    }

    // @Test
    public void youIndexARS() {

        MoneyAmountSeries dollar = Util.readSeries("ahorros-dolar.json");
        MoneyAmountSeries gold = Util.readSeries("ahorros-oro.json");
        MoneyAmountSeries peso = Util.readSeries("ahorros-peso.json");

        final String target = "USD";

        MoneyAmountSeries proportionInUSD = new SortedMapMoneyAmountSeries("USD");

        YearMonth start = dollar.getFrom().min(gold.getFrom());

        final YearMonth end = dollar.getTo().max(gold.getTo());

        final MoneyAmount oneUSD = new MoneyAmount(BigDecimal.ONE, "USD");
        final MoneyAmount oneARS = getForeignExchange(oneUSD.getCurrency(), "ARS").exchange(oneUSD, "ARS", start.getYear(), start.getMonth());
        final MoneyAmount oneXAU = getForeignExchange(oneUSD.getCurrency(), "XAU").exchange(oneUSD, "XAU", start.getYear(), start.getMonth());

        while (start.compareTo(end) <= 0) {
            MoneyAmount usdSavings = dollar.getAmountOrElseZero(start);
            MoneyAmount arsSavings = peso.getAmountOrElseZero(start);
            MoneyAmount xauSavings = gold.getAmountOrElseZero(start);

            usdSavings = getForeignExchange(usdSavings.getCurrency(), target).exchange(usdSavings, target, start.getYear(), start.getMonth());
            xauSavings = getForeignExchange(xauSavings.getCurrency(), target).exchange(xauSavings, target, start.getYear(), start.getMonth());
            arsSavings = getForeignExchange(arsSavings.getCurrency(), target).exchange(arsSavings, target, start.getYear(), start.getMonth());

            final MoneyAmount totalSavings = usdSavings.add(xauSavings).add(arsSavings);

            if (totalSavings.getAmount().signum() > 0) {

                BigDecimal usdSavingsPercent = usdSavings.getAmount().divide(totalSavings.getAmount(), MathContext.DECIMAL128);
                BigDecimal arsSavingsPercent = arsSavings.getAmount().divide(totalSavings.getAmount(), MathContext.DECIMAL128);
                BigDecimal xauSavingsPercent = xauSavings.getAmount().divide(totalSavings.getAmount(), MathContext.DECIMAL128);

                System.out.print(MessageFormat.format("{0}{1}\t{2}\t{3}\t{4}\t", String.valueOf(start.getYear()), start.getMonth(), usdSavingsPercent, arsSavingsPercent, xauSavingsPercent));

                BigDecimal usdPrice = getForeignExchange(oneUSD.getCurrency(), target).exchange(oneUSD, target, start.getYear(), start.getMonth()).getAmount();
                BigDecimal arsPrice = getForeignExchange(oneARS.getCurrency(), target).exchange(oneARS, target, start.getYear(), start.getMonth()).getAmount();
                BigDecimal xauPrice = getForeignExchange(oneXAU.getCurrency(), target).exchange(oneXAU, target, start.getYear(), start.getMonth()).getAmount();

                System.out.print(MessageFormat.format("{0}\t{1}\t{2}\t", usdPrice, arsPrice, xauPrice));

                BigDecimal youIndex = usdPrice.multiply(usdSavingsPercent)
                        .add(arsPrice.multiply(arsSavingsPercent))
                        .add(xauPrice.multiply(xauSavingsPercent));

               // final MoneyAmount index = new MoneyAmount(youIndex, target);

                //BigDecimal adjustedYouIndex = USD_INFLATION.adjust(index, start.getYear(), start.getMonth(),
                //        USD_INFLATION.getTo().getYear(), USD_INFLATION.getTo().getMonth()).getAmount();

               // System.out.println(MessageFormat.format("{0}\t{1}", youIndex, adjustedYouIndex));

                proportionInUSD.putAmount(start.getYear(), start.getMonth(), new MoneyAmount(youIndex, target));
            }
            start = start.next();
        }
        //proportionInUSD.forEach(new MoneyAmountProcessor() {
         //   @Override
          //  public void process(int year, int month, MoneyAmount amount) throws NoSeriesDataFoundException {
                //System.out.println(MessageFormat.format("{0}{1}\t{2}", String.valueOf(year), month, amount.getAmount()));
            //}
        //});
    }

    @Test
    public void ok() {
        assertTrue(1==1);
    }
    
    // @Test
    public void historicDollar()  {

        YearMonth latestData = Inflation.USD_INFLATION.getTo();
        final int todayMonth = latestData.getMonth();
        final int todayYear = latestData.getYear();
        final MoneyAmount oneDollar = new MoneyAmount(BigDecimal.ONE, "USD");
        final String ars = "ARS";

        final MoneyAmountSeries historicDollar = ARS_INFLATION.adjust(
                ForeignExchanges.USD_ARS.exchange(
                        USD_INFLATION.adjust(oneDollar, todayYear, todayMonth), ars), todayYear, todayMonth);

        YearMonth comparisonYM = new YearMonth(2004, 1);

        MoneyAmount oneDollarInDollarInComparisonYM = ARS_INFLATION.adjust(
                ForeignExchanges.getForeignExchange(oneDollar.getCurrency(), ars).exchange(
                USD_INFLATION.adjust(oneDollar, latestData.getYear(), latestData.getMonth(), comparisonYM.getYear(), comparisonYM.getMonth()),
                ars, comparisonYM.getYear(), comparisonYM.getMonth()),
                comparisonYM.getYear(), comparisonYM.getMonth(),
                todayYear, todayMonth);

        assertEquals(oneDollarInDollarInComparisonYM, historicDollar.getAmount(comparisonYM));

    }

    private List<Investment> read(String name) throws IOException {
        try (InputStream in = IndexTest.class.getResourceAsStream("/" + name);) {
            ObjectMapper om = new ObjectMapper();

            return om.readValue(in, new TypeReference<List<Investment>>() {
            });
        }
    }

    
        
    @Test
    
    public void realUSD() {
        MoneyAmountSeries usd = Util.readSeries("saving/ahorros-dolar.json");
        
        MoneyAmount x = usd.getAmount(2016, 5);
        assertEquals("USD", x.getCurrency());
        assertEquals(0, x.getAmount().compareTo(new BigDecimal("76203.5")));
        
        assertEquals(x, Inflation.USD_INFLATION.adjust(x, 2015, 5, 2015, 5));
    }
        
    
        @Test 
    public void wft() {
        MoneyAmountSeries s = Util.readSeries("saving/ahorros-conbala.json");
        MoneyAmountSeries usd = s.exchangeInto("USD");
        System.out.println("");
        
    }
    
    
}
