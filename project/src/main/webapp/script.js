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
  document.getElementById('query-form').style.display = 'none';
  document.getElementById('results').style.display = 'block';
  const placesDiv = document.getElementById('place');
  const params = [
    `quisines=${getQuisines()}`,
    `rating=${getRating()}`,
    `price=${getPrice()}`,
    `location=${getLocation()}`
  ].join('&');
  fetch('/query?' + params).then(response => response.json()).then((places) => {
    places.forEach((singlePlace) => {
      placesDiv.appendChild(createPlaceElement(singlePlace));
    });
  });
}

function getQuisines() {
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
  }
  result = result.endsWith(",") ? result.substring(0, result.length - 1) : result;
}

function getRating() {
  const rating = document.getElementById('rating-form').elements;
  for (i = 0; i < rating.length; i++) {
    if (rating[i].checked) {
      return rating[i].value;
    }
  }
// TODO: error if we get here and didn't return yet? (not currently possible)
}

function getPrice() {
  const price = document.getElementById('price-form').elements;
  for (i = 0; i < price.length; i++) {
    if (price[i].checked) {
      return price[i].value;
    }
  }
  // TODO: error if we get here and didn't return yet? (not currently possible)
}

function getLocation() {
  //TODO this is a hard-coded location, need to return the real location
  return '32.070058,34.794347';
}

/**
 * Creates a place element.
 */
function createPlaceElement(place) {
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

/**
 * Prompts the user with a request to get his location, and adds the location map to the
 * query page.
 */
function addUserLocationToMap() {
  const FIVE_SECONDS = 5000;

  if (!navigator.geolocation) { // Browser doesn't support Geolocation
    handleLocationError("Error: Your browser doesn't support geolocation.");
    return;
  }
  navigator.geolocation.getCurrentPosition(
    // In case of successs.
    (position) => {
      const userPosition = {
        lat: position.coords.latitude,
        lng: position.coords.longitude,
      };
      addMapWithWindow('map', userPosition);
    },
    // In case of error.
    () => {
      handleLocationError("Error: The Geolocation service failed.");
    },
    // Options.
    {
      timeout: FIVE_SECONDS,
    }
  );

  document.getElementById('map').style.display = 'block';
  document.getElementById('submit-query').style.display = 'block';
}

function addMapWithWindow(elementId, latLong) {
  const HIGH_ZOOM_LEVEL = 14;
  const map = new google.maps.Map(
      document.getElementById(elementId), {
        center: latLong,
        zoom: HIGH_ZOOM_LEVEL
      }
  );
  infoWindow = new google.maps.InfoWindow({
    content: 'My location',
    position: latLong
  });
  infoWindow.open(map);
}

function handleLocationError(errorMessage) {
  //TODO(M2?): Take user location in other ways.
  //DEVELOPEMENT MODE: cloud top doesn't enable to take it's location, we display a default location
  const GOOGLE_OFFICE_COORDINATES = {lat: 32.070058, lng:34.794347};
  addMapWithWindow('map', GOOGLE_OFFICE_COORDINATES);
  const mapElement = document.getElementById('map');
  const errorText = 'We had trouble getting yout location. ' + errorMessage +
      " Using default location";
  mapElement.appendChild(document.createTextNode(errorText));
}
