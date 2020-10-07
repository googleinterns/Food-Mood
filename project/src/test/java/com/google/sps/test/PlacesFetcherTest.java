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
import com.google.maps.model.Geometry;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.PlacesSearchResult;
import com.google.maps.model.PriceLevel;
import com.google.sps.data.Place;
import com.google.sps.data.PlacesFetcher;
import com.google.sps.data.errors.FetcherException;

import java.net.MalformedURLException;
import java.net.URL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class PlacesFetcherTest {

  /** url for Places used in tests. */
  private static final String URL_STRING = "https://www.google.com/";

  /** url for PlaceDetails used in tests. */
  private static final URL URL = createTestURL(URL_STRING);

  /** phone for Places and PlaceDetails used in tests. */
  private static final String PHONE = "+97250-0000-000";

  /** location for Places and PlaceDetails used in tests. */
  private static final LatLng LOCATION = new LatLng(32.08074, 34.78059);

  /** rating for Places and PlaceDetails used in tests. */
  private static final float RATING = 4;

  /** price level for Places used in tests. */
  private static final int PRICELEVEL_INT = 2;

  /** price level for PlaceDetails used in tests. */
  private static final PriceLevel PRICELEVEL = PriceLevel.MODERATE;

  /** A valid Place object with name "name1". */
  private static final Place PLACE_1 =
    Place.builder()
      .setName("name1")
      .setWebsiteUrl(URL_STRING)
      .setPhone(PHONE)
      .setRating(RATING)
      .setPriceLevel(PRICELEVEL_INT)
      .setLocation(LOCATION)
      .build();

  /** A valid Place object with name "name2". */
  private static final Place PLACE_2 =
    Place.builder()
      .setName("name2")
      .setWebsiteUrl(URL_STRING)
      .setPhone(PHONE)
      .setRating(RATING)
      .setPriceLevel(PRICELEVEL_INT)
      .setLocation(LOCATION)
      .build();

  /** A valid placeId for a  PlacesSearchResult used in tests. */
  private static final String PLACEID_1 = "ChIJN1t_tDeuEmsRUsoyG83frY4";

  /** A valid placeId for a valid PlacesSearchResult used in tests. */
  private static final String PLACEID_2 = "ChIJ02qnq0KuEmsRHUJF4zo1x4I";

  /** An invalid placeId for an ivalid PlacesSearchResult used in tests. */
  private static final String PLACEID_INVALID= "abc";

  /** A PlacesSearchResult with placeId "PLACEID_1" and other fields are null. */
  private static final PlacesSearchResult SEARCH_RESULT_1 =
      createTestPlacesSearchResult(PLACEID_1);

  /** A PlacesSearchResult with placeId "PLACEID_2"  and other fields are null. */
  private static final PlacesSearchResult SEARCH_RESULT_2 =
      createTestPlacesSearchResult(PLACEID_2);

  /** An invalid PlacesSearchResult with placeId "PLACEID_INVALID". */
  private static final PlacesSearchResult SEARCH_RESULT_INVALID =
  createTestPlacesSearchResult(PLACEID_INVALID);


  /** An array of valid PlacesSearchResults. */
  private static final PlacesSearchResult[] SEARCH_RESULT_ARR = {SEARCH_RESULT_1, SEARCH_RESULT_2};

  /** Valid PlaceDetails for PLACE_1. */
  private static final PlaceDetails PLACE_DETAILS_1 =
    createTestPlaceDetails("name1", URL, PHONE, RATING, PRICELEVEL, LOCATION);

  /** Valid PlaceDetails for PLACE_2. */
  private static final PlaceDetails PLACE_DETAILS_2 =
    createTestPlaceDetails("name2", URL, PHONE, RATING, PRICELEVEL, LOCATION);

  /**
   * creates URL from String for PlaceDetails used in tests.
   *
   * @param s The string representing the URL
   * @return A URL
   */
  private static URL createTestURL(String s) {
    try {
      return new URL(s);
    } catch (MalformedURLException e) {
      System.out.println("url not generated");
      return null;
    }
  }

  /**
   * creates PlaceDetails to be used in tests.
   *
   * @param name The name of the Place
   * @param url The url of the Place's website
   * @param phone The phonenumber of the Place
   * @param rating The Google rating of the Place
   * @param priceLevel The priceLevel of the Place as identified by Google Places
   * @param location The location of the Place
   * @return The PlacesDetails according to the given params
   */
  private static PlaceDetails createTestPlaceDetails(String name, URL url, String phone,
      float rating, PriceLevel priceLevel, LatLng location) {
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

  /** creates PlaceSearchResult to be used in tests.
   *
   * @param placeId The place Id of the created PlaceSearchResult
   * @return PlacesSearchResult with placeId as it's Id
   */
  private static PlacesSearchResult createTestPlacesSearchResult(String placeId) {
    PlacesSearchResult searchResult = new PlacesSearchResult();
    searchResult.placeId = placeId;
    return searchResult;
  }

  /** A PlacesFetcher instance to be spied on. */
  private static PlacesFetcher placesFetcher = new PlacesFetcher();

  @Test
  public void fetch_zeroSearchResults_returnsEmptyList() throws Exception {
    PlacesFetcher spiedFetcher = spy(placesFetcher);
    doReturn(new PlacesSearchResult[0]).when(spiedFetcher).getPlacesSearchResults();
    ImmutableList<Place> expectedOutput = ImmutableList.of();
    assertEquals(expectedOutput, spiedFetcher.fetch());
  }

  @Test
  public void fetch_validSearchResults_returnsListOfPlaces() throws Exception {
    PlacesFetcher spiedFetcher = spy(placesFetcher);
    doReturn(PLACE_DETAILS_1).when(spiedFetcher).getPlaceDetails(PLACEID_1);
    doReturn(PLACE_DETAILS_2).when(spiedFetcher).getPlaceDetails(PLACEID_2);
    doReturn(SEARCH_RESULT_ARR).when(spiedFetcher).getPlacesSearchResults();
    ImmutableList<Place> expectedOutput = ImmutableList.of(PLACE_1, PLACE_2);
    assertEquals(expectedOutput, spiedFetcher.fetch());
  }

  @Test
  public void fetch_PlaceDetailsQueryFails_throwsFetcherException()
      throws FetcherException {
    PlacesFetcher spiedFetcher = spy(placesFetcher);
    doReturn(new PlacesSearchResult[] {SEARCH_RESULT_INVALID})
        .when(spiedFetcher).getPlacesSearchResults();
    assertThrows(FetcherException.class, () -> spiedFetcher.fetch());
  }
}
