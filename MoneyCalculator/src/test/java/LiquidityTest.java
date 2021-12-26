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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.fede.calculator.money.MathConstants;
import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.InvestmentAsset;
import org.fede.calculator.money.series.InvestmentEvent;
import org.fede.calculator.money.series.InvestmentType;
import org.fede.calculator.money.series.SeriesReader;
import org.fede.calculator.money.series.YearMonth;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author federicogentile
 */
public class LiquidityTest {

    @Test
    public void liq() {

        final var liq = SeriesReader.readSeries("/saving/ahorros-dolar-liq.json");

        final List<Investment> investments = new ArrayList<>();

        for (var ym = liq.getFrom(); ym.compareTo(liq.getTo()) <= 0; ym = ym.next()) {

            var currentSavedUSD = liq.getAmountOrElseZero(ym).getAmount();
            var total = this.total(investments);
            if (currentSavedUSD.compareTo(total) > 0) {
                investments.add(this.newInvestment(currentSavedUSD.subtract(total, MathConstants.CONTEXT), ym));
            } else if (currentSavedUSD.compareTo(total) < 0) {

                this.sellUntilBelow(currentSavedUSD, investments, ym);
                total = this.total(investments);
                if (currentSavedUSD.compareTo(total) > 0) {
                    investments.add(this.newInvestment(currentSavedUSD.subtract(total, MathConstants.CONTEXT), ym));
                }
            }
        }

        for (var ym = liq.getFrom(); ym.compareTo(liq.getTo()) <= 0; ym = ym.next()) {
            var currentSavedUSD = liq.getAmountOrElseZero(ym).getAmount();
            var invested = this.total(investments, ym);
            //System.out.println(currentSavedUSD+" "+invested);
            assertEquals("Saved and invested must be the same.", 0, currentSavedUSD.compareTo(invested));
        }

    }

    private void sellUntilBelow(BigDecimal amount, List<Investment> investments, YearMonth ym) {
        while (total(investments).compareTo(amount) > 0) {
            investments.stream()
                    .filter(i -> i.getOut() == null)
                    .findAny()
                    .ifPresent(i -> this.sellInvestment(i, ym));
        }
    }

    private void sellInvestment(Investment inv, YearMonth ym) {
        final var out = new InvestmentEvent();
        out.setAmount(inv.getInvestment().getAmount());
        out.setCurrency("USD");
        out.setDate(ym.asDate());
        out.setFee(BigDecimal.ZERO);
        out.setTransferFee(BigDecimal.ZERO);
        inv.setOut(out);
    }

    private Investment newInvestment(BigDecimal amount, YearMonth ym) {
        final var in = new InvestmentEvent();
        in.setAmount(amount);
        in.setCurrency("USD");
        in.setDate(ym.asDate());
        in.setFee(BigDecimal.ZERO);
        in.setTransferFee(BigDecimal.ZERO);

        final var asset = new InvestmentAsset();
        asset.setAmount(amount);
        asset.setCurrency("USD");

        final var inv = new Investment();
        inv.setIn(in);
        inv.setInvestment(asset);
        inv.setType(InvestmentType.USD);
        return inv;
    }

    private BigDecimal total(List<Investment> investments) {
        return investments.stream()
                .filter(i -> i.getOut() == null)
                .map(Investment::getInvestment)
                .map(InvestmentAsset::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal total(List<Investment> investments, YearMonth ym) {
        return investments.stream()
                .filter(i -> i.isCurrent(ym.asToDate()))
                .map(Investment::getInvestment)
                .map(InvestmentAsset::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
