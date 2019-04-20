const FADE_SPEED = 200;

// Store user profile across JS files
let userProfile = undefined;

// Set up sign in tooltip for use across pages
let signInTooltip;
let infoTooltips;
let tutorialTooltips = [];

let newUserModal;
let modalOpen;

let tutorialElements;
const intervalTime = 3000;

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
    newUserModal = $("#newUserModal");
    modalOpen = false;
    tutorialElements = [
        $(".header"), $(".home-btn"), $(".new-btn"), $(".info-btn")
    ];
});

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

    const loginData = {
        userID: userProfile.getId(),
        name: userProfile.getName(),
        email: userProfile.getEmail()
    };

    $.post("/login", loginData, function (response) {
        const responseData = JSON.parse(response);
        if (responseData.success) {
            const isNewUser = responseData.isNewUser;
            if (isNewUser) {
                newUserModal.show();
                modalOpen = true;
            }
        } else {
            window.location.replace("/error");
            console.log("ERROR logging in");
        }
    });
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

/**
 * Close the modal on button click.
 */
function closeModal() {
    if (modalOpen) {
        newUserModal.hide();
        modalOpen = false;
    }
}

/**
 * Listen for keyup escape to close modal if it is open.
 */
$(document).keyup(function (e) {
    if (e.key === "Escape" && modalOpen) {
        newUserModal.hide();
        modalOpen = false;
    }
});

function startTutorial() {
    initTutorialTooltips();
    for (let i = 0; i < tutorialElements.length; i++) {
        tutorialAction(tutorialElements[i], i);
    }
}

function tutorialAction(elt, i) {
    setTimeout(function () {
        highlightTutorialElt(elt);
        showHideTooltip(tutorialTooltips[i][0], intervalTime);
    }, i * intervalTime);
}

function highlightTutorialElt(elt) {
    elt.css('zIndex', 121);
    setTimeout(function () {
        elt.css('zIndex', "");
    }, intervalTime)
}

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

function initTutorialTooltips() {
    let alt = "";
    let searchText = "To search for an existing carpools, go to our" +
        " home screen.";
    if (window.location.pathname === "/") {
        alt = "-main";
        searchText = "Search for existing carpools and request the host to" +
            " join."
    }
    tutorialTooltips.push(tippy("#profile-info", {
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
        content: "Sign in with your Google account to join or host a trip" +
            " and to view your upcoming trips."
    }));
    tutorialTooltips.push(tippy(`#home-btn-tutorial${alt}`, {
        animation: "scale",
        arrow: true,
        arrowType: "round",
        theme: "drawbridge",
        interactive: false,
        trigger: "manual",
        hideOnClick: false,
        maxWidth: 150,
        distance: 30,
        inertia: true,
        sticky: true,
        placement: "bottom",
        content: searchText
    }));
    tutorialTooltips.push(tippy(`#new-btn-tutorial${alt}`, {
        animation: "scale",
        arrow: true,
        arrowType: "round",
        theme: "drawbridge",
        interactive: false,
        trigger: "manual",
        hideOnClick: false,
        maxWidth: 150,
        distance: 30,
        inertia: true,
        sticky: true,
        placement: "bottom",
        content: "If you want to host your own carpool, press the host trip" +
            " button to create a new carpool."
    }));
    tutorialTooltips.push(tippy(`#info-btn-tutorial${alt}`, {
        animation: "scale",
        arrow: true,
        arrowType: "round",
        theme: "drawbridge",
        interactive: false,
        trigger: "manual",
        distance: 30,
        hideOnClick: false,
        maxWidth: 150,
        inertia: true,
        sticky: true,
        placement: "bottom",
        content: "If you still have any questions about Drawbridge, visit" +
            " our info page to learn more."
    }));
}
