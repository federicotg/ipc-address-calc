/*
 * Copyright (C) 2016 Federico Tello Gentile <federico.gentile@despegar.com>
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
package org.fede.calculator.web.dto;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
public class DetailedInvestmentReportDTO {

    private final InvestmentDTO total;
    private final List<InvestmentReportDTO> detail;
    private final Map<String, InvestmentDTO> subtotals;

    public DetailedInvestmentReportDTO(InvestmentDTO total, List<InvestmentReportDTO> detail, Map<String, InvestmentDTO> subtotals) {
        this.total = total;
        this.detail = detail;
        this.subtotals = subtotals;
    }

    public InvestmentDTO getTotal() {
        return total;
    }

    public List<InvestmentReportDTO> getDetail() {
        return detail;
    }

    public Map<String, InvestmentDTO> getSubtotals() {
        return subtotals;
    }


}
