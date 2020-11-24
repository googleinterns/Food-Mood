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

import com.google.maps.GeoApiContext;

/**
 * GeoContext holds the entry point for a Google GEO API request.
 * It is initiated once and used for all Google GEO EPI requests,
 * including Google Places API and Google Distance Matrix API.
 */
public final class GeoContext {

    private static GeoContext singleInstance = null;

    private GeoApiContext context;

    private GeoContext() {
        context = new GeoApiContext.Builder()
            .apiKey(System.getenv("API_KEY"))
            .build();
    }

    /**
     * Creates a GeoContext instance if not yet been anitialized and returns the intance's context.
     *
     * @return a GeoApiContext object used for Google GEO API requests
     */
    public static GeoApiContext getGeoApiContext() {
        // Double check if initialized for thread safety.
        if (singleInstance == null) {
            synchronized (GeoContext.class) {
                if (singleInstance == null) {
                    singleInstance = new GeoContext();
                }
            }
        }
        return singleInstance.context;
    }
}
