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
import org.fede.calculator.money.series.MoneyAmountProcessor;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.money.series.YearMonth;
import org.fede.calculator.web.dto.DetailedInvestmentReportDTO;
import org.fede.calculator.web.dto.ExpenseChartSeriesDTO;
import org.fede.calculator.web.dto.InvestmentDTO;
import org.fede.calculator.web.dto.InvestmentReportDTO;
import org.fede.calculator.web.dto.SavingsReportDTO;
import org.fede.util.Predicate;
import static org.fede.util.Util.read;
import static org.fede.util.Util.sumSeries;
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
    private Map<String, List<String>> savingsReportSeries;

    public InvestmentServiceImpl() {
        map.put("USD", USD_INFLATION);
        map.put("ARS", ARS_INFLATION);
    }

    @Override
    public List<SavingsReportDTO> savings(final int toYear, final int toMonth) throws NoSeriesDataFoundException {

        final MoneyAmountSeries pesos = sumSeries("ARS", this.savingsReportSeries.get("ars").toArray(new String[0]));
        final MoneyAmountSeries dollarsAndGold = sumSeries("USD", this.savingsReportSeries.get("usd").toArray(new String[0]));
        final IndexSeries dollarPrice = JSONIndexSeries.readSeries(this.savingsReportSeries.get("fx").iterator().next());

        final MoneyAmountSeries nominalIncomePesos = sumSeries("ARS", this.incomeSeries);

        final Aggregation yearSum = new SimpleAggregation(12);
        final MoneyAmountSeries nov99IncomePesos12 = yearSum.sum(ARS_INFLATION.adjust(nominalIncomePesos, toYear, toMonth));

        final MoneyAmountSeries nominalIncomeDollars = nominalIncomePesos.exchangeInto("USD");
        final MoneyAmountSeries nov99IncomeDollars12 = yearSum.sum(USD_INFLATION.adjust(nominalIncomeDollars, toYear, toMonth));

        final MoneyAmountSeries nominalIncomePesos12 = yearSum.sum(nominalIncomePesos);
        final MoneyAmountSeries nominalIncomeDollars12 = yearSum.sum(nominalIncomeDollars);

        final List<SavingsReportDTO> report = new ArrayList<>();

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

    private boolean isCurrent(Investment inv) {
        return inv.getOut() == null || !new Date().after(inv.getOut().getDate());
    }

    //@Override
//    public List<InvestmentReportDTO> investment(String currency, boolean current) throws NoSeriesDataFoundException {
//
//        final List<InvestmentReportDTO> report = new ArrayList<>();
//        final List<Investment> investments = new ArrayList<>();
//
//        for (String fileName : this.investmentSeries) {
//            investments.addAll(read(fileName, new TypeReference<List<Investment>>() {
//            }));
//        }
//
//        Date untilDate = this.map.get(currency).getTo().asToDate();
//
//        for (Investment item : investments) {
//            if ((!current || (this.isCurrent(item) && item.getInitialDate().before(untilDate)))) {
//
//                final Date until = this.untilDate(item, currency);
//
//                report.add(new InvestmentReportDTO(
//                        item.getType().name(),
//                        item.getInitialDate(),
//                        until,
//                        currency,
//                        this.initialAmount(item, currency).getAmount(),
//                        this.finalAmount(item, currency, until).getAmount(),
//                        this.inflation(currency, item.getInitialDate(), until),
//                        item.getOut() == null));
//            }
//        }
//        Collections.sort(report, new Comparator<InvestmentReportDTO>() {
//            @Override
//            public int compare(InvestmentReportDTO o1, InvestmentReportDTO o2) {
//                return o1.getFrom().compareTo(o2.getFrom());
//            }
//
//        });
//        return report;
//    }
    private MoneyAmount changeCurrency(MoneyAmount ma, String targetCurrency, Date date) throws NoSeriesDataFoundException {
        return ForeignExchanges.getForeignExchange(ma.getCurrency(), targetCurrency).exchange(ma, targetCurrency, date);
    }

    private MoneyAmount initialAmount(Investment investment, String targetCurrency) throws NoSeriesDataFoundException {

        MoneyAmount amount;
        if (targetCurrency.equals(investment.getInitialCurrency())) {
            amount = investment.getInitialMoneyAmount();
        } else if (targetCurrency.equals(investment.getCurrency())) {
            amount = investment.getMoneyAmount();
        } else {
            amount = this.changeCurrency(investment.getMoneyAmount(), targetCurrency, investment.getInitialDate());
        }
        return amount;
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

    private MoneyAmount finalAmount(Investment investment, String targetCurrency, Date date) throws NoSeriesDataFoundException {
        if (investment.getOut() != null) {
            return this.changeCurrency(investment.getOut().getMoneyAmount(), targetCurrency, investment.getOut().getDate());
        }
        return this.changeCurrency(investment.getMoneyAmount(), targetCurrency, date);
    }

    private BigDecimal inflation(String targetCurrency, Date from, Date to) throws NoSeriesDataFoundException {

        if (from.equals(to)) {
            return ZERO;
        }

        Inflation inflation = map.get(targetCurrency);
        if (inflation == null) {
            throw new IllegalArgumentException("Inflation for currency " + targetCurrency + " is unknown.");
        }
        return inflation.adjust(new MoneyAmount(ONE, targetCurrency), from, to).getAmount().subtract(ONE);
    }

    // @Override
//    public InvestmentDTO currentInvestment(String currency) throws NoSeriesDataFoundException {
//        final List<Investment> investments = new ArrayList<>();
//        for (String fileName : this.investmentSeries) {
//            investments.addAll(read(fileName, new TypeReference<List<Investment>>() {
//            }));
//        }
//
//        final Inflation inflation = this.map.get(currency);
//        final YearMonth until = inflation.getTo();
//        final Date untilDate = until.asToDate();
//
//        MoneyAmount initialAmount = new MoneyAmount(ZERO, currency);
//        MoneyAmount currentAmount = new MoneyAmount(ZERO, currency);
//
//        for (Investment item : investments) {
//            if (this.isCurrent(item) && item.getInitialDate().before(untilDate)) {
//                YearMonth start = new YearMonth(item.getInitialDate());
//                initialAmount = initialAmount.add(inflation.adjust(this.initialAmount(item, currency), start.getYear(), start.getMonth(), until.getYear(), until.getMonth()));
//                currentAmount = currentAmount.add(this.finalAmount(item, currency, untilDate));
//            }
//        }
//
//        return new InvestmentDTO(currency, initialAmount.getAmount(), currentAmount.getAmount(), untilDate);
//    }
    @Override
    public DetailedInvestmentReportDTO currentInvestmentsReport(String currency) throws NoSeriesDataFoundException {
        return this.investmentReport(currency,
                new Predicate<Investment>() {
            @Override
            public boolean test(Investment item) {
                return isCurrent(item);
            }
        }, true);

    }

    @Override
    public DetailedInvestmentReportDTO pastInvestmentsReport(String currency) throws NoSeriesDataFoundException {
        return this.investmentReport(currency,
                new Predicate<Investment>() {
            @Override
            public boolean test(Investment item) {
                return !isCurrent(item);
            }
        }, false);
    }

    private DetailedInvestmentReportDTO investmentReport(String currency, Predicate<Investment> filter, boolean includeTotal) throws NoSeriesDataFoundException {

        if (!this.map.containsKey(currency)) {
            throw new IllegalArgumentException("Currency " + currency + " does not have a known inflation index.");
        }

        final List<Investment> investments = new ArrayList<>();
        for (String fileName : this.investmentSeries) {
            investments.addAll(read(fileName, new TypeReference<List<Investment>>() {
            }));
        }

        final Inflation inflation = this.map.get(currency);
        final YearMonth until = inflation.getTo();
        final Date untilDate = until.asToDate();

        MoneyAmount initialAmount = new MoneyAmount(ZERO, currency);
        MoneyAmount currentAmount = new MoneyAmount(ZERO, currency);

        final List<InvestmentReportDTO> report = new ArrayList<>();

        for (Investment item : investments) {
            if (filter.test(item)) {

                if (includeTotal) {
                    YearMonth start = new YearMonth(item.getInitialDate());
                    initialAmount = initialAmount.add(inflation.adjust(this.initialAmount(item, currency), start.getYear(), start.getMonth(), until.getYear(), until.getMonth()));
                    currentAmount = currentAmount.add(this.finalAmount(item, currency, untilDate));
                }
                final Date itemUntil = this.untilDate(item, currency);

                report.add(new InvestmentReportDTO(
                        item.getType().name(),
                        item.getInitialDate(),
                        itemUntil,
                        currency,
                        this.initialAmount(item, currency).getAmount(),
                        this.finalAmount(item, currency, itemUntil).getAmount(),
                        this.inflation(currency, item.getInitialDate(), itemUntil),
                        item.getOut() == null));

            }
        }
        Collections.sort(report, new Comparator<InvestmentReportDTO>() {
            @Override
            public int compare(InvestmentReportDTO o1, InvestmentReportDTO o2) {
                return o1.getFrom().compareTo(o2.getFrom());
            }

        });
        return new DetailedInvestmentReportDTO(
                includeTotal ? new InvestmentDTO(currency, initialAmount.getAmount(), currentAmount.getAmount(), untilDate) : null,
                report);

    }

}
