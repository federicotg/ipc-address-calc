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
        <script type="text/javascript" src="//ajax.googleapis.com/ajax/libs/mootools/1.5.1/mootools-yui-compressed.js"></script>
        <script type="text/javascript" src="/scripts/canvasjs.min.js"></script>
        <script type="text/javascript" src="/scripts/charts.js"></script>
        <script type="text/javascript">

            window.addEvent('domready', function () {
                reloadChart();
            });

            function reloadChart() {
                document.id('chartContainer').empty();
                showChart('${uri}.json', null, 'chartContainer');
            }

        </script>
    </head>
    <body>
        <%@include file="../jspf/menu.jspf" %>
        <h1>${title}</h1>
        <div id="chartContainer" style="height:600px;"></div>
    </body>
</html>