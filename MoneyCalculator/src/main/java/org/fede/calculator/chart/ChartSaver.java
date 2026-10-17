/*
 * Copyright (C) 2025 fede
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

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.jfree.chart.JFreeChart;

/**
 * Shared executor for chart render/save work across chart types.
 *
 * @author fede
 */
public final class ChartSaver {

    private static final ExecutorService POOL = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors(),
            Thread.ofPlatform().name("chart-", 0).factory());

    private static final Queue<Future<?>> PENDING = new ConcurrentLinkedQueue<>();

    private ChartSaver() {
    }

    public static void save(String file, JFreeChart chart, int width, int height) {
        var strategy = ChartStrategy.currentStrategy();
        PENDING.add(POOL.submit(() -> {
            try {
                strategy.saveChart(file, chart, width, height);
            } catch (IOException ioEx) {
                System.err.println("Unexpected error. " + ioEx.getMessage());
                ioEx.printStackTrace(System.err);
            }
        }));
    }

    public static void awaitPending() {
        Future<?> future;
        while ((future = PENDING.poll()) != null) {
            try {
                future.get();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return;
            } catch (ExecutionException ex) {
                System.err.println("Unexpected error. " + ex.getCause().getMessage());
                ex.getCause().printStackTrace(System.err);
            }
        }
    }

    public static void shutdown() {
        awaitPending();
        POOL.shutdown();
        try {
            if (!POOL.awaitTermination(30, TimeUnit.SECONDS)) {
                POOL.shutdownNow();
            }
        } catch (InterruptedException ex) {
            POOL.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
