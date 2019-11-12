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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.PrintStream;
import java.math.BigDecimal;
import static java.math.BigDecimal.ZERO;
import static java.math.MathContext.DECIMAL64;
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
import java.util.Date;
import java.util.Map;
import static java.util.Map.entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.InvestmentType;
import static org.fede.calculator.money.series.InvestmentType.*;
import org.fede.calculator.money.series.YearMonth;
import org.fede.util.Pair;
import static org.fede.util.Pair.of;
import static org.fede.calculator.money.Inflation.USD_INFLATION;
import org.fede.calculator.money.series.InvestmentAsset;
import org.fede.calculator.money.series.InvestmentEvent;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.SeriesReader;
import static org.fede.calculator.money.series.SeriesReader.readSeries;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
public class ConsoleReports {

    private static final BigDecimal COEFFICIENT = new BigDecimal("0.1223");

    private static final BigDecimal REALTOR_FEE = new BigDecimal("0.045");
    private static final BigDecimal STAMP_TAX = new BigDecimal("0.018");
    private static final BigDecimal REGISTER_TAX = new BigDecimal("0.006");
    private static final BigDecimal NOTARY_FEE = new BigDecimal("0.02").multiply(new BigDecimal("1.21", DECIMAL64));

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

    private static <T, U, V> Pair<Pair<T, U>, V> ter(T t, U u, V v) {
        return of(of(t, u), v);
    }

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

    private Optional<MoneyAmount> total(Predicate<Investment> predicate, String reportCurrency, YearMonth limit) {
        return investments.stream()
                .filter(predicate)
                .map(i -> i.getInvestment().getMoneyAmount())
                .map(investedAmount -> ForeignExchanges.getForeignExchange(investedAmount.getCurrency(), reportCurrency).exchange(investedAmount, reportCurrency, limit.getYear(), limit.getMonth()))
                .reduce(MoneyAmount::add);
    }

    private void groupedInvestments() {
        final var reportCurrency = "USD";
        final var limit = USD_INFLATION.getTo();

        appendLine("===< Inversiones Actuales Agrupadas en ", reportCurrency, " ", String.valueOf(limit.getYear()), "/", String.valueOf(limit.getMonth()), ">===");

        final MoneyAmountSeries cashSeries = SeriesReader.readSeries("saving/ahorros-dolar-liq.json");

        final MoneyAmount cash = cashSeries.getAmount(cashSeries.getTo());

        final var total = this.total(Investment::isCurrent, reportCurrency, limit).map(t -> t.add(cash));
        Stream.concat(investments.stream()
                .filter(Investment::isCurrent)
                .collect(groupingBy(in -> of(in.getType().toString(), in.getCurrency()), MAPPER))
                .entrySet()
                .stream()
                .map(e -> of(e.getKey(), new MoneyAmount(e.getValue(), e.getKey().getSecond())))
                .map(p -> of(p.getFirst(), this.fx(p, reportCurrency))),
                Stream.of(ter("CASH", "USD", cash)))
                .sorted((p, q) -> q.getSecond().getAmount().compareTo(p.getSecond().getAmount()))
                .map(pair -> this.formatReport(total, pair.getSecond(), pair.getFirst().getFirst(), pair.getFirst().getSecond()))
                .forEach(this::appendLine);
        total
                .map(m -> format("Total: {0} -> {1,number,currency}", m.getCurrency(), m.getAmount()))
                .ifPresent(this::appendLine);
    }

