package org.fede.calculator.money;

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
import java.util.Arrays;
import java.util.Currency;
import static org.fede.calculator.money.ArgCurrency.*;
import static org.fede.calculator.money.Inflation.ARS_INFLATION;
import static org.fede.calculator.money.Inflation.USD_INFLATION;
import static org.fede.calculator.money.ForeignExchange.USD_ARS;
import org.fede.calculator.money.series.CachedSeries;
import org.fede.calculator.money.series.DollarCPISeries;
import org.fede.calculator.money.series.IndexSeries;
import org.fede.calculator.money.series.JSONDataPoint;
import org.fede.calculator.money.series.JSONIndexSeries;
import org.fede.calculator.money.series.JSONMoneyAmountSeries;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.YearMonth;
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
            System.out.println(expected);
            System.out.println(adjusted);
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
        MoneyAmount xDollars = USD_ARS.exchange(pesos10, Currency.getInstance("USD"), 1981, 6);
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

        final int todayYear = 2014;
        final int todayMonth = 9;

        final MoneyAmount oneDollar = new MoneyAmount(BigDecimal.ONE, "USD");
        final Currency ars = Currency.getInstance("ARS");

        MoneyAmountSeries expected = new JSONMoneyAmountSeries(Currency.getInstance("ARS"));

        for (int year = ARS_INFLATION.getFrom().getYear(); year < ARS_INFLATION.getTo().getYear(); year++) {
            for (int month = 1; month <= 12; month++) {
                MoneyAmount oneDollarBackThen = USD_INFLATION.adjust(oneDollar, todayYear, todayMonth, year, month);
                MoneyAmount pesosBackThen = ForeignExchange.USD_ARS.exchange(oneDollarBackThen, ars, year, month);
                MoneyAmount ma = ARS_INFLATION.adjust(pesosBackThen, year, month, todayYear, todayMonth);
                expected.putAmount(year, month, ma);
            }
        }
        final int year = ARS_INFLATION.getTo().getYear();
        for (int month = 1; month <= ARS_INFLATION.getTo().getMonth(); month++) {
            MoneyAmount oneDollarBackThen = USD_INFLATION.adjust(oneDollar, todayYear, todayMonth, year, month);
            MoneyAmount pesosBackThen = USD_ARS.exchange(oneDollarBackThen, ars, year, month);
            MoneyAmount ma = ARS_INFLATION.adjust(pesosBackThen, year, month, todayYear, todayMonth);
            expected.putAmount(year, month, ma);
        }

        MoneyAmountSeries result = ARS_INFLATION.adjust(
                USD_ARS.exchange(
                        USD_INFLATION.adjust(oneDollar, todayYear, todayMonth), ars), todayYear, todayMonth);

        assertEquals(expected, result);
    }

    @Test
    public void averages() throws NoSeriesDataFoundException {
        MoneyAmountSeries series = JSONMoneyAmountSeries.readSeries("unlp.json");
        MoneyAmountSeries averaged = new SimpleAggregation(6).average(JSONMoneyAmountSeries.readSeries("unlp.json"));

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
    public void limits() throws NoSeriesDataFoundException {
        assertEquals(1913, USD_INFLATION.getFrom().getYear());
        assertEquals(1, USD_INFLATION.getFrom().getMonth());

        //assertEquals(2014, Inflation.USD_INFLATION.getTo().getYear());
        //assertEquals(9, Inflation.USD_INFLATION.getTo().getMonth());
        MoneyAmountSeries series = JSONMoneyAmountSeries.readSeries("lifia.json");
        MoneyAmountSeries dolarizedSeries = USD_ARS.exchange(series, Currency.getInstance("USD"));

//        assertEquals(USD_ARS.getTo().getYear(), dolarizedSeries.getTo().getYear());
//        assertEquals(USD_ARS.getTo().getMonth(), dolarizedSeries.getTo().getMonth());

        assertEquals(series.getFrom().getYear(), dolarizedSeries.getFrom().getYear());
        assertEquals(series.getFrom().getMonth(), dolarizedSeries.getFrom().getMonth());
    }

    @Test
    public void inflationLimits() throws NoSeriesDataFoundException {
        MoneyAmountSeries series = JSONMoneyAmountSeries.readSeries("lifia.json");
        MoneyAmountSeries inflatedSeries = ARS_INFLATION.adjust(series, 1962, 9);

//        assertEquals(ARS_INFLATION.getTo().getYear(), inflatedSeries.getTo().getYear());
//        assertEquals(ARS_INFLATION.getTo().getMonth(), inflatedSeries.getTo().getMonth());

        assertEquals(series.getFrom().getYear(), inflatedSeries.getFrom().getYear());
        assertEquals(series.getFrom().getMonth(), inflatedSeries.getFrom().getMonth());

    }

    @Test
    public void seriesAddition() throws NoSeriesDataFoundException {
        MoneyAmountSeries lifia = JSONMoneyAmountSeries.readSeries("lifia.json");
        MoneyAmountSeries unlp = JSONMoneyAmountSeries.readSeries("unlp.json");
        MoneyAmountSeries sum = lifia.add(unlp);

        MoneyAmount expected = lifia.getAmount(2010, 5).add(unlp.getAmount(2010, 5));
        assertEquals(expected, sum.getAmount(2010, 5));

        expected = unlp.getAmount(2001, 12);
        assertEquals(expected, sum.getAmount(2001, 12));
    }

    @Test
    public void uninterpolated() throws NoSeriesDataFoundException {
        MoneyAmountSeries unlp = JSONMoneyAmountSeries.readSeries("unlp.json");
        MoneyAmountSeries ars = JSONMoneyAmountSeries.readSeries("ahorros-peso.json");
        try {
            MoneyAmountSeries sum = unlp.add(ars);
        } catch (NoSeriesDataFoundException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void ahorros() throws NoSeriesDataFoundException {
        //9 2002
        MoneyAmountSeries pesos = JSONMoneyAmountSeries.readSeries("ahorros-peso.json");
        MoneyAmountSeries dolares = JSONMoneyAmountSeries.readSeries("ahorros-dolar.json");

        MoneyAmount p = pesos.getAmount(2002, 9);
        MoneyAmount d = dolares.getAmount(2002, 9);
        MoneyAmount p2 = USD_ARS.exchange(d, Currency.getInstance("ARS"), 2002, 9);
        MoneyAmount expected = p.add(p2);

        MoneyAmountSeries newSeries = USD_ARS.exchange(dolares, Currency.getInstance("ARS"));
        assertEquals(new MoneyAmount(new BigDecimal("44152.57"), Currency.getInstance("ARS")), expected);
        assertEquals(expected,
                pesos.add(newSeries).getAmount(2002, 9));
    }

    // @Test arreglar el hecho de que no se esta interpolando la serie al crearla así.
    public void exchangeFailing() throws NoSeriesDataFoundException {
        JSONDataPoint fx1 = new JSONDataPoint(2000, 10);
        fx1.setValue(new BigDecimal("3"));
        JSONDataPoint fx2 = new JSONDataPoint(2000, 11);
        fx2.setValue(new BigDecimal("5"));
        JSONDataPoint fx3 = new JSONDataPoint(2000, 12);
        fx3.setValue(new BigDecimal("7"));
        JSONDataPoint fx4 = new JSONDataPoint(2001, 1);
        fx4.setValue(new BigDecimal("10"));

        JSONIndexSeries fxSeries = new JSONIndexSeries(Arrays.asList(new JSONDataPoint[]{fx1, fx2, fx3, fx4}));
        ForeignExchange fx = new SimpleForeignExchange(fxSeries, Currency.getInstance("USD"), Currency.getInstance("ARS"));

        JSONMoneyAmountSeries usdSeries = new JSONMoneyAmountSeries(Currency.getInstance("USD"));
        usdSeries.putAmount(1998, 5, new MoneyAmount(new BigDecimal("1"), Currency.getInstance("USD")));
        usdSeries.putAmount(2000, 11, new MoneyAmount(new BigDecimal("10"), Currency.getInstance("USD")));
        usdSeries.putAmount(2001, 1, new MoneyAmount(new BigDecimal("100"), Currency.getInstance("USD")));

        MoneyAmountSeries pasadaAPesos = fx.exchange(usdSeries, Currency.getInstance("ARS"));
        assertEquals(new MoneyAmount(new BigDecimal("70"), Currency.getInstance("ARS")), pasadaAPesos.getAmount(2000, 12));

    }

    @Test
    public void yearMonthDistances() {

        assertEquals(1, new YearMonth(2012, 12).monthsUntil(new YearMonth(2013, 1)));
        assertEquals(0, new YearMonth(2012, 12).monthsUntil(new YearMonth(2012, 12)));
        assertEquals(12, new YearMonth(2012, 12).monthsUntil(new YearMonth(2013, 12)));
        assertEquals(0, new YearMonth(2012, 2).monthsUntil(new YearMonth(2011, 6)));
        assertEquals(23, new YearMonth(2012, 1).monthsUntil(new YearMonth(2013, 12)));

    }

    @Test
    public void convertAustrales() throws NoSeriesDataFoundException {
        MoneyAmount oneDollar = new MoneyAmount(BigDecimal.ONE, "USD");
        MoneyAmount australes = ForeignExchange.USD_ARS.exchange(oneDollar, Currency.getInstance("ARS"), 1989, 12);
        assertTrue(australes.getAmount().compareTo(new BigDecimal("1000")) > 0);
    }

    
    @Test
    public void futureValueDollar() throws NoSeriesDataFoundException{
    System.err.println(new DollarCPISeries().predictValue(2015, 1));
    
    }
}
