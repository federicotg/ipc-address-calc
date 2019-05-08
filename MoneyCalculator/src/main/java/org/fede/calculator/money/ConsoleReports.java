/*
 * Copyright (C) 2019 Federico Tello Gentile <federicotg@gmail.com>
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
package org.fede.calculator.money;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.mapping;
import static java.text.MessageFormat.format;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.InvestmentEvent;
import org.fede.calculator.money.series.InvestmentType;
import org.fede.calculator.money.series.YearMonth;
import org.fede.util.Pair;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
public class ConsoleReports {

    private static final Predicate<Investment> IS_CURRENT = Investment::isCurrent;
    private static final Predicate<Investment> IS_PAST = Investment::isPast;

    private final NumberFormat nf = NumberFormat.getNumberInstance();
    private final NumberFormat moneyFormat = NumberFormat.getCurrencyInstance();
    private final NumberFormat percentFormat = NumberFormat.getPercentInstance();

    private final Collector<BigDecimal, ?, BigDecimal> reducer = reducing(BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP), BigDecimal::add);

    private final Collector<Investment, ?, BigDecimal> mapper = mapping(inv -> inv.getMoneyAmount().getAmount().setScale(6, RoundingMode.HALF_UP), reducer);

    private final List<Investment> investments;

    public ConsoleReports() throws IOException {
        this.nf.setMaximumFractionDigits(2);
        this.percentFormat.setMinimumFractionDigits(2);
        this.investments = this.readExt("investments.json");
    }

    public void separateTests() {
        System.out.println("-----");
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

    public void listStock() throws IOException {

        System.out.println("Ahorros actuales agrupados por moneda.");

        NumberFormat sixDigits = NumberFormat.getNumberInstance();
        sixDigits.setMinimumFractionDigits(6);

        investments.stream()
                .filter(IS_CURRENT)
                .collect(groupingBy(inv -> Pair.of(inv.getType().toString(), inv.getCurrency()), mapper))
                .entrySet()
                .stream()
                .map(e -> Pair.of(e.getKey(), e.getValue()))
                .sorted(ConsoleReports::compareGroups)
                .map(e -> format("{0} {2}: {1}", e.getFirst().getFirst(), sixDigits.format(e.getSecond()), e.getFirst().getSecond()))
                .forEach(System.out::println);
    }

    private MoneyAmount getAmount(Investment i) {
        return Optional.ofNullable(i.getOut()).map(InvestmentEvent::getMoneyAmount).orElse(i.getInvestment().getMoneyAmount());
    }

    private Optional<MoneyAmount> total(Predicate<Investment> predicate, String reportCurrency, YearMonth limit) {
        return investments.stream()
                .filter(predicate)
                .map(this::getAmount)
                //.peek(ma -> System.out.println(ma.toString()))
                .map(ma -> ForeignExchanges.getForeignExchange(ma.getCurrency(), reportCurrency).exchange(ma, reportCurrency, limit.getYear(), limit.getMonth()))
                //.peek(ma -> System.out.println(ma.toString()))
                .reduce(MoneyAmount::add);
    }

    public void listStock2() throws IOException {
        final String reportCurrency = "USD";
        System.out.println("Inversiones Actuales en " + reportCurrency + " agrupadas.");
        YearMonth limit = Inflation.USD_INFLATION.getTo();
        final Optional<MoneyAmount> total = this.total(IS_CURRENT, reportCurrency, limit);
        investments.stream()
                .filter(IS_CURRENT)
                .collect(groupingBy(in -> Pair.of(in.getType().toString(), in.getCurrency()), mapper))
                .entrySet()
                .stream()
                .map(e -> Pair.of(e.getKey(), new MoneyAmount(e.getValue(), e.getKey().getSecond())))
                .map(p -> Pair.of(p.getFirst(), this.fx(p, reportCurrency)))
                .sorted(ConsoleReports::compareGroups)
                .map(pair -> this.formatReport(total, pair.getSecond(), pair.getFirst().getFirst(), pair.getFirst().getSecond()))
                .forEach(System.out::println);
        total
                .map(m -> format("Total: {0} -> {1}", m.getCurrency(), moneyFormat.format(m.getAmount())))
                .ifPresent(System.out::println);

    }

    private String investmentType(Investment investment) {
        if ("CONAAFA".equals(investment.getCurrency())) {
            return "Renta Variable ARS";
        }
        if (investment.getType().equals(InvestmentType.USD)) {
            return "Líquido";
        }

        if (investment.getType().equals(InvestmentType.XAU)) {
            return "Gold";
        }

        if (InvestmentType.LETE.equals(investment.getType()) || (investment.getType().equals(InvestmentType.PF) && investment.getCurrency().equals("USD"))) {
            return "Renta Fija USD";
        }

        return "Renta Fija ARS";
    }

    public void listStockByTpe() throws IOException {

        final String reportCurrency = "USD";
        System.out.println("Inversiones Actuales en " + reportCurrency + " por tipo.");

        final YearMonth limit = Inflation.USD_INFLATION.getTo();
        final Optional<MoneyAmount> total = this.total(IS_CURRENT, reportCurrency, limit);

        investments.stream()
                .filter(IS_CURRENT)
                .collect(groupingBy(
                        this::investmentType,
                        mapping(inv -> ForeignExchanges.exchange(inv, "USD").getMoneyAmount().getAmount().setScale(6, RoundingMode.HALF_UP), reducer)))
                .entrySet()
                .stream()
                .map(entry -> this.formatReport(total, new MoneyAmount(entry.getValue(), "USD"), entry.getKey(), "USD"))
                .forEach(System.out::println);
        total
                .map(m -> format("Total: {0} -> {1}", m.getCurrency(), moneyFormat.format(m.getAmount())))
                .ifPresent(System.out::println);

    }

    private MoneyAmount fx(Pair<Pair<String, String>, MoneyAmount> p, String reportCurrency) {

        YearMonth limit = Inflation.USD_INFLATION.getTo();

        return ForeignExchanges.getForeignExchange(p.getSecond().getCurrency(), reportCurrency).exchange(p.getSecond(), reportCurrency, limit.getYear(), limit.getMonth());
    }

    private String formatReport(Optional<MoneyAmount> total, MoneyAmount subtotal, String type, String currency) {
        return format("{0} {1}: {2}. {3}", type, currency, moneyFormat.format(subtotal.getAmount()),
                percentFormat
                        .format(total.map(tot -> subtotal.getAmount().divide(tot.getAmount(), MathContext.DECIMAL64))
                                .orElse(BigDecimal.ZERO)));
    }

    private void print(Investment in, String context) {
        if ("1".equals(in.getId())) {
            System.out.println(MessageFormat.format("{1} {0}", in, context));
        }
    }

    private BigDecimal asRealUSDProfit(Investment in) {

        this.print(in, "Inicial");

        Investment i = ForeignExchanges.exchange(in, "USD");

        this.print(i, "In USD");

        i = Inflation.USD_INFLATION.real(i);

        this.print(i, "Real USD");

        MoneyAmount profit = this.profit(i);

        if ("1".equals(i.getId())) {
            System.out.println("Profit " + profit.getCurrency() + " " + this.moneyFormat.format(profit.getAmount()));
        }
        return profit.getAmount();
    }

    public void pastInvestmentsProfit() throws IOException {

        final Collector<Investment, ?, BigDecimal> profitMapper = mapping(this::asRealUSDProfit, reducer);

        System.out.println("Ganancia Inversiones Finalizadas en USD reales");

        this.investments.stream()
                .filter(IS_PAST)
                .collect(groupingBy(this::typeAndCurrency, profitMapper))
                .entrySet()
                .stream()
                .map(entry -> format("{0} {1} {2}", entry.getKey().getFirst(), entry.getKey().getSecond(), moneyFormat.format(entry.getValue())))
                .forEach(System.out::println);

        investments.stream()
                .filter(IS_PAST)
                .map(this::asRealUSDProfit)
                .reduce(BigDecimal::add)
                .map(moneyFormat::format)
                .map(amount -> format("Total: {0}", amount))
                .ifPresent(System.out::println);
    }

    public void currentInvestmentsProfit() throws IOException {

        System.out.println("Ganancia Inversiones Actuales en USD reales");

        this.investments.stream()
                .filter(IS_CURRENT)
                .collect(groupingBy(this::typeAndCurrency, mapping(this::asRealUSDProfit, reducer)))
                .entrySet()
                .stream()
                .map(entry -> format("{0} {1} {2}", entry.getKey().getFirst(), entry.getKey().getSecond(), moneyFormat.format(entry.getValue())))
                .forEach(System.out::println);
    }

    private Pair<String, String> typeAndCurrency(Investment in) {
        return Pair.of(in.getType().toString(), in.getCurrency());
    }

    private BigDecimal days(Investment in) {
        return BigDecimal.valueOf(ChronoUnit.DAYS.between(
                LocalDate.ofInstant(in.getInitialDate().toInstant(), ZoneId.systemDefault()),
                LocalDate.now()));
    }

    private BigDecimal addInterest(Investment in) {
        return in.getInvestment().getAmount()
                .multiply(in.getInterest())
                .multiply(this.days(in))
                .setScale(12, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(ChronoUnit.YEARS.getDuration().toDays()), MathContext.DECIMAL64)
                .add(in.getInvestment().getMoneyAmount().getAmount());
    }

    private MoneyAmount profit(Investment in) {

        if (in.getOut() == null) {

            final BigDecimal currentAmount
                    = //Optional.<Investment>empty()
                    Optional.ofNullable(in)
                            .filter(inv -> inv.getInterest() != null)
                            .map(this::addInterest)
                            .orElse(in.getInvestment().getMoneyAmount().getAmount());

            return new MoneyAmount(
                    currentAmount.subtract(in.getIn().getMoneyAmount().getAmount()),
                    in.getInvestment().getMoneyAmount().getCurrency());

        }

        return new MoneyAmount(
                in.getOut().getMoneyAmount().getAmount().subtract(in.getIn().getMoneyAmount().getAmount()),
                in.getOut().getCurrency());
    }

    public static void main(String[] args) {
        try {
            ConsoleReports me = new ConsoleReports();
            me.pastInvestmentsProfit();
            me.separateTests();

            me.currentInvestmentsProfit();
            me.separateTests();

            me.listStock();
            me.separateTests();

            me.listStock2();
            me.separateTests();

            me.listStockByTpe();
            me.separateTests();

           // me.netMonthlyInvestment();
           // me.separateTests();

            //me.netYearlyInvestment();
            //ystem.err.println("*-------------*");
            // me.aa();
        } catch (IOException ioEx) {
            System.err.println(ioEx.getMessage());
            ioEx.printStackTrace(System.err);
        }
    }

    private Stream<Movement> movements(Investment investment, MoneyAmount zero) {
        
        return Stream.concat(
                Stream.of(new Movement(investment.getIn().getDate(), investment.getIn().getMoneyAmount())),
                Optional.ofNullable(investment.getOut())
                        .map(out -> new Movement(out.getDate(), zero.subtract(out.getMoneyAmount())))
                        .map(Stream::of)
                        .orElseGet(Stream::empty));
    }

    private YearMonth yearMonth(Movement movement) {
        return new YearMonth(movement.getDate());
    }

    private Integer year(Movement movement) {
        return new YearMonth(movement.getDate()).getYear();
    }

    public void netMonthlyInvestment() {

        final String currency = "USD";
        System.out.println("Inversión Mensual en " + currency);

        final MoneyAmount zero = new MoneyAmount(BigDecimal.ZERO, currency);
        
        
        Map<YearMonth, MoneyAmount> investmentsByMonth = this.investments
                .stream()
                .filter(investment -> !(investment.getType().equals(InvestmentType.PF) && investment.getInvestment().getCurrency().equals("USD")))
                .filter(investment -> !investment.getType().equals(InvestmentType.LETE))
                .map(investment -> ForeignExchanges.exchange(investment, currency))
                .flatMap(investment -> this.movements(investment, zero))
                .collect(Collectors.groupingBy(this::yearMonth, Collectors.reducing(zero, Movement::getAmount, MoneyAmount::add)));

        investmentsByMonth
                .entrySet()
                .stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(e -> MessageFormat.format("{0}-{1} => {2} {3}", Integer.toString(e.getKey().getYear()), e.getKey().getMonth(), e.getValue().getCurrency(), moneyFormat.format(e.getValue().getAmount())))
                .forEach(System.out::println);

    }

    public void netYearlyInvestment() {

        final String currency = "USD";
        System.out.println("Inversión Anual en " + currency);

        final MoneyAmount zero = new MoneyAmount(BigDecimal.ZERO, currency);
        
        Map<Integer, MoneyAmount> investmentsByYear = this.investments
                .stream()
                .filter(investment -> !(investment.getType().equals(InvestmentType.PF) && investment.getInvestment().getCurrency().equals("USD")))
                .filter(investment -> !investment.getType().equals(InvestmentType.LETE))
                .map(investment -> ForeignExchanges.exchange(investment, currency))
                .flatMap(investment -> this.movements(investment, zero))
                .collect(Collectors.groupingBy(this::year, Collectors.reducing(zero, Movement::getAmount, MoneyAmount::add)));

        investmentsByYear
                .entrySet()
                .stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(e -> MessageFormat.format("{0} => {1} {2}", Integer.toString(e.getKey()), e.getValue().getCurrency(), moneyFormat.format(e.getValue().getAmount())))
                .forEach(System.out::println);

    }

}
