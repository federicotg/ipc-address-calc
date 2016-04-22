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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.fede.calculator.money.Aggregation;
import static org.fede.calculator.money.Inflation.USD_INFLATION;
import static org.fede.calculator.money.Inflation.ARS_INFLATION;
import org.fede.calculator.money.MathConstants;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import java.util.Collections;
import java.util.Comparator;
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
import org.fede.calculator.money.series.JSONIndexSeries;
import static org.fede.util.Util.readSeries;
import org.fede.calculator.money.series.MoneyAmountProcessor;
import org.fede.calculator.money.series.MoneyAmountSeries;
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

    private final Map<String, Inflation> map = new HashMap<>(2, 1.0f);

    @Resource(name = "incomesSeries")
    private List<ExpenseChartSeriesDTO> incomeSeries;

    @Resource(name = "investments")
    private List<String> investmentSeries;
    
    @Resource(name = "savingsReportSeries")
    private Map<String, String> savingsReportSeries;
    
            
    public InvestmentServiceImpl() {
        map.put("USD", USD_INFLATION);
        map.put("ARS", ARS_INFLATION);

    }

    @Override
    public List<SavingsReportDTO> savings(final int toYear, final int toMonth) throws NoSeriesDataFoundException {

        final List<SavingsReportDTO> report = new ArrayList<>();
        
//        final MoneyAmountSeries canaafa = readSeries("saving/ahorros-conaafa.json").exchangeInto("ARS");
//        final MoneyAmountSeries pesos = readSeries("saving/ahorros-peso.json").add(canaafa);
//        final MoneyAmountSeries dollarsAndGold = sumSeries("saving/ahorros-dolar.json", "saving/ahorros-oro.json");
//        final IndexSeries dollarPrice = JSONIndexSeries.readSeries("index/peso-dolar-libre.json");

        final MoneyAmountSeries pesos = readSeries(this.savingsReportSeries.get("pesos"))
                .add(readSeries(this.savingsReportSeries.get("conaafa")).exchangeInto("ARS"))
                .add(readSeries(this.savingsReportSeries.get("conbala")).exchangeInto("ARS"));
        
        final MoneyAmountSeries dollarsAndGold = readSeries(this.savingsReportSeries.get("dollars"))
                .add(readSeries(this.savingsReportSeries.get("gold")));
        
        final IndexSeries dollarPrice = JSONIndexSeries.readSeries(this.savingsReportSeries.get("dollarPrice"));

        final MoneyAmountSeries nominalIncomePesos = Util.sumSeries(this.incomeSeries);

        Aggregation yearSum = new SimpleAggregation(12);
        final MoneyAmountSeries nov99IncomePesos12 = yearSum.sum(ARS_INFLATION.adjust(nominalIncomePesos, toYear, toMonth));

        MoneyAmountSeries nominalIncomeDollars = nominalIncomePesos.exchangeInto("USD");
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

                MoneyAmount totNominalUSD = usd.add(ForeignExchanges.USD_ARS.exchange(ars, "USD", year, month));

                MoneyAmount totNominalARS = ars.add(ForeignExchanges.USD_ARS.exchange(usd, "ARS", year, month));

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

    @Override
    public List<InvestmentReportDTO> investment(String currency) throws NoSeriesDataFoundException {

        final List<InvestmentReportDTO> report = new ArrayList<>();
        final List<Investment> investments = new ArrayList<>();
        
        for(String fileName : this.investmentSeries){
            investments.addAll(Util.read(fileName, new TypeReference<List<Investment>>() {
            }));
        }
        
        for (Investment item : investments) {

            final Date until = this.untilDate(item, currency);

            report.add(new InvestmentReportDTO(
                    item.getType().name(),
                    item.getInitialDate(),
                    until,
                    currency,
                    this.initialAmount(item, currency),
                    this.finalAmount(item, currency, until),
                    this.inflation(currency, item.getInitialDate(), until),
                    item.getOut() == null));
        }
        Collections.sort(report, new Comparator<InvestmentReportDTO>() {
            @Override
            public int compare(InvestmentReportDTO o1, InvestmentReportDTO o2) {
                return o1.getFrom().compareTo(o2.getFrom());
            }

        });
        return report;
    }

    private MoneyAmount changeCurrency(MoneyAmount ma, String targetCurrency, Date date) throws NoSeriesDataFoundException {
        return ForeignExchanges.getForeignExchange(ma.getCurrency(), targetCurrency).exchange(ma, targetCurrency, date);
    }

    private BigDecimal initialAmount(Investment investment, String targetCurrency) throws NoSeriesDataFoundException {

        MoneyAmount amount;
        if (targetCurrency.equals(investment.getInitialCurrency())) {
            amount = investment.getInitialMoneyAmount();
        } else if (targetCurrency.equals(investment.getCurrency())) {
            amount = investment.getMoneyAmount();
        } else {
            amount = this.changeCurrency(investment.getMoneyAmount(), targetCurrency, investment.getInitialDate());
        }
        return amount.getAmount();
    }

    private Date untilDate(Investment item, String targetCurrency) {
        return max(
                item.getInitialDate(),
                item.getOut() != null
                        ? item.getOut().getDate()
                        : this.map.get(targetCurrency).getTo().asToDate());
    }

    private Date max(Date d1, Date d2) {
        return d1.compareTo(d2) > 0 ? d1 : d2;
    }

    private BigDecimal finalAmount(Investment investment, String targetCurrency, Date date) throws NoSeriesDataFoundException {
        if (investment.getOut() != null) {
            return this.changeCurrency(investment.getOut().getMoneyAmount(), targetCurrency, investment.getOut().getDate()).getAmount();
        }
        return this.changeCurrency(investment.getMoneyAmount(), targetCurrency, date).getAmount();
    }

    private BigDecimal inflation(String targetCurrency, Date from, Date to) throws NoSeriesDataFoundException {

        Inflation inflation = map.get(targetCurrency);
        if (inflation == null) {
            throw new IllegalArgumentException("Inflation for currency " + targetCurrency + " is unknown.");
        }
        return inflation.adjust(new MoneyAmount(ONE, targetCurrency), from, to).getAmount().subtract(ONE);
    }

}
