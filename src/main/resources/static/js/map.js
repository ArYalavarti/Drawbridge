/**
 * Set up global variables for use in all map actions.
 */
let map;

/**
 * Hardcoded starting location - will replace later.
 */
let curLat = 42.358188;
let curLong = -71.058502;

let found = [];
let coordinates = [];
let markers = [];

let addressNames = [];
let route = [];

let tooltips = []

/**
 * When the DOM loads, initialize Mapbox and the Map object.
 */
$(document).ready(function () {
    initMapbox();
    initMap();
    initDateTime();
    initTooltips();
    disableTrip();
    console.log("DOM ready.");
});

function onUserSignedIn() {
    console.log("User signed in.");
}

function onUserSignedOut() {
    console.log("User signed out.");
}

/**
 * Initializes the Map.
 */
function initMap() {
    // Create map object with custom settings and add NavigationControl
    map = new mapboxgl.Map({
        container: "map",
        keyboard: false,
        maxZoom: 18,
        style: "mapbox://styles/mapbox/streets-v11",
        center: [curLong, curLat],
        zoom: 12
    });
    map.addControl(new mapboxgl.NavigationControl());

    /**
     * Hide loading gif and show all initially hidden objects
     */
    $("#loading").css({
        visibility: "hidden"
    });
    $("[id=pre-load]").css({
        visibility: "visible"
    });
    console.log("Map loaded.");
}

/**
 * Initialize date and time pickers.
 */
function initDateTime() {
    flatpickr("#date", {
        minDate: "today",
        altInput: true,
        dateFormat: "m/d/Y"
    });
    flatpickr("#time", {
        enableTime: true,
        noCalendar: true,
        altInput: true,
        dateFormat: "H:i"
    });
}

function initTooltips() {
    tooltips[0] = tippy("#requiredTooltip", {
        animation: "scale",
        arrow: true,
        arrowType: "round",
        theme: "drawbridge",
        interactive: false,
        trigger: "manual",
        hideOnClick: false,
        inertia: true,
        sticky: true,
        placement: "top",
    });
}

/**
 * Handle changes to the address input boxes whenever the input box loses focus.
 *
 * @param {*} id
 *            The name of the input box (either 'start-input' or 'end-input')
 * @param {*} index
 *            The index to use for identification (either 0 or 1)
 */
function handleInput(id, index) {
    // Get the address value from the correct input box
    let address = $(`#${id}`).val();
    if (address === "") {
        removeMarker(index);
        disableTrip();
        return;
    }

    $(`#loading-${id}`).css({
        visibility: "visible"
    });

    setTimeout(function () {
        // Send network request for geocoding based on address box value
        mapboxClient.geocoding
            .forwardGeocode({
                query: address,
                proximity: [curLong, curLat],
                autocomplete: true,
                limit: 1
            })
            .send()
            .then(function (response) {
                // If valid response
                if (response && response.body && response.body.features && response.body.features.length) {
                    /**
                     * Get the first element of the suggestions, set the input box to that
                     * value, then update the addressNames and coordinates arrays with the
                     * feature data.
                     * */
                    let feature = response.body.features[0];
                    $(`#${id}`).val(feature.place_name);
                    coordinates[index] = feature.center;
                    // Add new marker on the map with the returned feature data
                    addStreetPoint(feature.center[1], feature.center[0], id, index, feature.place_name);
                }
                $(`#loading-${id}`).css({
                    visibility: "hidden"
                });
            });
    }, 800);
}

/**
 * Add a new street point on the map after a new address is inputted.
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
 */
function addStreetPoint(lat, long, id, index, name) {
    /**
     * Add a new marker on the map then mark the associated input type as found
     */
    addMarker(lat, long, id, index, name, map);
    found[index] = true;

    /**
     * If both are found, align the map view based on the trip coordinates. Otherwise,
     * move to the location of the given address.
     */
    if (found[0] && found[1]) {
        alignTrip();
        updateRoute();
        enableTrip();
    } else {
        moveToLocation(lat, long);
    }
}

/**
 * Removes a marker from the map given index to remove.
 *
 * @param {*} index
 *            Index to remove
 */
function removeMarker(index) {
    if (markers[index]) {
        markers[index].remove();
        delete markers[index];
        delete coordinates[index];
        route = [];
        removeRoute();
        found[index] = false;
    }
}

