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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class DataAccessorTest {

  // A helper that enables us to test datastore locally.
  // Has to be set up and teared down for each test.
  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  // A datastore service instance, that shall be created locally
  private final DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();

  // The tested dataaccessor, which will be initialized with a datastore service instance.
  private DataAccessor dataAccessor;

  @Before
  public void setUp() {
      helper.setUp();
      dataAccessor = new DataAccessor(datastoreService);
  }

  @After
  public void tearDown() {
      helper.tearDown();
  }

  @Test
  public void isRegistered_registered_true() {
    String userId = "12345";
    Entity userEntity = new Entity(DataAccessor.userEntityName, userId);
    datastoreService.put(userEntity);

    assertTrue(dataAccessor.isRegistered(userId));
  }

  @Test
  public void isRegistered_notRegistered_false() {
    String registeredUserId = "12345";
    String unRegisteredUserId = "54321";
    Entity userEntity = new Entity(DataAccessor.userEntityName, registeredUserId);
    datastoreService.put(userEntity);

    assertFalse(dataAccessor.isRegistered(unRegisteredUserId));
  }

  @Test
  public void isRegistered_emptyUserId_throwIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class, () -> dataAccessor.isRegistered(""));
  }

  @Test
  public void isRegistered_nullUserId_throwIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class, () -> dataAccessor.isRegistered(null));
  }

  @Test
  public void registerUser_validUserId_registerUser() {
    String userId = "12345";

    dataAccessor.registerUser(userId);
    List<Entity> results = createPreparedQueryByUserId(userId)
        .asList(FetchOptions.Builder.withDefaults());

    assertEquals(results.size(), 1);
    assertEquals(results.get(0), new Entity(DataAccessor.userEntityName, userId));
  }

  @Test
  public void registerUser_emptydUserId_throwIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class, () -> dataAccessor.registerUser(""));
  }

  @Test
  public void registerUser_nullUserId_throwIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class, () -> dataAccessor.registerUser(null));
  }

  @Test
  public void registerUser_alreadyRegistered_dontRegisterAgain() {
    String userId = "12345";
    dataAccessor.registerUser(userId);
    List<Entity> firstTime = createPreparedQueryByUserId(userId)
        .asList(FetchOptions.Builder.withDefaults());

    dataAccessor.registerUser(userId); // Trying to register a user we already registered
    List<Entity> secondTime = createPreparedQueryByUserId(userId)
        .asList(FetchOptions.Builder.withDefaults());

    assertEquals(firstTime.size(), 1);
    assertEquals(firstTime.get(0), new Entity(DataAccessor.userEntityName, userId));
    assertEquals(secondTime.size(), 1);
    assertEquals(firstTime.get(0), new Entity(DataAccessor.userEntityName, userId));
  }

  private PreparedQuery createPreparedQueryByUserId(String userId) {
    Key userIdKey = KeyFactory.createKey(DataAccessor.userEntityName, userId);
    Filter userIdFilter = new Query.FilterPredicate(
        Entity.KEY_RESERVED_PROPERTY,
        FilterOperator.EQUAL,
        userIdKey);
    Query query = new Query(DataAccessor.userEntityName).setFilter(userIdFilter).setKeysOnly();
    return datastoreService.prepare(query);
  }
}
