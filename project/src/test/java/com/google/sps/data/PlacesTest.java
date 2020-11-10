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

package com.google.sps.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import com.google.common.collect.ImmutableList;
import com.google.maps.model.LatLng;

@RunWith(JUnit4.class)
public final class PlacesTest {

  @Test
  public void randomSort_keepsAllItems() {
    Place place1 = createValidPlaceBuilderByName("name1").build();
    Place place2 = createValidPlaceBuilderByName("name2").build();
    ImmutableList<Place> result = Places.randomSort(ImmutableList.of(place1, place2));

    assertTrue(result.contains(place1));
    assertTrue(result.contains(place2));
  }

  @Test
  public void filter_noNeedToFilter_noFilter() {
    ImmutableList<Place> twoPlaces = ImmutableList.of(
        createValidPlaceBuilderByName("name1").build(),
        createValidPlaceBuilderByName("name2").build()
    );

    ImmutableList<Place> result = Places.filter(
      twoPlaces /* places */,
        1 /* min rating */,
        true /* filter if no website */,
        true /* filter branches of same place */
    );

    assertEquals(result, twoPlaces);
  }

  @Test
  public void filter_fewBranchesOfSamePlace_filterOutBranches() {
    Place branch1 = createValidPlaceBuilderByName("name")
        .setLocation(new LatLng(35.35, 30.30)).build();
    Place branch2 = createValidPlaceBuilderByName("name")
        .setLocation(new LatLng(30.30, 30.30)).build();
    Place branch3 = createValidPlaceBuilderByName("name")
        .setLocation(new LatLng(35.35, 35.35)).build();

    ImmutableList<Place> result = Places.filter(
        ImmutableList.of(branch1, branch2, branch3) /* places */,
        1 /* min rating */,
        false /* filter if no website */,
        true /* filter branches of same place */
      );

    assertEquals(result, ImmutableList.of(branch1));
  }


  @Test
  public void filter_noWebsite_filterOut() {
    Place websiteEmpty = createValidPlaceBuilderByName("name").setWebsiteUrl("").build();
    Place place1 = createValidPlaceBuilderByName("name1").build();
    Place place2 = createValidPlaceBuilderByName("name2").build();

    ImmutableList<Place> result = Places.filter(
        ImmutableList.of(websiteEmpty, place1, place2) /* places */,
        1 /* min rating */,
        true /* filter if no website */,
        false /* filter branches of same place */
    );

    assertEquals(result, ImmutableList.of(place1, place2));
  }

  @Test
  public void filter_tooLowRating_filterOut() {
    int highRating = 5;
    int lowerRating = 4;
    Place highRatingPlace = createValidPlaceBuilderByName("name1").setRating(highRating).build();
    Place lowRatingPlace = createValidPlaceBuilderByName("name2").setRating(lowerRating).build();

    ImmutableList<Place> result = Places.filter(
        ImmutableList.of(highRatingPlace, lowRatingPlace) /* places */,
        highRating /* min rating */,
        false /* filter if no website */,
        false /* filter branches of same place */
    );

    assertEquals(result, ImmutableList.of(highRatingPlace));
  }

  @Test
  public void filter_filterNotRequired_notFiltering() {
    Place websiteEmpty = createValidPlaceBuilderByName("name").setWebsiteUrl("").build();
    Place samePlace1 = createValidPlaceBuilderByName("name1").build();
    Place samePlace2 = createValidPlaceBuilderByName("name1").build();
    ImmutableList<Place> allPlaces = ImmutableList.of(websiteEmpty, samePlace1, samePlace2);

    ImmutableList<Place> result = Places.filter(
      allPlaces /* places */,
        1 /* min rating */,
        false /* filter if no website */,
        false /* filter branches of same place */
    );

    assertEquals(result, allPlaces);
  }

  // Returns a Place builder that has valid values of all attributes.
  private static Place.Builder createValidPlaceBuilderByName(String name) {
    return Place.builder()
        .setName(name)
        .setWebsiteUrl("website@google.com")
        .setPhone("+97250-0000-000")
        .setRating(4)
        .setPriceLevel(3)
        .setLocation(new LatLng(35.35, 30.30))
        .setGoogleUrl("googleurl.com")
        .setPlaceId("ChIJN1t_tDeuEmsRUsoyG83frY4")
        .setBusinessStatus("OPERATIONAL");
  }
}
