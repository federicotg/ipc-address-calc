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
        <title>Gráficos</title>
        <link rel="stylesheet" type="text/css" href="/styles/style.css" />
        <script type="text/javascript" src="//ajax.googleapis.com/ajax/libs/mootools/1.5.1/mootools-yui-compressed.js"></script>
        <script type="text/javascript" src="/scripts/canvasjs.min.js"></script>
        <script type="text/javascript" src="/scripts/charts.js"></script>
        <script type="text/javascript">

            window.addEvent('domready', function () {
                showChart('historicDollarValue.json', 'year=1992&month=1', 'chartContainer');
                showChart('/secure/unlpRealPesos.json', null, 'pesos2014');
            });
        </script>
    </head>
    <body>
        <h1>Gráficos</h1>
        
        <div id="chartContainer" style="height:400px;"></div>
        
        <div id="pesos2014" style="height:400px;"></div>
    </body>
</html>
