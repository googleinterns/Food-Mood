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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
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
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentMatcher;

@RunWith(JUnit4.class)
public final class PlacesFetcherTest {

  private static final String PLACE_WEBSITE = "https://www.wikipedia.org/"; // used for Places
  private static final String PLACE_GOOGLE_URL = "https://www.google.com/"; // used for Places
  private static final URL PLACE_DETAILS_WEBSITE =
      createTestURL(PLACE_WEBSITE); // used for PlaceDetails
  private static final URL PLACE_DETAILS_GOOGLE_URL =
      createTestURL(PLACE_GOOGLE_URL); // used for PlaceDetails
  private static final String PHONE = "+97250-0000-000";
  private static final LatLng LOCATION = new LatLng(32.08074, 34.78059);
  private static final float RATING = 4;
  private static final int PRICE_LEVEL_INT = 2; // used for Places and UserPreferences
  private static final PriceLevel PRICE_LEVEL = PriceLevel.MODERATE; // used for PlaceDetails
  private static final ImmutableList<String> CUISINES_LIST =
      ImmutableList.of("sushi", "hamburger"); // used for UserPreferences
  private static final ImmutableSet<String> CUISINES_SET =
      ImmutableSet.of("sushi", "hamburger"); // used for places
  private static final boolean OPEN_NOW = true;
  private static final BusinessStatus BUSINESS_STATUS =
      BusinessStatus.UNKNOWN; // used for Places and UserPreferences
  private static final String STRING_BUSINESS_STATUS = null; // used for PlaceDetails
  private static final int MAX_NUM_OF_RADIUS_EXTENSIONS = 4;

  /** Place IDs and names for valid PlacesSearchResults used in tests. */
  private static final String PLACEID_1 = "ChIJN1t_tDeuEmsRUsoyG83frY4";
  private static final String PLACEID_2 = "ChIJ02qnq0KuEmsRHUJF4zo1x4I";
  private static final String NAME_1 = "name1";
  private static final String NAME_2 = "name2";

  /** Valid UserPreferences builder. */
   private static final UserPreferences.Builder PREFERENCES_BUILDER =
      UserPreferences.builder()
      .setMinRating(RATING)
      .setMaxPriceLevel(PRICE_LEVEL_INT)
      .setLocation(LOCATION)
      .setCuisines(CUISINES_LIST)
      .setOpenNow(OPEN_NOW);

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
      createTestPlaceDetails(
          NAME_1, PLACE_DETAILS_WEBSITE, PHONE, RATING, PRICE_LEVEL,
          LOCATION, PLACE_DETAILS_GOOGLE_URL, PLACEID_1, STRING_BUSINESS_STATUS);
  private static final PlaceDetails PLACE_DETAILS_2 =
      createTestPlaceDetails(
          NAME_2, PLACE_DETAILS_WEBSITE, PHONE, RATING, PRICE_LEVEL,
          LOCATION, PLACE_DETAILS_GOOGLE_URL, PLACEID_2, STRING_BUSINESS_STATUS);

  private static URL createTestURL(String s) {
    try {
      return new URL(s);
    } catch (MalformedURLException e) {
      System.out.println("url not generated");
      return null;
    }
  }

  private static PlaceDetails createTestPlaceDetails(
        String name, URL website, String phone, float rating,
        PriceLevel priceLevel, LatLng location, URL googleUrl, String id, String status) {
    PlaceDetails placeDetails = new PlaceDetails();
    placeDetails.name = name;
    placeDetails.website = website;
    placeDetails.formattedPhoneNumber = phone;
    placeDetails.rating = rating;
    placeDetails.priceLevel = priceLevel;
    placeDetails.geometry = new Geometry();
    placeDetails.geometry.location = location;
    placeDetails.url = googleUrl;
    placeDetails.placeId = id;
    placeDetails.businessStatus = status;
    return placeDetails;
  }

  private static Place createValidPlace(
        String name, String placeId, ImmutableSet<String> cuisines) {
    return Place.builder()
    .setName(name)
    .setWebsiteUrl(PLACE_WEBSITE)
    .setPhone(PHONE)
    .setRating(RATING)
    .setPriceLevel(PRICE_LEVEL_INT)
    .setLocation(LOCATION)
    .setGoogleUrl(PLACE_GOOGLE_URL)
    .setPlaceId(placeId)
    .setBusinessStatus(BUSINESS_STATUS)
    .setCuisines(cuisines)
    .build();
  }


