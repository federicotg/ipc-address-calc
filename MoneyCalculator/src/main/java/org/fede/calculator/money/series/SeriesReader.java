/*
 * Copyright (C) 2016 Federico Tello Gentile <federicotg@gmail.com>
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.fede.calculator.money.MathConstants;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.SingleHttpClientSupplier;
import org.fede.calculator.fmp.CachedETF;
import org.fede.calculator.service.ETF;
import org.fede.calculator.fmp.FMPPriceData;
import org.fede.calculator.fmp.FinancialModelingPrep;
import org.fede.calculator.money.Currency;
import org.fede.calculator.ppi.CachedCCL;
import org.fede.calculator.ppi.PPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author fede
 */
public class SeriesReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeriesReader.class);

    public static final String APP_RESOURCES = System.getProperty("user.home") + File.separator + "Documents" + File.separator + "app-resources" + File.separator;

    public static final String SECRETS = APP_RESOURCES + "ppi-secrets.properties";

    private static final Map<String, String> CURRENCY_ETF = Map.of(
            "CSPX", ETF.CSPX,
            "RTWO", ETF.RTWO,
            "EIMI", ETF.EIMI,
            "XRSU", ETF.XRSU,
            "IWDA", ETF.IWDA,
            "MEUD", ETF.MEUD
    );

    private static final ObjectMapper OM = new ObjectMapper();

    private static final Map<String, JSONIndexSeries> CACHE = new ConcurrentHashMap<>();

    private static final TypeReference<List<JSONDataPoint>> INDEX_SERIES_TYPE_REFERENCE = new TypeReference<List<JSONDataPoint>>() {
    };

    private static final Map<String, MoneyAmountSeries> MACACHE = new ConcurrentHashMap<>();

    private static Map<String, FMPPriceData> ETFS;

    private static Map<String, BigDecimal> CCL;

    private static final Pattern INDEX_SERIES_NAME = Pattern.compile("index/([A-Z]{4,4})-(?:USD|EUR).json");

    static {
        OM.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private static JSONIndexSeries createIndexSeries(String name) {
        var indexSeries = new JSONIndexSeries(read(name, INDEX_SERIES_TYPE_REFERENCE));

        final var now = YearMonth.of(LocalDate.now());

        if (indexSeries.getIndex(now.year(), now.month()).signum() == 0) {

            var matcher = INDEX_SERIES_NAME.matcher(name);
            if (matcher.find()) {
                var currency = matcher.group(1);
                var value = etfs().get(CURRENCY_ETF.get(currency));
                if (value != null) {
                    indexSeries.put(now, value.price());
                }
            }
            if ("index/USD-EUR.json".equals(name)) {
                indexSeries.put(
                        now,
                        etfs().get(ETF.MEUS).price().divide(etfs().get(ETF.MEUD).price(), MathConstants.C));
            }
            if ("index/peso-dolar-libre.json".equals(name)) {
                indexSeries.put(now, ccl().get("ARS"));
            }
        }
        return indexSeries;

    }

    public static IndexSeries readIndexSeries(String name) {
        return CACHE.computeIfAbsent(name, SeriesReader::createIndexSeries);
    }

    private static Map<String, FMPPriceData> etfs() {
        if (ETFS == null) {
            try {
                var om = new ObjectMapper()
                        .setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE)
                        .registerModule(new JavaTimeModule());
                ETFS = new CachedETF(om, new FinancialModelingPrep(om, new SingleHttpClientSupplier())).etfs();
            } catch (IOException ex) {
                LOGGER.error("Unexpected error.", ex);
                ETFS = Map.of();
            }
        }
        return ETFS;
    }

    private static Map<String, BigDecimal> ccl() {
        if (CCL == null) {
            var om = new ObjectMapper()
                    .setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE)
                    .registerModule(new JavaTimeModule());
            CCL = new CachedCCL(om, new PPI(null, null, new SingleHttpClientSupplier())).ccl();
        }
        return CCL;
    }

    public static <T> T read(String name, TypeReference<T> typeReference) {
        try (InputStream in = new BufferedInputStream(new FileInputStream(new File(APP_RESOURCES + name)));) {
            return OM.readValue(in, typeReference);
        } catch (IOException ioEx) {
            LOGGER.error("Unexpected error.", ioEx);
            throw new IllegalArgumentException("Could not read series from resource " + name, ioEx);
        }
    }

    public static MoneyAmountSeries readSeries(String name) {
        return MACACHE.computeIfAbsent(name, (seriesName) -> read(seriesName));
    }

    private static MoneyAmount moneyAmount(BigDecimal value, Currency currency) {

        return value.signum() == 0
                ? MoneyAmount.zero(currency)
                : new MoneyAmount(value, currency);
    }

    private static MoneyAmountSeries read(String name) {

        try (InputStream is = new BufferedInputStream(new FileInputStream(APP_RESOURCES + name))) {

            JSONSeries series = OM.readValue(is, JSONSeries.class);

            final Currency currency = series.currency();
            final var maSeries = new SortedMapMoneyAmountSeries(currency, name);

            for (JSONDataPoint dp : series.data()) {
                maSeries.putAmount(YearMonth.of(dp.year(), dp.month()), moneyAmount(dp.value(), currency));
            }

            final InterpolationStrategy strategy = InterpolationStrategy.valueOf(series.interpolation());

            YearMonth ym = maSeries.getFrom();
            final YearMonth last = maSeries.getTo();

            while (ym.monthsUntil(last) > 0) {
                YearMonth next = ym.next();
                if (!maSeries.hasValue(next)) {
                    maSeries.putAmount(next, strategy.interpolate(maSeries.getAmount(ym), ym, currency));
                }
                ym = ym.next();
            }
            return maSeries;

        } catch (IOException ioEx) {
            LOGGER.error("Unexpected error.", ioEx);
            throw new IllegalArgumentException(MessageFormat.format("Could not read series named {0}", name), ioEx);
        }

    }

}
