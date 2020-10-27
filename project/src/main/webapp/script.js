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
// limitations under the License.import java.io.IOException;

/* The user location map. the map has to be accessed from different functions, so it has to be kept
 * globally. */
let globalUserMap;

/**
 * Fetches recommended places from the 'query' servlet, and switches from the query form to the
 * results elements in order to display them to the user.
 */
function fetchFromQuery() {
  try {
    const params = [
      `cuisines=${getUsercuisinesFromUi()}`,
      `rating=${getUserRatingFromUi()}`,
      `price=${getUserPriceFromUi()}`,
      `open=${getUserOpenNowFromUi()}`,
      `location=${getUserLocationFromUi()}`
    ].join('&');
    const placesDiv = document.getElementById('place');
    fetch('/query?' + params).then(response => response.json()).then((places) => {
      places.forEach((singlePlace) => {
        placesDiv.appendChild(createPlaceElement(singlePlace));
      });
    });
    document.getElementById('query-form').style.display = 'none';
    document.getElementById('results').style.display = 'block';
  } catch (error) {
    alert(error.message);
  }
}

function getUsercuisinesFromUi() {
  const cuisines = document.getElementById('cuisines-form').elements;
  let result = '';
  let i;
  for (i = 0; i < cuisines.length; i++) {
    if (cuisines[i].checked) {
      result = result + cuisines[i].value + ',';
    }
  }
  if (result === '') {
    throw new Error("Choose at least one cuisine.");
  }
  // Remove obselete comma
  result = result.slice(0, -1);
  return result;
}

function getUserRatingFromUi() {
  return getCheckedValueByElementId('rating-form',
      'Choose exactly one rating.');
}

function getUserPriceFromUi() {
  return getCheckedValueByElementId('price-form',
      'Choose exactly one price level.');
}

function getUserOpenNowFromUi() {
  return getCheckedValueByElementId('open-now-form',
      'Choose if the place should be open now or you don\'t mind.');
}

function getCheckedValueByElementId(elementId, errorMessage) {
  const options = document.getElementById(elementId).elements;
  let i;
  for (i = 0; i < options.length; i++) {
    if (options[i].checked) {
      return options[i].value;
    }
  }
  // If no item was checked and returned, there is an error
  throw new Error(errorMessage);
}

function getUserLocationFromUi() {
  const coords = JSON.parse(localStorage.getItem('userLocation'));
  console.log(coords.lat + "," + coords.lng);
  return coords.lat + "," + coords.lng;
}

function createPlaceElement(place) {
  const placeElement = document.createElement('div');
  placeElement.class = 'place-container';
  // Add name
  const name = document.createElement('li');
  name.innerText = place.name;
  placeElement.appendChild(name);
  placeElement.appendChild(document.createElement('br'));
  // Add link to website
  if (place.websiteUrl) {
    const websiteLink = document.createElement('a');
    websiteLink.href = place.websiteUrl;
    websiteLink.title = place.websiteUrl;
    websiteLink.innerHTML = 'Restaurant\'s website';
    placeElement.appendChild(websiteLink);
  } else {
    const noSite = document.createTextNode('We don\'t have a link to the restaurant\'s website.');
    placeElement.appendChild(noSite);
  }
  placeElement.appendChild(document.createElement('br'));
  // Add phone number
  const phone = document.createTextNode('Phone number:' + place.phone);
  placeElement.appendChild(phone);
  placeElement.appendChild(document.createElement('br'));

  return placeElement;
}

/**
 * Displays the query form to the user and hides the results, so that the user can try to get new
 * results.
 */
function tryAgain() {
  document.getElementById('query-form').style.display = 'block';
  document.getElementById('results').style.display = 'none';
  document.getElementById('place').innerHTML = '';
}

// Adds a search box to the map,  and sets it so that the user interraction with it is
function addSearchBoxToMap(map, searchBoxElement) {
  // Create the search box and link it to the UI element.
  const searchBox = new window.google.maps.places.SearchBox(searchBoxElement);
  map.controls[window.google.maps.ControlPosition.TOP_LEFT].push(searchBoxElement);
  // Bias the SearchBox results towards current map's viewport.
  map.addListener("bounds_changed", () => {
    searchBox.setBounds(map.getBounds());
  });
  let markers = [];
  // Listen for the event fired when the user selects a location, and retrieve more details for it.
  searchBox.addListener("places_changed", () => {
    const places = searchBox.getPlaces();
    if (places.length == 0) {
      return;
    }
    // Update the user location to be the first place. If there is more than one, it is changes
    // when the user clicks on a different marker
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
      // Create a marker for each place.
      const currentMarker = new window.google.maps.Marker({
        map,
        title: place.name,
        position: place.geometry.location,
      })
      var infowindow = new window.google.maps.InfoWindow({content: place.name});
      markers.push(currentMarker);
      currentMarker.addListener("click", () => {
        map.setCenter(currentMarker.getPosition());
        localStorage.setItem('userLocation', JSON.stringify(currentMarker.getPosition()));
        infowindow.open(map,currentMarker);
      });
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
 * Displays a Google Maps map that allows the user to search fo his location.
 */
function addMapWithSearchBox() {
  const DEFAULT_COORDINATES_GOOGLE_TEL_AVIV_OFFICE = {lat: 32.070058, lng:34.794347};
  const LOW_ZOOM_LEVEL = 9;
  let map = new window.google.maps.Map(document.getElementById("map"), {
    center: DEFAULT_COORDINATES_GOOGLE_TEL_AVIV_OFFICE,
    zoom: LOW_ZOOM_LEVEL,
    mapTypeId: "roadmap",
  });
  globalUserMap = map;
  localStorage.setItem('userLocation', JSON.stringify(DEFAULT_COORDINATES_GOOGLE_TEL_AVIV_OFFICE));
  const searchBoxElement = document.getElementById("location-input");
  addSearchBoxToMap(map, searchBoxElement);
}

/**
 * Prompts the user with a request to get his location, and adds the location map to the
 * query page.
 */
function getDeviceLocationAndShowOnMap() {
  const FIVE_SECONDS = 5000;
  const HIGH_ZOOM_LEVEL = 13;

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
      map.setZoom(HIGH_ZOOM_LEVEL);
      localStorage.setItem('userLocation', JSON.stringify(userPosition));
      // Add marker with info window to display user location.
      const infowindow = new window.google.maps.InfoWindow({
        content: 'My location',
      });
      const marker = new window.google.maps.Marker({
        position: userPosition,
        map,
        title: 'My location',
      });
      marker.addListener("click", () => {
        infowindow.open(map, marker);
      });
    },
    // In case of error.
    () => {
      displayGeolocationError('The Geolocation service failed');
    },
    // Options.
    {
      timeout: FIVE_SECONDS,
    }
  );
}

function displayGeolocationError(errorText) {
  document.getElementById('map-error-container').innerHTML =
      errorText + ', so we can\'t use your location.' + '<br>' +
      'Use the map to find your location.' + '<br>';
}
