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
  document.getElementById('map-container').style.display = 'none';
  document.getElementById('feedback-box').style.display = 'none';
  const map = createMap();
  const placesDiv = document.getElementById('place');
  fetch('/query')
  .then(response => response.json())
  .then((places) => {
    places.forEach((singlePlace) => {
      placesDiv.appendChild(createPlaceElement(singlePlace));
      addPlaceMarker(map, singlePlace)
      })
    })
  .then(() => {document.getElementById('waiting-message').style.display = 'none'})
  .then(() => {document.getElementById('map-container').style.display = 'block'})
  .then(() => {document.getElementById('feedback-box').style.display = 'block'});
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
  if (place.phone) {
    const phone = document.createTextNode('Phone number:' + place.phone);
    placeElement.appendChild(phone);
    placeElement.appendChild(document.createElement('br'));
  }

  return placeElement;
}

/**
 * Displays the query form to the user and hides the results, so that the user can try again with a
 * different query.
 */
function tryAgain() {
  document.getElementById('query-form').style.display = 'block';
  document.getElementById('results').style.display = 'none';
  document.getElementById('waiting-message').style.display = 'block'
  document.getElementById('place').innerHTML = '';
}


/**
 *  Creates a map and adds it to the page.
 */
function createMap() {
  const ZOOM_OUT = 12;
  const USER_LOCATION = { lat: 32.080576, lng: 34.780641 }; //TODO(M1): change to user's location
  const map = new google.maps.Map(document.getElementById('map-container'), {
    center: USER_LOCATION,
    zoom: ZOOM_OUT,
  });
  return map;
}

/**
 * Adds to the map a place's marker.
 */
function addPlaceMarker(map, place) {
  const ZOOM_IN = 15;
  const marker = new google.maps.Marker({
      title: place.name,
      position: place.location,
      description: place.name.link(place.websiteUrl),
      map: map
  });
  if (place.websiteUrl) {
    const infoWindow = new google.maps.InfoWindow({content: marker.description});
    marker.addListener('click', () => {
        infoWindow.open(map, marker);
    });
  }
  google.maps.event.addListener(marker,'click', () => {
    map.setZoom(ZOOM_IN);
    map.setCenter(marker.position);
  });
}
