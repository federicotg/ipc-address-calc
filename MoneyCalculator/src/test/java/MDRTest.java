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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import org.fede.calculator.money.ModifiedDietzReturn;
import org.fede.calculator.money.Series;
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
public class MDRTest {
    
    private Series series;
    
    public MDRTest() {
        this.series = new Series();
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
    public void mdr() {
    
        var from = LocalDate.of(2019, Month.JULY, 1);
        var to = LocalDate.of(2019, Month.JULY, 31);
        
        var mdr = new ModifiedDietzReturn(this.series.getInvestments(), "USD", false, from, to).get().getMoneyWeighted();
        
        assertEquals(1, mdr.compareTo(BigDecimal.ZERO));
        
    
    }
}
