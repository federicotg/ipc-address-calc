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
import org.fede.calculator.lpadress.LaPlataAddress;
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
public class CallesTest {
    
    public CallesTest() {
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
    
    private void testValidAddress(String address, int entre1, int entre2){
        LaPlataAddress addr = LaPlataAddress.createAddressFromString(address);
        assertTrue(addr.isValid());
        assertEquals(entre1, addr.getLowerBoundary());
        assertEquals(entre2, addr.getUpperBoundary());
    }
    
    @Test
    public void variasCalles() {
        testValidAddress("45 573", 6, 7);
        testValidAddress("d80 861", 3, 4);
        testValidAddress("50 1085", 16, 17);
    }
}
