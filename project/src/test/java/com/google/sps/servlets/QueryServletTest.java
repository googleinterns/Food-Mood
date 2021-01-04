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

package com.google.sps.servlets;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import com.google.sps.data.PlacesFetcher;
import com.google.sps.data.UserPreferences;
import com.google.sps.data.FetcherException;
import com.google.sps.data.PlacesScorer;
import com.google.sps.data.Place;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.maps.model.LatLng;
import com.google.sps.data.BusinessStatus;
import com.google.sps.data.DataAccessor;
import com.google.sps.data.UserVerifier;

@RunWith(JUnit4.class)
public final class QueryServletTest {

  private static final String RATING = "4";
  private static final String PRICE_LEVEL = "3";
  private static final String OPEN_NOW_STRING = "1";
  private static final Boolean OPEN_NOW = true;
  private static final String LOCATION_STRING = "30.30,35.35";
  private static final LatLng LOCATION = new LatLng(30.30, 35.35);
  private static final String CUISINES_STRING = "sushi,hamburger";
  private static final ImmutableList<String> CUISINES = ImmutableList.of("sushi", "hamburger");
  private static final String VALID_ID_TOKEN = "token";
  private static final HttpServletRequest REQUEST = mock(HttpServletRequest.class);
  private static final HttpServletResponse RESPONSE = mock(HttpServletResponse.class);
  private static final PlacesFetcher FETCHER = mock(PlacesFetcher.class);
  private static final PlacesScorer SCORER = mock(PlacesScorer.class);
  private static final UserVerifier USER_VERIFIER = mock(UserVerifier.class);
  private static final DataAccessor DATA_ACCESSOR = mock(DataAccessor.class);
  private StringWriter responseStringWriter;
  private PrintWriter responsePrintWriter;
  private QueryServlet servlet;

  @Before
  public void setUp() throws Exception {
    responseStringWriter = new StringWriter();
    responsePrintWriter = new PrintWriter(responseStringWriter);
    servlet = new QueryServlet();
    servlet.init(FETCHER, SCORER, USER_VERIFIER, DATA_ACCESSOR);
    when(RESPONSE.getWriter()).thenReturn(responsePrintWriter);
    initializeRequestParameters();
    clearInvocations(DATA_ACCESSOR);
    clearInvocations(FETCHER);
  }

  @Test
  public void postRequest_fetchedMoreThanMaxNumPlaces_respondMaxNumPlaces() throws Exception {
    ImmutableList<Place> placesListWithMoreThanMaxNum =
        createPlacesListBySize(QueryServlet.MAX_NUM_PLACES_TO_RECOMMEND + 1);
    when(FETCHER.fetch(any(UserPreferences.class)))
        .thenReturn(placesListWithMoreThanMaxNum);
    when(SCORER.getScores(eq(placesListWithMoreThanMaxNum), any(LatLng.class)))
        .thenReturn(createScoreMap(placesListWithMoreThanMaxNum));

    servlet.doPost(REQUEST, RESPONSE);

    assertEquals(getPlacesAmountInResponse(), QueryServlet.MAX_NUM_PLACES_TO_RECOMMEND);
  }

  @Test
  public void postRequest_fetchedLessThanMaxNumPlaces_respondAllFetchedPlaces() throws Exception {
    int numOfFetchedPlaces = QueryServlet.MAX_NUM_PLACES_TO_RECOMMEND - 1;
    ImmutableList<Place> placesListWithLessThanMaxNum = createPlacesListBySize(numOfFetchedPlaces);
    when(FETCHER.fetch(any(UserPreferences.class)))
        .thenReturn(placesListWithLessThanMaxNum);
    when(SCORER.getScores(eq(placesListWithLessThanMaxNum), any(LatLng.class)))
        .thenReturn(createScoreMap(placesListWithLessThanMaxNum));

    servlet.doPost(REQUEST, RESPONSE);

    assertEquals(getPlacesAmountInResponse(), numOfFetchedPlaces);
  }

  @Test
  public void postRequest_fetchedPlacesShouldBeFiltered_filterPlaces() throws Exception {
    when(REQUEST.getParameter("rating")).thenReturn("4");
    Place validPlace = createValidPlaceBuilder().setName("validPlace").build();
    Place lowRating = createValidPlaceBuilder().setRating(3).setName("lowRatingPlace").build();
    Place noWebsite = createValidPlaceBuilder()
        .setWebsiteUrl("")
        .setGoogleUrl("")
        .setName("noWebsitePlace")
        .build();
    // The following place purposely has the same name as the valid place, so that we make sure that
    // we end up with only one of them.
    Place validPlace2 = createValidPlaceBuilder().setName("validPlace").build();
    ImmutableList<Place> places =
        ImmutableList.of(validPlace, lowRating, noWebsite, validPlace2);
    ImmutableList<Place> filteredPlaces =
        ImmutableList.of(validPlace, lowRating, noWebsite);
    when(FETCHER.fetch(any(UserPreferences.class)))
        .thenReturn(places);
    when(SCORER.getScores(eq(filteredPlaces), any(LatLng.class)))
        .thenReturn(createScoreMap(filteredPlaces));

    servlet.doPost(REQUEST, RESPONSE);

    assertEquals(getPlacesAmountInResponse(), 1);
    assertEquals(
        new Gson().fromJson(responseStringWriter.getBuffer().toString(), JsonArray.class)
            .get(0) // The first element in the array (first place)
            .getAsJsonObject()
            .get("name")
            .getAsString(),
        "validPlace");
  }

