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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PlacesFetcher {

    // The type of places that will be searched is RESTAURANT. Since most places
    // that deliver food are not tagged as "MEAL-DELIVERY" type at Google Places but
    // rather as "RESTAURANT" this is the most suitable type to search for.
    private static final PlaceType TYPE = PlaceType.RESTAURANT;

    // In this radius around "LOCATION" places will be searched.
    private static final int SEARCH_RADIUS = 5000;

    // The entry point for a Google GEO API request.
    private static final GeoApiContext CONTEXT = new GeoApiContext.Builder().apiKey(System.getenv("API_KEY")).build();

    // A mapping between cuisines and text search words. */
    private static final Map<String, ArrayList<String>> CUISINE_TO_SEARCH_WORDS = getCuisinesMap();

    /**
     * Builds a query and requests it from Google Places API.
     *
     * @param preferences the UserPreferences as specified by the user
     * @return an immutable list of places that supply the query
     * @throws FetcherException when an error occurs in querying the Places API for
     *                          places or for places details
     */
    public ImmutableList<Place> fetch(UserPreferences preferences) throws FetcherException {
        TextSearchRequest query = PlacesApi
                .textSearchQuery(CONTEXT, createCuisinesQuery(preferences.cuisines()), preferences.location())
                .radius(SEARCH_RADIUS).maxPrice(PriceLevel.values()[preferences.maxPriceLevel()]).type(TYPE);
        if (preferences.openNow()) {
            query.openNow(preferences.openNow());
        }
        try {
            return createPlacesList(getPlacesSearchResults(query));
        } catch (ApiException | InterruptedException | IOException e) {
            throw new FetcherException("Couldn't fetch places from Places API", e);
        }
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

    private ImmutableList<Place> createPlacesList(PlacesSearchResult[] searchResultsArr) throws FetcherException {
        List<Place> places = new ArrayList<Place>();
        for (PlacesSearchResult searchResult : searchResultsArr) {
            PlaceDetailsRequest detailsRequest = genPlaceDetailsRequest(searchResult.placeId);
            PlaceDetails placeDetails;
            try {
                placeDetails = getPlaceDetails(detailsRequest);
            } catch (ApiException | InterruptedException | IOException e) {
                throw new FetcherException("Couldn't get place details from Places API", e);
            }
            places.add(Place.builder().setName(placeDetails.name)
                    .setWebsiteUrl(placeDetails.website == null ? "" : placeDetails.website.toString())
                    .setPhone(placeDetails.formattedPhoneNumber == null ? ""
                            : placeDetails.formattedPhoneNumber.toString())
                    .setRating(placeDetails.rating).setPriceLevel(Integer.parseInt(placeDetails.priceLevel.toString()))
                    .setLocation(placeDetails.geometry.location).build());
        }
        return ImmutableList.copyOf(places);
    }

    private PlaceDetailsRequest genPlaceDetailsRequest(String placeId) {
        return PlacesApi.placeDetails(CONTEXT, placeId).fields(PlaceDetailsRequest.FieldMask.NAME,
                PlaceDetailsRequest.FieldMask.WEBSITE, PlaceDetailsRequest.FieldMask.FORMATTED_PHONE_NUMBER,
                PlaceDetailsRequest.FieldMask.RATING, PlaceDetailsRequest.FieldMask.PRICE_LEVEL,
                PlaceDetailsRequest.FieldMask.GEOMETRY_LOCATION);
    }

    /**
     * Queries Google Places API to recieve requested details about a certain place.
     *
     * @param request A PlaceDetailsRequest to query certain details on a certain
     *                place
     * @return PlacesDetails containig requested details about the place
     */
    @VisibleForTesting
    PlaceDetails getPlaceDetails(PlaceDetailsRequest request) throws ApiException, InterruptedException, IOException {
        return request.await();
    }

    private static String createCuisinesQuery(ImmutableList<String> cuisines) {
        return cuisines.stream().map(cuisine -> String.join("|", CUISINE_TO_SEARCH_WORDS.get(cuisine)))
                .collect(Collectors.joining("|"));
    }

    private static final ImmutableMap<String, ArrayList<String>> getCuisinesMap() {
        Type mapType = new TypeToken<Map<String, ArrayList<String>>>() {
        }.getType();
        Map<String, ArrayList<String>> map = new Gson().fromJson(new JsonReader(
            new InputStreamReader(
                PlacesFetcher.class.getResourceAsStream("cuisinesSearchWords.json"))),
                mapType);
        return ImmutableMap.copyOf(map);
    }
}
