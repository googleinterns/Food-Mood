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
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

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

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  private DatastoreService datastoreService;
  private DataAccessor dataAccessor;

  @Before
  public void setUp() {
      helper.setUp();
      datastoreService = DatastoreServiceFactory.getDatastoreService();
      dataAccessor = new DataAccessor(datastoreService);
  }

  @After
  public void tearDown() {
      helper.tearDown();
  }

  @Test
  public void isRegisteredId_registered_true() {
    String userId = "12345";
    Entity userEntity = new Entity(dataAccessor.userEntityName, userId);
    datastoreService.put(userEntity);

    assertTrue(dataAccessor.isRegisteredId(userId));
  }

  @Test
  public void isRegisteredId_notRegistered_false() {
    assertFalse(dataAccessor.isRegisteredId("12345"));
  }

  @Test
  public void registerUserId_validUser_registerUser() {
    String userId = "12345";

    dataAccessor.registerUserId(userId);
    PreparedQuery results = createPreparedQueryByUserId(userId);

    assertEquals(results.asList(FetchOptions.Builder.withDefaults()).size(), 1);
  }

  private PreparedQuery createPreparedQueryByUserId(String userId) {
    Key userIdKey = KeyFactory.createKey(dataAccessor.userEntityName, userId);
    Filter userIdFilter =
        new Query.FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.EQUAL, userIdKey);
    Query query = new Query(dataAccessor.userEntityName).setFilter(userIdFilter).setKeysOnly();
    return datastoreService.prepare(query);
  }

  @Test
  public void registerUserId_invalidUser_notRegistering() {
    dataAccessor.registerUserId(null);
    dataAccessor.registerUserId("");
    Query query = new Query(dataAccessor.userEntityName).setKeysOnly();

    assertEquals(
        datastoreService.prepare(query).asList(FetchOptions.Builder.withDefaults()).size(), 0);
  }

  //   import static org.mockito.ArgumentMatchers.any;
  // import static org.mockito.Mockito.mock;
  // import static org.mockito.Mockito.never;
  // import static org.mockito.Mockito.verify;
  // import static org.mockito.Mockito.when;
  // import java.util.List;
  // @Test
  // public void isRegisteredId_registered_true() {
  //   // PreparedQuery mockedPreparedQuery = mock(PreparedQuery.class);
  //   // when(datastoreService.prepare(any(Query.class))).thenReturn(mockedPreparedQuery);
  //   // when(mockedPreparedQuery.asList(any(FetchOptions.class)))
  //   //     .thenReturn(List.of(new Entity("User", "12345")));

  //   // assertTrue(dataAccessor.isRegisteredId("12345"));
  // }

  // @Test
  // public void isRegisteredId_notRegistered_false() {
  //   assertFalse(dataAccessor.isRegisteredId("12345"));
  // }

  // @Test
  // public void registerUserId_validUser_registerUser() {
  //   dataAccessor.registerUserId("12345");

  //   verify(datastoreService).put(any(Entity.class));
  // }

  // @Test
  // public void registerUserId_invalidUser_notRegistering() {
  //   dataAccessor.registerUserId(null);
  //   dataAccessor.registerUserId("");

  //   verify(datastoreService, never()).put(any(Entity.class));
  // }
}
