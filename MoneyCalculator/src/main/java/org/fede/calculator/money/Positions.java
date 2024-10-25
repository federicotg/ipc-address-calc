/*
 * Copyright (C) 2021 federicogentile
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

import com.diogonunes.jcolor.Ansi;
import com.diogonunes.jcolor.Attribute;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.ONE;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import static java.text.MessageFormat.format;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.reducing;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import static org.fede.calculator.money.Currency.AY24;
import static org.fede.calculator.money.Currency.CSPX;
import static org.fede.calculator.money.Currency.EIMI;
import static org.fede.calculator.money.Currency.LECAP;
import static org.fede.calculator.money.Currency.LETE;
import static org.fede.calculator.money.Currency.MEUD;
import static org.fede.calculator.money.Currency.RTWO;
import static org.fede.calculator.money.Currency.USD;
import static org.fede.calculator.money.Currency.UVA;
import static org.fede.calculator.money.Currency.XRSU;
import static org.fede.calculator.money.ForeignExchanges.getMoneyAmountForeignExchange;
import static org.fede.calculator.money.Inflation.USD_INFLATION;
import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.InvestmentAsset;
import org.fede.calculator.money.series.YearMonth;
import static org.fede.calculator.money.MathConstants.C;
import org.fede.calculator.money.series.InvestmentEvent;
import org.fede.calculator.money.series.InvestmentType;
import org.fede.calculator.money.series.SeriesReader;
import org.fede.util.Pair;
import static org.fede.util.Pair.of;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author fede
 */
public class Positions {

    private static final Logger LOGGER = LoggerFactory.getLogger(Positions.class);

    private static final ZoneId SYSTEM_DEFAULT_ZONE_ID = ZoneId.systemDefault();

    private static final MoneyAmount ZERO_USD = MoneyAmount.zero(Currency.USD);

    private static final BigDecimal CAPITAL_GAINS_TAX_RATE = new BigDecimal("0.15");

    private static final Map<String, Function<BigDecimal, BigDecimal>> FEE_STRATEGIES = Map.of(
            "PPI_USD", new PPIGlobalUSDFeeStrategy(),
            "PPI_EUR", new PPIGlobalEURFeeStrategy(),
            "gettex", new InteractiveBrokersTieredEuronextEURFeeStrategy(),
            "lse", new InteractiveBrokersTieredLondonUSDFeeStrategy()
    );

    private static final Map<Currency, String> ETF_NAME = Map.of(
            CSPX, "iShares Core S&P 500",
            EIMI, "iShares Core MSCI EM IMI",
            XRSU, "Xtrackers Russell 2000",
            RTWO, "L&G Russell 2000 Small Cap Quality",
            MEUD, "Amundi Stoxx Europe 600"
    );

    private static final Map<String, Function<Investment, String>> GROUPINGS = Map.of(
            "h", i -> YearMonth.of(i.getInitialDate()).half(),
            "y", i -> Integer.toString(YearMonth.of(i.getInitialDate()).getYear()),
            "m", i -> YearMonth.of(i.getInitialDate()).monthString(),
            "q", i -> YearMonth.of(i.getInitialDate()).quarter());

    private static final String AVERAGE_KEY = "Avg.";

    private final Console console;
    private final Format format;
    private final Series series;
    private final Bar bar;
    //private final boolean withFee;

    public Positions(Console console, Format format, Series series, Bar bar) {
        this.console = console;
        this.format = format;
        this.series = series;
        this.bar = bar;
    }

