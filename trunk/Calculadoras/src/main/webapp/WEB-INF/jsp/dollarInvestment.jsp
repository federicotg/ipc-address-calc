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
            th{
                border-bottom-color: black;
                border-bottom-style: solid;
                border-bottom-width: 1px;
                padding:1em;
            }
            table {
                border-style: solid;
                border-color: black;
                border-width: 1px;
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
        </style>
        <script type="text/javascript" src="//ajax.googleapis.com/ajax/libs/mootools/1.5.1/mootools-yui-compressed.js"></script>
        <script type="text/javascript" src="/scripts/canvasjs.min.js"></script>
        <script type="text/javascript" src="/scripts/charts.js"></script>
        <script type="text/javascript">

            window.addEvent('domready', function () {

               // showChart('/secure/dollarInvestmentChart.json', null, 'chart');

            });


        </script>
    </head>
    <body>
        <%@include file="../jspf/menu.jspf" %>
        <h1>Inversión USD</h1>
       <%-- <div id="chart"></div>--%>
        <table>
            <thead>
                <tr>
                    <th>Fecha</th>
                    <th>Dólares</th>
                    <th>Nominales</th>
                    <th>Reales al <fmt:formatDate value="${moment}" pattern="MMM/YYYY"/></th>
                    <th>Valor al <fmt:formatDate value="${moment}" pattern="MMM/YYYY"/></th>
                    <th>Diferencia</th>
                    <th>%</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${report}" var="item">
                    <tr>
                        <td><fmt:formatDate value="${item.then}" pattern="MMM/YYYY"/></td>
                        <td><fmt:formatNumber type="CURRENCY">${item.usd}</fmt:formatNumber></td>
                        <td><fmt:formatNumber type="CURRENCY">${item.nominalPesosThen}</fmt:formatNumber></td>
                        <td><fmt:formatNumber type="CURRENCY">${item.realPesosNow}</fmt:formatNumber></td>
                        <td><fmt:formatNumber type="CURRENCY">${item.nominalPesosNow}</fmt:formatNumber></td>
                        <td><fmt:formatNumber type="CURRENCY">${item.nominalPesosNow - item.realPesosNow}</fmt:formatNumber></td>
                        <td><fmt:formatNumber type="PERCENT" minFractionDigits="2">${(item.nominalPesosNow - item.realPesosNow) / item.realPesosNow}</fmt:formatNumber></td>
                        </tr>
                </c:forEach>
            </tbody>
        </table>

    </body>
</html>
