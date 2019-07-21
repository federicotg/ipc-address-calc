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
import java.io.PrintStream;
import java.math.BigDecimal;
import static java.math.BigDecimal.ZERO;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.MessageFormat;
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
import java.time.Month;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import static java.util.Map.entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.InvestmentEvent;
import org.fede.calculator.money.series.InvestmentType;
import static org.fede.calculator.money.series.InvestmentType.*;
import org.fede.calculator.money.series.YearMonth;
import org.fede.util.Pair;
import static org.fede.util.Pair.of;
import static org.fede.calculator.money.Inflation.USD_INFLATION;
import org.fede.calculator.money.series.IndexSeries;
import org.fede.calculator.money.series.SeriesReader;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
public class ConsoleReports {

    private static final TypeReference<List<Investment>> TR = new TypeReference<List<Investment>>() {
    };
    private static final Collector<BigDecimal, ?, BigDecimal> REDUCER = reducing(ZERO.setScale(6, RoundingMode.HALF_UP), BigDecimal::add);
    private static final Collector<Investment, ?, BigDecimal> MAPPER = mapping(inv -> inv.getMoneyAmount().getAmount().setScale(6, RoundingMode.HALF_UP), REDUCER);

    private final NumberFormat nf = NumberFormat.getNumberInstance();
    private final NumberFormat percentFormat = NumberFormat.getPercentInstance();
    private final List<Investment> investments;
    private final StringBuilder out;

    private ConsoleReports(StringBuilder out) {
        this.nf.setMaximumFractionDigits(2);
        this.percentFormat.setMinimumFractionDigits(2);
        this.investments = SeriesReader.read("investments.json", TR);
        this.out = out;
    }

    private void appendLine(String... texts) {
        Arrays.stream(texts)
                .forEach(out::append);
        out.append("\n");
    }

    private void separateTests() {
        this.appendLine("");
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
                .filter(Investment::isCurrent)
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
                .map(inv -> ForeignExchanges.exchange(inv, reportCurrency))
                .map(this::getAmount)
                .map(investedAmount -> ForeignExchanges.getForeignExchange(investedAmount.getCurrency(), reportCurrency).exchange(investedAmount, reportCurrency, limit.getYear(), limit.getMonth()))
                .reduce(MoneyAmount::add);
    }

