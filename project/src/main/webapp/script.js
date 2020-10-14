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

/**
 * Fetches recommended places from the 'query' servlet, and switches from the query form to the
 * results elements in order to display them to the user.
 */
function fetchFromQuery() {
  try {
    const params = [
      `quisines=${getUserQuisinesFromUi()}`,
      `rating=${getUserRatingFromUi()}`,
      `price=${getUserPriceFromUi()}`,
      `location=${getUserLocationFromUi()}`
    ].join('&');
    const placesDiv = document.getElementById('place');
    fetch('/query?' + params).then(response => response.json()).then((places) => {
      places.forEach((singlePlace) => {
        placesDiv.appendChild(getPlaceUiElement(singlePlace));
      });
    });
    document.getElementById('query-form').style.display = 'none';
    document.getElementById('results').style.display = 'block';
  }
  catch(error) {
    // TODO: define what we want when there is an error
  }
}

function getUserQuisinesFromUi() {
  // const quisines = document.forms[0];
  const quisines = document.getElementById('quisines-form').elements;
  let result = "";
  let i;
  for (i = 0; i < quisines.length; i++) {
    if (quisines[i].checked) {
      result = result + quisines[i].value + ",";
    }
  }
  if (result === "") {
    alert("You must choose at least one quisine type!");
    throw "Quisines input error: user must choose at least one quisine.";
  }
  result = result.endsWith(",") ? result.substring(0, result.length - 1) : result;
}

function getUserRatingFromUi() {
  const rating = document.getElementById('rating-form').elements;
  for (i = 0; i < rating.length; i++) {
    if (rating[i].checked) {
      return rating[i].value;
    }
  }
  throw "Rating input error: user must choose exactly one rating.";
}

function getUserPriceFromUi() {
  const price = document.getElementById('price-form').elements;
  for (i = 0; i < price.length; i++) {
    if (price[i].checked) {
      return price[i].value;
    }
  }
  throw "Price input error: user must choose exactly one price level.";
}

function getUserLocationFromUi() {
  const coords = window.currentUserLocation;
  console.log(coords.lat + "," + coords.lng);
  return coords.lat + "," + coords.lng;
}

function getPlaceUiElement(place) {
  const placeElement = document.createElement('div');
  placeElement.class = 'place-container';

  // Add name
  const name = document.createElement('li');
  name.innerText = place.name;
  placeElement.appendChild(name);
  placeElement.appendChild(document.createElement('br'));

  // Add link to website
  if (place.website) {
    const websiteLink = document.createElement('a');
    websiteLink.href = place.website;
    websiteLink.title = place.website;
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
 * Displays the query form to the user and hides the results, so that the user can try again with a
 * different query.
 */
function tryAgain() {
  document.getElementById('query-form').style.display = 'block';
  document.getElementById('results').style.display = 'none';
  document.getElementById('place').innerHTML = '';
}

function addSearchBoxToMap(map, input) {
  // Create the search box and link it to the UI element.
  const searchBox = new google.maps.places.SearchBox(input);
  map.controls[google.maps.ControlPosition.TOP_LEFT].push(input);
  // Bias the SearchBox results towards current map's viewport.
  map.addListener("bounds_changed", () => {
    searchBox.setBounds(map.getBounds());
  });
  let markers = [];
  // Listen for the event fired when the user selects a prediction and retrieve
  // more details for that place.
  searchBox.addListener("places_changed", () => {
    const places = searchBox.getPlaces();

    if (places.length == 0) {
      return;
    }
    // Clear out the old markers.
    markers.forEach((marker) => {
      marker.setMap(null);
    });
    markers = [];
    // For each place, get the icon, name and location.
    const bounds = new google.maps.LatLngBounds();
    places.forEach((place) => {
      if (!place.geometry) {
        console.log("Returned place contains no geometry");
        return;
      }
      const icon = {
        url: place.icon,
        size: new google.maps.Size(71, 71),
        origin: new google.maps.Point(0, 0),
        anchor: new google.maps.Point(17, 34),
        scaledSize: new google.maps.Size(25, 25),
      };
      window.currentUserLocation = {
        lat: place.geometry.location.lat(),
        lng: place.geometry.location.lng()
      };
      // Create a marker for each place.
      markers.push(
        new google.maps.Marker({
          map,
          icon,
          title: place.name,
          position: place.geometry.location,
        })
      );

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
  window.map = new google.maps.Map(document.getElementById("map"), {
    center: DEFAULT_COORDINATES_GOOGLE_TEL_AVIV_OFFICE,
    zoom: LOW_ZOOM_LEVEL,
    mapTypeId: "roadmap",
  });
  window.currentUserLocation = DEFAULT_COORDINATES_GOOGLE_TEL_AVIV_OFFICE ;
  const input = document.getElementById("location-input");
  addSearchBoxToMap(window.map, input);
}

/**
 * Prompts the user with a request to get his location, and adds the location map to the
 * query page.
 */
function getDeviceLocationAndShowOnMap() {
  const FIVE_SECONDS = 5000;
  const HIGH_ZOOM_LEVEL = 13;

  if (!navigator.geolocation) { // Browser doesn't support Geolocation
    displayFeolocationError('Your browser doesn\'t support geolocation');
    return;
  }
  navigator.geolocation.getCurrentPosition(
    // In case of successs.
    (position) => {
      const map = window.map;
      const userPosition = {
        lat: position.coords.latitude,
        lng: position.coords.longitude,
      };
      map.setCenter(userPosition);
      map.setZoom(HIGH_ZOOM_LEVEL);
      window.currentUserLocation = userPosition;
      // Add marker with info window to display user location.
      const infowindow = new google.maps.InfoWindow({
        content: 'My location',
      });
      const marker = new google.maps.Marker({
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
      displayFeolocationError('The Geolocation service failed');
    },
    // Options.
    {
      timeout: FIVE_SECONDS,
    }
  );
}

function displayFeolocationError(errorText) {
  document.getElementById('map-error-container').appendChild(document.createTextNode(
    errorText + ', so we can\'t use device location.'
  ));
}
