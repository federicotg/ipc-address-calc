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
    <head profile="http://www.w3.org/2005/10/profile">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
        <link rel="icon" 
              type="image/png" 
              href="/images/favicon.png" />
        <title>Calculadora de Inflación</title>
        <link rel="stylesheet" type="text/css" href="/styles/style.css"/>
        <style type="text/css">
            input[type="text"]{
                width:8em;
            }
            input[type="text"], select, input[type="button"] {
                font-size: 130%;
            }
            section h2{
                text-transform: capitalize;
            }
            span{
                width:2em;
                text-align: right;
                display: inline-block;
            }
            div{
                margin-bottom: 0.4em;
            }
            div.margined{
                margin-left: 2.2em;
            }
            
        </style>
        <script type="text/javascript" src="//ajax.googleapis.com/ajax/libs/mootools/1.5.1/mootools-yui-compressed.js"></script>
        <script type="text/javascript" src="/scripts/accounting.min.js"></script>
        <script type="text/javascript">
            window.addEvent('domready', function () {
                var req = new Request.JSON({
                    url: 'money.json',
                    method: 'post',
                    headers: {'Content-Type': 'application/json;charset=UTF-8'},
                    urlEncoded: false,
                    onSuccess: function (money, responseText) {
                        var str = accounting.formatMoney(money.amount, money.toCurrencySymbol + " ", 2, ".", ",");
                        document.id('result' + money.currency.iso4217).value = str;
                    },
                    onError: function (text, error) {
                        alert(text + " - " + error);
                    },
                    onFailure: function () {
                        alert("El monto o las fechas no son válidos. Vuelva a intentar.");
                    }
                });
                var buttonAction = function (form) {
                    form.elements["result"+form.elements["fromIso4217"].value].value ="";
                    req.post(
                            JSON.stringify({
                                'amount': form.elements["amount"].value,
                                'fromYear': form.elements["fromYear"].value,
                                'fromMonth': form.elements["fromMonth"].value,
                                'toYear': form.elements["toYear"].value,
                                'toMonth': form.elements["toMonth"].value,
                                'currency':{'iso4217': form.elements["fromIso4217"].value}
                            }));
                };
                var usdAction = function () {
                    buttonAction(document.id("formUSD"));
                };
                var arsAction = function () {
                    buttonAction(document.id("formARS"));
                };
                document.id('buttonUSD').addEvent('click', usdAction);
                document.id('buttonARS').addEvent('click', arsAction);
                document.id('amountUSD').addEvent('keypress', function (e) {
                    if (e.key === 'enter') {
                        usdAction();
                    }
                });
                document.id('amountARS').addEvent('keypress', function (e) {
                    if (e.key === 'enter') {
                        arsAction();
                    }
                });
            });
        </script>
    </head>
    <body>


    <section id="main">
        <%@include file="../jspf/menu.jspf" %>
        <h1>Calculadora de Inflación</h1>
        <c:forEach items="${limits}" var="limit">
            <section>
                <h2>IPC ${limit.currency.name}</h2>
                <form:form modelAttribute="dto" id="form${limit.currency.iso4217}">
                    <div>
                        <input type="hidden" name="fromIso4217" value="${limit.currency.iso4217}"/>
                        <span>${limit.currency.symbol}</span>
                        <form:input path="amount" value="${moneyAmount}" id="amount${limit.currency.iso4217}" />  en 
                        <form:select path="fromMonth" >
                            <form:options items="${limit.months}" itemLabel="name" itemValue="id"/>
                        </form:select> de 
                        <form:select path="fromYear" >
                            <c:forEach begin="${limit.yearFrom}" end="${limit.yearTo}" var="year">
                                <form:option label="${year}" value="${year}"/></c:forEach>
                        </form:select>
                    </div>
                    <div class="margined">
                        tienen el mismo poder de compra que 
                    </div>
                    <div class="margined">
                        <input type="text" readonly="readonly" id="result${limit.currency.iso4217}"/>
                        en 
                        <form:select path="toMonth" >
                            <form:options items="${limit.months}" itemLabel="name" itemValue="id"/>
                        </form:select> de 
                        <form:select path="toYear" >
                            <c:forEach begin="${limit.yearFrom}" end="${limit.yearTo}" var="year">
                                <form:option label="${year}" value="${year}"/></c:forEach>
                        </form:select>
                    </div>
                    <div class="margined">
                        <input type="button" value="Calcular" id="button${limit.currency.iso4217}"/>
                    </div>
                </form:form>
            </section>
        </c:forEach>
        <section id="sources">
            <h3>Fuentes</h3>
            <ul>
                <li><a href="http://www.bls.gov/">Bureau of Labor Statistics of the U.S. Department of Labor</a>.</li>
                <li>Instituto Nacional de Estadísticas y Censos (INDEC): <a href="http://www.indec.mecon.ar/nuevaweb/cuadros/10/ipc-var-dde1943.xls">1943-2006</a>.</li>
                <li>IPCs provinciales <a href="http://elhombrecitodelsombrerogris.blogspot.com.ar/2010/12/nuevo-indice-de-precios-ipc-cqp.html">2007-2014</a>.</li>
                <li>Proyecto <a href="https://code.google.com/p/ipc-address-calc/">ipc-address-calc</a>.</li>
            </ul>
        </section>
    </section>

</body>
</html>
