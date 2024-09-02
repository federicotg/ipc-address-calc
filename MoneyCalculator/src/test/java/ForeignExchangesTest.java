/*
 * Copyright (C) 2016 Federico Tello Gentile <federicotg@gmail.com>
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
import java.math.RoundingMode;
import org.fede.calculator.money.CompoundForeignExchange;
import org.fede.calculator.money.Currency;
import org.fede.calculator.money.ForeignExchanges;
import static org.fede.calculator.money.ForeignExchanges.USD_ARS;
import static org.fede.calculator.money.ForeignExchanges.USD_EUR;
import static org.fede.calculator.money.ForeignExchanges.ARS_CONAAFA;
import static org.fede.calculator.money.ForeignExchanges.getForeignExchange;
import org.fede.calculator.money.MoneyAmount;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author fede
 */
public class ForeignExchangesTest {

    public ForeignExchangesTest() {
    }



    private void identity(Currency currency) {
        assertEquals(ForeignExchanges.getIdentityForeignExchange(currency), getForeignExchange(currency, currency));
    }

    @Test
    public void gettingThem() {

        this.identity(Currency.ARS);
        this.identity(Currency.USD);
        this.identity(Currency.EUR);
        this.identity(Currency.CONBALA);
        this.identity(Currency.CONAAFA);

        assertEquals(USD_ARS, getForeignExchange(Currency.USD, Currency.ARS));
        assertEquals(USD_EUR, getForeignExchange(Currency.USD, Currency.EUR));

        assertEquals(USD_ARS, getForeignExchange(Currency.ARS, Currency.USD));
        assertEquals(USD_EUR, getForeignExchange(Currency.EUR, Currency.USD));
        assertEquals(ARS_CONAAFA, getForeignExchange(Currency.ARS, Currency.CONAAFA));
        assertEquals(ARS_CONAAFA, getForeignExchange(Currency.CONAAFA, Currency.ARS));

        assertEquals(new CompoundForeignExchange(USD_ARS, USD_EUR), getForeignExchange(Currency.ARS, Currency.EUR));

        assertEquals(new CompoundForeignExchange(ARS_CONAAFA, USD_ARS), getForeignExchange(Currency.CONAAFA, Currency.USD));


    }
    
    @Test
    public void euroTest() {
        MoneyAmount oneEuro = new MoneyAmount(BigDecimal.ONE, Currency.EUR);
        MoneyAmount usd = ForeignExchanges.getForeignExchange(Currency.EUR, Currency.USD).exchange(oneEuro, Currency.USD, 2019,1);
        //System.out.println(usd.getAmount());
        assertEquals(0, usd.getAmount().setScale(4, RoundingMode.HALF_UP)
                .compareTo(new BigDecimal("1.1416").setScale(4, RoundingMode.HALF_UP)));
    }
    
    @Test
    public void euroTestInverse() {
        MoneyAmount oneUSD = new MoneyAmount(BigDecimal.ONE, Currency.USD);
        MoneyAmount eur = ForeignExchanges.getForeignExchange(Currency.USD, Currency.EUR).exchange(oneUSD, Currency.EUR, 2019,1);
        //System.out.println(eur.getAmount());
        assertEquals(0, eur.getAmount().setScale(4, RoundingMode.HALF_UP)
                .compareTo(new BigDecimal("0.875963559915908").setScale(4, RoundingMode.HALF_UP)));
    }
    

}
