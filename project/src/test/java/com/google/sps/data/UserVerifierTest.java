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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)

public class UserVerifierTest {

  private static final GoogleIdTokenVerifier GOOGLE_VERIFIER = mock(GoogleIdTokenVerifier.class);
  private static final UserVerifier USER_VERIFIER = new UserVerifier();

  @Before
  public void setUp() {
    USER_VERIFIER.updateGoogleVerifier(GOOGLE_VERIFIER);
  }

  @Test
  public void getUserIdByToken_emptyIdToken_emptyOptional() {
    Optional<String> result = USER_VERIFIER.getUserIdByToken("");

    assertFalse(result.isPresent());
  }

  @Test
  public void getUserIdByToken_nullIdToken_emptyOptional() {
    Optional<String> result = USER_VERIFIER.getUserIdByToken(null);

    assertFalse(result.isPresent());
  }

  @Test
  public void getUserIdByToken_validIdToken_getUserId() throws Exception {
    UserVerifier spiedUserVerifier = spy(USER_VERIFIER);
    GoogleIdToken mockedToken = mock(GoogleIdToken.class);
    Payload mockedPayload = mock(Payload.class);
    String validToken = "abcde";
    String validUserId = "12345";
    when(GOOGLE_VERIFIER.verify(validToken)).thenReturn(mockedToken);
    when(mockedToken.getPayload()).thenReturn(mockedPayload);
    doReturn(Optional.of(validUserId)).when(spiedUserVerifier).getSubjectFromPayload(mockedPayload);

    assertEquals(spiedUserVerifier.getUserIdByToken(validToken), Optional.of(validUserId));
  }
}
