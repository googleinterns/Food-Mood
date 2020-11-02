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
import com.google.common.collect.ImmutableList;
import com.google.maps.model.LatLng;

/**
 * Represents the preferences entered by a user.
 */
@AutoValue
public abstract class UserPreferences {

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
  public abstract ImmutableList<String> cuisines();

  /**
  * @return true if the user wants the place to be currently open.
  */
  public abstract boolean openNow();


  /**
   * @return a builder that enables to build a new Place object
   */
  public static Builder builder() {
    return new AutoValue_UserPreferences.Builder();
  }

  /**
   * A builder class for creating UserPreferences objects.
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
     * @param cuisines the cuisine types that the user prefers
     * @return a Place builder that enables to continue building
     */
    public abstract Builder setCuisines(ImmutableList<String> cuisines);

    /**
     * @param openNow a boolean representing whether the users wants the place to be currently open
     * @return a Place builder that enables to continue building
     */
    public abstract Builder setOpenNow(boolean openNow);

    /**
     * Builds the UserPreferences object according to the data that was set so far.
     *
     * @return the object that was built
     */
    abstract UserPreferences autoBuild();

    /**
     * Concludes the building of a new UserPreferences instance.
     * @return the new instance.
     * @throws IllegalArgumentException if an input isn't valid (rating / price level)
     */
    public UserPreferences build() throws IllegalArgumentException {
      UserPreferences preferences = autoBuild();
      ValidationUtils.validateRating(preferences.minRating());
      ValidationUtils.validatePriceLevel(preferences.maxPriceLevel());
      return preferences;
    }
  }
}

