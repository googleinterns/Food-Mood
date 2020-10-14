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

import com.google.auto.value.AutoValue;
import com.google.maps.model.LatLng;

import java.util.List;

/**
 * Represents the prefrences entered by a user
 */
@AutoValue
public abstract class UserPrefrences {

  /**
   * @return the minimal rating that the user wants to consider,
   * represented by a number between 1-5.
   */
  public abstract float minRating();

  /**
   * @return the maximal range of prices the user wants to consider,
   * represented by a number between 0-4.
   */
  public abstract int maxPriceLevel();

  /**
   * @return the coordinates of the location the user wants the food to be delivered to.
  */
  public abstract LatLng location();

  /**
  * @return the cuisine types that the user prefers.
  */
  public abstract List<String> cuisineTypes();

  /**
   * @return a builder that enables to build a new Place object
   */
  public static Builder builder() {
    return new AutoValue_UserPrefrences.Builder();
  }


/**
   * A builder class for creating UserPrefrences objects.
   */
  @AutoValue.Builder
  public abstract static class Builder {
    /**
     * @param minRating the minimum rating that the user wants to consider,
     *      represented by a number between 1-5
     * @return a Place builder that enables to continue building
     */
    public abstract Builder setMinRating(float minRating);

    /**
     * @param maxPriceLevel the maximal range of prices the user wants to consider,
     *      represented by a number between 0-4
     * @return a Place builder that enables to continue building
     */
    public abstract Builder setMaxPriceLevel(int maxPriceLevel);

    /**
     * @param location the coordinates of the location the user wants the food to be delivered to
     * @return a Place builder that enables to continue building
     */
    public abstract Builder setLocation(LatLng location);

    /**
     * @param cuisineTypes the cuisine types that the user prefers
     * @return a Place builder that enables to continue building
     */
    public abstract Builder setCuisineTypes(List<String> cuisineTypes);

    /**
     * Builds the UserPrefrences object according to the data that was set so far.
     *
     * @return the object that was built
     */
    abstract UserPrefrences autoBuild();

    /**
     * Concludes the building of a new UserPrefrences instance.
     * @return the new instance.
     * @throws IllegalArgumentException if an input isn't valid (rating / price level)
     */
    public UserPrefrences build() throws IllegalArgumentException {
      UserPrefrences prefrences = autoBuild();
      ValidationUtils.validateRating(prefrences.minRating());
      ValidationUtils.validatePriceLevel(prefrences.maxPriceLevel());
      return prefrences;
    }
  }
}

