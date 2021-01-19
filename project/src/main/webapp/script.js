// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the 'License');
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an 'AS IS' BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// This was added in order to let the linter know that we treat 'gapi' (Google API) as a global var.
/* global gapi */

// The user location map. Has to be accessed from different functions.
let globalUserMap;

// The current Google user.
let googleUser = null;

// The current places that were recommended to the user.
let recommendedPlaces = null;

/**
 * Fetches recommended places from the 'query' servlet, and switches from the query form to the
 * results elements in order to display them to the user.
 */
function fetchFromQuery() {
  clearAllMessages();
  let params;
  try {
    params = [
      `cuisines=${getUsercuisinesFromUi()}`,
      `rating=${getUserRatingFromUi()}`,
      `price=${getUserPriceFromUi()}`,
      `open=${getUserOpenNowFromUi()}`,
      `location=${getUserLocationFromUi()}`,
      `idToken=${getUserIdToken()}`,
      `newPlaces=${getUserNewPlacesFromUi()}`
    ].join('&');
  } catch (error) {
    document.getElementById('input-error-container').innerText = 'ERROR: ' + error.message;
    return;
  }
  const placesDiv = document.getElementById('place');
  displayResultsPage();
  const userCoords = JSON.parse(localStorage.getItem('userLocation'));
  const map = createUserResultsMap({lat: userCoords.lat, lng: userCoords.lng});
  fetch('/query?' + params, {method: 'POST'})
      .then(response => response.json())
      .then((places) => {
        recommendedPlaces = places;
        places.forEach((singlePlace) => {
          placesDiv.appendChild(createPlaceElement(singlePlace));
          addPlaceMarker(map, singlePlace)
        });
        displayAfterResults();
        if (places.length < 3) {
          displayNumResultsMessage(places.length);
        }
      })
      .catch((error) => {
        document.getElementById('problem-message-container').innerText = "Oops, we encountered a problem! \
            Could you please try again?";
      });
}

/** Gets the information about the cuisines that the user selected. */
function getUsercuisinesFromUi() {
  const cuisines = document.getElementById('cuisines-form').elements;
  let checkedCuisines = [];
  Array.prototype.forEach.call(cuisines, cuisine => {
    if (cuisine.checked) {
      checkedCuisines.push(cuisine.value);
    }
  });
  // Remove obselete comma
  let result = checkedCuisines.join(',');
  return result;
}

function getUserRatingFromUi() {
  return getCheckedValueByElementId('rating-form', 'Choose exactly one rating.');
}

function getUserIdToken() {
  if (!googleUser) {
    return "";
  }
  return googleUser.getAuthResponse().id_token;
}

function getUserPriceFromUi() {
  return getCheckedValueByElementId('price-form', 'Choose exactly one price level.');
}

function getUserOpenNowFromUi() {
  return getCheckedValueByElementId('open-now-form',
      'Choose if the place should be open now or you don\'t mind.');
}

function getUserNewPlacesFromUi() {
  return getCheckedValueByElementId('old-new-form', 'Choose whether to receive mainly new places.');
}

/** Gets an element that has several options and returns the checked option. */
function getCheckedValueByElementId(elementId, errorMessage) {
  const options = document.getElementById(elementId).elements;
  const chosenOption = Array.prototype.find.call(options, option => option.checked).value;
  if (chosenOption) {
    return chosenOption;
  }
  // If no item was checked and returned, there is an error
  throw new Error(errorMessage);
}

/** Gets that user location that was kept in the local storage. */
function getUserLocationFromUi() {
  const coords = JSON.parse(localStorage.getItem('userLocation'));
  return coords.lat + "," + coords.lng;
}

/** Displays a message to the user for a low number of results. */
function displayNumResultsMessage(numResults) {
  let messageElement = document.getElementById('problem-message-container');
  const tryAgainMessage = '<br>' +
      'You are welcome to try again, and maybe try to change some of the entered parameters.';
  if (numResults === 0) {
    messageElement.innerHTML = 'Your search had no results. ' + tryAgainMessage
  } else if (numResults === 1) {
    messageElement.innerHTML = 'Your search had only 1 result. ' + tryAgainMessage
  } else if (numResults === 2) {
    messageElement.innerHTML = 'Your search had only 2 results. ' + tryAgainMessage
  }
}

