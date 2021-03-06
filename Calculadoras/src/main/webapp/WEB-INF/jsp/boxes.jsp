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
<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
    <head profile="http://www.w3.org/25/10/profile">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
        <link rel="icon" 
              type="image/png" 
              href="/images/favicon.png" />
        <title>Contenido de las Cajas</title>
        <link rel="stylesheet" type="text/css" href="/styles/style.css" />
        <style type="text/css">
            li{
                line-height: 2.3em;

            }
            .opusSpan{
                border-radius: 0.2em;
                border-color: sandybrown;
                border-width: 1px;
                border-style: solid;
                padding:0.4em;
            }
            ul{
                list-style-type: square;
            }
            nav a {
                background-color: cornsilk;
                margin: 0.2em;
                display:inline-block;
            }
            .gotop{
                font-size: 50%;
            }
        </style>
    </head>
    <body>
        <%@include file="../jspf/menu.jspf" %>
        <h1 id="top">Contenido de las Cajas</h1>
        <nav>
            <c:forEach items="${boxes}" var="boxLabel">
                <a href="#${boxLabel.boxName}" class="opusSpan">Caja ${boxLabel.boxName}</a> 
            </c:forEach>
        </nav>
        <c:forEach items="${boxes}" var="boxLabel">
            <h2><a id="${boxLabel.boxName}">Caja ${boxLabel.boxName}</a> <span class="gotop"><fmt:formatNumber minFractionDigits="2" maxFractionDigits="2" groupingUsed="true">${boxLabel.sizeInGB}</fmt:formatNumber>  <abbr title="Gibibyte">GiB</abbr> <a href="#top">Volver al principio</a></span></h2>
            <ul>
                <c:forEach items="${boxLabel.contents}" var="medium">
                    <li>${medium.mediumName} <span class="gotop"><fmt:formatNumber minFractionDigits="2" maxFractionDigits="2" groupingUsed="true">${medium.sizeInGB}</fmt:formatNumber> GiB (<fmt:formatNumber type="percent" minFractionDigits="2" maxFractionDigits="2">${medium.usedCapacity}</fmt:formatNumber>)</span>
                        <c:forEach items="${medium.opus}" var="op" varStatus="status">
                            <span class="opusSpan ${op.type}">${op.name}</span><c:if test="${not status.last}"> </c:if>
                        </c:forEach>
                    </li>
                </c:forEach>
            </ul>
        </c:forEach>
    </body>
</html>
