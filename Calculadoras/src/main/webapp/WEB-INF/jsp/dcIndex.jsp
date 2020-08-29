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
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
        <link rel="icon" 
              type="image/png" 
              href="/images/favicon.png" />
        <link rel="stylesheet" type="text/css" href="/styles/style.css" />
        <title>Catálogo de Contenidos Digitales</title>
    </head>
    <body>
        <%@include file="../jspf/menu.jspf" %>
        <h1>Catálogo de Contenidos Digitales</h1>
        <ul>
            <li><a href="report">Reporte</a></li>
            <li><a href="boxes">Cajas</a></li>
            <li><a href="venueMap">Lugares</a></li>
            <li><a href="unseen">No vistas</a></li>
            <li><a href="notInHD">No en HD</a></li>
        </ul>
    </body>
</html>
