/*
 * Copyright (C) 2014 fede
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
package org.fede.calculator.money.series;

import java.util.List;

/**
 * Gold:
 * http://www.bundesbank.de/cae/servlet/StatisticDownload?tsId=BBEX3.M.XAU.USD.EA.AC.C06&its_csvFormat=en&its_fileFormat=csv&mode=its
 * Euro: https://www.statistics.dnb.nl/index.cgi?lang=uk&todo=Koersen =====
 * Guía: ===== Dólar: valor a fin de mes Ahorros USD: valor a fin de mes (a
 * partir de dic 2014, antes valor al 1 del mes) Ahorros ARA: valor a fin de mes
 * (a partir de dic 2014, antes valor al 1 del mes) CPI: valor del mes (se
 * conoce al mes siguiente) Gold: valor mensual según la serie de arriba Sueldo
 * UNLP: mes liquidado (fecha del recibo, anterior a cobrarlo) Sueldo LIFIA: mes
 * de cobro (fecha de la factura) Gastos: mes del pago (fecha del comprobante o
 * débito de tarjeta) Gastos del consorcio: mes que figura en la liquidación de
 * la administración
 *
 * @author fede
 */
public record JSONSeries(
        String currency,
        List<JSONDataPoint> data,
        String interpolation) {

}
