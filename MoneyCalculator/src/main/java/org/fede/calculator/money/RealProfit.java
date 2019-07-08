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
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.fede.calculator.money.series.Investment;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
public class RealProfit {

    private static final String REPORT_PATTERN = "{0} {1} => {2}. {3} {4} {5}";
    private static final BigDecimal BIG_LOSS = new BigDecimal("-0.10");
    private static final BigDecimal BIG_WIN = new BigDecimal("0.10");
    private static final BigDecimal LOSS = new BigDecimal("-0.05");
    private static final BigDecimal WIN = new BigDecimal("0.05");

    private final NumberFormat moneyFormat = NumberFormat.getCurrencyInstance();
    private final NumberFormat percentFormat = NumberFormat.getPercentInstance();
    private final DateFormat df = DateFormat.getDateInstance();

    private final Investment nominalInvestment;
    private final Investment realInvestment;
    private final MoneyAmount profit;

    public RealProfit(Investment nominalInvestment) {
        this.percentFormat.setMinimumFractionDigits(2);
        this.nominalInvestment = ForeignExchanges.exchange(nominalInvestment, "USD");
        this.realInvestment = Inflation.USD_INFLATION.real(this.nominalInvestment);
        this.profit = this.profit(this.nominalInvestment);
    }

    private MoneyAmount profit(Investment in) {

        if (in.getInterest() != null || in.getOut() == null) {

            final BigDecimal currentAmount = Optional.ofNullable(in)
                    .filter(inv -> inv.getInterest() != null)
                    .map(this::addInterest)
                    .orElse(in.getInvestment().getMoneyAmount().getAmount());

            return new MoneyAmount(
                    currentAmount.subtract(in.getIn().getMoneyAmount().getAmount()),
                    in.getInvestment().getMoneyAmount().getCurrency());

        }

        return new MoneyAmount(
                in.getOut().getMoneyAmount().getAmount().subtract(in.getIn().getMoneyAmount().getAmount()),
                in.getOut().getCurrency());
    }

    private BigDecimal days(Investment in) {
        return BigDecimal.valueOf(ChronoUnit.DAYS.between(
                LocalDate.ofInstant(in.getInitialDate().toInstant(), ZoneId.systemDefault()),
                LocalDate.now()));
    }

    private BigDecimal addInterest(Investment in) {
        return in.getInvestment()
                .getAmount()
                .setScale(12, RoundingMode.HALF_UP)
                .multiply(in.getInterest())
                .multiply(this.days(in))
                .setScale(12, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(ChronoUnit.YEARS.getDuration().toDays()), MathContext.DECIMAL64)
                .add(in.getIn().getMoneyAmount().getAmount());
    }

    public static String plusMinus(BigDecimal pct) {

        if (pct.compareTo(BIG_LOSS) <= 0) {
            return "----";
        }
        if (pct.compareTo(LOSS) <= 0) {
            return "--";
        }
        if (pct.compareTo(BigDecimal.ZERO) <= 0) {
            return "-";
        }
        if (pct.compareTo(BIG_WIN) >= 0) {
            return "++++";
        }
        if (pct.compareTo(WIN) >= 0) {
            return "++";
        }
        if (pct.compareTo(BigDecimal.ZERO) >= 0) {
            return "+";
        }
        return "";

    }

    private String fmt(MoneyAmount ma) {
        return ma.getCurrency().concat(" ").concat(moneyFormat.format(ma.getAmount()));
    }


public BigDecimal getRate(){
    return this.getRealProfit()
                .getAmount()
                .divide(this.realInvestment.getInitialMoneyAmount().getAmount(), MathContext.DECIMAL64);

}

    /**
     * 12 abr. 2019 USD $ 976,01 => USD $ 1.009,86. USD $ 33,85 3,47 % +
     *
     * @return
     */
    @Override
    public String toString() {

        final BigDecimal pct = this.getRate();
        return MessageFormat.format(REPORT_PATTERN,
                this.df.format(this.nominalInvestment.getInitialDate()),
                this.fmt(this.realInvestment.getInitialMoneyAmount()),
                this.fmt(nominalInvestment.getInitialMoneyAmount().add(this.profit)),
                this.fmt(nominalInvestment.getInitialMoneyAmount().add(this.profit).subtract(this.realInvestment.getInitialMoneyAmount())),
                this.percentFormat.format(pct),
                plusMinus(pct));
    }

    public MoneyAmount getRealProfit() {
        return nominalInvestment.getInitialMoneyAmount().add(this.profit).subtract(this.realInvestment.getInitialMoneyAmount());
    }

    public MoneyAmount getRealInitialAmount() {
        return this.realInvestment.getInitialMoneyAmount();
    }

}
