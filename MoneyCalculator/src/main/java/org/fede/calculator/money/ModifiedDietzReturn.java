/*
 * Copyright (C) 2021 federicogentile
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

import java.math.BigDecimal;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.InvestmentEvent;
import org.fede.calculator.money.series.YearMonth;
import org.fede.util.Pair;
import static org.fede.calculator.money.MathConstants.C;

/**
 *
 * @author fede
 */
public class ModifiedDietzReturn {

    private final List<Investment> investments;
    private final String currency;
    private final boolean nominal;
    private final LocalDate initialMoment;
    private final LocalDate finalMoment;
    private final long daysBetween;
    private final MoneyAmount zeroAmount;

    private static Instant min(Instant i1, Instant i2) {
        return i1.compareTo(i2) <= 0
                ? i1
                : i2;
    }

    private static Instant max(Instant i1, Instant i2) {
        return i1.compareTo(i2) >= 0
                ? i1
                : i2;
    }

    private static LocalDate min(LocalDate l1, LocalDate l2) {
        return l1.compareTo(l2) <= 0
                ? l1
                : l2;
    }

    private static LocalDate max(LocalDate l1, LocalDate l2) {
        return l1.compareTo(l2) >= 0
                ? l1
                : l2;
    }

    private static LocalDate asLocalDate(Instant i) {
        return LocalDate.ofInstant(i, ZoneId.systemDefault());
    }

    private static LocalDate asLocalDate(Date d) {
        return asLocalDate(d.toInstant());
    }

    public static long daysBetween(List<Investment> investments) {

        final var from = investments
                .stream()
                .map(Investment::getIn)
                .map(InvestmentEvent::getDate)
                .map(Date::toInstant)
                .reduce(ModifiedDietzReturn::min)
                .map(ModifiedDietzReturn::asLocalDate)
                .get();

        final var to = min(finalMoment(investments), LocalDate.now());

        return ChronoUnit.DAYS.between(from, to);
    }

    private static LocalDate finalMoment(List<Investment> investments) {

        if (investments.stream().map(Investment::getOut).anyMatch(Objects::isNull)) {
            return LocalDate.now();
        }

        return investments
                .stream()
                .map(Investment::getOut)
                .map(InvestmentEvent::getDate)
                .map(Date::toInstant)
                .reduce(ModifiedDietzReturn::max)
                .map(ModifiedDietzReturn::asLocalDate)
                .get();
    }

    public ModifiedDietzReturn(List<Investment> investments, String currency, boolean nominal) {
        this(investments,
                currency,
                nominal,
                investments
                        .stream()
                        .map(Investment::getIn)
                        .map(InvestmentEvent::getDate)
                        .map(Date::toInstant)
                        .reduce(ModifiedDietzReturn::min)
                        .map(ModifiedDietzReturn::asLocalDate)
                        .get(),
                finalMoment(investments));
    }

    public ModifiedDietzReturn(List<Investment> investments, String currency, boolean nominal, LocalDate initialMoment, LocalDate finalMoment) {
        this.investments = investments;
        this.currency = currency;
        this.zeroAmount = new MoneyAmount(ZERO, this.currency);
        this.nominal = nominal;
        this.initialMoment = max(initialMoment,
                investments
                        .stream()
                        .map(Investment::getIn)
                        .map(InvestmentEvent::getDate)
                        .map(Date::toInstant)
                        .reduce(ModifiedDietzReturn::min)
                        .map(ModifiedDietzReturn::asLocalDate)
                        .get());

        this.finalMoment = min(finalMoment, LocalDate.now());
        this.daysBetween = ChronoUnit.DAYS.between(this.initialMoment, this.finalMoment);
    }

    private List<Investment> getInvestments() {
        return investments;
    }

    private List<BigDecimal> cashFlows() {
        return this.cashFlows(this::cashFlowAmount);
    }

    private List<BigDecimal> adjustedCashFlows() {
        return this.cashFlows(this::adjustedCashFlowAmount);
    }

    private static boolean between(Date d, LocalDate initialMoment, LocalDate finalMoment) {
        final var ld = asLocalDate(d);
        return (ld.isEqual(initialMoment) || ld.isAfter(initialMoment))
                && (ld.isEqual(finalMoment) || ld.isBefore(finalMoment));
    }

