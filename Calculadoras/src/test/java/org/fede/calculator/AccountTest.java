/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.fede.calculator;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
public class AccountTest {

    private static final DateFormat DF = new SimpleDateFormat("dd/MM/yyyy");
    private Account account;

    private static class Movement implements Comparable<Movement> {

        private final int amount;
        private final Date from;
        private final Date to;

        public Movement(String from, String to, int points) throws ParseException {
            this(from, DF.parse(to), points);
        }

        public Movement(String from, Date to, int points) throws ParseException {
            this.from = DF.parse(from);
            this.to = to;
            if (!this.from.before(this.to)) {
                throw new IllegalArgumentException("from > to");
            }

            this.amount = points;
        }

        public boolean isCredit() {
            return this.amount > 0;
        }

        public boolean isDebit() {
            return !this.isCredit();
        }

        public boolean isCurrent(Date moment) {
            return from.compareTo(moment) <= 0 && moment.compareTo(to) < 0;
        }

        public int getAmount() {
            return this.amount;
        }

        public Date getFrom() {
            return from;
        }

        public Date getTo() {
            return to;
        }

        @Override
        public int compareTo(Movement o) {
            return this.from.compareTo(o.getFrom());
        }

    }

    private static class Account {

        List<Movement> movements = new ArrayList<>();

        public int getBalance(Date moment) {
            return this.movements.stream()
                    .filter(m -> m.isCurrent(moment))
                    .mapToInt(m -> m.getAmount())
                    .sum();
        }

        public int getBalance(String moment) throws ParseException {
            return this.getBalance(DF.parse(moment));
        }

        public int addPoints(String from, String to, int points) throws ParseException {
            this.movements.add(new Movement(from, to, points));
            return this.getBalance(from);
        }

        private void checkMovementsInvariant(List<Integer> credits, List<Integer> debits) {

            if (credits.stream().mapToInt(x -> x).sum() < debits.stream().mapToInt(x -> x).sum()) {
                throw new IllegalStateException("Debits sum more than credits.");
            }

        }

        /**
         *
         * @return the remaining debit that could not be cancelled.
         */
        private boolean allZero(List<Integer> list) {
            return list.stream().allMatch(x -> x == 0);
        }

        public int usePoints(String when, int points) throws ParseException {

            final Date moment = DF.parse(when);

            if (this.getBalance(moment) < points) {
                throw new IllegalArgumentException("Can't use more points than current balance.");
            }

            //todos los montos a favor ordenados
            List<Integer> credits = this.movements.stream()
                    .filter(m -> m.isCredit())
                    .filter(m -> m.isCurrent(moment))
                    .sorted()
                    .map(m -> m.getAmount())
                    .collect(Collectors.toList());

            //todos los montos en contra ordenados
            List<Integer> debits = this.movements.stream()
                    .filter(m -> m.isDebit())
                    .filter(m -> m.isCurrent(moment))
                    .sorted()
                    .map(m -> m.getAmount() * -1)
                    .collect(Collectors.toList());

            this.checkMovementsInvariant(credits, debits);

            while (!allZero(debits)) {
                int c = 0;
                int d = 0;

                while (d < debits.size() && debits.get(d) == 0) {
                    d++;
                }
                if (d < debits.size()) {

                    while (c < credits.size() && credits.get(c) == 0) {
                        c++;
                    }

                    if (c < credits.size()) {
                        int amountToDiscount = Math.min(credits.get(c), debits.get(d));
                        credits.set(c, credits.get(c) - amountToDiscount);
                        debits.set(d, debits.get(d) - amountToDiscount);
                    }
                }

            }

            int remainingPoints = Math.abs(points);

            List<Movement> newDebits = new ArrayList<>();

            final List<Movement> creditMovements = this.movements.stream()
                    .filter(m -> m.isCredit())
                    .filter(m -> m.isCurrent(moment))
                    .sorted()
                    .collect(Collectors.toList());

            if (creditMovements.size() != credits.size()) {
                throw new IllegalStateException("Current credit movements amount doesn't match credit values amount.");
            }

            while (remainingPoints > 0) {

                int firstNonZeroCreditIndex = IntStream.range(0, credits.size())
                        .filter(index -> credits.get(index) > 0)
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("The must be at least one movement with available points to discount."));

                Movement oldestAvailableCredit = creditMovements.get(firstNonZeroCreditIndex);

                int pointsToApply = Math.min(credits.get(firstNonZeroCreditIndex), remainingPoints);
                newDebits.add(new Movement(when, oldestAvailableCredit.getTo(), pointsToApply * -1));

                credits.set(firstNonZeroCreditIndex, credits.get(firstNonZeroCreditIndex) - pointsToApply);

                remainingPoints -= pointsToApply;

            }

