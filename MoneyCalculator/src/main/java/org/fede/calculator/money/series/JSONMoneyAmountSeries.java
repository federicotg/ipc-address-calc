/*
 * Copyright (C) 2014 fede
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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import org.fede.calculator.money.ForeignExchange;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.NoSeriesDataFoundException;

/**
 *
 * @author fede
 */
public class JSONMoneyAmountSeries extends SeriesSupport implements MoneyAmountSeries {

    
    private static final Map<Currency, ForeignExchange> FOREIGN_EXCHANGES_BY_CURRENCY = new HashMap<>();
    
    static{
        FOREIGN_EXCHANGES_BY_CURRENCY.put(Currency.getInstance("USD"), ForeignExchange.NO_FX);
        FOREIGN_EXCHANGES_BY_CURRENCY.put(Currency.getInstance("ARS"), ForeignExchange.USD_ARS);
        FOREIGN_EXCHANGES_BY_CURRENCY.put(Currency.getInstance("EUR"), ForeignExchange.USD_EUR);
        FOREIGN_EXCHANGES_BY_CURRENCY.put(Currency.getInstance("XAU"), ForeignExchange.USD_XAU);
    }
    
    private static ForeignExchange getForeignExchange(Currency from, Currency to){
        if(from.equals(to)){
            return ForeignExchange.NO_FX;
        }
        if(to.equals(Currency.getInstance("USD"))){
            ForeignExchange answer = FOREIGN_EXCHANGES_BY_CURRENCY.get(from);
            if(answer != null){
                return answer;
            }
        }
        throw new IllegalArgumentException("No foreign exchange from "+from.getSymbol()+" to "+to.getSymbol());
    }
    
    
    public static MoneyAmountSeries sumSeries(String... names) throws NoSeriesDataFoundException {
        if(names.length == 0){
            throw new IllegalArgumentException("You must at least read one series");
        }
        MoneyAmountSeries answer = null;
        for(String seriesName : names){
            if(seriesName!=null&& seriesName.length() > 0){
                MoneyAmountSeries s = readSeries(seriesName);
                answer = answer == null ? s : answer.add(s);
            }
        }
        return answer;
    }

    public static MoneyAmountSeries readSeries(String name) throws NoSeriesDataFoundException {
        try (InputStream is = JSONIndexSeries.class.getResourceAsStream("/" + name)) {
            JSONSeries series = new ObjectMapper().readValue(is, JSONSeries.class);

            final InterpolationStrategy strategy = InterpolationStrategy.valueOf(series.getInterpolation());
            SortedMap<YearMonth, MoneyAmount> interpolatedData = new TreeMap<>();
            final Currency currency = Currency.getInstance(series.getCurrency());
            for (JSONDataPoint dp : series.getData()) {
                interpolatedData.put(new YearMonth(dp.getYear(), dp.getMonth()), new MoneyAmount(dp.getValue(), currency));
            }
            if (series.getData().size() != interpolatedData.size()) {
                throw new IllegalArgumentException("Series " + name + " has incorrect year and month sequence.");
            }
            Map<YearMonth, MoneyAmount> extraData = new HashMap<>();
            YearMonth previousKey = interpolatedData.firstKey();
            MoneyAmount previousValue = interpolatedData.get(interpolatedData.firstKey());
            for (Map.Entry<YearMonth, MoneyAmount> entry : interpolatedData.entrySet()) {
                while (previousKey.monthsUntil(entry.getKey()) > 1) {
                    previousKey = previousKey.next();
                    extraData.put(previousKey, strategy.interpolate(previousValue, currency));
                }
                previousValue = entry.getValue();
                previousKey = entry.getKey();
            }
            interpolatedData.putAll(extraData);

            return new JSONMoneyAmountSeries(currency, interpolatedData);
        } catch (IOException ioEx) {
            throw new IllegalArgumentException("Could not read series named " + name, ioEx);
        }
    }

    /*public static List<MoneyAmountSeries> convertoToDollar(List<MoneyAmountSeries> series) throws NoSeriesDataFoundException {
        final List<MoneyAmountSeries> answer = new ArrayList<>(series.size());

        for (MoneyAmountSeries s : series) {
            if (s.getCurrency().equals(Currency.getInstance("USD"))) {
                answer.add(s);
            } else if (s.getCurrency().equals(Currency.getInstance("ARA"))) {
                answer.add(ForeignExchange.USD_ARS.exchange(s, Currency.getInstance("USD")));
            } else if (s.getCurrency().equals(Currency.getInstance("XAU"))) {
                answer.add(ForeignExchange.USD_XAU.exchange(s, Currency.getInstance("USD")));
            } else if (s.getCurrency().equals(Currency.getInstance("EUR"))) {
                answer.add(ForeignExchange.USD_EUR.exchange(s, Currency.getInstance("USD")));
            } else {
                throw new IllegalArgumentException("Can't convert from " + s.getCurrency().toString() + " to USD.");
            }
        }

        return answer;
    }*/

    private final Currency currency;
    private final SortedMap<YearMonth, MoneyAmount> values;

    private ThreadLocal<Calendar> calendar = new ThreadLocal<Calendar>() {

        @Override
        protected Calendar initialValue() {
            return Calendar.getInstance();
        }

    };

    public JSONMoneyAmountSeries(Currency currency) {
        this.currency = currency;
        this.values = new TreeMap<>();
    }

