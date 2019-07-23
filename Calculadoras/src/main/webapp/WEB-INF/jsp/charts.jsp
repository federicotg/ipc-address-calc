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
        <link rel="stylesheet" type="text/css" href="/styles/style.min.css" />
    </head>
    <body>
        <%@include file="../jspf/menu.jspf" %>
        <h1>Visualización de Datos</h1>
        <ul>
            <li>Ingresos: <a href="lifiaUnlpAndInterest">Combinados</a> | <a href="incomes">Segregados</a></li>
            <li>Ahorros: <a href="savings">Combinados</a> | <a href="savingsDetailed">Detallados</a> | <a href="investment/savings">Tabla</a> | <a href="savedSalaries">Sueldos Ahorrados</a> | <a href="goldSavings">Oro</a></li>
            <li>Gastos: <a href="expenses">Propios</a> | <a href="consortiumExpenses">Del Consorcio</a> | <a href="expensesPercent">Gastos / Ingresos</a></li>
            <li><a href="savingsAndIncomeChange">Cambio Anual de Ingresos y Ahorros</a></li>
            <li>Inversiones <a href="investment/current">Actuales</a> | <a href="investment/past">Pasadas</a></li>
        </ul>
    </body>
</html>