/** Displays the results page. */
function displayResultsPage() {
  document.getElementById('user-input').style.display = 'none';
  document.getElementById('results').style.display = 'block';
  document.getElementById('results-map-container').style.display = 'none';
  document.getElementById('feedback-box').style.display = 'none';
}

/** Displays the map and the feedback box in the results page after the results are ready. */
function displayAfterResults() {
  document.getElementById('waiting-message').style.display = 'none';
  document.getElementById('results-map-container').style.display = 'block';
  document.getElementById('feedback-box').style.display = 'block';
}

/** Creates a place element that has all the information that we want to display to the user. */
function createPlaceElement(place) {
  const placeElement = document.createElement('div');
  placeElement.class = 'place-container';
  // Add name
  const name = document.createElement('li');
  name.innerText = place.name;
  placeElement.appendChild(name);
  placeElement.appendChild(document.createElement('br'));
  // Add link to website
  let hasWebUrl = place.websiteUrl != null;
  let hasGoogleUrl = place.googleUrl != null;
  if (!hasWebUrl && !hasGoogleUrl) {
    placeElement.appendChild(document.createTextNode(
        'We don\'t have a link to the restaurant\'s website.'));
  } else {
    if (hasWebUrl) {
      addLinkToPlaceElement(placeElement, place.websiteUrl, 'Website');
    }
    if (hasWebUrl && hasGoogleUrl) {
      placeElement.appendChild(document.createTextNode(', '));
    }
    if (hasGoogleUrl) {
      addLinkToPlaceElement(placeElement, place.googleUrl, 'Google Maps link');
    }
  }
  placeElement.appendChild(document.createElement('br'));
  // Add phone number
  if (place.phone) {
    const phone = document.createTextNode('Phone number: ' + place.phone);
    placeElement.appendChild(phone);
    placeElement.appendChild(document.createElement('br'));
  }
  // Add rating stars
  addRatingStarsToElement(placeElement, place.rating);
  placeElement.appendChild(document.createElement('br'));
  return placeElement;
}

/** Adds a link that represents the given url and shows the given text. */
function addLinkToPlaceElement(placeElement, url, linkText) {
  const link = document.createElement('a');
  link.href = url;
  link.innerText = linkText;
  placeElement.appendChild(link);
}

/** Adds stars that represent a place's rating. */
function addRatingStarsToElement(element, rating) {
  const MAX_RATING = 5;
  const ratingPercentage = (rating / MAX_RATING) * 100; // Turn to percetage
  const roundedRating = Math.round(ratingPercentage / 10) * 10; // Round by 10%
  let outerStars = document.createElement('div');
  outerStars.classList.add('stars-outer');

  let innerStars = document.createElement('div');
  innerStars.classList.add('stars-inner');
  innerStars.style.width = roundedRating.toString() + "%";

  outerStars.appendChild(innerStars);
  element.appendChild(outerStars);
  element.appendChild(document.createTextNode(" ("+ rating +")"));
}

/**
 * Displays the query form to the user and hides the results, so that the user can try to get new
 * results.
 */
function tryAgainAndSendFeedback() {
  document.getElementById('user-input').style.display = 'block';
  document.getElementById('results').style.display = 'none';
  document.getElementById('waiting-message').style.display = 'block'
  clearAllMessages();

  postUserFeedback(null /** place user chose */, true /** user tried again */)
}

/** Clears all the messages that are displayed to the user during the user session. */
function clearAllMessages() {
  document.getElementById('place').innerText = '';
  document.getElementById('map-error-container').innerText = '';
  document.getElementById('input-error-container').innerText = '';
  document.getElementById('problem-message-container').innerText = '';
}

/**
 * Adds a search box to the map, and allows it to keep the user's updating location according to
 * their search box activity
 */
