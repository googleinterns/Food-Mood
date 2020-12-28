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

import com.google.maps.PlaceDetailsRequest;

/**
 * Responsible for generating a PlaceDetailsRequest for a specified place.
 */
public interface PlaceDetailsRequestGenerator {

    /**
     * Returns PlaceDetailsRequest for a specified place.
     *
     * @param placeId The search words, which define the wanted cuisine
     * @return A PlaceDetailsRequest for the place with the given placeId
     */
    PlaceDetailsRequest create(String placeId);

}

}
