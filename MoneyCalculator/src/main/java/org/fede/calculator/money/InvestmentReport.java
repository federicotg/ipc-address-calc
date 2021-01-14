/*
 * Copyright (C) 2021 federico
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
import java.math.MathContext;
import static java.math.RoundingMode.HALF_UP;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import static org.fede.calculator.money.Inflation.USD_INFLATION;
import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.InvestmentEvent;
import org.fede.calculator.money.series.YearMonth;

/**
 *
 * @author federico
 */
public class InvestmentReport {

    private static final String REPORT_PATTERN = "{0} {1} {2} {3} {4} {5} {6} {7} {8} {9} {10} {11}";

    private static final NumberFormat MONEY_FORMAT = NumberFormat.getCurrencyInstance();
    private static final DateFormat DF = DateFormat.getDateInstance();
    private static final NumberFormat PCT_FORMAT = NumberFormat.getPercentInstance();

    static {
        PCT_FORMAT.setMinimumFractionDigits(2);
    }

    private final Investment nominal;
    private final Investment real;
    private final BigDecimal capitalGainsTaxRate;
    private final BigDecimal feeRate;
    private final BigDecimal feeTaxRate;

    public InvestmentReport(
            Investment nominalInv,
            BigDecimal capitalGainsTaxRate,
            BigDecimal feeRate,
            BigDecimal feeTaxRate) {

        this.capitalGainsTaxRate = capitalGainsTaxRate;
        this.feeRate = feeRate;
        this.feeTaxRate = feeTaxRate;

        this.nominal = ForeignExchanges.exchange(nominalInv, "USD");
        this.real = Inflation.USD_INFLATION.real(this.nominal);

    }

    /* sin tax y fee*/
    public MoneyAmount getNetRealInvestment() {

        return this.real.getInitialMoneyAmount();
    }

    /*
     * 
     * con tax y fee
     */
    public MoneyAmount getGrossRealInvestment() {
        return this.getNetRealInvestment()
                .add(this.real.getIn().getFeeMoneyAmount().adjust(ONE, this.feeTaxRate));
    }

    /*
     * 
     * sin tax y fee para calcular capital gains
     */
    private MoneyAmount getNetNominalInvestment() {
        return this.nominal.getInitialMoneyAmount();
    }

    private MoneyAmount getGrossNominalInvestment() {
        return this.nominal.getInitialMoneyAmount().add(this.nominal.getIn().getFeeMoneyAmount());
    }

    /*
     * sin taxes y fees 
     * @return 
     */
    public MoneyAmount getGrossRealProfit() {

        return this.currentValue(this.real)
                .subtract(this.getNetRealInvestment());

    }

    private MoneyAmount feeAmount() {
        return this.real.getIn().getFeeMoneyAmount().adjust(ONE, this.feeTaxRate)
                .add(this.currentValue(real).adjust(ONE, this.feeRate));
    }

    private MoneyAmount capitalGainsTax() {

        final var capitalGain = this.currentValue(this.real)
                .subtract(this.getNetNominalInvestment());

        if (capitalGain.getAmount().signum() <= 0) {
            return new MoneyAmount(BigDecimal.ZERO, capitalGain.getCurrency());
        }

        return capitalGain.adjust(ONE, this.capitalGainsTaxRate);
    }

    /*
    con taxes y fees en contra
     */
    public MoneyAmount getNetRealProfit() {

        final var currentValue = this.currentValue(this.real);

        final var feeAmount = currentValue.adjust(ONE, this.feeRate);

        final var capitalGainAmount = this.capitalGainsTax();

        return currentValue
                .subtract(this.getGrossRealInvestment())
                .subtract(feeAmount)
                .subtract(capitalGainAmount);
    }

    private BigDecimal percent(MoneyAmount value, MoneyAmount total) {
        return value.getAmount()
                .divide(total.getAmount(), MathContext.DECIMAL64);
    }

    @Override
    public String toString() {

        final var grp = this.getGrossRealProfit();
        final var gri = this.getGrossRealInvestment();
        final var cgt = this.capitalGainsTax();
        final var fa = this.feeAmount();
        final var cv = this.currentValue(this.real);
        return MessageFormat.format(REPORT_PATTERN,
                String.format("%12s", DF.format(this.nominal.getInitialDate())),
                this.fmt(this.getNetRealInvestment()),
                this.fmt(cv),
                this.fmt(grp),
                String.format("%8s", PCT_FORMAT.format(this.percent(grp, gri))),
                this.fmt(this.getNetRealProfit()),
                String.format("%8s", PCT_FORMAT.format(this.percent(this.getNetRealProfit(), gri))),
                String.format("%8s", PCT_FORMAT.format(this.tna())),
                this.fmt(fa),
                String.format("%7s", PCT_FORMAT.format(this.percent(fa, cv))),
                this.fmt(cgt),
                String.format("%7s", PCT_FORMAT.format(this.percent(cgt, cv)))
        );
    }

    private BigDecimal tna() {
        final var days = this.days(real);
        if (days.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        
        final var cumulativeProfit =  this.getNetRealProfit().getAmount().divide(this.getGrossRealInvestment().getAmount(), MathContext.DECIMAL64);
        

        final double x = Math.pow(
                BigDecimal.ONE.add(cumulativeProfit).doubleValue(),
                365.0d / days.doubleValue()) - 1.0d;

        return BigDecimal.valueOf(x);

    }

    private String fmt(MoneyAmount ma) {
        return String.format("%11s", MONEY_FORMAT.format(ma.getAmount()));
    }

    public MoneyAmount getCurrentValue() {
        return this.currentValue(real);
    }

    private MoneyAmount currentValue(Investment in) {

        if (in.getInterest() != null || in.getOut() == null) {

            return this.toUSD(Optional.ofNullable(in)
                    .filter(inv -> inv.getInterest() != null)
                    .map(inv -> inv.getInvestment().getMoneyAmount().add(this.interest(inv)))
                    .orElseGet(in.getInvestment()::getMoneyAmount));
        }

        return this.toUSD(new MoneyAmount(
                in.getOut().getMoneyAmount().getAmount(),
                in.getOut().getCurrency()));

    }

    private MoneyAmount toUSD(MoneyAmount amount) {
        if ("USD".equals(amount.getCurrency())) {
            return amount;
        }

        final YearMonth limit = USD_INFLATION.getTo();
        return ForeignExchanges.getForeignExchange(amount.getCurrency(), "USD")
                .exchange(amount, "USD", limit.getYear(), limit.getMonth());
    }

    private MoneyAmount interest(Investment in) {
        return new MoneyAmount(
                in.getInvestment()
                        .getAmount()
                        .setScale(12, HALF_UP)
                        .multiply(in.getInterest())
                        .multiply(this.days(in))
                        .setScale(12, HALF_UP)
                        .divide(BigDecimal.valueOf(ChronoUnit.YEARS.getDuration().toDays()), MathConstants.CONTEXT),
                in.getInvestment().getCurrency());
    }

    private BigDecimal days(Investment in) {

        LocalDate now = LocalDate.now();

        final LocalDate startDate = LocalDate.ofInstant(in.getInitialDate().toInstant(), ZoneId.systemDefault());

        final LocalDate endDate = Optional.ofNullable(in.getOut())
                .map(InvestmentEvent::getDate)
                .map(Date::toInstant)
                .map(instant -> LocalDate.ofInstant(instant, ZoneId.systemDefault()))
                .filter(date -> date.isBefore(now))
                .orElse(now);

        return BigDecimal.valueOf(ChronoUnit.DAYS.between(startDate, endDate));
    }
}