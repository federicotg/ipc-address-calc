/*
 * Copyright (C) 2022 fede
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

import com.fasterxml.jackson.core.type.TypeReference;
import java.math.BigDecimal;
import static java.math.BigDecimal.ONE;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import static org.fede.calculator.money.MathConstants.C;
import static org.fede.calculator.money.MathConstants.RM;
import static org.fede.calculator.money.MathConstants.SCALE;
import org.fede.calculator.money.series.AnnualHistoricalReturn;
import org.fede.calculator.money.series.SeriesReader;
import org.fede.calculator.money.series.YearMonth;

/**
 *
 * @author fede
 */
public class HistoricalReturnSupplier implements Supplier<List<BigDecimal>> {

    private static final TypeReference<List<AnnualHistoricalReturn>> TR = new TypeReference<List<AnnualHistoricalReturn>>() {};
    
    
    private final String seriesName;

    public HistoricalReturnSupplier(String seriesName) {
        this.seriesName = seriesName;
    }
    
   private AnnualHistoricalReturn real(AnnualHistoricalReturn nominal) {
        return new AnnualHistoricalReturn(
                nominal.getYear(),
                Inflation.USD_INFLATION.adjust(
                        new MoneyAmount(nominal.getTotalReturn(), "USD"),
                        YearMonth.of(nominal.getYear(), 12),
                        YearMonth.of(nominal.getYear() - 1, 12)).getAmount());
    }
    
    @Override
    public List<BigDecimal> get() {
        return SeriesReader.read(this.seriesName, TR)
                .stream()
                .map(this::real)
                .map(AnnualHistoricalReturn::getTotalReturn)
                .map(r -> ONE.setScale(MathConstants.SCALE, RM).add(r.setScale(SCALE, RM).movePointLeft(2), C))
                .collect(Collectors.toList()); 
    }
    
}
