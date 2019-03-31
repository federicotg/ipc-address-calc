
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.mapping;
import static java.text.MessageFormat.format;

import org.fede.calculator.money.ForeignExchange;
import org.fede.calculator.money.ForeignExchanges;
import org.fede.calculator.money.Inflation;
import static org.fede.calculator.money.Inflation.ARS_INFLATION;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.InvestmentEvent;
import org.fede.calculator.money.series.InvestmentType;
import org.fede.util.Pair;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
public class InvestmentTest {

    private final DateFormat df = DateFormat.getDateInstance();
    private final NumberFormat nf = NumberFormat.getNumberInstance();
    private final NumberFormat moneyFormat = NumberFormat.getCurrencyInstance();
    private final NumberFormat percentFormat = NumberFormat.getPercentInstance();

    private final Collector<BigDecimal, ?, BigDecimal> reducer = reducing(BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP), BigDecimal::add);

    private final Collector<Investment, ?, BigDecimal> mapper = mapping(inv -> inv.getMoneyAmount().getAmount().setScale(6, RoundingMode.HALF_UP), reducer);

    private final List<Investment> inv;

    private final List<Investment> investments;

    public InvestmentTest() throws IOException {
        this.inv = this.read("investments-test.json");
        this.nf.setMaximumFractionDigits(2);
        this.percentFormat.setMinimumFractionDigits(2);
        this.investments = this.readExt("investments.json");
    }

    @Before
    public void separateTests() {
        System.out.println("");
    }

    // @Test
    public void pf() {

        assertFalse(inv.isEmpty());
        // final String message = "Invertí {0} el {1}. El {2} cobré {3}. En {4} del {5}
        // puse {6} y recuperé {7}. Gané {8}";
        final String message = "{0}\t{1}\t{2}";

        for (Investment investment : inv) {
            if (investment.getType().equals(InvestmentType.PF)) {

                if (investment.getIn().getCurrency().equals(investment.getOut().getCurrency())
                        && investment.getIn().getCurrency().equals("ARS")) {
                    MoneyAmount nominalIn = new MoneyAmount(investment.getIn().getAmount(),
                            investment.getIn().getCurrency());
                    MoneyAmount nominalOut = new MoneyAmount(investment.getOut().getAmount(),
                            investment.getIn().getCurrency());

                    MoneyAmount realIn = ARS_INFLATION.adjust(nominalIn, investment.getIn().getDate(),
                            investment.getOut().getDate());
                    String outDate = df.format(investment.getOut().getDate());

                    System.out.println(MessageFormat.format(message, outDate, this.nf.format(realIn.getAmount()),
                            this.nf.format(nominalOut.getAmount().subtract(realIn.getAmount()))));

                }
            }
        }
    }

    @Test
    public void usd() throws ParseException {

        MoneyAmount oneDollar = new MoneyAmount(BigDecimal.ONE, "USD");

        MoneyAmount oneUSDIn1951PurchasingPower = Inflation.USD_INFLATION.adjust(oneDollar, 2016, 5, 1951, 9);

        MoneyAmount oneUSDIn1951PurchasingPowerInPesos = ForeignExchanges.getForeignExchange("USD", "ARS")
                .exchange(oneUSDIn1951PurchasingPower, "ARS", 2016, 5);

        MoneyAmount pesosBackThen = Inflation.ARS_INFLATION.adjust(oneUSDIn1951PurchasingPowerInPesos, 2016, 5, 1951,
                9);

    }

    private List<Investment> read(String name) throws IOException {
        try (InputStream in = InvestmentTest.class.getResourceAsStream("/" + name);) {
            ObjectMapper om = new ObjectMapper();

            return om.readValue(in, new TypeReference<List<Investment>>() {
            });
        }
    }

    private List<Investment> readExt(String name) throws IOException {
        try (InputStream in = new FileInputStream("/home/fede/Sync/app-resources/" + name);) {
            ObjectMapper om = new ObjectMapper();

            return om.readValue(in, new TypeReference<List<Investment>>() {
            });
        }
    }

    private static int compareGroups(Pair<Pair<String, String>, ?> left, Pair<Pair<String, String>, ?> right) {
        int comparison = left.getFirst().getFirst().compareTo(right.getFirst().getFirst());
        return comparison != 0 ? comparison : left.getFirst().getSecond().compareTo(right.getFirst().getSecond());
    }

    @Test
    public void listStock() throws IOException {

        System.out.println("Ahorros actuales agrupados por moneda.");

        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMinimumFractionDigits(6);

        investments.stream()
                .filter(Investment::isCurrent)
                .collect(groupingBy(inv -> Pair.of(inv.getType().toString(), inv.getCurrency()), mapper))
                .entrySet()
                .stream()
                .map(e -> Pair.of(e.getKey(), e.getValue()))
                .sorted(InvestmentTest::compareGroups)
                .map(e -> format("{0} {2}: {1}", e.getFirst().getFirst(), nf.format(e.getSecond()), e.getFirst().getSecond()))
                .forEach(System.out::println);

    }

    @Test
    public void listStock2() throws IOException {

        final String reportCurrency = "USD";

        System.out.println("Inversiones Actuales en " + reportCurrency + " agrupadas.");

        final Optional<MoneyAmount> total = investments.stream()
                .filter(Investment::isCurrent)
                .collect(groupingBy(inv -> Pair.of(inv.getType().toString(), inv.getCurrency()), mapper))
                .entrySet()
                .stream()
                .map(e -> Pair.of(e.getKey().getFirst(), new MoneyAmount(e.getValue(), e.getKey().getSecond())))
                .map(p -> ForeignExchanges.getForeignExchange(p.getSecond().getCurrency(), reportCurrency)
                .exchange(p.getSecond(), reportCurrency, new Date()))
                .reduce(MoneyAmount::add);

        investments.stream()
                .filter(Investment::isCurrent)
                .collect(groupingBy(inv -> Pair.of(inv.getType().toString(), inv.getCurrency()), mapper))
                .entrySet()
                .stream()
                .map(e -> Pair.of(e.getKey(), new MoneyAmount(e.getValue(), e.getKey().getSecond())))
                .map(p -> Pair.of(p.getFirst(), this.fx(p, reportCurrency)))
                .sorted(InvestmentTest::compareGroups)
                .map(pair -> this.formatReport(total, pair.getSecond(), pair.getFirst().getFirst(), pair.getFirst().getSecond()))
                .forEach(System.out::println);

        total
                .map(m -> format("Total:\n{0} -> {1}", m.getCurrency(), moneyFormat.format(m.getAmount())))
                .ifPresent(System.out::println);

        /*
         * Stream.concat( Stream.of(""), investmets .stream()
         * .filter(Investment::isCurrent) .filter(i ->
         * "CAPLUSA".equals(i.getCurrency())).map(this::format)
         * ).forEach(System.out::println);
         */
    }
    
    private MoneyAmount fx(Pair<Pair<String, String>, MoneyAmount> p, String reportCurrency){
        return ForeignExchanges.getForeignExchange(p.getSecond().getCurrency(), reportCurrency).exchange(p.getSecond(), reportCurrency, new Date());
    }

    // private String format(Investment i) {
    // NumberFormat nf = NumberFormat.getNumberInstance();
    // nf.setMinimumFractionDigits(6);
    //
    // return MessageFormat.format("{0} {1}", i.getInitialDate(),
    // nf.format(i.getMoneyAmount().getAmount().setScale(6, RoundingMode.HALF_UP)));
    //
    // }
    private String formatReport(Optional<MoneyAmount> total, MoneyAmount subtotal, String type, String currency) {
        return format("{0} {1}: {2}. {3}", type, currency, moneyFormat.format(subtotal.getAmount()),
                percentFormat
                        .format(total.map(tot -> subtotal.getAmount().divide(tot.getAmount(), MathContext.DECIMAL64))
                                .orElse(BigDecimal.ZERO)));
    }

    @Test
    public void profit() throws IOException {
        List<Investment> investmets = this.readExt("investments.json");

        System.out.println("Renta de PF 2018: " + this.profit(investmets, InvestmentType.PF, 2018));
        System.out.println("Renta de FCI 2018: " + this.profit(investmets, InvestmentType.FCI, 2018));
    }

    private BigDecimal profit(List<Investment> investmets, InvestmentType it, int year) {
        return investmets
                .stream()
                .filter(inv -> inv.getOut() != null && inv.getOut().getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear() == year)
                .filter(inv -> inv.getType().equals(it))
                .map(inv -> inv.getOut().getAmount().subtract(inv.getIn().getAmount()))
                .collect(reducing(BigDecimal.ZERO, BigDecimal::add));

    }

    @Test
    public void pastInvestments() throws IOException {

        final Collector<Investment, ?, BigDecimal> profitMapper = mapping(inv -> this.profit(inv).getAmount(), reducer);

        System.out.println("Inversiones Finalizadas USD reales");

        this.investments.stream()
                .filter(inv -> !inv.isCurrent())
                .map(inv -> this.exchangeInto(inv, "USD"))
                .map(this::realUSD)
                .collect(groupingBy(this::typeAndCurrency, profitMapper))
                .entrySet()
                .stream()
                .map(entry -> format("{0} {1} {2}", entry.getKey().getFirst(), entry.getKey().getSecond(), moneyFormat.format(entry.getValue())))
                .forEach(System.out::println);

        investments.stream()
                .filter(inv -> !inv.isCurrent())
                .map(inv -> this.exchangeInto(inv, "USD"))
                .map(this::realUSD)
                .map(this::profit)
                .map(MoneyAmount::getAmount)
                .reduce(BigDecimal::add)
                .map(moneyFormat::format)
                .map(amount -> format("Total: {0}", amount))
                .ifPresent(System.out::println);

    }

    private Pair<String, String> typeAndCurrency(Investment inv) {
        return Pair.of(inv.getType().toString(), inv.getCurrency());
    }

    private MoneyAmount profit(Investment inv) {
        return new MoneyAmount(
                inv.getOut().getMoneyAmount().getAmount().subtract(inv.getIn().getMoneyAmount().getAmount()),
                inv.getOut().getCurrency());
    }

    private Investment realUSD(Investment inv) {

        Investment answer = new Investment();
        answer.setIn(this.realUSD(inv.getIn()));
        answer.setOut(this.realUSD(inv.getOut()));
        answer.setType(inv.getType());
        answer.setInvestment(inv.getInvestment());
        answer.setInterest(inv.getInterest());

        return answer;

    }

    private InvestmentEvent realUSD(InvestmentEvent in) {
        InvestmentEvent answer = new InvestmentEvent();
        MoneyAmount adjusted = Inflation.USD_INFLATION.adjust(in.getMoneyAmount(), in.getDate(), new Date());
        answer.setCurrency(adjusted.getCurrency());
        answer.setAmount(adjusted.getAmount());
        answer.setDate(in.getDate());
        return answer;
    }

    private Investment exchangeInto(Investment inv, String currency) {
        Investment answer = new Investment();
        answer.setIn(this.exchangeInto(inv.getIn(), currency));
        answer.setOut(this.exchangeInto(inv.getOut(), currency));
        answer.setType(inv.getType());
        answer.setInvestment(inv.getInvestment());
        answer.setInterest(inv.getInterest());

        return answer;

    }

    private InvestmentEvent exchangeInto(InvestmentEvent in, String currency) {

        ForeignExchange fx = ForeignExchanges.getForeignExchange(in.getCurrency(), currency);

        InvestmentEvent answer = new InvestmentEvent();

        MoneyAmount ma = fx.exchange(in.getMoneyAmount(), currency, in.getDate());
        answer.setAmount(ma.getAmount());
        answer.setCurrency(ma.getCurrency());
        answer.setDate(in.getDate());
        return answer;
    }

}
