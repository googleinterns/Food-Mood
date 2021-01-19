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
// limitations under the License.import java.io.IOException;

package com.google.sps.servlets;

import static com.google.common.base.Strings.isNullOrEmpty;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.sps.data.UserVerifier;
import java.util.Optional;

/**
 * A utility class for validating user tokens.
 */
public final class TokenValidator {

  /**
   * This function is meant for validating HTTP servlet requests that require using the user's
   * ID token (request parameter "idToken"). It verifies the token and returns the matching user ID.
   * If the token isn't valid, the given response parameter is modified in order to send an error.
   *
   * @param request the HttpServletRequest that we want to check
   * @param response the HttpServletResponse that is updated in case of invalidity
   * @param userVerifier the verifier that is used for verifing the user token
   * @param validationPurpose the reason for turning the token to user ID
   * @throws IllegalArgumentException if the given request has missing inputs (no token)
   * @throws IOException if updating the response encounters a problem
   * @return An Optional with the valid ID if the token is valid, an empty Optional otherwise
   */
  public static Optional<String> validateAndGetId(HttpServletRequest request, HttpServletResponse
      response, UserVerifier userVerifier, String validationPurpose)
      throws IllegalArgumentException, IOException {
    String userIdToken = request.getParameter("idToken");
    if (isNullOrEmpty(userIdToken)) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST,
          "No user token was received (" + validationPurpose + ")");
      return Optional.empty();
    }
    Optional<String> optionalUserId = userVerifier.getUserIdByToken(userIdToken);
    if (!optionalUserId.isPresent()) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND,
          "Couldn't verify user token (" + validationPurpose + ")");
      return Optional.empty();
    }
    return optionalUserId;
  }

  private TokenValidator() { }
}
