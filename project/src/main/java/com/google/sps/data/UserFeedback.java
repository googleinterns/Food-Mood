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

import static com.google.sps.data.ValidationUtils.validateNonEmptyString;
import static com.google.sps.data.ValidationUtils.validateChosenPlaceInReccomendedPlaces;;

import java.util.Optional;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

/**
 * Represents the input feedback a user provides for a Food-Mood recommendation.
 */
@AutoValue
public abstract class UserFeedback {

  /** @return the user thst supplied the feedback. */
  public abstract String userId();

  /** @return the time in which the feedback was given, in milliseconds. */
  public abstract long feedbackTimeInMillis();

  /** @return the list of places IDs that were recommended to the user. */
  public abstract ImmutableList<String> recommendedPlaces();

  /** @return the ID of the place the user chose, if any. */
  public abstract Optional<String> chosenPlace();

  /**
  * @return true if the user requested to try again and get more recommendations (implying they
  * weren't satisfied with the recommendations they recieved).
  */
  public abstract boolean userTriedAgain();


  /** @return a builder that enables to build a new UserFeedback object. */
  public static Builder builder() {
    return new AutoValue_UserFeedback.Builder();
  }

  /** A builder class for creating a UserFeedback objects. */
  @AutoValue.Builder
  public abstract static class Builder {

    /**
     * @param userId the user thst supplied the feedback.
     * @return a UserFeedback builder that enables to continue building.
     */
    public abstract Builder setUserId(String userId);

    /**
     * @param feedbackTimeInMillis the time in which the feedback was given, in milliseconds.
     * @return a UserFeedback builder that enables to continue building.
     */
    public abstract Builder setFeedbackTimeInMillis(long feedbackTimeInMillis);

    /**
     * @param placesRecommendedToUser a list of IDs of the places that were recommended to the user.
     * @return a UserFeedback builder that enables to continue building.
     */
    public abstract Builder setRecommendedPlaces(ImmutableList<String> recommendedPlaces);

    /**
     * @param chosenPlaceId the ID of the place the user chose, if any.
     * @return a UserFeedback builder that enables to continue building.
     */
    public abstract Builder setChosenPlace(String chosenPlace);

    /**
     * @param triedAgain true if the user requested to try again and get more recommendations
     * @return a UserFeedback builder that enables to continue building
     */
    public abstract Builder setUserTriedAgain(boolean userTriedAgain);

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
     * @throws IllegalArgumentException if the user feedback isn't consistent.
     */
    public UserFeedback build() throws IllegalArgumentException {
      // The feedback time is set to be the time in which the object is created.
      setFeedbackTimeInMillis(System.currentTimeMillis());
      UserFeedback feedback = autoBuild();
      validateChosenPlaceInReccomendedPlaces(feedback.chosenPlace(),
          feedback.recommendedPlaces());
      validateNonEmptyString(feedback.userId(), "User ID cannot be empty.");
      return feedback;
    }
  }
}
