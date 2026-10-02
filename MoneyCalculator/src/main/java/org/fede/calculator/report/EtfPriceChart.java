/*
 * Copyright (C) 2026 fede
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
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static java.util.stream.Collectors.groupingBy;
import org.fede.calculator.chart.ChartStyle;
import org.fede.calculator.chart.Scale;
import org.fede.calculator.chart.TimeSeriesChart;
import org.fede.calculator.chart.ValueFormat;
import org.fede.calculator.money.Currency;
import static org.fede.calculator.money.Currency.USD;
import org.fede.calculator.money.ForeignExchanges;
import org.fede.calculator.money.MathConstants;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.series.Investment;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;

/**
 *
 * @author fede
 */
public class EtfPriceChart {

    private final Series series;

    public EtfPriceChart(Series series) {
        this.series = series;
    }

    public void create(Currency etf) {
        var fx = ForeignExchanges.getForeignExchange(etf, USD);
        var oneShare = new MoneyAmount(BigDecimal.ONE, etf);
        var prices = new TimeSeries(etf.name());

        for (var ym = fx.getFrom(); !ym.isAfter(fx.getTo()); ym = ym.plusMonths(1)) {
            if (fx.hasRate(ym)) {
                var usd = fx.exchange(oneShare, USD, ym);
                prices.add(day(ym.atEndOfMonth()), usd.amount());
            }
        }

        var labels = purchaseLabels(etf, prices);

        new TimeSeriesChart(new ChartStyle(ValueFormat.CURRENCY_DECIMALS, Scale.LINEAR))
                .createFromTimeSeries(
                        etf.name() + " USD",
                        List.of(prices),
                        USD,
                        labels,
                        "etf-" + etf.name().toLowerCase() + "-usd");
    }

    private Map<LocalDate, String> purchaseLabels(Currency etf, TimeSeries prices) {
        var buysByDate = this.series.getInvestments().stream()
                .filter(investment -> investment.getCurrency() == etf)
                .collect(groupingBy(Investment::getInitialDate));

        var labels = new HashMap<LocalDate, String>();
        for (var entry : buysByDate.entrySet()) {
            var date = entry.getKey();
            var buys = entry.getValue();
            var quantity = buys.stream()
                    .map(investment -> investment.getInvestment().getAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (quantity.signum() == 0) {
                continue;
            }
            labels.put(date, quantity.stripTrailingZeros().toPlainString());

            var period = day(date);
            if (prices.getDataItem(period) == null) {
                var usd = buys.stream()
                        .map(investment -> investment.getIn().getMoneyAmount(USD).amount())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                prices.add(period, usd.divide(quantity, MathConstants.C));
            }
        }
        return labels;
    }

    private static Day day(LocalDate date) {
        return new Day(date.getDayOfMonth(), date.getMonthValue(), date.getYear());
    }

}
