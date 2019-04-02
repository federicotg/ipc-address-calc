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
<%@ taglib uri = "http://java.sun.com/jsp/jstl/functions" prefix = "fn" %>

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

            article{
                /*width: 50%;
                float: left;*/
            }

            td {
                font-size: 90%;
            }

            h2{
                font-size: 110%;
            }            
        </style>
    </head>
    <body>
        <%@include file="../jspf/menu.jspf" %>
        <h1>Inversión</h1>
        
        <article id="reportUSD">
            
            <c:if test="${not empty reportUSD.total}">
                <%--
                <h2>Total</h2>
                <table>
                    <thead>
                        <tr>
                            <th>Hasta</th>
                            <th>Inversión</th>
                            <th>Retorno</th>
                            <th>+/-</th>
                            <th>+/- %</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td><fmt:formatDate value="${reportUSD.total.to}" type="date" /></td>
                            <td class="valueTd"><fmt:formatNumber type="CURRENCY" currencySymbol="USD">${reportUSD.total.initialAmount}</fmt:formatNumber></td>
                            <td class="valueTd"><fmt:formatNumber type="CURRENCY">${reportUSD.total.finalAmount}</fmt:formatNumber></td>
                            <c:choose><c:when test="${reportUSD.total.differenceAmount ge 0}"><td class="valueTd win"></c:when><c:otherwise><td class="valueTd loss"></c:otherwise></c:choose>
                                <fmt:formatNumber type="CURRENCY" currencySymbol="USD">${reportUSD.total.differenceAmount}</fmt:formatNumber></td>
                            <c:choose><c:when test="${reportUSD.total.pct ge 0}"><td class="valueTd win"></c:when><c:otherwise><td class="valueTd loss"></c:otherwise></c:choose>
                                <fmt:formatNumber type="PERCENT" minFractionDigits="2">${reportUSD.total.pct}</fmt:formatNumber>

                                <c:choose>
                                    <c:when test="${reportUSD.total.pct gt 0.05}"><strong>&#x21C8;</strong></c:when>
                                    <c:when test="${reportUSD.total.pct gt 0}"><strong>&#x2197;</strong></c:when>
                                    <c:when test="${reportUSD.total.pct.unscaledValue() == 0}"><strong>&#x3d;</strong></c:when>
                                    <c:when test="${reportUSD.total.pct lt -0.05}"><strong>&#x21CA;</strong></c:when>
                                    <c:otherwise><strong>&#x2198;</strong></c:otherwise>
                                </c:choose>

                            </td>
                        </tr>
                    </tbody>
                </table--%>


                <h2>Subtotal</h2>
                <p><a href="${filteringUris['all']}">Todos</a></p>
                <table>
                    <thead>
                        <tr>
                            <th>Moneda</th>
                            <th>Hasta</th>
                            <th>Inversión Nominal</th>
                            <th>Retorno Nominal</th>
                            <th>+/-  Nominal</th>
                        </tr>
                    </thead>
                    <tbody>

                        <c:forEach items="${reportUSD.subtotals}" var="entry">
                            <tr>
                                <td><a href="${filteringUris[entry.key]}">${entry.key}</a></td>
                                <td><fmt:formatDate value="${entry.value.to}" type="date" /></td>
                                <td class="valueTd"><fmt:formatNumber type="CURRENCY" currencySymbol="USD">${entry.value.initialAmount}</fmt:formatNumber></td>
                                <td class="valueTd"><fmt:formatNumber type="CURRENCY" currencySymbol="USD">${entry.value.finalAmount}</fmt:formatNumber></td>
                                <c:choose><c:when test="${entry.value.differenceAmount ge 0}"><td class="valueTd win"></c:when><c:otherwise><td class="valueTd loss"></c:otherwise></c:choose>
                                    <fmt:formatNumber type="CURRENCY" currencySymbol="USD">${entry.value.differenceAmount}</fmt:formatNumber>


                                        &nbsp;


                                    <fmt:formatNumber type="PERCENT" minFractionDigits="2">${entry.value.pct}</fmt:formatNumber>


                                    <c:choose>
                                        <c:when test="${entry.value.pct gt 0.05}"><strong>&#x21C8;</strong></c:when>
                                        <c:when test="${entry.value.pct gt 0}"><strong>&#x2197;</strong></c:when>
                                        <c:when test="${entry.value.pct.unscaledValue() == 0}"><strong>&#x3d;</strong></c:when>
                                        <c:when test="${entry.value.pct lt -0.05}"><strong>&#x21CA;</strong></c:when>
                                        <c:otherwise><strong>&#x2198;</strong></c:otherwise>
                                    </c:choose>



                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>                            




                <h2>Detalles</h2>
            </c:if>
            <table>
                <thead>
 <tr>
                        <th>Moneda <a href="${sortingUris['currency']}"><strong>&#x21F5;</strong></a></th>
                        <th>Desde <a href="${sortingUris['from']}"><strong>&#x21F5;</strong></a></th>
                        <th>Hasta <a href="${sortingUris['to']}"><strong>&#x21F5;</strong></a></th>
                        <th>Inversión <a href="${sortingUris['investment']}"><strong>&#x21F5;</strong></a></th>
                        <th>Retorno <a href="${sortingUris['return']}"><strong>&#x21F5;</strong></a></th>
                        <th>+/- <a href="${sortingUris['pctDif']}"><strong>&#x21F5;</strong></a></th>
                        <th>Inf.</th>
                        <th>+/- Real <a href="${sortingUris['realPctDif']}"><strong>&#x21F5;</strong></a></th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${reportUSD.detail}" var="item">
                        <tr>
                            <td>${item.investmentCurrency}</td>
                            <td><fmt:formatDate value="${item.from}" type="date" /></td>
                            <td><fmt:formatDate value="${item.to}" type="date" /></td>
                            <td class="valueTd"><fmt:formatNumber type="CURRENCY" currencySymbol="USD">${item.initialAmount}</fmt:formatNumber></td>
                            <td class="valueTd"><fmt:formatNumber type="CURRENCY" currencySymbol="USD">${item.finalAmount}</fmt:formatNumber></td>
                            <c:choose><c:when test="${item.differenceAmount ge 0}"><td class="valueTd win"></c:when><c:otherwise><td class="valueTd loss"></c:otherwise></c:choose>
                                <fmt:formatNumber type="CURRENCY" currencySymbol="USD">${item.differenceAmount}</fmt:formatNumber>
                                    &nbsp;
                                <fmt:formatNumber type="PERCENT" minFractionDigits="2">${item.pct}</fmt:formatNumber></td>
                                <td class="valueTd">
                                <fmt:formatNumber type="PERCENT" minFractionDigits="2">${item.inflationPct}</fmt:formatNumber>


                                </td>

                            <c:choose>
                                <c:when test="${item.differencePct ge 0}">
                                    <td class="valueTd win">
                                    </c:when>
                                    <c:otherwise>
                                    <td class="valueTd loss">
                                    </c:otherwise>
                                </c:choose>

                                <fmt:formatNumber type="CURRENCY" minFractionDigits="2">${item.differencePct}</fmt:formatNumber>


                                    &nbsp;

                                <fmt:formatNumber type="PERCENT" minFractionDigits="2">${item.diffPct}</fmt:formatNumber>
                                <c:choose>
                                    <c:when test="${item.diffPct gt 0.05}"><strong>&#x21C8;</strong></c:when>
                                    <c:when test="${item.diffPct gt 0}"><strong>&#x2197;</strong></c:when>
                                    <c:when test="${item.diffPct.unscaledValue() == 0}"><strong>&#x3d;</strong></c:when>
                                    <c:when test="${item.diffPct lt -0.05}"><strong>&#x21CA;</strong></c:when>
                                    <c:otherwise><strong>&#x2198;</strong></c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
                <tfoot>
                    <tr>
                        <th>Moneda</th>
                        <th>Desde</th>
                        <th>Hasta</th>
                        <th>Inversión</th>
                        <th>Retorno</th>
                        <th>+/-</th>
                        <th>Inf.</th>
                        <th>+/- Real</th>
                    </tr>
                </tfoot>
            </table>
        </article>
    </body>
</html>
