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
        <title>Inversión</title>
        <link rel="stylesheet" type="text/css" href="/styles/style.min.css" />
        <style type="text/css">

            #chart{

                height: 800px;
            }
            .valueTd{
                text-align: right;
            }

            .loss {
                color: red;
            }

            .win {
                color: green;
            }

        </style>
    </head>
    <body>
        <%@include file="../jspf/menu.jspf" %>
        <h1>Inversión</h1>
        <table>
            <thead>
                <tr>
                    <th>Tipo</th>
                    <th>Desde</th>
                    <th>Hasta</th>
                    <th>Inversión</th>
                    <th>Retorno</th>
                    <th>+/-</th>
                    <th>+/- %</th>
                    <th>Inflación</th>
                    <th>+/- % Real</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${report}" var="item">
                    <tr>
                        <td>${item.type}</td>
                        <td><fmt:formatDate value="${item.from}" type="date" /></td>
                        <td><fmt:formatDate value="${item.to}" type="date" /></td>
                        <td class="valueTd">${item.currency} <fmt:formatNumber type="CURRENCY">${item.initialAmount}</fmt:formatNumber></td>
                        <td class="valueTd">${item.currency} <fmt:formatNumber type="CURRENCY">${item.finalAmount}</fmt:formatNumber></td>
                        <td class="valueTd">${item.currency} <fmt:formatNumber type="CURRENCY">${item.differenceAmount}</fmt:formatNumber></td>
                        <td class="valueTd"><fmt:formatNumber type="PERCENT" minFractionDigits="2">${item.pct}</fmt:formatNumber></td>
                        <td class="valueTd"><fmt:formatNumber type="PERCENT" minFractionDigits="2">${item.inflationPct}</fmt:formatNumber></td>
                        <c:choose>
                            <c:when test="${item.differencePct ge 0}"><td class="valueTd win"></c:when>
                            <c:otherwise><td class="valueTd loss"></c:otherwise>
                            </c:choose>
                            <fmt:formatNumber type="PERCENT" minFractionDigits="2">${item.differencePct}</fmt:formatNumber></td>
                        </tr>
                </c:forEach>
            </tbody>
        </table>

    </body>
</html>
