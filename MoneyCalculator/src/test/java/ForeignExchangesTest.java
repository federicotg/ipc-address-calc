/*
 * Copyright (C) 2016 Federico Tello Gentile <federico.gentile@despegar.com>
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

import org.fede.calculator.money.CompoundForeignExchange;
import org.fede.calculator.money.ForeignExchange;
import org.fede.calculator.money.ForeignExchanges;
import static org.fede.calculator.money.ForeignExchanges.USD_ARS;
import static org.fede.calculator.money.ForeignExchanges.USD_EUR;
import static org.fede.calculator.money.ForeignExchanges.USD_XAU;
import static org.fede.calculator.money.ForeignExchanges.ARS_CONAAFA;
import static org.fede.calculator.money.ForeignExchanges.getForeignExchange;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Federico Tello Gentile <federico.gentile@despegar.com>
 */
public class ForeignExchangesTest {

    public ForeignExchangesTest() {
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

    private void identity(String currency) {
        assertEquals(ForeignExchanges.getIdentityForeignExchange(currency), getForeignExchange(currency, currency));
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void gettingThem() {

        this.identity("ARS");
        this.identity("USD");
        this.identity("EUR");
        this.identity("XAU");
        this.identity("CONBALA");
        this.identity("CONAAFA");

        assertEquals(USD_ARS, getForeignExchange("USD", "ARS"));
        assertEquals(USD_XAU, getForeignExchange("USD", "XAU"));
        assertEquals(USD_EUR, getForeignExchange("USD", "EUR"));

        assertEquals(USD_ARS, getForeignExchange("ARS", "USD"));
        assertEquals(USD_XAU, getForeignExchange("XAU", "USD"));
        assertEquals(USD_EUR, getForeignExchange("EUR", "USD"));
        assertEquals(ARS_CONAAFA, getForeignExchange("ARS", "CONAAFA"));
        assertEquals(ARS_CONAAFA, getForeignExchange("CONAAFA", "ARS"));

        assertEquals(new CompoundForeignExchange(USD_ARS, USD_EUR), getForeignExchange("ARS", "EUR"));
        assertEquals(new CompoundForeignExchange(USD_XAU, USD_EUR), getForeignExchange("XAU", "EUR"));
        assertEquals(new CompoundForeignExchange(USD_XAU, USD_ARS), getForeignExchange("XAU", "ARS"));
        assertEquals(new CompoundForeignExchange(USD_ARS, USD_XAU), getForeignExchange("ARS", "XAU"));

        assertEquals(new CompoundForeignExchange(ARS_CONAAFA, USD_ARS), getForeignExchange("CONAAFA", "USD"));

        ForeignExchange result = getForeignExchange("CONAAFA", "XAU");

        assertEquals(new CompoundForeignExchange(
                ARS_CONAAFA,
                new CompoundForeignExchange(USD_ARS, USD_XAU)),
                result);

    }
}
