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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.repackaged.com.google.api.client.util.Strings;

public class DataAccessor {

  private final DatastoreService datastoreService;
  @VisibleForTesting // Applies to all the following
  static final String USER_ENTITY_NAME = "User";
  static final String RECOMMENDATION_ENTITY_KIND = "Recommendation";
  static final String USER_ID_PROPERTY_NAME = "UserId";
  static final String PLACE_ID_PROPERTY_NAME = "PlaceId";
  static final String CHOSEN_PROPERTY_NAME = "WasChosenByUser";
  static final String TRY_AGAIN_PROPERTY_NAME = "WasChosenByUser";

  /**
   * A constructor that creates a DatastoreService instance for the class.
   */
  public DataAccessor() {
    this.datastoreService = DatastoreServiceFactory.getDatastoreService();
  }

  @VisibleForTesting
  DataAccessor(DatastoreService datastore) {
    this.datastoreService = datastore;
  }

  /**
  * @param userId the ID of the user that we want to check the registration status for
  * @return whether the user is registered to our system, by checking whether their ID was
  *     previously added to datastore.
  */
  public boolean isRegistered(String userId) {
    checkArgument(!Strings.isNullOrEmpty(userId), "Invalid user ID");
    Key userIdKey = KeyFactory.createKey(USER_ENTITY_NAME, userId);
    Filter userIdFilter =
        new Query.FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.EQUAL, userIdKey);
    Query query = new Query(USER_ENTITY_NAME).setFilter(userIdFilter).setKeysOnly();
    return datastoreService.prepare(query)
        .asList(FetchOptions.Builder.withDefaults())
        .size() > 0;
  }

  /**
  * Registers the user in our system, by adding an entity that represents them to datastore.
  *
  * @param userId the ID of the user that we want to register to our system
  */
  public void registerUser(String userId) {
    checkArgument(!Strings.isNullOrEmpty(userId), "Invalid user ID");
    checkArgument(!isRegistered(userId), "User already registered.");
    Entity userEntity = new Entity(USER_ENTITY_NAME, userId);
    datastoreService.put(userEntity);
  }

  /**
  * Updates the database with information from the user feedback.
  *
  * @param feedback a UserFeedback that holds all the information about the user feedback.
  */
  public void updateUserFeedback(UserFeedback feedback) {
    for (String place : feedback.placesRecommendedToUser()) {
      Entity recommendationEntity = new Entity(RECOMMENDATION_ENTITY_KIND);
      recommendationEntity.setProperty(USER_ID_PROPERTY_NAME, feedback.userId());
      recommendationEntity.setProperty(PLACE_ID_PROPERTY_NAME, place);
      recommendationEntity.setProperty(CHOSEN_PROPERTY_NAME,
          feedback.placeUserChose().isPresent() && place == feedback.placeUserChose().get());
      recommendationEntity.setProperty(TRY_AGAIN_PROPERTY_NAME, feedback.userTriedAgain());
      datastoreService.put(recommendationEntity);
    }
  }

  /**
  * Approches the database and gets the places that were recommended to the user in the past.
  * It is possible to get only places that the user chose, according to their feedback.
  *
  * @param userId the user we want the information about.
  * @param getOnlyPlacesUserChose if this is true, the returned places would consist only of places
  *                               that the user chose, according to their feedback.
  * @return the IDs of the places that the user received recommendations about in the past.
  */
  public ImmutableList<String> getUserPlacesHistory(String userId, boolean getOnlyPlacesUserChose) {
    Filter userIdFilter =
        new Query.FilterPredicate(USER_ID_PROPERTY_NAME, FilterOperator.EQUAL, userId);
    Filter chosenPlacesFilter =
        new Query.FilterPredicate(CHOSEN_PROPERTY_NAME, FilterOperator.EQUAL, true);
    Filter filter = getOnlyPlacesUserChose
        ? CompositeFilterOperator.and(userIdFilter, chosenPlacesFilter)
        : userIdFilter;
    Query query = new Query(RECOMMENDATION_ENTITY_KIND).setFilter(filter);
    return datastoreService.prepare(query)
        .asList(FetchOptions.Builder.withDefaults())
        .stream()
        .map(entity -> (String)entity.getProperty(PLACE_ID_PROPERTY_NAME))
        .collect(ImmutableList.toImmutableList());
  }
}
