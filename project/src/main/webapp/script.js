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
  let placesDiv = document.getElementById('place');
  fetch('/query').then(response => response.json()).then((places) => {
    places.forEach((singlePlace) => {
      placesDiv.appendChild(createPlaceElement(singlePlace));
    });
  });
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
  const websiteLink = document.createElement('a');
  websiteLink.href = place.website;
  websiteLink.title = place.website;
  websiteLink.innerHTML = 'Restaurant\'s website';
  placeElement.appendChild(websiteLink);
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
