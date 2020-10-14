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
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.sps.data.Place;
import com.google.sps.data.Places;
import com.google.appengine.repackaged.com.google.common.collect.ImmutableList;
import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.google.sps.data.PlacesFetcher;

/**
 * A servlet that handles the user query. Currently accepts no input, and responds with a list of
 * recommended places (in Json format).
 */
@WebServlet("/query")
public final class QueryServlet extends HttpServlet {

  @VisibleForTesting
  static final int MAX_NUM_PLACES_TO_RECOMMEND = 3;
  private PlacesFetcher fetcher;


  @Override
  public void init() {
    fetcher = new PlacesFetcher();
  }

  void init(PlacesFetcher inputFetcher) {
    fetcher = inputFetcher;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    try {
      //TODO(M1): add call to filterer
      List<Place> placesToDisplay = Places.randomSort(fetcher.fetch())
          .stream()
          .limit(MAX_NUM_PLACES_TO_RECOMMEND)
          .collect(Collectors.toList());
      response.setContentType("application/json");
      response.getWriter().write(new Gson().toJson(placesToDisplay));
    }
    catch(Exception e) {
      //TODO(M1): handle errors - TBD
    }
  }
}
