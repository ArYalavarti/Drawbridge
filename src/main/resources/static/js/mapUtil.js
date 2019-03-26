let mapboxClient;

/**
 * Initializes the Mapbox using the accessToken and sets up the
 * mapboxClient for use in Geolocating.
 */
function initMapbox() {
    mapboxgl.accessToken =
        "pk.eyJ1IjoiYXJ2Mzk1IiwiYSI6ImNqdGpodWcwdDB6dXEzeXBrOHJyeGVpNm8ifQ.bAwH-KG_5A5kwIxCf6xCSQ";
    mapboxClient = mapboxSdk({
        accessToken: mapboxgl.accessToken
    });
}

/**
 * Add marker on the map with the given parameters.
 *
 * @param {*} lat
 *            The marker center latitude.
 * @param {*} long
 *            The marker center longitude.
 * @param {*} id
 *            The name of the input box (either 'start-input' or 'end-input')
 * @param {*} index
 *            The index to use for identification (either 0 or 1)
 * @param {*} name
 *            The name of the location
 * @param {*} map
 *            The mapbox object to manipulate
 */
function addMarker(lat, long, id, index, name, map) {
    let el = document.createElement("div");
    el.className = `marker ${id}`;
    if (id == "start-input") {
        el.className = el.className + " pulse";
    }
    if (markers[index]) {
        markers[index].remove();
    }
    let popup = new mapboxgl.Popup({
        offset: 25
    }).setHTML(parseAddress(name, index));

    markers[index] = new mapboxgl.Marker(el).setLngLat([long, lat]).setPopup(popup);
    markers[index].addTo(map);
}

/**
 * Parse an address and return formatted HTML code.
 *
 * @param {*} raw
 * @param {*} index
 */
function parseAddress(raw, index) {
    if (raw.indexOf(",") > -1) {
        let title = raw.substr(0, raw.indexOf(","));
        addressNames[index] = title;
        return `<div class="popup-title">${title}</div>
                <img src="/images/divider.png" style="height: 2px; width: auto;" />
                <div class="popup-content">${raw.substr(raw.indexOf(",") + 1)}</div>`;
    } else {
        addressNames[index] = raw;
        return `<div class="popup-title">${raw}</div>`;
    }
}

/**
 * Calculates the route direction coordinates based on starting and ending locations
 * using the Mapbox directions API.
 *
 * @param {*} c
 */
function calcRoute(c) {
    let url =
        "https://api.mapbox.com/directions/v5/mapbox/driving/" +
        c +
        "?geometries=geojson&&access_token=" +
        mapboxgl.accessToken;

    let req = new XMLHttpRequest();
    req.responseType = "json";
    req.open("GET", url, true);
    req.onload = function () {
        let jsonResponse = req.response;
        route = [jsonResponse.routes[0].distance * 0.001, jsonResponse.routes[0].duration / 60];

        let coords = jsonResponse.routes[0].geometry;
        addRoute(coords, map);
    };
    req.send();
}

/**
 * Adds the route visualization to the map based on the given set of coordinates.
 *
 * @param {*} coords
 * @param {*} map
 */
function addRoute(coords, map) {
    if (map.getSource("route")) {
        map.removeLayer("route");
        map.removeSource("route");
    } else {
        map.addLayer({
            id: "route",
            type: "line",
            source: {
                type: "geojson",
                data: {
                    type: "Feature",
                    properties: {},
                    geometry: coords
                }
            },
            layout: {
                "line-join": "round",
                "line-cap": "round"
            },
            paint: {
                "line-color": "#47A5FF",
                "line-width": 7,
                "line-opacity": 0.7
            }
        });
    }
}