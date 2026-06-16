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
import java.util.stream.Stream;
import static org.fede.calculator.money.Currency.USD;
import static org.fede.calculator.money.MathConstants.C;
import org.fede.calculator.money.MoneyAmount;
import org.fede.calculator.money.series.SeriesReader;

/**
 *
 * @author fede
 */
public class CAEYSafeWithdrawalRate {

    private final BigDecimal cashRealYield;
    private final BigDecimal bondsRealYield;
    private final BigDecimal cape;
    private final BigDecimal capeExUs;
    private final BigDecimal capeEmerging;

    public CAEYSafeWithdrawalRate(BigDecimal expectedInflation, BigDecimal bondsRealYield, BigDecimal cape, BigDecimal capeExUs, BigDecimal capeEmerging) {
        this.cashRealYield = expectedInflation.negate(C);
        this.bondsRealYield = bondsRealYield;
        this.cape = cape;
        this.capeExUs = capeExUs;
        this.capeEmerging = capeEmerging;
    }

    public CAEYSafeWithdrawalRate() {
        this(
                SeriesReader.readPercent("expectedInflation"),
                SeriesReader.readPercent("bond10"),
                SeriesReader.readBigDecimal("cape"),
                SeriesReader.readBigDecimal("cape.exus"),
                SeriesReader.readBigDecimal("cape.em")
        );
    }

    public BigDecimal capeWR(
            MoneyAmount usEquity,
            MoneyAmount devExUs,
            MoneyAmount emerging,
            MoneyAmount bonds,
            MoneyAmount cash) {

        var totalEquity = usEquity.add(devExUs).add(emerging).amount();

        var totalPortfolioValue = Stream.of(usEquity, devExUs, emerging, cash, bonds)
                .reduce(MoneyAmount.zero(USD), MoneyAmount::add);

        final var caey = Stream.of(
                ONE.divide(this.cape, C).multiply(usEquity.adjust(totalEquity, ONE).amount(), C),
                ONE.divide(this.capeExUs, C).multiply(devExUs.adjust(totalEquity, ONE).amount(), C),
                ONE.divide(this.capeEmerging, C).multiply(emerging.adjust(totalEquity, ONE).amount(), C))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return SeriesReader.readPercent("cape.a")
                .add(SeriesReader.readPercent("cape.b").multiply(caey, C))
                .add(this.bondsRealYield.multiply(bonds.adjust(totalPortfolioValue.amount(), ONE).amount(), C))
                .add(this.cashRealYield.multiply(cash.adjust(totalPortfolioValue.amount(), ONE).amount(), C));
    }

}