    private List<BigDecimal> cashFlows(Function<InvestmentEvent, BigDecimal> cashFlowFunction) {

        return Stream.concat(
                this.getInvestments()
                        .stream()
                        .map(Investment::getIn)
                        .filter(ie -> between(ie.getDate(), this.initialMoment, this.finalMoment))
                        .map(cashFlowFunction),
                this.getInvestments()
                        .stream()
                        .map(Investment::getOut)
                        .filter(Objects::nonNull)
                        .filter(ie -> between(ie.getDate(), this.initialMoment, this.finalMoment))
                        .map(cashFlowFunction)
                        .map(BigDecimal::negate))
                .collect(Collectors.toList());
    }

    private BigDecimal adjustedCashFlowAmount(InvestmentEvent ie) {

        final var cashFlowDate = asLocalDate(ie.getDate());

        final var cashFlowTime = ChronoUnit.DAYS.between(this.initialMoment, cashFlowDate);

        return this.cashFlowAmount(ie)
                .multiply(BigDecimal.valueOf(this.daysBetween - cashFlowTime).divide(BigDecimal.valueOf(this.daysBetween), C), C);
    }

    private BigDecimal cashFlowAmount(InvestmentEvent ie) {

        final var ym = YearMonth.of(ie.getDate());
        final var fx = ie.getFx() != null
                ? new MoneyAmount(ie.getAmount().multiply(ie.getFx(), C), "USD")
                : ForeignExchanges.getMoneyAmountForeignExchange(ie.getCurrency(), currency).apply(ie.getMoneyAmount(), ym);

        return nominal
                ? fx.getAmount()
                : Inflation.USD_INFLATION.adjust(fx, ym, Inflation.USD_INFLATION.getTo()).getAmount();
    }

    private MoneyAmount portfolioValue(YearMonth ym) {

        return this.getInvestments().stream()
                .filter(i -> i.isCurrent(ym.asToDate()))
                .map(Investment::getInvestment)
                .map(asset -> ForeignExchanges.getMoneyAmountForeignExchange(asset.getCurrency(), currency).apply(asset.getMoneyAmount(), ym))
                .map(ma -> nominal ? ma : Inflation.USD_INFLATION.adjust(ma, ym, Inflation.USD_INFLATION.getTo()))
                .reduce(this.zeroAmount, MoneyAmount::add)
                .max(this.zeroAmount);
    }

    public Pair<BigDecimal, BigDecimal> get() {

        final var v1 = this.portfolioValue(YearMonth.of(this.finalMoment.getYear(), this.finalMoment.getMonthValue()));
        final var v0 = this.portfolioValue(YearMonth.of(this.initialMoment.getYear(), this.initialMoment.getMonthValue()).prev());

        final var cashFlowSum = this.cashFlows()
                .stream()
                .reduce(ZERO, BigDecimal::add);

        final var adjustedCashFlowSum = this.adjustedCashFlows()
                .stream()
                .reduce(ZERO, BigDecimal::add);

        if (v0.isZero() && adjustedCashFlowSum.signum() == 0) {
            return Pair.of(ZERO, BigDecimal.ZERO);
        }

        final var result = v1.getAmount()
                .subtract(v0.getAmount(), C)
                .subtract(cashFlowSum, C)
                .divide(v0.getAmount().add(adjustedCashFlowSum, C), C)
                .max(BigDecimal.ONE.negate());

        return Pair.of(result, annualized(result));

    }

    public Pair<BigDecimal, BigDecimal> monthlyLinked() {
        List<BigDecimal> monthyMDR = new ArrayList<>(60);

        final var from = YearMonth.of(this.initialMoment).prev();
        final var to = YearMonth.of(this.finalMoment);

        for (var ym = from; ym.compareTo(to) < 0; ym = ym.next()) {

            var next = ym.next();

            final var st = LocalDate.ofInstant(ym.asToDate().toInstant(), ZoneId.systemDefault()).plusDays(1);

            final var fn = LocalDate.ofInstant(next.asToDate().toInstant(), ZoneId.systemDefault());

            monthyMDR.add(new ModifiedDietzReturn(
                    this.investments,
                    this.currency,
                    this.nominal,
                    st,
                    fn).get().getFirst());

        }
        final var value = monthyMDR.stream()
                .map(ONE::add)
                .reduce(ONE, BigDecimal::multiply)
                .subtract(ONE);

        return Pair.of(value, annualized(value));

    }

    private BigDecimal annualized(BigDecimal value) {
        return BigDecimal.valueOf(Math.pow(1.0d + value.doubleValue(), 365.0d / (double) this.daysBetween) - 1.0d);
    }

}
