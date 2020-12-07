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
// limitations under the License.

package com.google.sps.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.maps.model.LatLng;

/**
 * Responsible for calculating a place’s score
 * based on driving duration to the user’s location and rating.
 */
public interface PlacesScorer {

    /**
     * Returns a map of a place and the score the place gets based on a scoring algorithm.
     *
     * @param places A list of places we want to calculate their score
     * @param userLocation The user’s location used for score calculation
     * @return A map between a place to a double representing the place’s score
     */
    ImmutableMap<Place, Double> getScores(ImmutableList<Place> places, LatLng userLocation);

}
