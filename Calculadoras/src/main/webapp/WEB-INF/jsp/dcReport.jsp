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
        <title>Videos</title>
        <link rel="stylesheet" type="text/css" href="/styles/style.css" />
        <style type="text/css">

            thead{
                text-align: center;
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
        <h1>Videos</h1>
        <table>
            <thead>
                <tr>
                    <th>Caja</th>
                    <th>Tipo</th>
                    <th>Título</th>
                    <th>Música</th>
                    <th>Fecha</th>
                    <th>Lugar</th>
                    <th>Calidad</th>
                    <th>Lenguaje</th>
                    <th>Subtítulos</th>
                    <th>Formato</th>
                    <th>IMDB</th>
                    <th>Yo</th>
                    <th>A.M.</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${list}" var="item">
                    <tr>
                        <td>
                            <c:forEach items="${item.boxes}" var="box" varStatus="status">
                                <a href="/dc/report/box/${box}">${box}</a><c:if test="${not status.last}">, </c:if>
                            </c:forEach>
                        </td>
                        <td>
                            <c:forEach items="${item.opusTypes}" var="type" varStatus="status">
                                <a href="/dc/report/type/${type}">${type}</a><c:if test="${not status.last}">, </c:if>
                            </c:forEach>
                        </td>
                        <td>${item.title}</td>
                        <td>${item.musicBy}</td>
                        <td><fmt:formatDate value="${item.date}" pattern="dd/MM/yyyy"/></td>
                        <td>
                            <c:forEach items="${item.venues}" var="venue" varStatus="status">
                                <a href="/dc/report/venue/${venue}">${venue}</a><c:if test="${not status.last}">, </c:if>
                            </c:forEach>
                        </td>
                        <td>${item.quality}</td>
                        <td>${item.language}</td>
                        <td>${item.subtitles}</td>
                        <td>${item.format}</td>
                        <td>
                            <c:if test="${not empty item.imdb}">
                                <a href="${item.imdb}">IMDB</a>
                            </c:if>
                        </td>
                        <td><c:if test="${item.seenByFede}"><strong>x</strong></c:if></td>
                        <td><c:if test="${item.seenByAnaMaria}"><strong>x</strong></c:if></td>
                        </tr>
                </c:forEach>
            </tbody>
        </table>

    </body>
</html>
