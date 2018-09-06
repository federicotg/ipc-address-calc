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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.fede.calculator.money.ForeignExchanges;
import org.fede.calculator.money.Inflation;
import static org.fede.calculator.money.Inflation.ARS_INFLATION;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.InvestmentType;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
public class InvestmentTest {

    private final DateFormat df = DateFormat.getDateInstance();
    private final NumberFormat nf = NumberFormat.getNumberInstance();

    private final List<Investment> inv;

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
    public void pf() {

        assertFalse(inv.isEmpty());
        //final String message = "Invertí {0} el {1}. El {2} cobré {3}. En {4} del {5} puse {6} y recuperé {7}. Gané {8}";
        final String message = "{0}\t{1}\t{2}";

        for (Investment investment : inv) {
            if (investment.getType().equals(InvestmentType.PF)) {

                if (investment.getIn().getCurrency().equals(investment.getOut().getCurrency())
                        && investment.getIn().getCurrency().equals("ARS")) {
                    MoneyAmount nominalIn = new MoneyAmount(investment.getIn().getAmount(), investment.getIn().getCurrency());
                    MoneyAmount nominalOut = new MoneyAmount(investment.getOut().getAmount(), investment.getIn().getCurrency());

                    MoneyAmount realIn = ARS_INFLATION.adjust(nominalIn, investment.getIn().getDate(), investment.getOut().getDate());
                    String outDate = df.format(investment.getOut().getDate());

                    System.out.println(MessageFormat.format(message, outDate, this.nf.format(realIn.getAmount()), this.nf.format(nominalOut.getAmount().subtract(realIn.getAmount()))));

                }
            }
        }
    }

    @Test
    public void usd() throws ParseException {

        MoneyAmount oneDollar = new MoneyAmount(BigDecimal.ONE, "USD");

        MoneyAmount oneUSDIn1951PurchasingPower = Inflation.USD_INFLATION.adjust(oneDollar, 2016, 5, 1951, 9);

        MoneyAmount oneUSDIn1951PurchasingPowerInPesos = ForeignExchanges.getForeignExchange("USD", "ARS").exchange(
                oneUSDIn1951PurchasingPower,
                "ARS",
                2016,
                5);

        MoneyAmount pesosBackThen = Inflation.ARS_INFLATION.adjust(oneUSDIn1951PurchasingPowerInPesos, 2016, 5, 1951, 9);

    }

    private List<Investment> read(String name) throws IOException {
        try (InputStream in = InvestmentTest.class.getResourceAsStream("/" + name);) {
            ObjectMapper om = new ObjectMapper();

            return om.readValue(in, new TypeReference<List<Investment>>() {
            });
        }
    }

    @Test
    public void listStock() throws IOException {
        List<Investment> investmets = this.read("investments.json");

        final Collector<BigDecimal, ?, BigDecimal> reducer = Collectors.reducing(BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP), BigDecimal::add);

        final Collector<Investment, ?, BigDecimal> mapper = Collectors.mapping(inv -> inv.getMoneyAmount().getAmount().setScale(6, RoundingMode.HALF_UP), reducer);

        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMinimumFractionDigits(6);

        investmets.stream()
                .filter(Investment::isCurrent)
                .collect(Collectors.groupingBy(Investment::getCurrency, mapper))
                .entrySet()
                .stream()
                .forEach(e -> System.out.println(MessageFormat.format("{0}: {1}", e.getKey(), nf.format(e.getValue()))));

        Stream.concat(Stream.of(""),
                investmets.stream()
                        .filter(Investment::isCurrent)
                        .filter(inv -> "CAPLUSA".equals(inv.getCurrency()))
                        .map(this::format))
                .forEach(System.out::println);

    }

    private String format(Investment i) {
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMinimumFractionDigits(6);

        return MessageFormat.format("{0} {1}",
                i.getInitialDate(), nf.format(i.getMoneyAmount().getAmount().setScale(6, RoundingMode.HALF_UP)));

    }

}
