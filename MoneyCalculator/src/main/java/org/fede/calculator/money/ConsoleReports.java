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
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.mapping;
import static java.text.MessageFormat.format;
import java.util.Arrays;
import java.util.Map;
import static java.util.Map.entry;
import java.util.Objects;
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
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.SeriesReader;
import static org.fede.calculator.money.series.SeriesReader.readSeries;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
public class ConsoleReports {

    private static final String REPORT_FORMAT = "\"{0}\";\"{1}\";\"{2}\";\"{3}\";\"{4}\";\"{5}\";\"{6}\";\"{7}\"";

    private static final Map<String, String> FCI_NAMES = Map.of(
            "CONAAFA", "Consultatio Acciones Argentina Clase A",
            "CONBALA", "Consultatio Balance Fund F.C.I. Clase A",
            "CAPLUSA", "Consultatio Ahorro Plus Argentina (Ahorro Plus A)");

    private static final Map<String, String> FCI_TYPES = Map.of(
            "CONAAFA", "Renta variable en $",
            "CONBALA", "Renta fija en $",
            "CAPLUSA", "Renta fija en $");

    private static final String CONSULTATIO_ASSET_MANAGEMENT_CUIT = "30-67726994-0";
    private static final String BANCO_DE_VALORES_CUIT = "30-57612427-5";

    private static final TypeReference<List<Investment>> TR = new TypeReference<List<Investment>>() {
    };
    private static final Collector<BigDecimal, ?, BigDecimal> REDUCER = reducing(ZERO.setScale(6, RoundingMode.HALF_UP), BigDecimal::add);
    private static final Collector<Investment, ?, BigDecimal> MAPPER = mapping(inv -> inv.getMoneyAmount().getAmount().setScale(6, RoundingMode.HALF_UP), REDUCER);

    private static final Comparator<Pair<Pair<String, String>, ?>> TYPE_CURRENCY_COMPARATOR = Comparator.comparing((Pair<Pair<String, String>, ?> pair) -> pair.getFirst().getFirst())
            .thenComparing(Comparator.comparing(pair -> pair.getFirst().getSecond()));

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

    private void investments() {

        appendLine("===< Inversiones actuales agrupados por moneda >===");

        final NumberFormat sixDigits = NumberFormat.getNumberInstance();
        sixDigits.setMinimumFractionDigits(6);

        investments.stream()
                .filter(Investment::isCurrent)
                .collect(groupingBy(inv -> of(inv.getType().toString(), inv.getCurrency()), MAPPER))
                .entrySet()
                .stream()
                .map(e -> of(e.getKey(), e.getValue()))
                .sorted(TYPE_CURRENCY_COMPARATOR)
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
        final var reportCurrency = "USD";
        appendLine("===< Inversiones Actuales Agrupadas en ", reportCurrency, " >===");
        final var limit = USD_INFLATION.getTo();
        final var total = this.total(Investment::isCurrent, reportCurrency, limit);
        investments.stream()
                .filter(Investment::isCurrent)
                .collect(groupingBy(in -> of(in.getType().toString(), in.getCurrency()), MAPPER))
                .entrySet()
                .stream()
                .map(e -> of(e.getKey(), new MoneyAmount(e.getValue(), e.getKey().getSecond())))
                .map(p -> of(p.getFirst(), this.fx(p, reportCurrency)))
                .sorted(TYPE_CURRENCY_COMPARATOR)
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
        if (investment.getInvestment().getCurrency().equals("CSPX")) {
            return "Renta Variable USD";
        }
        if (BONO.equals(investment.getType())
                || (PF.equals(investment.getType()) && investment.getCurrency().equals("USD"))) {
            return "Renta Fija USD";
        }
        return "Renta Fija ARS";
    }

