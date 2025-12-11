/*
 * Copyright (C) 2025 fede
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
package org.fede.calculator.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.fede.calculator.money.Currency;
import static org.fede.calculator.money.Currency.CSPX;
import static org.fede.calculator.money.Currency.EIMI;
import static org.fede.calculator.money.Currency.RTWO;
import static org.fede.calculator.money.Currency.EMIM;
import static org.fede.calculator.money.Currency.MEUS;
import static org.fede.calculator.money.Currency.RTWOE;
import static org.fede.calculator.money.Currency.SXR8;
import static org.fede.calculator.money.Currency.XRSU;
import static org.fede.calculator.money.Currency.MEUD;
import static org.fede.calculator.money.Currency.USD;
import static org.fede.calculator.money.Currency.XUSE;
import org.fede.calculator.money.ForeignExchange;
import org.fede.calculator.money.ForeignExchanges;
import static org.fede.calculator.money.MathConstants.C;
import org.fede.calculator.money.MoneyAmount;
import static org.fede.calculator.money.MoneyAmount.zero;
import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.InvestmentAsset;
import org.fede.calculator.money.series.InvestmentEvent;
import org.fede.calculator.money.series.InvestmentType;
import org.fede.calculator.money.series.SeriesReader;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 *
 * @author fede
 */
public class RebalancingReport {

    private final Format format;
    private final Series series;
    private final Console console;
    private final Map<Currency, BigDecimal> weights;

    private final Map<Currency, List<Currency>> currencyEquivalences = new EnumMap<>(Map.of(
            CSPX, List.of(CSPX, SXR8),
            RTWO, List.of(RTWO, XRSU, RTWOE),
            XUSE, List.of(XUSE, MEUD, MEUS),
            EIMI, List.of(EIMI, EMIM)
    ));

    public static RebalancingReport equity(Format format, Series series, Console console) {
        return new RebalancingReport(format, series, console, new EnumMap<>(Map.of(
                CSPX, BigDecimal.valueOf(65l).movePointLeft(2),
                RTWO, BigDecimal.valueOf(10l).movePointLeft(2),
                XUSE, BigDecimal.valueOf(15l).movePointLeft(2),
                EIMI, BigDecimal.valueOf(10l).movePointLeft(2)
        )));
    }

    private RebalancingReport(Format format, Series series, Console console, Map<Currency, BigDecimal> weights) {
        this.format = format;
        this.series = series;
        this.console = console;
        this.weights = weights;
    }

