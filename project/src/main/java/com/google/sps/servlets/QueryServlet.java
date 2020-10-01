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
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import com.google.gson.Gson;

// import com.google.gson.Gson;

/** A servlet that handles the user query. */
@WebServlet("/query")
public final class QueryServlet extends HttpServlet {

  @Override
  public void doGet(final HttpServletRequest request, 
      final HttpServletResponse response) throws IOException {
    final int NUM_PLACES_TO_RECOMMEND = 3;
    final Set<Place> fetchedPlaces = new PlacesFetcher().fetch();
    final List<Place> sortedPlaces = new ArrayList<Place>(fetchedPlaces);
    Collections.sort(sortedPlaces, Places.randomComparator);
    final int numOfPlacesToShow = Math.min(NUM_PLACES_TO_RECOMMEND, 
        sortedPlaces.size());
    response.setContentType("application/json");
    for (int i = 0; i < numOfPlacesToShow; ++i) {
      response.getWriter().write(new Gson().toJson(sortedPlaces[i]));
    }
  }
}
