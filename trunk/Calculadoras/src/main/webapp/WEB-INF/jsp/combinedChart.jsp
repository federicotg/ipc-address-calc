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

                showChart('/secure/${uri}.json', 'months=6', 'combined');

            });

            function reloadChart() {
                document.id('combined').empty();
                var m = document.id('months').value;
                var pn = document.id('pn').checked;
                var pr = document.id('pr').checked;
                var dn = document.id('dn').checked;
                var dr = document.id('dr').checked;
                showChart('/secure/${uri}.json',
                        'months=' + m
                        + '&pn=' + pn
                        + '&pr=' + pr
                        + '&dn=' + dn
                        + '&dr=' + dr, 
                'combined');
            }

        </script>
    </head>
    <body>
        <%@include file="../jspf/menu.jspf" %>
        <h1>${title}</h1>
        <label for="months">Promediar</label>
        <select id="months" onchange="reloadChart()">
            <option value="1">1</option>
            <option value="6" selected="selected">6</option>
            <option value="12">12</option>
        </select><label for="months">meses.</label><br/>
        <label for="pn">Pesos Nominales</label><input type="checkbox" id="pn" onchange="reloadChart()"/><br/>
        <label for="pr">Pesos Reales</label><input type="checkbox" id="pr" checked onchange="reloadChart()"/><br/>
        <label for="dn">Dólares Nominales</label><input type="checkbox" id="dn" checked onchange="reloadChart()"/><br/>
        <label for="dr">Dólares Reales</label><input type="checkbox" id="dr" checked onchange="reloadChart()"/>
        <div id="combined" style="height:500px;"></div>


    </body>
</html>
