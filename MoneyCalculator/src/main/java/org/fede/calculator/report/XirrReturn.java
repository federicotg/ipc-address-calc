/*
 * Copyright (C) 2026 federicogentile
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
import static java.math.BigDecimal.ZERO;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.apache.commons.math3.exception.NoBracketingException;
import org.fede.calculator.money.Currency;
import static org.fede.calculator.money.Currency.USD;
import org.fede.calculator.money.ForeignExchanges;
import org.fede.calculator.money.Inflation;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.InvestmentEvent;

/**
 *
 * @author fede
 */
public class XirrReturn {

    private static final double[] RATE_BRACKETS = {
        -0.999999999d, -0.99d, -0.90d, -0.75d, -0.50d, -0.25d,
        0.0d, 0.10d, 0.25d, 0.50d, 1.0d, 2.0d, 5.0d, 10.0d,
        20.0d, 50.0d, 100.0d, 1_000.0d
    };

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
                .reduce(XirrReturn::max)
                .get();
    }

    private static boolean between(LocalDate d, LocalDate initialMoment, LocalDate finalMoment) {
        return (d.isEqual(initialMoment) || d.isAfter(initialMoment))
                && (d.isEqual(finalMoment) || d.isBefore(finalMoment));
    }

    public XirrReturn(List<Investment> investments, boolean nominal) {
        this(investments, USD, nominal);
    }

    public XirrReturn(List<Investment> investments, Currency currency, boolean nominal) {
        this(investments,
                currency,
                nominal,
                investments
                        .stream()
                        .map(Investment::getIn)
                        .map(InvestmentEvent::getDate)
                        .reduce(XirrReturn::min)
                        .get(),
                finalMoment(investments));
    }

    public XirrReturn(List<Investment> investments, boolean nominal, LocalDate initialMoment, LocalDate finalMoment) {
        this(investments, USD, nominal, initialMoment, finalMoment);
    }

    public XirrReturn(List<Investment> investments, Currency currency, boolean nominal, LocalDate initialMoment, LocalDate finalMoment) {
        this.investments = investments;
        this.currency = currency;
        this.zeroAmount = MoneyAmount.zero(this.currency);
        this.nominal = nominal;
        this.initialMoment = max(initialMoment,
                investments
                        .stream()
                        .map(Investment::getIn)
                        .map(InvestmentEvent::getDate)
                        .reduce(XirrReturn::min)
                        .get());

        this.finalMoment = min(finalMoment, LocalDate.now());
        this.daysBetween = ChronoUnit.DAYS.between(this.initialMoment, this.finalMoment);
    }

    private MoneyAmount portfolioValue(YearMonth ym) {
        final var toDate = ym.atEndOfMonth();
        return this.investments
                .stream()
                .filter(i -> i.isCurrent(toDate))
                .map(Investment::getInvestment)
                .map(asset -> ForeignExchanges.getMoneyAmountForeignExchange(asset.getCurrency(), currency).apply(asset.getMoneyAmount(), ym))
                .map(ma -> nominal ? ma : Inflation.usdInflation().adjust(ma, ym, Inflation.usdInflation().getTo()))
                .reduce(this.zeroAmount, MoneyAmount::add)
                .max(this.zeroAmount);
    }

    private BigDecimal cashFlowAmount(InvestmentEvent ie) {

        final var ym = YearMonth.from(ie.getDate());

        final var fx = ie.getMoneyAmount(this.currency);

        return nominal
                ? fx.amount()
                : Inflation.usdInflation().adjust(fx, ym, Inflation.usdInflation().getTo()).amount();
    }

    private List<CashFlow> cashFlows() {

        final List<CashFlow> cashFlows = new ArrayList<>();

        final var initialValue = this.portfolioValue(YearMonth.from(this.initialMoment).plusMonths(-1));
        if (!initialValue.isZero()) {
            cashFlows.add(new CashFlow(this.initialMoment, initialValue.amount().negate()));
        }

        this.investments
                .stream()
                .map(Investment::getIn)
                .filter(ie -> between(ie.getDate(), this.initialMoment, this.finalMoment))
                .map(ie -> new CashFlow(ie.getDate(), this.cashFlowAmount(ie).negate()))
                .forEach(cashFlows::add);

        this.investments
                .stream()
                .map(Investment::getOut)
                .filter(Objects::nonNull)
                .filter(ie -> between(ie.getDate(), this.initialMoment, this.finalMoment))
                .map(ie -> new CashFlow(ie.getDate(), this.cashFlowAmount(ie)))
                .forEach(cashFlows::add);

        final var finalValue = this.portfolioValue(YearMonth.from(this.finalMoment));
        if (!finalValue.isZero()) {
            cashFlows.add(new CashFlow(this.finalMoment, finalValue.amount()));
        }

        return cashFlows;
    }

    public ModifiedDietzReturnResult get() {

        if (this.daysBetween == 0) {
            return new ModifiedDietzReturnResult(ZERO, ZERO);
        }

        final var cashFlows = this.cashFlows();

        final var hasPositive = cashFlows.stream().map(CashFlow::amount).anyMatch(amount -> amount.signum() > 0);
        final var hasNegative = cashFlows.stream().map(CashFlow::amount).anyMatch(amount -> amount.signum() < 0);

        if (!hasPositive || !hasNegative) {
            return new ModifiedDietzReturnResult(ZERO, ZERO);
        }

        final var xirr = this.solve(cashFlows);
        final var totalReturn = Math.pow(1.0d + xirr, (double) this.daysBetween / 365.0d) - 1.0d;

        return new ModifiedDietzReturnResult(BigDecimal.valueOf(totalReturn), BigDecimal.valueOf(xirr));
    }

    private double solve(List<CashFlow> cashFlows) {

        final var solver = new BrentSolver(1.0e-12d, 1.0e-10d);

        for (int i = 0; i < RATE_BRACKETS.length - 1; i++) {
            try {
                return solver.solve(
                        1_000,
                        rate -> this.netPresentValue(cashFlows, rate),
                        RATE_BRACKETS[i],
                        RATE_BRACKETS[i + 1]);
            } catch (NoBracketingException e) {
                // Try the next interval. Some cash-flow series only bracket at
                // very high gains or deep losses.
            }
        }

        return ZERO.doubleValue();
    }

    private double netPresentValue(List<CashFlow> cashFlows, double rate) {

        return cashFlows
                .stream()
                .mapToDouble(cashFlow -> cashFlow.amount().doubleValue()
                / Math.pow(1.0d + rate, ChronoUnit.DAYS.between(this.initialMoment, cashFlow.date()) / 365.0d))
                .sum();
    }

    private record CashFlow(LocalDate date, BigDecimal amount) {
    }

}
