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
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.InvestmentEvent;
import org.fede.calculator.money.series.YearMonth;
import org.fede.util.Pair;

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

    public ModifiedDietzReturn(List<Investment> investments, String currency, boolean nominal) {
        this(
                investments,
                currency,
                nominal,
                investments
                        .stream()
                        .map(Investment::getIn)
                        .map(InvestmentEvent::getDate)
                        .map(d -> d.toInstant())
                        .reduce((i1, i2) -> i1.compareTo(i2) <= 0 ? i1 : i2)
                        .map(i -> LocalDate.ofInstant(i, ZoneId.systemDefault())).get(),
                LocalDate.ofInstant(Inflation.USD_INFLATION.getTo().asToDate().toInstant(), ZoneId.systemDefault()));
    }

    public ModifiedDietzReturn(List<Investment> investments, String currency, boolean nominal, LocalDate initialMoment, LocalDate finalMoment) {
        this.investments = investments;
        this.currency = currency;
        this.nominal = nominal;
        this.initialMoment = initialMoment;
        this.finalMoment = finalMoment;
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

    private List<BigDecimal> cashFlows(Function<InvestmentEvent, BigDecimal> cashFlowFunction) {

        return Stream.concat(
                this.getInvestments()
                        .stream()
                        .map(Investment::getIn)
                        .map(cashFlowFunction),
                this.getInvestments()
                        .stream()
                        .map(Investment::getOut).filter(Objects::nonNull)
                        .map(cashFlowFunction)
                        .map(BigDecimal::negate))
                .collect(Collectors.toList());
    }

    private BigDecimal adjustedCashFlowAmount(InvestmentEvent ie) {

        final var cashFlowDate = LocalDateTime.ofInstant(ie.getDate().toInstant(), ZoneId.systemDefault());

        final var timeLength = ChronoUnit.DAYS.between(this.initialMoment, this.finalMoment);

        final var cashFlowTime = ChronoUnit.DAYS.between(this.initialMoment, cashFlowDate);

        return this.cashFlowAmount(ie)
                .multiply(BigDecimal.valueOf(timeLength - cashFlowTime).divide(BigDecimal.valueOf(timeLength), MathConstants.CONTEXT), MathConstants.CONTEXT);
    }

    private BigDecimal cashFlowAmount(InvestmentEvent ie) {

        final var ym = YearMonth.of(ie.getDate());
        final var fx = ForeignExchanges.getForeignExchange(ie.getCurrency(), currency)
                .exchange(ie.getMoneyAmount(), currency, ym.getYear(), ym.getMonth());

        final var limit = Inflation.USD_INFLATION.getTo();

        return nominal
                ? fx.getAmount()
                : Inflation.USD_INFLATION.adjust(fx, ym.getYear(), ym.getMonth(), limit.getYear(), limit.getMonth()).getAmount();

    }

    private MoneyAmount portfolioValue(YearMonth ym) {

        final var limit = Inflation.USD_INFLATION.getTo();

        return this.getInvestments().stream()
                .filter(i -> i.isCurrent(ym.asToDate()))
                .map(Investment::getInvestment)
                .map(asset -> ForeignExchanges.getForeignExchange(asset.getCurrency(), currency).exchange(asset.getMoneyAmount(), currency, ym.getYear(), ym.getMonth()))
                .map(ma -> nominal ? ma : Inflation.USD_INFLATION.adjust(ma, ym.getYear(), ym.getMonth(), limit.getYear(), limit.getMonth()))
                .reduce(new MoneyAmount(ZERO, currency), MoneyAmount::add);
    }

    private String line(YearMonth ym, MoneyAmount start, MoneyAmount cashFlow, MoneyAmount end) {
        return MessageFormat.format("{0}-{1};{2};{3};{4}",
                String.valueOf(ym.getYear()),
                ym.getMonth(),
                start.getAmount(),
                cashFlow.getAmount(),
                end.getAmount());
    }

    public Pair<BigDecimal, BigDecimal> get() {

        final var v1 = this.portfolioValue(YearMonth.of(this.finalMoment.getYear(), this.finalMoment.getMonthValue()));
        final var v0 = this.portfolioValue(YearMonth.of(this.initialMoment.getYear(), this.finalMoment.getMonthValue()).prev());
        final var cashFlowSum = this.cashFlows().stream().reduce(ZERO, BigDecimal::add);
        final var adjustedCashFlowSum = this.adjustedCashFlows().stream().reduce(ZERO, BigDecimal::add);

        final var result = v1.getAmount()
                .subtract(v0.getAmount(), MathConstants.CONTEXT)
                .subtract(cashFlowSum, MathConstants.CONTEXT)
                .divide(v0.getAmount().add(adjustedCashFlowSum, MathConstants.CONTEXT), MathConstants.CONTEXT);

        return Pair.of(result, BigDecimal.valueOf(Math.pow(1.0d + result.doubleValue(), 365.0d / (double) ChronoUnit.DAYS.between(this.initialMoment, this.finalMoment)) - 1.0d));

    }

}
