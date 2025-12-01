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
package org.fede.calculator.report;

import java.math.BigDecimal;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import org.fede.calculator.money.Currency;
import org.fede.calculator.money.ForeignExchanges;
import org.fede.calculator.money.Inflation;
import org.fede.calculator.money.MoneyAmount;
import static org.fede.calculator.money.Currency.USD;
import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.InvestmentEvent;
import java.time.YearMonth;
import static org.fede.calculator.money.MathConstants.C;

/**
 *
 * @author fede
 */
public class ModifiedDietzReturn {

    private final List<Investment> investments;
    private final Currency currency;
    private final boolean nominal;
    private final LocalDate initialMoment;
    private final LocalDate finalMoment;
    private final long daysBetween;
    private final MoneyAmount zeroAmount;

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

    private static LocalDate finalMoment(List<Investment> investments) {

        if (investments.stream().map(Investment::getOut).anyMatch(Objects::isNull)) {
            return LocalDate.now();
        }

        return investments
                .stream()
                .map(Investment::getOut)
                .map(InvestmentEvent::getDate)
                .reduce(ModifiedDietzReturn::max)
                .get();
    }

    public ModifiedDietzReturn(List<Investment> investments, boolean nominal) {
        this(investments, USD, nominal);
    }

    public ModifiedDietzReturn(List<Investment> investments, Currency currency, boolean nominal) {
        this(investments,
                currency,
                nominal,
                investments
                        .stream()
                        .map(Investment::getIn)
                        .map(InvestmentEvent::getDate)
                        .reduce(ModifiedDietzReturn::min)
                        .get(),
                finalMoment(investments));
    }

    public ModifiedDietzReturn(List<Investment> investments, boolean nominal, LocalDate initialMoment, LocalDate finalMoment) {
        this(investments, USD, nominal, initialMoment, finalMoment);
    }

    public ModifiedDietzReturn(List<Investment> investments, Currency currency, boolean nominal, LocalDate initialMoment, LocalDate finalMoment) {
        this.investments = investments;
        this.currency = currency;
        this.zeroAmount = MoneyAmount.zero(this.currency);
        this.nominal = nominal;
        this.initialMoment = max(initialMoment,
                investments
                        .stream()
                        .map(Investment::getIn)
                        .map(InvestmentEvent::getDate)
                        .reduce(ModifiedDietzReturn::min)
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

    private static boolean between(LocalDate d, LocalDate initialMoment, LocalDate finalMoment) {
        return (d.isEqual(initialMoment) || d.isAfter(initialMoment))
                && (d.isEqual(finalMoment) || d.isBefore(finalMoment));
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
                .toList();
    }

    private BigDecimal adjustedCashFlowAmount(InvestmentEvent ie) {

        final var cashFlowDate = ie.getDate();

        final var cashFlowTime = ChronoUnit.DAYS.between(this.initialMoment, cashFlowDate);

        return this.cashFlowAmount(ie)
                .multiply(BigDecimal.valueOf(this.daysBetween - cashFlowTime).divide(BigDecimal.valueOf(Math.max(1l, this.daysBetween)), C), C);
    }

    private BigDecimal cashFlowAmount(InvestmentEvent ie) {

        final var ym = YearMonth.from(ie.getDate());
        final var fx = ie.getFx() != null
                ? new MoneyAmount(ie.getAmount().multiply(ie.getFx(), C), USD)
                : ForeignExchanges.getMoneyAmountForeignExchange(ie.getCurrency(), currency).apply(ie.getMoneyAmount(), ym);

        return nominal
                ? fx.amount()
                : Inflation.USD_INFLATION.adjust(fx, ym, Inflation.USD_INFLATION.getTo()).amount();
    }

    private MoneyAmount portfolioValue(YearMonth ym) {
        final var toDate = ym.atEndOfMonth();
        return this.getInvestments()
                .stream()
                .filter(i -> i.isCurrent(toDate))
                .map(Investment::getInvestment)
                .map(asset -> ForeignExchanges.getMoneyAmountForeignExchange(asset.getCurrency(), currency).apply(asset.getMoneyAmount(), ym))
                .map(ma -> nominal ? ma : Inflation.USD_INFLATION.adjust(ma, ym, Inflation.USD_INFLATION.getTo()))
                .reduce(this.zeroAmount, MoneyAmount::add)
                .max(this.zeroAmount);
    }

    public ModifiedDietzReturnResult get() {

        final var v1 = this.portfolioValue(YearMonth.of(this.finalMoment.getYear(), this.finalMoment.getMonthValue()));
        final var v0 = this.portfolioValue(YearMonth.of(this.initialMoment.getYear(), this.initialMoment.getMonthValue()).plusMonths(-1));

        final var cashFlowSum = this.cashFlows()
                .stream()
                .reduce(ZERO, BigDecimal::add);

        final var adjustedCashFlowSum = this.adjustedCashFlows()
                .stream()
                .reduce(ZERO, BigDecimal::add);

        if (v0.isZero() && adjustedCashFlowSum.signum() == 0) {
            return new ModifiedDietzReturnResult(ZERO, ZERO);
        }

        final var result = v1.amount()
                .subtract(v0.amount(), C)
                .subtract(cashFlowSum, C)
                .divide(v0.amount().add(adjustedCashFlowSum, C), C)
                .max(ONE.negate());

        return new ModifiedDietzReturnResult(result, annualized(result));

    }

    public ModifiedDietzReturnResult monthlyLinked() {
        List<BigDecimal> monthyMDR = new ArrayList<>(60);

        final var from = YearMonth.from(this.initialMoment).plusMonths(-1);
        final var to = YearMonth.from(this.finalMoment);

        for (var ym = from; ym.compareTo(to) < 0; ym = ym.plusMonths(1)) {

            var next = ym.plusMonths(1);

            final var st = ym.atEndOfMonth();
            final var fn = next.atEndOfMonth();
            monthyMDR.add(new ModifiedDietzReturn(
                    this.investments,
                    this.currency,
                    this.nominal,
                    st,
                    fn).get().getMoneyWeighted());
        }

        final var value = monthyMDR.stream()
                .map(ONE::add)
                .reduce(ONE, BigDecimal::multiply)
                .subtract(ONE);

        return new ModifiedDietzReturnResult(value, annualized(value));
    }

    private BigDecimal annualized(BigDecimal value) {

        if (this.daysBetween == 0) {
            return ZERO;
        }

        return BigDecimal.valueOf(Math.pow(1.0d + value.doubleValue(), 365.0d / (double) this.daysBetween) - 1.0d);
    }

}
