/*
 * Copyright (C) 2021 fede
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

import java.math.BigDecimal;
import static java.math.BigDecimal.ONE;
import java.util.function.Function;
import static org.fede.calculator.money.MathConstants.C;
import org.fede.calculator.money.series.SeriesReader;

/**
 *
 * @author fede
 */
public class PPIGlobalUSDFeeStrategy implements Function<BigDecimal, BigDecimal> {

    private final BigDecimal FEE_RATE = new BigDecimal("0.006");

    @Override
    public BigDecimal apply(BigDecimal presentValue) {
        var iva = SeriesReader.readPercent("iva").add(ONE);
        return presentValue
                .multiply(FEE_RATE, C)
                .max(BigDecimal.TEN)
                .multiply(iva, C);

    }
    
}
