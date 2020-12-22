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

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class UserFeedbackTest {

  private static final String PLACE_ID_1 = "12345";
  private static final ImmutableList<String> PLACES_WITH_PLACE_ID_1 =
      ImmutableList.of(PLACE_ID_1, "11111", "22222");

  @Test
  public void build_invalidBothChoosePlaceAndTryagain_throwsIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class, () -> {
      UserFeedback.builder()
          .setPlacesRecommendedToUser(PLACES_WITH_PLACE_ID_1)
          .setPlaceUserChose(PLACE_ID_1)
          .setUserTriedAgain(true)
          .build();
    });
  }

  @Test
  public void build_invalidChoosePlaceNotInRecommendedPlaces_throwsIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class, () -> {
      UserFeedback.builder()
          .setPlacesRecommendedToUser(ImmutableList.of("11111", "22222", "33333"))
          .setPlaceUserChose("44444")
          .setUserTriedAgain(false)
          .build();
    });
  }

  @Test
  public void build_validUserChosePlace_returnsValidUserFeedback() {
    UserFeedback userFeedback = UserFeedback.builder()
        .setPlacesRecommendedToUser(PLACES_WITH_PLACE_ID_1)
        .setPlaceUserChose(PLACE_ID_1)
        .setUserTriedAgain(false)
        .build();
    assertAll(
        () -> assertEquals(PLACES_WITH_PLACE_ID_1, userFeedback.placesRecommendedToUser()),
        () -> assertEquals(Optional.of(PLACE_ID_1), userFeedback.placeUserChose()),
        () -> assertEquals(false, userFeedback.userTriedAgain())
    );
  }

  @Test
  public void build_validUserTriedAgain_returnsValidUserFeedback() {
    UserFeedback userFeedback = UserFeedback.builder()
        .setPlacesRecommendedToUser(PLACES_WITH_PLACE_ID_1)
        .setUserTriedAgain(true)
        .build();
    assertAll(
        () -> assertEquals(PLACES_WITH_PLACE_ID_1, userFeedback.placesRecommendedToUser()),
        () -> assertEquals(Optional.empty(), userFeedback.placeUserChose()),
        () -> assertEquals(true, userFeedback.userTriedAgain())
    );
  }
}
