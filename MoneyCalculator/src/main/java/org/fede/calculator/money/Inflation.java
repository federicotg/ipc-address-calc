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
package org.fede.calculator.money;

import java.time.LocalDate;
import java.util.Optional;
import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.InvestmentEvent;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.Series;
import org.fede.calculator.money.series.SeriesReader;
import java.time.YearMonth;

/**
 *
 * @author fede
 */
public interface Inflation extends Series {

    /**
     * Ajusta por inflación un monto.
     *
     * @param amount el monto a ajustar. Es el valor nominal.
     * @param from
     * @param to
     * @return el monto ajustado por inflación. Queda expresado en valores de
     * toYear y toMonth
     * @throws NoSeriesDataFoundException
     */
    MoneyAmount adjust(MoneyAmount amount, YearMonth from, YearMonth to);

    MoneyAmount adjust(MoneyAmount amount, LocalDate from, LocalDate to);

    /**
     * Ajusta por inflación todos los valores de la serie
     *
     * @param series la serie a ajustar.
     * @param ym
     * @return una serie de valores expresados en valores de referenceYear y
     * referenceMonth
     * @throws NoSeriesDataFoundException
     */
    MoneyAmountSeries adjust(MoneyAmountSeries series, YearMonth ym);

    /**
     * Calcula el valor ajustado por inflación del monto especificado en cada
     * mes que se pueda.
     *
     * @param amount el monto a ajustar
     * @param reference
     * @return el valor especificado expresado en valores ajustados para cada
     * mes de la serie.
     * @throws NoSeriesDataFoundException
     */
    MoneyAmountSeries adjust(MoneyAmount amount, YearMonth reference);

    Currency getCurrency();

    default Investment real(Investment investment) {
        return this.real(investment, this.getTo());
    }

    private Investment real(Investment in, YearMonth moment) {
        Investment answer = new Investment();
        answer.setIn(this.real(in.getIn(), moment));
        answer.setOut(this.real(in.getOut(), moment));
        answer.setType(in.getType());
        answer.setInvestment(in.getInvestment());
        answer.setInterest(in.getInterest());
        answer.setComment(in.getComment());
        return answer;
    }

    private InvestmentEvent real(InvestmentEvent event, YearMonth moment) {
        if (event == null) {
            return null;
        }

        InvestmentEvent answer = new InvestmentEvent();
        YearMonth start = YearMonth.from(event.getDate());
        MoneyAmount adjusted = this.adjust(event.getMoneyAmount(), start, moment);
        answer.setCurrency(adjusted.currency());
        answer.setAmount(adjusted.amount());
        answer.setDate(event.getDate());
        Optional.ofNullable(event.getTransferFee())
                .map(f -> new MoneyAmount(f, event.getCurrency()))
                .map(ma -> this.adjust(ma, start, moment))
                .map(MoneyAmount::amount)
                .ifPresent(answer::setTransferFee);
        answer.setFee(
                this.adjust(
                        new MoneyAmount(event.getFee(), event.getCurrency()),
                        start,
                        moment).amount());
        answer.setFx(event.getFx());
        return answer;
    }

    static Inflation usdInflation() {
        return UsdHolder.INSTANCE;
    }

    static Inflation anses() {
        return AnsesHolder.INSTANCE;
    }

    final class UsdHolder {

        static final Inflation INSTANCE
                = new CPIInflation(SeriesReader.readIndexSeries("index/bls.json"), Currency.USD);
    }

    final class AnsesHolder {

        static final Inflation INSTANCE
                = new CPIInflation(SeriesReader.readIndexSeries("index/anses.json"), Currency.ARS);
    }

}
