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

import com.google.appengine.repackaged.com.google.common.collect.ImmutableList;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.PlaceType;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PriceLevel;
import com.google.maps.GeoApiContext;
import com.google.maps.TextSearchRequest;
import com.google.maps.PlaceDetailsRequest;
import com.google.maps.errors.ApiException;
import com.google.maps.model.PlacesSearchResult;
import com.google.maps.PlacesApi;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PlacesFetcher {

    /**
     * Those constants are temporaraly hardcoded for M0 version.
     * In next versions those same constants will be the fields of a UserPrefrences
     * instance passed to the PlacesFetcher constructor by the Servlet.
     */
    private static final LatLng LOCATION = new LatLng(32.080576, 34.780641); // Rabin Square TLV;
    private static final String CUISINES ="sushi"; // TODO(M0): change to set of types;
    private static final PriceLevel MAX_PRICE_LEVEL = PriceLevel.values()[2];
    private static final boolean OPEN_NOW = true;

    /** The type of places that will be searched is RESTAURANT. Since most places
     * that deliver food are not tagged as "MEAL-DELIVERY" type at Google Places but
     * rather as "RESTAURANT" this is the most suitable type to search for. */
    private static final PlaceType TYPE = PlaceType.RESTAURANT;

    /** In this radius around "LOCATION" places will be searched. */
    private static final int SEARCH_RADIUS = 5000;
    // TODO(M1): check at least 10 results, and if less extend radius

    /** The entry point for a Google GEO API request. */
    private static final GeoApiContext CONTEXT = new GeoApiContext.Builder()
        .apiKey(System.getenv("API_KEY"))
        .build();

    public PlacesFetcher() {
    }

    // TODO(M0): add exception handling and testing
    /**
     * Builds a query and requests it from Google Places API.
     *
     * @return list of places that supply the query.
     * @throws IOException
     * @throws InterruptedException
     * @throws ApiException
     */
    public List<Place> fetch() throws IOException, InterruptedException, ApiException {
        PlacesSearchResult[] results = getPlacesSearchResults();
        createPlacesList(results);
        return createPlacesList(results);
    }

    /**
     * Queries Google Places API according to given params.
     *
     * @return A PlacesSearchResponse which contains the search results
     * @throws ApiException
     * @throws InterruptedException
     * @throws IOException
     */
    public PlacesSearchResult[] getPlacesSearchResults()
            throws ApiException, InterruptedException, IOException {
        TextSearchRequest query =
            PlacesApi.textSearchQuery(CONTEXT, CUISINES, LOCATION)
                .radius(SEARCH_RADIUS)
                .maxPrice(MAX_PRICE_LEVEL)
                .type(TYPE);
        if (OPEN_NOW) {
            query.openNow(OPEN_NOW);
        }
        PlacesSearchResponse results = query.await();
        return results.results;
    }

    /**
     * Creates a Place out of each PlacesSearchResult and returns a list of those Places.
     *
     * @param searchResultsArr An array of maximum 20 PlacesSearchResults
     * @return An immutable list of Places
     * @throws IOException
     * @throws InterruptedException
     * @throws ApiException
     */
    private List<Place> createPlacesList(PlacesSearchResult[] searchResultsArr)
            throws ApiException, InterruptedException, IOException {
        List<Place> places = new ArrayList<Place>();
        for (PlacesSearchResult searchResult: searchResultsArr) {
          PlaceDetails placeDetails = getPlaceDetails(searchResult.placeId);
            places.add(
                Place.builder()
                    .setName(placeDetails.name)
                    .setWebsiteUrl((placeDetails.website == null) ?
                            "" :placeDetails.website.toString())
                    .setPhone(placeDetails.formattedPhoneNumber)
                    .setRating(placeDetails.rating)
                    .setPriceLevel(Integer.parseInt(placeDetails.priceLevel.toString()))
                    .setLocation(placeDetails.geometry.location)
                    .build());
        }
        return ImmutableList.copyOf(places);
    }

    /**
     * Queries Google Places API to recieve details about a certain place.
     *
     * @param placeId The Google Places placeId of the place that his details will be queried
     * @return PlacesDetails containig certain details about the place
     * @throws ApiException
     * @throws InterruptedException
     * @throws IOException
     */
    public PlaceDetails getPlaceDetails(String placeId)
            throws ApiException, InterruptedException, IOException {
        return PlacesApi.placeDetails(CONTEXT, placeId)
            .fields(
                PlaceDetailsRequest.FieldMask.NAME,
                PlaceDetailsRequest.FieldMask.WEBSITE,
                PlaceDetailsRequest.FieldMask.FORMATTED_PHONE_NUMBER,
                PlaceDetailsRequest.FieldMask.RATING,
                PlaceDetailsRequest.FieldMask.PRICE_LEVEL,
                PlaceDetailsRequest.FieldMask.GEOMETRY_LOCATION)
            .await();
    }
}
