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
import static java.math.BigDecimal.ZERO;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
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
import static org.fede.calculator.money.MathConstants.CONTEXT;

/**
 *
 * @author federicogentile
 */
public class ModifiedDietzReturn {

    private final List<Investment> investments;
    private final String currency;
    private final boolean nominal;
    private final LocalDate initialMoment;
    private final LocalDate finalMoment;
    private final long daysBetween;

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
                        .map(Investment::getOut).filter(Objects::nonNull)
                        .filter(ie -> between(ie.getDate(), this.initialMoment, this.finalMoment))
                        .map(cashFlowFunction)
                        .map(BigDecimal::negate))
                .collect(Collectors.toList());
    }

    private BigDecimal adjustedCashFlowAmount(InvestmentEvent ie) {

        final var cashFlowDate = asLocalDate(ie.getDate());

        final var cashFlowTime = ChronoUnit.DAYS.between(this.initialMoment, cashFlowDate);

        return this.cashFlowAmount(ie)
                .multiply(BigDecimal.valueOf(this.daysBetween - cashFlowTime).divide(BigDecimal.valueOf(this.daysBetween), CONTEXT), CONTEXT);
    }

    private BigDecimal cashFlowAmount(InvestmentEvent ie) {

        final var ym = YearMonth.of(ie.getDate());
        final var fx = ForeignExchanges.getForeignExchange(ie.getCurrency(), currency)
                .exchange(ie.getMoneyAmount(), currency, ym);

        return nominal
                ? fx.getAmount()
                : Inflation.USD_INFLATION.adjust(fx, ym, Inflation.USD_INFLATION.getTo()).getAmount();

    }

    private MoneyAmount portfolioValue(YearMonth ym) {

        return this.getInvestments().stream()
                .filter(i -> i.isCurrent(ym.asToDate()))
                .map(Investment::getInvestment)
                .map(asset -> ForeignExchanges.getForeignExchange(asset.getCurrency(), currency).exchange(asset.getMoneyAmount(), currency, ym))
                .map(ma -> nominal ? ma : Inflation.USD_INFLATION.adjust(ma, ym, Inflation.USD_INFLATION.getTo()))
                .reduce(new MoneyAmount(ZERO, currency), MoneyAmount::add);
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

        final var result = v1.getAmount()
                .subtract(v0.getAmount(), CONTEXT)
                .subtract(cashFlowSum, CONTEXT)
                .divide(v0.getAmount().add(adjustedCashFlowSum, CONTEXT), CONTEXT);

        return Pair.of(
                result,
                BigDecimal.valueOf(Math.pow(1.0d + result.doubleValue(), 365.0d / (double) this.daysBetween) - 1.0d));

    }

}
