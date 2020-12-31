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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
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
  public void scoreSort_sortedByScoringAlgorithm() {
    Place placeLowRating = createValidPlaceBuilderByName("name1").setRating(1).build();
    Place placeHighRating = createValidPlaceBuilderByName("name2").setRating(2).build();
    ImmutableList<Place> placesList = ImmutableList.of(placeLowRating, placeHighRating);
    LatLng userLoaction = new LatLng(32.09, 34.78);
    PlacesScorer mockScorer = mock(PlacesScorerImpl.class);
    when(mockScorer.getScores(placesList, userLoaction))
        .thenReturn(ImmutableMap.of(placeLowRating, 0.5d, placeHighRating, 1d));

    ImmutableList<Place> result = Places.scoreSort(placesList, userLoaction, mockScorer);

    assertEquals(ImmutableList.of(placeHighRating, placeLowRating), result);
  }

  @Test
  public void filter_noNeedToFilter_noFilter() {
    ImmutableList<Place> twoPlaces = ImmutableList.of(
        createValidPlaceBuilderByName("name1").build(),
        createValidPlaceBuilderByName("name2").build()
    );

    ImmutableList<Place> result = Places.filter(
      twoPlaces,
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
        ImmutableList.of(branch1, branch2, branch3),
        1 /* min rating */,
        false /* filter if no website */,
        true /* filter branches of same place */
      );

    assertEquals(result, ImmutableList.of(branch1));
  }

  @Test
  public void filter_tooLowRating_filterOut() {
    int highRating = 5;
    int lowerRating = 4;
    Place highRatingPlace = createValidPlaceBuilderByName("name1").setRating(highRating).build();
    Place lowRatingPlace = createValidPlaceBuilderByName("name2").setRating(lowerRating).build();

    ImmutableList<Place> result = Places.filter(
        ImmutableList.of(highRatingPlace, lowRatingPlace),
        highRating /* min rating */,
        false /* filter if no website */,
        false /* filter branches of same place */
    );

    assertEquals(result, ImmutableList.of(highRatingPlace));
  }

  @Test
  public void filter_floatingPointRating_filterOutByRoundingToNearestInt() {
    // This test checks that the rating is filtered after rounding it to the nearest int.
    // For example, when places with 5 starts are desired, places that have more than 4.5 stars will
    // be supplied.
    int threshRating = 4;
    Place higherRatingPlace =
        createValidPlaceBuilderByName("higherRating").setRating(threshRating + 0.5f).build();
    Place slightlyLowerRatingPlace =
        createValidPlaceBuilderByName("slightlyLowerRating").setRating(threshRating - 0.1f).build();
    Place muchLowerRatingPlace =
        createValidPlaceBuilderByName("muchLowerRating").setRating(threshRating - 0.8f).build();

    ImmutableList<Place> result = Places.filter(
        ImmutableList.of(higherRatingPlace, slightlyLowerRatingPlace, muchLowerRatingPlace),
        threshRating /* min rating */,
        false /* filter if no website */,
        false /* filter branches of same place */
    );

    assertEquals(result, ImmutableList.of(higherRatingPlace, slightlyLowerRatingPlace));
  }

  @Test
  public void filter_branchesFilterNotRequired_notFiltering() {
    Place samePlace1 = createValidPlaceBuilderByName("name1").build();
    Place samePlace2 = createValidPlaceBuilderByName("name1").build();
    ImmutableList<Place> allPlaces = ImmutableList.of(samePlace1, samePlace2);

    ImmutableList<Place> result = Places.filter(
        allPlaces,
        1 /* min rating */,
        false /* filter if no website */,
        false /* filter branches of same place */
    );

    assertEquals(result, allPlaces);
  }

  @Test
  public void filter_websiteFilterNotRequired_notFiltering() {
    Place emptyWebsitePlace = createValidPlaceBuilderByName("name").setWebsiteUrl("").build();
    ImmutableList<Place> emptyWebsitePlaceList = ImmutableList.of(emptyWebsitePlace);

    ImmutableList<Place> result = Places.filter(
        emptyWebsitePlaceList,
        1 /* min rating */,
        false /* filter if no website */,
        false /* filter branches of same place */
    );

    assertEquals(result, emptyWebsitePlaceList);
  }

  @Test
  public void filter_filterByWebsitePresence() {
    ImmutableList<Place> placesToKeep = ImmutableList.of(
        // Place has both a website URL and a google URL
        createValidPlaceBuilderByName("name1").build(),
        // Place has a website URL and doesn't have a google URL
        createValidPlaceBuilderByName("name2").setGoogleUrl("").build(),
        // Place doesn't have a website URL and has a google URL
        createValidPlaceBuilderByName("name3").setWebsiteUrl("").build()
    );
    // Place doesn't have a website URL or a google URL
    Place placeToFilter =
        createValidPlaceBuilderByName("name4").setWebsiteUrl("").setGoogleUrl("").build();
    ImmutableList<Place> allPlaces =
        ImmutableList.<Place>builder().addAll(placesToKeep).add(placeToFilter).build();

    ImmutableList<Place> result = Places.filter(
        allPlaces,
        1 /* min rating */,
        true /* filter if no website */,
        false /* filter branches of same place */
    );

    assertEquals(result, placesToKeep);
  }

  @Test
  public void filter_filterByStatus() {
    Place placeToKeep = createValidPlaceBuilderByName("name1")
        .setBusinessStatus(BusinessStatus.OPERATIONAL).build();
    ImmutableList<Place> placesToFilter = ImmutableList.of(
        createValidPlaceBuilderByName("name2")
            .setBusinessStatus(BusinessStatus.CLOSED_TEMPORARILY).build(),
        createValidPlaceBuilderByName("name3")
            .setBusinessStatus(BusinessStatus.CLOSED_PERMANENTLY).build(),
        createValidPlaceBuilderByName("name4").setBusinessStatus(BusinessStatus.UNKNOWN).build()
    );
    ImmutableList<Place> allPlaces =
        ImmutableList.<Place>builder().addAll(placesToFilter).add(placeToKeep).build();

    ImmutableList<Place> result = Places.filter(
        allPlaces,
        1 /* min rating */,
        false /* filter if no website */,
        false /* filter branches of same place */
    );

    assertEquals(result, ImmutableList.of(placeToKeep));
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
        .setBusinessStatus(BusinessStatus.OPERATIONAL);
  }
}
