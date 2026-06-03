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
package org.fede.calculator.chart;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberTick;
import org.jfree.chart.axis.ValueTick;
import org.jfree.chart.ui.RectangleEdge;

/**
 *
 * @author fede
 */
class DenseLogarithmicAxis extends LogarithmicAxis {

    private static final long serialVersionUID = 1L;
    private static final double LABEL_TOLERANCE = 1e-9;

    DenseLogarithmicAxis(String label) {
        super(label);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected List refreshTicksVertical(
            Graphics2D g2,
            Rectangle2D dataArea,
            RectangleEdge edge) {
        return addIntermediateLabels(super.refreshTicksVertical(g2, dataArea, edge));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private List addIntermediateLabels(List ticks) {
        var result = new ArrayList<>(ticks.size());

        for (Object tick : ticks) {
            String text = tick instanceof NumberTick nt ? nt.getText() : "";
            if (tick instanceof NumberTick nt
                    && (text == null || text.isEmpty())
                    && shouldLabel(nt)) {
                result.add(new NumberTick(
                        nt.getTickType(),
                        nt.getValue(),
                        formatTickLabel(nt.getValue()),
                        nt.getTextAnchor(),
                        nt.getRotationAnchor(),
                        nt.getAngle()));
            } else {
                result.add(tick);
            }
        }

        return result;
    }

    private boolean shouldLabel(ValueTick tick) {
        double value = tick.getValue();
        if (value <= 0.0) {
            return false;
        }

        double exponent = Math.floor(Math.log10(value));
        double normalizedValue = value / Math.pow(10.0, exponent);

        return isCloseTo(normalizedValue, 2.0)
                || isCloseTo(normalizedValue, 5.0);
    }

    private String formatTickLabel(double value) {
        NumberFormat format = getNumberFormatOverride();

        if (format != null) {
            return format.format(value);
        }

        return makeTickLabel(value);
    }

    private boolean isCloseTo(double value, double expected) {
        return Math.abs(value - expected) < LABEL_TOLERANCE;
    }
}
