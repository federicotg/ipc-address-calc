package org.fede.calculator.money.series;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class YearMonthTest {

    @Test
    void testNextWithinSameYear() {
        YearMonth ym = YearMonth.of(2024, 5);
        YearMonth next = ym.next();

        assertEquals(2024, next.getYear());
        assertEquals(6, next.getMonth());
    }

    @Test
    void testNextAtYearEnd() {
        YearMonth ym = YearMonth.of(2024, 12);
        YearMonth next = ym.next();

        assertEquals(2025, next.getYear());
        assertEquals(1, next.getMonth());
    }

    @Test
    void testPrevWithinSameYear() {
        YearMonth ym = YearMonth.of(2024, 8);
        YearMonth prev = ym.prev();

        assertEquals(2024, prev.getYear());
        assertEquals(7, prev.getMonth());
    }

    @Test
    void testPrevAtYearStart() {
        YearMonth ym = YearMonth.of(2024, 1);
        YearMonth prev = ym.prev();

        assertEquals(2023, prev.getYear());
        assertEquals(12, prev.getMonth());
    }

    @Test
    void testNextAndPrevAreInverses() {
        YearMonth ym = YearMonth.of(2023, 9);

        assertEquals(ym, ym.next().prev());
        assertEquals(ym, ym.prev().next());
    }
}