    public void positions(boolean nominal) {

        this.console.appendLine(this.format.title("Positions Without Fees"));

        final var descWidth = 36;
        final var posWidth = 4;
        final var lastWidth = 10;
        final var costWidth = 14;
        final var costPct = 9;
        final var mkvWidth = 14;
        final var mkvPctWidth = 8;
        final var avgWidth = 10;
        final var pnlWidth = 14;
        final var pnlPctWidth = 9;

        final var separator = IntStream.rangeClosed(0, costPct + descWidth + posWidth + lastWidth + costWidth + mkvWidth + mkvPctWidth + avgWidth + pnlWidth + pnlPctWidth)
                .mapToObj(n -> "=")
                .collect(joining());

        final var fmt = " {0}{1}{2}{3}{4}{5}{6}{7}{8}{9}";

        this.console.appendLine(MessageFormat.format(fmt,
                this.format.text("       Fund", descWidth),
                this.format.text(" Pos.", posWidth),
                this.format.text("    Last", lastWidth),
                this.format.text("   Cost Basis", costWidth),
                this.format.text("    %", costPct),
                this.format.text(" Market Value", mkvWidth),
                this.format.text("   % ", mkvPctWidth),
                this.format.text("Avg. Price", avgWidth),
                this.format.text("       P&L", pnlWidth),
                this.format.text("    %", pnlPctWidth)));

        this.console.appendLine(separator);

        final var positions = this.series.getInvestments()
                .stream()
                .filter(Investment::isCurrent)
                .filter(Investment::isETF)
                .map(inv -> ForeignExchanges.exchange(inv, Currency.USD))
                .map(inv -> nominal ? inv : Inflation.USD_INFLATION.real(inv))
                .collect(groupingBy(Investment::getCurrency))
                .values()
                .stream()
                .map(this::position)
                .toList();

        final var totalMarketValue = positions
                .stream()
                .map(Position::getMarketValue)
                .reduce(ZERO_USD, MoneyAmount::add);

        final var totalCostBasis = positions
                .stream()
                .map(Position::getCostBasis)
                .reduce(ZERO_USD, MoneyAmount::add);

        final var totalPnL = positions
                .stream()
                .map(Position::getUnrealizedPnL)
                .reduce(ZERO_USD, MoneyAmount::add);

        positions
                .stream()
                .sorted(comparing((Position p) -> p.getMarketValue().getAmount(), reverseOrder()))
                .map(p -> MessageFormat.format(
                fmt,
                this.format.text(p.getFundName(), descWidth),
                String.format("%" + posWidth + "d", p.getPosition().intValue()),
                this.format.currency(p.getLast().getAmount(), lastWidth),
                this.format.currency(p.getCostBasis().getAmount(), costWidth),
                this.format.percent(p.getCostBasis().getAmount().divide(totalCostBasis.getAmount(), C), costPct),
                this.format.currency(p.getMarketValue().getAmount(), mkvWidth),
                this.format.percent(p.getMarketValue().getAmount().divide(totalMarketValue.getAmount(), C), mkvPctWidth),
                this.format.currency(p.getAveragePrice().getAmount(), avgWidth),
                this.format.currencyPL(p.getUnrealizedPnL().getAmount(), pnlWidth),
                this.format.percent(p.getUnrealizedPnL().getAmount().divide(p.getCostBasis().getAmount(), C), pnlPctWidth)))
                .forEach(this.console::appendLine);

        //this.console.appendLine(separator);
        this.console.appendLine(MessageFormat.format(fmt,
                this.format.text("Total", descWidth),
                this.format.text("", posWidth),
                this.format.text("", lastWidth),
                this.format.currency(totalCostBasis.getAmount(), costWidth),
                this.format.text("", costPct),
                this.format.currency(totalMarketValue.getAmount(), mkvWidth),
                this.format.text("", mkvPctWidth),
                this.format.text("", avgWidth),
                this.format.currencyPL(totalPnL.getAmount(), pnlWidth),
                this.format.percent(totalPnL.getAmount().divide(totalCostBasis.getAmount(), C), pnlPctWidth)));

        this.console.appendLine(this.format.subtitle("Costs"));

        final var capitalGainsTax = this.capitalGainsTaxAmount();

        final var sellFee = this.sellFee();

        final var wealthTax = wealthTax(nominal);

        final var cost = this.by(nominal, i -> "*", Investment::getCost).values().stream().findFirst().get();

        final var inflationCost = nominal ? this.inflationCost() : ZERO_USD;

        final var totalCost = sellFee.add(inflationCost.add(cost.add(wealthTax.add(capitalGainsTax))));

        final var taxes = List.of(
                new DescriptionAndMoneyAmount("Buy Cost", cost),
                new DescriptionAndMoneyAmount("Wealth Tax", wealthTax),
                new DescriptionAndMoneyAmount("Sell Fee", sellFee),
                new DescriptionAndMoneyAmount("Tax", capitalGainsTax),
                new DescriptionAndMoneyAmount("Inflation Cost", inflationCost),
                new DescriptionAndMoneyAmount("Total Cost", totalCost));

        taxes.stream().filter(p -> !p.amount().isZero()).forEach(tax -> this.printTaxLine(tax, totalCost));

        this.console.appendLine(this.format.subtitle("Fees"));
        this.costs(nominal);
        this.annualCost(nominal);
    }

