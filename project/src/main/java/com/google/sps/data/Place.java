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

import com.google.auto.value.AutoValue;
import static com.google.appengine.repackaged.com.google.common.base.Preconditions.checkArgument;

/**
 * Represents a place that food can be ordered from in the food-mood
 * web application.
 */
@AutoValue
public abstract class Place {

  /** The maximal valid rating value. */
  private static final int MAX_RATING = 5;
  /** The minimal valid rating value. */
  private static final int MIN_RATING = 1;
  /** The maximal valid price level value. */
  private static final int MAX_PRICE_LEVEL = 4;
  /** The minimal valid price level value. */
  private static final int MIN_PRICE_LEVEL = 0;

  /**
   * @return the name of the place.
   */
  public abstract String name();

  /**
   * @return the url of the place’s website.
   */
  public abstract String websiteUrl();

  /**
   * @return a phone number that can be used to contact the place.
   */
  public abstract String phone();

  /**
   * @return The rating of the place, represented by a number between 1-5.
   */
  public abstract int rating();

  /**
   * @return the place’s price level, represented by a number between 0-4.
   */
  public abstract int priceLevel();

  /**
   * @return Coordinate of the physical place.
   */
  public abstract double longitude();

  /**
   * @return Coordinate of the physical place.
  */
  public abstract double latitude();

  /**
   * Creates a new Place instance.
   * @param name The name of the place.
   * @param websiteUrl The url of the place’s website.
   * @param phone A phone number that can be used to contact the place.
   * @param rating The rating of the place, represented by a number of 1-5.
   * @param priceLevel The place’s price level, represented by a number od 0-4.
   * @param longitude Coordinate of the physical place.
   * @param latitude Coordinate of the physical place.
   * @return the new object instance.
   * @throws IllegalArgumentException if an input isn't valid (rating / price level)
   */
  public static Place create(String name, String websiteUrl, String phone,
      int rating, int priceLevel, double longitude, double latitude)
      throws IllegalArgumentException {
      checkArgument(rating >= MIN_RATING && rating <= MAX_RATING, "Rating should be between %s-%s",
          MIN_RATING, MAX_RATING);
      checkArgument(priceLevel >= MIN_PRICE_LEVEL && priceLevel <= MAX_PRICE_LEVEL,
          "Price level should be between %s-%s", MIN_PRICE_LEVEL, MAX_PRICE_LEVEL);
      return new AutoValue_Place(name, websiteUrl, phone, rating, priceLevel, longitude, latitude);
  }
}
