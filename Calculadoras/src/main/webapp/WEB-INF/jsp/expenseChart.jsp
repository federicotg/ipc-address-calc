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
        <link rel="stylesheet" type="text/css" href="/styles/style.min.css" />
        <script type="text/javascript" src="/scripts/all.min.js"></script>
        <script type="text/javascript">

            window.onload = function() {

                reloadChart();
                document.getElementById('changeAll').onchange = function(){
                    
                var state = document.getElementById('changeAll').checked;
                var checkboxes = document.getElementsByClassName("chbx");
                for(var i=0;i<checkboxes.length;i++){
                        checkboxes[i].checked=state;
                }
                reloadChart();    
                };

            };

            function reloadChart() {
                var node = document.getElementById('chartContainer');
                
                while (node.firstChild) {
                    node.removeChild(node.firstChild);
                }

                var checkboxes = document.querySelectorAll('input[type=checkbox]');
                var series = "";
                for (var i = 0; i < checkboxes.length; i++) {
                    if (checkboxes[i].checked) {
                        series += "&series=" + checkboxes[i].value;
                    }
                }
                var m = document.getElementById('months').value;
                var year = document.getElementById('year').value;
                var month = document.getElementById('month').value;
                showChart('/secure/${uri}.json', 'months=' + m + series+"&year="+year+"&month="+month, 'chartContainer');
            }
 
        </script>
    </head>
    <body>
        <%@include file="../jspf/menu.jspf" %>
        <h1>${title}</h1>
        <form:form modelAttribute="dto">
            <c:if test="${not empty monthlyPeriods}">
                <input type="checkbox" id="changeAll" checked="checked" /><label for="changeAll">Todos</label>
                <form:checkboxes items="${series}" path="series" itemValue="name" cssClass="chbx" itemLabel="name" onchange="reloadChart()" /><br/>
                <label for="months">Aplicar promedio </label>

                <form:select id="months" path="months" onchange="reloadChart()">
                    <c:forEach items="${monthlyPeriods}" var="p">
                        <form:option value="${p.value}">${p.key}</form:option>
                    </c:forEach>
                </form:select><br/>
            </c:if>

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
