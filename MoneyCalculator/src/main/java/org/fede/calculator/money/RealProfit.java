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

import java.math.BigDecimal;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import static org.fede.calculator.money.Inflation.USD_INFLATION;
import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.InvestmentEvent;
import org.fede.calculator.money.series.YearMonth;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
public class RealProfit {

    private static final String REPORT_PATTERN = "{0} {1} => {2}. {3} {4}. Net {5} {6} {7}";

    public static String plusMinus(BigDecimal pct) {

        final var sign = pct.signum() >= 0
                ? "+"
                : "-";

        return IntStream.range(0, pct.abs().movePointRight(2).setScale(0, HALF_UP).intValue())
                .mapToObj(index -> sign)
                .collect(Collectors.joining());
    }

    private final NumberFormat moneyFormat = NumberFormat.getCurrencyInstance();
    private final NumberFormat percentFormat = NumberFormat.getPercentInstance();
    private final DateFormat df = DateFormat.getDateInstance();

    private final Investment nominalInvestment;
    private final Investment realInvestment;
    private final MoneyAmount profit;

    private final MoneyAmount afterFeesAndTaxesProfit;

    private final BigDecimal tax;
    private final BigDecimal fee;

    public RealProfit(Investment nominalInvestment) {
        this(nominalInvestment, ZERO, ZERO);
    }

    public RealProfit(Investment nominalInvestment, BigDecimal tax, BigDecimal fee) {
        this.percentFormat.setMinimumFractionDigits(2);
        this.nominalInvestment = ForeignExchanges.exchange(nominalInvestment, "USD");
        this.realInvestment = Inflation.USD_INFLATION.real(this.nominalInvestment);

        this.fee = fee;
        this.tax = tax;

        final YearMonth limit = USD_INFLATION.getTo();

        MoneyAmount p = this.profit(this.nominalInvestment);

        MoneyAmount usdProfit = null;

        if (p.getCurrency().equals("USD")) {
            usdProfit = p;
        } else {
            usdProfit = ForeignExchanges.getForeignExchange(this.nominalInvestment.getCurrency(), "USD")
                    .exchange(p, "USD", limit.getYear(), limit.getMonth());
        }

        final var afterFee = usdProfit.adjust(ONE, ONE.subtract(fee));

        final var afterFeesAndTaxes = afterFee
                .subtract(afterFee.subtract(this.nominalInvestment.getInitialMoneyAmount()).adjust(ONE, tax));

        if (this.nominalInvestment.getOut() != null && this.nominalInvestment.getOut().getDate().before(new Date())) {
            final YearMonth endYM = new YearMonth(this.nominalInvestment.getOut().getDate());
            this.profit = USD_INFLATION.adjust(usdProfit, endYM.getYear(), endYM.getMonth(), limit.getYear(), limit.getMonth());

            this.afterFeesAndTaxesProfit = USD_INFLATION.adjust(afterFeesAndTaxes, endYM.getYear(), endYM.getMonth(), limit.getYear(), limit.getMonth());

        } else {
            this.profit = usdProfit;
            this.afterFeesAndTaxesProfit = afterFeesAndTaxes;
        }
    }

    private MoneyAmount profit(Investment in) {

        if (in.getInterest() != null || in.getOut() == null) {

            return Optional.ofNullable(in)
                    .filter(inv -> inv.getInterest() != null)
                    .map(inv -> inv.getInvestment().getMoneyAmount().add(this.interest(inv)))
                    .orElse(in.getInvestment().getMoneyAmount());
        }

        return new MoneyAmount(
                in.getOut().getMoneyAmount().getAmount(),
                in.getOut().getCurrency());
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

    private String fmt(MoneyAmount ma) {
        return ma.getCurrency().concat(" ").concat(moneyFormat.format(ma.getAmount()));
    }

    public BigDecimal getRate() {
        return this.getRealProfit()
                .getAmount()
                .divide(this.realInvestment.getInitialMoneyAmount().getAmount(), MathConstants.CONTEXT);
    }

    /**
     * 12 abr. 2019 USD $ 976,01 => USD $ 1.009,86. USD $ 33,85 3,47 % +
     *
     * @return
     */
    @Override
    public String toString() {

        final var capitalGain = this.profit.subtract(this.realInvestment.getInitialMoneyAmount());

        final var afterFeesAndTaxesCapitalGain = this.afterFeesAndTaxesProfit.subtract(this.realInvestment.getInitialMoneyAmount());

        final var pctAfterTaxAndFee = afterFeesAndTaxesCapitalGain.getAmount()
                .divide(this.realInvestment.getInitialMoneyAmount().getAmount(), MathConstants.CONTEXT);

        final BigDecimal pct = this.getRate();

        return MessageFormat.format(REPORT_PATTERN,
                this.df.format(this.nominalInvestment.getInitialDate()),
                this.fmt(this.realInvestment.getInitialMoneyAmount()),
                this.fmt(this.profit),
                this.fmt(capitalGain),
                this.percentFormat.format(pct),
                this.fmt(afterFeesAndTaxesCapitalGain),
                this.percentFormat.format(pctAfterTaxAndFee),
                plusMinus(pctAfterTaxAndFee)
        );
    }

    public MoneyAmount getRealProfit() {
        return this.profit.subtract(this.realInvestment.getInitialMoneyAmount());
    }

    public MoneyAmount getRealInitialAmount() {
        return this.realInvestment.getInitialMoneyAmount();
    }

    public MoneyAmount getAfterFeesAndTaxesProfit() {
        return afterFeesAndTaxesProfit.subtract(this.realInvestment.getInitialMoneyAmount());
    }

}