    private void groupedInvestments() {
        final String reportCurrency = "USD";
        appendLine("===< Inversiones Actuales Agrupadas en ", reportCurrency, " >===");
        final YearMonth limit = USD_INFLATION.getTo();
        final Optional<MoneyAmount> total = this.total(Investment::isCurrent, reportCurrency, limit);
        investments.stream()
                .filter(Investment::isCurrent)
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
        if (investment.getInvestment().getCurrency().equals("LECAP")) {
            return "Renta Fija ARS";
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
        final Optional<MoneyAmount> total = this.total(Investment::isCurrent, reportCurrency, limit);

        investments.stream()
                .filter(Investment::isCurrent)
                .collect(groupingBy(
                        this::investmentType,
                        mapping(inv -> ForeignExchanges.getForeignExchange(inv.getInvestment().getCurrency(), reportCurrency)
                        .exchange(this.getAmount(inv), reportCurrency, limit.getYear(), limit.getMonth())
                        .getAmount()
                        .setScale(6, RoundingMode.HALF_UP),
                                REDUCER)))
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
                                .orElse(ZERO)));
    }

    private void pastInvestmentsProfit() {

        appendLine("===< Ganancia Inversiones Finalizadas en USD reales >===");

        this.pastInvestmentsRealProfit("CONBALA", FCI);
        this.pastInvestmentsRealProfit("CAPLUSA", FCI);
        this.pastInvestmentsRealProfit("USD", PF);
        this.pastInvestmentsRealProfit("UVA", PF);
        this.pastInvestmentsRealProfit("ARS", PF);
        this.pastInvestmentsRealProfit("LECAP", BONO);
        this.pastInvestmentsRealProfit("LETE", BONO);

    }

    private void currentInvestmentsRealProfit() {
        this.currentInvestmentsRealProfit(null, null);
    }

    private void currentInvestmentsRealProfit(String currency, InvestmentType type) {
        this.investmentsRealProfit(currency, type, Investment::isCurrent, false);
    }

    private void pastInvestmentsRealProfit(String currency, InvestmentType type) {
        this.investmentsRealProfit(currency, type, Investment::isPast, true);
    }

    private void investmentsRealProfit(String currency, InvestmentType type, Predicate<Investment> predicate, boolean totalOnly) {

        final String currencyText = Optional.ofNullable(currency).map(c -> format(" en {0}", c)).orElse("");

        if (!totalOnly) {
            if (type == null) {
                appendLine("===< Ganancia en Inversiones Actuales", currencyText, " en USD reales >===");

            } else {
                appendLine("===< Ganancia en Inversiones Actuales en ", type.toString(), currencyText, " en USD reales >===");
            }
        }

        this.investments.stream()
                .filter(i -> !totalOnly)
                .filter(predicate)
                .filter(i -> type == null || i.getType().equals(type))
                .filter(i -> currency == null || i.getCurrency().equals(currency))
                .sorted(Comparator.comparing(Investment::getInitialDate))
                .map(RealProfit::new)
                .map(RealProfit::toString)
                .forEach(this::appendLine);

        final BigDecimal total = this.totalSum(currency, type, RealProfit::getRealInitialAmount, predicate);
        final BigDecimal profit = this.totalSum(currency, type, RealProfit::getRealProfit, predicate);
        final BigDecimal pct = profit.divide(total, MathConstants.CONTEXT);

        this.appendLine(format("{4} TOTAL: {0,number,currency} => {1,number,currency} {2} {3}",
                total,
                profit,
                this.percentFormat.format(pct),
                RealProfit.plusMinus(pct),
                Stream.of(type, currencyText)
                        .filter(t -> totalOnly)
                        .filter(Objects::nonNull)
                        .map(Object::toString)
                        .collect(Collectors.joining(""))));
    }

    private BigDecimal totalSum(String currency, InvestmentType type, Function<RealProfit, MoneyAmount> totalFunction, Predicate<Investment> predicate) {

        return this.investments.stream()
                .filter(predicate)
                .filter(i -> type == null || i.getType().equals(type))
                .filter(i -> currency == null || i.getCurrency().equals(currency))
                .map(RealProfit::new)
                .map(totalFunction)
                .map(MoneyAmount::getAmount)
                .collect(Collectors.reducing(ZERO, BigDecimal::add));
    }

    private void printReport(PrintStream out) {
        out.println(this.out.toString());
    }

    private void fci(final int year) {

        final Map<String, String> fciNames = new HashMap<>(3);
        final Map<String, String> fciTypes = new HashMap<>(3);

        fciNames.put("CONAAFA", "Consultatio Acciones Argentina Clase A");
        fciNames.put("CONBALA", "Consultatio Balance Fund F.C.I. Clase A");
        fciNames.put("CAPLUSA", "Consultatio Ahorro Plus Argentina (Ahorro Plus A)");

        fciTypes.put("CONAAFA", "Renta variable en $");
        fciTypes.put("CONBALA", "Renta fija en $");
        fciTypes.put("CAPLUSA", "Renta fija en $");

        final String consultatioAssetManagement = "30-67726994-0";
        final String bancoDeValores = "30-57612427-5";

        final DateFormat df = DateFormat.getDateInstance();
        final NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(6);

        final IndexSeries conbala = SeriesReader.readIndexSeries("index/CONBALA_AR-peso.json");
        final IndexSeries conaafa = SeriesReader.readIndexSeries("index/CONAAFA_AR-peso.json");
        final IndexSeries caplusa = SeriesReader.readIndexSeries("index/CAPLUSA_AR-peso.json");

        final Map<String, BigDecimal> dicPreviousYearValues = new HashMap<>(3);
        dicPreviousYearValues.put("CONAAFA", conaafa.getIndex(year - 1, 12));
        dicPreviousYearValues.put("CONBALA", conbala.getIndex(year - 1, 12));
        dicPreviousYearValues.put("CAPLUSA", caplusa.getIndex(year - 1, 12));

        final Map<String, BigDecimal> dicValues = new HashMap<>(3);
        dicValues.put("CONAAFA", conaafa.getIndex(year, 12));
        dicValues.put("CONBALA", conbala.getIndex(year, 12));
        dicValues.put("CAPLUSA", caplusa.getIndex(year, 12));

        this.appendLine(
                Stream.of("Fecha de adquisición",
                        "Tipo de fondo",
                        "Denominación",
                        "CUIT Soc. Gerente",
                        "CUIT Soc. Depositaria",
                        "Cantidad",
                        "Valor cotización al 31/12/" + (year - 1),
                        "Valor cotización al 31/12/" + year).collect(Collectors.joining("\";\"", "\"", "\"")));

        this.investments.stream()
                .filter(inv -> InvestmentType.FCI.equals(inv.getType()))
                //.filter(inv -> inv.getInitialDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear() == year)
                .filter(inv -> this.currentIn(inv, year))
                .sorted(Comparator.comparing(Investment::getInitialDate, (left, right) -> left.compareTo(right)))
                .map(inv
                        -> MessageFormat.format(
                        "\"{0}\";\"{1}\";\"{2}\";\"{3}\";\"{4}\";\"{5}\";\"{6}\";\"{7}\"",
                        df.format(inv.getInitialDate()),
                        fciTypes.get(inv.getInvestment().getCurrency()),
                        fciNames.get(inv.getInvestment().getCurrency()),
                        consultatioAssetManagement,
                        bancoDeValores,
                        nf.format(inv.getInvestment().getAmount()),
                        nf.format(dicPreviousYearValues.get(inv.getInvestment().getCurrency())),
                        nf.format(dicValues.get(inv.getInvestment().getCurrency()))
                )).forEach(this::appendLine);

    }

    private boolean currentIn(Investment inv, int year) {

        final LocalDate reference = LocalDate.of(year, Month.DECEMBER, 31);
        final LocalDate buyDate = inv.getIn().getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        final LocalDate sellDate = Optional.ofNullable(inv.getOut())
                .map(e -> e.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                .orElse(LocalDate.of(2099, Month.DECEMBER, 31));

        return (buyDate.isBefore(reference) || buyDate.isEqual(reference))
                && sellDate.isAfter(reference);
    }

    public static void main(String[] args) {
        try {

            final ConsoleReports me = new ConsoleReports(new StringBuilder(1024));

            final Map<Pair<String, Integer>, Runnable> actions = Map.ofEntries(
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
                    entry(of("all", 11), me::currentInvestmentsRealProfit),
                    entry(of("bp", 12), () -> me.fci(2018)),
                    entry(of("gold", 13), () -> me.currentInvestmentsRealProfit("XAU", XAU))
            );

            final Set<String> params = Arrays.stream(args).map(String::toLowerCase).collect(Collectors.toSet());

            if (params.contains("help")) {
                System.out.println(actions.keySet()
                        .stream()
                        .sorted(Comparator.comparing(Pair::getSecond))
                        .map(Pair::getFirst)
                        .collect(Collectors.joining(", ")));
            } else {

                actions.entrySet()
                        .stream()
                        .filter(e -> params.isEmpty() || params.contains(e.getKey().getFirst().toLowerCase()))
                        .sorted(Comparator.comparing(e -> e.getKey().getSecond()))
                        .map(Map.Entry::getValue)
                        .forEach(r -> {
                            r.run();
                            me.separateTests();
                        });
                me.printReport(System.out);
            }
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            ex.printStackTrace(System.err);
        }
    }

}
