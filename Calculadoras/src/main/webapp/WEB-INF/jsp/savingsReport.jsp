<%-- 

  Copyright (C) 2014 fede
 
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 
--%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
    <head profile="http://www.w3.org/25/10/profile">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
        <link rel="icon" 
              type="image/png" 
              href="/images/favicon.png" />
        <title>Ahorros</title>
        <link rel="stylesheet" type="text/css" href="/styles/style.css" />
        <style type="text/css">

            #chart{

                height: 800px;
            }
            .valueTd, .valueTdUsd{
                text-align: right;
            }

            .valueTdUsd{
                color: forestgreen;
            }

            table{
                font-size: 90%;
            }

            .first{
                border-left-color: black;
                border-left-style: solid;
                border-left-width: 1px;
            }

            .last{
                border-right-color: black;
                border-right-style: solid;
                border-right-width: 1px;
            }
        </style>
    </head>
    <body>
        <%@include file="../jspf/menu.jspf" %>
        <h1>Ahorros</h1>     
        <table>
            <thead>
                <tr>
                    <th colspan="3"></th>
                    <th colspan="6">Nominal</th>
                    <th colspan="6">Reales ${limits.referenceDateString}</th>
                    <th colspan="4">Ingreso Nominal</th>
                    <th colspan="4">Ingreso Real ${limits.referenceDateString}</th>
                </tr>
                <tr>
                    <th id="month">Mes</th>
                    <th id="usd">Dolar</th>
                    <th id="usdVar">+/- %</th>
                    <th id="usdAu">Dólares+Oro</th>
                    <th id="ara">Pesos</th>
                    <th id="totUSD">Total USD</th>
                    <th id="percentVar">+/- %</th>
                    <th id="totARA">Total Pesos</th>
                    <th id="araVar">+/- %</th>
                    <th id="usd99">Dólares+Oro</th>
                    <th id="ara99">Pesos</th>
                    <th id="totUSD99">Total USD</th>
                    <th id="varUSD99">+/- %</th>
                    <th id="totARA99">Total Pesos</th>
                    <th id="varARA99">+/- %</th>
                    <th id="araNom">Pesos</th>
                    <th id="pctSaving">% Ahorro</th>
                    <th id="usd2">Dólares</th>
                    <th id="pct2">% Ahorro</th>
                    <th id="ara3">Pesos</th>
                    <th id="pct3">% Ahorro</th>
                    <th id="usd3">Dólares</th>
                    <th id="savings">% Ahorro</th>
                </tr>
            </thead>
            <tbody><c:forEach items="${report}" var="item"><tr>
                        <td headers="month" class="first">${item.moment.month}/${item.moment.year}</td>
                        <td headers="usd" class="valueTd"><fmt:formatNumber type="CURRENCY">${item.pesosForDollar}</fmt:formatNumber></td>
                        <td headers="usdVar" class="valueTd"><fmt:formatNumber type="PERCENT">${item.pesosForDollarPctVar}</fmt:formatNumber></td>
                        <td headers="usdAu" class="valueTdUsd first"><fmt:formatNumber type="CURRENCY">${item.nominalDollars}</fmt:formatNumber></td>
                        <td headers="ara" class="valueTd"><fmt:formatNumber type="CURRENCY">${item.nominalPesos}</fmt:formatNumber></td>
                        <td headers="totUSD" class="valueTdUsd"><fmt:formatNumber type="CURRENCY">${item.totalNominalDollars}</fmt:formatNumber></td>
                        <td headers="percentVar" class="valueTdUsd"><fmt:formatNumber type="PERCENT">${item.totalNominalDollarsPctVar}</fmt:formatNumber></td>
                        <td headers="totARA" class="valueTd"><fmt:formatNumber type="CURRENCY">${item.totalNominalPesos}</fmt:formatNumber></td>
                        <td headers="araVar" class="valueTd"><fmt:formatNumber type="PERCENT">${item.totalNominalPesosPctVar}</fmt:formatNumber></td>
                        <td headers="usd99" class="valueTdUsd first"><fmt:formatNumber type="CURRENCY">${item.nov99Dollars}</fmt:formatNumber></td>
                        <td headers="ara99" class="valueTd"><fmt:formatNumber type="CURRENCY">${item.nov99Pesos}</fmt:formatNumber></td>
                        <td headers="totUSD99" class="valueTdUsd"><fmt:formatNumber type="CURRENCY">${item.totalNov99Dollars}</fmt:formatNumber></td>
                        <td headers="varUSD99" class="valueTdUsd"><fmt:formatNumber type="PERCENT">${item.totalNov99DollarsPctVar}</fmt:formatNumber></td>
                        <td headers="totARA99" class="valueTd"><fmt:formatNumber type="CURRENCY">${item.totalNov99Pesos}</fmt:formatNumber></td>
                        <td headers="varARA99" class="valueTd"><fmt:formatNumber type="PERCENT">${item.totalNov99PesosPctVar}</fmt:formatNumber></td>
                        <td headers="araNom" class="valueTd first"><fmt:formatNumber type="CURRENCY">${item.nominalIncomePesos}</fmt:formatNumber></td>
                        <td headers="pctSaving" class="valueTd"><fmt:formatNumber type="PERCENT">${item.nominalPesosPctSaved}</fmt:formatNumber></td>
                        <td headers="usd2" class="valueTdUsd"><fmt:formatNumber type="CURRENCY">${item.nominalIncomeDollars}</fmt:formatNumber></td>
                        <td headers="pct2" class="valueTdUsd"><fmt:formatNumber type="PERCENT">${item.nominalDollarPctSaved}</fmt:formatNumber></td>
                        <td headers="ara3" class="valueTd first"><fmt:formatNumber type="CURRENCY">${item.nov99IncomePesos}</fmt:formatNumber></td>
                        <td headers="pct3" class="valueTd"><fmt:formatNumber type="PERCENT">${item.nov99PesosPctSaved}</fmt:formatNumber></td>
                        <td headers="usd3" class="valueTdUsd"><fmt:formatNumber type="CURRENCY">${item.nov99IncomeDollars}</fmt:formatNumber></td>
                        <td headers="savings" class="valueTdUsd last"><fmt:formatNumber type="PERCENT">${item.nov99DollarPctSaved}</fmt:formatNumber></td>
                    </tr></c:forEach></tbody>
            <tfoot>
                <tr>
                    <th>Mes</th>
                    <th>Dolar</th>
                    <th>+/- %</th>
                    <th>Dólares+Oro</th>
                    <th>Pesos</th>
                    <th>Total USD</th>
                    <th>+/- %</th>
                    <th>Total Pesos</th>
                    <th>+/- %</th>
                    <th>Dólares+Oro</th>
                    <th>Pesos</th>
                    <th>Total USD</th>
                    <th>+/- %</th>
                    <th>Total Pesos</th>
                    <th>+/- %</th>
                    <th>Pesos</th>
                    <th>% Ahorro</th>
                    <th>Dólares</th>
                    <th>% Ahorro</th>
                    <th>Pesos</th>
                    <th>% Ahorro</th>
                    <th>Dólares</th>
                    <th>% Ahorro</th>
                </tr>
                <tr>
                    <th colspan="3"></th>
                    <th colspan="6">Nominal</th>
                    <th colspan="6">Reales ${limits.referenceDateString}</th>
                    <th colspan="4">Ingreso Nominal</th>
                    <th colspan="4">Ingreso Real ${limits.referenceDateString}</th>
                </tr>
            </tfoot>
        </table>
    </body>
</html>
