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
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE html>
<html lang="es">
    <head profile="http://www.w3.org/2005/10/profile">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
        <security:csrfMetaTags />
        <link rel="icon" 
              type="image/png" 
              href="/images/favicon.png" />
        <title>¿Entre Qué y Qué?</title>
        <link rel="stylesheet" type="text/css" href="/styles/style.css"/>
        <style type="text/css">
            .group{
                border-color: gray;
                border-style: solid;
                border-width: 1px;
                margin-bottom: 1em;
                padding: 0.3em;
                min-height: 1.5em;
                border-radius: 0.3em;
            }
            .group div{
                margin-bottom: 0.3em;
            }
            input[type="text"], input[type="checkbox"]{
                background-color: cornsilk;
                border-radius: 0.3em;
                border-style:solid;
                border-color:lightgray;
            }
            label, input, #result{
                font-size: 130%;
            }
            label{
                padding: 0.5em;
                margin-top: 0.5em;
            }
            input#calle, input#diagonal{
                width:2.5em;
            }
            input#numeroCalle, input#numeroDiag{
                width:3em;
            }
        </style>
        <script type="text/javascript" src="//ajax.googleapis.com/ajax/libs/mootools/1.5.1/mootools-yui-compressed.js"></script>
        <script type="text/javascript">
            window.addEvent('domready', function () {

                var processingImage = new Image();
                processingImage.src = "/images/loadingAnimation.gif";

                var showResult = function (message, container) {
                    container.empty();
                    container.appendChild(document.createTextNode(message));
                };
                var showProcessingAnimation = function () {
                    var container = document.id('result');
                    container.empty();
                    container.appendChild(processingImage);
                };

                var req = new Request.JSON({
                    url: 'address.json',
                    method: 'post',
                    headers: {'Content-Type': 'application/json;charset=UTF-8'},
                    urlEncoded: false,
                    onSuccess: function (address, responseText) {
                        var res = document.id('result');
                        if (address.valid) {
                            showResult(
                                    address.street
                                    + ' número '
                                    + address.number
                                    + ' entre '
                                    + address.lowerBoundary
                                    + ' y '
                                    + address.upperBoundary, res);
                        } else {
                            showResult(address.message, res);
                        }
                    },
                    onError: function (text, error) {
                        showResult(text + ' - ' + error, document.id('result'));
                    }
                });
                
                
                var csrfParameter = $$("meta[name='_csrf_parameter']").get("content")[0];
                var csrfHeader = $$("meta[name='_csrf_header']").get("content")[0];
                var csrfToken = $$("meta[name='" + csrfParameter + "']").get("content")[0];
                req.setHeader(csrfHeader, csrfToken);

                var streetAction = function (event) {
                    event.stop();
                    showProcessingAnimation();
                    req.post(JSON.stringify(
                            {
                                'originalText': (document.id('tolosa').checked ? 't' : '')
                                        + document.id('calle').value
                                        + ' '
                                        + document.id('numeroCalle').value
                            }));
                };

                var streetEnterHandler = function (e) {
                    if (e.key === 'enter') {
                        streetAction(e);
                    }
                };

                var diagAction = function (event) {
                    event.stop();
                    showProcessingAnimation();
                    req.post(JSON.stringify(
                            {
                                'originalText': 'd'
                                        + document.id('diagonal').value
                                        + ' '
                                        + document.id('numeroDiag').value
                            }));
                };
                var diagEnterHandler = function (e) {
                    if (e.key === 'enter') {
                        diagAction(e);
                    }
                };

                document.id('calleButton').addEvent('click', streetAction);
                document.id('numeroCalle').addEvent('keypress', streetEnterHandler);
                document.id('calle').addEvent('keypress', streetEnterHandler);
                document.id('diagButton').addEvent('click', diagAction);
                document.id('numeroDiag').addEvent('keypress', diagEnterHandler);
                document.id('diagonal').addEvent('keypress', diagEnterHandler);
            });
        </script>
    </head>
    <body>
        <section id="main">
            <%@include file="../jspf/menu.jspf" %>
            <h1>¿Entre Qué y Qué?</h1>
            <form>
                <section class="group">
                    <div>
                        <label for="calle">Calle o avenida</label> 
                        <input type="text" id="calle" placeholder="17"/> 
                        <label for="numeroCalle">número</label> 
                        <input type="text" id="numeroCalle"  placeholder="864"/> 
                        <input type="button" id="calleButton" value="Calcular"/>
                    </div>
                    <div>
                        <input type="checkbox" id="tolosa" />
                        <label for="tolosa">Seleccionar para direcciones en Tolosa o Ringuelet</label>
                    </div>

                </section>
                <section class="group">
                    <div>
                        <label for="diagonal">Diagonal</label>
                        <input type="text" id="diagonal" placeholder="79"/> 
                        <label for="numeroDiag" >número</label>
                        <input type="text" id="numeroDiag" placeholder="118"/> 
                        <input type="button" id="diagButton" value="Calcular"/>
                    </div>
                </section>
            </form>   
            <div id="result" class="group">
            </div>
            <section id="sources">
                <h3>Fuentes</h3>
                <ul>
                    <li>Proyecto <a href="https://code.google.com/p/ipc-address-calc/">ipc-address-calc</a>.</li>
                </ul>
            </section>
        </section>
    </body>
</html>