    private void printTaxLine(DescriptionAndMoneyAmount tax, MoneyAmount totalPnL) {
        this.console.appendLine(MessageFormat.format("{0} {1} {2}",
                this.format.text(tax.description(), 14),
                this.format.currency(tax.amount().getAmount(), 12),
                this.format.percent(tax.amount().getAmount().divide(totalPnL.getAmount(), C), 10)));
    }

    private MoneyAmount inflationCost() {
        final var real = this.series.getInvestments()
                .stream()
                .filter(Investment::isCurrent)
                .filter(Investment::isETF)
                .map(inv -> ForeignExchanges.exchange(inv, Currency.USD))
                .map(Inflation.USD_INFLATION::real)
                .map(Investment::getInitialMoneyAmount)
                .reduce(ZERO_USD, MoneyAmount::add);

        final var nominal = this.series.getInvestments()
                .stream()
                .filter(Investment::isCurrent)
                .filter(Investment::isETF)
                .map(inv -> ForeignExchanges.exchange(inv, Currency.USD))
                .map(Investment::getInitialMoneyAmount)
                .reduce(ZERO_USD, MoneyAmount::add);

        return real.subtract(nominal);
    }

    private MoneyAmount sellFee() {
        return this.series.getInvestments()
                .stream()
                .filter(Investment::isCurrent)
                .filter(Investment::isETF)
                .map(i -> ForeignExchanges.exchange(i, Currency.USD))
                .map(this::sellFee)
                .reduce(ZERO_USD, MoneyAmount::add);
    }

    private MoneyAmount sellFee(Investment i) {
        return new MoneyAmount(FEE_STRATEGIES.get(this.feeStrategyKey(i)).apply(this.currentValueUSD(i).getAmount()), Currency.USD);
    }

    private MoneyAmount capitalGainsTaxAmount() {

        return this.series.getInvestments()
                .stream()
                .filter(Investment::isCurrent)
                .filter(Investment::isETF)
                .map(this::unrealizedUSDCapitalGains)
                .reduce(ZERO_USD, MoneyAmount::add);

    }

    private MoneyAmount currentValueUSD(Investment i) {
        return ForeignExchanges.getMoneyAmountForeignExchange(i.getCurrency(), USD)
                .apply(i.getInvestment().getMoneyAmount(), Inflation.USD_INFLATION.getTo());
    }

    private String feeStrategyKey(Investment i) {
        if (i.getComment() == null) {
            return "PPI_" + (i.getCurrency().equals(MEUD) ? "EUR" : "USD");
        }
        return i.getComment();
    }

    private MoneyAmount wealthTax(boolean nominal) {
        final var now = YearMonth.of(LocalDate.now());
        return this.series.getExpense("bbpp", nominal)
                .filter((ym, map) -> ym.compareTo(now) <= 0)
                .reduce(ZERO_USD, MoneyAmount::add);
    }

