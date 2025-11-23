package org.fede.calculator.money;

import java.math.BigDecimal;
import java.util.List;
import org.fede.calculator.money.series.MoneyAmountItem;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.SortedMapMoneyAmountSeries;

/**
 * Accumulator: running (prefix) aggregations from the beginning of a series.
 */
public class Accumulator implements Aggregator {

    @Override
    public MoneyAmountSeries average(MoneyAmountSeries series) {
        final var currency = series.getCurrency();
        MoneyAmountSeries result = new SortedMapMoneyAmountSeries(currency, series.getName() + " avg");

        List<MoneyAmountItem> items = series.items().toList();

        BigDecimal sum = BigDecimal.ZERO;
        long count = 0L;

        for (var item : items) {
            sum = sum.add(item.amount().amount());
            count++;
            BigDecimal avg = sum.divide(BigDecimal.valueOf(count), MathConstants.C);
            result.putAmount(item.ym(), new MoneyAmount(avg, currency));
        }

        // extend last value up to inflation end (same behaviour as your original)
        var last = result.getAmount(result.getTo());
        var ym = result.getTo();
        while (ym.isBefore(Inflation.USD_INFLATION.getTo())) {
            ym = ym.plusMonths(1);
            result.putAmount(ym, last);
        }

        return result;
    }

    @Override
    public MoneyAmountSeries sum(MoneyAmountSeries series) {
        final var currency = series.getCurrency();
        MoneyAmountSeries result = new SortedMapMoneyAmountSeries(currency, series.getName() + " sum");

        List<MoneyAmountItem> items = series.items().toList();

        BigDecimal sum = BigDecimal.ZERO;

        for (var item : items) {
            sum = sum.add(item.amount().amount());
            result.putAmount(item.ym(), new MoneyAmount(sum, currency));
        }

        // extend last value up to inflation end
        var last = result.getAmount(result.getTo());
        var ym = result.getTo();
        while (ym.isBefore(Inflation.USD_INFLATION.getTo())) {
            ym = ym.plusMonths(1);
            result.putAmount(ym, last);
        }

        return result;
    }
}
