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

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Optional;
import com.google.common.collect.ImmutableList;

/**
 * A utility class for validation.
 */
public final class ValidationUtils {

  /** The maximal valid rating value. */
  private static final float MAX_RATING = 5.0f;
  /** The minimal valid rating value. */
  private static final float MIN_RATING = 1.0f;
  /** The maximal valid price level value. */
  private static final int MAX_PRICE_LEVEL = 4;
  /** The minimal valid price level value. */
  private static final int MIN_PRICE_LEVEL = 0;

  /**
   * @param rating the rating of a place, should be represented by a number between 1-5.
   * @throws IllegalArgumentException
   */
  public static void validateRating(float rating) throws IllegalArgumentException {
    checkArgument(rating >= MIN_RATING && rating <= MAX_RATING,
        "Rating should be between %s-%s", MIN_RATING, MAX_RATING);
  }

  /**
   * @param priceLevel the price level of a place, should be represented by a number between 0-4.
   * @throws IllegalArgumentException
   */
  public static void validatePriceLevel(int priceLevel) throws IllegalArgumentException {
    checkArgument(priceLevel >= MIN_PRICE_LEVEL && priceLevel <= MAX_PRICE_LEVEL,
        "Price level should be between %s-%s", MIN_PRICE_LEVEL, MAX_PRICE_LEVEL);
  }

  /**
   * @param placeUserChose the place the user chose to order from
   * @param placesRecommendedToUser the places that the system recommended to the user
   * @throws IllegalArgumentException if chosen place wasn't one of the recommended places
   */
  public static void validateChosenPlaceInReccomendedPlaces(Optional<String> placeUserChose,
      ImmutableList<String> placesRecommendedToUser) throws IllegalArgumentException {
    checkArgument(
        !placeUserChose.isPresent() || placesRecommendedToUser.contains(placeUserChose.get()),
        "Chosen place must be one of places that were recommended to the user."
    );
  }

  /**
   * @param placeUserChose the place the user chose to order from
   * @param userTriedAgain whether the user requested the system for new recommendations
   * @throws IllegalArgumentException if the user chose a place and yet requested new
   *                                  recommendations
   */
  public static void validateUserTriedAgainOnlyIfdidntchoose(Optional<String> placeUserChose,
      boolean userTriedAgain) throws IllegalArgumentException {
    checkArgument(
        !(placeUserChose.isPresent() && userTriedAgain),
        "User can't both try again and choose a place."
    );
  }

  private ValidationUtils() {
  }
}
