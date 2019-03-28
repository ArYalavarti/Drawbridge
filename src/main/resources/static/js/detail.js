/**
 * Set up global variables for use in all map actions.
 */
let map;

let markers = [];
let addressNames = [];

let curLat = 42.358188;
let curLong = -71.058502;

/**
 * When the DOM is ready, show home and info buttons, initialize the
 * mapbox client, and initialize the map.
 */
$(document).ready(function() {
	showHomeInfo();
	initMapbox();
	initMap();
});

/**
 * Overriden function for user sign in action.
 */
function onUserSignedIn() {
	/**
	 * Hide the sign in tooltip if it is visible.
	 */
	console.log("User signed in.");
	signInTooltip[0].hide();
}

/**
 * Overriden function for user sign out action.
 */
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
		center: coordinates[0],
		zoom: 12,
		interactive: false
	});
	setRoute();
	console.log("Map loaded.");
}

/**
 * Sets the route on the map.
 */
function setRoute() {
	// Fit the map to the coordinates of the trip starting and ending coordinates
	map.fitBounds(coordinates, {
		padding: {
			top: 75,
			bottom: 100,
			left: 75,
			right: 75
		},
		linear: false
	});

	// Add the two markers and draw the route
	addMarker(coordinates[0][1], coordinates[0][0], "start-input", 0, startName, map);
	addMarker(coordinates[1][1], coordinates[1][0], "end-input", 1, endName, map);
	drawRoute(coordinates.join(";"));
}

/** Sets up the button click handlers */
function joinClick(tid) {
	if (userProfile == undefined) {
		$("html, body").animate(
			{
				scrollTop: 0
			},
			"slow"
		);
		signInTooltip[0].setContent("Sign in with your Google Account to join this trip.");
		signInTooltip[0].show();
	} else {
		const data = {
			action: "join",
			user: userProfile.getId()
		};
		sendRequest(data, "/trip/" + tid);
	}
}

function leaveClick(tid) {
	if (userProfile == undefined) {
		$("html, body").animate(
			{
				scrollTop: 0
			},
			"slow"
		);
		signInTooltip[0].setContent("Sign in with your Google Account to join this trip.");
		signInTooltip[0].show();
	} else {
		const data = {
			action: "leave",
			user: userProfile.getId()
		};
		sendRequest(data, "/trip/" + tid);
	}
}

function deleteClick(tid) {
	const data = {
		action: "delete"
	};
	sendRequest(data, "/trip/" + tid);
}

function approveClick(tid, pendUID) {
	const data = {
		action: "approve",
		user: pendUID
	};
	sendRequest(data, "/trip/" + tid);
}

function denyClick(tid, pendUID) {
	const data = {
		action: "deny",
		user: pendUID
	};
	sendRequest(data, "/trip/" + tid);
}

function sendRequest(data, url) {
	const req = new XMLHttpRequest();
	req.open("POST", url);
	req.send(data);
}

/**
 * Draws the route on the map
 * @param {*} c
 */
function drawRoute(c) {
	let url =
		"https://api.mapbox.com/directions/v5/mapbox/driving/" +
		c +
		"?geometries=geojson&&access_token=" +
		mapboxgl.accessToken;

	let req = new XMLHttpRequest();
	req.responseType = "json";
	req.open("GET", url, true);
	req.onload = function() {
		let jsonResponse = req.response;
		let coords = jsonResponse.routes[0].geometry;
		addRoute(coords, map);
	};
	req.send();
}