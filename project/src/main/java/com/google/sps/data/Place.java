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
import com.google.auto.value.AutoValue;
import com.google.maps.model.LatLng;

/**
 * Represents a place that food can be ordered from in the food-mood
 * web application.
 */
@AutoValue
public abstract class Place {

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
   * @return the coordinates of the physical place.
   */
  public abstract LatLng location();

  /**
   * @return the URL of the place’s google page.
   */
  public abstract String googleUrl();

  /**
    * @return the place's placeId as defiened on Google Places.
    */
  public abstract String placeId();

  /**
    * @return the place's business status.
    */
  public abstract BusinessStatus businessStatus();

  /**
    * @return the place's cuisines.
    */
  public abstract ImmutableList<String> cuisines();

  /**
   * @return a builder that enables to build a new Place object.
   */
  public static Builder builder() {
    return new AutoValue_Place.Builder();
  }

  /**
   * @return a builder that enables to continue building an existing Place object.
   */
  public abstract Builder toBuilder();

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
     * @param location the coordinates of the physical place
     * @return a Place builder that enables to continue building
     */
    public abstract Builder setLocation(LatLng location);

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
     * @param googleUrl the URL of the place’s google page.
     * @return a Place builder that enables to continue building
     */
    public abstract Builder setGoogleUrl(String googleUrl);

    /**
     * @param placeId the place's placeId as defiened on Google Places.
     * @return a Place builder that enables to continue building
     */
    public abstract Builder setPlaceId(String placeId);

    /**
     * @param businessStatus the place's business status.
     * @return a Place builder that enables to continue building
     */
    public abstract Builder setBusinessStatus(BusinessStatus businessStatus);

    // Builds the list of cuisines that a place holds.
    protected abstract ImmutableList.Builder<String> cuisinesBuilder();

    /**
     * @param cuisines the place's cuisines.
     * @return a Place builder that enables to continue building
     */
    public Builder addCuisine(String cuisine) {
      cuisinesBuilder().add(cuisine);
      return this;
    }

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
      ValidationUtils.validateRating(place.rating());
      ValidationUtils.validatePriceLevel(place.priceLevel());
      return place;
    }
  }
}
