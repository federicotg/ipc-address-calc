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
import java.text.MessageFormat;
import org.fede.calculator.money.ArgentinaInflation;
import org.fede.calculator.money.CPIInflation;
import org.fede.calculator.money.Inflation;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.series.ArgentinaCompoundCPISeries;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.SeriesReader;
import org.junit.Test;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
public class InflationComparisonTest {

    public InflationComparisonTest() {
    }

    @Test
    public void print() {

        final Inflation cqp = new ArgentinaInflation(new CPIInflation(new ArgentinaCompoundCPISeries(), "ARS"));
        final Inflation priceStats = new ArgentinaInflation(new CPIInflation(SeriesReader.readIndexSeries("index/cpi_arg.json"), "ARS"));
        final MoneyAmount onePeso = new MoneyAmount(BigDecimal.ONE, "ARS");

        final MoneyAmountSeries cqpSeries = cqp.adjust(onePeso, 1999, 1);
        final MoneyAmountSeries priceStatsSeries = priceStats.adjust(onePeso, 1999, 1);

        cqpSeries.forEach((yearMonth, amount) -> {
            System.out.println(MessageFormat.format("{0}\t{1}\t{2}\t{3}", yearMonth.getYear(), yearMonth.getMonth(), amount.toString(), priceStatsSeries.getAmount(yearMonth)));
        });

    }
}
