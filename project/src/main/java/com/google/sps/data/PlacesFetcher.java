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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.PlaceType;
import com.google.maps.model.PriceLevel;
import com.google.maps.GeoApiContext;
import com.google.maps.TextSearchRequest;
import com.google.maps.PlaceDetailsRequest;
import com.google.maps.errors.ApiException;
import com.google.maps.model.PlacesSearchResult;
import com.google.maps.PlacesApi;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Map;
import java.util.stream.Collectors;

public class PlacesFetcher {

    // The type of places that will be searched is RESTAURANT. Since most places
    // that deliver food are not tagged as "MEAL-DELIVERY" type at Google Places but
    // rather as "RESTAURANT" this is the most suitable type to search for.
    private static final PlaceType TYPE = PlaceType.RESTAURANT;

    // The initial search radius in meters.
    private static final int INIT_SEARCH_RADIUS_M = 5000;

    // The minimal number of results to be fetched.
    private static final int MIN_NUM_OF_RESULTS = 10;

    // The maximal number of times the search radius will be extended.
    private static final int MAX_NUM_OF_RADIUS_EXTENSIONS = 4;

    // The entry point for a Google GEO API request.
    private GeoApiContext context;

    // The path of the configuration file containing the mapping of cuisines to search words.
    private static final String CUISINES_SEARCH_WORDS_CONFIG_PATH  = "cuisinesSearchWords.json";

    // A mapping between cuisines and text search words. */
    private static final ImmutableMap<String, List<String>> CUISINE_TO_SEARCH_WORDS =
        getCuisinesMap();

    /**
     * PlacesFetcher constructor.
     *
     * @param geoApiContext the GeoApiContext used for all Google GEO API requests
     */
    public PlacesFetcher(GeoApiContext geoApiContext) {
        this.context = geoApiContext;
    }
    /**
     * Builds a query and requests it from Google Places API.
     *
     * @param preferences the UserPreferences as specified by the user
     * @return an immutable list of places that supply the query
     * @throws FetcherException when an error occurs in querying the Places API
     *     for places or for places details
     */
    public ImmutableList<Place> fetch(UserPreferences preferences) throws FetcherException {
        PlacesSearchResult[] placesSearchResult;
        int attemptsCounter = 0;
        do {
            attemptsCounter++;
            try {
                placesSearchResult = getPlacesSearchResults(
                    genTextSearchRequest(preferences, INIT_SEARCH_RADIUS_M * attemptsCounter));
            } catch (ApiException | InterruptedException | IOException | IllegalStateException e) {
                throw new FetcherException("Couldn't fetch places from Places API", e);
            }
        } while (
            placesSearchResult.length < MIN_NUM_OF_RESULTS
            && attemptsCounter < MAX_NUM_OF_RADIUS_EXTENSIONS);
        return createPlacesList(placesSearchResult);
    }

    private TextSearchRequest genTextSearchRequest(UserPreferences preferences, int radius) {
        TextSearchRequest request = PlacesApi.textSearchQuery(
            context, createCuisinesQuery(preferences.cuisines()), preferences.location())
                .radius(radius)
                .maxPrice(PriceLevel.values()[preferences.maxPriceLevel()])
                .type(TYPE);
        if (preferences.openNow()) {
            request.openNow(preferences.openNow());
        }
        return request;
    }

    /**
     * Queries Google Places API according to given query.
     *
     * @param query A TextSearchRequest with all params to query on
     * @return A PlacesSearchResponse which contains the search results
     * @throws IOException
     * @throws InterruptedException
     * @throws ApiException
     */
    @VisibleForTesting
    PlacesSearchResult[] getPlacesSearchResults(TextSearchRequest query)
            throws ApiException, InterruptedException, IOException {
        return query.await().results;
    }

    private ImmutableList<Place> createPlacesList(PlacesSearchResult[] searchResultsArr)
            throws FetcherException {
        List<Place> places = new ArrayList<Place>();
        for (PlacesSearchResult searchResult : searchResultsArr) {
            PlaceDetailsRequest detailsRequest = genPlaceDetailsRequest(searchResult.placeId);
            PlaceDetails placeDetails;
            try {
                placeDetails = getPlaceDetails(detailsRequest);
            } catch (ApiException | InterruptedException | IOException e) {
                throw new FetcherException(
                    "Couldn't get place details from Places API", e);
            }
            places.add(
                Place.builder()
                    .setName(placeDetails.name)
                    .setWebsiteUrl(Objects.toString(placeDetails.website, ""))
                    .setPhone(Strings.nullToEmpty(placeDetails.formattedPhoneNumber))
                    .setRating(placeDetails.rating)
                    .setPriceLevel(Integer.parseInt(placeDetails.priceLevel.toString()))
                    .setLocation(placeDetails.geometry.location)
                    .setPlaceId(placeDetails.placeId)
                    .setGoogleUrl(Objects.toString(placeDetails.url, ""))
                    .setBusinessStatus(BusinessStatus.valueOf(
                        Objects.toString(placeDetails.businessStatus, "UNKNOWN")))
                    .build());
        }
        return ImmutableList.copyOf(places);
    }

    private PlaceDetailsRequest genPlaceDetailsRequest(String placeId) {
        return PlacesApi.placeDetails(context, placeId)
            .fields(
                PlaceDetailsRequest.FieldMask.NAME,
                PlaceDetailsRequest.FieldMask.WEBSITE,
                PlaceDetailsRequest.FieldMask.FORMATTED_PHONE_NUMBER,
                PlaceDetailsRequest.FieldMask.RATING,
                PlaceDetailsRequest.FieldMask.PRICE_LEVEL,
                PlaceDetailsRequest.FieldMask.GEOMETRY_LOCATION,
                PlaceDetailsRequest.FieldMask.PLACE_ID,
                PlaceDetailsRequest.FieldMask.URL,
                PlaceDetailsRequest.FieldMask.BUSINESS_STATUS);
    }

    /**
     * Queries Google Places API to recieve requested details about a certain place.
     *
     * @param request A PlaceDetailsRequest to query certain details on a certain
     *                place
     * @return PlacesDetails containig requested details about the place
     */
    @VisibleForTesting
    PlaceDetails getPlaceDetails(PlaceDetailsRequest request)
            throws ApiException, InterruptedException, IOException {
        return request.await();
    }

     /**
     * Creates a String query that includes all text search words matching the specified cuisines.
     *
     * @param cuisines A list of the cuisines we want to query
     * @return A String of text search words to query on
     * @throws FetcherException when an invalid cuisine is queried
     */
    @VisibleForTesting
    String createCuisinesQuery(ImmutableList<String> cuisines) throws FetcherException {
        return cuisines.stream()
            .map(cuisine -> getSearchWords(cuisine))
            .collect(Collectors.joining("|"));
    }

    private static String getSearchWords(String cuisine) throws FetcherException {
        try {
            return String.join("|", CUISINE_TO_SEARCH_WORDS.get(cuisine));
        } catch (NullPointerException e) {
            throw new FetcherException("Couldn't query on invalid cuisine", e);
        }
    }

    private static ImmutableMap<String, List<String>> getCuisinesMap() {
        Type mapType = new TypeToken<Map<String, List<String>>>() {
        }.getType();
        Map<String, List<String>> map = new Gson().fromJson(new JsonReader(
            new InputStreamReader(
                PlacesFetcher.class.getResourceAsStream(CUISINES_SEARCH_WORDS_CONFIG_PATH))),
                mapType);
        return ImmutableMap.copyOf(map);
    }
}
