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
package org.fede.calculator.money.series;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
public class CertificateDepositInvestment extends Investment {

    @Override
    public MoneyAmountSeries getMoneyAmountSeries() {
        YearMonth start = new YearMonth(this.getInvestmentDate());
        YearMonth end = new YearMonth(this.getOut().getDate());
        SortedMapMoneyAmountSeries series = new SortedMapMoneyAmountSeries(this.getIn().getCurrency());
        while (start.compareTo(end) < 0) {
            series.putAmount(start.getYear(), start.getMonth(), this.getInvestedAmount());
            start = start.next();
        }
        if (start.compareTo(end) == 0) {
            series.putAmount(start.getYear(), start.getMonth(), this.getOut().getMoneyAmount());
        }
        return series;
    }

}
