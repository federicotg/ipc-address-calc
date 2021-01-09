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

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.Date;
import org.fede.calculator.money.ForeignExchanges;
import org.fede.calculator.money.Inflation;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.RealProfit;
import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.InvestmentAsset;
import org.fede.calculator.money.series.InvestmentEvent;
import org.fede.calculator.money.series.InvestmentType;
import org.fede.calculator.money.series.YearMonth;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author fede
 */
public class RealProfitTest {

    public RealProfitTest() {
    }

    @Test
    public void currentProfit() {

        final var initialAmount = new MoneyAmount(new BigDecimal("1000"), "USD");

        final var assetAmount = new MoneyAmount(new BigDecimal("3"), "CSPX");

        final var initialDate = Date.from(LocalDate.of(2019, Month.MARCH, 13).atStartOfDay(ZoneId.systemDefault()).toInstant());

        final var initialYm = YearMonth.of(initialDate);
        final var finalYm = Inflation.USD_INFLATION.getTo();

        InvestmentEvent in = new InvestmentEvent();
        in.setAmount(initialAmount.getAmount());
        in.setCurrency(initialAmount.getCurrency());
        in.setDate(initialDate);

        InvestmentAsset asset = new InvestmentAsset();
        asset.setAmount(new BigDecimal("3"));
        asset.setCurrency("CSPX");

        Investment inv = new Investment();
        inv.setIn(in);
        inv.setInvestment(asset);
        inv.setType(InvestmentType.ETF);

        final var fee = new BigDecimal("0.006");
        final var tax = new BigDecimal("0.07");

        final var profit = new RealProfit(inv, tax, fee);

        final var realUsdInvestment = Inflation.USD_INFLATION.adjust(initialAmount, initialYm.getYear(), initialYm.getMonth(), finalYm.getYear(), finalYm.getMonth());

        assertEquals(realUsdInvestment,
                profit.getRealInitialAmount());

        final var usdReturns = ForeignExchanges.getForeignExchange(assetAmount.getCurrency(), "USD").exchange(assetAmount, "USD", finalYm.getYear(), finalYm.getMonth());

        final var usdRealProfit = usdReturns.subtract(realUsdInvestment);

        assertEquals(usdRealProfit, profit.getRealProfit());

        final var feeAmount = usdReturns.getAmount().multiply(fee, MathContext.DECIMAL64);
        final var afterFee = usdReturns.getAmount().subtract(feeAmount, MathContext.DECIMAL64);

        final var taxAmount = afterFee
                .subtract(initialAmount.getAmount(), MathContext.DECIMAL64)
                .multiply(tax, MathContext.DECIMAL64);

        final var netAmount = afterFee.subtract(taxAmount);

        assertEquals(new MoneyAmount(netAmount, "USD").subtract(realUsdInvestment), profit.getAfterFeesAndTaxesProfit());

    }
}
