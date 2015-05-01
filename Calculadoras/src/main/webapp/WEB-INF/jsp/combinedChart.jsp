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
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html>
    <head profile="http://www.w3.org/25/10/profile">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
        <link rel="icon" 
              type="image/png" 
              href="/images/favicon.png" />
        <title>${title}</title>
        <link rel="stylesheet" type="text/css" href="/styles/style.css" />
        <%--script type="text/javascript" src="//ajax.googleapis.com/ajax/libs/mootools/1.5.1/mootools-yui-compressed.js"></script--%>
        <%--script type="text/javascript" src="/scripts/canvasjs.min.js"></script--%>
        <script type="text/javascript" src="/scripts/all.js"></script>
        <script type="text/javascript">

            window.addEvent('domready', function () {

                showChart('/secure/${uri}.json', 
                          'months=' + ${dto.months}
                        + '&year=' + ${dto.year}
                        + '&month=' + ${dto.month},
                'chartContainer');

            });

            function reloadChart() {
                document.id('chartContainer').empty();
                if (document.id('months')) {
                    var m = document.id('months').value;
                }

                var pn = document.id('pn').checked;
                var pr = document.id('pr').checked;
                var dn = document.id('dn').checked;
                var dr = document.id('dr').checked;
                var year = document.id('year').value;
                var month = document.id('month').value;
                showChart('/secure/${uri}.json',
                        (m ? 'months=' + m : '')
                        + '&pn=' + pn
                        + '&pr=' + pr
                        + '&dn=' + dn
                        + '&dr=' + dr
                        + '&year=' + year
                        + '&month=' + month
                        ,
                        'chartContainer');
            }

        </script>
    </head>
    <body>
        <%@include file="../jspf/menu.jspf" %>
        <h1>${title}</h1>
        <form:form modelAttribute="dto">
            <c:if test="${not empty monthlyPeriods}">
                <label for="months">Aplicar promedio </label>
                <form:select id="months" path="months" onchange="reloadChart()">
                    <c:forEach items="${monthlyPeriods}" var="p">
                        <form:option value="${p.value}">${p.key}</form:option>
                    </c:forEach>
                </form:select><br/>



            </c:if>

            <label for="pn">Pesos Nominales</label><form:checkbox id="pn" path="pn" onchange="reloadChart()"/><br/>
            <label for="pr">Pesos Reales</label><form:checkbox id="pr" path="pr" onchange="reloadChart()"/><br/>
            <label for="dn">Dólares Nominales</label><form:checkbox id="dn" path="dn" onchange="reloadChart()"/><br/>
            <label for="dr">Dólares Reales</label><form:checkbox id="dr" path="dr" onchange="reloadChart()"/>


            <p>En valores de 
            <form:select path="month" onchange="reloadChart()" id="month">
                <form:options items="${dto.limit.months}" itemLabel="name" itemValue="id"/>
            </form:select> de 
            <form:select path="year" onchange="reloadChart()"  id="year">
                <c:forEach begin="${dto.limit.yearFrom}" end="${dto.limit.yearTo}" var="year">
                    <form:option label="${year}" value="${year}"/></c:forEach>
            </form:select></p>

        </form:form>
        <div id="chartContainer" style="height:600px;"></div>
    </body>
</html>