function addSearchBoxToMap(map, searchBoxElement) {
  // Create the search box and link it to the UI element.
  const searchBox = new window.google.maps.places.SearchBox(searchBoxElement);
  map.controls[window.google.maps.ControlPosition.TOP_LEFT].push(searchBoxElement);
  // Bias the SearchBox results towards current map's viewport.
  map.addListener("bounds_changed", () => {searchBox.setBounds(map.getBounds());});
  let markers = [];
  // Listen for the event fired when the user selects a location, and retrieve more details for it.
  searchBox.addListener("places_changed", () => {
    const places = searchBox.getPlaces();
    if (places.length === 0) {
      return;
    }
    // Update the user location to be the first place. This will change if the user clicks on a
    // differnet marker (if there is more than one).
    localStorage.setItem('userLocation', JSON.stringify(places[0].geometry.location));
    // Clear out the old markers.
    markers.forEach((marker) => {marker.setMap(null);});
    markers = [];
    // For each place, get the name and location.
    const bounds = new window.google.maps.LatLngBounds();
    places.forEach((place) => {
      if (!place.geometry) {
        return;
      }
      markers.push(createInteractiveMarkerForPlace(place, map));
      if (place.geometry.viewport) {
        // Only geocodes have viewport.
        bounds.union(place.geometry.viewport);
      } else {
        bounds.extend(place.geometry.location);
      }
    });
    map.fitBounds(bounds);
  });
}

/**
 * Creates a marker for the given place in the given map. If the marker is clicked, it becomes
 * the map's center and updates the user's location in our storage.
 */
function createInteractiveMarkerForPlace(place, map) {
  const currentMarker = createMapMarker(map, place.geometry.location, place.name);
  var infoWindow = new window.google.maps.InfoWindow({content: place.name});
  currentMarker.addListener("click", () => {
    map.setCenter(currentMarker.getPosition());
    localStorage.setItem('userLocation', JSON.stringify(currentMarker.getPosition()));
    infoWindow.open(map,currentMarker);
  });
  return currentMarker;
}

/** Utility function for creating a new map marker based on it's required traits. */
function createMapMarker(map, placePosition, placeTitle) {
  return new window.google.maps.Marker({
    map,
    title: placeTitle,
    position: placePosition
  })
}

/** Displays a Google Maps map that allows the user to search for their location. */
function addMapWithSearchBox() {
  const DEFAULT_COORDINATES_GOOGLE_TEL_AVIV_OFFICE = {lat: 32.070058, lng:34.794347};
  const LOW_ZOOM_LEVEL = 9;
  let userLocationMap = new window.google.maps.Map(document.getElementById("map"), {
    center: DEFAULT_COORDINATES_GOOGLE_TEL_AVIV_OFFICE,
    zoom: LOW_ZOOM_LEVEL,
    mapTypeId: "roadmap",
  });
  globalUserMap = userLocationMap;
  localStorage.setItem('userLocation', JSON.stringify(DEFAULT_COORDINATES_GOOGLE_TEL_AVIV_OFFICE));
  const searchBoxElement = document.getElementById("location-input");
  addSearchBoxToMap(userLocationMap, searchBoxElement);
}

/**
 * Prompts the user with a request to get their location, and adds the location map to the
 * query page.
 */
function getDeviceLocationAndShowOnMap() {
  document.getElementById('map-error-container').innerText = '';
  const FIVE_SECONDS = 5000;
  const ZOOM_IN = 13;

  if (!navigator.geolocation) { // Browser doesn't support Geolocation
    displayGeolocationError('Your browser doesn\'t support geolocation');
    return;
  }
  navigator.geolocation.getCurrentPosition(
    // In case of successs.
    (position) => {
      const map = globalUserMap;
      const userPosition = {
        lat: position.coords.latitude,
        lng: position.coords.longitude,
      };
      map.setCenter(userPosition);
      map.setZoom(ZOOM_IN);
      localStorage.setItem('userLocation', JSON.stringify(userPosition));
      // Add marker with info window to display user location.
      const infoWindow = new window.google.maps.InfoWindow({content: 'My location'});
      const marker = createMapMarker(map, userPosition, 'My location');
      marker.addListener("click", () => infoWindow.open(map, marker));
    },
    // In case of error.
    () => displayGeolocationError('The Geolocation service failed'),
    // Options.
    {timeout: FIVE_SECONDS}
  );
}

