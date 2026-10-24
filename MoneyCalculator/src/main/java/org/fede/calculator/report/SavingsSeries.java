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

/**
 *
 * @author fede
 */
public enum SavingsSeries {

    AY24("ahorros-ay24", AssetClass.BONDS),
    CONBALA("ahorros-conbala", AssetClass.BONDS),
    UVA("ahorros-uva", AssetClass.BONDS),
    DOLAR_ON("ahorros-dolar-ON", AssetClass.BONDS),
    LECAP("ahorros-lecap", AssetClass.BONDS),
    LETE("ahorros-lete", AssetClass.BONDS),
    CAPLUSA("ahorros-caplusa", AssetClass.BONDS),

    DOLAR_BANCO("ahorros-dolar-banco", AssetClass.CASH),
    DOLAR_PF("ahorros-dolar-pf", AssetClass.CASH),
    PESO("ahorros-peso", AssetClass.CASH),
    DOLAR_LIQ("ahorros-dolar-liq", AssetClass.CASH),
    EURO("ahorros-euro", AssetClass.CASH),
    EURO_LIQ("ahorros-euro-liq", AssetClass.CASH),
    DAI("ahorros-dai", AssetClass.CASH),

    CSPX("ahorros-cspx", AssetClass.EQUITY),
    EIMI("ahorros-eimi", AssetClass.EQUITY),
    RTWO("ahorros-rtwo", AssetClass.EQUITY),
    XUSE("ahorros-xuse", AssetClass.EQUITY),
    MEUD("ahorros-meud", AssetClass.EQUITY),
    CONAAFA("ahorros-conaafa", AssetClass.EQUITY),
    XRSU("ahorros-xrsu", AssetClass.EQUITY);

    private final String seriesName;
    private final AssetClass assetClass;

    SavingsSeries(String seriesName, AssetClass assetClass) {
        this.seriesName = seriesName;
        this.assetClass = assetClass;
    }

    public String getSeriesName() {
        return seriesName;
    }

    public AssetClass getAssetClass() {
        return assetClass;
    }

    public boolean usesInvestmentCashFlow() {
        return switch (this) {
            case CSPX, EIMI, RTWO, XUSE, MEUD, XRSU, CONAAFA,
                 AY24, CONBALA, LECAP, LETE, CAPLUSA, UVA ->
                true;
            default ->
                false;
        };
    }

}
