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

import org.fede.calculator.money.NoSeriesDataFoundException;
import org.fede.calculator.money.SimpleAggregation;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
public class SimpleAggregationTest {
    
    private MoneyAmountSeries series;
    
    public SimpleAggregationTest() {
    }
    
    @Before
    public void moneyAmountSeries(){
        
    }
    

    /**
     * Test of average method, of class SimpleAggregation.
     */
    //@Test
    public void testAverage() throws NoSeriesDataFoundException {
        System.out.println("average");
        MoneyAmountSeries series = null;
        SimpleAggregation instance = null;
        MoneyAmountSeries expResult = null;
        MoneyAmountSeries result = instance.average(series);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of sum method, of class SimpleAggregation.
     */
   // @Test
    public void testSum() throws NoSeriesDataFoundException {
        System.out.println("sum");
        MoneyAmountSeries series = null;
        SimpleAggregation instance = null;
        MoneyAmountSeries expResult = null;
        MoneyAmountSeries result = instance.sum(series);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of change method, of class SimpleAggregation.
     */
   // @Test
    public void testChange() throws NoSeriesDataFoundException {
        System.out.println("change");
        MoneyAmountSeries series = null;
        SimpleAggregation instance = null;
        MoneyAmountSeries expResult = null;
        MoneyAmountSeries result = instance.change(series);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
