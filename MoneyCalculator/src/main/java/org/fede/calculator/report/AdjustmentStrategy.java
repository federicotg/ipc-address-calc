/*
 * Copyright (C) 2026 fede
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
package org.fede.calculator.report;

import java.math.BigDecimal;
import static java.math.BigDecimal.ONE;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.series.SeriesReader;

/**
 *
 * @author fede
 */
public enum AdjustmentStrategy {

    TIMES2PLUSIVA {
        @Override
        public MoneyAmount adjust(MoneyAmount amount) {
            final var iva = SeriesReader.readPercent("iva").add(ONE);
            return amount.adjust(ONE, BigDecimal.TWO).adjust(ONE, iva);
        }
    },
    AGUINALDO {
        @Override
        public MoneyAmount adjust(MoneyAmount amount) {
            return amount.adjust(BigDecimal.valueOf(12), BigDecimal.valueOf(13));
        }
    },
    SEVERANCE {
        @Override
        public MoneyAmount adjust(MoneyAmount amount) {
            return Future.severance(amount).getTotal();
        }
    };

    public abstract MoneyAmount adjust(MoneyAmount amount);
}
