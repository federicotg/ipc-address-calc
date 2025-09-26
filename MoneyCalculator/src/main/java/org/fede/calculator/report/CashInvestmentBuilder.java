/*
 * Copyright (C) 2021 fede
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import org.fede.calculator.money.MathConstants;
import static org.fede.calculator.money.Currency.USD;
import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.InvestmentAsset;
import org.fede.calculator.money.series.InvestmentEvent;
import org.fede.calculator.money.series.InvestmentType;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.YearMonth;

/**
 *
 * @author fede
 */
public class CashInvestmentBuilder {

    private final Supplier<MoneyAmountSeries> cash;

    private List<Investment> data;

    public CashInvestmentBuilder(Supplier<MoneyAmountSeries> cash) {
        this.cash = cash;
    }

    public List<Investment> cashInvestments() {

        if (this.data == null) {

            final var cashInvesments = this.cash.get();

            this.data = new ArrayList<>(cashInvesments.getFrom().monthsUntil(cashInvesments.getTo()));
            
            for (var ym = cashInvesments.getFrom(); ym.compareTo(cashInvesments.getTo()) <= 0; ym = ym.next()) {

                var currentSavedUSD = cashInvesments.getAmountOrElseZero(ym).getAmount();
                var total = this.total(this.data);
                if (currentSavedUSD.compareTo(total) > 0) {
                    this.data.add(this.newInvestment(currentSavedUSD.subtract(total, MathConstants.C), ym));
                } else if (currentSavedUSD.compareTo(total) < 0) {

                    this.sellUntilBelow(currentSavedUSD, this.data, ym);
                    total = this.total(this.data);
                    if (currentSavedUSD.compareTo(total) > 0) {
                        this.data.add(this.newInvestment(currentSavedUSD.subtract(total, MathConstants.C), ym));
                    }
                }
            }
        }

        return this.data;

    }

    private void sellUntilBelow(BigDecimal amount, List<Investment> investments, YearMonth ym) {

        while (total(investments).compareTo(amount) > 0) {
            investments.stream()
                    .filter(i -> Objects.isNull(i.getOut()))
                    .min(Comparator.comparing(Investment::getInitialDate))
                    .ifPresent(i -> this.sellInvestment(i, ym));
        }
    }

    private void sellInvestment(Investment inv, YearMonth ym) {
        final var out = new InvestmentEvent();
        out.setAmount(inv.getInvestment().getAmount());
        out.setCurrency(USD);
        out.setDate(ym.prev().asToDate());
        out.setFee(ZERO);
        out.setTransferFee(ZERO);
        inv.setOut(out);
    }

    private Investment newInvestment(BigDecimal amount, YearMonth ym) {
        final var in = new InvestmentEvent();
        in.setAmount(amount);
        in.setCurrency(USD);
        in.setDate(ym.asDate());
        in.setFee(ZERO);
        in.setTransferFee(ZERO);

        final var asset = new InvestmentAsset();
        asset.setAmount(amount);
        asset.setCurrency(USD);

        final var inv = new Investment();
        inv.setIn(in);
        inv.setInvestment(asset);
        inv.setType(InvestmentType.USD_CASH);
        return inv;
    }

    private BigDecimal total(List<Investment> investments) {
        return investments.stream()
                .filter(i -> i.getOut() == null)
                .map(Investment::getInvestment)
                .map(InvestmentAsset::getAmount)
                .reduce(ZERO, BigDecimal::add);
    }

}
