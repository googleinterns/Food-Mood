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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.junit.Assert.assertTrue;

import com.google.appengine.repackaged.com.google.common.collect.ImmutableList;
import com.google.maps.model.LatLng;

@RunWith(JUnit4.class)
public final class PlacesTest {

  /** A valid Place object with name "name1". */
  private static final Place PLACE_1 =
      Place.builder()
          .setName("name1")
          .setWebsiteUrl("website@google.com")
          .setPhone("+97250-0000-000")
          .setRating(4)
          .setPriceLevel(3)
          .setLocation(new LatLng(35.35, 30.30))
          .build();

  /** A valid Place object with name "name2". */
  private static final Place PLACE_2 =
      Place.builder()
          .setName("name2")
          .setWebsiteUrl("website@google.com")
          .setPhone("+97250-0000-000")
          .setRating(4)
          .setPriceLevel(3)
          .setLocation(new LatLng(35.35, 30.30))
          .build();

  @Test
  public void randomSort_keepsAllItems() {
    ImmutableList<Place> result = Places.randomSort(ImmutableList.of(PLACE_1, PLACE_2));

    assertTrue(result.contains(PLACE_1));
    assertTrue(result.contains(PLACE_2));
  }
}
