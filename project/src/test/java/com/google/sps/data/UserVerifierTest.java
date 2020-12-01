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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.maps.model.LatLng;

@RunWith(JUnit4.class)

public class UserVerifierTest {

  private UserVerifier verifier;

  @Before
  public void setUp() {
    GoogleIdTokenVerifier googleVerifier = mock(GoogleIdTokenVerifier.class)
    verifier = new UserVerifier(googleVerifier);
  }

  @Test
  public void getUserIdByToken_emptyIdToken_emptyOptional() {
    Optional<String> result = verifier.getUserIdByToken("");

    assertFalse(result.isPresent());
  }

  @Test
  public void getUserIdByToken_nullIdToken_emptyOptional() {
    Optional<String> result = verifier.getUserIdByToken(null);

    assertFalse(result.isPresent());
  }
}
