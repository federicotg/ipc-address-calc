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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import org.fede.calculator.money.ForeignExchange;
import org.fede.calculator.money.ForeignExchanges;
import org.fede.calculator.money.Inflation;
import static org.fede.calculator.money.MathConstants.CONTEXT;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.NoSeriesDataFoundException;
import org.fede.calculator.money.SimpleAggregation;
import org.fede.calculator.money.series.IndexSeries;
import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.SeriesReader;
import org.fede.calculator.money.series.YearMonth;
import org.fede.calculator.web.dto.DetailedInvestmentReportDTO;
import org.fede.calculator.web.dto.ExpenseChartSeriesDTO;
import org.fede.calculator.web.dto.InvestmentDTO;
import org.fede.calculator.web.dto.InvestmentReportDTO;
import org.fede.calculator.web.dto.SavingsReportDTO;
import static org.fede.util.Util.sumSeries;

/**
 *
 * @author fede
 */
public class InvestmentServiceImpl implements InvestmentService, MathConstants {

    private static final TypeReference<List<Investment>> TYPE_REFERENCE = new TypeReference<List<Investment>>() {
    };
    private static final Map<String, Inflation> MAP = new ConcurrentHashMap<>(2, 1.0f);

    static {
        MAP.put("USD", USD_INFLATION);
        MAP.put("ARS", ARS_INFLATION);
    }

    private final List<ExpenseChartSeriesDTO> incomeSeries;

    private final List<String> investmentSeries;

    private final Map<String, List<String>> savingsReportSeries;

    public InvestmentServiceImpl(
            List<ExpenseChartSeriesDTO> incomeSeries,
            List<String> investmentSeries,
            Map<String, List<String>> savingsReportSeries) {
        this.incomeSeries = incomeSeries;
        this.investmentSeries = investmentSeries;
        this.savingsReportSeries = savingsReportSeries;
    }

    @Override
    public List<SavingsReportDTO> savings(final int toYear, final int toMonth) throws NoSeriesDataFoundException {

        final MoneyAmountSeries pesos = sumSeries("ARS", this.savingsReportSeries.get("ars")
                .toArray(new String[this.savingsReportSeries.get("ars").size()]));

        final MoneyAmountSeries dollarsAndGold = sumSeries("USD", this.savingsReportSeries.get("usd")
                .toArray(new String[this.savingsReportSeries.get("usd").size()]));
        final IndexSeries dollarPrice = SeriesReader.readIndexSeries(this.savingsReportSeries.get("fx").iterator().next());

        final MoneyAmountSeries nominalIncomePesos = sumSeries("ARS", this.incomeSeries);

        final Aggregation yearSum = new SimpleAggregation(12);
        final MoneyAmountSeries nov99IncomePesos12 = yearSum.sum(ARS_INFLATION.adjust(nominalIncomePesos, toYear, toMonth));

        final MoneyAmountSeries nominalIncomeDollars = nominalIncomePesos.exchangeInto("USD");
        final MoneyAmountSeries nov99IncomeDollars12 = yearSum.sum(USD_INFLATION.adjust(nominalIncomeDollars, toYear, toMonth));

        final MoneyAmountSeries nominalIncomePesos12 = yearSum.sum(nominalIncomePesos);
        final MoneyAmountSeries nominalIncomeDollars12 = yearSum.sum(nominalIncomeDollars);

        final List<SavingsReportDTO> report = new ArrayList<>();

        pesos.forEach((int year, int month, MoneyAmount amount) -> {
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
        });

        return report;
    }

