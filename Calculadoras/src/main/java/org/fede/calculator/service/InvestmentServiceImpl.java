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
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;
import org.fede.calculator.money.Aggregation;
import org.fede.calculator.money.ForeignExchanges;
import org.fede.calculator.money.Inflation;
import static org.fede.calculator.money.Inflation.USD_INFLATION;
import org.fede.calculator.money.MathConstants;
import static org.fede.calculator.money.MathConstants.C;
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
public class InvestmentServiceImpl implements InvestmentService {

    
    
    private static final TypeReference<List<Investment>> TYPE_REFERENCE = new TypeReference<List<Investment>>() {
    };
    private static final Map<String, Inflation> MAP = new ConcurrentHashMap<>(2, 1.0f);

    static {
        MAP.put("USD", USD_INFLATION);
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

            dto.setNov99Dollars(nov99Usd.getAmount());

            MoneyAmount totNominalUSD = usd.add(ForeignExchanges.USD_ARS.exchange(ars, "USD", year, month));

            MoneyAmount totNominalARS = ars.add(ForeignExchanges.USD_ARS.exchange(usd, "ARS", year, month));

            dto.setTotalNominalDollars(totNominalUSD.getAmount());
            dto.setTotalNominalPesos(totNominalARS.getAmount());

            dto.setTotalNov99Dollars(USD_INFLATION.adjust(totNominalUSD, year, month, toYear, toMonth).getAmount());

            dto.setPesosForDollar(dollarPrice.getIndex(year, month));

            dto.setNominalIncomePesos(nominalIncomePesos12.getAmount(yearMonth).getAmount());
            dto.setNominalIncomeDollars(nominalIncomeDollars12.getAmount(yearMonth).getAmount());
            dto.setNov99IncomeDollars(nov99IncomeDollars12.getAmount(yearMonth).getAmount());

            if (report.size() > 12) {
                SavingsReportDTO otherDto = report.get(report.size() - 13);

                dto.setPesosForDollarPctVar(pctChange(dto.getPesosForDollar(), otherDto.getPesosForDollar()));

                dto.setTotalNominalDollarsPctVar(pctChange(dto.getTotalNominalDollars(), otherDto.getTotalNominalDollars()));
                dto.setTotalNominalPesosPctVar(pctChange(dto.getTotalNominalPesos(), otherDto.getTotalNominalPesos()));

                dto.setTotalNov99DollarsPctVar(pctChange(dto.getTotalNov99Dollars(), otherDto.getTotalNov99Dollars()));


                dto.setNominalPesosPctSaved(savingsPct(dto.getTotalNominalPesos(), otherDto.getTotalNominalPesos(), dto.getNominalIncomePesos()));

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
        return now.subtract(then).divide(then, C);
    }

    private static BigDecimal savingsPct(BigDecimal savingsNow, BigDecimal savingsThen, BigDecimal avgIncome) {
        if (savingsNow.compareTo(savingsThen) == 0) {
            return ZERO;
        }
        return savingsNow.subtract(savingsThen).divide(avgIncome, C);
    }

    private static BigDecimal realAmount(MoneyAmount nominalAmount, String targetCurrency, Date from, Date to) {

        YearMonth ymFrom = adjustDate(from);
        YearMonth ymTo = adjustDate(to);

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
        return null; 
    }

    @Override
    public DetailedInvestmentReportDTO pastInvestmentsReport(String currency) {

        return null; //this.investmentReport(currency, (item) -> !item.isCurrent() && item.getOut().getDate().before(new Date()), false);
    }

    private static YearMonth adjustDate(Date exactDate) {
        var exactLocalDate = exactDate.toInstant().atZone(ZoneOffset.UTC).toLocalDate();

        //if (exactLocalDate.getDayOfMonth() < 16) {
        //    LocalDate adjusted = exactLocalDate.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        //    return YearMonth.of(adjusted.getYear(), adjusted.getMonthValue());
        //}
        return YearMonth.of(exactLocalDate.getYear(), exactLocalDate.getMonthValue());
    }

//    private DetailedInvestmentReportDTO investmentReport(String currency, Predicate<Investment> filter, boolean includeTotal) {
//
//        if (!MAP.containsKey(currency)) {
//            throw new IllegalArgumentException("Currency " + currency + " does not have a known inflation index.");
//        }
//
//        final var inflation = MAP.get(currency);
//        final var untilDate = inflation.getTo().asToDate();
//
//        var initialAmount = new MoneyAmount(ZERO, currency);
//        var currentAmount = new MoneyAmount(ZERO, currency);
//
//        final List<InvestmentReportDTO> report = new ArrayList<>();
//
//        for (Investment item : this.investmentSeries.stream()
//                .flatMap(fileName -> SeriesReader.read(fileName, TYPE_REFERENCE).stream())
//                .filter(filter)
//                .collect(Collectors.toList())) {
//
//            final var itemUntilDate = item.getOut() == null ? untilDate : item.getOut().getDate();
//            final var itemUntilYearMonth = adjustDate(itemUntilDate);
//
//            if (includeTotal) {
//                final YearMonth start = adjustDate(item.getInitialDate());
//                initialAmount = initialAmount.add(
//                        inflation.adjust(item.initialAmount(currency),
//                                start.getYear(),
//                                start.getMonth(),
//                                itemUntilYearMonth.getYear(),
//                                itemUntilYearMonth.getMonth()));
//                currentAmount = currentAmount.add(item.finalAmount(currency, itemUntilDate));
//            }
//
//            final var finalAmount = item.finalAmount(currency, itemUntilDate).getAmount();
//            final var realAmount = realAmount(item.initialAmount(currency), currency, item.getInitialDate(), itemUntilDate);
//
//            report.add(new InvestmentReportDTO(
//                    item.getType().name(),
//                    item.getInitialDate(),
//                    itemUntilDate,
//                    currency,
//                    item.initialAmount(currency).getAmount(),
//                    finalAmount,
//                    inflation(currency, item.getInitialDate(), itemUntilDate),
//                    item.getInvestment().getCurrency(),
//                    finalAmount.subtract(realAmount).setScale(2, MathConstants.RM),
//                    realAmount));
//
//        }
//
//        final Map<String, InvestmentDTO> subtotals = report.stream()
//                .collect(groupingBy(InvestmentReportDTO::getInvestmentCurrency, reducing(new InvestmentDTO(), this::mapper, this::reducer)));
//
//        final BigDecimal total = subtotals.values()
//                .stream()
//                .map(InvestmentDTO::getFinalAmount)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//        subtotals.values()
//                .stream()
//                .forEach(dto -> dto.setRelativePct(dto.getFinalAmount().divide(total, C)));
//
//        return new DetailedInvestmentReportDTO(
//                includeTotal
//                        ? new InvestmentDTO(currency, initialAmount.getAmount(), currentAmount.getAmount(), untilDate)
//                        : null,
//                report,
//                subtotals);
//    }

    private InvestmentDTO reducer(InvestmentDTO left, InvestmentDTO right) {
        if (left.getCurrency() == null) {
            return right;
        }

        return new InvestmentDTO(
                left.getCurrency(),
                left.getInitialAmount().add(right.getInitialAmount()),
                left.getFinalAmount().add(right.getFinalAmount()),
                max(left.getTo(), right.getTo()));

    }

    private InvestmentDTO mapper(InvestmentReportDTO in) {

        return new InvestmentDTO(
                in.getCurrency(),
                in.getInitialAmount(),
                in.getFinalAmount(),
                in.getTo());
    }
    
    private static Date max(Date left, Date right){
        return left.compareTo(right) < 0 ? right : left;
    }

}
