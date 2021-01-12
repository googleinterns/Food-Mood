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

package com.google.sps.servlets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.sps.data.DataAccessor;
import com.google.sps.data.UserFeedback;
import com.google.sps.data.UserVerifier;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentMatcher;

@RunWith(JUnit4.class)
public class FeedbackServletTest {

  private static final HttpServletRequest REQUEST = mock(HttpServletRequest.class);
  private static final HttpServletResponse RESPONSE = mock(HttpServletResponse.class);
  private static final String ID_TOKEN = "abcde";
  private static final String USER_ID = "12345";
  private static UserVerifier mockUserVerifier;
  private static DataAccessor mockDataAccessor;
  private FeedbackServlet servlet;

  @Before
  public void setUp() throws Exception {
    mockUserVerifier = mock(UserVerifier.class);
    mockDataAccessor = mock(DataAccessor.class);
    servlet = new FeedbackServlet();
    servlet.init(mockUserVerifier, mockDataAccessor);
  }

  @Test
  public void doPost_validToken_updatesFeedback() throws Exception {
    mockValidRequestParams(ID_TOKEN,
        "place1,place2,place3" /* recommendedPlaces */,
        "place1" /* chosenPlace */,
        "false" /* tryAgain */);
    when(mockUserVerifier.getUserIdByToken(ID_TOKEN)).thenReturn(Optional.of(USER_ID));

    servlet.doPost(REQUEST, RESPONSE);

    // The parameters here must match those that are mocked in the request.
    // We can't compare instances of UserFeedback, because of the "feedbackTimeInMillis" attribute.
    verify(mockDataAccessor).updateUserFeedback(argThat(feedback -> {
      return feedback != null
          && feedback.userId().equals(USER_ID)
          && feedback.recommendedPlaces().equals(ImmutableList.of("place1","place2","place3"))
          && feedback.chosenPlace().equals(Optional.of("place1"))
          && feedback.userTriedAgain() == false;
    }));
  }

  @Test
  public void doPost_invalidToken_badRequestResponseSent() throws Exception {
    mockValidRequestParams();
    when(REQUEST.getParameter("idToken")).thenReturn(null);

    servlet.doPost(REQUEST, RESPONSE);

    verify(mockDataAccessor, never()).updateUserFeedback(any(UserFeedback.class));
    verify(RESPONSE)
        .sendError(HttpServletResponse.SC_BAD_REQUEST,FeedbackServlet.INVALID_TOKEN_MSG);
  }

  @Test
  public void doPost_getUserByTokenFails_badRequestResponseSent() throws Exception {
    mockValidRequestParams();
    when(mockUserVerifier.getUserIdByToken(ID_TOKEN)).thenReturn(Optional.empty());

    servlet.doPost(REQUEST, RESPONSE);

    verify(mockDataAccessor, never()).updateUserFeedback(any(UserFeedback.class));
    verify(RESPONSE, atLeastOnce())
        .sendError(HttpServletResponse.SC_NOT_FOUND, FeedbackServlet.NO_USER_TOKEN_MSG);
  }

  @Test
  public void doPost_invalidUserFeedback_badRequestResponseSent() throws Exception {
    // Chosen place not in recommended places
    mockValidRequestParams(ID_TOKEN,
        "place1,place2,place3" /* recommendedPlaces */,
        "place4" /* chosenPlace */,
        "true" /* tryAgain */);
    when(mockUserVerifier.getUserIdByToken(ID_TOKEN)).thenReturn(Optional.of(USER_ID));

    servlet.doPost(REQUEST, RESPONSE);

    verify(mockDataAccessor, never()).updateUserFeedback(any(UserFeedback.class));
    verify(RESPONSE, atLeastOnce())
        .sendError(HttpServletResponse.SC_BAD_REQUEST, FeedbackServlet.INVALID_USER_FEEDBACK_MSG);
  }

  private void mockValidRequestParams() {
    when(REQUEST.getParameter("idToken")).thenReturn(ID_TOKEN);
    when(REQUEST.getParameter("recommendedPlaces")).thenReturn("place1,place2,place3");
    when(REQUEST.getParameter("chosenPlace")).thenReturn("place1");
    when(REQUEST.getParameter("tryAgain")).thenReturn("false");
  }

  private void mockValidRequestParams(String idToken, String recommendedPlaces, String chosenPlace,
      String tryAgain ) {
    when(REQUEST.getParameter("idToken")).thenReturn(idToken);
    when(REQUEST.getParameter("recommendedPlaces")).thenReturn(recommendedPlaces);
    when(REQUEST.getParameter("chosenPlace")).thenReturn(chosenPlace);
    when(REQUEST.getParameter("tryAgain")).thenReturn(tryAgain);
  }
}