    private JSONMoneyAmountSeries(Currency currency, SortedMap<YearMonth, MoneyAmount> interpolatedData) {
        this.currency = currency;
        this.values = interpolatedData;
    }

    @Override
    public MoneyAmount getAmount(Date day) throws NoSeriesDataFoundException {

        Calendar cal = this.calendar.get();
        cal.setTime(day);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        return this.getAmount(year, month + 1);
    }

    @Override
    public MoneyAmount getAmount(int year, int month) throws NoSeriesDataFoundException {
        return this.values.get(new YearMonth(year, month));
    }

    @Override
    public YearMonth getFrom() {
        return this.values.firstKey();
    }

    @Override
    public YearMonth getTo() {
        return this.values.lastKey();
    }

    @Override
    public void putAmount(int year, int month, MoneyAmount amount) {
        this.values.put(new YearMonth(year, month), amount);
    }

    @Override
    public Currency getCurrency() {
        return currency;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MoneyAmountSeries)) {
            return false;
        }
        MoneyAmountSeries other = (MoneyAmountSeries) obj;
        boolean equal = this.getCurrency().equals(other.getCurrency())
                && Objects.equals(this.getFrom(), other.getFrom())
                && Objects.equals(this.getTo(), other.getTo());
        if (!equal) {
            /*System.err.println("Not eq 1");

             System.err.println(this.getCurrency());
             System.err.println(other.getCurrency());
             System.err.println(this.getFrom());
             System.err.println(other.getFrom());
             System.err.println(this.getTo());
             System.err.println(other.getTo());*/

            return false;
        }
        equal = true;
        try {
            for (Iterator<Map.Entry<YearMonth, MoneyAmount>> it = this.values.entrySet().iterator(); it.hasNext();) {
                Map.Entry<YearMonth, MoneyAmount> e = it.next();
                equal &= e.getValue().equals(other.getAmount(e.getKey().getYear(), e.getKey().getMonth()));
            }
            /*if (!equal) {
             System.err.println("Not eq 2");
             }*/
            return equal;
        } catch (NoSeriesDataFoundException ex) {

            System.err.println("Not eq 2");

            return false;
        }

    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(this.currency);
        hash = 37 * hash + Objects.hashCode(this.values);
        return hash;
    }

    @Override
    public void forEach(MoneyAmountProcessor processor) throws NoSeriesDataFoundException {
        for (Iterator<Map.Entry<YearMonth, MoneyAmount>> it = this.values.entrySet().iterator(); it.hasNext();) {
            Map.Entry<YearMonth, MoneyAmount> entry = it.next();
            processor.process(entry.getKey().getYear(), entry.getKey().getMonth(), entry.getValue());
        }
    }

    @Override
    public MoneyAmountSeries map(MoneyAmountTransform transform) {
        MoneyAmountSeries answer = new JSONMoneyAmountSeries(this.currency);
        for (Iterator<Map.Entry<YearMonth, MoneyAmount>> it = this.values.entrySet().iterator(); it.hasNext();) {
            Map.Entry<YearMonth, MoneyAmount> entry = it.next();
            answer.putAmount(
                    entry.getKey().getYear(),
                    entry.getKey().getMonth(),
                    transform.transform(entry.getKey().getYear(), entry.getKey().getMonth(), entry.getValue()));
        }
        return answer;
    }

    @Override
    public MoneyAmountSeries add(final MoneyAmountSeries other) throws NoSeriesDataFoundException {

        if (!other.getCurrency().equals(this.getCurrency())) {
            Currency usd = Currency.getInstance("USD");
            return getForeignExchange(this.getCurrency(), usd).exchange(this, usd)
                    .add(getForeignExchange(other.getCurrency(), usd).exchange(other, usd));
        }

        if (this.getFrom().compareTo(other.getFrom()) > 0) {
            return other.add(this);
        }

        final YearMonth otherStart = other.getFrom();
        final YearMonth otherEnd = other.getTo();

        final MoneyAmountSeries answer = new JSONMoneyAmountSeries(this.currency);
        //this empieza antes o son iguales
        this.forEach(new MoneyAmountProcessor() {

            @Override
            public void process(int thisYear, int thisMonth, MoneyAmount amount) throws NoSeriesDataFoundException {
                YearMonth now = new YearMonth(thisYear, thisMonth);
                if (now.compareTo(otherStart) < 0 || now.compareTo(otherEnd) > 0) {
                    answer.putAmount(thisYear, thisMonth, amount);
                } else {
                    answer.putAmount(thisYear, thisMonth, amount.add(other.getAmount(thisYear, thisMonth)));
                }
            }
        });

        // si el otro termina despues tengo que copiar sus valores al resultado.
        final YearMonth thisEnd = this.getTo();

        other.forEach(new MoneyAmountProcessor() {

            @Override
            public void process(int year, int month, MoneyAmount amount) throws NoSeriesDataFoundException {
                final YearMonth otherNow = new YearMonth(year, month);
                if (otherNow.compareTo(thisEnd) > 0) {
                    answer.putAmount(year, month, amount);
                }
            }
        });

        return answer;
    }

    @Override
    public void forEachNonZero(final MoneyAmountProcessor processor) throws NoSeriesDataFoundException {
        this.forEach(new MoneyAmountProcessor() {
            @Override
            public void process(int year, int month, MoneyAmount amount) throws NoSeriesDataFoundException {

                if (!amount.isZero()) {
                    processor.process(year, month, amount);
                }
            }
        });
    }

}
