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
<html>
    <head profile="http://www.w3.org/25/10/profile">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
        <link rel="icon" 
              type="image/png" 
              href="/images/favicon.png" />
        <title>Inversión USD</title>
        <link rel="stylesheet" type="text/css" href="/styles/style.css" />
        <style type="text/css">

            thead{
                text-align: center;
            }
            
            thead th:last-child{
                border-right-color: black;
                border-right-style: solid;
                border-right-width: 1px;
            }
            th{
                border-bottom-color: black;
                border-bottom-style: solid;
                border-bottom-width: 1px;
                border-left-color: black;
                border-left-style: solid;
                border-left-width: 1px;
                padding:1em;
            }
            table {
                border-top-style: solid;
                border-top-color: black;
                border-top-width: 1px;
                font-size: 90%;
            }
            tbody td {
                /*border-bottom-color: black;
                border-bottom-style: solid;
                border-bottom-width: 1px;*/
                padding:0.4em;
            }
            table{
                border-spacing: 0px;
            }
            tbody tr:nth-child(even) {
                background-color: white;
            }
            tbody tr:hover {
                background-color: cornsilk;
            }
            #chart{

                height: 800px;
            }
            .valueTd{
                text-align: right;
            }
        </style>
    </head>
    <body>
        <%@include file="../jspf/menu.jspf" %>
        <h1>Inversión USD</h1>
        <table>
            <thead>
                <tr>
                    <th colspan="2">Compra</th>
                    <th colspan="2">Pesos</th>
                    <th colspan="3">Valor de venta (<fmt:formatDate value="${moment}" pattern="MMM/YYYY"/>)</th>
                    <th colspan="2">Tipo de Cambio Real</th>
                </tr>
                <tr>
                    <th>Fecha</th>
                    <th>Dólares</th>
                    <th>Nominales</th>
                    <th>Reales al <fmt:formatDate value="${moment}" pattern="MMM/YYYY"/></th>
                    <th>Pesos</th>
                    <th>Diferencia</th>
                    <th>%</th>
                    <th>$/USD</th>
                    <th>Dif. dólar hoy</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${report}" var="item">
                    <tr>
                        <td><fmt:formatDate value="${item.then}" pattern="MMM/YYYY"/></td>
                        <td class="valueTd"><fmt:formatNumber type="CURRENCY">${item.usd}</fmt:formatNumber></td>
                        <td class="valueTd"><fmt:formatNumber type="CURRENCY">${item.nominalPesosThen}</fmt:formatNumber></td>
                        <td class="valueTd"><fmt:formatNumber type="CURRENCY">${item.realPesosNow}</fmt:formatNumber></td>
                        <td class="valueTd"><fmt:formatNumber type="CURRENCY">${item.nominalPesosNow}</fmt:formatNumber></td>
                        <td class="valueTd"><fmt:formatNumber type="CURRENCY">${item.nominalPesosNow - item.realPesosNow}</fmt:formatNumber></td>
                        <td class="valueTd"><fmt:formatNumber type="PERCENT" minFractionDigits="2">${(item.nominalPesosNow - item.realPesosNow) / item.realPesosNow}</fmt:formatNumber></td>
                        <td class="valueTd"><fmt:formatNumber type="CURRENCY">${item.realUsdThen}</fmt:formatNumber></td>
                        <td class="valueTd"><fmt:formatNumber type="PERCENT" minFractionDigits="2">${(item.realUsdThen - item.nominalUsdNow) / item.nominalUsdNow}</fmt:formatNumber></td>
                        </tr>
                </c:forEach>
            </tbody>
        </table>

    </body>
</html>
