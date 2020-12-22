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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
  private static final ImmutableList<String> CUISINES = ImmutableList.of("sushi", "hamburger");
  private static final boolean OPEN_NOW = true;
  private static final BusinessStatus BUSINESS_STATUS_1 =
      BusinessStatus.OPERATIONAL; // used for Places and UserPreferences
  private static final BusinessStatus BUSINESS_STATUS_2 =
      BusinessStatus.UNKNOWN; // used for Places and UserPreferences
  private static final String STRING_BUSINESS_STATUS_1 = "OPERATIONAL"; // used for PlaceDetails
  private static final String STRING_BUSINESS_STATUS_2 = null; // used for PlaceDetails
  private static final int MAX_NUM_OF_RADIUS_EXTENSIONS = 4;

  /** Place IDs for valid PlacesSearchResults used in tests. */
  private static final String PLACEID_1 = "ChIJN1t_tDeuEmsRUsoyG83frY4";
  private static final String PLACEID_2 = "ChIJ02qnq0KuEmsRHUJF4zo1x4I";

  /** Valid Place objects. */
  private static final Place.Builder PLACE_1_BUILDER =
      Place.builder()
      .setName("name1")
      .setWebsiteUrl(PLACE_WEBSITE)
      .setPhone(PHONE)
      .setRating(RATING)
      .setPriceLevel(PRICE_LEVEL_INT)
      .setLocation(LOCATION)
      .setGoogleUrl(PLACE_GOOGLE_URL)
      .setPlaceId(PLACEID_1)
      .setBusinessStatus(BUSINESS_STATUS_1);

  private static final Place.Builder PLACE_2_BUILDER =
      Place.builder()
      .setName("name2")
      .setWebsiteUrl(PLACE_WEBSITE)
      .setPhone(PHONE)
      .setRating(RATING)
      .setPriceLevel(PRICE_LEVEL_INT)
      .setLocation(LOCATION)
      .setGoogleUrl(PLACE_GOOGLE_URL)
      .setPlaceId(PLACEID_2)
      .setBusinessStatus(BUSINESS_STATUS_2);

  /** Valid UserPreferences builder. */
   private static final UserPreferences.Builder PREFERENCES_BUILDER =
      UserPreferences.builder()
      .setMinRating(RATING)
      .setMaxPriceLevel(PRICE_LEVEL_INT)
      .setLocation(LOCATION)
      .setCuisines(CUISINES)
      .setOpenNow(OPEN_NOW);

  /** Valid PlacesSearchResult. */
  private static final PlacesSearchResult SEARCH_RESULT_1 =
      createTestPlacesSearchResult(PLACEID_1);
  private static final PlacesSearchResult SEARCH_RESULT_2 =
      createTestPlacesSearchResult(PLACEID_2);

  /** An array of valid PlacesSearchResults. */
  private static final PlacesSearchResult[] SEARCH_RESULT_ARR =
      {SEARCH_RESULT_1, SEARCH_RESULT_2 };
  private static final PlacesSearchResult[] SEARCH_RESULT_1_ONLY_ARR =
      {SEARCH_RESULT_1};
  private static final PlacesSearchResult[] SEARCH_RESULT_2_ONLY_ARR =
      {SEARCH_RESULT_2};

  /** Valid PlaceDetails. */
  private static final PlaceDetails PLACE_DETAILS_1 =
      createTestPlaceDetails(
          "name1", PLACE_DETAILS_WEBSITE, PHONE, RATING, PRICE_LEVEL,
          LOCATION, PLACE_DETAILS_GOOGLE_URL, PLACEID_1, STRING_BUSINESS_STATUS_1);
  private static final PlaceDetails PLACE_DETAILS_2 =
      createTestPlaceDetails(
          "name2", PLACE_DETAILS_WEBSITE, PHONE, RATING, PRICE_LEVEL,
          LOCATION, PLACE_DETAILS_GOOGLE_URL, PLACEID_2, STRING_BUSINESS_STATUS_2);

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

  private static PlacesSearchResult createTestPlacesSearchResult(String placeId) {
    PlacesSearchResult searchResult = new PlacesSearchResult();
    searchResult.placeId = placeId;
    return searchResult;
  }

  /** A PlacesFetcher instance to be tested. */
  private static PlacesFetcher placesFetcher = new PlacesFetcher(GeoContext.getGeoApiContext());

  @Test
  public void fetch_zeroSearchResults_returnsEmptyList() throws Exception {
    PlacesFetcher spiedFetcher = spy(placesFetcher);
    doReturn(new PlacesSearchResult[0])
      .when(spiedFetcher)
      .getPlacesSearchResults(any(TextSearchRequest.class));
    ImmutableList<Place> output = spiedFetcher.fetch(PREFERENCES_BUILDER.build());
    verify(spiedFetcher, times(MAX_NUM_OF_RADIUS_EXTENSIONS))
      .getPlacesSearchResults(any(TextSearchRequest.class));
    assertEquals(ImmutableList.of(), output);
  }

  @Test
  public void fetch_validSearchResults_returnsListOfPlaces() throws Exception {
    PlacesFetcher spiedFetcher = spy(placesFetcher);
    Place place1 = PLACE_1_BUILDER.setCuisines(ImmutableList.of("sushi", "asian")).build();
    Place place2 = PLACE_2_BUILDER.setCuisines(ImmutableList.of("hamburger")).build();




    UserPreferences preferences =
        PREFERENCES_BUILDER.setCuisines(ImmutableList.of("sushi", "asian", "hamburger")).build();
    doReturn(PLACE_DETAILS_1)
      .doReturn(PLACE_DETAILS_2)
      .when(spiedFetcher)
      .getPlaceDetails(any(PlaceDetailsRequest.class));
    doReturn(SEARCH_RESULT_1_ONLY_ARR)
      .doReturn(SEARCH_RESULT_1_ONLY_ARR)
      .doReturn(SEARCH_RESULT_2_ONLY_ARR)
      .when(spiedFetcher)
      .getPlacesSearchResults(any(TextSearchRequest.class));
    assertEquals(
      ImmutableList.of(place1, place2), spiedFetcher.fetch(preferences));
  }

  @Test
  public void fetch_noPreferedCuisines_returnsListOfPlaces() throws Exception {
    PlacesFetcher spiedFetcher = spy(placesFetcher);
    doReturn(PLACE_DETAILS_1)
      .doReturn(PLACE_DETAILS_2)
      .when(spiedFetcher)
      .getPlaceDetails(any(PlaceDetailsRequest.class));
    doReturn(SEARCH_RESULT_ARR)
      .when(spiedFetcher)
      .getPlacesSearchResults(any(TextSearchRequest.class));
    assertEquals(
      ImmutableList.of(PLACE_1, PLACE_2),
      spiedFetcher.fetch(PREFERENCES_BUILDER.setCuisines(ImmutableList.of()).build()));
  }

  @Test
  public void fetch_noOpenNowPreference_returnsListOfPlaces() throws Exception {
    PlacesFetcher spiedFetcher = spy(placesFetcher);
    doReturn(PLACE_DETAILS_1)
      .doReturn(PLACE_DETAILS_2)
      .when(spiedFetcher)
      .getPlaceDetails(any(PlaceDetailsRequest.class));
    doReturn(SEARCH_RESULT_ARR)
      .when(spiedFetcher)
      .getPlacesSearchResults(any(TextSearchRequest.class));
    assertEquals(
      ImmutableList.of(PLACE_1, PLACE_2),
      spiedFetcher.fetch(PREFERENCES_BUILDER.setOpenNow(false).build()));
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
  public void createCuisinesQuery_getsValidCuisines_returnsQuery() throws Exception {
    assertEquals(
      "sushi|burger|hamburger", placesFetcher.createCuisinesQuery(CUISINES));
  }

  @Test
  public void createCuisinesQuery_getsAnEmptyListOfCuisines_returnsEmptyQuery() throws Exception {
    assertEquals("", placesFetcher.getSearchWords(ImmutableList.of()));
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
    doReturn(new PlacesSearchResult[0])
      .doReturn(new PlacesSearchResult[] {SEARCH_RESULT_1 })
      .when(spiedFetcher)
      .getPlacesSearchResults(any(TextSearchRequest.class));
    doReturn(PLACE_DETAILS_1)
      .when(spiedFetcher)
      .getPlaceDetails(any(PlaceDetailsRequest.class));
    assertEquals(
      ImmutableList.of(PLACE_1), spiedFetcher.fetch(PREFERENCES_BUILDER.build()));
  }
}