/**
 * Updates the route visualization on the map.
 */
function updateRoute() {
    removeRoute();
    calcRoute(coordinates.join(";"));
}

/**
 * Removes the route visualization from the map.
 */
function removeRoute() {
    if (map.getSource("route")) {
        map.removeLayer("route");
        map.removeSource("route");
    } else {
        return;
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
        setTripInfo();

        let coords = jsonResponse.routes[0].geometry;
        addRoute(coords, map);
    };
    req.send();
}

/**
 * Disable the trip realign button and hide route information modal
 */
function disableTrip() {
    $(".trip-setting").css({
        background: "#a5a5a5",
        cursor: "auto"
    });
    $("#car-icon").attr("src", "/images/car-disabled.png");
    $("#route-info").css({
        visibility: "hidden"
    });
}

/**
 * Enable the trip realign button and show route information modal
 */
function enableTrip() {
    $(".trip-setting").css({
        background: "#fff",
        cursor: "pointer"
    });
    $("#car-icon").attr("src", "/images/car-icon.png");
    $("#route-info").css({
        visibility: "visible"
    });
}

/**
 * Sets the trip info test boxes to the appropriate distance and duration
 */
function setTripInfo() {
    $("#distance").text(`${((route[0]) * 0.621371).toFixed(2)} mi`);
    $("#duration").text(`${route[1].toFixed(0)} min`);
}

/**
 * Centers the map view.
 */
function centerMap() {
    map.flyTo({
        center: [curLong, curLat],
        pitch: 0,
        bearing: 0,
        zoom: 12
    });
}

/**
 * Realigns the map view to north and 0 tilt.
 */
function alignMap() {
    map.flyTo({
        pitch: 0,
        bearing: 0
    });
}

/**
 * Moves the map view to a new center location with the
 * provided arguments.
 *
 * @param {*} lat
 * @param {*} lng
 */
function moveToLocation(lat, lng) {
    map.flyTo({
        center: [lng, lat],
        zoom: 12
    });
}

/**
 * Realigns the map view based on the current trip coordinates.
 */
function alignTrip() {
    if (found[0] && found[1]) {
        let top = 400;
        let left = 250;
        let right = 150;
        let bottom = 150;

        /**
         * Checks if the trip positions are routed diagonally upwards or if the window
         * height is below a threshold. Then the map view is adjusted since the search
         * menu will not cover up the trip.
         */
        if (
            (coordinates[0][0] < coordinates[1][0] && coordinates[0][1] < coordinates[1][1]) ||
            (coordinates[0][0] > coordinates[1][0] && coordinates[0][1] > coordinates[1][1]) ||
            $(window).height() < 600
        ) {
            top = 150;
        }

        /**
         * Checks if in half screen vertical mode and adjust map view.
         */
        if ($(window).height() < 600 && $(window).width() >= 767) {
            right = 50;
            bottom = 50;
        }

        /**
         * Checks if the window width is below a threshold. Then the map view is adjusted
         * since the search menu will not cover up the trip.
         */
        if ($(window).width() < 767) {
            top = 100;
            left = 75;
            right = 75;
        }

        // Fits the bounds of the map to the given padding sizes.
        map.fitBounds(coordinates, {
            padding: {
                top: top,
                bottom: bottom,
                left: left,
                right: right
            },
            linear: false
        });
    }
}

/**
 * Handle submit response when the submit button is pressed.
 */
function handleSubmit() {
    let dateInput = $("#date").val();
    let timeInput = $("#time").val();
    let date = new Date(`${dateInput} ${timeInput}`);

    if (dateInput === "" || timeInput === "" || coordinates[0] === undefined || coordinates[1] === undefined) {
        tooltips[0][0].show();
        setTimeout(function () {
            tooltips[0][0].hide();
        }, 3000);
    } else {
        let userID;
        if (userProfile === undefined) {
            userID = null;
        } else {
            userID = userProfile.getId();
        }

        const postParameters = {
            startName: addressNames[0],
            endName: addressNames[1],
            startCoordinates: coordinates[0].slice(0).reverse(),
            endCoordinates: coordinates[1].slice(0).reverse(),
            date: date.getTime(),
            userID: userID
        };
        console.log(postParameters);
    }
}