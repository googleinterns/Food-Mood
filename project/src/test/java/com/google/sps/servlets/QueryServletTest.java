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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertEquals;

import com.google.sps.data.PlacesFetcher;
import com.google.sps.data.Place;
import com.google.appengine.repackaged.com.google.common.collect.ImmutableList;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.appengine.repackaged.com.google.gson.JsonArray;
import com.google.appengine.repackaged.com.google.gson.JsonObject;
import com.google.maps.model.LatLng;

@RunWith(JUnit4.class)
public final class QueryServletTest {

  private static final HttpServletRequest request = mock(HttpServletRequest.class);
  private static final HttpServletResponse response = mock(HttpServletResponse.class);
  private static final PlacesFetcher fetcher = mock(PlacesFetcher.class);
  private static final int maxNumPlaces = QueryServlet.MAX_NUM_PLACES_TO_RECOMMEND;

  @Test
  public void getRequest_fetchedMoreThanMaxNumPlaces_respondWithMaxNumPlaces() throws Exception{
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    ImmutableList<Place> placesListWithMoreThanMaxNum = getImmutableListBySize(maxNumPlaces + 1);
    QueryServlet servlet = new QueryServlet();
    servlet.init(fetcher);
    when(fetcher.fetch()).thenReturn(placesListWithMoreThanMaxNum);
    when(response.getWriter()).thenReturn(pw);

    servlet.doGet(request, response);

    JsonArray jsonPlaces = new Gson().fromJson(sw.getBuffer().toString(), JsonArray.class);

    // Make sure we got the right number of elements
    assertEquals(jsonPlaces.size(), maxNumPlaces);
  }

  @Test
  public void getRequest_fetchedLessThanMaxNumPlaces_respondWithAllFetchedPlaces() throws Exception{
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    int numOfFetchedPlaces = maxNumPlaces - 1;
    ImmutableList<Place> placesListWithLessThanMaxNum = getImmutableListBySize(numOfFetchedPlaces);
    QueryServlet servlet = new QueryServlet();
    servlet.init(fetcher);
    when(fetcher.fetch()).thenReturn(placesListWithLessThanMaxNum);
    when(response.getWriter()).thenReturn(pw);

    servlet.doGet(request, response);

    JsonArray jsonPlaces = new Gson().fromJson(sw.getBuffer().toString(), JsonArray.class);

    // Make sure we got the right number of elements
    assertEquals(jsonPlaces.size(), numOfFetchedPlaces);
  }

  /**
   * @param numOfPlaces the number of elements we want to have on the list
   * @return an immutable list that has the required number of Place elements. All elements are
   * identical except for their name, which is serialized - '0', '1', '2', etc.
   */
  private static ImmutableList<Place> getImmutableListBySize(int numOfPlaces) {
    List<Place> tempList = new ArrayList<Place>();
    for (int i = 0; i < numOfPlaces; ++i) {
      String name = String.valueOf(i);
      tempList.add(Place.builder()
          .setName(name)
          .setWebsiteUrl("website@google.com")
          .setPhone("+97250-0000-000")
          .setRating(4)
          .setPriceLevel(3)
          .setLocation(new LatLng(35.35, 30.30))
          .build()
      );
    }
    return ImmutableList.copyOf(tempList);
  }
}
