const FADE_SPEED = 200;

// Store user profile across JS files
let userProfile = undefined;

// Set up sign in tooltip for use across pages
let signInTooltip;
let infoTooltips;

/**
 * When the DOM loads, check for the logged in cookie.
 */
$(document).ready(function () {
    // Initialize tooltip
    initSignInTooltip();

    /**
     * If cookies are not enabled or the user is not signed in, display
     * the sign in button
     */
    if (navigator.cookieEnabled && getCookie("loggedIn") === "true") {
        $("#sign-in").css({visibility: "visible"});
    }
});

/**
 * Initializes the sign in tooltip below the sign in button.
 */
function initSignInTooltip() {
    signInTooltip = tippy("#sign-in", {
        animation: "scale",
        arrow: true,
        arrowType: "round",
        theme: "drawbridge",
        interactive: false,
        trigger: "manual",
        hideOnClick: false,
        maxWidth: 150,
        inertia: true,
        sticky: true,
        placement: "bottom",
    });
    infoTooltips = tippy(".fixed-controls", {
        animation: "scale",
        arrow: true,
        arrowType: "round",
        theme: "drawbridge-alt",
        interactive: "true",
        hideOnClick: true,
        inertia: true,
        sticky: true,
        placement: "top",
    });

}

/**
 * Gets the cookie value of a given attribute
 * @param {*} cname
 */
function getCookie(cname) {
    let name = cname + "=";
    let decodedCookie = decodeURIComponent(document.cookie);
    let ca = decodedCookie.split(';');
    for (let i = 0; i < ca.length; i++) {
        let c = ca[i];
        while (c.charAt(0) === ' ') {
            c = c.substring(1);
        }
        if (c.indexOf(name) === 0) {
            return c.substring(name.length, c.length);
        }
    }
    return "";
}

/**
 * Handles sign in errors.
 *
 * @param {*} error
 */
function onFailure(error) {
    console.log(error);
}

/**
 * Handles successful sign in requests.
 *
 * @param {*} googleUser
 */
function onSignIn(googleUser) {
    // Store userprofile in global variable
    userProfile = googleUser.getBasicProfile();
    document.cookie = "loggedIn=true; path=/";

    // Performs page specific actions after user has signed in
    onUserSignedIn();

    // Add profile picture
    $("#profile-picture-wrapper").prepend(
        $("<img>", {
            id: "profile-picture",
            src: `${userProfile.getImageUrl()}`,
            onerror: "this.onerror=null;this.src='/images/temp.png';"
        })
    );

    // Set user name
    $("#user-name").text(userProfile.getGivenName());

    // Hide the sign in button and show the profile info button
    $("#profile-info").css({visibility: "visible"});
    $("#sign-in").css({visibility: "hidden "});

    /**
     * TODO add post request to server to create new user account if one
     * does not already exist in the database
     */
}

/**
 * Sign out the user.
 */
function signOut() {
    let auth2 = gapi.auth2.getAuthInstance();
    auth2.signOut().then(function () {
        // Reset userProfile variable
        userProfile = undefined;
        document.cookie = "loggedIn=false; path=/";

        // Performs page specific actions after user has signed out
        onUserSignedOut();

        // Hide profile info dropdown and show login button
        $("#profile-info").css({visibility: "hidden"});
        $("#sign-in").css({visibility: "visible"});
    });
}

/**
 * Show the home and info buttons on the screen.
 */
function showHomeInfo() {
    $("#home-btn").show();
    $("#info-btn").show();
    $("#new-btn").show();
}

/**
 * Change the given image to the hovered version.
 * @param {*} e
 */
function hover(e) {
    e.setAttribute('src', `../images/${e.className}-hover.png`);
}

/**
 * Change the given image to the un-hovered version.
 * @param {*} e
 */
function unhover(e) {
    e.setAttribute('src', `../images/${e.className}.png`);
}
