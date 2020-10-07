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

import com.google.appengine.repackaged.com.google.common.collect.ImmutableList;

public class PlacesFetcher {

    /** Places will be fetched within a ceratain radius from this location. */
    private final LatLng location;

    /** The cuisine types of the places that are fetched. */
    private final String cuisineType;

    /** The maximum price level as identified in Google Places of the places that will be fetched. */
    private final PriceLevel maxPriceLevel;

    /** Specifies if the fetched places must be open at the time of fetching. */
    private final boolean openNow;

    /** The type of places that will be searched. */
    private static final PlaceType TYPE = PlaceType.RESTAURANT;

    /** The search radius for place. */
    private static final int SEARCH_RADIUS = 5000;
    // TODO (M1): check at least 10 results, and if less extend radius

    /** The entry point for a Google GEO API request. */
    private static final GeoApiContext CONTEXT = new GeoApiContext.Builder()
        .apiKey("AIza...") // TODO : save key in a file that can be accessed and pushed to github
        .build();

    /**
     * Fields are temporaraly hard coded for M0 version. In next versions those same fields will be
     * the fields of a UserPrefrences instance passed to the PlacesFetcher constructor by the Servlet.
     */
    public PlacesFetcher() {
        this.location = new LatLng(32.080576, 34.780641); // Rabin Square TLV
        this.cuisineType = "sushi"; // TODO (talbarnahor): change to set of types
        this.maxPriceLevel = PriceLevel.values()[2];
        this.openNow = true;
    }

    // TODO (talbarnahor): add exception handling and testing
    /**
     * Builds a query and requests it from Google Places API.
     *
     * @return list of places that supply the query.
     * @throws IOException
     * @throws InterruptedException
     * @throws ApiException
     */
    public List<Place> fetch() throws IOException, InterruptedException, ApiException {
        PlacesSearchResult results[] = getPlacesSearchResults();
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
        PlacesApi.textSearchQuery(CONTEXT, cuisineType, location)
            .radius(SEARCH_RADIUS)
            .maxPrice(maxPriceLevel)
            .type(TYPE);
        if (openNow) {
            query.openNow(openNow);
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
                    .setWebsiteUrl(placeDetails.website.toString())
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