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

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * A utility class for Place objects.
 */
public final class Places {

  /**
   * Sorts the given list of Places randomly.
   * @param immutableList the list we want to sort.
   * @return a new list containing the original list's elements in random order.
   */
  public static ImmutableList<Place> randomSort(ImmutableList<Place> immutableList) {
    List<Place> mutablePlaces = new ArrayList<>(immutableList);
    Collections.shuffle(mutablePlaces);
    return ImmutableList.copyOf(mutablePlaces);
  }

  private Places() { }
}
