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
import java.util.Optional;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.sps.data.DataAccessor;
import com.google.sps.data.UserFeedback;
import com.google.sps.data.UserVerifier;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

/** A servlet that registers a user, according to a given token. */
@WebServlet("/register")
@SuppressWarnings("serial")
public final class FeedbackServlet extends HttpServlet {

  private UserVerifier userVerifier;
  private DataAccessor dataAccessor;
  @VisibleForTesting // Applies to the following variables
  static final String INVALID_TOKEN_MSG =
      "No user ID token was received, so can't process user feedback.";
  static final String NO_USER_TOKEN_MSG =
      "Didn't manage to get data for given user ID token, so can't process user feedback.";
  static final String INVALID_USER_FEEDBACK_MSG =
      "The user feedback input was invalid.";


  @Override
  public void init() {
    this.userVerifier = UserVerifier.create(System.getenv("CLIENT_ID"));
    this.dataAccessor = new DataAccessor();
  }

  @VisibleForTesting
  void init(UserVerifier userVerifier, DataAccessor dataAccessor) {
    this.userVerifier = userVerifier;
    this.dataAccessor = dataAccessor;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String userIdToken = request.getParameter("idToken");
    if (isNullOrEmpty(userIdToken)) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, INVALID_TOKEN_MSG);
      return;
    }
    Optional<String> optionalUserId = userVerifier.getUserIdByToken(userIdToken);
    if (!optionalUserId.isPresent()) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND, NO_USER_TOKEN_MSG);
      return;
    }
    try {
      UserFeedback feedback = UserFeedback.builder()
          .setUserId(optionalUserId.get())
          .setRecommendedPlaces(
              ImmutableList.copyOf(request.getParameter("recommendedPlaces").split(",")))
          .setChosenPlace(request.getParameter("chosenPlace"))
          .setUserTriedAgain(request.getParameter("tryAgain").equals("true"))
          .build();
      dataAccessor.updateUserFeedback(feedback);
    } catch (IllegalArgumentException e) {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, INVALID_USER_FEEDBACK_MSG);
    }
  }
}