  private static PlacesSearchResult createTestPlacesSearchResult(String placeId) {
    PlacesSearchResult searchResult = new PlacesSearchResult();
    searchResult.placeId = placeId;
    return searchResult;
  }

  private static ArgumentMatcher<FakeSearchRequestGenerator.FakeSearchRequest>
      matchesSearchRequest(final String cuisine) {
    return request -> request != null
        && Arrays.asList(request.searchWords.split("\\|")).contains(cuisine);
  }

  private static ArgumentMatcher<FakePlaceDetailsRequestGenerator.FakePlaceDetailsRequest>
      matchesDetailsRequest(final String placeId) {
    return request -> request != null
        && request.placeId.equals(placeId);
  }

  /** A PlacesFetcher instance to be tested. */
  private static PlacesFetcher placesFetcher =
      new PlacesFetcher(
        new FakeSearchRequestGenerator(GeoContext.getGeoApiContext()),
        new FakePlaceDetailsRequestGenerator(GeoContext.getGeoApiContext()));

  @Test
  public void fetch_zeroSearchResults_returnsEmptyList() throws Exception {
    PlacesFetcher spiedFetcher = spy(placesFetcher);
    doReturn(new PlacesSearchResult[0])
      .when(spiedFetcher)
      .getPlacesSearchResults(any(TextSearchRequest.class));
    ImmutableList<Place> output = spiedFetcher.fetch(PREFERENCES_BUILDER.build());
    verify(spiedFetcher, times(MAX_NUM_OF_RADIUS_EXTENSIONS * CUISINES_LIST.size()))
      .getPlacesSearchResults(any(TextSearchRequest.class));
    assertEquals(ImmutableList.of(), output);
  }

  @Test
  public void fetch_validSearchResults_returnsListOfPlaces() throws Exception {
    PlacesFetcher spiedFetcher = spy(placesFetcher);
    Place place1 = createValidPlace(NAME_1, PLACEID_1, ImmutableSet.of("sushi", "asian"));
    Place place2 = createValidPlace(NAME_2, PLACEID_2, ImmutableSet.of("hamburger"));
    UserPreferences userPrefs =
        PREFERENCES_BUILDER.setCuisines(ImmutableList.of("sushi", "asian", "hamburger")).build();
    doReturn(PLACE_DETAILS_1)
      .when(spiedFetcher)
      .getPlaceDetails(argThat(matchesDetailsRequest(PLACEID_1)));
    doReturn(PLACE_DETAILS_2)
      .when(spiedFetcher)
      .getPlaceDetails(argThat(matchesDetailsRequest(PLACEID_2)));
    doReturn(new PlacesSearchResult[] {SEARCH_RESULT_1 })
      .when(spiedFetcher)
      .getPlacesSearchResults(argThat(matchesSearchRequest("sushi")));
    doReturn(new PlacesSearchResult[] {SEARCH_RESULT_1 })
      .when(spiedFetcher)
      .getPlacesSearchResults(argThat(matchesSearchRequest("asian")));
    doReturn(new PlacesSearchResult[] {SEARCH_RESULT_2 })
      .when(spiedFetcher)
      .getPlacesSearchResults(argThat(matchesSearchRequest("hamburger")));
    assertEquals(
      ImmutableList.of(place1, place2), spiedFetcher.fetch(userPrefs));
  }

  @Test
  public void fetch_noPreferedCuisines_returnsListOfPlaces() throws Exception {
    PlacesFetcher spiedFetcher = spy(placesFetcher);
    Place place1 = createValidPlace(NAME_1, PLACEID_1, ImmutableSet.of("sushi"));
    Place place2 = createValidPlace(NAME_2, PLACEID_2, ImmutableSet.of("sushi"));
    UserPreferences prefsNoCuisines = PREFERENCES_BUILDER.setCuisines(ImmutableList.of()).build();
    doReturn(PLACE_DETAILS_1)
      .when(spiedFetcher)
      .getPlaceDetails(argThat(matchesDetailsRequest(PLACEID_1)));
    doReturn(PLACE_DETAILS_2)
      .when(spiedFetcher)
      .getPlaceDetails(argThat(matchesDetailsRequest(PLACEID_2)));
    doReturn(SEARCH_RESULT_ARR)
      .when(spiedFetcher)
      .getPlacesSearchResults(argThat(matchesSearchRequest("sushi")));
    doReturn(new PlacesSearchResult[0])
      .when(spiedFetcher)
      .getPlacesSearchResults(
          argThat(
            (ArgumentMatcher<FakeSearchRequestGenerator.FakeSearchRequest>) request
                -> !request.searchWords.contains("sushi")));
    assertEquals(
      ImmutableList.of(place1, place2),
      spiedFetcher.fetch(prefsNoCuisines));
  }

