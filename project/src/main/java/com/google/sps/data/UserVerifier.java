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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Optional;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.common.annotations.VisibleForTesting;
import com.google.api.client.http.javanet.NetHttpTransport;

public class UserVerifier {

  private GoogleIdTokenVerifier verifier;

  public UserVerifier(String clientId) {
    this.verifier = new GoogleIdTokenVerifier
    .Builder(new NetHttpTransport(), new JacksonFactory())
    .setAudience(Collections.singletonList(clientId))
    .build();
  }

  @VisibleForTesting
  UserVerifier(GoogleIdTokenVerifier googleVerifier) {
    this.verifier = googleVerifier;
  }

  /**
   * Autheticates the given token using Google's verifier, and returns an Optional that holds
   * the user's id, if the process was successful.
   *
   * @param id_token the token of the user
   * @return an optional that holds the verified user ID, or null if the process wasn't successful
   */
  public Optional<String> getUserIdByToken(String id_token) {
    GoogleIdToken idToken;
    try {
      idToken = verifier.verify(id_token);
    } catch (GeneralSecurityException | IOException e) {
      return Optional.empty();
    }
    return idToken != null ? Optional.of(idToken.getPayload().getSubject()) : Optional.empty();
  }
}
