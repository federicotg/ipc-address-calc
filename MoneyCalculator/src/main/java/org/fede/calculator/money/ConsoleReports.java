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
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Comparator;
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
import java.util.Arrays;
import java.util.Map;
import static java.util.Map.entry;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.InvestmentEvent;
import org.fede.calculator.money.series.InvestmentType;
import static org.fede.calculator.money.series.InvestmentType.*;
import org.fede.calculator.money.series.YearMonth;
import org.fede.util.Pair;
import static org.fede.util.Pair.of;
import static org.fede.calculator.money.Inflation.USD_INFLATION;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
public class ConsoleReports {

    private static final TypeReference<List<Investment>> TR = new TypeReference<List<Investment>>() {
    };
    private static final Predicate<Investment> IS_CURRENT = Investment::isCurrent;
    private static final Predicate<Investment> IS_PAST = Investment::isPast;
    private static final Collector<BigDecimal, ?, BigDecimal> REDUCER = reducing(BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP), BigDecimal::add);
    private static final Collector<Investment, ?, BigDecimal> MAPPER = mapping(inv -> inv.getMoneyAmount().getAmount().setScale(6, RoundingMode.HALF_UP), REDUCER);

    private final NumberFormat nf = NumberFormat.getNumberInstance();
    private final NumberFormat percentFormat = NumberFormat.getPercentInstance();
    private final List<Investment> investments;
    private final StringBuilder out;

    private ConsoleReports(StringBuilder out) throws IOException {
        this.nf.setMaximumFractionDigits(2);
        this.percentFormat.setMinimumFractionDigits(2);
        this.investments = this.readExt("investments.json");
        this.out = out;
    }

    private void appendLine(String... texts) {
        Arrays.stream(texts)
                .forEach(out::append);
        out.append("\n");
    }

    private void separateTests() {
        this.appendLine("-----");
    }

    private List<Investment> readExt(String name) throws IOException {
        try (InputStream in = new FileInputStream("/home/fede/Sync/app-resources/" + name);) {
            return new ObjectMapper().readValue(in, TR);
        }
    }

    private static int compareGroups(Pair<Pair<String, String>, ?> left, Pair<Pair<String, String>, ?> right) {
        int comparison = left.getFirst().getFirst().compareTo(right.getFirst().getFirst());
        return comparison != 0 ? comparison : left.getFirst().getSecond().compareTo(right.getFirst().getSecond());
    }

    private void investments() {

        appendLine("===< Inversiones actuales agrupados por moneda >===");

        NumberFormat sixDigits = NumberFormat.getNumberInstance();
        sixDigits.setMinimumFractionDigits(6);

        investments.stream()
                .filter(IS_CURRENT)
                .collect(groupingBy(inv -> of(inv.getType().toString(), inv.getCurrency()), MAPPER))
                .entrySet()
                .stream()
                .map(e -> of(e.getKey(), e.getValue()))
                .sorted(ConsoleReports::compareGroups)
                .map(e -> format("{0} {2}: {1}", e.getFirst().getFirst(), sixDigits.format(e.getSecond()), e.getFirst().getSecond()))
                .forEach(this::appendLine);
    }

    private MoneyAmount getAmount(Investment i) {
        return Optional.ofNullable(i.getOut()).map(InvestmentEvent::getMoneyAmount).orElse(i.getInvestment().getMoneyAmount());
    }

    private Optional<MoneyAmount> total(Predicate<Investment> predicate, String reportCurrency, YearMonth limit) {
        return investments.stream()
                .filter(predicate)
                .map(this::getAmount)
                .map(ma -> ForeignExchanges.getForeignExchange(ma.getCurrency(), reportCurrency).exchange(ma, reportCurrency, limit.getYear(), limit.getMonth()))
                .reduce(MoneyAmount::add);
    }

    private void groupedInvestments() {
        final String reportCurrency = "USD";
        appendLine("===< Inversiones Actuales Agrupadas en ", reportCurrency, " >===");
        final YearMonth limit = USD_INFLATION.getTo();
        final Optional<MoneyAmount> total = this.total(IS_CURRENT, reportCurrency, limit);
        investments.stream()
                .filter(IS_CURRENT)
                .collect(groupingBy(in -> of(in.getType().toString(), in.getCurrency()), MAPPER))
                .entrySet()
                .stream()
                .map(e -> of(e.getKey(), new MoneyAmount(e.getValue(), e.getKey().getSecond())))
                .map(p -> of(p.getFirst(), this.fx(p, reportCurrency)))
                .sorted(ConsoleReports::compareGroups)
                .map(pair -> this.formatReport(total, pair.getSecond(), pair.getFirst().getFirst(), pair.getFirst().getSecond()))
                .forEach(this::appendLine);
        total
                .map(m -> format("Total: {0} -> {1,number,currency}", m.getCurrency(), m.getAmount()))
                .ifPresent(this::appendLine);

    }

    private String investmentType(Investment investment) {
        if ("CONAAFA".equals(investment.getCurrency())) {
            return "Renta Variable ARS";
        }
        if (investment.getType().equals(USD)) {
            return "Líquido";
        }

        if (investment.getType().equals(XAU)) {
            return "Gold";
        }

        if (BONO.equals(investment.getType())
                || (PF.equals(investment.getType()) && investment.getCurrency().equals("USD"))) {
            return "Renta Fija USD";
        }

        return "Renta Fija ARS";
    }

    private void listStockByTpe() {

        final String reportCurrency = "USD";
        appendLine("===< Inversiones Actuales en ", reportCurrency, " por tipo >===");

        final YearMonth limit = USD_INFLATION.getTo();
        final Optional<MoneyAmount> total = this.total(IS_CURRENT, reportCurrency, limit);

        investments.stream()
                .filter(IS_CURRENT)
                .collect(groupingBy(
                        this::investmentType,
                        mapping(inv -> ForeignExchanges.exchange(inv, reportCurrency).getMoneyAmount().getAmount().setScale(6, RoundingMode.HALF_UP), REDUCER)))
                .entrySet()
                .stream()
                .map(entry -> this.formatReport(total, new MoneyAmount(entry.getValue(), reportCurrency), entry.getKey(), reportCurrency))
                .forEach(this::appendLine);
        total
                .map(m -> format("Total: {0} -> {1,number,currency}", m.getCurrency(), m.getAmount()))
                .ifPresent(this::appendLine);

    }

    private MoneyAmount fx(Pair<Pair<String, String>, MoneyAmount> p, String reportCurrency) {

        YearMonth limit = USD_INFLATION.getTo();

        return ForeignExchanges.getForeignExchange(p.getSecond().getCurrency(), reportCurrency).exchange(p.getSecond(), reportCurrency, limit.getYear(), limit.getMonth());
    }

    private String formatReport(Optional<MoneyAmount> total, MoneyAmount subtotal, String type, String currency) {
        return format("{0} {1}: {2,number,currency}. {3}", type, currency, subtotal.getAmount(),
                percentFormat
                        .format(total.map(tot -> subtotal.getAmount().divide(tot.getAmount(), MathConstants.CONTEXT))
                                .orElse(BigDecimal.ZERO)));
    }

    private BigDecimal asRealUSDProfit(Investment in) {

        Investment i = ForeignExchanges.exchange(in, "USD");
        i = USD_INFLATION.real(i);
        MoneyAmount profit = this.profit(i);
        return profit.getAmount();
    }

    private void pastInvestmentsProfit() {

        final Collector<Investment, ?, BigDecimal> profitMapper = mapping(this::asRealUSDProfit, REDUCER);

        appendLine("===< Ganancia Inversiones Finalizadas en USD reales >===");

        this.investments.stream()
                .filter(IS_PAST)
                .collect(groupingBy(this::typeAndCurrency, profitMapper))
                .entrySet()
                .stream()
                .map(entry -> format("{0} {1} {2,number,currency}", entry.getKey().getFirst(), entry.getKey().getSecond(), entry.getValue()))
                .forEach(this::appendLine);

        investments.stream()
                .filter(IS_PAST)
                .map(this::asRealUSDProfit)
                .reduce(BigDecimal::add)
                .map(amount -> format("Total: {0,number,currency}", amount))
                .ifPresent(this::appendLine);
    }

    private void currentInvestmentsRealProfit() {
        this.currentInvestmentsRealProfit(null, null);
    }

    private void currentInvestmentsRealProfit(String currency, InvestmentType type) {

        final String currencyText = Optional.ofNullable(currency).map(c -> format(" en {0}", c)).orElse("");

        if (type == null) {
            appendLine("===< Ganancia en Inversiones Actuales", currencyText, " en USD reales >===");

        } else {
            appendLine("===< Ganancia en Inversiones Actuales en ", type.toString(), currencyText, " en USD reales >===");
        }

        this.investments.stream()
                .filter(IS_CURRENT)
                .filter(i -> type == null || i.getType().equals(type))
                .filter(i -> currency == null || i.getCurrency().equals(currency))
                .sorted(Comparator.comparing(Investment::getInitialDate))
                .map(RealProfit::new)
                .map(RealProfit::toString)
                .forEach(this::appendLine);

        final BigDecimal total = this.totalSum(currency, type, RealProfit::getRealInitialAmount);
        final BigDecimal profit = this.totalSum(currency, type, RealProfit::getRealProfit);
        final BigDecimal pct = profit.divide(total, MathConstants.CONTEXT);

        this.appendLine(format("TOTAL: {0,number,currency} => {1,number,currency} {2} {3}",
                total,
                profit,
                this.percentFormat.format(pct),
                RealProfit.plusMinus(pct)
        ));
    }

    private BigDecimal totalSum(String currency, InvestmentType type, Function<RealProfit, MoneyAmount> totalFunction) {

        return this.investments.stream()
                .filter(IS_CURRENT)
                .filter(i -> type == null || i.getType().equals(type))
                .filter(i -> currency == null || i.getCurrency().equals(currency))
                .map(RealProfit::new)
                .map(totalFunction)
                .map(MoneyAmount::getAmount)
                .collect(Collectors.reducing(BigDecimal.ZERO, BigDecimal::add));
    }

    private Pair<String, String> typeAndCurrency(Investment in) {
        return of(in.getType().toString(), in.getCurrency());
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
                .divide(BigDecimal.valueOf(ChronoUnit.YEARS.getDuration().toDays()), MathConstants.CONTEXT)
                .add(in.getInvestment().getMoneyAmount().getAmount());
    }

    private MoneyAmount profit(Investment in) {

        if (in.getOut() == null) {

            final BigDecimal currentAmount = Optional.ofNullable(in)
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

    private void printReport(PrintStream out) {
        out.println(this.out.toString());
    }

    public static void main(String[] args) {
        try {

            final ConsoleReports me = new ConsoleReports(new StringBuilder(1024));

            Map<Pair<String, Integer>, Runnable> actions = Map.ofEntries(
                    entry(of("past", 0), me::pastInvestmentsProfit),
                    entry(of("i", 1), me::investments),
                    entry(of("gi", 2), me::groupedInvestments),
                    entry(of("ti", 3), me::listStockByTpe),
                    entry(of("UVA", 4), () -> me.currentInvestmentsRealProfit("UVA", PF)),
                    entry(of("LETE", 5), () -> me.currentInvestmentsRealProfit("LETE", BONO)),
                    entry(of("LECAP", 6), () -> me.currentInvestmentsRealProfit("LECAP", BONO)),
                    entry(of("AY24", 7), () -> me.currentInvestmentsRealProfit("AY24", BONO)),
                    entry(of("USD", 8), () -> me.currentInvestmentsRealProfit("USD", BONO)),
                    entry(of("CONAAFA", 9), () -> me.currentInvestmentsRealProfit("CONAAFA", FCI)),
                    entry(of("USD", 10), () -> me.currentInvestmentsRealProfit("USD", PF)),
                    entry(of("all", 11), me::currentInvestmentsRealProfit));

            final Set<String> params = Arrays.stream(args).map(String::toLowerCase).collect(Collectors.toSet());

            actions.entrySet().stream()
                    .filter(e -> params.isEmpty() || params.contains(e.getKey().getFirst().toLowerCase()))
                    .sorted(Comparator.comparing(e -> e.getKey().getSecond()))
                    .map(Map.Entry::getValue)
                    .forEach(r -> {
                        r.run();
                        me.separateTests();
                    });
            me.printReport(System.out);

        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            ex.printStackTrace(System.err);
        }
    }

}
