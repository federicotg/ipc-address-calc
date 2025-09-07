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
package org.fede.calculator.money;
import org.apache.commons.math3.distribution.NormalDistribution;

/**
 *
 * @author fede
 */
public class PortfolioProjections {
      /**
     * Calculates the estimated portfolio value at a given percentile.
     * This model assumes that the portfolio returns follow a log-normal distribution.
     *
     * @param initialValue  The starting value of the portfolio.
     * @param cagr          The Compound Annual Growth Rate (geometric mean) of the portfolio.
     * @param volatility    The annual volatility (standard deviation) of the portfolio's returns.
     * @param years         The number of years to project into the future.
     * @param percentile    The desired percentile (e.g., 0.90 for the 90th percentile).
     * @return The estimated portfolio value at the specified percentile.
     */
    public static double calculatePortfolioPercentile(double initialValue, double cagr, double volatility, int years, double percentile) {
        if (percentile <= 0 || percentile >= 1) {
            throw new IllegalArgumentException("Percentile must be between 0 and 1.");
        }

        // The Z-score for the desired percentile from the standard normal distribution.
        NormalDistribution normalDistribution = new NormalDistribution();
        double zScore = normalDistribution.inverseCumulativeProbability(percentile);

        // The expected log-return (drift) of the portfolio.
        // We adjust the CAGR by half of the variance to get the arithmetic mean return in the log-normal model.
        double drift = (cagr - (volatility * volatility) / 2) * years;

        // The random component (diffusion) of the portfolio's log-return.
        double diffusion = volatility * Math.sqrt(years) * zScore;

        // The final portfolio value is the initial value multiplied by the exponentiated sum of the drift and diffusion.
        return initialValue * Math.exp(drift + diffusion);
    }

    public static void main(String[] args) {
        // Your example inputs
        double initialPortfolioValue = 430000.0;
        double expectedCagr = 0.04; // 4%
        double portfolioVolatility = 0.15; // 15%
        int projectionYears = 5;
        double desiredPercentile = 0.90; // 90th percentile

        // Calculate the 90th percentile of the portfolio value
        double percentileValue = calculatePortfolioPercentile(initialPortfolioValue, expectedCagr, portfolioVolatility, projectionYears, desiredPercentile);

        // Print the result
        System.out.printf("Initial Portfolio Value: $%,.2f%n", initialPortfolioValue);
        System.out.printf("Expected CAGR: %.2f%%%n", expectedCagr * 100);
        System.out.printf("Annual Volatility: %.2f%%%n", portfolioVolatility * 100);
        System.out.printf("Projection over %d years.%n", projectionYears);
        System.out.println("------------------------------------");
        System.out.printf("The estimated 90th percentile of your portfolio value is: $%,.2f%n", percentileValue);
    }
}
