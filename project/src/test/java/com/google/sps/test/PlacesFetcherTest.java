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

package com.google.sps.test;

import com.google.appengine.repackaged.com.google.common.collect.ImmutableList;
import com.google.maps.PlaceDetailsRequest;
import com.google.maps.TextSearchRequest;
import com.google.maps.errors.ApiException;
import com.google.maps.model.Geometry;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.PlacesSearchResult;
import com.google.maps.model.PriceLevel;
import com.google.sps.data.Place;
import com.google.sps.data.PlacesFetcher;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import com.google.sps.data.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@RunWith(JUnit4.class)
public final class PlacesFetcherTest {

  private static final String PLACE_URL = "https://www.google.com/"; // used for Places
  private static final URL PLACES_DETAILS_URL = createTestURL(PLACE_URL); // used for PlaceDetails
  private static final String PHONE = "+97250-0000-000";
  private static final LatLng LOCATION = new LatLng(32.08074, 34.78059);
  private static final float RATING = 4;
  private static final int PRICELEVEL_INT = 2; // used for Places
  private static final PriceLevel PRICELEVEL = PriceLevel.MODERATE; // used for PlaceDetails

  /** Valid Place objects. */
  private static final Place PLACE_1 =
    Place.builder()
      .setName("name1")
      .setWebsiteUrl(PLACE_URL)
      .setPhone(PHONE)
      .setRating(RATING)
      .setPriceLevel(PRICELEVEL_INT)
      .setLocation(LOCATION)
      .build();
  private static final Place PLACE_2 =
    Place.builder()
      .setName("name2")
      .setWebsiteUrl(PLACE_URL)
      .setPhone(PHONE)
      .setRating(RATING)
      .setPriceLevel(PRICELEVEL_INT)
      .setLocation(LOCATION)
      .build();

  /** Place IDs for valid PlacesSearchResults used in tests. */
  private static final String PLACEID_1 = "ChIJN1t_tDeuEmsRUsoyG83frY4";
  private static final String PLACEID_2 = "ChIJ02qnq0KuEmsRHUJF4zo1x4I";

  /** Valid PlacesSearchResult. */
  private static final PlacesSearchResult SEARCH_RESULT_1 =
      createTestPlacesSearchResult(PLACEID_1);
  private static final PlacesSearchResult SEARCH_RESULT_2 =
      createTestPlacesSearchResult(PLACEID_2);

  /** An array of valid PlacesSearchResults. */
  private static final PlacesSearchResult[] SEARCH_RESULT_ARR = {SEARCH_RESULT_1, SEARCH_RESULT_2};

  /** Valid PlaceDetails. */
  private static final PlaceDetails PLACE_DETAILS_1 =
    createTestPlaceDetails("name1", PLACES_DETAILS_URL, PHONE, RATING, PRICELEVEL, LOCATION);
  private static final PlaceDetails PLACE_DETAILS_2 =
    createTestPlaceDetails("name2", PLACES_DETAILS_URL, PHONE, RATING, PRICELEVEL, LOCATION);

  /** Creates URL from String for PlaceDetails used in tests. */
  private static URL createTestURL(String s) {
    try {
      return new URL(s);
    } catch (MalformedURLException e) {
      System.out.println("url not generated");
      return null;
    }
  }

  private static PlaceDetails createTestPlaceDetails(
      String name, URL url, String phone, float rating, PriceLevel priceLevel, LatLng location) {
    PlaceDetails placeDetails = new PlaceDetails();
    placeDetails.name = name;
    placeDetails.website = url;
    placeDetails.formattedPhoneNumber = phone;
    placeDetails.rating = rating;
    placeDetails.priceLevel = priceLevel;
    placeDetails.geometry = new Geometry();
    placeDetails.geometry.location = location;
    return placeDetails;
  }

  private static PlacesSearchResult createTestPlacesSearchResult(String placeId) {
    PlacesSearchResult searchResult = new PlacesSearchResult();
    searchResult.placeId = placeId;
    return searchResult;
  }

  /** A PlacesFetcher instance to be tested. */
  private static PlacesFetcher placesFetcher = new PlacesFetcher();

  @Test
  public void fetch_zeroSearchResults_returnsEmptyList() throws Exception {
    PlacesFetcher spiedFetcher = spy(placesFetcher);
    doReturn(new PlacesSearchResult[0])
        .when(spiedFetcher).getPlacesSearchResults(any(TextSearchRequest.class));
    assertEquals(ImmutableList.of(), spiedFetcher.fetch());
  }

  @Test
  public void fetch_validSearchResults_returnsListOfPlaces()
      throws ApiException, InterruptedException, IOException {
    PlacesFetcher spiedFetcher = spy(placesFetcher);
    doReturn(PLACE_DETAILS_1).doReturn(PLACE_DETAILS_2)
        .when(spiedFetcher).getPlaceDetails(any(PlaceDetailsRequest.class));
    doReturn(SEARCH_RESULT_ARR)
        .when(spiedFetcher).getPlacesSearchResults(any(TextSearchRequest.class));
    assertEquals(ImmutableList.of(PLACE_1, PLACE_2), spiedFetcher.fetch());
  }
}
