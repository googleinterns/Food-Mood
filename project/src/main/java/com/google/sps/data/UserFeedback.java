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

import java.util.Optional;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

/**
 * Represents the feedback that users enter about the recommendations they were given.
 */
@AutoValue
public abstract class UserFeedback {

  /** @return a list of IDs of the places that were recommended to the user. */
  public abstract ImmutableList<String> recommendedPlacesIds();

  /** @return the ID of the place the user chose, if any. */
  public abstract Optional<String> chosenPlaceId();

  /**
  * @return true if the user requested to try again and get more recommendations (implying they
  * weren't satisfied with the recommenrations he recieved).
  */
  public abstract boolean triedAgain();


  /** @return a builder that enables to build a new UserFeedback object. */
  public static Builder builder() {
    return new AutoValue_UserFeedback.Builder();
  }

  /** A builder class for creating a UserFeedback objects. */
  @AutoValue.Builder
  public abstract static class Builder {
    /**
     * @param recommendedPlacesIds a list of IDs of the places that were recommended to the user.
     * @return a UserFeedback builder that enables to continue building.
     */
    public abstract Builder setRecommendedPlacesIds(ImmutableList<String> recommendedPlacesIds);

    /**
     * @param chosenPlaceId the ID of the place the user chose, if any.
     * @return a UserFeedback builder that enables to continue building.
     */
    public abstract Builder setChosenPlaceId(Optional<String> chosenPlaceId);

    /**
     * @param triedAgain true if the user requested to try again and get more recommendations
     * @return a UserFeedback builder that enables to continue building
     */
    public abstract Builder setTriedAgain(boolean triedAgain);

    /**
     * Builds the UserFeedback object according to the data that was set so far.
     *
     * @return the object that was built
     */
    abstract UserFeedback autoBuild();

    /**
     * Concludes the building of a new UserFeedback instance.
     *
     * @return the new instance.
     * @throws IllegalArgumentException if an input isn't valid
     */
    public UserFeedback build() throws IllegalArgumentException {
      UserFeedback feedback = autoBuild();
      ValidationUtils.validateChosenPlaceInReccomendedPlaces(feedback.chosenPlaceId(),
          feedback.recommendedPlacesIds());
      return feedback;
    }
  }
}
