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
  private static final float MAX_RATING = 5.0f;
  /** The minimal valid rating value. */
  private static final float MIN_RATING = 1.0f;
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
   * @return the rating of the place, represented by a number between 1-5.
   */
  public abstract float rating();

  /**
   * @return the place’s price level, represented by a number between 0-4.
   */
  public abstract int priceLevel();

  /**
   * @return coordinate of the physical place.
   */
  public abstract double longitude();

  /**
   * @return coordinate of the physical place.
  */
  public abstract double latitude();

  /**
   * @return a builder that enables to build a new Place object
   */
  public static Builder builder() {
    return new AutoValue_Place.Builder();
  }

  /**
   * A builder class for creating Place objects.
   */
  @AutoValue.Builder
  public abstract static class Builder {
    /**
     * @param name the name of the place
     * @return a Place builder that enables to continue building
     */
    public abstract Builder setName(String name);

    /**
     * @param websiteUrl the url of the place’s website
     * @return a Place builder that enables to continue building
     */
    public abstract Builder setWebsiteUrl(String websiteUrl);

    /**
     * @param phone a phone number that can be used to contact the place
     * @return a Place builder that enables to continue building
     */
    public abstract Builder setPhone(String phone);

    /**
     * @param longitude coordinate of the physical place
     * @return a Place builder that enables to continue building
     */
    public abstract Builder setLongitude(double longitude);

    /**
     * @param latitude coordinate of the physical place
     * @return a Place builder that enables to continue building
     */
    public abstract Builder setLatitude(double latitude);

    /**
     * @param rating the rating of the place, represented by a number of 1-5
     * @return a Place builder that enables to continue building
     */
    public abstract Builder setRating(float rating);

    /**
     * @param priceLevel the place’s price level, represented by a number od 0-4.
     * @return a Place builder that enables to continue building
     */
    public abstract Builder setPriceLevel(int priceLevel);

    /**
     * Builds the Place object according to the data that was set so far.
     *
     * @return the object that was built
     */
    abstract Place autoBuild();

    /**
     * Concludes the building of a new Place instance.
     * @return the new instance.
     * @throws IllegalArgumentException if an input isn't valid (rating / price level)
     */
    public Place build() throws IllegalArgumentException {
      Place place = autoBuild();
      checkArgument(place.rating() >= MIN_RATING && place.rating() <= MAX_RATING,
          "Rating should be between %s-%s", MIN_RATING, MAX_RATING);
      checkArgument(place.priceLevel() >= MIN_PRICE_LEVEL && place.priceLevel() <= MAX_PRICE_LEVEL,
          "Price level should be between %s-%s", MIN_PRICE_LEVEL, MAX_PRICE_LEVEL);
      return place;
    }
  }
}
