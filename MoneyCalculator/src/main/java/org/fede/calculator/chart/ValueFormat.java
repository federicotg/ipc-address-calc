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
            return ValueFormat.PCT;
        }

    },
    CURRENCY {
        @Override
        public Format format() {
            return ValueFormat.CURR;
        }
    },
    CURRENCY_DECIMALS {
        @Override
        public Format format() {
            return ValueFormat.CURR_DEC;
        }
    },
    NUMBER {
        @Override
        public Format format() {
            return NUM;
        }
    },
    DATE {
        @Override
        public Format format() {
            return DATE_FMT;
        }
    };

    private static final NumberFormat PCT = NumberFormat.getPercentInstance();

    private static final NumberFormat CURR_DEC = NumberFormat.getCurrencyInstance();

    private static final NumberFormat CURR = NumberFormat.getCurrencyInstance();

    private static final NumberFormat NUM = NumberFormat.getNumberInstance();

    private static final Format DATE_FMT = new SimpleDateFormat("dd-MMM-yy");

    static {
        PCT.setMinimumFractionDigits(2);
        CURR_DEC.setMinimumFractionDigits(2);
        CURR.setMaximumFractionDigits(0);
    }

    public abstract Format format();

}
