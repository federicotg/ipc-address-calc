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

    private static final BigDecimal EPSILON = BigDecimal.ONE.movePointLeft(4);
    private static ObjectMapper OM = new ObjectMapper();

    public InvestmentReportTest() {
    }

    @Test
    public void pfArs() throws JsonProcessingException {

        final var inv = OM.readValue(
                "{ \"type\": \"PF\", \"in\": { \"currency\": \"ARS\", \"date\": \"09/05/2006\", \"amount\": \"4800\" }, \"investment\": { \"currency\": \"ARS\", \"amount\": \"4800\" }, \"out\": { \"currency\": \"ARS\", \"date\": \"08/06/2006\", \"amount\": \"4820.12\" } }",
                Investment.class);

        final var r = new InvestmentReport(inv, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

        assertNotNull(r);

        assertTrue(r.getGrossRealInvestment().getAmount().subtract(new BigDecimal("2004.609909")).abs().compareTo(EPSILON) <= 0);
        assertTrue(r.getCurrentValue().getAmount().subtract(new BigDecimal("2002.542322")).abs().compareTo(EPSILON) <= 0);
        assertTrue(r.getNetRealProfit().getAmount().subtract(new BigDecimal("-2.06758")).abs().compareTo(EPSILON) <= 0);
    }

    @Test
    public void pfUVA() throws JsonProcessingException {
        final var inv = OM.readValue(
                "{ \"type\": \"PF\", \"interest\": \"0.050088\", \"in\": { \"currency\": \"ARS\", \"date\": \"04/09/2018\", \"amount\": 20000 }, \"investment\": { \"currency\": \"UVA\", \"amount\": 769.82 }, \"out\": { \"currency\": \"ARS\", \"date\": \"03/12/2018\", \"amount\": 23231.76 } }",
                Investment.class);
        final var r = new InvestmentReport(inv, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

        assertNotNull(r);
        //System.out.println(r.getNetRealProfit().getAmount());
        assertTrue(r.getNetRealProfit().getAmount().subtract(new BigDecimal("131.2417")).abs().compareTo(EPSILON) <= 0);

    }
}
