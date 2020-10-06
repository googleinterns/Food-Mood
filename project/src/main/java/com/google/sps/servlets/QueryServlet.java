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

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.sps.data.Place;
import com.google.sps.data.Places;
import com.google.appengine.repackaged.com.google.common.collect.ImmutableList;
import com.google.gson.Gson;

/** A servlet that handles the user query. */
@WebServlet("/query")
public class QueryServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    int numPlacesToRecommend = 3;
    ImmutableList<Place> fetchedPlaces = PlacesFetcher.fetch();
    ImmutableList<Place> sortedPlaces = Places.randomSort(fetchedPlaces);
    int numPlacesToDisplay = Math.min(numPlacesToRecommend, sortedPlaces.size());
    response.setContentType("application/json");
    for (int i = 0; i < numPlacesToDisplay; ++i) {
      response.getWriter().write(new Gson().toJson(sortedPlaces.get(i)));
    }
  }
}
