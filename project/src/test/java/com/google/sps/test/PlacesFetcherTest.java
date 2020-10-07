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

package com.google.sps.test;

import com.google.appengine.repackaged.com.google.common.collect.ImmutableList;
import com.google.maps.errors.ApiException;
import com.google.maps.model.Geometry;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.PlacesSearchResult;
import com.google.maps.model.PriceLevel;
import com.google.sps.data.Place;
import com.google.sps.data.PlacesFetcher;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.mockito.Mockito.*;

@RunWith(JUnit4.class)
public final class PlacesFetcherTest {

  /** fields to be used in tests. */
  private static final String urlString = "https://www.google.com/";
  private static final URL url = createTestURL(urlString); //used in PlaceDetails
  private static final String phone = "+97250-0000-000";
  private static final LatLng location =  new LatLng(32.08074, 34.78059);
  private static final int rating = 4;
  private static final int priceLevelInt = 2;
  private static final PriceLevel priceLevel = PriceLevel.MODERATE; //used in PlaceDetails
  private static final String placeId_1 = "ChIJN1t_tDeuEmsRUsoyG83frY4";
  private static final String placeId_2 = "ChIJ02qnq0KuEmsRHUJF4zo1x4I";

  /** Places to be used in tests. */
  private static final Place PLACE_1 = Place.create("name1", urlString, phone, rating, priceLevelInt, location);
  private static final Place PLACE_2 = Place.create("name2", urlString, phone, rating, priceLevelInt, location);

  /** PlacesSearchResults to be used in tests. */
  private static PlacesSearchResult SEARCH_RESULT_1 = createTestPlacesSearchResult(placeId_1);
  private static PlacesSearchResult SEARCH_RESULT_2 = createTestPlacesSearchResult(placeId_2);
  private static final PlacesSearchResult[] SEARCH_RESULT_ARR = {SEARCH_RESULT_1,  SEARCH_RESULT_2};

  /** PlacesDetails to be used in tests. */
  private static final PlaceDetails PLACE_DETAILS_1 = 
    createTestPlaceDetails("name1", url, phone, rating, priceLevel, location);
  private static final PlaceDetails PLACE_DETAILS_2 = 
    createTestPlaceDetails("name2", url, phone, rating, priceLevel, location);

  /** creates URL from String for PlaceDetails used in tests. */
  private static final URL createTestURL(String s) {
    try {
      return new URL(s);
    } catch (MalformedURLException e) {
      System.out.println("url not generated");
      return null;
    }
  }

  /** creates PlaceDetails to be used in tests. */
  private static final PlaceDetails createTestPlaceDetails(
    String name, URL url, String phone,int rating, PriceLevel priceLevel, LatLng location) {
    PlaceDetails placeDetails = new PlaceDetails();
    placeDetails.name = name;
    placeDetails.website = url;
    placeDetails.formattedPhoneNumber = phone;
    placeDetails.rating = rating;
    placeDetails.priceLevel = priceLevel;
    placeDetails.geometry = new Geometry();
    placeDetails.geometry.location = location;
    return placeDetails;
  }

  /** creates PlaceSearchResult to be used in tests. */
  private static final PlacesSearchResult createTestPlacesSearchResult(String placeId) {
    PlacesSearchResult searchResult = new PlacesSearchResult();
    searchResult.placeId = placeId;
    return searchResult;
  }

    /** A PlacesFetcher instance to be spied on. */
  PlacesFetcher placesFetcher = new PlacesFetcher();

  @Test
  public void fetch_zeroSearchResults_returnsEmptyList() throws Exception {
      PlacesFetcher spiedFetcher = spy(placesFetcher);
      doReturn(new PlacesSearchResult[0]).when(spiedFetcher).getPlacesSearchResults();
      ImmutableList<Place> expectedOutput = ImmutableList.of();
      assertEquals(expectedOutput, spiedFetcher.fetch());
  }

  @Test
  public void fetch_validSearchResults_returnsListOfPlaces() throws ApiException, InterruptedException, IOException {
      PlacesFetcher spiedFetcher = spy(placesFetcher);
      doReturn(PLACE_DETAILS_1).when(spiedFetcher).getPlaceDetails(placeId_1);
      doReturn(PLACE_DETAILS_2).when(spiedFetcher).getPlaceDetails(placeId_2);
      doReturn(SEARCH_RESULT_ARR).when(spiedFetcher).getPlacesSearchResults();
      ImmutableList<Place> expectedOutput = ImmutableList.of(PLACE_1, PLACE_2);
      assertEquals(expectedOutput, spiedFetcher.fetch());        
    } 

}
