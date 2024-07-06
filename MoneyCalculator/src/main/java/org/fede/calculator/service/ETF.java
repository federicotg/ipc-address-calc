/*
 * Copyright (C) 2024 fede
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

import java.util.Map;
import org.fede.calculator.fmp.ExchangeTradedFundData;

/**
 *
 * @author fede
 */
public interface ETF {

    public static final String CSPX = "CSPX.L";
    public static final String RTWO = "RTWO.L";
    public static final String EIMI = "EIMI.L";
    public static final String IWDA = "IWDA.L";
    public static final String MEUD = "MEUD.PA";
    public static final String MEUS = "MEUS.L";
    public static final String XRSU = "XRSU.L";

    Map<String, ExchangeTradedFundData> etfs();
}
