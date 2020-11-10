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
  }

  @Test
  public void getRequest_fetchedMoreThanMaxNumPlaces_respondMaxNumPlaces() throws Exception {
    setRequestParameters(/*cuisines*/ "sushi");
    ImmutableList<Place> placesListWithMoreThanMaxNum =
        createPlacesListBySize(QueryServlet.MAX_NUM_PLACES_TO_RECOMMEND + 1);
    when(FETCHER.fetch(any(UserPreferences.class))).thenReturn(placesListWithMoreThanMaxNum);

    servlet.doGet(REQUEST, RESPONSE);

    assertEquals(getPlacesAmountInResponse(), QueryServlet.MAX_NUM_PLACES_TO_RECOMMEND);
  }

  @Test
  public void getRequest_fetchedLessThanMaxNumPlaces_respondAllFetchedPlaces() throws Exception {
    setRequestParameters(/*cuisines*/ "sushi");
    int numOfFetchedPlaces = QueryServlet.MAX_NUM_PLACES_TO_RECOMMEND - 1;
    ImmutableList<Place> placesListWithLessThanMaxNum = createPlacesListBySize(numOfFetchedPlaces);
    when(FETCHER.fetch(any(UserPreferences.class))).thenReturn(placesListWithLessThanMaxNum);

    servlet.doGet(REQUEST, RESPONSE);

    assertEquals(getPlacesAmountInResponse(), numOfFetchedPlaces);
  }

  @Test
  // This test only makes sure that the servlets accepts the format of sending more than one
  // cuisine, seperated by commas.
  public void getRequest_fetchMoreThanOneCuisine_success() throws Exception {
    setRequestParameters(/*cuisines*/ "sushi,hamburger");
    ImmutableList<Place> places = createPlacesListBySize(QueryServlet.MAX_NUM_PLACES_TO_RECOMMEND);
    when(FETCHER.fetch(any(UserPreferences.class))).thenReturn(places);

    servlet.doGet(REQUEST, RESPONSE);

    assertEquals(getPlacesAmountInResponse(), QueryServlet.MAX_NUM_PLACES_TO_RECOMMEND);
  }

  // Returns an immutable list that has the required number of Place elements. All elements are
  // identical except for their name, which is serialized - '0', '1', '2', etc.
  private static ImmutableList<Place> createPlacesListBySize(int numOfPlaces) {
    ImmutableList.Builder<Place> places = ImmutableList.builder();
    for (int i = 0; i < numOfPlaces; ++i) {
      String name = String.valueOf(i);
      places.add(Place.builder()
          .setName(name)
          .setWebsiteUrl("website@google.com")
          .setPhone("+97250-0000-000")
          .setRating(4)
          .setPriceLevel(3)
          .setLocation(new LatLng(35.35, 30.30))
          .build()
      );
    }
    return places.build();
  }

  // Returns the number of json elements in the servlet's response
  private int getPlacesAmountInResponse() {
    return new Gson()
        .fromJson(responseStringWriter.getBuffer().toString(), JsonArray.class)
        .size();
  }

  // Set the parameters for the mocked http request. Most parameter are contant valid values, and
  // the cuisines parameter is supplied by the calling function.
  private void setRequestParameters(String cuisines) {
    when(REQUEST.getParameter("rating")).thenReturn("4");
    when(REQUEST.getParameter("price")).thenReturn("3");
    when(REQUEST.getParameter("open")).thenReturn("1");
    when(REQUEST.getParameter("location")).thenReturn("30.30,35.35");
    when(REQUEST.getParameter("cuisines")).thenReturn(cuisines);
  }
}
