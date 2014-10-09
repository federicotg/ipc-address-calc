/* 
 * Copyright (C) 2014 fede
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

var reviver = function (key, value) {
    var a;
    if (typeof value === 'string') {
        a = /^date-(\d+)-(\d+)-(\d+)$/.exec(value);
        if (a) {
            return new Date(a[1], a[2], a[3]);
        }
    }
    return value;
};


function showChart(url, params, container) {
    var req = new Request({
        url: url,
        method: 'get',
        headers: {'Content-Type': 'application/json;charset=UTF-8'},
        urlEncoded: false,
        onSuccess: function (responseText) {
            if (this.status === 200) {
                var chartObject = JSON.parse(responseText, reviver);
                if (chartObject.successful) {
                    var chart = new CanvasJS.Chart(container, chartObject);
                    chart.render();
                }
            }
        },
        onFailure: function () {
            alert("Failure.");
        },
        onException: function () {
            alert("Exception");
        }
    });
    if (params) {
        req.send(params);
    } else {
        req.send();
    }

}
