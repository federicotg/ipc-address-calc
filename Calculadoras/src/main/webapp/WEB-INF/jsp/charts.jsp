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
<html lang="es">
    <head profile="http://www.w3.org/25/10/profile">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
        <link rel="icon" 
              type="image/png" 
              href="/images/favicon.png" />
        <title>Visualización de Datos</title>
        <link rel="stylesheet" type="text/css" href="/styles/style.css" />
    </head>
    <body>
        <%@include file="../jspf/menu.jspf" %>
        <h1>Visualización de Datos</h1>
        <ul>
            <li><a href="unlp">UNLP</a></li>
            <li><a href="lifia">LIFIA</a></li>
            <li><a href="interest">Plazo Fijo</a></li>
            <li><a href="lifiaAndUnlp">LIFIA + UNLP</a></li>
            <li><a href="lifiaUnlpAndInterest">LIFIA + UNLP + Plazo Fijo</a></li>
            <li><a href="savings">Ahorros</a></li>
            <li><a href="savingsDetailed">Ahorros Detallados</a></li>
            <li><a href="savedSalaries">Sueldos Ahorrados</a></li>
            <li><a href="goldSavings">Oro</a></li>
            <li><a href="incomes">Ingresos</a></li>
            <li><a href="expenses">Gastos</a></li>
            <li><a href="expensesPercent">Gastos / Ingresos</a></li>
            <li><a href="consortiumExpenses">Gastos del Consorcio</a></li>
        </ul>
        <h1>Reportes</h1>
        <ul>
            <li><a href="investment/dollar">Dólar</a></li>
            <li><a href="investment/savings">Ahorros</a></li>
        </ul>
    </body>
</html>
