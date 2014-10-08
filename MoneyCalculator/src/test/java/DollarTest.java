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
import org.fede.calculator.money.series.CachedSeries;
import org.fede.calculator.money.series.DollarCPISeries;
import org.fede.calculator.money.series.IndexSeries;
import org.fede.calculator.money.MoneyAmount;
import static org.fede.calculator.money.Inflation.ARS_INFLATION;
import static org.fede.calculator.money.Inflation.USD_INFLATION;
import org.fede.calculator.money.NoSeriesDataFoundException;
import org.fede.calculator.money.json.JSONDataPoint;
import org.fede.calculator.money.json.JSONSeries;
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
            MoneyAmount expected = new MoneyAmount(new BigDecimal("1362.3216374269"), "USD");
            MoneyAmount adjusted
                    = /*Inflation.USD_INFLATION*/ new CPIInflation(new CachedSeries(new DollarCPISeries(new MockBlsCPISource())), Currency.getInstance("USD"))
                    .adjust(oneHundred, 1923, 2013);
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
            assertEquals(new MoneyAmount(new BigDecimal("70652.72"), "ARS"), adjusted);
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
        MoneyAmount xDollars = ForeignExchange.INSTANCE.exchangeAmountIntoCurrency(pesos10, Currency.getInstance("USD"), 1981, 6);
        assertEquals(new MoneyAmount(new BigDecimal("0.00141844"), "USD"), xDollars);

        //this.print("CqP", new CqPSeries());
        //this.print("Indec", new IndecCPISeries());
    }

    private void print(String name, IndexSeries s) {
        NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
        nf.setMaximumFractionDigits(20);
        nf.setGroupingUsed(false);
        System.out.print(name + "\n\n[");
        for (int year = s.getFromYear(); year <= s.getToYear(); year++) {
            for (int month = 1; month < 13; month++) {
                try {
                    System.out.print("{\"year\":\"" + year + "\",\"month\":\"" + month + "\",\"value\":\"" + nf.format(s.getIndex(year, month)) + "\"}");
                    if (year <= s.getToYear() || month < 12) {
                        System.out.println(",");
                    }
                } catch (NoSeriesDataFoundException ex) {

                }
            }
        }
        System.out.print("]");
    }

    //@Test
    public void historicDollar() throws NoSeriesDataFoundException {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
        nf.setGroupingUsed(false);
        final int todayYear = 2014;
        final int todayMonth = 8;
        final MoneyAmount oneDollar = new MoneyAmount(BigDecimal.ONE, "USD");
        final Currency ars = Currency.getInstance("ARS");
        System.out.println("[");
        for (int year = ARS_INFLATION.getFromYear(); year <= ARS_INFLATION.getToYear(); year++) {
            for (int month = 1; month <= 12; month++) {
                MoneyAmount oneDollarBackThen = USD_INFLATION.adjust(oneDollar, todayYear, todayMonth, year, month);
                MoneyAmount pesosBackThen = ForeignExchange.INSTANCE.exchangeAmountIntoCurrency(oneDollarBackThen, ars, year, month);
                MoneyAmount ma = ARS_INFLATION.adjust(pesosBackThen, year, month, todayYear, todayMonth);
                //System.out.println(String.valueOf(year) + (month<10?"0":"")+String.valueOf(month) +"28"+ "\t" + nf.format(ma.getAmount()));
                System.out.print(
                "{x: new Date("
                        +year
                        +", "
                        +((month-1)<10?"0":"")+String.valueOf(month-1)
                        +", 1), y: "
                        +nf.format(ma.getAmount())
                        +"}");
                if (year < ARS_INFLATION.getToYear() || month < 12) {
                        System.out.println(",");
                    }
            }
        }
        System.out.println("]");

    }
    
    @Test
    public void inflate() throws NoSeriesDataFoundException{
        MoneyAmountSeries unlp = JSONMoneyAmountSeries.readSeries("unlp.json");
        MoneyAmountSeries unlpDeflacted = unlp.adjust(ARS_INFLATION, 1999, 11);
        MoneyAmount ma = unlpDeflacted.getAmount(2014, 1);
        MoneyAmount expected = ARS_INFLATION.adjust(unlp.getAmount(2014, 1), 2014, 1, 1999, 11);
        
        assertEquals(expected, ma);
        assertEquals(new MoneyAmount(new BigDecimal("231.6932084953"),"ARS"), ma);
        
    }

}