/** Desplays the given geolocation on the screen. */
function displayGeolocationError(errorText) {
  document.getElementById('map-error-container').innerHTML =
      errorText + ', so we can\'t use your location.' + '<br>' +
      'Use the map to find your location.' + '<br> <br>';
}

/**
 *  Creates a map and adds it to the page.
 */
function createUserResultsMap(userLocation) {
  const ZOOM_OUT = 12;
  const map = new window.google.maps.Map(
    document.getElementById('results-map-container'), {
        center: userLocation,
        zoom: ZOOM_OUT,
    }
  );
  const marker = new window.google.maps.Marker({
    title: "Me!",
    position: userLocation,
    map: map,
    icon: "https://maps.google.com/mapfiles/kml/shapes/homegardenbusiness.png"
  });
  const infoWindow = new window.google.maps.InfoWindow({content: 'Me!'});
  marker.addListener("click", () => infoWindow.open(map, marker));
  return map;
}

/** Adds to the map a marker for the given place. */
function addPlaceMarker(map, place) {
  const ZOOM_IN = 14;
  let marker = new window.google.maps.Marker({
      title: place.name,
      position: place.location,
      description: place.name.link(place.websiteUrl),
      map: map
  });
  if (place.websiteUrl) {
    const infoWindow = new window.google.maps.InfoWindow({
        content: marker.description
    });
    marker.addListener('click', () => {
        infoWindow.open(map, marker);
    });
  }
  window.google.maps.event.addListener(marker,'click', () => {
    map.setZoom(ZOOM_IN);
    map.setCenter(marker.position);
  });
}

/**
 * Called when a user signs in with a Google account.
 * Updates the global google user, and displays a welcoming messege.
 */
function onSignIn(user) {
  document.getElementById('user-welcome-message-container').innerText =
      "Hello, " + user.getBasicProfile().getName() + "!";
  googleUser = user;
  registerUserByToken();
  document.getElementById('sign-out-button').style.display = 'inline-block';
  document.getElementById('old-new-form').style.display = 'block';
  document.getElementById('feedback-box').style.display = 'inline-block';
}

/** Called when a user signs out of a Google account, updates the screen and the global user. */
function signOut() {
  gapi.auth2.getAuthInstance().signOut();
  document.getElementById('user-welcome-message-container').innerText =
      'You are currently not logged in with a Google account.';
  googleUser = null;
  document.getElementById('sign-out-button').style.display = 'none';
  document.getElementById('old-new-form').style.display = 'none';
  document.getElementById('feedback-box').style.display = 'none';
  document.getElementById('user-feedback-container').style.display = 'none';
}

/** Registers the logged in user, using the registration servlet. */
function registerUserByToken() {
  if (!googleUser) {
    return;
  }
  fetch('/register?idToken=' + googleUser.getAuthResponse().id_token, {method: 'POST'});
}

/** Sends feedback that includes the place the user chose. */
function sendUserChoiceAsFeedback() {
  let indexOfPlaceUserChose = getCheckedValueByElementId('chosen-place-form',
      'Please select the place that you chose.');
  if (indexOfPlaceUserChose <= recommendedPlaces.length) {
    postUserFeedback(recommendedPlaces[indexOfPlaceUserChose].placeId,
        false /** user tried again */)
  }
  document.getElementById('user-feedback-container').style.display = 'inline-block';
  document.getElementById('feedback-box').style.display = 'none';
}

/** Sends the user feedback to the feedback servlet, and clears the recommendedPlaces variable. */
function postUserFeedback(chosenPlaceId, tryAgain) {
  if (!googleUser) {
    return;
  }
  let recommendedPlacesIdArr = [];
  recommendedPlaces.forEach((place) => recommendedPlacesIdArr.push(place.placeId));
  let params = [
    `idToken=${getUserIdToken()}`,
    `recommendedPlaces=${recommendedPlacesIdArr.join(',')}`,
    `chosenPlace=${chosenPlaceId}`,
    `tryAgain=${tryAgain}`
  ].join('&');
  fetch('/feedback?' + params, {method: 'POST'});
  document.getElementById('user-feedback-container').innerText =
      'Thank you, your feedback was received!';
  recommendedPlaces = null
}
