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

/**
 * The package that currently holds all the java files of the food-mood project.
 */
package com.google.sps.data;

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
   * @param places the list we want to sort.
   * @return a new list containing the original list's elements arranged by descending order of their scores.
   */
  public static ImmutableList<Place> scoreSort(ImmutableList<Place> places, LatLng userLocation) {
    List<Place> mutablePlaces = new ArrayList<>(places);
    Map<Place, Double> placesScores = new PlacesScorer(places, userLocation).getScores();
    Collections.sort(mutablePlaces, (p1, p2) -> {
      return placesScores.get(p2).compareTo(placesScores.get(p1));
    });
    return ImmutableList.copyOf(mutablePlaces);
  }


  /**
   * Filters the given list of places according to the given parameters.
   * @param places the list we want to filter.
   * @param minRating The minimal rating we want to limit the list by. A number between 1 and 5.
   * @param filterIfNoWebsite Specifies whether we should filter if there is no available website
   *        for the restaurant.
   * @param filterBranchesOfSamePlace Specifies whether we should remove different branches of the
   *        same place from the results. Places are considered the same if they have the same name.
   * @return the filtered list.
   */
  public static ImmutableList<Place> filter(ImmutableList<Place> places, int minRating, Boolean
      filterIfNoWebsite, Boolean filterBranchesOfSamePlace) {
    //TODO(M1): also filter by place's status when the attribute is added
    ImmutableList<Place> result =
        places.stream()
            .sorted(Comparator.comparing(Place::rating).reversed())
            .filter(place -> place.rating() >= minRating)
            //TODO(M1): also chack about having a google maps link, when attribute added
            .filter(place -> !(filterIfNoWebsite && Strings.isNullOrEmpty(place.websiteUrl())))
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

  private Places() { }
}
