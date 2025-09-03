package org.fede.calculator.report;

import org.fede.calculator.money.Currency;
import org.fede.calculator.money.series.InvestmentType;


/**
 *
 * @author federicogentile
 */
public record InvestmentTypeAndCurrency(InvestmentType type, Currency currency){}

