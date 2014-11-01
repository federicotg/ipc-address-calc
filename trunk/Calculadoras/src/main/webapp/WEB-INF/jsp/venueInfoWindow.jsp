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
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<div id="content">
    <h1>${detail.name}</h1>
    <p>Más información en <a href="${detail.wikipedia}">Wikipedia</a></p>
    <ul>
        <c:forEach items="${detail.performances}" var="perf">
            <c:choose >
                <c:when test="${not empty perf.imdb}">
                    <li><a href="${perf.imdb}">${perf.opusTitle} ${perf.year}</a></li>
                    </c:when>
                    <c:otherwise>
                    <li>${perf.opusTitle} ${perf.year}</li>
                    </c:otherwise>
                </c:choose>
            </c:forEach>
    </ul>
</div>
