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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import org.fede.calculator.money.Aggregation;
import static org.fede.calculator.money.Inflation.USD_INFLATION;
import static org.fede.calculator.money.Inflation.ARS_INFLATION;
import org.fede.calculator.money.MathConstants;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import org.fede.calculator.money.ForeignExchanges;
import org.fede.calculator.money.Inflation;
import static org.fede.calculator.money.MathConstants.CONTEXT;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.NoSeriesDataFoundException;
import org.fede.calculator.money.SimpleAggregation;
import org.fede.calculator.money.series.IndexSeries;
import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.InvestmentType;
import org.fede.calculator.money.series.JSONIndexSeries;
import static org.fede.util.Util.sumSeries;
import static org.fede.util.Util.readSeries;
import org.fede.calculator.money.series.MoneyAmountProcessor;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.web.dto.DollarReportDTO;
import org.fede.calculator.web.dto.ExpenseChartSeriesDTO;
import org.fede.calculator.web.dto.InvestmentReportDTO;
import org.fede.calculator.web.dto.SavingsReportDTO;
import org.fede.util.Util;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 *
 * @author fede
 */
@Service
@Lazy
public class InvestmentServiceImpl implements InvestmentService, MathConstants {

    @Resource(name = "incomesSeries")
    private List<ExpenseChartSeriesDTO> incomeSeries;

    @Override
    public List<DollarReportDTO> dollar() throws NoSeriesDataFoundException {

        final List<Investment> investments = read("investments.json");
        final MoneyAmount oneDollar = new MoneyAmount(ONE, "USD");
        final List<DollarReportDTO> answer = new ArrayList<>(investments.size());
        final Date moment = ForeignExchanges.USD_ARS.getTo().asDate();
        final Currency ars = Currency.getInstance("ARS");

        for (Investment inv : investments) {
            if (inv.getType().equals(InvestmentType.USD)) {
                DollarReportDTO dto = new DollarReportDTO();

                answer.add(dto);

                dto.setUsd(inv.getIn().getAmount());

                Date thenDate = inv.getIn().getDate();
                dto.setThen(thenDate);

                dto.setNominalPesosThen(inv.getIn().getAmount());
                dto.setRealPesosNow(ARS_INFLATION.adjust(inv.getIn().getMoneyAmount(), thenDate, moment).getAmount());
                dto.setNow(moment);
                dto.setNominalPesosNow(ForeignExchanges.USD_ARS.exchange(inv.getInvestment().getMoneyAmount(), ars, moment).getAmount());

                MoneyAmount oneDollarThen = USD_INFLATION.adjust(oneDollar, moment, thenDate);

                MoneyAmount realDollarThen = ARS_INFLATION.adjust(
                        ForeignExchanges.USD_ARS.exchange(oneDollarThen, ars, thenDate),
                        thenDate, moment);

                dto.setRealUsdThen(realDollarThen.getAmount());

                MoneyAmount oneDollarNow = ForeignExchanges.USD_ARS.exchange(oneDollar, ars, moment);
                dto.setNominalUsdNow(oneDollarNow.getAmount());

            }
        }

        return answer;

    }