    private static BigDecimal pctChange(BigDecimal now, BigDecimal then) {
        if (then.signum() == 0) {
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

    private boolean isCurrent(Investment inv) {
        return inv.getOut() == null || !new Date().after(inv.getOut().getDate());
    }

    private static MoneyAmount changeCurrency(MoneyAmount ma, String targetCurrency, Date date) throws NoSeriesDataFoundException {
        ForeignExchange fx = ForeignExchanges.getForeignExchange(ma.getCurrency(), targetCurrency);
        YearMonth min = new YearMonth(date).min(fx.getTo());
        return fx.exchange(ma, targetCurrency, min.getYear(), min.getMonth());
    }

    private static MoneyAmount initialAmount(Investment investment, String targetCurrency) throws NoSeriesDataFoundException {

        MoneyAmount amount;
        if (targetCurrency.equals(investment.getInitialCurrency())) {
            amount = investment.getInitialMoneyAmount();
        } else if (targetCurrency.equals(investment.getCurrency())) {
            amount = investment.getMoneyAmount();
        } else {
            amount = changeCurrency(investment.getMoneyAmount(), targetCurrency, investment.getInitialDate());
        }
        return amount;
    }

    private static Date untilDate(Investment item, String targetCurrency) {
        
        return Optional.ofNullable(item.getOut())
                .map(out -> out.getDate())
                .orElse(new Date());
        
        /*return max(
                item.getInitialDate(),
                item.getOut() != null
                        ? item.getOut().getDate()
                        : MAP.get(targetCurrency).getTo().asToDate());*/
    }

    /*private static Date max(Date d1, Date d2) {
        return d1.compareTo(d2) > 0 ? d1 : d2;
    }

    private static Date min(Date d1, Date d2) {
        return d1.compareTo(d2) > 0 ? d2 : d1;
    }*/
    
    private static MoneyAmount finalAmount(Investment investment, String targetCurrency, Date date) throws NoSeriesDataFoundException {
        if (investment.getOut() != null) {
            return changeCurrency(investment.getOut().getMoneyAmount(), targetCurrency, investment.getOut().getDate());
        }
        return changeCurrency(investment.getMoneyAmount(), targetCurrency, date);
    }

    private static BigDecimal inflation(String targetCurrency, Date from, Date to) throws NoSeriesDataFoundException {

        YearMonth ymFrom = new YearMonth(from);
        YearMonth ymTo = new YearMonth(to);
        
        if (ymFrom.equals(ymTo)) {
            return ZERO;
        }

        Inflation inflation = MAP.get(targetCurrency);
        
        if (inflation == null) {
            throw new IllegalArgumentException("Inflation for currency " + targetCurrency + " is unknown.");
        }
        YearMonth min = ymTo.min(inflation.getTo());
        return inflation.adjust(
                new MoneyAmount(ONE, targetCurrency), 
                ymFrom.getYear(),
                ymFrom.getMonth(), 
                min.getYear(),
                min.getMonth()).getAmount().subtract(ONE);
    }

    @Override
    public DetailedInvestmentReportDTO currentInvestmentsReport(String currency) throws NoSeriesDataFoundException {
        return this.investmentReport(currency, (item) -> isCurrent(item), true);

    }

    @Override
    public DetailedInvestmentReportDTO pastInvestmentsReport(String currency) throws NoSeriesDataFoundException {
        return this.investmentReport(currency, (item) -> !isCurrent(item), false);
    }

    private DetailedInvestmentReportDTO investmentReport(String currency, Predicate<Investment> filter, boolean includeTotal)
            throws NoSeriesDataFoundException {

        if (!MAP.containsKey(currency)) {
            throw new IllegalArgumentException("Currency " + currency + " does not have a known inflation index.");
        }

        final List<Investment> investments = new ArrayList<>(this.investmentSeries.size());
        for (String fileName : this.investmentSeries) {
            investments.addAll(SeriesReader.read(fileName, TYPE_REFERENCE));
        }

        final Inflation inflation = MAP.get(currency);
        final YearMonth until = inflation.getTo();
        final Date untilDate = until.asToDate();

        MoneyAmount initialAmount = new MoneyAmount(ZERO, currency);
        MoneyAmount currentAmount = new MoneyAmount(ZERO, currency);

        final List<InvestmentReportDTO> report = new ArrayList<>();

        for (Investment item : investments) {
            if (filter.test(item)) {

                if (includeTotal) {
                    YearMonth start = new YearMonth(item.getInitialDate());
                    initialAmount = initialAmount.add(
                            inflation.adjust(initialAmount(item, currency),
                                    start.getYear(),
                                    start.getMonth(),
                                    until.getYear(),
                                    until.getMonth()));
                    currentAmount = currentAmount.add(finalAmount(item, currency, untilDate));
                }

                final Date itemUntil = untilDate(item, currency);

                report.add(new InvestmentReportDTO(
                        item.getType().name(),
                        item.getInitialDate(),
                        itemUntil,
                        currency,
                        initialAmount(item, currency).getAmount(),
                        finalAmount(item, currency, itemUntil).getAmount(),
                        inflation(currency, item.getInitialDate(), itemUntil),
                        item.getInvestment().getCurrency()));
            }
        }

        Collections.sort(report, (InvestmentReportDTO o1, InvestmentReportDTO o2) -> o1.getFrom().compareTo(o2.getFrom()));

        return new DetailedInvestmentReportDTO(
                includeTotal
                        ? new InvestmentDTO(currency, initialAmount.getAmount(), currentAmount.getAmount(), untilDate)
                        : null,
                report);
    }

}