            this.movements.addAll(newDebits);
            return this.getBalance(DF.parse(when));
        }
    }

    public AccountTest() {
    }

    @Before
    public void createAccount() {
        this.account = new Account();
    }

    @Test
    public void simpleCase() throws ParseException {

        assertEquals(0, account.getBalance("01/01/2001"));
        assertEquals(0, account.getBalance("01/01/2010"));
        assertEquals(0, account.getBalance("01/01/2100"));

        assertEquals(25, account.addPoints("01/01/2001", "30/06/2001", 25));
        assertEquals(50, account.addPoints("10/01/2001", "09/07/2001", 25));
        assertEquals(150, account.addPoints("20/01/2001", "19/07/2001", 100));
        assertEquals(175, account.addPoints("31/01/2001", "30/07/2001", 25));
        assertEquals(200, account.addPoints("10/03/2001", "06/09/2001", 25));

        assertEquals(175, account.usePoints("03/04/2001", 25));
        assertEquals(150, account.usePoints("04/04/2001", 25));

        // El 01/07/2001 se vencen 25 puntos, pero ya están consumidos, el saldo no cambia.
        assertEquals(150, account.getBalance("29/06/2001"));
        assertEquals(150, account.getBalance("01/07/2001"));

        // El 1/9/2001 quedan 25 puntos
        assertEquals(25, account.getBalance("01/09/2001"));

        // El 7/9/2001 se vencen los uimos puntos, el saldo queda en 0.
        assertEquals(0, account.getBalance("07/09/2001"));

    }

    @Test
    public void simpleCase2() throws ParseException {

        assertEquals(25, account.addPoints("01/01/2001", "30/06/2001", 25));
        assertEquals(50, account.addPoints("10/01/2001", "09/07/2001", 25));
        assertEquals(150, account.addPoints("20/01/2001", "19/07/2001", 100));
        assertEquals(175, account.addPoints("31/01/2001", "30/07/2001", 25));
        assertEquals(200, account.addPoints("10/03/2001", "06/09/2001", 25));

        assertEquals(187, account.usePoints("03/04/2001", 13));
        assertEquals(150, account.usePoints("04/04/2001", 37));

        // El 01/07/2001 se vencen 25 puntos, pero ya están consumidos, el saldo no cambia.
        assertEquals(150, account.getBalance("29/06/2001"));
        assertEquals(150, account.getBalance("01/07/2001"));

        // El 1/9/2001 quedan 25 puntos
        assertEquals(25, account.getBalance("01/09/2001"));

        // El 7/9/2001 se vencen los ultimos puntos, el saldo queda en 0.
        assertEquals(0, account.getBalance("07/09/2001"));

    }

    @Test
    public void simpleCase3() throws ParseException {

        assertEquals(25, account.addPoints("01/01/2001", "30/06/2001", 25));
        assertEquals(50, account.addPoints("10/01/2001", "09/07/2001", 25));
        assertEquals(150, account.addPoints("20/01/2001", "19/07/2001", 100));
        assertEquals(175, account.addPoints("31/01/2001", "30/07/2001", 25));
        assertEquals(200, account.addPoints("10/03/2001", "06/09/2001", 25));

        assertEquals(163, account.usePoints("03/04/2001", 37));
        assertEquals(150, account.usePoints("04/04/2001", 13));

        // El 01/07/2001 se vencen 25 puntos, pero ya están consumidos, el saldo no cambia.
        assertEquals(150, account.getBalance("29/06/2001"));
        assertEquals(150, account.getBalance("01/07/2001"));

        // El 1/9/2001 quedan 25 puntos
        assertEquals(25, account.getBalance("01/09/2001"));

        // El 7/9/2001 se vencen los ultimos puntos, el saldo queda en 0.
        assertEquals(0, account.getBalance("07/09/2001"));

    }

    @Test
    public void letThemExpire() throws ParseException {

        assertEquals(1, account.addPoints("01/01/2001", "30/06/2001", 1));
        assertEquals(2, account.addPoints("10/01/2001", "09/07/2001", 1));
        assertEquals(3, account.addPoints("13/01/2001", "12/07/2001", 1));
        assertEquals(4, account.addPoints("16/01/2001", "15/07/2001", 1));
        assertEquals(5, account.addPoints("20/01/2001", "19/07/2001", 1));

        assertEquals(0, account.getBalance("31/12/2000"));

        assertEquals(5, account.getBalance("29/06/2001"));
        assertEquals(4, account.getBalance("08/07/2001"));
        assertEquals(3, account.getBalance("11/07/2001"));
        assertEquals(2, account.getBalance("14/07/2001"));

        assertEquals(1, account.getBalance("16/07/2001"));

        assertEquals(0, account.usePoints("16/07/2001", 1));

        assertEquals(0, account.getBalance("18/07/2001"));

        assertEquals(0, account.getBalance("19/07/2001"));
        assertEquals(0, account.getBalance("21/12/2001"));

    }

    @Test
    public void consumeThemAll() throws ParseException {

        assertEquals(50, account.addPoints("01/01/2001", "30/06/2001", 50));
        assertEquals(100, account.addPoints("01/01/2001", "30/06/2001", 50));

        assertEquals(90, account.usePoints("01/06/2001", 10));
        assertEquals(80, account.usePoints("01/06/2001", 10));
        assertEquals(70, account.usePoints("01/06/2001", 10));
        assertEquals(60, account.usePoints("01/06/2001", 10));
        assertEquals(0, account.usePoints("01/06/2001", 60));

        assertEquals(0, account.getBalance("02/06/2001"));

    }

}
