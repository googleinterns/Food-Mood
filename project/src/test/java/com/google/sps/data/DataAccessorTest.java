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
import com.google.common.collect.ImmutableList;
import com.google.maps.model.LatLng;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class DataAccessorTest {

  // The prefered cuisines to be stored in user preferences storing tests
  private static final ImmutableList<String> CUISINES = ImmutableList.of("sushi", "burger");

  // A helper that enables us to test datastore locally.
  // Has to be set up and teared down for each test.
  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  // A datastore service instance, that shall be created locally
  private final DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();

  // The tested dataAccessor, which will be initialized with a datastore service instance.
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
    Entity userEntity = new Entity(DataAccessor.USER_ENTITY_KIND, userId);
    datastoreService.put(userEntity);

    assertTrue(dataAccessor.isRegistered(userId));
  }

  @Test
  public void isRegistered_notRegistered_false() {
    String registeredUserId = "12345";
    String unRegisteredUserId = "54321";
    Entity userEntity = new Entity(DataAccessor.USER_ENTITY_KIND, registeredUserId);
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
    List<Entity> results = createPreparedQueryByUserIdKey(userId)
        .asList(FetchOptions.Builder.withDefaults());

    assertEquals(results.size(), 1);
    assertEquals(results.get(0), new Entity(DataAccessor.USER_ENTITY_KIND, userId));
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
  public void registerUser_alreadyRegistered_throwIllegalArgumentException() {
    // This test assumes that "registerUser" adds the user to the system, and makes sure that
    // the same user can't be added to the system more than once.
    String userId = "12345";
    dataAccessor.registerUser(userId);

    assertThrows(IllegalArgumentException.class, () -> dataAccessor.registerUser(userId));
  }

  @Test
  public void storeUserPreferences_validUserIdAndPreferences_userPreferencesStored() {
    String userId = "12345";

    dataAccessor.storeUserPreferences(
      userId, getValidUserPreferencesBuilder().setCuisines(CUISINES).build());
    List<Entity> results = createPreparedQueryByUserIdProperty(userId)
        .asList(FetchOptions.Builder.withDefaults());

    assertEquals(1, results.size());
    assertEquals(CUISINES, results.get(0).getProperty("preferedCuisines"));
  }

  @Test
  public void storeUserPreferences_validUserIdNoPreferredCuisines_nothingStored() {
    String userId = "12345";

    dataAccessor.storeUserPreferences(
      userId, getValidUserPreferencesBuilder().setCuisines(ImmutableList.of()).build());
    List<Entity> results = createPreparedQueryByUserIdProperty(userId)
        .asList(FetchOptions.Builder.withDefaults());

    assertEquals(0, results.size());
  }

  @Test
  public void storeUserPreferences_emptydUserId_throwIllegalArgumentException() {
    UserPreferences userPrefs = getValidUserPreferencesBuilder().setCuisines(CUISINES).build();
    assertThrows(
        IllegalArgumentException.class,
        () -> dataAccessor.storeUserPreferences("", userPrefs));
  }

  // Used to query the registered users database
  private PreparedQuery createPreparedQueryByUserIdKey(String userId) {
    Key userIdKey = KeyFactory.createKey(DataAccessor.USER_ENTITY_KIND, userId);
    Filter userIdFilter = new Query.FilterPredicate(
        Entity.KEY_RESERVED_PROPERTY,
        FilterOperator.EQUAL,
        userIdKey);
    Query query = new Query(DataAccessor.USER_ENTITY_KIND).setFilter(userIdFilter).setKeysOnly();
    return datastoreService.prepare(query);
  }

  // Used to query the user preferences database
  private PreparedQuery createPreparedQueryByUserIdProperty(String userId) {
    Filter userIdFilter = new Query.FilterPredicate(
        "userId",
        FilterOperator.EQUAL,
        userId);
    Query query = new Query(DataAccessor.PREFERNCES_ENTITY_KIND).setFilter(userIdFilter);
    return datastoreService.prepare(query);
  }

  // Returns a UserPreferences builder that has valid values of all attributes.
  private UserPreferences.Builder getValidUserPreferencesBuilder() {
    return UserPreferences.builder()
        .setMinRating(4)
        .setMaxPriceLevel(2)
        .setLocation(new LatLng(32.08, 34.78))
        .setOpenNow(true);
  }
}
