/*
 * Copyright (C) 2023 federicogentile
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

import org.fede.calculator.report.Format;
import com.diogonunes.jcolor.Ansi;
import com.diogonunes.jcolor.Attribute;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * @author federicogentile
 */
public class References {

    private final Console console;
    private final Format format;

    public References(Console console, Format format) {
        this.console = console;
        this.format = format;
    }

    public void refs(String title, List<String> labels, List<Attribute> colors) {
        this.console.appendLine(this.format.title(title));
        this.console.appendLine("References:");

        this.console.appendLine(IntStream.range(0, labels.size())
                .mapToObj(i -> Ansi.colorize(" ", colors.get(i)) + labels.get(i))
                .collect(Collectors.joining(" ", "", "")));
    }

    public void refsLabels(List<String> labels, List<Attribute> colors) {
        this.refsLabels(null, labels, colors);
    }

    public void refsLabels(String title, List<String> labels, List<Attribute> colors) {
        if (title != null) {
            this.console.appendLine(this.format.title(title));
        } else {
            this.console.appendLine("");

        }
        this.console.appendLine("References:");

        this.console.appendLine(IntStream.range(0, labels.size())
                .mapToObj(i -> Ansi.colorize(labels.get(i), colors.get(i)))
                .collect(Collectors.joining(" ", "", "")));
    }
}
