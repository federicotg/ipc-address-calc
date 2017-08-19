/*
 * Copyright (C) 2017 Federico Tello Gentile <federicotg@gmail.com>
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
package org.fede.calculator;

import com.fasterxml.jackson.core.type.TypeReference;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.fede.calculator.money.series.IndexSeries;
import org.fede.calculator.money.series.Investment;
import org.fede.calculator.money.series.InvestmentType;
import org.fede.calculator.money.series.SeriesReader;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
public class FCITest {

    private final Map<String, String> fciNames = new HashMap<>(3);
    private final Map<String, String> fciTypes = new HashMap<>(3);

    private static final TypeReference<List<Investment>> TYPE_REFERENCE = new TypeReference<List<Investment>>() {
    };

    public FCITest() {
        fciNames.put("CONAAFA", "Consultatio Acciones Argentina F.C.I. Clase A (número de inscripción 271 en la Comisión Nacional de Valores)");
        fciNames.put("CONBALA", "Consultatio Balance Fund F.C.I. Clase A (número de inscripción 120 en la Comisión Nacional de Valores)");

        fciTypes.put("CONAAFA", "Renta variable en $");
        fciTypes.put("CONBALA", "Renta fija en $");

    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void fci() {

        final int year = 2016;

        final DateFormat df = DateFormat.getDateInstance();
        final NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(6);

        IndexSeries conbala = SeriesReader.readIndexSeries("index/CONBALA_AR-peso.json");
        IndexSeries conaafa = SeriesReader.readIndexSeries("index/CONAAFA_AR-peso.json");

        final Map<String, BigDecimal> dicPreviousYearValues = new HashMap<>(3);
        dicPreviousYearValues.put("CONAAFA", conaafa.getIndex(year - 1, 12));
        dicPreviousYearValues.put("CONBALA", conbala.getIndex(year - 1, 12));

        final Map<String, BigDecimal> dicValues = new HashMap<>(3);
        dicValues.put("CONAAFA", conaafa.getIndex(year, 12));
        dicValues.put("CONBALA", conbala.getIndex(year, 12));

        System.out.println(Stream.of("Fecha de adquisición",
                "Tipo de fondo",
                "Denominación",
                "CUIT Soc. Gerente",
                "CUIT Soc. Depositaria",
                "Cantidad",
                "Valor cotización al 31/12/" + (year - 1),
                "Valor cotización al 31/12/" + year).collect(Collectors.joining("\";\"", "\"", "\"")));

        Collections.singletonList("investments.json").stream()
                .flatMap(fileName -> SeriesReader.read(fileName, TYPE_REFERENCE).stream())
                .filter(inv -> InvestmentType.FCI.equals(inv.getType()))
                .filter(inv -> inv.getInitialDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear() == year)
                .sorted(Comparator.comparing(Investment::getInitialDate, (left, right) -> left.compareTo(right)))
                .map(inv -> MessageFormat.format("\"{0}\";\"{1}\";\"{2}\";\"{3}\";\"{4}\";\"{5}\";\"{6}\";\"{7}\"",
                df.format(inv.getInitialDate()),
                fciTypes.get(inv.getInvestment().getCurrency()),
                fciNames.get(inv.getInvestment().getCurrency()),
                "30-67726994-0",
                "30-57612427-5",
                nf.format(inv.getInvestment().getAmount()),
                nf.format(dicPreviousYearValues.get(inv.getInvestment().getCurrency())),
                nf.format(dicValues.get(inv.getInvestment().getCurrency()))
        )).forEach(System.out::println);

    }

    @Test
    public void posessions() {

        BigDecimal currentlyInvested = Collections.singletonList("investments.json").stream()
                .flatMap(fileName -> SeriesReader.read(fileName, TYPE_REFERENCE).stream())
                .filter(inv -> InvestmentType.FCI.equals(inv.getType()))
                .filter(inv -> inv.getOut() == null)
                .map(inv -> inv.getInitialMoneyAmount().getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> map = Collections.singletonList("investments.json").stream()
                .flatMap(fileName -> SeriesReader.read(fileName, TYPE_REFERENCE).stream())
                .filter(inv -> InvestmentType.FCI.equals(inv.getType()))
                .filter(inv -> inv.getOut() == null)
                .collect(
                        Collectors.groupingBy(
                                inv -> inv.getInvestment().getCurrency(),
                                Collectors.mapping(inv -> inv.getInitialMoneyAmount().getAmount(), Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                        )
                );

        NumberFormat nf = NumberFormat.getCurrencyInstance();

        System.out.println("Total Invertido: " + nf.format(currentlyInvested));

        map.entrySet().stream().forEach(e -> System.out.println(e.getKey() + ": " + nf.format(e.getValue())));

    }

}
