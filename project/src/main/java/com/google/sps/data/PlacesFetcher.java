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

import com.google.maps.model.LatLng;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.PlaceType;
import com.google.maps.model.PriceLevel;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.*;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;
import java.util.*;
import com.google.appengine.repackaged.com.google.common.collect.ImmutableList;
import java.io.*;

public final class PlacesFetcher {

    /**
     * Temporary fields for M0 version. In next versions those fields will be
     * the fields of a UserPrefrences instance passed to the fetcher by the Servlet.
     */
    private static final LatLng location = new LatLng(32.080576, 34.780641); // Rabin Square TLV
    private static final String cuisineType = "sushi"; // TODO (talbarnahor): change to set of types
    private static final PriceLevel maxPriceLevel = PriceLevel.MODERATE; // TODO: map int from form to PrivceLevel
    private static final boolean openNow = true;

    /**
     * The type of places that will be searched
     */
    private static final PlaceType TYPE = PlaceType.RESTAURANT;

    /**
     * The search radius for places
     */
    private static final int SEARCH_RADIUS = 5000; // TODO (M1): check at least 10 results, and if less extend radius

    /**
     * The entry point for a Google GEO API request
     */
    private static final GeoApiContext CONTEXT = new GeoApiContext.Builder()
        .apiKey("AIza...") // TODO : save key in a file where it can be accessed and pushed to github
        .build();
   
    /**
     * Builds a query and requests it from Google Places API.
     * 
     * @return list of places that supply the query.
     * @throws IOException
     * @throws InterruptedException
     * @throws ApiException
     */
    public static List<Place> fetch() throws IOException, InterruptedException, ApiException { // TODO (talbarnahor): add exception handling and testing
        PlacesSearchResponse results = 
            PlacesApi.textSearchQuery(CONTEXT, cuisineType, location)
                .radius(SEARCH_RADIUS)
                .maxPrice(maxPriceLevel)
                .openNow(openNow)
                .type(TYPE)
                .await();
        return createPlacesList(results.results);
    }

    /**
     * Creates a Place out of each PlacesSearchResult and returns a list of those
     * Places
     * 
     * @param searchResultsArr An array of maximum 20 PlacesSearchResults
     * @return An immutable list of Places
     * @throws IOException
     * @throws InterruptedException
     * @throws ApiException
     */
    private static List<Place> createPlacesList(PlacesSearchResult[] searchResultsArr)
            throws ApiException, InterruptedException, IOException {
        List<Place> placesSet = new ArrayList<Place>();
        for (PlacesSearchResult searchResult: searchResultsArr) {
            PlaceDetails placeDetails = 
                PlacesApi.placeDetails(CONTEXT, searchResult.placeId).await();
                System.out.println(placeDetails.priceLevel.toString());
            Place place = Place.create(
                placeDetails.name, placeDetails.website, placeDetails.formattedPhoneNumber,
                placeDetails.rating, Integer.parseInt(placeDetails.priceLevel.toString()),
                placeDetails.geometry.location);
            placesSet.add(place);
        }
        return ImmutableList.copyOf(placesSet);
   }

    public PlacesFetcher() { }

}