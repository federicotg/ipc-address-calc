package org.fede.calculator.report;

import java.math.BigDecimal;

/**
 *
 * @author fede
 */
public record TypeAndAmount(
        String type,
        BigDecimal amount) {

}
