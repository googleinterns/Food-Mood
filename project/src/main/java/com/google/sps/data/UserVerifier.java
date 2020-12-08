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

  /**
   * A constructor that takes in a GoogleIdTokenVerifier object.
   * @param googleVerifier a Google tool for verifying the validity of Google users' tokens
   */
  @VisibleForTesting
  UserVerifier(GoogleIdTokenVerifier googleVerifier) {
    this.verifier = googleVerifier;
  }

  /**
   * @param clientId A Google API client ID for the Google Sign-In services
   * @return A UserVerifier instance
   */
  public static UserVerifier create(String clientId) {
    return new UserVerifier(
        new GoogleIdTokenVerifier
            .Builder(new NetHttpTransport(), new JacksonFactory())
            .setAudience(Collections.singletonList(clientId))
            .build()
    );
  }

  /**
   * Autheticates the given token using Google's verifier, and returns an Optional that holds
   * the user's id, if the process was successful.
   *
   * @param idToken the token of the user
   * @return an optional that holds the verified user ID, or null if the process wasn't successful
   */
  public Optional<String> getUserIdByToken(String idToken) {
    GoogleIdToken googleIdToken;
    try {
      googleIdToken = verifier.verify(idToken);
    } catch (GeneralSecurityException | IOException e) {
      return Optional.empty();
    }
    return googleIdToken != null
        ? Optional.of(googleIdToken.getPayload().getSubject())
        : Optional.empty();
  }
}
