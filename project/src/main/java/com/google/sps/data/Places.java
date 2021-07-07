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

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.maps.model.LatLng;

/**
 * A utility class for Place objects.
 */
public final class Places {

  /**
   * Sorts the given list of Places randomly.
   *
   * @param places the list we want to sort.
   * @return a new list containing the original list's elements in random order.
   */
  public static ImmutableList<Place> randomSort(ImmutableList<Place> places) {
    List<Place> mutablePlaces = new ArrayList<>(places);
    Collections.shuffle(mutablePlaces);
    return ImmutableList.copyOf(mutablePlaces);
  }

  /**
   * Sorts the given list of Places by score.
   *
   * @param places the list we want to sort
   * @param userLocation the user's loaction, used for scores calculations
   * @param scorer the PlacesScorer which calculates a score for each place
   * @return a new list containing the original list's elements
   *     arranged by descending order of their scores.
   */
  public static ImmutableList<Place> scoreSort(
        ImmutableList<Place> places, LatLng userLocation, PlacesScorer scorer) {
    List<Place> mutablePlaces = new ArrayList<>(places);
    Map<Place, Double> placesScores = scorer.getScores(places, userLocation);
    Collections.sort(mutablePlaces, (p1, p2) -> {
      return placesScores.get(p2).compareTo(placesScores.get(p1));
    });
    return ImmutableList.copyOf(mutablePlaces);
  }

  /**
   * Filters the given list of places according to the given parameters.
   * @param places the list we want to filter.
   * @param approxMinRating The approximate minimum Place's rating, an int between 1 and 5. Places
   *        will only be approved if their rating's nearest int is higher than this (or equal to).
   * @param filterIfNoWebsite Specifies whether we should filter if there is no available website
   *        for the restaurant.
   * @param filterBranchesOfSamePlace Specifies whether we should remove different branches of the
   *        same place from the results. Places are considered the same if they have the same name.
   * @return the filtered list.
   */
  public static ImmutableList<Place> filter(ImmutableList<Place> places, int approxMinRating,
      Boolean filterIfNoWebsite, Boolean filterBranchesOfSamePlace) {
    ImmutableList<Place> result =
        places.stream()
            .sorted(Comparator.comparing(Place::rating).reversed())
            .filter(place -> place.businessStatus() == BusinessStatus.OPERATIONAL)
            .filter(place -> Math.rint(place.rating()) >= approxMinRating)
            .filter(place -> !(filterIfNoWebsite && placeHasNoWebsiteLink(place)))
            .collect(ImmutableList.toImmutableList());
    if (filterBranchesOfSamePlace) {
      result = result.stream().collect(
          collectingAndThen(
              toCollection(() -> new TreeSet<>(comparing(Place::name))),
              ImmutableList::copyOf
          )
      );
    }
    return result;
  }

  /**
   * Filters the given list to keep only places that weren't previously recommended to the user, as
   * long as the result's size is bigger than the given thresh (or equal to it).
   * @param places the list we want to filter.
   * @param userId The Id of the user whose places we want to filter.
   * @param dataAccessor the accessor we use to get information about the user's history.
   * @param minNumPlacesThresh the minimal number of places that we require to have after this
   * function's filter. If the filteres result is smaller that the thresh, th function retrieves the
   * original list.
   * @return the filtered list.
   */
  public static ImmutableList<Place> keepOnlyNewPlaces(ImmutableList<Place> places,
      String userId, DataAccessor dataAccessor, int minNumPlacesThresh) {
    checkArgument(!isNullOrEmpty(userId), "User ID cannot be empty.");
    if (places.size() <= minNumPlacesThresh) {
      return places; // The list is not long enough to filter
    }
    ImmutableList<String> placesRecommendedToUser =
        dataAccessor.getPlacesRecommendedToUser(userId, false /* getOnlyPlacesUserChose */);
    ImmutableList<Place> result =
        places.stream()
        .filter(p -> !placesRecommendedToUser.contains(p.placeId()))
        .collect(ImmutableList.toImmutableList());
    return result.size() >= minNumPlacesThresh ? result : places;
  }

  private static boolean placeHasNoWebsiteLink(Place place) {
    return Strings.isNullOrEmpty(place.websiteUrl()) && Strings.isNullOrEmpty(place.googleUrl());
  }

  private Places() { }
}
