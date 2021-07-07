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

import static com.google.common.base.Strings.isNullOrEmpty;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.sps.data.FetcherException;
import com.google.sps.data.GeoContext;
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
import com.google.sps.data.DataAccessor;
import com.google.sps.data.UserVerifier;

/**
 * A servlet that handles the user's food-mood recommendation query, and responds with a list of
 * recommended places (in Json format).
 */
@WebServlet("/query")
@SuppressWarnings("serial")
public final class QueryServlet extends HttpServlet {

  @VisibleForTesting
  static final int DESIRED_NUM_PLACES_TO_RECOMMEND = 3;
  private PlacesFetcher fetcher;
  private PlacesScorer scorer;
  private UserVerifier userVerifier;
  private DataAccessor dataAccessor;

  @Override
  public void init() {
    this.fetcher = new PlacesFetcher(GeoContext.getGeoApiContext());
    this.scorer = new PlacesScorerImpl(GeoContext.getGeoApiContext());
    this.userVerifier = UserVerifier.create(System.getenv("CLIENT_ID"));
    this.dataAccessor = new DataAccessor();
  }

  void init(
      PlacesFetcher inputFetcher,
      PlacesScorer inputScorer,
      UserVerifier inputUserVerifier,
      DataAccessor inputDataAccessor) {
    this.fetcher = inputFetcher;
    this.scorer = inputScorer;
    this.userVerifier = inputUserVerifier;
    this.dataAccessor = inputDataAccessor;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Optional<String> optionalUserId = TokenValidator.validateAndGetId(request, response,
        userVerifier, "query" /* validationPurpose */, false /* sendErrors*/);
    ImmutableList<Place> filteredPlaces;
    UserPreferences userPrefs;
    String cuisines = request.getParameter("cuisines");
    try {
      userPrefs =
          UserPreferences.builder()
              .setMinRating(Float.parseFloat(request.getParameter("rating")))
              .setMaxPriceLevel(Integer.parseInt(request.getParameter("price")))
              .setOpenNow(Integer.parseInt(request.getParameter("open")) != 0)
              .setLocation(getLatLngFromString(request.getParameter("location")))
              .setCuisines(cuisines.isEmpty()
                  ? ImmutableList.of() : ImmutableList.copyOf(cuisines.split(",")))
              .build();
      String userIdToken = request.getParameter("idToken");
      storePreferences(userIdToken, userPrefs);
      filteredPlaces = Places.filter(
          fetcher.fetch(userPrefs) /* places */,
          Integer.parseInt(request.getParameter("rating")) /* approximate minimum rating */,
          true /* filter if no website */,
          true /* filter branches of same place */
      );
      if (optionalUserId.isPresent() && Integer.parseInt(request.getParameter("newPlaces")) == 1) {
        filteredPlaces = Places.keepOnlyNewPlaces(filteredPlaces, optionalUserId.get(),
            dataAccessor, DESIRED_NUM_PLACES_TO_RECOMMEND);
      }
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
          .limit(DESIRED_NUM_PLACES_TO_RECOMMEND)
          .collect(Collectors.toList())
    ));
  }

  private static LatLng getLatLngFromString(String coordinates) {
    String[] latLng = coordinates.split(",");
    return new LatLng(Double.parseDouble(latLng[0]), Double.parseDouble(latLng[1]));
  }

  // Store the user's preferences in the database, only if the user is signed in.
  private void storePreferences(String userIdToken, UserPreferences userPrefs) {
    if (!isNullOrEmpty(userIdToken)) {
      Optional<String> optionalUserId = userVerifier.getUserIdByToken(userIdToken);
      if (optionalUserId.isPresent()) {
        String userId = optionalUserId.get();
        dataAccessor.storeUserPreferences(userId, userPrefs);
      }
    }
  }
}
