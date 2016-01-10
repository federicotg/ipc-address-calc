/*
 * Copyright (C) 2014 fede
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
package org.fede.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.fede.calculator.money.NoSeriesDataFoundException;
import org.fede.calculator.money.series.JSONMoneyAmountSeries;
import org.fede.calculator.money.series.MoneyAmountSeries;
import org.fede.calculator.web.dto.ExpenseChartSeriesDTO;

/**
 *
 * @author fede
 */
public class Util {

    public static <T> String list(Collection<T> elements) {
        return list(elements, ", ");
    }

    public static <T> String list(Collection<T> elements, String separator) {
        StringBuilder sb = new StringBuilder(elements.size() * 10);
        for (Iterator<T> it = elements.iterator(); it.hasNext();) {
            sb.append(it.next().toString());
            if (it.hasNext()) {
                sb.append(separator);
            }
        }
        return sb.toString();
    }
    
    public static MoneyAmountSeries sumSeries(List<ExpenseChartSeriesDTO> dtos) throws NoSeriesDataFoundException{
        
        List<String> seriesNames = new ArrayList<>(dtos.size());
        for(ExpenseChartSeriesDTO dto : dtos){
            seriesNames.add(dto.getSeriesName());
        }
        return JSONMoneyAmountSeries.sumSeries(seriesNames.toArray(new String[seriesNames.size()]));
        
    }
}
