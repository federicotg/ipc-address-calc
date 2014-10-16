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
import java.math.BigDecimal;
import java.util.Currency;
import org.fede.calculator.money.ForeignExchange;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.NoSeriesDataFoundException;
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
public class GoldTest {
    
    public GoldTest() {
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
    public void usdToGold() throws NoSeriesDataFoundException {
        MoneyAmount usd = new MoneyAmount(new BigDecimal("1000"), Currency.getInstance("USD"));
        MoneyAmount gold = ForeignExchange.USD_XAU.exchange(usd, Currency.getInstance("XAU"), 2013, 7);
        
        assertEquals(Currency.getInstance("XAU"), gold.getCurrency());
        
        MoneyAmount expected = new MoneyAmount(new BigDecimal("0.778605176"), Currency.getInstance("XAU"));
        assertEquals(expected, gold);
    
    }
}
