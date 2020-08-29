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
<html lang="es">
    <head profile="http://www.w3.org/25/10/profile">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
        <link rel="icon" 
              type="image/png" 
              href="/images/favicon.png" />
        <title>${title}</title>
        <link rel="stylesheet" type="text/css" href="/styles/style.css" />
        <script type="text/javascript" src="/scripts/accounting.js"></script><script type="text/javascript" src="/scripts/canvasjs.min.js"></script><script type="text/javascript" src="/scripts/charts.js"></script>
        <script type="text/javascript">

            window.onload = function () {
                reloadChart();
            };

            function reloadChart() {
                var node = document.getElementById('chartContainer');

                while (node.firstChild) {
                    node.removeChild(node.firstChild);
                }

                if (document.getElementById('months')) {
                    var m = document.getElementById('months').value;
                }

                var pn = document.getElementById('pn').checked;
                var pr = document.getElementById('pr').checked;
                var dn = document.getElementById('dn').checked;
                var dr = document.getElementById('dr').checked;
                var en = document.getElementById('en').checked;
                var er = document.getElementById('er').checked;
                var year = document.getElementById('year').value;
                var month = document.getElementById('month').value;

                var seriesCheckboxes = document.querySelectorAll(".seriesCbox");
                var selectedSeries = null;
                for (var i = 0; i < seriesCheckboxes.length; i++) {
                    if (seriesCheckboxes[i].checked) {
                        if (selectedSeries === null) {
                            selectedSeries = seriesCheckboxes[i].value;
                        } else {
                            selectedSeries += ("," + seriesCheckboxes[i].value);
                        }
                    }
                }


                showChart('/secure/${uri}.json',
                        (m ? 'months=' + m : '')
                        + '&pn=' + pn
                        + '&pr=' + pr
                        + '&dn=' + dn
                        + '&dr=' + dr
                        + '&en=' + en
                        + '&er=' + er
                        + '&year=' + year
                        + '&month=' + month
                        + '&series=' + selectedSeries
                        + '&chartType=line',
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


            <c:if test="${not empty stackedSeries}">
                <fieldset>
                    <c:forEach items="${stackedSeries}" var="s" varStatus="status">
                        <form:checkbox id="label${status.index}" value="${s}" class="seriesCbox" path="series" onchange="reloadChart()"/><label for="label${status.index}">${s}</label>
                    </c:forEach>
                </fieldset>
            </c:if>
            <fieldset>
                <form:checkbox id="pn" path="pn" onchange="reloadChart()"/><label for="pn">Pesos Nominales</label>
                <form:checkbox id="pr" path="pr" onchange="reloadChart()"/><label for="pr">Pesos Reales</label>
                <form:checkbox id="dn" path="dn" onchange="reloadChart()"/><label for="dn">Dólares Nominales</label>
                <form:checkbox id="dr" path="dr" onchange="reloadChart()"/><label for="dr">Dólares Reales</label>
                <form:checkbox id="en" path="en" onchange="reloadChart()"/><label for="en">Euros Nominales</label>
                <form:checkbox id="er" path="er" onchange="reloadChart()"/><label for="er">Euros Reales</label>
            </fieldset>
            <p>En valores de 
                <form:select path="month" onchange="reloadChart()" id="month">
                    <form:options items="${dto.limit.months}" itemLabel="name" itemValue="id"/>
                </form:select> de 
                <form:select path="year" onchange="reloadChart()"  id="year">
                    <c:forEach begin="${dto.limit.yearFrom}" end="${dto.limit.yearTo}" var="year">
                        <form:option label="${year}" value="${year}"/></c:forEach>
                </form:select></p>

        </form:form>
        <div id="chartContainer" style="height:900px;"></div>
    </body>
</html>
