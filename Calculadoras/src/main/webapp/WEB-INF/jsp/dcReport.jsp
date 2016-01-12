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
        <title>Catálogo de Contenidos Digitales</title>
        <link rel="stylesheet" type="text/css" href="/styles/style.min.css" />
        <style type="text/css">

            .centeredTd{
                text-align: center;
            }
            strong{
                font-size: 150%;
            }
        </style>
    </head>
    <body>
        <%@include file="../jspf/menu.jspf" %>
        <h1>Catálogo de Contenidos Digitales</h1>
        <table summary="La información de cada elemento del catálogo: en qué caja está, que obra es, de quién es la música, en qué formato está, etc.">
            <thead>
                <tr>
                    <th id="box">Caja</th>
                    <th id="type">Tipo</th>
                    <th id="title">Título</th>
                    <th id="music">Música</th>
                    <th id="date">Fecha</th>
                    <th id="venue">Lugar</th>
                    <th id="quality">Calidad</th>
                    <th id="lang">Leng.</th>
                    <th id="sub">Sub.</th>
                    <th id="format">Formato</th>
                    <th id="imdbLink">IMDB</th>
                    <th id="f">F.</th>
                    <th id="am">A.M.</th>
                    <th id="e">E.</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${list}" var="item">
                    <tr>
                        <td headers="box">
                            <c:forEach items="${item.boxes}" var="box" varStatus="status">
                                <a href="/dc/report/box/${box}">Caja ${box}</a><c:if test="${not status.last}">, </c:if>
                            </c:forEach>
                        </td>
                        <td headers="type">
                            <c:forEach items="${item.opusTypes}" var="type" varStatus="status">
                                <a href="/dc/report/type/${type}">${type}</a><c:if test="${not status.last}">, </c:if>
                            </c:forEach>
                        </td>
                        <td headers="title">
                            <c:forEach items="${item.titles}" var="title" varStatus="status">
                                <a href="/dc/report/opus/${title}">${title}</a><c:if test="${not status.last}">, </c:if>
                            </c:forEach>
                        </td>
                        <td headers="music">
                            <c:forEach items="${item.musicBy}" var="composer" varStatus="status">
                                <spring:url value="/dc/report/composer/${composer}" var="composerUri" htmlEscape="true"/>
                                <a href="${composerUri}">${composer}</a><c:if test="${not status.last}">, </c:if>
                            </c:forEach>
                        </td>
                        <td class="centeredTd" headers="date"><fmt:formatDate value="${item.date}" pattern="dd/MM/yyyy"/></td>
                        <td headers="venue">
                            <c:forEach items="${item.venues}" var="venue" varStatus="status">
                                <a href="/dc/report/venue/${venue}">${venue}</a><c:if test="${not status.last}">, </c:if>
                            </c:forEach>
                        </td>
                        <td class="centeredTd" headers="quality">${item.quality}</td>
                        <td class="centeredTd" headers="lang">${item.language}</td>
                        <td class="centeredTd" headers="sub">${item.subtitles}</td>
                        <td class="centeredTd" headers="format">${item.format}</td>
                        <td headers="imdbLink">
                            <c:if test="${not empty item.imdb}">
                                <a href="${item.imdb}">IMDB</a>
                            </c:if>
                        </td>
                        <td class="centeredTd" headers="f"><c:if test="${item.seenByFede}"><strong>☑</strong></c:if></td>
                        <td class="centeredTd" headers="am"><c:if test="${item.seenByAnaMaria}"><strong>☑</strong></c:if></td>
                        <td class="centeredTd" headers="e"><c:if test="${item.seenByElsa}"><strong>☑</strong></c:if></td>
                        </tr>
                </c:forEach>
            </tbody>
        </table>
    </body>
</html>
