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
package org.fede.calculator.service;

import java.util.List;
import org.fede.calculator.money.NoSeriesDataFoundException;
import org.fede.calculator.web.dto.DollarReportDTO;
import org.fede.calculator.web.dto.InvestmentReportDTO;
import org.fede.calculator.web.dto.SavingsReportDTO;

/**
 *
 * @author fede
 */
public interface InvestmentService {
    
    //List<DollarReportDTO> dollar() throws NoSeriesDataFoundException;
    
    List<SavingsReportDTO> savings(int year, int month) throws NoSeriesDataFoundException;
    
    public List<InvestmentReportDTO> investment(String currency) throws NoSeriesDataFoundException;
    
}
