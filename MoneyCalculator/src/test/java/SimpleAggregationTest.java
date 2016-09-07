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
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import org.fede.calculator.money.Aggregation;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.NoSeriesDataFoundException;
import org.fede.calculator.money.SimpleAggregation;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.SortedMapMoneyAmountSeries;
import org.fede.calculator.money.series.YearMonth;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
public class SimpleAggregationTest {

    private static class TestMoneyAmountProcessor implements BiConsumer<YearMonth, MoneyAmount> {

        private final String[] expected;
        private int i = 0;
        private final String currencySymbol;

        public TestMoneyAmountProcessor(String[] expected, String currencySymbol) {
            this.expected = expected;
            this.currencySymbol = currencySymbol;
            this.i = 0;
        }

        @Override
        public void accept(YearMonth yearMonth, MoneyAmount amount) throws NoSeriesDataFoundException {
            assertEquals(new MoneyAmount(new BigDecimal(this.expected[i]), this.currencySymbol), amount);
            this.i++;
        }
    }

    private static final String[] ASCENDING = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21"};

    private final MoneyAmountSeries asc;
    private final Aggregation aggr = new SimpleAggregation(12);

    public SimpleAggregationTest() {
        YearMonth ym = new YearMonth(1936, 8);
        SortedMap<YearMonth, MoneyAmount> ascendingValues = new TreeMap<>();
        for (String s : ASCENDING) {
            ascendingValues.put(ym, new MoneyAmount(new BigDecimal(s), "ARS"));
            ym = ym.next();
        }
        this.asc = new SortedMapMoneyAmountSeries("ARS", ascendingValues);
    }

    @Test
    public void sum() {
        this.aggr.sum(this.asc)
                .forEach(new TestMoneyAmountProcessor(
                        new String[]{"1", "3", "6", "10", "15", "21", "28", "36", "45", "55", "66", "78", "90", "102", "114", "126", "138", "150", "162", "174", "186"},
                        "ARS"));
    }

    @Test
    public void avg() {
        this.aggr.average(this.asc).forEach(
                new TestMoneyAmountProcessor(
                        new String[]{"1", "1.5", "2", "2.5", "3", "3.5", "4", "4.5", "5", "5.5", "6", "6.5", "7.5", "8.5", "9.5", "10.5", "11.5", "12.5", "13.5", "14.5", "15.5"},
                        "ARS"));
    }

    @Test
    public void change() {
        this.aggr.change(this.asc).forEach(
                new TestMoneyAmountProcessor(
                        new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "11", "11", "11", "11", "11", "11", "11", "11", "11"},
                        "ARS"));
    }

}