    private MoneyAmount unrealizedUSDCapitalGains(Investment i) {

        final var initialUSDAmount = Optional.ofNullable(i.getIn().getFx())
                .map(fx -> i.getInitialMoneyAmount().getAmount().multiply(fx, C))
                .map(usd -> new MoneyAmount(usd, Currency.USD))
                .orElseGet(i::getInitialMoneyAmount);

        return this.currentValueUSD(i)
                .subtract(this.sellFee(i))
                .subtract(initialUSDAmount)
                .max(ZERO_USD)
                .adjust(ONE, CAPITAL_GAINS_TAX_RATE);

    }

    public void dca(boolean nominal, String type) {

        this.console.appendLine(this.format.title((nominal ? "Nominal" : "Real") + " Dollar Cost Average"));

        final var classifier = GROUPINGS.get(type);
        this.dca(nominal, classifier);

        this.console.appendLine(this.format.subtitle("Costs"));
        this.cost(classifier, nominal);

        this.annualCost(nominal);
    }

    private void dca(boolean nominal, Function<Investment, String> groupingFunction) {

        final var averagesByGroup = this.positionsBy(
                this.series.getInvestments(),
                (i) -> AVERAGE_KEY,
                nominal);

        final var positionByGroup = this.positionsBy(
                this.series.getInvestments(),
                groupingFunction,
                nominal);

        this.console.appendLine(this.format.text("", 11),
                positionByGroup.keySet().stream()
                        .map(CurrencyAndGroupKey::currency)
                        .distinct()
                        .sorted()
                        .map(currency -> this.format.text(currency.name(), 9))
                        .collect(joining()));

        positionByGroup.keySet().stream()
                .map(CurrencyAndGroupKey::groupKey)
                .distinct()
                .sorted()
                .map(year -> this.avgPrice(year, positionByGroup, averagesByGroup))
                .forEach(this.console::appendLine);

        averagesByGroup.keySet().stream()
                .map(CurrencyAndGroupKey::groupKey)
                .distinct()
                .map(year -> this.avgPrice(year, averagesByGroup, averagesByGroup))
                .forEach(this.console::appendLine);

        this.console.appendLine(this.format.text("Curr.", 8),
                ETF_NAME.keySet().stream()
                        .sorted()
                        .map(this::currentPice)
                        .collect(joining()));
    }

    private MoneyAmount current(Currency currency) {
        final var ma = new MoneyAmount(ONE, currency);
        return ForeignExchanges.getMoneyAmountForeignExchange(ma.getCurrency(), USD).apply(ma, Inflation.USD_INFLATION.getTo());
    }

    private String currentPice(Currency currency) {
        return Ansi.colorize(this.format.currency(this.current(currency).getAmount(), 9), Attribute.WHITE_TEXT());
    }

    private Map<CurrencyAndGroupKey, Position> positionsBy(List<Investment> investments, Function<Investment, String> groupingFunction, boolean nominal) {
        return investments
                .stream()
                .filter(Investment::isCurrent)
                .filter(Investment::isETF)
                .map(inv -> ForeignExchanges.exchange(inv, Currency.USD))
                .map(inv -> nominal ? inv : Inflation.USD_INFLATION.real(inv))
                .collect(groupingBy(i -> new CurrencyAndGroupKey(i.getCurrency(), groupingFunction.apply(i))))
                .entrySet()
                .stream()
                .collect(toMap(e -> e.getKey(), e -> this.position(e.getValue())));
    }

    private String avgPrice(String year, Map<CurrencyAndGroupKey, Position> positionsByGroup, Map<CurrencyAndGroupKey, Position> averagesByGroup) {
        return Stream.concat(
                Stream.of(this.format.text(String.valueOf(year), 8)),
                positionsByGroup.keySet().stream()
                        .map(CurrencyAndGroupKey::currency)
                        .distinct()
                        .sorted()
                        .map(currency -> new CurrencyAndGroupKey(currency, year))
                        .map(key -> this.avgPrice(positionsByGroup, key, averagesByGroup)))
                .collect(joining());

    }

