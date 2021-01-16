/*
 * Copyright (C) 2021 federico
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import org.fede.calculator.money.InvestmentReport;
import org.fede.calculator.money.series.Investment;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author federico
 */
public class InvestmentReportTest {
    
    public InvestmentReportTest() {
    }
    
    
    @Test
     public void hello() throws JsonProcessingException {
     
     final var inv = new ObjectMapper().readValue(
             "{ \"type\": \"PF\", \"in\": { \"currency\": \"ARS\", \"date\": \"09/05/2006\", \"amount\": \"4800\" }, \"investment\": { \"currency\": \"ARS\", \"amount\": \"4800\" }, \"out\": { \"currency\": \"ARS\", \"date\": \"08/06/2006\", \"amount\": \"4820.12\" } }", 
             Investment.class);
     
     final var r = new InvestmentReport(inv, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
     
     assertNotNull(r);
     
     }
}
