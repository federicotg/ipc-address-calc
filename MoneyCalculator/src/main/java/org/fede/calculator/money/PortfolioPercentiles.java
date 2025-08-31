
package org.fede.calculator.money;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.fede.calculator.money.series.SeriesReader;

public class PortfolioPercentiles {

  public static double valueAtPercentile(
            double initialValue, 
            double cagr,       // g (ej. 0.035 = 3.5%), retorno geométrico
            double volatility, // sigma (ej. 0.17 = 17%)
            double years, 
            double percentile) {

        NormalDistribution normal = new NormalDistribution();

        // Cuantil z_p de la normal estándar
        double z = normal.inverseCumulativeProbability(percentile);
  
        // μ = g - 0.5 * σ^2 (CAGR como retorno geométrico)
        double mu = cagr - 0.5 * volatility * volatility;

        // Fórmula: V_p(T) = V0 * exp( (μT) + σ√T * z )
        return initialValue * Math.exp(mu * years + volatility * Math.sqrt(years) * z);
    }

    public static void main(String[] args) {
        double V0 = 460000;    // inversión inicial
        double g = SeriesReader.readPercent("futureReturn").doubleValue();       // 5% CAGR esperado
        double sigma = SeriesReader.readPercent("futureVolatility").doubleValue();    // 10% volatilidad anual

        for (double T : new double[]{1, 5, 10, 20, 30}) {
            double p25 = valueAtPercentile(V0, g, sigma, T, 0.1);
            double p75 = valueAtPercentile(V0, g, sigma, T, 0.9);
            System.out.printf("%.0f años -> P10 = %.2f | P90 = %.2f%n", T, p25, p75);
        }
    }
}