  @Test
  public void fetch_noOpenNowPreference_returnsListOfPlaces() throws Exception {
    PlacesFetcher spiedFetcher = spy(placesFetcher);
    Place place1 = createValidPlace(NAME_1, PLACEID_1, CUISINES_SET);
    Place place2 = createValidPlace(NAME_2, PLACEID_2, CUISINES_SET);
    UserPreferences userPrefs =
        PREFERENCES_BUILDER.setCuisines(CUISINES_LIST).setOpenNow(false).build();
    doReturn(PLACE_DETAILS_1)
      .when(spiedFetcher)
      .getPlaceDetails(argThat(matchesDetailsRequest(PLACEID_1)));
    doReturn(PLACE_DETAILS_2)
      .when(spiedFetcher)
      .getPlaceDetails(argThat(matchesDetailsRequest(PLACEID_2)));
    doReturn(SEARCH_RESULT_ARR)
      .when(spiedFetcher)
      .getPlacesSearchResults(argThat(matchesSearchRequest(CUISINES_LIST.get(0))));
    doReturn(SEARCH_RESULT_ARR)
      .when(spiedFetcher)
      .getPlacesSearchResults(argThat(matchesSearchRequest(CUISINES_LIST.get(1))));
    assertEquals(
      ImmutableList.of(place1, place2),
      spiedFetcher.fetch(userPrefs));
  }

  @Test
  public void fetch_ResultsQueryFails_throwsFetcherException() throws Exception {
    PlacesFetcher spiedFetcher = spy(placesFetcher);
    doThrow(new IOException())
      .when(spiedFetcher)
      .getPlacesSearchResults(any(TextSearchRequest.class));
    FetcherException thrown =
        assertThrows(FetcherException.class, () -> spiedFetcher.fetch(PREFERENCES_BUILDER.build()));
    assertTrue(thrown.getCause() instanceof IOException);
    assertTrue(thrown.getMessage().contains("fetch places"));
  }

  @Test
  public void fetch_PlaceDetailsQueryFails_throwsFetcherException() throws Exception {
    PlacesFetcher spiedFetcher = spy(placesFetcher);
    doReturn(SEARCH_RESULT_ARR)
      .when(spiedFetcher)
      .getPlacesSearchResults(any(TextSearchRequest.class));
    doThrow(new IOException())
      .when(spiedFetcher)
      .getPlaceDetails(any(PlaceDetailsRequest.class));
    FetcherException thrown =
        assertThrows(FetcherException.class, () -> spiedFetcher.fetch(PREFERENCES_BUILDER.build()));
    assertTrue(thrown.getCause() instanceof IOException);
    assertTrue(thrown.getMessage().contains("place details"));
  }

  @Test
  public void getSearchWords_getsValidCuisines_returnsQuery() throws Exception {
    assertEquals(
      "burger|hamburger", placesFetcher.getSearchWords("hamburger"));
  }

  @Test
  public void getSearchWords_getsInvalidCuisine_throwsFetcherException() throws Exception {
    FetcherException thrown =
        assertThrows(
            FetcherException.class,
            () -> placesFetcher.getSearchWords("blah"));
    assertTrue(thrown.getCause() instanceof NullPointerException);
    assertTrue(thrown.getMessage().contains("invalid cuisine"));
  }

  @Test
  public void fetch_resultsOnlyAfterRadiusExtension_returnsListOfPlaces() throws Exception {
    PlacesFetcher spiedFetcher = spy(placesFetcher);
    UserPreferences userPrefs =
        PREFERENCES_BUILDER.setCuisines(CUISINES_LIST).setOpenNow(true).build();
    Place place1 = createValidPlace(NAME_1, PLACEID_1, CUISINES_SET);
    doReturn(new PlacesSearchResult[0])
      .doReturn(new PlacesSearchResult[] {SEARCH_RESULT_1 })
      .when(spiedFetcher)
      .getPlacesSearchResults(any(TextSearchRequest.class));
    doReturn(PLACE_DETAILS_1)
      .when(spiedFetcher)
      .getPlaceDetails(argThat(matchesDetailsRequest(PLACEID_1)));
    assertEquals(
      ImmutableList.of(place1), spiedFetcher.fetch(userPrefs));
  }
}
