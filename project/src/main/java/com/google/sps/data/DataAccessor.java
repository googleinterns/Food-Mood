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
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;

public class DataAccessor {

  private final DatastoreService datastoreService;
  @VisibleForTesting
  static final String USER_ENTITY_NAME = "User";

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
  * @param userId the id of the user that we want to check the registration status about
  * @return whether the user is registered to our system, by checking whether their id was
  *     previously added to datastore.
  */
  public boolean isRegistered(String userId) {
    ValidationUtils.validateNonEmptyString(userId, "Invalid user ID.");
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
  * @param userId the id of the user that we want to register to our system
  */
  public void registerUser(String userId) {
    ValidationUtils.validateNonEmptyString(userId, "Invalid user ID.");
    checkArgument(!isRegistered(userId), "User already registered.");
    Entity userEntity = new Entity(USER_ENTITY_NAME, userId);
    datastoreService.put(userEntity);
  }
}
