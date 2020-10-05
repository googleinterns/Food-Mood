// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.import java.io.IOException;

/**
 * Fetch information from the 'query' servlet.
 */
function fetchFromQuery() { 
  let placesDiv = document.getElementById("place");
  // fetch('/query').then(response => response.json()).then((places) => {
  //   places.forEach((singlePlace) => {
  //     placesDiv.appendChild(createPlaceElement(singlePlace));
  //   });
  // });
  let places = [{name: "Place1", website: "google.com", phone: "+972520000000"}, 
                {name: "Place2", website: "google.com", phone: "+972520000000"}, 
                {name: "Place3", website: "google.com", phone: "+972520000000"}];
  places.forEach((singlePlace) => {
    placesDiv.appendChild(createPlaceElement(singlePlace));
  });
}

/** 
 * Creates a record in a table. 
 */
function createPlaceElement(place) {
  const placeElement = document.createElement('div');
  placeElement.class = "place-container";

  let name = document.createElement('li');
  name.innerText = place.name; 
  placeElement.appendChild(name);
  placeElement.appendChild(document.createElement("br"));

  let websiteLink = document.createElement('a');
  websiteLink.href = place.website;
  websiteLink.title = place.website;
  websiteLink.innerHTML = "Restaurant's website";
  placeElement.appendChild(websiteLink);
  placeElement.appendChild(document.createElement("br"));
  
  let phone = document.createTextNode("Phone number:" + place.phone); 
  placeElement.appendChild(phone);
  placeElement.appendChild(document.createElement("br"));

  return placeElement;
}