    /**
     * Prints the buy operations based on the current portfolio and the
     * specified contribution based on the Buy-only Greedy Proportional
     * Rebalancing.
     *
     * @param usd
     * @param eur
     * @param transfer
     * @param detail
     */
    public void buy(MoneyAmount usd, MoneyAmount eur, MoneyAmount transfer, boolean detail) {

        if (usd.amount().compareTo(ZERO) < 0
                || eur.amount().compareTo(ZERO) < 0
                || transfer.amount().compareTo(ZERO) < 0) {
            throw new IllegalArgumentException("Only positive amounts.");
        }

        final var now = YearMonth.now();
        final var contribution = new MoneyAmount(BigDecimal.valueOf(9970l), USD)
                .min(ForeignExchanges.getForeignExchange(eur.currency(), USD)
                        .exchange(eur, USD, now)
                        .add(usd));

        final var virtualValues = this.virtualPortfolioValues(now);

        final var prices = currencyEquivalences.keySet()
                .stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        curr -> ForeignExchanges.getForeignExchange(curr, USD)
                                .exchange(new MoneyAmount(ONE, curr), USD, now),
                        keepFirst(),
                        currencyMapSupplier()));

        this.console.appendLine(this.format.title("Greedy Proportional Rebalancing"));
        this.console.appendLine(this.format.subtitle("Initial State"));
        this.print(virtualValues);
        this.console.appendLine("Contribution: ", this.format.currency(contribution, 16));
        this.rebalance(weights, virtualValues, prices, contribution, transfer, detail);

    }

    private Map<Currency, MoneyAmount> virtualPortfolioValues(YearMonth now) {
        final Map<Currency, BigDecimal> quantities = this.series.getInvestments()
                .stream()
                .filter(Investment::isETF)
                .filter(i -> i.isCurrent(LocalDate.now()))
                .collect(Collectors.groupingBy(
                        Investment::getCurrency,
                        currencyMapSupplier(),
                        Collectors.mapping(
                                i -> i.getInvestment().getAmount(),
                                Collectors.reducing(ZERO, BigDecimal::add))));

        final Map<Currency, MoneyAmount> values = new EnumMap<>(Currency.class);

        for (var e : quantities.entrySet()) {
            final Currency curr = e.getKey();
            values.put(
                    curr,
                    ForeignExchanges.getForeignExchange(curr, USD)
                            .exchange(new MoneyAmount(e.getValue(), curr), USD, now));
        }

        final var virtualValues = new EnumMap<Currency, MoneyAmount>(Currency.class);

        for (var e : currencyEquivalences.entrySet()) {
            final Currency curr = e.getKey();
            virtualValues.put(
                    curr,
                    sum(e.getValue().stream().map(values::get)));
        }
        return virtualValues;
    }

    public void sell(MoneyAmount c, boolean allowOverSell, boolean detail) {

        if (c.amount().compareTo(ZERO) < 0) {
            throw new IllegalArgumentException("Only positive amounts.");
        }

        final var now = YearMonth.now();

        this.console.appendLine(this.format.title("Greedy Proportional Rebalancing"));
        this.console.appendLine(this.format.subtitle("Initial State"));
        this.print(this.virtualPortfolioValues(now));
        this.console.appendLine("Withdrawal: ", this.format.currency(c, 16));

        var lots = this.lots(LocalDate.now());

        var sold = zero(USD);

        final List<Investment> soldInvestments = new ArrayList<>();

        boolean keepGoing = true;
        while (keepGoing
                && this.hasLotsRemaining(lots)
                && sold.amount().compareTo(c.amount()) < 0) {
            Sale bestSale = null;
            BigDecimal trackingError = null;
            for (var etf : lots.keySet()) {
                var sale = this.sellingOldest(etf, lots);
                if (sale != null) {

                    var v = this.value(
                            ForeignExchanges.getForeignExchange(sale.soldCurrency, USD),
                            List.of(sale.soldInvestment()),
                            now);
                    var oversold = v.add(sold).amount().compareTo(c.amount()) > 0;

                    var newTE = this.trackingError(this.virtualValues(sale.remainingLots(), now));
                    if ((!oversold || allowOverSell)
                            && (trackingError == null || newTE.compareTo(trackingError) < 0)) {
                        trackingError = newTE;
                        bestSale = sale;
                    }
                }
            }

            keepGoing = bestSale != null;

            if (bestSale != null) {
                var soldValue = this.value(
                        ForeignExchanges.getForeignExchange(bestSale.soldCurrency, USD),
                        List.of(bestSale.soldInvestment()),
                        now);

                lots = bestSale.remainingLots();
                soldInvestments.add(bestSale.soldInvestment());
                sold = sold.add(soldValue);
            }
        }
        this.console.appendLine(this.format.subtitle("Final State"));
        this.print(this.virtualValues(lots, now));
        this.console.appendLine("");
        this.console.appendLine("Sold: ", this.format.currency(sold, 16));

        var cgt = zero(USD);
        final var cgtr = SeriesReader.readPercent("capitalGainsTaxRate");

        for (var i : soldInvestments) {

            var fx = ForeignExchanges.getForeignExchange(i.getCurrency(), USD);
            var initial = i.getIn().getFx() == null
                    ? i.getInitialMoneyAmount()
                    : new MoneyAmount(i.getIn().getAmount().multiply(i.getIn().getFx(), C), USD);
            var current = fx.exchange(i.getInvestment().getMoneyAmount(), USD, now);
            cgt = cgt.add(current.subtract(initial).adjust(ONE, cgtr));

        }

        if (!sold.isZero()) {
            this.console.appendLine(
                    MessageFormat.format("Capital Gains Tax: {0} {1}",
                            this.format.currency(cgt, 16),
                            this.format.percent(cgt.amount().divide(sold.amount(), C), 6)));
        }
        this.console.appendLine("Neto: ", this.format.currency(sold.subtract(cgt), 16));

        ObjectMapper om = null;
        if (detail) {
            om = this.om();
        }
        this.console.appendLine(this.format.subtitle("Detail"));

        this.console.appendLine(MessageFormat.format("Sell {0} lots.", soldInvestments.size()));

        for (var i : soldInvestments) {

            var fx = ForeignExchanges.getForeignExchange(i.getCurrency(), USD);

            this.console.appendLine(MessageFormat.format("Sell {2} {0} bought on {1} for {3}",
                    i.getCurrency(),
                    i.getInitialDate(),
                    this.format.number(i.getInvestment().getAmount(), 4),
                    this.format.currency(fx.exchange(i.getInvestment().getMoneyAmount(), USD, now), 16)));

            if (detail) {
                var out = new InvestmentEvent();
                var outCurrency = i.getInitialCurrency();

                var outFx = ForeignExchanges.getForeignExchange(i.getCurrency(), outCurrency);

                var outAmount = outFx.exchange(i.getInvestment().getMoneyAmount(), outCurrency, now);

                out.setAmount(outAmount.amount());
                out.setCurrency(outCurrency);
                out.setDate(LocalDate.now());
                out.setFee(ZERO);
                out.setFx(outCurrency != USD
                        ? ForeignExchanges.getForeignExchange(outCurrency, USD)
                                .exchange(new MoneyAmount(ONE, outCurrency), USD, now)
                                .amount()
                        : null);
                out.setTransferFee(ZERO);

                this.console.appendLine(om.writeValueAsString(out));
            }

        }
    }

    private static MoneyAmount sum(Map<?, MoneyAmount> m) {
        return sum(m.values().stream());
    }

    private static MoneyAmount sum(Stream<MoneyAmount> s) {
        return s.reduce(zero(USD), MoneyAmount::add);
    }

    private void rebalance(
            Map<Currency, BigDecimal> w,
            Map<Currency, MoneyAmount> v,
            Map<Currency, MoneyAmount> p,
            MoneyAmount c,
            MoneyAmount transferFee,
            boolean detail) {

        final MoneyAmount totalFuturePortfolioValue = sum(v)
                .add(c);

        final Map<Currency, MoneyAmount> targetValue = w.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> totalFuturePortfolioValue.adjust(ONE, e.getValue()),
                        keepFirst(),
                        currencyMapSupplier()));

        final Map<Currency, MoneyAmount> deficits = new EnumMap<>(Currency.class);
        for (var curr : targetValue.keySet()) {
            var deficit = targetValue.get(curr).subtract(v.get(curr));
            if (deficit.amount().compareTo(ZERO) > 0) {
                deficits.put(curr, deficit);
            }
        }

        final var deficitsSum = sum(deficits);
        final Map<Currency, MoneyAmount> allocation = deficits.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> c.adjust(ONE,
                                e.getValue()
                                        .amount()
                                        .divide(deficitsSum.amount(), C)),
                        keepFirst(),
                        currencyMapSupplier()));

        final Map<Currency, BigDecimal> shares = allocation.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue()
                                .amount()
                                .divide(p.get(e.getKey()).amount(), C)
                                .setScale(0, RoundingMode.DOWN),
                        keepFirst(),
                        currencyMapSupplier()));

        var remainder = this.remainder(shares, p, c);

        var keepImproving = true;
        while (keepImproving && this.canBuyAtLeastOneMore(p, remainder)) {

            final BigDecimal te = this.trackingError(this.newValues(shares, v, p));
            BigDecimal delta = ZERO;
            Currency max = null;
            for (var curr : p.keySet()) {

                if (p.get(curr).amount().compareTo(remainder.amount()) < 0) {

                    final Map<Currency, BigDecimal> withOneMoreShare = new EnumMap<>(Currency.class);
                    withOneMoreShare.putAll(shares);
                    withOneMoreShare.put(curr, withOneMoreShare.getOrDefault(curr, ZERO).add(ONE));

                    var newTe = this.trackingError(this.newValues(withOneMoreShare, v, p));
                    var d2 = te.subtract(newTe);
                    if (d2.compareTo(delta) > 0) {
                        delta = d2;
                        max = curr;
                    }
                }
            }
            keepImproving = max != null;

            if (keepImproving) {
                // add one. update things
                shares.put(max, shares.getOrDefault(max, ZERO).add(ONE));
                remainder = remainder(shares, p, c);
            }
        }
        this.console.appendLine(this.format.subtitle("Result"));
        this.print(this.newValues(shares, v, p));
        this.console.appendLine(this.format.subtitle("Buy"));
        this.printBigDecimal(shares);
        this.console.appendLine("Remainder: ", this.format.currency(remainder, 16));

        if (shares.values().stream().anyMatch(quantity -> quantity.signum() > 0)) {

            final Map<Currency, MoneyAmount> feeAttrbution = new EnumMap<>(Currency.class);
            for (var curr : shares.keySet()) {
                final var proportion = shares.get(curr)
                        .multiply(p.get(curr).amount(), C)
                        .divide(c.subtract(remainder).amount(), C);
                feeAttrbution.put(
                        curr,
                        transferFee.adjust(ONE, proportion));
            }

            this.console.appendLine(this.format.subtitle("Transfer Fee"));
            this.print(feeAttrbution);

            if (detail) {
                final var om = this.om();

                this.console.appendLine(this.format.subtitle("JSON"));
                this.console.appendLine(shares.entrySet()
                        .stream()
                        .filter(e -> e.getValue().signum() > 0)
                        .map(e -> this.asInvestment(e.getKey(), e.getValue(), c, shares, p, feeAttrbution))
                        .map(om::writeValueAsString)
                        .collect(Collectors.joining(",\n", ",\n", "")));
            }
        }
    }

    private ObjectMapper om() {
        return JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .enable(SerializationFeature.INDENT_OUTPUT)
                .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
                .build();
    }

    private Investment asInvestment(
            Currency curr,
            BigDecimal quantity,
            MoneyAmount c,
            Map<Currency, BigDecimal> shares,
            Map<Currency, MoneyAmount> p,
            Map<Currency, MoneyAmount> feeAttribution) {
        var i = new Investment();
        var in = new InvestmentEvent();
        in.setCurrency(c.currency());
        in.setAmount(shares.get(curr)
                .multiply(p.get(curr).amount(), C));
        in.setDate(LocalDate.now());
        in.setFee(ZERO);
        in.setTransferFee(feeAttribution.get(curr).amount());
        in.setFx(null);
        i.setIn(in);
        i.setComment("lse");
        i.setType(InvestmentType.ETF);
        var asset = new InvestmentAsset();
        asset.setCurrency(curr);
        asset.setAmount(quantity);
        i.setInvestment(asset);
        return i;
    }

    private boolean canBuyAtLeastOneMore(Map<Currency, MoneyAmount> p, MoneyAmount remainder) {
        return p.values()
                .stream()
                .anyMatch(price -> price.amount().compareTo(remainder.amount()) <= 0);
    }

    private MoneyAmount remainder(
            Map<Currency, BigDecimal> shares,
            Map<Currency, MoneyAmount> p,
            MoneyAmount c) {
        return c
                .subtract(sum(shares.entrySet()
                        .stream()
                        .map(e -> p.get(e.getKey()).adjust(ONE, e.getValue()))));
    }

    private Map<Currency, MoneyAmount> newValues(
            Map<Currency, BigDecimal> shares,
            Map<Currency, MoneyAmount> v,
            Map<Currency, MoneyAmount> p) {
        final Map<Currency, MoneyAmount> newValues = new EnumMap<>(Currency.class);

        for (var e : v.entrySet()) {
            var curr = e.getKey();
            newValues.put(
                    curr,
                    p.get(curr)
                            .adjust(ONE, shares.getOrDefault(curr, ZERO))
                            .add(v.get(curr)));
        }
        return newValues;
    }

    private BigDecimal trackingError(Map<Currency, MoneyAmount> values) {
        final var sum = sum(values);

        if (sum.isZero()) {
            return ZERO;
        }
        return values.entrySet()
                .stream()
                .map(e -> this.squaredError(e.getKey(), e.getValue(), sum))
                .reduce(ZERO, BigDecimal::add);
    }

    private BigDecimal squaredError(
            Currency c,
            MoneyAmount currentValue,
            MoneyAmount currentTotalValue) {

        return this.weights
                .get(c)
                .subtract(currentValue.amount()
                        .divide(currentTotalValue.amount(), C), C)
                .pow(2, C);
    }

    private boolean hasLotsRemaining(Map<Currency, Deque<Investment>> lots) {
        return !lots.isEmpty()
                && lots.values().stream().anyMatch(Predicate.not(Collection::isEmpty));
    }

    private MoneyAmount value(
            ForeignExchange fx,
            Collection<Investment> inv,
            YearMonth ym) {
        return sum(inv.stream()
                .map(Investment::getInvestment)
                .map(InvestmentAsset::getMoneyAmount)
                .map(ma -> fx.exchange(ma, USD, ym)));

    }

    private Map<Currency, MoneyAmount> virtualValues(Map<Currency, Deque<Investment>> lots, YearMonth now) {

        final Map<Currency, MoneyAmount> newValues = new EnumMap<>(Currency.class);

        for (var e : lots.entrySet()) {
            var curr = e.getKey();
            var fx = ForeignExchanges.getForeignExchange(curr, USD);
            newValues.put(
                    curr,
                    this.value(fx, e.getValue(), now)
            );
        }

        final Map<Currency, MoneyAmount> virtualNewValues = new EnumMap<>(Currency.class);

        for (var e : currencyEquivalences.entrySet()) {
            Currency curr = e.getKey();
            virtualNewValues.put(
                    curr,
                    sum(e.getValue()
                            .stream()
                            .map(c -> newValues.getOrDefault(c, zero(USD)))));

        }

        return virtualNewValues;
    }

    private Sale sellingOldest(Currency c, Map<Currency, Deque<Investment>> lots) {

        final Map<Currency, Deque<Investment>> newLots = new EnumMap<>(Currency.class);

        for (var e : lots.entrySet()) {
            newLots.put(e.getKey(), new ArrayDeque<>(e.getValue()));
        }
        var lot = newLots.get(c);
        if (lot.isEmpty()) {
            return null;
        }
        return new Sale(c, newLots, lot.removeFirst());
    }

    private Map<Currency, Deque<Investment>> lots(LocalDate now) {

        return this.series.getInvestments()
                .stream()
                .filter(Investment::isETF)
                .filter(i -> i.isCurrent(now))
                .collect(Collectors.groupingBy(Investment::getCurrency))
                .entrySet()
                .stream()
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                e -> this.asDeque(e.getValue()),
                                keepFirst(),
                                currencyMapSupplier()
                        )
                );

    }

    private void print(Currency c, MoneyAmount m, MoneyAmount total) {
        if (!m.isZero()) {

            this.console.appendLine(
                    MessageFormat.format("{0} {1} {2}",
                            c,
                            this.format.currency(m, 20),
                            this.format.percent(m.amount().divide(total.amount(), C), 12)));
        } else {
            this.console.appendLine(
                    MessageFormat.format("{0} {1} {2}",
                            c,
                            ZERO,
                            this.format.percent(ZERO, 12)));

        }
    }

    private void print(Currency c, BigDecimal v) {
        if (v.signum() > 0) {
            this.console.appendLine(MessageFormat.format("{0} {1}", c, this.format.number(v)));
        }
    }

    private void print(Map<Currency, MoneyAmount> m) {

        final var sum = sum(m);
        m.forEach((c, ma) -> this.print(c, ma, sum));

    }

    private void printBigDecimal(Map<Currency, BigDecimal> m) {

        m.forEach(this::print);

    }

    private static <V> Supplier<Map<Currency, V>> currencyMapSupplier() {
        return () -> new EnumMap<>(Currency.class);
    }

    private static <T> BinaryOperator<T> keepFirst() {
        return (a, b) -> a;
    }

    private Deque<Investment> asDeque(List<Investment> i) {
        final Deque<Investment> deque = new ArrayDeque<>(i.size());
        i.stream()
                .sorted(Comparator.comparing(Investment::getInitialDate))
                .forEachOrdered(deque::addLast);
        return deque;
    }

    private record Sale(
            Currency soldCurrency,
            Map<Currency, Deque<Investment>> remainingLots,
            Investment soldInvestment) {

    }
}
