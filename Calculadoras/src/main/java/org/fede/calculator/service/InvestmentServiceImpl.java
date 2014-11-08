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
package org.fede.calculator.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import org.fede.calculator.money.Aggregation;
import org.fede.calculator.money.ForeignExchange;
import static org.fede.calculator.money.ForeignExchange.USD_ARS;
import org.fede.calculator.money.Inflation;
import static org.fede.calculator.money.Inflation.USD_INFLATION;
import static org.fede.calculator.money.Inflation.ARS_INFLATION;
import org.fede.calculator.money.MathConstants;
import static org.fede.calculator.money.MathConstants.CONTEXT;
import static org.fede.calculator.money.MathConstants.ROUNDING_MODE;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.NoSeriesDataFoundException;
import org.fede.calculator.money.SimpleAggregation;
import org.fede.calculator.money.series.IndexSeries;
import org.fede.calculator.money.series.JSONIndexSeries;
import org.fede.calculator.money.series.JSONMoneyAmountSeries;
import org.fede.calculator.money.series.MoneyAmountProcessor;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.Series;
import org.fede.calculator.money.series.YearMonth;
import org.fede.calculator.web.dto.DollarReportDTO;
import org.fede.calculator.web.dto.SavingsReportDTO;
import org.springframework.stereotype.Service;

/**
 *
 * @author fede
 */
@Service
public class InvestmentServiceImpl implements InvestmentService, MathConstants {

    @Override
    public List<DollarReportDTO> dollar() throws NoSeriesDataFoundException {

        final Date moment = new Date();

        final MoneyAmount oneDollar = new MoneyAmount(BigDecimal.ONE, "USD");
        final Currency ars = Currency.getInstance("ARS");

        final MoneyAmountSeries dolares = JSONMoneyAmountSeries.readSeries("dolares.json");
        final Currency peso = Currency.getInstance("ARS");

        final List<DollarReportDTO> answer = new ArrayList<>();

        dolares.forEachNonZero(new MoneyAmountProcessor() {
            @Override
            public void process(int year, int month, MoneyAmount dollar) throws NoSeriesDataFoundException {
                DollarReportDTO dto = new DollarReportDTO();
                dto.setUsd(dollar.getAmount());
                answer.add(dto);
                Date thenDate = createDate(year, month);
                dto.setThen(thenDate);
                MoneyAmount pesos = ForeignExchange.USD_ARS.exchange(dollar, peso, thenDate);
                dto.setNominalPesosThen(pesos.getAmount());
                BigDecimal realPesos = Inflation.ARS_INFLATION.adjust(pesos, thenDate, moment).getAmount();
                dto.setRealPesosNow(realPesos);
                dto.setNow(moment);
                dto.setNominalPesosNow(ForeignExchange.USD_ARS.exchange(dollar, peso, moment).getAmount());

                MoneyAmount oneDollarThen = USD_INFLATION.adjust(oneDollar, moment, thenDate);

                MoneyAmount realDollarThen = Inflation.ARS_INFLATION.adjust(
                        ForeignExchange.USD_ARS.exchange(oneDollarThen, ars, thenDate),
                        thenDate, moment);

                dto.setRealUsdThen(realDollarThen.getAmount());

                MoneyAmount oneDollarNow = ForeignExchange.USD_ARS.exchange(oneDollar, ars, moment);
                dto.setNominalUsdNow(oneDollarNow.getAmount());

            }
        });
        return answer;
    }

    private Date createDate(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
    }