    private String avgPrice(Map<CurrencyAndGroupKey, Position> positionsByGroup, CurrencyAndGroupKey key, Map<CurrencyAndGroupKey, Position> averagesByGroup) {
        return Optional.ofNullable(positionsByGroup.get(key))
                .map(Position::getAveragePrice)
                .map(MoneyAmount::getAmount)
                .map(avgPrice -> this.colorized(avgPrice, averagesByGroup.get(new CurrencyAndGroupKey(key.currency(), AVERAGE_KEY)).getAveragePrice().getAmount(), this.current(key.currency()).getAmount()))
                .orElseGet(() -> this.format.text("", 9));
    }

    private String colorized(BigDecimal avgPrice, BigDecimal globalAverage, BigDecimal current) {
        Attribute color;
        if (avgPrice.compareTo(globalAverage) >= 0 && avgPrice.compareTo(current) >= 0) {
            color = Attribute.RED_TEXT();
        } else if (avgPrice.compareTo(globalAverage) > 0 || avgPrice.compareTo(current) > 0) {
            color = Attribute.YELLOW_TEXT();
        } else {
            color = Attribute.GREEN_TEXT();
        }

        return Ansi.colorize(
                this.format.currency(avgPrice, 9),
                color);
    }

    private String exchangeClassifier(Investment i) {
        if (i.getComment() == null) {
            return i.getCurrency().equals(MEUD)
                    ? "Saxo €"
                    : "Saxo $";
        }
        return i.getComment().equals("lse")
                ? "IBKR $"
                : "IBKR €";
    }

    private void costs(boolean nominal) {

        final Function<Investment, String> yearClassifier = i -> String.valueOf(YearMonth.of(i.getInitialDate()).getYear());
        final Function<Investment, String> brokerClassifier = i -> i.getComment() == null ? "PPI " : "IBKR";
        final Function<Investment, String> anyClassifier = i -> "All ";
        //  final Function<Investment, String> etfClassifier = Investment::getCurrency;
        //  final Function<Investment, String> currencyClassifier = i -> "gettex".equals(i.getComment()) ? "EUR" : "USD";

        this.cost(yearClassifier, nominal);
        this.cost(brokerClassifier, nominal);
        //    this.cost(etfClassifier, nominal);
        //    this.cost(currencyClassifier, nominal);
        this.cost(this::exchangeClassifier, nominal);
        this.cost(anyClassifier, nominal);

    }

