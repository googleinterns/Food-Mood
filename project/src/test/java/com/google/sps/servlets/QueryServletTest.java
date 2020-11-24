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
import static org.mockito.Mockito.mock;
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
import com.google.sps.data.PlacesFetcher;
import com.google.sps.data.UserPreferences;
import com.google.sps.data.FetcherException;
import com.google.sps.data.Place;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.maps.model.LatLng;

@RunWith(JUnit4.class)
public final class QueryServletTest {

  private static final HttpServletRequest REQUEST = mock(HttpServletRequest.class);
  private static final HttpServletResponse RESPONSE = mock(HttpServletResponse.class);
  private static final PlacesFetcher FETCHER = mock(PlacesFetcher.class);
  private StringWriter responseStringWriter;
  private PrintWriter responsePrintWriter;
  private QueryServlet servlet;

  @Before
  public void setUp() throws Exception {
    responseStringWriter = new StringWriter();
    responsePrintWriter = new PrintWriter(responseStringWriter);
    servlet = new QueryServlet();
    servlet.init(FETCHER);
    when(RESPONSE.getWriter()).thenReturn(responsePrintWriter);
    initializeRequestParameters();
  }

  @Test
  public void getRequest_fetchedMoreThanMaxNumPlaces_respondMaxNumPlaces() throws Exception {
    ImmutableList<Place> placesListWithMoreThanMaxNum =
        createPlacesListBySize(QueryServlet.MAX_NUM_PLACES_TO_RECOMMEND + 1);
    when(FETCHER.fetch(any(UserPreferences.class))).thenReturn(placesListWithMoreThanMaxNum);

    servlet.doGet(REQUEST, RESPONSE);

    assertEquals(getPlacesAmountInResponse(), QueryServlet.MAX_NUM_PLACES_TO_RECOMMEND);
  }

  @Test
  public void getRequest_fetchedLessThanMaxNumPlaces_respondAllFetchedPlaces() throws Exception {
    int numOfFetchedPlaces = QueryServlet.MAX_NUM_PLACES_TO_RECOMMEND - 1;
    ImmutableList<Place> placesListWithLessThanMaxNum = createPlacesListBySize(numOfFetchedPlaces);
    when(FETCHER.fetch(any(UserPreferences.class))).thenReturn(placesListWithLessThanMaxNum);

    servlet.doGet(REQUEST, RESPONSE);

    assertEquals(getPlacesAmountInResponse(), numOfFetchedPlaces);
  }

  @Test
  public void getRequest_fetchedPlacesShouldBeFiltered_filterPlaces() throws Exception {
    when(REQUEST.getParameter("rating")).thenReturn("4");
    Place validPlace = createValidPlaceBuilder().setName("validPlace").build();
    Place lowRating = createValidPlaceBuilder().setRating(3).setName("lowRatingPlace").build();
    Place noWebsite = createValidPlaceBuilder().setWebsiteUrl("").setName("noWebsitePlace").build();
    // The following place purposely has the same name as the valid place, so that we make sure that
    // we end up with only one of them.
    Place validPlace2 = createValidPlaceBuilder().setName("validPlace").build();
    ImmutableList<Place> places =
        ImmutableList.of(validPlace, lowRating, noWebsite, validPlace2);
    when(FETCHER.fetch(any(UserPreferences.class))).thenReturn(places);

    servlet.doGet(REQUEST, RESPONSE);

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
  public void getRequest_fetcherException_forwardException() throws Exception {
    when(FETCHER.fetch(any(UserPreferences.class))).thenThrow(FetcherException.class);

    servlet.doGet(REQUEST, RESPONSE);

    verify(RESPONSE).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
        "Fetching from Google Places API encountered a problem");
  }

  @Test
  public void getRequest_userPreferencesBuildException_forwardException() throws Exception {
    // The rating parameter cannot be negative, so this should cause the UserPreferences builder to
    // throw an IllegalArgumentException
    when(REQUEST.getParameter("rating")).thenReturn("-5");

    servlet.doGet(REQUEST, RESPONSE);

    verify(RESPONSE).sendError(HttpServletResponse.SC_BAD_REQUEST,
        "Parsing the user preferences encountered a problem");
  }

  @Test
  // This test checks that the user preferences are processed correctly based on the given
  // parameters. The crucial point of the test is that the strings that are given as parameters
  // match the expect user preferences.
  public void getRequest_userPreferencesForwardedToFetcher() throws Exception {
    when(REQUEST.getParameter("rating")).thenReturn("4");
    when(REQUEST.getParameter("price")).thenReturn("3");
    when(REQUEST.getParameter("open")).thenReturn("1");
    when(REQUEST.getParameter("location")).thenReturn("35.35000000,30.00000000");
    when(REQUEST.getParameter("cuisines")).thenReturn("sushi,hamburger");
    UserPreferences expectedUserPrefs = UserPreferences.builder()
        .setMinRating(4)
        .setMaxPriceLevel(3)
        .setOpenNow(true)
        .setLocation(new LatLng(35.35000000, 30.00000000))
        .setCuisines(ImmutableList.of("sushi", "hamburger"))
        .build();

    servlet.doGet(REQUEST, RESPONSE);

    verify(FETCHER).fetch(expectedUserPrefs);
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

  private static Place.Builder createValidPlaceBuilder() {
    return Place.builder()
          .setName("name")
          .setWebsiteUrl("website@google.com")
          .setPhone("+97250-0000-000")
          .setRating(4)
          .setPriceLevel(3)
          .setLocation(new LatLng(35.35, 30.30));
  }

  // Returns the number of json elements in the servlet's response
  private int getPlacesAmountInResponse() {
    return new Gson()
        .fromJson(responseStringWriter.getBuffer().toString(), JsonArray.class)
        .size();
  }

  // Initialize the parameters for the mocked http request, with constant valid values.
  private void initializeRequestParameters() {
    when(REQUEST.getParameter("rating")).thenReturn("4");
    when(REQUEST.getParameter("price")).thenReturn("3");
    when(REQUEST.getParameter("open")).thenReturn("1");
    when(REQUEST.getParameter("location")).thenReturn("30.30,35.35");
    when(REQUEST.getParameter("cuisines")).thenReturn("sushi");
  }
}
