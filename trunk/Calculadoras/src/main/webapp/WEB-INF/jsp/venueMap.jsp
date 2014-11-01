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
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
        <link rel="icon" 
              type="image/png" 
              href="/images/favicon.png" />
        <link rel="stylesheet" type="text/css" href="/styles/style.css" />
        <script type="text/javascript" src="//ajax.googleapis.com/ajax/libs/mootools/1.5.1/mootools-yui-compressed.js"></script>
        <style>
            html, body, #map-canvas {
                height: 100%;
                margin: 0px;
                padding: 0px
            }
        </style>
        <script src="https://maps.googleapis.com/maps/api/js?v=3.exp"></script>

        <script>

            function getContent(venueName) {
                var req = new Request({
                    url: 'venueInfoWindow?name=' + venueName,
                    method: 'get',
                    async: false,
                    headers: {'Content-Type': 'application/json;charset=UTF-8'},
                    urlEncoded: false,
                    onSuccess: function (responseText) {
                        if (this.status === 200) {
                            this.html = responseText;
                        }
                    }
                });
                req.send();
                return req.html;
            }

            function placeMarkers(map, venues) {

                var infowindow = new google.maps.InfoWindow({
                    content: 'this.title'
                });

                for (var i = 0; i < venues.length; i++) {
                    var v = venues[i];

                    var marker = new google.maps.Marker({
                        position: new google.maps.LatLng(v.latLon.lat, v.latLon.lon),
                        map: map,
                        title: v.name
                    });
                    google.maps.event.addListener(marker, 'click', function () {
                        infowindow.content = getContent(this.title);
                        infowindow.open(map, this);
                    });
                }
            }

            function initialize() {

                var mapOptions = {
                    zoom: 3,
                    center: new google.maps.LatLng(0, 0)
                };

                var map = new google.maps.Map(document.id('map-canvas'), mapOptions);

                var req = new Request({
                    url: 'venues.json',
                    method: 'get',
                    headers: {'Content-Type': 'application/json;charset=UTF-8'},
                    urlEncoded: false,
                    onSuccess: function (responseText) {
                        if (this.status === 200) {
                            var venues = JSON.parse(responseText);
                            placeMarkers(map, venues);
                        }
                    },
                    onFailure: function () {
                        alert("Failure.");
                    },
                    onException: function () {
                        alert("Exception");
                    }
                });
                req.send();
            }

            google.maps.event.addDomListener(window, 'load', initialize);

        </script>
        <title>Lugares</title>
    </head>
    <body>
        <%@include file="../jspf/menu.jspf" %>
        <h1>Lugares</h1>

        <div id="map-canvas"></div>

    </body>
</html>