    @Override
    public List<SavingsReportDTO> savings() throws NoSeriesDataFoundException {

        final List<SavingsReportDTO> report = new ArrayList<>();

        final MoneyAmountSeries pesos = JSONMoneyAmountSeries.readSeries("ahorros-peso.json");

        final MoneyAmountSeries dollars = JSONMoneyAmountSeries.readSeries("ahorros-dolar.json");

        final IndexSeries dollarPrice = JSONIndexSeries.readSeries("peso-dolar-libre.json");

        MoneyAmountSeries nominalIncomePesos = JSONMoneyAmountSeries.readSeries("lifia.json")
                .add(JSONMoneyAmountSeries.readSeries("unlp.json"))
                .add(JSONMoneyAmountSeries.readSeries("plazofijo.json"));


        Aggregation yearSum = new SimpleAggregation(12);
        final MoneyAmountSeries nov99IncomePesos12 = yearSum.sum(Inflation.ARS_INFLATION.adjust(nominalIncomePesos, 1999, 11));


        MoneyAmountSeries nominalIncomeDollars = ForeignExchange.USD_ARS.exchange(nominalIncomePesos, Currency.getInstance("USD"));
        final MoneyAmountSeries nov99IncomeDollars12 = yearSum.sum(Inflation.USD_INFLATION.adjust(nominalIncomeDollars, 1999, 11));

        final MoneyAmountSeries nominalIncomePesos12 = yearSum.sum(nominalIncomePesos);
        final MoneyAmountSeries nominalIncomeDollars12 = yearSum.sum(nominalIncomeDollars);

        pesos.forEach(new MoneyAmountProcessor() {
            @Override
            public void process(int year, int month, MoneyAmount amount) throws NoSeriesDataFoundException {

                SavingsReportDTO dto = new SavingsReportDTO(year, month);
                report.add(dto);

                MoneyAmount usd = dollars.getAmount(year, month);
                MoneyAmount ars = pesos.getAmount(year, month);

                dto.setNominalDollars(usd.getAmount());
                dto.setNominalPesos(ars.getAmount());

                MoneyAmount nov99Usd = USD_INFLATION.adjust(usd, year, month, 1999, 11);
                MoneyAmount nov99Ars = ARS_INFLATION.adjust(ars, year, month, 1999, 11);

                dto.setNov99Dollars(nov99Usd.getAmount());
                dto.setNov99Pesos(nov99Ars.getAmount());

                MoneyAmount totNominalUSD = usd.add(USD_ARS.exchange(ars, Currency.getInstance("USD"), year, month));

                MoneyAmount totNominalARS = ars.add(USD_ARS.exchange(usd, Currency.getInstance("ARS"), year, month));

                dto.setTotalNominalDollars(totNominalUSD.getAmount());
                dto.setTotalNominalPesos(totNominalARS.getAmount());

                dto.setTotalNov99Dollars(USD_INFLATION.adjust(totNominalUSD, year, month, 1999, 11).getAmount());
                dto.setTotalNov99Pesos(ARS_INFLATION.adjust(totNominalARS, year, month, 1999, 11).getAmount());

                dto.setPesosForDollar(dollarPrice.getIndex(year, month));

                dto.setNominalIncomePesos(nominalIncomePesos12.getAmount(year, month).getAmount());
                dto.setNominalIncomeDollars(nominalIncomeDollars12.getAmount(year, month).getAmount());
                dto.setNov99IncomePesos(nov99IncomePesos12.getAmount(year, month).getAmount());
                dto.setNov99IncomeDollars(nov99IncomeDollars12.getAmount(year, month).getAmount());

                if (report.size() > 12) {
                    SavingsReportDTO otherDto = report.get(report.size() - 13);

                    dto.setNominalDollarsPctVar(pctChange(dto.getNominalDollars(), otherDto.getNominalDollars()));
                    dto.setNominalPesosPctVar(pctChange(dto.getNominalPesos(), otherDto.getNominalPesos()));

                    dto.setNov99DollarsPctVar(pctChange(dto.getNov99Dollars(), otherDto.getNov99Dollars()));
                    dto.setNov99PesosPctVar(pctChange(dto.getNov99Pesos(), otherDto.getNov99Pesos()));

                    dto.setPesosForDollarPctVar(pctChange(dto.getPesosForDollar(), otherDto.getPesosForDollar()));

                    dto.setTotalNominalDollarsPctVar(pctChange(dto.getTotalNominalDollars(), otherDto.getTotalNominalDollars()));
                    dto.setTotalNominalPesosPctVar(pctChange(dto.getTotalNominalPesos(), otherDto.getTotalNominalPesos()));

                    dto.setTotalNov99DollarsPctVar(pctChange(dto.getTotalNov99Dollars(), otherDto.getTotalNov99Dollars()));
                    dto.setTotalNov99PesosPctVar(pctChange(dto.getTotalNov99Pesos(), otherDto.getTotalNov99Pesos()));

                    dto.setNominalPesosPctSaved(savingsPct(dto.getTotalNominalPesos(), otherDto.getTotalNominalPesos(), dto.getNominalIncomePesos()));
                    dto.setNov99PesosPctSaved(savingsPct(dto.getTotalNov99Pesos(), otherDto.getTotalNov99Pesos(), dto.getNov99IncomePesos()));
                    dto.setNominalDollarPctSaved(savingsPct(dto.getTotalNominalDollars(), otherDto.getTotalNominalDollars(), dto.getNominalIncomeDollars()));
                    dto.setNov99DollarPctSaved(savingsPct(dto.getTotalNov99Dollars(), otherDto.getTotalNov99Dollars(), dto.getNov99IncomeDollars()));
                }
            }
        });

        return report;
    }

    private static BigDecimal pctChange(BigDecimal now, BigDecimal then) {
        if (then.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ONE;
        }
        return now.subtract(then).divide(then, CONTEXT);
    }

    private static BigDecimal savingsPct(BigDecimal savingsNow, BigDecimal savingsThen, BigDecimal avgIncome) {
        if (savingsNow.compareTo(savingsThen) == 0) {
            return BigDecimal.ZERO;
        }
        return savingsNow.subtract(savingsThen).divide(avgIncome, CONTEXT);
    }

}
