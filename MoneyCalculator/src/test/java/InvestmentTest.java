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

import org.fede.calculator.money.series.InvestmentType;
import org.fede.calculator.money.series.Investment;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import org.fede.calculator.money.ForeignExchange;
import org.fede.calculator.money.ForeignExchanges;
import static org.fede.calculator.money.Inflation.ARS_INFLATION;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.NoSeriesDataFoundException;
import org.fede.calculator.money.series.CertificateDepositInvestment;
import org.fede.calculator.money.series.CurrencyInvestment;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
public class InvestmentTest {

    private final DateFormat df = DateFormat.getDateInstance();
    private final NumberFormat nf = NumberFormat.getNumberInstance();

    private List<Investment> inv;

    public InvestmentTest() throws IOException {
        this.inv = this.read("investments-test.json");
        this.nf.setMaximumFractionDigits(2);
    }

    @Test
    public void allOK() {
        for (Investment i : this.inv) {
            assertTrue(i.isValid());
        }
    }


    //@Test
    public void pf() throws NoSeriesDataFoundException {

        assertFalse(inv.isEmpty());
        //final String message = "Invertí {0} el {1}. El {2} cobré {3}. En {4} del {5} puse {6} y recuperé {7}. Gané {8}";
        final String message = "{0}\t{1}\t{2}";

        for (Investment investment : inv) {
            if (investment.getType().equals(InvestmentType.PF)) {
                
                assertTrue(investment instanceof CertificateDepositInvestment);
                
                if (investment.getIn().getCurrency().equals(investment.getOut().getCurrency())
                        && investment.getIn().getCurrency().equals("ARS")) {
                    MoneyAmount nominalIn = new MoneyAmount(investment.getIn().getAmount(), investment.getIn().getCurrency());
                    MoneyAmount nominalOut = new MoneyAmount(investment.getOut().getAmount(), investment.getIn().getCurrency());

                    MoneyAmount realIn = ARS_INFLATION.adjust(nominalIn, investment.getInvestmentDate(), investment.getOut().getDate());
                    String outDate = df.format(investment.getOut().getDate());

                    System.out.println(MessageFormat.format(message, outDate, this.nf.format(realIn.getAmount()), this.nf.format(nominalOut.getAmount().subtract(realIn.getAmount()))));

                }
            }
        }
    }

    //@Test
    public void usd() throws NoSeriesDataFoundException, ParseException {

        System.out.println("-----------------------------------");

        assertFalse(inv.isEmpty());

        final ForeignExchange usdToDollar = ForeignExchanges.getForeignExchange(Currency.getInstance("USD"), Currency.getInstance("ARS"));

        final String message = "{0}\t{1}\t{2}";
        final Date feb2016 = new SimpleDateFormat("dd/MM/yyyy").parse("28/02/2016");
        for (Investment investment : inv) {
            if (investment.getType().equals(InvestmentType.USD)) {
                assertTrue(investment instanceof CurrencyInvestment);
                MoneyAmount nominalIn = investment.getInvestedAmount();
                MoneyAmount realIn = ARS_INFLATION.adjust(nominalIn, investment.getInvestmentDate(), feb2016);

                MoneyAmount feb2016ARSValue = usdToDollar.exchange(
                        investment.getInvestmentValue(),
                        Currency.getInstance("ARS"), feb2016);

                System.out.println(
                        MessageFormat.format(
                                message,
                                investment.getInvestmentDate(),
                                this.nf.format(realIn.getAmount()),
                                this.nf.format(feb2016ARSValue.getAmount().subtract(realIn.getAmount()))));
            }
        }
    }
    


    

    private List<Investment> read(String name) throws IOException {
        try (InputStream in = InvestmentTest.class.getResourceAsStream("/" + name);) {
            ObjectMapper om = new ObjectMapper();

            return om.readValue(in, new TypeReference<List<Investment>>() {
            });
        }
    }

}
