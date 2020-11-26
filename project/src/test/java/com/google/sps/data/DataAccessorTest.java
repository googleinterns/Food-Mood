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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class DataAccessorTest {

  private final DatastoreService datastoreServise = mock(DatastoreService.class);
  private DataAccessor userDataAccessor;

  @Before
  public void setUp() {
    userDataAccessor = new DataAccessor(datastoreServise);
  }

  @Test
  public void isRegisteredId_registered_true() {
    boolean result =  userDataAccessor.isRegisteredId("12345");
    // TODO
  }

  @Test
  public void isRegisteredId_notRegistered_false() {
    // TODO
  }

  @Test
  // TODO: other options for test - get what the function received as input and check it (possible
  // without powermock?) Is there a different way to check this?
  public void registerUserId_validUser_registerUser() {
    userDataAccessor.registerUserId("12345");

    verify(datastoreServise).put(any(Entity.class));
  }

  @Test
  public void registerUserId_invalidUser_notRegistering() {
    userDataAccessor.registerUserId(null);
    userDataAccessor.registerUserId("");

    verify(datastoreServise, never()).put(any(Entity.class));
  }
}
