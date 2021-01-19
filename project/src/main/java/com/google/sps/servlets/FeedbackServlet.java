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

/** A servlet that is responsible for storing users' feedback. */
@WebServlet("/feedback")
@SuppressWarnings("serial")
public final class FeedbackServlet extends HttpServlet {

  private UserVerifier userVerifier;
  private DataAccessor dataAccessor;

  @Override
  public void init() {
    this.userVerifier = UserVerifier.create(System.getenv("CLIENT_ID"));
    this.dataAccessor = new DataAccessor();
  }

  @VisibleForTesting
  void init(UserVerifier verifier, DataAccessor accessor) {
    this.userVerifier = verifier;
    this.dataAccessor = accessor;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Optional<String> optionalUserId = TokenValidator.validateAndGetId(
          request, response, userVerifier, "store user feedback" /* validationPurpose */);
    if (!optionalUserId.isPresent()) {
      return;
    }
    try {
      UserFeedback.Builder feedback = UserFeedback.builder()
          .setUserId(optionalUserId.get())
          .setRecommendedPlaces(
              ImmutableList.copyOf(request.getParameter("recommendedPlaces").split(",")))
          .setUserTriedAgain(request.getParameter("tryAgain").equals("true"));
      String chosenPlace = request.getParameter("chosenPlace");
      if (!isNullOrEmpty(chosenPlace)) {
        feedback.setChosenPlace(chosenPlace);
      }
      dataAccessor.updateUserFeedback(feedback.build());
    } catch (IllegalArgumentException e) {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid user feedback input.");
    }
  }
}