    private String assetAllocation(Investment investment) {
        final Set<String> equities = Set.of("CSPX", "EIMI", "MEUD", "XRSU", "SPY4");
        final Set<String> bonds = Set.of("LQDA", "JPEA", "LECAP", "LETE", "SPY4", "UVA", "AY24", "SRFDIIA");

        if (equities.contains(investment.getInvestment().getCurrency())) {
            return "EQ";
        }
        if (investment.getType().equals(InvestmentType.BONO)
                || investment.getType().equals(InvestmentType.PF)
                || bonds.contains(investment.getInvestment().getCurrency())) {
            return "BO";
        }

        return "CASH";

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

        final MoneyAmountSeries cashSeries = SeriesReader.readSeries("saving/ahorros-dolar-liq.json");

        final MoneyAmount cash = cashSeries.getAmount(cashSeries.getTo());

        final Optional<MoneyAmount> total = this.total(Investment::isCurrent, reportCurrency, limit).map(tot -> tot.add(cash));

        final Investment i = new Investment();
        final InvestmentEvent in = new InvestmentEvent();
        final InvestmentAsset asset = new InvestmentAsset();
        in.setAmount(cash.getAmount());
        in.setCurrency(cash.getCurrency());
        in.setDate(Date.from(LocalDate.now().minusYears(1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        i.setIn(in);
        asset.setAmount(cash.getAmount());
        asset.setCurrency(cash.getCurrency());
        i.setInvestment(asset);
        i.setType(InvestmentType.USD);

        Stream.concat(Stream.of(i), investments.stream())
                .filter(Investment::isCurrent)
                .collect(groupingBy(
                        //this::investmentType,
                        this::assetAllocation,
                        mapping(inv -> ForeignExchanges.getForeignExchange(inv.getInvestment().getCurrency(), reportCurrency)
                        .exchange(inv.getInvestment().getMoneyAmount(), reportCurrency, limit.getYear(), limit.getMonth())
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
        this.pastInvestmentsRealProfit("CONAAFA", FCI);
        this.pastInvestmentsRealProfit("USD", PF);
        this.pastInvestmentsRealProfit("UVA", PF);
        this.pastInvestmentsRealProfit("ARS", PF);
        this.pastInvestmentsRealProfit("LECAP", BONO);
        this.pastInvestmentsRealProfit("LETE", BONO);
        this.pastInvestmentsRealProfit("AY24", BONO);
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

        this.appendLine(format("{4}TOTAL: {0,number,currency} => {5,number,currency} {1,number,currency} {2} {3}",
                total,
                profit,
                this.percentFormat.format(pct),
                RealProfit.plusMinus(pct),
                Stream.of(type, currencyText)
                        .filter(t -> totalOnly)
                        .filter(Objects::nonNull)
                        .map(Object::toString)
                        .collect(Collectors.joining("", "", " ")),
                total.add(profit)));
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

        final var limit = USD_INFLATION.getTo();

        this.appendLine(format("Total income: {0,number,currency}",
                Stream.of(readSeries("income/lifia.json"), readSeries("income/unlp.json"), readSeries("income/despegar.json"))
                        .map(incomeSeries -> incomeSeries.exchangeInto("USD"))
                        .map(usdSeries -> USD_INFLATION.adjust(usdSeries, limit.getYear(), limit.getMonth()))
                        .flatMap(MoneyAmountSeries::moneyAmountStream)
                        .collect(reducing(MoneyAmount::add))
                        .orElse(new MoneyAmount(ZERO, "USD")).getAmount()));

    }

    private void income(int months) {

        final var limit = USD_INFLATION.getTo();

        final var averageRealUSDIncome = Stream.of(readSeries("income/lifia.json"), readSeries("income/unlp.json"), readSeries("income/despegar.json"))
                .map(incomeSeries -> incomeSeries.exchangeInto("USD"))
                .map(usdSeries -> USD_INFLATION.adjust(usdSeries, limit.getYear(), limit.getMonth()))
                .collect(reducing(MoneyAmountSeries::add))
                .map(new SimpleAggregation(months)::average)
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
                    //entry(of("AY24", 7), () -> me.currentInvestmentsRealProfit("AY24", BONO)),
                    entry(of("USD", 8), () -> me.currentInvestmentsRealProfit("USD", BONO)),
                    //entry(of("CONAAFA", 9), () -> me.currentInvestmentsRealProfit("CONAAFA", FCI)),
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
                    entry(of("income", 22), me::income),
                    //entry(of("close", 23), me::closeCONAAFA),
                    entry(of("EIMI", 24), () -> me.currentInvestmentsRealProfit("EIMI", ETF)),
                    entry(of("MEUD", 25), () -> me.currentInvestmentsRealProfit("MEUD", ETF)),
                    entry(of("XRSU", 26), () -> me.currentInvestmentsRealProfit("XRSU", ETF)),
                    //entry(of("VAGU", 27), () -> me.currentInvestmentsRealProfit("VAGU", ETF)),
                    entry(of("house", 28), () -> me.houseIrrecoverableCosts(USD_INFLATION.getTo())),
                    entry(of("house1", 29), () -> me.houseIrrecoverableCosts(new YearMonth(2011, 8))),
                    entry(of("house3", 30), () -> me.houseIrrecoverableCosts(new YearMonth(2013, 8))),
                    entry(of("house5", 31), () -> me.houseIrrecoverableCosts(new YearMonth(2015,8))),
                    entry(of("exp", 32), () -> me.expenses())
                    
                    
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

    public void closeCONAAFA() {
        final BigDecimal sellPrice = new BigDecimal("174.2392938");

        final Date sellDate = Date.from(LocalDate.of(2019, Month.AUGUST, 23).atStartOfDay(ZoneId.systemDefault()).toInstant());
        ObjectMapper om = new ObjectMapper();
        om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        this.investments
                .stream()
                .sorted(Comparator.comparing(Investment::getInitialDate))
                .filter(inv -> inv.getCurrency().equals("CONAAFA"))
                .filter(inv -> inv.getOut() == null)
                .map(inv -> this.close(inv, sellPrice, sellDate))
                .forEach(i -> this.print(om, i));
    }

    private Investment close(Investment inv, BigDecimal sellPrice, Date sellDate) {
        InvestmentEvent out = new InvestmentEvent();
        out.setAmount(inv.getInvestment().getAmount().multiply(sellPrice, DECIMAL64));
        out.setCurrency("ARS");
        out.setDate(sellDate);
        inv.setOut(out);
        return inv;
    }

    private void print(ObjectMapper om, Investment i) {
        try {
            System.out.println(MessageFormat.format("{0},", om.writeValueAsString(i)));
        } catch (JsonProcessingException ex) {
            Logger.getLogger(ConsoleReports.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private MoneyAmount applyCoefficient(YearMonth ym, MoneyAmount amount) {
        return new MoneyAmount(amount.getAmount().multiply(COEFFICIENT, DECIMAL64), amount.getCurrency());
    }

    private void houseIrrecoverableCosts(YearMonth timeLimit) {

        final var limit = USD_INFLATION.getTo();
        
        final var proportionalExpenses = SeriesReader.readSeries("expense/consorcio-reparaciones.json")
                .map(this::applyCoefficient);

        final var realExpensesInUSD = Stream.concat(
            Stream.of("expense/inmobiliario-43.json", "expense/seguro.json", "expense/reparaciones.json").map(SeriesReader::readSeries), 
            Stream.of(proportionalExpenses))
                .reduce(MoneyAmountSeries::add)
                .map(expenses -> expenses.exchangeInto("USD"))
                .map(usdExpenses -> Inflation.USD_INFLATION.adjust(usdExpenses, limit.getYear(), limit.getMonth()))
                .map(s -> s.map((ym, amount) -> this.limit(timeLimit, ym, amount)))
                .map(MoneyAmountSeries::moneyAmountStream)
                .orElseGet(Stream::empty)
                .reduce(MoneyAmount::add)
                .orElse(new MoneyAmount(ZERO, "USD"));

        this.buyVsRent(realExpensesInUSD, ZERO, timeLimit);
        this.buyVsRent(realExpensesInUSD, new BigDecimal("0.02"), timeLimit);
        this.buyVsRent(realExpensesInUSD, new BigDecimal("0.03"), timeLimit);

    }

    private MoneyAmount limit(YearMonth timeLimit, YearMonth ym, MoneyAmount amount){
        return ym.compareTo(timeLimit) <= 0 ? amount : new MoneyAmount(ZERO, amount.getCurrency());
    }

    private void buyVsRent(MoneyAmount realExpensesInUSD, BigDecimal rate, YearMonth timeLimit) {
        final var limit = USD_INFLATION.getTo();

        final var nominalInitialCost = new BigDecimal("96000");
        final var nominalTransactionCost = nominalInitialCost.multiply(
                REALTOR_FEE.add(STAMP_TAX, DECIMAL64)
                        .add(REGISTER_TAX, DECIMAL64)
                        .add(NOTARY_FEE, DECIMAL64),
                DECIMAL64);

        final var start = new YearMonth(2010, 8);
        final var realInitialCost = USD_INFLATION.adjust(new MoneyAmount(nominalInitialCost, "USD"),
                start.getYear(), start.getMonth(),
                limit.getYear(), limit.getMonth());

        final var realTransactionCost = USD_INFLATION.adjust(new MoneyAmount(nominalTransactionCost, "USD"),
                start.getYear(), start.getMonth(),
                limit.getYear(), limit.getMonth());

        final var months = BigDecimal.valueOf(start.monthsUntil(timeLimit));
        final var years = months.divide(new BigDecimal(12), DECIMAL64);

        // interest rate cost
        final var opportunityCost = new MoneyAmount(
                nominalInitialCost
                        .multiply(rate, DECIMAL64)
                        .multiply(years, DECIMAL64), "USD");

        final var totalRealExpense = realExpensesInUSD.add(opportunityCost).add(realTransactionCost);

        this.appendLine("===< Costo de ", 
                String.valueOf(start.getMonth()), "/", String.valueOf(start.getYear()), 
                " a ",
                String.valueOf(timeLimit.getMonth()), "/", String.valueOf(timeLimit.getYear()),
                " con retorno anual de ", percentFormat.format(rate), " >===");
        //this.appendLine("\tInversión inicial real USD ", format("{0,number, currency}", realInitialCost.getAmount()));
        //this.appendLine("Costo:");
        this.appendLine("USD reales ", String.valueOf(limit.getMonth()), "/", String.valueOf(limit.getYear()));
        this.appendLine("\tTotal USD ",
                format("{0,number,currency} ", totalRealExpense.getAmount()),
                format("{0}", percentFormat.format(totalRealExpense.getAmount().divide(realInitialCost.getAmount(), DECIMAL64))));

        final var monthlyCost = totalRealExpense.getAmount().divide(months, DECIMAL64);
        this.appendLine("\tMensual USD ",
                format("{0,number,currency} ", monthlyCost),
                format("{0}", percentFormat.format(monthlyCost.divide(realInitialCost.getAmount(), DECIMAL64))),
                format(" - ARS {0,number,currency}", ForeignExchanges.getForeignExchange("USD", "ARS")
                        .exchange(new MoneyAmount(monthlyCost, "USD"), "ARS", limit.getYear(), limit.getMonth())
                        .getAmount()));

        final var yearlyCost = totalRealExpense.getAmount().divide(years, DECIMAL64);
        this.appendLine("\tAnual USD ",
                format("{0,number,currency} ", yearlyCost),
                format("{0}\n", percentFormat.format(yearlyCost.divide(realInitialCost.getAmount(), DECIMAL64))));

    }

    private void expenses(){

    var expenses = Stream.of(
        of("service", "absa.json"),
        of("communications", "cablevision.json"),
        of("communications", "celular-a.json"),
        of("communications", "celular-f.json"),
        of("taxes", "contadora.json"),
        of("health", "emergencia.json"),
        of("home", "expensas.json"),
        of("service", "gas.json"),
        of("taxes", "inmobiliario-43.json"),
        of("health", "ioma.json"),
        of("home", "limpieza.json"),
        of("service", "luz.json"),
        of("taxes", "monotributo-angeles.json"),
        of("taxes", "municipal-43.json"),
        of("entertainment", "netflix.json"),
        of("home", "seguro.json"),
        of("home", "reparaciones.json"),
        of("communications", "telefono-43.json"),
        of("entertainment", "xbox.json"))
        .collect(groupingBy(Pair::getFirst, mapping(Pair::getSecond, Collectors.toList())));

    var limit = USD_INFLATION.getTo();
        
    expenses.entrySet()
        .stream()
        .map(e -> of(e.getKey(), this.totalRealUSD(e.getValue(), limit).getAmount()))
        .sorted(Comparator.comparing(Pair::getSecond))
        .forEach(e -> this.appendLine(e.getFirst(), " USD ", format("{0,number,currency} ", e.getSecond())));
    }



    private MoneyAmount totalRealUSD(List<String> jsonResources, YearMonth limit){
        return jsonResources.stream()
        .map(s -> "expense/" + s)
        .map(SeriesReader::readSeries)
        .reduce(MoneyAmountSeries::add)
                .map(expenses -> expenses.exchangeInto("USD"))
                .map(usdExpenses -> Inflation.USD_INFLATION.adjust(usdExpenses, limit.getYear(), limit.getMonth()))
                //.map(MoneyAmountSeries::moneyAmountStream)
                .map(this::average)
                .orElse(new MoneyAmount(ZERO, "USD"));

    }

    private MoneyAmount average(MoneyAmountSeries s){
        var months = s.getFrom().monthsUntil(s.getTo());
        return new MoneyAmount(s.moneyAmountStream()
            .map(MoneyAmount::getAmount)
            .reduce(BigDecimal::add)
            .orElse(ZERO)
            .divide(new BigDecimal(months), DECIMAL64), "USD");
    }

}
