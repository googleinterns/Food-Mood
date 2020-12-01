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
// limitations under the License.import java.io.IOException;

package com.google.sps.servlets;

import java.io.IOException;
import java.util.stream.Collectors;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.sps.data.FetcherException;
import com.google.sps.data.Place;
import com.google.sps.data.Places;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.maps.model.LatLng;
import com.google.sps.data.PlacesFetcher;
import com.google.sps.data.PlacesScorer;
import com.google.sps.data.UserPreferences;
import com.google.sps.data.PlacesScorerImpl;

/**
 * A servlet that handles the user's food-mood recommendation query, and responds with a list of
 * recommended places (in Json format).
 */
@WebServlet("/query")
public final class QueryServlet extends HttpServlet {

  @VisibleForTesting
  static final int MAX_NUM_PLACES_TO_RECOMMEND = 3;
  private PlacesFetcher fetcher;
  private PlacesScorer scorer;

  @Override
  public void init() {
    fetcher = new PlacesFetcher();
    scorer = new PlacesScorerImpl();
  }

  void init(PlacesFetcher inputFetcher, PlacesScorer inputScorer) {
    fetcher = inputFetcher;
    scorer = inputScorer;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    ImmutableList<Place> filteredPlaces;
    UserPreferences userPrefs;
    try {
      userPrefs =
          UserPreferences.builder()
              .setMinRating(Float.parseFloat(request.getParameter("rating")))
              .setMaxPriceLevel(Integer.parseInt(request.getParameter("price")))
              .setOpenNow(Integer.parseInt(request.getParameter("open")) != 0)
              .setLocation(getLatLngFromString(request.getParameter("location")))
              .setCuisines(ImmutableList.copyOf(request.getParameter("cuisines").split(",")))
              .build();
      filteredPlaces = Places.filter(
          fetcher.fetch(userPrefs) /* places */,
          Integer.parseInt(request.getParameter("rating")) /* min rating */,
          true /* filter if no website */,
          true /* filter branches of same place */
      );
    } catch (FetcherException e) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Fetching from Google Places API encountered a problem");
      return;
    } catch (IllegalArgumentException e) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST,
          "Parsing the user preferences encountered a problem");
      return;
    }
    response.setContentType("application/json");
    response.getWriter().write(new Gson().toJson(
      Places.scoreSort(filteredPlaces, userPrefs.location(), scorer)
          .stream()
          .limit(MAX_NUM_PLACES_TO_RECOMMEND)
          .collect(Collectors.toList())
    ));
  }

  private static LatLng getLatLngFromString(String coordinates) {
    String[] latLng = coordinates.split(",");
    return new LatLng(Float.parseFloat(latLng[0]), Float.parseFloat(latLng[1]));
  }
 }