  @Test
  public void postRequest_fetcherException_forwardException() throws Exception {
    when(FETCHER.fetch(any(UserPreferences.class))).thenThrow(FetcherException.class);

    servlet.doPost(REQUEST, RESPONSE);

    verify(RESPONSE).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
        "Fetching from Google Places API encountered a problem");
  }

  @Test
  public void postRequest_userPreferencesBuildException_forwardException() throws Exception {
    // The rating parameter cannot be negative, so this should cause the UserPreferences builder to
    // throw an IllegalArgumentException
    when(REQUEST.getParameter("rating")).thenReturn("-5");

    servlet.doPost(REQUEST, RESPONSE);

    verify(RESPONSE).sendError(HttpServletResponse.SC_BAD_REQUEST,
        "Parsing the user preferences encountered a problem");
  }

  @Test
  // This test checks that the user preferences are processed correctly based on the given
  // parameters. The crucial point of the test is that the strings that are given as parameters
  // match the expect user preferences.
  public void postRequest_userPreferencesForwardedToFetcher() throws Exception {
    UserPreferences expectedUserPrefs = createUserPreferences();

    servlet.doPost(REQUEST, RESPONSE);

    verify(FETCHER).fetch(expectedUserPrefs);
  }

  @Test
  // This test checks that the PlacesScorer is called with the expected parameters
  public void postRequest_placesAndUserLocationForwadedToScorer() throws Exception {
    ImmutableList<Place> places = createPlacesListBySize(1);
    when(FETCHER.fetch(any(UserPreferences.class))).thenReturn(places);
    when(REQUEST.getParameter("location")).thenReturn("00.00000000,00.00000000");

    servlet.doPost(REQUEST, RESPONSE);

    verify(SCORER).getScores(places, new LatLng(00, 00));
  }

  @Test
  // This test checks that storeUserPreferences is called with the expected parameters
  // when the servlet gets a valid ID token
  public void postRequest_validIdToken_userIdAndPreferencesForwardedForStoring() throws Exception {
    when(FETCHER.fetch(any(UserPreferences.class))).thenReturn(ImmutableList.of());
    when(USER_VERIFIER.getUserIdByToken(VALID_ID_TOKEN)).thenReturn(Optional.of("userId"));
    UserPreferences expectedUserPrefs = createUserPreferences();

    servlet.doPost(REQUEST, RESPONSE);

    verify(DATA_ACCESSOR).storeUserPreferences("userId", expectedUserPrefs);
  }

  @Test
  // This test checks that storeUserPreferences is not called
  // when the servlet gets an invalid ID token
  public void postRequest_invalidIdToken_userPreferencesAreNotStored() throws Exception {
    when(REQUEST.getParameter("idToken")).thenReturn("");
    when(FETCHER.fetch(any(UserPreferences.class))).thenReturn(ImmutableList.of());
    when(USER_VERIFIER.getUserIdByToken("")).thenReturn(Optional.empty());

    servlet.doPost(REQUEST, RESPONSE);

    verify(DATA_ACCESSOR, never())
        .storeUserPreferences(any(String.class), any(UserPreferences.class));
  }

  // Returns an immutable list that has the required number of Place elements. All elements are
  // identical except for their name, which is serialized - '0', '1', '2', etc.
  private static ImmutableList<Place> createPlacesListBySize(int numOfPlaces) {
    ImmutableList.Builder<Place> places = ImmutableList.builder();
    for (int i = 0; i < numOfPlaces; ++i) {
      places.add(createValidPlaceBuilder()
          .setName(String.valueOf(i))
          .build()
      );
    }
    return places.build();
  }

  // Returns an immutable map of all places on the places list scored by 1
  private static ImmutableMap<Place, Double> createScoreMap(ImmutableList<Place> places) {
    return places.stream()
        .collect(ImmutableMap.toImmutableMap(place -> place, place -> 1d));
  }

  private static Place.Builder createValidPlaceBuilder() {
    return Place.builder()
          .setName("name")
          .setWebsiteUrl("website@google.com")
          .setPhone("+97250-0000-000")
          .setRating(4)
          .setPriceLevel(3)
          .setLocation(new LatLng(35.35, 30.30))
          .setBusinessStatus(BusinessStatus.OPERATIONAL)
          .setGoogleUrl("google.com")
          .setPlaceId("placeId")
          .setCuisines(ImmutableList.of("sushi", "hamburger"));
  }

  // Returns the number of json elements in the servlet's response
  private int getPlacesAmountInResponse() {
    return new Gson()
        .fromJson(responseStringWriter.getBuffer().toString(), JsonArray.class)
        .size();
  }

  // Initialize the parameters for the mocked http request, with constant valid values.
  private void initializeRequestParameters() {
    when(REQUEST.getParameter("rating")).thenReturn(RATING);
    when(REQUEST.getParameter("price")).thenReturn(PRICE_LEVEL);
    when(REQUEST.getParameter("open")).thenReturn(OPEN_NOW_STRING);
    when(REQUEST.getParameter("location")).thenReturn(LOCATION_STRING);
    when(REQUEST.getParameter("cuisines")).thenReturn(CUISINES_STRING);
    when(REQUEST.getParameter("idToken")).thenReturn(VALID_ID_TOKEN);
  }

  private static UserPreferences createUserPreferences() {
    return UserPreferences.builder()
      .setMinRating(Float.parseFloat(RATING))
      .setMaxPriceLevel(Integer.parseInt(PRICE_LEVEL))
      .setOpenNow(OPEN_NOW)
      .setLocation(LOCATION)
      .setCuisines(CUISINES)
      .build();
  }
}
