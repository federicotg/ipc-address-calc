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
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.fede.calculator.money.Aggregation;
import org.fede.calculator.money.ForeignExchange;
import org.fede.calculator.money.ForeignExchanges;
import org.fede.calculator.money.Inflation;
import static org.fede.calculator.money.Inflation.ARS_INFLATION;
import static org.fede.calculator.money.Inflation.USD_INFLATION;
import org.fede.calculator.money.MathConstants;
import static org.fede.calculator.money.MathConstants.CONTEXT;
import org.fede.calculator.money.MoneyAmount;
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
    public List<SavingsReportDTO> savings(final int toYear, final int toMonth) {

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

        pesos.forEach((yearMonth, amount) -> {
            int year = yearMonth.getYear();
            int month = yearMonth.getMonth();
            
            SavingsReportDTO dto = new SavingsReportDTO(year, month);
            report.add(dto);

            MoneyAmount usd = dollarsAndGold.getAmount(yearMonth);
            MoneyAmount ars = amount;

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

            dto.setNominalIncomePesos(nominalIncomePesos12.getAmount(yearMonth).getAmount());
            dto.setNominalIncomeDollars(nominalIncomeDollars12.getAmount(yearMonth).getAmount());
            dto.setNov99IncomePesos(nov99IncomePesos12.getAmount(yearMonth).getAmount());
            dto.setNov99IncomeDollars(nov99IncomeDollars12.getAmount(yearMonth).getAmount());

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

    private static MoneyAmount changeCurrency(MoneyAmount ma, String targetCurrency, Date date) {
        ForeignExchange fx = ForeignExchanges.getForeignExchange(ma.getCurrency(), targetCurrency);
        YearMonth min = new YearMonth(date).min(fx.getTo());
        return fx.exchange(ma, targetCurrency, min.getYear(), min.getMonth());
    }

    private static MoneyAmount initialAmount(Investment investment, String targetCurrency) {

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

    private static MoneyAmount finalAmount(Investment investment, String targetCurrency, Date date) {

        if (investment.getOut() != null) {
            return changeCurrency(investment.getOut().getMoneyAmount(), targetCurrency, date);
        }

        return changeCurrency(investment.getMoneyAmount(), targetCurrency, date);
    }

    private static BigDecimal realAmount(MoneyAmount nominalAmount, String targetCurrency, Date from, Date to) {

        YearMonth ymFrom = new YearMonth(from);
        YearMonth ymTo = new YearMonth(to);

        if (ymFrom.equals(ymTo)) {
            return nominalAmount.getAmount();
        }

        Inflation inflation = MAP.get(targetCurrency);

        if (inflation == null) {
            throw new IllegalArgumentException("Inflation for currency " + targetCurrency + " is unknown.");
        }

        return inflation.adjust(
                nominalAmount,
                ymFrom.getYear(),
                ymFrom.getMonth(),
                ymTo.getYear(),
                ymTo.getMonth()).getAmount();
    }

    private static BigDecimal inflation(String targetCurrency, Date from, Date to) {
        return realAmount(new MoneyAmount(ONE, targetCurrency), targetCurrency, from, to).subtract(ONE);
    }

    @Override
    public DetailedInvestmentReportDTO currentInvestmentsReport(String currency) {
        final Inflation inflation = MAP.get(currency);
        final YearMonth until = inflation.getTo();
        final Date untilDate = until.asToDate();
        return this.investmentReport(currency, (item) -> isCurrent(item) && item.getInitialDate().before(untilDate), true);
    }

    @Override
    public DetailedInvestmentReportDTO pastInvestmentsReport(String currency) {

        final Inflation inflation = MAP.get(currency);
        final YearMonth until = inflation.getTo();
        final Date untilDate = until.asToDate();

        return this.investmentReport(currency, (item) -> !isCurrent(item) && item.getOut().getDate().before(untilDate), false);
    }

    private DetailedInvestmentReportDTO investmentReport(String currency, Predicate<Investment> filter, boolean includeTotal) {

        if (!MAP.containsKey(currency)) {
            throw new IllegalArgumentException("Currency " + currency + " does not have a known inflation index.");
        }

        final List<Investment> investments = this.investmentSeries.stream()
                .flatMap(fileName -> SeriesReader.read(fileName, TYPE_REFERENCE).stream())
                .collect(Collectors.toList());

        final Inflation inflation = MAP.get(currency);
        final YearMonth until = inflation.getTo();
        final Date untilDate = until.asToDate();

        MoneyAmount initialAmount = new MoneyAmount(ZERO, currency);
        MoneyAmount currentAmount = new MoneyAmount(ZERO, currency);

        final List<InvestmentReportDTO> report = new ArrayList<>();

        for (Investment item : investments) {
            if (filter.test(item)) {

                final Date itemUntilDate = item.getOut() == null ? untilDate : item.getOut().getDate();
                final YearMonth itemUntilYearMonth = new YearMonth(itemUntilDate);

                if (includeTotal) {
                    YearMonth start = new YearMonth(item.getInitialDate());
                    initialAmount = initialAmount.add(
                            inflation.adjust(initialAmount(item, currency),
                                    start.getYear(),
                                    start.getMonth(),
                                    itemUntilYearMonth.getYear(),
                                    itemUntilYearMonth.getMonth()));
                    currentAmount = currentAmount.add(finalAmount(item, currency, itemUntilDate));
                }

                BigDecimal finalAmount = finalAmount(item, currency, itemUntilDate).getAmount();

                MoneyAmount investedAmount = initialAmount(item, currency);

                BigDecimal realAmount = realAmount(investedAmount, currency, item.getInitialDate(), itemUntilDate);

                report.add(new InvestmentReportDTO(
                        item.getType().name(),
                        item.getInitialDate(),
                        itemUntilDate,
                        currency,
                        initialAmount(item, currency).getAmount(),
                        finalAmount,
                        inflation(currency, item.getInitialDate(), itemUntilDate),
                        item.getInvestment().getCurrency(),
                        finalAmount.subtract(realAmount).setScale(2, ROUNDING_MODE)));
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
