/*
 * Copyright (C) 2016 Federico Tello Gentile <federicotg@gmail.com>
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

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.fede.calculator.config.AppConfig;
import org.fede.calculator.service.DigitalContentService;
import org.fede.calculator.service.InvestmentService;
import org.fede.digitalcontent.model.DigitalContent;
import org.fede.digitalcontent.model.Person;
import org.fede.digitalcontent.model.Quality;
import org.fede.digitalcontent.model.Repository;
import org.fede.digitalcontent.model.StorageBox;
import org.fede.digitalcontent.model.StorageMedium;
import org.fede.util.Pair;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {AppConfig.class}, loader = AnnotationConfigContextLoader.class)
public class SingleMediaTest {

    private static final Pattern BOX_DISC = Pattern.compile("(\\d{1,2})-(\\d{1,2})");

    @Autowired
    private DigitalContentService service;

    @Autowired
    private InvestmentService investments;

    private static final NumberFormat NF = NumberFormat.getIntegerInstance();

    static {
        NF.setMinimumIntegerDigits(3);
        NF.setGroupingUsed(false);
    }

    public SingleMediaTest() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    //@Test
    public void singles() {
        assertNotNull(this.service);
        Repository.STORAGE.stream()
                .filter(disc -> disc.getContents().size() == 1)
                .map(disc -> MessageFormat.format("Disc: {0}", disc.toString()))
                .forEach(System.out::println);

    }

    /**
     * De cada disco cuáles videos no vi, ordenados por caja y disco.
     */
    @Test
    public void toSee() {

        final Person me = Repository.PERSON.findById("Federico");

        Repository.DIGITALCONTENT.stream()
                .filter(dc -> !dc.isSeenBy(me))
                .filter(dc -> dc.getQuality() != null && !dc.getQuality().equals(Quality.DVD480))
                .map(dc -> new Pair<>(dc, Repository.STORAGEBOX.stream().filter(box -> box.contains(dc)).collect(Collectors.toList())))
                .map(this::remap)
                .reduce((m1, m2) -> this.merge(m1, m2))
                .orElse(Collections.emptyMap())
                .forEach((box, list) -> System.out.println(MessageFormat.format("{0} -> {1}", box.getName(), list.stream().map(dc -> dc.toString()).collect(Collectors.joining(", ")))));

    }

    private String formatMediumName(String name) {

        Matcher m = BOX_DISC.matcher(name);
        if (m.matches()) {
            return new StringBuilder(5)
                    .append(NF.format(Integer.parseInt(m.group(1))))
                    .append("-")
                    .append(NF.format(Integer.parseInt(m.group(2))))
                    .toString();
        }
        return name;

    }

    private Map<StorageMedium, List<DigitalContent>> merge(Map<StorageMedium, List<DigitalContent>> m1, Map<StorageMedium, List<DigitalContent>> m2) {
        Map<StorageMedium, List<DigitalContent>> merged = new TreeMap<>(Comparator.comparing(medium -> this.formatMediumName(medium.getName())));
        merged.putAll(m2);
        m1.forEach((box, list) -> merged.merge(
                box,
                list,
                (l1, l2) -> Stream.concat(l1.stream(), l2.stream()).collect(Collectors.toList())));

        return merged;
    }

    private Map<StorageMedium, List<DigitalContent>> remap(Pair<DigitalContent, List<StorageBox>> pair) {
        DigitalContent dc = pair.getFirst();
        return pair.getSecond().stream()
                .flatMap(box -> box.getMedia().stream())
                .filter(medium -> medium.contains(dc))
                .collect(Collectors.groupingBy(medium -> medium, Collectors.mapping(medium -> dc, Collectors.toList())));

    }

//    @Test
//    public void report() throws NoSeriesDataFoundException {
//        List<InvestmentReportDTO> report = this.investments.pastInvestmentsReport("USD").getDetail();
//        for(InvestmentReportDTO dto : report){
//            System.out.println(dto.getInflationPct());
//        }
//    }
    private boolean isInOneMedium(DigitalContent dc) {
        return Repository.STORAGE.stream()
                .filter(disc -> disc.contains(dc))
                .count() == 1l;
//        int count = 0;
//        for (StorageMedium disc : Repository.STORAGE.findAll()) {
//            if (disc.contains(dc)) {
//                count++;
//            }
//        }
//        return count == 1;
    }
}
