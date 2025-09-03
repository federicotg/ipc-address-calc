/*
 * Copyright (C) 2025 fede
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
package org.fede.calculator.chart;

import java.text.Format;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

/**
 *
 * @author fede
 */
public enum ValueFormat {
    PERCENTAGE {
        @Override
        public Format format() {
            var answer = NumberFormat.getPercentInstance();
            answer.setMinimumFractionDigits(1);
            return answer;
        }

    },
    CURRENCY {
        @Override
        public Format format() {
            var nf = NumberFormat.getCurrencyInstance();
            nf.setCurrency(java.util.Currency.getInstance("USD"));
            nf.setMaximumFractionDigits(0);
            return nf;
        }
    },
    NUMBER {
        @Override
        public Format format() {
            return NumberFormat.getNumberInstance();
        }
    },
    DATE {
        @Override
        public Format format() {
            return new SimpleDateFormat("dd-MMM-yy");
        }
    };

    public abstract Format format();

}
