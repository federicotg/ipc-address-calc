package org.fede.calculator.web.dto;

import java.math.BigDecimal;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
public class SavingsReportCurrencyElementDTO {
    
    private AmountAndVariationDTO value = new AmountAndVariationDTO();
    private BigDecimal savings;
    private AmountAndVariationDTO income = new AmountAndVariationDTO();
    private AmountAndVariationDTO total = new AmountAndVariationDTO();

    public AmountAndVariationDTO getValue() {
        return value;
    }

    public void setValue(AmountAndVariationDTO value) {
        this.value = value;
    }

    public BigDecimal getSavings() {
        return savings;
    }

    public void setSavings(BigDecimal savings) {
        this.savings = savings;
    }

    public AmountAndVariationDTO getIncome() {
        return income;
    }

    public void setIncome(AmountAndVariationDTO income) {
        this.income = income;
    }

    public AmountAndVariationDTO getTotal() {
        return total;
    }

    public void setTotal(AmountAndVariationDTO total) {
        this.total = total;
    }
    
    
    
}