    private void cost(Function<Investment, String> classifier, boolean nominal) {

        final var inv = this.by(nominal, classifier, Investment::getInitialMoneyAmount);
        final var cost = this.by(nominal, classifier, Investment::getCost);
        final var totalInv = inv.values().stream().map(MoneyAmount::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        inv
                .keySet()
                .stream()
                .sorted()
                .forEach(e -> this.costReport(e, inv, cost, totalInv));
        this.console.appendLine("");

    }

    private void annualCost(boolean nominal) {

        final Function<Investment, String> any = i -> "all";

        final var totalCost = this.by(nominal, any, Investment::getCost)
                .values()
                .stream()
                .map(MoneyAmount::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        final var totalInv = this.by(nominal, any, Investment::getInitialMoneyAmount)
                .values()
                .stream()
                .map(MoneyAmount::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final var startDate = this.series.getInvestments()
                .stream()
                .filter(Investment::isCurrent)
                .filter(Investment::isETF)
                .map(Investment::getInitialDate)
                .min(Comparator.naturalOrder())
                .map(Date::toInstant)
                .map(i -> LocalDate.ofInstant(i, SYSTEM_DEFAULT_ZONE_ID))
                .get();

        final var days = ChronoUnit.DAYS.between(startDate, LocalDate.now());

        this.console.appendLine(MessageFormat.format("{0} {1}",
                "Cost per year",
                this.format.percent(
                        new BigDecimal("0.00104").add(
                                new BigDecimal(Math.pow(totalCost.divide(totalInv, C).add(ONE, C).doubleValue(), 365.0d / (double) days) - 1.0d))), C));

    }

    private void costReport(String label, Map<String, MoneyAmount> m1, Map<String, MoneyAmount> m2, BigDecimal totalinv) {
        this.console.appendLine(label,
                this.format.currency(m1.get(label).getAmount(), 13),
                this.format.percent(m1.get(label).getAmount().divide(totalinv, C), 9),
                this.format.currency(m2.get(label).getAmount(), 11),
                this.format.percent(m2.get(label).getAmount().divide(m1.get(label).getAmount(), C), 8));
    }

    private Map<String, MoneyAmount> by(boolean nominal, Function<Investment, String> classifier, Function<Investment, MoneyAmount> func) {
        return this.series.getInvestments()
                .stream()
                .filter(Investment::isCurrent)
                .filter(Investment::isETF)
                .map(inv -> ForeignExchanges.exchange(inv, Currency.USD))
                .map(inv -> nominal ? inv : Inflation.USD_INFLATION.real(inv))
                .collect(groupingBy(classifier,
                        mapping(func, reducing(ZERO_USD, MoneyAmount::add))));
    }

    private Position position(List<Investment> investments) {
        final var symbol = investments.stream().findAny().get().getCurrency();

        final var position = investments.stream()
                .map(Investment::getInvestment)
                .map(InvestmentAsset::getAmount)
                .reduce(ZERO, BigDecimal::add);

        final var now = YearMonth.of(new Date());

        return new Position(
                ETF_NAME.get(symbol),
                position,
                ForeignExchanges.getMoneyAmountForeignExchange(symbol, USD).apply(new MoneyAmount(ONE, symbol), now),
                investments.stream()
                        .map(i -> i.getIn().getMoneyAmount())
                        .reduce(ZERO_USD, MoneyAmount::add),
                ForeignExchanges.getMoneyAmountForeignExchange(symbol, USD)
                        .apply(investments.stream()
                                .map(Investment::getMoneyAmount)
                                .reduce(MoneyAmount.zero(symbol), MoneyAmount::add),
                                now),
                new MoneyAmount(
                        investments.stream()
                                .map(Investment::getIn)
                                .map(InvestmentEvent::getAmount)
                                .reduce(ZERO, BigDecimal::add)
                                .divide(position, C),
                        Currency.USD));
    }

    public void portfolioChart(String type, String subtype, int year, int month) {
        try {

            final var items = this.portfolioItems(subtype, year, month);

            DefaultPieDataset<String> ds = new DefaultPieDataset<>();

            for (var item : items) {
                ds.setValue(
                        Investments.ETF_NAME.getOrDefault(
                                item.getAmount().getCurrency(),
                                item.getAmount().getCurrency().name()),
                        item.getDollarAmount().amount());
            }

            final var pctFormat = NumberFormat.getPercentInstance(Locale.of("es", "AR"));
            pctFormat.setMinimumFractionDigits(2);

            JFreeChart portfolio = ChartFactory.createPieChart("Portfolio", ds);
            var lg = new StandardPieSectionLabelGenerator("{0} {2}",
                    NumberFormat.getInstance(Locale.of("es", "AR")),
                    pctFormat);

            var p = (PiePlot) portfolio.getPlot();

            p.setLabelGenerator(lg);

            ChartUtils.saveChartAsPNG(new File("portfolio.png"), portfolio, 1200, 900);
        } catch (IOException ioEx) {
            LOGGER.error("Error writting chart.", ioEx);
        }

    }

    public void portfolio(String type, String subtype, int year, int month) {

        final var items = this.portfolioItems(subtype, year, month);

        final var total = items.stream()
                .map(PortfolioItem::getDollarAmount)
                .reduce(ZERO_USD, MoneyAmount::add);

        final var pct = "pct".equals(type);

        items.stream()
                .map(i -> pct ? i.asPercentReport(total) : i.asReport(total))
                .forEach(this.console::appendLine);

        if (!pct) {
            this.console.appendLine("--------------------------------------");
            this.console.appendLine(format("Total {0}", this.format.currency(total.getAmount())));
        }
    }

    private List<PortfolioItem> portfolioItems(String subtype, int year, int month) {
        final var ym = YearMonth.of(year, month);

        final Map<String, Map<String, Optional<MoneyAmount>>> grouped
                = Stream.of(
                        of("BOND", this.lastAmount("ahorros-ay24", ym)),
                        of("BOND", this.lastAmount("ahorros-conbala", ym)),
                        of("BOND", this.lastAmount("ahorros-uva", ym)),
                        of("BOND", this.lastAmount("ahorros-dolar-ON", ym)),
                        of("BOND", this.lastAmount("ahorros-lecap", ym)),
                        of("BOND", this.lastAmount("ahorros-lete", ym)),
                        of("BOND", this.lastAmount("ahorros-dolar-pf", ym)),
                        of("BOND", this.lastAmount("ahorros-caplusa", ym)),
                        of("CASH", this.lastAmount("ahorros-dolar-banco", ym)),
                        of("CASH", this.lastAmount("ahorros-peso", ym)),
                        of("CASH", this.lastAmount("ahorros-dolar-liq", ym)),
                        of("CASH", this.lastAmount("ahorros-euro", ym)),
                        of("CASH", this.lastAmount("ahorros-dai", ym)),
                        of("EQUITY", this.lastAmount("ahorros-cspx", ym)),
                        of("EQUITY", this.lastAmount("ahorros-eimi", ym)),
                        of("EQUITY", this.lastAmount("ahorros-rtwo", ym)),
                        of("EQUITY", this.lastAmount("ahorros-meud", ym)),
                        of("EQUITY", this.lastAmount("ahorros-conaafa", ym)),
                        of("EQUITY", this.lastAmount("ahorros-xrsu", ym)))
                        .filter(p -> "all".equals(subtype) || p.first().equalsIgnoreCase(subtype))
                        .collect(groupingBy(
                                Pair::first,
                                groupingBy(
                                        p -> p.second().get().getCurrency().name(),
                                        mapping(
                                                p -> p.second().get(),
                                                reducing(MoneyAmount::add)))));

        return grouped
                .entrySet()
                .stream()
                .flatMap(e -> this.item(e.getKey(), e.getValue(), ym))
                .sorted(comparing((PortfolioItem::getDollarAmount), comparing(MoneyAmount::getAmount)).reversed())
                .toList();

    }

    private Stream<PortfolioItem> item(String type, Map<String, Optional<MoneyAmount>> amounts, YearMonth ym) {

        return amounts.values()
                .stream()
                .flatMap(Optional::stream)
                .filter(Predicate.not(MoneyAmount::isZero))
                .map(amount -> new PortfolioItem(amount, type, ym));
    }

    private Supplier<MoneyAmount> lastAmount(String seriesName, YearMonth ym) {
        return () -> SeriesReader.readSeries("saving/".concat(seriesName).concat(".json")).getAmountOrElseZero(ym);
    }

    public void listStockByType() {

        final var reportCurrency = USD;
        final var limit = USD_INFLATION.getTo();
        final var limitStr = String.valueOf(limit.getMonth()) + "/" + String.valueOf(limit.getYear());

        this.console.appendLine(this.format.title(format("Inversiones Actuales en {0} por tipo. ", limitStr)));

        final Optional<MoneyAmount> total = this.total(Investment::isCurrent, reportCurrency, limit);

        this.series.getInvestments().stream()
                .filter(Investment::isCurrent)
                .collect(groupingBy(
                        this::assetAllocation,
                        mapping(inv -> getMoneyAmountForeignExchange(inv.getInvestment().getCurrency(), reportCurrency).apply(inv.getInvestment().getMoneyAmount(), limit)
                        .getAmount()
                        .setScale(MathConstants.SCALE, MathConstants.RM),
                                reducing(ZERO, BigDecimal::add))))
                .entrySet()
                .stream()
                .map(entry -> this.formatReport(total, entry.getValue(), entry.getKey()))
                .forEach(this.console::appendLine);

        total.map(t -> format("-----------------------------\n{0} {1}", this.format.text("Total", 5), this.format.currency(t, 16)))
                .ifPresent(this.console::appendLine);
    }

    private String assetAllocation(Investment investment) {
        final Set<Currency> equities = Set.of(CSPX, EIMI, MEUD, XRSU, RTWO);
        final Set<Currency> bonds = Set.of(LECAP, LETE, UVA, AY24);

        if (equities.contains(investment.getInvestment().getCurrency())) {
            return "EQ";
        }
        if (investment.getType().equals(InvestmentType.BONO)
                || investment.getType().equals(InvestmentType.PF)
                || bonds.contains(investment.getInvestment().getCurrency())) {
            return "BO";
        }

        return "CASH";

    }

    private Optional<MoneyAmount> total(Predicate<Investment> predicate, Currency reportCurrency, YearMonth limit) {
        return this.series.getInvestments().stream()
                .filter(predicate)
                .map(Investment::getInvestment)
                .map(InvestmentAsset::getMoneyAmount)
                .map(investedAmount -> getMoneyAmountForeignExchange(investedAmount.getCurrency(), reportCurrency).apply(investedAmount, limit))
                .reduce(MoneyAmount::add);
    }

    public void groupedInvestments() {
        final var reportCurrency = USD;
        final var limit = USD_INFLATION.getTo();

        this.console.appendLine("Inversiones Actuales Agrupadas en ", reportCurrency.name(), " ", String.valueOf(limit.getYear()), "/", String.valueOf(limit.getMonth()));

        final var total = this.total(Investment::isCurrent, reportCurrency, limit);
        this.series.getInvestments().stream()
                .filter(Investment::isCurrent)
                .collect(groupingBy(in -> new InvestmentTypeAndCurrency(in.getType(), in.getCurrency()),
                        mapping(inv -> inv.getMoneyAmount().getAmount(),
                                reducing(ZERO, BigDecimal::add))))
                .entrySet()
                .stream()
                .map(e -> new InvestmentTypeCurrencyAndAmount(e.getKey(), e.getValue()))
                .map(p -> this.fx(p, reportCurrency))
                .sorted((p, q) -> q.amount().compareTo(p.amount()))
                .map(pair -> this.formatReport(total, pair.amount(), pair.type()))
                .forEach(this.console::appendLine);

        total.map(t -> format("-----------------------------\n{0}{1}", this.format.text("Total", 5), this.format.currency(t, 16)))
                .ifPresent(this.console::appendLine);
    }

    private InvestmentTypeCurrencyAndAmount fx(InvestmentTypeCurrencyAndAmount p, Currency reportCurrency) {

        return new InvestmentTypeCurrencyAndAmount(
                p.type(),
                reportCurrency,
                getMoneyAmountForeignExchange(
                        p.currency(),
                        reportCurrency)
                        .apply(new MoneyAmount(p.amount(), p.currency()),
                                USD_INFLATION.getTo()).getAmount());
    }

    private String formatReport(Optional<MoneyAmount> total, BigDecimal subtotal, InvestmentType type) {
        return this.formatReport(total, subtotal, type.toString());
    }

    private String formatReport(Optional<MoneyAmount> total, BigDecimal subtotal, String type) {

        return format("{0}{1}{2}",
                this.format.text(type, 5),
                this.format.currency(subtotal, 16),
                this.bar.pctBar(total.map(tot -> subtotal.divide(tot.getAmount(), C)).orElse(ZERO)));
    }

    private record CurrencyAndGroupKey(Currency currency, String groupKey) {

    }

    private record DescriptionAndMoneyAmount(String description, MoneyAmount amount) {

    }

}
