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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import com.google.common.collect.ImmutableList;
import com.google.maps.PlaceDetailsRequest;
import com.google.maps.TextSearchRequest;
import com.google.maps.model.Geometry;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.PlacesSearchResult;
import com.google.maps.model.PriceLevel;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class PlacesFetcherTest {

  private static final String PLACE_URL = "https://www.google.com/"; // used for Places
  private static final URL PLACES_DETAILS_URL = createTestURL(PLACE_URL); // used for PlaceDetails
  private static final String PHONE = "+97250-0000-000";
  private static final LatLng LOCATION = new LatLng(32.08074, 34.78059);
  private static final float RATING = 4;
  private static final int PRICELEVEL_INT = 2; // used for Places and UserPrefrences
  private static final PriceLevel PRICELEVEL = PriceLevel.MODERATE; // used for PlaceDetails
  private static final ImmutableList<String> CUISINES = ImmutableList.of("sushi", "burger");
  private static final boolean OPEN_NOW = true;

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

  /** Valid UserPrefrences builder. */
   private static final UserPrefrences.Builder prefrencesBuilder =
      UserPrefrences.builder()
      .setMinRating(RATING)
      .setMaxPriceLevel(PRICELEVEL_INT)
      .setLocation(LOCATION)
      .setCuisines(CUISINES)
      .setOpenNow(OPEN_NOW);

  /** Place IDs for valid PlacesSearchResults used in tests. */
  private static final String PLACEID_1 = "ChIJN1t_tDeuEmsRUsoyG83frY4";
  private static final String PLACEID_2 = "ChIJ02qnq0KuEmsRHUJF4zo1x4I";

  /** Valid PlacesSearchResult. */
  private static final PlacesSearchResult SEARCH_RESULT_1 =
      createTestPlacesSearchResult(PLACEID_1);
  private static final PlacesSearchResult SEARCH_RESULT_2 =
      createTestPlacesSearchResult(PLACEID_2);

  /** An array of valid PlacesSearchResults. */
  private static final PlacesSearchResult[] SEARCH_RESULT_ARR =
      {SEARCH_RESULT_1, SEARCH_RESULT_2 };

  /** Valid PlaceDetails. */
  private static final PlaceDetails PLACE_DETAILS_1 =
      createTestPlaceDetails("name1", PLACES_DETAILS_URL, PHONE, RATING, PRICELEVEL, LOCATION);
  private static final PlaceDetails PLACE_DETAILS_2 =
      createTestPlaceDetails("name2", PLACES_DETAILS_URL, PHONE, RATING, PRICELEVEL, LOCATION);

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
    doReturn(new PlacesSearchResult[0]).when(spiedFetcher)
      .getPlacesSearchResults(any(TextSearchRequest.class));
    assertEquals(ImmutableList.of(), spiedFetcher.fetch(prefrencesBuilder.build()));
  }

  @Test
  public void fetch_validSearchResults_returnsListOfPlaces() throws Exception {
    PlacesFetcher spiedFetcher = spy(placesFetcher);
    doReturn(PLACE_DETAILS_1).
    doReturn(PLACE_DETAILS_2).when(spiedFetcher)
        .getPlaceDetails(any(PlaceDetailsRequest.class));
    doReturn(SEARCH_RESULT_ARR).when(spiedFetcher)
        .getPlacesSearchResults(any(TextSearchRequest.class));
    assertEquals(
      ImmutableList.of(PLACE_1, PLACE_2), spiedFetcher.fetch(prefrencesBuilder.build()));
  }

  @Test
  public void fetch_noPreferedCuisines_returnsListOfPlaces() throws Exception {
    PlacesFetcher spiedFetcher = spy(placesFetcher);
    doReturn(PLACE_DETAILS_1).
    doReturn(PLACE_DETAILS_2).when(spiedFetcher)
        .getPlaceDetails(any(PlaceDetailsRequest.class));
    doReturn(SEARCH_RESULT_ARR).when(spiedFetcher)
        .getPlacesSearchResults(any(TextSearchRequest.class));
    assertEquals(
      ImmutableList.of(PLACE_1, PLACE_2),
      spiedFetcher.fetch(prefrencesBuilder.setCuisines(ImmutableList.of()).build()));
  }

  @Test
  public void fetch_noOpenNowPreference_returnsListOfPlaces() throws Exception {
    PlacesFetcher spiedFetcher = spy(placesFetcher);
    doReturn(PLACE_DETAILS_1).
    doReturn(PLACE_DETAILS_2).when(spiedFetcher)
        .getPlaceDetails(any(PlaceDetailsRequest.class));
    doReturn(SEARCH_RESULT_ARR).when(spiedFetcher)
        .getPlacesSearchResults(any(TextSearchRequest.class));
    assertEquals(
      ImmutableList.of(PLACE_1, PLACE_2),
      spiedFetcher.fetch(prefrencesBuilder.setOpenNow(false).build()));
  }

  @Test
  public void fetch_ResultsQueryFails_throwsFetcherException() throws Exception {
    PlacesFetcher spiedFetcher = spy(placesFetcher);
    doThrow(new IOException()).when(spiedFetcher)
        .getPlacesSearchResults(any(TextSearchRequest.class));
    FetcherException thrown =
        assertThrows(FetcherException.class, () -> spiedFetcher.fetch(prefrencesBuilder.build()));
    assertTrue(thrown.getCause() instanceof IOException);
    assertTrue(thrown.getMessage().contains("fetch places"));
  }

  @Test
  public void fetch_PlaceDetailsQueryFails_throwsFetcherException() throws Exception {
    PlacesFetcher spiedFetcher = spy(placesFetcher);
    doReturn(SEARCH_RESULT_ARR).when(spiedFetcher)
        .getPlacesSearchResults(any(TextSearchRequest.class));
    doThrow(new IOException()).when(spiedFetcher)
        .getPlaceDetails(any(PlaceDetailsRequest.class));
    FetcherException thrown =
        assertThrows(FetcherException.class, () -> spiedFetcher.fetch(prefrencesBuilder.build()));
    assertTrue(thrown.getCause() instanceof IOException);
    assertTrue(thrown.getMessage().contains("place details"));
  }
}
