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
import java.io.IOException;
import java.math.BigDecimal;
import static java.math.BigDecimal.ONE;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;
import static org.fede.calculator.money.ArgCurrency.*;
import org.fede.calculator.money.CPIInflation;
import org.fede.calculator.money.ForeignExchange;
import org.fede.calculator.money.Inflation;
import static org.fede.calculator.money.Inflation.ARS_INFLATION;
import static org.fede.calculator.money.Inflation.USD_INFLATION;
import org.fede.calculator.money.MathConstants;
import org.fede.calculator.money.series.CachedSeries;
import org.fede.calculator.money.series.DollarCPISeries;
import org.fede.calculator.money.series.IndexSeries;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.NoSeriesDataFoundException;
import org.fede.calculator.money.SimpleAverage;
import org.fede.calculator.money.series.JSONIndexSeries;
import org.fede.calculator.money.series.JSONMoneyAmountSeries;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author fede
 */
public class DollarTest {

    public DollarTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void january2013() {
        try {
            assertEquals(0, new BigDecimal("230.280").compareTo(new DollarCPISeries(new MockBlsCPISource()).getIndex(2013, 1)));
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void cachedSeries() {
        try {
            MockSeries ms = new MockSeries();
            IndexSeries series = new CachedSeries(ms);
            series.getIndex(2013, 1);
            series.getIndex(2013, 1);
            series.getIndex(2013, 1);
            series.getIndex(2013, 1);
            series.getIndex(2013, 1);
            assertEquals(1, ms.getCalls());

            series.getIndex(2012, 1);
            series.getIndex(2012, 1);

            assertEquals(2, ms.getCalls());

            series.getIndex(2000, 5);
            assertEquals(3, ms.getCalls());

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void hundredUSDToday() {
        try {
            MoneyAmount oneHundred = new MoneyAmount(new BigDecimal("100"), "USD");
            MoneyAmount expected = new MoneyAmount(new BigDecimal("13.38774249192"), "USD");
            MoneyAmount adjusted
                    = /*Inflation.USD_INFLATION*/ new CPIInflation(new CachedSeries(new DollarCPISeries(new MockBlsCPISource())), Currency.getInstance("USD"))
                    .adjust(oneHundred, 2013, 12, 1964, 12);
            //System.out.println(expected);
            //System.out.println(adjusted);
            assertEquals(expected, adjusted);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            ex.printStackTrace(System.err);
            fail(ex.getMessage());
        }
    }

    @Test
    public void hundredUSD1923() {
        try {
            MoneyAmount oneHundred = new MoneyAmount(new BigDecimal("100"), "USD");
            MoneyAmount expected = new MoneyAmount(new BigDecimal("1347.104046"), "USD");
            MoneyAmount adjusted
                    = new CPIInflation(new CachedSeries(new DollarCPISeries(new MockBlsCPISource())), Currency.getInstance("USD"))
                    .adjust(oneHundred, 1923, 12, 2013, 12);
            //System.out.println(expected);
            //System.out.println(adjusted);
            assertEquals(expected, adjusted);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            ex.printStackTrace(System.err);
            fail(ex.getMessage());
        }
    }

    @Test
    public void cqp1() {
        try {
            MoneyAmount adjusted = ARS_INFLATION.adjust(new MoneyAmount(new BigDecimal("521144.26"), "ARS"), 2013, 7, 1999, 11);
            assertEquals(new MoneyAmount(new BigDecimal("70652.7198300712"), "ARS"), adjusted);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            ex.printStackTrace(System.err);
            fail(ex.getMessage());
        }
    }

    @Test
    public void indec1970() {
        try {

            final MoneyAmount oneHundred = new MoneyAmount(new BigDecimal("100"), "ARS");

            MoneyAmount adjusted = ARS_INFLATION.adjust(oneHundred, 2014, 1, 1970, 1);
            assertEquals(new MoneyAmount(new BigDecimal("5.06413639570361"), "ARS"), adjusted);

        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            ex.printStackTrace(System.err);
            fail(ex.getMessage());
        }
    }

    @Test
    public void conversions() {
        assertEquals(0, ONE.compareTo(PESO.convertTo(ONE, PESO)));
        assertEquals(0, new BigDecimal("10000").compareTo(PESO.convertTo(ONE, AUSTRAL)));
        assertEquals(0, new BigDecimal("1000").compareTo(AUSTRAL.convertTo(ONE, ARGENTINO)));
        assertEquals(0, new BigDecimal("10000").compareTo(ARGENTINO.convertTo(ONE, LEY)));
        assertEquals(0, new BigDecimal("100").compareTo(LEY.convertTo(ONE, MONEDA_NACIONAL)));
        assertEquals(0, new BigDecimal("10000000000000").compareTo(PESO.convertTo(ONE, MONEDA_NACIONAL)));
        assertEquals(0, new BigDecimal("0.0000000000001").compareTo(MONEDA_NACIONAL.convertTo(ONE, PESO)));
        assertEquals(0, new BigDecimal("0.0000001").compareTo(LEY.convertTo(ONE, AUSTRAL)));
    }

    @Test
    public void jsonSeries() throws IOException, NoSeriesDataFoundException {
        assertEquals(0, new BigDecimal("2.060").compareTo(JSONIndexSeries.readSeries("peso-dolar-libre.json").getIndex(1987, 5)));
    }

    @Test
    public void fx() throws NoSeriesDataFoundException {
        MoneyAmount pesos10 = new MoneyAmount(new BigDecimal("10"), Currency.getInstance("ARS"));
        MoneyAmount xDollars = ForeignExchange.INSTANCE.exchange(pesos10, Currency.getInstance("USD"), 1981, 6);
        assertEquals(new MoneyAmount(new BigDecimal("0.00141844"), "USD"), xDollars);
    }
    
    @Test
    public void inflate() throws NoSeriesDataFoundException {
        MoneyAmountSeries unlp = JSONMoneyAmountSeries.readSeries("unlp.json");
        MoneyAmountSeries unlpDeflacted = ARS_INFLATION.adjust(unlp, 1999, 11);
        MoneyAmount ma = unlpDeflacted.getAmount(2014, 1);
        MoneyAmount expected = ARS_INFLATION.adjust(unlp.getAmount(2014, 1), 2014, 1, 1999, 11);

        assertEquals(expected, ma);
        assertEquals(new MoneyAmount(new BigDecimal("231.6932084953"), "ARS"), ma);

    }

    @Test
    public void historicDollar() throws NoSeriesDataFoundException {

        //Inflation localUSDInflation = new CPIInflation(new DollarCPISeries(new JSONBlsCPISource("bls.json")), Currency.getInstance("USD"));

        final int todayYear = 2013;
        final int todayMonth = 8;

        final MoneyAmount oneDollar = new MoneyAmount(BigDecimal.ONE, "USD");
        final Currency ars = Currency.getInstance("ARS");

        MoneyAmountSeries expected = new JSONMoneyAmountSeries(Currency.getInstance("ARS"));

        for (int year = Inflation.ARS_INFLATION.getFrom().getYear(); year < Inflation.ARS_INFLATION.getTo().getYear(); year++) {
            for (int month = 1; month <= 12; month++) {
                MoneyAmount oneDollarBackThen = USD_INFLATION.adjust(oneDollar, todayYear, todayMonth, year, month);
                MoneyAmount pesosBackThen = ForeignExchange.INSTANCE.exchange(oneDollarBackThen, ars, year, month);
                MoneyAmount ma = ARS_INFLATION.adjust(pesosBackThen, year, month, todayYear, todayMonth);
                expected.putAmount(year, month, ma);
            }
        }
        final int year = Inflation.ARS_INFLATION.getTo().getYear();
        for (int month = 1; month <= Inflation.ARS_INFLATION.getTo().getMonth(); month++) {
                MoneyAmount oneDollarBackThen = USD_INFLATION.adjust(oneDollar, todayYear, todayMonth, year, month);
                MoneyAmount pesosBackThen = ForeignExchange.INSTANCE.exchange(oneDollarBackThen, ars, year, month);
                MoneyAmount ma = ARS_INFLATION.adjust(pesosBackThen, year, month, todayYear, todayMonth);
                expected.putAmount(year, month, ma);
            }

        MoneyAmountSeries result = Inflation.ARS_INFLATION.adjust(
                ForeignExchange.INSTANCE.exchange(
                        USD_INFLATION.adjust(oneDollar, todayYear, todayMonth), ars), todayYear, todayMonth);

        assertEquals(expected, result);
    }

    
    @Test
    public void averages() throws NoSeriesDataFoundException{
        MoneyAmountSeries series = JSONMoneyAmountSeries.readSeries("unlp.json");
        MoneyAmountSeries averaged = new SimpleAverage(6).average(JSONMoneyAmountSeries.readSeries("unlp.json"));
        
        assertEquals(series.getAmount(series.getFrom().getYear(), series.getFrom().getMonth()), averaged.getAmount(averaged.getFrom().getYear(), averaged.getFrom().getMonth()));
        
        BigDecimal x1 = series.getAmount(2010, 1).getAmount();
        BigDecimal x2 = series.getAmount(2010, 2).getAmount();
        BigDecimal x3 = series.getAmount(2010, 3).getAmount();
        BigDecimal x4 = series.getAmount(2010, 4).getAmount();
        BigDecimal x5 = series.getAmount(2010, 5).getAmount();
        BigDecimal x6 = series.getAmount(2010, 6).getAmount();
        
        BigDecimal value = averaged.getAmount(2010, 6).getAmount();
        BigDecimal expected = x1.add(x2).add(x3).add(x4).add(x5).add(x6).divide(new BigDecimal(6), MathConstants.CONTEXT);
        assertEquals(expected, value);
        
    }
    
    @Test
    public void limits() throws NoSeriesDataFoundException{
        assertEquals(1913, Inflation.USD_INFLATION.getFrom().getYear());
        assertEquals(1, Inflation.USD_INFLATION.getFrom().getMonth());
        
        assertEquals(2014, Inflation.USD_INFLATION.getTo().getYear());
        assertEquals(8, Inflation.USD_INFLATION.getTo().getMonth());
        
        MoneyAmountSeries series = JSONMoneyAmountSeries.readSeries("lifia.json");
        MoneyAmountSeries dolarizedSeries = ForeignExchange.INSTANCE.exchange(series, Currency.getInstance("USD"));
        
        assertEquals(ForeignExchange.INSTANCE.getTo().getYear(), dolarizedSeries.getTo().getYear());
        assertEquals(ForeignExchange.INSTANCE.getTo().getMonth(), dolarizedSeries.getTo().getMonth());
        
    }
}