    @Override
    public List<SavingsReportDTO> savings(final int toYear, final int toMonth) throws NoSeriesDataFoundException {

        final List<SavingsReportDTO> report = new ArrayList<>();

        final MoneyAmountSeries pesos = readSeries("ahorros-peso.json");

        final MoneyAmountSeries dollarsAndGold = sumSeries("ahorros-dolar.json", "ahorros-oro.json");

        final IndexSeries dollarPrice = JSONIndexSeries.readSeries("peso-dolar-libre.json");

        MoneyAmountSeries nominalIncomePesos = Util.sumSeries(this.incomeSeries);

        Aggregation yearSum = new SimpleAggregation(12);
        final MoneyAmountSeries nov99IncomePesos12 = yearSum.sum(ARS_INFLATION.adjust(nominalIncomePesos, toYear, toMonth));

        MoneyAmountSeries nominalIncomeDollars = nominalIncomePesos.exchangeInto(Currency.getInstance("USD"));
        final MoneyAmountSeries nov99IncomeDollars12 = yearSum.sum(USD_INFLATION.adjust(nominalIncomeDollars, toYear, toMonth));

        final MoneyAmountSeries nominalIncomePesos12 = yearSum.sum(nominalIncomePesos);
        final MoneyAmountSeries nominalIncomeDollars12 = yearSum.sum(nominalIncomeDollars);

        pesos.forEach(new MoneyAmountProcessor() {
            @Override
            public void process(int year, int month, MoneyAmount amount) throws NoSeriesDataFoundException {

                SavingsReportDTO dto = new SavingsReportDTO(year, month);
                report.add(dto);

                MoneyAmount usd = dollarsAndGold.getAmount(year, month);
                MoneyAmount ars = pesos.getAmount(year, month);

                dto.setNominalDollars(usd.getAmount());
                dto.setNominalPesos(ars.getAmount());

                MoneyAmount nov99Usd = USD_INFLATION.adjust(usd, year, month, toYear, toMonth);
                MoneyAmount nov99Ars = ARS_INFLATION.adjust(ars, year, month, toYear, toMonth);

                dto.setNov99Dollars(nov99Usd.getAmount());
                dto.setNov99Pesos(nov99Ars.getAmount());

                MoneyAmount totNominalUSD = usd.add(ForeignExchanges.USD_ARS.exchange(ars, Currency.getInstance("USD"), year, month));

                MoneyAmount totNominalARS = ars.add(ForeignExchanges.USD_ARS.exchange(usd, Currency.getInstance("ARS"), year, month));

                dto.setTotalNominalDollars(totNominalUSD.getAmount());
                dto.setTotalNominalPesos(totNominalARS.getAmount());

                dto.setTotalNov99Dollars(USD_INFLATION.adjust(totNominalUSD, year, month, toYear, toMonth).getAmount());
                dto.setTotalNov99Pesos(ARS_INFLATION.adjust(totNominalARS, year, month, toYear, toMonth).getAmount());

                dto.setPesosForDollar(dollarPrice.getIndex(year, month));

                dto.setNominalIncomePesos(nominalIncomePesos12.getAmount(year, month).getAmount());
                dto.setNominalIncomeDollars(nominalIncomeDollars12.getAmount(year, month).getAmount());
                dto.setNov99IncomePesos(nov99IncomePesos12.getAmount(year, month).getAmount());
                dto.setNov99IncomeDollars(nov99IncomeDollars12.getAmount(year, month).getAmount());

                if (report.size() > 12) {
                    SavingsReportDTO otherDto = report.get(report.size() - 13);

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
        if (then.compareTo(ZERO) == 0) {
            return ONE;
        }
        return now.subtract(then).divide(then, CONTEXT);
    }

    private static BigDecimal savingsPct(BigDecimal savingsNow, BigDecimal savingsThen, BigDecimal avgIncome) {
        if (savingsNow.compareTo(savingsThen) == 0) {
            return ZERO;
        }
        return savingsNow.subtract(savingsThen).divide(avgIncome, CONTEXT);
    }

    private static List<Investment> read(String name) {
        try (InputStream in = InvestmentServiceImpl.class.getResourceAsStream("/" + name);) {
            ObjectMapper om = new ObjectMapper();

            return om.readValue(in, new TypeReference<List<Investment>>() {
            });
        } catch (IOException ioEx) {
            throw new IllegalArgumentException("Could not read investments from resource " + name, ioEx);
        }
    }

    @Override
    public List<InvestmentReportDTO> investment(String currency, String investmentSeries) throws NoSeriesDataFoundException {

        final Currency targetCurrency = Currency.getInstance(currency);
        final List<InvestmentReportDTO> report = new ArrayList<>();

        for (Investment item : read(investmentSeries)) {

            final Date until = this.untilDate(item, targetCurrency);

            report.add(new InvestmentReportDTO(
                    item.getInitialDate(),
                    until,
                    currency,
                    this.initialAmount(item, targetCurrency),
                    this.finalAmount(item, targetCurrency, until),
                    this.inflation(targetCurrency, item.getInitialDate(), until),
                    item.getOut() == null));
        }
        return report;
    }

    private MoneyAmount changeCurrency(MoneyAmount ma, Currency targetCurrency, Date date) throws NoSeriesDataFoundException {
        return ForeignExchanges.getForeignExchange(ma.getCurrency(), targetCurrency).exchange(ma, targetCurrency, date);
    }

    private BigDecimal initialAmount(Investment investment, Currency targetCurrency) throws NoSeriesDataFoundException {

        MoneyAmount amount;
        if (targetCurrency.equals(investment.getInitialCurrency())) {
            amount = investment.getIn().getMoneyAmount();
        } else if (targetCurrency.equals(investment.getCurrency())) {
            amount = investment.getMoneyAmount();
        } else {
            amount = this.changeCurrency(investment.getMoneyAmount(), targetCurrency, investment.getInitialDate());
        }
        return amount.getAmount();
    }

    private Date untilDate(Investment item, Currency targetCurrency) {
        return item.getOut() != null
                ? item.getOut().getDate()
                : ForeignExchanges.getForeignExchange(item.getMoneyAmount().getCurrency(), targetCurrency).getTo().asToDate();
    }

    private BigDecimal finalAmount(Investment investment, Currency targetCurrency, Date date) throws NoSeriesDataFoundException {
        if (investment.getOut() != null) {
            return changeCurrency(investment.getOut().getMoneyAmount(), targetCurrency, investment.getOut().getDate()).getAmount();
        }
        return changeCurrency(investment.getMoneyAmount(), targetCurrency, date).getAmount();
    }

    private BigDecimal inflation(Currency targetCurrency, Date from, Date to) throws NoSeriesDataFoundException {
        Map<Currency, Inflation> map = new HashMap<>(2, 1.0f);

        map.put(Currency.getInstance("USD"), USD_INFLATION);
        map.put(Currency.getInstance("ARS"), ARS_INFLATION);

        Inflation inflation = map.get(targetCurrency);
        if (inflation == null) {
            throw new IllegalArgumentException("Inflation for currency " + targetCurrency + " is unknown.");
        }
        return inflation.adjust(new MoneyAmount(ONE, targetCurrency), from, to).getAmount().subtract(ONE);
    }

}