    private void listStockByTpe() {

        final var reportCurrency = "USD";
        appendLine("===< Inversiones Actuales en ", reportCurrency, " por tipo >===");

        final var limit = USD_INFLATION.getTo();
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

        final var limit = USD_INFLATION.getTo();

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

    private void pastInvestmentsRealProfit() {
        this.investmentsRealProfit(null, null, Investment::isPast, true);
    }

    private void globalInvestmentsRealProfit() {
        this.investmentsRealProfit(null, null, (inv) -> true, true);
    }

    private void currentInvestmentsRealProfit(String currency, InvestmentType type) {
        this.investmentsRealProfit(currency, type, Investment::isCurrent, false);
    }

    private void pastInvestmentsRealProfit(String currency, InvestmentType type) {
        this.investmentsRealProfit(currency, type, Investment::isPast, true);
    }

    private void investmentsRealProfit(String currency, InvestmentType type, Predicate<Investment> predicate, boolean totalOnly) {

        final var currencyText = Optional.ofNullable(currency).map(c -> format(" {0}", c)).orElse("");

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
        final BigDecimal pct = total.compareTo(BigDecimal.ZERO) > 0
                ? profit.divide(total, MathConstants.CONTEXT)
                : BigDecimal.ZERO;

        this.appendLine(format("{4}TOTAL: {0,number,currency} => {1,number,currency} {2} {3}",
                total,
                profit,
                this.percentFormat.format(pct),
                RealProfit.plusMinus(pct),
                Stream.of(type, currencyText)
                        .filter(t -> totalOnly)
                        .filter(Objects::nonNull)
                        .map(Object::toString)
                        .collect(Collectors.joining("", "", " "))));
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

        final var df = DateFormat.getDateInstance();
        final var nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(6);

        final var conbala = SeriesReader.readIndexSeries("index/CONBALA_AR-peso.json");
        final var conaafa = SeriesReader.readIndexSeries("index/CONAAFA_AR-peso.json");
        final var caplusa = SeriesReader.readIndexSeries("index/CAPLUSA_AR-peso.json");

        final var dicPreviousYearValues = Map.of(
                "CONAAFA", conaafa.getIndex(year - 1, 12),
                "CONBALA", conbala.getIndex(year - 1, 12),
                "CAPLUSA", caplusa.getIndex(year - 1, 12));

        final var dicValues = Map.of(
                "CONAAFA", conaafa.getIndex(year, 12),
                "CONBALA", conbala.getIndex(year, 12),
                "CAPLUSA", caplusa.getIndex(year, 12));

        this.appendLine(
                format(REPORT_FORMAT,
                        Stream.of("Fecha de adquisición",
                                "Tipo de fondo",
                                "Denominación",
                                "CUIT Soc. Gerente",
                                "CUIT Soc. Depositaria",
                                "Cantidad",
                                "Valor cotización al 31/12/" + (year - 1),
                                "Valor cotización al 31/12/" + year)
                                .toArray(Object[]::new)));

        this.investments.stream()
                .filter(inv -> FCI.equals(inv.getType()))
                .filter(inv -> inv.currentInYear(year))
                .sorted(Comparator.comparing(Investment::getInitialDate, (left, right) -> left.compareTo(right)))
                .map(inv
                        -> format(REPORT_FORMAT,
                        df.format(inv.getInitialDate()),
                        FCI_TYPES.get(inv.getInvestment().getCurrency()),
                        FCI_NAMES.get(inv.getInvestment().getCurrency()),
                        CONSULTATIO_ASSET_MANAGEMENT_CUIT,
                        BANCO_DE_VALORES_CUIT,
                        nf.format(inv.getInvestment().getAmount()),
                        nf.format(dicPreviousYearValues.get(inv.getInvestment().getCurrency())),
                        nf.format(dicValues.get(inv.getInvestment().getCurrency()))
                )).forEach(this::appendLine);

    }

    private void income() {
        this.income(3);
        this.appendLine("");
        this.income(6);
        this.appendLine("");
        this.income(12);
        this.appendLine("");
        this.income(18);
        this.appendLine("");
        this.income(24);
        this.appendLine("");
    }

    private void income(int months) {

        final var limit = USD_INFLATION.getTo();

        final var averageRealUSDIncome = Stream.of(readSeries("income/lifia.json"), readSeries("income/unlp.json"), readSeries("income/despegar.json"))
                .map(incomeSeries -> incomeSeries.exchangeInto("USD"))
                .map(usdSeries -> USD_INFLATION.adjust(usdSeries, limit.getYear(), limit.getMonth()))
                .map(new SimpleAggregation(months)::average)
                .collect(reducing(MoneyAmountSeries::add))
                .map(allRealUSDIncome -> allRealUSDIncome.getAmount(allRealUSDIncome.getTo()))
                .orElse(new MoneyAmount(ZERO, "USD"));

        this.appendLine("===< Average ", String.valueOf(months), " month income in ", String.valueOf(limit.getMonth()), "/", String.valueOf(limit.getYear()), " real USD >===");

        this.appendLine("\tIncome: ",
                averageRealUSDIncome.getCurrency(),
                " ",
                format("{0,number,currency}", averageRealUSDIncome.getAmount()));

        final var twentyPct = new MoneyAmount(averageRealUSDIncome.getAmount().multiply(new BigDecimal("0.2")), averageRealUSDIncome.getCurrency());

        this.appendLine("20% saving: ",
                averageRealUSDIncome.getCurrency(),
                " ",
                format("{0,number,currency}", twentyPct.getAmount()),
                " / ",
                format("{0,number,currency}",
                        ForeignExchanges.getForeignExchange(twentyPct.getCurrency(), "ARS").exchange(twentyPct, "ARS", limit.getYear(), limit.getMonth()).getAmount()));
    }

    public static void main(String[] args) {
        try {

            final var me = new ConsoleReports(new StringBuilder(1024));

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
                    entry(of("gold", 11), () -> me.currentInvestmentsRealProfit("XAU", XAU)),
                    entry(of("bp", 12), () -> me.fci(2018)),
                    entry(of("current", 13), me::currentInvestmentsRealProfit),
                    entry(of("allpast", 14), me::pastInvestmentsRealProfit),
                    entry(of("global", 15), me::globalInvestmentsRealProfit),
                    entry(of("CSPX", 16), () -> me.currentInvestmentsRealProfit("CSPX", ETF)),
                    entry(of("income12", 17), () -> me.income(12)),
                    entry(of("income6", 18), () -> me.income(6)),
                    entry(of("income3", 19), () -> me.income(3)),
                    entry(of("income18", 20), () -> me.income(18)),
                    entry(of("income24", 21), () -> me.income(24)),
                    entry(of("income", 22), me::income)
            );

            final var params = Arrays.stream(args).map(String::toLowerCase).collect(Collectors.toSet());

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
                            me.appendLine("");
                        });
                me.printReport(System.out);
            }
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            ex.printStackTrace(System.err);
        }
    }

}
